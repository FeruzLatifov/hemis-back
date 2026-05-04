package uz.hemis.service.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.EmployeeJobs;
import uz.hemis.domain.entity.employee.Teacher;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.EmployeeJobsRepository;
import uz.hemis.domain.repository.TeacherRepository;
import uz.hemis.domain.repository.UserRepository;

import org.springframework.data.domain.Example;

import uz.hemis.common.audit.Audited;
import uz.hemis.common.audit.AuditAction;

import java.util.*;

/**
 * Teacher Service - OLD-HEMIS Compatible
 *
 * <p>O'qituvchi/xodim bilan bog'liq biznes logikasi.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final EmployeeJobsRepository employeeJobsRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Generate or retrieve teacher unique ID
     *
     * @param data request data (citizenship, pinfl, serial, gender, year)
     * @param universityCode current user's university code
     * @return Map with success, is_new, unique_id, teacher
     */
    @Audited(action = AuditAction.CREATE, entity = "Teacher", entityClass = Teacher.class)
    public Map<String, Object> generateTeacherId(Map<String, Object> data, String universityCode) {
        Map<String, Object> result = new LinkedHashMap<>();
        log.info("Generating teacher ID - data: {}, university: {}", data, universityCode);

        // Validate parameters
        String citizenship = safeString(data.get("citizenship"));
        String pinfl = safeString(data.get("pinfl"));
        String serial = safeString(data.get("serial"));
        String gender = safeString(data.get("gender"));
        String year = safeString(data.get("year"));

        if (citizenship == null || citizenship.isEmpty()) {
            result.put("success", false);
            result.put("message", "Citizenship value incorrect");
            result.put("data", data);
            return result;
        }

        if ("11".equals(citizenship) && (pinfl == null || pinfl.isEmpty())) {
            result.put("success", false);
            result.put("message", "PINFL value incorrect");
            result.put("data", data);
            return result;
        }

        if (serial == null || serial.isEmpty()) {
            result.put("success", false);
            result.put("message", "Passport serial value incorrect");
            result.put("data", data);
            return result;
        }

        if (gender == null || gender.isEmpty()) {
            result.put("success", false);
            result.put("message", "Gender value incorrect");
            result.put("data", data);
            return result;
        }

        if (year == null || year.isEmpty()) {
            result.put("success", false);
            result.put("message", "Year value incorrect");
            result.put("data", data);
            return result;
        }

        // Search for existing teacher
        Optional<Teacher> existingTeacher;
        if ("11".equals(citizenship)) {
            existingTeacher = teacherRepository.findByPinfl(pinfl);
        } else {
            existingTeacher = teacherRepository.findBySerialNumberAndCitizenship(serial, citizenship);
        }

        if (existingTeacher.isPresent()) {
            Teacher teacher = existingTeacher.get();
            log.info("Found existing teacher: {}", teacher.getCode());
            result.put("success", true);
            result.put("is_new", false);
            result.put("unique_id", teacher.getCode());
            result.put("teacher", teacherToMap(teacher));
            return result;
        }

        // Create new teacher
        try {
            String yearSuffix = year.length() >= 2 ? year.substring(year.length() - 2) : year;
            String uniqueCode = generateUniqueCode(universityCode, yearSuffix, gender);

            Teacher teacher = new Teacher();
            teacher.setId(UUID.randomUUID());
            teacher.setPinfl(pinfl);
            teacher.setSerialNumber(serial);
            teacher.setCode(uniqueCode);
            teacher.setUniversity(universityCode);
            teacher.setEmployeeYear(year);
            teacher.setGender(gender);
            teacher.setCitizenship(citizenship);

            Teacher saved = teacherRepository.save(teacher);
            log.info("Created new teacher: {}", saved.getCode());

            result.put("success", true);
            result.put("is_new", true);
            result.put("unique_id", saved.getCode());
            result.put("teacher", teacherToMap(saved));
            return result;

        } catch (Exception e) {
            log.error("Error creating teacher: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            result.put("data", data);
            return result;
        }
    }

    /**
     * Get teacher by PINFL or serial number
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherByPinfl(String pinfl) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (pinfl == null || pinfl.isEmpty()) {
            result.put("success", false);
            result.put("code", "bad_request");
            result.put("data", "Bad request!");
            return result;
        }

        // Use findAllByPinfl — same PINFL may exist at multiple universities
        List<Teacher> teachers = teacherRepository.findAllByPinfl(pinfl);
        Optional<Teacher> teacher = teachers.stream().findFirst();
        if (!teacher.isPresent()) {
            // Try by serial number
            Teacher probe = new Teacher();
            probe.setSerialNumber(pinfl);
            List<Teacher> bySerial = teacherRepository.findAll(Example.of(probe));
            teacher = bySerial.stream().findFirst();
        }

        if (teacher.isPresent()) {
            result.put("success", true);
            result.put("code", "ok");
            result.put("data", teacherToDetailMap(teacher.get()));
            return result;
        }

        result.put("success", false);
        result.put("code", "not_found");
        result.put("data", "Teacher not found!");
        return result;
    }

    /**
     * Get teacher by code
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherByCode(String code) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (code == null || code.isEmpty()) {
            result.put("success", false);
            result.put("code", "bad_request");
            result.put("data", "Bad request!");
            return result;
        }

        // JPA Example query - mavjud Repository metodlaridan foydalanish
        Teacher probe = new Teacher();
        probe.setCode(code);
        Optional<Teacher> teacher = teacherRepository.findOne(Example.of(probe));

        if (teacher.isPresent()) {
            result.put("success", true);
            result.put("code", "ok");
            result.put("data", teacherToDetailMap(teacher.get()));
            return result;
        }

        result.put("success", false);
        result.put("code", "not_found");
        result.put("data", "Teacher not found!");
        return result;
    }

    /**
     * Get user's university code
     */
    public String getUserUniversityCode(String userId) {
        try {
            UUID uuid = UUID.fromString(userId);
            return userRepository.findById(uuid)
                    .map(user -> user.getUniversity() != null ? user.getUniversity().getCode() : null)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Error getting university code for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    private String generateUniqueCode(String universityCode, String yearSuffix, String gender) {
        // Format: UUU + YY + GG + NNN
        String codePrefix = universityCode + yearSuffix + gender;
        long count = teacherRepository.countByCodePrefix(codePrefix);
        // Retry loop to avoid duplicate key
        for (int i = 0; i < 100; i++) {
            String sequence = String.format("%03d", count + 1 + i);
            String candidate = codePrefix + sequence;
            if (!teacherRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        // Fallback: timestamp-based unique code
        return codePrefix + System.currentTimeMillis() % 100000;
    }

    private Map<String, Object> teacherToMap(Teacher teacher) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", teacher.getId().toString());
        map.put("code", teacher.getCode());
        map.put("pinfl", teacher.getPinfl());
        map.put("serialNumber", teacher.getSerialNumber());
        return map;
    }

    private Map<String, Object> teacherToDetailMap(Teacher teacher) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", teacher.getCode());
        map.put("pinfl", teacher.getPinfl());
        map.put("serial_number", teacher.getSerialNumber());
        map.put("firstname", teacher.getFirstName());
        map.put("lastname", teacher.getSecondName());
        map.put("fathername", teacher.getThirdName());
        map.put("birthday", teacher.getBirthDate());
        map.put("gender_code", teacher.getGender());
        map.put("citizenship_code", teacher.getCitizenship());
        map.put("employee_year", teacher.getEmployeeYear());
        map.put("jobs", loadJobsForTeacher(teacher.getId()));
        return map;
    }

    /**
     * Load jobs for teacher with classifier names — old-hemis compatible.
     */
    private List<Map<String, Object>> loadJobsForTeacher(UUID teacherId) {
        try {
            String sql = """
                SELECT ej._university, u.name as university_name, u.tin as university_tin,
                       ej._department, d.name_uz as department_name,
                       ej._employee_form, ef.name as employee_form_name,
                       ej._employee_position, ep.name as employee_position_name,
                       ej._employee_rate, er.name as employee_rate_name,
                       ej._employee_type, et.name as employee_type_name,
                       ej._employee_status, es.name as employee_status_name,
                       ej.job_start_date, ej.job_end_date
                FROM hemishe_e_employee_jobs ej
                LEFT JOIN hemishe_e_university u ON u.code = ej._university AND u.delete_ts IS NULL
                LEFT JOIN hemishe_e_university_department d ON d.code = ej._department AND d.delete_ts IS NULL
                -- Yangi jadvallarga yo'naltirilgan — Bosqich 4.5 (delete_ts yo'q, is_active bor)
                LEFT JOIN employment_form ef ON ef.code = ej._employee_form
                LEFT JOIN h_position ep ON ep.code = ej._employee_position
                LEFT JOIN employee_rate er ON er.code = ej._employee_rate
                LEFT JOIN university_employee_type et ON et.code = ej._employee_type
                LEFT JOIN university_employee_status_type es ON es.code = ej._employee_status
                WHERE ej._employee = ? AND ej.delete_ts IS NULL
                ORDER BY ej.create_ts DESC
                """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, teacherId);
            List<Map<String, Object>> jobs = new ArrayList<>();

            for (Map<String, Object> row : rows) {
                Map<String, Object> jobMap = new LinkedHashMap<>();
                jobMap.put("university_code", row.get("_university"));
                jobMap.put("university_name", row.get("university_name"));
                jobMap.put("university_tin", row.get("university_tin"));
                jobMap.put("department_code", row.get("_department"));
                jobMap.put("department_name", row.get("department_name"));

                // Faculty lookup from department code (old-hemis logic)
                String depCode = row.get("_department") != null ? row.get("_department").toString() : null;
                String fCode = null;
                String fName = null;
                if (depCode != null) {
                    try {
                        List<Map<String, Object>> facRows = jdbcTemplate.queryForList(
                            "SELECT code, name_uz FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL",
                            depCode.length() > 4 ? depCode.substring(0, depCode.indexOf("-", 4) > 0 ? depCode.indexOf("-", 4) : depCode.length()) : depCode
                        );
                        if (!facRows.isEmpty()) {
                            fCode = facRows.getFirst().get("code") != null ? facRows.getFirst().get("code").toString() : null;
                            fName = facRows.getFirst().get("name_uz") != null ? facRows.getFirst().get("name_uz").toString() : null;
                        }
                    } catch (Exception ignored) {}
                }
                jobMap.put("faculty_code", fCode);
                jobMap.put("faculty_name", fName);

                jobMap.put("employee_form_code", row.get("_employee_form"));
                jobMap.put("employee_form_name", row.get("employee_form_name"));
                jobMap.put("employee_position_code", row.get("_employee_position"));
                jobMap.put("employee_position_name", row.get("employee_position_name"));
                jobMap.put("employee_rate_code", row.get("_employee_rate"));
                jobMap.put("employee_rate_name", row.get("employee_rate_name"));
                jobMap.put("employee_type_code", row.get("_employee_type"));
                jobMap.put("employee_type_name", row.get("employee_type_name"));
                jobMap.put("employee_status_code", row.get("_employee_status"));
                jobMap.put("employee_status_name", row.get("employee_status_name"));
                jobMap.put("job_start_date", row.get("job_start_date") != null ? row.get("job_start_date").toString() : null);
                jobMap.put("job_end_date", row.get("job_end_date") != null ? row.get("job_end_date").toString() : null);
                jobs.add(jobMap);
            }
            return jobs;
        } catch (Exception e) {
            log.warn("Failed to load jobs for teacher {}: {}", teacherId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private String safeString(Object obj) {
        if (obj == null) return null;
        String str = String.valueOf(obj);
        return "null".equals(str) ? null : str;
    }
}
