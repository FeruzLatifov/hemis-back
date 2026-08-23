package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.classifier.SpecialityAttachmentService;
import uz.hemis.service.classifier.dto.SpecialityAttachmentSnapshotDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentSnapshotFilter;

import java.util.List;

/**
 * OTM-facing speciality-ATTACHMENT distribution — bootstrap PULL snapshot.
 *
 * <p>The tenant-scoped sibling of {@link SpecialityDistributionController}: where the
 * classifier pull is global (every OTM gets the identical {@code h_speciality} set), an
 * <em>attachment</em> is per-OTM — "which specialities is THIS university allowed to run".
 * A Univer coming online pulls its own full attachment set at once (speciality code+name,
 * education form, status).</p>
 *
 * <p>The OTM is identified <strong>server-side</strong> from its JWT {@code university_code}
 * claim (OAuth2 {@code client_credentials}) — never a URL/param, so one OTM can never pull
 * another's set (anti-spoofing, ministry convention). Served live off the read replica (a
 * per-tenant, index-backed ~150-row read) — always fresh, deliberately NOT application-cached
 * (see {@code SpecialityAttachmentService#getSnapshot}); 224 OTMs polling this indexed lookup is
 * trivial replica load.</p>
 *
 * <p>Audience: 224 Univer Yii2 backends over OAuth {@code client_credentials};
 * secured by {@code isAuthenticated()}.</p>
 *
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/v1/university/speciality-attachments")
@Tag(name = "Mutaxassisliklar")
@SecurityRequirement(name = "oauth2")
@RequiredArgsConstructor
@Slf4j
public class SpecialityAttachmentDistributionController {

    private static final Profiles NON_PROD = Profiles.of("dev", "local", "test");

    private final SpecialityAttachmentService attachmentService;
    private final ScopeResolver scopeResolver;
    private final Environment environment;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "OTMga biriktirilgan mutaxassisliklar ro'yxatini olish",
            description = """
                    ## Shu OTM biriktirilgan mutaxassisliklar

                    Chaqirayotgan OTM qaysi mutaxassisliklarni, qaysi ta'lim shaklida olib borishga ruxsat
                    etilgani. **`data` — TEKIS massiv**; har element = bitta (mutaxassislik × ta'lim shakli).
                    Bitta mutaxassislik bir nechta shaklda bo'lsa — bir nechta alohida qator.

                    **OTM aniqlanishi:** JWT `university_code` claim'idan — so'rovda parametr YO'Q. Har OTM
                    faqat o'zinikini oladi; boshqa OTM to'plamini so'rab bo'lmaydi (fail-closed, IDOR himoya).

                    **Join kaliti:** `specialityCode` — klassifikator (`/classifiers/speciality`) `code`'si
                    bilan AYNI qiymat; shu orqali biriktirishni umumiy klassifikator daraxtiga bog'laysiz.

                    **Ta'lim turi (`educationType`):** 11=Bakalavr · 12=Magistr.
                    **Ta'lim shakli (`educationForm`):** 11=Kunduzgi · 12=Kechki · 16=Masofaviy.
                    **O'quv yili (`eduYear`):** 2026 = 2026-2027.

                    **⚠️ Faqat FAOL biriktirishlar:** ro'yxat faqat `status = ACTIVE` (faol)
                    biriktirishlarni qaytaradi. Vaqtincha to'xtatilgan (SUSPENDED) yoki bekor
                    qilingan (REVOKED) biriktirishlar OTM uchun ruxsat bekor hisoblanadi va bu
                    yerda **KO'RINMAYDI** — OTM faqat o'zining jonli, ruxsat etilgan to'plamini oladi.
                    (Har bir qatordagi `status` maydoni shu sababdan doim `ACTIVE`.)

                    **Qamrov:** replikadan jonli o'qiladi (har doim yangi, app-cache YO'Q).

                    ## Filtr parametrlari (ixtiyoriy, query-string)

                    Barchasi **ixtiyoriy** — bittasi ham berilmasa butun to'plam qaytadi (eski xulq
                    o'zgarmaydi). Bir nechtasi birga berilsa **VA (AND)** mantig'i bilan toraytiriladi.
                    OTM identifikatori (`universityCode`) **hech qachon parametr emas** — u JWT'dan
                    olinadi, shuning uchun filtr faqat SHU OTM to'plamini toraytiradi, boshqa OTM
                    ma'lumotini ocholmaydi.

                    | Parametr | Tur | Misol | Izoh |
                    |----------|-----|-------|------|
                    | `eduYear` | integer | `2026` | O'quv yili (2026 = 2026-2027) |
                    | `educationType` | string | `11` | 11=Bakalavr, 12=Magistr |
                    | `educationForm` | string | `11` | 11=Kunduzgi, 12=Kechki, 16=Masofaviy |
                    | `specialityCode` | string | `60710100` | Mutaxassislik kodi (klassifikator `code`) |

                    **Misol:** `?eduYear=2026&educationForm=11` — 2026 o'quv yili, kunduzgi biriktirishlar.
                    `status` parametri YO'Q — ro'yxat doim faqat faol (ACTIVE) biriktirishlarni qaytaradi.
                    """
    )
    public ResponseEntity<ResponseWrapper<List<SpecialityAttachmentSnapshotDto>>> snapshot(
            @Parameter(description = "O'quv yili bo'yicha filtr (2026 = 2026-2027 o'quv yili). Bo'sh — barcha yillar.")
            @RequestParam(required = false) Integer eduYear,
            @Parameter(description = "Ta'lim turi kodi bo'yicha filtr (h_education_type klassifikatoridan: 11=Bakalavr, 12=Magistr, ...). Bo'sh — barcha turlar.")
            @RequestParam(required = false) String educationType,
            @Parameter(description = "Ta'lim shakli kodi bo'yicha filtr (h_education_form klassifikatoridan: 11=Kunduzgi, 12=Kechki, 13=Sirtqi, 16=Masofaviy, ...). Bo'sh — barcha shakllar.")
            @RequestParam(required = false) String educationForm,
            @Parameter(description = "Mutaxassislik kodi bo'yicha filtr (klassifikator code'i). Bo'sh — barcha mutaxassisliklar.")
            @RequestParam(required = false) String specialityCode,
            HttpServletRequest request) {
        String universityCode = resolveUniversityCode(request);
        // Defense-in-depth (consistent with SpecialityAttachmentService list/create/delete): even
        // though universityCode is the caller's own signed claim, require the caller's access scope
        // to allow it — a UNIVERSITY_BACKEND client is scoped to its own OTM, so a mismatched or
        // out-of-scope code fails closed (403) instead of returning another OTM's snapshot.
        if (!scopeResolver.currentScope().allows(universityCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This OTM snapshot is outside your access scope");
        }
        // Filters only NARROW the caller's own set (tenant is always the JWT claim, never a param).
        SpecialityAttachmentSnapshotFilter filter =
                new SpecialityAttachmentSnapshotFilter(eduYear, educationType, educationForm, specialityCode);
        List<SpecialityAttachmentSnapshotDto> items = attachmentService.getSnapshot(universityCode, filter);
        log.info("OTM speciality-attachment pull: universityCode={}, items={}, filtered={}",
                universityCode, items.size(), !filter.isEmpty());
        return ResponseEntity.ok(ResponseWrapper.success(items));
    }

    /**
     * Resolve the calling OTM's {@code universityCode} — tenant identity, so fail-closed.
     *
     * <p>The ONLY production source is the JWT {@code university_code} claim (OAuth2
     * client_credentials). The {@code X-University-Code} header is honored <strong>only</strong>
     * under a non-prod profile (dev/local/test) — it lets a developer hit the endpoint without a
     * full OTM token. In production the header is ignored entirely, so an authenticated caller
     * cannot spoof another OTM's code to read its snapshot (IDOR / OWASP A01 fail-closed).</p>
     */
    private String resolveUniversityCode(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String fromToken = jwt.getClaimAsString("university_code");
            if (fromToken != null && !fromToken.isBlank()) {
                return fromToken;
            }
        }
        // Non-prod only: no real OTM JWT locally — allow a header override for manual testing.
        if (environment.acceptsProfiles(NON_PROD)) {
            String fromHeader = request.getHeader("X-University-Code");
            if (fromHeader != null && !fromHeader.isBlank()) {
                return fromHeader;
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "universityCode aniqlanmadi — JWT 'university_code' claim kerak");
    }
}
