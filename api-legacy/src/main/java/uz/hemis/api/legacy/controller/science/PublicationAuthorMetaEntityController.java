package uz.hemis.api.legacy.controller.science;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uz.hemis.domain.entity.research.PublicationAuthorMeta;
import uz.hemis.service.legacy.science.ScienceEntityLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

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
@Tag(name = "25.Ilmiy nashr mualliflari meta ma'lumotlari",
     description = "Nashr mualliflari meta ma'lumotlarini boshqarish - ilmiy, metodik va patent nashrlar mualliflarini ro'yxatga olish")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationAuthorMeta")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationAuthorMetaEntityController {

    private final ScienceEntityLegacyService scienceService;

    private static final String ENTITY_NAME = "hemishe_EPublicationAuthorMeta";

    // =====================================================
    // GET - List all with pagination
    // =====================================================
    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping
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
        Page<PublicationAuthorMeta> entityPage = scienceService.findAllPublicationAuthorMeta(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> scienceService.toPublicationAuthorMetaMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Get by ID
    // =====================================================
    @PreAuthorize("hasAuthority('science.view')")
    @GetMapping("/{entityId}")
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

        Optional<PublicationAuthorMeta> entity = scienceService.findPublicationAuthorMetaById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(scienceService.toPublicationAuthorMetaMap(entity.get(), returnNulls));
    }

    // =====================================================
    // POST - Create new
    // =====================================================
    @PreAuthorize("hasAuthority('science.edit')")
    @PostMapping
    @Operation(summary = "Yangi nashr muallifi yaratish",
               description = "Yangi nashr muallifi meta ma'lumotini yaratish")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> create(
            @RequestBody Object rawBody,
            @RequestParam(required = false) Boolean returnNulls) {

        // PHP sends array: [{...}], unwrap to single object
        Map<String, Object> body;
        boolean isArrayInput = false;
        if (rawBody instanceof List) {
            List<?> list = (List<?>) rawBody;
            if (list.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            body = (Map<String, Object>) list.get(0);
            isArrayInput = true;
        } else {
            body = (Map<String, Object>) rawBody;
        }

        log.debug("POST create/upsert PublicationAuthorMeta: {}", body);

        // CUBA UPSERT: if body contains 'id' and entity exists, update instead of create
        if (body.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(body.get("id").toString());
                Optional<PublicationAuthorMeta> existingOpt = scienceService.findPublicationAuthorMetaById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    PublicationAuthorMeta existing = existingOpt.get();
                    scienceService.updatePublicationAuthorMetaFromMap(existing, body);
                    existing.setUpdateTs(LocalDateTime.now());
                    PublicationAuthorMeta saved = scienceService.savePublicationAuthorMeta(existing);
                    Map<String, Object> result = scienceService.toPublicationAuthorMetaMinimalMap(saved);
                    return ResponseEntity.ok(isArrayInput ? List.of(result) : result);
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        PublicationAuthorMeta entity = new PublicationAuthorMeta();
        scienceService.updatePublicationAuthorMetaFromMap(entity, body);

        PublicationAuthorMeta saved = scienceService.savePublicationAuthorMeta(entity);

        // OLD-HEMIS POST da faqat minimal response qaytaradi
        Map<String, Object> result = scienceService.toPublicationAuthorMetaMinimalMap(saved);
        return ResponseEntity.ok(isArrayInput ? List.of(result) : result);
    }

    // =====================================================
    // PUT - Update existing
    // =====================================================
    @PreAuthorize("hasAuthority('science.edit')")
    @PutMapping("/{entityId}")
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

        Optional<PublicationAuthorMeta> existingOpt = scienceService.findPublicationAuthorMetaById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationAuthorMeta entity = existingOpt.get();
        scienceService.updatePublicationAuthorMetaFromMap(entity, body);

        PublicationAuthorMeta saved = scienceService.savePublicationAuthorMeta(entity);
        return ResponseEntity.ok(scienceService.toPublicationAuthorMetaMap(saved, returnNulls));
    }

    // =====================================================
    // DELETE - Soft delete
    // =====================================================
    @PreAuthorize("hasAuthority('science.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "Nashr muallifini o'chirish",
               description = "Nashr muallifi meta ma'lumotini soft delete qilish (delete_ts o'rnatiladi)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@Parameter(description = "Entity UUID") @PathVariable UUID entityId) {
        log.debug("DELETE PublicationAuthorMeta id: {}", entityId);

        Optional<PublicationAuthorMeta> entityOpt = scienceService.findPublicationAuthorMetaById(entityId);
        if (entityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        scienceService.softDeletePublicationAuthorMeta(entityOpt.get());

        return ResponseEntity.ok().build();
    }
}
