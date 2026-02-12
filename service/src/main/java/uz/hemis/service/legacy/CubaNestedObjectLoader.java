package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.common.JsonNull;

import java.util.*;

/**
 * CUBA Nested Object Loader - Shared service for loading nested/reference objects
 * 
 * Provides CUBA-compatible nested object loading for legacy API controllers.
 * Used by UniversityDepartment, Teacher, StudentDiploma, Classifier controllers.
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CubaNestedObjectLoader {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Load classifier object (code, name) - basic format
     * Used for: gender, citizenship, education_type, ownership, belongsTo, etc.
     * OLD-HEMIS compatibility: includes code field
     *
     * @param tableName DB table name (e.g. "hemishe_h_gender")
     * @param entityName CUBA entity name suffix (e.g. "HGender")
     * @param code classifier code
     * @return CUBA-format map or null
     */
    public Map<String, Object> loadClassifier(String tableName, String entityName, String code) {
        if (code == null || code.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM " + sanitizeTableName(tableName) + " WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> obj = new LinkedHashMap<>();
            String fullEntityName = "hemishe_" + entityName;
            obj.put("_entityName", fullEntityName);
            // OLD-HEMIS: _instanceName format based on @NamePattern
            String name = row.get("name") != null ? str(row.get("name")) : "";
            obj.put("_instanceName", buildInstanceName(fullEntityName, str(row.get("code")), name));
            obj.put("id", str(row.get("code")));
            // OLD-HEMIS compatibility: include code field
            obj.put("code", str(row.get("code")));
            obj.put("name", name);
            return obj;
        } catch (EmptyResultDataAccessException e) {
            // Return minimal object with just id
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            obj.put("id", code);
            return obj;
        } catch (Exception e) {
            log.debug("Classifier load error ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    /**
     * Load classifier with additional name fields (name_ru, name_en, active)
     * Used for: hemis_version_type, university_contract_category, etc.
     * OLD-HEMIS compatibility: includes nameRu, nameEn, active fields
     */
    public Map<String, Object> loadClassifierWithNames(String tableName, String entityName, String code) {
        if (code == null || code.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name, name_ru, name_en, active FROM " + sanitizeTableName(tableName) + " WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> obj = new LinkedHashMap<>();
            String fullEntityName = "hemishe_" + entityName;
            obj.put("_entityName", fullEntityName);
            // OLD-HEMIS: _instanceName format based on @NamePattern
            String name = row.get("name") != null ? str(row.get("name")) : "";
            obj.put("_instanceName", buildInstanceName(fullEntityName, str(row.get("code")), name));
            obj.put("id", str(row.get("code")));
            // OLD-HEMIS compatibility: include code, nameRu, nameEn with JsonNull for null/empty values
            obj.put("code", str(row.get("code")));
            Object nameRu = row.get("name_ru");
            obj.put("nameRu", (nameRu != null && !nameRu.toString().isEmpty()) ? str(nameRu) : JsonNull.INSTANCE);
            obj.put("name", name);
            obj.put("active", row.get("active") != null ? row.get("active") : true);
            Object nameEn = row.get("name_en");
            obj.put("nameEn", (nameEn != null && !nameEn.toString().isEmpty()) ? str(nameEn) : JsonNull.INSTANCE);
            return obj;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            obj.put("id", code);
            return obj;
        } catch (Exception e) {
            log.debug("Classifier load error ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    /**
     * Load classifier with name-only _instanceName format
     * Used for: deparmentType where _instanceName = name only (without code prefix)
     * OLD-HEMIS compatibility: _instanceName is just "Kafedra", not "12 Kafedra"
     * Also includes nameRu and nameEn fields
     */
    public Map<String, Object> loadClassifierNameOnly(String tableName, String entityName, String code) {
        if (code == null || code.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name, name_ru, name_en, active FROM " + sanitizeTableName(tableName) + " WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            // OLD-HEMIS: deparmentType _instanceName is name only (no code prefix)
            String name = row.get("name") != null ? str(row.get("name")) : "";
            obj.put("_instanceName", name);
            obj.put("id", str(row.get("code")));
            // OLD-HEMIS field order: nameRu, name, active, nameEn - use JsonNull for null/empty values
            Object nameRu = row.get("name_ru");
            obj.put("nameRu", (nameRu != null && !nameRu.toString().isEmpty()) ? str(nameRu) : JsonNull.INSTANCE);
            obj.put("name", name);
            obj.put("active", row.get("active") != null ? row.get("active") : true);
            Object nameEn = row.get("name_en");
            obj.put("nameEn", (nameEn != null && !nameEn.toString().isEmpty()) ? str(nameEn) : JsonNull.INSTANCE);
            return obj;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            obj.put("id", code);
            return obj;
        } catch (Exception e) {
            log.debug("Classifier load error ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    /**
     * Load classifier with _instanceName format "code-name" (hyphen separator)
     * Used for: universityActivityStatus where _instanceName = "10-Ishlamoqda"
     * OLD-HEMIS compatibility: specific format for activity status
     */
    public Map<String, Object> loadClassifierWithHyphen(String tableName, String entityName, String code) {
        if (code == null || code.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM " + sanitizeTableName(tableName) + " WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            // OLD-HEMIS: universityActivityStatus _instanceName format is "code-name" (with hyphen)
            String name = row.get("name") != null ? str(row.get("name")) : "";
            obj.put("_instanceName", str(row.get("code")) + "-" + name);
            obj.put("id", str(row.get("code")));
            obj.put("code", str(row.get("code")));
            obj.put("name", name);
            return obj;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            obj.put("id", code);
            return obj;
        } catch (Exception e) {
            log.debug("Classifier load error ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    /**
     * OLD-HEMIS @NamePattern: entity lar uchun _instanceName formatini aniqlash
     * "name" = "%s|name" (faqat name); "code_dash_name" = "%s - %s|code,name"
     * Qolganlari default "code_space_name" = "%s %s|code,name"
     */
    private static final Set<String> NAME_ONLY_ENTITIES = Set.of(
        "hemishe_HEducationYear", "hemishe_HTransferType",
        "hemishe_HCertificateNames", "hemishe_HCertificateGrades",
        "hemishe_HCertificateSubjects", "hemishe_HCertificateType",
        "hemishe_HDepartmentType"
    );
    private static final Set<String> CODE_DASH_NAME_ENTITIES = Set.of(
        "hemishe_HPaymentForm", "hemishe_HDiplomBlankGenerateStatus",
        "hemishe_HSpecialityBachelor", "hemishe_HSpecialityMaster",
        "hemishe_HSpecialityOrdinatura", "hemishe_HStipendRate",
        "hemishe_HStipendRateCategory"
    );

    private String buildInstanceName(String entityName, String code, String name) {
        if (NAME_ONLY_ENTITIES.contains(entityName)) return name;
        if (CODE_DASH_NAME_ENTITIES.contains(entityName)) return code + " - " + name;
        return code + " " + name;
    }

    /**
     * Load classifier with _instanceName format based on OLD-HEMIS @NamePattern
     * Used by TeacherEntityController for classifiers in eTeacher-view
     * OLD-HEMIS compatibility: includes nameRu, nameEn, active fields
     */
    /**
     * Load classifier _minimal view — only _entityName, _instanceName, id
     * Used for nested references with view="_minimal"
     */
    public Map<String, Object> loadClassifierMinimal(String tableName, String entityName, String code) {
        if (code == null || code.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM " + sanitizeTableName(tableName) + " WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            String name = row.get("name") != null ? row.get("name").toString() : "";
            nested.put("_instanceName", buildInstanceName(entityName, code, name));
            nested.put("id", code);
            return nested;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            nested.put("id", code);
            return nested;
        } catch (Exception e) {
            log.debug("Classifier minimal load error ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    public Map<String, Object> loadClassifierFull(String tableName, String entityName, String code) {
        if (code == null || code.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name, name_ru, name_en, active FROM " + sanitizeTableName(tableName) + " WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            String name = row.get("name") != null ? row.get("name").toString() : "";
            nested.put("_instanceName", buildInstanceName(entityName, code, name));
            nested.put("id", code);
            // OLD-HEMIS compatibility: include code, nameRu, nameEn, active fields
            nested.put("code", code);
            // OLD-HEMIS: null values must be serialized (returnNulls=true in views)
            nested.put("nameRu", row.get("name_ru") != null ? row.get("name_ru") : JsonNull.INSTANCE);
            nested.put("name", name);
            nested.put("active", row.get("active") != null ? row.get("active") : true);
            nested.put("nameEn", row.get("name_en") != null ? row.get("name_en") : JsonNull.INSTANCE);
            return nested;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            nested.put("id", code);
            return nested;
        } catch (Exception e) {
            log.debug("Classifier full load error ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    /**
     * Load university object - full CUBA format
     * Used by UniversityDepartment, Teacher, Diploma controllers
     */
    public Map<String, Object> loadUniversity(String universityCode) {
        if (universityCode == null || universityCode.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL", universityCode);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversity");
            // OLD-HEMIS: _instanceName format is "code-name"
            nested.put("_instanceName", str(row.get("code")) + "-" + row.get("name"));
            nested.put("id", str(row.get("code")));
            nested.put("code", str(row.get("code")));
            nested.put("name", row.get("name"));
            return nested;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversity");
            nested.put("id", universityCode);
            nested.put("code", universityCode);
            return nested;
        } catch (Exception e) {
            log.debug("University load error (code={}): {}", universityCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load university object for Teacher view - OLD-HEMIS format (24 fields only)
     * Used by TeacherLegacyService for eTeacher-view
     * Does NOT include: soato, universityActivityStatus, universityType, terrain,
     * soatoRegion, versionType, parentUniversity, universityContractCategory, ownership, belongsTo
     */
    public Map<String, Object> loadUniversityForTeacher(String universityCode) {
        if (universityCode == null || universityCode.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name, tin, address, student_url, teacher_url, active, " +
                    "add_student, accreditation_edit, allow_grouping, allow_transfer_outside, one_id, gpa_edit, " +
                    "add_foreign_student, grading_system, uzbmb_url, university_url, mail_address, bank_info, cadastre, accreditation_info, add_transfer_student, add_academic_mobile_student " +
                    "FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL", universityCode);

            // OLD-HEMIS field order for Teacher university (24 fields, NO nested objects)
            Map<String, Object> uni = new LinkedHashMap<>();
            uni.put("_entityName", "hemishe_EUniversity");
            uni.put("_instanceName", row.get("code") + "-" + row.get("name"));
            uni.put("id", str(row.get("code")));
            uni.put("studentUrl", row.get("student_url") != null ? str(row.get("student_url")) : JsonNull.INSTANCE);
            uni.put("gradingSystem", row.get("grading_system") != null ? row.get("grading_system") : JsonNull.INSTANCE);
            uni.put("accreditationInfo", row.get("accreditation_info") != null ? str(row.get("accreditation_info")) : JsonNull.INSTANCE);
            uni.put("uzbmbUrl", row.get("uzbmb_url") != null ? str(row.get("uzbmb_url")) : JsonNull.INSTANCE);
            uni.put("tin", row.get("tin") != null ? str(row.get("tin")) : JsonNull.INSTANCE);
            uni.put("universityUrl", row.get("university_url") != null ? str(row.get("university_url")) : JsonNull.INSTANCE);
            uni.put("addStudent", row.get("add_student") != null ? row.get("add_student") : true);
            uni.put("bankInfo", row.get("bank_info") != null ? str(row.get("bank_info")) : JsonNull.INSTANCE);
            uni.put("address", row.get("address") != null ? str(row.get("address")) : JsonNull.INSTANCE);
            uni.put("accreditationEdit", row.get("accreditation_edit") != null ? row.get("accreditation_edit") : false);
            uni.put("active", row.get("active") != null ? row.get("active") : false);
            uni.put("mailAddress", row.get("mail_address") != null ? str(row.get("mail_address")) : JsonNull.INSTANCE);
            uni.put("cadastre", row.get("cadastre") != null ? str(row.get("cadastre")) : JsonNull.INSTANCE);
            uni.put("oneId", row.get("one_id") != null ? row.get("one_id") : true);
            uni.put("allowGrouping", row.get("allow_grouping") != null ? row.get("allow_grouping") : true);
            uni.put("addForeignStudent", row.get("add_foreign_student") != null ? row.get("add_foreign_student") : JsonNull.INSTANCE);
            uni.put("teacherUrl", row.get("teacher_url") != null ? str(row.get("teacher_url")) : JsonNull.INSTANCE);
            uni.put("allowTransferOutside", row.get("allow_transfer_outside") != null ? row.get("allow_transfer_outside") : true);
            uni.put("name", str(row.get("name")));
            uni.put("gpaEdit", row.get("gpa_edit") != null ? row.get("gpa_edit") : false);
            uni.put("addTransferStudent", row.get("add_transfer_student") != null ? row.get("add_transfer_student") : JsonNull.INSTANCE);
            uni.put("addAcademicMobileStudent", row.get("add_academic_mobile_student") != null ? row.get("add_academic_mobile_student") : JsonNull.INSTANCE);

            return uni;
        } catch (EmptyResultDataAccessException e) {
            return loadUniversity(universityCode);
        } catch (Exception e) {
            log.debug("University for Teacher load error (code={}): {}", universityCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load university object - extended format with all fields
     * Used by UniversityDepartmentEntityController for view mode
     */
    public Map<String, Object> loadUniversityFull(String universityCode) {
        if (universityCode == null || universityCode.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name, tin, address, student_url, teacher_url, active, version, " +
                    "add_student, accreditation_edit, allow_grouping, allow_transfer_outside, one_id, gpa_edit, " +
                    "_university_type, _ownership, _university_contract_category, _soato, _soato_region, " +
                    "_university_activity_status, _university_belongs_to, _university_version, " +
                    "add_foreign_student, grading_system, uzbmb_url, university_url, mail_address, bank_info, accreditation_info, add_transfer_student, add_academic_mobile_student " +
                    "FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL", universityCode);

            // OLD-HEMIS field order for university nested object
            // Using JsonNull.INSTANCE for null values to ensure they are serialized
            Map<String, Object> uni = new LinkedHashMap<>();
            uni.put("_entityName", "hemishe_EUniversity");
            uni.put("_instanceName", row.get("code") + "-" + row.get("name"));
            uni.put("id", str(row.get("code")));
            uni.put("studentUrl", row.get("student_url") != null ? str(row.get("student_url")) : JsonNull.INSTANCE);
            uni.put("gradingSystem", row.get("grading_system") != null ? row.get("grading_system") : JsonNull.INSTANCE);
            uni.put("accreditationInfo", row.get("accreditation_info") != null ? str(row.get("accreditation_info")) : JsonNull.INSTANCE);

            // soato nested
            if (row.get("_soato") != null) {
                Map<String, Object> soatoObj = loadSoato(str(row.get("_soato")));
                if (soatoObj != null) uni.put("soato", soatoObj);
            }
            // universityActivityStatus - uses hyphen separator in _instanceName (e.g., "10-Ishlamoqda")
            if (row.get("_university_activity_status") != null)
                uni.put("universityActivityStatus", loadClassifierWithHyphen("hemishe_h_university_activity_status", "HUniversityActivityStatus", str(row.get("_university_activity_status"))));
            // universityType
            if (row.get("_university_type") != null)
                uni.put("universityType", loadClassifier("hemishe_h_university_type", "HUniversityType", str(row.get("_university_type"))));
            uni.put("uzbmbUrl", row.get("uzbmb_url") != null ? str(row.get("uzbmb_url")) : JsonNull.INSTANCE);
            uni.put("tin", row.get("tin") != null ? str(row.get("tin")) : JsonNull.INSTANCE);
            uni.put("universityUrl", row.get("university_url") != null ? str(row.get("university_url")) : JsonNull.INSTANCE);
            uni.put("terrain", JsonNull.INSTANCE); // OLD-HEMIS returns this field
            // soatoRegion
            if (row.get("_soato_region") != null) {
                Map<String, Object> soatoRegionObj = loadSoato(str(row.get("_soato_region")));
                if (soatoRegionObj != null) uni.put("soatoRegion", soatoRegionObj);
            }
            // versionType
            if (row.get("_university_version") != null)
                uni.put("versionType", loadClassifierWithNames("hemishe_h_hemis_version_type", "HHemisVersionType", str(row.get("_university_version"))));

            uni.put("addStudent", row.get("add_student") != null ? row.get("add_student") : true);
            uni.put("bankInfo", row.get("bank_info") != null ? str(row.get("bank_info")) : JsonNull.INSTANCE);
            uni.put("parentUniversity", JsonNull.INSTANCE); // OLD-HEMIS returns this field
            uni.put("address", row.get("address") != null ? str(row.get("address")) : JsonNull.INSTANCE);
            uni.put("accreditationEdit", row.get("accreditation_edit") != null ? row.get("accreditation_edit") : false);
            uni.put("active", row.get("active") != null ? row.get("active") : false);
            uni.put("mailAddress", row.get("mail_address") != null ? str(row.get("mail_address")) : JsonNull.INSTANCE);
            uni.put("cadastre", JsonNull.INSTANCE); // OLD-HEMIS returns this field
            // universityContractCategory
            if (row.get("_university_contract_category") != null)
                uni.put("universityContractCategory", loadClassifierWithNames("hemishe_h_university_contract_category", "HUniversityContractCategory", str(row.get("_university_contract_category"))));
            uni.put("oneId", row.get("one_id") != null ? row.get("one_id") : true);
            uni.put("allowGrouping", row.get("allow_grouping") != null ? row.get("allow_grouping") : true);
            uni.put("addForeignStudent", row.get("add_foreign_student") != null ? row.get("add_foreign_student") : JsonNull.INSTANCE);
            uni.put("teacherUrl", row.get("teacher_url") != null ? str(row.get("teacher_url")) : JsonNull.INSTANCE);
            uni.put("allowTransferOutside", row.get("allow_transfer_outside") != null ? row.get("allow_transfer_outside") : true);
            // ownership
            if (row.get("_ownership") != null)
                uni.put("ownership", loadClassifier("hemishe_h_ownership", "HOwnership", str(row.get("_ownership"))));
            uni.put("name", str(row.get("name")));
            uni.put("gpaEdit", row.get("gpa_edit") != null ? row.get("gpa_edit") : false);
            uni.put("addTransferStudent", row.get("add_transfer_student") != null ? row.get("add_transfer_student") : JsonNull.INSTANCE);
            uni.put("addAcademicMobileStudent", row.get("add_academic_mobile_student") != null ? row.get("add_academic_mobile_student") : JsonNull.INSTANCE);
            // belongsTo
            if (row.get("_university_belongs_to") != null)
                uni.put("belongsTo", loadClassifier("hemishe_h_university_belongs_to", "HUniversityBelongsTo", str(row.get("_university_belongs_to"))));

            return uni;
        } catch (EmptyResultDataAccessException e) {
            return loadUniversity(universityCode);
        } catch (Exception e) {
            log.debug("University full load error (code={}): {}", universityCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load university by UUID (CUBA id column) - resolves UUID to code then loads full object
     * Used when FK stores university UUID instead of code
     */
    public Map<String, Object> loadUniversityByUuid(UUID universityUuid) {
        if (universityUuid == null) return null;
        try {
            String code = jdbcTemplate.queryForObject(
                    "SELECT code FROM hemishe_e_university WHERE id = ?::uuid AND delete_ts IS NULL",
                    String.class, universityUuid);
            return loadUniversity(code);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversity");
            nested.put("id", universityUuid.toString());
            return nested;
        } catch (Exception e) {
            log.debug("University by UUID load error (uuid={}): {}", universityUuid, e.getMessage());
            return null;
        }
    }

    /**
     * Load department object
     * Used by Teacher, Diploma controllers
     */
    public Map<String, Object> loadDepartment(String departmentCode) {
        if (departmentCode == null || departmentCode.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, university_code, _deparment_type FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL", departmentCode);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversityDepartment");
            nested.put("_instanceName", row.get("name_uz"));
            nested.put("id", str(row.get("code")));
            nested.put("code", str(row.get("code")));
            nested.put("nameUz", row.get("name_uz"));
            nested.put("name", row.get("name_uz"));

            // deparmentType nested classifier (OLD-HEMIS typo preserved)
            if (row.get("_deparment_type") != null) {
                nested.put("deparmentType", loadClassifierNameOnly("hemishe_h_department_type", "HDepartmentType", str(row.get("_deparment_type"))));
            }

            // Nested university inside department
            String uniCode = row.get("university_code") != null ? row.get("university_code").toString() : null;
            if (uniCode != null) {
                Map<String, Object> uniNested = loadUniversity(uniCode);
                if (uniNested != null) nested.put("university", uniNested);
            }

            return nested;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversityDepartment");
            nested.put("id", departmentCode);
            nested.put("code", departmentCode);
            return nested;
        } catch (Exception e) {
            log.debug("Department load error (code={}): {}", departmentCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load SOATO object - OLD-HEMIS format
     * Used by University, Teacher, Diploma controllers
     * OLD-HEMIS compatibility: includes active field
     */
    public Map<String, Object> loadSoato(String soatoCode) {
        if (soatoCode == null || soatoCode.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, name_ru, active FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL", soatoCode);
            Map<String, Object> soato = new LinkedHashMap<>();
            soato.put("_entityName", "hemishe_HSoato");
            // OLD-HEMIS: _instanceName format is "code - name_uz"
            String nameUz = row.get("name_uz") != null ? str(row.get("name_uz")) : "";
            soato.put("_instanceName", row.get("code") + " - " + nameUz);
            soato.put("id", row.get("code"));
            // OLD-HEMIS compatibility: include code and active fields
            soato.put("code", str(row.get("code")));
            soato.put("active", row.get("active") != null ? row.get("active") : true);
            soato.put("name_ru", row.get("name_ru") != null ? str(row.get("name_ru")) : "");
            soato.put("name_uz", nameUz);
            return soato;
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            log.debug("SOATO load error (code={}): {}", soatoCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load SOATO for Teacher view - OLD-HEMIS format
     * Teacher soato: has code, NO active, NO name_ru
     */
    public Map<String, Object> loadSoatoForTeacher(String soatoCode) {
        if (soatoCode == null || soatoCode.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL", soatoCode);
            Map<String, Object> soato = new LinkedHashMap<>();
            soato.put("_entityName", "hemishe_HSoato");
            String nameUz = row.get("name_uz") != null ? str(row.get("name_uz")) : "";
            soato.put("_instanceName", row.get("code") + " - " + nameUz);
            soato.put("id", str(row.get("code")));
            // Teacher soato format: code bor, active yo'q, name_ru yo'q
            soato.put("code", str(row.get("code")));
            soato.put("name_uz", nameUz);
            return soato;
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            log.debug("SOATO teacher load error (code={}): {}", soatoCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load SOATO with _instanceName and parent_code (for top-level soato/currentSoato)
     * OLD-HEMIS compatibility: has parent_code, NO name_ru
     */
    public Map<String, Object> loadSoatoFull(String soatoCode) {
        if (soatoCode == null || soatoCode.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, name_ru, parent_code, active FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL", soatoCode);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HSoato");
            nested.put("_instanceName", row.get("code") + " - " + row.get("name_uz"));
            nested.put("id", row.get("code"));
            nested.put("code", row.get("code"));
            // OLD-HEMIS compatibility: include active and name_ru
            nested.put("active", row.get("active") != null ? row.get("active") : true);
            nested.put("name_ru", row.get("name_ru"));

            // parent_code nested object
            Object parentCode = row.get("parent_code");
            if (parentCode != null) {
                try {
                    Map<String, Object> parentRow = jdbcTemplate.queryForMap(
                            "SELECT code, name_uz, parent_code FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL",
                            String.valueOf(parentCode));
                    Map<String, Object> parent = new LinkedHashMap<>();
                    parent.put("_entityName", "hemishe_HSoato");
                    parent.put("_instanceName", parentRow.get("code") + " - " + parentRow.get("name_uz"));
                    parent.put("id", parentRow.get("code"));
                    parent.put("code", parentRow.get("code"));
                    // OLD-HEMIS: parent_code.parent_code = null (one level deep)
                    parent.put("parent_code", null);
                    parent.put("name_uz", parentRow.get("name_uz"));
                    nested.put("parent_code", parent);
                } catch (EmptyResultDataAccessException ex) {
                    // parent not found
                }
            }

            nested.put("name_uz", row.get("name_uz"));
            return nested;
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            log.debug("SOATO full load error (code={}): {}", soatoCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load SOATO for terrain's nested soato (has name_ru, NO parent_code)
     * OLD-HEMIS compatibility: terrain.soato and currentTerrain.soato have name_ru
     */
    public Map<String, Object> loadSoatoForTerrain(String soatoCode) {
        if (soatoCode == null || soatoCode.isEmpty()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, name_ru FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL", soatoCode);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HSoato");
            nested.put("_instanceName", row.get("code") + " - " + row.get("name_uz"));
            nested.put("id", row.get("code"));
            nested.put("code", row.get("code"));
            // OLD-HEMIS compatibility: terrain soato has name_ru, NO parent_code
            nested.put("name_ru", row.get("name_ru"));
            nested.put("name_uz", row.get("name_uz"));
            return nested;
        } catch (Exception e) {
            log.debug("SOATO terrain load error (code={}): {}", soatoCode, e.getMessage());
            return null;
        }
    }

    /**
     * Helper: put nested object into map, respecting returnNulls flag
     */
    public void putNested(Map<String, Object> map, String key, String code, String tableName, String entityName, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put(key, null);
            return;
        }
        Map<String, Object> nested = loadClassifier(tableName, entityName, code);
        if (nested != null) {
            map.put(key, nested);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    /**
     * Helper: put nested classifier with nameRu, nameEn, active fields (name-only _instanceName)
     * Used for: certificate sub-entities (HCertificateType, HCertificateNames, HCertificateGrades, HCertificateSubjects)
     */
    public void putNestedWithNames(Map<String, Object> map, String key, String code, String tableName, String entityName, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put(key, null);
            return;
        }
        Map<String, Object> nested = loadClassifierNameOnly(tableName, entityName, code);
        if (nested != null) {
            map.put(key, nested);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    /**
     * Load student _minimal view + specialityBachelor, specialityMaster, paymentForm
     * Used by rIAdministrativeStudent3-view
     */
    public Map<String, Object> loadStudentMinimalWithSpeciality(UUID studentId) {
        if (studentId == null) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    """
                    SELECT id, code, firstname, lastname, fathername,
                           _payment_form, _speciality_bachelor, _speciality_master
                    FROM hemishe_e_student WHERE id = ? AND delete_ts IS NULL
                    """,
                    studentId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", "hemishe_EStudent");
            String instanceName = String.format("%s %s %s",
                    row.get("lastname") != null ? row.get("lastname") : "",
                    row.get("firstname") != null ? row.get("firstname") : "",
                    row.get("fathername") != null ? row.get("fathername") : "").trim();
            result.put("_instanceName", instanceName);
            result.put("id", row.get("id"));

            // OLD-HEMIS _minimal view includes @NamePattern fields
            result.put("specialityMaster", null); // always present (returnNulls=true)
            result.put("lastname", row.get("lastname"));
            result.put("firstname", row.get("firstname"));

            // paymentForm — _minimal view (5 fields: _entityName, _instanceName, id, code, name)
            if (row.get("_payment_form") != null) {
                Map<String, Object> pfObj = loadClassifier("hemishe_h_payment_form", "HPaymentForm", String.valueOf(row.get("_payment_form")));
                if (pfObj != null) result.put("paymentForm", pfObj);
            }

            result.put("fathername", row.get("fathername"));

            // specialityBachelor — _minimal view
            putSpecialityOrNull(result, "specialityBachelor", row.get("_speciality_bachelor"),
                    "hemishe_HSpecialityBachelor", "hemishe_h_speciality_bachelor");
            // specialityMaster — already set above as null, override if present
            if (row.get("_speciality_master") != null) {
                putSpecialityOrNull(result, "specialityMaster", row.get("_speciality_master"),
                        "hemishe_HSpecialityMaster", "hemishe_h_speciality_master");
            }

            // OLD-HEMIS: returnNulls=true — null values must be serialized as JSON null
            result.replaceAll((k, v) -> v == null ? JsonNull.INSTANCE : v);
            return result;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", "hemishe_EStudent");
            result.put("_instanceName", studentId.toString());
            result.put("id", studentId);
            return result;
        } catch (Exception e) {
            log.debug("Student minimal load error (id={}): {}", studentId, e.getMessage());
            return null;
        }
    }

    /**
     * Load student object for diploma view (full format with 48+ fields)
     * OLD-HEMIS formatida to'liq student objectni qaytaradi
     */
    public Map<String, Object> loadStudentForDiploma(UUID studentId) {
        if (studentId == null) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    """
                    SELECT id, version, code, firstname, lastname, fathername, birthday, pinfl,
                           serial_number, _university, _soato, address, current_address,
                           _payment_form, _education_form, _education_type, _education_year,
                           _country, _gender, _language, _student_status, _citizenship,
                           _social_category, _course, _speciality, _faculty, _nationality,
                           _accomodation, _current_soato, _terrain, current_terrain_code,
                           _roommate_type, _living_status, _poverty_level, _student_type,
                           _academic_mobile_type, _grant_type, _admission_type, _transfer_type,
                           _transfer_country, transfer_university, _expel_reason, _academic_reason,
                           active, tag, phone, email, parent_phone, responsible_person_phone,
                           geo_address, group_name, group_id, edu_start_date,
                           graduation_date, study_duration, is_duplicate, is_graduate,
                           status_order_name, status_order_date, status_order_number,
                           status_order_category, enroll_order_category, enroll_order_number,
                           enroll_order_date, enroll_order_name, verified, points,
                           roommate_count, decree_info_name, decree_info_number, decree_info_date,
                           passport_given_date, status, _current_education_year_code,
                           status_education_year_code, _graduation_year,
                           _speciality_bachelor, _speciality_master
                    FROM hemishe_e_student WHERE id = ? AND delete_ts IS NULL
                    """,
                    studentId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", "hemishe_EStudent");
            String instanceName = String.format("%s %s %s",
                    row.get("lastname") != null ? row.get("lastname") : "",
                    row.get("firstname") != null ? row.get("firstname") : "",
                    row.get("fathername") != null ? row.get("fathername") : "").trim();
            result.put("_instanceName", instanceName);
            result.put("id", row.get("id"));
            result.put("isGraduate", row.get("is_graduate"));
            result.put("statusOrderDate", row.get("status_order_date"));
            result.put("decreeInfoName", row.get("decree_info_name"));
            result.put("groupId", row.get("group_id"));

            // Nested classifiers
            putClassifierIfNotNull(result, "educationYear", row.get("_education_year"),
                    "hemishe_HEducationYear", "hemishe_h_education_year");
            putClassifierIfNotNull(result, "educationForm", row.get("_education_form"),
                    "hemishe_HEducationForm", "hemishe_h_education_form");

            result.put("points", row.get("points"));
            result.put("tag", row.get("tag"));
            result.put("decreeInfoNumber", row.get("decree_info_number"));
            result.put("responsiblePersonPhone", row.get("responsible_person_phone"));
            result.put("geoAddress", row.get("geo_address"));
            result.put("serialNumber", row.get("serial_number"));
            result.put("active", row.get("active"));
            result.put("statusOrderCategory", row.get("status_order_category"));
            result.put("decreeInfoDate", row.get("decree_info_date"));
            result.put("lastname", row.get("lastname"));
            result.put("groupName", row.get("group_name"));
            result.put("statusOrderNumber", row.get("status_order_number"));
            result.put("phone", row.get("phone"));
            result.put("status", row.get("status"));
            result.put("enrollOrderName", row.get("enroll_order_name"));
            result.put("pinfl", row.get("pinfl"));
            result.put("birthday", row.get("birthday"));
            result.put("firstname", row.get("firstname"));
            result.put("code", row.get("code"));

            // Additional nested classifiers for full 48-field support
            putClassifierIfNotNull(result, "paymentForm", row.get("_payment_form"),
                    "hemishe_HPaymentForm", "hemishe_h_payment_form");
            // speciality bachelor/master — FK is UUID, lookup by id
            // Always include keys (old-hemis returnNulls=true → null serialized)
            putSpecialityOrNull(result, "specialityBachelor", row.get("_speciality_bachelor"),
                    "hemishe_HSpecialityBachelor", "hemishe_h_speciality_bachelor");
            putSpecialityOrNull(result, "specialityMaster", row.get("_speciality_master"),
                    "hemishe_HSpecialityMaster", "hemishe_h_speciality_master");
            putClassifierIfNotNull(result, "gender", row.get("_gender"),
                    "hemishe_HGender", "hemishe_h_gender");
            putClassifierIfNotNull(result, "citizenship", row.get("_citizenship"),
                    "hemishe_HCitizenship", "hemishe_h_citizenship");
            putClassifierIfNotNull(result, "nationality", row.get("_nationality"),
                    "hemishe_HNationality", "hemishe_h_nationality");
            putClassifierIfNotNull(result, "educationType", row.get("_education_type"),
                    "hemishe_HEducationType", "hemishe_h_education_type");
            putClassifierIfNotNull(result, "course", row.get("_course"),
                    "hemishe_HCourse", "hemishe_h_course");
            putClassifierIfNotNull(result, "studentStatus", row.get("_student_status"),
                    "hemishe_HStudentStatusType", "hemishe_h_student_status_type");
            putClassifierIfNotNull(result, "country", row.get("_country"),
                    "hemishe_HCountry", "hemishe_h_country");
            putClassifierIfNotNull(result, "accomodation", row.get("_accomodation"),
                    "hemishe_HAccomodation", "hemishe_h_accomodation");
            putClassifierIfNotNull(result, "language", row.get("_language"),
                    "hemishe_HEducationLanguage", "hemishe_h_education_language");
            putClassifierIfNotNull(result, "socialCategory", row.get("_social_category"),
                    "hemishe_HStudentSocialType", "hemishe_h_student_social_type");
            putClassifierIfNotNull(result, "studentType", row.get("_student_type"),
                    "hemishe_HStudentType", "hemishe_h_student_type");
            putClassifierIfNotNull(result, "povertyLevel", row.get("_poverty_level"),
                    "hemishe_HPovertyLevel", "hemishe_h_poverty_level");
            putClassifierIfNotNull(result, "grantType", row.get("_grant_type"),
                    "hemishe_HGrantType", "hemishe_h_grant_type");
            putClassifierIfNotNull(result, "admissionType", row.get("_admission_type"),
                    "hemishe_HAdmissionType", "hemishe_h_admission_type");
            putClassifierIfNotNull(result, "transferType", row.get("_transfer_type"),
                    "hemishe_HTransferType", "hemishe_h_transfer_type");
            putClassifierIfNotNull(result, "transferCountry", row.get("_transfer_country"),
                    "hemishe_HCountry", "hemishe_h_country");
            putClassifierIfNotNull(result, "expelReason", row.get("_expel_reason"),
                    "hemishe_HExpel", "hemishe_h_expel");
            putClassifierIfNotNull(result, "academicReason", row.get("_academic_reason"),
                    "hemishe_HAcademicReason", "hemishe_h_academic_reason");
            putClassifierIfNotNull(result, "academicMobileType", row.get("_academic_mobile_type"),
                    "hemishe_HAcademicMobileType", "hemishe_h_academic_mobile_type");
            putClassifierIfNotNull(result, "roommateType", row.get("_roommate_type"),
                    "hemishe_HStudentRoomMateType", "hemishe_h_student_room_mate_type");
            putClassifierIfNotNull(result, "livingStatus", row.get("_living_status"),
                    "hemishe_HStudentLivingStatus", "hemishe_h_student_living_status");
            putClassifierIfNotNull(result, "terrain", row.get("_terrain"),
                    "hemishe_HTerrain", "hemishe_h_terrain");
            putClassifierIfNotNull(result, "currentTerrain", row.get("current_terrain_code"),
                    "hemishe_HTerrain", "hemishe_h_terrain");
            putClassifierIfNotNull(result, "statusEducationYear", row.get("status_education_year_code"),
                    "hemishe_HEducationYear", "hemishe_h_education_year");
            putClassifierIfNotNull(result, "currentEducationYear", row.get("_current_education_year_code"),
                    "hemishe_HEducationYear", "hemishe_h_education_year");
            putClassifierIfNotNull(result, "graduationYear", row.get("_graduation_year"),
                    "hemishe_HEducationYear", "hemishe_h_education_year");

            // soato
            if (row.get("_soato") != null) {
                Map<String, Object> soatoObj = loadSoatoFull(str(row.get("_soato")));
                if (soatoObj != null) result.put("soato", soatoObj);
            }
            if (row.get("_current_soato") != null) {
                Map<String, Object> currentSoatoObj = loadSoatoFull(str(row.get("_current_soato")));
                if (currentSoatoObj != null) result.put("currentSoato", currentSoatoObj);
            }

            // university
            if (row.get("_university") != null) {
                Map<String, Object> uniObj = loadUniversity(str(row.get("_university")));
                if (uniObj != null) result.put("university", uniObj);
            }

            // faculty (department)
            if (row.get("_faculty") != null) {
                Map<String, Object> facultyObj = loadDepartment(str(row.get("_faculty")));
                if (facultyObj != null) result.put("faculty", facultyObj);
            }

            result.put("parentPhone", row.get("parent_phone"));
            result.put("speciality", row.get("_speciality"));
            result.put("enrollOrderDate", row.get("enroll_order_date"));
            result.put("enrollOrderNumber", row.get("enroll_order_number"));
            result.put("roommateCount", row.get("roommate_count"));
            result.put("isDuplicate", row.get("is_duplicate"));
            result.put("email", row.get("email"));
            result.put("address", row.get("address"));
            result.put("eduStartDate", row.get("edu_start_date"));
            result.put("passportGivenDate", row.get("passport_given_date"));
            result.put("verified", row.get("verified"));
            result.put("currentAddress", row.get("current_address"));
            result.put("fathername", row.get("fathername"));
            result.put("graduationDate", row.get("graduation_date"));
            result.put("statusOrderName", row.get("status_order_name"));
            result.put("enrollOrderCategory", row.get("enroll_order_category"));
            result.put("studyDuration", row.get("study_duration"));
            result.put("transferUniversity", row.get("transfer_university"));
            result.put("version", row.get("version"));

            // OLD-HEMIS: returnNulls=true — null values must be serialized
            result.replaceAll((k, v) -> v == null ? JsonNull.INSTANCE : v);
            return result;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", "hemishe_EStudent");
            result.put("_instanceName", studentId.toString());
            result.put("id", studentId);
            return result;
        } catch (Exception e) {
            log.debug("Student load error (id={}): {}", studentId, e.getMessage());
            return null;
        }
    }

    // Internal helper for classifier inside student/other complex objects
    private void putClassifierIfNotNull(Map<String, Object> map, String key, Object code,
                                         String entityName, String tableName) {
        if (code == null) return;
        Map<String, Object> nested = loadClassifierFull(tableName, entityName, String.valueOf(code));
        if (nested != null) {
            map.put(key, nested);
        }
    }

    /**
     * Load speciality by UUID id — for specialityBachelor/specialityMaster (PK is UUID, not code)
     * Returns _minimal view: _entityName, _instanceName, id, code, name
     * Always puts key (null if not found — matches old-hemis returnNulls=true)
     */
    private void putSpecialityOrNull(Map<String, Object> map, String key, Object uuidVal,
                                      String entityName, String tableName) {
        if (uuidVal == null) {
            map.put(key, null);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT id, code, name FROM " + sanitizeTableName(tableName) + " WHERE id = ?::uuid AND delete_ts IS NULL",
                    uuidVal);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            String name = row.get("name") != null ? row.get("name").toString() : "";
            String code = row.get("code") != null ? row.get("code").toString() : "";
            nested.put("_instanceName", code + " - " + name);
            nested.put("id", row.get("id"));
            nested.put("code", code);
            nested.put("name", name);
            map.put(key, nested);
        } catch (EmptyResultDataAccessException e) {
            map.put(key, null);
        } catch (Exception e) {
            log.debug("Speciality load error ({}, id={}): {}", tableName, uuidVal, e.getMessage());
            map.put(key, null);
        }
    }

    /**
     * Sanitize table name to prevent SQL injection
     * Only allows alphanumeric, underscore characters
     */
    private String sanitizeTableName(String tableName) {
        if (tableName == null || !tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        return tableName;
    }

    private String str(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }
}
