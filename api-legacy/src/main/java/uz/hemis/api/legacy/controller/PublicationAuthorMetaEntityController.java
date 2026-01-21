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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PublicationAuthorMeta Entity Controller (CUBA Pattern)
 * Tag 25: Nashr mualliflari meta ma'lumotlari
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EPublicationAuthorMeta
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_EPublicationAuthorMeta
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Nested objects for university, employee, publications
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EPublicationAuthorMeta           - List all with pagination
 * - GET    /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{id}      - Get by ID
 * - POST   /app/rest/v2/entities/hemishe_EPublicationAuthorMeta           - Create new
 * - PUT    /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{id}      - Soft delete
 */
@Tag(name = "25.Nashr mualliflari meta ma'lumotlari",
     description = "Nashr mualliflari meta ma'lumotlarini boshqarish - ilmiy, metodik va patent nashrlar mualliflarini ro'yxatga olish")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationAuthorMeta")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationAuthorMetaEntityController {

    private final PublicationAuthorMetaRepository repository;

    private static final String ENTITY_NAME = "hemishe_EPublicationAuthorMeta";

    // =====================================================
    // GET - List all with pagination
    // =====================================================
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Barcha nashr mualliflari ro'yxati",
               description = "Paginatsiya bilan barcha nashr mualliflari meta ma'lumotlarini olish")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Offset - boshlanish pozitsiyasi")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit - maksimal yozuvlar soni")
            @RequestParam(defaultValue = "100") Integer limit,
            @Parameter(description = "Saralash: field-asc yoki field-desc")
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all PublicationAuthorMeta - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<PublicationAuthorMeta> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Get by ID
    // =====================================================
    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(summary = "ID bo'yicha nashr muallifi olish",
               description = "UUID orqali bitta nashr muallifi meta ma'lumotini olish")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Entity UUID") @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET PublicationAuthorMeta by id: {}", entityId);

        Optional<PublicationAuthorMeta> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // POST - Create new
    // =====================================================
    @PostMapping
    @Transactional
    @Operation(summary = "Yangi nashr muallifi yaratish",
               description = "Yangi nashr muallifi meta ma'lumotini yaratish")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new PublicationAuthorMeta: {}", body);

        PublicationAuthorMeta entity = new PublicationAuthorMeta();
        updateFromMap(entity, body);

        PublicationAuthorMeta saved = repository.save(entity);

        // OLD-HEMIS POST da faqat minimal response qaytaradi
        return ResponseEntity.status(HttpStatus.CREATED).body(toMinimalMap(saved));
    }

    // =====================================================
    // PUT - Update existing
    // =====================================================
    @PutMapping("/{entityId}")
    @Transactional
    @Operation(summary = "Nashr muallifini yangilash",
               description = "Mavjud nashr muallifi meta ma'lumotini yangilash")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Entity UUID") @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT PublicationAuthorMeta id: {}, body: {}", entityId, body);

        Optional<PublicationAuthorMeta> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationAuthorMeta entity = existingOpt.get();
        updateFromMap(entity, body);

        PublicationAuthorMeta saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    // =====================================================
    // DELETE - Soft delete
    // =====================================================
    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(summary = "Nashr muallifini o'chirish",
               description = "Nashr muallifi meta ma'lumotini soft delete qilish (delete_ts o'rnatiladi)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@Parameter(description = "Entity UUID") @PathVariable UUID entityId) {
        log.debug("DELETE PublicationAuthorMeta id: {}", entityId);

        Optional<PublicationAuthorMeta> entityOpt = repository.findById(entityId);
        if (entityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // SOFT DELETE: delete_ts o'rnatish (CUBA pattern)
        PublicationAuthorMeta entity = entityOpt.get();
        entity.setDeleteTs(LocalDateTime.now());
        repository.save(entity);

        return ResponseEntity.ok().build();
    }

    // =====================================================
    // toMap - OLD-HEMIS formatiga 100% mos
    // OLD-HEMIS nested objectlar qaytarmaydi, faqat flat fieldlar
    // =====================================================
    private Map<String, Object> toMap(PublicationAuthorMeta entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // CUBA standard fields - OLD-HEMIS tartibi
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", "com.company.hemishe.entity.EPublicationAuthorMeta-" + entity.getId() + " [detached]");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // isCheckedByAuthor
        putIfNotNull(map, "isCheckedByAuthor", entity.getIsCheckedByAuthor(), returnNulls);

        // active
        putIfNotNull(map, "active", entity.getActive(), returnNulls);

        // version - OLD-HEMIS da bor
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        // publicationTypeTable
        putIfNotNull(map, "publicationTypeTable", entity.getPublicationTypeTable(), returnNulls);

        // isMainAuthor
        putIfNotNull(map, "isMainAuthor", entity.getIsMainAuthor(), returnNulls);

        // NOTE: OLD-HEMIS nested objectlar qaytarmaydi (university, employee, publication*)

        return map;
    }

    // =====================================================
    // updateFromMap - Request body dan entity yangilash
    // =====================================================
    @SuppressWarnings("unchecked")
    private void updateFromMap(PublicationAuthorMeta entity, Map<String, Object> map) {
        // isCheckedByAuthor - Boolean yoki String bo'lishi mumkin
        if (map.containsKey("isCheckedByAuthor")) {
            Object val = map.get("isCheckedByAuthor");
            if (val instanceof Boolean) {
                entity.setIsCheckedByAuthor((Boolean) val);
            } else if (val instanceof String) {
                entity.setIsCheckedByAuthor(Boolean.parseBoolean((String) val));
            }
        }

        // active - Boolean yoki String bo'lishi mumkin
        if (map.containsKey("active")) {
            Object val = map.get("active");
            if (val instanceof Boolean) {
                entity.setActive((Boolean) val);
            } else if (val instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) val));
            }
        }

        // isMainAuthor - Number yoki String bo'lishi mumkin
        if (map.containsKey("isMainAuthor")) {
            Object val = map.get("isMainAuthor");
            if (val instanceof Number) {
                entity.setIsMainAuthor(((Number) val).intValue());
            } else if (val instanceof String) {
                entity.setIsMainAuthor(Integer.parseInt((String) val));
            }
        }

        // publicationTypeTable
        if (map.containsKey("publicationTypeTable")) {
            entity.setPublicationTypeTable((String) map.get("publicationTypeTable"));
        }

        // position
        if (map.containsKey("position")) {
            Object val = map.get("position");
            if (val instanceof Number) {
                entity.setPosition(((Number) val).intValue());
            }
        }

        // university - CUBA nested object format: {"code": "401"}
        if (map.containsKey("university")) {
            Object universityVal = map.get("university");
            if (universityVal instanceof Map) {
                Map<String, Object> universityMap = (Map<String, Object>) universityVal;
                entity.setUniversity((String) universityMap.getOrDefault("code", universityMap.get("id")));
            }
        }

        // employee - CUBA nested object format: {"id": "uuid"}
        if (map.containsKey("employee")) {
            Object employeeVal = map.get("employee");
            if (employeeVal instanceof Map) {
                Map<String, Object> employeeMap = (Map<String, Object>) employeeVal;
                entity.setEmployee(UUID.fromString((String) employeeMap.get("id")));
            }
        }

        // publicationScientific - CUBA nested object format: {"id": "uuid"}
        if (map.containsKey("publicationScientific")) {
            Object pubVal = map.get("publicationScientific");
            if (pubVal instanceof Map) {
                Map<String, Object> pubMap = (Map<String, Object>) pubVal;
                entity.setPublicationScientific(UUID.fromString((String) pubMap.get("id")));
            }
        }

        // publicationProperty - CUBA nested object format: {"id": "uuid"}
        if (map.containsKey("publicationProperty")) {
            Object pubVal = map.get("publicationProperty");
            if (pubVal instanceof Map) {
                Map<String, Object> pubMap = (Map<String, Object>) pubVal;
                entity.setPublicationProperty(UUID.fromString((String) pubMap.get("id")));
            }
        }

        // publicationMethodical - CUBA nested object format: {"id": "uuid"}
        if (map.containsKey("publicationMethodical")) {
            Object pubVal = map.get("publicationMethodical");
            if (pubVal instanceof Map) {
                Map<String, Object> pubMap = (Map<String, Object>) pubVal;
                entity.setPublicationMethodical(UUID.fromString((String) pubMap.get("id")));
            }
        }
    }

    // =====================================================
    // Helper methods
    // =====================================================
    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    /**
     * OLD-HEMIS POST response format - faqat minimal fieldlar
     * _entityName, _instanceName, id
     */
    private Map<String, Object> toMinimalMap(PublicationAuthorMeta entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", "com.company.hemishe.entity.EPublicationAuthorMeta-" + entity.getId() + " [detached]");
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        return map;
    }
}
