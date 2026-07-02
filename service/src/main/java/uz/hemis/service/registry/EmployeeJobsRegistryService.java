package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.registry.dto.EmployeeJobsDetailDto;
import uz.hemis.service.registry.dto.EmployeeJobsDictionariesDto;
import uz.hemis.service.registry.dto.EmployeeJobsRowDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Employee Jobs Registry Service - READ-ONLY business logic.
 *
 * <p>Source table {@code hemishe_e_employee_jobs} (entity {@code LegacyEmployeeJobs}) read via
 * {@link EntityManager} native queries. Soft-delete enforced with explicit
 * {@code AND t.delete_ts IS NULL}. The employee is resolved PII-safely by full name only
 * (join {@code _employee} UUID → {@code hemishe_e_teacher.id}); PINFL is never selected.
 * Classifier and university tables LEFT-JOINed with their own {@code delete_ts IS NULL} guard
 * ({@code h_position} is a modern reference table without {@code delete_ts}) and a raw-code
 * fallback when the classifier row is missing.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeJobsRegistryService {

    private final EntityManager entityManager;

    /** Employee full name (PINFL-free). CONCAT_WS skips nulls. */
    private static final String EMP_NAME = "CONCAT_WS(' ', emp.lastname, emp.firstname, emp.fathername)";

    private static final String BASE_FROM =
        "FROM hemishe_e_employee_jobs t " +
        "LEFT JOIN hemishe_e_university u ON u.code = t._university AND u.delete_ts IS NULL " +
        "LEFT JOIN hemishe_e_teacher emp ON emp.id = t._employee AND emp.delete_ts IS NULL " +
        "LEFT JOIN hemishe_e_university_department dep ON dep.code = t._department " +
        "  AND dep.university_code = t._university AND dep.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_university_employee_type et ON et.code = t._employee_type AND et.delete_ts IS NULL " +
        "LEFT JOIN h_position pos ON pos.code = t._employee_position " +
        "LEFT JOIN hemishe_h_university_employee_status_type st ON st.code = t._employee_status AND st.delete_ts IS NULL " +
        "LEFT JOIN hemishe_h_university_employee_form ef ON ef.code = t._employee_form AND ef.delete_ts IS NULL ";

    private static final String ROW_COLS =
        "t.id, t._employee, " + EMP_NAME + ", t._university, u.name, " +
        "t._department, dep.name_uz, t._employee_type, et.name, " +
        "t._employee_position, pos.name, t._employee_status, st.name, " +
        "t.job_start_date, t.job_end_date, t.active ";

    public Page<EmployeeJobsRowDto> getEmployeeJobs(String q, String universityCode, String employeeType,
                                                    Boolean active, Pageable pageable) {
        log.debug("Getting employee jobs: q={}, universityCode={}, employeeType={}, active={}, page={}",
                  q, universityCode, employeeType, active, pageable.getPageNumber());

        List<Object> params = new ArrayList<>();
        String where = buildWhere(q, universityCode, employeeType, active, params);

        String countSql = "SELECT COUNT(*) " + BASE_FROM + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bind(countQuery, params, 1);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        String dataSql = "SELECT " + ROW_COLS + BASE_FROM + where +
            "ORDER BY t.job_start_date DESC NULLS LAST, t.id LIMIT ? OFFSET ?";
        Query dataQuery = entityManager.createNativeQuery(dataSql);
        int idx = bind(dataQuery, params, 1);
        dataQuery.setParameter(idx, pageable.getPageSize());
        dataQuery.setParameter(idx + 1, pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<EmployeeJobsRowDto> content = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<EmployeeJobsDetailDto> getEmployeeJobDetail(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String sql = "SELECT " + ROW_COLS +
            ", t._employee_form, ef.name, t._employee_rate, t.contract_number, t.contract_date, " +
            "  t.decree_number, t.decree_date " +
            BASE_FROM + "WHERE t.delete_ts IS NULL AND t.id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] r = rows.get(0);
        EmployeeJobsRowDto row = mapRow(r);
        return Optional.of(new EmployeeJobsDetailDto(
            row.id(), row.employeeId(), row.employeeName(), row.universityCode(), row.universityName(),
            row.departmentCode(), row.departmentName(), row.employeeTypeCode(), row.employeeTypeName(),
            row.positionCode(), row.positionName(), row.statusCode(), row.statusName(),
            row.jobStartDate(), row.jobEndDate(), row.active(),
            (String) r[16], label(r[17], r[16]), (String) r[18], (String) r[19], toLocalDate(r[20]),
            (String) r[21], toLocalDate(r[22])
        ));
    }

    @Cacheable(value = "employeeJobsDictionaries", key = "'all'")
    public EmployeeJobsDictionariesDto getDictionaries() {
        log.debug("Loading employee-jobs dictionaries");
        return new EmployeeJobsDictionariesDto(
            loadUniversities(),
            loadClassifier("hemishe_h_university_employee_type")
        );
    }

    // ---- helpers ----

    private String buildWhere(String q, String universityCode, String employeeType,
                              Boolean active, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE t.delete_ts IS NULL ");
        String like = (q != null && !q.isBlank()) ? "%" + q.trim().toLowerCase() + "%" : null;
        if (like != null) {
            where.append("AND (LOWER(" + EMP_NAME + ") LIKE ? OR LOWER(t.decree_number) LIKE ?) ");
            params.add(like);
            params.add(like);
        }
        if (universityCode != null && !universityCode.isBlank()) {
            where.append("AND t._university = ? ");
            params.add(universityCode.trim());
        }
        if (employeeType != null && !employeeType.isBlank()) {
            where.append("AND t._employee_type = ? ");
            params.add(employeeType.trim());
        }
        if (active != null) {
            where.append("AND t.active = ? ");
            params.add(active);
        }
        return where.toString();
    }

    private EmployeeJobsRowDto mapRow(Object[] r) {
        return new EmployeeJobsRowDto(
            str(r[0]), str(r[1]), (String) r[2], (String) r[3], (String) r[4],
            (String) r[5], (String) r[6], (String) r[7], label(r[8], r[7]),
            (String) r[9], label(r[10], r[9]), (String) r[11], label(r[12], r[11]),
            toLocalDate(r[13]), toLocalDate(r[14]), (Boolean) r[15]
        );
    }

    private List<EmployeeJobsDictionariesDto.DictionaryItem> loadUniversities() {
        Query query = entityManager.createNativeQuery(
            "SELECT code, name FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY name");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new EmployeeJobsDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
            .collect(Collectors.toList());
    }

    private List<EmployeeJobsDictionariesDto.DictionaryItem> loadClassifier(String table) {
        // Table name is a fixed literal (not user input) — safe to inline.
        String sql = "SELECT code, name FROM " + table +
                     " WHERE delete_ts IS NULL AND active = true ORDER BY name";
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new EmployeeJobsDictionariesDto.DictionaryItem((String) row[0], (String) row[1]))
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
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return null;
    }
}
