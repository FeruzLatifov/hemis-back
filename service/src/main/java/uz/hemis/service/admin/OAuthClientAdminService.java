package uz.hemis.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.auth.ClientType;
import uz.hemis.common.validation.SecretStrengthPolicy;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.security.OAuthClient;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.repository.OAuthClientRepository;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.admin.dto.OAuthClientCreateRequest;
import uz.hemis.service.admin.dto.OAuthClientResponse;
import uz.hemis.service.admin.dto.OAuthClientSecretResponse;
import uz.hemis.service.admin.dto.OAuthClientSecretRotateRequest;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * OTM API-client (oauth_client) administration — the machine-account counterpart of
 * {@link UserAdminService}. Creates and manages {@code client_credentials} clients for the Univer
 * API. Deliberately SEPARATE from {@code users}: an OTM integration account lives ONLY here, not in
 * the human user table, to avoid the dual-identity confusion.
 *
 * <p>The secret is provided by the admin (like a password) and stored BCrypt-hashed — it is never
 * returned. Every UNIVERSITY_BACKEND client is bound to the {@code OTM_API} role so its tokens carry
 * the OTM permission set (mirrors the M001 dual-write).</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OAuthClientAdminService {

    private final OAuthClientRepository oAuthClientRepository;
    private final UniversityRepository universityRepository;
    private final RoleRepository roleRepository;
    private final OAuthClientSecretService secretService;

    // =====================================================
    // READ
    // =====================================================

    public Page<OAuthClientResponse> getClients(String search, String clientType, String universityCode,
                                                Boolean active, Pageable pageable) {
        // clientType filter omitted (all OTM clients are UNIVERSITY_BACKEND); param kept for API symmetry.
        return oAuthClientRepository
                .findAllFiltered(nullToEmpty(search), nullToEmpty(universityCode), active, pageable)
                .map(c -> toResponse(c, false));
    }

    public OAuthClientResponse getClient(UUID id) {
        OAuthClient client = oAuthClientRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("OAuthClient", "id", id));
        return toResponse(client, true);
    }

    // =====================================================
    // WRITE
    // =====================================================

    @Audited(action = AuditAction.CREATE, entity = "OAuthClient", entityClass = OAuthClient.class)
    @Transactional
    public OAuthClientResponse createClient(OAuthClientCreateRequest request) {
        if (oAuthClientRepository.existsByClientId(request.getClientId())) {
            throw new BadRequestException("Client already exists: " + request.getClientId());
        }
        University uni = universityRepository.findById(request.getUniversityCode())
                .orElseThrow(() -> new BadRequestException("University not found: " + request.getUniversityCode()));

        OAuthClient client = new OAuthClient();
        client.setClientId(request.getClientId());
        client.setClientSecretHash(secretService.hash(request.getClientSecret()));
        client.setClientName((request.getClientName() != null && !request.getClientName().isBlank())
                ? request.getClientName() : uni.getName());
        client.setClientType(ClientType.UNIVERSITY_BACKEND);
        client.setUniversity(uni);
        client.setGrantTypes(new ArrayList<>(List.of("client_credentials", "password")));
        client.setIsActive(request.getActive() == null || request.getActive());
        roleRepository.findByCode("OTM_API").ifPresent(r -> client.getRoles().add(r));

        OAuthClient saved = oAuthClientRepository.save(client);
        log.info("OTM oauth_client created: clientId={}, university={}, active={}",
                saved.getClientId(), uni.getCode(), saved.getIsActive());
        return toResponse(saved, true);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "OAuthClient", entityClass = OAuthClient.class, keyArg = "id")
    public OAuthClientResponse toggleStatus(UUID id) {
        OAuthClient client = oAuthClientRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("OAuthClient", "id", id));
        client.setIsActive(!Boolean.TRUE.equals(client.getIsActive()));
        OAuthClient saved = oAuthClientRepository.save(client);
        log.info("OTM oauth_client status toggled: clientId={}, active={}", saved.getClientId(), saved.getIsActive());
        return toResponse(saved, true);
    }

    /**
     * Maxfiy kalit almashtirish (rotatsiya).
     *
     * <p>So'rov tanasi bo'sh bo'lsa markaz kriptografik kuchli maxfiy kalit generatsiya qiladi va uni
     * javobda BIR MARTA qaytaradi. Admin o'z qiymatini bersa — ochiq matn javobda qaytarilmaydi.</p>
     *
     * <p><strong>Kuchga kirishi — faqat YANGI tokenlarga.</strong>
     * {@code OAuthClientAuthenticationService} keshlamaydi, har token so'rovida DB hashiga
     * solishtiradi, ya'ni eski maxfiy kalit bilan yangi token OLINMAYDI. Lekin allaqachon berilgan JWT'lar
     * <strong>24 soatgacha</strong> ishlashda davom etadi
     * ({@code hemis.security.oauth.client-token-expiration}, default 86400s — bu vazirlik bo'ylab
     * yagona siyosat; entity'dagi {@code access_token_ttl_seconds} ustuni token berishda
     * ISHLATILMAYDI).</p>
     *
     * <p><strong>Diqqat:</strong> {@link #toggleStatus(UUID)} ham berilgan tokenlarni bekor
     * QILMAYDI. {@code is_active} faqat token berish paytida tekshiriladi
     * ({@code findOperationalByClientIdWithPermissions}); JWT filtri har so'rovda oauth_client'ni
     * qayta o'qimaydi — faqat imzo va blacklist. Ya'ni hozircha mashina tokenini muddatidan oldin
     * bekor qilishning yo'li YO'Q; bu alohida ish (jti blacklist yoki tokens_valid_from).</p>
     *
     * <p>Ochiq maxfiy kalit logga, auditga yoki keshga HECH QACHON yozilmaydi.</p>
     */
    @Audited(action = AuditAction.UPDATE, entity = "OAuthClient", entityClass = OAuthClient.class, keyArg = "id")
    @Transactional
    public OAuthClientSecretResponse rotateSecret(UUID id, OAuthClientSecretRotateRequest request) {
        OAuthClient client = oAuthClientRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("OAuthClient", "id", id));

        String supplied = (request != null) ? request.getClientSecret() : null;
        boolean generated = (supplied == null || supplied.isBlank());
        String plainSecret = generated ? secretService.generatePlainSecret() : supplied.trim();

        // Admin bergan qiymat uchun server-tomon tekshiruvlar. Frontend'dagi baho
        // (secretStrength.ts) foydalanuvchiga yordam beradi, LEKIN xavfsizlik nazorati EMAS —
        // endpoint to'g'ridan-to'g'ri ham chaqirilishi mumkin.
        if (!generated) {
            // Mustahkamlik siyosati — frontend bahosining server-tomon nusxasi
            // (SecretStrengthPolicy javadoc'iga qarang: nega entropiya, tarkib qoidalari emas).
            var violations = SecretStrengthPolicy.validate(plainSecret, client.getClientId());
            if (!violations.isEmpty()) {
                throw new BadRequestException(SecretStrengthPolicy.describe(violations));
            }
            // Eski maxfiy kalitni "yangi" deb qayta o'rnatish rotatsiya emas — OTM hech narsa
            // o'zgarmaganini bilmay qoladi, eski maxfiy kalit esa amalda qolaveradi.
            if (secretService.matches(plainSecret, client.getClientSecretHash())) {
                throw new BadRequestException("Yangi maxfiy kalit eskisidan farq qilishi kerak");
            }
        }

        client.setClientSecretHash(secretService.hash(plainSecret));
        client.setSecretRotatedAt(LocalDateTime.now());
        client.setSecretVersion(client.getSecretVersion() == null ? 2 : client.getSecretVersion() + 1);
        OAuthClient saved = oAuthClientRepository.save(client);

        log.warn("OTM oauth_client secret rotated: clientId={}, version={}, generated={} — OTM .env update required",
                saved.getClientId(), saved.getSecretVersion(), generated);

        return generated
                ? OAuthClientSecretResponse.generated(saved.getId(), saved.getClientId(), plainSecret,
                        saved.getSecretVersion(), saved.getSecretRotatedAt())
                : OAuthClientSecretResponse.supplied(saved.getId(), saved.getClientId(),
                        saved.getSecretVersion(), saved.getSecretRotatedAt());
    }

    @Audited(action = AuditAction.DELETE, entity = "OAuthClient", entityClass = OAuthClient.class, keyArg = "id")
    @Transactional
    public void softDelete(UUID id) {
        OAuthClient client = oAuthClientRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("OAuthClient", "id", id));
        client.setIsActive(false);
        client.softDelete();
        oAuthClientRepository.save(client);
        log.info("OTM oauth_client soft-deleted: clientId={}", client.getClientId());
    }

    // =====================================================
    // Helpers
    // =====================================================

    private OAuthClientResponse toResponse(OAuthClient c, boolean includeRoles) {
        return OAuthClientResponse.builder()
                .id(c.getId())
                .clientId(c.getClientId())
                .clientName(c.getClientName())
                .clientType(c.getClientType() != null ? c.getClientType().name() : null)
                .universityCode(c.getUniversity() != null ? c.getUniversity().getCode() : null)
                .universityName(c.getUniversity() != null ? c.getUniversity().getName() : null)
                .active(c.getIsActive())
                .grantTypes(c.getGrantTypes())
                .scopes(c.getScopes())
                .roles(includeRoles ? c.getRoles().stream().map(Role::getCode).sorted().toList() : null)
                .secretVersion(c.getSecretVersion())
                .secretRotatedAt(c.getSecretRotatedAt())
                .lastUsedAt(c.getLastUsedAt())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
