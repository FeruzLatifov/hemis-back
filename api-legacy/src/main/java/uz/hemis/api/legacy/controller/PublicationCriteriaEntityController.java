package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.PublicationCriteria;
import uz.hemis.domain.repository.PublicationCriteriaRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PublicationCriteria Entity Controller (CUBA Pattern)
 * Tag 26: Nashrlarni baholash mezonlari
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EPublicationCriteria
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EPublicationCriteria/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EPublicationCriteria/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EPublicationCriteria/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EPublicationCriteria/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EPublicationCriteria/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EPublicationCriteria           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EPublicationCriteria           - Create new
 */
@Tag(name = "26.Nashrlarni baholash mezonlari")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationCriteria")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationCriteriaEntityController {

    private final PublicationCriteriaRepository repository;
    private static final String ENTITY_NAME = "hemishe_EPublicationCriteria";

    @GetMapping("/{entityId}")
    @Operation(summary = "Baholash mezonini ID bo'yicha olish", description = "UUID bo'yicha bitta baholash mezonini qaytaradi")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET PublicationCriteria by id: {}", entityId);

        Optional<PublicationCriteria> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            // OLD-HEMIS format: error va details fieldlari
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Baholash mezonini yangilash", description = "Mavjud baholash mezonini yangilaydi")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT PublicationCriteria id: {}", entityId);

        Optional<PublicationCriteria> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            // OLD-HEMIS format: error va details fieldlari
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        PublicationCriteria entity = existingOpt.get();
        updateFromMap(entity, body);

        PublicationCriteria saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Baholash mezonini o'chirish", description = "Baholash mezonini soft delete qiladi")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID entityId) {
        log.debug("DELETE PublicationCriteria id: {}", entityId);

        Optional<PublicationCriteria> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            // OLD-HEMIS format: error va details fieldlari
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        repository.delete(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Baholash mezonlarini qidirish (GET)", description = "URL parametrlari orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search PublicationCriteria with filter: {}", filter);

        List<PublicationCriteria> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Baholash mezonlarini qidirish (POST)", description = "JSON filter orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search PublicationCriteria with filter: {}", filter);

        List<PublicationCriteria> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Barcha baholash mezonlarini olish", description = "Sahifalangan ro'yxatni qaytaradi")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Jami sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifa hajmi") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Saralash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all PublicationCriteria - offset: {}, limit: {}", offset, limit);

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
        Page<PublicationCriteria> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Yangi baholash mezoni yaratish", description = "Yangi baholash mezonini yaratadi")
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Baholash mezoni ma'lumotlari",
                content = @Content(
                    examples = @ExampleObject(
                        value = """
                            {
                              "_university": "401",
                              "_education_year": "2024",
                              "_publication_type_table": "hemishe_EPublicationScientific",
                              "markValue": 10,
                              "active": true
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new PublicationCriteria");

        PublicationCriteria entity = new PublicationCriteria();
        updateFromMap(entity, body);
        PublicationCriteria saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(PublicationCriteria entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name - use publication type table if available
        String instanceName = entity.getPublicationTypeTable() != null
            ? entity.getPublicationTypeTable()
            : "PublicationCriteria-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());
        map.put("version", entity.getVersion());

        // Fields in camelCase (OLD-HEMIS CUBA format)
        // OLD-HEMIS faqat: active, version, publicationTypeTable, markValue qaytaradi
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "publicationTypeTable", entity.getPublicationTypeTable(), returnNulls);
        putIfNotNull(map, "markValue", entity.getMarkValue(), returnNulls);

        // OLD-HEMIS format: audit fields yo'q

        return map;
    }

    private void updateFromMap(PublicationCriteria entity, Map<String, Object> map) {
        // uId - support both formats
        if (map.containsKey("u_id") || map.containsKey("uId")) {
            entity.setUId(extractInteger(map.getOrDefault("uId", map.get("u_id"))));
        }
        if (map.containsKey("_university")) {
            entity.setUniversity(extractString(map.get("_university")));
        }
        if (map.containsKey("_education_year")) {
            entity.setEducationYear(extractString(map.get("_education_year")));
        }
        if (map.containsKey("_publication_type_table")) {
            entity.setPublicationTypeTable(extractString(map.get("_publication_type_table")));
        }
        if (map.containsKey("_publication_methodical_type")) {
            entity.setPublicationMethodicalType(extractString(map.get("_publication_methodical_type")));
        }
        if (map.containsKey("_publication_scientific_type")) {
            entity.setPublicationScientificType(extractString(map.get("_publication_scientific_type")));
        }
        if (map.containsKey("_publication_property_type")) {
            entity.setPublicationPropertyType(extractString(map.get("_publication_property_type")));
        }
        // inPublicationDatabase - support both formats
        if (map.containsKey("_in_publication_database") || map.containsKey("inPublicationDatabase")) {
            entity.setInPublicationDatabase(extractInteger(map.getOrDefault("inPublicationDatabase", map.get("_in_publication_database"))));
        }
        // markValue - support both formats
        if (map.containsKey("mark_value") || map.containsKey("markValue")) {
            entity.setMarkValue(extractInteger(map.getOrDefault("markValue", map.get("mark_value"))));
        }
        if (map.containsKey("position")) {
            entity.setPosition(extractInteger(map.get("position")));
        }
        if (map.containsKey("active")) {
            entity.setActive(extractBoolean(map.get("active")));
        }
        if (map.containsKey("_translations")) {
            entity.setTranslations(extractString(map.get("_translations")));
        }
    }

    /**
     * Extracts string value from various formats:
     * - Simple string: "401" -> "401"
     * - Nested object (CUBA format): {"_entityName": "...", "id": "401"} -> "401"
     * - Nested object with code: {"code": "401"} -> "401"
     */
    private String extractString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) value;
            // Try to extract "id" first (CUBA nested entity format)
            Object id = nested.get("id");
            if (id != null) return extractString(id);
            // Try "code" as fallback (some reference entities use code)
            Object code = nested.get("code");
            if (code != null) return extractString(code);
        }
        return value.toString();
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

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
