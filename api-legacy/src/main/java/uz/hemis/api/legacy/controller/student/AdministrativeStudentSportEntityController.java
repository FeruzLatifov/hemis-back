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
import uz.hemis.domain.entity.AdministrativeStudentSport;
import uz.hemis.service.legacy.student.StudentEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeStudentSport Entity Controller (CUBA Pattern)
 * Entity: hemishe_RIAdministrativeStudentSport
 */
@Tag(name = "44.Inspeksiya administrative StudentSport - Talaba sport yutuqlari", description = "Talaba sport yutuqlari hisoboti")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeStudentSportEntityController {

    private final StudentEntityLegacyService studentService;

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeStudentSport by ID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeStudentSport by id: {}", entityId);

        Optional<AdministrativeStudentSport> entity = studentService.findAdministrativeStudentSportById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found", "details", "Entity hemishe_RIAdministrativeStudentSport with id " + entityId + " not found"));
        }

        return ResponseEntity.ok(studentService.toAdministrativeStudentSportMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeStudentSport")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeStudentSport id: {}", entityId);

        Optional<AdministrativeStudentSport> existingOpt = studentService.findAdministrativeStudentSportById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeStudentSport entity = existingOpt.get();
        studentService.updateAdministrativeStudentSportFromMap(entity, body);

        AdministrativeStudentSport saved = studentService.saveAdministrativeStudentSport(entity);
        return ResponseEntity.ok(studentService.toAdministrativeStudentSportMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeStudentSport")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeStudentSport id: {}", entityId);

        Optional<AdministrativeStudentSport> entity = studentService.findAdministrativeStudentSportById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeStudentSport with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        studentService.deleteAdministrativeStudentSport(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeStudentSport (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort) {

        log.debug("GET search AdministrativeStudentSport with filter: {}, offset: {}, limit: {}", filter, offset, limit);

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
        Page<AdministrativeStudentSport> entityPage = studentService.findAllAdministrativeStudentSport(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeStudentSportMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeStudentSport (POST)")
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

        log.debug("POST search AdministrativeStudentSport with body: {}, offset: {}, limit: {}", body, effectiveOffset, effectiveLimit);

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
        Page<AdministrativeStudentSport> entityPage = studentService.findAllAdministrativeStudentSport(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeStudentSportMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeStudentSport")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeStudentSport - offset: {}, limit: {}", offset, limit);

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
        Page<AdministrativeStudentSport> entityPage = studentService.findAllAdministrativeStudentSport(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> studentService.toAdministrativeStudentSportMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeStudentSport")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeStudentSport");

        AdministrativeStudentSport entity = new AdministrativeStudentSport();
        studentService.updateAdministrativeStudentSportFromMap(entity, body);
        AdministrativeStudentSport saved = studentService.saveAdministrativeStudentSport(entity);

        return ResponseEntity.ok(studentService.toAdministrativeStudentSportMap(saved, returnNulls));
    }
}
