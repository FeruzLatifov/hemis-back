package uz.hemis.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.auth.ClientType;
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
    public OAuthClientResponse toggleStatus(UUID id) {
        OAuthClient client = oAuthClientRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("OAuthClient", "id", id));
        client.setIsActive(!Boolean.TRUE.equals(client.getIsActive()));
        OAuthClient saved = oAuthClientRepository.save(client);
        log.info("OTM oauth_client status toggled: clientId={}, active={}", saved.getClientId(), saved.getIsActive());
        return toResponse(saved, true);
    }

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
