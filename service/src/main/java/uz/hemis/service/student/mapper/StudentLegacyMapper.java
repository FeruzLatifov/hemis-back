package uz.hemis.service.student.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hemis.common.dto.legacy.StudentLegacyDto;
import uz.hemis.domain.entity.student.Student;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Student Legacy Mapper - Convert Student Entity to OLD-HEMIS CUBA Format
 *
 * <p><strong>Purpose:</strong> 100% backward compatibility with OLD-HEMIS REST API</p>
 * <p><strong>Strategy:</strong> Query classifier tables directly via JDBC for nested objects</p>
 *
 * <p>Why JDBC instead of JPA?</p>
 * <ul>
 *   <li>Student entity uses VARCHAR soft references (no @ManyToOne)</li>
 *   <li>Creating JPA entities for 50+ classifier tables is overkill</li>
 *   <li>JDBC is faster for simple lookups</li>
 *   <li>Keeps domain layer clean</li>
 * </ul>
 *
 * <p><strong>Table Naming (CUBA format in test1_hemis):</strong></p>
 * <ul>
 *   <li>Classifiers: hemishe_h_* (e.g., hemishe_h_country, hemishe_h_education_type)</li>
 *   <li>Entities: hemishe_e_* (e.g., hemishe_e_student, hemishe_e_university)</li>
 *   <li>Response _entityName: hemishe_H* or hemishe_E* (CUBA format)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StudentLegacyMapper {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Convert Student entity to OLD-HEMIS CUBA format
     *
     * @param student Student entity
     * @return CUBA-formatted DTO with all nested objects
     */
    public StudentLegacyDto toLegacyDto(Student student) {
        if (student == null) {
            return null;
        }

        StudentLegacyDto dto = new StudentLegacyDto();

        // Basic fields
        dto.setId(student.getId());
        dto.setCode(student.getCode());
        dto.setPinfl(student.getPinfl());
        dto.setSerialNumber(student.getSerialNumber());
        dto.setFirstname(student.getFirstName());
        dto.setLastname(student.getLastname());
        dto.setFathername(student.getFathername());
        dto.setBirthday(student.getBirthday());
        dto.setPhone(student.getPhone());
        dto.setEmail(student.getEmail());
        dto.setAddress(student.getAddress());
        dto.setCurrentAddress(student.getCurrentAddress());
        dto.setParentPhone(student.getParentPhone());
        dto.setResponsiblePersonPhone(student.getResponsiblePersonPhone());
        dto.setGeoAddress(student.getGeoAddress());
        dto.setPassportGivenDate(student.getPassportGivenDate());
        dto.setTag(student.getTag());
        dto.setGroupId(student.getGroupId());
        dto.setGroupName(student.getGroupName());
        dto.setIsGraduate(student.getIsGraduate());
        dto.setIsDuplicate(student.getIsDuplicate());
        dto.setEnrollOrderName(student.getEnrollOrderName());
        dto.setEnrollOrderDate(student.getEnrollOrderDate());
        dto.setEnrollOrderNumber(student.getEnrollOrderNumber());
        dto.setEnrollOrderCategory(student.getEnrollOrderCategory());

        // Status Order Fields (OLD-HEMIS backward compatibility)
        dto.setStatusOrderName(student.getStatusOrderName());
        dto.setStatusOrderDate(student.getStatusOrderDate());
        dto.setStatusOrderNumber(student.getStatusOrderNumber());
        dto.setStatusOrderCategory(student.getStatusOrderCategory());

        // Decree Info Fields
        dto.setDecreeInfoName(student.getDecreeInfoName());
        dto.setDecreeInfoNumber(student.getDecreeInfoNumber());
        dto.setDecreeInfoDate(student.getDecreeInfoDate());

        // Education Start / Graduation Fields
        dto.setEduStartDate(student.getEduStartDate());
        dto.setGraduationDate(student.getGraduationDate());
        dto.setStudyDuration(student.getStudyDuration());

        // Points field
        dto.setPoints(student.getPoints());
        dto.setRoommateCount(student.getRoommateCount());

        dto.setFullname(buildFullname(student));
        dto.setInstanceName(buildFullname(student));

        // Audit fields
        dto.setVersion(student.getVersion());
        dto.setActive(student.getActive());
        dto.setVerified(student.getVerified());
        dto.setCreateTs(student.getCreateTs());
        dto.setUpdateTs(student.getUpdateTs());
        dto.setCreatedBy(student.getCreatedBy());
        dto.setUpdatedBy(student.getUpdatedBy());

        // Nested reference objects - query CUBA classifiers (hemishe_h_* tables)
        dto.setCountry(loadSimpleReference("hemishe_h_country", "hemishe_HCountry", student.getCountry()));
        dto.setEducationType(loadSimpleReference("hemishe_h_education_type", "hemishe_HEducationType", student.getEducationType()));
        dto.setEducationYear(loadSimpleReference("hemishe_h_education_year", "hemishe_HEducationYear", student.getEducationYear()));
        dto.setEducationForm(loadSimpleReference("hemishe_h_education_form", "hemishe_HEducationForm", student.getEducationForm()));
        dto.setLanguage(loadSimpleReference("hemishe_h_education_language", "hemishe_HEducationLanguage", student.getLanguage()));
        dto.setSocialCategory(loadSimpleReference("hemishe_h_student_social_type", "hemishe_HStudentSocialType", student.getSocialCategory()));
        dto.setStudentStatus(loadSimpleReference("hemishe_h_student_status_type", "hemishe_HStudentStatusType", student.getStudentStatus()));
        dto.setCitizenship(loadSimpleReference("hemishe_h_citizenship", "hemishe_HCitizenship", student.getCitizenship()));
        dto.setGender(loadSimpleReference("hemishe_h_gender", "hemishe_HGender", student.getGender()));
        dto.setNationality(loadSimpleReference("hemishe_h_nationality", "hemishe_HNationality", student.getNationality()));
        dto.setPaymentForm(loadSimpleReference("hemishe_h_payment_form", "hemishe_HPaymentForm", student.getPaymentForm()));
        dto.setGrantType(loadSimpleReference("hemishe_h_grant_type", "hemishe_HGrantType", student.getGrantType()));
        dto.setPovertyLevel(loadSimpleReference("hemishe_h_poverty_level", "hemishe_HPovertyLevel", student.getPovertyLevel()));
        dto.setCourse(loadSimpleReference("hemishe_h_course", "hemishe_HCourse", student.getCourse()));
        dto.setAccomodation(loadSimpleReference("hemishe_h_accomodation", "hemishe_HAccomodation", student.getAccomodation()));
        dto.setLivingStatus(loadSimpleReference("hemishe_h_student_living_status", "hemishe_HStudentLivingStatus", student.getLivingStatus()));
        dto.setRoommateType(loadSimpleReference("hemishe_h_student_room_mate_type", "hemishe_HStudentRoomMateType", student.getRoommateType()));
        dto.setStipendRate(loadSimpleReference("hemishe_h_stipend_rate", "hemishe_HStipendRate", student.getStipendRate()));
        dto.setExpelReason(loadSimpleReference("hemishe_h_expel", "hemishe_HExpel", student.getExpelReason()));
        dto.setDoctoralStudentType(loadSimpleReference("hemishe_h_doctoral_student_type", "hemishe_HDoctoralStudentType", student.getDoctoralStudentType()));
        // OLD-HEMIS compatibility: These fields ARE returned by OLD-HEMIS in view mode (with null values)
        dto.setAdmissionType(loadSimpleReference("hemishe_h_admission_type", "hemishe_HAdmissionType", student.getAdmissionType()));
        dto.setTransferCountry(loadSimpleReference("hemishe_h_country", "hemishe_HCountry", student.getTransferCountry()));
        dto.setTransferType(loadSimpleReference("hemishe_h_transfer_type", "hemishe_HTransferType", student.getTransferType()));
        dto.setTransferUniversity(student.getTransferUniversity());
        dto.setStudentType(loadSimpleReference("hemishe_h_student_type", "hemishe_HStudentType", student.getStudentType()));
        dto.setStatusEducationYear(loadSimpleReference("hemishe_h_education_year", "hemishe_HEducationYear", student.getStatusEducationYearCode()));
        dto.setCurrentEducationYear(loadSimpleReference("hemishe_h_education_year", "hemishe_HEducationYear", student.getCurrentEducationYearCode()));
        dto.setAcademicMobileType(loadSimpleReference("hemishe_h_academic_mobile_type", "hemishe_HAcademicMobileType", student.getAcademicMobileType()));
        dto.setAcademicReason(loadSimpleReference("hemishe_h_academic_reason", "hemishe_HAcademicReason", student.getAcademicReason()));
        dto.setGraduationYear(loadSimpleReference("hemishe_h_education_year", "hemishe_HEducationYear", student.getGraduationYear()));
        dto.setSpecialityOrdinatura(loadSimpleSpecialityByUuid(student.getSpecialityOrdinatura(), "hemishe_h_speciality_ordinatura", "hemishe_HSpecialityOrdinatura"));

        // Complex nested objects
        dto.setUniversity(loadUniversity(student.getUniversity()));
        dto.setFaculty(loadFaculty(student.getFaculty()));
        dto.setSoato(loadSoatoWithParent(student.getSoato()));
        dto.setCurrentSoato(loadSoatoWithParent(student.getCurrentSoato()));
        dto.setTerrain(loadTerrainByCode(student.getTerrain()));
        dto.setCurrentTerrain(loadCurrentTerrainByCode(student.getCurrentTerrainCode()));
        dto.setSpecialityBachelor(loadSpecialityByUuid(student.getSpecialityBachelor()));
        dto.setSpecialityMaster(loadSimpleSpecialityByUuid(student.getSpecialityMaster(), "hemishe_h_speciality_master", "hemishe_HSpecialityMaster"));
        dto.setSpecialityDoctoral(loadSimpleSpecialityByUuid(student.getSpecialityDoctoral(), "hemishe_h_speciality_doctoral", "hemishe_HSpecialityDoctoral"));

        // Speciality info: depends on educationType (OLD-HEMIS: EStudent.getSpecialityCode())
        // 11=Bachelor → specialityBachelor, 12=Master → specialityMaster, 13=Doctoral → specialityDoctoral
        String eduType = student.getEducationType();
        if ("12".equals(eduType) && dto.getSpecialityMaster() != null) {
            StudentLegacyDto.SimpleReferenceDto spec = dto.getSpecialityMaster();
            dto.setSpecialityName(spec.getName());
            dto.setSpecialityCode(spec.getCode());
            dto.setCommonSpecialityName(spec.getName());
            dto.setCommonSpecialityCode(spec.getCode());
        } else if ("13".equals(eduType) && dto.getSpecialityDoctoral() != null) {
            StudentLegacyDto.SimpleReferenceDto spec = dto.getSpecialityDoctoral();
            dto.setSpecialityName(spec.getName());
            dto.setSpecialityCode(spec.getCode());
            dto.setCommonSpecialityName(spec.getName());
            dto.setCommonSpecialityCode(spec.getCode());
        } else if (student.getSpecialityBachelor() != null) {
            StudentLegacyDto.SpecialityReferenceDto spec = dto.getSpecialityBachelor();
            if (spec != null) {
                dto.setSpecialityName(spec.getName());
                dto.setSpecialityCode(spec.getCode());
                dto.setCommonSpecialityName(spec.getName());
                dto.setCommonSpecialityCode(spec.getCode());
            }
        }

        return dto;
    }

    /**
     * Load simple classifier reference from CUBA tables
     *
     * @param tableName  actual database table name (hemishe_h_*)
     * @param entityName CUBA entity name for response (hemishe_H*)
     * @param code       classifier code
     */
    private StudentLegacyDto.SimpleReferenceDto loadSimpleReference(String tableName, String entityName, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name, name_ru, name_en, active, version FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.SimpleReferenceDto ref = new StudentLegacyDto.SimpleReferenceDto();
            ref.setEntityName(entityName);
            ref.setId(code);
            String name = (String) row.get("name");
            ref.setName(name);

            // _instanceName format: depends on entity type
            if ("hemishe_HEducationYear".equals(entityName)) {
                ref.setInstanceName(name != null ? name : code);
            } else if ("hemishe_HPaymentForm".equals(entityName)) {
                // Payment form uses "12 - To'lov-shartnoma" format with dash
                ref.setInstanceName(code + " - " + (name != null ? name : ""));
            } else if ("hemishe_HExpel".equals(entityName)) {
                // OLD-HEMIS: expelReason _instanceName is name-only (no code prefix)
                ref.setInstanceName(name != null ? name : "");
            } else {
                ref.setInstanceName(code + " " + (name != null ? name : ""));
            }

            // OLD-HEMIS compatibility: different classifiers return different fields
            // Verified against actual OLD-HEMIS responses:
            switch (entityName) {
                case "hemishe_HCountry":
                case "hemishe_HEducationLanguage":
                case "hemishe_HEducationForm":
                case "hemishe_HCourse":
                case "hemishe_HPaymentForm":
                case "hemishe_HCitizenship":
                case "hemishe_HNationality":
                case "hemishe_HAccomodation":
                case "hemishe_HStudentLivingStatus":
                case "hemishe_HStudentRoomMateType":
                case "hemishe_HAcademicReason":
                case "hemishe_HAcademicMobileType":
                case "hemishe_HGrantType":
                case "hemishe_HAdmissionType":
                case "hemishe_HTransferType":
                case "hemishe_HDoctoralStudentType":
                case "hemishe_HStipendRate":
                case "hemishe_HStudentType":
                case "hemishe_HPovertyLevel":
                    // These return: code (no nameRu, nameEn, active)
                    ref.setCode(code);
                    break;
                case "hemishe_HExpel":
                    // OLD-HEMIS: expelReason returns active (no code, nameRu, nameEn)
                    ref.setActive(getBoolean(row, "active"));
                    break;
                case "hemishe_HEducationType":
                    // Returns: code, nameRu, nameEn, active
                    ref.setCode(code);
                    ref.setNameRu((String) row.get("name_ru"));
                    ref.setNameEn((String) row.get("name_en"));
                    ref.setActive(getBoolean(row, "active"));
                    break;
                case "hemishe_HStudentSocialType":
                    // Returns: code, active (no nameRu, nameEn)
                    ref.setCode(code);
                    ref.setActive(getBoolean(row, "active"));
                    break;
                case "hemishe_HGender":
                    // Returns: code only (no active, nameRu, nameEn)
                    ref.setCode(code);
                    break;
                case "hemishe_HStudentStatusType":
                    // OLD-HEMIS: returns code only (no nameRu, nameEn, active)
                    ref.setCode(code);
                    break;
                case "hemishe_HEducationYear":
                    // Returns: code + name (no nameRu, nameEn, active)
                    ref.setCode(code);
                    ref.setNameRu((String) row.get("name_ru"));
                    ref.setNameEn((String) row.get("name_en"));
                    break;
                case "hemishe_HHemisVersionType":
                case "hemishe_HUniversityContractCategory":
                    // Returns: code, nameRu, name, active, nameEn
                    ref.setCode(code);
                    ref.setNameRu((String) row.get("name_ru"));
                    ref.setActive(getBoolean(row, "active"));
                    ref.setNameEn((String) row.get("name_en"));
                    break;
                default:
                    // Default: return code
                    ref.setCode(code);
                    break;
            }

            // OLD-HEMIS service endpoints include version in ALL classifiers
            ref.setVersion(getInteger(row, "version"));

            return ref;
        } catch (Exception e) {
            log.debug("Failed to load reference {}.{}: {}", tableName, code, e.getMessage());
            return null;
        }
    }

    /**
     * Load university with nested references from hemishe_e_university
     */
    private StudentLegacyDto.UniversityReferenceDto loadUniversity(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = """
                SELECT code, name, student_url, teacher_url, tin, address, active,
                       add_student, add_transfer_student, allow_grouping, allow_transfer_outside,
                       accreditation_edit, gpa_edit, version, one_id,
                       _university_type, _ownership, _university_version, _university_contract_category,
                       add_foreign_student, grading_system, uzbmb_url, university_url,
                       mail_address, bank_info, accreditation_info, add_academic_mobile_student
                FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL
                """;
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.UniversityReferenceDto uni = new StudentLegacyDto.UniversityReferenceDto();
            uni.setId(code);
            uni.setCode(code);
            String uniName = (String) row.get("name");
            uni.setName(uniName);
            uni.setInstanceName(code + "-" + (uniName != null ? uniName : ""));
            uni.setStudentUrl((String) row.get("student_url"));
            uni.setTeacherUrl((String) row.get("teacher_url"));
            uni.setTin((String) row.get("tin"));
            uni.setAddress((String) row.get("address"));
            uni.setActive(getBoolean(row, "active"));
            uni.setAddStudent(getBoolean(row, "add_student"));
            uni.setAddTransferStudent(getBoolean(row, "add_transfer_student"));
            uni.setAllowGrouping(getBoolean(row, "allow_grouping"));
            uni.setAllowTransferOutside(getBoolean(row, "allow_transfer_outside"));
            uni.setAccreditationEdit(getBoolean(row, "accreditation_edit"));
            uni.setGpaEdit(getBoolean(row, "gpa_edit"));
            uni.setOneId(getBoolean(row, "one_id"));
            uni.setVersion(getInteger(row, "version"));

            // 7 ta yangi maydon (OLD-HEMIS compatibility)
            uni.setAddForeignStudent(getBoolean(row, "add_foreign_student"));
            uni.setGradingSystem(getBoolean(row, "grading_system"));
            uni.setUzbmbUrl((String) row.get("uzbmb_url"));
            uni.setUniversityUrl((String) row.get("university_url"));
            uni.setMailAddress((String) row.get("mail_address"));
            uni.setBankInfo((String) row.get("bank_info"));
            uni.setAccreditationInfo((String) row.get("accreditation_info"));
            uni.setAddAcademicMobileStudent(getBoolean(row, "add_academic_mobile_student"));

            // Nested references
            uni.setUniversityType(loadSimpleReference("hemishe_h_university_type", "hemishe_HUniversityType", (String) row.get("_university_type")));
            uni.setOwnership(loadSimpleReference("hemishe_h_ownership", "hemishe_HOwnership", (String) row.get("_ownership")));
            uni.setVersionType(loadSimpleReference("hemishe_h_hemis_version_type", "hemishe_HHemisVersionType", (String) row.get("_university_version")));
            uni.setUniversityContractCategory(loadSimpleReference("hemishe_h_university_contract_category", "hemishe_HUniversityContractCategory", (String) row.get("_university_contract_category")));

            return uni;
        } catch (Exception e) {
            log.debug("Failed to load university {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load faculty/department from hemishe_e_university_department
     */
    private StudentLegacyDto.FacultyReferenceDto loadFaculty(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name_uz, university_code, version FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.FacultyReferenceDto faculty = new StudentLegacyDto.FacultyReferenceDto();
            faculty.setId(code);
            faculty.setCode(code);
            String nameUz = (String) row.get("name_uz");
            faculty.setNameUz(nameUz);
            faculty.setInstanceName(nameUz != null ? nameUz : code);
            faculty.setVersion(getInteger(row, "version"));

            // OLD-HEMIS compatibility: faculty nested university object
            String universityCode = (String) row.get("university_code");
            if (universityCode != null && !universityCode.isBlank()) {
                faculty.setUniversity(loadUniversity(universityCode));
            }

            return faculty;
        } catch (Exception e) {
            log.debug("Failed to load faculty {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load SOATO with parent_code from hemishe_h_soato
     * OLD-HEMIS format for TOP-LEVEL soato/currentSoato: parent_code included, NO name_ru
     */
    private StudentLegacyDto.SoatoReferenceDto loadSoatoWithParent(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name_uz, name_ru, parent_code, version FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.SoatoReferenceDto soato = new StudentLegacyDto.SoatoReferenceDto();
            soato.setId(code);
            soato.setCode(code);
            String soatoNameUz = (String) row.get("name_uz");
            soato.setNameUz(soatoNameUz);
            // OLD-HEMIS compatibility: top-level soato HAS name_ru
            soato.setNameRu((String) row.get("name_ru"));
            soato.setInstanceName(code + " - " + (soatoNameUz != null ? soatoNameUz : ""));
            soato.setVersion(getInteger(row, "version"));

            // OLD-HEMIS compatibility: top-level soato HAS parent_code
            String parentCode = (String) row.get("parent_code");
            if (parentCode != null && !parentCode.isBlank()) {
                soato.setParentCode(loadParentSoato(parentCode));
            }

            return soato;
        } catch (Exception e) {
            log.debug("Failed to load soato with parent {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load parent SOATO (for nested parent_code object)
     * OLD-HEMIS format: _entityName, _instanceName, id, code, name_uz (no name_ru, no parent_code)
     */
    private StudentLegacyDto.SoatoReferenceDto loadParentSoato(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name_uz, version FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.SoatoReferenceDto soato = new StudentLegacyDto.SoatoReferenceDto();
            soato.setId(code);
            soato.setCode(code);
            String soatoNameUz = (String) row.get("name_uz");
            soato.setNameUz(soatoNameUz);
            // OLD-HEMIS compatibility: parent_code object has NO name_ru, NO nested parent_code
            soato.setInstanceName(code + " - " + (soatoNameUz != null ? soatoNameUz : ""));
            soato.setVersion(getInteger(row, "version"));

            return soato;
        } catch (Exception e) {
            log.debug("Failed to load parent soato {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load SOATO for currentTerrain.soato (nested) from hemishe_h_soato
     * OLD-HEMIS format: HAS name_ru, HAS parent_code
     */
    private StudentLegacyDto.SoatoReferenceDto loadSoatoForCurrentTerrain(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name_uz, name_ru, parent_code, version FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.SoatoReferenceDto soato = new StudentLegacyDto.SoatoReferenceDto();
            soato.setId(code);
            soato.setCode(code);
            String soatoNameUz = (String) row.get("name_uz");
            soato.setNameUz(soatoNameUz);
            soato.setNameRu((String) row.get("name_ru"));
            soato.setInstanceName(code + " - " + (soatoNameUz != null ? soatoNameUz : ""));
            soato.setVersion(getInteger(row, "version"));

            // parent_code as nested object
            String parentCode = (String) row.get("parent_code");
            if (parentCode != null && !parentCode.isBlank()) {
                soato.setParentCode(loadParentSoato(parentCode));
            }

            return soato;
        } catch (Exception e) {
            log.debug("Failed to load soato for currentTerrain {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load SOATO for terrain.soato (nested) from hemishe_h_soato
     * OLD-HEMIS format: HAS name_ru, HAS parent_code
     */
    private StudentLegacyDto.SoatoReferenceDto loadSoatoForTerrain(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name_uz, name_ru, parent_code, version FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.SoatoReferenceDto soato = new StudentLegacyDto.SoatoReferenceDto();
            soato.setId(code);
            soato.setCode(code);
            String soatoNameUz = (String) row.get("name_uz");
            soato.setNameUz(soatoNameUz);
            soato.setNameRu((String) row.get("name_ru"));
            soato.setInstanceName(code + " - " + (soatoNameUz != null ? soatoNameUz : ""));
            soato.setVersion(getInteger(row, "version"));

            // parent_code as nested object
            String parentCode = (String) row.get("parent_code");
            if (parentCode != null && !parentCode.isBlank()) {
                soato.setParentCode(loadParentSoato(parentCode));
            }

            return soato;
        } catch (Exception e) {
            log.debug("Failed to load soato for terrain {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load terrain by terrain code (for dto.terrain field)
     * OLD-HEMIS: EStudent._TERRAIN stores the HTerrain.code (PK), NOT the SOATO code.
     * OLD-HEMIS format: terrain.soato has NO name_ru
     *
     * FIX: Previously looked up terrain by SOATO code (student._soato), which was wrong
     * because multiple terrains can share the same SOATO, causing code mismatch
     * (e.g., returning "6101" instead of stored "6116").
     * Now correctly looks up terrain by its own code (student._terrain).
     */
    private StudentLegacyDto.TerrainReferenceDto loadTerrainByCode(String terrainCode) {
        if (terrainCode == null || terrainCode.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name, name_ru, _soato, version FROM hemishe_h_terrain WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, terrainCode);

            String tCode = (String) row.get("code");
            String tName = (String) row.get("name");
            String tSoato = (String) row.get("_soato");
            StudentLegacyDto.TerrainReferenceDto terrain = new StudentLegacyDto.TerrainReferenceDto();
            terrain.setId(tCode);
            terrain.setCode(tCode);
            terrain.setName(tName);
            terrain.setNameRu((String) row.get("name_ru"));
            terrain.setInstanceName(tCode + " " + (tName != null ? tName : ""));
            terrain.setVersion(getInteger(row, "version"));

            // OLD-HEMIS compatibility: terrain.soato NO name_ru
            terrain.setSoato(loadSoatoForTerrain(tSoato));

            return terrain;
        } catch (Exception e) {
            log.debug("No terrain found for code {}: {}", terrainCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load currentTerrain by terrain code (for dto.currentTerrain field)
     * OLD-HEMIS: EStudent.CURRENT_TERRAIN_CODE stores the HTerrain.code (PK), NOT the SOATO code.
     * OLD-HEMIS format: currentTerrain.soato HAS name_ru
     *
     * FIX: Previously looked up terrain by SOATO code (student._current_soato), which was wrong.
     * Now correctly looks up terrain by its own code (student.current_terrain_code).
     */
    private StudentLegacyDto.TerrainReferenceDto loadCurrentTerrainByCode(String terrainCode) {
        if (terrainCode == null || terrainCode.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name, name_ru, _soato, version FROM hemishe_h_terrain WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, terrainCode);

            String tCode = (String) row.get("code");
            String tName = (String) row.get("name");
            String tSoato = (String) row.get("_soato");
            StudentLegacyDto.TerrainReferenceDto terrain = new StudentLegacyDto.TerrainReferenceDto();
            terrain.setId(tCode);
            terrain.setCode(tCode);
            terrain.setName(tName);
            terrain.setNameRu((String) row.get("name_ru"));
            terrain.setInstanceName(tCode + " " + (tName != null ? tName : ""));
            terrain.setVersion(getInteger(row, "version"));

            // OLD-HEMIS compatibility: currentTerrain.soato HAS name_ru
            terrain.setSoato(loadSoatoForCurrentTerrain(tSoato));

            return terrain;
        } catch (Exception e) {
            log.debug("No currentTerrain found for code {}: {}", terrainCode, e.getMessage());
            return null;
        }
    }

    /**
     * Load speciality as SimpleReferenceDto by UUID
     * Used for specialityMaster, specialityDoctoral where type is SimpleReferenceDto
     */
    private StudentLegacyDto.SimpleReferenceDto loadSimpleSpecialityByUuid(UUID uuid, String tableName, String entityName) {
        if (uuid == null) {
            return null;
        }

        try {
            String sql = "SELECT id, code, name, version, active FROM " + tableName + " WHERE id = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, uuid);

            StudentLegacyDto.SimpleReferenceDto ref = new StudentLegacyDto.SimpleReferenceDto();
            ref.setEntityName(entityName);
            ref.setId(row.get("id").toString());
            String specCode = (String) row.get("code");
            String specName = (String) row.get("name");
            ref.setCode(specCode);
            ref.setName(specName);
            ref.setInstanceName(specCode + " - " + (specName != null ? specName : ""));
            ref.setActive(getBoolean(row, "active"));
            ref.setVersion(getInteger(row, "version"));

            return ref;
        } catch (Exception e) {
            log.debug("Failed to load simple speciality {} from {}: {}", uuid, tableName, e.getMessage());
            return null;
        }
    }

    /**
     * Load speciality by UUID from hemishe_h_speciality_bachelor
     */
    private StudentLegacyDto.SpecialityReferenceDto loadSpecialityByUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        try {
            String sql = "SELECT id, code, name, version FROM hemishe_h_speciality_bachelor WHERE id = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, uuid);

            StudentLegacyDto.SpecialityReferenceDto spec = new StudentLegacyDto.SpecialityReferenceDto();
            spec.setId(row.get("id").toString());
            String specCode = (String) row.get("code");
            String specName = (String) row.get("name");
            spec.setCode(specCode);
            spec.setName(specName);
            spec.setInstanceName(specCode + " - " + (specName != null ? specName : ""));
            spec.setVersion(getInteger(row, "version"));
            return spec;
        } catch (Exception e) {
            log.debug("Failed to load speciality {}: {}", uuid, e.getMessage());
            return null;
        }
    }

    /**
     * Build fullname from parts
     */
    private String buildFullname(Student student) {
        StringBuilder sb = new StringBuilder();
        if (student.getLastname() != null) {
            sb.append(student.getLastname()).append(" ");
        }
        if (student.getFirstName() != null) {
            sb.append(student.getFirstName()).append(" ");
        }
        if (student.getFathername() != null) {
            sb.append(student.getFathername());
        }
        return sb.toString().trim();
    }

    /**
     * Safe get Boolean from map
     */
    private Boolean getBoolean(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return null;
    }

    /**
     * Safe get Integer from map
     */
    private Integer getInteger(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val instanceof Integer) {
            return (Integer) val;
        } else if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return null;
    }

    /**
     * Convert Student entity to flat service Map (OLD-HEMIS service endpoint format).
     * Used by: get, getActive, getById, getDoctoral, getWithStatus endpoints.
     * Matches old-hemis StudentServiceBean.get() format EXACTLY (snake_case + name lookups).
     */
    public Map<String, Object> toFlatServiceMap(Student student) {
        if (student == null) return null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", emptyIfNull(student.getCode())); // old-hemis: id = student CODE
        result.put("pinfl", emptyIfNull(student.getPinfl()));
        result.put("serial_number", emptyIfNull(student.getSerialNumber()));
        result.put("firstname", emptyIfNull(student.getFirstName()));
        result.put("lastname", emptyIfNull(student.getLastname()));
        result.put("fathername", emptyIfNull(student.getFathername()));
        result.put("birthday", student.getBirthday() != null ? student.getBirthday().toString() + " 00:00:00.000" : "");
        result.put("gender_code", emptyIfNull(student.getGender()));
        result.put("gender_name", lookupName("hemishe_h_gender", student.getGender()));
        result.put("nationality_code", emptyIfNull(student.getNationality()));
        result.put("nationality_name", lookupName("hemishe_h_nationality", student.getNationality()));
        result.put("citizenship_code", emptyIfNull(student.getCitizenship()));
        result.put("citizenship_name", lookupName("hemishe_h_citizenship", student.getCitizenship()));
        result.put("education_type_code", emptyIfNull(student.getEducationType()));
        result.put("education_type_name", lookupName("hemishe_h_education_type", student.getEducationType()));
        result.put("education_form_code", emptyIfNull(student.getEducationForm()));
        result.put("education_form_name", lookupName("hemishe_h_education_form", student.getEducationForm()));
        result.put("payment_type_code", emptyIfNull(student.getPaymentForm()));
        result.put("payment_type_name", lookupName("hemishe_h_payment_form", student.getPaymentForm()));
        result.put("university_code", emptyIfNull(student.getUniversity()));
        result.put("university_name", lookupName("hemishe_e_university", student.getUniversity()));
        result.put("university_ownership_code", lookupUniversityField(student.getUniversity(), "_ownership"));
        result.put("university_ownership_name", lookupNameByCode("hemishe_h_ownership", lookupUniversityField(student.getUniversity(), "_ownership")));
        result.put("university_type_code", lookupUniversityField(student.getUniversity(), "_university_type"));
        result.put("university_type_name", lookupNameByCode("hemishe_h_university_type", lookupUniversityField(student.getUniversity(), "_university_type")));
        result.put("faculty_code", emptyIfNull(student.getFaculty()));
        result.put("faculty_name", lookupFacultyName(student.getFaculty()));

        // Speciality (conditional by education type)
        String eduType = student.getEducationType();
        if ("11".equals(eduType)) {
            result.put("speciality_id", student.getSpecialityBachelor() != null ? student.getSpecialityBachelor().toString() : "");
            result.put("speciality_code", lookupSpecialityCode("hemishe_h_speciality_bachelor", student.getSpecialityBachelor()));
            result.put("speciality_name", lookupSpecialityName("hemishe_h_speciality_bachelor", student.getSpecialityBachelor()));
        } else if ("12".equals(eduType)) {
            result.put("speciality_id", student.getSpecialityMaster() != null ? student.getSpecialityMaster().toString() : "");
            result.put("speciality_code", lookupSpecialityCode("hemishe_h_speciality_master", student.getSpecialityMaster()));
            result.put("speciality_name", lookupSpecialityName("hemishe_h_speciality_master", student.getSpecialityMaster()));
        } else if ("13".equals(eduType)) {
            result.put("speciality_id", student.getSpecialityDoctoral() != null ? student.getSpecialityDoctoral().toString() : "");
            result.put("speciality_code", lookupSpecialityCode("hemishe_h_speciality_doctoral", student.getSpecialityDoctoral()));
            result.put("speciality_name", lookupSpecialityName("hemishe_h_speciality_doctoral", student.getSpecialityDoctoral()));
        }

        result.put("course_code", emptyIfNull(student.getCourse()));
        result.put("course", lookupName("hemishe_h_course", student.getCourse()));
        result.put("country_code", emptyIfNull(student.getCountry()));
        result.put("country_name", lookupName("hemishe_h_country", student.getCountry()));
        result.put("soato_code", emptyIfNull(student.getSoato()));

        // Region/district from SOATO
        String soato = student.getSoato();
        result.put("region", lookupRegionName(soato));
        result.put("district", lookupSoatoNameUz(soato));
        result.put("address", emptyIfNull(student.getAddress()));

        String currentSoato = student.getCurrentSoato();
        result.put("regionCurrent", lookupRegionName(currentSoato));
        result.put("districtCurrent", lookupSoatoNameUz(currentSoato));
        result.put("address_current", emptyIfNull(student.getCurrentAddress()));

        result.put("accomodation_code", emptyIfNull(student.getAccomodation()));
        result.put("accomodation_name", lookupName("hemishe_h_accomodation", student.getAccomodation()));
        result.put("social_category_code", emptyIfNull(student.getSocialCategory()));
        result.put("social_category_name", lookupName("hemishe_h_student_social_type", student.getSocialCategory()));
        result.put("education_year", emptyIfNull(student.getEducationYear()));
        result.put("education_language_code", emptyIfNull(student.getLanguage()));
        result.put("education_language_name", lookupName("hemishe_h_education_language", student.getLanguage()));
        result.put("group_id", student.getGroupId());
        result.put("group_name", student.getGroupName());
        result.put("status_code", emptyIfNull(student.getStudentStatus()));
        result.put("status_name", lookupName("hemishe_h_student_status_type", student.getStudentStatus()));
        result.put("student_type_code", "11"); // old-hemis default
        result.put("student_type_name", lookupName("hemishe_h_student_type", "11"));

        return result;
    }

    private String lookupUniversityField(String universityCode, String field) {
        if (universityCode == null || universityCode.isBlank()) return "";
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT " + field + " FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL",
                    String.class, universityCode);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return "";  // Rekord topilmadi — CUBA compat: "" qaytaradi
        } catch (org.springframework.dao.DataAccessException e) {
            log.warn("DB lookup failed: {}", e.getMessage());
            return "";
        }
    }

    private String lookupNameByCode(String tableName, String code) {
        if (code == null || code.isBlank()) return "";
        return lookupName(tableName, code);
    }

    private String lookupFacultyName(String code) {
        if (code == null || code.isBlank()) return "";
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT name_uz FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL",
                    String.class, code);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return "";  // Rekord topilmadi — CUBA compat: "" qaytaradi
        } catch (org.springframework.dao.DataAccessException e) {
            log.warn("DB lookup failed: {}", e.getMessage());
            return "";
        }
    }

    private String lookupSpecialityName(String tableName, java.util.UUID uuid) {
        if (uuid == null) return "";
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT name FROM " + tableName + " WHERE id = ? AND delete_ts IS NULL",
                    String.class, uuid);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return "";  // Rekord topilmadi — CUBA compat: "" qaytaradi
        } catch (org.springframework.dao.DataAccessException e) {
            log.warn("DB lookup failed: {}", e.getMessage());
            return "";
        }
    }

    private String lookupSoatoNameUz(String code) {
        if (code == null || code.isBlank()) return "";
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT name_uz FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL",
                    String.class, code);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return "";  // Rekord topilmadi — CUBA compat: "" qaytaradi
        } catch (org.springframework.dao.DataAccessException e) {
            log.warn("DB lookup failed: {}", e.getMessage());
            return "";
        }
    }

    private String lookupRegionName(String soatoCode) {
        if (soatoCode == null || soatoCode.length() < 5) return "";
        String regionCode = soatoCode.substring(0, 4);
        return lookupSoatoNameUz(regionCode);
    }

    /**
     * Convert Student entity to billing/testGet format.
     * Used by: testGet endpoint — matches old-hemis StudentServiceBean.testGet() field names exactly.
     */
    public Map<String, Object> toBillingMap(Student student) {
        if (student == null) return null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", student.getId().toString());
        result.put("hemisId", emptyIfNull(student.getCode()));
        result.put("birthDate", student.getBirthday() != null ? student.getBirthday().toString() : "");
        result.put("serialNumber", emptyIfNull(student.getSerialNumber()));
        result.put("languageCode", emptyIfNull(student.getLanguage()));
        result.put("studentTypeCode", "11"); // old-hemis default: "Oddiy"
        result.put("paymentFormCode", emptyIfNull(student.getPaymentForm()));
        result.put("pinfl", emptyIfNull(student.getPinfl()));
        result.put("universityCode", emptyIfNull(student.getUniversity()));
        result.put("facultyCode", emptyIfNull(student.getFaculty()));

        // Conditional speciality fields based on educationType
        String eduType = student.getEducationType();
        if ("11".equals(eduType)) {
            result.put("specialityBachelorId", student.getSpecialityBachelor() != null ? student.getSpecialityBachelor().toString() : "");
            result.put("specialityBachelorCode", lookupSpecialityCode("hemishe_h_speciality_bachelor", student.getSpecialityBachelor()));
        } else if ("12".equals(eduType)) {
            result.put("specialityMasterId", student.getSpecialityMaster() != null ? student.getSpecialityMaster().toString() : "");
            result.put("specialityMasterCode", lookupSpecialityCode("hemishe_h_speciality_master", student.getSpecialityMaster()));
        } else if ("13".equals(eduType)) {
            result.put("specialityDoctoralId", student.getSpecialityDoctoral() != null ? student.getSpecialityDoctoral().toString() : "");
            result.put("specialityDoctoralCode", lookupSpecialityCode("hemishe_h_speciality_doctoral", student.getSpecialityDoctoral()));
        }

        result.put("eduFormCode", emptyIfNull(student.getEducationForm()));
        result.put("courseCode", emptyIfNull(student.getCourse()));
        result.put("statusCode", emptyIfNull(student.getStudentStatus()));
        result.put("educationTypeCode", emptyIfNull(student.getEducationType()));
        result.put("citizenshipCode", emptyIfNull(student.getCitizenship()));
        result.put("socialCategoryCode", emptyIfNull(student.getSocialCategory()));
        result.put("countryCode", emptyIfNull(student.getCountry()));
        result.put("groupId", emptyIfNull(student.getGroupId()));
        result.put("groupName", emptyIfNull(student.getGroupName()));
        result.put("eduYearCode", emptyIfNull(student.getEducationYear()));
        result.put("gpa", null);

        return result;
    }

    /**
     * Convert Student entity to RStudentFull format (flat denormalized camelCase).
     * Used by: tashkentStudents endpoint — matches old-hemis RStudentFull entity EXACTLY.
     */
    public Map<String, Object> toRStudentFullMap(Student student) {
        if (student == null) return null;

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("_entityName", "hemishe_RStudentFull");
        r.put("_instanceName", emptyIfNull(student.getCode()));
        r.put("id", emptyIfNull(student.getCode()));
        r.put("code", emptyIfNull(student.getCode()));
        r.put("citizenshipCode", emptyIfNull(student.getCitizenship()));
        r.put("citizenshipName", lookupName("hemishe_h_citizenship", student.getCitizenship()));
        r.put("countryCode", emptyIfNull(student.getCountry()));
        r.put("countryName", lookupName("hemishe_h_country", student.getCountry()));
        r.put("nationalityCode", emptyIfNull(student.getNationality()));
        r.put("nationalityName", lookupName("hemishe_h_nationality", student.getNationality()));
        r.put("pinfl", emptyIfNull(student.getPinfl()));
        r.put("passportNumber", emptyIfNull(student.getSerialNumber()));
        String fullname = buildFullname(student);
        r.put("fullname", fullname);
        r.put("birthday", student.getBirthday() != null ? student.getBirthday().toString() : null);
        r.put("genderCode", emptyIfNull(student.getGender()));
        r.put("genderName", lookupName("hemishe_h_gender", student.getGender()));

        // Region/district from SOATO
        String soato = student.getSoato();
        String regionCode = (soato != null && soato.length() >= 4) ? soato.substring(0, 4) : "";
        r.put("regionCode", regionCode);
        r.put("regionName", lookupSoatoNameUz(regionCode));
        r.put("districtCode", emptyIfNull(soato));
        r.put("districtName", lookupSoatoNameUz(soato));
        r.put("address", emptyIfNull(student.getAddress()));
        r.put("addressCurrent", emptyIfNull(student.getCurrentAddress()));

        // University info
        r.put("universityCode", emptyIfNull(student.getUniversity()));
        r.put("universityName", lookupName("hemishe_e_university", student.getUniversity()));
        r.put("universtiryOwnershipCode", lookupUniversityField(student.getUniversity(), "_ownership")); // typo preserved from old-hemis!
        r.put("universityOwnershipName", lookupNameByCode("hemishe_h_ownership", lookupUniversityField(student.getUniversity(), "_ownership")));
        String uniSoato = lookupUniversityField(student.getUniversity(), "_soato");
        String uniRegionCode = (uniSoato != null && uniSoato.length() >= 4) ? uniSoato.substring(0, 4) : "";
        r.put("universityRegionCode", uniRegionCode);
        r.put("universityRegionName", lookupSoatoNameUz(uniRegionCode));
        r.put("universityDistrictCode", emptyIfNull(uniSoato));
        r.put("universityDistrictName", lookupSoatoNameUz(uniSoato));

        // Faculty
        r.put("facultyCode", emptyIfNull(student.getFaculty()));
        r.put("facultyName", lookupFacultyName(student.getFaculty()));

        // Education fields
        r.put("educationTypeCode", emptyIfNull(student.getEducationType()));
        r.put("educationTypeName", lookupName("hemishe_h_education_type", student.getEducationType()));
        r.put("educationFormCode", emptyIfNull(student.getEducationForm()));
        r.put("educationFormName", lookupName("hemishe_h_education_form", student.getEducationForm()));
        r.put("paymentFormCode", emptyIfNull(student.getPaymentForm()));
        r.put("paymentFormName", lookupName("hemishe_h_payment_form", student.getPaymentForm()));
        r.put("courseCode", emptyIfNull(student.getCourse()));
        r.put("courseName", lookupName("hemishe_h_course", student.getCourse()));

        // Speciality (conditional by education type)
        String eduType = student.getEducationType();
        UUID specUuid = null;
        String specTable = null;
        if ("11".equals(eduType)) { specUuid = student.getSpecialityBachelor(); specTable = "hemishe_h_speciality_bachelor"; }
        else if ("12".equals(eduType)) { specUuid = student.getSpecialityMaster(); specTable = "hemishe_h_speciality_master"; }
        else if ("13".equals(eduType)) { specUuid = student.getSpecialityDoctoral(); specTable = "hemishe_h_speciality_doctoral"; }
        r.put("specialityId", specUuid);
        r.put("specialityCode", specTable != null ? lookupSpecialityCode(specTable, specUuid) : "");
        r.put("specialityName", specTable != null ? lookupSpecialityName(specTable, specUuid) : "");

        r.put("educationLanguageCode", emptyIfNull(student.getLanguage()));
        r.put("educationLanguageName", lookupName("hemishe_h_education_language", student.getLanguage()));
        r.put("accomodationCode", emptyIfNull(student.getAccomodation()));
        r.put("accomodationName", lookupName("hemishe_h_accomodation", student.getAccomodation()));
        r.put("socialCategoryCode", emptyIfNull(student.getSocialCategory()));
        r.put("socialCategoryName", lookupName("hemishe_h_student_social_type", student.getSocialCategory()));
        r.put("statusCode", emptyIfNull(student.getStudentStatus()));
        r.put("statusName", lookupName("hemishe_h_student_status_type", student.getStudentStatus()));
        r.put("eduYear", emptyIfNull(student.getEducationYear()));
        r.put("created_at", student.getCreateTs() != null ? student.getCreateTs().toLocalDate().toString() : null);
        r.put("updated_at", student.getUpdateTs() != null ? student.getUpdateTs().toLocalDate().toString() : null);
        r.put("isGraduate", student.getIsGraduate());

        return r;
    }

    /**
     * Convert Student entity to Map for SERVICE endpoints (student/validate, student/id).
     * OLD-HEMIS service endpoints: NO _instanceName, NO createdBy/updatedBy, but HAVE version.
     */
    public Map<String, Object> toLegacyMapForService(Student student) {
        Map<String, Object> map = toLegacyMap(student);
        if (map == null) return null;
        // Service endpoints don't include _instanceName at any level
        // and don't include createdBy/updatedBy
        map.remove("_instanceName");
        map.remove("createdBy");
        map.remove("updatedBy");
        removeInstanceNamesRecursive(map);
        // OLD-HEMIS service endpoints: soato objects don't have name_ru
        removeNameRuFromSoatoRecursive(map);
        return map;
    }

    /**
     * Remove _instanceName from all nested Map objects recursively.
     */
    @SuppressWarnings("unchecked")
    private void removeInstanceNamesRecursive(Map<String, Object> map) {
        for (Object value : map.values()) {
            if (value instanceof Map) {
                Map<String, Object> nested = (Map<String, Object>) value;
                nested.remove("_instanceName");
                removeInstanceNamesRecursive(nested);
            }
        }
    }

    /**
     * Remove name_ru from specific SOATO fields for service endpoints.
     * OLD-HEMIS: soato and terrain.soato have NO name_ru;
     * but currentSoato and currentTerrain.soato KEEP name_ru.
     */
    @SuppressWarnings("unchecked")
    private void removeNameRuFromSoatoRecursive(Map<String, Object> map) {
        // soato (main field) — no name_ru
        Object soato = map.get("soato");
        if (soato instanceof Map) {
            ((Map<String, Object>) soato).remove("name_ru");
        }
        // terrain.soato — no name_ru
        Object terrain = map.get("terrain");
        if (terrain instanceof Map) {
            Object terrainSoato = ((Map<String, Object>) terrain).get("soato");
            if (terrainSoato instanceof Map) {
                ((Map<String, Object>) terrainSoato).remove("name_ru");
            }
        }
        // currentSoato — KEEP name_ru
        // currentTerrain.soato — KEEP name_ru
    }

    private String lookupSpecialityCode(String tableName, java.util.UUID uuid) {
        if (uuid == null) return "";
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT code FROM " + tableName + " WHERE id = ? AND delete_ts IS NULL",
                    String.class, uuid);
        } catch (Exception e) {
            return "";
        }
    }

    private String emptyIfNull(String val) {
        return val != null ? val : "";
    }

    private String lookupName(String tableName, String code) {
        if (code == null || code.isBlank()) return "";
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT name FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL",
                    String.class, code);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Convert Student entity to Map (for JSON serialization with _entityName)
     *
     * <p>Used by student/update endpoint to return OLD-HEMIS CUBA format</p>
     *
     * @param student Student entity
     * @return Map representation with _entityName field
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toLegacyMap(Student student) {
        if (student == null) {
            return null;
        }

        try {
            // Convert DTO to Map via JSON
            StudentLegacyDto dto = toLegacyDto(student);
            String json = objectMapper.writeValueAsString(dto);
            Map<String, Object> map = objectMapper.readValue(json, LinkedHashMap.class);

            // Add CUBA _entityName at top level
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", "hemishe_EStudent");
            result.putAll(map);

            return result;
        } catch (Exception e) {
            log.error("Failed to convert student to legacy map: {}", e.getMessage());
            // Fallback: simple map
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("_entityName", "hemishe_EStudent");
            fallback.put("id", student.getId().toString());
            fallback.put("code", student.getCode());
            return fallback;
        }
    }
}
