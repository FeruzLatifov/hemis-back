package uz.hemis.service.legacy.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import uz.hemis.service.legacy.CubaEntityMapHelper;
import uz.hemis.service.legacy.CubaNestedObjectLoader;
import uz.hemis.service.legacy.ReferenceDataLegacyService;
import uz.hemis.service.legacy.SoftDeleteRestoreLegacyService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Student Domain Legacy Service
 *
 * Batch 5 controllerlar uchun service layer.
 * CUBA Platform REST API backward compatibility saqlanadi.
 *
 * Entities:
 * - StudentCertificate (UUID PK)
 * - AdministrativeStudent2 (UUID PK)
 * - AdministrativeStudent3 (UUID PK)
 * - AdministrativeStudent4 (UUID PK)
 * - AdministrativeStudentSport (UUID PK)
 * - AdministrativeSportFacilities (UUID PK)
 * - Citizenship (String PK - code)
 * - Expel (UUID PK)
 * - StudentStatusType (String PK - code)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentEntityLegacyService {

    private final StudentCertificateRepository studentCertificateRepository;
    private final AdministrativeStudent2Repository administrativeStudent2Repository;
    private final AdministrativeStudent3Repository administrativeStudent3Repository;
    private final AdministrativeStudent4Repository administrativeStudent4Repository;
    private final AdministrativeStudentSportRepository administrativeStudentSportRepository;
    private final AdministrativeSportFacilitiesRepository administrativeSportFacilitiesRepository;
    private final CitizenshipRepository citizenshipRepository;
    private final ExpelRepository expelRepository;
    private final StudentStatusTypeRepository studentStatusTypeRepository;

    private final ReferenceDataLegacyService referenceDataService;
    private final SoftDeleteRestoreLegacyService softDeleteRestoreService;
    private final CubaNestedObjectLoader nestedObjectLoader;

    // =====================================================
    // StudentCertificate
    // =====================================================

    public Optional<StudentCertificate> findStudentCertificateById(UUID id) {
        return studentCertificateRepository.findById(id);
    }

    public Page<StudentCertificate> findAllStudentCertificate(Pageable pageable) {
        return studentCertificateRepository.findAll(pageable);
    }

    @Transactional
    public StudentCertificate saveStudentCertificate(StudentCertificate entity) {
        return studentCertificateRepository.save(entity);
    }

    @Transactional
    public void deleteStudentCertificate(StudentCertificate entity) {
        entity.setDeleteTs(LocalDateTime.now());
        studentCertificateRepository.save(entity);
    }

    public Map<String, Object> toStudentCertificateMap(StudentCertificate entity, Boolean returnNulls) {
        return toStudentCertificateMap(entity, returnNulls, null);
    }

    public Map<String, Object> toStudentCertificateMap(StudentCertificate entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("_entityName", "hemishe_EStudentCertificate");
        map.put("_instanceName", "com.company.hemishe.entity.EStudentCertificate-" + entity.getId() + " [detached]");

        // Reference fields: faqat view mode da qaytariladi
        boolean useNested = view != null && !view.isEmpty();
        if (useNested) {
            // OLD-HEMIS: university is full nested object in certificate view (with soato, universityType, etc.)
            if (entity.getUniversity() != null) {
                map.put("university", nestedObjectLoader.loadUniversityFull(entity.getUniversity()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", null);
            }
            if (entity.getStudent() != null) {
                Map<String, Object> studentMap = nestedObjectLoader.loadStudentForDiploma(entity.getStudent());
                if (studentMap != null) {
                    postProcessStudentForCertificate(studentMap);
                }
                map.put("student", studentMap);
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("student", null);
            }
            nestedObjectLoader.putNestedWithNames(map, "certificateType", entity.getCertificateType(),
                    "hemishe_h_certificate_type", "HCertificateType", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "certificateName", entity.getCertificateName(),
                    "hemishe_h_certificate_names", "HCertificateNames", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "certificateGrade", entity.getCertificateGrade(),
                    "hemishe_h_certificate_grades", "HCertificateGrades", returnNulls);
            nestedObjectLoader.putNestedWithNames(map, "certificateSubject", entity.getCertificateSubject(),
                    "hemishe_h_certificate_subjects", "HCertificateSubjects", returnNulls);
        }

        CubaEntityMapHelper.putIfNotNull(map, "issueDate", entity.getIssueDate(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "validDate", entity.getValidDate(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", entity.getActive(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        return map;
    }

    /**
     * Post-process student nested object for certificate view.
     * OLD-HEMIS certificate student format:
     * - ALL null reference fields present (academicMobileType=null, etc.)
     * - Classifiers with basic format only (NO nameRu/nameEn/active/code)
     * - Full university (with all fields, not minimal 5-field version)
     */
    @SuppressWarnings("unchecked")
    private void postProcessStudentForCertificate(Map<String, Object> student) {
        // 1. Add missing null reference fields that OLD-HEMIS always includes
        Object jsonNull = uz.hemis.common.JsonNull.INSTANCE;
        String[] nullFields = {
            "currentEducationYear", "academicMobileType", "transferCountry", "povertyLevel",
            "admissionType", "specialityOrdinatura", "academicReason", "livingStatus",
            "roommateType", "expelReason", "stipendRate", "transferType",
            "doctoralStudentType", "specialityDoctoral", "graduationYear", "studentSuccess"
        };
        for (String field : nullFields) {
            student.putIfAbsent(field, jsonNull);
        }
        // studentSuccess is an empty array in OLD-HEMIS
        if (student.get("studentSuccess") == jsonNull) {
            student.put("studentSuccess", new java.util.ArrayList<>());
        }

        // 2. Replace minimal university with full university (loadUniversityForTeacher format)
        Object uniObj = student.get("university");
        if (uniObj instanceof Map) {
            Map<String, Object> uniMap = (Map<String, Object>) uniObj;
            Object uniCode = uniMap.get("code");
            if (uniCode == null) uniCode = uniMap.get("id");
            if (uniCode != null) {
                Map<String, Object> fullUni = nestedObjectLoader.loadUniversityForTeacher(String.valueOf(uniCode));
                if (fullUni != null) {
                    student.put("university", fullUni);
                }
            }
        }

        // 3. Selectively strip classifier fields to match OLD-HEMIS student view format
        // Each classifier has specific fields; loadClassifierFull returns all, so strip extras per entity
        stripClassifierFieldsForCertStudent(student);

        // 4. Strip faculty extras — OLD-HEMIS cert student faculty only has basic department fields
        Object facultyObj = student.get("faculty");
        if (facultyObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fac = (Map<String, Object>) facultyObj;
            fac.remove("status");
            fac.remove("deparmentType");
            fac.remove("parent");
            fac.remove("university");
        }

        // 5. Strip name_ru from currentSoato — OLD-HEMIS cert student currentSoato doesn't have name_ru
        Object currentSoatoObj = student.get("currentSoato");
        if (currentSoatoObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cs = (Map<String, Object>) currentSoatoObj;
            cs.remove("name_ru");
            cs.remove("active");
        }

        // 6. Replace terrain/currentTerrain with proper objects including nested soato
        replaceTerrain(student, "terrain", false);
        replaceTerrain(student, "currentTerrain", true);
    }

    /**
     * Replace basic classifier terrain with a full terrain object including nested soato.
     */
    @SuppressWarnings("unchecked")
    private void replaceTerrain(Map<String, Object> student, String field, boolean includeNameRuInSoato) {
        Object terrainObj = student.get(field);
        if (terrainObj instanceof Map) {
            Map<String, Object> terrainMap = (Map<String, Object>) terrainObj;
            String terrainCode = (String) terrainMap.get("id");
            if (terrainCode == null) terrainCode = (String) terrainMap.get("code");
            if (terrainCode != null) {
                Map<String, Object> fullTerrain = nestedObjectLoader.loadTerrainWithSoato(terrainCode, includeNameRuInSoato);
                if (fullTerrain != null) {
                    student.put(field, fullTerrain);
                }
            }
        }
    }

    // Classifiers that keep nameRu and nameEn (in addition to code)
    private static final java.util.Set<String> CLASSIFIERS_KEEP_NAME_RU_EN = java.util.Set.of(
            "hemishe_HEducationType", "hemishe_HEducationYear");
    // Classifiers that keep active
    private static final java.util.Set<String> CLASSIFIERS_KEEP_ACTIVE = java.util.Set.of(
            "hemishe_HEducationType", "hemishe_HStudentSocialType");
    // All known student classifiers (strip extras from these; leave other entities alone)
    private static final java.util.Set<String> STUDENT_CLASSIFIER_ENTITIES = java.util.Set.of(
            "hemishe_HCountry", "hemishe_HEducationLanguage", "hemishe_HEducationForm",
            "hemishe_HCourse", "hemishe_HPaymentForm", "hemishe_HCitizenship",
            "hemishe_HNationality", "hemishe_HAccomodation", "hemishe_HStudentLivingStatus",
            "hemishe_HStudentRoomMateType", "hemishe_HExpel", "hemishe_HAcademicReason",
            "hemishe_HAcademicMobileType", "hemishe_HGrantType", "hemishe_HAdmissionType",
            "hemishe_HTransferType", "hemishe_HDoctoralStudentType", "hemishe_HStipendRate",
            "hemishe_HStudentType", "hemishe_HPovertyLevel", "hemishe_HGender",
            "hemishe_HStudentStatusType", "hemishe_HEducationType", "hemishe_HStudentSocialType",
            "hemishe_HEducationYear");

    /**
     * Selectively strip classifier extras to match OLD-HEMIS student view format.
     * All classifiers keep: code. Additional fields depend on entity type.
     */
    @SuppressWarnings("unchecked")
    private void stripClassifierFieldsForCertStudent(Map<String, Object> student) {
        for (Map.Entry<String, Object> entry : student.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Map) {
                Map<String, Object> nested = (Map<String, Object>) val;
                String entityName = (String) nested.get("_entityName");
                if (entityName != null && STUDENT_CLASSIFIER_ENTITIES.contains(entityName)) {
                    // All student classifiers keep code; selectively strip others
                    if (!CLASSIFIERS_KEEP_NAME_RU_EN.contains(entityName)) {
                        nested.remove("nameRu");
                        nested.remove("nameEn");
                    }
                    if (!CLASSIFIERS_KEEP_ACTIVE.contains(entityName)) {
                        nested.remove("active");
                    }
                }
            }
        }
    }

    public void updateStudentCertificateFromMap(StudentCertificate entity, Map<String, Object> data) {
        if (data.containsKey("university")) {
            entity.setUniversity(CubaEntityMapHelper.extractCodeOrId(data.get("university")));
        }
        if (data.containsKey("student")) {
            entity.setStudent(CubaEntityMapHelper.extractUuid(data.get("student")));
        }
        if (data.containsKey("certificateType")) {
            entity.setCertificateType(CubaEntityMapHelper.extractCodeOrId(data.get("certificateType")));
        }
        if (data.containsKey("certificateName")) {
            entity.setCertificateName(CubaEntityMapHelper.getStringValue(data.get("certificateName")));
        }
        if (data.containsKey("certificateGrade")) {
            entity.setCertificateGrade(CubaEntityMapHelper.getStringValue(data.get("certificateGrade")));
        }
        if (data.containsKey("certificateSubject")) {
            entity.setCertificateSubject(CubaEntityMapHelper.getStringValue(data.get("certificateSubject")));
        }
        if (data.containsKey("issueDate")) {
            entity.setIssueDate(CubaEntityMapHelper.parseLocalDate(data.get("issueDate")));
        }
        if (data.containsKey("validDate")) {
            entity.setValidDate(CubaEntityMapHelper.parseLocalDate(data.get("validDate")));
        }
        if (data.containsKey("serialNumber")) {
            entity.setSerialNumber(CubaEntityMapHelper.getStringValue(data.get("serialNumber")));
        }
        if (data.containsKey("active")) {
            entity.setActive(CubaEntityMapHelper.getBooleanValue(data.get("active")));
        }
    }

    // =====================================================
    // AdministrativeStudent2
    // =====================================================

    public Optional<AdministrativeStudent2> findAdministrativeStudent2ById(UUID id) {
        return administrativeStudent2Repository.findById(id);
    }

    public List<AdministrativeStudent2> findAllAdministrativeStudent2() {
        return administrativeStudent2Repository.findAll();
    }

    public Page<AdministrativeStudent2> findAllAdministrativeStudent2(Pageable pageable) {
        return administrativeStudent2Repository.findAll(pageable);
    }

    @Transactional
    public AdministrativeStudent2 saveAdministrativeStudent2(AdministrativeStudent2 entity) {
        return administrativeStudent2Repository.save(entity);
    }

    @Transactional
    public void deleteAdministrativeStudent2(AdministrativeStudent2 entity) {
        administrativeStudent2Repository.delete(entity);
    }

    public Map<String, Object> toAdministrativeStudent2Map(AdministrativeStudent2 entity, Boolean returnNulls) {
        return toAdministrativeStudent2Map(entity, returnNulls, null);
    }

    /**
     * OLD-HEMIS view — {@code rIAdministrativeStudent2-view} (views.xml:701-706):
     * 4 refs (university, educationYear, country, educationType) {@code _minimal}.
     */
    public Map<String, Object> toAdministrativeStudent2Map(AdministrativeStudent2 entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", "hemishe_RIAdministrativeStudent2");
        map.put("_instanceName", buildAdministrativeStudent2InstanceName(entity));
        map.put("id", entity.getId());

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
            nestedObjectLoader.putNestedWithNames(map, "educationType", entity.getEducationType(),
                    "hemishe_h_education_type", "HEducationType", returnNulls);
        }

        CubaEntityMapHelper.putIfNotNull(map, "exchangeDocument", entity.getExchangeDocument(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "exchangeType", entity.getExchangeType(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "studentFullname", entity.getStudentFullname(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", 1, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "exchangeUniversityName", entity.getExchangeUniversityName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        return map;
    }

    private String buildAdministrativeStudent2InstanceName(AdministrativeStudent2 entity) {
        return "com.company.hemishe.entity.RIAdministrativeStudent2-" + entity.getId() + " [detached]";
    }

    public void updateAdministrativeStudent2FromMap(AdministrativeStudent2 entity, Map<String, Object> data) {
        if (data.containsKey("university")) {
            entity.setUniversity(CubaEntityMapHelper.extractCodeOrId(data.get("university")));
        }
        if (data.containsKey("educationYear")) {
            entity.setEducationYear(CubaEntityMapHelper.extractCodeOrId(data.get("educationYear")));
        }
        if (data.containsKey("country")) {
            entity.setCountry(CubaEntityMapHelper.extractCodeOrId(data.get("country")));
        }
        if (data.containsKey("educationType")) {
            entity.setEducationType(CubaEntityMapHelper.extractCodeOrId(data.get("educationType")));
        }
        if (data.containsKey("exchangeDocument")) {
            entity.setExchangeDocument(CubaEntityMapHelper.getStringValue(data.get("exchangeDocument")));
        }
        if (data.containsKey("exchangeType")) {
            entity.setExchangeType(CubaEntityMapHelper.getStringValue(data.get("exchangeType")));
        }
        if (data.containsKey("studentFullname")) {
            entity.setStudentFullname(CubaEntityMapHelper.getStringValue(data.get("studentFullname")));
        }
        if (data.containsKey("exchangeUniversityName")) {
            entity.setExchangeUniversityName(CubaEntityMapHelper.getStringValue(data.get("exchangeUniversityName")));
        }
        if (data.containsKey("specialityName")) {
            entity.setSpecialityName(CubaEntityMapHelper.getStringValue(data.get("specialityName")));
        }
        if (data.containsKey("specialityCode")) {
            entity.setSpecialityCode(CubaEntityMapHelper.getStringValue(data.get("specialityCode")));
        }
    }

    // =====================================================
    // AdministrativeStudent3
    // =====================================================

    public Optional<AdministrativeStudent3> findAdministrativeStudent3ById(UUID id) {
        return administrativeStudent3Repository.findById(id);
    }

    public Page<AdministrativeStudent3> findAllAdministrativeStudent3(Pageable pageable) {
        return administrativeStudent3Repository.findAll(pageable);
    }

    @Transactional
    public AdministrativeStudent3 saveAdministrativeStudent3(AdministrativeStudent3 entity) {
        return administrativeStudent3Repository.save(entity);
    }

    @Transactional
    public void deleteAdministrativeStudent3(AdministrativeStudent3 entity) {
        administrativeStudent3Repository.delete(entity);
    }

    public Map<String, Object> toAdministrativeStudent3Map(AdministrativeStudent3 entity, Boolean returnNulls) {
        return toAdministrativeStudent3Map(entity, returnNulls, null);
    }

    public Map<String, Object> toAdministrativeStudent3Map(AdministrativeStudent3 entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", "hemishe_RIAdministrativeStudent3");
        map.put("_instanceName", buildAdministrativeStudent3InstanceName(entity));
        map.put("id", entity.getId());

        boolean useNested = view != null && !view.isEmpty();

        // OLD-HEMIS: reference fieldlar faqat view mode da qaytariladi
        // student nested object — _minimal view + specialityBachelor, specialityMaster, paymentForm
        if (useNested && entity.getStudent() != null) {
            Map<String, Object> studentObj = nestedObjectLoader.loadStudentMinimalWithSpeciality(entity.getStudent());
            map.put("student", studentObj != null ? studentObj : entity.getStudent().toString());
        }

        // university nested object (FK stores code as varchar, not UUID)
        if (useNested && entity.getUniversity() != null) {
            Map<String, Object> uniObj = nestedObjectLoader.loadUniversity(entity.getUniversity());
            map.put("university", uniObj != null ? uniObj : entity.getUniversity());
        }

        // educationYear nested object (FK stores code as varchar)
        if (useNested && entity.getEducationYear() != null) {
            Map<String, Object> eyObj = nestedObjectLoader.loadClassifier("hemishe_h_education_year", "HEducationYear", entity.getEducationYear());
            map.put("educationYear", eyObj != null ? eyObj : entity.getEducationYear());
        }

        CubaEntityMapHelper.putIfNotNull(map, "mastersUniversityName", entity.getMastersUniversityName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "company", entity.getCompany(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "position", entity.getPosition(), returnNulls);
        return map;
    }

    private String buildAdministrativeStudent3InstanceName(AdministrativeStudent3 entity) {
        return "com.company.hemishe.entity.RIAdministrativeStudent3-" + entity.getId() + " [detached]";
    }

    public void updateAdministrativeStudent3FromMap(AdministrativeStudent3 entity, Map<String, Object> data) {
        if (data.containsKey("university")) {
            entity.setUniversity(CubaEntityMapHelper.extractCodeOrId(data.get("university")));
        }
        if (data.containsKey("educationYear")) {
            entity.setEducationYear(CubaEntityMapHelper.extractCodeOrId(data.get("educationYear")));
        }
        if (data.containsKey("student")) {
            entity.setStudent(CubaEntityMapHelper.extractUuid(data.get("student")));
        }
        if (data.containsKey("educationType")) {
            entity.setEducationType(CubaEntityMapHelper.extractCodeOrId(data.get("educationType")));
        }
        if (data.containsKey("company")) {
            entity.setCompany(CubaEntityMapHelper.getStringValue(data.get("company")));
        }
        if (data.containsKey("position")) {
            entity.setPosition(CubaEntityMapHelper.getStringValue(data.get("position")));
        }
        if (data.containsKey("mastersUniversityName")) {
            entity.setMastersUniversityName(CubaEntityMapHelper.getStringValue(data.get("mastersUniversityName")));
        }
    }

    // =====================================================
    // AdministrativeStudent4
    // =====================================================

    public Optional<AdministrativeStudent4> findAdministrativeStudent4ById(UUID id) {
        return administrativeStudent4Repository.findById(id);
    }

    public Page<AdministrativeStudent4> findAllAdministrativeStudent4(Pageable pageable) {
        return administrativeStudent4Repository.findAll(pageable);
    }

    @Transactional
    public AdministrativeStudent4 saveAdministrativeStudent4(AdministrativeStudent4 entity) {
        return administrativeStudent4Repository.save(entity);
    }

    @Transactional
    public void deleteAdministrativeStudent4(AdministrativeStudent4 entity) {
        administrativeStudent4Repository.delete(entity);
    }

    public Map<String, Object> toAdministrativeStudent4Map(AdministrativeStudent4 entity, Boolean returnNulls) {
        return toAdministrativeStudent4Map(entity, returnNulls, null);
    }

    /**
     * OLD-HEMIS view — {@code rIAdministrativeStudent4-view} (views.xml:716-723):
     * 4 refs (university, educationYear, country, student/_minimal with fullname).
     * AS4 da FK lar UUID, string code emas — minimal reference {@code {_entityName, id}}.
     */
    public Map<String, Object> toAdministrativeStudent4Map(AdministrativeStudent4 entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", "hemishe_RIAdministrativeStudent4");
        map.put("_instanceName", buildAdministrativeStudent4InstanceName(entity));
        map.put("id", entity.getId());

        boolean useNested = view != null && !view.isEmpty() && !"_local".equals(view);
        if (useNested) {
            putUuidRef(map, "university", entity.getUniversity(), "hemishe_EUniversity", returnNulls);
            putUuidRef(map, "educationYear", entity.getEducationYear(), "hemishe_HEducationYear", returnNulls);
            putUuidRef(map, "country", entity.getCountry(), "hemishe_HCountry", returnNulls);
            putUuidRef(map, "student", entity.getStudent(), "hemishe_EStudent", returnNulls);
        }

        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "olimpiadaType", entity.getOlimpiadaType(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "olimpiadaPlace", entity.getOlimpiadaPlace(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "olimpiadaName", entity.getOlimpiadaName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "olimpiadaSectionName", entity.getOlimpiadaSectionName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "olimpiadaPlaceDate", entity.getOlimpiadaPlaceDate(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "olimpiadaSubject", entity.getOlimpiadaSubject(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "takenPosition", entity.getTakenPosition(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "diplomaSerial", entity.getDiplomaSerial(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "diplomaNumber", entity.getDiplomaNumber(), returnNulls);
        return map;
    }

    /**
     * CUBA {@code _minimal} view pattern for UUID-based FK — {@code {_entityName, id, _instanceName}}.
     *
     * <p>{@code _instanceName} CUBA detached marker formatida qaytariladi (real entity ni load
     * qilmasdan) — clientlar ID orqali to'liq obyektni alohida so'rovi bilan olishlari mumkin.</p>
     */
    private void putUuidRef(Map<String, Object> map, String key, UUID id, String entityName, Boolean returnNulls) {
        if (id != null) {
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("_entityName", entityName);
            ref.put("id", id.toString());
            ref.put("_instanceName", "com.company.hemishe.entity." + entityName.replace("hemishe_", "") + "-" + id + " [detached]");
            map.put(key, ref);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, uz.hemis.common.JsonNull.INSTANCE);
        }
    }

    private String buildAdministrativeStudent4InstanceName(AdministrativeStudent4 entity) {
        return "com.company.hemishe.entity.RIAdministrativeStudent4-" + entity.getId() + " [detached]";
    }

    public void updateAdministrativeStudent4FromMap(AdministrativeStudent4 entity, Map<String, Object> data) {
        if (data.containsKey("university")) {
            entity.setUniversity(CubaEntityMapHelper.extractUuid(data.get("university")));
        }
        if (data.containsKey("educationYear")) {
            entity.setEducationYear(CubaEntityMapHelper.extractUuid(data.get("educationYear")));
        }
        if (data.containsKey("country")) {
            entity.setCountry(CubaEntityMapHelper.extractUuid(data.get("country")));
        }
        if (data.containsKey("student")) {
            entity.setStudent(CubaEntityMapHelper.extractUuid(data.get("student")));
        }
        if (data.containsKey("olimpiadaType")) {
            entity.setOlimpiadaType(CubaEntityMapHelper.getStringValue(data.get("olimpiadaType")));
        }
        if (data.containsKey("olimpiadaPlace")) {
            entity.setOlimpiadaPlace(CubaEntityMapHelper.getStringValue(data.get("olimpiadaPlace")));
        }
        if (data.containsKey("olimpiadaName")) {
            entity.setOlimpiadaName(CubaEntityMapHelper.getStringValue(data.get("olimpiadaName")));
        }
        if (data.containsKey("olimpiadaSectionName")) {
            entity.setOlimpiadaSectionName(CubaEntityMapHelper.getStringValue(data.get("olimpiadaSectionName")));
        }
        if (data.containsKey("olimpiadaPlaceDate")) {
            entity.setOlimpiadaPlaceDate(CubaEntityMapHelper.getStringValue(data.get("olimpiadaPlaceDate")));
        }
        if (data.containsKey("olimpiadaSubject")) {
            entity.setOlimpiadaSubject(CubaEntityMapHelper.getStringValue(data.get("olimpiadaSubject")));
        }
        if (data.containsKey("takenPosition")) {
            entity.setTakenPosition(CubaEntityMapHelper.getStringValue(data.get("takenPosition")));
        }
        if (data.containsKey("diplomaSerial")) {
            entity.setDiplomaSerial(CubaEntityMapHelper.getStringValue(data.get("diplomaSerial")));
        }
        if (data.containsKey("diplomaNumber")) {
            entity.setDiplomaNumber(CubaEntityMapHelper.getStringValue(data.get("diplomaNumber")));
        }
    }

    // =====================================================
    // AdministrativeStudentSport
    // =====================================================

    public Optional<AdministrativeStudentSport> findAdministrativeStudentSportById(UUID id) {
        return administrativeStudentSportRepository.findById(id);
    }

    public Page<AdministrativeStudentSport> findAllAdministrativeStudentSport(Pageable pageable) {
        return administrativeStudentSportRepository.findAll(pageable);
    }

    @Transactional
    public AdministrativeStudentSport saveAdministrativeStudentSport(AdministrativeStudentSport entity) {
        return administrativeStudentSportRepository.save(entity);
    }

    @Transactional
    public void deleteAdministrativeStudentSport(AdministrativeStudentSport entity) {
        administrativeStudentSportRepository.delete(entity);
    }

    public Map<String, Object> toAdministrativeStudentSportMap(AdministrativeStudentSport entity, Boolean returnNulls) {
        return toAdministrativeStudentSportMap(entity, returnNulls, null);
    }

    /**
     * OLD-HEMIS view — {@code rIAdministrativeStudentSport-view} (views.xml:724-729):
     * 4 refs (university, educationYear, student, sportType) {@code _minimal}.
     */
    public Map<String, Object> toAdministrativeStudentSportMap(AdministrativeStudentSport entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", "hemishe_RIAdministrativeStudentSport");
        map.put("_instanceName", buildAdministrativeStudentSportInstanceName(entity));
        map.put("id", entity.getId());

        boolean useNested = view != null && !view.isEmpty() && !"_local".equals(view);
        if (useNested) {
            putUuidRef(map, "university", entity.getUniversity(), "hemishe_EUniversity", returnNulls);
            putUuidRef(map, "educationYear", entity.getEducationYear(), "hemishe_HEducationYear", returnNulls);
            putUuidRef(map, "student", entity.getStudent(), "hemishe_EStudent", returnNulls);
            putUuidRef(map, "sportType", entity.getSportType(), "hemishe_HSportType", returnNulls);
        }

        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "sportDate", entity.getSportDate(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "sportTypeRank", entity.getSportTypeRank(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "sportTypeRankDocument", entity.getSportTypeRankDocument(), returnNulls);
        return map;
    }

    private String buildAdministrativeStudentSportInstanceName(AdministrativeStudentSport entity) {
        return "com.company.hemishe.entity.RIAdministrativeStudentSport-" + entity.getId() + " [detached]";
    }

    public void updateAdministrativeStudentSportFromMap(AdministrativeStudentSport entity, Map<String, Object> data) {
        if (data.containsKey("university")) {
            entity.setUniversity(CubaEntityMapHelper.extractUuid(data.get("university")));
        }
        if (data.containsKey("educationYear")) {
            entity.setEducationYear(CubaEntityMapHelper.extractUuid(data.get("educationYear")));
        }
        if (data.containsKey("student")) {
            entity.setStudent(CubaEntityMapHelper.extractUuid(data.get("student")));
        }
        if (data.containsKey("sportType")) {
            entity.setSportType(CubaEntityMapHelper.extractUuid(data.get("sportType")));
        }
        if (data.containsKey("sportDate")) {
            entity.setSportDate(CubaEntityMapHelper.parseLocalDate(data.get("sportDate")));
        }
        if (data.containsKey("sportTypeRank")) {
            entity.setSportTypeRank(CubaEntityMapHelper.getStringValue(data.get("sportTypeRank")));
        }
        if (data.containsKey("sportTypeRankDocument")) {
            entity.setSportTypeRankDocument(CubaEntityMapHelper.getStringValue(data.get("sportTypeRankDocument")));
        }
    }

    // =====================================================
    // AdministrativeSportFacilities
    // =====================================================

    public Optional<AdministrativeSportFacilities> findAdministrativeSportFacilitiesById(UUID id) {
        return administrativeSportFacilitiesRepository.findById(id);
    }

    public Page<AdministrativeSportFacilities> findAllAdministrativeSportFacilities(Pageable pageable) {
        return administrativeSportFacilitiesRepository.findAll(pageable);
    }

    @Transactional
    public AdministrativeSportFacilities saveAdministrativeSportFacilities(AdministrativeSportFacilities entity) {
        return administrativeSportFacilitiesRepository.save(entity);
    }

    @Transactional
    public void deleteAdministrativeSportFacilities(AdministrativeSportFacilities entity) {
        administrativeSportFacilitiesRepository.delete(entity);
    }

    public Map<String, Object> toAdministrativeSportFacilitiesMap(AdministrativeSportFacilities entity, Boolean returnNulls) {
        return toAdministrativeSportFacilitiesMap(entity, returnNulls, null);
    }

    /**
     * OLD-HEMIS view — {@code rIAdministrativeSportFacilities-view} (views.xml:730-733):
     * 2 refs (university, educationYear) {@code _minimal}.
     */
    public Map<String, Object> toAdministrativeSportFacilitiesMap(AdministrativeSportFacilities entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", "hemishe_RIAdministrativeSportFacilities");
        map.put("_instanceName", buildAdministrativeSportFacilitiesInstanceName(entity));
        map.put("id", entity.getId());
        CubaEntityMapHelper.putIfNotNull(map, "square", entity.getSquare(), returnNulls);

        boolean useNested = view != null && !view.isEmpty() && !"_local".equals(view);
        if (useNested) {
            if (entity.getUniversity() != null) {
                map.put("university", nestedObjectLoader.loadUniversity(entity.getUniversity()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", uz.hemis.common.JsonNull.INSTANCE);
            }
            nestedObjectLoader.putNestedWithNames(map, "educationYear", entity.getEducationYear(),
                    "hemishe_h_education_year", "HEducationYear", returnNulls);
        }

        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        return map;
    }

    private String buildAdministrativeSportFacilitiesInstanceName(AdministrativeSportFacilities entity) {
        return "com.company.hemishe.entity.RIAdministrativeSportFacilities-" + entity.getId() + " [detached]";
    }

    private Map<String, Object> buildUniversityObject(String code) {
        Map<String, String> universityData = referenceDataService.getUniversityData(code);
        Map<String, Object> universityMap = new LinkedHashMap<>();
        universityMap.put("_entityName", "hemishe_EUniversity");
        universityMap.put("_instanceName", code + "-" + universityData.get("name"));
        universityMap.put("id", code);
        universityMap.put("code", code);
        universityMap.put("name", universityData.get("name"));
        return universityMap;
    }

    private Map<String, Object> buildEducationYearObject(String code) {
        Map<String, String> yearData = referenceDataService.getEducationYearData(code);
        Map<String, Object> yearMap = new LinkedHashMap<>();
        yearMap.put("_entityName", "hemishe_HEducationYear");
        yearMap.put("_instanceName", yearData.get("name"));
        yearMap.put("id", code);
        yearMap.put("name", yearData.get("name"));
        return yearMap;
    }

    public void updateAdministrativeSportFacilitiesFromMap(AdministrativeSportFacilities entity, Map<String, Object> data) {
        if (data.containsKey("university")) {
            entity.setUniversity(CubaEntityMapHelper.extractCodeOrId(data.get("university")));
        }
        if (data.containsKey("educationYear")) {
            entity.setEducationYear(CubaEntityMapHelper.extractCodeOrId(data.get("educationYear")));
        }
        if (data.containsKey("square")) {
            Object val = data.get("square");
            if (val != null) {
                entity.setSquare(Double.parseDouble(val.toString()));
            } else {
                entity.setSquare(null);
            }
        }
    }

    // =====================================================
    // Citizenship (String PK - code)
    // =====================================================

    public Optional<Citizenship> findCitizenshipById(String code) {
        return citizenshipRepository.findById(code);
    }

    public List<Citizenship> findAllCitizenship() {
        return citizenshipRepository.findAll();
    }

    public Page<Citizenship> findAllCitizenship(Pageable pageable) {
        return citizenshipRepository.findAll(pageable);
    }

    @Transactional
    public Citizenship saveCitizenship(Citizenship entity) {
        return citizenshipRepository.save(entity);
    }

    @Transactional
    public void deleteCitizenship(Citizenship entity) {
        // Classifier: deactivation (isActive=false) instead of soft-delete
        entity.setActive(false);
        citizenshipRepository.save(entity);
    }

    public boolean hasSoftDeletedCitizenship(String code) {
        // Classifier: deactivated == "soft-deleted"
        return citizenshipRepository.findById(code).map(c -> !c.isActive()).orElse(false);
    }

    @Transactional
    public void restoreSoftDeletedCitizenship(String code) {
        citizenshipRepository.findById(code).ifPresent(c -> {
            c.setActive(true);
            citizenshipRepository.save(c);
        });
    }

    public Map<String, Object> toCitizenshipMap(Citizenship entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        String instanceName = entity.getCode() + " " + (entity.getName() != null ? entity.getName() : "");
        map.put("_entityName", "hemishe_HCitizenship");
        map.put("_instanceName", instanceName);
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        CubaEntityMapHelper.putIfNotNull(map, "name", entity.getName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", entity.isActive(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        return map;
    }

    public Map<String, Object> toCitizenshipMinimalMap(Citizenship entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        String instanceName = entity.getCode() + " " + (entity.getName() != null ? entity.getName() : "");
        map.put("_entityName", "hemishe_HCitizenship");
        map.put("_instanceName", instanceName);
        map.put("id", entity.getCode());
        return map;
    }

    public void updateCitizenshipFromMap(Citizenship entity, Map<String, Object> data) {
        if (data.containsKey("name")) {
            entity.setName(CubaEntityMapHelper.getStringValue(data.get("name")));
        }
        if (data.containsKey("nameEn")) {
            entity.setNameEn(CubaEntityMapHelper.getStringValue(data.get("nameEn")));
        }
        if (data.containsKey("nameRu")) {
            entity.setNameRu(CubaEntityMapHelper.getStringValue(data.get("nameRu")));
        }
        if (data.containsKey("active")) {
            Object v = data.get("active");
            if (v instanceof Boolean b) {
                entity.setActive(b);
            } else if (v != null) {
                entity.setActive(Boolean.parseBoolean(v.toString()));
            }
        }
    }

    // =====================================================
    // Expel
    // =====================================================

    public Optional<Expel> findExpelById(UUID id) {
        return expelRepository.findById(id);
    }

    public List<Expel> findAllExpel() {
        return expelRepository.findAll();
    }

    @Transactional
    public Expel saveExpel(Expel entity) {
        return expelRepository.save(entity);
    }

    @Transactional
    public void deleteExpel(Expel entity) {
        entity.setDeleteTs(LocalDateTime.now());
        expelRepository.save(entity);
    }

    public Map<String, Object> toExpelMap(Expel entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", "hemishe_RExpel");
        map.put("_instanceName", "com.company.hemishe.entity.RExpel-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());
        CubaEntityMapHelper.putIfNotNull(map, "universityCode", entity.getUniversityCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "universityName", entity.getUniversityName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "facultyCode", entity.getFacultyCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "facultyName", entity.getFacultyName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "educationTypeCode", entity.getEducationTypeCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "educationTypeName", entity.getEducationTypeName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "educationYearCode", entity.getEducationYearCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "educationYearName", entity.getEducationYearName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "semesterTypeCode", entity.getSemesterTypeCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "semesterTypeName", entity.getSemesterTypeName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "courseCode", entity.getCourseCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "courseName", entity.getCourseName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateDate", entity.getUpdateDate(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "expelReasonCode", entity.getExpelReasonCode(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "expelReasonName", entity.getExpelReasonName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "expelCount", entity.getExpelCount(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        return map;
    }

    public void updateExpelFromMap(Expel entity, Map<String, Object> data) {
        if (data.containsKey("universityCode")) {
            entity.setUniversityCode(CubaEntityMapHelper.getStringValue(data.get("universityCode")));
        }
        if (data.containsKey("universityName")) {
            entity.setUniversityName(CubaEntityMapHelper.getStringValue(data.get("universityName")));
        }
        if (data.containsKey("facultyCode")) {
            entity.setFacultyCode(CubaEntityMapHelper.getStringValue(data.get("facultyCode")));
        }
        if (data.containsKey("facultyName")) {
            entity.setFacultyName(CubaEntityMapHelper.getStringValue(data.get("facultyName")));
        }
        if (data.containsKey("educationTypeCode")) {
            entity.setEducationTypeCode(CubaEntityMapHelper.getStringValue(data.get("educationTypeCode")));
        }
        if (data.containsKey("educationTypeName")) {
            entity.setEducationTypeName(CubaEntityMapHelper.getStringValue(data.get("educationTypeName")));
        }
        if (data.containsKey("educationYearCode")) {
            entity.setEducationYearCode(CubaEntityMapHelper.getStringValue(data.get("educationYearCode")));
        }
        if (data.containsKey("educationYearName")) {
            entity.setEducationYearName(CubaEntityMapHelper.getStringValue(data.get("educationYearName")));
        }
        if (data.containsKey("semesterTypeCode")) {
            entity.setSemesterTypeCode(CubaEntityMapHelper.getStringValue(data.get("semesterTypeCode")));
        }
        if (data.containsKey("semesterTypeName")) {
            entity.setSemesterTypeName(CubaEntityMapHelper.getStringValue(data.get("semesterTypeName")));
        }
        if (data.containsKey("courseCode")) {
            entity.setCourseCode(CubaEntityMapHelper.getStringValue(data.get("courseCode")));
        }
        if (data.containsKey("courseName")) {
            entity.setCourseName(CubaEntityMapHelper.getStringValue(data.get("courseName")));
        }
        if (data.containsKey("updateDate")) {
            entity.setUpdateDate(CubaEntityMapHelper.parseLocalDate(data.get("updateDate")));
        }
        if (data.containsKey("expelReasonCode")) {
            entity.setExpelReasonCode(CubaEntityMapHelper.getStringValue(data.get("expelReasonCode")));
        }
        if (data.containsKey("expelReasonName")) {
            entity.setExpelReasonName(CubaEntityMapHelper.getStringValue(data.get("expelReasonName")));
        }
        if (data.containsKey("expelCount")) {
            entity.setExpelCount(CubaEntityMapHelper.getIntegerValue(data.get("expelCount")));
        }
    }

    // =====================================================
    // StudentStatusType (String PK - code)
    // =====================================================

    public Optional<StudentStatusType> findStudentStatusTypeById(String code) {
        return studentStatusTypeRepository.findById(code);
    }

    public List<StudentStatusType> findAllStudentStatusType() {
        return studentStatusTypeRepository.findAll();
    }

    public Page<StudentStatusType> findAllStudentStatusType(Pageable pageable) {
        return studentStatusTypeRepository.findAll(pageable);
    }

    @Transactional
    public StudentStatusType saveStudentStatusType(StudentStatusType entity) {
        return studentStatusTypeRepository.save(entity);
    }

    @Transactional
    public void deleteStudentStatusType(StudentStatusType entity) {
        entity.setActive(false);
        studentStatusTypeRepository.save(entity);
    }

    public boolean hasSoftDeletedStudentStatusType(String code) {
        return softDeleteRestoreService.hasSoftDeletedRecord("hemishe_h_student_status_type", code);
    }

    @Transactional
    public void restoreSoftDeletedStudentStatusType(String code) {
        softDeleteRestoreService.restoreSoftDeletedRecord("hemishe_h_student_status_type", code);
    }

    public Map<String, Object> toStudentStatusTypeMap(StudentStatusType entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        String instanceName = entity.getCode() + " " + (entity.getName() != null ? entity.getName() : "");
        map.put("_entityName", "hemishe_HStudentStatusType");
        map.put("_instanceName", instanceName);
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        CubaEntityMapHelper.putIfNotNull(map, "name", entity.getName(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", entity.isActive(), returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        return map;
    }

    public Map<String, Object> toStudentStatusTypeMinimalMap(StudentStatusType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        String instanceName = entity.getCode() + " " + (entity.getName() != null ? entity.getName() : "");
        map.put("_entityName", "hemishe_HStudentStatusType");
        map.put("_instanceName", instanceName);
        map.put("id", entity.getCode());
        return map;
    }

    public void updateStudentStatusTypeFromMap(StudentStatusType entity, Map<String, Object> data) {
        if (data.containsKey("name")) {
            entity.setName(CubaEntityMapHelper.getStringValue(data.get("name")));
        }
        if (data.containsKey("nameEn")) {
            entity.setNameEn(CubaEntityMapHelper.getStringValue(data.get("nameEn")));
        }
        if (data.containsKey("nameRu")) {
            entity.setNameRu(CubaEntityMapHelper.getStringValue(data.get("nameRu")));
        }
        if (data.containsKey("active")) {
            entity.setActive(CubaEntityMapHelper.getBooleanValue(data.get("active")));
        }
    }
}
