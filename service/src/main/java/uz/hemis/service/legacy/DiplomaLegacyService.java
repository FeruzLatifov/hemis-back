package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.StudentDiploma;
import uz.hemis.domain.repository.StudentDiplomaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Diploma Legacy Service - Business logic for CUBA diploma entity endpoints
 *
 * Extracted from StudentDiplomaEntityController to separate concerns.
 * Handles CRUD operations, entity mapping, and nested object loading for diploma views.
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DiplomaLegacyService {

    private final StudentDiplomaRepository diplomaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final CubaNestedObjectLoader nestedLoader;

    private static final String ENTITY_NAME = "hemishe_EStudentDiploma";

    // ==================== CRUD Operations ====================

    public Optional<StudentDiploma> findById(UUID id) {
        return diplomaRepository.findById(id);
    }

    public List<StudentDiploma> findAll() {
        return diplomaRepository.findAll();
    }

    public Page<StudentDiploma> findAll(Pageable pageable) {
        return diplomaRepository.findAll(pageable);
    }

    public List<StudentDiploma> findByActiveTrue() {
        return diplomaRepository.findByActiveTrue();
    }

    public List<StudentDiploma> findByUniversity(String university) {
        return diplomaRepository.findByUniversity(university);
    }

    public List<StudentDiploma> findByStudent(UUID studentId) {
        return diplomaRepository.findByStudent(studentId);
    }

    public List<StudentDiploma> findByDiplomaNumber(String diplomaNumber) {
        return diplomaRepository.findByDiplomaNumber(diplomaNumber);
    }

    public List<StudentDiploma> findByDiplomaNumberContaining(String diplomaNumber) {
        return diplomaRepository.findByDiplomaNumberContainingIgnoreCase(diplomaNumber);
    }

    @Transactional
    public StudentDiploma save(StudentDiploma entity) {
        return diplomaRepository.save(entity);
    }

    @Transactional
    public void softDelete(StudentDiploma entity) {
        entity.setDeleteTs(LocalDateTime.now());
        diplomaRepository.save(entity);
    }

    // ==================== Entity Mapping ====================

    /**
     * Convert StudentDiploma entity to CUBA-compatible Map
     * When view parameter is provided, returns nested objects for related entities
     */
    public Map<String, Object> toDiplomaMap(StudentDiploma entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();

        // CUBA standard fields (OLD-HEMIS format)
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", "com.company.hemishe.entity.EStudentDiploma-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());

        boolean useNestedObjects = view != null && !view.isEmpty();

        // Entity fields - with nested objects when view is provided
        if (useNestedObjects) {
            if (entity.getUniversity() != null) {
                Map<String, Object> uni = fetchUniversity(entity.getUniversity());
                map.put("university", uni != null ? uni : createMinimalNestedObject("hemishe_EUniversity", entity.getUniversity(), entity.getUniversity()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", null);
            }
            if (entity.getStudent() != null) {
                Map<String, Object> student = fetchStudent(entity.getStudent());
                map.put("student", student != null ? student : createMinimalNestedObject("hemishe_EStudent", entity.getStudent(), entity.getStudent().toString()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("student", null);
            }
        }

        CubaEntityMapHelper.putIfNotNull(map, "speciality", entity.getSpeciality(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "diplomaNumber", entity.getDiplomaNumber(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "registerNumber", entity.getRegisterNumber(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "registerDate", entity.getRegisterDate(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "translations", entity.getTranslations(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "academicRecord", entity.getAcademicRecord(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", entity.getActive(), returnNulls);

        // OLD-HEMIS: department only in view mode
        if (useNestedObjects && entity.getDepartment() != null) {
            Map<String, Object> dept = fetchDepartment(entity.getDepartment());
            map.put("department", dept != null ? dept : createMinimalNestedObject("hemishe_EUniversityDepartment", entity.getDepartment(), entity.getDepartment()));
        }

        CubaEntityMapHelper.putIfNotNull(map, "totalAcload", entity.getTotalAcload(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "avgGrade", entity.getAvgGrade(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);

        // diplomCategory -> diplomBlankCategory (nested object, only in view mode)
        if (useNestedObjects && entity.getDiplomCategory() != null) {
            Map<String, Object> cat = fetchDiplomBlankCategory(entity.getDiplomCategory());
            map.put("diplomBlankCategory", cat != null ? cat : createMinimalNestedObject("hemishe_HDiplomBlankCategory", entity.getDiplomCategory(), entity.getDiplomCategory()));
        }

        // OLD-HEMIS: educationYear only in view mode
        if (useNestedObjects && entity.getEducationYear() != null) {
            Map<String, Object> ey = fetchEducationYear(entity.getEducationYear());
            map.put("educationYear", ey != null ? ey : createMinimalNestedObject("hemishe_HEducationYear", entity.getEducationYear(), entity.getEducationYear()));
        }

        // OLD-HEMIS: educationType only in view mode
        if (useNestedObjects && entity.getEducationType() != null) {
            Map<String, Object> et = fetchEducationType(entity.getEducationType());
            map.put("educationType", et != null ? et : createMinimalNestedObject("hemishe_HEducationType", entity.getEducationType(), entity.getEducationType()));
        }

        CubaEntityMapHelper.putIfNotNull(map, "totalCredit", entity.getTotalCredit(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "tag", entity.getTag(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "verify", entity.getVerify(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "hash", entity.getHash(), returnNulls);

        // blankGenerateStatusCode -> blankGenerateStatus (nested object)
        if (useNestedObjects) {
            if (entity.getBlankGenerateStatusCode() != null) {
                Map<String, Object> bgs = fetchBlankGenerateStatus(entity.getBlankGenerateStatusCode());
                map.put("blankGenerateStatus", bgs != null ? bgs : createMinimalNestedObject("hemishe_HDiplomBlankGenerateStatus", entity.getBlankGenerateStatusCode(), entity.getBlankGenerateStatusCode()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("blankGenerateStatus", uz.hemis.common.JsonNull.INSTANCE);
            }
        } else {
            CubaEntityMapHelper.putIfNotNull(map, "blankGenerateStatusCode", entity.getBlankGenerateStatusCode(), returnNulls);
        }

        CubaEntityMapHelper.putIfNotNull(map, "studyDuration", entity.getStudyDuration(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "graduationDate", entity.getGraduationDate(), returnNulls);

        // admissionYear - old-hemis da object sifatida qaytariladi
        if (useNestedObjects && entity.getAdmissionYear() != null) {
            Map<String, Object> ay = fetchEducationYear(entity.getAdmissionYear());
            map.put("admissionYear", ay != null ? ay : createMinimalNestedObject("hemishe_HEducationYear", entity.getAdmissionYear(), entity.getAdmissionYear()));
        } else {
            CubaEntityMapHelper.putIfNotNull(map, "admissionYear", entity.getAdmissionYear(), returnNulls);
        }

        // Audit fields
        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);

        return map;
    }

    /**
     * Minimal response format for POST/PUT operations
     */
    public Map<String, Object> minimalDiplomaResponse(StudentDiploma entity) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", "com.company.hemishe.entity.EStudentDiploma-" + entity.getId() + " [detached]");
        response.put("id", entity.getId().toString());
        return response;
    }

    /**
     * Update StudentDiploma entity from Map
     * OLD-HEMIS format: underscore'siz maydonlar (university, student, speciality)
     * Supports nested object format: {"id": "value"}
     */
    public void updateDiplomaFromMap(StudentDiploma entity, Map<String, Object> map) {
        if (map.containsKey("university")) {
            entity.setUniversity(extractIdAsString(map.get("university")));
        }
        if (map.containsKey("student")) {
            String idStr = extractIdAsString(map.get("student"));
            if (idStr != null) {
                entity.setStudent(UUID.fromString(idStr));
            }
        }
        if (map.containsKey("speciality")) {
            entity.setSpeciality(extractIdAsString(map.get("speciality")));
        }
        if (map.containsKey("diplomaNumber")) {
            Object val = map.get("diplomaNumber");
            entity.setDiplomaNumber(val != null ? val.toString() : null);
        }
        if (map.containsKey("registerNumber")) {
            Object val = map.get("registerNumber");
            entity.setRegisterNumber(val != null ? val.toString() : null);
        }
        if (map.containsKey("registerDate")) {
            Object val = map.get("registerDate");
            if (val instanceof String) {
                entity.setRegisterDate(LocalDate.parse((String) val));
            }
        }
        if (map.containsKey("translations")) {
            Object val = map.get("translations");
            entity.setTranslations(val != null ? val.toString() : null);
        }
        if (map.containsKey("academicRecord")) {
            Object val = map.get("academicRecord");
            entity.setAcademicRecord(val != null ? val.toString() : null);
        }
        if (map.containsKey("active")) {
            Object activeVal = map.get("active");
            if (activeVal instanceof Boolean) {
                entity.setActive((Boolean) activeVal);
            } else if (activeVal != null) {
                entity.setActive(Boolean.parseBoolean(activeVal.toString()));
            }
        }
        if (map.containsKey("department")) {
            entity.setDepartment(extractIdAsString(map.get("department")));
        }
        if (map.containsKey("totalAcload")) {
            Object val = map.get("totalAcload");
            entity.setTotalAcload(val != null ? val.toString() : null);
        }
        if (map.containsKey("avgGrade")) {
            Object val = map.get("avgGrade");
            entity.setAvgGrade(val != null ? val.toString() : null);
        }
        if (map.containsKey("specialityName")) {
            Object val = map.get("specialityName");
            entity.setSpecialityName(val != null ? val.toString() : null);
        }
        if (map.containsKey("diplomCategory")) {
            entity.setDiplomCategory(extractIdAsString(map.get("diplomCategory")));
        }
        if (map.containsKey("educationYear")) {
            entity.setEducationYear(extractIdAsString(map.get("educationYear")));
        }
        if (map.containsKey("educationType")) {
            entity.setEducationType(extractIdAsString(map.get("educationType")));
        }
        if (map.containsKey("totalCredit")) {
            Object val = map.get("totalCredit");
            entity.setTotalCredit(val != null ? val.toString() : null);
        }
        if (map.containsKey("specialityCode")) {
            Object val = map.get("specialityCode");
            entity.setSpecialityCode(val != null ? val.toString() : null);
        }
        if (map.containsKey("tag")) {
            Object val = map.get("tag");
            entity.setTag(val != null ? val.toString() : null);
        }
        if (map.containsKey("verify")) {
            Object val = map.get("verify");
            entity.setVerify(val != null ? val.toString() : null);
        }
        if (map.containsKey("hash")) {
            Object val = map.get("hash");
            entity.setHash(val != null ? val.toString() : null);
        }
        if (map.containsKey("blankGenerateStatusCode")) {
            Object val = map.get("blankGenerateStatusCode");
            entity.setBlankGenerateStatusCode(val != null ? val.toString() : null);
        }
        if (map.containsKey("studyDuration")) {
            Object val = map.get("studyDuration");
            if (val instanceof Number) {
                entity.setStudyDuration(((Number) val).floatValue());
            } else if (val instanceof String && !((String) val).isEmpty()) {
                entity.setStudyDuration(Float.parseFloat((String) val));
            }
        }
        if (map.containsKey("graduationDate")) {
            Object val = map.get("graduationDate");
            if (val instanceof String) {
                entity.setGraduationDate(LocalDate.parse((String) val));
            }
        }
        if (map.containsKey("admissionYear")) {
            entity.setAdmissionYear(extractIdAsString(map.get("admissionYear")));
        }
    }

    // ==================== Filter/Search Support ====================

    /**
     * Try to apply filtering at database level for known conditions
     * Returns null if conditions are not supported for database filtering
     */
    public List<StudentDiploma> applyDatabaseFiltering(List<Map<String, Object>> conditions, int limit, int offset) {
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }

        for (Map<String, Object> cond : conditions) {
            String property = (String) cond.get("property");
            String operator = (String) cond.get("operator");
            Object value = cond.get("value");

            if (value == null) continue;

            // diplomaNumber filter
            if ("diplomaNumber".equals(property)) {
                List<StudentDiploma> filtered;
                if ("=".equals(operator)) {
                    filtered = diplomaRepository.findByDiplomaNumber(String.valueOf(value));
                } else {
                    filtered = diplomaRepository.findByDiplomaNumberContainingIgnoreCase(String.valueOf(value));
                }
                return applyPagination(filtered, limit, offset);
            }

            // university filter
            if ("university".equals(property)) {
                List<StudentDiploma> filtered = diplomaRepository.findByUniversity(String.valueOf(value));
                return applyPagination(filtered, limit, offset);
            }

            // student filter
            if ("student".equals(property)) {
                UUID studentId = UUID.fromString(String.valueOf(value));
                List<StudentDiploma> filtered = diplomaRepository.findByStudent(studentId);
                return applyPagination(filtered, limit, offset);
            }
        }

        return null;
    }

    /**
     * Apply CUBA filter conditions in memory
     */
    public List<StudentDiploma> applyConditions(List<StudentDiploma> list, List<Map<String, Object>> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return list;
        }

        return list.stream()
                .filter(entity -> {
                    for (Map<String, Object> cond : conditions) {
                        String property = (String) cond.get("property");
                        String operator = (String) cond.get("operator");
                        Object value = cond.get("value");

                        if (!matchesCondition(entity, property, operator, value)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if entity matches a single condition
     */
    public boolean matchesCondition(StudentDiploma entity, String property, String operator, Object value) {
        if (property == null || operator == null) return true;

        Object fieldValue = getFieldValue(entity, property);

        if ("=".equals(operator)) {
            if (fieldValue == null) return value == null;
            return fieldValue.toString().equals(String.valueOf(value));
        } else if ("contains".equals(operator) || "like".equals(operator)) {
            if (fieldValue == null) return false;
            return fieldValue.toString().toLowerCase().contains(String.valueOf(value).toLowerCase());
        } else if ("notEmpty".equals(operator)) {
            return fieldValue != null && !fieldValue.toString().isEmpty();
        }

        return true;
    }

    /**
     * Get field value from entity by property name
     */
    public Object getFieldValue(StudentDiploma entity, String property) {
        return switch (property) {
            case "diplomaNumber" -> entity.getDiplomaNumber();
            case "university" -> entity.getUniversity();
            case "student" -> entity.getStudent();
            case "speciality" -> entity.getSpeciality();
            case "active" -> entity.getActive();
            case "educationType" -> entity.getEducationType();
            case "educationYear" -> entity.getEducationYear();
            case "department" -> entity.getDepartment();
            default -> null;
        };
    }

    /**
     * Apply pagination to a list
     */
    public List<StudentDiploma> applyPagination(List<StudentDiploma> list, int limit, int offset) {
        int start = Math.min(offset, list.size());
        int end = Math.min(start + limit, list.size());
        return list.subList(start, end);
    }

    // ==================== Utility Methods ====================

    /**
     * Extract ID as String from nested object or simple value
     * OLD-HEMIS format: nested object {"_entityName": "...", "id": "..."} dan id olish
     * yoki oddiy string qiymat
     */
    @SuppressWarnings("unchecked")
    public String extractIdAsString(Object val) {
        if (val == null) return null;
        if (val instanceof UUID) return val.toString();
        if (val instanceof String) return (String) val;
        if (val instanceof Map) {
            Map<String, Object> objMap = (Map<String, Object>) val;
            Object id = objMap.get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    /**
     * Check if map has value for any of the specified keys
     * OLD-HEMIS format tekshirish
     */
    @SuppressWarnings("unchecked")
    public boolean hasValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val == null) continue;

            // university, student uchun: nested object {"_entityName": "...", "id": "..."}
            if ("university".equals(key) || "student".equals(key)) {
                if (val instanceof Map) {
                    Map<String, Object> objMap = (Map<String, Object>) val;
                    Object entityName = objMap.get("_entityName");
                    Object id = objMap.get("id");
                    if (entityName != null && id != null && !id.toString().isEmpty()) {
                        return true;
                    }
                }
                continue;
            }

            // speciality uchun: oddiy string
            if ("speciality".equals(key)) {
                if (val instanceof String && !((String) val).isEmpty()) {
                    return true;
                }
                continue;
            }

            // Boshqa maydonlar uchun oddiy tekshirish
            if (!val.toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if value is a valid nested reference with expected entity name
     * OLD-HEMIS format: nested reference must be {"_entityName": "expectedEntity", "id": "..."}
     */
    @SuppressWarnings("unchecked")
    public boolean isValidNestedRef(Object val, String expectedEntityName) {
        if (val == null || !(val instanceof Map)) {
            return false;
        }
        Map<String, Object> objMap = (Map<String, Object>) val;

        Object entityName = objMap.get("_entityName");
        if (entityName == null || !expectedEntityName.equals(entityName.toString())) {
            return false;
        }

        Object id = objMap.get("id");
        return id != null && !id.toString().isEmpty();
    }

    /**
     * Create validation error map
     */
    public Map<String, Object> createValidationError(String fieldName) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("message", "may not be null");
        error.put("messageTemplate", "{javax.validation.constraints.NotNull.message}");
        error.put("path", fieldName);
        error.put("invalidValue", null);
        return error;
    }

    /**
     * Fetch university nested object for diploma view
     */
    public Map<String, Object> fetchUniversity(String code) {
        if (code == null || code.isEmpty()) return null;
        return nestedLoader.loadUniversity(code);
    }

    /**
     * Fetch student nested object with all classifiers for diploma view
     * OLD-HEMIS formatida 48 maydonli to'liq student objectni qaytaradi
     */
    public Map<String, Object> fetchStudent(UUID studentId) {
        // Delegate to CubaNestedObjectLoader.loadStudentForDiploma which returns full 48-field object
        return nestedLoader.loadStudentForDiploma(studentId);
    }

    /**
     * Format date to string (yyyy-MM-dd)
     */
    private String formatDate(Object dateObj) {
        if (dateObj == null) return "";
        if (dateObj instanceof java.sql.Date) {
            return dateObj.toString();
        }
        if (dateObj instanceof LocalDate) {
            return dateObj.toString();
        }
        return str(dateObj);
    }

    /**
     * Fetch department nested object
     */
    public Map<String, Object> fetchDepartment(String code) {
        if (code == null || code.isEmpty()) return null;
        return nestedLoader.loadDepartment(code);
    }

    /**
     * Fetch education type classifier
     */
    public Map<String, Object> fetchEducationType(String code) {
        if (code == null || code.isEmpty()) return null;
        return nestedLoader.loadClassifier("hemishe_h_education_type", "HEducationType", code);
    }

    /**
     * Fetch education year classifier
     */
    public Map<String, Object> fetchEducationYear(String code) {
        if (code == null || code.isEmpty()) return null;
        return nestedLoader.loadClassifier("hemishe_h_education_year", "HEducationYear", code);
    }

    /**
     * Fetch blank generate status classifier
     */
    public Map<String, Object> fetchBlankGenerateStatus(String code) {
        if (code == null || code.isEmpty()) return null;
        return nestedLoader.loadClassifier("hemishe_h_diplom_blank_generate_status", "HDiplomBlankGenerateStatus", code);
    }

    /**
     * Fetch diplom blank category classifier
     */
    public Map<String, Object> fetchDiplomBlankCategory(String code) {
        if (code == null || code.isEmpty()) return null;
        return nestedLoader.loadClassifier("hemishe_h_diplom_blank_category", "HDiplomBlankCategory", code);
    }

    /**
     * Create minimal nested object (entityName + id + instanceName)
     */
    public Map<String, Object> createMinimalNestedObject(String entityName, Object id, String instanceName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", entityName);
        result.put("_instanceName", instanceName);
        result.put("id", id);
        return result;
    }

    // ==================== HELPER METHODS ====================

    private void putClassifierIfNotNull(Map<String, Object> map, String key, String code,
                                        String tableName, String entityName) {
        if (code == null || code.isEmpty()) return;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name, active, version FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", entityName);
            obj.put("id", row.get("code"));
            obj.put("code", row.get("code"));
            obj.put("name", row.get("name") != null ? row.get("name") : "");
            obj.put("active", row.get("active") != null ? row.get("active") : true);
            obj.put("version", row.get("version") != null ? ((Number) row.get("version")).intValue() : 1);
            map.put(key, obj);
        } catch (EmptyResultDataAccessException e) {
            // classifier not found, skip
        } catch (Exception e) {
            log.debug("putClassifierIfNotNull error for {} code={}: {}", key, code, e.getMessage());
        }
    }

    private void putSoatoIfNotNull(Map<String, Object> map, String key, String code) {
        if (code == null || code.isEmpty()) return;
        Map<String, Object> soato = nestedLoader.loadSoato(code);
        if (soato != null) {
            map.put(key, soato);
        }
    }

    private String str(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }
}
