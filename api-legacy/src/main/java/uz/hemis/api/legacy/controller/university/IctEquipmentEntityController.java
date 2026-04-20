package uz.hemis.api.legacy.controller.university;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.infrastructure.IctEquipment;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * IctEquipment Entity Controller (CUBA Pattern)
 * Tag 65: ICT Equipment (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIctEquipment
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RIctEquipment
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RIctEquipment/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RIctEquipment/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RIctEquipment/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RIctEquipment/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RIctEquipment/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RIctEquipment           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RIctEquipment           - Create new
 */
@Tag(name = "65.Xo'jalik hisobot", description = "AKT bilan jihozlanganlik")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIctEquipment")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class IctEquipmentEntityController {

    private final UniversityRefLegacyService universityRefService;
    private final CubaFilterHelper filterHelper;

    @GetMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get IctEquipment by ID", description = "Returns a single IctEquipment by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET IctEquipment by id: {}", entityId);

        Optional<IctEquipment> entity = universityRefService.findIctEquipmentById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found", "details", "Entity hemishe_RIctEquipment with id " + entityId + " not found"));
        }

        return ResponseEntity.ok(universityRefService.toIctEquipmentMap(entity.get(), returnNulls, view));
    }

    @PutMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update IctEquipment", description = "Updates an existing IctEquipment")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT IctEquipment id: {}", entityId);

        Optional<IctEquipment> existingOpt = universityRefService.findIctEquipmentById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IctEquipment entity = existingOpt.get();
        universityRefService.updateIctEquipmentFromMap(entity, body);

        IctEquipment saved = universityRefService.saveIctEquipment(entity);
        return ResponseEntity.ok(universityRefService.toIctEquipmentMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete IctEquipment", description = "Soft deletes an IctEquipment")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE IctEquipment id: {}", entityId);

        Optional<IctEquipment> entity = universityRefService.findIctEquipmentById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        universityRefService.deleteIctEquipment(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search IctEquipment (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<IctEquipment> allEntities = universityRefService.findAllIctEquipment();
        List<IctEquipment> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> universityRefService.toIctEquipmentMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search IctEquipment (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Offset") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Limit") @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        int effectiveOffset = filterHelper.extractInt(body, "offset", offset, 0);
        int effectiveLimit = filterHelper.extractInt(body, "limit", limit, 50);
        String filterJson = filterHelper.extractFilterFromBody(body);

        log.debug("POST search - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<IctEquipment> allEntities = universityRefService.findAllIctEquipment();
        List<IctEquipment> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> universityRefService.toIctEquipmentMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all IctEquipment", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all IctEquipment - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int safeLimit = Math.max(limit, 1);
        int page = offset / safeLimit;
        PageRequest pageRequest = PageRequest.of(page, safeLimit, sorting);
        Page<IctEquipment> entityPage = universityRefService.findAllIctEquipment(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> universityRefService.toIctEquipmentMap(e, returnNulls, view))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create IctEquipment", description = "Creates a new IctEquipment")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new IctEquipment");

        IctEquipment entity = new IctEquipment();
        universityRefService.updateIctEquipmentFromMap(entity, body);
        IctEquipment saved = universityRefService.saveIctEquipment(entity);

        return ResponseEntity.ok(universityRefService.toIctEquipmentMap(saved, returnNulls));
    }
}
