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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.ProjectExecutor;
import uz.hemis.service.legacy.science.ScienceEntityLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProjectExecutor Entity Controller (CUBA Pattern)
 * Tag: 21.Ilmiy loyiha ijrochilari
 * Entity: hemishe_EProjectExecutor
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - POST   /app/rest/v2/entities/hemishe_EProjectExecutor           - Create new
 * - GET    /app/rest/v2/entities/hemishe_EProjectExecutor/{id}      - Get by ID
 * - GET    /app/rest/v2/entities/hemishe_EProjectExecutor           - List all with pagination
 * - PUT    /app/rest/v2/entities/hemishe_EProjectExecutor/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EProjectExecutor/{id}      - Soft delete
 *
 * @since 2.0.0
 */
@Tag(name = "21.Ilmiy loyiha ijrochilari", description = "Loyiha ijrochilari entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EProjectExecutor")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ProjectExecutorEntityController {

    private final ScienceEntityLegacyService scienceService;
    private static final String ENTITY_NAME = "hemishe_EProjectExecutor";

    // TEST endpoint
    @GetMapping("/test")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> test() {
        log.info("TEST endpoint called");
        try {
            long count = scienceService.countProjectExecutor();
            log.info("Count: {}", count);
            return ResponseEntity.ok("Test OK! Count: " + count);
        } catch (Exception e) {
            log.error("Error in test", e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getClass().getName());
            error.put("message", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(500).body(error);
        }
    }

    // =====================================================
    // POST - Yangi loyiha ijrochisi yaratish
    // =====================================================

    @PostMapping
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Loyiha ijrochisi yaratish",
        description = """
            Yangi loyiha ijrochisi yozuvini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EProjectExecutor
            **Auth:** Bearer token (required)

            **Misol request body:**
            ```json
            {
                "project": {"id": "project-uuid"},
                "projectExecutorType": {"id": "executor-type-uuid"},
                "outsider": "Tashqi ijrochi ismi",
                "startDate": "2024-01-01",
                "endDate": "2024-12-31",
                "active": true
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EProjectExecutor\",\"_instanceName\":\"com.company.hemishe.entity.EProjectExecutor-uuid [detached]\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Loyiha ijrochisi ma'lumotlari",
                required = true)
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new ProjectExecutor");
        log.debug("Request body: {}", body);

        ProjectExecutor entity = new ProjectExecutor();

        // Agar id berilgan bo'lsa, ishlatish (OLD-HEMIS pattern)
        if (body.containsKey("id")) {
            try {
                entity.setId(UUID.fromString(body.get("id").toString()));
            } catch (Exception e) {
                log.warn("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        scienceService.updateProjectExecutorFromMap(entity, body);

        // Version va timestamps
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        ProjectExecutor saved = scienceService.saveProjectExecutor(entity);
        log.info("ProjectExecutor created with id: {}", saved.getId());

        // OLD-HEMIS: minimal response
        return ResponseEntity.ok(scienceService.toProjectExecutorMinimalMap(saved));
    }

    // =====================================================
    // GET /{entityId} - Bitta loyiha ijrochisini olish
    // =====================================================

    @GetMapping("/{entityId}")
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Bitta loyiha ijrochisini olish",
        description = """
            ID bo'yicha loyiha ijrochisi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Loyiha ijrochisi UUID") @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET ProjectExecutor by id: {}, returnNulls: {}", entityId, returnNulls);

        Optional<ProjectExecutor> entity = scienceService.findProjectExecutorById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EProjectExecutor/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(scienceService.toProjectExecutorMap(entity.get(), returnNulls));
    }

    // =====================================================
    // GET - Barcha loyiha ijrochilari ro'yxati (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @PreAuthorize("permitAll()")
    @Operation(summary = "Barcha loyiha ijrochilari ro'yxati")
    public ResponseEntity<?> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET all ProjectExecutor - offset: {}, limit: {}", offset, limit);

        try {
            if (limit == null) {
                List<ProjectExecutor> allEntities = scienceService.findAllProjectExecutor();
                log.info("Found {} entities", allEntities.size());
                List<Map<String, Object>> result = allEntities.stream()
                    .map(e -> scienceService.toProjectExecutorMap(e, returnNulls))
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
            Page<ProjectExecutor> entityPage = scienceService.findAllProjectExecutor(pageRequest);

            List<Map<String, Object>> result = entityPage.getContent().stream()
                .map(e -> scienceService.toProjectExecutorMap(e, returnNulls))
                .collect(Collectors.toList());

            if (Boolean.TRUE.equals(returnCount)) {
                return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                    .body(result);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching ProjectExecutor entities", e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getClass().getName());
            error.put("message", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(500).body(error);
        }
    }

    // =====================================================
    // PUT /{entityId} - Loyiha ijrochisini yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Loyiha ijrochisini yangilash",
        description = "Mavjud loyiha ijrochisi ma'lumotlarini qisman yangilash"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT ProjectExecutor id: {}", entityId);

        Optional<ProjectExecutor> existingOpt = scienceService.findProjectExecutorById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("PUT /entities/hemishe_EProjectExecutor/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        ProjectExecutor entity = existingOpt.get();
        scienceService.updateProjectExecutorFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        ProjectExecutor saved = scienceService.saveProjectExecutor(entity);

        // OLD-HEMIS: minimal response
        return ResponseEntity.ok(scienceService.toProjectExecutorMinimalMap(saved));
    }

    // =====================================================
    // DELETE /{entityId} - Loyiha ijrochisini o'chirish (soft delete)
    // =====================================================

    @DeleteMapping("/{entityId}")
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Loyiha ijrochisini o'chirish",
        description = "Loyiha ijrochisini soft delete qilish (delete_ts belgilanadi)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE ProjectExecutor id: {}", entityId);

        Optional<ProjectExecutor> entity = scienceService.findProjectExecutorById(entityId);
        if (entity.isEmpty()) {
            log.warn("DELETE /entities/hemishe_EProjectExecutor/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        scienceService.deleteProjectExecutor(entity.get());
        log.info("DELETE /entities/hemishe_EProjectExecutor/{} - muvaffaqiyatli o'chirildi", entityId);

        // OLD-HEMIS: 200 OK
        return ResponseEntity.ok().build();
    }
}
