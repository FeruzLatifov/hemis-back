package uz.hemis.service.legacy.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.academic.*;
import uz.hemis.domain.entity.student.*;
import uz.hemis.domain.entity.employee.*;
import uz.hemis.domain.entity.university.*;
import uz.hemis.domain.entity.research.*;
import uz.hemis.domain.entity.finance.*;
import uz.hemis.domain.entity.security.*;
import uz.hemis.domain.entity.reference.*;
import uz.hemis.domain.entity.system.*;
import uz.hemis.domain.entity.infrastructure.*;
import uz.hemis.domain.entity.base.*;
import uz.hemis.domain.entity.enums.*;
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
 * - EmployeeRate (read-only classifier, String PK)
 * - EmployeeCertificate (full CRUD + UPSERT, UUID PK)
 * - EmploymentForm (read-only classifier, String PK)
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

    private final EmployeeRateRepository employeeRateRepository;
    private final EmployeeCertificateRepository employeeCertificateRepository;
    private final EmploymentFormRepository employmentFormRepository;
    private final UniversityEmployeeStatusTypeRepository universityEmployeeStatusTypeRepository;
    private final UniversityEmployeeTypeRepository universityEmployeeTypeRepository;
    private final SoftDeleteRestoreLegacyService softDeleteRestoreService;
    private final CubaNestedObjectLoader nestedObjectLoader;
    private final AdministrativeEmployee1Repository administrativeEmployee1Repository;
    private final AdministrativeEmployee2Repository administrativeEmployee2Repository;
    private final AdministrativeEmployee3Repository administrativeEmployee3Repository;

    // ====================================================================
    //  EmployeeRate (read-only classifier)
    // ====================================================================

    private static final String EMPLOYEE_RATE_ENTITY = "hemishe_HUniversityEmployeeRate";

    public Optional<EmployeeRate> findEmployeeRateById(String code) {
        return employeeRateRepository.findById(code);
    }

    public Page<EmployeeRate> findAllEmployeeRate(PageRequest pageRequest) {
        return employeeRateRepository.findAll(pageRequest);
    }

    public List<EmployeeRate> findAllEmployeeRate(Sort sort) {
        return employeeRateRepository.findAll(sort);
    }

    public Map<String, Object> toEmployeeRateMap(EmployeeRate entity, Boolean returnNulls) {
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
        // Audit P2.T4: deletedBy field — Vazirlik 7 yil retention compliance.
        entity.setDeletedBy(currentAuditor());
        employeeCertificateRepository.save(entity);
    }

    /**
     * SecurityContext'dan auditor username — fallback "SYSTEM" (anonymous/system events).
     * Mirror of {@code SecurityAuditorAware} (security/audit/) — bu service modul'da
     * direct access yo'q, lokal helper.
     */
    private String currentAuditor() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "SYSTEM";
        }
        return auth.getName();
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
        return toEmployeeCertificateMap(cert, false, null);
    }

    public Map<String, Object> toEmployeeCertificateMap(EmployeeCertificate cert, Boolean returnNulls) {
        return toEmployeeCertificateMap(cert, returnNulls, null);
    }

    /**
     * OLD-HEMIS CUBA view support.
     *
     * <p>Default view ({@code view=null} yoki {@code "_local"}): faqat scalar fieldlar +
     * audit timestamp'lar — OLD-HEMIS {@code _local} bilan mos.</p>
     *
     * <p>{@code eEmpoyeeCertificate-view}: old-hemis {@code views.xml} ga muvofiq
     * 6 ta reference field nested obyekt sifatida qo'shiladi — university, employee,
     * certificateType, certificateName, certificateGrade, certificateSubject.</p>
     */
    public Map<String, Object> toEmployeeCertificateMap(EmployeeCertificate cert, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", cert.getId());
        map.put("_entityName", EMPLOYEE_CERTIFICATE_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.EEmpoyeeCertificate-" + cert.getId() + " [detached]");

        // Named view (e.g. "eEmpoyeeCertificate-view") — reference fieldlar nested object sifatida qaytariladi
        boolean useNested = view != null && !view.isEmpty() && !"_local".equals(view);
        if (useNested) {
            if (cert.getUniversity() != null) {
                map.put("university", nestedObjectLoader.loadUniversityFull(cert.getUniversity()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", uz.hemis.common.JsonNull.INSTANCE);
            }
            if (cert.getEmployee() != null) {
                Map<String, Object> emp = new LinkedHashMap<>();
                emp.put("_entityName", "hemishe_ETeacher");
                emp.put("id", cert.getEmployee().toString());
                emp.put("_instanceName", "com.company.hemishe.entity.ETeacher-" + cert.getEmployee() + " [detached]");
                map.put("employee", emp);
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employee", uz.hemis.common.JsonNull.INSTANCE);
            }
            nestedObjectLoader.putNestedWithNames(map, "certificateType", cert.getCertificateType(),
                    "hemishe_h_certificate_type", "HCertificateType", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "certificateName", cert.getCertificateName(),
                    "hemishe_h_certificate_names", "HCertificateNames", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "certificateGrade", cert.getCertificateGrade(),
                    "hemishe_h_certificate_grades", "HCertificateGrades", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "certificateSubject", cert.getCertificateSubject(),
                    "hemishe_h_certificate_subjects", "HCertificateSubjects", returnNulls);
        }

        uz.hemis.service.legacy.CubaEntityMapHelper.putIfNotNull(map, "issueDate", cert.getIssueDate(), returnNulls);
        uz.hemis.service.legacy.CubaEntityMapHelper.putIfNotNull(map, "validDate", cert.getValidDate(), returnNulls);
        uz.hemis.service.legacy.CubaEntityMapHelper.putIfNotNull(map, "serialNumber", cert.getSerialNumber(), returnNulls);
        uz.hemis.service.legacy.CubaEntityMapHelper.putIfNotNull(map, "active", cert.getActive(), returnNulls);
        uz.hemis.service.legacy.CubaEntityMapHelper.putIfNotNull(map, "createTs", cert.getCreateTs(), returnNulls);
        uz.hemis.service.legacy.CubaEntityMapHelper.putIfNotNull(map, "updateTs", cert.getUpdateTs(), returnNulls);
        uz.hemis.service.legacy.CubaEntityMapHelper.putIfNotNull(map, "deleteTs", cert.getDeleteTs(), returnNulls);
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
    //  EmploymentForm (read-only classifier)
    // ====================================================================

    private static final String EMPLOYEE_FORM_ENTITY = "hemishe_HUniversityEmployeeForm";

    public Optional<EmploymentForm> findEmploymentFormById(String code) {
        return employmentFormRepository.findById(code);
    }

    public Page<EmploymentForm> findAllEmploymentForm(PageRequest pageRequest) {
        return employmentFormRepository.findAll(pageRequest);
    }

    public List<EmploymentForm> findAllEmploymentForm(Sort sort) {
        return employmentFormRepository.findAll(sort);
    }

    public Map<String, Object> toEmploymentFormMap(EmploymentForm entity, Boolean returnNulls) {
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

    /**
     * @deprecated OOM risk — use Pageable variant. Doctoral/PhD records 224 OTM × multi-year.
     */
    @Deprecated(since = "2.5.0", forRemoval = true)
    public List<AdministrativeEmployee1> findAllAdministrativeEmployee1() {
        log.warn("DEPRECATED: findAllAdministrativeEmployee1() — use Pageable variant.");
        return administrativeEmployee1Repository.findAll();
    }

    @Transactional
    public AdministrativeEmployee1 saveAdministrativeEmployee1(AdministrativeEmployee1 entity) {
        return administrativeEmployee1Repository.save(entity);
    }

    @Transactional
    public void softDeleteAdministrativeEmployee1(AdministrativeEmployee1 entity) {
        entity.setDeleteTs(LocalDateTime.now());
        entity.setDeletedBy(currentAuditor());
        administrativeEmployee1Repository.save(entity);
    }

    public Map<String, Object> toAdministrativeEmployee1Map(AdministrativeEmployee1 entity, Boolean returnNulls) {
        return toAdministrativeEmployee1Map(entity, returnNulls, null);
    }

    /**
     * OLD-HEMIS view — {@code rIAdministrativeEmployee1-view} (views.xml:567-573):
     * 5 refs (university, educationYear, employee, country, degree) {@code _minimal}.
     */
    public Map<String, Object> toAdministrativeEmployee1Map(AdministrativeEmployee1 entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ADMIN_EMP1_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RIAdministrativeEmployee1-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());

        boolean useNested = view != null && !view.isEmpty() && !"_local".equals(view);
        if (useNested) {
            if (entity.getUniversity() != null) {
                map.put("university", nestedObjectLoader.loadUniversity(entity.getUniversity()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", uz.hemis.common.JsonNull.INSTANCE);
            }
            nestedObjectLoader.putNestedWithNames(map, "educationYear", entity.getEducationYear(),
                    "hemishe_h_education_year", "HEducationYear", returnNulls);
            if (entity.getEmployee() != null) {
                Map<String, Object> emp = new LinkedHashMap<>();
                emp.put("_entityName", "hemishe_ETeacher");
                emp.put("id", entity.getEmployee().toString());
                emp.put("_instanceName", "com.company.hemishe.entity.ETeacher-" + entity.getEmployee() + " [detached]");
                map.put("employee", emp);
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employee", uz.hemis.common.JsonNull.INSTANCE);
            }
            nestedObjectLoader.putNestedWithNames(map, "country", entity.getCountry(),
                    "hemishe_h_country", "HCountry", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "degree", entity.getDegree(),
                    "hemishe_h_scientific_degree", "HScientificDegree", returnNulls);
        }

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

    /**
     * @deprecated OOM risk — use Pageable variant.
     */
    @Deprecated(since = "2.5.0", forRemoval = true)
    public List<AdministrativeEmployee2> findAllAdministrativeEmployee2() {
        log.warn("DEPRECATED: findAllAdministrativeEmployee2() — use Pageable variant.");
        return administrativeEmployee2Repository.findAll();
    }

    @Transactional
    public AdministrativeEmployee2 saveAdministrativeEmployee2(AdministrativeEmployee2 entity) {
        return administrativeEmployee2Repository.save(entity);
    }

    @Transactional
    public void softDeleteAdministrativeEmployee2(AdministrativeEmployee2 entity) {
        entity.setDeleteTs(LocalDateTime.now());
        entity.setDeletedBy(currentAuditor());
        administrativeEmployee2Repository.save(entity);
    }

    public Map<String, Object> toAdministrativeEmployee2Map(AdministrativeEmployee2 entity, Boolean returnNulls) {
        return toAdministrativeEmployee2Map(entity, returnNulls, null);
    }

    /**
     * OLD-HEMIS view — {@code rIAdministrativeEmployee2-view} (views.xml:574-581):
     * 6 refs (university, educationYear, employee, country, internshipForm, internshipType) {@code _minimal}.
     */
    public Map<String, Object> toAdministrativeEmployee2Map(AdministrativeEmployee2 entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ADMIN_EMP2_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RIAdministrativeEmployee2-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());

        boolean useNested = view != null && !view.isEmpty() && !"_local".equals(view);
        if (useNested) {
            if (entity.getUniversity() != null) {
                map.put("university", nestedObjectLoader.loadUniversity(entity.getUniversity()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", uz.hemis.common.JsonNull.INSTANCE);
            }
            nestedObjectLoader.putNestedWithNames(map, "educationYear", entity.getEducationYear(),
                    "hemishe_h_education_year", "HEducationYear", returnNulls);
            if (entity.getEmployee() != null) {
                // AdministrativeEmployee2.employee — String (tasdiqlangan entity field)
                Map<String, Object> emp = new LinkedHashMap<>();
                emp.put("_entityName", "hemishe_ETeacher");
                emp.put("id", entity.getEmployee());
                emp.put("_instanceName", "com.company.hemishe.entity.ETeacher-" + entity.getEmployee() + " [detached]");
                map.put("employee", emp);
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employee", uz.hemis.common.JsonNull.INSTANCE);
            }
            nestedObjectLoader.putNestedWithNames(map, "country", entity.getCountry(),
                    "hemishe_h_country", "HCountry", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "internshipForm", entity.getInternshipForm(),
                    "hemishe_h_internship_form", "HInternshipForm", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "internshipType", entity.getInternshipType(),
                    "hemishe_h_internship_type", "HInternshipType", returnNulls);
        }

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

    /**
     * @deprecated OOM risk — use Pageable variant.
     */
    @Deprecated(since = "2.5.0", forRemoval = true)
    public List<AdministrativeEmployee3> findAllAdministrativeEmployee3() {
        log.warn("DEPRECATED: findAllAdministrativeEmployee3() — use Pageable variant.");
        return administrativeEmployee3Repository.findAll();
    }

    @Transactional
    public AdministrativeEmployee3 saveAdministrativeEmployee3(AdministrativeEmployee3 entity) {
        return administrativeEmployee3Repository.save(entity);
    }

    @Transactional
    public void softDeleteAdministrativeEmployee3(AdministrativeEmployee3 entity) {
        entity.setDeleteTs(LocalDateTime.now());
        entity.setDeletedBy(currentAuditor());
        administrativeEmployee3Repository.save(entity);
    }

    public Map<String, Object> toAdministrativeEmployee3Map(AdministrativeEmployee3 entity, Boolean returnNulls) {
        return toAdministrativeEmployee3Map(entity, returnNulls, null);
    }

    /**
     * OLD-HEMIS view — {@code rIAdministrativeEmployee3-view} (views.xml:582-586):
     * 3 refs (university, educationYear, country) {@code _minimal}.
     */
    public Map<String, Object> toAdministrativeEmployee3Map(AdministrativeEmployee3 entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ADMIN_EMP3_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RIAdministrativeEmployee3-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());

        boolean useNested = view != null && !view.isEmpty() && !"_local".equals(view);
        if (useNested) {
            if (entity.getUniversity() != null) {
                map.put("university", nestedObjectLoader.loadUniversity(entity.getUniversity()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", uz.hemis.common.JsonNull.INSTANCE);
            }
            nestedObjectLoader.putNestedWithNames(map, "educationYear", entity.getEducationYear(),
                    "hemishe_h_education_year", "HEducationYear", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "country", entity.getCountry(),
                    "hemishe_h_country", "HCountry", returnNulls);
        }

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
