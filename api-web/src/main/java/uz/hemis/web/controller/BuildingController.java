package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.building.BuildingCreateUpdateDto;
import uz.hemis.common.dto.building.BuildingDto;
import uz.hemis.common.dto.building.BuildingLifecycleDto;
import uz.hemis.service.infrastructure.BuildingLifecycleService;
import uz.hemis.service.infrastructure.UniversityBuildingService;

import java.util.List;
import java.util.UUID;

/**
 * Universitet binolari uchun Web API (vazirlik/admin UI).
 * Univer push sync ({@code POST .../sync}) api-university modulida alohida.
 */
@RestController
@RequestMapping("/api/v1/web")
@Tag(name = "Buildings", description = "Universitet binolari boshqaruvi")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BuildingController {

    private final UniversityBuildingService buildingService;
    private final BuildingLifecycleService lifecycleService;

    // =====================================================
    // University-scoped endpoints (OTM ichidagi binolar)
    // =====================================================

    @GetMapping("/universities/{universityCode}/buildings")
    @PreAuthorize("hasAuthority('buildings.view')")
    @Operation(summary = "OTM binolari ro'yxati", description = "Bitta universitetning binolarini pagination bilan qaytaradi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Binolar ro'yxati"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<BuildingDto>>> list(
            @Parameter(description = "OTM kodi")
            @PathVariable String universityCode,
            @Parameter(hidden = true)
            @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        log.info("GET /universities/{}/buildings page={}", universityCode, pageable.getPageNumber());
        Page<BuildingDto> page = buildingService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @PostMapping("/universities/{universityCode}/buildings")
    @PreAuthorize("hasAuthority('buildings.edit')")
    @Operation(summary = "Yangi bino qo'shish", description = "OTM'ga yangi bino yozuvini yaratadi. cad_number berilsa cadastre'dan manzil/maydon auto-fill bo'ladi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bino yaratildi"),
            @ApiResponse(responseCode = "400", description = "Validatsiya xatosi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<ResponseWrapper<BuildingDto>> create(
            @Parameter(description = "OTM kodi")
            @PathVariable String universityCode,
            @Valid @RequestBody BuildingCreateUpdateDto dto) {
        log.info("POST /universities/{}/buildings name={}", universityCode, dto.getName());
        BuildingDto created = buildingService.create(universityCode, dto);
        return ResponseEntity.ok(ResponseWrapper.success(created, "Bino muvaffaqiyatli yaratildi"));
    }

    // =====================================================
    // Building-scoped endpoints (ID bo'yicha)
    // =====================================================

    @GetMapping("/buildings/{id}")
    @PreAuthorize("hasAuthority('buildings.view')")
    @Operation(summary = "Bino tafsilotlari", description = "ID bo'yicha bitta bino")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bino topildi"),
            @ApiResponse(responseCode = "404", description = "Bino topilmadi")
    })
    public ResponseEntity<ResponseWrapper<BuildingDto>> detail(
            @Parameter(description = "Bino UUID")
            @PathVariable UUID id) {
        log.info("GET /buildings/{}", id);
        return ResponseEntity.ok(ResponseWrapper.success(buildingService.findById(id)));
    }

    @PutMapping("/buildings/{id}")
    @PreAuthorize("hasAuthority('buildings.edit')")
    @Operation(summary = "Binoni yangilash",
            description = "Bino ma'lumotlarini yangilaydi. last_renovation_date o'zgarsa avtomatik RENOVATED lifecycle event yoziladi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bino yangilandi"),
            @ApiResponse(responseCode = "400", description = "Validatsiya xatosi"),
            @ApiResponse(responseCode = "404", description = "Bino topilmadi")
    })
    public ResponseEntity<ResponseWrapper<BuildingDto>> update(
            @Parameter(description = "Bino UUID")
            @PathVariable UUID id,
            @Valid @RequestBody BuildingCreateUpdateDto dto) {
        log.info("PUT /buildings/{}", id);
        BuildingDto updated = buildingService.update(id, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated, "Bino yangilandi"));
    }

    @DeleteMapping("/buildings/{id}")
    @PreAuthorize("hasAuthority('buildings.edit')")
    @Operation(summary = "Binoni o'chirish (soft delete)",
            description = "Binoni deleted_at belgisi bilan o'chiradi. Ma'lumot saqlanadi, faqat query'larda ko'rinmaydi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bino o'chirildi"),
            @ApiResponse(responseCode = "404", description = "Bino topilmadi")
    })
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @Parameter(description = "Bino UUID")
            @PathVariable UUID id) {
        log.info("DELETE /buildings/{}", id);
        buildingService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success("Bino muvaffaqiyatli o'chirildi"));
    }

    // =====================================================
    // Lifecycle history (immutable read)
    // =====================================================

    @GetMapping("/buildings/{id}/history")
    @PreAuthorize("hasAuthority('buildings.view')")
    @Operation(summary = "Bino tarixi",
            description = "Binoga tegishli barcha lifecycle voqealar (CONSTRUCTED, RENOVATED, EXPANDED, ...). Event sana bo'yicha DESC.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voqealar ro'yxati")
    })
    public ResponseEntity<ResponseWrapper<List<BuildingLifecycleDto>>> history(
            @Parameter(description = "Bino UUID")
            @PathVariable UUID id) {
        log.info("GET /buildings/{}/history", id);
        List<BuildingLifecycleDto> events = lifecycleService.getHistory(id);
        return ResponseEntity.ok(ResponseWrapper.success(events));
    }
}
