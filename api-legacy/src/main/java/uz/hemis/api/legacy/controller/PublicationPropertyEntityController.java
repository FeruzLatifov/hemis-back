package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

    // =====================================================
    // GET /{entityId} - Bitta ilmiy ishlanmani olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Ilmiy ishlanmani olish",
        description = "ID bo'yicha ilmiy ishlanma (intellektual mulk) ma'lumotlarini olish"
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
        PublicationProperty entity = entityOpt.get();
        entity.setDeleteTs(LocalDateTime.now());
        repository.save(entity);

        log.info("DELETE /entities/hemishe_EPublicationProperty/{} - soft delete muvaffaqiyatli", entityId);
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

        // Default limit
        if (limit == null || limit <= 0) {
            limit = 100;
        }
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
        description = "Yangi ilmiy ishlanma (patent, ixtiro, foydali model) yaratish"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Ilmiy ishlanma ma'lumotlari",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Yangi patent",
                        value = """
                            {
                              "name": "Yangi innovatsion texnologiya",
                              "numbers": "IAP 2024/001",
                              "authors": "Karimov A.B., Sodiqov D.E.",
                              "authorCounts": 2,
                              "propertyDate": "2024-01-15",
                              "active": true
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new PublicationProperty");
        log.debug("Request body: {}", body);

        PublicationProperty entity = new PublicationProperty();

        if (body.containsKey("id")) {
            try {
                entity.setId(UUID.fromString(body.get("id").toString()));
            } catch (Exception e) {
                log.warn("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        updateFromMap(entity, body);
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        PublicationProperty saved = repository.save(entity);
        log.info("PublicationProperty created with id: {}", saved.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private String buildInstanceName(PublicationProperty entity) {
        return entity.getName() != null ? entity.getName() : "";
    }

    private Map<String, Object> toMap(PublicationProperty entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "numbers", entity.getNumbers(), returnNulls);
        putIfNotNull(map, "propertyDate", entity.getPropertyDate(), returnNulls);
        putIfNotNull(map, "authorCounts", entity.getAuthorCounts(), returnNulls);
        putIfNotNull(map, "parameter", entity.getParameter(), returnNulls);
        map.put("active", entity.getActive());
        putIfNotNull(map, "isChecked", entity.getIsChecked(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "authors", entity.getAuthors(), returnNulls);

        return map;
    }

    private void updateFromMap(PublicationProperty entity, Map<String, Object> map) {
        // uId
        if (map.containsKey("uId")) {
            entity.setUId(extractInteger(map.get("uId")));
        }
        if (map.containsKey("u_id")) {
            entity.setUId(extractInteger(map.get("u_id")));
        }

        // name
        if (map.containsKey("name")) {
            entity.setName(extractString(map.get("name")));
        }

        // numbers
        if (map.containsKey("numbers")) {
            entity.setNumbers(extractString(map.get("numbers")));
        }

        // authors
        if (map.containsKey("authors")) {
            entity.setAuthors(extractString(map.get("authors")));
        }

        // authorCounts
        if (map.containsKey("authorCounts")) {
            entity.setAuthorCounts(extractInteger(map.get("authorCounts")));
        }
        if (map.containsKey("author_counts")) {
            entity.setAuthorCounts(extractInteger(map.get("author_counts")));
        }

        // parameter
        if (map.containsKey("parameter")) {
            entity.setParameter(extractString(map.get("parameter")));
        }

        // propertyDate
        if (map.containsKey("propertyDate")) {
            entity.setPropertyDate(extractLocalDate(map.get("propertyDate")));
        }
        if (map.containsKey("property_date")) {
            entity.setPropertyDate(extractLocalDate(map.get("property_date")));
        }

        // filename
        if (map.containsKey("filename")) {
            entity.setFilename(extractString(map.get("filename")));
        }

        // position
        if (map.containsKey("position")) {
            entity.setPosition(extractInteger(map.get("position")));
        }

        // active
        if (map.containsKey("active")) {
            entity.setActive(extractBoolean(map.get("active")));
        }

        // translations
        if (map.containsKey("translations")) {
            entity.setTranslations(extractString(map.get("translations")));
        }

        // isChecked
        if (map.containsKey("isChecked")) {
            entity.setIsChecked(extractBoolean(map.get("isChecked")));
        }
        if (map.containsKey("is_checked")) {
            entity.setIsChecked(extractBoolean(map.get("is_checked")));
        }

        // isCheckedDate
        if (map.containsKey("isCheckedDate")) {
            entity.setIsCheckedDate(extractLocalDateTime(map.get("isCheckedDate")));
        }
        if (map.containsKey("is_checked_date")) {
            entity.setIsCheckedDate(extractLocalDateTime(map.get("is_checked_date")));
        }

        // Foreign keys (camelCase va snake_case)
        if (map.containsKey("university")) {
            entity.setUniversity(extractString(map.get("university")));
        }
        if (map.containsKey("_university")) {
            entity.setUniversity(extractString(map.get("_university")));
        }

        if (map.containsKey("patentType")) {
            entity.setPatentType(extractString(map.get("patentType")));
        }
        if (map.containsKey("_patent_type")) {
            entity.setPatentType(extractString(map.get("_patent_type")));
        }

        if (map.containsKey("publicationDatabase")) {
            entity.setPublicationDatabase(extractString(map.get("publicationDatabase")));
        }
        if (map.containsKey("_publication_database")) {
            entity.setPublicationDatabase(extractString(map.get("_publication_database")));
        }

        if (map.containsKey("locality")) {
            entity.setLocality(extractString(map.get("locality")));
        }
        if (map.containsKey("_locality")) {
            entity.setLocality(extractString(map.get("_locality")));
        }

        if (map.containsKey("country")) {
            entity.setCountry(extractString(map.get("country")));
        }
        if (map.containsKey("_country")) {
            entity.setCountry(extractString(map.get("_country")));
        }

        // employee - UUID (faqat shu field UUID)
        if (map.containsKey("employee")) {
            entity.setEmployee(extractStringId(map.get("employee")));
        }
        if (map.containsKey("_employee")) {
            entity.setEmployee(extractStringId(map.get("_employee")));
        }

        if (map.containsKey("educationYear")) {
            entity.setEducationYear(extractString(map.get("educationYear")));
        }
        if (map.containsKey("_education_year")) {
            entity.setEducationYear(extractString(map.get("_education_year")));
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

    private String extractString(Object value) {
        if (value == null) return null;
        return value.toString();
    }

    private Integer extractInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean extractBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private LocalDate extractLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) return null;
            try {
                return LocalDate.parse(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime extractLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) return null;
            try {
                return LocalDateTime.parse(str);
            } catch (Exception e) {
                try {
                    return LocalDate.parse(str).atStartOfDay();
                } catch (Exception e2) {
                    return null;
                }
            }
        }
        return null;
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
