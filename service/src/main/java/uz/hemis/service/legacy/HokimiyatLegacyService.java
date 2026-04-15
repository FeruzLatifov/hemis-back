package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hokimiyat Legacy Service - Business logic for hokimiyat data endpoints
 *
 * <p>Extracted from HokimiyatServiceController to follow Clean Architecture.</p>
 * <p>OLD-HEMIS: HokimiyatServiceBean.students()</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HokimiyatLegacyService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Get students updated yesterday with faculty name join.
     *
     * <p>Native SQL: joins hemishe_e_student with hemishe_e_university and hemishe_e_university_department</p>
     * <p>Filter: update_ts between yesterday and today</p>
     *
     * @return result map with status, count, items (or error info)
     */
    public Map<String, Object> getStudentsUpdatedYesterday() {
        String sql = """
            SELECT
                s.id,
                s.create_ts,
                s.update_ts,
                s."_university",
                s.pinfl,
                s.serial_number,
                s."_country",
                s."_citizenship",
                s."_nationality",
                s."_gender",
                s.birthday,
                s."_soato",
                s."_current_soato",
                s."_student_type",
                s."_payment_form",
                s."_education_year",
                s."_education_form",
                s."_education_type",
                s."_faculty",
                d.name_uz as faculty_name,
                s."_language",
                s."_accomodation",
                s."_course",
                s."_social_category",
                s."_stipend_rate",
                s."_roommate_type",
                s."_living_status",
                s."_student_status"
            FROM hemishe_e_student s
            INNER JOIN hemishe_e_university u ON u.code = s."_university"
            LEFT JOIN hemishe_e_university_department d ON d.code = s."_faculty"
            WHERE
                s.delete_ts IS NULL
                AND s.update_ts >= CURRENT_DATE - 1
                AND s.update_ts < CURRENT_DATE
            """;

        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql);

            result.put("status", "OK");
            result.put("count", items.size());
            result.put("items", items);
        } catch (Exception e) {
            log.error("Error fetching hokimiyat students", e);
            result.put("status", "ERROR");
            result.put("title", "Chetlashgan talabalar");
            result.put("message", e.getMessage());
        }

        return result;
    }
}
