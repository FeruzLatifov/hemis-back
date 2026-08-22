package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.util.JdbcTemporal;
import uz.hemis.service.registry.dto.DoctoralStudentDetailDto;
import uz.hemis.service.registry.dto.DoctoralStudentDictionariesDto;
import uz.hemis.service.registry.dto.DoctoralStudentRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Researcher (Doctoral Student) Registry Service - READ-ONLY business logic.
 *
 * <p>Source table {@code hemishe_e_doctorate_student}. Read via {@link EntityManager} native
 * queries (never a JpaRepository). Soft-delete is enforced with an explicit
 * {@code AND t.delete_ts IS NULL} (native queries bypass {@code @SQLRestriction}).
 * University and classifier tables are LEFT-JOINed with their own {@code delete_ts IS NULL}
 * guard and a raw-code fallback when the classifier row is missing. PII columns
 * ({@code passport_pin}, {@code passport_number}, {@code home_address}, {@code _translations})
 * are never selected — the researcher is shown by full name only.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DoctoralStudentRegistryService {

    private final EntityManager entityManager;

    /** Full-name expression (PII-safe). CONCAT_WS skips nulls. */
    private static final String FULL_NAME = "CONCAT_WS(' ', t.second_name, t.first_name, t.third_name)";

    private static final String BASE_FROM =
        "FROM hemishe_e_doctorate_student t " +
        "LEFT JOIN hemishe_e_university u ON u.code = t._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_science_branch sb ON sb.code = t._science_branch AND sb.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_doctoral_student_type dt ON dt.code = t._doctoral_student_type AND dt.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_doctoral_student_status st ON st.code = t._doctorate_student_status AND st.delete_ts IS NULL ";

    private static final String ROW_COLS =
        "t.id, " + FULL_NAME + ", t.student_id_number, t._university, u.name, " +
        "t._science_branch, sb.name, t._doctoral_student_type, dt.name, " +
        "t._doctorate_student_status, st.name, t.accepted_date, t.active ";

    public Page<DoctoralStudentRowDto> getResearchers(String q, String universityCode, String scienceBranch,
                                                      String doctoralStudentType, String status, Boolean active,
                                                      Pageable pageable) {
        log.debug("Getting researchers: q={}, universityCode={}, scienceBranch={}, doctoralStudentType={}, status={}, active={}, page={}",
                  q, universityCode, scienceBranch, doctoralStudentType, status, active, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, scienceBranch, doctoralStudentType, status, active, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = "SELECT " + ROW_COLS + BASE_FROM + where +
            "ORDER BY t.accepted_date DESC NULLS LAST, t.id LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<DoctoralStudentRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<DoctoralStudentDetailDto> getResearcherDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql = "SELECT " + ROW_COLS +
            ", t.dissertation_theme, t.birth_date, t._level, t._department, t._payment_form " +
            BASE_FROM + "WHERE t.delete_ts IS NULL AND t.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        DoctoralStudentRowDto row = mapRow(r);
        return Optional.of(new DoctoralStudentDetailDto(
            row.id(), row.fullName(), row.studentIdNumber(), row.universityCode(), row.universityName(),
            row.scienceBranchCode(), row.scienceBranchName(), row.doctoralStudentTypeCode(), row.doctoralStudentTypeName(),
            row.statusCode(), row.statusName(), row.acceptedDate(), row.active(),
            (String) r[13], toLocalDate(r[14]), (String) r[15], (String) r[16], (String) r[17]
        ));
    }

    @Cacheable(value = "researchersDictionaries", key = "'all'")
    public DoctoralStudentDictionariesDto getDictionaries() {
        log.debug("Loading researcher dictionaries");
        return new DoctoralStudentDictionariesDto(
            loadUniversities(),
            loadClassifier("hemishe_h_science_branch"),
            loadClassifier("hemishe_h_doctoral_student_type"),
            loadClassifier("hemishe_h_doctoral_student_status")
        );
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String scienceBranch, String doctoralStudentType,
                              String status, Boolean active, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE t.delete_ts IS NULL ");
        String like = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (like != null) {
            where.append("AND (LOWER(" + FULL_NAME + ") LIKE ? OR LOWER(t.student_id_number) LIKE ? " +
                         "OR LOWER(t.dissertation_theme) LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND t._university = ? ");
            params.add(universityCode.trim());
        }
        if (scienceBranch != null && !scienceBranch.isBlank()) {
            where.append("AND t._science_branch = ? ");
            params.add(scienceBranch.trim());
        }
        if (doctoralStudentType != null && !doctoralStudentType.isBlank()) {
            where.append("AND t._doctoral_student_type = ? ");
            params.add(doctoralStudentType.trim());
        }
        if (status != null && !status.isBlank()) {
            where.append("AND t._doctorate_student_status = ? ");
            params.add(status.trim());
        }
        if (active != null) {
            where.append("AND t.active = ? ");
            params.add(active);
        }
        return where.toString();
    }

    private DoctoralStudentRowDto mapRow(Object[] r) {
        return new DoctoralStudentRowDto(
            str(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], label(r[6], r[5]), (String) r[7], label(r[8], r[7]),
            (String) r[9], label(r[10], r[9]), toLocalDate(r[11]), (Boolean) r[12]
        );
    }

    private List<DoctoralStudentDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new DoctoralStudentDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<DoctoralStudentDictionariesDto.DictionaryItem> loadClassifier(String table) {
        // Table name is a fixed literal (not user input) — safe to inline.
        String sql = "SELECT code, name FROM " + table +
                     " WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new DoctoralStudentDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private static int bind(Query query, List<Object> params, int start) {
        int i = start;
        for (Object p : params) {
            query.setParameter(i++, p);
        }
        return i;
    }

    private static String label(Object name, Object code) {
        if (name != null) return (String) name;
        return code != null ? code.toString() : null;
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static LocalDate toLocalDate(Object o) {
        return JdbcTemporal.toLocalDate(o);
    }
}
