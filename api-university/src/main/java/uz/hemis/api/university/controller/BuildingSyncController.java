package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.building.BuildingSyncDto;
import uz.hemis.common.dto.building.BuildingSyncResult;
import uz.hemis.service.infrastructure.UniversityBuildingSyncService;

import java.util.List;

/**
 * Univer (per-OTM) → markaz {@code university_building} bulk sync.
 *
 * <p>Idempotent: {@code (universityCode, sourceUid)} juftligi bo'yicha upsert.
 * Har item SHA-256 content hash'i orqali change detection — o'zgarmagan yozuvlar skip qilinadi.</p>
 *
 * <p>University identification:
 * <ol>
 *   <li><b>Production:</b> OAuth2 client_credentials — universityCode JWT {@code university_code} claim'dan</li>
 *   <li><b>Dev/test:</b> HTTP header {@code X-University-Code} (localhost only, security filter permitAll)</li>
 * </ol>
 * URL'da universityCode YO'Q — caller spoofing'dan himoyalanish (ministry convention,
 * EmployeeSyncController bilan bir xil pattern).
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

    @PostMapping("/buildings/sync")
    @Operation(summary = "Binolarni sync qilish (bulk upsert)",
            description = "OTM'ning binolar ro'yxatini markazga yuboradi. Idempotent — o'zgarmagan yozuvlar (content_hash) skip qilinadi. " +
                    "universityCode JWT claim'idan yoki dev'da `X-University-Code` header'idan olinadi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync natijasi (muvaffaqiyatli/xatolar soni)"),
            @ApiResponse(responseCode = "400", description = "Validatsiya xatosi"),
            @ApiResponse(responseCode = "401", description = "universityCode aniqlanmadi")
    })
    public ResponseEntity<ResponseWrapper<BuildingSyncResult>> sync(
            @Parameter(hidden = true) HttpServletRequest request,
            @Valid @RequestBody @NotEmpty List<BuildingSyncDto> items) {
        String universityCode = resolveUniversityCode(request);
        log.info("POST /university/buildings/sync universityCode={} itemCount={}", universityCode, items.size());
        BuildingSyncResult result = syncService.syncFromUniver(universityCode, items);
        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    /**
     * universityCode aniqlash:
     *   1. JWT claim {@code university_code} (OAuth2 client_credentials).
     *   2. Dev fallback: HTTP header {@code X-University-Code} (faqat dev profile, security permitAll).
     */
    private String resolveUniversityCode(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String fromToken = jwt.getClaimAsString("university_code");
            if (fromToken != null && !fromToken.isBlank()) {
                return fromToken;
            }
        }
        String fromHeader = request.getHeader("X-University-Code");
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "universityCode aniqlanmadi — JWT 'university_code' claim yoki 'X-University-Code' header kerak");
    }
}
