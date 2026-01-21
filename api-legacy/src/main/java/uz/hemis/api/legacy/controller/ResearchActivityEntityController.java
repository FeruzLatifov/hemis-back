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
import uz.hemis.domain.entity.ResearchActivity;
import uz.hemis.domain.repository.ResearchActivityRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ilmiy faoliyat Entity Controller (CUBA Pattern)
 * Tag: 18.Ilmiy faollik
 * Entity: hemishe_EResearchActivity
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EResearchActivity/{id}      - ID bo'yicha olish
 * - PUT    /app/rest/v2/entities/hemishe_EResearchActivity/{id}      - Yangilash
 * - DELETE /app/rest/v2/entities/hemishe_EResearchActivity/{id}      - O'chirish (soft delete)
 * - GET    /app/rest/v2/entities/hemishe_EResearchActivity/search    - Qidirish (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EResearchActivity/search    - Qidirish (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EResearchActivity           - Ro'yxat (pagination)
 * - POST   /app/rest/v2/entities/hemishe_EResearchActivity           - Yaratish
 *
 * @since 2.0.0
 */
@Tag(name = "18.Ilmiy faollik", description = "Ilmiy faoliyat entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EResearchActivity")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ResearchActivityEntityController {

    private final ResearchActivityRepository repository;
    private static final String ENTITY_NAME = "hemishe_EResearchActivity";
    private static final String CUBA_ENTITY_CLASS = "com.company.hemishe.entity.EResearchActivity";

    // =====================================================
    // GET /{entityId} - Bitta ilmiy faoliyatni olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Bitta ilmiy faoliyatni olish",
        description = """
            ID bo'yicha ilmiy faoliyat ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EResearchActivity/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Ilmiy faoliyat ma'lumotlari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan ilmiy faoliyat topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Ilmiy faoliyat UUID identifikatori")
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET ResearchActivity by id: {}", entityId);

        Optional<ResearchActivity> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EResearchActivity/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Ilmiy faoliyatni yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Ilmiy faoliyatni yangilash",
        description = """
            Mavjud ilmiy faoliyat ma'lumotlarini qisman yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_EResearchActivity/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Ilmiy faoliyat yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT ResearchActivity id: {}", entityId);

        Optional<ResearchActivity> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("PUT /entities/hemishe_EResearchActivity/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        ResearchActivity entity = existingOpt.get();
        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        ResearchActivity saved = repository.save(entity);

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE /{entityId} - Ilmiy faoliyatni o'chirish (soft delete)
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Ilmiy faoliyatni o'chirish",
        description = """
            Ilmiy faoliyatni soft delete qilish (delete_ts ni belgilaydi).

            **OLD-HEMIS Compatible** - 200 OK qaytaradi (204 emas!)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE ResearchActivity id: {}", entityId);

        Optional<ResearchActivity> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("DELETE /entities/hemishe_EResearchActivity/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        repository.delete(entity.get());
        log.info("DELETE /entities/hemishe_EResearchActivity/{} - muvaffaqiyatli o'chirildi", entityId);

        // OLD-HEMIS: 200 OK (not 204)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Ilmiy faoliyatlarni qidirish (GET)",
        description = "URL parametrlari orqali ilmiy faoliyatlarni qidirish"
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search ResearchActivity with filter: {}", filter);

        List<ResearchActivity> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Ilmiy faoliyatlarni qidirish (POST)",
        description = "JSON filter orqali ilmiy faoliyatlarni qidirish"
    )
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search ResearchActivity with filter: {}", filter);

        List<ResearchActivity> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha ilmiy faoliyatlar ro'yxati (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    @Operation(
        summary = "Barcha ilmiy faoliyatlar ro'yxati",
        description = "Sahifalangan ilmiy faoliyatlar ro'yxatini olish"
    )
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all ResearchActivity - offset: {}, limit: {}", offset, limit);

        // Agar limit null bo'lsa, barcha yozuvlarni qaytarish
        if (limit == null) {
            List<ResearchActivity> allEntities = repository.findAll();
            List<Map<String, Object>> result = allEntities.stream()
                .map(e -> toMap(e, returnNulls))
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

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<ResearchActivity> entityPage = repository.findAll(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST - Yangi ilmiy faoliyat yaratish
    // =====================================================

    @PostMapping
    @Transactional
    @Operation(
        summary = "Ilmiy faoliyat yaratish",
        description = """
            Yangi ilmiy faoliyat yozuvini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EResearchActivity
            **Auth:** Bearer token (required)

            **Misol request body:**
            ```json
            {
                "hIndex": "5",
                "scientificWorkCount": "10",
                "referenceCount": "25",
                "link": "https://scholar.google.com/test"
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EResearchActivity\",\"_instanceName\":\"com.company.hemishe.entity.EResearchActivity-uuid [detached]\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Ilmiy faoliyat ma'lumotlari",
                required = true)
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new ResearchActivity");
        log.debug("Request body: {}", body);

        ResearchActivity entity = new ResearchActivity();

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

        ResearchActivity saved = repository.save(entity);
        log.info("ResearchActivity created with id: {}", saved.getId());

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
     * OLD-HEMIS format: com.company.hemishe.entity.EResearchActivity-{id} [detached]
     */
    private String buildInstanceName(ResearchActivity entity) {
        return CUBA_ENTITY_CLASS + "-" + entity.getId() + " [detached]";
    }

    /**
     * Entity -> OLD-HEMIS Map formatiga o'girish
     * Field tartib va nomlar OLD-HEMIS ga 100% mos bo'lishi kerak!
     */
    private Map<String, Object> toMap(ResearchActivity entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS exact field order
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // Fields in OLD-HEMIS order (camelCase!)
        putIfNotNull(map, "scientificWorkCount", entity.getScientificWorkCount(), returnNulls);
        putIfNotNull(map, "link", entity.getLink(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "referenceCount", entity.getReferenceCount(), returnNulls);
        putIfNotNull(map, "hIndex", entity.getHIndex(), returnNulls);

        return map;
    }

    /**
     * OLD-HEMIS Map -> Entity ga o'girish
     */
    private void updateFromMap(ResearchActivity entity, Map<String, Object> map) {
        // hIndex
        if (map.containsKey("hIndex")) {
            entity.setHIndex(getStringValue(map.get("hIndex")));
        }

        // scientificWorkCount
        if (map.containsKey("scientificWorkCount")) {
            entity.setScientificWorkCount(getStringValue(map.get("scientificWorkCount")));
        }

        // referenceCount
        if (map.containsKey("referenceCount")) {
            entity.setReferenceCount(getStringValue(map.get("referenceCount")));
        }

        // link
        if (map.containsKey("link")) {
            entity.setLink(getStringValue(map.get("link")));
        }

        // University (foreign key - {code: "xxx"} yoki to'g'ridan-to'g'ri string code)
        if (map.containsKey("university")) {
            entity.setUniversity(extractCode(map.get("university")));
        }

        // Education year (foreign key)
        if (map.containsKey("educationYear")) {
            entity.setEducationYear(extractCode(map.get("educationYear")));
        }

        // Scholar database (foreign key)
        if (map.containsKey("scholarDatabase")) {
            entity.setScholarDatabase(extractCode(map.get("scholarDatabase")));
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

    private String getStringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    /**
     * Extract code from nested object {code: "xxx"} or plain string
     */
    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            // Try "code" first (CUBA pattern), then "id"
            Object code = nested.get("code");
            if (code != null) {
                return code.toString();
            }
            Object id = nested.get("id");
            if (id != null) {
                return id.toString();
            }
        }
        // Plain string value
        return value.toString();
    }
}
