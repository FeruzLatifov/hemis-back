package uz.hemis.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Defense-in-depth tenant ownership guard.
 *
 * <p>Multi-tenant 224 OTM ekosistemida cross-tenant data leakage'ni oldini oladi.
 * Controller darajasidagi {@code @PreAuthorize} bypass qilingan holda ham
 * (test, internal queue, refactor) — service layer'da ikkinchi tekshirish qatlami.</p>
 *
 * <p><strong>Ikki rejim:</strong></p>
 * <ul>
 *   <li>{@link #verifyOwnership(String)} — qat'iy. Faqat o'z OTM resursiga ruxsat.
 *       SUPER_ADMIN ham bloklanadi. {@code UniversityBuildingSyncService} kabi
 *       OTM'ning <em>o'zi</em> chaqiradigan operatsiyalar uchun.</li>
 *   <li>{@link #verifyOwnershipOrAdmin(String)} — bypass bilan. {@code admin.full}
 *       authority egalari (vazirlik admin) cross-tenant operatsiyalarga ruxsat.</li>
 * </ul>
 *
 * <p><strong>Anonymous/non-JWT auth:</strong> har ikki rejimda ham bloklanadi.</p>
 *
 * <p><strong>Misol:</strong></p>
 * <pre>
 * &#064;Transactional
 * public UniversityDto update(String code, UniversityDto dto) {
 *     tenantGuard.verifyOwnershipOrAdmin(code);   // ministry admin OK, OTM admin only own
 *     // ... business logic
 * }
 * </pre>
 *
 * @since 2.5.0
 */
@Component
@Slf4j
public class TenantGuard {

    private static final String CLAIM_UNIVERSITY_CODE = "university_code";
    private static final String CLAIM_AUTHORITIES = "authorities";
    private static final String AUTHORITY_ADMIN_FULL = "admin.full";

    /**
     * Strict tenant check — SUPER_ADMIN ham bloklanadi.
     * <p>Use case: OTM'ning o'zi chaqiradigan operatsiyalar (building sync, etc.).</p>
     *
     * @throws SecurityException agar caller universityCode ga ega bo'lmasa, yoki anonymous bo'lsa
     */
    public void verifyOwnership(String universityCode) {
        String callerCode = extractCallerUniversityCode();
        if (!Objects.equals(callerCode, universityCode)) {
            log.warn("Cross-tenant access blocked (strict): caller={}, requested={}",
                    callerCode, universityCode);
            throw new SecurityException(
                    "Caller does not own university " + universityCode + " (cross-tenant forbidden)");
        }
    }

    /**
     * Tenant check with {@code admin.full} bypass.
     * <p>Use case: vazirlik admin har OTM bilan ishlay olishi kerak (registry,
     * profile management, etc.); OTM admin esa faqat o'z OTM bilan.</p>
     *
     * @throws SecurityException agar caller na admin, na universityCode egasi bo'lsa
     */
    public void verifyOwnershipOrAdmin(String universityCode) {
        Jwt jwt = extractJwt();
        if (hasAdminBypass(jwt)) {
            return;
        }
        String callerCode = jwt.getClaimAsString(CLAIM_UNIVERSITY_CODE);
        if (!Objects.equals(callerCode, universityCode)) {
            log.warn("Cross-tenant access blocked: caller={}, requested={}",
                    callerCode, universityCode);
            throw new SecurityException(
                    "Caller does not own university " + universityCode + " (cross-tenant forbidden)");
        }
    }

    private String extractCallerUniversityCode() {
        return extractJwt().getClaimAsString(CLAIM_UNIVERSITY_CODE);
    }

    private Jwt extractJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        log.warn("Tenant guard rejected non-JWT authentication: type={}",
                auth == null ? "null" : auth.getClass().getSimpleName());
        throw new SecurityException("Tenant-scoped operation requires JWT authentication");
    }

    private boolean hasAdminBypass(Jwt jwt) {
        List<String> authorities = jwt.getClaimAsStringList(CLAIM_AUTHORITIES);
        return authorities != null && authorities.contains(AUTHORITY_ADMIN_FULL);
    }
}
