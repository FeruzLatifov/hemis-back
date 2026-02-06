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
import uz.hemis.domain.entity.Exam;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "10.Imtihonlar", description = "Imtihonlar entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EExam")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ExamEntityController {

    private final AcademicEntityLegacyService academicService;
    private final CubaFilterHelper filterHelper;

    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Exam> entity = academicService.findExamById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(academicService.toExamMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Exam> existingOpt = academicService.findExamById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        Exam saved = academicService.saveExam(existingOpt.get());
        return ResponseEntity.ok(academicService.toExamMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        Optional<Exam> entity = academicService.findExamById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        academicService.deleteExam(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Exam> allEntities = academicService.findAllExam();
        List<Exam> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> academicService.toExamMap(e, returnNulls))
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

        List<Exam> allEntities = academicService.findAllExam();
        List<Exam> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> academicService.toExamMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "50") Integer limit, @RequestParam(required = false) String sort, @RequestParam(required = false) Boolean returnNulls) {
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            sorting = Sort.by(parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
        }
        Page<Exam> entityPage = academicService.findAllExam(PageRequest.of(offset / limit, limit, sorting));
        return ResponseEntity.ok(entityPage.getContent().stream().map(e -> academicService.toExamMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Exam entity = new Exam();
        Exam saved = academicService.saveExam(entity);
        return ResponseEntity.ok(academicService.toExamMap(saved, returnNulls));
    }
}
