package uz.hemis.service.student;

import uz.hemis.common.vo.Pinfl;
import lombok.RequiredArgsConstructor;
import uz.hemis.common.vo.Pinfl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.service.student.mapper.StudentLegacyMapper;
import uz.hemis.domain.repository.StudentRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Student Legacy API Service - CUBA REST API compatible methods
 *
 * <p>Flat/legacy map methods extracted from StudentService.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentLegacyApiService {

    private final StudentRepository studentRepository;
    private final StudentLegacyMapper studentLegacyMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Verify student exists by PINFL (CUBA compatible)
     */
    public Object verify(String pinfl) {
        log.debug("CUBA API: verify student by PINFL: {}", Pinfl.maskOrEmpty(pinfl));
        return studentRepository.existsMasterByPinfl(pinfl);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.get(pinfl)
     */
    public Map<String, Object> getByPinflFlat(String pinfl) {
        List<Student> students = studentRepository.findByPinflAndStudentStatusIn(pinfl, Arrays.asList("11", "16"));
        if (students.isEmpty()) return null;
        return studentLegacyMapper.toFlatServiceMap(students.get(0));
    }

    /**
     * OLD-HEMIS: StudentServiceBean.getActive(pinfl)
     */
    public Map<String, Object> getActiveFlat(String pinfl) {
        String sql = """
                SELECT id FROM hemishe_e_student
                WHERE pinfl = ?
                AND _student_status IN ('11','16')
                AND decree_info_number IS NOT NULL AND decree_info_number <> ''
                AND delete_ts IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, pinfl);
        if (rows.isEmpty()) {
            sql = """
                    SELECT id FROM hemishe_e_student
                    WHERE serial_number = ? AND delete_ts IS NULL
                    LIMIT 1
                    """;
            rows = jdbcTemplate.queryForList(sql, pinfl);
            if (rows.isEmpty()) return null;
        }
        UUID id = (UUID) rows.get(0).get("id");
        Optional<Student> student = studentRepository.findById(id);
        return student.map(studentLegacyMapper::toFlatServiceMap).orElse(null);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.getById(code)
     */
    public Map<String, Object> getByCodeFlat(String code) {
        Optional<Student> student = studentRepository.findByCodeAndStudentStatusIn(code, List.of("11"));
        return student.map(studentLegacyMapper::toFlatServiceMap).orElse(null);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.getDoctoral(pinfl)
     */
    public Map<String, Object> getDoctoralFlat(String pinfl) {
        List<Student> students = studentRepository.findByPinflAndEducationTypeAndStudentStatusIn(
                pinfl, "13", List.of("11"));
        if (students.isEmpty()) return null;
        return studentLegacyMapper.toFlatServiceMap(students.get(0));
    }

    /**
     * OLD-HEMIS: StudentServiceBean.getWithStatus(pinfl)
     */
    public Map<String, Object> getWithStatusFlat(String pinfl) {
        List<Student> students = studentRepository.findByPinflAndStudentStatusIn(pinfl, List.of("11"));
        if (!students.isEmpty()) {
            return studentLegacyMapper.toFlatServiceMap(students.get(0));
        }
        students = studentRepository.findAllByPinfl(pinfl);
        if (!students.isEmpty()) {
            return studentLegacyMapper.toFlatServiceMap(students.get(0));
        }
        students = studentRepository.findBySerialNumber(pinfl);
        if (!students.isEmpty()) {
            return studentLegacyMapper.toFlatServiceMap(students.get(0));
        }
        return null;
    }

    /**
     * OLD-HEMIS: StudentServiceBean.testGet(pinfl)
     */
    public Map<String, Object> testGetFlat(String pinfl) {
        List<Student> students = studentRepository.findByPinflAndStudentStatusIn(pinfl, Arrays.asList("11", "16"));
        if (students.isEmpty()) return null;
        return studentLegacyMapper.toBillingMap(students.get(0));
    }

    /**
     * OLD-HEMIS: StudentServiceBean.students(university, limit, offset)
     */
    public Map<String, Object> getStudentsByUniversityFlatWithCount(String university, int limit, int offset) {
        int safeLimit = Math.min(limit, 1000);
        List<Student> students = studentRepository.findStudentsByUniversityPaginated(university, safeLimit, offset);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Student s : students) {
            data.add(studentLegacyMapper.toLegacyMapForService(s));
        }
        long count = studentRepository.countByUniversityAndStudentStatus(university, "11")
                   + studentRepository.countByUniversityAndStudentStatus(university, "16");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", data);
        result.put("count", count);
        return result;
    }

    /**
     * OLD-HEMIS: StudentServiceBean.tashkentStudents(limit, offset)
     */
    public Map<String, Object> getTashkentStudentsResult(int limit, int offset) {
        int safeLimit = Math.min(limit, 1000);
        Map<String, Object> response = new LinkedHashMap<>();

        String countSql = """
                SELECT count(*)
                FROM hemishe_r_student_full
                WHERE status_code = '11'
                AND university_region_code LIKE '1726%'
                """;
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
        if (count == null || count == 0) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
            return response;
        }

        String sql = """
                SELECT code, citizenship_code, citizenship_name, country_code, country_name,
                       nationality_code, nationality_name, pinfl, passport_number, fullname,
                       birthday, gender_code, gender_name, region_code, region_name,
                       district_code, district_name, address, address_current,
                       university_code, university_name, universtiry_ownership_code,
                       university_ownership_name, university_region_code, university_region_name,
                       university_district_code, university_district_name,
                       faculty_code, faculty_name,
                       education_type_code, education_type_name, education_form_code, education_form_name,
                       payment_form_code, payment_form_name, course_code, course_name,
                       speciality_id, speciality_code, speciality_name,
                       education_language_code, education_language_name,
                       stipend_type_code, stipend_type_name,
                       accomodation_code, accomodation_name,
                       social_category_code, social_category_name,
                       status_code, status_name,
                       created_at, updated_at, edu_year, is_graduate
                FROM hemishe_r_student_full
                WHERE status_code = '11'
                AND university_region_code LIKE '1726%'
                ORDER BY created_at
                LIMIT ? OFFSET ?
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, safeLimit, offset);
        if (rows.isEmpty()) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
            return response;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("_entityName", "hemishe_RStudentFull");
            String code = str(row.get("code"));
            r.put("_instanceName", code);
            r.put("id", code);
            r.put("code", code);
            r.put("citizenshipCode", str(row.get("citizenship_code")));
            r.put("citizenshipName", str(row.get("citizenship_name")));
            r.put("countryCode", str(row.get("country_code")));
            r.put("countryName", str(row.get("country_name")));
            r.put("nationalityCode", str(row.get("nationality_code")));
            r.put("nationalityName", str(row.get("nationality_name")));
            r.put("pinfl", str(row.get("pinfl")));
            r.put("passportNumber", str(row.get("passport_number")));
            r.put("fullname", str(row.get("fullname")));
            r.put("birthday", row.get("birthday") != null ? row.get("birthday").toString() : null);
            r.put("genderCode", str(row.get("gender_code")));
            r.put("genderName", str(row.get("gender_name")));
            r.put("regionCode", str(row.get("region_code")));
            r.put("regionName", str(row.get("region_name")));
            r.put("districtCode", str(row.get("district_code")));
            r.put("districtName", str(row.get("district_name")));
            r.put("address", str(row.get("address")));
            r.put("addressCurrent", str(row.get("address_current")));
            r.put("universityCode", str(row.get("university_code")));
            r.put("universityName", str(row.get("university_name")));
            r.put("universtiryOwnershipCode", str(row.get("universtiry_ownership_code")));
            r.put("universityOwnershipName", str(row.get("university_ownership_name")));
            r.put("universityRegionCode", str(row.get("university_region_code")));
            r.put("universityRegionName", str(row.get("university_region_name")));
            r.put("universityDistrictCode", str(row.get("university_district_code")));
            r.put("universityDistrictName", str(row.get("university_district_name")));
            r.put("facultyCode", str(row.get("faculty_code")));
            r.put("facultyName", str(row.get("faculty_name")));
            r.put("educationTypeCode", str(row.get("education_type_code")));
            r.put("educationTypeName", str(row.get("education_type_name")));
            r.put("educationFormCode", str(row.get("education_form_code")));
            r.put("educationFormName", str(row.get("education_form_name")));
            r.put("paymentFormCode", str(row.get("payment_form_code")));
            r.put("paymentFormName", str(row.get("payment_form_name")));
            r.put("courseCode", str(row.get("course_code")));
            r.put("courseName", str(row.get("course_name")));
            r.put("specialityId", row.get("speciality_id"));
            r.put("specialityCode", str(row.get("speciality_code")));
            r.put("specialityName", str(row.get("speciality_name")));
            r.put("educationLanguageCode", str(row.get("education_language_code")));
            r.put("educationLanguageName", str(row.get("education_language_name")));
            r.put("stipendTypeCode", str(row.get("stipend_type_code")));
            r.put("stipendTypeName", str(row.get("stipend_type_name")));
            r.put("accomodationCode", str(row.get("accomodation_code")));
            r.put("accomodationName", str(row.get("accomodation_name")));
            r.put("socialCategoryCode", str(row.get("social_category_code")));
            r.put("socialCategoryName", str(row.get("social_category_name")));
            r.put("statusCode", str(row.get("status_code")));
            r.put("statusName", str(row.get("status_name")));
            r.put("created_at", row.get("created_at") != null ? row.get("created_at").toString() : null);
            r.put("updated_at", row.get("updated_at") != null ? row.get("updated_at").toString() : null);
            r.put("eduYear", str(row.get("edu_year")));
            r.put("isGraduate", str(row.get("is_graduate")));
            data.add(r);
        }

        response.put("success", true);
        response.put("code", "ok");
        response.put("count", count);
        response.put("limit", limit);
        response.put("offset", offset);
        response.put("data", data);
        return response;
    }

    private static String str(Object val) {
        return val != null ? val.toString() : "";
    }
}
