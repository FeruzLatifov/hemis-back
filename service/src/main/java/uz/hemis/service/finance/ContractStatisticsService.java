package uz.hemis.service.finance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.finance.ContractStatistics;
import uz.hemis.domain.repository.ContractStatisticsRepository;
import uz.hemis.service.legacy.LegacyClassifierMapLoader;

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
    private final JdbcTemplate jdbcTemplate;
    /**
     * Cached classifier nested-map loader — avoids 7+ DB roundtrips per CUBA response row.
     * Each {@code build*Map} method below is now a thin delegate to the cacheable loader.
     */
    private final LegacyClassifierMapLoader classifierMapLoader;

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

    // =====================================================
    // Classifier nested maps — delegated to LegacyClassifierMapLoader (cached, 24h TTL).
    // Eski implementation har response uchun 7+ DB roundtrip qilardi (mapper N+1).
    // Yangi loader bilan: birinchi response 7 query, keyingi har response 0 query (cache hit).
    // =====================================================

    private Map<String, Object> buildEducationTypeMap(String code) {
        return classifierMapLoader.educationTypeMap(code);
    }

    private Map<String, Object> buildUniversityMap(String code) {
        return classifierMapLoader.universityMap(code);
    }

    private Map<String, Object> buildEducationYearMap(String code) {
        return classifierMapLoader.educationYearMap(code);
    }

    private Map<String, Object> buildEducationFormMap(String code) {
        return classifierMapLoader.educationFormMap(code);
    }

    private Map<String, Object> buildFacultyMap(String code) {
        return classifierMapLoader.facultyMap(code);
    }

    private Map<String, Object> buildCourseMap(String code) {
        return classifierMapLoader.courseMap(code);
    }

    private Map<String, Object> buildSemesterMap(String code) {
        return classifierMapLoader.semesterMap(code);
    }
}
