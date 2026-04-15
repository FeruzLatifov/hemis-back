package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Student Statistics Service - Tashkent statistics queries
 *
 * <p>Extracted from StudentService as part of service decomposition.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentStatisticsService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndPaymentForm()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> byTashkentAndPaymentForm() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (to'luv shakli kesimida)",
                "byTashkentAndPaymentForm",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "budget", "Davlat grantida tahsil olayotgan talabalar soni",
                        "contract", "To'lov shartnoma asosida tahsil olayotgan talabalar soni",
                        "total", "Jami talabalar soni"),
                """
                SELECT t.university_code, t.university_name,
                       b.student_count as budget, c.student_count as contract,
                       b.student_count + c.student_count as total
                FROM hemishe_r_student_full t
                INNER JOIN (
                    SELECT e.university_code, e.payment_form_code, count(*) as student_count
                    FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                    GROUP BY e.university_code, payment_form_code
                ) b ON b.university_code = t.university_code AND b.payment_form_code = '11'
                INNER JOIN (
                    SELECT e.university_code, e.payment_form_code, count(*) as student_count
                    FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                    GROUP BY e.university_code, payment_form_code
                ) c ON c.university_code = t.university_code AND c.payment_form_code = '12'
                WHERE t.university_region_code like '1726%'
                GROUP BY t.university_code, t.university_name, budget, contract
                ORDER BY t.university_code
                """);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndRegionDistrict()
     */
    public Map<String, Object> byTashkentAndRegionDistrict() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (OTM, viloyat va tumanlar kesimida)",
                "byTashkentAndRegionDistrict",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "region_code", "Viloyat kodi", "region_name", "Viloyat nomi",
                        "district_code", "Tuman kodi", "district_name", "Tuman nomi",
                        "payment_form_code", "To'lov turi kodi", "payment_form_name", "To'lov turi nomi",
                        "student_count", "Talabalar soni"),
                """
                SELECT e.university_code, e.university_name,
                       e.region_code, e.region_name, e.district_code, e.district_name,
                       e.payment_form_code,
                       CASE WHEN (e.payment_form_name IS NULL) THEN '(Belgilanmagan)' ELSE e.payment_form_name END as payment_form_name,
                       count(*) as student_count
                FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                GROUP BY e.university_code, e.university_name, e.region_code, e.region_name,
                         e.district_code, e.district_name, e.payment_form_name, payment_form_code
                ORDER BY e.university_code, e.university_name, e.region_code, e.region_name,
                         e.district_code, e.district_name, e.payment_form_name
                """);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndRegionDistrictAndEduType()
     */
    public Map<String, Object> byTashkentAndRegionDistrictAndEduType() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (OTM, viloyat, tumanlar va ta'lim turi kesimida)",
                "byTashkentAndRegionDistrictAndEduType",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "region_code", "Viloyat kodi", "region_name", "Viloyat nomi",
                        "district_code", "Tuman kodi", "district_name", "Tuman nomi",
                        "education_type_code", "Ta'lim turi kodi", "education_type_name", "Ta'lim turi nomi",
                        "student_count", "Talabalar soni"),
                """
                SELECT e.university_code, e.university_name,
                       e.region_code, e.region_name, e.district_code, e.district_name,
                       e.education_type_code,
                       CASE WHEN (e.education_type_name IS NULL) THEN '(Belgilanmagan)' ELSE e.education_type_name END as education_type_name,
                       count(*) as student_count
                FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                GROUP BY e.university_code, e.university_name, e.region_code, e.region_name,
                         e.district_code, e.district_name, e.education_type_code, e.education_type_name
                ORDER BY e.university_code, e.university_name, e.region_code, e.region_name,
                         e.district_code, e.district_name, e.education_type_code, e.education_type_name
                """);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndFacultyAndCourse()
     */
    public Map<String, Object> byTashkentAndFacultyAndCourse() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (OTM, fakultet, ta'lim turi va kurslar kesimida)",
                "byTashkentAndFacultyAndCourse",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "faculty_code", "Fakultet kodi", "faculty_name", "Fakultet nomi",
                        "education_type_code", "Ta'lim turi kodi", "education_type_name", "Ta'lim turi nomi",
                        "course_code", "O'quv kurs kodi", "course_name", "O'quv kurs nomi",
                        "student_count", "Talabalar soni"),
                """
                SELECT e.university_code, e.university_name,
                       e.faculty_code, e.faculty_name,
                       e.education_type_code, e.education_type_name,
                       e.course_code, e.course_name,
                       count(*) as student_count
                FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                GROUP BY e.university_code, e.university_name, e.faculty_code, e.faculty_name,
                         e.education_type_code, e.education_type_name, e.course_code, e.course_name
                ORDER BY e.university_code, e.university_name, e.faculty_code, e.faculty_name,
                         e.education_type_code, e.education_type_name, e.course_code, e.course_name
                """);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndEduFormTypeAndGender()
     */
    public Map<String, Object> byTashkentAndEduFormTypeAndGender() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (OTM, ta'lim shakli, turi va jinslar kesimida)",
                "byTashkentAndEduFormTypeAndGender",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "education_form_code", "Ta'lim shakli kodi", "education_form_name", "Ta'lim shakli nomi",
                        "education_type_code", "Ta'lim turi kodi", "education_type_name", "Ta'lim turi nomi",
                        "gender_code", "Jins kodi", "gender_name", "Jins nomi",
                        "student_count", "Talabalar soni"),
                """
                SELECT e.university_code, e.university_name,
                       e.education_form_code,
                       CASE WHEN (e.education_form_name IS NULL) THEN '(Belgilanmagan)' ELSE e.education_form_name END as education_form_name,
                       e.education_type_code, e.education_type_name,
                       e.gender_code, e.gender_name,
                       count(*) as student_count
                FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                GROUP BY e.university_code, e.university_name, e.education_form_code, e.education_form_name,
                         e.education_type_code, e.education_type_name, e.gender_code, e.gender_name
                ORDER BY e.university_code, e.university_name, e.education_form_code, e.education_form_name,
                         e.education_type_code, e.education_type_name, e.gender_code, e.gender_name
                """);
    }

    /**
     * Helper: Execute Tashkent statistics query with standard response format
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeTashkentStats(String title, String methodName,
                                                      Map<String, String> columns, String sql) {
        Map<String, Object> result = new LinkedHashMap<>();
        String sqlCount = "SELECT count(*) as total_count FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'";
        try {
            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql);
            Long total = jdbcTemplate.queryForObject(sqlCount, Long.class);

            Map<String, String> orderedColumns = new LinkedHashMap<>(columns);

            result.put("status", "OK");
            result.put("title", title);
            result.put("message", null);
            result.put("total_student_count", total);
            result.put("columns", orderedColumns);
            result.put("items", items);
        } catch (Exception e) {
            log.error("Error in {}", methodName, e);
            result.put("status", "ERROR");
            result.put("title", title);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
