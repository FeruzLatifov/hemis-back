package uz.hemis.service.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uz.hemis.common.dto.legacy.StudentLegacyDto;
import uz.hemis.domain.entity.Student;

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
 * <p><strong>Table Naming (CUBA format in test3_hemis):</strong></p>
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
        dto.setFullname(buildFullname(student));

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
        dto.setGrantType(null); // grantType alohida jadvalda yo'q
        dto.setCourse(loadSimpleReference("hemishe_h_course", "hemishe_HCourse", student.getCourse()));
        dto.setAccomodation(loadSimpleReference("hemishe_h_accomodation", "hemishe_HAccomodation", student.getAccomodation()));
        dto.setLivingStatus(loadSimpleReference("hemishe_h_student_living_status", "hemishe_HStudentLivingStatus", student.getLivingStatus()));
        dto.setRoommateType(loadSimpleReference("hemishe_h_student_room_mate_type", "hemishe_HStudentRoomMateType", student.getRoommateType()));
        dto.setStudentType(loadSimpleReference("hemishe_h_student_type", "hemishe_HStudentType", "11")); // default = "Oddiy"
        dto.setStatusEducationYear(loadSimpleReference("hemishe_h_education_year", "hemishe_HEducationYear", student.getEducationYear()));

        // Complex nested objects
        dto.setUniversity(loadUniversity(student.getUniversity()));
        dto.setFaculty(loadFaculty(student.getFaculty()));
        dto.setSoato(loadSoato(student.getSoato()));
        dto.setCurrentSoato(loadSoato(student.getCurrentSoato()));
        dto.setTerrain(loadTerrainBySoato(student.getSoato()));
        dto.setCurrentTerrain(loadTerrainBySoato(student.getCurrentSoato()));
        dto.setSpecialityBachelor(loadSpecialityByUuid(student.getSpecialityBachelor()));

        // Speciality info from bachelor speciality
        if (student.getSpecialityBachelor() != null) {
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
            ref.setCode(code);
            ref.setName((String) row.get("name"));
            ref.setNameRu((String) row.get("name_ru"));
            ref.setNameEn((String) row.get("name_en"));
            ref.setActive(getBoolean(row, "active"));
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
                       add_student, allow_grouping, allow_transfer_outside,
                       accreditation_edit, gpa_edit, version,
                       _university_type, _ownership, _university_version, _university_contract_category
                FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL
                """;
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.UniversityReferenceDto uni = new StudentLegacyDto.UniversityReferenceDto();
            uni.setId(code);
            uni.setCode(code);
            uni.setName((String) row.get("name"));
            uni.setStudentUrl((String) row.get("student_url"));
            uni.setTeacherUrl((String) row.get("teacher_url"));
            uni.setTin((String) row.get("tin"));
            uni.setAddress((String) row.get("address"));
            uni.setActive(getBoolean(row, "active"));
            uni.setAddStudent(getBoolean(row, "add_student"));
            uni.setAllowGrouping(getBoolean(row, "allow_grouping"));
            uni.setAllowTransferOutside(getBoolean(row, "allow_transfer_outside"));
            uni.setAccreditationEdit(getBoolean(row, "accreditation_edit"));
            uni.setGpaEdit(getBoolean(row, "gpa_edit"));
            uni.setVersion(getInteger(row, "version"));

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
            String sql = "SELECT code, name_uz, name_ru, name_en, version FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.FacultyReferenceDto faculty = new StudentLegacyDto.FacultyReferenceDto();
            faculty.setId(code);
            faculty.setCode(code);
            faculty.setNameUz((String) row.get("name_uz"));
            faculty.setNameRu((String) row.get("name_ru"));
            faculty.setNameEn((String) row.get("name_en"));
            faculty.setVersion(getInteger(row, "version"));

            return faculty;
        } catch (Exception e) {
            log.debug("Failed to load faculty {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load SOATO with parent from hemishe_h_soato
     */
    private StudentLegacyDto.SoatoReferenceDto loadSoato(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name_uz, name_ru, parent_code, version FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.SoatoReferenceDto soato = new StudentLegacyDto.SoatoReferenceDto();
            soato.setId(code);
            soato.setCode(code);
            soato.setNameUz((String) row.get("name_uz"));
            soato.setNameRu((String) row.get("name_ru"));
            soato.setVersion(getInteger(row, "version"));

            // Recursive parent
            String parentCode = (String) row.get("parent_code");
            if (parentCode != null && !parentCode.equals(code)) {
                soato.setParentCode(loadSoato(parentCode));
            }

            return soato;
        } catch (Exception e) {
            log.debug("Failed to load soato {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load terrain (mahalla) from hemishe_h_terrain
     */
    private StudentLegacyDto.TerrainReferenceDto loadTerrain(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name, name_ru, _soato, version FROM hemishe_h_terrain WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.TerrainReferenceDto terrain = new StudentLegacyDto.TerrainReferenceDto();
            terrain.setId(code);
            terrain.setCode(code);
            terrain.setName((String) row.get("name"));
            terrain.setNameRu((String) row.get("name_ru"));
            terrain.setVersion(getInteger(row, "version"));

            terrain.setSoato(loadSoato((String) row.get("_soato")));

            return terrain;
        } catch (Exception e) {
            log.debug("Failed to load terrain {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load terrain by SOATO code (find first terrain in that region)
     */
    private StudentLegacyDto.TerrainReferenceDto loadTerrainBySoato(String soatoCode) {
        if (soatoCode == null || soatoCode.isBlank()) {
            return null;
        }

        try {
            // Find first terrain in this SOATO region
            String sql = "SELECT code, name, name_ru, _soato, version FROM hemishe_h_terrain WHERE _soato = ? AND delete_ts IS NULL LIMIT 1";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, soatoCode);

            StudentLegacyDto.TerrainReferenceDto terrain = new StudentLegacyDto.TerrainReferenceDto();
            terrain.setId((String) row.get("code"));
            terrain.setCode((String) row.get("code"));
            terrain.setName((String) row.get("name"));
            terrain.setNameRu((String) row.get("name_ru"));
            terrain.setVersion(getInteger(row, "version"));

            terrain.setSoato(loadSoato(soatoCode));

            return terrain;
        } catch (Exception e) {
            log.debug("No terrain found for soato {}: {}", soatoCode, e.getMessage());
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
            spec.setCode((String) row.get("code"));
            spec.setName((String) row.get("name"));
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
}
