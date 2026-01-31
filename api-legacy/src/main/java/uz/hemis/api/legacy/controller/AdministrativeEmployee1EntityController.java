package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.AdministrativeEmployee1;
import uz.hemis.domain.repository.AdministrativeEmployee1Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Administrative Employee1 Entity Controller (CUBA Pattern)
 *
 * Жаҳоннинг нуфузли топ-1000 университетларида PhD ёки DSc илмий
 * даражасига эга бўлган ўқитувчилар тўғрисида маълумот
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_RIAdministrativeEmployee1</li>
 *   <li>Table: hemishe_ri_administrative_employee1</li>
 *   <li>Primary key: id (UUID)</li>
 *   <li>Base URL: /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1</li>
 *   <li>100% backward compatible with OLD-HEMIS CUBA Platform REST API</li>
 * </ul>
 *
 * <p><strong>Endpoints (7 ta):</strong></p>
 * <ul>
 *   <li>POST / - Yangi yozuv yaratish</li>
 *   <li>GET /{entityId} - ID bo'yicha olish</li>
 *   <li>PUT /{entityId} - Yangilash</li>
 *   <li>DELETE /{entityId} - O'chirish (soft delete)</li>
 *   <li>GET / - Barcha ro'yxat (sahifalangan)</li>
 *   <li>GET /search - URL parametrlari bilan qidirish</li>
 *   <li>POST /search - JSON filter bilan qidirish</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "38.Inspeksiya administrative teacher", description = "Topish-1000 universitetlaridan PhD/DSc darajali o'qituvchilar hisoboti")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeEmployee1EntityController {

    private final AdministrativeEmployee1Repository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ENTITY_NAME = "hemishe_RIAdministrativeEmployee1";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // =====================================================
    // 1. CREATE (POST)
    // =====================================================

    @PostMapping
    @Operation(
        summary = "Yangi administrative employee1 yozuvi yaratish",
        description = """
            Yangi yozuv yaratadi - topish-1000 universitetlaridan PhD/DSc darajali o'qituvchi.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **CUBA Foreign Key Format:**
            - `_university`: {"id": "uuid-string"}
            - `_educationYear`: {"id": "uuid-string"}
            - `_employee`: {"id": "uuid-string"}
            - `_country`: {"id": "uuid-string"}
            - `_degree`: {"id": "uuid-string"}
            - `_rank`: {"id": "uuid-string"}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Administrative Employee1 ma'lumotlari (CUBA format)",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Namuna",
                        value = """
                            {
                                "_university": {"id": "00000000-0000-0000-0000-000000000001"},
                                "_educationYear": {"id": "00000000-0000-0000-0000-000000000002"},
                                "_employee": {"id": "00000000-0000-0000-0000-000000000003"},
                                "_country": {"id": "00000000-0000-0000-0000-000000000004"},
                                "foreignUniversity": "Harvard University",
                                "_degree": {"id": "00000000-0000-0000-0000-000000000005"},
                                "_rank": {"id": "00000000-0000-0000-0000-000000000006"},
                                "diplomaType": "PhD",
                                "diplomaSerialNumber": "AA1234567",
                                "diplomaDate": "2023-06-15",
                                "specialityCode": "01.01.01",
                                "specialityName": "Matematika",
                                "councilDate": "2023-05-20",
                                "councilNumber": "DSc/PhD-001"
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> body,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create hemishe_RIAdministrativeEmployee1: {}", body);

        AdministrativeEmployee1 entity = new AdministrativeEmployee1();
        updateEntityFromMap(entity, body);

        AdministrativeEmployee1 saved = repository.save(entity);

        // OLD-HEMIS: 201 Created qaytaradi
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    // =====================================================
    // 2. GET BY ID
    // =====================================================

    @GetMapping("/{entityId}")
    @Operation(
        summary = "Administrative employee1 yozuvini ID bo'yicha olish",
        description = "Berilgan UUID bo'yicha bitta yozuvni qaytaradi"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Entity UUID", example = "00000000-0000-0000-0000-000000000000")
            @PathVariable UUID entityId,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("GET hemishe_RIAdministrativeEmployee1 by id: {}", entityId);

        Optional<AdministrativeEmployee1> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // 3. UPDATE (PUT)
    // =====================================================

    @PutMapping("/{entityId}")
    @Operation(
        summary = "Administrative employee1 yozuvini yangilash",
        description = "Mavjud yozuvni yangilaydi"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Entity UUID")
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT hemishe_RIAdministrativeEmployee1 id: {}", entityId);

        Optional<AdministrativeEmployee1> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeEmployee1 entity = existingOpt.get();
        updateEntityFromMap(entity, body);

        AdministrativeEmployee1 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    // =====================================================
    // 4. DELETE (Soft Delete)
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Administrative employee1 yozuvini o'chirish",
        description = "Soft delete - delete_ts ga qiymat qo'yiladi"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    @Transactional
    public ResponseEntity<Void> delete(
            @Parameter(description = "Entity UUID")
            @PathVariable UUID entityId) {

        log.info("DELETE hemishe_RIAdministrativeEmployee1 id: {}", entityId);

        Optional<AdministrativeEmployee1> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Soft delete
        entity.get().setDeleteTs(LocalDateTime.now());
        repository.save(entity.get());

        // OLD-HEMIS: 200 OK qaytaradi (204 emas)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // 5. GET ALL (List)
    // =====================================================

    @GetMapping
    @Operation(
        summary = "Barcha administrative employee1 yozuvlarini olish",
        description = "Sahifalangan ro'yxat qaytaradi"
    )
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Boshlang'ich indeks", example = "0")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Maksimal yozuvlar soni", example = "50")
            @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("GET all hemishe_RIAdministrativeEmployee1 - offset: {}, limit: {}", offset, limit);

        List<AdministrativeEmployee1> allEntities = repository.findAll();

        int start = Math.min(offset, allEntities.size());
        int end = Math.min(start + limit, allEntities.size());

        List<AdministrativeEmployee1> paged = allEntities.subList(start, end);

        List<Map<String, Object>> result = paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // 6. SEARCH (GET)
    // =====================================================

    @GetMapping("/search")
    @Operation(
        summary = "Administrative employee1 yozuvlarini qidirish (GET)",
        description = "Filter parametri orqali qidirish"
    )
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @Parameter(description = "CUBA filter JSON")
            @RequestParam(required = false) String filter,
            @Parameter(description = "Boshlang'ich indeks")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Maksimal yozuvlar soni")
            @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("GET search hemishe_RIAdministrativeEmployee1 with filter: {}", filter);

        List<AdministrativeEmployee1> allEntities = repository.findAll();

        // Apply CUBA filter
        List<AdministrativeEmployee1> filtered = applyFilter(allEntities, filter);

        int start = Math.min(offset, filtered.size());
        int end = Math.min(start + limit, filtered.size());

        List<AdministrativeEmployee1> paged = filtered.subList(start, end);

        List<Map<String, Object>> result = paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // 7. SEARCH (POST)
    // =====================================================

    @PostMapping("/search")
    @Operation(
        summary = "Administrative employee1 yozuvlarini qidirish (POST)",
        description = "Request body orqali murakkab qidirish"
    )
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Boshlang'ich indeks")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Maksimal yozuvlar soni")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        // Body'dan limit/offset/filter o'qish
        int effectiveLimit = limit != null ? limit : 50;
        int effectiveOffset = offset != null ? offset : 0;
        String filterJson = null;

        if (body != null) {
            if (limit == null && body.containsKey("limit")) {
                Object bodyLimit = body.get("limit");
                if (bodyLimit instanceof Number) {
                    effectiveLimit = ((Number) bodyLimit).intValue();
                } else if (bodyLimit instanceof String) {
                    effectiveLimit = Integer.parseInt((String) bodyLimit);
                }
            }
            if (offset == null && body.containsKey("offset")) {
                Object bodyOffset = body.get("offset");
                if (bodyOffset instanceof Number) {
                    effectiveOffset = ((Number) bodyOffset).intValue();
                } else if (bodyOffset instanceof String) {
                    effectiveOffset = Integer.parseInt((String) bodyOffset);
                }
            }
            // Filter from body
            if (body.containsKey("filter")) {
                Object filterObj = body.get("filter");
                if (filterObj instanceof String) {
                    filterJson = (String) filterObj;
                } else if (filterObj instanceof Map) {
                    try {
                        filterJson = objectMapper.writeValueAsString(filterObj);
                    } catch (Exception e) {
                        log.warn("Cannot serialize filter: {}", e.getMessage());
                    }
                }
            }
        }

        log.debug("POST search hemishe_RIAdministrativeEmployee1 - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<AdministrativeEmployee1> allEntities = repository.findAll();

        // Apply CUBA filter
        List<AdministrativeEmployee1> filtered = applyFilter(allEntities, filterJson);

        int start = Math.min(effectiveOffset, filtered.size());
        int end = Math.min(start + effectiveLimit, filtered.size());

        List<AdministrativeEmployee1> paged = filtered.subList(start, end);

        List<Map<String, Object>> result = paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Entity dan Map ga konvertatsiya - OLD-HEMIS format
     *
     * OLD-HEMIS default view faqat oddiy maydonlarni qaytaradi:
     * - Foreign key references (_university, _employee, etc.) qaytarilmaydi
     * - Audit fields (createTs, createdBy, updateTs) qaytarilmaydi
     */
    private Map<String, Object> toMap(AdministrativeEmployee1 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // CUBA standard fields
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId().toString());

        // Simple fields only - OLD-HEMIS default view
        // Note: OLD-HEMIS does NOT return foreign keys or audit fields by default
        putIfNotNull(map, "diplomaSerialNumber", entity.getDiplomaSerialNumber(), returnNulls);
        putIfNotNull(map, "diplomaType", entity.getDiplomaType(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        putDateIfNotNull(map, "councilDate", entity.getCouncilDate(), returnNulls);
        putIfNotNull(map, "foreignUniversity", entity.getForeignUniversity(), returnNulls);
        putIfNotNull(map, "councilNumber", entity.getCouncilNumber(), returnNulls);
        putDateIfNotNull(map, "diplomaDate", entity.getDiplomaDate(), returnNulls);

        return map;
    }

    /**
     * Map dan Entity ga konvertatsiya - CUBA format qabul qiladi
     */
    private void updateEntityFromMap(AdministrativeEmployee1 entity, Map<String, Object> data) {
        // Foreign keys - CUBA format: {"id": "uuid-string"}
        // Note: university, educationYear, country, degree, rank are String in DB
        // Only employee is UUID
        if (data.containsKey("_university")) {
            entity.setUniversity(extractString(data.get("_university")));
        }
        if (data.containsKey("_educationYear")) {
            entity.setEducationYear(extractString(data.get("_educationYear")));
        }
        if (data.containsKey("_employee")) {
            entity.setEmployee(extractUuid(data.get("_employee")));
        }
        if (data.containsKey("_country")) {
            entity.setCountry(extractString(data.get("_country")));
        }
        if (data.containsKey("_degree")) {
            entity.setDegree(extractString(data.get("_degree")));
        }
        if (data.containsKey("_rank")) {
            entity.setRank(extractString(data.get("_rank")));
        }

        // Simple fields
        if (data.containsKey("foreignUniversity")) {
            entity.setForeignUniversity((String) data.get("foreignUniversity"));
        }
        if (data.containsKey("diplomaType")) {
            entity.setDiplomaType((String) data.get("diplomaType"));
        }
        if (data.containsKey("diplomaSerialNumber")) {
            entity.setDiplomaSerialNumber((String) data.get("diplomaSerialNumber"));
        }
        if (data.containsKey("diplomaDate")) {
            entity.setDiplomaDate(parseDate(data.get("diplomaDate")));
        }
        if (data.containsKey("specialityCode")) {
            entity.setSpecialityCode((String) data.get("specialityCode"));
        }
        if (data.containsKey("specialityName")) {
            entity.setSpecialityName((String) data.get("specialityName"));
        }
        if (data.containsKey("councilDate")) {
            entity.setCouncilDate(parseDate(data.get("councilDate")));
        }
        if (data.containsKey("councilNumber")) {
            entity.setCouncilNumber((String) data.get("councilNumber"));
        }
    }

    private String buildInstanceName(AdministrativeEmployee1 entity) {
        // CUBA format: com.company.hemishe.entity.RIAdministrativeEmployee1-UUID [detached]
        return "com.company.hemishe.entity.RIAdministrativeEmployee1-" + entity.getId() + " [detached]";
    }

    /**
     * CUBA format: {"id": "uuid-string"} - faqat Map qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id instanceof String str && !str.isEmpty()) {
                try {
                    return UUID.fromString(str);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private LocalDate parseDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof String str && !str.isEmpty()) {
            try {
                return LocalDate.parse(str, DATE_FORMAT);
            } catch (Exception e) {
                log.warn("Cannot parse date: {}", value);
                return null;
            }
        }
        return null;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    private void putDateIfNotNull(Map<String, Object> map, String key, LocalDate value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value.format(DATE_FORMAT));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    private void putDateTimeIfNotNull(Map<String, Object> map, String key, LocalDateTime value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value.format(DATETIME_FORMAT));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    private void putReference(Map<String, Object> map, String key, UUID value, Boolean returnNulls) {
        if (value != null) {
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("id", value.toString());
            map.put(key, ref);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    /**
     * String FK maydon uchun CUBA reference formati
     */
    private void putStringReference(Map<String, Object> map, String key, String value, Boolean returnNulls) {
        if (value != null && !value.isEmpty()) {
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("id", value);
            map.put(key, ref);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    /**
     * CUBA format: {"id": "string"} - String qiymat qaytaradi
     */
    @SuppressWarnings("unchecked")
    private String extractString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id instanceof String str && !str.isEmpty()) {
                return str;
            }
        }
        return null;
    }

    // =====================================================
    // CUBA FILTER LOGIC
    // =====================================================

    /**
     * CUBA filter formatini qo'llash
     * Format: {"conditions":[{"property":"field","operator":"=","value":"val"}]}
     */
    @SuppressWarnings("unchecked")
    private List<AdministrativeEmployee1> applyFilter(List<AdministrativeEmployee1> entities, String filterJson) {
        if (filterJson == null || filterJson.isBlank()) {
            return entities;
        }

        try {
            Map<String, Object> filter = objectMapper.readValue(filterJson, new TypeReference<>() {});
            Object conditionsObj = filter.get("conditions");

            if (!(conditionsObj instanceof List)) {
                return entities;
            }

            List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;

            return entities.stream()
                .filter(entity -> matchesAllConditions(entity, conditions))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Cannot parse CUBA filter: {}", e.getMessage());
            return entities;
        }
    }

    /**
     * Entity barcha shartlarga mos kelishini tekshirish
     */
    private boolean matchesAllConditions(AdministrativeEmployee1 entity, List<Map<String, Object>> conditions) {
        for (Map<String, Object> condition : conditions) {
            if (!matchesCondition(entity, condition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Bitta shartni tekshirish
     */
    private boolean matchesCondition(AdministrativeEmployee1 entity, Map<String, Object> condition) {
        String property = (String) condition.get("property");
        String operator = (String) condition.get("operator");
        Object filterValue = condition.get("value");

        if (property == null || operator == null) {
            return true;
        }

        Object entityValue = getPropertyValue(entity, property);

        return compareValues(entityValue, operator, filterValue);
    }

    /**
     * Entity maydon qiymatini olish
     */
    private Object getPropertyValue(AdministrativeEmployee1 entity, String property) {
        return switch (property) {
            case "id" -> entity.getId() != null ? entity.getId().toString() : null;
            case "diplomaSerialNumber" -> entity.getDiplomaSerialNumber();
            case "diplomaType" -> entity.getDiplomaType();
            case "diplomaDate" -> entity.getDiplomaDate() != null ? entity.getDiplomaDate().toString() : null;
            case "specialityCode" -> entity.getSpecialityCode();
            case "specialityName" -> entity.getSpecialityName();
            case "councilDate" -> entity.getCouncilDate() != null ? entity.getCouncilDate().toString() : null;
            case "councilNumber" -> entity.getCouncilNumber();
            case "foreignUniversity" -> entity.getForeignUniversity();
            case "version" -> entity.getVersion();
            case "_university", "university" -> entity.getUniversity();
            case "_educationYear", "educationYear" -> entity.getEducationYear();
            case "_employee", "employee" -> entity.getEmployee() != null ? entity.getEmployee().toString() : null;
            case "_country", "country" -> entity.getCountry();
            case "_degree", "degree" -> entity.getDegree();
            case "_rank", "rank" -> entity.getRank();
            default -> null;
        };
    }

    /**
     * Qiymatlarni solishtirish
     */
    private boolean compareValues(Object entityValue, String operator, Object filterValue) {
        // Handle null checks for special operators
        if ("isNull".equals(operator)) {
            return entityValue == null;
        }
        if ("notEmpty".equals(operator)) {
            if (entityValue == null) return false;
            if (entityValue instanceof String s) return !s.isEmpty();
            return true;
        }

        // For other operators, if filter value is null, skip
        if (filterValue == null) {
            return true;
        }

        String entityStr = entityValue != null ? entityValue.toString() : "";
        String filterStr = filterValue.toString();

        return switch (operator) {
            case "=", "equal" -> entityStr.equals(filterStr);
            case "<>", "!=", "notEqual" -> !entityStr.equals(filterStr);
            case "contains" -> entityStr.toLowerCase().contains(filterStr.toLowerCase());
            case "startsWith" -> entityStr.toLowerCase().startsWith(filterStr.toLowerCase());
            case "endsWith" -> entityStr.toLowerCase().endsWith(filterStr.toLowerCase());
            case ">" -> compareNumeric(entityStr, filterStr) > 0;
            case "<" -> compareNumeric(entityStr, filterStr) < 0;
            case ">=" -> compareNumeric(entityStr, filterStr) >= 0;
            case "<=" -> compareNumeric(entityStr, filterStr) <= 0;
            default -> true;
        };
    }

    /**
     * Raqamli solishtirish
     */
    private int compareNumeric(String a, String b) {
        try {
            return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }
}
