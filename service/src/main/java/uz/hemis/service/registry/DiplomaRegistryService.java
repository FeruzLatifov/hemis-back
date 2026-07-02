package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.DiplomaDetailDto;
import uz.hemis.service.registry.dto.DiplomaDictionariesDto;
import uz.hemis.service.registry.dto.DiplomaRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Diploma Registry Service - READ-ONLY business logic for the Diplomas (Diplomlar) registry.
 *
 * <p>Source table {@code hemishe_e_student_diploma} is DOUBLE-MAPPED by two JPA entities
 * ({@code student.StudentDiploma} + {@code finance.Diploma}); therefore this service uses
 * {@link EntityManager} NATIVE queries only (never a JpaRepository) to avoid the ambiguous
 * mapping. Large {@code @Lob} columns ({@code academic_record}, {@code translations}) are
 * never selected.</p>
 *
 * <p>Soft-delete is enforced with an explicit {@code AND d.delete_ts IS NULL} (native queries
 * bypass {@code @SQLRestriction}). University and student are LEFT-JOINed with their own
 * {@code delete_ts IS NULL} guard. PINFL is never exposed — the student is shown by full name.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DiplomaRegistryService {

    private final EntityManager entityManager;

    /** Full-name expression (PINFL-free). CONCAT_WS skips nulls. */
    private static final String FULL_NAME = "CONCAT_WS(' ', s.lastname, s.firstname, s.fathername)";

    private static final String BASE_FROM =
        "FROM hemishe_e_student_diploma d " +
        "LEFT JOIN hemishe_e_university u ON u.code = d._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_e_student s ON s.id = d._student AND s.delete_ts IS NULL ";

    /**
     * Paged diploma list.
     *
     * @param q             search (diploma_number / register_number / student full name)
     * @param universityCode filter by university code
     * @param educationYear  filter by education-year classifier code
     * @param verify         filter by verify value
     * @param pageable       pagination
     */
    public Page<DiplomaRowDto> getDiplomas(String q, String universityCode, String educationYear,
                                           String verify, Pageable pageable) {
        log.debug("Getting diplomas: q={}, universityCode={}, educationYear={}, verify={}, page={}",
                  q, universityCode, educationYear, verify, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, educationYear, verify, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql =
            "SELECT d.id, d.diploma_number, d.register_number, d.register_date, " +
            "  d._student, " + FULL_NAME + ", d._university, u.name, d.speciality_name, " +
            "  d._education_year, d.graduation_date, d.avg_grade, d.verify, d.active " +
            BASE_FROM + where +
            "ORDER BY d.register_date DESC NULLS LAST, d.id " +
            "LIMIT ? OFFSET ?";

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<DiplomaRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Single diploma detail by id (UUID). Returns empty if not found or id is not a valid UUID.
     */
    public Optional<DiplomaDetailDto> getDiplomaDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql =
            "SELECT d.id, d.diploma_number, d.register_number, d.register_date, " +
            "  d._student, " + FULL_NAME + ", d._university, u.name, d.speciality_name, " +
            "  d._education_year, d.graduation_date, d.avg_grade, d.verify, d.active, " +
            "  d._education_type, d._admission_year, d.speciality_code, d.total_credit, d.hash " +
            BASE_FROM +
            "WHERE d.delete_ts IS NULL AND d.id = ?";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        return Optional.of(new DiplomaDetailDto(
            str(r[0]), (String) r[1], (String) r[2], toLocalDate(r[3]),
            str(r[4]), (String) r[5], (String) r[6], (String) r[7], (String) r[8],
            (String) r[9], toLocalDate(r[10]), (String) r[11], (String) r[12], (Boolean) r[13],
            (String) r[14], (String) r[15], (String) r[16], (String) r[17], (String) r[18]
        ));
    }

    /** Filter dictionaries (cached). */
    @Cacheable(value = "diplomasDictionaries", key = "'all'")
    public DiplomaDictionariesDto getDictionaries() {
        log.debug("Loading diploma dictionaries");
        List<DiplomaDictionariesDto.DictionaryItem> universities = loadUniversities();
        List<DiplomaDictionariesDto.DictionaryItem> educationYears = loadEducationYears();
        return new DiplomaDictionariesDto(universities, educationYears);
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String educationYear,
                              String verify, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE d.delete_ts IS NULL ");
        String searchLike = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (searchLike != null) {
            where.append("AND (LOWER(d.diploma_number) LIKE ? OR LOWER(d.register_number) LIKE ? " +
                         "OR LOWER(" + FULL_NAME + ") LIKE ?) ");
            params.add(searchLike);
            params.add(searchLike);
            params.add(searchLike);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND d._university = ? ");
            params.add(universityCode.trim());
        }
        if (educationYear != null && !educationYear.isBlank()) {
            where.append("AND d._education_year = ? ");
            params.add(educationYear.trim());
        }
        if (verify != null && !verify.isBlank()) {
            where.append("AND d.verify = ? ");
            params.add(verify.trim());
        }
        return where.toString();
    }

    private DiplomaRowDto mapRow(Object[] r) {
        return new DiplomaRowDto(
            str(r[0]), (String) r[1], (String) r[2], toLocalDate(r[3]),
            str(r[4]), (String) r[5], (String) r[6], (String) r[7], (String) r[8],
            (String) r[9], toLocalDate(r[10]), (String) r[11], (String) r[12], (Boolean) r[13]
        );
    }

    private List<DiplomaDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new DiplomaDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<DiplomaDictionariesDto.DictionaryItem> loadEducationYears() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_h_education_year WHERE delete_ts IS NULL AND active = true ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new DiplomaDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
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

    private static LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return null;
    }
}
