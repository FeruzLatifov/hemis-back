package uz.hemis.api.legacy.controller.university;

import uz.hemis.api.legacy.adapter.LegacyResponseHelper;

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
import uz.hemis.domain.entity.infrastructure.Laboratories;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Laboratories Entity Controller (CUBA Pattern)
 * Tag 66: Laboratories (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RLaboratories
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RLaboratories
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RLaboratories/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RLaboratories/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RLaboratories/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RLaboratories/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RLaboratories/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RLaboratories           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RLaboratories           - Create new
 */
@Tag(name = "65.Xo'jalik hisobot", description = "Laboratoriyalar bilan ta'minlanganlik")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RLaboratories")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class LaboratoriesEntityController {

    private final UniversityRefLegacyService universityRefService;
    private final CubaFilterHelper filterHelper;

    @GetMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Laboratories by ID", description = "Returns a single Laboratories by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET Laboratories by id: {}", entityId);

        Optional<Laboratories> entity = universityRefService.findLaboratoriesById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity hemishe_RLaboratories with id " + entityId + " not found"));
        }

        return ResponseEntity.ok(universityRefService.toLaboratoriesMap(entity.get(), returnNulls, view));
    }

    @PutMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update Laboratories", description = "Updates an existing Laboratories")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT Laboratories id: {}", entityId);

        Optional<Laboratories> existingOpt = universityRefService.findLaboratoriesById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Laboratories entity = existingOpt.get();
        universityRefService.updateLaboratoriesFromMap(entity, body);

        Laboratories saved = universityRefService.saveLaboratories(entity);
        return ResponseEntity.ok(universityRefService.toLaboratoriesMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete Laboratories", description = "Soft deletes an Laboratories")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE Laboratories id: {}", entityId);

        Optional<Laboratories> entity = universityRefService.findLaboratoriesById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        universityRefService.deleteLaboratories(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search Laboratories (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Laboratories> allEntities = universityRefService.findAllLaboratories();
        List<Laboratories> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> universityRefService.toLaboratoriesMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search Laboratories (POST)", description = "Search using JSON filter")
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

        List<Laboratories> allEntities = universityRefService.findAllLaboratories();
        List<Laboratories> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> universityRefService.toLaboratoriesMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all Laboratories", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all Laboratories - offset: {}, limit: {}", offset, limit);

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
        Page<Laboratories> entityPage = universityRefService.findAllLaboratories(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> universityRefService.toLaboratoriesMap(e, returnNulls, view))
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
    @Operation(summary = "Create Laboratories", description = "Creates a new Laboratories")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new Laboratories");

        Laboratories entity = new Laboratories();
        universityRefService.updateLaboratoriesFromMap(entity, body);
        Laboratories saved = universityRefService.saveLaboratories(entity);

        return ResponseEntity.ok(universityRefService.toLaboratoriesMap(saved, returnNulls));
    }
}
