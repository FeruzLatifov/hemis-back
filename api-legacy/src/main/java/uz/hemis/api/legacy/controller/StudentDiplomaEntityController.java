package uz.hemis.api.legacy.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.StudentDiploma;
import uz.hemis.domain.repository.StudentDiplomaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Student Diploma Entity Controller
 * Tag 12: Diplomlar
 *
 * CUBA Platform REST API compatible controller for student diplomas
 * Entity: hemishe_EStudentDiploma
 *
 * Endpoints (7 ta):
 * 1. GET    /app/rest/v2/entities/hemishe_EStudentDiploma           - Barcha diplomlar
 * 2. GET    /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - Bitta diploma
 * 3. PUT    /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - Yangilash
 * 4. POST   /app/rest/v2/entities/hemishe_EStudentDiploma           - Yaratish
 * 5. DELETE /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - O'chirish
 * 6. GET    /app/rest/v2/entities/hemishe_EStudentDiploma/search    - Qidirish (GET)
 * 7. POST   /app/rest/v2/entities/hemishe_EStudentDiploma/search    - Qidirish (POST)
 */
@Tag(name = "12.Diplomlar", description = "Talaba diplomlari (hemishe_EStudentDiploma) - CUBA compatible CRUD")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentDiploma")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class StudentDiplomaEntityController {

    private final StudentDiplomaRepository repository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String ENTITY_NAME = "hemishe_EStudentDiploma";

    // Entity name mappings for nested objects
    private static final String ENTITY_UNIVERSITY = "hemishe_EUniversity";
    private static final String ENTITY_STUDENT = "hemishe_EStudent";
    private static final String ENTITY_EDUCATION_TYPE = "hemishe_HEducationType";
    private static final String ENTITY_EDUCATION_YEAR = "hemishe_HEducationYear";
    private static final String ENTITY_DEPARTMENT = "hemishe_EUniversityDepartment";
    private static final String ENTITY_BLANK_STATUS = "hemishe_HDiplomBlankGenerateStatus";
    private static final String ENTITY_BLANK_CATEGORY = "hemishe_HDiplomBlankCategory";

    // =============================================
    // 1. GET ALL - Barcha diplomlar
    // =============================================
    @GetMapping
    @Operation(summary = "Barcha diplomlarni olish", description = "Diplomlar ro'yxatini pagination bilan qaytaradi. CUBA filter qo'llab-quvvatlanadi.")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sort (e.g. 'createTs-desc')") @RequestParam(required = false) String sort,
            @Parameter(description = "CUBA filter JSON") @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all diplomas - limit: {}, offset: {}, filter: {}", limit, offset, filter);

        // Filter mavjud bo'lsa - bazadan to'g'ridan-to'g'ri filter qilamiz (findAll EMAS!)
        if (filter != null && !filter.isEmpty()) {
            try {
                Map<String, Object> filterMap = objectMapper.readValue(filter, new TypeReference<>() {});

                if (filterMap.containsKey("conditions")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> conditions = (List<Map<String, Object>>) filterMap.get("conditions");

                    // diplomaNumber filter - bazadan to'g'ridan-to'g'ri qidiramiz
                    for (Map<String, Object> cond : conditions) {
                        String property = (String) cond.get("property");
                        String operator = (String) cond.get("operator");
                        Object value = cond.get("value");

                        if ("diplomaNumber".equals(property) && value != null) {
                            List<StudentDiploma> filtered;
                            if ("=".equals(operator)) {
                                // Aniq qidirish
                                filtered = repository.findByDiplomaNumber(String.valueOf(value));
                            } else {
                                // LIKE qidirish
                                filtered = repository.findByDiplomaNumberContainingIgnoreCase(String.valueOf(value));
                            }

                            // Pagination
                            int start = Math.min(offset, filtered.size());
                            int end = Math.min(start + limit, filtered.size());
                            filtered = filtered.subList(start, end);

                            List<Map<String, Object>> result = filtered.stream()
                                    .map(e -> toMap(e, returnNulls, view))
                                    .collect(Collectors.toList());

                            return ResponseEntity.ok(result);
                        }

                        // university filter
                        if ("university".equals(property) && value != null) {
                            List<StudentDiploma> filtered = repository.findByUniversity(String.valueOf(value));

                            int start = Math.min(offset, filtered.size());
                            int end = Math.min(start + limit, filtered.size());
                            filtered = filtered.subList(start, end);

                            List<Map<String, Object>> result = filtered.stream()
                                    .map(e -> toMap(e, returnNulls, view))
                                    .collect(Collectors.toList());

                            return ResponseEntity.ok(result);
                        }

                        // student filter
                        if ("student".equals(property) && value != null) {
                            UUID studentId = UUID.fromString(String.valueOf(value));
                            List<StudentDiploma> filtered = repository.findByStudent(studentId);

                            int start = Math.min(offset, filtered.size());
                            int end = Math.min(start + limit, filtered.size());
                            filtered = filtered.subList(start, end);

                            List<Map<String, Object>> result = filtered.stream()
                                    .map(e -> toMap(e, returnNulls, view))
                                    .collect(Collectors.toList());

                            return ResponseEntity.ok(result);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Filter parse error, using pagination: {}", e.getMessage());
            }
        }

        // Filter yo'q yoki qo'llab-quvvatlanmaydigan filter - standart pagination
        // CUBA compatible: sort parametri bo'lmasa, sort qo'shilmaydi (DB natural order)
        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest;
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            pageRequest = PageRequest.of(page, limit, Sort.by(direction, field));
        } else {
            pageRequest = PageRequest.of(page, limit);
        }
        Page<StudentDiploma> resultPage = repository.findAll(pageRequest);

        List<Map<String, Object>> result = resultPage.getContent().stream()
                .map(e -> toMap(e, returnNulls, view))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =============================================
    // 2. GET BY ID - Bitta diploma
    // =============================================
    @GetMapping("/{entityId}")
    @Operation(summary = "Bitta diplomni olish", description = "UUID bo'yicha diplomni qaytaradi")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET diploma by id: {}", entityId);

        Optional<StudentDiploma> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls, view));
    }

    // =============================================
    // 3. PUT - Yangilash
    // =============================================
    @PutMapping("/{entityId}")
    @Transactional
    @Operation(summary = "Diplomni yangilash", description = "Mavjud diplomni yangilaydi")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT diploma id: {}", entityId);

        Optional<StudentDiploma> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StudentDiploma entity = existingOpt.get();
        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        StudentDiploma saved = repository.save(entity);

        // OLD-HEMIS format: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", "com.company.hemishe.entity.EStudentDiploma-" + saved.getId() + " [detached]");
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =============================================
    // 4. POST - Yaratish
    // =============================================
    @PostMapping
    @Transactional
    @Operation(summary = "Yangi diploma yaratish", description = "Yangi diploma yaratadi")
    public ResponseEntity<?> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create diploma");

        StudentDiploma entity = new StudentDiploma();

        // ID ni body dan olish yoki yangi yaratish
        if (body.containsKey("id")) {
            entity.setId(UUID.fromString((String) body.get("id")));
        } else {
            entity.setId(UUID.randomUUID());
        }

        updateFromMap(entity, body);
        entity.setCreateTs(LocalDateTime.now());

        entityManager.persist(entity);
        entityManager.flush();

        // OLD-HEMIS format: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", "com.company.hemishe.entity.EStudentDiploma-" + entity.getId() + " [detached]");
        response.put("id", entity.getId().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * OLD-HEMIS format tekshirish:
     * - university/student: nested object {"_entityName": "...", "id": "..."}
     * - speciality: oddiy string
     */
    @SuppressWarnings("unchecked")
    private boolean hasValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val == null) continue;

            // university, student uchun: nested object {"_entityName": "...", "id": "..."}
            if ("university".equals(key) || "student".equals(key)) {
                if (val instanceof Map) {
                    Map<String, Object> objMap = (Map<String, Object>) val;
                    // _entityName va id bo'lishi shart
                    Object entityName = objMap.get("_entityName");
                    Object id = objMap.get("id");
                    if (entityName != null && id != null && !id.toString().isEmpty()) {
                        return true;
                    }
                }
                // Nested object bo'lmasa - xato
                continue;
            }

            // speciality uchun: oddiy string
            if ("speciality".equals(key)) {
                if (val instanceof String && !((String) val).isEmpty()) {
                    return true;
                }
                continue;
            }

            // Boshqa maydonlar uchun oddiy tekshirish
            if (!val.toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> createValidationError(String fieldName) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("message", "may not be null");
        error.put("messageTemplate", "{javax.validation.constraints.NotNull.message}");
        error.put("path", fieldName);
        error.put("invalidValue", null);
        return error;
    }

    /**
     * OLD-HEMIS format: nested reference must be {"_entityName": "expectedEntity", "id": "..."}
     */
    @SuppressWarnings("unchecked")
    private boolean isValidNestedRef(Object val, String expectedEntityName) {
        if (val == null || !(val instanceof Map)) {
            return false;
        }
        Map<String, Object> objMap = (Map<String, Object>) val;

        // _entityName majburiy va to'g'ri bo'lishi kerak
        Object entityName = objMap.get("_entityName");
        if (entityName == null || !expectedEntityName.equals(entityName.toString())) {
            return false;
        }

        // id majburiy
        Object id = objMap.get("id");
        return id != null && !id.toString().isEmpty();
    }

    /**
     * OLD-HEMIS format: nested object {"_entityName": "...", "id": "..."} dan id olish
     * yoki oddiy string qiymat
     */
    @SuppressWarnings("unchecked")
    private String extractIdAsString(Object val) {
        if (val == null) return null;
        if (val instanceof UUID) return val.toString();
        if (val instanceof String) return (String) val;
        if (val instanceof Map) {
            Map<String, Object> objMap = (Map<String, Object>) val;
            // OLD-HEMIS format: {"_entityName": "...", "id": "..."}
            Object id = objMap.get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    // =============================================
    // 5. DELETE - O'chirish (soft delete)
    // =============================================
    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(summary = "Diplomni o'chirish", description = "Diplomni soft delete qiladi")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE diploma id: {}", entityId);

        Optional<StudentDiploma> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StudentDiploma diploma = entity.get();
        diploma.setDeleteTs(LocalDateTime.now());
        repository.save(diploma);

        // Old-hemis bilan bir xil: 200 OK (204 emas)
        return ResponseEntity.ok().build();
    }

    // =============================================
    // 6. GET /search - Qidirish (URL params)
    // Old Hemis bilan bir xil: CUBA JSON filter qabul qiladi
    // =============================================
    @GetMapping("/search")
    @Operation(summary = "Diplomlarni qidirish (GET)", description = "CUBA filter formatida diplomlarni qidiradi")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        log.debug("GET search diplomas with filter: {}, limit: {}, offset: {}", filter, limit, offset);

        List<StudentDiploma> result;

        if (filter != null && !filter.isEmpty()) {
            // CUBA JSON filter parse qilish (Old Hemis bilan bir xil)
            try {
                Map<String, Object> filterMap = objectMapper.readValue(filter, new TypeReference<>() {});

                if (filterMap.containsKey("conditions")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> conditions = (List<Map<String, Object>>) filterMap.get("conditions");

                    // Database filtering (POST /search bilan bir xil logic)
                    result = applyDatabaseFiltering(conditions, limit, offset);
                    if (result != null) {
                        return ResponseEntity.ok(
                                result.stream()
                                        .map(e -> toMap(e, returnNulls, view))
                                        .collect(Collectors.toList())
                        );
                    }

                    // Fallback: Memory filtering
                    int page = offset / Math.max(limit, 1);
                    PageRequest pageRequest = PageRequest.of(page, limit * 10);
                    Page<StudentDiploma> resultPage = repository.findAll(pageRequest);
                    result = applyConditions(resultPage.getContent(), conditions);

                    int start = Math.min(offset, result.size());
                    int end = Math.min(start + limit, result.size());
                    result = result.subList(start, end);
                } else {
                    // conditions yo'q - oddiy text search
                    result = repository.findByDiplomaNumberContainingIgnoreCase(filter);
                    int start = Math.min(offset, result.size());
                    int end = Math.min(start + limit, result.size());
                    result = result.subList(start, end);
                }
            } catch (Exception e) {
                // JSON parse xatosi - oddiy text search
                log.debug("Filter is not JSON, using text search: {}", filter);
                result = repository.findByDiplomaNumberContainingIgnoreCase(filter);
                int start = Math.min(offset, result.size());
                int end = Math.min(start + limit, result.size());
                result = result.subList(start, end);
            }
        } else {
            // CUBA compatible: no explicit sort
            int page = offset / Math.max(limit, 1);
            PageRequest pageRequest = PageRequest.of(page, limit);
            Page<StudentDiploma> resultPage = repository.findAll(pageRequest);
            result = resultPage.getContent();
        }

        return ResponseEntity.ok(
                result.stream()
                        .map(e -> toMap(e, returnNulls, view))
                        .collect(Collectors.toList())
        );
    }

    // =============================================
    // 7. POST /search - Qidirish (JSON filter)
    // =============================================
    @PostMapping("/search")
    @Operation(summary = "Diplomlarni qidirish (POST)", description = "CUBA filter formatida diplomlarni qidiradi")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {

        // Body dan yoki query param dan limit/offset olish
        int effectiveLimit = 50;  // default
        int effectiveOffset = 0;  // default

        // Query param dan olish
        if (limit != null) {
            effectiveLimit = limit;
        }
        // Body dan olish (query param ustidan yoziladi)
        if (requestBody != null && requestBody.containsKey("limit")) {
            Object bodyLimit = requestBody.get("limit");
            if (bodyLimit instanceof Number) {
                effectiveLimit = ((Number) bodyLimit).intValue();
            } else if (bodyLimit instanceof String) {
                try {
                    effectiveLimit = Integer.parseInt((String) bodyLimit);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (offset != null) {
            effectiveOffset = offset;
        }
        if (requestBody != null && requestBody.containsKey("offset")) {
            Object bodyOffset = requestBody.get("offset");
            if (bodyOffset instanceof Number) {
                effectiveOffset = ((Number) bodyOffset).intValue();
            } else if (bodyOffset instanceof String) {
                try {
                    effectiveOffset = Integer.parseInt((String) bodyOffset);
                } catch (NumberFormatException ignored) {}
            }
        }

        log.info("POST search diplomas with effectiveLimit: {}, effectiveOffset: {}, queryLimit: {}, queryOffset: {}, bodyHasLimit: {}",
                effectiveLimit, effectiveOffset, limit, offset,
                (requestBody != null && requestBody.containsKey("limit")));

        List<StudentDiploma> result;

        // Check if filter is present
        // OLD-HEMIS CUBA format: {"filter": {"conditions": [...]}}
        if (requestBody != null && requestBody.containsKey("filter")) {
            Object filterObj = requestBody.get("filter");
            @SuppressWarnings("unchecked")
            Map<String, Object> filterMap = (Map<String, Object>) filterObj;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> conditions = (List<Map<String, Object>>) filterMap.get("conditions");

            // Try to use database-level filtering for known conditions
            result = applyDatabaseFiltering(conditions, effectiveLimit, effectiveOffset);
            if (result != null) {
                return ResponseEntity.ok(
                        result.stream()
                                .map(e -> toMap(e, returnNulls, view))
                                .collect(Collectors.toList())
                );
            }

            // Fallback: Use paginated findAll and filter in memory
            int page = effectiveOffset / Math.max(effectiveLimit, 1);
            PageRequest pageRequest = PageRequest.of(page, effectiveLimit * 10);
            Page<StudentDiploma> resultPage = repository.findAll(pageRequest);
            result = applyConditions(resultPage.getContent(), conditions);

            // Apply pagination to filtered results
            int start = Math.min(effectiveOffset, result.size());
            int end = Math.min(start + effectiveLimit, result.size());
            result = result.subList(start, end);
        } else {
            // CUBA compatible: no explicit sort
            int page = effectiveOffset / Math.max(effectiveLimit, 1);
            PageRequest pageRequest = PageRequest.of(page, effectiveLimit);
            Page<StudentDiploma> resultPage = repository.findAll(pageRequest);
            result = resultPage.getContent();
        }

        return ResponseEntity.ok(
                result.stream()
                        .map(e -> toMap(e, returnNulls, view))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Try to apply filtering at database level for known conditions
     * Returns null if conditions are not supported for database filtering
     */
    private List<StudentDiploma> applyDatabaseFiltering(List<Map<String, Object>> conditions, int limit, int offset) {
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }

        for (Map<String, Object> cond : conditions) {
            String property = (String) cond.get("property");
            String operator = (String) cond.get("operator");
            Object value = cond.get("value");

            if (value == null) continue;

            // diplomaNumber filter
            if ("diplomaNumber".equals(property)) {
                List<StudentDiploma> filtered;
                if ("=".equals(operator)) {
                    filtered = repository.findByDiplomaNumber(String.valueOf(value));
                } else {
                    filtered = repository.findByDiplomaNumberContainingIgnoreCase(String.valueOf(value));
                }
                int start = Math.min(offset, filtered.size());
                int end = Math.min(start + limit, filtered.size());
                return filtered.subList(start, end);
            }

            // university filter
            if ("university".equals(property)) {
                List<StudentDiploma> filtered = repository.findByUniversity(String.valueOf(value));
                int start = Math.min(offset, filtered.size());
                int end = Math.min(start + limit, filtered.size());
                return filtered.subList(start, end);
            }

            // student filter
            if ("student".equals(property)) {
                UUID studentId = UUID.fromString(String.valueOf(value));
                List<StudentDiploma> filtered = repository.findByStudent(studentId);
                int start = Math.min(offset, filtered.size());
                int end = Math.min(start + limit, filtered.size());
                return filtered.subList(start, end);
            }
        }

        return null;
    }

    /**
     * Apply CUBA filter conditions
     */
    private List<StudentDiploma> applyConditions(List<StudentDiploma> list, List<Map<String, Object>> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return list;
        }

        return list.stream()
                .filter(entity -> {
                    for (Map<String, Object> cond : conditions) {
                        String property = (String) cond.get("property");
                        String operator = (String) cond.get("operator");
                        Object value = cond.get("value");

                        if (!matchesCondition(entity, property, operator, value)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private boolean matchesCondition(StudentDiploma entity, String property, String operator, Object value) {
        if (property == null || operator == null) return true;

        Object fieldValue = getFieldValue(entity, property);

        if ("=".equals(operator)) {
            if (fieldValue == null) return value == null;
            return fieldValue.toString().equals(String.valueOf(value));
        } else if ("contains".equals(operator) || "like".equals(operator)) {
            if (fieldValue == null) return false;
            return fieldValue.toString().toLowerCase().contains(String.valueOf(value).toLowerCase());
        } else if ("notEmpty".equals(operator)) {
            return fieldValue != null && !fieldValue.toString().isEmpty();
        }

        return true;
    }

    private Object getFieldValue(StudentDiploma entity, String property) {
        return switch (property) {
            case "diplomaNumber" -> entity.getDiplomaNumber();
            case "university" -> entity.getUniversity();
            case "student" -> entity.getStudent();
            case "speciality" -> entity.getSpeciality();
            case "active" -> entity.getActive();
            case "educationType" -> entity.getEducationType();
            case "educationYear" -> entity.getEducationYear();
            case "department" -> entity.getDepartment();
            default -> null;
        };
    }

    /**
     * Convert entity to CUBA-compatible Map
     * When view parameter is provided, returns nested objects for related entities
     */
    private Map<String, Object> toMap(StudentDiploma entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();

        // CUBA standard fields (OLD-HEMIS format)
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", "com.company.hemishe.entity.EStudentDiploma-" + entity.getId() + " [detached]");
        map.put("id", entity.getId().toString());

        boolean useNestedObjects = view != null && !view.isEmpty();

        // Entity fields - with nested objects when view is provided
        // OLD-HEMIS: university va student faqat view bilan qaytariladi
        if (useNestedObjects) {
            if (entity.getUniversity() != null) {
                map.put("university", fetchUniversity(entity.getUniversity()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", null);
            }
            if (entity.getStudent() != null) {
                map.put("student", fetchStudent(entity.getStudent()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("student", null);
            }
        }

        putIfNotNull(map, "speciality", entity.getSpeciality(), returnNulls);
        putIfNotNull(map, "diplomaNumber", entity.getDiplomaNumber(), returnNulls);
        putIfNotNull(map, "registerNumber", entity.getRegisterNumber(), returnNulls);
        putIfNotNull(map, "registerDate", entity.getRegisterDate(), returnNulls);
        putIfNotNull(map, "translations", entity.getTranslations(), returnNulls);
        putIfNotNull(map, "academicRecord", entity.getAcademicRecord(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);

        if (useNestedObjects && entity.getDepartment() != null) {
            map.put("department", fetchDepartment(entity.getDepartment()));
        } else {
            putIfNotNull(map, "department", entity.getDepartment(), returnNulls);
        }

        putIfNotNull(map, "totalAcload", entity.getTotalAcload(), returnNulls);
        putIfNotNull(map, "avgGrade", entity.getAvgGrade(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);

        // diplomCategory -> diplomBlankCategory (nested object)
        if (useNestedObjects && entity.getDiplomCategory() != null) {
            map.put("diplomBlankCategory", fetchDiplomBlankCategory(entity.getDiplomCategory()));
        } else {
            putIfNotNull(map, "diplomCategory", entity.getDiplomCategory(), returnNulls);
        }

        if (useNestedObjects && entity.getEducationYear() != null) {
            map.put("educationYear", fetchEducationYear(entity.getEducationYear()));
        } else {
            putIfNotNull(map, "educationYear", entity.getEducationYear(), returnNulls);
        }

        if (useNestedObjects && entity.getEducationType() != null) {
            map.put("educationType", fetchEducationType(entity.getEducationType()));
        } else {
            putIfNotNull(map, "educationType", entity.getEducationType(), returnNulls);
        }

        putIfNotNull(map, "totalCredit", entity.getTotalCredit(), returnNulls);
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "tag", entity.getTag(), returnNulls);
        putIfNotNull(map, "verify", entity.getVerify(), returnNulls);
        putIfNotNull(map, "hash", entity.getHash(), returnNulls);

        // blankGenerateStatusCode -> blankGenerateStatus (nested object)
        if (useNestedObjects) {
            if (entity.getBlankGenerateStatusCode() != null) {
                map.put("blankGenerateStatus", fetchBlankGenerateStatus(entity.getBlankGenerateStatusCode()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("blankGenerateStatus", null);
            }
        } else {
            putIfNotNull(map, "blankGenerateStatusCode", entity.getBlankGenerateStatusCode(), returnNulls);
        }

        putIfNotNull(map, "studyDuration", entity.getStudyDuration(), returnNulls);
        putIfNotNull(map, "graduationDate", entity.getGraduationDate(), returnNulls);

        // admissionYear - old-hemis da object sifatida qaytariladi
        if (useNestedObjects && entity.getAdmissionYear() != null) {
            map.put("admissionYear", fetchEducationYear(entity.getAdmissionYear()));
        } else {
            putIfNotNull(map, "admissionYear", entity.getAdmissionYear(), returnNulls);
        }

        // Audit fields
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);

        return map;
    }

    // =============================================
    // Nested object fetchers
    // =============================================

    private Map<String, Object> fetchUniversity(String code) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_UNIVERSITY);
            result.put("_instanceName", row.get("name"));
            result.put("id", row.get("code"));
            result.put("code", row.get("code"));
            result.put("name", row.get("name"));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return createMinimalNestedObject(ENTITY_UNIVERSITY, code, code);
        }
    }

    private Map<String, Object> fetchStudent(UUID studentId) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    """
                    SELECT id, version, code, firstname, lastname, fathername, birthday, pinfl,
                           serial_number, _university, _soato, address, current_address,
                           _payment_form, _education_form, _education_type, _education_year,
                           _country, _gender, _language, _student_status, _citizenship,
                           _social_category, _course, _speciality, _faculty,
                           active, tag, phone, email, parent_phone, responsible_person_phone,
                           geo_address, group_name, group_id, edu_start_date,
                           graduation_date, study_duration, is_duplicate, is_graduate,
                           status_order_name, status_order_date, status_order_number,
                           status_order_category, enroll_order_category, enroll_order_number,
                           enroll_order_date, enroll_order_name, verified, points,
                           roommate_count, decree_info_name, decree_info_number, decree_info_date,
                           passport_given_date, status, _current_education_year_code
                    FROM hemishe_e_student WHERE id = ? AND delete_ts IS NULL
                    """,
                    studentId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_STUDENT);
            String instanceName = String.format("%s %s %s",
                    row.get("lastname") != null ? row.get("lastname") : "",
                    row.get("firstname") != null ? row.get("firstname") : "",
                    row.get("fathername") != null ? row.get("fathername") : "").trim();
            result.put("_instanceName", instanceName);
            result.put("id", row.get("id"));
            result.put("isGraduate", row.get("is_graduate"));
            result.put("statusOrderDate", row.get("status_order_date"));
            result.put("decreeInfoName", row.get("decree_info_name"));
            result.put("groupId", row.get("group_id"));

            // Nested classifier: educationYear
            putClassifierIfNotNull(result, "educationYear", row.get("_education_year"),
                    ENTITY_EDUCATION_YEAR, "hemishe_h_education_year");
            // Nested classifier: educationForm
            putClassifierIfNotNull(result, "educationForm", row.get("_education_form"),
                    "hemishe_HEducationForm", "hemishe_h_education_form");

            result.put("points", row.get("points"));
            result.put("tag", row.get("tag"));
            result.put("decreeInfoNumber", row.get("decree_info_number"));
            result.put("responsiblePersonPhone", row.get("responsible_person_phone"));
            result.put("geoAddress", row.get("geo_address"));
            result.put("serialNumber", row.get("serial_number"));
            result.put("active", row.get("active"));
            result.put("statusOrderCategory", row.get("status_order_category"));
            result.put("decreeInfoDate", row.get("decree_info_date"));
            result.put("lastname", row.get("lastname"));
            result.put("groupName", row.get("group_name"));
            result.put("statusOrderNumber", row.get("status_order_number"));
            result.put("phone", row.get("phone"));
            result.put("status", row.get("status"));
            result.put("enrollOrderName", row.get("enroll_order_name"));
            result.put("pinfl", row.get("pinfl"));
            result.put("birthday", row.get("birthday"));
            result.put("firstname", row.get("firstname"));
            result.put("code", row.get("code"));

            // Nested classifier: paymentForm
            putClassifierIfNotNull(result, "paymentForm", row.get("_payment_form"),
                    "hemishe_HPaymentForm", "hemishe_h_payment_form");
            // Nested: soato (special structure)
            putSoatoIfNotNull(result, row.get("_soato"));

            result.put("parentPhone", row.get("parent_phone"));
            result.put("speciality", row.get("_speciality"));
            result.put("enrollOrderDate", row.get("enroll_order_date"));
            result.put("enrollOrderNumber", row.get("enroll_order_number"));
            result.put("roommateCount", row.get("roommate_count"));
            result.put("isDuplicate", row.get("is_duplicate"));
            result.put("email", row.get("email"));
            result.put("address", row.get("address"));
            result.put("eduStartDate", row.get("edu_start_date"));
            result.put("passportGivenDate", row.get("passport_given_date"));
            result.put("verified", row.get("verified"));
            result.put("currentAddress", row.get("current_address"));
            result.put("fathername", row.get("fathername"));
            result.put("graduationDate", row.get("graduation_date"));
            result.put("statusOrderName", row.get("status_order_name"));
            result.put("enrollOrderCategory", row.get("enroll_order_category"));
            result.put("studyDuration", row.get("study_duration"));

            return result;
        } catch (EmptyResultDataAccessException e) {
            return createMinimalNestedObject(ENTITY_STUDENT, studentId, studentId.toString());
        }
    }

    /**
     * Classifier nested object yaratish (old-hemis format)
     * entityName: CUBA entity name (e.g. "hemishe_HEducationForm")
     * tableName: actual DB table name (e.g. "hemishe_h_education_form")
     */
    private void putClassifierIfNotNull(Map<String, Object> map, String key, Object code,
                                         String entityName, String tableName) {
        if (code == null) return;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT * FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL",
                    String.valueOf(code));
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            String name = row.get("name") != null ? row.get("name").toString() : "";
            nested.put("_instanceName", row.get("code") + " " + name);
            nested.put("id", row.get("code"));
            if (row.containsKey("name_ru")) nested.put("nameRu", row.get("name_ru"));
            if (row.containsKey("name")) nested.put("name", row.get("name"));
            if (row.containsKey("active")) nested.put("active", row.get("active"));
            if (row.containsKey("name_en")) nested.put("nameEn", row.get("name_en"));
            map.put(key, nested);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            nested.put("id", code);
            map.put(key, nested);
        }
    }

    /**
     * Soato nested object - old-hemis format bilan
     * Soato has name_uz, name_ru, parent_code (nested) instead of name
     */
    private void putSoatoIfNotNull(Map<String, Object> map, Object code) {
        if (code == null) return;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, name_ru, parent_code, active FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL",
                    String.valueOf(code));
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HSoato");
            nested.put("_instanceName", row.get("code") + " - " + row.get("name_uz"));
            nested.put("id", row.get("code"));
            nested.put("active", row.get("active"));
            nested.put("name_ru", row.get("name_ru"));

            // parent_code nested object
            Object parentCode = row.get("parent_code");
            if (parentCode != null) {
                try {
                    Map<String, Object> parentRow = jdbcTemplate.queryForMap(
                            "SELECT code, name_uz, parent_code FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL",
                            String.valueOf(parentCode));
                    Map<String, Object> parent = new LinkedHashMap<>();
                    parent.put("_entityName", "hemishe_HSoato");
                    parent.put("_instanceName", parentRow.get("code") + " - " + parentRow.get("name_uz"));
                    parent.put("id", parentRow.get("code"));
                    parent.put("code", parentRow.get("code"));
                    parent.put("parent_code", parentRow.get("parent_code"));
                    parent.put("name_uz", parentRow.get("name_uz"));
                    nested.put("parent_code", parent);
                } catch (EmptyResultDataAccessException ex) {
                    nested.put("parent_code", null);
                }
            } else {
                nested.put("parent_code", null);
            }

            nested.put("name_uz", row.get("name_uz"));
            map.put("soato", nested);
        } catch (EmptyResultDataAccessException e) {
            // soato topilmadi
        }
    }

    private Map<String, Object> fetchDepartment(String code) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_DEPARTMENT);
            result.put("_instanceName", row.get("name_uz"));
            result.put("id", row.get("code"));
            result.put("code", row.get("code"));
            result.put("name", row.get("name_uz"));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return createMinimalNestedObject(ENTITY_DEPARTMENT, code, code);
        }
    }

    private Map<String, Object> fetchEducationType(String code) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_h_education_type WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_EDUCATION_TYPE);
            result.put("_instanceName", row.get("name"));
            result.put("id", row.get("code"));
            result.put("code", row.get("code"));
            result.put("name", row.get("name"));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return createMinimalNestedObject(ENTITY_EDUCATION_TYPE, code, code);
        }
    }

    private Map<String, Object> fetchEducationYear(String code) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_h_education_year WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_EDUCATION_YEAR);
            result.put("_instanceName", row.get("name"));
            result.put("id", row.get("code"));
            result.put("code", row.get("code"));
            result.put("name", row.get("name"));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return createMinimalNestedObject(ENTITY_EDUCATION_YEAR, code, code);
        }
    }

    private Map<String, Object> fetchBlankGenerateStatus(String code) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_h_diplom_blank_generate_status WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_BLANK_STATUS);
            result.put("_instanceName", row.get("name"));
            result.put("id", row.get("code"));
            result.put("code", row.get("code"));
            result.put("name", row.get("name"));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return createMinimalNestedObject(ENTITY_BLANK_STATUS, code, code);
        }
    }

    private Map<String, Object> fetchDiplomBlankCategory(String code) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_h_diplom_blank_category WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_BLANK_CATEGORY);
            result.put("_instanceName", row.get("name"));
            result.put("id", row.get("code"));
            result.put("code", row.get("code"));
            result.put("name", row.get("name"));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return createMinimalNestedObject(ENTITY_BLANK_CATEGORY, code, code);
        }
    }

    private Map<String, Object> createMinimalNestedObject(String entityName, Object id, String instanceName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", entityName);
        result.put("_instanceName", instanceName);
        result.put("id", id);
        return result;
    }

    /**
     * Update entity from Map
     * OLD-HEMIS format: underscore'siz maydonlar (university, student, speciality)
     * Supports nested object format: {"id": "value"}
     */
    private void updateFromMap(StudentDiploma entity, Map<String, Object> map) {
        // OLD-HEMIS format: underscore'siz maydonlar
        if (map.containsKey("university")) {
            entity.setUniversity(extractIdAsString(map.get("university")));
        }
        if (map.containsKey("student")) {
            String idStr = extractIdAsString(map.get("student"));
            if (idStr != null) {
                entity.setStudent(UUID.fromString(idStr));
            }
        }
        if (map.containsKey("speciality")) {
            entity.setSpeciality(extractIdAsString(map.get("speciality")));
        }
        if (map.containsKey("diplomaNumber")) {
            Object val = map.get("diplomaNumber");
            entity.setDiplomaNumber(val != null ? val.toString() : null);
        }
        if (map.containsKey("registerNumber")) {
            Object val = map.get("registerNumber");
            entity.setRegisterNumber(val != null ? val.toString() : null);
        }
        if (map.containsKey("registerDate")) {
            Object val = map.get("registerDate");
            if (val instanceof String) {
                entity.setRegisterDate(LocalDate.parse((String) val));
            }
        }
        if (map.containsKey("translations")) {
            Object val = map.get("translations");
            entity.setTranslations(val != null ? val.toString() : null);
        }
        if (map.containsKey("academicRecord")) {
            Object val = map.get("academicRecord");
            entity.setAcademicRecord(val != null ? val.toString() : null);
        }
        if (map.containsKey("active")) {
            Object activeVal = map.get("active");
            if (activeVal instanceof Boolean) {
                entity.setActive((Boolean) activeVal);
            } else if (activeVal != null) {
                entity.setActive(Boolean.parseBoolean(activeVal.toString()));
            }
        }
        if (map.containsKey("department")) {
            entity.setDepartment(extractIdAsString(map.get("department")));
        }
        if (map.containsKey("totalAcload")) {
            Object val = map.get("totalAcload");
            entity.setTotalAcload(val != null ? val.toString() : null);
        }
        if (map.containsKey("avgGrade")) {
            Object val = map.get("avgGrade");
            entity.setAvgGrade(val != null ? val.toString() : null);
        }
        if (map.containsKey("specialityName")) {
            Object val = map.get("specialityName");
            entity.setSpecialityName(val != null ? val.toString() : null);
        }
        if (map.containsKey("diplomCategory")) {
            entity.setDiplomCategory(extractIdAsString(map.get("diplomCategory")));
        }
        if (map.containsKey("educationYear")) {
            entity.setEducationYear(extractIdAsString(map.get("educationYear")));
        }
        if (map.containsKey("educationType")) {
            entity.setEducationType(extractIdAsString(map.get("educationType")));
        }
        if (map.containsKey("totalCredit")) {
            Object val = map.get("totalCredit");
            entity.setTotalCredit(val != null ? val.toString() : null);
        }
        if (map.containsKey("specialityCode")) {
            Object val = map.get("specialityCode");
            entity.setSpecialityCode(val != null ? val.toString() : null);
        }
        if (map.containsKey("tag")) {
            Object val = map.get("tag");
            entity.setTag(val != null ? val.toString() : null);
        }
        if (map.containsKey("verify")) {
            Object val = map.get("verify");
            entity.setVerify(val != null ? val.toString() : null);
        }
        if (map.containsKey("hash")) {
            Object val = map.get("hash");
            entity.setHash(val != null ? val.toString() : null);
        }
        if (map.containsKey("blankGenerateStatusCode")) {
            Object val = map.get("blankGenerateStatusCode");
            entity.setBlankGenerateStatusCode(val != null ? val.toString() : null);
        }
        if (map.containsKey("studyDuration")) {
            Object val = map.get("studyDuration");
            if (val instanceof Number) {
                entity.setStudyDuration(((Number) val).floatValue());
            } else if (val instanceof String && !((String) val).isEmpty()) {
                entity.setStudyDuration(Float.parseFloat((String) val));
            }
        }
        if (map.containsKey("graduationDate")) {
            Object val = map.get("graduationDate");
            if (val instanceof String) {
                entity.setGraduationDate(LocalDate.parse((String) val));
            }
        }
        if (map.containsKey("admissionYear")) {
            entity.setAdmissionYear(extractIdAsString(map.get("admissionYear")));
        }
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
