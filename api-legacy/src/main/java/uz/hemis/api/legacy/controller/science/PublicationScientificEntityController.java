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
import uz.hemis.domain.entity.research.PublicationScientific;
import uz.hemis.service.legacy.science.ScienceDoctorateEntityLegacyService;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

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

    private final ScienceDoctorateEntityLegacyService scienceService;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EPublicationScientific";

    // =====================================================
    // GET /{entityId} - Bitta ilmiy nashrni olish
    // =====================================================

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping("/{entityId}")
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

        Optional<PublicationScientific> entity = scienceService.findPublicationScientificById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EPublicationScientific/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(scienceService.toPublicationScientificMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Ilmiy nashrni yangilash
    // =====================================================

    @PreAuthorize("hasAuthority('science.edit')")
    @PutMapping("/{entityId}")
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

        Optional<PublicationScientific> existingOpt = scienceService.findPublicationScientificById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationScientific entity = existingOpt.get();
        scienceService.updatePublicationScientificFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        PublicationScientific saved = scienceService.savePublicationScientific(entity);

        return ResponseEntity.ok(scienceService.toPublicationScientificMinimalMap(saved));
    }

    // =====================================================
    // DELETE /{entityId} - Ilmiy nashrni o'chirish
    // =====================================================

    @PreAuthorize("hasAuthority('science.delete')")
    @DeleteMapping("/{entityId}")
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

        Optional<PublicationScientific> entityOpt = scienceService.findPublicationScientificById(entityId);
        if (entityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        scienceService.deletePublicationScientific(entityOpt.get());

        log.info("DELETE /entities/hemishe_EPublicationScientific/{} - soft delete muvaffaqiyatli", entityId);

        // OLD-HEMIS: 200 OK (not 204)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping("/search")
    @Operation(summary = "Ilmiy nashrlar qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<PublicationScientific> allEntities = scienceService.findAllPublicationScientific();
        List<PublicationScientific> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toPublicationScientificMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PreAuthorize("hasAuthority('science.view')")
    @PostMapping("/search")
    @Operation(summary = "Ilmiy nashrlar qidirish (POST)")
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

        List<PublicationScientific> allEntities = scienceService.findAllPublicationScientific();
        List<PublicationScientific> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> scienceService.toPublicationScientificMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha ilmiy nashrlar (paginated)
    // =====================================================

    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping({"", "/"})
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

        int safeLimit = Math.max(limit, 1);
        int page = offset / safeLimit;
        PageRequest pageRequest = PageRequest.of(page, safeLimit, sorting);
        Page<PublicationScientific> entityPage = scienceService.findAllPublicationScientific(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> scienceService.toPublicationScientificMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST - Yangi ilmiy nashr yaratish
    // =====================================================

    @PreAuthorize("hasAuthority('science.edit')")
    @PostMapping
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

        log.info("POST create/upsert PublicationScientific");
        log.debug("Request body: {}", body);

        // CUBA UPSERT: if body contains 'id' and entity exists, update instead of create
        if (body.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(body.get("id").toString());
                var existingOpt = scienceService.findPublicationScientificById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    PublicationScientific entity = existingOpt.get();
                    scienceService.updatePublicationScientificFromMap(entity, body);
                    entity.setUpdateTs(LocalDateTime.now());
                    PublicationScientific saved = scienceService.savePublicationScientific(entity);
                    return ResponseEntity.ok(scienceService.toPublicationScientificMinimalMap(saved));
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        PublicationScientific entity = new PublicationScientific();

        // Agar id berilgan bo'lsa, ishlatish (OLD-HEMIS pattern)
        if (body.containsKey("id")) {
            try {
                entity.setId(UUID.fromString(body.get("id").toString()));
            } catch (Exception e) {
                log.warn("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        scienceService.updatePublicationScientificFromMap(entity, body);

        // Version va timestamps
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        PublicationScientific saved = scienceService.savePublicationScientific(entity);
        log.info("PublicationScientific created with id: {}", saved.getId());

        return ResponseEntity.ok(scienceService.toPublicationScientificMinimalMap(saved));
    }

}
