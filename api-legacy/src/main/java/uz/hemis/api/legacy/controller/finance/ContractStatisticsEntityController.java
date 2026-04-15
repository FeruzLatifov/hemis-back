package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.domain.entity.ContractStatistics;
import uz.hemis.service.legacy.finance.FinanceEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Contract Statistics Entity Controller (CUBA Pattern)
 * Tag 36: Shartnoma statistikasi (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RContractStatistics
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RContractStatistics           - List all with pagination
 * - GET    /app/rest/v2/entities/hemishe_RContractStatistics/{id}      - Get by ID
 * - GET    /app/rest/v2/entities/hemishe_RContractStatistics/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RContractStatistics/search    - Search (JSON filter)
 * - POST   /app/rest/v2/entities/hemishe_RContractStatistics           - Create new
 * - PUT    /app/rest/v2/entities/hemishe_RContractStatistics/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RContractStatistics/{id}      - Soft delete
 *
 * @since 1.0.0
 */
@Tag(name = "36.Shartnoma statistikasi", description = "Shartnoma statistikasi entity ma'lumotlari API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RContractStatistics")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ContractStatisticsEntityController {

    private final FinanceEntityLegacyService financeService;
    private final CubaFilterHelper filterHelper;

    private static final String ENTITY_NAME = "hemishe_RContractStatistics";

    // =====================================================
    // GET - List all (paginated)
    // =====================================================

    @PreAuthorize("hasAuthority('reports.view')")
    @GetMapping
    @Operation(
        summary = "Shartnoma statistikasi ro'yxati",
        description = """
            Sahifalangan shartnoma statistikasi ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_RContractStatistics
            **Auth:** Bearer token (required)

            **Pagination:**
            - offset: Boshlang'ich pozitsiya (default: 0)
            - limit: Sahifadagi yozuvlar soni (default: 50)
            - returnCount: X-Total-Count headerini qaytarish
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Statistika ro'yxati qaytarildi",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Map.class)))),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi - Token noto'g'ri yoki muddati o'tgan"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - Foydalanuvchida o'qish huquqi yo'q")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish (X-Total-Count header)")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni")
            @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Tartiblash (masalan: date-desc)")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi (masalan: rContractStatistics-view)")
            @RequestParam(required = false) String view) {

        log.debug("GET all contract statistics - offset: {}, limit: {}, view: {}", offset, limit, view);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, parts[0]);
        }

        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<ContractStatistics> entityPage = financeService.findAllContractStatistics(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
                .map(e -> financeService.toContractStatisticsMap(e, returnNulls))
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                    .body(result);
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // GET - By ID
    // =====================================================

    @PreAuthorize("hasAuthority('reports.view')")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta shartnoma statistikasini olish",
        description = """
            ID bo'yicha shartnoma statistikasi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_RContractStatistics/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Statistika ma'lumotlari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan statistika topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Statistika UUID identifikatori", required = true)
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET contract statistics by id: {}", entityId);

        Optional<ContractStatistics> entity = financeService.findContractStatisticsById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(financeService.toContractStatisticsMap(entity.get(), returnNulls));
    }

    // =====================================================
    // GET - Search
    // =====================================================

    @PreAuthorize("hasAuthority('reports.view')")
    @GetMapping("/search")
    @Operation(
        summary = "Shartnoma statistikasini qidirish (GET)",
        description = """
            URL parametrlari orqali shartnoma statistikasini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_RContractStatistics/search
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Qidiruv natijalari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @Parameter(description = "CUBA filter expression")
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<ContractStatistics> allEntities = financeService.findAllContractStatistics();
        List<ContractStatistics> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
                .map(e -> financeService.toContractStatisticsMap(e, returnNulls))
                .collect(Collectors.toList()));
    }

    // =====================================================
    // POST - Search
    // =====================================================

    @PreAuthorize("hasAuthority('reports.view')")
    @PostMapping("/search")
    @Operation(
        summary = "Shartnoma statistikasini qidirish (POST)",
        description = """
            JSON filter orqali shartnoma statistikasini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_RContractStatistics/search
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Qidiruv natijalari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Offset") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Limit") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        int effectiveOffset = filterHelper.extractInt(body, "offset", offset, 0);
        int effectiveLimit = filterHelper.extractInt(body, "limit", limit, 50);
        String filterJson = filterHelper.extractFilterFromBody(body);

        log.debug("POST search - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<ContractStatistics> allEntities = financeService.findAllContractStatistics();
        List<ContractStatistics> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
                .map(e -> financeService.toContractStatisticsMap(e, returnNulls))
                .collect(Collectors.toList()));
    }

    // =====================================================
    // POST - Create
    // =====================================================

    @PreAuthorize("hasAuthority('reports.edit')")
    @PostMapping
    @Operation(
        summary = "Yangi shartnoma statistikasi yaratish",
        description = """
            Yangi shartnoma statistikasi yozuvini yaratish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_RContractStatistics
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Statistika yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov - Validatsiya xatosi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create contract statistics: {}", body);

        ContractStatistics entity = new ContractStatistics();
        financeService.updateContractStatisticsFromMap(entity, body);
        ContractStatistics saved = financeService.saveContractStatistics(entity);

        return ResponseEntity.ok(financeService.toContractStatisticsMap(saved, returnNulls));
    }

    // =====================================================
    // PUT - Update
    // =====================================================

    @PreAuthorize("hasAuthority('reports.edit')")
    @PutMapping("/{entityId}")
    @Operation(
        summary = "Shartnoma statistikasini yangilash",
        description = """
            Mavjud shartnoma statistikasini yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_RContractStatistics/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Statistika yangilandi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Statistika UUID identifikatori", required = true)
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT update contract statistics id: {}", entityId);

        Optional<ContractStatistics> existingOpt = financeService.findContractStatisticsById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ContractStatistics entity = existingOpt.get();
        financeService.updateContractStatisticsFromMap(entity, body);
        ContractStatistics saved = financeService.saveContractStatistics(entity);

        return ResponseEntity.ok(financeService.toContractStatisticsMap(saved, returnNulls));
    }

    // =====================================================
    // DELETE - Soft delete
    // =====================================================

    @PreAuthorize("hasAuthority('reports.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Shartnoma statistikasini o'chirish",
        description = """
            Shartnoma statistikasini soft delete qilish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** DELETE /app/rest/v2/entities/hemishe_RContractStatistics/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Muvaffaqiyatli - Statistika o'chirildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Statistika UUID identifikatori", required = true)
            @PathVariable UUID entityId) {

        log.info("DELETE contract statistics id: {}", entityId);

        Optional<ContractStatistics> entity = financeService.findContractStatisticsById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        financeService.deleteContractStatistics(entity.get());
        return ResponseEntity.noContent().build();
    }
}
