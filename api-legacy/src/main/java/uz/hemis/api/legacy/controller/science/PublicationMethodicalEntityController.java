package uz.hemis.api.legacy.controller.science;

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
import uz.hemis.domain.entity.PublicationMethodical;
import uz.hemis.service.legacy.science.ScienceEntityLegacyService;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PublicationMethodical Entity Controller (CUBA Pattern)
 * Tag 55: Publications - Methodical (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EPublicationMethodical
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_EPublicationMethodical
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EPublicationMethodical/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EPublicationMethodical/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EPublicationMethodical/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EPublicationMethodical/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EPublicationMethodical/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EPublicationMethodical           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EPublicationMethodical           - Create new
 */
@Tag(name = "24.Ilmiy uslubiy nashlar")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationMethodical")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationMethodicalEntityController {

    private final ScienceEntityLegacyService scienceService;
    private final CubaFilterHelper filterHelper;

    @GetMapping("/{entityId}")
    @Operation(summary = "Get PublicationMethodical by ID", description = "Returns a single PublicationMethodical by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET PublicationMethodical by id: {}", entityId);

        Optional<PublicationMethodical> entity = scienceService.findPublicationMethodicalById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(scienceService.toPublicationMethodicalMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update PublicationMethodical", description = "Updates an existing PublicationMethodical")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT PublicationMethodical id: {}", entityId);

        Optional<PublicationMethodical> existingOpt = scienceService.findPublicationMethodicalById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationMethodical entity = existingOpt.get();
        scienceService.updatePublicationMethodicalFromMap(entity, body);

        PublicationMethodical saved = scienceService.savePublicationMethodical(entity);
        return ResponseEntity.ok(scienceService.toPublicationMethodicalMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete PublicationMethodical", description = "Soft deletes an PublicationMethodical")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE PublicationMethodical id: {}", entityId);

        Optional<PublicationMethodical> entity = scienceService.findPublicationMethodicalById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        scienceService.softDeletePublicationMethodical(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search PublicationMethodical (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<PublicationMethodical> allEntities = scienceService.findAllPublicationMethodical();
        List<PublicationMethodical> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toPublicationMethodicalMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search PublicationMethodical (POST)", description = "Search using JSON filter")
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

        List<PublicationMethodical> allEntities = scienceService.findAllPublicationMethodical();
        List<PublicationMethodical> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toPublicationMethodicalMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all PublicationMethodical", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all PublicationMethodical - offset: {}, limit: {}", offset, limit);

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
        Page<PublicationMethodical> entityPage = scienceService.findAllPublicationMethodical(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> scienceService.toPublicationMethodicalMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create PublicationMethodical", description = "Creates a new PublicationMethodical")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create/upsert PublicationMethodical");

        // CUBA UPSERT: if body contains 'id' and entity exists, update instead of create
        if (body.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(body.get("id").toString());
                Optional<PublicationMethodical> existingOpt = scienceService.findPublicationMethodicalById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    PublicationMethodical existing = existingOpt.get();
                    scienceService.updatePublicationMethodicalFromMap(existing, body);
                    existing.setUpdateTs(java.time.LocalDateTime.now());
                    PublicationMethodical saved = scienceService.savePublicationMethodical(existing);
                    return ResponseEntity.ok(scienceService.toPublicationMethodicalMap(saved, returnNulls));
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        PublicationMethodical entity = new PublicationMethodical();
        scienceService.updatePublicationMethodicalFromMap(entity, body);
        PublicationMethodical saved = scienceService.savePublicationMethodical(entity);

        return ResponseEntity.ok(scienceService.toPublicationMethodicalMap(saved, returnNulls));
    }
}
