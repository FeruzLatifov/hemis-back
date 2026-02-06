package uz.hemis.api.legacy.controller.science;

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
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.PublicationProperty;
import uz.hemis.service.legacy.science.ScienceDoctorateEntityLegacyService;

import uz.hemis.api.legacy.util.CubaFilterHelper;

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

    private final ScienceDoctorateEntityLegacyService scienceService;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EPublicationProperty";

    // =====================================================
    // GET /{entityId} - Bitta ilmiy ishlanmani olish
    // =====================================================

    @GetMapping("/{entityId}")
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

        Optional<PublicationProperty> entity = scienceService.findPublicationPropertyById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EPublicationProperty/{} - topilmadi", entityId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(scienceService.toPublicationPropertyMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Ilmiy ishlanmani yangilash
    // =====================================================

    @PutMapping("/{entityId}")
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

        Optional<PublicationProperty> existingOpt = scienceService.findPublicationPropertyById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationProperty entity = existingOpt.get();
        scienceService.updatePublicationPropertyFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        PublicationProperty saved = scienceService.savePublicationProperty(entity);

        return ResponseEntity.ok(scienceService.toPublicationPropertyMinimalMap(saved));
    }

    // =====================================================
    // DELETE /{entityId} - Ilmiy ishlanmani o'chirish
    // =====================================================

    @DeleteMapping("/{entityId}")
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

        Optional<PublicationProperty> entityOpt = scienceService.findPublicationPropertyById(entityId);
        if (entityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        scienceService.deletePublicationProperty(entityOpt.get());

        log.info("DELETE /entities/hemishe_EPublicationProperty/{} - soft delete muvaffaqiyatli", entityId);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Operation(summary = "Ilmiy ishlanmalar qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<PublicationProperty> allEntities = scienceService.findAllPublicationProperty();
        List<PublicationProperty> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toPublicationPropertyMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Operation(summary = "Ilmiy ishlanmalar qidirish (POST)")
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

        List<PublicationProperty> allEntities = scienceService.findAllPublicationProperty();
        List<PublicationProperty> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toPublicationPropertyMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha ilmiy ishlanmalar (paginated)
    // =====================================================

    @GetMapping({"", "/"})
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
        Page<PublicationProperty> entityPage = scienceService.findAllPublicationProperty(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> scienceService.toPublicationPropertyMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST - Yangi ilmiy ishlanma yaratish
    // =====================================================

    @PostMapping
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

        log.info("POST create/upsert PublicationProperty");
        log.debug("Request body: {}", body);

        // CUBA UPSERT: if body contains 'id' and entity exists, update instead of create
        if (body.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(body.get("id").toString());
                Optional<PublicationProperty> existingOpt = scienceService.findPublicationPropertyById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    PublicationProperty existing = existingOpt.get();
                    scienceService.updatePublicationPropertyFromMap(existing, body);
                    existing.setUpdateTs(LocalDateTime.now());
                    PublicationProperty saved = scienceService.savePublicationProperty(existing);
                    return ResponseEntity.ok(scienceService.toPublicationPropertyMap(saved, returnNulls));
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        PublicationProperty entity = new PublicationProperty();

        scienceService.updatePublicationPropertyFromMap(entity, body);
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        PublicationProperty saved = scienceService.savePublicationProperty(entity);
        log.info("PublicationProperty created with id: {}", saved.getId());

        return ResponseEntity.ok(scienceService.toPublicationPropertyMinimalMap(saved));
    }

}
