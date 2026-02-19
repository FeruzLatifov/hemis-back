package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.JsonNull;
import uz.hemis.domain.entity.Teacher;
import uz.hemis.domain.repository.TeacherRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Teacher Legacy Service - Business logic for CUBA teacher entity endpoints
 *
 * Extracted from TeacherEntityController to separate concerns.
 * Handles nested object loading for eTeacher-view and teacher-specific operations.
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeacherLegacyService {

    private final TeacherRepository teacherRepository;
    private final JdbcTemplate jdbcTemplate;
    private final CubaNestedObjectLoader nestedLoader;

    private static final String ENTITY_NAME = "hemishe_ETeacher";

    // ==================== CRUD Operations ====================

    public Optional<Teacher> findById(UUID id) {
        return teacherRepository.findById(id);
    }

    public Optional<Teacher> findByCode(String code) {
        return teacherRepository.findByCode(code);
    }

    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    public Page<Teacher> findAll(Pageable pageable) {
        return teacherRepository.findAll(pageable);
    }

    @Transactional
    public Teacher save(Teacher entity) {
        return teacherRepository.save(entity);
    }

    @Transactional
    public void softDelete(Teacher entity) {
        entity.setDeleteTs(LocalDateTime.now());
        teacherRepository.save(entity);
    }

    // ==================== Code Generation ====================

    /**
     * Generate unique teacher code with retry logic
     *
     * <p><strong>Format:</strong> {universityCode}{YY}{gender}{sequence}</p>
     * <p>Example: "5202511001" = university 520, year 2025, male (11), sequence 001</p>
     */
    public String generateUniqueTeacherCode(String universityCode, String year, String gender) {
        String yearSuffix = year;
        if (year != null && year.length() == 4) {
            yearSuffix = year.substring(2);
        }

        String prefix = universityCode + yearSuffix + gender;
        String maxCode = teacherRepository.findMaxCodeByPrefix(prefix + "%");

        long sequence;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            String seqStr = maxCode.substring(prefix.length());
            try {
                sequence = Long.parseLong(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = teacherRepository.countByCodePrefix(prefix) + 1;
            }
        } else {
            sequence = 1;
        }

        return prefix + String.format("%03d", sequence);
    }

    // ==================== Mapping Operations ====================

    /**
     * Teacher entity ni CUBA Map formatiga o'tkazish
     * OLD-HEMIS exact field order preserved
     */
    public Map<String, Object> toTeacherMap(Teacher entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);

        String instanceName = entity.getFullName() != null && !entity.getFullName().isEmpty() ?
            entity.getFullName() : "Teacher-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        boolean isViewMode = view != null && view.contains("eTeacher");

        if ("_local".equals(view)) {
            CubaEntityMapHelper.putIfNotNull(map, "pinfl", entity.getPinfl(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "birthday", entity.getBirthDate(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "firstname", entity.getFirstName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "code", entity.getCode(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "tag", entity.getTag(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "address", entity.getAddress(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "lastname", entity.getSecondName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "fathername", entity.getThirdName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "phone", entity.getPhone(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "employeeYear", entity.getEmployeeYear(), returnNulls);
        } else if (isViewMode) {
            // OLD-HEMIS exact field order for eTeacher-view
            CubaEntityMapHelper.putIfNotNull(map, "pinfl", entity.getPinfl(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "birthday", entity.getBirthDate(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "firstname", entity.getFirstName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "code", entity.getCode(), returnNulls);
            putClassifier(map, "gender", entity.getGender(),
                    "hemishe_HGender", "hemishe_h_gender", returnNulls);
            putUniversity(map, entity.getUniversity(), returnNulls);
            putClassifier(map, "employmentForm", entity.getEmploymentForm(),
                    "hemishe_HEmploymentForm", "hemishe_h_employment_form", returnNulls);
            putClassifier(map, "academicDegree", entity.getAcademicDegree(),
                    "hemishe_HAcademicDegree", "hemishe_h_academic_degree", returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "tag", entity.getTag(), returnNulls);
            putDepartment(map, entity.getDepartment(), returnNulls);
            putSoato(map, "soatoRegion", entity.getSoatoRegion(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "address", entity.getAddress(), returnNulls);
            putClassifier(map, "citizenship", entity.getCitizenship(),
                    "hemishe_HCitizenship", "hemishe_h_citizenship", returnNulls);
            putClassifier(map, "academicRank", entity.getAcademicRank(),
                    "hemishe_HAcademicRank", "hemishe_h_academic_rank", returnNulls);
            List<Map<String, Object>> jobs = loadJobs(entity.getCode(), entity.getUniversity(), returnNulls);
            map.put("jobs", jobs);
            CubaEntityMapHelper.putIfNotNull(map, "lastname", entity.getSecondName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "fathername", entity.getThirdName(), returnNulls);
            putClassifier(map, "employeeType", entity.getEmployeeType(),
                    "hemishe_HUniversityEmployeeType", "hemishe_h_university_employee_type", returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "phone", entity.getPhone(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "employeeYear", entity.getEmployeeYear(), returnNulls);
            putClassifier(map, "position", entity.getPosition(),
                    "hemishe_HTeacherPositionType", "hemishe_h_teacher_position_type", returnNulls);
            putClassifier(map, "universityEmploymentForm", entity.getUniversityEmploymentForm(),
                    "hemishe_HUniversityEmployeeForm", "hemishe_h_university_employee_form", returnNulls);
            putSoato(map, "soatoDistrict", entity.getSoatoDistrict(), returnNulls);
            // OLD-HEMIS compatibility: do NOT include fullname, version, deletedBy, deleteTs
        } else {
            // Default view (not _local, not eTeacher-view)
            // OLD-HEMIS field order: pinfl, birthday, firstname, code, tag, serialNumber, address, version, lastname, fathername, phone, employeeYear, fullname
            CubaEntityMapHelper.putIfNotNull(map, "pinfl", entity.getPinfl(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "birthday", entity.getBirthDate(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "firstname", entity.getFirstName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "code", entity.getCode(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "tag", entity.getTag(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "address", entity.getAddress(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "lastname", entity.getSecondName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "fathername", entity.getThirdName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "phone", entity.getPhone(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "employeeYear", entity.getEmployeeYear(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "fullname", entity.getFullName(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        }

        return map;
    }

    /**
     * CUBA POST response format: faqat {_entityName, _instanceName, id}
     */
    public Map<String, Object> minimalTeacherResponse(Teacher entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        String instanceName = entity.getFullName() != null && !entity.getFullName().isEmpty() ?
            entity.getFullName() : "Teacher-" + entity.getId();
        map.put("_instanceName", instanceName);
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        return map;
    }

    /**
     * Map dan Teacher entity ga fieldlarni o'tkazish (partial update)
     */
    public void updateTeacherFromMap(Teacher entity, Map<String, Object> map) {
        if (map.containsKey("firstname")) {
            entity.setFirstname(CubaEntityMapHelper.getStringValue(map.get("firstname")));
        }
        if (map.containsKey("lastname")) {
            entity.setLastname(CubaEntityMapHelper.getStringValue(map.get("lastname")));
        }
        if (map.containsKey("fathername")) {
            entity.setFathername(CubaEntityMapHelper.getStringValue(map.get("fathername")));
        }
        if (map.containsKey("pinfl")) {
            entity.setPinfl(CubaEntityMapHelper.getStringValue(map.get("pinfl")));
        }
        if (map.containsKey("serialNumber")) {
            entity.setSerialNumber(CubaEntityMapHelper.getStringValue(map.get("serialNumber")));
        }
        if (map.containsKey("birthday")) {
            entity.setBirthday(CubaEntityMapHelper.parseLocalDate(map.get("birthday")));
        }
        if (map.containsKey("phone")) {
            entity.setPhone(CubaEntityMapHelper.getStringValue(map.get("phone")));
        }
        if (map.containsKey("address")) {
            entity.setAddress(CubaEntityMapHelper.getStringValue(map.get("address")));
        }
        if (map.containsKey("employeeYear")) {
            entity.setEmployeeYear(CubaEntityMapHelper.getStringValue(map.get("employeeYear")));
        }
        if (map.containsKey("code")) {
            entity.setCode(CubaEntityMapHelper.getStringValue(map.get("code")));
        }
        if (map.containsKey("tag")) {
            entity.setTag(CubaEntityMapHelper.getStringValue(map.get("tag")));
        }

        entity.setCitizenship(extractRefCode(map, "citizenship", "_citizenship", entity.getCitizenship()));
        entity.setGender(extractRefCode(map, "gender", "_gender", entity.getGender()));
        entity.setUniversity(extractRefCode(map, "university", "_university", entity.getUniversity()));
        entity.setAcademicDegree(extractRefCode(map, "academicDegree", "_academic_degree", entity.getAcademicDegree()));
        entity.setAcademicRank(extractRefCode(map, "academicRank", "_academic_rank", entity.getAcademicRank()));
        entity.setDepartment(extractRefCode(map, "department", "_department", entity.getDepartment()));
        entity.setPosition(extractRefCode(map, "position", "_position", entity.getPosition()));
        entity.setEmployeeType(extractRefCode(map, "employeeType", "_employee_type", entity.getEmployeeType()));
        entity.setEmploymentForm(extractRefCode(map, "employmentForm", "_employment_form", entity.getEmploymentForm()));
        entity.setUniversityEmploymentForm(extractRefCode(map, "universityEmploymentForm", "_university_employment_form", entity.getUniversityEmploymentForm()));
        entity.setSoatoRegion(extractRefCode(map, "soatoRegion", "_soato_region", entity.getSoatoRegion()));
        entity.setSoatoDistrict(extractRefCode(map, "soatoDistrict", "_soato_district", entity.getSoatoDistrict()));
    }

    /**
     * Extract reference code from map — supports both formats:
     * 1. Nested object: "gender": {"code": "11"} (PHP sync format)
     * 2. Flat underscore: "_gender": "11" (CUBA format)
     */
    @SuppressWarnings("unchecked")
    public String extractRefCode(Map<String, Object> map, String fieldName, String underscoreFieldName, String currentValue) {
        if (map.containsKey(fieldName)) {
            Object val = map.get(fieldName);
            if (val instanceof Map) {
                Object code = ((Map<String, Object>) val).get("code");
                return code != null ? code.toString() : currentValue;
            }
            return CubaEntityMapHelper.getStringValue(val);
        }
        if (map.containsKey(underscoreFieldName)) {
            Object val = map.get(underscoreFieldName);
            if (val instanceof Map) {
                Object code = ((Map<String, Object>) val).get("code");
                return code != null ? code.toString() : currentValue;
            }
            return CubaEntityMapHelper.getStringValue(val);
        }
        return currentValue;
    }

    // ==================== Nested Object Loading ====================

    /**
     * Load teacher jobs (EEmployeeJobs) for a teacher - OLD-HEMIS full format
     *
     * @param teacherCode teacher code
     * @param universityCode university code
     * @param returnNulls whether to include null values
     * @return list of job maps in CUBA format
     */
    public List<Map<String, Object>> loadJobs(String teacherCode, String universityCode, Boolean returnNulls) {
        if (teacherCode == null) return Collections.emptyList();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT j.id, j._employee, j._university, j._department, j._employee_type, " +
                    "j._employee_position, j._employee_rate, j._employee_form, j._employee_status, " +
                    "j.job_start_date, j.job_end_date, j.tag, j.contract_date, j.contract_number, " +
                    "j.decree_date, j.decree_number, t.firstname, t.lastname, t.fathername " +
                    "FROM hemishe_e_employee_jobs j " +
                    "LEFT JOIN hemishe_e_teacher t ON j._employee = t.id " +
                    "WHERE j._employee = (SELECT id FROM hemishe_e_teacher WHERE code = ? AND delete_ts IS NULL) " +
                    "AND j.delete_ts IS NULL",
                    teacherCode);
            if (rows.isEmpty()) return Collections.emptyList();

            List<Map<String, Object>> jobsList = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                Map<String, Object> job = new LinkedHashMap<>();
                job.put("_entityName", "hemishe_EEmployeeJobs");

                // _instanceName from teacher fullname
                String firstName = row.get("firstname") != null ? row.get("firstname").toString() : "";
                String lastName = row.get("lastname") != null ? row.get("lastname").toString() : "";
                String fatherName = row.get("fathername") != null ? row.get("fathername").toString() : "";
                String instanceName = (lastName + " " + firstName + " " + fatherName).trim();
                job.put("_instanceName", instanceName);

                job.put("id", row.get("id") != null ? row.get("id").toString() : null);
                Object jobEndDate = row.get("job_end_date");
                job.put("jobEndDate", jobEndDate != null ? formatDate(jobEndDate) : JsonNull.INSTANCE);
                job.put("contractDate", formatDate(row.get("contract_date")));

                // university - full 24-field format
                String uniCode = row.get("_university") != null ? row.get("_university").toString() : null;
                if (uniCode != null) {
                    Map<String, Object> uni = nestedLoader.loadUniversityForTeacher(uniCode);
                    if (uni != null) job.put("university", uni);
                }

                job.put("jobStartDate", formatDate(row.get("job_start_date")));

                // employeeForm classifier
                putJobClassifier(job, "employeeForm", row.get("_employee_form"),
                        "hemishe_HUniversityEmployeeForm", "hemishe_h_university_employee_form");

                job.put("contractNumber", row.get("contract_number"));

                // employee - minimal teacher reference
                if (row.get("_employee") != null) {
                    Map<String, Object> emp = new LinkedHashMap<>();
                    emp.put("_entityName", "hemishe_ETeacher");
                    emp.put("_instanceName", instanceName);
                    emp.put("id", row.get("_employee").toString());
                    job.put("employee", emp);
                }

                // employeeStatus classifier
                putJobClassifier(job, "employeeStatus", row.get("_employee_status"),
                        "hemishe_HUniversityEmployeeStatusType", "hemishe_h_university_employee_status_type");

                job.put("decreeDate", formatDate(row.get("decree_date")));
                job.put("decreeNumber", row.get("decree_number"));

                // employeeType classifier
                putJobClassifier(job, "employeeType", row.get("_employee_type"),
                        "hemishe_HUniversityEmployeeType", "hemishe_h_university_employee_type");

                // employeeRate classifier
                putJobClassifier(job, "employeeRate", row.get("_employee_rate"),
                        "hemishe_HUniversityEmployeeRate", "hemishe_h_university_employee_rate");

                // employeePosition classifier
                putJobClassifier(job, "employeePosition", row.get("_employee_position"),
                        "hemishe_HTeacherPositionType", "hemishe_h_teacher_position_type");

                job.put("tag", row.get("tag"));

                // department with deparmentType - must be included even if null (OLD-HEMIS format)
                String deptCode = row.get("_department") != null ? row.get("_department").toString() : null;
                if (deptCode != null) {
                    Map<String, Object> dept = loadJobDepartment(deptCode);
                    job.put("department", dept != null ? dept : JsonNull.INSTANCE);
                } else {
                    job.put("department", JsonNull.INSTANCE);
                }

                jobsList.add(job);
            }
            return jobsList;
        } catch (Exception e) {
            log.warn("Could not fetch jobs for teacher {}: {}", teacherCode, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Put classifier for job object (simple format without code)
     */
    private void putJobClassifier(Map<String, Object> job, String key, Object code,
                                   String entityName, String tableName) {
        if (code == null) return;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL",
                    code.toString());
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", entityName);
            String name = row.get("name") != null ? row.get("name").toString() : "";
            obj.put("_instanceName", name);
            obj.put("id", row.get("code"));
            obj.put("name", name);
            job.put(key, obj);
        } catch (Exception e) {
            log.debug("putJobClassifier error for {}: {}", key, e.getMessage());
        }
    }

    /**
     * Format date to yyyy-MM-dd string (OLD-HEMIS format)
     */
    private String formatDate(Object date) {
        if (date == null) return null;
        if (date instanceof LocalDate) {
            return date.toString(); // LocalDate.toString() returns yyyy-MM-dd
        }
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate().toString();
        }
        if (date instanceof java.util.Date) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) date);
        }
        return date.toString();
    }

    /**
     * Load department for job (with parent and deparmentType)
     */
    private Map<String, Object> loadJobDepartment(String deptCode) {
        if (deptCode == null) return null;
        try {
            // _department column stores code (PK of hemishe_e_university_department)
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, parent_code, _deparment_type FROM hemishe_e_university_department " +
                    "WHERE code = ? AND delete_ts IS NULL", deptCode);
            Map<String, Object> dept = new LinkedHashMap<>();
            dept.put("_entityName", "hemishe_EUniversityDepartment");
            dept.put("_instanceName", row.get("name_uz"));
            dept.put("id", row.get("code"));
            dept.put("parent", JsonNull.INSTANCE); // OLD-HEMIS returns null for parent

            // deparmentType classifier
            String typeCode = row.get("_deparment_type") != null ? row.get("_deparment_type").toString() : null;
            if (typeCode != null) {
                try {
                    Map<String, Object> typeRow = jdbcTemplate.queryForMap(
                            "SELECT code, name FROM hemishe_h_university_department_type WHERE code = ? AND delete_ts IS NULL",
                            typeCode);
                    Map<String, Object> typeObj = new LinkedHashMap<>();
                    typeObj.put("_entityName", "hemishe_HUniversityDepartmentType");
                    String typeName = typeRow.get("name") != null ? typeRow.get("name").toString() : "";
                    typeObj.put("_instanceName", typeName);
                    typeObj.put("id", typeRow.get("code"));
                    typeObj.put("name", typeName);
                    dept.put("deparmentType", typeObj);
                } catch (Exception e) {
                    log.warn("deparmentType load error for code {}: {}", typeCode, e.getMessage(), e);
                }
            }

            dept.put("nameUz", row.get("name_uz"));
            return dept;
        } catch (Exception e) {
            log.warn("loadJobDepartment error for code {}: {}", deptCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Put classifier nested object into result map
     * OLD-HEMIS format: _entityName, _instanceName, id, code, name
     * citizenship uchun active qo'shiladi
     */
    public void putClassifier(Map<String, Object> map, String key, String code,
                              String entityName, String tableName, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put(key, null);
            }
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name, active FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", entityName);
            String name = row.get("name") != null ? row.get("name").toString() : "";
            // OLD-HEMIS: _instanceName format is "code name"
            obj.put("_instanceName", row.get("code") + " " + name);
            obj.put("id", row.get("code"));
            obj.put("code", row.get("code"));
            obj.put("name", name);
            // OLD-HEMIS: citizenship uchun active qo'shiladi
            if ("citizenship".equals(key)) {
                obj.put("active", row.get("active") != null ? row.get("active") : true);
            }
            map.put(key, obj);
        } catch (EmptyResultDataAccessException e) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put(key, null);
            }
        } catch (Exception e) {
            log.debug("putClassifier error for {} code={}: {}", key, code, e.getMessage());
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put(key, null);
            }
        }
    }

    /**
     * Put university nested object into result map (Teacher format - 24 fields only)
     * OLD-HEMIS Teacher returns university WITHOUT nested objects (soato, universityType, etc.)
     */
    public void putUniversity(Map<String, Object> map, String code, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", null);
            }
            return;
        }
        // Use Teacher-specific loader - 24 fields only, NO nested objects
        Map<String, Object> uni = nestedLoader.loadUniversityForTeacher(code);
        if (uni != null) {
            map.put("university", uni);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("university", null);
        }
    }

    /**
     * Put department nested object into result map (with nested university)
     */
    public void putDepartment(Map<String, Object> map, String code, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("department", null);
            }
            return;
        }
        Map<String, Object> dept = nestedLoader.loadDepartment(code);
        if (dept != null) {
            map.put("department", dept);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("department", null);
        }
    }

    /**
     * Put SOATO nested object into result map (Teacher format)
     * Teacher soato: has code, NO active, NO name_ru
     */
    public void putSoato(Map<String, Object> map, String key, String code, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put(key, null);
            }
            return;
        }
        // Use Teacher-specific soato loader - code bor, active yo'q, name_ru yo'q
        Map<String, Object> soato = nestedLoader.loadSoatoForTeacher(code);
        if (soato != null) {
            map.put(key, soato);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }
}
