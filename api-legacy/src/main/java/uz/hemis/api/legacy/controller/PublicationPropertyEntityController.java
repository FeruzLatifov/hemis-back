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
import uz.hemis.domain.entity.PublicationProperty;
import uz.hemis.domain.repository.PublicationPropertyRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ilmiy ishlanmalar Entity Controller (CUBA Pattern)
 * Tag: 23.Ilmiy ishlanmalar
 * Entity: hemishe_EPublicationProperty
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EPublicationProperty/{id}      - ID bo'yicha olish
 * - PUT    /app/rest/v2/entities/hemishe_EPublicationProperty/{id}      - Yangilash
 * - DELETE /app/rest/v2/entities/hemishe_EPublicationProperty/{id}      - O'chirish (soft delete)
 * - GET    /app/rest/v2/entities/hemishe_EPublicationProperty/search    - Qidirish (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EPublicationProperty/search    - Qidirish (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EPublicationProperty           - Ro'yxat (pagination)
 * - POST   /app/rest/v2/entities/hemishe_EPublicationProperty           - Yaratish
 *
 * @since 2.0.0
 */
@Tag(name = "23.Ilmiy ishlanmalar", description = "Ilmiy ishlanmalar (intellektual mulk) entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationProperty")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationPropertyEntityController {

    private final PublicationPropertyRepository repository;
    private static final String ENTITY_NAME = "hemishe_EPublicationProperty";
    private static final String CUBA_ENTITY_CLASS = "com.company.hemishe.entity.EPublicationProperty";

    // =====================================================
    // GET /{entityId} - Bitta ilmiy ishlanmani olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Ilmiy ishlanmani olish",
        description = """
            ID bo'yicha ilmiy ishlanma (intellektual mulk) ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Ilmiy ishlanma UUID")
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET PublicationProperty by id: {}", entityId);

        Optional<PublicationProperty> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EPublicationProperty/{} - topilmadi", entityId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Ilmiy ishlanmani yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Ilmiy ishlanmani yangilash",
        description = "Mavjud ilmiy ishlanma ma'lumotlarini qisman yangilash"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT PublicationProperty id: {}", entityId);

        Optional<PublicationProperty> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationProperty entity = existingOpt.get();
        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        PublicationProperty saved = repository.save(entity);

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE /{entityId} - Ilmiy ishlanmani o'chirish
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Ilmiy ishlanmani o'chirish",
        description = "Soft delete - delete_ts ni belgilaydi (CUBA pattern)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE PublicationProperty id: {}", entityId);

        Optional<PublicationProperty> entityOpt = repository.findById(entityId);
        if (entityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // SOFT DELETE: delete_ts o'rnatish (CUBA pattern)
        // Hard delete QILINMAYDI - foreign key constraint xatosi bo'lmasligi uchun
        PublicationProperty entity = entityOpt.get();
        entity.setDeleteTs(LocalDateTime.now());
        repository.save(entity);

        log.info("DELETE /entities/hemishe_EPublicationProperty/{} - soft delete muvaffaqiyatli", entityId);

        // OLD-HEMIS: 200 OK (not 204)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Ilmiy ishlanmalar qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search PublicationProperty with filter: {}", filter);

        List<PublicationProperty> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Ilmiy ishlanmalar qidirish (POST)")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search PublicationProperty with filter: {}", filter);

        List<PublicationProperty> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha ilmiy ishlanmalar (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    @Operation(summary = "Barcha ilmiy ishlanmalar ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all PublicationProperty - offset: {}, limit: {}", offset, limit);

        // Default limit - xavfsizlik uchun (browser qotib qolmasligi uchun)
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        // Maksimal limit - juda katta response oldini olish
        if (limit > 1000) {
            limit = 1000;
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
        Page<PublicationProperty> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST - Yangi ilmiy ishlanma yaratish
    // =====================================================

    @PostMapping
    @Transactional
    @Operation(
        summary = "Ilmiy ishlanma yaratish",
        description = """
            Yangi ilmiy ishlanma (intellektual mulk) yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EPublicationProperty

            **Misol request body:**
            ```json
            {
                "name": "Yangi ixtiro nomi",
                "numbers": "FAP 00123",
                "authors": "Aliyev A., Karimov B.",
                "authorCounts": 2,
                "propertyDate": "2024-01-15",
                "active": true
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EPublicationProperty\",\"_instanceName\":\"com.company.hemishe.entity.EPublicationProperty-uuid [detached]\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new PublicationProperty");
        log.debug("Request body: {}", body);

        PublicationProperty entity = new PublicationProperty();

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

        PublicationProperty saved = repository.save(entity);
        log.info("PublicationProperty created with id: {}", saved.getId());

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
     * OLD-HEMIS format: _instanceName = name qiymati
     */
    private String buildInstanceName(PublicationProperty entity) {
        // OLD-HEMIS: _instanceName = entity nomi (name field)
        return entity.getName() != null ? entity.getName() : "";
    }

    /**
     * Entity -> OLD-HEMIS Map formatiga o'girish
     * OLD-HEMIS default view - faqat asosiy fieldlar, foreign keys va audit fields yo'q
     */
    private Map<String, Object> toMap(PublicationProperty entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS exact field order (default view)
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // Fields in OLD-HEMIS exact order
        putIfNotNull(map, "numbers", entity.getNumbers(), returnNulls);
        putIfNotNull(map, "propertyDate", entity.getPropertyDate(), returnNulls);
        putIfNotNull(map, "authorCounts", entity.getAuthorCounts(), returnNulls);
        putIfNotNull(map, "parameter", entity.getParameter(), returnNulls);
        map.put("active", entity.getActive());
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "isChecked", entity.getIsChecked(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "authors", entity.getAuthors(), returnNulls);

        // OLD-HEMIS default view da foreign keys va audit fields QAYTARILMAYDI!
        // Faqat view=_local yoki view=full bo'lganda qaytariladi

        return map;
    }

    /**
     * OLD-HEMIS Map -> Entity ga o'girish
     */
    private void updateFromMap(PublicationProperty entity, Map<String, Object> map) {
        // uId
        if (map.containsKey("uId")) {
            Object val = map.get("uId");
            if (val != null) {
                entity.setUId(Integer.parseInt(val.toString()));
            }
        }

        // name
        if (map.containsKey("name")) {
            Object val = map.get("name");
            entity.setName(val != null ? val.toString() : null);
        }

        // numbers
        if (map.containsKey("numbers")) {
            Object val = map.get("numbers");
            entity.setNumbers(val != null ? val.toString() : null);
        }

        // authors
        if (map.containsKey("authors")) {
            Object val = map.get("authors");
            entity.setAuthors(val != null ? val.toString() : null);
        }

        // authorCounts
        if (map.containsKey("authorCounts")) {
            Object val = map.get("authorCounts");
            if (val != null) {
                entity.setAuthorCounts(Integer.parseInt(val.toString()));
            }
        }

        // parameter
        if (map.containsKey("parameter")) {
            Object val = map.get("parameter");
            entity.setParameter(val != null ? val.toString() : null);
        }

        // propertyDate
        if (map.containsKey("propertyDate")) {
            Object val = map.get("propertyDate");
            if (val != null) {
                try {
                    entity.setPropertyDate(LocalDate.parse(val.toString()));
                } catch (Exception e) {
                    log.warn("Invalid date format for propertyDate: {}", val);
                }
            }
        }

        // filename
        if (map.containsKey("filename")) {
            Object val = map.get("filename");
            entity.setFilename(val != null ? val.toString() : null);
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

        // translations
        if (map.containsKey("translations")) {
            Object val = map.get("translations");
            entity.setTranslations(val != null ? val.toString() : null);
        }

        // isChecked
        if (map.containsKey("isChecked")) {
            Object val = map.get("isChecked");
            if (val instanceof Boolean) {
                entity.setIsChecked((Boolean) val);
            } else if (val != null) {
                entity.setIsChecked(Boolean.valueOf(val.toString()));
            }
        }

        // isCheckedDate
        if (map.containsKey("isCheckedDate")) {
            Object val = map.get("isCheckedDate");
            if (val != null) {
                try {
                    entity.setIsCheckedDate(LocalDateTime.parse(val.toString()));
                } catch (Exception e) {
                    log.warn("Invalid date format for isCheckedDate: {}", val);
                }
            }
        }

        // Foreign key references (String - bazaga CAST(? AS uuid) bilan yoziladi)
        if (map.containsKey("university")) {
            entity.setUniversity(extractStringId(map.get("university")));
        }

        if (map.containsKey("patentType")) {
            entity.setPatentType(extractStringId(map.get("patentType")));
        }

        if (map.containsKey("publicationDatabase")) {
            entity.setPublicationDatabase(extractStringId(map.get("publicationDatabase")));
        }

        if (map.containsKey("locality")) {
            entity.setLocality(extractStringId(map.get("locality")));
        }

        if (map.containsKey("country")) {
            entity.setCountry(extractStringId(map.get("country")));
        }

        if (map.containsKey("employee")) {
            entity.setEmployee(extractStringId(map.get("employee")));
        }

        if (map.containsKey("educationYear")) {
            entity.setEducationYear(extractStringId(map.get("educationYear")));
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

    /**
     * Foreign key uchun OLD-HEMIS formatida qaytarish (String)
     * OLD-HEMIS nested object qaytaradi: {"id": "uuid"}
     */
    private void putForeignKey(Map<String, Object> map, String key, String value, Boolean returnNulls) {
        if (value != null) {
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("id", value);
            map.put(key, ref);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    /**
     * Foreign key dan ID ni String sifatida olish
     * Input: {"id": "uuid"} yoki "uuid" string
     */
    @SuppressWarnings("unchecked")
    private String extractStringId(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            return id != null ? id.toString() : null;
        }
        return value.toString();
    }
}
