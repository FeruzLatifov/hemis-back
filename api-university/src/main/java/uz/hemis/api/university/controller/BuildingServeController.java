package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.building.BuildingDto;
import uz.hemis.common.dto.building.CadastreDto;
import uz.hemis.service.infrastructure.CadastreIngestService;
import uz.hemis.service.infrastructure.UniversityBuildingService;
import uz.hemis.service.university.UniversityExternalDataService;

import java.util.List;

/**
 * OTM serve — markaz Univer'ga binolar + kadastr beradi (aggregation'ning READ juftligi).
 *
 * <p>universityCode har doim JWT {@code university_code} claim'dan (URL'da EMAS — spoofing himoyasi,
 * {@code BuildingSyncController} bilan bir xil).</p>
 */
@RestController
@RequestMapping("/api/v1/university")
@Tag(name = "Binolar va kadastr", description = "OTM binolari va kadastr obyektlari — markazga yuborish (sync) va markazdan olish (serve)")
@SecurityRequirement(name = "oauth2")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BuildingServeController {

    private final UniversityBuildingService buildingService;
    private final CadastreIngestService cadastreIngestService;
    private final UniversityExternalDataService externalDataService;
    private final Environment environment;

    @GetMapping("/buildings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "OTM o'z binolari", description = "Markazdagi o'z binolarini qaytaradi (token-scoped).")
    public ResponseEntity<ResponseWrapper<List<BuildingDto>>> myBuildings(
            @Parameter(hidden = true) HttpServletRequest request) {
        String code = resolveUniversityCode(request);
        log.info("GET /university/buildings universityCode={}", code);
        return ResponseEntity.ok(ResponseWrapper.success(buildingService.findAllByUniversity(code)));
    }

    @GetMapping("/cadastre/by-cadnum")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Kadastr obyektini olish — bazada mavjud bo'lsa qaytaradi, bo'lmasa kadastrdan olib keladi",
            description = "Kadastr raqami bo'yicha kadastr obyektini qaytaradi. Agar obyekt markaz bazasida (university_cadastre) "
                    + "mavjud bo'lsa — o'sha saqlangan ma'lumot qaytariladi; mavjud bo'lmasa — kadastr tizimidan (api-mspd) olib kelinadi, "
                    + "markazda saqlanadi va qaytariladi. Kadastr raqami noto'g'ri yoki topilmasa → 422 CADASTRE_NOT_FOUND.")
    public ResponseEntity<ResponseWrapper<CadastreDto>> cadastreByCadNum(
            @Parameter(description = "Kadastr raqami. Format: hudud:tuman:zona:kvartal:uchastka:bino "
                    + "— 6 ta son guruhi ':' bilan (NN:NN:NN:NN:NN:NNNN). Pastdagi qiymat namuna (haqiqiy raqam emas).",
                    example = "00:00:00:00:00:0000")
            @RequestParam @NotBlank String cadNumber) {
        log.info("GET /university/cadastre/by-cadnum cadNumber={}", cadNumber);
        return ResponseEntity.ok(ResponseWrapper.success(cadastreIngestService.getByCadNumberOrFetch(cadNumber)));
    }

    @PostMapping("/cadastre/sync")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "OTM kadastrini sync qilish (INN bo'yicha)",
            description = "OTM'ning INN'i bo'yicha barcha kadastr raqamlarini (cadastr_list) oladi va har biriga to'liq "
                    + "detalni kadastr tizimidan (api-mspd) olib markazda saqlaydi. INN university_code'dan avtomatik (URL'да emas).\n\n"
                    + "**force=false** (default): bazada allaqachon COMPLETE bo'lgan raqamlar qayta OLINMAYDI — faqat yangi/PENDING "
                    + "raqamlar olinadi (tez, takroriy sync uchun). **force=true**: barcha raqamlar kadastrdan qaytadan olinadi "
                    + "(to'liq yangilash, sekin — har raqamga tashqi API chaqiruvi bo'ladi).")
    public ResponseEntity<ResponseWrapper<CadastreIngestService.CadastreIngestResult>> syncMyCadastre(
            @Parameter(description = "true → barcha raqamni kadastrdan qayta olish (to'liq yangilash). "
                    + "false → faqat bazada yo'q yoki PENDING bo'lganlarni olish (tez).")
            @RequestParam(defaultValue = "false") boolean force,
            @Parameter(hidden = true) HttpServletRequest request) {
        String code = resolveUniversityCode(request);
        String tin = externalDataService.resolveTin(code);
        log.info("POST /university/cadastre/sync universityCode={}, force={}", code, force);
        return ResponseEntity.ok(ResponseWrapper.success(cadastreIngestService.ingestByInn(tin, force)));
    }

    private String resolveUniversityCode(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String fromToken = jwt.getClaimAsString("university_code");
            if (fromToken != null && !fromToken.isBlank()) {
                return fromToken;
            }
        }
        // X-University-Code header fallback FAQAT dev/test profilда — prod'да spoofing/IDOR himoyasi
        // (claim'siz token boshqa OTM kodini header'да yuborib uning binolarини o'qiy olmasin).
        if (isDevOrTest()) {
            String fromHeader = request.getHeader("X-University-Code");
            if (fromHeader != null && !fromHeader.isBlank()) {
                return fromHeader;
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "universityCode aniqlanmadi — JWT 'university_code' claim majburiy");
    }

    /** X-University-Code header fallback faqat dev/test'да yoqiladi (prod IDOR himoyasi). */
    private boolean isDevOrTest() {
        for (String p : environment.getActiveProfiles()) {
            if ("dev".equals(p) || "test".equals(p)) {
                return true;
            }
        }
        return false;
    }
}
