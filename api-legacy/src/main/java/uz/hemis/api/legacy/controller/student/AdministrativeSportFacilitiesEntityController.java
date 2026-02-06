package uz.hemis.api.legacy.controller.student;

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
import uz.hemis.domain.entity.AdministrativeSportFacilities;
import uz.hemis.service.legacy.student.StudentEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeSportFacilities Entity Controller (CUBA Pattern)
 * Entity: hemishe_RIAdministrativeSportFacilities
 */
@Tag(name = "Administrative Reports - Sport Facilities")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeSportFacilitiesEntityController {

    private final StudentEntityLegacyService studentService;

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeSportFacilities by ID")
    public ResponseEntity<?> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeSportFacilities by id: {}", entityId);

        Optional<AdministrativeSportFacilities> entity = studentService.findAdministrativeSportFacilitiesById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeSportFacilities with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(studentService.toAdministrativeSportFacilitiesMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeSportFacilities")
    public ResponseEntity<?> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeSportFacilities id: {}", entityId);

        Optional<AdministrativeSportFacilities> existingOpt = studentService.findAdministrativeSportFacilitiesById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeSportFacilities with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        AdministrativeSportFacilities entity = existingOpt.get();
        studentService.updateAdministrativeSportFacilitiesFromMap(entity, body);

        AdministrativeSportFacilities saved = studentService.saveAdministrativeSportFacilities(entity);
        return ResponseEntity.ok(studentService.toAdministrativeSportFacilitiesMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeSportFacilities")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeSportFacilities id: {}", entityId);

        Optional<AdministrativeSportFacilities> entity = studentService.findAdministrativeSportFacilitiesById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeSportFacilities with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        studentService.deleteAdministrativeSportFacilities(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeSportFacilities (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort) {

        log.debug("GET search AdministrativeSportFacilities with filter: {}, offset: {}, limit: {}", filter, offset, limit);

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
        Page<AdministrativeSportFacilities> entityPage = studentService.findAllAdministrativeSportFacilities(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeSportFacilitiesMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeSportFacilities (POST)")
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

        log.debug("POST search AdministrativeSportFacilities with body: {}, offset: {}, limit: {}", body, effectiveOffset, effectiveLimit);

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
        Page<AdministrativeSportFacilities> entityPage = studentService.findAllAdministrativeSportFacilities(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeSportFacilitiesMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeSportFacilities")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeSportFacilities - offset: {}, limit: {}", offset, limit);

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
        Page<AdministrativeSportFacilities> entityPage = studentService.findAllAdministrativeSportFacilities(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeSportFacilitiesMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeSportFacilities")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeSportFacilities");

        AdministrativeSportFacilities entity = new AdministrativeSportFacilities();
        studentService.updateAdministrativeSportFacilitiesFromMap(entity, body);
        AdministrativeSportFacilities saved = studentService.saveAdministrativeSportFacilities(entity);

        return ResponseEntity.ok(studentService.toAdministrativeSportFacilitiesMap(saved, returnNulls));
    }
}
