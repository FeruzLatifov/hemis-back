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
import uz.hemis.domain.entity.MethodicalPublicationType;
import uz.hemis.domain.repository.MethodicalPublicationTypeRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Uslubiy nashr turlari Entity Controller (CUBA Pattern)
 * Tag 27: Uslubiy nashr turlari
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_HMethodicalPublicationType
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_HMethodicalPublicationType           - List all
 * - GET    /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{id}      - Get by ID
 * - POST   /app/rest/v2/entities/hemishe_HMethodicalPublicationType           - Create new
 * - PUT    /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{id}      - Soft delete
 */
@Tag(name = "27.Uslubiy nashr turlari", description = "Uslubiy nashr turlari klassifikatori (darslik, o'quv qo'llanma, metodik qo'llanma va boshqalar)")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HMethodicalPublicationType")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class MethodicalPublicationTypeEntityController {

    private final MethodicalPublicationTypeRepository repository;
    private static final String ENTITY_NAME = "hemishe_HMethodicalPublicationType";

    /**
     * Barcha uslubiy nashr turlarini olish
     */
    @GetMapping
    @Operation(summary = "Barcha uslubiy nashr turlarini olish", description = "Sahifalangan ro'yxatni qaytaradi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "[{\"_entityName\":\"hemishe_HMethodicalPublicationType\",\"_instanceName\":\"11 Darslik\",\"id\":\"11\",\"code\":\"11\",\"name\":\"Darslik\",\"active\":true,\"version\":1}]")))
    })
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Jami sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifa hajmi") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Saralash (masalan: code-asc)") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all MethodicalPublicationType - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.by(Sort.Direction.ASC, "code");
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        // Simple list without pagination for classifiers (usually small datasets)
        List<MethodicalPublicationType> entities = repository.findAllByDeleteTsIsNull();

        // Sort
        if ("desc".equalsIgnoreCase(sort != null && sort.contains("-") ? sort.split("-")[1] : "")) {
            entities.sort((a, b) -> b.getCode().compareTo(a.getCode()));
        } else {
            entities.sort((a, b) -> a.getCode().compareTo(b.getCode()));
        }

        // Apply offset and limit
        List<Map<String, Object>> result = entities.stream()
            .skip(offset)
            .limit(limit)
            .map(this::toMapForList)
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * ID bo'yicha uslubiy nashr turini olish
     * Regex: "search" so'zini exclude qiladi, chunki /search alohida endpoint
     */
    @GetMapping("/{entityId:(?!search$).*}")
    @Operation(summary = "Uslubiy nashr turini ID bo'yicha olish", description = "Code bo'yicha bitta uslubiy nashr turini qaytaradi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Uslubiy nashr turi kodi", example = "11") @PathVariable String entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET MethodicalPublicationType by id: {}", entityId);

        Optional<MethodicalPublicationType> entity = repository.findByCodeAndDeleteTsIsNull(entityId);
        if (entity.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(toMapForList(entity.get()));
    }

    /**
     * Yangi uslubiy nashr turi yaratish
     *
     * OLD-HEMIS request format: {"code": "99", "name": "Test", "active": true}
     * OLD-HEMIS response format: {"_entityName": "...", "_instanceName": "99 Test", "id": "99"}
     */
    @PostMapping
    @Operation(summary = "Yangi uslubiy nashr turi yaratish", description = "Yangi uslubiy nashr turini yaratadi va saqlaydi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_HMethodicalPublicationType\",\"_instanceName\":\"99 Test\",\"id\":\"99\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Uslubiy nashr turi ma'lumotlari",
                content = @Content(
                    examples = @ExampleObject(
                        value = """
                            {
                              "code": "99",
                              "name": "Yangi nashr turi",
                              "active": true
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new MethodicalPublicationType: {}", body);

        // Validate required fields
        String code = extractString(body.get("code"));
        if (code == null || code.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Validation error");
            errorResponse.put("details", "Field 'code' is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // OLD-HEMIS behavior: if exists (including soft-deleted), update and return (upsert)
        // Use native query to bypass @SQLRestriction and find even soft-deleted records
        Optional<MethodicalPublicationType> existingOpt = repository.findByCodeIncludingDeleted(code);
        MethodicalPublicationType entity;
        if (existingOpt.isPresent()) {
            entity = existingOpt.get();
            // Restore if soft-deleted
            entity.setDeleteTs(null);
            entity.setDeletedBy(null);
            updateFromMap(entity, body);
        } else {
            entity = new MethodicalPublicationType();
            entity.setCode(code);
            updateFromMap(entity, body);
        }

        MethodicalPublicationType saved = repository.save(entity);

        // OLD-HEMIS POST response format: faqat 3 ta field
        return ResponseEntity.ok(toMapForCreate(saved));
    }

    /**
     * Uslubiy nashr turini yangilash
     */
    @PutMapping("/{entityId:(?!search$).*}")
    @Operation(summary = "Uslubiy nashr turini yangilash", description = "Mavjud uslubiy nashr turini yangilaydi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Uslubiy nashr turi kodi", example = "11") @PathVariable String entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT MethodicalPublicationType id: {}", entityId);

        Optional<MethodicalPublicationType> existingOpt = repository.findByCodeAndDeleteTsIsNull(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        MethodicalPublicationType entity = existingOpt.get();
        updateFromMap(entity, body);

        MethodicalPublicationType saved = repository.save(entity);
        return ResponseEntity.ok(toMapForList(saved));
    }

    /**
     * Uslubiy nashr turini o'chirish (soft delete)
     */
    @DeleteMapping("/{entityId:(?!search$).*}")
    @Operation(summary = "Uslubiy nashr turini o'chirish", description = "Uslubiy nashr turini soft delete qiladi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> delete(
            @Parameter(description = "Uslubiy nashr turi kodi", example = "11") @PathVariable String entityId) {

        log.debug("DELETE MethodicalPublicationType id: {}", entityId);

        Optional<MethodicalPublicationType> entity = repository.findByCodeAndDeleteTsIsNull(entityId);
        if (entity.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        MethodicalPublicationType toDelete = entity.get();
        toDelete.setDeleteTs(LocalDateTime.now());
        repository.save(toDelete);

        return ResponseEntity.ok().build();
    }

    /**
     * Filter bo'yicha qidirish (GET)
     * OLD-HEMIS: GET /entities/hemishe_HMethodicalPublicationType/search?filter={...}
     * Filter parametri MAJBURIY - bo'lmasa "Server error" qaytaradi
     */
    @GetMapping("/search")
    @Operation(summary = "Filter bo'yicha qidirish (GET)", description = "Filter shartlari bo'yicha entitylarni topadi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "500", description = "Server error - filter yo'q")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchGet(
            @Parameter(description = "Filter JSON (majburiy)", example = "{\"conditions\":[]}")
            @RequestParam(required = false) String filter,
            @Parameter(description = "Jami sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifa hajmi") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Saralash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search MethodicalPublicationType - filter: {}", filter);

        // OLD-HEMIS: filter parametri majburiy, bo'lmasa "Server error" qaytaradi
        if (filter == null || filter.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Server error");
            errorResponse.put("details", "");
            return ResponseEntity.status(500).body(errorResponse);
        }

        return ResponseEntity.ok(performSearch(offset, limit, sort));
    }

    /**
     * Filter bo'yicha qidirish (POST)
     * OLD-HEMIS: POST /entities/hemishe_HMethodicalPublicationType/search
     * Body: {"filter": {"conditions": []}} formatida bo'lishi kerak
     */
    @PostMapping("/search")
    @Operation(summary = "Filter bo'yicha qidirish (POST)", description = "Filter shartlari bo'yicha entitylarni topadi (body da filter)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri filter")
    })
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Jami sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifa hajmi") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Saralash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search MethodicalPublicationType - body: {}", body);

        // OLD-HEMIS: body da "filter" kaliti bo'lishi kerak
        if (body == null || !body.containsKey("filter")) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Cannot parse entities filter");
            errorResponse.put("details", "Entities filter cannot be null");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        return ResponseEntity.ok(performSearch(offset, limit, sort));
    }

    /**
     * Umumiy search logikasi
     */
    private List<Map<String, Object>> performSearch(Integer offset, Integer limit, String sort) {
        // Oddiy klassifikator uchun barcha entitylarni qaytaramiz
        List<MethodicalPublicationType> entities = repository.findAllByDeleteTsIsNull();

        // Sort
        if (sort != null && !sort.isEmpty()) {
            String field = sort.startsWith("+") || sort.startsWith("-") ? sort.substring(1) : sort;
            boolean desc = sort.startsWith("-");
            if ("code".equalsIgnoreCase(field)) {
                entities.sort((a, b) -> desc ? b.getCode().compareTo(a.getCode()) : a.getCode().compareTo(b.getCode()));
            } else if ("name".equalsIgnoreCase(field)) {
                entities.sort((a, b) -> {
                    String nameA = a.getName() != null ? a.getName() : "";
                    String nameB = b.getName() != null ? b.getName() : "";
                    return desc ? nameB.compareTo(nameA) : nameA.compareTo(nameB);
                });
            }
        } else {
            entities.sort((a, b) -> a.getCode().compareTo(b.getCode()));
        }

        // Apply offset and limit
        return entities.stream()
            .skip(offset)
            .limit(limit)
            .map(this::toMapForSearch)
            .collect(Collectors.toList());
    }

    /**
     * OLD-HEMIS GET list format: barcha fieldlar
     */
    private Map<String, Object> toMapForList(MethodicalPublicationType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getCode() + " " + (entity.getName() != null ? entity.getName() : ""));
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        map.put("name", entity.getName());
        map.put("active", entity.getActive() != null ? entity.getActive() : true);
        map.put("version", entity.getVersion() != null ? entity.getVersion() : 1);
        return map;
    }

    /**
     * OLD-HEMIS search response format: deletedBy ham qaytariladi
     */
    private Map<String, Object> toMapForSearch(MethodicalPublicationType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getCode() + " " + (entity.getName() != null ? entity.getName() : ""));
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        map.put("name", entity.getName());
        map.put("active", entity.getActive() != null ? entity.getActive() : true);
        map.put("version", entity.getVersion() != null ? entity.getVersion() : 1);
        // OLD-HEMIS search responseda deletedBy ham qaytariladi (agar mavjud bo'lsa)
        if (entity.getDeletedBy() != null) {
            map.put("deletedBy", entity.getDeletedBy());
        }
        return map;
    }

    /**
     * OLD-HEMIS POST response format: faqat 3 ta field
     */
    private Map<String, Object> toMapForCreate(MethodicalPublicationType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getCode() + " " + (entity.getName() != null ? entity.getName() : ""));
        map.put("id", entity.getCode());
        return map;
    }

    private void updateFromMap(MethodicalPublicationType entity, Map<String, Object> map) {
        if (map.containsKey("name")) {
            entity.setName(extractString(map.get("name")));
        }
        if (map.containsKey("nameRu") || map.containsKey("name_ru")) {
            entity.setNameRu(extractString(map.getOrDefault("nameRu", map.get("name_ru"))));
        }
        if (map.containsKey("nameEn") || map.containsKey("name_en")) {
            entity.setNameEn(extractString(map.getOrDefault("nameEn", map.get("name_en"))));
        }
        if (map.containsKey("active")) {
            entity.setActive(extractBoolean(map.get("active")));
        }
    }

    private String extractString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        return value.toString();
    }

    private Boolean extractBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
}
