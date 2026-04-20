package uz.hemis.api.legacy.controller.science;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.research.ProjectMeta;
import uz.hemis.service.legacy.science.ScienceEntityLegacyService;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Loyiha meta ma'lumotlari Entity Controller (CUBA Pattern)
 * Tag: 20.Ilmiy loyiha meta ma'lumotlari
 * Entity: hemishe_EProjectMeta
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EProjectMeta/{id}      - ID bo'yicha olish
 * - PUT    /app/rest/v2/entities/hemishe_EProjectMeta/{id}      - Yangilash
 * - DELETE /app/rest/v2/entities/hemishe_EProjectMeta/{id}      - O'chirish (soft delete)
 * - GET    /app/rest/v2/entities/hemishe_EProjectMeta/search    - Qidirish (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EProjectMeta/search    - Qidirish (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EProjectMeta           - Ro'yxat (pagination)
 * - POST   /app/rest/v2/entities/hemishe_EProjectMeta           - Yaratish
 *
 * @since 2.0.0
 */
@Tag(name = "20.Ilmiy loyiha meta ma'lumotlari", description = "Loyiha meta ma'lumotlari entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EProjectMeta")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ProjectMetaEntityController {

    private final ScienceEntityLegacyService scienceService;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EProjectMeta";

    // =====================================================
    // GET /{entityId} - Bitta loyiha meta ma'lumotini olish
    // =====================================================

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Loyiha meta ma'lumotini olish",
        description = """
            ID bo'yicha loyiha meta ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EProjectMeta/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Loyiha meta UUID")
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET ProjectMeta by id: {}", entityId);

        Optional<ProjectMeta> entity = scienceService.findProjectMetaById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EProjectMeta/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(scienceService.toProjectMetaMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Loyiha meta ma'lumotini yangilash
    // =====================================================

    @PreAuthorize("hasAuthority('science.edit')")
    @PutMapping("/{entityId}")
    @Operation(
        summary = "Loyiha meta ma'lumotini yangilash",
        description = "Mavjud loyiha meta ma'lumotlarini qisman yangilash"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT ProjectMeta id: {}", entityId);

        Optional<ProjectMeta> existingOpt = scienceService.findProjectMetaById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProjectMeta entity = existingOpt.get();
        scienceService.updateProjectMetaFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        ProjectMeta saved = scienceService.saveProjectMeta(entity);

        // OLD-HEMIS: minimal response
        return ResponseEntity.ok(scienceService.toProjectMetaMinimalMap(saved));
    }

    // =====================================================
    // DELETE /{entityId} - Loyiha meta ma'lumotini o'chirish
    // =====================================================

    @PreAuthorize("hasAuthority('science.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Loyiha meta ma'lumotini o'chirish",
        description = "Soft delete - delete_ts ni belgilaydi"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE ProjectMeta id: {}", entityId);

        Optional<ProjectMeta> entity = scienceService.findProjectMetaById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        scienceService.deleteProjectMeta(entity.get());
        log.info("DELETE /entities/hemishe_EProjectMeta/{} - muvaffaqiyatli", entityId);

        // OLD-HEMIS: 200 OK (not 204)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping("/search")
    @Operation(summary = "Loyiha meta qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<ProjectMeta> allEntities = scienceService.findAllProjectMeta();
        List<ProjectMeta> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toProjectMetaMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PreAuthorize("hasAuthority('science.view')")
    @PostMapping("/search")
    @Operation(summary = "Loyiha meta qidirish (POST)")
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

        List<ProjectMeta> allEntities = scienceService.findAllProjectMeta();
        List<ProjectMeta> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toProjectMetaMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha loyiha meta ma'lumotlari (paginated)
    // =====================================================

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping({"", "/"})
    @Operation(summary = "Barcha loyiha meta ma'lumotlari ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all ProjectMeta - offset: {}, limit: {}", offset, limit);

        // Agar limit null bo'lsa, barcha yozuvlarni qaytarish
        if (limit == null) {
            List<ProjectMeta> allEntities = scienceService.findAllProjectMeta();
            List<Map<String, Object>> result = allEntities.stream()
                .map(e -> scienceService.toProjectMetaMap(e, returnNulls))
                .collect(Collectors.toList());
            if (Boolean.TRUE.equals(returnCount)) {
                return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(allEntities.size()))
                    .body(result);
            }
            return ResponseEntity.ok(result);
        }

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "DESC".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int safeLimit = Math.max(limit, 1);
        int page = offset / safeLimit;
        PageRequest pageRequest = PageRequest.of(page, safeLimit, sorting);
        Page<ProjectMeta> entityPage = scienceService.findAllProjectMeta(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> scienceService.toProjectMetaMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST - Yangi loyiha meta ma'lumoti yaratish
    // =====================================================

    @PreAuthorize("hasAuthority('science.edit')")
    @PostMapping
    @Operation(
        summary = "Loyiha meta ma'lumoti yaratish",
        description = """
            Yangi loyiha meta ma'lumotini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EProjectMeta

            **Misol request body:**
            ```json
            {
                "fiscalYear": 2024,
                "budget": 50000000.0,
                "quantityMembers": 5,
                "active": true
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EProjectMeta\",\"_instanceName\":\"com.company.hemishe.entity.EProjectMeta-uuid [detached]\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new ProjectMeta");
        log.debug("Request body: {}", body);

        ProjectMeta entity = new ProjectMeta();

        // Agar id berilgan bo'lsa, ishlatish (OLD-HEMIS pattern)
        if (body.containsKey("id")) {
            try {
                entity.setId(UUID.fromString(body.get("id").toString()));
            } catch (Exception e) {
                log.warn("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        scienceService.updateProjectMetaFromMap(entity, body);

        // Version va timestamps
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        ProjectMeta saved = scienceService.saveProjectMeta(entity);
        log.info("ProjectMeta created with id: {}", saved.getId());

        // OLD-HEMIS: minimal response (faqat _entityName, _instanceName, id)
        return ResponseEntity.ok(scienceService.toProjectMetaMinimalMap(saved));
    }
}
