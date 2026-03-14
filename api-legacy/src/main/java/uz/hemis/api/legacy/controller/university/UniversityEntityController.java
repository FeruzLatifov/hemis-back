package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.hemis.api.legacy.adapter.JsonNull;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.domain.entity.University;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * University Entity Controller (CUBA Pattern)
 *
 * @since 2.0.0
 */
@Tag(name = "15.OTM", description = "Oliy ta'lim muassasalari - universitetlar ro'yxati va boshqaruvi")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EUniversity")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityEntityController {

    private final UniversityRefLegacyService universityService;
    private final CubaFilterHelper filterHelper;

    private static final String ENTITY_NAME = "hemishe_EUniversity";

    @GetMapping("/{entityId}")
    @Operation(summary = "OTM olish (ID bo'yicha)")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable String entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET /entities/hemishe_EUniversity/{}", entityId);

        Optional<University> entity = universityService.findUniversityById(entityId);
        if (entity.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_EUniversity with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }
        Map<String, Object> result = universityService.toUniversityMap(entity.get(), returnNulls, view);

        // OLD-HEMIS compatibility: Replace null values with JsonNull.INSTANCE
        // This ensures Jackson serializes them as "field": null instead of omitting
        if (Boolean.TRUE.equals(returnNulls)) {
            result.replaceAll((k, v) -> v == null ? JsonNull.INSTANCE : v);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "OTM yangilash")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT /entities/hemishe_EUniversity/{}", entityId);

        Optional<University> existingOpt = universityService.findUniversityById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_EUniversity with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        University entity = existingOpt.get();
        universityService.updateUniversityFromMap(entity, body);
        University saved = universityService.saveUniversity(entity);
        return ResponseEntity.ok(universityService.toUniversityMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "OTM o'chirish")
    public ResponseEntity<?> delete(@PathVariable String entityId) {
        log.info("DELETE /entities/hemishe_EUniversity/{}", entityId);

        Optional<University> entity = universityService.findUniversityById(entityId);
        if (entity.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_EUniversity with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }
        universityService.deleteUniversity(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "OTM qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<University> allEntities = universityService.findAllUniversity();
        List<University> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );
        return ResponseEntity.ok(result.stream()
            .map(e -> universityService.toUniversityMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "OTM qidirish (POST)")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        int effectiveOffset = filterHelper.extractInt(body, "offset", offset, 0);
        int effectiveLimit = filterHelper.extractInt(body, "limit", limit, 50);
        String filterJson = filterHelper.extractFilterFromBody(body);

        log.debug("POST search - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<University> allEntities = universityService.findAllUniversity();
        List<University> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );
        return ResponseEntity.ok(result.stream()
            .map(e -> universityService.toUniversityMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "OTM ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET /entities/hemishe_EUniversity - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        List<University> entities;
        long totalElements;
        if (limit == null) {
            entities = universityService.findAllUniversity(sorting);
            totalElements = entities.size();
        } else {
            int effectiveOffset = offset != null ? offset : 0;
            int page = effectiveOffset / limit;
            PageRequest pageRequest = PageRequest.of(page, limit, sorting);
            var entityPage = universityService.findAllUniversity(pageRequest);
            entities = entityPage.getContent();
            totalElements = entityPage.getTotalElements();
        }

        List<Map<String, Object>> result = entities.stream()
            .map(e -> universityService.toUniversityMap(e, returnNulls, view))
            .collect(Collectors.toList());

        log.info("Jami {} ta OTM topildi", result.size());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(totalElements))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "OTM yaratish yoki yangilash")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            HttpServletRequest request) {

        log.info("POST /entities/hemishe_EUniversity - body: {}", body);

        String code = universityService.extractUniversityCode(body);
        if (code == null || code.isEmpty() || "null".equals(code)) {
            code = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            log.info("POST - code auto-generated: {}", code);
        }

        Optional<University> existingOpt = universityService.findUniversityById(code);
        University entity;

        if (existingOpt.isPresent()) {
            log.info("POST - mavjud OTM yangilanmoqda: {}", code);
            entity = existingOpt.get();
        } else {
            // Check for soft-deleted record to restore (OLD-HEMIS compatibility)
            Optional<University> deletedOpt = universityService.findUniversityByIdIncludingDeleted(code);
            if (deletedOpt.isPresent()) {
                log.info("POST - o'chirilgan OTM tiklanmoqda: {}", code);
                entity = deletedOpt.get();
                entity.setDeleteTs(null);
                entity.setDeletedBy(null);
            } else {
                log.info("POST - yangi OTM yaratilmoqda: {}", code);
                entity = new University();
                entity.setCode(code);
            }
        }

        universityService.updateUniversityFromMap(entity, body);
        University saved = universityService.saveUniversity(entity);

        URI location = ServletUriComponentsBuilder
                .fromRequestUri(request)
                .path("/{id}")
                .buildAndExpand(saved.getCode())
                .toUri();

        return ResponseEntity.ok()
                .header("Location", location.toString())
                .body(universityService.toUniversityMinimalMap(saved));
    }
}
