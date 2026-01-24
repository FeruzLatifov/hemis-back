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
import uz.hemis.domain.entity.AdministrativeEmployee2;
import uz.hemis.domain.repository.AdministrativeEmployee2Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeEmployee2 Entity Controller (CUBA Pattern)
 * Tag 52: Administrative Reports - Employees (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAdministrativeEmployee2
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2           - Create new
 */
@Tag(name = "Administrative Reports - Employees")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeEmployee2EntityController {

    private final AdministrativeEmployee2Repository repository;
    private final ObjectMapper objectMapper;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeEmployee2";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeEmployee2 by ID", description = "Returns a single AdministrativeEmployee2 by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeEmployee2 by id: {}", entityId);

        Optional<AdministrativeEmployee2> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeEmployee2", description = "Updates an existing AdministrativeEmployee2")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeEmployee2 id: {}", entityId);

        Optional<AdministrativeEmployee2> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeEmployee2 entity = existingOpt.get();
        updateFromMap(entity, body);

        AdministrativeEmployee2 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeEmployee2", description = "Soft deletes an AdministrativeEmployee2")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeEmployee2 id: {}", entityId);

        Optional<AdministrativeEmployee2> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            // OLD-HEMIS format: 404 with CUBA error structure
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_RIAdministrativeEmployee2 with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        repository.delete(entity.get());
        // OLD-HEMIS: 200 OK qaytaradi (204 emas)
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeEmployee2 (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search AdministrativeEmployee2 with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<AdministrativeEmployee2> allEntities = repository.findAll();

        // Apply CUBA filter
        List<AdministrativeEmployee2> filtered = applyFilter(allEntities, filter);

        int start = Math.min(offset, filtered.size());
        int end = Math.min(start + limit, filtered.size());

        List<AdministrativeEmployee2> paged = filtered.subList(start, end);

        return ResponseEntity.ok(paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeEmployee2 (POST)", description = "Search using JSON filter")
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

        log.debug("POST search AdministrativeEmployee2 - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<AdministrativeEmployee2> allEntities = repository.findAll();

        // Apply CUBA filter
        List<AdministrativeEmployee2> filtered = applyFilter(allEntities, filterJson);

        int start = Math.min(effectiveOffset, filtered.size());
        int end = Math.min(start + effectiveLimit, filtered.size());

        List<AdministrativeEmployee2> paged = filtered.subList(start, end);

        return ResponseEntity.ok(paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeEmployee2", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeEmployee2 - offset: {}, limit: {}", offset, limit);

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
        Page<AdministrativeEmployee2> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeEmployee2", description = "Creates a new AdministrativeEmployee2")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeEmployee2");

        AdministrativeEmployee2 entity = new AdministrativeEmployee2();
        updateFromMap(entity, body);
        AdministrativeEmployee2 saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Entity dan Map ga konvertatsiya - OLD-HEMIS format (camelCase)
     */
    private Map<String, Object> toMap(AdministrativeEmployee2 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // CUBA format instance name
        String instanceName = "com.company.hemishe.entity.RIAdministrativeEmployee2-" + entity.getId() + " [detached]";
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId().toString());

        // Simple fields - camelCase (OLD-HEMIS format)
        putIfNotNull(map, "foreignUniversity", entity.getForeignUniversity(), returnNulls);
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "trainingTypeName", entity.getTrainingTypeName(), returnNulls);
        putIfNotNull(map, "trainingContract", entity.getTrainingContract(), returnNulls);
        putDateIfNotNull(map, "trainingDateStart", entity.getTrainingDateStart(), returnNulls);
        putDateIfNotNull(map, "trainingDateEnd", entity.getTrainingDateEnd(), returnNulls);
        putIfNotNull(map, "year", entity.getYear(), returnNulls);
        putIfNotNull(map, "subject", entity.getSubject(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        return map;
    }

    /**
     * Map dan Entity ga konvertatsiya - CUBA format qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private void updateFromMap(AdministrativeEmployee2 entity, Map<String, Object> map) {
        // Foreign keys - CUBA format: {"id": "string"} yoki direct value (String sifatida saqlanadi)
        if (map.containsKey("university") || map.containsKey("_university")) {
            entity.setUniversity(extractString(map.getOrDefault("university", map.get("_university"))));
        }
        if (map.containsKey("educationYear") || map.containsKey("_educationYear")) {
            entity.setEducationYear(extractString(map.getOrDefault("educationYear", map.get("_educationYear"))));
        }
        if (map.containsKey("employee") || map.containsKey("_employee")) {
            entity.setEmployee(extractString(map.getOrDefault("employee", map.get("_employee"))));
        }
        if (map.containsKey("country") || map.containsKey("_country")) {
            entity.setCountry(extractString(map.getOrDefault("country", map.get("_country"))));
        }
        if (map.containsKey("internshipForm") || map.containsKey("_internshipForm")) {
            entity.setInternshipForm(extractString(map.getOrDefault("internshipForm", map.get("_internshipForm"))));
        }
        if (map.containsKey("internshipType") || map.containsKey("_internshipType")) {
            entity.setInternshipType(extractString(map.getOrDefault("internshipType", map.get("_internshipType"))));
        }

        // Simple fields
        if (map.containsKey("foreignUniversity")) {
            entity.setForeignUniversity((String) map.get("foreignUniversity"));
        }
        if (map.containsKey("specialityCode")) {
            entity.setSpecialityCode((String) map.get("specialityCode"));
        }
        if (map.containsKey("specialityName")) {
            entity.setSpecialityName((String) map.get("specialityName"));
        }
        if (map.containsKey("trainingTypeName")) {
            entity.setTrainingTypeName((String) map.get("trainingTypeName"));
        }
        if (map.containsKey("trainingContract")) {
            entity.setTrainingContract((String) map.get("trainingContract"));
        }
        if (map.containsKey("trainingDateStart")) {
            entity.setTrainingDateStart(parseDate(map.get("trainingDateStart")));
        }
        if (map.containsKey("trainingDateEnd")) {
            entity.setTrainingDateEnd(parseDate(map.get("trainingDateEnd")));
        }
        if (map.containsKey("year")) {
            entity.setYear((String) map.get("year"));
        }
        if (map.containsKey("subject")) {
            entity.setSubject((String) map.get("subject"));
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
    private List<AdministrativeEmployee2> applyFilter(List<AdministrativeEmployee2> entities, String filterJson) {
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
    private boolean matchesAllConditions(AdministrativeEmployee2 entity, List<Map<String, Object>> conditions) {
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
    private boolean matchesCondition(AdministrativeEmployee2 entity, Map<String, Object> condition) {
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
    private Object getPropertyValue(AdministrativeEmployee2 entity, String property) {
        return switch (property) {
            case "id" -> entity.getId() != null ? entity.getId().toString() : null;
            case "foreignUniversity" -> entity.getForeignUniversity();
            case "specialityCode" -> entity.getSpecialityCode();
            case "specialityName" -> entity.getSpecialityName();
            case "trainingTypeName" -> entity.getTrainingTypeName();
            case "trainingContract" -> entity.getTrainingContract();
            case "trainingDateStart" -> entity.getTrainingDateStart() != null ? entity.getTrainingDateStart().toString() : null;
            case "trainingDateEnd" -> entity.getTrainingDateEnd() != null ? entity.getTrainingDateEnd().toString() : null;
            case "year" -> entity.getYear();
            case "subject" -> entity.getSubject();
            case "version" -> entity.getVersion();
            case "_university", "university" -> entity.getUniversity();
            case "_educationYear", "educationYear" -> entity.getEducationYear();
            case "_employee", "employee" -> entity.getEmployee();
            case "_country", "country" -> entity.getCountry();
            case "_internshipForm", "internshipForm" -> entity.getInternshipForm();
            case "_internshipType", "internshipType" -> entity.getInternshipType();
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
