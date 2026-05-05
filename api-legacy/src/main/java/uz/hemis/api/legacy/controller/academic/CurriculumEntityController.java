package uz.hemis.api.legacy.controller.academic;

import uz.hemis.api.legacy.adapter.LegacyResponseHelper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.domain.entity.academic.Curriculum;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "08.O'quv reja", description = "O'quv reja entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_ECurriculum")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class CurriculumEntityController {

    private final AcademicEntityLegacyService academicService;
    private final CubaFilterHelper filterHelper;
    private final LegacySecurityHelper securityHelper;

    /** OWASP A01 BOLA defense — caller must own the curriculum's university. */
    private boolean isAccessAllowed(Curriculum entity) {
        String callerCode = securityHelper.getUniversityCodeFromContext();
        if (callerCode == null || callerCode.isEmpty()) {
            return true; // admin/system scope
        }
        return callerCode.equals(entity.getUniversity());
    }

    private Map<String, Object> forbiddenBody() {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", "Forbidden");
        err.put("details", "Resource belongs to another university");
        return err;
    }

    @Operation(summary = "ID bo'yicha topish (CUBA entity API)")
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Curriculum> entity = academicService.findCurriculumById(entityId);
        if (entity.isEmpty()) return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity hemishe_ECurriculum with id " + entityId + " not found"));
        if (!isAccessAllowed(entity.get())) return ResponseEntity.status(403).body(forbiddenBody());
        return ResponseEntity.ok(academicService.toCurriculumMap(entity.get(), returnNulls));
    }

    @Operation(summary = "Yangilash (CUBA entity API)")
    @PreAuthorize("hasAuthority('students.edit')")
    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Curriculum> existingOpt = academicService.findCurriculumById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity hemishe_ECurriculum with id " + entityId + " not found"));
        if (!isAccessAllowed(existingOpt.get())) return ResponseEntity.status(403).body(forbiddenBody());
        // Mass-assignment defense — body cannot relocate to foreign OTM.
        body.remove("_university");
        body.remove("university");
        Curriculum saved = academicService.saveCurriculum(existingOpt.get());
        return ResponseEntity.ok(academicService.toCurriculumMap(saved, returnNulls));
    }

    @Operation(summary = "Soft delete (CUBA entity API)")
    @PreAuthorize("hasAuthority('students.delete')")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        Optional<Curriculum> entity = academicService.findCurriculumById(entityId);
        if (entity.isEmpty()) return ResponseEntity.status(404).build();
        if (!isAccessAllowed(entity.get())) return ResponseEntity.status(403).body(forbiddenBody());
        academicService.deleteCurriculum(entity.get());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "GET search — CUBA filter qidirish")
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Curriculum> allEntities = academicService.findAllCurriculum();
        List<Curriculum> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> academicService.toCurriculumMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @Operation(summary = "POST search — CUBA filter JSON body bilan qidirish")
    @PreAuthorize("hasAuthority('students.view')")
    @PostMapping("/search")
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

        List<Curriculum> allEntities = academicService.findAllCurriculum();
        List<Curriculum> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> academicService.toCurriculumMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @Operation(summary = "Barcha entity'lar (CUBA pagination — offset/limit)")
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "50") Integer limit, @RequestParam(required = false) String sort, @RequestParam(required = false) Boolean returnNulls) {
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            sorting = Sort.by(parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
        }
        Page<Curriculum> entityPage = academicService.findAllCurriculum(PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1), sorting));
        return ResponseEntity.ok(entityPage.getContent().stream().map(e -> academicService.toCurriculumMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @Operation(summary = "Yangi entity yaratish (CUBA entity API)")
    @PreAuthorize("hasAuthority('students.edit')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Curriculum entity = new Curriculum();
        Curriculum saved = academicService.saveCurriculum(entity);
        return ResponseEntity.ok(academicService.toCurriculumMap(saved, returnNulls));
    }
}
