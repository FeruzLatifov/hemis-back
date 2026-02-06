package uz.hemis.api.legacy.controller.document;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.domain.entity.DiplomaBlank;
import uz.hemis.service.legacy.document.DocumentLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Diploma Blank Entity Controller (CUBA Pattern)
 *
 * @since 2.0.0
 */
@Tag(name = "Legacy Entity APIs - Diploma Blanks", description = "CUBA-compatible CRUD operations for diploma blanks entity")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EDiplomaBlank")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DiplomaBlankEntityController {

    private final DocumentLegacyService documentService;
    private final CubaFilterHelper filterHelper;

    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId, @RequestParam(required = false) Boolean returnNulls) {
        Optional<DiplomaBlank> entity = documentService.findDiplomaBlankById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(documentService.toDiplomaBlankMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Optional<DiplomaBlank> existingOpt = documentService.findDiplomaBlankById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        DiplomaBlank entity = existingOpt.get();
        documentService.updateDiplomaBlankFromMap(entity, body);
        DiplomaBlank saved = documentService.saveDiplomaBlank(entity);
        return ResponseEntity.ok(documentService.toDiplomaBlankMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        Optional<DiplomaBlank> entity = documentService.findDiplomaBlankById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        documentService.deleteDiplomaBlank(entity.get());
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

        List<DiplomaBlank> allEntities = documentService.findAllDiplomaBlank();
        List<DiplomaBlank> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> documentService.toDiplomaBlankMap(e, returnNulls))
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

        List<DiplomaBlank> allEntities = documentService.findAllDiplomaBlank();
        List<DiplomaBlank> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> documentService.toDiplomaBlankMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean returnNulls) {

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            sorting = Sort.by(parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
        }

        return ResponseEntity.ok(documentService.findAllDiplomaBlank(PageRequest.of(offset / limit, limit, sorting))
            .getContent().stream()
            .map(e -> documentService.toDiplomaBlankMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        DiplomaBlank entity = new DiplomaBlank();
        documentService.updateDiplomaBlankFromMap(entity, body);
        DiplomaBlank saved = documentService.saveDiplomaBlank(entity);
        return ResponseEntity.ok(documentService.toDiplomaBlankMap(saved, returnNulls));
    }
}
