package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.AdministrativeEmployee3;
import uz.hemis.domain.repository.AdministrativeEmployee3Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeEmployee3 Entity Controller (CUBA Pattern)
 * Tag 52: Administrative Reports - Employees (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAdministrativeEmployee3
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3           - Create new
 */
@Tag(name = "40.OTMda xorijiy o'qituvchilar", description = "OTMda faoliyat olib borayotgan xorijiy o'qituvchilar")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeEmployee3EntityController {

    private final AdministrativeEmployee3Repository repository;
    private final ObjectMapper objectMapper;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeEmployee3";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeEmployee3 by ID", description = "Returns a single AdministrativeEmployee3 by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeEmployee3 by id: {}", entityId);

        Optional<AdministrativeEmployee3> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeEmployee3", description = "Updates an existing AdministrativeEmployee3")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeEmployee3 id: {}", entityId);

        Optional<AdministrativeEmployee3> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeEmployee3 entity = existingOpt.get();
        updateFromMap(entity, body);

        AdministrativeEmployee3 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeEmployee3", description = "Soft deletes an AdministrativeEmployee3")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeEmployee3 id: {}", entityId);

        Optional<AdministrativeEmployee3> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            // OLD-HEMIS format: 404 with CUBA error structure
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_RIAdministrativeEmployee3 with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        repository.delete(entity.get());
        // OLD-HEMIS: 200 OK qaytaradi (204 emas)
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeEmployee3 (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search AdministrativeEmployee3 with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<AdministrativeEmployee3> allEntities = repository.findAll();

        // Apply CUBA filter
        List<AdministrativeEmployee3> filtered = applyFilter(allEntities, filter);

        int start = Math.min(offset, filtered.size());
        int end = Math.min(start + limit, filtered.size());

        List<AdministrativeEmployee3> paged = filtered.subList(start, end);

        return ResponseEntity.ok(paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeEmployee3 (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Offset for pagination") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

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
            // Filter from body - conditions to'g'ridan-to'g'ri kelishi mumkin
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
            } else if (body.containsKey("conditions")) {
                // Body o'zi filter sifatida kelishi mumkin
                try {
                    filterJson = objectMapper.writeValueAsString(body);
                } catch (Exception e) {
                    log.warn("Cannot serialize body as filter: {}", e.getMessage());
                }
            }
        }

        log.debug("POST search AdministrativeEmployee3 - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<AdministrativeEmployee3> allEntities = repository.findAll();

        // Apply CUBA filter
        List<AdministrativeEmployee3> filtered = applyFilter(allEntities, filterJson);

        int start = Math.min(effectiveOffset, filtered.size());
        int end = Math.min(start + effectiveLimit, filtered.size());

        List<AdministrativeEmployee3> paged = filtered.subList(start, end);

        return ResponseEntity.ok(paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeEmployee3", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeEmployee3 - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<AdministrativeEmployee3> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeEmployee3", description = "Creates a new AdministrativeEmployee3")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeEmployee3");

        AdministrativeEmployee3 entity = new AdministrativeEmployee3();
        updateFromMap(entity, body);
        AdministrativeEmployee3 saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Entity dan Map ga konvertatsiya - OLD-HEMIS format (camelCase)
     */
    private Map<String, Object> toMap(AdministrativeEmployee3 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // CUBA format instance name
        String instanceName = "com.company.hemishe.entity.RIAdministrativeEmployee3-" + entity.getId() + " [detached]";
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId().toString());

        // Simple fields - camelCase (OLD-HEMIS format)
        putIfNotNull(map, "fullname", entity.getFullname(), returnNulls);
        putIfNotNull(map, "workPlace", entity.getWorkPlace(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "subject", entity.getSubject(), returnNulls);
        putIfNotNull(map, "contractData", entity.getContractData(), returnNulls);
        putDateIfNotNull(map, "arrivalDate", entity.getArrivalDate(), returnNulls);
        putDateIfNotNull(map, "departureDate", entity.getDepartureDate(), returnNulls);
        putIfNotNull(map, "lessonTime", entity.getLessonTime(), returnNulls);
        putIfNotNull(map, "year", entity.getYear(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        return map;
    }

    /**
     * Map dan Entity ga konvertatsiya - CUBA format qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private void updateFromMap(AdministrativeEmployee3 entity, Map<String, Object> map) {
        // Foreign keys - CUBA format: {"id": "string"} yoki direct value (String sifatida saqlanadi)
        if (map.containsKey("university") || map.containsKey("_university")) {
            entity.setUniversity(extractString(map.getOrDefault("university", map.get("_university"))));
        }
        if (map.containsKey("educationYear") || map.containsKey("_educationYear")) {
            entity.setEducationYear(extractString(map.getOrDefault("educationYear", map.get("_educationYear"))));
        }
        if (map.containsKey("country") || map.containsKey("_country")) {
            entity.setCountry(extractString(map.getOrDefault("country", map.get("_country"))));
        }
        if (map.containsKey("employee") || map.containsKey("_employee")) {
            entity.setEmployee(extractUuid(map.getOrDefault("employee", map.get("_employee"))));
        }
        if (map.containsKey("employeeForm") || map.containsKey("_employeeForm")) {
            entity.setEmployeeForm(extractString(map.getOrDefault("employeeForm", map.get("_employeeForm"))));
        }
        if (map.containsKey("condutionForm") || map.containsKey("_condutionForm")) {
            entity.setCondutionForm(extractString(map.getOrDefault("condutionForm", map.get("_condutionForm"))));
        }

        // Simple fields
        if (map.containsKey("fullname")) {
            entity.setFullname((String) map.get("fullname"));
        }
        if (map.containsKey("workPlace")) {
            entity.setWorkPlace((String) map.get("workPlace"));
        }
        if (map.containsKey("specialityName")) {
            entity.setSpecialityName((String) map.get("specialityName"));
        }
        if (map.containsKey("subject")) {
            entity.setSubject((String) map.get("subject"));
        }
        if (map.containsKey("contractData")) {
            entity.setContractData((String) map.get("contractData"));
        }
        if (map.containsKey("arrivalDate")) {
            entity.setArrivalDate(parseDate(map.get("arrivalDate")));
        }
        if (map.containsKey("departureDate")) {
            entity.setDepartureDate(parseDate(map.get("departureDate")));
        }
        if (map.containsKey("lessonTime")) {
            Object lt = map.get("lessonTime");
            if (lt instanceof Number) {
                entity.setLessonTime(((Number) lt).intValue());
            } else if (lt instanceof String str && !str.isEmpty()) {
                entity.setLessonTime(Integer.parseInt(str));
            }
        }
        if (map.containsKey("year")) {
            entity.setYear((String) map.get("year"));
        }
    }

    /**
     * CUBA format: {"id": "string"} yoki direct String
     */
    @SuppressWarnings("unchecked")
    private String extractString(Object value) {
        if (value == null) return null;
        if (value instanceof String str) return str.isEmpty() ? null : str;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id instanceof String str && !str.isEmpty()) {
                return str;
            }
        }
        return null;
    }

    /**
     * CUBA format: {"id": "uuid-string"} yoki direct UUID string
     */
    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        String str = null;
        if (value == null) return null;
        if (value instanceof String s) {
            str = s.isEmpty() ? null : s;
        } else if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id instanceof String s && !s.isEmpty()) {
                str = s;
            }
        }
        if (str != null) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                log.warn("Cannot parse UUID: {}", str);
                return null;
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

    // =====================================================
    // CUBA FILTER METHODS
    // =====================================================

    /**
     * CUBA filter ni qo'llash
     */
    @SuppressWarnings("unchecked")
    private List<AdministrativeEmployee3> applyFilter(List<AdministrativeEmployee3> entities, String filterJson) {
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
    private boolean matchesAllConditions(AdministrativeEmployee3 entity, List<Map<String, Object>> conditions) {
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
    private boolean matchesCondition(AdministrativeEmployee3 entity, Map<String, Object> condition) {
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
    private Object getPropertyValue(AdministrativeEmployee3 entity, String property) {
        return switch (property) {
            case "id" -> entity.getId() != null ? entity.getId().toString() : null;
            case "fullname" -> entity.getFullname();
            case "workPlace" -> entity.getWorkPlace();
            case "specialityName" -> entity.getSpecialityName();
            case "subject" -> entity.getSubject();
            case "contractData" -> entity.getContractData();
            case "arrivalDate" -> entity.getArrivalDate() != null ? entity.getArrivalDate().toString() : null;
            case "departureDate" -> entity.getDepartureDate() != null ? entity.getDepartureDate().toString() : null;
            case "lessonTime" -> entity.getLessonTime();
            case "year" -> entity.getYear();
            case "version" -> entity.getVersion();
            case "_university", "university" -> entity.getUniversity();
            case "_educationYear", "educationYear" -> entity.getEducationYear();
            case "_employee", "employee" -> entity.getEmployee() != null ? entity.getEmployee().toString() : null;
            case "_country", "country" -> entity.getCountry();
            case "_employeeForm", "employeeForm" -> entity.getEmployeeForm();
            case "_condutionForm", "condutionForm" -> entity.getCondutionForm();
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
