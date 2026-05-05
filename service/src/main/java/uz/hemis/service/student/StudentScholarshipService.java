package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.repository.StudentRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Student Scholarship Service - scholarship checks and expel queries
 *
 * <p>Extracted from StudentService as part of service decomposition.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentScholarshipService {

    private final JdbcTemplate jdbcTemplate;

    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");

    /**
     * OLD-HEMIS: StudentServiceBean.checkScholarship(tin, pinfls)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> checkScholarshipNative(String tin, String[] pinfls) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (tin == null || !DIGITS_ONLY.matcher(tin).matches()) {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }
        if (pinfls == null || pinfls.length == 0) {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }
        for (String p : pinfls) {
            if (!DIGITS_ONLY.matcher(p).matches()) {
                result.put("success", false);
                result.put("data", "Bad request");
                return result;
            }
        }

        try (java.sql.Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            String[] pinflParams = new String[pinfls.length];
            System.arraycopy(pinfls, 0, pinflParams, 0, pinfls.length);
            java.sql.Array pinflArray = conn.createArrayOf("text", pinflParams);

            String sql = "SELECT t.pinfl, t.fullname FROM (" +
                    "SELECT r.pinfl, stipend_check(?, r.pinfl) as data, " +
                    "get_student_fullname_by_pinfl(r.pinfl) as fullname " +
                    "FROM (SELECT UNNEST(?) as pinfl) r" +
                    ") t WHERE t.\"data\" = false";

            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql, tin, pinflArray);
            result.put("success", true);
            result.put("data", items);
        } catch (Exception e) {
            // OWASP A05 — sanitize internal exception (DB constraint names leak via e.getMessage()).
            log.error("Error in checkScholarship", e);
            result.put("success", false);
            result.put("data", "Internal error");
        }
        return result;
    }

    /**
     * OLD-HEMIS: StudentServiceBean.checkScholarship2(tin, docOn, students)
     */
    @SuppressWarnings("unchecked")
    public Object checkScholarship(Map<String, Object> request) {
        log.info("Checking scholarship2 eligibility: {}", request);
        Map<String, Object> result = new LinkedHashMap<>();

        String tin = request.get("tin") != null ? request.get("tin").toString() : null;
        String docOnStr = request.get("docOn") != null ? request.get("docOn").toString() : null;
        Object studentsObj = request.get("students");

        if (tin == null || !DIGITS_ONLY.matcher(tin).matches()) {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }

        List<Map<String, Object>> studentsList;
        if (studentsObj instanceof List) {
            studentsList = (List<Map<String, Object>>) studentsObj;
        } else {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }

        if (studentsList.isEmpty()) {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }

        try {
            java.time.LocalDate docOn = java.time.LocalDate.parse(docOnStr);
            java.time.LocalDate first = docOn.withDayOfMonth(1);
            String dt = first.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String[] pinflParams = new String[studentsList.size()];
            java.math.BigDecimal[] sumParams = new java.math.BigDecimal[studentsList.size()];
            for (int i = 0; i < studentsList.size(); i++) {
                Map<String, Object> s = studentsList.get(i);
                String pinfl = s.get("pinfl") != null ? s.get("pinfl").toString() : "";
                String sum = s.get("sum") != null ? s.get("sum").toString() : "0";

                if (!DIGITS_ONLY.matcher(pinfl).matches()) {
                    result.put("success", false);
                    result.put("data", "Bad request");
                    return result;
                }
                pinflParams[i] = pinfl;
                sumParams[i] = new java.math.BigDecimal(sum);
            }

            try (java.sql.Connection conn = jdbcTemplate.getDataSource().getConnection()) {
                java.sql.Array pinflArray = conn.createArrayOf("text", pinflParams);
                java.sql.Array sumArray = conn.createArrayOf("numeric", sumParams);

                String sql = "SELECT t.pinfl, t.fullname, t.data as statusId FROM (" +
                        "SELECT r.pinfl, stipend_check2(?, r.pinfl, r.amount, ?::date) as data, " +
                        "get_student_fullname_by_pinfl(r.pinfl) as fullname FROM (" +
                        "SELECT UNNEST(?) as pinfl, " +
                        "UNNEST(?) as amount) r) t " +
                        "WHERE t.\"data\" <> 0";

                List<Map<String, Object>> items = jdbcTemplate.queryForList(sql, tin, dt, pinflArray, sumArray);
                result.put("success", true);
                result.put("students", items);
            }
        } catch (Exception e) {
            // OWASP A05 — sanitize internal exception.
            log.error("Error in checkScholarship2", e);
            result.put("success", false);
            result.put("data", "Internal error");
        }
        return result;
    }

    /**
     * OLD-HEMIS: StudentServiceBean.isExpel(pinfls)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> isExpel(String[] pinfls) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (pinfls == null || pinfls.length == 0) {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }

        try {
            StringBuilder placeholders = new StringBuilder();
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < pinfls.length; i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
                params.add(pinfls[i]);
            }

            String sql = """
                    SELECT s.pinfl,
                           COALESCE(s.lastname,'') || ' ' || COALESCE(s.firstname,'') || ' ' || COALESCE(s.fathername,'') as fullname,
                           s._university as "universityCode",
                           s._expel_reason as "expelReasonCode",
                           h.name as "expelReasonName"
                    FROM hemishe_e_student s
                    LEFT JOIN expel h ON h.code = s._expel_reason
                    WHERE s.pinfl IN (%s)
                      AND s._student_status = '12'
                      AND s.delete_ts IS NULL
                    ORDER BY s.create_ts DESC
                    """.formatted(placeholders.toString());

            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql, params.toArray());
            result.put("success", true);
            result.put("data", items);
        } catch (Exception e) {
            log.error("Error in isExpel", e);
            result.put("success", false);
            result.put("data", e.getMessage());
        }
        return result;
    }
}
