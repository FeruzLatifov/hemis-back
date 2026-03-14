package uz.hemis.api.legacy.controller.academic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.AcademicMethodologicPublications;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AcademicMethodologicPublications Entity Controller (CUBA Pattern)
 * Entity: hemishe_RIAcademicMethodologicPublications
 * Uslubiy nashrlar haqida ma'lumot
 */
@Tag(name = "Academic Reports - Methodologic Publications")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AcademicMethodologicPublicationsEntityController {

    private final AcademicEntityLegacyService academicService;
    private static final String ENTITY_NAME = "hemishe_RIAcademicMethodologicPublications";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get by ID")
    public ResponseEntity<?> getById(@PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        Optional<AcademicMethodologicPublications> entity = academicService.findAcademicMethodologicPublicationsById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found",
                "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
        }
        return ResponseEntity.ok(academicService.toAcademicMethodologicPublicationsMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update")
    public ResponseEntity<?> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        Optional<AcademicMethodologicPublications> existingOpt = academicService.findAcademicMethodologicPublicationsById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found",
                "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
        }
        AcademicMethodologicPublications entity = existingOpt.get();
        academicService.updateAcademicMethodologicPublicationsFromMap(entity, body);
        return ResponseEntity.ok(academicService.toAcademicMethodologicPublicationsMap(
            academicService.saveAcademicMethodologicPublications(entity), returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        Optional<AcademicMethodologicPublications> entity = academicService.findAcademicMethodologicPublicationsById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found",
                "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
        }
        academicService.deleteAcademicMethodologicPublications(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search GET")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort) {
        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), limit, buildSort(sort));
        return ResponseEntity.ok(academicService.findAllAcademicMethodologicPublications(pageRequest).getContent().stream()
            .map(e -> academicService.toAcademicMethodologicPublicationsMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search POST")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort) {
        int effectiveLimit = limit != null ? limit : (body != null && body.get("limit") != null ? ((Number) body.get("limit")).intValue() : 50);
        int effectiveOffset = offset != null ? offset : (body != null && body.get("offset") != null ? ((Number) body.get("offset")).intValue() : 0);
        PageRequest pageRequest = PageRequest.of(effectiveOffset / Math.max(effectiveLimit, 1), effectiveLimit, buildSort(sort));
        return ResponseEntity.ok(academicService.findAllAcademicMethodologicPublications(pageRequest).getContent().stream()
            .map(e -> academicService.toAcademicMethodologicPublicationsMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), limit, buildSort(sort));
        var entityPage = academicService.findAllAcademicMethodologicPublications(pageRequest);
        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> academicService.toAcademicMethodologicPublicationsMap(e, returnNulls)).collect(Collectors.toList());
        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        AcademicMethodologicPublications entity = new AcademicMethodologicPublications();
        academicService.updateAcademicMethodologicPublicationsFromMap(entity, body);
        return ResponseEntity.ok(academicService.toAcademicMethodologicPublicationsMap(
            academicService.saveAcademicMethodologicPublications(entity), returnNulls));
    }

    private Sort buildSort(String sort) {
        if (sort == null || sort.isEmpty()) return Sort.unsorted();
        String[] parts = sort.split("-");
        return Sort.by(parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
    }
}
