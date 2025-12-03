package uz.hemis.api.legacy.controller;

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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.adapter.LegacyEntityAdapter;
import uz.hemis.common.dto.StudentMetaDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.service.StudentMetaService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * StudentMeta Entity Controller (CUBA Pattern) - Talaba meta ma'lumotlari
 * Tag 04: Talaba
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_EStudentMeta</p>
 *
 * <p><strong>CLEAN ARCHITECTURE IMPLEMENTATION:</strong></p>
 * <ul>
 *   <li>Uses Service layer for all business logic</li>
 *   <li>Uses LegacyEntityAdapter for CUBA compatibility</li>
 *   <li>Enforces soft-delete only (no physical DELETE)</li>
 *   <li>Validates all operations</li>
 *   <li>Manages cache automatically</li>
 *   <li>Logs all operations for audit</li>
 * </ul>
 *
 * <p><strong>100% BACKWARD COMPATIBLE:</strong></p>
 * <ul>
 *   <li>Preserves exact CUBA entity API pattern</li>
 *   <li>URL: /app/rest/v2/entities/hemishe_EStudentMeta</li>
 *   <li>Response format: CUBA Map structure with _entityName, _instanceName</li>
 *   <li>Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)</li>
 *   <li>Same HTTP status codes</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET    /app/rest/v2/entities/hemishe_EStudentMeta/{id}      - Get by ID</li>
 *   <li>PUT    /app/rest/v2/entities/hemishe_EStudentMeta/{id}      - Update</li>
 *   <li>DELETE /app/rest/v2/entities/hemishe_EStudentMeta/{id}      - Soft delete</li>
 *   <li>GET    /app/rest/v2/entities/hemishe_EStudentMeta           - List all with pagination</li>
 *   <li>POST   /app/rest/v2/entities/hemishe_EStudentMeta           - Create new</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "04.Talaba", description = "Talaba meta entity ma'lumotlari API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentMeta")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class StudentMetaEntityController {

    private final StudentMetaService studentMetaService;
    private final LegacyEntityAdapter adapter;

    private static final String ENTITY_NAME = "hemishe_EStudentMeta";

    /**
     * Build _instanceName for CUBA compatibility
     * Format: "university:uId"
     */
    private String buildInstanceName(StudentMetaDto dto) {
        if (dto.getUniversity() != null && dto.getUId() != null) {
            return dto.getUniversity() + ":" + dto.getUId();
        }
        return dto.getId() != null ? dto.getId().toString() : "new";
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    /**
     * Bitta talaba meta ma'lumotlarini olish
     */
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta talaba meta ma'lumotlarini olish",
        description = """
            ID bo'yicha talaba meta ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EStudentMeta/{entityId}
            **Auth:** Bearer token (required)

            **Parametrlar:**
            - dynamicAttributes: Dinamik atributlarni qaytarish (boolean)
            - returnNulls: Null qiymatlarni qaytarish (boolean)
            - view: CUBA view nomi (string)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Talaba meta ma'lumotlari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi - Token noto'g'ri yoki muddati o'tgan"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - Foydalanuvchida entity o'qish huquqi yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan talaba meta topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Talaba meta UUID identifikatori", example = "00000000-0000-0000-0000-000000000000")
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET student meta by id: {} (via service layer)", entityId);

        try {
            StudentMetaDto dto = studentMetaService.findById(entityId);
            Map<String, Object> cubaMap = adapter.toMap(dto, ENTITY_NAME, returnNulls);
            return ResponseEntity.ok(cubaMap);

        } catch (ResourceNotFoundException e) {
            log.debug("Student meta not found: {}", entityId);
            return ResponseEntity.notFound().build();
        }
    }

    // =====================================================
    // GET ALL (LIST)
    // =====================================================

    /**
     * Barcha talaba meta ma'lumotlarini olish (pagination bilan)
     */
    @GetMapping
    @Operation(
        summary = "Barcha talaba meta ma'lumotlarini olish",
        description = """
            Barcha talaba meta ma'lumotlarini pagination bilan olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EStudentMeta
            **Auth:** Bearer token (required)

            **Pagination parametrlari:**
            - limit: Sahifadagi elementlar soni (default: 100)
            - offset: Boshlash pozitsiyasi (default: 0)
            - sort: Saralash maydoni va yo'nalishi (masalan: "+uId" yoki "-createTs")
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Talaba meta ro'yxati qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Sahifadagi elementlar soni")
            @RequestParam(defaultValue = "100") int limit,
            @Parameter(description = "Boshlash pozitsiyasi")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Saralash (masalan: +uId, -createTs)")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET all student metas with pagination: limit={}, offset={}", limit, offset);

        // Build pageable
        int page = offset / Math.max(limit, 1);
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "createTs");

        if (sort != null && !sort.isEmpty()) {
            String field = sort.startsWith("+") || sort.startsWith("-") ? sort.substring(1) : sort;
            Sort.Direction direction = sort.startsWith("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
            sortOrder = Sort.by(direction, field);
        }

        Pageable pageable = PageRequest.of(page, limit, sortOrder);

        Page<StudentMetaDto> dtoPage = studentMetaService.findAll(pageable);

        List<Map<String, Object>> result = dtoPage.getContent().stream()
                .map(dto -> adapter.toMap(dto, ENTITY_NAME, returnNulls))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // CREATE (POST)
    // =====================================================

    /**
     * Yangi talaba meta yaratish
     */
    @PostMapping
    @Operation(
        summary = "Yangi talaba meta yaratish",
        description = """
            Yangi talaba meta ma'lumotlarini yaratish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EStudentMeta
            **Auth:** Bearer token (required)

            **Request body:** StudentMeta JSON object
            **Response:** Yaratilgan entity (CUBA Map format)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov - Validatsiya xatosi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
        @ApiResponse(responseCode = "409", description = "Konflikt - (uId, university) kombinatsiyasi mavjud")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody StudentMetaDto dto,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create student meta: university={}", dto.getUniversity());

        StudentMetaDto created = studentMetaService.create(dto);
        Map<String, Object> cubaMap = adapter.toMap(created, ENTITY_NAME, returnNulls);

        return ResponseEntity.status(201).body(cubaMap);
    }

    // =====================================================
    // UPDATE (PUT)
    // =====================================================

    /**
     * Talaba meta ma'lumotlarini yangilash
     */
    @PutMapping("/{entityId}")
    @Operation(
        summary = "Talaba meta ma'lumotlarini yangilash",
        description = """
            ID bo'yicha talaba meta ma'lumotlarini yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_EStudentMeta/{entityId}
            **Auth:** Bearer token (required)

            **Request body:** Yangilanadigan maydonlar (partial update)
            **Response:** Yangilangan entity (CUBA Map format)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov - Validatsiya xatosi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi"),
        @ApiResponse(responseCode = "409", description = "Konflikt - (uId, university) kombinatsiyasi mavjud")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Talaba meta UUID identifikatori")
            @PathVariable UUID entityId,
            @RequestBody StudentMetaDto dto,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT update student meta: {}", entityId);

        try {
            StudentMetaDto updated = studentMetaService.partialUpdate(entityId, dto);
            Map<String, Object> cubaMap = adapter.toMap(updated, ENTITY_NAME, returnNulls);
            return ResponseEntity.ok(cubaMap);

        } catch (ResourceNotFoundException e) {
            log.debug("Student meta not found: {}", entityId);
            return ResponseEntity.notFound().build();
        }
    }

    // =====================================================
    // DELETE (SOFT DELETE)
    // =====================================================

    /**
     * Talaba meta ma'lumotlarini o'chirish (soft delete)
     */
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Talaba meta ma'lumotlarini o'chirish (soft delete)",
        description = """
            ID bo'yicha talaba meta ma'lumotlarini o'chirish.

            **CRITICAL:** Faqat soft delete! Fizik o'chirish amalga oshirilmaydi.
            deleteTs maydoni joriy vaqtga o'rnatiladi.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** DELETE /app/rest/v2/entities/hemishe_EStudentMeta/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Muvaffaqiyatli o'chirildi (soft delete)"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Talaba meta UUID identifikatori")
            @PathVariable UUID entityId) {

        log.debug("DELETE (soft) student meta: {}", entityId);

        try {
            studentMetaService.softDelete(entityId);
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) {
            log.debug("Student meta not found: {}", entityId);
            return ResponseEntity.notFound().build();
        }
    }
}
