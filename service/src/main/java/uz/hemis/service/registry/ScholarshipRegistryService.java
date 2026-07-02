package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.ScholarshipDetailDto;
import uz.hemis.service.registry.dto.ScholarshipDictionariesDto;
import uz.hemis.service.registry.dto.ScholarshipRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Scholarship Registry Service - READ-ONLY business logic for the Scholarships
 * (Stipendiyalar) registry. Source table {@code hemishe_e_student_scholarship_full}
 * read via {@link EntityManager} native queries.
 *
 * <p>The physical table has NO {@code active} column (confirmed against the CUBA
 * {@code EStudentScholarshipFull} entity), so {@code active} is derived in SQL:
 * {@code (sc.end_date IS NULL OR sc.end_date >= CURRENT_DATE)}.</p>
 *
 * <p>Soft-delete enforced with explicit {@code AND sc.delete_ts IS NULL}. University and
 * student LEFT-JOINed with their own {@code delete_ts IS NULL} guard. PINFL never exposed.
 * Monthly amounts (from {@code hemishe_e_student_scholarship_amount}) are loaded only in the
 * detail response.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScholarshipRegistryService {

    private final EntityManager entityManager;

    private static final String FULL_NAME = "CONCAT_WS(' ', s.lastname, s.firstname, s.fathername)";

    private static final String ACTIVE_EXPR = "(sc.end_date IS NULL OR sc.end_date >= CURRENT_DATE)";

    private static final String BASE_FROM =
        "FROM hemishe_e_student_scholarship_full sc " +
        "LEFT JOIN hemishe_e_university u ON u.code = sc._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_e_student s ON s.id = sc._student AND s.delete_ts IS NULL ";

    /**
     * Paged scholarship list.
     *
     * @param q              search (student full name / decree)
     * @param universityCode filter by university code
     * @param educationYear  filter by education-year classifier code
     * @param stipendCategory filter by stipend-category classifier code
     * @param pageable       pagination
     */
    public Page<ScholarshipRowDto> getScholarships(String q, String universityCode, String educationYear,
                                                   String stipendCategory, Pageable pageable) {
        log.debug("Getting scholarships: q={}, universityCode={}, educationYear={}, stipendCategory={}, page={}",
                  q, universityCode, educationYear, stipendCategory, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, educationYear, stipendCategory, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql =
            "SELECT sc.id, sc._student, " + FULL_NAME + ", sc._university, u.name, " +
            "  sc._education_year, sc.semester_number, sc._stipend_category, sc._stipend_type, " +
            "  sc._payment_form, sc.decree, sc.start_date, sc.end_date, " + ACTIVE_EXPR + " " +
            BASE_FROM + where +
            "ORDER BY sc.start_date DESC NULLS LAST, sc.id " +
            "LIMIT ? OFFSET ?";

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<ScholarshipRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Single scholarship detail by id (UUID), including the monthly amounts child list.
     * Returns empty if not found or id is not a valid UUID.
     */
    public Optional<ScholarshipDetailDto> getScholarshipDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql =
            "SELECT sc.id, sc._student, " + FULL_NAME + ", sc._university, u.name, " +
            "  sc._education_year, sc.semester_number, sc._stipend_category, sc._stipend_type, " +
            "  sc._payment_form, sc.decree, sc.start_date, sc.end_date, " + ACTIVE_EXPR + ", " +
            "  sc._education_type, sc._education_form, sc._semester " +
            BASE_FROM +
            "WHERE sc.delete_ts IS NULL AND sc.id = ?";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        List<ScholarshipDetailDto.AmountItem> amounts = loadAmounts(uuid);
        return Optional.of(new ScholarshipDetailDto(
            str(r[0]), str(r[1]), (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], (String) r[6], (String) r[7], (String) r[8],
            (String) r[9], (String) r[10], toLocalDate(r[11]), toLocalDate(r[12]), toBool(r[13]),
            (String) r[14], (String) r[15], (String) r[16], amounts
        ));
    }

    /** Filter dictionaries (cached). */
    @Cacheable(value = "scholarshipsDictionaries", key = "'all'")
    public ScholarshipDictionariesDto getDictionaries() {
        log.debug("Loading scholarship dictionaries");
        return new ScholarshipDictionariesDto(loadUniversities(), loadEducationYears());
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String educationYear,
                              String stipendCategory, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE sc.delete_ts IS NULL ");
        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (searchLike != null) {
            where.append("AND (LOWER(" + FULL_NAME + ") LIKE ? OR LOWER(sc.decree) LIKE ?) ");
            params.add(searchLike);
            params.add(searchLike);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND sc._university = ? ");
            params.add(universityCode.trim());
        }
        if (educationYear != null && !educationYear.isBlank()) {
            where.append("AND sc._education_year = ? ");
            params.add(educationYear.trim());
        }
        if (stipendCategory != null && !stipendCategory.isBlank()) {
            where.append("AND sc._stipend_category = ? ");
            params.add(stipendCategory.trim());
        }
        return where.toString();
    }

    private ScholarshipRowDto mapRow(Object[] r) {
        return new ScholarshipRowDto(
            str(r[0]), str(r[1]), (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], (String) r[6], (String) r[7], (String) r[8],
            (String) r[9], (String) r[10], toLocalDate(r[11]), toLocalDate(r[12]), toBool(r[13])
        );
    }

    private List<ScholarshipDetailDto.AmountItem> loadAmounts(UUID scholarshipId) {
        Query query = entityManager.createNativeQuery(
            "SELECT month_, summa FROM hemishe_e_student_scholarship_amount " +
            "WHERE delete_ts IS NULL AND _student_scholarship = ? ORDER BY month_");
        query.setParameter(1, scholarshipId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new ScholarshipDetailDto.AmountItem(
                toLocalDate(row[0]),
                row[1] != null ? ((Number) row[1]).doubleValue() : null))
            .collect(Collectors.toList());
    }

    private List<ScholarshipDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new ScholarshipDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<ScholarshipDictionariesDto.DictionaryItem> loadEducationYears() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_h_education_year WHERE delete_ts IS NULL AND active = true ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new ScholarshipDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private static int bind(Query query, List<Object> params, int start) {
        int i = start;
        for (Object p : params) {
            query.setParameter(i++, p);
        }
        return i;
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static Boolean toBool(Object o) {
        return o == null ? null : (Boolean) o;
    }

    private static LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return null;
    }
}
