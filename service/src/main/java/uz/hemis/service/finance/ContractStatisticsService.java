package uz.hemis.service.finance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.finance.ContractStatistics;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.university.UniversityDepartment;
import uz.hemis.domain.repository.ContractStatisticsRepository;
import uz.hemis.domain.repository.UniversityDepartmentRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ContractStatistics Service - Business Logic Layer
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * <p>Endpoint: POST /app/rest/v2/services/student/contractStatistics</p>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractStatisticsService {

    private static final String ENTITY_NAME = "hemishe_RContractStatistics";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final ContractStatisticsRepository repository;
    private final UniversityRepository universityRepository;
    private final UniversityDepartmentRepository universityDepartmentRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Submit contract statistics (OLD-HEMIS Compatible)
     */
    @Transactional(noRollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> submitContractStatistics(Map<String, Object> request, String username) {
        log.info("[ContractStatistics] Submitting: {}", request);
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            Map<String, Object> data = (Map<String, Object>) request.get("contractStatistics");
            if (data == null) {
                result.put("success", false);
                result.put("message", "Missing 'contractStatistics' parameter");
                return result;
            }

            String universityCode = extractCode(data, "university");
            String educationYearCode = extractCode(data, "educationYear");
            String educationTypeCode = extractCode(data, "educationType");
            String educationFormCode = extractCode(data, "educationForm");
            String facultyCode = extractCode(data, "faculty");
            String courseCode = extractCode(data, "course");
            String semesterCode = extractCode(data, "semester");

            String dateStr = (String) data.get("date");
            LocalDate date = dateStr != null ? LocalDate.parse(dateStr, DATE_FORMAT) : null;

            Integer dailyCount = extractInteger(data, "dailyCount");
            Integer total = extractInteger(data, "total");

            // Check if record exists - UPDATE, else CREATE
            // Uses findFirst to handle duplicate records in database
            Optional<ContractStatistics> existing = repository.findFirstByUniversityAndEducationYearAndEducationTypeAndEducationFormAndFacultyAndCourseAndSemesterAndDateAndDeleteTsIsNull(
                    universityCode, educationYearCode, educationTypeCode, educationFormCode,
                    facultyCode, courseCode, semesterCode, date);

            ContractStatistics entity;
            String message;

            if (existing.isPresent()) {
                // UPDATE existing record
                entity = existing.get();
                entity.setDailyCount(dailyCount);
                entity.setTotal(total);
                entity.setUpdatedBy(username);
                entity.setUpdateTs(LocalDateTime.now());
                message = "Successfully updated!";
            } else {
                // CREATE new record
                entity = new ContractStatistics();
                entity.setId(UUID.randomUUID());
                entity.setUniversity(universityCode);
                entity.setEducationYear(educationYearCode);
                entity.setEducationType(educationTypeCode);
                entity.setEducationForm(educationFormCode);
                entity.setFaculty(facultyCode);
                entity.setCourse(courseCode);
                entity.setSemester(semesterCode);
                entity.setDate(date);
                entity.setDailyCount(dailyCount);
                entity.setTotal(total);
                entity.setCreatedBy(username);
                entity.setCreateTs(LocalDateTime.now());
                entity.setUpdateTs(LocalDateTime.now());
                entity.setVersion(1);
                message = "Successfully created!";
            }

            ContractStatistics saved = repository.saveAndFlush(entity);
            log.info("[ContractStatistics] {}: id={}", message, saved.getId());

            result.put("success", true);
            result.put("message", message);
            result.put("data", toOldHemisResponse(saved));

            return result;

        } catch (Exception e) {
            log.error("[ContractStatistics] Error: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Map<String, Object> data, String fieldName) {
        Object field = data.get(fieldName);
        if (field instanceof Map) {
            return (String) ((Map<String, Object>) field).get("code");
        } else if (field instanceof String) {
            return (String) field;
        }
        return null;
    }

    private Integer extractInteger(Map<String, Object> data, String fieldName) {
        Object value = data.get(fieldName);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Convert entity to OLD-HEMIS response format with FULL nested objects
     */
    private Map<String, Object> toOldHemisResponse(ContractStatistics entity) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        map.put("id", entity.getId().toString());

        if (entity.getDate() != null) {
            map.put("date", entity.getDate().format(DATE_FORMAT));
        }

        // Full nested objects (OLD-HEMIS format)
        if (entity.getEducationType() != null) {
            map.put("educationType", buildEducationTypeMap(entity.getEducationType()));
        }
        if (entity.getUniversity() != null) {
            map.put("university", buildUniversityMap(entity.getUniversity()));
        }
        if (entity.getEducationYear() != null) {
            map.put("educationYear", buildEducationYearMap(entity.getEducationYear()));
        }
        if (entity.getEducationForm() != null) {
            map.put("educationForm", buildEducationFormMap(entity.getEducationForm()));
        }

        map.put("version", entity.getVersion() != null ? entity.getVersion() : 1);

        if (entity.getFaculty() != null) {
            map.put("faculty", buildFacultyMap(entity.getFaculty()));
        }

        if (entity.getDailyCount() != null) {
            map.put("dailyCount", entity.getDailyCount());
        }
        if (entity.getTotal() != null) {
            map.put("total", entity.getTotal());
        }
        if (entity.getCreatedBy() != null) {
            map.put("createdBy", entity.getCreatedBy());
        }

        if (entity.getCourse() != null) {
            map.put("course", buildCourseMap(entity.getCourse()));
        }

        if (entity.getCreateTs() != null) {
            map.put("createTs", entity.getCreateTs().format(DATETIME_FORMAT));
        }

        if (entity.getSemester() != null) {
            map.put("semester", buildSemesterMap(entity.getSemester()));
        }

        if (entity.getUpdateTs() != null) {
            map.put("updateTs", entity.getUpdateTs().format(DATETIME_FORMAT));
        }

        return map;
    }

    private Map<String, Object> buildEducationTypeMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationType");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name, name_ru, name_en, is_active as active, version, created_by, updated_by, created_at as create_ts, updated_at as update_ts " +
                "FROM education_type WHERE code = ?", code);

            result.put("id", code);
            if (data.get("name_ru") != null) result.put("nameRu", data.get("name_ru"));
            result.put("code", code);
            if (data.get("updated_by") != null) result.put("updatedBy", data.get("updated_by"));
            if (data.get("created_by") != null) result.put("createdBy", data.get("created_by"));
            if (data.get("name") != null) result.put("name", data.get("name"));
            if (data.get("active") != null) result.put("active", data.get("active"));
            if (data.get("create_ts") != null) result.put("createTs", formatTimestamp(data.get("create_ts")));
            if (data.get("name_en") != null) result.put("nameEn", data.get("name_en"));
            if (data.get("update_ts") != null) result.put("updateTs", formatTimestamp(data.get("update_ts")));
            if (data.get("version") != null) result.put("version", data.get("version"));
        } catch (Exception e) {
            log.warn("Education type not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }

        return result;
    }

    private Map<String, Object> buildUniversityMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_EUniversity");

        Optional<University> uni = universityRepository.findById(code);
        if (uni.isPresent()) {
            University u = uni.get();
            result.put("id", code);
            if (u.getStudentUrl() != null) result.put("studentUrl", u.getStudentUrl());
            if (u.getGradingSystem() != null) result.put("gradingSystem", u.getGradingSystem());
            result.put("code", code);
            if (u.getUzbmbUrl() != null) result.put("uzbmbUrl", u.getUzbmbUrl());
            if (u.getTin() != null) result.put("tin", u.getTin());
            if (u.getCreateTs() != null) result.put("createTs", u.getCreateTs().format(DATETIME_FORMAT));
            if (u.getAddStudent() != null) result.put("addStudent", u.getAddStudent());
            if (u.getAddress() != null) result.put("address", u.getAddress());
            if (u.getUpdatedBy() != null) result.put("updatedBy", u.getUpdatedBy());
            if (u.getAccreditationEdit() != null) result.put("accreditationEdit", u.getAccreditationEdit());
            if (u.getActive() != null) result.put("active", u.getActive());
            if (u.getVersion() != null) result.put("version", u.getVersion());
            if (u.getOneId() != null) result.put("oneId", u.getOneId());
            if (u.getAllowGrouping() != null) result.put("allowGrouping", u.getAllowGrouping());
            if (u.getAllowTransferOutside() != null) result.put("allowTransferOutside", u.getAllowTransferOutside());
            if (u.getCreatedBy() != null) result.put("createdBy", u.getCreatedBy());
            if (u.getName() != null) result.put("name", u.getName());
            if (u.getGpaEdit() != null) result.put("gpaEdit", u.getGpaEdit());
            if (u.getUpdateTs() != null) result.put("updateTs", u.getUpdateTs().format(DATETIME_FORMAT));
        } else {
            result.put("id", code);
            result.put("code", code);
        }

        return result;
    }

    private Map<String, Object> buildEducationYearMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationYear");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name, is_active as active, version, created_by, created_at as create_ts, updated_at as update_ts " +
                "FROM education_year WHERE code = ?", code);

            result.put("id", code);
            result.put("code", code);
            if (data.get("created_by") != null) result.put("createdBy", data.get("created_by"));
            if (data.get("name") != null) result.put("name", data.get("name"));
            if (data.get("active") != null) result.put("active", data.get("active"));
            if (data.get("create_ts") != null) result.put("createTs", formatTimestamp(data.get("create_ts")));
            if (data.get("update_ts") != null) result.put("updateTs", formatTimestamp(data.get("update_ts")));
            if (data.get("version") != null) result.put("version", data.get("version"));
        } catch (Exception e) {
            log.warn("Education year not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }

        return result;
    }

    private Map<String, Object> buildEducationFormMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationForm");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name, name_ru, name_en, is_active as active, version, created_by, updated_by, created_at as create_ts, updated_at as update_ts " +
                "FROM education_form WHERE code = ?", code);

            result.put("id", code);
            if (data.get("name_ru") != null) result.put("nameRu", data.get("name_ru"));
            result.put("code", code);
            if (data.get("updated_by") != null) result.put("updatedBy", data.get("updated_by"));
            if (data.get("created_by") != null) result.put("createdBy", data.get("created_by"));
            if (data.get("name") != null) result.put("name", data.get("name"));
            if (data.get("active") != null) result.put("active", data.get("active"));
            if (data.get("create_ts") != null) result.put("createTs", formatTimestamp(data.get("create_ts")));
            if (data.get("name_en") != null) result.put("nameEn", data.get("name_en"));
            if (data.get("update_ts") != null) result.put("updateTs", formatTimestamp(data.get("update_ts")));
            if (data.get("version") != null) result.put("version", data.get("version"));
        } catch (Exception e) {
            log.warn("Education form not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }

        return result;
    }

    private Map<String, Object> buildFacultyMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_EUniversityDepartment");

        Optional<UniversityDepartment> dept = universityDepartmentRepository.findByCode(code);
        if (dept.isPresent()) {
            UniversityDepartment d = dept.get();
            result.put("id", code);
            result.put("code", code);
            if (d.getUpdatedBy() != null) result.put("updatedBy", d.getUpdatedBy());
            if (d.getVersion() != null) result.put("version", d.getVersion());
            if (d.getNameUz() != null) result.put("nameUz", d.getNameUz());
            if (d.getNameRu() != null) result.put("nameRu", d.getNameRu());
            if (d.getCreatedBy() != null) result.put("createdBy", d.getCreatedBy());
            if (d.getCreateTs() != null) result.put("createTs", d.getCreateTs().format(DATETIME_FORMAT));
            if (d.getUpdateTs() != null) result.put("updateTs", d.getUpdateTs().format(DATETIME_FORMAT));
            if (d.getStatus() != null) result.put("status", d.getStatus());
        } else {
            result.put("id", code);
            result.put("code", code);
        }

        return result;
    }

    private Map<String, Object> buildCourseMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HCourse");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name, is_active as active, version, created_by, created_at as create_ts " +
                "FROM course WHERE code = ?", code);

            result.put("id", code);
            result.put("code", code);
            if (data.get("created_by") != null) result.put("createdBy", data.get("created_by"));
            if (data.get("name") != null) result.put("name", data.get("name"));
            if (data.get("active") != null) result.put("active", data.get("active"));
            if (data.get("create_ts") != null) result.put("createTs", formatTimestamp(data.get("create_ts")));
            if (data.get("version") != null) result.put("version", data.get("version"));
        } catch (Exception e) {
            log.warn("Course not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }

        return result;
    }

    private Map<String, Object> buildSemesterMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HSemester");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name, is_active as active, version, created_by, updated_by, created_at as create_ts " +
                "FROM semester WHERE code = ?", code);

            result.put("id", code);
            result.put("code", code);
            if (data.get("updated_by") != null) result.put("updatedBy", data.get("updated_by"));
            if (data.get("created_by") != null) result.put("createdBy", data.get("created_by"));
            if (data.get("name") != null) result.put("name", data.get("name"));
            if (data.get("active") != null) result.put("active", data.get("active"));
            if (data.get("create_ts") != null) result.put("createTs", formatTimestamp(data.get("create_ts")));
            if (data.get("version") != null) result.put("version", data.get("version"));
        } catch (Exception e) {
            log.warn("Semester not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }

        return result;
    }

    private String formatTimestamp(Object ts) {
        if (ts instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) ts).toLocalDateTime().format(DATETIME_FORMAT);
        } else if (ts instanceof LocalDateTime) {
            return ((LocalDateTime) ts).format(DATETIME_FORMAT);
        }
        return ts != null ? ts.toString() : null;
    }
}
