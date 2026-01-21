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
import uz.hemis.domain.entity.PublicationScientific;
import uz.hemis.domain.repository.PublicationScientificRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ilmiy nashrlar Entity Controller (CUBA Pattern)
 * Tag: 22.Ilmiy nashrlar
 * Entity: hemishe_EPublicationScientific
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EPublicationScientific/{id}      - ID bo'yicha olish
 * - PUT    /app/rest/v2/entities/hemishe_EPublicationScientific/{id}      - Yangilash
 * - DELETE /app/rest/v2/entities/hemishe_EPublicationScientific/{id}      - O'chirish (soft delete)
 * - GET    /app/rest/v2/entities/hemishe_EPublicationScientific/search    - Qidirish (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EPublicationScientific/search    - Qidirish (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EPublicationScientific           - Ro'yxat (pagination)
 * - POST   /app/rest/v2/entities/hemishe_EPublicationScientific           - Yaratish
 *
 * @since 2.0.0
 */
@Tag(name = "22.Ilmiy nashrlar", description = "Ilmiy nashrlar entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationScientific")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationScientificEntityController {

    private final PublicationScientificRepository repository;
    private static final String ENTITY_NAME = "hemishe_EPublicationScientific";
    private static final String CUBA_ENTITY_CLASS = "com.company.hemishe.entity.EPublicationScientific";

    // =====================================================
    // GET /{entityId} - Bitta ilmiy nashrni olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Ilmiy nashrni olish",
        description = """
            ID bo'yicha ilmiy nashr ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Ilmiy nashr UUID")
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET PublicationScientific by id: {}", entityId);

        Optional<PublicationScientific> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EPublicationScientific/{} - topilmadi", entityId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Ilmiy nashrni yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Ilmiy nashrni yangilash",
        description = "Mavjud ilmiy nashr ma'lumotlarini qisman yangilash"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT PublicationScientific id: {}", entityId);

        Optional<PublicationScientific> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationScientific entity = existingOpt.get();
        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        PublicationScientific saved = repository.save(entity);

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE /{entityId} - Ilmiy nashrni o'chirish
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Ilmiy nashrni o'chirish",
        description = "Soft delete - delete_ts ni belgilaydi (CUBA pattern)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE PublicationScientific id: {}", entityId);

        Optional<PublicationScientific> entityOpt = repository.findById(entityId);
        if (entityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // SOFT DELETE: delete_ts o'rnatish (CUBA pattern)
        // Hard delete QILINMAYDI - foreign key constraint xatosi bo'lmasligi uchun
        PublicationScientific entity = entityOpt.get();
        entity.setDeleteTs(LocalDateTime.now());
        repository.save(entity);

        log.info("DELETE /entities/hemishe_EPublicationScientific/{} - soft delete muvaffaqiyatli", entityId);

        // OLD-HEMIS: 200 OK (not 204)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Ilmiy nashrlar qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search PublicationScientific with filter: {}", filter);

        List<PublicationScientific> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Ilmiy nashrlar qidirish (POST)")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search PublicationScientific with filter: {}", filter);

        List<PublicationScientific> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha ilmiy nashrlar (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    @Operation(summary = "Barcha ilmiy nashrlar ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all PublicationScientific - offset: {}, limit: {}", offset, limit);

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
        Page<PublicationScientific> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST - Yangi ilmiy nashr yaratish
    // =====================================================

    @PostMapping
    @Transactional
    @Operation(
        summary = "Ilmiy nashr yaratish",
        description = """
            Yangi ilmiy nashr yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EPublicationScientific

            **Misol request body:**
            ```json
            {
                "name": "Quantum Computing in Medicine",
                "authors": "Aliyev A., Karimov B.",
                "authorCounts": 2,
                "issueYear": 2024,
                "doi": "10.1234/example.2024",
                "active": true
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EPublicationScientific\",\"_instanceName\":\"com.company.hemishe.entity.EPublicationScientific-uuid [detached]\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new PublicationScientific");
        log.debug("Request body: {}", body);

        PublicationScientific entity = new PublicationScientific();

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

        PublicationScientific saved = repository.save(entity);
        log.info("PublicationScientific created with id: {}", saved.getId());

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
     * OLD-HEMIS format: com.company.hemishe.entity.EPublicationScientific-{id} [detached]
     */
    private String buildInstanceName(PublicationScientific entity) {
        return CUBA_ENTITY_CLASS + "-" + entity.getId() + " [detached]";
    }

    /**
     * Entity -> OLD-HEMIS Map formatiga o'girish
     * Field nomlari OLD-HEMIS ga 100% mos bo'lishi kerak (camelCase)!
     */
    private Map<String, Object> toMap(PublicationScientific entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS exact field order
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // Fields in OLD-HEMIS order (camelCase!)
        putIfNotNull(map, "uId", entity.getUId(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "keywords", entity.getKeywords(), returnNulls);
        putIfNotNull(map, "authors", entity.getAuthors(), returnNulls);
        putIfNotNull(map, "authorCounts", entity.getAuthorCounts(), returnNulls);
        putIfNotNull(map, "sourceName", entity.getSourceName(), returnNulls);
        putIfNotNull(map, "issueYear", entity.getIssueYear(), returnNulls);
        putIfNotNull(map, "parameter", entity.getParameter(), returnNulls);
        putIfNotNull(map, "doi", entity.getDoi(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        putIfNotNull(map, "position", entity.getPosition(), returnNulls);
        // active - OLD-HEMIS har doim qaytaradi
        map.put("active", entity.getActive());
        putIfNotNull(map, "translations", entity.getTranslations(), returnNulls);
        putIfNotNull(map, "isChecked", entity.getIsChecked(), returnNulls);
        putIfNotNull(map, "isCheckedDate", entity.getIsCheckedDate(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        // Foreign key references (String -> nested object with id)
        putForeignKey(map, "university", entity.getUniversity(), returnNulls);
        putForeignKey(map, "scientificPublicationType", entity.getScientificPublicationType(), returnNulls);
        putForeignKey(map, "publicationDatabase", entity.getPublicationDatabase(), returnNulls);
        putForeignKey(map, "locality", entity.getLocality(), returnNulls);
        putForeignKey(map, "country", entity.getCountry(), returnNulls);
        putForeignKey(map, "employee", entity.getEmployee(), returnNulls);
        putForeignKey(map, "educationYear", entity.getEducationYear(), returnNulls);

        // Audit fields
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);

        return map;
    }

    /**
     * OLD-HEMIS Map -> Entity ga o'girish
     */
    private void updateFromMap(PublicationScientific entity, Map<String, Object> map) {
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

        // keywords
        if (map.containsKey("keywords")) {
            Object val = map.get("keywords");
            entity.setKeywords(val != null ? val.toString() : null);
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

        // sourceName
        if (map.containsKey("sourceName")) {
            Object val = map.get("sourceName");
            entity.setSourceName(val != null ? val.toString() : null);
        }

        // issueYear
        if (map.containsKey("issueYear")) {
            Object val = map.get("issueYear");
            if (val != null) {
                entity.setIssueYear(Integer.parseInt(val.toString()));
            }
        }

        // parameter
        if (map.containsKey("parameter")) {
            Object val = map.get("parameter");
            entity.setParameter(val != null ? val.toString() : null);
        }

        // doi
        if (map.containsKey("doi")) {
            Object val = map.get("doi");
            entity.setDoi(val != null ? val.toString() : null);
        }

        // filename
        if (map.containsKey("filename")) {
            Object val = map.get("filename");
            entity.setFilename(val != null ? val.toString() : null);
        }

        // position
        if (map.containsKey("position")) {
            Object val = map.get("position");
            entity.setPosition(val != null ? val.toString() : null);
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

        if (map.containsKey("scientificPublicationType")) {
            entity.setScientificPublicationType(extractStringId(map.get("scientificPublicationType")));
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
     * CUBA format: {"id": "uuid"} - faqat Map qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private String extractStringId(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }
}
