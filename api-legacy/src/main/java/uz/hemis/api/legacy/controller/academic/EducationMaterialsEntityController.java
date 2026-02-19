package uz.hemis.api.legacy.controller.academic;

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
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.domain.entity.EducationMaterials;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * EducationMaterials Entity Controller (CUBA Pattern)
 * Tag 64: Education Materials (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_REducationMaterials
 */
@Tag(name = "65.Xo'jalik hisobot", description = "O'quv materiallari darajasi")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_REducationMaterials")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class EducationMaterialsEntityController {

    private final AcademicEntityLegacyService academicService;
    private final CubaFilterHelper filterHelper;

    @GetMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get EducationMaterials by ID", description = "Returns a single EducationMaterials by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET EducationMaterials by id: {}", entityId);

        Optional<EducationMaterials> entity = academicService.findEducationMaterialsById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(academicService.toEducationMaterialsMap(entity.get(), returnNulls, view));
    }

    @PutMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update EducationMaterials", description = "Updates an existing EducationMaterials")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT EducationMaterials id: {}", entityId);

        Optional<EducationMaterials> existingOpt = academicService.findEducationMaterialsById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EducationMaterials entity = existingOpt.get();
        academicService.updateEducationMaterialsFromMap(entity, body);

        EducationMaterials saved = academicService.saveEducationMaterials(entity);
        return ResponseEntity.ok(academicService.toEducationMaterialsMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete EducationMaterials", description = "Soft deletes an EducationMaterials")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE EducationMaterials id: {}", entityId);

        Optional<EducationMaterials> entity = academicService.findEducationMaterialsById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        academicService.deleteEducationMaterials(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search EducationMaterials (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<EducationMaterials> allEntities = academicService.findAllEducationMaterials();
        List<EducationMaterials> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> academicService.toEducationMaterialsMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search EducationMaterials (POST)", description = "Search using JSON filter")
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

        List<EducationMaterials> allEntities = academicService.findAllEducationMaterials();
        List<EducationMaterials> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> academicService.toEducationMaterialsMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all EducationMaterials", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all EducationMaterials - offset: {}, limit: {}", offset, limit);

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
        Page<EducationMaterials> entityPage = academicService.findAllEducationMaterials(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> academicService.toEducationMaterialsMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create EducationMaterials", description = "Creates a new EducationMaterials")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new EducationMaterials");

        EducationMaterials entity = new EducationMaterials();
        academicService.updateEducationMaterialsFromMap(entity, body);
        EducationMaterials saved = academicService.saveEducationMaterials(entity);

        return ResponseEntity.ok(academicService.toEducationMaterialsMap(saved, returnNulls));
    }
}
