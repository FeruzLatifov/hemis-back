package uz.hemis.api.legacy.controller.science;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.PublicationCriteria;

import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.service.legacy.science.ScienceEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * PublicationCriteria Entity Controller (CUBA Pattern)
 * Tag 26: Nashrlarni baholash mezonlari
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EPublicationCriteria
 */
@Tag(name = "26.Ilmiy nashrlarni baholash mezonlari")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationCriteria")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationCriteriaEntityController {

    private final ScienceEntityLegacyService scienceService;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EPublicationCriteria";

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping("/{entityId}")
    @Operation(summary = "Baholash mezonini ID bo'yicha olish", description = "UUID bo'yicha bitta baholash mezonini qaytaradi")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET PublicationCriteria by id: {}", entityId);

        Optional<PublicationCriteria> entity = scienceService.findPublicationCriteriaById(entityId);
        if (entity.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(scienceService.toPublicationCriteriaMap(entity.get(), returnNulls));
    }

    @PreAuthorize("hasAuthority('science.edit')")
    @PutMapping("/{entityId}")
    @Operation(summary = "Baholash mezonini yangilash", description = "Mavjud baholash mezonini yangilaydi")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT PublicationCriteria id: {}", entityId);

        Optional<PublicationCriteria> existingOpt = scienceService.findPublicationCriteriaById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        PublicationCriteria entity = existingOpt.get();
        scienceService.updatePublicationCriteriaFromMap(entity, body);

        PublicationCriteria saved = scienceService.savePublicationCriteria(entity);
        return ResponseEntity.ok(scienceService.toPublicationCriteriaMap(saved, returnNulls));
    }

    @PreAuthorize("hasAuthority('science.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "Baholash mezonini o'chirish", description = "Baholash mezonini soft delete qiladi")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID entityId) {
        log.debug("DELETE PublicationCriteria id: {}", entityId);

        Optional<PublicationCriteria> entity = scienceService.findPublicationCriteriaById(entityId);
        if (entity.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        scienceService.deletePublicationCriteria(entity.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping("/search")
    @Operation(summary = "Baholash mezonlarini qidirish (GET)", description = "URL parametrlari orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<PublicationCriteria> allEntities = scienceService.findAllPublicationCriteria();
        List<PublicationCriteria> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toPublicationCriteriaMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('science.view')")
    @PostMapping("/search")
    @Operation(summary = "Baholash mezonlarini qidirish (POST)", description = "JSON filter orqali qidirish")
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

        List<PublicationCriteria> allEntities = scienceService.findAllPublicationCriteria();
        List<PublicationCriteria> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toPublicationCriteriaMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping
    @Operation(summary = "Barcha baholash mezonlarini olish", description = "Sahifalangan ro'yxatni qaytaradi")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Jami sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifa hajmi") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Saralash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all PublicationCriteria - offset: {}, limit: {}", offset, limit);

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
        Page<PublicationCriteria> entityPage = scienceService.findAllPublicationCriteria(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> scienceService.toPublicationCriteriaMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('science.edit')")
    @PostMapping
    @Operation(summary = "Yangi baholash mezoni yaratish", description = "Yangi baholash mezonini yaratadi")
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Baholash mezoni ma'lumotlari",
                content = @Content(
                    examples = @ExampleObject(
                        value = """
                            {
                              "_university": "401",
                              "_education_year": "2024",
                              "_publication_type_table": "hemishe_EPublicationScientific",
                              "markValue": 10,
                              "active": true
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new PublicationCriteria");

        PublicationCriteria entity = new PublicationCriteria();
        scienceService.updatePublicationCriteriaFromMap(entity, body);
        PublicationCriteria saved = scienceService.savePublicationCriteria(entity);

        // OLD-HEMIS: POST doesn't return 'active' field
        Map<String, Object> result = scienceService.toPublicationCriteriaMap(saved, returnNulls);
        result.remove("active");
        return ResponseEntity.ok(result);
    }
}
