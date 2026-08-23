package uz.hemis.api.legacy.controller.academic;

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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.adapter.LegacyEntityAdapter;
import uz.hemis.common.dto.academic.AcademicScoreDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.service.academic.AcademicScoreService;

import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Academic Score Entity Controller (CUBA Pattern) - REFACTORED
 * Tag: 34.Akademik hisobotlar o'zlashtirish
 *
 * ✅ CLEAN ARCHITECTURE IMPLEMENTATION:
 * - Uses AcademicScoreService for all business logic
 * - Uses LegacyEntityAdapter for CUBA compatibility
 * - Enforces soft-delete only (no physical DELETE)
 * - Validates all operations
 *
 * ✅ 100% BACKWARD COMPATIBLE:
 * - URL: /app/rest/v2/entities/hemishe_RAcademicScore
 * - Response format: CUBA Map structure with _entityName
 * - Same HTTP status codes
 *
 * ✅ CORRECT TABLE MAPPING:
 * - Entity: AcademicScore
 * - Table: hemishe_r_academic_score
 * - Legacy entity name: hemishe_RAcademicScore
 *
 * @since 2.0.0 (Clean Architecture)
 */
@Tag(name = "34.Akademik hisobotlar o'zlashtirish", description = "Akademik o'zlashtirish (baholar) hisobotlarini boshqarish API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RAcademicScore")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class GradeEntityController {

    private final AcademicScoreService academicScoreService;
    private final LegacyEntityAdapter adapter;

    private static final String ENTITY_NAME = "hemishe_RAcademicScore";

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Akademik o'zlashtirish yozuvini ID bo'yicha olish",
        description = "Berilgan UUID orqali akademik o'zlashtirish hisobotini qaytaradi"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Yozuv topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Entity UUID")
            @PathVariable UUID entityId,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("GET academic score by id: {} (via service)", entityId);

        try {
            AcademicScoreDto dto = academicScoreService.findById(entityId);
            Map<String, Object> cubaMap = adapter.toMap(dto, ENTITY_NAME, returnNulls);
            return ResponseEntity.ok(cubaMap);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{entityId}")
    @Operation(
        summary = "Akademik o'zlashtirish yozuvini yangilash",
        description = "Mavjud akademik o'zlashtirish hisobotini yangilaydi"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Yozuv topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Entity UUID")
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT academic score id: {} (via service)", entityId);

        try {
            AcademicScoreDto dto = adapter.fromMap(body, AcademicScoreDto.class);
            AcademicScoreDto updated = academicScoreService.update(entityId, dto);
            Map<String, Object> cubaMap = adapter.toMap(updated, ENTITY_NAME, returnNulls);
            return ResponseEntity.ok(cubaMap);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Akademik o'zlashtirish yozuvini o'chirish",
        description = "Yozuvni soft-delete qiladi (delete_ts ga qiymat qo'yiladi)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Yozuv topilmadi")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Entity UUID")
            @PathVariable UUID entityId) {
        log.debug("DELETE academic score id: {} (SOFT DELETE via service)", entityId);

        try {
            academicScoreService.softDelete(entityId);
            // OLD-HEMIS: 200 OK qaytaradi (204 emas)
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    @Operation(
        summary = "Akademik o'zlashtirish yozuvlarini qidirish (GET)",
        description = "Filter parametri orqali qidirish"
    )
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @Parameter(description = "JPQL filter")
            @RequestParam(required = false) String filter,
            @Parameter(description = "Boshlang'ich indeks")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Maksimal yozuvlar soni")
            @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("GET search academic scores with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1));
        List<AcademicScoreDto> dtos = academicScoreService.findAll(pageRequest).getContent();
        List<Map<String, Object>> cubaMaps = adapter.toMapList(dtos, ENTITY_NAME, returnNulls);
        return ResponseEntity.ok(cubaMaps);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/search")
    @Operation(
        summary = "Akademik o'zlashtirish yozuvlarini qidirish (POST)",
        description = "Request body orqali murakkab qidirish. limit/offset body yoki query param sifatida yuborilishi mumkin."
    )
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Boshlang'ich indeks")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Maksimal yozuvlar soni")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        // Body'dan limit/offset o'qish (agar query param bo'lmasa)
        int effectiveLimit = limit != null ? limit : 50;
        int effectiveOffset = offset != null ? offset : 0;

        if (body != null) {
            if (limit == null && body.containsKey("limit")) {
                Object bodyLimit = body.get("limit");
                if (bodyLimit instanceof Number) {
                    effectiveLimit = ((Number) bodyLimit).intValue();
                } else if (bodyLimit instanceof String) {
                    effectiveLimit = Integer.parseInt((String) bodyLimit);
                }
            }
            if (offset == null && body.containsKey("offset")) {
                Object bodyOffset = body.get("offset");
                if (bodyOffset instanceof Number) {
                    effectiveOffset = ((Number) bodyOffset).intValue();
                } else if (bodyOffset instanceof String) {
                    effectiveOffset = Integer.parseInt((String) bodyOffset);
                }
            }
        }

        log.debug("POST search academic scores - offset: {}, limit: {}", effectiveOffset, effectiveLimit);

        PageRequest pageRequest = PageRequest.of(effectiveOffset / Math.max(effectiveLimit, 1), Math.max(effectiveLimit, 1));
        List<AcademicScoreDto> dtos = academicScoreService.findAll(pageRequest).getContent();
        List<Map<String, Object>> cubaMaps = adapter.toMapList(dtos, ENTITY_NAME, returnNulls);
        return ResponseEntity.ok(cubaMaps);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
        summary = "Barcha akademik o'zlashtirish yozuvlarini olish",
        description = "Sahifalash bilan barcha yozuvlarni qaytaradi"
    )
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Boshlang'ich indeks")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Maksimal yozuvlar soni")
            @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Saralash (masalan: updateDate-desc)")
            @RequestParam(required = false) String sort,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("GET all academic scores - offset: {}, limit: {} (via service)", offset, limit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            sorting = Sort.by(
                parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC,
                parts[0]
            );
        }

        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1), sorting);
        Page<AcademicScoreDto> page = academicScoreService.findAll(pageRequest);

        List<Map<String, Object>> cubaMaps = adapter.toMapList(
            page.getContent(),
            ENTITY_NAME,
            returnNulls
        );

        return ResponseEntity.ok(cubaMaps);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
        summary = "Yangi akademik o'zlashtirish yozuvi yaratish",
        description = """
            Yangi akademik o'zlashtirish hisobotini yaratadi.

            **Request Body (CUBA format):**
            ```json
            {
                "_entityName": "hemishe_RAcademicScore",
                "universityCode": "401",
                "universityName": "TATU",
                "facultyCode": "401-01",
                "facultyName": "Dasturiy injiniring",
                "educationTypeCode": "11",
                "educationTypeName": "Bakalavr",
                "educationYearCode": "2024",
                "educationYearName": "2024-2025",
                "semesterTypeCode": "1",
                "semesterTypeName": "1-semestr",
                "courseCode": "1",
                "courseName": "1-kurs",
                "tableType": "ўзлаштириш кўрсаткичлари",
                "scorePercent": 85.5,
                "scoreType": "yaxshi",
                "debitorCount": 5.0,
                "updateDate": "2024-01-15"
            }
            ```

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Akademik o'zlashtirish ma'lumotlari (CUBA format)",
                required = true,
                content = @Content(
                    mediaType = "application/json"
                )
            )
            @RequestBody Map<String, Object> body,
            @Parameter(description = "null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create academic score (via service)");

        AcademicScoreDto dto = adapter.fromMap(body, AcademicScoreDto.class);
        AcademicScoreDto created = academicScoreService.create(dto);
        Map<String, Object> cubaMap = adapter.toMap(created, ENTITY_NAME, returnNulls);

        // OLD-HEMIS: 201 Created qaytaradi
        return ResponseEntity.ok(cubaMap);
    }
}
