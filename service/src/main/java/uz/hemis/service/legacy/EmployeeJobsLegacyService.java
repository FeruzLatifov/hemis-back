package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.EmployeeJobs;
import uz.hemis.domain.repository.EmployeeJobsRepository;

import uz.hemis.common.JsonNull;

import java.util.*;

/**
 * EmployeeJobs nested object loading service.
 *
 * <p>Extracted from EmployeeJobsEntityController to remove direct repository
 * dependencies for nested object loading. Uses JdbcTemplate for CUBA-compatible
 * classifier/reference lookups (same pattern as {@link CubaNestedObjectLoader}).</p>
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeJobsLegacyService {

    private final JdbcTemplate jdbcTemplate;
    private final CubaNestedObjectLoader nestedObjectLoader;
    private final EmployeeJobsRepository employeeJobsRepository;

    private static final String ENTITY_NAME = "hemishe_EEmployeeJobs";

    // ==================== CRUD Operations ====================

    public Optional<EmployeeJobs> findById(UUID id) {
        return employeeJobsRepository.findById(id);
    }

    public List<EmployeeJobs> findByUniversity(String universityCode) {
        return employeeJobsRepository.findByUniversityCode(universityCode);
    }

    public List<EmployeeJobs> findByEmployeePinfl(String pinfl) {
        return employeeJobsRepository.findByEmployeePinfl(pinfl);
    }

    public List<EmployeeJobs> findByEmployeeId(UUID employeeId) {
        return employeeJobsRepository.findByEmployeeId(employeeId);
    }

    public Page<EmployeeJobs> findAllByUniversity(String universityCode, PageRequest pageRequest) {
        return employeeJobsRepository.findByUniversityCode(universityCode, pageRequest);
    }

    @Transactional
    public EmployeeJobs save(EmployeeJobs entity) {
        return employeeJobsRepository.save(entity);
    }

    @Transactional
    public EmployeeJobs saveAndFlush(EmployeeJobs entity) {
        return employeeJobsRepository.saveAndFlush(entity);
    }

    @Transactional
    public void delete(EmployeeJobs entity) {
        // Soft delete — AuditableEntity pattern (deleted_at, @SQLRestriction)
        entity.softDelete();
        employeeJobsRepository.save(entity);
    }

    /**
     * Convert EmployeeJobs row data to CUBA-compatible Map with nested objects.
     *
     * @param entityId    entity UUID
     * @param employee    employee UUID
     * @param university  university code
     * @param department  department code
     * @param employeeType employee type code
     * @param employeePosition position code
     * @param employeeRate rate code
     * @param employeeForm form code
     * @param employeeStatus status code
     * @param version     entity version
     * @param tag         tag field
     * @param contractDate contract date
     * @param contractNumber contract number
     * @param jobStartDate job start date
     * @param jobEndDate   job end date
     * @param decreeDate   decree date
     * @param decreeNumber decree number
     * @param returnNulls  whether to include null values
     * @param view         view name (for extended nested objects)
     * @return CUBA-compatible map
     */
    public Map<String, Object> toMap(
            UUID entityId, UUID employee, String university, String department,
            String employeeType, String employeePosition, String employeeRate,
            String employeeForm, String employeeStatus,
            Integer version, String tag,
            Object contractDate, String contractNumber,
            Object jobStartDate, Object jobEndDate,
            Object decreeDate, String decreeNumber,
            Boolean returnNulls, String view) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(employee, department, employeeRate));
        map.put("id", entityId);

        // _local = faqat scalar fieldlar (CUBA _local view)
        // default (no view) = scalar + employee basic reference
        // custom view = to'liq nested objectlar (eEmployeeJobs-view)
        boolean isLocalView = "_local".equals(view);
        boolean useFullView = view != null && !view.isEmpty() && !isLocalView;

        // OLD-HEMIS eEmployeeJobs-view field order
        putIfNotNull(map, "jobEndDate", jobEndDate, returnNulls);
        putIfNotNull(map, "contractDate", contractDate, returnNulls);
        if (useFullView) {
            putNestedUniversity(map, university, returnNulls);
        }
        putIfNotNull(map, "jobStartDate", jobStartDate, returnNulls);
        if (useFullView) {
            putNestedEmployeeForm(map, employeeForm, returnNulls);
        }
        putIfNotNull(map, "contractNumber", contractNumber, returnNulls);
        if (useFullView) {
            // Full employee with birthday, code, gender, academicDegree, academicRank
            putNestedEmployee(map, employee, returnNulls);
            putNestedClassifier(map, "employeeStatus", "hemishe_h_university_employee_status_type",
                    "hemishe_HUniversityEmployeeStatusType", employeeStatus, returnNulls);
        } else if (!isLocalView) {
            // Default view: basic employee reference only
            putNestedEmployeeBasic(map, employee, returnNulls);
        }
        putIfNotNull(map, "decreeDate", decreeDate, returnNulls);
        putIfNotNull(map, "decreeNumber", decreeNumber, returnNulls);
        if (useFullView) {
            putNestedClassifier(map, "employeeType", "hemishe_h_university_employee_type",
                    "hemishe_HUniversityEmployeeType", employeeType, returnNulls);
            putNestedRate(map, employeeRate, returnNulls);
            putNestedClassifier(map, "employeePosition", "hemishe_h_teacher_position_type",
                    "hemishe_HTeacherPositionType", employeePosition, returnNulls);
        }
        putIfNotNull(map, "tag", tag, returnNulls);
        if (useFullView) {
            putNestedDepartment(map, department, returnNulls);
        }

        return map;
    }

    /**
     * Build OLD-HEMIS compatible instance name: "FULLNAME DepartmentName RateName"
     */
    public String buildInstanceName(UUID employeeId, String departmentCode, String rateCode) {
        StringBuilder sb = new StringBuilder();
        sb.append(getEmployeeFullName(employeeId));

        if (departmentCode != null && !departmentCode.isEmpty()) {
            try {
                Map<String, Object> row = jdbcTemplate.queryForMap(
                        "SELECT name_uz FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL",
                        departmentCode);
                String deptName = row.get("name_uz") != null ? row.get("name_uz").toString() : null;
                if (deptName != null && !deptName.isEmpty()) {
                    sb.append(" ").append(deptName);
                }
            } catch (Exception e) {
                log.debug("Failed to fetch department name for instanceName: code={}, error={}", departmentCode, e.getMessage());
            }
        }

        if (rateCode != null && !rateCode.isEmpty()) {
            try {
                Map<String, Object> row = jdbcTemplate.queryForMap(
                        "SELECT name FROM hemishe_h_university_employee_rate WHERE code = ? AND delete_ts IS NULL",
                        rateCode);
                String rateName = row.get("name") != null ? row.get("name").toString() : null;
                if (rateName != null && !rateName.isEmpty()) {
                    sb.append(" ").append(rateName);
                }
            } catch (Exception e) {
                log.debug("Failed to fetch rate name for instanceName: code={}, error={}", rateCode, e.getMessage());
            }
        }

        return sb.toString();
    }

    /**
     * Get employee full name by UUID (for _instanceName).
     * Returns "FAMILIYA ISM OTASI" format (uppercase).
     */
    public String getEmployeeFullName(UUID employeeId) {
        if (employeeId == null) {
            return "Unknown Employee";
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT firstname, lastname, fathername FROM hemishe_e_teacher WHERE id = ? AND delete_ts IS NULL",
                    employeeId);
            String fullName = buildFullName(
                    str(row.get("firstname")),
                    str(row.get("lastname")),
                    str(row.get("fathername")));
            return fullName.toUpperCase();
        } catch (EmptyResultDataAccessException e) {
            return "Employee-" + employeeId;
        } catch (Exception e) {
            log.debug("Failed to fetch employee name: {}", e.getMessage());
            return "Employee-" + employeeId;
        }
    }

    /**
     * Load basic employee reference (CUBA default view — id, name fields only, no classifiers).
     */
    private void putNestedEmployeeBasic(Map<String, Object> map, UUID employeeId, Boolean returnNulls) {
        if (employeeId == null) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("employee", JsonNull.INSTANCE);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT id, firstname, lastname, fathername " +
                    "FROM hemishe_e_teacher WHERE id = ? AND delete_ts IS NULL",
                    employeeId);
            String fullName = buildFullName(str(row.get("firstname")), str(row.get("lastname")), str(row.get("fathername")));
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_ETeacher");
            nested.put("_instanceName", fullName);
            nested.put("id", row.get("id"));
            nested.put("firstname", row.get("firstname"));
            nested.put("lastname", row.get("lastname"));
            nested.put("fathername", row.get("fathername"));
            nested.put("fullname", fullName);
            map.put("employee", nested);
        } catch (Exception e) {
            log.debug("Failed to fetch employee basic: {}", e.getMessage());
            map.put("employee", buildFallbackEmployee(employeeId));
        }
    }

    /**
     * Load nested employee object (hemishe_ETeacher).
     */
    public void putNestedEmployee(Map<String, Object> map, UUID employeeId, Boolean returnNulls) {
        if (employeeId == null) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("employee", JsonNull.INSTANCE);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT id, firstname, lastname, fathername, code, birthday, " +
                    "_gender, _academic_degree, _academic_rank " +
                    "FROM hemishe_e_teacher WHERE id = ? AND delete_ts IS NULL",
                    employeeId);
            String fullName = buildFullName(str(row.get("firstname")), str(row.get("lastname")), str(row.get("fathername")));
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_ETeacher");
            nested.put("_instanceName", fullName);
            nested.put("id", row.get("id"));
            // OLD-HEMIS eEmployeeJobs-view field order: birthday, firstname, code, gender, academicDegree, academicRank, lastname, fathername, fullname
            nested.put("birthday", row.get("birthday"));
            nested.put("firstname", row.get("firstname"));
            nested.put("code", str(row.get("code")));
            putEmployeeClassifier(nested, "gender", str(row.get("_gender")),
                    "hemishe_h_gender", "HGender");
            putEmployeeClassifier(nested, "academicDegree", str(row.get("_academic_degree")),
                    "hemishe_h_academic_degree", "HAcademicDegree");
            putEmployeeClassifier(nested, "academicRank", str(row.get("_academic_rank")),
                    "hemishe_h_academic_rank", "HAcademicRank");
            nested.put("lastname", row.get("lastname"));
            nested.put("fathername", row.get("fathername"));
            nested.put("fullname", fullName);
            map.put("employee", nested);
        } catch (EmptyResultDataAccessException e) {
            // Employee topilmadi - OLD-HEMIS compatible fallback
            map.put("employee", buildFallbackEmployee(employeeId));
        } catch (Exception e) {
            log.debug("Failed to fetch employee: {}", e.getMessage());
            map.put("employee", buildFallbackEmployee(employeeId));
        }
    }

    /**
     * Put classifier nested object inside employee map (gender, academicDegree, academicRank).
     */
    private void putEmployeeClassifier(Map<String, Object> nested, String key, String code,
                                        String tableName, String entityName) {
        if (code != null && !code.isEmpty()) {
            Map<String, Object> obj = nestedObjectLoader.loadClassifier(tableName, entityName, code);
            if (obj != null) {
                nested.put(key, obj);
                return;
            }
        }
        nested.put(key, JsonNull.INSTANCE);
    }

    private void putNestedUniversity(Map<String, Object> map, String universityCode, Boolean returnNulls) {
        if (universityCode == null || universityCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("university", JsonNull.INSTANCE);
            return;
        }
        // OLD-HEMIS eEmployeeJobs-view: full university (14+ fields) based on Teacher format
        Map<String, Object> nested = nestedObjectLoader.loadUniversityForTeacher(universityCode);
        if (nested != null) {
            // Strip fields not present in OLD-HEMIS eEmployeeJobs-view university
            nested.remove("accreditationInfo");
            nested.remove("uzbmbUrl");
            nested.remove("universityUrl");
            nested.remove("bankInfo");
            nested.remove("mailAddress");
            nested.remove("cadastre");
            nested.remove("addForeignStudent");
            nested.remove("addTransferStudent");
            map.put("university", nested);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("university", JsonNull.INSTANCE);
        }
    }

    private void putNestedDepartment(Map<String, Object> map, String departmentCode, Boolean returnNulls) {
        if (departmentCode == null || departmentCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("department", JsonNull.INSTANCE);
            return;
        }
        // OLD-HEMIS eEmployeeJobs-view: full department with university, deparmentType, nameRu, status
        Map<String, Object> nested = nestedObjectLoader.loadDepartment(departmentCode);
        if (nested != null) {
            // Strip fields not in OLD-HEMIS eEmployeeJobs-view department
            nested.remove("code");
            nested.remove("parent");
            map.put("department", nested);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("department", JsonNull.INSTANCE);
        }
    }

    private void putNestedRate(Map<String, Object> map, String rateCode, Boolean returnNulls) {
        if (rateCode == null || rateCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("employeeRate", JsonNull.INSTANCE);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_h_university_employee_rate WHERE code = ? AND delete_ts IS NULL",
                    rateCode);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HUniversityEmployeeRate");
            nested.put("_instanceName", str(row.get("name")));
            nested.put("id", str(row.get("code")));
            // OLD-HEMIS: code and version NOT included in eEmployeeJobs-view
            nested.put("name", str(row.get("name")));
            map.put("employeeRate", nested);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HUniversityEmployeeRate");
            nested.put("id", rateCode);
            map.put("employeeRate", nested);
        } catch (Exception e) {
            log.debug("Failed to fetch rate: {}", e.getMessage());
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HUniversityEmployeeRate");
            nested.put("id", rateCode);
            map.put("employeeRate", nested);
        }
    }

    private void putNestedClassifier(Map<String, Object> map, String key, String tableName,
                                     String entityName, String code, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put(key, JsonNull.INSTANCE);
            return;
        }
        Map<String, Object> nested = nestedObjectLoader.loadClassifier(tableName, entityName.replace("hemishe_", ""), code);
        if (nested != null) {
            // Override _entityName to use full name format
            nested.put("_entityName", entityName);
            nested.put("_instanceName", nested.get("name"));
            // OLD-HEMIS doesn't include .code in EmployeeJobs classifiers
            nested.remove("code");
            map.put(key, nested);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, JsonNull.INSTANCE);
        }
    }

    private void putNestedEmployeeForm(Map<String, Object> map, String formCode, Boolean returnNulls) {
        if (formCode == null || formCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("employeeForm", JsonNull.INSTANCE);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_h_university_employee_form WHERE code = ? AND delete_ts IS NULL",
                    formCode);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HUniversityEmployeeForm");
            String name = row.get("name") != null ? str(row.get("name")) : "";
            nested.put("_instanceName", formCode + " " + name);
            nested.put("id", formCode);
            nested.put("name", name);
            map.put("employeeForm", nested);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HUniversityEmployeeForm");
            nested.put("id", formCode);
            map.put("employeeForm", nested);
        } catch (Exception e) {
            log.debug("Failed to fetch employeeForm: {}", e.getMessage());
            if (Boolean.TRUE.equals(returnNulls)) map.put("employeeForm", JsonNull.INSTANCE);
        }
    }

    private String buildFullName(String firstName, String secondName, String thirdName) {
        StringBuilder sb = new StringBuilder();
        if (secondName != null && !secondName.isEmpty()) sb.append(secondName);
        if (firstName != null && !firstName.isEmpty()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(firstName);
        }
        if (thirdName != null && !thirdName.isEmpty()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(thirdName);
        }
        return !sb.isEmpty() ? sb.toString() : "Employee";
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            // Jackson NON_NULL global config skips null values,
            // JsonNull.INSTANCE serializes as JSON null while bypassing NON_NULL filter.
            map.put(key, JsonNull.INSTANCE);
        }
    }

    /**
     * Build fallback employee nested object when teacher not found in DB.
     * Matches OLD-HEMIS eEmployeeJobs-view employee field structure.
     */
    private Map<String, Object> buildFallbackEmployee(UUID employeeId) {
        String fallbackName = "Employee-" + employeeId;
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_ETeacher");
        nested.put("_instanceName", fallbackName);
        nested.put("id", employeeId);
        nested.put("birthday", JsonNull.INSTANCE);
        nested.put("firstname", "");
        nested.put("code", JsonNull.INSTANCE);
        nested.put("gender", JsonNull.INSTANCE);
        nested.put("academicDegree", JsonNull.INSTANCE);
        nested.put("academicRank", JsonNull.INSTANCE);
        nested.put("lastname", "");
        nested.put("fathername", "");
        nested.put("fullname", fallbackName);
        return nested;
    }

    private String str(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }
}
