package uz.hemis.service.legacy.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;
import uz.hemis.service.legacy.CubaNestedObjectLoader;
import uz.hemis.service.legacy.SoftDeleteRestoreLegacyService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uz.hemis.service.legacy.CubaEntityMapHelper.*;

/**
 * Legacy service for Employee Reference domain entities.
 * Extracts toMap / updateFromMap / CRUD logic from 6 employee controllers.
 *
 * Entities handled:
 * - TeacherPositionType (read-only classifier, String PK)
 * - UniversityEmployeeRate (read-only classifier, String PK)
 * - EmployeeCertificate (full CRUD + UPSERT, UUID PK)
 * - UniversityEmployeeForm (read-only classifier, String PK)
 * - UniversityEmployeeStatusType (read-only classifier, String PK)
 * - UniversityEmployeeType (full CRUD + UPSERT + soft-delete restore, String PK)
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeRefLegacyService {

    private final TeacherPositionTypeRepository teacherPositionTypeRepository;
    private final UniversityEmployeeRateRepository universityEmployeeRateRepository;
    private final EmployeeCertificateRepository employeeCertificateRepository;
    private final UniversityEmployeeFormRepository universityEmployeeFormRepository;
    private final UniversityEmployeeStatusTypeRepository universityEmployeeStatusTypeRepository;
    private final UniversityEmployeeTypeRepository universityEmployeeTypeRepository;
    private final SoftDeleteRestoreLegacyService softDeleteRestoreService;
    private final CubaNestedObjectLoader nestedObjectLoader;
    private final AdministrativeEmployee1Repository administrativeEmployee1Repository;
    private final AdministrativeEmployee2Repository administrativeEmployee2Repository;
    private final AdministrativeEmployee3Repository administrativeEmployee3Repository;

    // ====================================================================
    //  TeacherPositionType (read-only classifier)
    // ====================================================================

    private static final String TEACHER_POSITION_TYPE_ENTITY = "hemishe_HTeacherPositionType";

    public Optional<TeacherPositionType> findTeacherPositionTypeById(String code) {
        return teacherPositionTypeRepository.findById(code);
    }

    public Page<TeacherPositionType> findAllTeacherPositionType(PageRequest pageRequest) {
        return teacherPositionTypeRepository.findAll(pageRequest);
    }

    public List<TeacherPositionType> findAllTeacherPositionType(Sort sort) {
        return teacherPositionTypeRepository.findAll(sort);
    }

    public Map<String, Object> toTeacherPositionTypeMap(TeacherPositionType entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", TEACHER_POSITION_TYPE_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.isActive(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "deleteTs", null, returnNulls);
        putIfNotNull(map, "deletedBy", null, returnNulls);
        return map;
    }

    // ====================================================================
    //  UniversityEmployeeRate (read-only classifier)
    // ====================================================================

    private static final String EMPLOYEE_RATE_ENTITY = "hemishe_HUniversityEmployeeRate";

    public Optional<UniversityEmployeeRate> findUniversityEmployeeRateById(String code) {
        return universityEmployeeRateRepository.findById(code);
    }

    public Page<UniversityEmployeeRate> findAllUniversityEmployeeRate(PageRequest pageRequest) {
        return universityEmployeeRateRepository.findAll(pageRequest);
    }

    public List<UniversityEmployeeRate> findAllUniversityEmployeeRate(Sort sort) {
        return universityEmployeeRateRepository.findAll(sort);
    }

    public Map<String, Object> toUniversityEmployeeRateMap(UniversityEmployeeRate entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", EMPLOYEE_RATE_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.isActive(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        // Classifier uses isActive instead of soft-delete — deleteTs/deletedBy always null
        putIfNotNull(map, "deleteTs", null, returnNulls);
        putIfNotNull(map, "deletedBy", null, returnNulls);
        return map;
    }

    // ====================================================================
    //  EmployeeCertificate (full CRUD + UPSERT, UUID PK)
    // ====================================================================

    private static final String EMPLOYEE_CERTIFICATE_ENTITY = "hemishe_EEmpoyeeCertificate";

    public Optional<EmployeeCertificate> findEmployeeCertificateById(UUID id) {
        return employeeCertificateRepository.findById(id);
    }

    public Page<EmployeeCertificate> findAllEmployeeCertificate(Pageable pageable) {
        return employeeCertificateRepository.findAll(pageable);
    }

    @Transactional
    public EmployeeCertificate saveEmployeeCertificate(EmployeeCertificate entity) {
        return employeeCertificateRepository.save(entity);
    }

    @Transactional
    public void softDeleteEmployeeCertificate(EmployeeCertificate entity) {
        entity.setDeleteTs(LocalDateTime.now());
        employeeCertificateRepository.save(entity);
    }

    @Transactional
    public EmployeeCertificate createOrUpsertEmployeeCertificate(Map<String, Object> data) {
        if (data.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(data.get("id").toString());
                var existingOpt = employeeCertificateRepository.findById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    EmployeeCertificate cert = existingOpt.get();
                    updateEmployeeCertificateFromMap(cert, data);
                    cert.setUpdateTs(LocalDateTime.now());
                    return employeeCertificateRepository.save(cert);
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID format for id: {}", data.get("id"));
            }
        }

        EmployeeCertificate cert = new EmployeeCertificate();
        cert.setId(UUID.randomUUID());
        cert.setCreateTs(LocalDateTime.now());
        cert.setUpdateTs(LocalDateTime.now());
        updateEmployeeCertificateFromMap(cert, data);
        return employeeCertificateRepository.save(cert);
    }

    public Map<String, Object> toEmployeeCertificateMap(EmployeeCertificate cert) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", cert.getId());
        map.put("_entityName", EMPLOYEE_CERTIFICATE_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.EEmpoyeeCertificate-" + cert.getId() + " [detached]");
        // OLD-HEMIS: default view da reference fieldlar qaytarilmaydi
        // university, employee, certificateType, certificateName, certificateGrade, certificateSubject
        map.put("issueDate", cert.getIssueDate());
        map.put("validDate", cert.getValidDate());
        map.put("serialNumber", cert.getSerialNumber());
        map.put("active", cert.getActive());
        map.put("createTs", cert.getCreateTs());
        map.put("updateTs", cert.getUpdateTs());
        map.put("deleteTs", cert.getDeleteTs());
        return map;
    }

    public void updateEmployeeCertificateFromMap(EmployeeCertificate cert, Map<String, Object> data) {
        if (data.containsKey("university")) {
            cert.setUniversity(extractString(data.get("university")));
        }
        if (data.containsKey("employee")) {
            Object empObj = data.get("employee");
            cert.setEmployee(empObj != null ? UUID.fromString(extractString(empObj)) : null);
        }
        if (data.containsKey("certificateType")) {
            cert.setCertificateType(extractString(data.get("certificateType")));
        }
        if (data.containsKey("certificateName")) {
            cert.setCertificateName(extractString(data.get("certificateName")));
        }
        if (data.containsKey("certificateGrade")) {
            cert.setCertificateGrade(extractString(data.get("certificateGrade")));
        }
        if (data.containsKey("certificateSubject")) {
            cert.setCertificateSubject(extractString(data.get("certificateSubject")));
        }
        if (data.containsKey("issueDate")) {
            Object issueDateObj = data.get("issueDate");
            cert.setIssueDate(issueDateObj != null ? LocalDate.parse(issueDateObj.toString()) : null);
        }
        if (data.containsKey("validDate")) {
            Object validDateObj = data.get("validDate");
            cert.setValidDate(validDateObj != null ? LocalDate.parse(validDateObj.toString()) : null);
        }
        if (data.containsKey("serialNumber")) {
            cert.setSerialNumber(extractString(data.get("serialNumber")));
        }
        if (data.containsKey("active")) {
            cert.setActive(getBooleanValue(data.get("active")));
        }
    }

    // ====================================================================
    //  UniversityEmployeeForm (read-only classifier)
    // ====================================================================

    private static final String EMPLOYEE_FORM_ENTITY = "hemishe_HUniversityEmployeeForm";

    public Optional<UniversityEmployeeForm> findUniversityEmployeeFormById(String code) {
        return universityEmployeeFormRepository.findById(code);
    }

    public Page<UniversityEmployeeForm> findAllUniversityEmployeeForm(PageRequest pageRequest) {
        return universityEmployeeFormRepository.findAll(pageRequest);
    }

    public List<UniversityEmployeeForm> findAllUniversityEmployeeForm(Sort sort) {
        return universityEmployeeFormRepository.findAll(sort);
    }

    public Map<String, Object> toUniversityEmployeeFormMap(UniversityEmployeeForm entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", EMPLOYEE_FORM_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());
        // OLD-HEMIS specific field order
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
        // Classifier uses isActive instead of soft-delete — deleteTs/deletedBy always null
        putIfNotNull(map, "deleteTs", null, returnNulls);
        map.put("code", entity.getCode());
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.isActive(), returnNulls);
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "deletedBy", null, returnNulls);
        return map;
    }

    // ====================================================================
    //  UniversityEmployeeStatusType (read-only classifier)
    // ====================================================================

    private static final String EMPLOYEE_STATUS_TYPE_ENTITY = "hemishe_HUniversityEmployeeStatusType";

    public Optional<UniversityEmployeeStatusType> findUniversityEmployeeStatusTypeById(String code) {
        return universityEmployeeStatusTypeRepository.findById(code);
    }

    public Page<UniversityEmployeeStatusType> findAllUniversityEmployeeStatusType(PageRequest pageRequest) {
        return universityEmployeeStatusTypeRepository.findAll(pageRequest);
    }

    public List<UniversityEmployeeStatusType> findAllUniversityEmployeeStatusType(Sort sort) {
        return universityEmployeeStatusTypeRepository.findAll(sort);
    }

    public Map<String, Object> toUniversityEmployeeStatusTypeMap(UniversityEmployeeStatusType entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", EMPLOYEE_STATUS_TYPE_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());
        // OLD-HEMIS specific field order
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
        putIfNotNull(map, "deleteTs", null, returnNulls);
        map.put("code", entity.getCode());
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.isActive(), returnNulls);
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "deletedBy", null, returnNulls);
        return map;
    }

    // ====================================================================
    //  UniversityEmployeeType (full CRUD + UPSERT + soft-delete restore)
    // ====================================================================

    private static final String EMPLOYEE_TYPE_ENTITY = "hemishe_HUniversityEmployeeType";
    private static final String EMPLOYEE_TYPE_TABLE = "hemishe_h_university_employee_type";

    public Optional<UniversityEmployeeType> findUniversityEmployeeTypeById(String code) {
        return universityEmployeeTypeRepository.findById(code);
    }

    public Page<UniversityEmployeeType> findAllUniversityEmployeeType(PageRequest pageRequest) {
        return universityEmployeeTypeRepository.findAll(pageRequest);
    }

    public List<UniversityEmployeeType> findAllUniversityEmployeeType(Sort sort) {
        return universityEmployeeTypeRepository.findAll(sort);
    }

    @Transactional
    public UniversityEmployeeType saveUniversityEmployeeType(UniversityEmployeeType entity) {
        return universityEmployeeTypeRepository.save(entity);
    }

    @Transactional
    public UniversityEmployeeType updateUniversityEmployeeType(UniversityEmployeeType entity, Map<String, Object> data, String currentUsername) {
        if (data.containsKey("name")) {
            entity.setName((String) data.get("name"));
        }
        if (data.containsKey("nameEn")) {
            entity.setNameEn((String) data.get("nameEn"));
        }
        if (data.containsKey("nameRu")) {
            entity.setNameRu((String) data.get("nameRu"));
        }
        if (data.containsKey("active")) {
            Object activeValue = data.get("active");
            if (activeValue instanceof Boolean) {
                entity.setActive((Boolean) activeValue);
            } else if (activeValue instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeValue));
            }
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(currentUsername);
        return universityEmployeeTypeRepository.save(entity);
    }

    @Transactional
    public void softDeleteUniversityEmployeeType(UniversityEmployeeType entity, String currentUsername) {
        entity.setActive(false);
        entity.setUpdatedBy(currentUsername);
        universityEmployeeTypeRepository.save(entity);
    }

    @Transactional
    public UniversityEmployeeType createOrUpsertUniversityEmployeeType(String code, String name, Map<String, Object> data, String currentUsername) {
        Optional<UniversityEmployeeType> existingOpt = universityEmployeeTypeRepository.findById(code);

        UniversityEmployeeType entity;
        boolean isNew = false;

        if (existingOpt.isPresent()) {
            entity = existingOpt.get();
            entity.setUpdatedAt(LocalDateTime.now());
            log.debug("Found existing entity with code: {}", code);
        } else {
            if (softDeleteRestoreService.hasSoftDeletedRecord(EMPLOYEE_TYPE_TABLE, code)) {
                softDeleteRestoreService.restoreSoftDeletedRecord(EMPLOYEE_TYPE_TABLE, code);
                entity = universityEmployeeTypeRepository.findById(code)
                    .orElseThrow(() -> new IllegalStateException("Failed to restore entity with code: " + code));
                log.info("Restored soft-deleted entity with code: {}", code);
            } else {
                entity = new UniversityEmployeeType();
                entity.setCode(code);
                entity.setCreatedAt(LocalDateTime.now());
                entity.setCreatedBy(currentUsername);
                isNew = true;
                log.debug("Creating new entity with code: {}", code);
            }
        }

        entity.setName(name);

        if (data.containsKey("nameEn")) {
            entity.setNameEn((String) data.get("nameEn"));
        }
        if (data.containsKey("nameRu")) {
            entity.setNameRu((String) data.get("nameRu"));
        }
        if (data.containsKey("active")) {
            Object activeValue = data.get("active");
            if (activeValue instanceof Boolean) {
                entity.setActive((Boolean) activeValue);
            } else if (activeValue instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeValue));
            }
        } else if (isNew) {
            entity.setActive(true);
        }

        entity.setUpdatedBy(currentUsername);
        return universityEmployeeTypeRepository.save(entity);
    }

    public Map<String, Object> toUniversityEmployeeTypeMap(UniversityEmployeeType entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", EMPLOYEE_TYPE_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
        putIfNotNull(map, "active", entity.isActive(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "deleteTs", null, returnNulls);
        putIfNotNull(map, "deletedBy", null, returnNulls);
        return map;
    }

    public Map<String, Object> toUniversityEmployeeTypeMinimalMap(UniversityEmployeeType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", EMPLOYEE_TYPE_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());
        return map;
    }

    /**
     * Apply CUBA-style filter to UniversityEmployeeType entity list.
     */
    @SuppressWarnings("unchecked")
    public List<UniversityEmployeeType> applyCubaFilter(List<UniversityEmployeeType> entities, Object filterObj) {
        if (!(filterObj instanceof Map)) {
            return entities;
        }

        Map<String, Object> filter = (Map<String, Object>) filterObj;
        Object conditionsObj = filter.get("conditions");

        if (!(conditionsObj instanceof List)) {
            return entities;
        }

        List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;

        return entities.stream()
            .filter(entity -> matchesAllConditions(entity, conditions))
            .collect(Collectors.toList());
    }

    private boolean matchesAllConditions(UniversityEmployeeType entity, List<Map<String, Object>> conditions) {
        for (Map<String, Object> condition : conditions) {
            String property = (String) condition.get("property");
            String operator = (String) condition.get("operator");
            Object value = condition.get("value");

            if (!matchesCondition(entity, property, operator, value)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesCondition(UniversityEmployeeType entity, String property, String operator, Object value) {
        if (property == null || operator == null) {
            return true;
        }

        Object entityValue = getEntityValue(entity, property);
        String strValue = value != null ? value.toString() : null;
        String strEntityValue = entityValue != null ? entityValue.toString() : null;

        return switch (operator.toLowerCase()) {
            case "=" -> strValue != null && strValue.equals(strEntityValue);
            case "<>", "!=" -> strValue == null || !strValue.equals(strEntityValue);
            case "like" -> strEntityValue != null && strValue != null &&
                strEntityValue.toLowerCase().contains(strValue.toLowerCase().replace("%", ""));
            case "in" -> {
                if (value instanceof List) {
                    yield ((List<?>) value).stream()
                        .anyMatch(v -> v != null && v.toString().equals(strEntityValue));
                }
                yield false;
            }
            case "isnull", "isNull" -> entityValue == null;
            case "notnull", "notNull" -> entityValue != null;
            default -> true;
        };
    }

    private Object getEntityValue(UniversityEmployeeType entity, String property) {
        return switch (property.toLowerCase()) {
            case "code", "id" -> entity.getCode();
            case "name" -> entity.getName();
            case "nameen", "name_en" -> entity.getNameEn();
            case "nameru", "name_ru" -> entity.getNameRu();
            case "active" -> entity.isActive();
            case "version" -> entity.getVersion();
            default -> null;
        };
    }

    // ====================================================================
    //  AdministrativeEmployee1 (PhD/DSc from top-1000 universities)
    // ====================================================================

    private static final String ADMIN_EMP1_ENTITY = "hemishe_RIAdministrativeEmployee1";

    public Optional<AdministrativeEmployee1> findAdministrativeEmployee1ById(UUID id) {
        return administrativeEmployee1Repository.findById(id);
    }

    public List<AdministrativeEmployee1> findAllAdministrativeEmployee1() {
        return administrativeEmployee1Repository.findAll();
    }

    @Transactional
    public AdministrativeEmployee1 saveAdministrativeEmployee1(AdministrativeEmployee1 entity) {
        return administrativeEmployee1Repository.save(entity);
    }

    @Transactional
    public void softDeleteAdministrativeEmployee1(AdministrativeEmployee1 entity) {
        entity.setDeleteTs(LocalDateTime.now());
        administrativeEmployee1Repository.save(entity);
    }

    public Map<String, Object> toAdministrativeEmployee1Map(AdministrativeEmployee1 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ADMIN_EMP1_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RIAdministrativeEmployee1-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        putIfNotNull(map, "diplomaSerialNumber", entity.getDiplomaSerialNumber(), returnNulls);
        putIfNotNull(map, "diplomaType", entity.getDiplomaType(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "councilDate", formatDate(entity.getCouncilDate()), returnNulls);
        putIfNotNull(map, "foreignUniversity", entity.getForeignUniversity(), returnNulls);
        putIfNotNull(map, "councilNumber", entity.getCouncilNumber(), returnNulls);
        putIfNotNull(map, "diplomaDate", formatDate(entity.getDiplomaDate()), returnNulls);
        return map;
    }

    public void updateAdministrativeEmployee1FromMap(AdministrativeEmployee1 entity, Map<String, Object> data) {
        if (data.containsKey("_university")) entity.setUniversity(extractString(data.get("_university")));
        if (data.containsKey("_educationYear")) entity.setEducationYear(extractString(data.get("_educationYear")));
        if (data.containsKey("_employee")) entity.setEmployee(extractUuid(data.get("_employee")));
        if (data.containsKey("_country")) entity.setCountry(extractString(data.get("_country")));
        if (data.containsKey("_degree")) entity.setDegree(extractString(data.get("_degree")));
        if (data.containsKey("_rank")) entity.setRank(extractString(data.get("_rank")));
        if (data.containsKey("foreignUniversity")) entity.setForeignUniversity(getStringValue(data.get("foreignUniversity")));
        if (data.containsKey("diplomaType")) entity.setDiplomaType(getStringValue(data.get("diplomaType")));
        if (data.containsKey("diplomaSerialNumber")) entity.setDiplomaSerialNumber(getStringValue(data.get("diplomaSerialNumber")));
        if (data.containsKey("diplomaDate")) entity.setDiplomaDate(parseLocalDate(data.get("diplomaDate")));
        if (data.containsKey("specialityCode")) entity.setSpecialityCode(getStringValue(data.get("specialityCode")));
        if (data.containsKey("specialityName")) entity.setSpecialityName(getStringValue(data.get("specialityName")));
        if (data.containsKey("councilDate")) entity.setCouncilDate(parseLocalDate(data.get("councilDate")));
        if (data.containsKey("councilNumber")) entity.setCouncilNumber(getStringValue(data.get("councilNumber")));
    }

    // ====================================================================
    //  AdministrativeEmployee2 (Internship at top-1000 universities)
    // ====================================================================

    private static final String ADMIN_EMP2_ENTITY = "hemishe_RIAdministrativeEmployee2";

    public Optional<AdministrativeEmployee2> findAdministrativeEmployee2ById(UUID id) {
        return administrativeEmployee2Repository.findById(id);
    }

    public List<AdministrativeEmployee2> findAllAdministrativeEmployee2() {
        return administrativeEmployee2Repository.findAll();
    }

    @Transactional
    public AdministrativeEmployee2 saveAdministrativeEmployee2(AdministrativeEmployee2 entity) {
        return administrativeEmployee2Repository.save(entity);
    }

    @Transactional
    public void softDeleteAdministrativeEmployee2(AdministrativeEmployee2 entity) {
        entity.setDeleteTs(LocalDateTime.now());
        administrativeEmployee2Repository.save(entity);
    }

    public Map<String, Object> toAdministrativeEmployee2Map(AdministrativeEmployee2 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ADMIN_EMP2_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RIAdministrativeEmployee2-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "foreignUniversity", entity.getForeignUniversity(), returnNulls);
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "trainingTypeName", entity.getTrainingTypeName(), returnNulls);
        putIfNotNull(map, "trainingContract", entity.getTrainingContract(), returnNulls);
        putIfNotNull(map, "trainingDateStart", formatDate(entity.getTrainingDateStart()), returnNulls);
        putIfNotNull(map, "trainingDateEnd", formatDate(entity.getTrainingDateEnd()), returnNulls);
        putIfNotNull(map, "year", entity.getYear(), returnNulls);
        putIfNotNull(map, "subject", entity.getSubject(), returnNulls);
        return map;
    }

    public void updateAdministrativeEmployee2FromMap(AdministrativeEmployee2 entity, Map<String, Object> data) {
        if (data.containsKey("_university")) entity.setUniversity(extractString(data.get("_university")));
        if (data.containsKey("_educationYear")) entity.setEducationYear(extractString(data.get("_educationYear")));
        if (data.containsKey("_employee")) entity.setEmployee(extractString(data.get("_employee")));
        if (data.containsKey("_country")) entity.setCountry(extractString(data.get("_country")));
        if (data.containsKey("_internshipForm")) entity.setInternshipForm(extractString(data.get("_internshipForm")));
        if (data.containsKey("_internshipType")) entity.setInternshipType(extractString(data.get("_internshipType")));
        if (data.containsKey("foreignUniversity")) entity.setForeignUniversity(getStringValue(data.get("foreignUniversity")));
        if (data.containsKey("specialityCode")) entity.setSpecialityCode(getStringValue(data.get("specialityCode")));
        if (data.containsKey("specialityName")) entity.setSpecialityName(getStringValue(data.get("specialityName")));
        if (data.containsKey("trainingTypeName")) entity.setTrainingTypeName(getStringValue(data.get("trainingTypeName")));
        if (data.containsKey("trainingContract")) entity.setTrainingContract(getStringValue(data.get("trainingContract")));
        if (data.containsKey("trainingDateStart")) entity.setTrainingDateStart(parseLocalDate(data.get("trainingDateStart")));
        if (data.containsKey("trainingDateEnd")) entity.setTrainingDateEnd(parseLocalDate(data.get("trainingDateEnd")));
        if (data.containsKey("year")) entity.setYear(getStringValue(data.get("year")));
        if (data.containsKey("subject")) entity.setSubject(getStringValue(data.get("subject")));
    }

    // ====================================================================
    //  AdministrativeEmployee3 (DSc or professor title from foreign)
    // ====================================================================

    private static final String ADMIN_EMP3_ENTITY = "hemishe_RIAdministrativeEmployee3";

    public Optional<AdministrativeEmployee3> findAdministrativeEmployee3ById(UUID id) {
        return administrativeEmployee3Repository.findById(id);
    }

    public List<AdministrativeEmployee3> findAllAdministrativeEmployee3() {
        return administrativeEmployee3Repository.findAll();
    }

    @Transactional
    public AdministrativeEmployee3 saveAdministrativeEmployee3(AdministrativeEmployee3 entity) {
        return administrativeEmployee3Repository.save(entity);
    }

    @Transactional
    public void softDeleteAdministrativeEmployee3(AdministrativeEmployee3 entity) {
        entity.setDeleteTs(LocalDateTime.now());
        administrativeEmployee3Repository.save(entity);
    }

    public Map<String, Object> toAdministrativeEmployee3Map(AdministrativeEmployee3 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ADMIN_EMP3_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RIAdministrativeEmployee3-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "fullname", entity.getFullname(), returnNulls);
        putIfNotNull(map, "workPlace", entity.getWorkPlace(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "subject", entity.getSubject(), returnNulls);
        putIfNotNull(map, "contractData", entity.getContractData(), returnNulls);
        putIfNotNull(map, "arrivalDate", formatDate(entity.getArrivalDate()), returnNulls);
        putIfNotNull(map, "departureDate", formatDate(entity.getDepartureDate()), returnNulls);
        putIfNotNull(map, "lessonTime", entity.getLessonTime(), returnNulls);
        putIfNotNull(map, "year", entity.getYear(), returnNulls);
        return map;
    }

    public void updateAdministrativeEmployee3FromMap(AdministrativeEmployee3 entity, Map<String, Object> data) {
        if (data.containsKey("_university")) entity.setUniversity(extractString(data.get("_university")));
        if (data.containsKey("_educationYear")) entity.setEducationYear(extractString(data.get("_educationYear")));
        if (data.containsKey("_country")) entity.setCountry(extractString(data.get("_country")));
        if (data.containsKey("_employee")) entity.setEmployee(extractUuid(data.get("_employee")));
        if (data.containsKey("_employeeForm")) entity.setEmployeeForm(extractString(data.get("_employeeForm")));
        if (data.containsKey("_condutionForm")) entity.setCondutionForm(extractString(data.get("_condutionForm")));
        if (data.containsKey("fullname")) entity.setFullname(getStringValue(data.get("fullname")));
        if (data.containsKey("workPlace")) entity.setWorkPlace(getStringValue(data.get("workPlace")));
        if (data.containsKey("specialityName")) entity.setSpecialityName(getStringValue(data.get("specialityName")));
        if (data.containsKey("subject")) entity.setSubject(getStringValue(data.get("subject")));
        if (data.containsKey("contractData")) entity.setContractData(getStringValue(data.get("contractData")));
        if (data.containsKey("arrivalDate")) entity.setArrivalDate(parseLocalDate(data.get("arrivalDate")));
        if (data.containsKey("departureDate")) entity.setDepartureDate(parseLocalDate(data.get("departureDate")));
        if (data.containsKey("lessonTime")) entity.setLessonTime(getIntegerValue(data.get("lessonTime")));
        if (data.containsKey("year")) entity.setYear(getStringValue(data.get("year")));
    }

    // ====================================================================
    //  Helper methods
    // ====================================================================

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }
}
