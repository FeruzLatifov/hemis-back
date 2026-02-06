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
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.domain.entity.Faculty;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Faculty Entity Controller (CUBA Pattern)
 * CUBA Platform REST API compatible controller
 */
@Tag(name = "49.Fakultetlar", description = "Fakultetlar entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EFaculty")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class FacultyEntityController {

    private final UniversityRefLegacyService universityRefService;
    private final CubaFilterHelper filterHelper;

    @GetMapping("/{entityId}")
    @Operation(summary = "Get faculty by ID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET faculty by id: {}", entityId);
        Optional<Faculty> entity = universityRefService.findFacultyById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(universityRefService.toFacultyMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update faculty")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT faculty id: {}", entityId);
        Optional<Faculty> existingOpt = universityRefService.findFacultyById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Faculty entity = existingOpt.get();
        universityRefService.updateFacultyFromMap(entity, body);
        Faculty saved = universityRefService.saveFaculty(entity);
        return ResponseEntity.ok(universityRefService.toFacultyMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete faculty")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE faculty id: {}", entityId);
        Optional<Faculty> entity = universityRefService.findFacultyById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        universityRefService.deleteFaculty(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search faculties (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Faculty> allEntities = universityRefService.findAllFaculty();
        List<Faculty> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> universityRefService.toFacultyMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search faculties (POST)")
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

        List<Faculty> allEntities = universityRefService.findAllFaculty();
        List<Faculty> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> universityRefService.toFacultyMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all faculties")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all faculties - offset: {}, limit: {}", offset, limit);

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
        Page<Faculty> entityPage = universityRefService.findAllFaculty(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> universityRefService.toFacultyMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create faculty")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new faculty");
        Faculty entity = new Faculty();
        universityRefService.updateFacultyFromMap(entity, body);
        Faculty saved = universityRefService.saveFaculty(entity);
        return ResponseEntity.ok(universityRefService.toFacultyMap(saved, returnNulls));
    }
}
