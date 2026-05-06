package uz.hemis.api.legacy.controller.finance;

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
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.domain.entity.finance.ScholarshipAmount;
import uz.hemis.service.legacy.finance.FinanceEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * ScholarshipAmount Entity Controller (CUBA Pattern)
 * Entity: hemishe_EStudentScholarshipAmount
 */
@Tag(name = "62.Stipendiya", description = "Stipendiya summa entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentScholarshipAmount")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ScholarshipAmountEntityController {

    private final FinanceEntityLegacyService financeService;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EStudentScholarshipAmount";

    @Operation(summary = "Stipendiya summasini ID bo'yicha topish (CUBA entity API)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        log.debug("GET scholarshipAmount by id: {}", entityId);
        Optional<ScholarshipAmount> entity = financeService.findScholarshipAmountById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(financeService.toScholarshipAmountMap(entity.get(), returnNulls));
    }

    @Operation(summary = "Stipendiya summasini yangilash (CUBA entity API)")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("PUT scholarshipAmount id: {}", entityId);
        Optional<ScholarshipAmount> existingOpt = financeService.findScholarshipAmountById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        ScholarshipAmount entity = existingOpt.get();
        financeService.updateScholarshipAmountFromMap(entity, body);
        ScholarshipAmount saved = financeService.saveScholarshipAmount(entity);
        return ResponseEntity.ok(financeService.toScholarshipAmountMap(saved, returnNulls));
    }

    @Operation(summary = "Stipendiya summasini soft delete (CUBA entity API)")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE scholarshipAmount id: {}", entityId);
        Optional<ScholarshipAmount> entity = financeService.findScholarshipAmountById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        financeService.deleteScholarshipAmount(entity.get());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "GET search — CUBA filter qidirish")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<ScholarshipAmount> allEntities = financeService.findAllScholarshipAmount();
        List<ScholarshipAmount> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> financeService.toScholarshipAmountMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @Operation(summary = "POST search — CUBA filter JSON body bilan qidirish")
    @PreAuthorize("isAuthenticated()")
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

        List<ScholarshipAmount> allEntities = financeService.findAllScholarshipAmount();
        List<ScholarshipAmount> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> financeService.toScholarshipAmountMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @Operation(summary = "Barcha stipendiya summalari (CUBA pagination — offset/limit)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        log.debug("GET all scholarshipAmounts - offset: {}, limit: {}", offset, limit);
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, parts[0]);
        }
        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<ScholarshipAmount> entityPage = financeService.findAllScholarshipAmount(pageRequest);
        List<Map<String, Object>> result = entityPage.getContent().stream().map(e -> financeService.toScholarshipAmountMap(e, returnNulls)).collect(Collectors.toList());
        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Yangi stipendiya summasi yaratish (CUBA entity API)")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("POST create new scholarshipAmount");
        ScholarshipAmount entity = new ScholarshipAmount();
        financeService.updateScholarshipAmountFromMap(entity, body);
        ScholarshipAmount saved = financeService.saveScholarshipAmount(entity);
        return ResponseEntity.ok(financeService.toScholarshipAmountMap(saved, returnNulls));
    }
}
