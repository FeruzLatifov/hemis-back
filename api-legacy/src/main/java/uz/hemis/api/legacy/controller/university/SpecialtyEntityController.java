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
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.academic.Specialty;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Specialty Entity Controller (CUBA Pattern)
 * Entity: hemishe_EUniversitySpeciality
 *
 * CUBA Platform REST API compatible controller
 */
@Tag(name = "50.Mutaxassisliklar", description = "Mutaxassisliklar entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EUniversitySpeciality")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class SpecialtyEntityController {

    private final UniversityRefLegacyService universityRefService;
    private final CubaFilterHelper filterHelper;

    @PreAuthorize("hasAuthority('universities.view')")
    @GetMapping("/{entityId}")
    @Operation(summary = "Get specialty by ID", description = "Returns a single specialty by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET specialty by id: {}", entityId);
        Optional<Specialty> entity = universityRefService.findSpecialtyById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found", "details", "Entity hemishe_EUniversitySpeciality with id " + entityId + " not found"));
        }
        return ResponseEntity.ok(universityRefService.toSpecialtyMap(entity.get(), returnNulls));
    }

    @PreAuthorize("hasAuthority('universities.edit')")
    @PutMapping("/{entityId}")
    @Operation(summary = "Update specialty", description = "Updates an existing specialty")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT specialty id: {}", entityId);
        Optional<Specialty> existingOpt = universityRefService.findSpecialtyById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Specialty entity = existingOpt.get();
        universityRefService.updateSpecialtyFromMap(entity, body);
        Specialty saved = universityRefService.saveSpecialty(entity);
        return ResponseEntity.ok(universityRefService.toSpecialtyMap(saved, returnNulls));
    }

    @PreAuthorize("hasAuthority('universities.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete specialty", description = "Soft deletes a specialty")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE specialty id: {}", entityId);
        Optional<Specialty> entity = universityRefService.findSpecialtyById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        universityRefService.deleteSpecialty(entity.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('universities.view')")
    @GetMapping("/search")
    @Operation(summary = "Search specialties (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Specialty> allEntities = universityRefService.findAllSpecialty();
        List<Specialty> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> universityRefService.toSpecialtyMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('universities.view')")
    @PostMapping("/search")
    @Operation(summary = "Search specialties (POST)", description = "Search using JSON filter")
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

        List<Specialty> allEntities = universityRefService.findAllSpecialty();
        List<Specialty> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> universityRefService.toSpecialtyMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('universities.view')")
    @GetMapping
    @Operation(summary = "Get all specialties", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all specialties - offset: {}, limit: {}", offset, limit);

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
        Page<Specialty> entityPage = universityRefService.findAllSpecialty(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> universityRefService.toSpecialtyMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Create specialty (single object)
     *
     * OLD-HEMIS: Bitta object qabul qiladi
     */
    @PreAuthorize("hasAuthority('universities.edit')")
    @PostMapping
    @Operation(summary = "Create specialty", description = "Creates a new specialty (OLD-HEMIS format)")
    public ResponseEntity<?> create(
            @RequestBody Object body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new specialty");

        // OLD-HEMIS: Array yoki single object qabul qilishi mumkin
        if (body instanceof List) {
            // Array format - OLD-HEMIS batch create
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body;
            List<Map<String, Object>> results = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Specialty entity = universityRefService.findExistingSpecialtyOrNew(item);
                universityRefService.updateSpecialtyFromMap(entity, item);
                Specialty saved = universityRefService.saveSpecialty(entity);
                results.add(universityRefService.toSpecialtyCreateResponse(saved));
            }

            // OLD-HEMIS: 200 OK + Array response
            return ResponseEntity.ok(results);
        } else {
            // Single object
            @SuppressWarnings("unchecked")
            Map<String, Object> item = (Map<String, Object>) body;
            Specialty entity = universityRefService.findExistingSpecialtyOrNew(item);
            universityRefService.updateSpecialtyFromMap(entity, item);
            Specialty saved = universityRefService.saveSpecialty(entity);
            return ResponseEntity.ok(universityRefService.toSpecialtyCreateResponse(saved));
        }
    }
}
