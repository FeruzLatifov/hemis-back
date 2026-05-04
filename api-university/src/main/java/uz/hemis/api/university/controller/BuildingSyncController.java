package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.building.BuildingSyncDto;
import uz.hemis.common.dto.building.BuildingSyncResult;
import uz.hemis.service.infrastructure.UniversityBuildingSyncService;

import java.util.List;

/**
 * Univer (OTM) → hemis-back bulk building sync endpoint.
 *
 * <p>Idempotent: {@code (universityCode, sourceUid)} juftligi bo'yicha upsert.
 * Har item SHA-256 content hash'i orqali change detection — o'zgarmagan yozuvlar skip qilinadi.</p>
 */
@RestController
@RequestMapping("/api/v1/university")
@Tag(name = "University Building Sync", description = "OTM tomondan binolarni markazga yuborish (sync)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BuildingSyncController {

    private final UniversityBuildingSyncService syncService;

    @PostMapping("/{universityCode}/buildings/sync")
    @PreAuthorize("hasAuthority('buildings.sync') and @subject.ownsUniversity(#universityCode)")
    @Operation(summary = "Binolarni sync qilish (bulk upsert)",
            description = "OTM'ning binolar ro'yxatini markazga yuboradi. Idempotent — o'zgarmagan yozuvlar skip qilinadi. " +
                    "Caller (UNIVERSITY_BACKEND OAuth client) faqat o'z universityCode'ini sync qila oladi — boshqasi 403.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync natijasi (muvaffaqiyatli/xatolar soni)"),
            @ApiResponse(responseCode = "400", description = "Validatsiya xatosi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<ResponseWrapper<BuildingSyncResult>> sync(
            @Parameter(description = "OTM kodi", example = "401")
            @PathVariable String universityCode,
            @Valid @RequestBody @NotEmpty List<BuildingSyncDto> items) {
        log.info("POST /university/{}/buildings/sync itemCount={}", universityCode, items.size());
        BuildingSyncResult result = syncService.syncFromUniver(universityCode, items);
        return ResponseEntity.ok(ResponseWrapper.success(result));
    }
}
