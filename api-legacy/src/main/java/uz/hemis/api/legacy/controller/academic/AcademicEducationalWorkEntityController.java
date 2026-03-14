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
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.AcademicEducationalWork;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AcademicEducationalWork Entity Controller (CUBA Pattern)
 * Tag 47: Academic Reports - Educational Work (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAcademicEducationalWork
 *
 * O'quv ishlari haqida ma'lumot
 */
@Tag(name = "Academic Reports - Educational Work")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAcademicEducationalWork")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AcademicEducationalWorkEntityController {

    private final AcademicEntityLegacyService academicService;
    private static final String ENTITY_NAME = "hemishe_RIAcademicEducationalWork";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AcademicEducationalWork by ID", description = "Returns a single AcademicEducationalWork by UUID")
    public ResponseEntity<?> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AcademicEducationalWork by id: {}", entityId);

        Optional<AcademicEducationalWork> entity = academicService.findAcademicEducationalWorkById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAcademicEducationalWork with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(academicService.toAcademicEducationalWorkMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AcademicEducationalWork", description = "Updates an existing AcademicEducationalWork")
    public ResponseEntity<?> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AcademicEducationalWork id: {}", entityId);

        Optional<AcademicEducationalWork> existingOpt = academicService.findAcademicEducationalWorkById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAcademicEducationalWork with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        AcademicEducationalWork entity = existingOpt.get();
        academicService.updateAcademicEducationalWorkFromMap(entity, body);

        AcademicEducationalWork saved = academicService.saveAcademicEducationalWork(entity);
        return ResponseEntity.ok(academicService.toAcademicEducationalWorkMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AcademicEducationalWork", description = "Soft deletes an AcademicEducationalWork")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AcademicEducationalWork id: {}", entityId);

        Optional<AcademicEducationalWork> entity = academicService.findAcademicEducationalWorkById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAcademicEducationalWork with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        academicService.deleteAcademicEducationalWork(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AcademicEducationalWork (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort) {

        log.debug("GET search AcademicEducationalWork with filter: {}, offset: {}, limit: {}", filter, offset, limit);

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
        Page<AcademicEducationalWork> entityPage = academicService.findAllAcademicEducationalWork(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> academicService.toAcademicEducationalWorkMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AcademicEducationalWork (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort) {

        int effectiveLimit = limit != null ? limit :
            (body != null && body.get("limit") != null ? ((Number) body.get("limit")).intValue() : 50);
        int effectiveOffset = offset != null ? offset :
            (body != null && body.get("offset") != null ? ((Number) body.get("offset")).intValue() : 0);

        log.debug("POST search AcademicEducationalWork with body: {}, offset: {}, limit: {}", body, effectiveOffset, effectiveLimit);

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
        Page<AcademicEducationalWork> entityPage = academicService.findAllAcademicEducationalWork(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> academicService.toAcademicEducationalWorkMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AcademicEducationalWork", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AcademicEducationalWork - offset: {}, limit: {}", offset, limit);

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
        Page<AcademicEducationalWork> entityPage = academicService.findAllAcademicEducationalWork(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> academicService.toAcademicEducationalWorkMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create AcademicEducationalWork", description = "Creates a new AcademicEducationalWork")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AcademicEducationalWork");

        AcademicEducationalWork entity = new AcademicEducationalWork();
        academicService.updateAcademicEducationalWorkFromMap(entity, body);
        AcademicEducationalWork saved = academicService.saveAcademicEducationalWork(entity);

        return ResponseEntity.ok(academicService.toAcademicEducationalWorkMap(saved, returnNulls));
    }
}
