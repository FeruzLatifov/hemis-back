package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Classifier Legacy Service - CUBA compatible operations for classifier entities
 *
 * Handles read-only operations for reference/classifier tables:
 * - hemishe_HEducationType
 * - hemishe_HEducationForm
 * - hemishe_HCourse
 * - hemishe_HEducationYear
 * - hemishe_HTransferType
 * - hemishe_HAdmissionType
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClassifierLegacyService {

    private final EducationTypeRepository educationTypeRepository;
    private final EducationFormRepository educationFormRepository;
    private final HCourseRepository hCourseRepository;
    private final EducationYearRepository educationYearRepository;
    private final TransferTypeRepository transferTypeRepository;
    private final AdmissionTypeRepository admissionTypeRepository;
    private final UniversityDepartmentTypeRepository universityDepartmentTypeRepository;
    private final uz.hemis.domain.repository.UniversityRepository universityRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final HokimiyatClassifierService hokimiyatClassifierService;

    private static final DateTimeFormatter CUBA_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * OLD-HEMIS /info endpoint uchun classifiers — aniq OLD nomlar va tartib (93 ta).
     * Key: OLD API dagi classifier nomi (univer tomonidan yuboriladi).
     * Value: hozirgi DB table nomi — YANGI jadvallar (V009-V013) ga yo'naltirilgan.
     * null = maxsus holat (h_university — repository orqali).
     * Izoh: Speciality* (bachelor/master/ordinatura/doctoral) va h_employment_form
     *       hali hemishe_h_* da, chunki yangi jadval yaratilmagan.
     */
    private static final LinkedHashMap<String, String> OLD_CLASSIFIER_MAP = new LinkedHashMap<>() {{
        put("h_admission_type", "hemishe_h_admission_type");
        put("h_transfer_type", "hemishe_h_transfer_type");
        put("h_external_service_type", "hemishe_h_external_service_type");
        put("h_grant_type", "hemishe_h_grant_type");
        put("h_poverty_level", "hemishe_h_poverty_level");
        put("h_certificate_grades", "hemishe_h_certificate_grades");
        put("h_certificate_subjects", "hemishe_h_certificate_subjects");
        put("h_certificate_names", "hemishe_h_certificate_names");
        put("h_certificate_type", "hemishe_h_certificate_type");
        put("h_hemis_version_type", "hemishe_h_hemis_version_type");
        put("h_outside_activity", "hemishe_h_outside_activities");
        put("h_outside_activities", "hemishe_h_outside_activities");
        put("h_scholarship_decree_type", "hemishe_h_scholarship_decree_type");
        put("h_resource_type", "hemishe_h_resource_type");
        put("h_internship_type", "hemishe_h_internship_type");
        put("h_internship_form", "hemishe_h_internship_form");
        put("h_conduction_form", "hemishe_h_teacher_conduction_form");
        put("h_terrain", "hemishe_h_terrain");
        put("h_stipend_rate_category", "hemishe_h_stipend_rate_category");
        put("h_speciality_ordinatura", "hemishe_h_speciality_ordinatura"); // legacy (Speciality tegilmagan)
        put("h_academic_mobile_type", "hemishe_h_academic_mobile_type");
        put("h_contract_class", "hemishe_h_contract_types");
        put("h_student_living_status", "hemishe_h_student_living_status");
        put("h_student_roommate_type", "hemishe_h_student_room_mate_type");
        put("h_student_type", "hemishe_h_student_type");
        put("h_sport_type", "hemishe_h_sport_type");
        put("h_graduate_inactive_type", "hemishe_h_graduate_inactive_type");
        put("h_graduate_fields_type", "hemishe_h_graduate_fields_type");
        put("h_diplom_blank_status", "hemishe_h_diplom_blank_status");
        put("h_diplom_blank_category", "hemishe_h_diplom_blank_category");
        put("h_contract_summa_type", "hemishe_h_contract_summa_type");
        put("h_contract_type", "hemishe_h_contract_type");
        put("h_decree_type", "hemishe_h_decree_type");
        put("h_decree_type_param", "hemishe_h_decree_type_param");
        put("h_scientific_platform", "hemishe_h_scholar_database");
        put("h_doctorate_student_status", "hemishe_h_doctoral_student_status");
        put("h_education_year", "hemishe_h_education_year");
        put("h_semester", "hemishe_h_semester_list");
        put("h_science_branch", "hemishe_h_speciality_doctoral"); // legacy special (Speciality tegilmagan)
        put("h_exam_finish", "hemishe_h_exam_finish");
        put("h_final_exam_type", "hemishe_h_final_exam_type");
        put("h_locality_type", "hemishe_h_locality_type");
        put("h_academic_reason", "hemishe_h_academic_reason");
        put("h_attendance_setting", "hemishe_h_attandance_setting");
        put("h_university", null); // Special: repository-based
        put("h_country", "hemishe_h_country");
        put("h_soato", "hemishe_h_soato");
        put("h_nationality", "hemishe_h_nationality");
        put("h_citizenship_type", "hemishe_h_citizenship");
        put("h_gender", "hemishe_h_gender");
        put("h_bachelor_speciality", "hemishe_h_speciality_bachelor"); // legacy (Speciality tegilmagan)
        put("h_master_speciality", "hemishe_h_speciality_master"); // legacy (Speciality tegilmagan)
        put("h_university_form", "hemishe_h_university_type");
        put("h_ownership", "hemishe_h_ownership");
        put("h_structure_type", "hemishe_h_university_department_type");
        put("h_employee_type", "hemishe_h_university_employee_type");
        put("h_teacher_status", "hemishe_h_university_employee_status_type");
        put("h_employment_staff", "hemishe_h_university_employee_rate");
        put("h_employment_form", "hemishe_h_employment_form"); // legacy (boshqa jadval, migratsiya qilinmagan)
        // Univer (old-hemis) uchun hemishe_h_teacher_position_type eski CUBA jadvalga yo'naltiriladi.
        // Yangi backend kodi position + position_type (V013) bilan ishlaydi — dublikatsiya yo'q.
        put("h_teacher_position_type", "hemishe_h_teacher_position_type");
        put("h_qualification", "hemishe_h_qualification");
        put("h_teacher_success", "hemishe_h_teacher_achievement_type");
        put("h_academic_degree", "hemishe_h_academic_degree");
        put("h_academic_rank", "hemishe_h_academic_rank");
        put("h_student_status", "hemishe_h_student_status_type");
        put("h_student_success", "hemishe_h_student_achievement_type");
        put("h_expel_reason", "hemishe_h_expel");
        put("h_accommodation", "hemishe_h_accomodation");
        put("h_doctoral_student_type", "hemishe_h_doctoral_student_type");
        put("h_social_category", "hemishe_h_student_social_type");
        put("h_education_type", "hemishe_h_education_type");
        put("h_education_form", "hemishe_h_education_form");
        put("h_language", "hemishe_h_education_language");
        put("h_marking_system", "hemishe_h_grade_system_type");
        put("h_grade_type", "hemishe_h_score_type");
        put("h_exam_type", "hemishe_h_exam_type");
        put("h_course", "hemishe_h_course");
        put("h_semestr_type", "hemishe_h_semester");
        put("h_education_week_type", "hemishe_h_education_week_type");
        put("h_subject_block", "hemishe_h_subject_block");
        put("h_subject_type", "hemishe_h_subject_type");
        put("h_training_type", "hemishe_h_class_type");
        put("h_project_type", "hemishe_h_project_type");
        put("h_locality", "hemishe_h_project_locality");
        put("h_project_currency", "hemishe_h_currency");
        put("h_project_executor_type", "hemishe_h_project_executor_type");
        put("h_scientific_publication_type", "hemishe_h_publication_type");
        put("h_methodical_publication_type", "hemishe_h_methodical_publication_type");
        put("h_patient_type", "hemishe_h_patient_type");
        put("h_publication_database", "hemishe_h_publication_database");
        put("h_payment_form", "hemishe_h_payment_form");
        put("h_stipend_rate", "hemishe_h_stipend_rate");
        put("h_auditorium_type", "hemishe_h_auditorium_type");
        put("h_device_type", "hemishe_h_device_type");
    }};

    // ==================== EducationType ====================

    // Cache invariant: barcha findAll* metodlar List.copyOf(...) bilan immutable qaytaradi.
    // Sabab: Hibernate-managed mutable list cache'ga to'g'ridan-to'g'ri yozilsa, caller
    // mutation cache'ni buzadi. @CacheEvict pair YO'Q — hemishe_h_* FROZEN (faqat seed),
    // runtime modifikatsiya yo'q (`grep` 0 ta save/update/delete topdi).

    @Cacheable(value = "classifierEducationType", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<EducationType> findAllEducationTypes() {
        return List.copyOf(educationTypeRepository.findAll());
    }

    public Optional<EducationType> findEducationTypeByCode(String code) {
        return educationTypeRepository.findById(code);
    }

    public Map<String, Object> toEducationTypeMap(EducationType e, Boolean returnNulls) {
        return classifierToMap("hemishe_HEducationType", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.isActive(), e.getCreatedAt(), e.getCreatedBy(),
                e.getUpdatedAt(), e.getUpdatedBy(), null, null, returnNulls);
    }

    // ==================== EducationForm ====================

    @Cacheable(value = "classifierEducationForm", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<EducationForm> findAllEducationForms() {
        return List.copyOf(educationFormRepository.findAll());
    }

    public Optional<EducationForm> findEducationFormByCode(String code) {
        return educationFormRepository.findById(code);
    }

    public Map<String, Object> toEducationFormMap(EducationForm e, Boolean returnNulls) {
        return classifierToMap("hemishe_HEducationForm", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.isActive(), e.getCreatedAt(), e.getCreatedBy(),
                e.getUpdatedAt(), e.getUpdatedBy(), null, null, returnNulls);
    }

    // ==================== HCourse ====================

    @Cacheable(value = "classifierCourse", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<HCourse> findAllCourses() {
        return List.copyOf(hCourseRepository.findAll());
    }

    public Optional<HCourse> findCourseByCode(String code) {
        return hCourseRepository.findById(code);
    }

    public Map<String, Object> toCourseMap(HCourse e, Boolean returnNulls) {
        return classifierToMap("hemishe_HCourse", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.isActive(), e.getCreatedAt(), e.getCreatedBy(),
                e.getUpdatedAt(), e.getUpdatedBy(), null, null, returnNulls);
    }

    // ==================== EducationYear ====================

    @Cacheable(value = "classifierEducationYear", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<EducationYear> findAllEducationYears() {
        return List.copyOf(educationYearRepository.findAll());
    }

    public Optional<EducationYear> findEducationYearByCode(String code) {
        return educationYearRepository.findById(code);
    }

    public Map<String, Object> toEducationYearMap(EducationYear e, Boolean returnNulls) {
        return classifierToMap("hemishe_HEducationYear", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.isActive(), e.getCreatedAt(), e.getCreatedBy(),
                e.getUpdatedAt(), e.getUpdatedBy(), null, null, returnNulls);
    }

    // ==================== TransferType ====================

    @Cacheable(value = "classifierTransferType", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<TransferType> findAllTransferTypes() {
        return List.copyOf(transferTypeRepository.findAll());
    }

    public Optional<TransferType> findTransferTypeByCode(String code) {
        return transferTypeRepository.findById(code);
    }

    public Map<String, Object> toTransferTypeMap(TransferType e, Boolean returnNulls) {
        return classifierToMap("hemishe_HTransferType", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.isActive(), e.getCreatedAt(), e.getCreatedBy(),
                e.getUpdatedAt(), e.getUpdatedBy(), null, null, returnNulls);
    }

    // ==================== AdmissionType ====================

    @Cacheable(value = "classifierAdmissionType", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<AdmissionType> findAllAdmissionTypes() {
        return List.copyOf(admissionTypeRepository.findAll());
    }

    public Optional<AdmissionType> findAdmissionTypeByCode(String code) {
        return admissionTypeRepository.findById(code);
    }

    public Map<String, Object> toAdmissionTypeMap(AdmissionType e, Boolean returnNulls) {
        return classifierToMap("hemishe_HAdmissionType", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.isActive(), e.getCreatedAt(), e.getCreatedBy(),
                e.getUpdatedAt(), e.getUpdatedBy(), null, null, returnNulls);
    }

    // ==================== Shared CUBA Map Builder ====================

    /**
     * NamePattern map — old-hemis @NamePattern ga mos.
     * "name" = faqat name; "code_space_name" = code + " " + name;
     * "code_dash_name" = code + " - " + name; "code_hyphen_name" = code + "-" + name
     */
    private static final Map<String, String> NAME_PATTERN_MAP = Map.ofEntries(
        // BaseCodeNameEntity: "%s %s|code,name"
        Map.entry("hemishe_HEducationType", "code_space_name"),
        Map.entry("hemishe_HEducationForm", "code_space_name"),
        Map.entry("hemishe_HCourse", "code_space_name"),
        Map.entry("hemishe_HAdmissionType", "code_space_name"),
        Map.entry("hemishe_HAcademicDegree", "code_space_name"),
        Map.entry("hemishe_HAcademicReason", "code_space_name"),
        Map.entry("hemishe_HCountry", "code_space_name"),
        Map.entry("hemishe_HExamFinish", "code_space_name"),
        Map.entry("hemishe_HResourceType", "code_space_name"),
        Map.entry("hemishe_HScienceBranch", "code_space_name"),
        Map.entry("hemishe_HCertificateLanguage", "code_space_name"),
        Map.entry("hemishe_HTerrain", "code_space_name"),
        Map.entry("hemishe_HUniversityBelongsTo", "code_space_name"),
        Map.entry("hemishe_HTeacherConductionForm", "code_space_name"),
        Map.entry("hemishe_HScholarshipDecreeType", "code_space_name"),
        Map.entry("hemishe_HAttandanceSetting", "code_space_name"),
        Map.entry("hemishe_HHemisVersionType", "code_space_name"),
        // "%s - %s|code,name"
        Map.entry("hemishe_HPaymentForm", "code_dash_name"),
        Map.entry("hemishe_HDiplomBlankGenerateStatus", "code_dash_name"),
        Map.entry("hemishe_HSpecialityBachelor", "code_dash_name"),
        Map.entry("hemishe_HSpecialityMaster", "code_dash_name"),
        Map.entry("hemishe_HSpecialityOrdinatura", "code_dash_name"),
        Map.entry("hemishe_HStipendRate", "code_dash_name"),
        Map.entry("hemishe_HStipendRateCategory", "code_dash_name"),
        // "%s-%s|code,name"
        Map.entry("hemishe_HUniversityActivityStatus", "code_hyphen_name")
        // All others default to "name" only
    );

    /**
     * Build _instanceName based on old-hemis @NamePattern for each entity.
     */
    private static String buildInstanceName(String entityName, String code, String name) {
        String pattern = NAME_PATTERN_MAP.getOrDefault(entityName, "name");
        return switch (pattern) {
            case "code_space_name" -> code + " " + name;
            case "code_dash_name" -> code + " - " + name;
            case "code_hyphen_name" -> code + "-" + name;
            default -> name;
        };
    }

    /**
     * Convert classifier entity to CUBA-compatible Map
     */
    public Map<String, Object> classifierToMap(String entityName, String code, String name,
            String nameRu, String nameEn, Integer version, Boolean active,
            LocalDateTime createTs, String createdBy,
            LocalDateTime updateTs, String updatedBy,
            LocalDateTime deleteTs, String deletedBy,
            Boolean returnNulls) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", entityName);
        map.put("_instanceName", buildInstanceName(entityName, code, name));
        map.put("code", code);
        map.put("name", name);

        CubaEntityMapHelper.putIfNotNull(map, "nameRu", nameRu, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "nameEn", nameEn, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", version, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", active, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createTs", createTs != null ? createTs.format(CUBA_DT) : null, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createdBy", createdBy, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", updateTs != null ? updateTs.format(CUBA_DT) : null, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updatedBy", updatedBy, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", deleteTs != null ? deleteTs.format(CUBA_DT) : null, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deletedBy", deletedBy, returnNulls);

        return map;
    }

    /**
     * Apply pagination to list
     */
    public <T> List<T> applyPagination(List<T> list, int limit, int offset) {
        int from = Math.min(offset, list.size());
        int to = Math.min(from + limit, list.size());
        return list.subList(from, to);
    }

    // ==================== Dynamic Classifier Methods ====================

    /**
     * Get all classifiers with items (OLD-HEMIS /allItems endpoint).
     * Uses OLD_CLASSIFIER_MAP to return exactly 93 classifiers matching old-hemis.
     */
    public Map<String, Object> getAllClassifiersWithItems() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : OLD_CLASSIFIER_MAP.entrySet()) {
            String oldApiName = entry.getKey();
            String tableName = entry.getValue();

            try {
                // Special case: h_university (repository-based)
                if (tableName == null) {
                    if ("h_university".equals(oldApiName)) {
                        Map<String, Object> uniData = getUniversityClassifier();
                        if (uniData != null) {
                            // Extract classifier from wrapper
                            Object clf = uniData.get("classifier");
                            if (clf instanceof Map<?, ?> clfMap) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> typedMap = (Map<String, Object>) clfMap;
                                classifiersList.add(typedMap);
                            }
                        }
                    }
                    continue;
                }

                if (!tableExists(tableName)) {
                    continue;
                }

                Map<String, Object> classifierData = getClassifierWithItems(tableName, oldApiName);
                if (classifierData != null) {
                    classifiersList.add(classifierData);
                }
            } catch (Exception e) {
                log.debug("Error loading classifier {}: {}", oldApiName, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    /**
     * Get all classifiers info (metadata only) (OLD-HEMIS /info endpoint).
     * Uses OLD_CLASSIFIER_MAP to return exactly the same 93 classifier names
     * and order as old-hemis for backward compatibility.
     */
    public Map<String, Object> getAllClassifiersInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : OLD_CLASSIFIER_MAP.entrySet()) {
            String oldApiName = entry.getKey();
            String tableName = entry.getValue();

            try {
                if (tableName == null) {
                    // Special case: h_university (repository-based)
                    if ("h_university".equals(oldApiName)) {
                        classifiersList.add(getUniversityClassifierInfo());
                    }
                    continue;
                }

                if (!tableExists(tableName)) {
                    continue;
                }

                Map<String, Object> classifierInfo = getClassifierInfoWithName(tableName, oldApiName);
                if (classifierInfo != null) {
                    classifiersList.add(classifierInfo);
                }
            } catch (Exception e) {
                log.debug("Error loading classifier info {}: {}", oldApiName, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    /**
     * Get single classifier by name (OLD-HEMIS /single endpoint).
     * Bosqich 3 refactor: OLD_CLASSIFIER_MAP orqali yangi jadvallarga yo'naltiriladi,
     * shunday qilib /single endpoint ham /allItems/info kabi yangi data manbasiga mos bo'ladi.
     */
    public Map<String, Object> getSingleClassifier(String classifier) {
        if (classifier == null || classifier.isEmpty()) {
            return null;
        }

        // Special handling for h_university
        if ("h_university".equals(classifier)) {
            return getUniversityClassifier();
        }

        // OLD_CLASSIFIER_MAP orqali to'g'ri jadvalni aniqlash (yangi yoki legacy).
        // Fallback: agar map'da topilmasa, eski xulq-atvor — "hemishe_" + classifier.
        String tableName = OLD_CLASSIFIER_MAP.get(classifier);
        if (tableName == null) {
            tableName = "hemishe_" + classifier;
        }
        if (!tableExists(tableName)) {
            return null;
        }

        try {
            Map<String, Object> classifierData = getClassifierWithItems(tableName, classifier);
            if (classifierData == null) {
                return null;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("classifier", classifierData);
            return result;
        } catch (Exception e) {
            log.error("Error loading classifier {}: {}", classifier, e.getMessage());
            return null;
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Convert flat parentId/parentCode/parentName fields into nested parent object.
     * OLD-HEMIS returns parent as: { "_entityName", "_instanceName", "id", "code", "name", "nameEn", "nameRu", "version", "isChecked" }
     */
    private void buildParentNestedObject(Map<String, Object> item, String entityName) {
        Object parentId = item.remove("parentId");
        Object parentCode = item.remove("parentCode");
        Object parentName = item.remove("parentName");
        Object parentNameEn = item.remove("parentNameEn");
        Object parentNameRu = item.remove("parentNameRu");
        Object parentVersion = item.remove("parentVersion");
        Object parentIsChecked = item.remove("parentIsChecked");
        Object parentActive = item.remove("parentActive");

        if (parentId == null) {
            return; // no parent — don't add null parent field
        }

        Map<String, Object> parent = new LinkedHashMap<>();
        if (entityName != null) {
            parent.put("_entityName", entityName);
            parent.put("_instanceName", parentCode != null ? parentCode.toString() + " " + parentName : parentName);
        }
        parent.put("id", parentId.toString());
        parent.put("code", parentCode);
        parent.put("name", parentName);
        if (parentNameEn != null) parent.put("nameEn", parentNameEn);
        if (parentNameRu != null) parent.put("nameRu", parentNameRu);
        parent.put("version", parentVersion);
        if (parentIsChecked != null) parent.put("isChecked", parentIsChecked);
        if (parentActive != null) parent.put("active", parentActive);

        item.put("parent", parent);
    }

    private String getCubaEntityName(String tableName) {
        return switch (tableName) {
            // === YANGI jadvallarda LEGACY CUBA entity nomi saqlanadi (rename compat) ===
            // Univer _entityName'ga tayangan bo'lishi mumkin — eski nomni yubormasak buzilishi mumkin
            case "hemis_version" -> "hemishe_HHemisVersionType";              // old: hemishe_h_hemis_version_type
            case "employee_rate" -> "hemishe_HUniversityEmployeeRate";        // old: hemishe_h_university_employee_rate
            case "contract_class" -> "hemishe_HContractTypes";                // old: hemishe_h_contract_types
            case "certificate_grade" -> "hemishe_HCertificateGrades";         // old: plural (_grades)
            case "certificate_subject" -> "hemishe_HCertificateSubjects";     // old: plural (_subjects)
            case "certificate_name" -> "hemishe_HCertificateNames";           // old: plural (_names)
            case "outside_activity" -> "hemishe_HOutsideActivities";          // old: plural (_activities)
            default -> {
                // Generic conversion:
                //   hemishe_h_academic_degree → hemishe_HAcademicDegree (eski CUBA)
                //   gender → hemishe_HGender (yangi jadval, prefixed)
                //   education_language → hemishe_HEducationLanguage
                String suffix = tableName.startsWith("hemishe_h_")
                        ? tableName.substring("hemishe_h_".length())
                        : tableName;
                StringBuilder sb = new StringBuilder("hemishe_H");
                boolean capitalizeNext = true;
                for (char c : suffix.toCharArray()) {
                    if (c == '_') { capitalizeNext = true; continue; }
                    sb.append(capitalizeNext ? Character.toUpperCase(c) : c);
                    capitalizeNext = false;
                }
                yield sb.toString();
            }
        };
    }

    private List<String> getClassifierTables() {
        try {
            String sql = "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = 'public' AND table_name LIKE 'hemishe_h_%' " +
                    "ORDER BY table_name";
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            log.error("Error getting classifier tables: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName, columnName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> getClassifierWithItems(String tableName) {
        return getClassifierWithItems(tableName, null);
    }

    private Map<String, Object> getClassifierWithItems(String tableName, String apiName) {
        String classifierName = apiName != null ? apiName : tableName.replace("hemishe_", "");
        boolean hasVersion = columnExists(tableName, "version");
        // Eski jadvallar: `active`. Yangi jadvallar: `is_active`. Ikkalasini ham tekshiramiz.
        boolean hasActive = columnExists(tableName, "active") || columnExists(tableName, "is_active");
        boolean hasDeleteTs = columnExists(tableName, "delete_ts");

        String countSql = buildCountSql(tableName, hasVersion, hasDeleteTs);
        Map<String, Object> countResult;
        try {
            countResult = jdbcTemplate.queryForMap(countSql);
        } catch (Exception e) {
            log.debug("Error counting {}: {}", tableName, e.getMessage());
            return null;
        }

        long count = ((Number) countResult.get("cnt")).longValue();
        long version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).longValue() : 0;

        String itemsSql = buildItemsSql(tableName, hasActive, hasDeleteTs);
        List<Map<String, Object>> items;
        try {
            items = jdbcTemplate.queryForList(itemsSql);
        } catch (Exception e) {
            log.debug("Error loading items from {}: {}", tableName, e.getMessage());
            items = Collections.emptyList();
        }

        // Add _entityName, id (CUBA compatibility: id = code if no UUID id column)
        String entityName = getCubaEntityName(tableName);
        for (Map<String, Object> item : items) {
            if (entityName != null) {
                item.put("_entityName", entityName);
            }
            // CUBA: id field must always exist — use code if no UUID id column
            if (!item.containsKey("id") && item.containsKey("code")) {
                item.put("id", item.get("code"));
            }
            buildParentNestedObject(item, entityName);
            item.values().removeIf(v -> v == null);
        }

        Map<String, Object> classifierData = new LinkedHashMap<>();
        classifierData.put("title", getClassifierTitle(classifierName));
        classifierData.put("version", version);
        classifierData.put("count", count);
        classifierData.put("items", items);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(classifierName, classifierData);
        return wrapper;
    }

    /**
     * Get classifier info using a custom API name (for OLD-HEMIS name mapping).
     */
    private Map<String, Object> getClassifierInfoWithName(String tableName, String apiName) {
        boolean hasVersion = columnExists(tableName, "version");
        boolean hasDeleteTs = columnExists(tableName, "delete_ts");

        String countSql = buildCountSql(tableName, hasVersion, hasDeleteTs);
        Map<String, Object> countResult;
        try {
            countResult = jdbcTemplate.queryForMap(countSql);
        } catch (Exception e) {
            log.debug("Error counting {}: {}", tableName, e.getMessage());
            return null;
        }

        long count = ((Number) countResult.get("cnt")).longValue();
        long version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).longValue() : 0;

        Map<String, Object> classifierInfo = new LinkedHashMap<>();
        classifierInfo.put("title", getClassifierTitle(apiName));
        classifierInfo.put("version", version);
        classifierInfo.put("count", count);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(apiName, classifierInfo);
        return wrapper;
    }

    /**
     * Get university classifier info (metadata only, for /info endpoint).
     * Uses SUM(version) — matching old-hemis.
     */
    private Map<String, Object> getUniversityClassifierInfo() {
        try {
            String statsSql = "SELECT COUNT(*) as cnt, COALESCE(SUM(COALESCE(version, 1)), 0) as ver " +
                               "FROM hemishe_e_university WHERE delete_ts IS NULL";
            Map<String, Object> stats = jdbcTemplate.queryForMap(statsSql);
            long count = ((Number) stats.get("cnt")).longValue();
            long versionSum = ((Number) stats.get("ver")).longValue();

            Map<String, Object> classifierInfo = new LinkedHashMap<>();
            classifierInfo.put("title", "hemishe_h_Oliy ta'lim muassasalari ro'yxati");
            classifierInfo.put("version", versionSum);
            classifierInfo.put("count", count);

            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("h_university", classifierInfo);
            return wrapper;
        } catch (Exception e) {
            log.debug("Error getting university classifier info: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> getClassifierInfo(String tableName) {
        String classifierName = tableName.replace("hemishe_", "");
        boolean hasVersion = columnExists(tableName, "version");
        boolean hasDeleteTs = columnExists(tableName, "delete_ts");

        String countSql = buildCountSql(tableName, hasVersion, hasDeleteTs);
        Map<String, Object> countResult;
        try {
            countResult = jdbcTemplate.queryForMap(countSql);
        } catch (Exception e) {
            log.debug("Error counting {}: {}", tableName, e.getMessage());
            return null;
        }

        long count = ((Number) countResult.get("cnt")).longValue();
        long version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).longValue() : 0;

        Map<String, Object> classifierInfo = new LinkedHashMap<>();
        classifierInfo.put("title", getClassifierTitle(classifierName));
        classifierInfo.put("version", version);
        classifierInfo.put("count", count);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(classifierName, classifierInfo);
        return wrapper;
    }

    private Map<String, Object> getUniversityClassifier() {
        // Delegate to HokimiyatClassifierService which has the university items builder
        Map<String, Object> uniClassifier = hokimiyatClassifierService.getUniversityClassifierForHokimiyat();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("classifier", uniClassifier);
        return result;
    }

    private String buildCountSql(String tableName, boolean hasVersion, boolean hasDeleteTs) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as cnt");
        if (hasVersion) {
            // OLD-HEMIS uses SUM(version), NOT MAX(version)
            sql.append(", COALESCE(SUM(COALESCE(version, 1)), 0) as ver");
        } else {
            sql.append(", 1 as ver");
        }
        sql.append(" FROM ").append(tableName);
        if (hasDeleteTs) {
            sql.append(" WHERE delete_ts IS NULL");
        }
        return sql.toString();
    }

    private String buildItemsSql(String tableName, boolean hasActive, boolean hasDeleteTs) {
        boolean hasParentUuid = columnExists(tableName, "_parent");
        boolean hasParentCode = columnExists(tableName, "parent_code");
        // Eski: `active`, Yangi: `is_active` — univer doim `active` kutadi → alias bilan
        boolean hasActiveCol = columnExists(tableName, "active");
        boolean hasIsActiveCol = columnExists(tableName, "is_active");

        StringBuilder sql = new StringBuilder("SELECT ");
        String alias = hasParentUuid ? "t." : "";
        // UUID id (speciality tables have UUID primary key)
        if (columnExists(tableName, "id")) {
            sql.append(alias).append("id, ");
        }
        sql.append(alias).append("code, ").append(alias).append("name");
        // Additional columns for CUBA compatibility
        if (columnExists(tableName, "name_en")) sql.append(", ").append(alias).append("name_en as \"nameEn\"");
        if (columnExists(tableName, "name_ru")) sql.append(", ").append(alias).append("name_ru as \"nameRu\"");
        if (hasActiveCol) sql.append(", ").append(alias).append("active");
        else if (hasIsActiveCol) sql.append(", ").append(alias).append("is_active as active");
        if (columnExists(tableName, "is_checked")) sql.append(", ").append(alias).append("is_checked as \"isChecked\"");
        if (hasParentCode) sql.append(", ").append(alias).append("parent_code as \"parentCode\"");
        sql.append(", COALESCE(").append(alias).append("version, 1) as version");
        // Self-join for _parent UUID: fetch parent entity fields
        if (hasParentUuid) {
            sql.append(", p.id as \"parentId\", p.code as \"parentCode\"");
            sql.append(", p.name as \"parentName\"");
            if (columnExists(tableName, "name_en")) sql.append(", p.name_en as \"parentNameEn\"");
            if (columnExists(tableName, "name_ru")) sql.append(", p.name_ru as \"parentNameRu\"");
            sql.append(", COALESCE(p.version, 1) as \"parentVersion\"");
            if (columnExists(tableName, "is_checked")) sql.append(", p.is_checked as \"parentIsChecked\"");
            if (hasActiveCol) sql.append(", p.active as \"parentActive\"");
            else if (hasIsActiveCol) sql.append(", p.is_active as \"parentActive\"");
        }
        sql.append(" FROM ").append(tableName);
        if (hasParentUuid) {
            sql.append(" t LEFT JOIN ").append(tableName).append(" p ON t._parent = p.id");
        }
        if (hasDeleteTs) {
            sql.append(hasParentUuid ? " WHERE t.delete_ts IS NULL" : " WHERE delete_ts IS NULL");
        }
        sql.append(" ORDER BY ").append(hasParentUuid ? "t." : "").append("code");
        return sql.toString();
    }

    private String getClassifierTitle(String classifierName) {
        return switch (classifierName) {
            case "h_admission_type" -> "Qabul turlari";
            case "h_transfer_type" -> "O'tkazish turlari";
            case "h_external_service_type" -> "Tashqi xizmat turlari";
            case "h_grant_type" -> "Grant turlari";
            case "h_poverty_level" -> "Kam ta'minlanganlik darajasi";
            case "h_certificate_grades" -> "Attestat baholari";
            case "h_certificate_subjects" -> "Attestat fanlari";
            case "h_certificate_names" -> "Attestat nomlari";
            case "h_certificate_type" -> "Attestat turlari";
            case "h_hemis_version_type" -> "HEMIS versiya turlari";
            case "h_outside_activity", "h_outside_activities" -> "Auditoriyadan tashqari mashg'ulotlar";
            case "h_scholarship_decree_type" -> "Stipendiya qaror turlari";
            case "h_resource_type" -> "Resurs turlari";
            case "h_internship_type" -> "Amaliyot turlari";
            case "h_internship_form" -> "Amaliyot shakllari";
            case "h_conduction_form" -> "Dars o'tkazish shakllari";
            case "h_terrain" -> "Joylashgan hudud turi";
            case "h_stipend_rate_category" -> "Stipendiya stavka toifalari";
            case "h_speciality_ordinatura" -> "Ordinatura mutaxassisliklari";
            case "h_academic_mobile_type" -> "Akademik mobillik turlari";
            case "h_contract_class" -> "Shartnoma sinflari";
            case "h_student_living_status" -> "Talaba yashash joyi statusi";
            case "h_student_roommate_type" -> "Birgalikda yashaydiganlar toifasi";
            case "h_student_type" -> "Talaba toifalari";
            case "h_sport_type" -> "Sport turlari";
            case "h_graduate_inactive_type" -> "Bitiruvchi nofaol turlari";
            case "h_graduate_fields_type" -> "Bitiruvchi soha turlari";
            case "h_diplom_blank_status" -> "Diplom blank holati";
            case "h_diplom_blank_category" -> "Diplom blank toifasi";
            case "h_contract_summa_type" -> "Shartnoma summa turlari";
            case "h_contract_type" -> "Shartnoma turlari";
            case "h_decree_type" -> "Buyruq turlari";
            case "h_decree_type_param" -> "Buyruq turi parametrlari";
            case "h_scientific_platform" -> "Ilmiy platformalar";
            case "h_doctorate_student_status" -> "Doktorant holatlari";
            case "h_education_year" -> "O'quv yillar ro'yxati";
            case "h_semester" -> "Semestrlar";
            case "h_science_branch" -> "Fan tarmoqlari";
            case "h_exam_finish" -> "Imtihon yakunlash turlari";
            case "h_final_exam_type" -> "Yakuniy imtihon turlari";
            case "h_locality_type" -> "Joylashuv turlari";
            case "h_academic_reason" -> "Akademik ta'til sabablari";
            case "h_attendance_setting" -> "Davomat sozlamalari";
            case "h_university" -> "Oliy ta'lim muassasalari ro'yxati";
            case "h_country" -> "Davlatlar nomlari";
            case "h_soato" -> "Viloyat va tumanlar";
            case "h_nationality" -> "Millatlar nomlari";
            case "h_citizenship_type" -> "Fuqarolik holatlari turlari";
            case "h_gender" -> "Jins turlari";
            case "h_bachelor_speciality" -> "Bakalavriat ta'lim yo'nalishlari";
            case "h_master_speciality" -> "Magistratura mutaxassisliklari";
            case "h_university_form" -> "OTM shakllari";
            case "h_ownership" -> "OTM mulkchilik shakllari";
            case "h_structure_type" -> "Bo'lim turlari";
            case "h_employee_type" -> "Xodim turlari";
            case "h_teacher_status" -> "O'qituvchi holatlari";
            case "h_employment_staff" -> "Xodim shtat turlari";
            case "h_employment_form" -> "Bandlik shakllari";
            case "h_teacher_position_type" -> "O'qituvchi lavozimlari";
            case "h_qualification" -> "Malaka toifalari";
            case "h_teacher_success" -> "O'qituvchi yutuqlari turlari";
            case "h_academic_degree" -> "Ilmiy darajalar turlari";
            case "h_academic_rank" -> "Ilmiy unvonlar";
            case "h_student_status" -> "Talaba statusi turlari";
            case "h_student_success" -> "Talaba yutuqlari turlari";
            case "h_expel_reason" -> "Chetlatish sabablari";
            case "h_accommodation" -> "Talabalar yashash joylari turlari";
            case "h_doctoral_student_type" -> "Doktorant turlari";
            case "h_social_category" -> "Talabalarning ijtimoiy toifalari";
            case "h_education_type" -> "Ta'lim turlari";
            case "h_education_form" -> "Ta'lim shakllari";
            case "h_language" -> "Ta'lim tillari";
            case "h_marking_system" -> "Baholash tizimlari";
            case "h_grade_type" -> "Baho turlari";
            case "h_exam_type" -> "Imtihon turlari";
            case "h_course" -> "O'quv kurslari";
            case "h_semestr_type" -> "Semestr turlari";
            case "h_education_week_type" -> "Ta'lim hafta turlari";
            case "h_subject_block" -> "Fan bloklari";
            case "h_subject_type" -> "Fan turlari";
            case "h_training_type" -> "Mashg'ulot turlari";
            case "h_project_type" -> "Loyiha turlari";
            case "h_locality" -> "Joylashuvlar";
            case "h_project_currency" -> "Valyutalar";
            case "h_project_executor_type" -> "Loyiha ijrochi turlari";
            case "h_scientific_publication_type" -> "Ilmiy nashr turlari";
            case "h_methodical_publication_type" -> "Metodik nashr turlari";
            case "h_patient_type" -> "Bemor turlari";
            case "h_publication_database" -> "Nashr bazalari";
            case "h_payment_form" -> "To'lov turlari";
            case "h_stipend_rate" -> "Stipendiya turlari";
            case "h_auditorium_type" -> "Auditoriya turlari";
            case "h_device_type" -> "Qurilma turlari";
            default -> classifierName.replace("h_", "").replace("_", " ");
        };
    }

    // ==================== UniversityDepartmentType ====================

    public Optional<UniversityDepartmentType> findDepartmentTypeById(String code) {
        return universityDepartmentTypeRepository.findById(code);
    }

    @Cacheable(value = "classifierDepartmentType", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<UniversityDepartmentType> findAllDepartmentTypes() {
        return universityDepartmentTypeRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "classifierDepartmentType", key = "'all'")
    public UniversityDepartmentType saveDepartmentType(UniversityDepartmentType entity) {
        return universityDepartmentTypeRepository.save(entity);
    }

    @Transactional
    @CacheEvict(value = "classifierDepartmentType", key = "'all'")
    public void softDeleteDepartmentType(UniversityDepartmentType entity) {
        entity.setActive(false);
        universityDepartmentTypeRepository.save(entity);
    }

    /**
     * Create or restore (if soft-deleted) a UniversityDepartmentType.
     * Uses native query to bypass @Where(deleteTs IS NULL) filter.
     *
     * @param code     the code (primary key)
     * @param name     the name
     * @param nameRu   the Russian name
     * @param nameEn   the English name
     * @return the created or restored entity
     */
    @Transactional
    public UniversityDepartmentType createOrRestoreDepartmentType(String code, String name, String nameRu, String nameEn) {
        // Check if entity exists (including soft-deleted) using native query
        String checkSql = "SELECT COUNT(*) FROM hemishe_h_university_department_type WHERE code = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, code);

        if (count != null && count > 0) {
            // Entity exists - update it (restore if soft-deleted)
            String updateSql = """
                UPDATE hemishe_h_university_department_type
                SET name = ?, name_ru = ?, name_en = ?, active = true,
                    delete_ts = NULL, deleted_by = NULL, update_ts = ?
                WHERE code = ?
                """;
            jdbcTemplate.update(updateSql, name, nameRu, nameEn, LocalDateTime.now(), code);
        } else {
            // Create new entity
            String insertSql = """
                INSERT INTO hemishe_h_university_department_type (code, name, name_ru, name_en, active, create_ts, update_ts, version)
                VALUES (?, ?, ?, ?, true, ?, ?, 1)
                """;
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.update(insertSql, code, name, nameRu, nameEn, now, now);
        }

        // Return the entity (now visible since deleteTs is NULL)
        return universityDepartmentTypeRepository.findById(code)
                .orElseThrow(() -> new IllegalStateException("Failed to create/restore department type: " + code));
    }

    /**
     * Convert UniversityDepartmentType to CUBA-compatible map.
     *
     * <p><strong>OLD-HEMIS FIELD ORDER (100% compatible):</strong></p>
     * <ul>
     *   <li>_entityName, _instanceName, id - har doim</li>
     *   <li>nameRu, deleteTs - returnNulls=true bo'lganda</li>
     *   <li>code, name, active - har doim (null bo'lmasa)</li>
     *   <li>nameEn - returnNulls=true bo'lganda</li>
     *   <li>version - har doim</li>
     *   <li>deletedBy - returnNulls=true bo'lganda</li>
     * </ul>
     */
    public Map<String, Object> toDepartmentTypeMap(UniversityDepartmentType entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS exact field order
        map.put("_entityName", "hemishe_HUniversityDepartmentType");
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());

        // returnNulls=true bo'lganda nameRu
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);

        // returnNulls=true bo'lganda deleteTs
        putIfNotNull(map, "deleteTs", null, returnNulls);

        // Asosiy maydonlar (har doim, null bo'lmasa)
        putIfNotNull(map, "code", entity.getCode(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.isActive(), returnNulls);

        // returnNulls=true bo'lganda nameEn
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);

        // Version (har doim)
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        // returnNulls=true bo'lganda deletedBy
        putIfNotNull(map, "deletedBy", null, returnNulls);

        return map;
    }

    public void updateDepartmentTypeFromMap(UniversityDepartmentType entity, Map<String, Object> body) {
        if (body.containsKey("name")) {
            Object val = body.get("name");
            entity.setName(val != null ? val.toString() : null);
        }
        if (body.containsKey("active")) {
            Object val = body.get("active");
            if (val instanceof Boolean) {
                entity.setActive((Boolean) val);
            } else if (val != null) {
                entity.setActive(Boolean.parseBoolean(val.toString()));
            }
        }
        entity.setUpdatedAt(LocalDateTime.now());
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
