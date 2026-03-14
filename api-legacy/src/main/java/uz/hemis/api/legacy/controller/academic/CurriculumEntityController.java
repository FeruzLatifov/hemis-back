package uz.hemis.api.legacy.controller.academic;

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
import uz.hemis.domain.entity.Curriculum;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "08.O'quv reja", description = "O'quv reja entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_ECurriculum")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class CurriculumEntityController {

    private final AcademicEntityLegacyService academicService;
    private final CubaFilterHelper filterHelper;

    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Curriculum> entity = academicService.findCurriculumById(entityId);
        if (entity.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Entity not found", "details", "Entity hemishe_ECurriculum with id " + entityId + " not found"));
        return ResponseEntity.ok(academicService.toCurriculumMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Curriculum> existingOpt = academicService.findCurriculumById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Entity not found", "details", "Entity hemishe_ECurriculum with id " + entityId + " not found"));
        Curriculum saved = academicService.saveCurriculum(existingOpt.get());
        return ResponseEntity.ok(academicService.toCurriculumMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        Optional<Curriculum> entity = academicService.findCurriculumById(entityId);
        if (entity.isEmpty()) return ResponseEntity.status(404).build();
        academicService.deleteCurriculum(entity.get());
        return ResponseEntity.ok().build();
    }

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

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Curriculum entity = new Curriculum();
        Curriculum saved = academicService.saveCurriculum(entity);
        return ResponseEntity.ok(academicService.toCurriculumMap(saved, returnNulls));
    }
}
