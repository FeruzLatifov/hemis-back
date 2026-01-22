package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.ContractStatistics;
import uz.hemis.domain.repository.ContractStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ContractStatistics Service - Business Logic Layer
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * <p>Endpoint: POST /app/rest/v2/services/student/contractStatistics</p>
 *
 * <p><strong>Request format:</strong></p>
 * <pre>
 * {
 *   "contractStatistics": {
 *     "university": {"code": "999"},
 *     "educationYear": {"code": "2021"},
 *     "educationType": {"code": "12"},
 *     "educationForm": {"code": "11"},
 *     "faculty": {"code": "999-192"},
 *     "course": {"code": "11"},
 *     "semester": {"code": "11"},
 *     "date": "2021-09-08",
 *     "dailyCount": 5,
 *     "total": 5
 *   }
 * }
 * </pre>
 *
 * <p><strong>Response format:</strong></p>
 * <pre>
 * {
 *   "success": true,
 *   "message": "Successfully created!",
 *   "data": {...}
 * }
 * </pre>
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

    /**
     * Submit contract statistics (OLD-HEMIS Compatible)
     *
     * @param request request map containing contractStatistics data
     * @param username current user's username for createdBy field
     * @return OLD-HEMIS compatible response
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> submitContractStatistics(Map<String, Object> request, String username) {
        log.info("[ContractStatistics] Submitting: {}", request);
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // Extract contractStatistics object from request
            Map<String, Object> data = (Map<String, Object>) request.get("contractStatistics");
            if (data == null) {
                log.warn("Missing 'contractStatistics' parameter in request");
                result.put("success", false);
                result.put("message", "Missing 'contractStatistics' parameter");
                return result;
            }

            // Extract codes from nested objects
            String universityCode = extractCode(data, "university");
            String educationYearCode = extractCode(data, "educationYear");
            String educationTypeCode = extractCode(data, "educationType");
            String educationFormCode = extractCode(data, "educationForm");
            String facultyCode = extractCode(data, "faculty");
            String courseCode = extractCode(data, "course");
            String semesterCode = extractCode(data, "semester");

            // Extract date
            String dateStr = (String) data.get("date");
            LocalDate date = dateStr != null ? LocalDate.parse(dateStr, DATE_FORMAT) : null;

            // Extract counts
            Integer dailyCount = extractInteger(data, "dailyCount");
            Integer total = extractInteger(data, "total");

            // Create entity
            ContractStatistics entity = new ContractStatistics();
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
            entity.setVersion(1);

            // Save
            ContractStatistics saved = repository.save(entity);
            log.info("[ContractStatistics] Created: id={}, university={}", saved.getId(), saved.getUniversity());

            // Build response
            result.put("success", true);
            result.put("message", "Successfully created!");
            result.put("data", toOldHemisResponse(saved));

            return result;

        } catch (Exception e) {
            log.error("[ContractStatistics] Error: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            return result;
        }
    }

    /**
     * Extract code from nested object like {"code": "value"}
     */
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

    /**
     * Extract integer from data
     */
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
     * Convert entity to OLD-HEMIS response format
     *
     * <p>Response includes nested classifier objects with _entityName, id, code</p>
     */
    private Map<String, Object> toOldHemisResponse(ContractStatistics entity) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        map.put("id", entity.getId().toString());

        // Date
        if (entity.getDate() != null) {
            map.put("date", entity.getDate().format(DATE_FORMAT));
        }

        // Nested classifier objects (OLD-HEMIS format)
        if (entity.getEducationType() != null) {
            map.put("educationType", createClassifierRef("hemishe_HEducationType", entity.getEducationType()));
        }
        if (entity.getUniversity() != null) {
            map.put("university", createClassifierRef("hemishe_EUniversity", entity.getUniversity()));
        }
        if (entity.getEducationYear() != null) {
            map.put("educationYear", createClassifierRef("hemishe_HEducationYear", entity.getEducationYear()));
        }
        if (entity.getEducationForm() != null) {
            map.put("educationForm", createClassifierRef("hemishe_HEducationForm", entity.getEducationForm()));
        }

        // Version
        map.put("version", entity.getVersion() != null ? entity.getVersion() : 1);

        // Faculty
        if (entity.getFaculty() != null) {
            map.put("faculty", createClassifierRef("hemishe_EUniversityDepartment", entity.getFaculty()));
        }

        // Counts
        if (entity.getDailyCount() != null) {
            map.put("dailyCount", entity.getDailyCount());
        }
        if (entity.getTotal() != null) {
            map.put("total", entity.getTotal());
        }

        // Created by
        if (entity.getCreatedBy() != null) {
            map.put("createdBy", entity.getCreatedBy());
        }

        // Course
        if (entity.getCourse() != null) {
            map.put("course", createClassifierRef("hemishe_HCourse", entity.getCourse()));
        }

        // Timestamps
        if (entity.getCreateTs() != null) {
            map.put("createTs", entity.getCreateTs().format(DATETIME_FORMAT));
        }

        // Semester
        if (entity.getSemester() != null) {
            map.put("semester", createClassifierRef("hemishe_HSemester", entity.getSemester()));
        }

        // Update timestamp
        if (entity.getUpdateTs() != null) {
            map.put("updateTs", entity.getUpdateTs().format(DATETIME_FORMAT));
        }

        return map;
    }

    /**
     * Create classifier reference object
     *
     * <p>Format: {"_entityName": "...", "id": "code", "code": "code"}</p>
     */
    private Map<String, Object> createClassifierRef(String entityName, String code) {
        Map<String, Object> ref = new LinkedHashMap<>();
        ref.put("_entityName", entityName);
        ref.put("id", code);
        ref.put("code", code);
        return ref;
    }
}
