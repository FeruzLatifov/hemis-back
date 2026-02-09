package uz.hemis.service.legacy.finance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.ContractStatistics;
import uz.hemis.domain.entity.REmployment;
import uz.hemis.domain.entity.Scholarship;
import uz.hemis.domain.entity.ScholarshipAmount;
import uz.hemis.domain.repository.ContractStatisticsRepository;
import uz.hemis.domain.repository.REmploymentRepository;
import uz.hemis.domain.repository.ScholarshipAmountRepository;
import uz.hemis.domain.repository.ScholarshipRepository;
import uz.hemis.service.legacy.CubaNestedObjectLoader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Finance Entity Legacy Service
 *
 * <p>Finance domain entity lar uchun CRUD operatsiyalari:</p>
 * <ul>
 *   <li>ScholarshipAmount - Stipendiya summalari</li>
 *   <li>Scholarship - Stipendiya yozuvlari</li>
 *   <li>ContractStatistics - Shartnoma statistikasi</li>
 *   <li>REmployment - Bandlik statistikasi</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FinanceEntityLegacyService {

    private final ScholarshipAmountRepository scholarshipAmountRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ContractStatisticsRepository contractStatisticsRepository;
    private final REmploymentRepository rEmploymentRepository;
    private final CubaNestedObjectLoader nestedObjectLoader;

    // =====================================================
    // ScholarshipAmount Methods
    // =====================================================

    private static final String SCHOLARSHIP_AMOUNT_ENTITY = "hemishe_EStudentScholarshipAmount";

    public Optional<ScholarshipAmount> findScholarshipAmountById(UUID id) {
        return scholarshipAmountRepository.findById(id);
    }

    public List<ScholarshipAmount> findAllScholarshipAmount() {
        return scholarshipAmountRepository.findAll();
    }

    public Page<ScholarshipAmount> findAllScholarshipAmount(Pageable pageable) {
        return scholarshipAmountRepository.findAll(pageable);
    }

    @Transactional
    public ScholarshipAmount saveScholarshipAmount(ScholarshipAmount entity) {
        return scholarshipAmountRepository.save(entity);
    }

    @Transactional
    public void deleteScholarshipAmount(ScholarshipAmount entity) {
        scholarshipAmountRepository.delete(entity);
    }

    public Map<String, Object> toScholarshipAmountMap(ScholarshipAmount entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", SCHOLARSHIP_AMOUNT_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.EStudentScholarshipAmount-" + entity.getId() + " [detached]");
        map.put("id", entity.getId());
        putIfNotNull(map, "_studentScholarship", entity.getStudentScholarship(), returnNulls);
        putIfNotNull(map, "month", entity.getMonth(), returnNulls);
        putIfNotNull(map, "summa", entity.getSumma(), returnNulls);
        putIfNotNull(map, "localId", entity.getLocalId(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        return map;
    }

    public void updateScholarshipAmountFromMap(ScholarshipAmount entity, Map<String, Object> map) {
        if (map.containsKey("_studentScholarship")) {
            Object val = map.get("_studentScholarship");
            if (val != null) entity.setStudentScholarship(UUID.fromString(val.toString()));
        }
        if (map.containsKey("month")) {
            Object val = map.get("month");
            if (val != null) entity.setMonth(LocalDate.parse(val.toString()));
        }
        if (map.containsKey("summa")) {
            Object val = map.get("summa");
            if (val != null) entity.setSumma(Double.parseDouble(val.toString()));
        }
        if (map.containsKey("localId")) {
            Object val = map.get("localId");
            entity.setLocalId(val != null ? val.toString() : null);
        }
    }

    // =====================================================
    // Scholarship Methods
    // =====================================================

    private static final String SCHOLARSHIP_ENTITY = "hemishe_EStudentScholarshipFull";

    public Optional<Scholarship> findScholarshipById(UUID id) {
        return scholarshipRepository.findById(id);
    }

    public List<Scholarship> findAllScholarship() {
        return scholarshipRepository.findAll();
    }

    public Page<Scholarship> findAllScholarship(Pageable pageable) {
        return scholarshipRepository.findAll(pageable);
    }

    @Transactional
    public Scholarship saveScholarship(Scholarship entity) {
        return scholarshipRepository.save(entity);
    }

    @Transactional
    public void deleteScholarship(Scholarship entity) {
        scholarshipRepository.delete(entity);
    }

    public Map<String, Object> toScholarshipMap(Scholarship entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", SCHOLARSHIP_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.EStudentScholarshipFull-" + entity.getId() + " [detached]");
        map.put("id", entity.getId());
        putIfNotNull(map, "_student", entity.getStudent(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_educationType", entity.getEducationType(), returnNulls);
        putIfNotNull(map, "_educationForm", entity.getEducationForm(), returnNulls);
        putIfNotNull(map, "_paymentForm", entity.getPaymentForm(), returnNulls);
        putIfNotNull(map, "_semester", entity.getSemester(), returnNulls);
        putIfNotNull(map, "_educationYear", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "_stipendCategory", entity.getStipendCategory(), returnNulls);
        putIfNotNull(map, "_stipendType", entity.getStipendType(), returnNulls);
        putIfNotNull(map, "decree", entity.getDecree(), returnNulls);
        putIfNotNull(map, "group", entity.getGroup(), returnNulls);
        putIfNotNull(map, "curriculum", entity.getCurriculum(), returnNulls);
        putIfNotNull(map, "startDate", entity.getStartDate(), returnNulls);
        putIfNotNull(map, "endDate", entity.getEndDate(), returnNulls);
        putIfNotNull(map, "localId", entity.getLocalId(), returnNulls);
        putIfNotNull(map, "semesterNumber", entity.getSemesterNumber(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        return map;
    }

    public void updateScholarshipFromMap(Scholarship entity, Map<String, Object> map) {
        if (map.containsKey("_student")) {
            Object val = map.get("_student");
            if (val != null) entity.setStudent(UUID.fromString(val.toString()));
        }
        if (map.containsKey("_university")) {
            Object val = map.get("_university");
            entity.setUniversity(val != null ? val.toString() : null);
        }
        if (map.containsKey("_educationType")) {
            Object val = map.get("_educationType");
            entity.setEducationType(val != null ? val.toString() : null);
        }
        if (map.containsKey("_educationForm")) {
            Object val = map.get("_educationForm");
            entity.setEducationForm(val != null ? val.toString() : null);
        }
        if (map.containsKey("_paymentForm")) {
            Object val = map.get("_paymentForm");
            entity.setPaymentForm(val != null ? val.toString() : null);
        }
        if (map.containsKey("_semester")) {
            Object val = map.get("_semester");
            entity.setSemester(val != null ? val.toString() : null);
        }
        if (map.containsKey("_educationYear")) {
            Object val = map.get("_educationYear");
            entity.setEducationYear(val != null ? val.toString() : null);
        }
        if (map.containsKey("_stipendCategory")) {
            Object val = map.get("_stipendCategory");
            entity.setStipendCategory(val != null ? val.toString() : null);
        }
        if (map.containsKey("_stipendType")) {
            Object val = map.get("_stipendType");
            entity.setStipendType(val != null ? val.toString() : null);
        }
        if (map.containsKey("decree")) {
            Object val = map.get("decree");
            entity.setDecree(val != null ? val.toString() : null);
        }
        if (map.containsKey("group")) {
            Object val = map.get("group");
            entity.setGroup(val != null ? val.toString() : null);
        }
        if (map.containsKey("curriculum")) {
            Object val = map.get("curriculum");
            entity.setCurriculum(val != null ? val.toString() : null);
        }
        if (map.containsKey("startDate")) {
            Object val = map.get("startDate");
            if (val != null) entity.setStartDate(LocalDate.parse(val.toString()));
        }
        if (map.containsKey("endDate")) {
            Object val = map.get("endDate");
            if (val != null) entity.setEndDate(LocalDate.parse(val.toString()));
        }
        if (map.containsKey("localId")) {
            Object val = map.get("localId");
            entity.setLocalId(val != null ? val.toString() : null);
        }
        if (map.containsKey("semesterNumber")) {
            Object val = map.get("semesterNumber");
            entity.setSemesterNumber(val != null ? val.toString() : null);
        }
    }

    // =====================================================
    // ContractStatistics Methods
    // =====================================================

    private static final String CONTRACT_STATISTICS_ENTITY = "hemishe_RContractStatistics";

    public Optional<ContractStatistics> findContractStatisticsById(UUID id) {
        return contractStatisticsRepository.findById(id);
    }

    public List<ContractStatistics> findAllContractStatistics() {
        return contractStatisticsRepository.findAll();
    }

    public Page<ContractStatistics> findAllContractStatistics(Pageable pageable) {
        return contractStatisticsRepository.findAll(pageable);
    }

    @Transactional
    public ContractStatistics saveContractStatistics(ContractStatistics entity) {
        return contractStatisticsRepository.save(entity);
    }

    @Transactional
    public void deleteContractStatistics(ContractStatistics entity) {
        contractStatisticsRepository.delete(entity);
    }

    public Map<String, Object> toContractStatisticsMap(ContractStatistics entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", CONTRACT_STATISTICS_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.RContractStatistics-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());

        putIfNotNull(map, "date", entity.getDate(), returnNulls);

        // OLD-HEMIS: reference fieldlar (educationType, university, educationYear, educationForm,
        // faculty, course, semester) default view da qaytarilmaydi

        putIfNotNull(map, "dailyCount", entity.getDailyCount(), returnNulls);
        putIfNotNull(map, "total", entity.getTotal(), returnNulls);

        return map;
    }

    public void updateContractStatisticsFromMap(ContractStatistics entity, Map<String, Object> map) {
        if (map.containsKey("date")) {
            Object val = map.get("date");
            if (val != null) entity.setDate(LocalDate.parse(val.toString()));
        }
        if (map.containsKey("_university") || map.containsKey("university")) {
            String code = extractCode(map.containsKey("university") ? map.get("university") : map.get("_university"));
            entity.setUniversity(code);
        }
        if (map.containsKey("_educationYear") || map.containsKey("educationYear")) {
            String code = extractCode(map.containsKey("educationYear") ? map.get("educationYear") : map.get("_educationYear"));
            entity.setEducationYear(code);
        }
        if (map.containsKey("_educationType") || map.containsKey("educationType")) {
            String code = extractCode(map.containsKey("educationType") ? map.get("educationType") : map.get("_educationType"));
            entity.setEducationType(code);
        }
        if (map.containsKey("_educationForm") || map.containsKey("educationForm")) {
            String code = extractCode(map.containsKey("educationForm") ? map.get("educationForm") : map.get("_educationForm"));
            entity.setEducationForm(code);
        }
        if (map.containsKey("_faculty") || map.containsKey("faculty")) {
            String code = extractCode(map.containsKey("faculty") ? map.get("faculty") : map.get("_faculty"));
            entity.setFaculty(code);
        }
        if (map.containsKey("_course") || map.containsKey("course")) {
            String code = extractCode(map.containsKey("course") ? map.get("course") : map.get("_course"));
            entity.setCourse(code);
        }
        if (map.containsKey("_semester") || map.containsKey("semester")) {
            String code = extractCode(map.containsKey("semester") ? map.get("semester") : map.get("_semester"));
            entity.setSemester(code);
        }
        if (map.containsKey("dailyCount")) {
            Object val = map.get("dailyCount");
            if (val instanceof Number) entity.setDailyCount(((Number) val).intValue());
        }
        if (map.containsKey("total")) {
            Object val = map.get("total");
            if (val instanceof Number) entity.setTotal(((Number) val).intValue());
        }
    }

    // =====================================================
    // REmployment Methods
    // =====================================================

    private static final String R_EMPLOYMENT_ENTITY = "hemishe_REmployment";

    public Optional<REmployment> findREmploymentById(UUID id) {
        return rEmploymentRepository.findById(id);
    }

    public List<REmployment> findAllREmployment() {
        return rEmploymentRepository.findAll();
    }

    public Page<REmployment> findAllREmployment(Pageable pageable) {
        return rEmploymentRepository.findAll(pageable);
    }

    @Transactional
    public REmployment saveREmployment(REmployment entity) {
        return rEmploymentRepository.save(entity);
    }

    @Transactional
    public void deleteREmployment(REmployment entity) {
        rEmploymentRepository.delete(entity);
    }

    /**
     * UPSERT support: Find existing REmployment by 9-field unique key combination
     */
    public Optional<REmployment> findREmploymentByUniqueKey(
            String departmentCode,
            String educationYearCode,
            String educationTypeCode,
            String educationFormCode,
            String paymentFormCode,
            String genderCode,
            String workplaceCompatibilityCode,
            String graduateFieldsTypeCode,
            String graduateInactiveTypeCode) {
        return rEmploymentRepository.findByDepartmentCodeAndEducationYearCodeAndEducationTypeCodeAndEducationFormCodeAndPaymentFormCodeAndGenderCodeAndWorkplaceCompatibilityCodeAndGraduateFieldsTypeCodeAndGraduateInactiveTypeCode(
                departmentCode, educationYearCode, educationTypeCode, educationFormCode,
                paymentFormCode, genderCode, workplaceCompatibilityCode,
                graduateFieldsTypeCode, graduateInactiveTypeCode
        );
    }

    public Map<String, Object> toREmploymentMap(REmployment entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", R_EMPLOYMENT_ENTITY);
        map.put("_instanceName", "com.company.hemishe.entity.REmployment-" + entity.getId() + " [detached]");
        map.put("id", entity.getId());
        putIfNotNull(map, "uId", entity.getUId(), returnNulls);
        putIfNotNull(map, "qty", entity.getQty(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        return map;
    }

    /**
     * Minimal response map for CREATE endpoint (OLD-HEMIS compatible)
     */
    public Map<String, Object> toREmploymentMinimalMap(REmployment entity) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", R_EMPLOYMENT_ENTITY);
        response.put("_instanceName", "com.company.hemishe.entity.REmployment-" + entity.getId() + " [detached]");
        response.put("id", entity.getId().toString());
        return response;
    }

    public void updateREmploymentFromMap(REmployment entity, Map<String, Object> body) {
        // uId
        if (body.containsKey("uId")) {
            Object val = body.get("uId");
            if (val instanceof Number) {
                entity.setUId(((Number) val).longValue());
            } else if (val instanceof String) {
                entity.setUId(Long.parseLong((String) val));
            }
        }

        // qty
        if (body.containsKey("qty")) {
            Object val = body.get("qty");
            if (val instanceof Number) {
                entity.setQty(((Number) val).intValue());
            }
        }

        // FK fields - support both "university" and "_university" formats
        entity.setUniversityCode(extractCodeMultiKey(body, "university", "_university"));
        entity.setDepartmentCode(extractCodeMultiKey(body, "department", "_department"));
        entity.setEducationYearCode(extractCodeMultiKey(body, "educationYear", "_educationYear"));
        entity.setEducationTypeCode(extractCodeMultiKey(body, "educationType", "_educationType"));
        entity.setEducationFormCode(extractCodeMultiKey(body, "educationForm", "_educationForm"));
        entity.setPaymentFormCode(extractCodeMultiKey(body, "paymentForm", "_paymentForm"));
        entity.setGenderCode(extractCodeMultiKey(body, "gender", "_gender"));
        entity.setWorkplaceCompatibilityCode(extractCodeMultiKey(body, "workplaceCompatibility", "_workplaceCompatibility"));
        entity.setGraduateFieldsTypeCode(extractCodeMultiKey(body, "graduateFieldsType", "_graduateFieldsType"));
        entity.setGraduateInactiveTypeCode(extractCodeMultiKey(body, "graduateInactiveType", "_graduateInactiveType"));
    }

    /**
     * Extract UPSERT key codes from request body
     */
    public String extractREmploymentCode(Map<String, Object> body, String key1, String key2) {
        return extractCodeMultiKey(body, key1, key2);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    private void addInstanceName(Map<String, Object> nested, String code) {
        String name = nested.get("name") != null ? nested.get("name").toString() : "";
        nested.put("_instanceName", code + " " + name);
    }

    private String extractCodeMultiKey(Map<String, Object> body, String key1, String key2) {
        if (body.containsKey(key1)) {
            return extractCode(body.get(key1));
        }
        if (body.containsKey(key2)) {
            return extractCode(body.get(key2));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object code = nested.get("code");
            if (code != null) return code.toString();
            Object id = nested.get("id");
            if (id != null) return id.toString();
        }
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }
}
