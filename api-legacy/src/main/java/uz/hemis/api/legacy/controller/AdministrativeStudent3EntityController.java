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
import uz.hemis.domain.entity.AdministrativeStudent3;
import uz.hemis.domain.repository.AdministrativeStudent3Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeStudent3 Entity Controller (CUBA Pattern)
 * Tag 51: Administrative Reports - Students (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAdministrativeStudent3
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RIAdministrativeStudent3
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent3           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeStudent3           - Create new
 */
@Tag(name = "42.Inspeksiya administrative student3 - Bitiruvchilar band bo'lishi", description = "Bitiruvchilar bandligi hisoboti")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeStudent3")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeStudent3EntityController {

    private final AdministrativeStudent3Repository repository;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeStudent3";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeStudent3 by ID", description = "Returns a single AdministrativeStudent3 by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeStudent3 by id: {}", entityId);

        Optional<AdministrativeStudent3> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeStudent3", description = "Updates an existing AdministrativeStudent3")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeStudent3 id: {}", entityId);

        Optional<AdministrativeStudent3> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeStudent3 entity = existingOpt.get();
        updateFromMap(entity, body);

        AdministrativeStudent3 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeStudent3", description = "Soft deletes an AdministrativeStudent3")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeStudent3 id: {}", entityId);

        Optional<AdministrativeStudent3> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeStudent3 with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        repository.delete(entity.get());
        // OLD-HEMIS returns 200 OK with empty response (not 204)
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeStudent3 (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort) {

        log.debug("GET search AdministrativeStudent3 with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<AdministrativeStudent3> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeStudent3 (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort) {

        // CUBA format: limit/offset query param yoki body ichida kelishi mumkin
        int effectiveLimit = limit != null ? limit :
            (body != null && body.get("limit") != null ? ((Number) body.get("limit")).intValue() : 50);
        int effectiveOffset = offset != null ? offset :
            (body != null && body.get("offset") != null ? ((Number) body.get("offset")).intValue() : 0);

        log.debug("POST search AdministrativeStudent3 with body: {}, offset: {}, limit: {}", body, effectiveOffset, effectiveLimit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = effectiveOffset / Math.max(effectiveLimit, 1);
        PageRequest pageRequest = PageRequest.of(page, effectiveLimit, sorting);
        Page<AdministrativeStudent3> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeStudent3", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeStudent3 - offset: {}, limit: {}", offset, limit);

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
        Page<AdministrativeStudent3> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeStudent3", description = "Creates a new AdministrativeStudent3")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeStudent3");

        AdministrativeStudent3 entity = new AdministrativeStudent3();
        updateFromMap(entity, body);
        AdministrativeStudent3 saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(AdministrativeStudent3 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS CUBA format - faqat shu maydonlar qaytariladi
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId());

        // Entity-specific fields (OLD-HEMIS tartibida)
        putIfNotNull(map, "mastersUniversityName", entity.getMastersUniversityName(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "company", entity.getCompany(), returnNulls);
        putIfNotNull(map, "position", entity.getPosition(), returnNulls);

        return map;
    }

    private String buildInstanceName(AdministrativeStudent3 entity) {
        // OLD-HEMIS CUBA format: com.company.hemishe.entity.RIAdministrativeStudent3-UUID [detached]
        return "com.company.hemishe.entity.RIAdministrativeStudent3-" + entity.getId() + " [detached]";
    }

    private void updateFromMap(AdministrativeStudent3 entity, Map<String, Object> body) {
        // OLD-HEMIS CUBA format - {"id": "..."} va camelCase field nomlari

        // university: {"id": "UUID"}
        if (body.containsKey("university")) {
            entity.setUniversity(extractUuid(body.get("university")));
        }

        // educationYear: {"id": "UUID"}
        if (body.containsKey("educationYear")) {
            entity.setEducationYear(extractUuid(body.get("educationYear")));
        }

        // student: {"id": "UUID"}
        if (body.containsKey("student")) {
            entity.setStudent(extractUuid(body.get("student")));
        }

        // educationType: {"id": "UUID"}
        if (body.containsKey("educationType")) {
            entity.setEducationType(extractUuid(body.get("educationType")));
        }

        // company - oddiy string
        if (body.containsKey("company")) {
            Object val = body.get("company");
            entity.setCompany(val != null ? val.toString() : null);
        }

        // position - oddiy string
        if (body.containsKey("position")) {
            Object val = body.get("position");
            entity.setPosition(val != null ? val.toString() : null);
        }

        // mastersUniversityName - oddiy string
        if (body.containsKey("mastersUniversityName")) {
            Object val = body.get("mastersUniversityName");
            entity.setMastersUniversityName(val != null ? val.toString() : null);
        }
    }

    /**
     * OLD-HEMIS CUBA format: {"id": "UUID"} yoki to'g'ridan-to'g'ri string
     * Entity FK lar uchun - UUID ga convert qiladi
     */
    @SuppressWarnings("unchecked")
    private java.util.UUID extractUuid(Object value) {
        if (value == null) return null;
        String strValue = null;
        if (value instanceof String str) {
            strValue = str.isEmpty() ? null : str;
        } else if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) {
                strValue = id.toString();
            } else {
                Object code = nested.get("code");
                if (code != null) {
                    strValue = code.toString();
                }
            }
        } else {
            strValue = value.toString();
        }

        if (strValue == null || strValue.isEmpty()) return null;
        try {
            return java.util.UUID.fromString(strValue);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", strValue);
            return null;
        }
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
