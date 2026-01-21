package uz.hemis.api.legacy.controller;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.ProjectMeta;
import uz.hemis.domain.repository.ProjectMetaRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loyiha meta ma'lumotlari Entity Controller (CUBA Pattern)
 * Tag: 20.Loyiha meta ma'lumotlari
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
@Tag(name = "20.Loyiha meta ma'lumotlari", description = "Loyiha meta ma'lumotlari entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EProjectMeta")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ProjectMetaEntityController {

    private final ProjectMetaRepository repository;
    private static final String ENTITY_NAME = "hemishe_EProjectMeta";
    private static final String CUBA_ENTITY_CLASS = "com.company.hemishe.entity.EProjectMeta";

    // =====================================================
    // GET /{entityId} - Bitta loyiha meta ma'lumotini olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
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

        Optional<ProjectMeta> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EProjectMeta/{} - topilmadi", entityId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Loyiha meta ma'lumotini yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
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

        Optional<ProjectMeta> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProjectMeta entity = existingOpt.get();
        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        ProjectMeta saved = repository.save(entity);

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE /{entityId} - Loyiha meta ma'lumotini o'chirish
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
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

        Optional<ProjectMeta> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        log.info("DELETE /entities/hemishe_EProjectMeta/{} - muvaffaqiyatli", entityId);

        // OLD-HEMIS: 200 OK (not 204)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Loyiha meta qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search ProjectMeta with filter: {}", filter);

        List<ProjectMeta> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Loyiha meta qidirish (POST)")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search ProjectMeta with filter: {}", filter);

        List<ProjectMeta> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha loyiha meta ma'lumotlari (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
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
            List<ProjectMeta> allEntities = repository.findAll();
            return ResponseEntity.ok(allEntities.stream()
                .map(e -> toMap(e, returnNulls))
                .collect(Collectors.toList()));
        }

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "DESC".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<ProjectMeta> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST - Yangi loyiha meta ma'lumoti yaratish
    // =====================================================

    @PostMapping
    @Transactional
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

        updateFromMap(entity, body);

        // Version va timestamps
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        ProjectMeta saved = repository.save(entity);
        log.info("ProjectMeta created with id: {}", saved.getId());

        // OLD-HEMIS: minimal response (faqat _entityName, _instanceName, id)
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * OLD-HEMIS format: com.company.hemishe.entity.EProjectMeta-{id} [detached]
     */
    private String buildInstanceName(ProjectMeta entity) {
        return CUBA_ENTITY_CLASS + "-" + entity.getId() + " [detached]";
    }

    /**
     * Entity -> OLD-HEMIS Map formatiga o'girish
     * Field nomlari OLD-HEMIS ga 100% mos bo'lishi kerak (camelCase)!
     */
    private Map<String, Object> toMap(ProjectMeta entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS exact field order
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // Fields in OLD-HEMIS order (camelCase!)
        putIfNotNull(map, "quantityMembers", entity.getQuantityMembers(), returnNulls);
        // active - OLD-HEMIS har doim qaytaradi (null bo'lsa ham)
        map.put("active", entity.getActive());
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "fiscalYear", entity.getFiscalYear(), returnNulls);
        putIfNotNull(map, "budget", entity.getBudget(), returnNulls);

        return map;
    }

    /**
     * OLD-HEMIS Map -> Entity ga o'girish
     */
    private void updateFromMap(ProjectMeta entity, Map<String, Object> map) {
        // fiscalYear
        if (map.containsKey("fiscalYear")) {
            Object val = map.get("fiscalYear");
            if (val != null) {
                entity.setFiscalYear(Integer.parseInt(val.toString()));
            }
        }

        // budget
        if (map.containsKey("budget")) {
            Object val = map.get("budget");
            if (val != null) {
                entity.setBudget(Double.parseDouble(val.toString()));
            }
        }

        // quantityMembers
        if (map.containsKey("quantityMembers")) {
            Object val = map.get("quantityMembers");
            if (val != null) {
                entity.setQuantityMembers(Integer.parseInt(val.toString()));
            }
        }

        // position
        if (map.containsKey("position")) {
            Object val = map.get("position");
            if (val != null) {
                entity.setPosition(Integer.parseInt(val.toString()));
            }
        }

        // active
        if (map.containsKey("active")) {
            Object val = map.get("active");
            if (val instanceof Boolean) {
                entity.setActive((Boolean) val);
            } else if (val != null) {
                entity.setActive(Boolean.valueOf(val.toString()));
            }
        }

        // project (foreign key - UUID)
        if (map.containsKey("project")) {
            entity.setProject(extractUuid(map.get("project")));
        }

        // translations
        if (map.containsKey("translations")) {
            Object val = map.get("translations");
            entity.setTranslations(val != null ? val.toString() : null);
        }
    }

    // =====================================================
    // Utility Methods
    // =====================================================

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) {
                try {
                    return UUID.fromString(id.toString());
                } catch (Exception e) {
                    log.warn("Invalid UUID: {}", id);
                }
            }
        }
        try {
            return UUID.fromString(value.toString());
        } catch (Exception e) {
            log.warn("Invalid UUID format: {}", value);
            return null;
        }
    }
}
