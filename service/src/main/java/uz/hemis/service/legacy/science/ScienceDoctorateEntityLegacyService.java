package uz.hemis.service.legacy.science;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Science + Doctorate Domain Entity Legacy Service
 *
 * <p>Batch 7 controllerlari uchun CRUD operatsiyalari:</p>
 * <ul>
 *   <li>PublicationProperty (UUID PK) - Ilmiy ishlanmalar</li>
 *   <li>PublicationScientific (UUID PK) - Ilmiy nashrlar</li>
 *   <li>DissertationDefense (UUID PK) - Dissertatsiya himoyasi</li>
 *   <li>DoctoralStudent (UUID PK) - Doktorantlar</li>
 *   <li>DoctoralStudentStatus (String code PK) - Doktorant statuslari</li>
 *   <li>DoctoralStudentType (String code PK) - Doktorant turlari</li>
 *   <li>PublicationLocality (String code PK) - Nashr hududlari</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScienceDoctorateEntityLegacyService {

    private final PublicationPropertyRepository publicationPropertyRepository;
    private final PublicationScientificRepository publicationScientificRepository;
    private final DissertationDefenseRepository dissertationDefenseRepository;
    private final DoctoralStudentRepository doctoralStudentRepository;
    private final DoctoralStudentStatusRepository doctoralStudentStatusRepository;
    private final DoctoralStudentTypeRepository doctoralStudentTypeRepository;
    private final PublicationLocalityRepository publicationLocalityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // =====================================================
    // PublicationProperty (UUID PK)
    // =====================================================

    private static final String PUBLICATION_PROPERTY_ENTITY = "hemishe_EPublicationProperty";

    public Optional<PublicationProperty> findPublicationPropertyById(UUID id) {
        return publicationPropertyRepository.findById(id);
    }

    public List<PublicationProperty> findAllPublicationProperty() {
        return publicationPropertyRepository.findAll();
    }

    public Page<PublicationProperty> findAllPublicationProperty(Pageable pageable) {
        return publicationPropertyRepository.findAll(pageable);
    }

    @Transactional
    public PublicationProperty savePublicationProperty(PublicationProperty entity) {
        return publicationPropertyRepository.save(entity);
    }

    @Transactional
    public void deletePublicationProperty(PublicationProperty entity) {
        entity.setDeleteTs(LocalDateTime.now());
        publicationPropertyRepository.save(entity);
    }

    public Map<String, Object> toPublicationPropertyMap(PublicationProperty entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PUBLICATION_PROPERTY_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : "");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "numbers", entity.getNumbers(), returnNulls);
        putIfNotNull(map, "propertyDate", entity.getPropertyDate(), returnNulls);
        putIfNotNull(map, "authorCounts", entity.getAuthorCounts(), returnNulls);
        putIfNotNull(map, "parameter", entity.getParameter(), returnNulls);
        map.put("active", entity.getActive());
        putIfNotNull(map, "isChecked", entity.getIsChecked(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "authors", entity.getAuthors(), returnNulls);
        return map;
    }

    public Map<String, Object> toPublicationPropertyMinimalMap(PublicationProperty entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PUBLICATION_PROPERTY_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : "");
        map.put("id", entity.getId().toString());
        return map;
    }

    public void updatePublicationPropertyFromMap(PublicationProperty entity, Map<String, Object> map) {
        if (map.containsKey("uId") || map.containsKey("u_id")) {
            entity.setUId(extractInteger(map.containsKey("uId") ? map.get("uId") : map.get("u_id")));
        }
        if (map.containsKey("name")) entity.setName(extractString(map.get("name")));
        if (map.containsKey("numbers")) entity.setNumbers(extractString(map.get("numbers")));
        if (map.containsKey("authors")) entity.setAuthors(extractString(map.get("authors")));
        if (map.containsKey("authorCounts") || map.containsKey("author_counts")) {
            entity.setAuthorCounts(extractInteger(map.containsKey("authorCounts") ? map.get("authorCounts") : map.get("author_counts")));
        }
        if (map.containsKey("parameter")) entity.setParameter(extractString(map.get("parameter")));
        if (map.containsKey("propertyDate") || map.containsKey("property_date")) {
            entity.setPropertyDate(extractLocalDate(map.containsKey("propertyDate") ? map.get("propertyDate") : map.get("property_date")));
        }
        if (map.containsKey("filename")) entity.setFilename(extractString(map.get("filename")));
        if (map.containsKey("position")) entity.setPosition(extractInteger(map.get("position")));
        if (map.containsKey("active")) entity.setActive(extractBoolean(map.get("active")));
        if (map.containsKey("translations")) entity.setTranslations(extractString(map.get("translations")));
        if (map.containsKey("isChecked") || map.containsKey("is_checked")) {
            entity.setIsChecked(extractBoolean(map.containsKey("isChecked") ? map.get("isChecked") : map.get("is_checked")));
        }
        if (map.containsKey("isCheckedDate") || map.containsKey("is_checked_date")) {
            entity.setIsCheckedDate(extractLocalDateTime(map.containsKey("isCheckedDate") ? map.get("isCheckedDate") : map.get("is_checked_date")));
        }
        // FK fields
        if (map.containsKey("university") || map.containsKey("_university")) {
            entity.setUniversity(extractString(map.containsKey("university") ? map.get("university") : map.get("_university")));
        }
        if (map.containsKey("patentType") || map.containsKey("_patent_type")) {
            entity.setPatentType(extractString(map.containsKey("patentType") ? map.get("patentType") : map.get("_patent_type")));
        }
        if (map.containsKey("publicationDatabase") || map.containsKey("_publication_database")) {
            entity.setPublicationDatabase(extractString(map.containsKey("publicationDatabase") ? map.get("publicationDatabase") : map.get("_publication_database")));
        }
        if (map.containsKey("locality") || map.containsKey("_locality")) {
            entity.setLocality(extractString(map.containsKey("locality") ? map.get("locality") : map.get("_locality")));
        }
        if (map.containsKey("country") || map.containsKey("_country")) {
            entity.setCountry(extractString(map.containsKey("country") ? map.get("country") : map.get("_country")));
        }
        if (map.containsKey("employee") || map.containsKey("_employee")) {
            entity.setEmployee(extractStringId(map.containsKey("employee") ? map.get("employee") : map.get("_employee")));
        }
        if (map.containsKey("educationYear") || map.containsKey("_education_year")) {
            entity.setEducationYear(extractString(map.containsKey("educationYear") ? map.get("educationYear") : map.get("_education_year")));
        }
    }

    // =====================================================
    // PublicationScientific (UUID PK)
    // =====================================================

    private static final String PUBLICATION_SCIENTIFIC_ENTITY = "hemishe_EPublicationScientific";

    public Optional<PublicationScientific> findPublicationScientificById(UUID id) {
        return publicationScientificRepository.findById(id);
    }

    public List<PublicationScientific> findAllPublicationScientific() {
        return publicationScientificRepository.findAll();
    }

    public Page<PublicationScientific> findAllPublicationScientific(Pageable pageable) {
        return publicationScientificRepository.findAll(pageable);
    }

    @Transactional
    public PublicationScientific savePublicationScientific(PublicationScientific entity) {
        return publicationScientificRepository.save(entity);
    }

    @Transactional
    public void deletePublicationScientific(PublicationScientific entity) {
        entity.setDeleteTs(LocalDateTime.now());
        publicationScientificRepository.save(entity);
    }

    public Map<String, Object> toPublicationScientificMap(PublicationScientific entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PUBLICATION_SCIENTIFIC_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : "");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "keywords", entity.getKeywords(), returnNulls);
        putIfNotNull(map, "authors", entity.getAuthors(), returnNulls);
        putIfNotNull(map, "authorCounts", entity.getAuthorCounts(), returnNulls);
        putIfNotNull(map, "sourceName", entity.getSourceName(), returnNulls);
        putIfNotNull(map, "issueYear", entity.getIssueYear(), returnNulls);
        putIfNotNull(map, "parameter", entity.getParameter(), returnNulls);
        putIfNotNull(map, "doi", entity.getDoi(), returnNulls);
        map.put("active", entity.getActive());
        putIfNotNull(map, "isChecked", entity.getIsChecked(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        return map;
    }

    public Map<String, Object> toPublicationScientificMinimalMap(PublicationScientific entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PUBLICATION_SCIENTIFIC_ENTITY);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : "");
        map.put("id", entity.getId().toString());
        return map;
    }

    public void updatePublicationScientificFromMap(PublicationScientific entity, Map<String, Object> map) {
        if (map.containsKey("uId") || map.containsKey("u_id")) {
            entity.setUId(extractInteger(map.containsKey("uId") ? map.get("uId") : map.get("u_id")));
        }
        if (map.containsKey("name")) entity.setName(extractString(map.get("name")));
        if (map.containsKey("keywords")) entity.setKeywords(extractString(map.get("keywords")));
        if (map.containsKey("authors")) entity.setAuthors(extractString(map.get("authors")));
        if (map.containsKey("authorCounts") || map.containsKey("author_counts")) {
            entity.setAuthorCounts(extractInteger(map.containsKey("authorCounts") ? map.get("authorCounts") : map.get("author_counts")));
        }
        if (map.containsKey("sourceName") || map.containsKey("source_name")) {
            entity.setSourceName(extractString(map.containsKey("sourceName") ? map.get("sourceName") : map.get("source_name")));
        }
        if (map.containsKey("issueYear") || map.containsKey("issue_year")) {
            entity.setIssueYear(extractInteger(map.containsKey("issueYear") ? map.get("issueYear") : map.get("issue_year")));
        }
        if (map.containsKey("parameter")) entity.setParameter(extractString(map.get("parameter")));
        if (map.containsKey("doi")) entity.setDoi(extractString(map.get("doi")));
        if (map.containsKey("filename")) entity.setFilename(extractString(map.get("filename")));
        if (map.containsKey("position")) entity.setPosition(extractString(map.get("position")));
        if (map.containsKey("active")) entity.setActive(extractBoolean(map.get("active")));
        if (map.containsKey("translations")) entity.setTranslations(extractString(map.get("translations")));
        if (map.containsKey("isChecked") || map.containsKey("is_checked")) {
            entity.setIsChecked(extractBoolean(map.containsKey("isChecked") ? map.get("isChecked") : map.get("is_checked")));
        }
        if (map.containsKey("isCheckedDate") || map.containsKey("is_checked_date")) {
            entity.setIsCheckedDate(extractLocalDateTime(map.containsKey("isCheckedDate") ? map.get("isCheckedDate") : map.get("is_checked_date")));
        }
        // FK fields
        if (map.containsKey("university") || map.containsKey("_university")) {
            entity.setUniversity(extractString(map.containsKey("university") ? map.get("university") : map.get("_university")));
        }
        if (map.containsKey("scientificPublicationType") || map.containsKey("_scientific_publication_type")) {
            entity.setScientificPublicationType(extractString(map.containsKey("scientificPublicationType") ? map.get("scientificPublicationType") : map.get("_scientific_publication_type")));
        }
        if (map.containsKey("publicationDatabase") || map.containsKey("_publication_database")) {
            entity.setPublicationDatabase(extractString(map.containsKey("publicationDatabase") ? map.get("publicationDatabase") : map.get("_publication_database")));
        }
        if (map.containsKey("locality") || map.containsKey("_locality")) {
            entity.setLocality(extractString(map.containsKey("locality") ? map.get("locality") : map.get("_locality")));
        }
        if (map.containsKey("country") || map.containsKey("_country")) {
            entity.setCountry(extractString(map.containsKey("country") ? map.get("country") : map.get("_country")));
        }
        if (map.containsKey("employee") || map.containsKey("_employee")) {
            entity.setEmployee(extractStringId(map.containsKey("employee") ? map.get("employee") : map.get("_employee")));
        }
        if (map.containsKey("educationYear") || map.containsKey("_education_year")) {
            entity.setEducationYear(extractString(map.containsKey("educationYear") ? map.get("educationYear") : map.get("_education_year")));
        }
    }

    // =====================================================
    // DissertationDefense (UUID PK)
    // =====================================================

    private static final String DISSERTATION_DEFENSE_ENTITY = "hemishe_EDissertationDefense";

    public Optional<DissertationDefense> findDissertationDefenseById(UUID id) {
        return dissertationDefenseRepository.findById(id);
    }

    public List<DissertationDefense> findAllDissertationDefense() {
        return dissertationDefenseRepository.findAll();
    }

    public Page<DissertationDefense> findAllDissertationDefense(Pageable pageable) {
        return dissertationDefenseRepository.findAll(pageable);
    }

    @Transactional
    public DissertationDefense saveDissertationDefense(DissertationDefense entity) {
        return dissertationDefenseRepository.save(entity);
    }

    @Transactional
    public void deleteDissertationDefense(DissertationDefense entity) {
        entity.setDeleteTs(LocalDateTime.now());
        dissertationDefenseRepository.save(entity);
    }

    public Map<String, Object> toDissertationDefenseMap(DissertationDefense entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", DISSERTATION_DEFENSE_ENTITY);
        map.put("_instanceName", entity.getDefensePlace() != null ? entity.getDefensePlace() : "");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "defenseDate", entity.getDefenseDate(), returnNulls);
        putIfNotNull(map, "defensePlace", entity.getDefensePlace(), returnNulls);
        putIfNotNull(map, "approvedDate", entity.getApprovedDate(), returnNulls);
        putIfNotNull(map, "diplomaNumber", entity.getDiplomaNumber(), returnNulls);
        putIfNotNull(map, "diplomaGivenDate", entity.getDiplomaGivenDate(), returnNulls);
        putIfNotNull(map, "diplomaGivenByWhom", entity.getDiplomaGivenByWhom(), returnNulls);
        putIfNotNull(map, "registerNumber", entity.getRegisterNumber(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        map.put("active", entity.getActive());
        return map;
    }

    public void updateDissertationDefenseFromMap(DissertationDefense entity, Map<String, Object> map) {
        if (map.containsKey("uId") || map.containsKey("u_id")) {
            entity.setUId(extractInteger(map.containsKey("uId") ? map.get("uId") : map.get("u_id")));
        }
        if (map.containsKey("doctorateStudent") || map.containsKey("_doctorate_student")) {
            entity.setDoctorateStudent(extractUuid(map.containsKey("doctorateStudent") ? map.get("doctorateStudent") : map.get("_doctorate_student")));
        }
        if (map.containsKey("defenseDate") || map.containsKey("defense_date")) {
            entity.setDefenseDate(extractLocalDate(map.containsKey("defenseDate") ? map.get("defenseDate") : map.get("defense_date")));
        }
        if (map.containsKey("defensePlace") || map.containsKey("defense_place")) {
            entity.setDefensePlace(extractString(map.containsKey("defensePlace") ? map.get("defensePlace") : map.get("defense_place")));
        }
        if (map.containsKey("approvedDate") || map.containsKey("approved_date")) {
            entity.setApprovedDate(extractLocalDate(map.containsKey("approvedDate") ? map.get("approvedDate") : map.get("approved_date")));
        }
        if (map.containsKey("diplomaNumber") || map.containsKey("diploma_number")) {
            entity.setDiplomaNumber(extractString(map.containsKey("diplomaNumber") ? map.get("diplomaNumber") : map.get("diploma_number")));
        }
        if (map.containsKey("diplomaGivenDate") || map.containsKey("diploma_given_date")) {
            entity.setDiplomaGivenDate(extractLocalDate(map.containsKey("diplomaGivenDate") ? map.get("diplomaGivenDate") : map.get("diploma_given_date")));
        }
        if (map.containsKey("diplomaGivenByWhom") || map.containsKey("diploma_given_by_whom")) {
            entity.setDiplomaGivenByWhom(extractString(map.containsKey("diplomaGivenByWhom") ? map.get("diplomaGivenByWhom") : map.get("diploma_given_by_whom")));
        }
        if (map.containsKey("registerNumber") || map.containsKey("register_number")) {
            entity.setRegisterNumber(extractString(map.containsKey("registerNumber") ? map.get("registerNumber") : map.get("register_number")));
        }
        if (map.containsKey("filename")) entity.setFilename(extractString(map.get("filename")));
        if (map.containsKey("position") || map.containsKey("_position")) {
            entity.setPosition(extractInteger(map.containsKey("position") ? map.get("position") : map.get("_position")));
        }
        if (map.containsKey("active")) entity.setActive(extractBoolean(map.get("active")));
        if (map.containsKey("translations") || map.containsKey("_translations")) {
            entity.setTranslations(extractString(map.containsKey("translations") ? map.get("translations") : map.get("_translations")));
        }
        if (map.containsKey("speciality") || map.containsKey("_speciality")) {
            entity.setSpeciality(extractUuid(map.containsKey("speciality") ? map.get("speciality") : map.get("_speciality")));
        }
    }

    // =====================================================
    // DoctoralStudent (UUID PK)
    // =====================================================

    private static final String DOCTORAL_STUDENT_ENTITY = "hemishe_EDoctoralStudent";

    public Optional<DoctoralStudent> findDoctoralStudentById(UUID id) {
        return doctoralStudentRepository.findById(id);
    }

    public List<DoctoralStudent> findAllDoctoralStudent() {
        return doctoralStudentRepository.findAll();
    }

    public Page<DoctoralStudent> findAllDoctoralStudent(Pageable pageable) {
        return doctoralStudentRepository.findAll(pageable);
    }

    @Transactional
    public DoctoralStudent saveDoctoralStudent(DoctoralStudent entity) {
        return doctoralStudentRepository.save(entity);
    }

    @Transactional
    public void deleteDoctoralStudent(DoctoralStudent entity) {
        entity.setDeleteTs(LocalDateTime.now());
        doctoralStudentRepository.save(entity);
    }

    public Map<String, Object> toDoctoralStudentMap(DoctoralStudent entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", DOCTORAL_STUDENT_ENTITY);
        map.put("_instanceName", entity.getFullName() != null ? entity.getFullName() : "");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "firstName", entity.getFirstName(), returnNulls);
        putIfNotNull(map, "secondName", entity.getSecondName(), returnNulls);
        putIfNotNull(map, "thirdName", entity.getThirdName(), returnNulls);
        putIfNotNull(map, "passportNumber", entity.getPassportNumber(), returnNulls);
        putIfNotNull(map, "passportPin", entity.getPassportPin(), returnNulls);
        putIfNotNull(map, "birthDate", entity.getBirthDate(), returnNulls);
        putIfNotNull(map, "dissertationTheme", entity.getDissertationTheme(), returnNulls);
        putIfNotNull(map, "homeAddress", entity.getHomeAddress(), returnNulls);
        putIfNotNull(map, "acceptedDate", entity.getAcceptedDate(), returnNulls);
        putIfNotNull(map, "studentIdNumber", entity.getStudentIdNumber(), returnNulls);
        map.put("active", entity.getActive());
        return map;
    }

    public void updateDoctoralStudentFromMap(DoctoralStudent entity, Map<String, Object> map) {
        if (map.containsKey("uId") || map.containsKey("u_id")) {
            entity.setUId(extractInteger(map.containsKey("uId") ? map.get("uId") : map.get("u_id")));
        }
        if (map.containsKey("firstName") || map.containsKey("first_name")) {
            entity.setFirstName(extractString(map.containsKey("firstName") ? map.get("firstName") : map.get("first_name")));
        }
        if (map.containsKey("secondName") || map.containsKey("second_name")) {
            entity.setSecondName(extractString(map.containsKey("secondName") ? map.get("secondName") : map.get("second_name")));
        }
        if (map.containsKey("thirdName") || map.containsKey("third_name")) {
            entity.setThirdName(extractString(map.containsKey("thirdName") ? map.get("thirdName") : map.get("third_name")));
        }
        if (map.containsKey("passportNumber") || map.containsKey("passport_number")) {
            entity.setPassportNumber(extractString(map.containsKey("passportNumber") ? map.get("passportNumber") : map.get("passport_number")));
        }
        if (map.containsKey("passportPin") || map.containsKey("passport_pin")) {
            entity.setPassportPin(extractString(map.containsKey("passportPin") ? map.get("passportPin") : map.get("passport_pin")));
        }
        if (map.containsKey("birthDate") || map.containsKey("birth_date")) {
            entity.setBirthDate(extractLocalDate(map.containsKey("birthDate") ? map.get("birthDate") : map.get("birth_date")));
        }
        if (map.containsKey("dissertationTheme") || map.containsKey("dissertation_theme")) {
            entity.setDissertationTheme(extractString(map.containsKey("dissertationTheme") ? map.get("dissertationTheme") : map.get("dissertation_theme")));
        }
        if (map.containsKey("homeAddress") || map.containsKey("home_address")) {
            entity.setHomeAddress(extractString(map.containsKey("homeAddress") ? map.get("homeAddress") : map.get("home_address")));
        }
        if (map.containsKey("acceptedDate") || map.containsKey("accepted_date")) {
            entity.setAcceptedDate(extractLocalDate(map.containsKey("acceptedDate") ? map.get("acceptedDate") : map.get("accepted_date")));
        }
        if (map.containsKey("studentIdNumber") || map.containsKey("student_id_number")) {
            entity.setStudentIdNumber(extractString(map.containsKey("studentIdNumber") ? map.get("studentIdNumber") : map.get("student_id_number")));
        }
        if (map.containsKey("active")) entity.setActive(extractBoolean(map.get("active")));
        if (map.containsKey("translations") || map.containsKey("_translations")) {
            entity.setTranslations(extractString(map.containsKey("translations") ? map.get("translations") : map.get("_translations")));
        }
        // FK fields
        if (map.containsKey("scienceBranch") || map.containsKey("_science_branch")) {
            entity.setScienceBranch(extractString(map.containsKey("scienceBranch") ? map.get("scienceBranch") : map.get("_science_branch")));
        }
        if (map.containsKey("paymentForm") || map.containsKey("_payment_form")) {
            entity.setPaymentForm(extractString(map.containsKey("paymentForm") ? map.get("paymentForm") : map.get("_payment_form")));
        }
        if (map.containsKey("citizenship") || map.containsKey("_citizenship")) {
            entity.setCitizenship(extractString(map.containsKey("citizenship") ? map.get("citizenship") : map.get("_citizenship")));
        }
        if (map.containsKey("nationality") || map.containsKey("_nationality")) {
            entity.setNationality(extractString(map.containsKey("nationality") ? map.get("nationality") : map.get("_nationality")));
        }
        if (map.containsKey("gender") || map.containsKey("_gender")) {
            entity.setGender(extractString(map.containsKey("gender") ? map.get("gender") : map.get("_gender")));
        }
        if (map.containsKey("country") || map.containsKey("_country")) {
            entity.setCountry(extractString(map.containsKey("country") ? map.get("country") : map.get("_country")));
        }
        if (map.containsKey("province") || map.containsKey("_province")) {
            entity.setProvince(extractString(map.containsKey("province") ? map.get("province") : map.get("_province")));
        }
        if (map.containsKey("district") || map.containsKey("_district")) {
            entity.setDistrict(extractString(map.containsKey("district") ? map.get("district") : map.get("_district")));
        }
        if (map.containsKey("soato") || map.containsKey("_soato")) {
            entity.setSoato(extractString(map.containsKey("soato") ? map.get("soato") : map.get("_soato")));
        }
        if (map.containsKey("doctoralStudentType") || map.containsKey("_doctoral_student_type")) {
            entity.setDoctoralStudentType(extractString(map.containsKey("doctoralStudentType") ? map.get("doctoralStudentType") : map.get("_doctoral_student_type")));
        }
        if (map.containsKey("doctorateStudentStatus") || map.containsKey("_doctorate_student_status")) {
            entity.setDoctorateStudentStatus(extractString(map.containsKey("doctorateStudentStatus") ? map.get("doctorateStudentStatus") : map.get("_doctorate_student_status")));
        }
        if (map.containsKey("level") || map.containsKey("_level")) {
            entity.setLevel(extractString(map.containsKey("level") ? map.get("level") : map.get("_level")));
        }
        if (map.containsKey("university") || map.containsKey("_university")) {
            entity.setUniversity(extractString(map.containsKey("university") ? map.get("university") : map.get("_university")));
        }
        if (map.containsKey("department") || map.containsKey("_department")) {
            entity.setDepartment(extractString(map.containsKey("department") ? map.get("department") : map.get("_department")));
        }
        if (map.containsKey("position") || map.containsKey("_position")) {
            entity.setPosition(extractInteger(map.containsKey("position") ? map.get("position") : map.get("_position")));
        }
        if (map.containsKey("speciality") || map.containsKey("_speciality")) {
            entity.setSpeciality(extractUuid(map.containsKey("speciality") ? map.get("speciality") : map.get("_speciality")));
        }
        if (map.containsKey("educationYear") || map.containsKey("_education_year")) {
            entity.setEducationYear(extractString(map.containsKey("educationYear") ? map.get("educationYear") : map.get("_education_year")));
        }
    }

    // =====================================================
    // DoctoralStudentStatus (String code PK)
    // =====================================================

    private static final String DOCTORAL_STUDENT_STATUS_ENTITY = "hemishe_HDoctoralStudentStatus";

    public Optional<DoctoralStudentStatus> findDoctoralStudentStatusById(String code) {
        return doctoralStudentStatusRepository.findById(code);
    }

    public List<DoctoralStudentStatus> findAllDoctoralStudentStatus() {
        return doctoralStudentStatusRepository.findAll();
    }

    @Transactional
    public DoctoralStudentStatus saveDoctoralStudentStatus(DoctoralStudentStatus entity) {
        return doctoralStudentStatusRepository.save(entity);
    }

    @Transactional
    public void deleteDoctoralStudentStatus(DoctoralStudentStatus entity) {
        entity.setDeleteTs(LocalDateTime.now());
        entity.setActive(false);
        doctoralStudentStatusRepository.save(entity);
    }

    /**
     * Native UPSERT for DoctoralStudentStatus (PostgreSQL ON CONFLICT)
     */
    @Transactional
    public DoctoralStudentStatus upsertDoctoralStudentStatus(String code, String name, String nameRu, String nameEn) {
        LocalDateTime now = LocalDateTime.now();
        entityManager.createNativeQuery(
            "INSERT INTO hemishe_h_doctoral_student_status " +
            "(code, name, name_ru, name_en, active, version, create_ts, update_ts, delete_ts, deleted_by) " +
            "VALUES (:code, :name, :nameRu, :nameEn, true, 1, :now, :now, NULL, NULL) " +
            "ON CONFLICT (code) DO UPDATE SET " +
            "name = EXCLUDED.name, name_ru = EXCLUDED.name_ru, name_en = EXCLUDED.name_en, " +
            "active = true, update_ts = EXCLUDED.update_ts, delete_ts = NULL, deleted_by = NULL, " +
            "version = hemishe_h_doctoral_student_status.version + 1"
        )
        .setParameter("code", code)
        .setParameter("name", name)
        .setParameter("nameRu", nameRu)
        .setParameter("nameEn", nameEn)
        .setParameter("now", now)
        .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        return doctoralStudentStatusRepository.findById(code).orElseThrow();
    }

    public Map<String, Object> toDoctoralStudentStatusMap(DoctoralStudentStatus entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", DOCTORAL_STUDENT_STATUS_ENTITY);
        map.put("_instanceName", entity.getName());
        map.put("id", entity.getCode());
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "code", entity.getCode(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        return map;
    }

    public Map<String, Object> toDoctoralStudentStatusMinimalMap(DoctoralStudentStatus entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", DOCTORAL_STUDENT_STATUS_ENTITY);
        map.put("_instanceName", entity.getCode() + " " + entity.getName());
        map.put("id", entity.getCode());
        return map;
    }

    // =====================================================
    // DoctoralStudentType (String code PK)
    // =====================================================

    private static final String DOCTORAL_STUDENT_TYPE_ENTITY = "hemishe_HDoctoralStudentType";

    public Optional<DoctoralStudentType> findDoctoralStudentTypeById(String code) {
        return doctoralStudentTypeRepository.findById(code);
    }

    public List<DoctoralStudentType> findAllDoctoralStudentType() {
        return doctoralStudentTypeRepository.findAll();
    }

    @Transactional
    public DoctoralStudentType saveDoctoralStudentType(DoctoralStudentType entity) {
        return doctoralStudentTypeRepository.save(entity);
    }

    @Transactional
    public void deleteDoctoralStudentType(DoctoralStudentType entity) {
        entity.setDeleteTs(LocalDateTime.now());
        entity.setActive(false);
        doctoralStudentTypeRepository.save(entity);
    }

    /**
     * Native UPSERT for DoctoralStudentType (PostgreSQL ON CONFLICT)
     */
    @Transactional
    public DoctoralStudentType upsertDoctoralStudentType(String code, String name, String nameRu, String nameEn) {
        LocalDateTime now = LocalDateTime.now();
        entityManager.createNativeQuery(
            "INSERT INTO hemishe_h_doctoral_student_type " +
            "(code, name, name_ru, name_en, active, version, create_ts, update_ts, delete_ts, deleted_by) " +
            "VALUES (:code, :name, :nameRu, :nameEn, true, 1, :now, :now, NULL, NULL) " +
            "ON CONFLICT (code) DO UPDATE SET " +
            "name = EXCLUDED.name, name_ru = EXCLUDED.name_ru, name_en = EXCLUDED.name_en, " +
            "active = true, update_ts = EXCLUDED.update_ts, delete_ts = NULL, deleted_by = NULL, " +
            "version = hemishe_h_doctoral_student_type.version + 1"
        )
        .setParameter("code", code)
        .setParameter("name", name)
        .setParameter("nameRu", nameRu)
        .setParameter("nameEn", nameEn)
        .setParameter("now", now)
        .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        return doctoralStudentTypeRepository.findById(code).orElseThrow();
    }

    public Map<String, Object> toDoctoralStudentTypeMap(DoctoralStudentType entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", DOCTORAL_STUDENT_TYPE_ENTITY);
        map.put("_instanceName", entity.getName());
        map.put("id", entity.getCode());
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "code", entity.getCode(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        return map;
    }

    public Map<String, Object> toDoctoralStudentTypeMinimalMap(DoctoralStudentType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", DOCTORAL_STUDENT_TYPE_ENTITY);
        map.put("_instanceName", entity.getCode() + " " + entity.getName());
        map.put("id", entity.getCode());
        return map;
    }

    // =====================================================
    // PublicationLocality (String code PK)
    // =====================================================

    private static final String PUBLICATION_LOCALITY_ENTITY = "hemishe_HPublicationLocality";

    public Optional<PublicationLocality> findPublicationLocalityById(String code) {
        return publicationLocalityRepository.findById(code);
    }

    public List<PublicationLocality> findAllPublicationLocality() {
        return publicationLocalityRepository.findAll();
    }

    @Transactional
    public PublicationLocality savePublicationLocality(PublicationLocality entity) {
        return publicationLocalityRepository.save(entity);
    }

    @Transactional
    public void deletePublicationLocality(PublicationLocality entity) {
        entity.setDeleteTs(LocalDateTime.now());
        entity.setActive(false);
        publicationLocalityRepository.save(entity);
    }

    /**
     * Native UPSERT for PublicationLocality (PostgreSQL ON CONFLICT)
     */
    @Transactional
    public PublicationLocality upsertPublicationLocality(String code, String name, String nameRu, String nameEn) {
        LocalDateTime now = LocalDateTime.now();
        entityManager.createNativeQuery(
            "INSERT INTO hemishe_h_publication_locality " +
            "(code, name, name_ru, name_en, active, version, create_ts, update_ts, delete_ts, deleted_by) " +
            "VALUES (:code, :name, :nameRu, :nameEn, true, 1, :now, :now, NULL, NULL) " +
            "ON CONFLICT (code) DO UPDATE SET " +
            "name = EXCLUDED.name, name_ru = EXCLUDED.name_ru, name_en = EXCLUDED.name_en, " +
            "active = true, update_ts = EXCLUDED.update_ts, delete_ts = NULL, deleted_by = NULL, " +
            "version = hemishe_h_publication_locality.version + 1"
        )
        .setParameter("code", code)
        .setParameter("name", name)
        .setParameter("nameRu", nameRu)
        .setParameter("nameEn", nameEn)
        .setParameter("now", now)
        .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        return publicationLocalityRepository.findById(code).orElseThrow();
    }

    public Map<String, Object> toPublicationLocalityMap(PublicationLocality entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PUBLICATION_LOCALITY_ENTITY);
        map.put("_instanceName", entity.getName());
        map.put("id", entity.getCode());
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "code", entity.getCode(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        return map;
    }

    public Map<String, Object> toPublicationLocalityMinimalMap(PublicationLocality entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", PUBLICATION_LOCALITY_ENTITY);
        map.put("_instanceName", entity.getCode() + " " + entity.getName());
        map.put("id", entity.getCode());
        return map;
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) return id.toString();
            Object code = nested.get("code");
            if (code != null) return code.toString();
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractStringId(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    private Integer extractInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean extractBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private LocalDate extractLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) return null;
            try {
                return LocalDate.parse(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime extractLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) return null;
            try {
                return LocalDateTime.parse(str);
            } catch (Exception e) {
                try {
                    return LocalDate.parse(str).atStartOfDay();
                } catch (Exception e2) {
                    return null;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        if (value instanceof String) {
            try {
                return UUID.fromString((String) value);
            } catch (Exception e) {
                return null;
            }
        }
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) return extractUuid(id);
        }
        return null;
    }
}
