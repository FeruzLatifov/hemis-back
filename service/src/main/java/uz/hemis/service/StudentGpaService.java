package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.StudentGpa;
import uz.hemis.domain.repository.StudentGpaRepository;

import java.util.*;

/**
 * StudentGpa Service - OLD-HEMIS Compatible
 *
 * <p>Provides GPA data in CUBA format for backward compatibility</p>
 *
 * <p>Response format (view=eStudentGpa-view):</p>
 * <pre>
 * {
 *   "_entityName": "hemishe_EStudentGpa",
 *   "_instanceName": "...",
 *   "id": "UUID",
 *   "debtSubjects": 0,
 *   "method": "one_year",
 *   "level": {
 *     "_entityName": "hemishe_HCourse",
 *     "_instanceName": "12 2-kurs",
 *     "id": "12",
 *     "code": "12",
 *     "name": "2-kurs"
 *   },
 *   "creditSum": "47.0",
 *   "subjects": 11,
 *   "educationYear": {...},
 *   "studentId": {...},
 *   "gpa": "4.0"
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentGpaService {

    private final StudentGpaRepository studentGpaRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Create new GPA record - OLD-HEMIS CUBA format
     *
     * <p>Request format (CUBA Entity):</p>
     * <pre>
     * {
     *   "studentId": {"id": "UUID"},    // or "studentId": "UUID"
     *   "educationYear": {"code": "2023"}, // or "educationYear": "2023"
     *   "level": {"code": "12"},        // or "level": "12"
     *   "gpa": "4.0",
     *   "method": "one_year",
     *   "creditSum": "47.0",
     *   "subjects": 11,
     *   "debtSubjects": 0
     * }
     * </pre>
     *
     * @param requestBody CUBA entity format request
     * @return Created GPA record in CUBA format
     */
    @Transactional
    public Map<String, Object> create(Map<String, Object> requestBody) {
        log.info("Creating new GPA record: {}", requestBody);

        StudentGpa gpa = new StudentGpa();

        // Generate new UUID
        gpa.setId(UUID.randomUUID());

        // Parse studentId (can be nested object or direct UUID string)
        Object studentIdObj = requestBody.get("studentId");
        if (studentIdObj != null) {
            if (studentIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> studentMap = (Map<String, Object>) studentIdObj;
                Object idValue = studentMap.get("id");
                if (idValue != null) {
                    gpa.setStudentId(UUID.fromString(idValue.toString()));
                }
            } else {
                gpa.setStudentId(UUID.fromString(studentIdObj.toString()));
            }
        }

        // Parse educationYear (can be nested object with code or direct code string)
        Object educationYearObj = requestBody.get("educationYear");
        if (educationYearObj != null) {
            if (educationYearObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> yearMap = (Map<String, Object>) educationYearObj;
                Object codeValue = yearMap.get("code");
                if (codeValue == null) codeValue = yearMap.get("id");
                if (codeValue != null) {
                    gpa.setEducationYearCode(codeValue.toString());
                }
            } else {
                gpa.setEducationYearCode(educationYearObj.toString());
            }
        }

        // Parse level (can be nested object with code or direct code string)
        Object levelObj = requestBody.get("level");
        if (levelObj != null) {
            if (levelObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> levelMap = (Map<String, Object>) levelObj;
                Object codeValue = levelMap.get("code");
                if (codeValue == null) codeValue = levelMap.get("id");
                if (codeValue != null) {
                    gpa.setLevelCode(codeValue.toString());
                }
            } else {
                gpa.setLevelCode(levelObj.toString());
            }
        }

        // Simple string/number fields
        if (requestBody.get("gpa") != null) {
            gpa.setGpa(requestBody.get("gpa").toString());
        }

        if (requestBody.get("method") != null) {
            gpa.setMethod(requestBody.get("method").toString());
        }

        if (requestBody.get("creditSum") != null) {
            gpa.setCreditSum(requestBody.get("creditSum").toString());
        }

        if (requestBody.get("subjects") != null) {
            gpa.setSubjects(Integer.parseInt(requestBody.get("subjects").toString()));
        }

        if (requestBody.get("debtSubjects") != null) {
            gpa.setDebtSubjects(Integer.parseInt(requestBody.get("debtSubjects").toString()));
        }

        // Save to database
        StudentGpa saved = studentGpaRepository.save(gpa);
        log.info("Created GPA record with ID: {}", saved.getId());

        // Return in CUBA format
        return toLegacyMap(saved);
    }

    /**
     * Upsert GPA record - OLD-HEMIS CUBA format (services/student/gpa)
     *
     * <p>OLD-HEMIS Logic (from StudentServiceBean.gpa):</p>
     * <ol>
     *   <li>Find existing GPA by ID or (studentId + educationYear)</li>
     *   <li>If found, delete it</li>
     *   <li>Save new GPA record</li>
     * </ol>
     *
     * <p>Request format:</p>
     * <pre>
     * {
     *   "gpa": {
     *     "id": "UUID (optional)",
     *     "studentId": {"id": "UUID"},
     *     "educationYear": {"code": "2023"},
     *     "level": {"code": "12"},
     *     "gpa": "3.90",
     *     "method": "one_year",
     *     "creditSum": "50.0",
     *     "subjects": 12,
     *     "debtSubjects": 0
     *   }
     * }
     * </pre>
     *
     * @param requestBody CUBA service format request with "gpa" wrapper
     * @param username    Current user's username (for createdBy field)
     * @return Upserted GPA record in CUBA format
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> upsert(Map<String, Object> requestBody, String username) {
        log.info("Upserting GPA record: {}", requestBody);

        // Extract "gpa" wrapper (old-hemis service format)
        Object gpaObj = requestBody.get("gpa");
        if (gpaObj == null) {
            throw new IllegalArgumentException("Missing 'gpa' parameter");
        }

        Map<String, Object> gpaData = (Map<String, Object>) gpaObj;

        // Parse ID (optional)
        UUID requestId = null;
        if (gpaData.get("id") != null) {
            requestId = UUID.fromString(gpaData.get("id").toString());
        }

        // Parse studentId
        UUID studentId = null;
        Object studentIdObj = gpaData.get("studentId");
        if (studentIdObj != null) {
            if (studentIdObj instanceof Map) {
                Map<String, Object> studentMap = (Map<String, Object>) studentIdObj;
                Object idValue = studentMap.get("id");
                if (idValue != null) {
                    studentId = UUID.fromString(idValue.toString());
                }
            } else {
                studentId = UUID.fromString(studentIdObj.toString());
            }
        }

        // Parse educationYear code
        String educationYearCode = null;
        Object educationYearObj = gpaData.get("educationYear");
        if (educationYearObj != null) {
            if (educationYearObj instanceof Map) {
                Map<String, Object> yearMap = (Map<String, Object>) educationYearObj;
                Object codeValue = yearMap.get("code");
                if (codeValue == null) codeValue = yearMap.get("id");
                if (codeValue != null) {
                    educationYearCode = codeValue.toString();
                }
            } else {
                educationYearCode = educationYearObj.toString();
            }
        }

        // OLD-HEMIS Logic: Find existing GPA by ID or (studentId + educationYear)
        StudentGpa existingGpa = null;

        if (requestId != null) {
            existingGpa = studentGpaRepository.findById(requestId).orElse(null);
        }

        if (existingGpa == null && studentId != null && educationYearCode != null) {
            existingGpa = studentGpaRepository.findByStudentIdAndEducationYearCode(studentId, educationYearCode)
                    .orElse(null);
        }

        // Delete existing if found
        if (existingGpa != null) {
            log.info("Deleting existing GPA record: {}", existingGpa.getId());
            studentGpaRepository.delete(existingGpa);
            studentGpaRepository.flush(); // Ensure delete is executed before insert
        }

        // Create new GPA record
        StudentGpa newGpa = new StudentGpa();
        newGpa.setId(UUID.randomUUID());
        newGpa.setStudentId(studentId);
        newGpa.setEducationYearCode(educationYearCode);

        // Parse level
        Object levelObj = gpaData.get("level");
        if (levelObj != null) {
            if (levelObj instanceof Map) {
                Map<String, Object> levelMap = (Map<String, Object>) levelObj;
                Object codeValue = levelMap.get("code");
                if (codeValue == null) codeValue = levelMap.get("id");
                if (codeValue != null) {
                    newGpa.setLevelCode(codeValue.toString());
                }
            } else {
                newGpa.setLevelCode(levelObj.toString());
            }
        }

        // Simple fields
        if (gpaData.get("gpa") != null) {
            newGpa.setGpa(gpaData.get("gpa").toString());
        }
        if (gpaData.get("method") != null) {
            newGpa.setMethod(gpaData.get("method").toString());
        }
        if (gpaData.get("creditSum") != null) {
            newGpa.setCreditSum(gpaData.get("creditSum").toString());
        }
        if (gpaData.get("subjects") != null) {
            newGpa.setSubjects(Integer.parseInt(gpaData.get("subjects").toString()));
        }
        if (gpaData.get("debtSubjects") != null) {
            newGpa.setDebtSubjects(Integer.parseInt(gpaData.get("debtSubjects").toString()));
        }

        // Audit fields
        newGpa.setCreatedBy(username);
        newGpa.setCreateTs(java.time.LocalDateTime.now());
        newGpa.setUpdateTs(java.time.LocalDateTime.now());

        // Save new record
        StudentGpa saved = studentGpaRepository.save(newGpa);
        log.info("Upserted GPA record with ID: {}", saved.getId());

        // Return in CUBA format (services format - includes version, createdBy, createTs, updateTs)
        return toLegacyServiceMap(saved);
    }

    /**
     * Convert StudentGpa entity to OLD-HEMIS CUBA service format
     *
     * <p>Matches exact field structure from old-hemis /services/student/gpa response</p>
     * <p>Includes: version, createdBy, createTs, updateTs (different from entity format)</p>
     */
    private Map<String, Object> toLegacyServiceMap(StudentGpa gpa) {
        Map<String, Object> map = new LinkedHashMap<>();

        // Entity metadata
        map.put("_entityName", "hemishe_EStudentGpa");
        map.put("id", gpa.getId().toString());

        // GPA fields
        map.put("debtSubjects", gpa.getDebtSubjects() != null ? gpa.getDebtSubjects() : 0);
        map.put("method", gpa.getMethod());

        // Nested: level (hemishe_HCourse) - minimal format for services
        if (gpa.getLevelCode() != null) {
            Map<String, Object> levelRef = new LinkedHashMap<>();
            levelRef.put("_entityName", "hemishe_HCourse");
            levelRef.put("id", gpa.getLevelCode());
            levelRef.put("code", gpa.getLevelCode());
            map.put("level", levelRef);
        }

        map.put("creditSum", gpa.getCreditSum());
        map.put("subjects", gpa.getSubjects());

        // Nested: educationYear - minimal format for services
        if (gpa.getEducationYearCode() != null) {
            Map<String, Object> yearRef = new LinkedHashMap<>();
            yearRef.put("_entityName", "hemishe_HEducationYear");
            yearRef.put("id", gpa.getEducationYearCode());
            yearRef.put("code", gpa.getEducationYearCode());
            map.put("educationYear", yearRef);
        }

        map.put("version", gpa.getVersion() != null ? gpa.getVersion() : 1);

        // Nested: studentId - minimal format for services
        if (gpa.getStudentId() != null) {
            Map<String, Object> studentRef = new LinkedHashMap<>();
            studentRef.put("_entityName", "hemishe_EStudent");
            studentRef.put("id", gpa.getStudentId().toString());
            studentRef.put("fullname", ""); // Old-hemis returns empty fullname
            map.put("studentId", studentRef);
        }

        map.put("createdBy", gpa.getCreatedBy());
        map.put("gpa", gpa.getGpa());

        // Format timestamps as old-hemis
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        if (gpa.getCreateTs() != null) {
            map.put("createTs", gpa.getCreateTs().format(formatter));
        }
        if (gpa.getUpdateTs() != null) {
            map.put("updateTs", gpa.getUpdateTs().format(formatter));
        }

        return map;
    }

    /**
     * Get all GPA records with pagination - OLD-HEMIS format
     *
     * @param limit  Max results
     * @param offset Skip first N results
     * @return List of GPA records in CUBA format
     */
    public List<Map<String, Object>> findAll(int limit, int offset) {
        log.info("Finding all GPA records: limit={}, offset={}", limit, offset);

        Page<StudentGpa> page = studentGpaRepository.findAll(
                PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1))
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentGpa gpa : page.getContent()) {
            result.add(toLegacyMap(gpa));
        }

        return result;
    }

    /**
     * Get GPA records by university code - OLD-HEMIS format
     *
     * @param universityCode University code
     * @param limit          Max results
     * @param offset         Skip first N results
     * @return List of GPA records in CUBA format
     */
    public List<Map<String, Object>> findByUniversity(String universityCode, int limit, int offset) {
        log.info("Finding GPA records by university: code={}, limit={}, offset={}", universityCode, limit, offset);

        Page<StudentGpa> page = studentGpaRepository.findByUniversityCode(
                universityCode,
                PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1))
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentGpa gpa : page.getContent()) {
            result.add(toLegacyMap(gpa));
        }

        return result;
    }

    /**
     * Get single GPA record by ID - OLD-HEMIS format
     *
     * @param id GPA record UUID
     * @return GPA record in CUBA format or null
     */
    public Map<String, Object> findById(UUID id) {
        log.info("Finding GPA record by ID: {}", id);

        return studentGpaRepository.findById(id)
                .map(this::toLegacyMap)
                .orElse(null);
    }

    /**
     * Count GPA records by university
     *
     * @param universityCode University code
     * @return Count
     */
    public long countByUniversity(String universityCode) {
        return studentGpaRepository.countByUniversityCode(universityCode);
    }

    /**
     * Count all GPA records
     *
     * @return Count
     */
    public long countAll() {
        return studentGpaRepository.count();
    }

    /**
     * Convert StudentGpa entity to OLD-HEMIS CUBA format
     *
     * <p>Matches exact field structure from old-hemis response</p>
     */
    private Map<String, Object> toLegacyMap(StudentGpa gpa) {
        Map<String, Object> map = new LinkedHashMap<>();

        // Entity metadata
        map.put("_entityName", "hemishe_EStudentGpa");
        map.put("_instanceName", "com.company.hemishe.entity.EStudentGpa-" + gpa.getId() + " [detached]");
        map.put("id", gpa.getId().toString());

        // GPA fields (ordered as old-hemis returns)
        map.put("debtSubjects", gpa.getDebtSubjects() != null ? gpa.getDebtSubjects() : 0);
        map.put("method", gpa.getMethod());

        // Nested: level (hemishe_HCourse)
        map.put("level", loadCourseLevel(gpa.getLevelCode()));

        map.put("creditSum", gpa.getCreditSum());
        map.put("subjects", gpa.getSubjects());

        // Nested: educationYear (hemishe_HEducationYear)
        map.put("educationYear", loadEducationYear(gpa.getEducationYearCode()));

        // Nested: studentId (hemishe_EStudent with name fields)
        map.put("studentId", loadStudent(gpa.getStudentId()));

        map.put("gpa", gpa.getGpa());

        return map;
    }

    /**
     * Load course level reference (hemishe_h_course)
     */
    private Map<String, Object> loadCourseLevel(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name FROM hemishe_h_course WHERE code = ?";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            String name = (String) row.get("name");
            String instanceName = code + " " + (name != null ? name : "");

            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("_entityName", "hemishe_HCourse");
            ref.put("_instanceName", instanceName.trim());
            ref.put("id", code);
            ref.put("code", code);
            ref.put("name", name);

            return ref;
        } catch (Exception e) {
            log.debug("Failed to load course level {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load education year reference (hemishe_h_education_year)
     */
    private Map<String, Object> loadEducationYear(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name FROM hemishe_h_education_year WHERE code = ?";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            String name = (String) row.get("name");

            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("_entityName", "hemishe_HEducationYear");
            ref.put("_instanceName", name != null ? name : code);
            ref.put("id", code);
            ref.put("name", name);

            return ref;
        } catch (Exception e) {
            log.debug("Failed to load education year {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load student reference (hemishe_e_student with name fields)
     */
    private Map<String, Object> loadStudent(UUID studentId) {
        if (studentId == null) {
            return null;
        }

        try {
            String sql = "SELECT id, lastname, firstname, fathername FROM hemishe_e_student WHERE id = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, studentId);

            String lastname = (String) row.get("lastname");
            String firstname = (String) row.get("firstname");
            String fathername = (String) row.get("fathername");

            // Build instance name: "LASTNAME FIRSTNAME FATHERNAME"
            StringBuilder instanceName = new StringBuilder();
            if (lastname != null) instanceName.append(lastname);
            if (firstname != null) {
                if (instanceName.length() > 0) instanceName.append(" ");
                instanceName.append(firstname);
            }
            if (fathername != null) {
                if (instanceName.length() > 0) instanceName.append(" ");
                instanceName.append(fathername);
            }

            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("_entityName", "hemishe_EStudent");
            ref.put("_instanceName", instanceName.toString());
            ref.put("id", studentId.toString());
            ref.put("lastname", lastname);
            ref.put("firstname", firstname);
            ref.put("fathername", fathername);

            return ref;
        } catch (Exception e) {
            log.debug("Failed to load student {}: {}", studentId, e.getMessage());
            return null;
        }
    }
}
