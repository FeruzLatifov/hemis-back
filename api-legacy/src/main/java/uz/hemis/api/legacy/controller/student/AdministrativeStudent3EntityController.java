package uz.hemis.api.legacy.controller.student;

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
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.student.AdministrativeStudent3;
import uz.hemis.service.legacy.student.StudentEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * AdministrativeStudent3 Entity Controller (CUBA Pattern)
 * Entity: hemishe_RIAdministrativeStudent3
 */
@Tag(name = "42.Inspeksiya administrative student3 - Bitiruvchilar band bo'lishi", description = "Bitiruvchilar bandligi hisoboti")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeStudent3")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeStudent3EntityController {

    private final StudentEntityLegacyService studentService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeStudent3 by ID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeStudent3 by id: {}", entityId);

        Optional<AdministrativeStudent3> entity = studentService.findAdministrativeStudent3ById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity hemishe_RIAdministrativeStudent3 with id " + entityId + " not found"));
        }

        return ResponseEntity.ok(studentService.toAdministrativeStudent3Map(entity.get(), returnNulls, view));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeStudent3")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeStudent3 id: {}", entityId);

        Optional<AdministrativeStudent3> existingOpt = studentService.findAdministrativeStudent3ById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeStudent3 entity = existingOpt.get();
        studentService.updateAdministrativeStudent3FromMap(entity, body);

        AdministrativeStudent3 saved = studentService.saveAdministrativeStudent3(entity);
        return ResponseEntity.ok(studentService.toAdministrativeStudent3Map(saved, returnNulls));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeStudent3")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeStudent3 id: {}", entityId);

        Optional<AdministrativeStudent3> entity = studentService.findAdministrativeStudent3ById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeStudent3 with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        studentService.deleteAdministrativeStudent3(entity.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeStudent3 (GET)")
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
        Page<AdministrativeStudent3> entityPage = studentService.findAllAdministrativeStudent3(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeStudent3Map(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeStudent3 (POST)")
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
        Page<AdministrativeStudent3> entityPage = studentService.findAllAdministrativeStudent3(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeStudent3Map(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Get all AdministrativeStudent3")
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

        int safeLimit = Math.max(limit, 1);
        int page = offset / safeLimit;
        PageRequest pageRequest = PageRequest.of(page, safeLimit, sorting);
        Page<AdministrativeStudent3> entityPage = studentService.findAllAdministrativeStudent3(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeStudent3Map(e, returnNulls, view))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(summary = "Create AdministrativeStudent3")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeStudent3");

        AdministrativeStudent3 entity = new AdministrativeStudent3();
        studentService.updateAdministrativeStudent3FromMap(entity, body);
        AdministrativeStudent3 saved = studentService.saveAdministrativeStudent3(entity);

        return ResponseEntity.ok(studentService.toAdministrativeStudent3Map(saved, returnNulls));
    }
}
