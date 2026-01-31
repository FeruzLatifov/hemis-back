package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Teacher;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.TeacherRepository;
import uz.hemis.domain.repository.UserRepository;

import org.springframework.data.domain.Example;

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

    /**
     * Generate or retrieve teacher unique ID
     *
     * @param data request data (citizenship, pinfl, serial, gender, year)
     * @param universityCode current user's university code
     * @return Map with success, is_new, unique_id, teacher
     */
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

        Optional<Teacher> teacher = teacherRepository.findByPinfl(pinfl);
        if (!teacher.isPresent()) {
            // Try by serial number using JPA Example query
            Teacher probe = new Teacher();
            probe.setSerialNumber(pinfl);
            teacher = teacherRepository.findOne(Example.of(probe));
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
        map.put("jobs", new ArrayList<>()); // Jobs would need separate query
        return map;
    }

    private String safeString(Object obj) {
        if (obj == null) return null;
        String str = String.valueOf(obj);
        return "null".equals(str) ? null : str;
    }
}
