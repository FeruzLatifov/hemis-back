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
import uz.hemis.domain.entity.Project;
import uz.hemis.service.legacy.science.ScienceEntityLegacyService;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Project Entity Controller (CUBA Pattern)
 * Tag: 19.Ilmiy loyihalar
 * Entity: hemishe_EProject
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EProject/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EProject/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EProject/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EProject/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EProject/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EProject           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EProject           - Create new
 *
 * @since 2.0.0
 */
@Tag(name = "19.Ilmiy loyihalar", description = "Loyihalar entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EProject")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ProjectEntityController {

    private final ScienceEntityLegacyService scienceService;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EProject";

    // =====================================================
    // GET /{entityId} - Bitta loyihani olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta loyihani olish",
        description = """
            ID bo'yicha loyiha ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EProject/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Loyiha UUID") @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET Project by id: {}, returnNulls: {}", entityId, returnNulls);

        Optional<Project> entity = scienceService.findProjectById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EProject/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(scienceService.toProjectMap(entity.get(), returnNulls, view));
    }

    // =====================================================
    // PUT /{entityId} - Loyihani yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Operation(
        summary = "Loyihani yangilash",
        description = "Mavjud loyiha ma'lumotlarini qisman yangilash"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT Project id: {}", entityId);

        Optional<Project> existingOpt = scienceService.findProjectById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("PUT /entities/hemishe_EProject/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        Project entity = existingOpt.get();
        scienceService.updateProjectFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        Project saved = scienceService.saveProject(entity);

        // OLD-HEMIS: minimal response
        return ResponseEntity.ok(scienceService.toProjectMinimalMap(saved));
    }

    // =====================================================
    // DELETE /{entityId} - Loyihani o'chirish (soft delete)
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Loyihani o'chirish",
        description = "Loyihani soft delete qilish (delete_ts belgilanadi)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE Project id: {}", entityId);

        Optional<Project> entity = scienceService.findProjectById(entityId);
        if (entity.isEmpty()) {
            log.warn("DELETE /entities/hemishe_EProject/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        // Soft delete: delete_ts belgilash (hard delete emas — FK constraint buzilmasligi uchun)
        scienceService.softDeleteProject(entity.get());
        log.info("DELETE /entities/hemishe_EProject/{} - muvaffaqiyatli o'chirildi (soft)", entityId);

        // OLD-HEMIS: 200 OK
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Operation(summary = "Loyihalarni qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Project> allEntities = scienceService.findAllProject();
        List<Project> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toProjectMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Operation(summary = "Loyihalarni qidirish (POST)")
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

        List<Project> allEntities = scienceService.findAllProject();
        List<Project> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toProjectMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha loyihalar ro'yxati (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Operation(summary = "Barcha loyihalar ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all Project - offset: {}, limit: {}", offset, limit);

        if (limit == null) {
            List<Project> allEntities = scienceService.findAllProject();
            List<Map<String, Object>> result = allEntities.stream()
                .map(e -> scienceService.toProjectMap(e, returnNulls, view))
                .collect(Collectors.toList());

            if (Boolean.TRUE.equals(returnCount)) {
                return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(result.size()))
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
        Page<Project> entityPage = scienceService.findAllProject(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> scienceService.toProjectMap(e, returnNulls, view))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST - Yangi loyiha yaratish
    // =====================================================

    @PostMapping
    @Operation(
        summary = "Loyiha yaratish",
        description = """
            Yangi loyiha yozuvini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EProject
            **Auth:** Bearer token (required)

            **Misol request body:**
            ```json
            {
                "name": "Innovatsion loyiha",
                "projectNumber": "PRJ-2024-001",
                "contractNumber": "SH-001/2024",
                "contractDate": "2024-01-15",
                "startDate": "2024-02-01",
                "endDate": "2025-01-31",
                "active": true
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EProject\",\"_instanceName\":\"Innovatsion loyiha\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Loyiha ma'lumotlari",
                required = true)
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new Project");
        log.debug("Request body: {}", body);

        Project entity = new Project();

        // Agar id berilgan bo'lsa, ishlatish (OLD-HEMIS pattern)
        if (body.containsKey("id")) {
            try {
                entity.setId(UUID.fromString(body.get("id").toString()));
            } catch (Exception e) {
                log.warn("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        scienceService.updateProjectFromMap(entity, body);

        // Version va timestamps
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        Project saved = scienceService.saveProject(entity);
        log.info("Project created with id: {}", saved.getId());

        // OLD-HEMIS: minimal response
        return ResponseEntity.ok(scienceService.toProjectMinimalMap(saved));
    }
}
