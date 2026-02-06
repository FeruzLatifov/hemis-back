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

    private static final String ENTITY_NAME = "hemishe_EEmployeeJob";

    // ==================== CRUD Operations ====================

    public Optional<EmployeeJobs> findById(UUID id) {
        return employeeJobsRepository.findById(id);
    }

    public List<EmployeeJobs> findByUniversity(String universityCode) {
        return employeeJobsRepository.findByUniversity(universityCode);
    }

    public List<EmployeeJobs> findByEmployeeCode(String employeeCode) {
        return employeeJobsRepository.findByEmployeeCode(employeeCode);
    }

    public List<EmployeeJobs> findByEmployeeId(UUID employeeId) {
        return employeeJobsRepository.findByEmployeeId(employeeId);
    }

    public Page<EmployeeJobs> findAllByUniversity(String universityCode, PageRequest pageRequest) {
        return employeeJobsRepository.findAllByUniversity(universityCode, pageRequest);
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
        employeeJobsRepository.delete(entity);
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

        putIfNotNull(map, "contractDate", contractDate, returnNulls);
        putIfNotNull(map, "jobStartDate", jobStartDate, returnNulls);
        putIfNotNull(map, "contractNumber", contractNumber, returnNulls);

        putNestedEmployee(map, employee, returnNulls);

        putIfNotNull(map, "version", version, returnNulls);
        putIfNotNull(map, "decreeDate", decreeDate, returnNulls);
        putIfNotNull(map, "decreeNumber", decreeNumber, returnNulls);

        putNestedRate(map, employeeRate, returnNulls);
        putIfNotNull(map, "tag", tag, returnNulls);
        putNestedDepartment(map, department, returnNulls);
        putIfNotNull(map, "jobEndDate", jobEndDate, returnNulls);

        boolean useFullView = view != null && !view.isEmpty();
        if (useFullView) {
            putNestedUniversity(map, university, returnNulls);
            putNestedClassifier(map, "employeeType", "hemishe_h_university_employee_type",
                    "hemishe_HUniversityEmployeeType", employeeType, returnNulls);
            putNestedClassifier(map, "employeePosition", "hemishe_h_teacher_position_type",
                    "hemishe_HTeacherPositionType", employeePosition, returnNulls);
            putNestedEmployeeForm(map, employeeForm, returnNulls);
            putNestedClassifier(map, "employeeStatus", "hemishe_h_university_employee_status_type",
                    "hemishe_HUniversityEmployeeStatusType", employeeStatus, returnNulls);
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
                        "SELECT name_uz, name FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL",
                        departmentCode);
                String deptName = row.get("name_uz") != null ? row.get("name_uz").toString() :
                        (row.get("name") != null ? row.get("name").toString() : null);
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
                    "SELECT first_name, second_name, third_name FROM hemishe_e_teacher WHERE id = ? AND delete_ts IS NULL",
                    employeeId);
            String fullName = buildFullName(
                    str(row.get("first_name")),
                    str(row.get("second_name")),
                    str(row.get("third_name")));
            return fullName.toUpperCase();
        } catch (EmptyResultDataAccessException e) {
            return "Employee-" + employeeId;
        } catch (Exception e) {
            log.debug("Failed to fetch employee name: {}", e.getMessage());
            return "Employee-" + employeeId;
        }
    }

    /**
     * Load nested employee object (hemishe_ETeacher).
     */
    public void putNestedEmployee(Map<String, Object> map, UUID employeeId, Boolean returnNulls) {
        if (employeeId == null) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("employee", null);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT id, first_name, second_name, third_name, version FROM hemishe_e_teacher WHERE id = ? AND delete_ts IS NULL",
                    employeeId);
            String fullName = buildFullName(str(row.get("first_name")), str(row.get("second_name")), str(row.get("third_name")));
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_ETeacher");
            nested.put("_instanceName", fullName);
            nested.put("id", row.get("id"));
            nested.put("firstname", row.get("first_name"));
            nested.put("version", row.get("version") != null ? ((Number) row.get("version")).intValue() : 1);
            nested.put("lastname", row.get("second_name"));
            nested.put("fathername", row.get("third_name"));
            nested.put("fullname", fullName);
            map.put("employee", nested);
        } catch (EmptyResultDataAccessException e) {
            // Employee topilmadi - OLD-HEMIS compatible fallback
            // Note: Jackson non_null bo'lgani uchun null maydonlar ko'rinmaydi
            // Shu sababli empty string ishlatamiz
            String fallbackName = "Employee-" + employeeId;
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_ETeacher");
            nested.put("_instanceName", fallbackName);
            nested.put("id", employeeId);
            nested.put("firstname", "");
            nested.put("version", 1);
            nested.put("lastname", "");
            nested.put("fathername", "");
            nested.put("fullname", fallbackName);
            map.put("employee", nested);
        } catch (Exception e) {
            log.debug("Failed to fetch employee: {}", e.getMessage());
            // Employee xatosi - OLD-HEMIS compatible fallback
            String fallbackName = "Employee-" + employeeId;
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_ETeacher");
            nested.put("_instanceName", fallbackName);
            nested.put("id", employeeId);
            nested.put("firstname", "");
            nested.put("version", 1);
            nested.put("lastname", "");
            nested.put("fathername", "");
            nested.put("fullname", fallbackName);
            map.put("employee", nested);
        }
    }

    private void putNestedUniversity(Map<String, Object> map, String universityCode, Boolean returnNulls) {
        if (universityCode == null || universityCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("university", null);
            return;
        }
        Map<String, Object> nested = nestedObjectLoader.loadUniversity(universityCode);
        if (nested != null) {
            map.put("university", nested);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("university", null);
        }
    }

    private void putNestedDepartment(Map<String, Object> map, String departmentCode, Boolean returnNulls) {
        if (departmentCode == null || departmentCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("department", null);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, name, version FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL",
                    departmentCode);
            String deptName = row.get("name_uz") != null ? row.get("name_uz").toString() :
                    (row.get("name") != null ? row.get("name").toString() : "");
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversityDepartment");
            nested.put("_instanceName", deptName);
            nested.put("id", str(row.get("code")));
            nested.put("code", str(row.get("code")));
            nested.put("version", row.get("version") != null ? ((Number) row.get("version")).intValue() : 1);
            nested.put("nameUz", deptName);
            map.put("department", nested);
        } catch (EmptyResultDataAccessException e) {
            // Department topilmadi - fallback with code as name
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversityDepartment");
            nested.put("_instanceName", departmentCode);
            nested.put("id", departmentCode);
            nested.put("code", departmentCode);
            nested.put("version", 1);
            nested.put("nameUz", departmentCode); // null o'rniga code ishlatamiz
            map.put("department", nested);
        } catch (Exception e) {
            log.debug("Failed to fetch department: {}", e.getMessage());
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversityDepartment");
            nested.put("_instanceName", departmentCode);
            nested.put("id", departmentCode);
            nested.put("code", departmentCode);
            nested.put("version", 1);
            nested.put("nameUz", departmentCode); // null o'rniga code ishlatamiz
            map.put("department", nested);
        }
    }

    private void putNestedRate(Map<String, Object> map, String rateCode, Boolean returnNulls) {
        if (rateCode == null || rateCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("employeeRate", null);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name, version FROM hemishe_h_university_employee_rate WHERE code = ? AND delete_ts IS NULL",
                    rateCode);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HUniversityEmployeeRate");
            nested.put("_instanceName", str(row.get("name")));
            nested.put("id", str(row.get("code")));
            nested.put("code", str(row.get("code")));
            nested.put("name", str(row.get("name")));
            nested.put("version", row.get("version") != null ? ((Number) row.get("version")).intValue() : 1);
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
            if (Boolean.TRUE.equals(returnNulls)) map.put(key, null);
            return;
        }
        Map<String, Object> nested = nestedObjectLoader.loadClassifier(tableName, entityName.replace("hemishe_", ""), code);
        if (nested != null) {
            // Override _entityName to use full name format
            nested.put("_entityName", entityName);
            nested.put("_instanceName", nested.get("name"));
            map.put(key, nested);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    private void putNestedEmployeeForm(Map<String, Object> map, String formCode, Boolean returnNulls) {
        if (formCode == null || formCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("employeeForm", null);
            return;
        }
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_HEmployeeForm");
        nested.put("id", formCode);
        nested.put("code", formCode);
        map.put("employeeForm", nested);
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
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    private String str(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }
}
