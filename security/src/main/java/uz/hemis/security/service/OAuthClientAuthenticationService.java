package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.security.OAuthClient;
import uz.hemis.domain.repository.OAuthClientRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Authentication service for OAuth 2.0 B2B machine accounts (client_credentials grant).
 *
 * <p>Validates in the following order (fail-fast):</p>
 * <ol>
 *   <li>client_id exists and is operational (active, not deleted, not expired)</li>
 *   <li>client_secret matches the stored BCrypt hash (via {@link PasswordEncoder})</li>
 *   <li>The requested grant type is in {@code grant_types}</li>
 *   <li>Remote IP falls inside {@code allowed_ip_cidr} (if configured)</li>
 * </ol>
 *
 * <p>Uses the same {@code LegacyPasswordEncoder} as human login, so operators can
 * rotate secrets with any BCrypt tool without per-service-type encoder glue.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthClientAuthenticationService {

    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate a client_credentials grant request.
     *
     * @param clientId OAuth public identifier
     * @param clientSecret plain-text secret provided by the client
     * @param grantType requested grant (e.g. {@code "client_credentials"})
     * @param remoteIp caller IP (from X-Forwarded-For / RemoteAddr)
     * @return the authenticated, operational {@link OAuthClient}
     * @throws OAuthClientAuthenticationException on any validation failure
     */
    @Transactional
    public OAuthClient authenticate(String clientId,
                                    String clientSecret,
                                    String grantType,
                                    String remoteIp) {
        if (clientId == null || clientId.isBlank()) {
            throw OAuthClientAuthenticationException.invalidClient("client_id is required");
        }
        if (clientSecret == null || clientSecret.isEmpty()) {
            throw OAuthClientAuthenticationException.invalidClient("client_secret is required");
        }

        OAuthClient client = clientRepository.findOperationalByClientIdWithPermissions(clientId)
                .orElseThrow(() -> {
                    log.warn("client_credentials: unknown or inactive client_id '{}' from {}", clientId, remoteIp);
                    return OAuthClientAuthenticationException.invalidClient("Unknown or inactive client");
                });

        if (!passwordEncoder.matches(clientSecret, client.getClientSecretHash())) {
            log.warn("client_credentials: invalid secret for '{}' from {}", clientId, remoteIp);
            throw OAuthClientAuthenticationException.invalidClient("Invalid client credentials");
        }

        if (!client.supportsGrant(grantType)) {
            log.warn("client_credentials: grant '{}' not allowed for '{}'", grantType, clientId);
            throw OAuthClientAuthenticationException.unsupportedGrant(
                    "Grant type '" + grantType + "' not allowed for this client");
        }

        if (!isIpAllowed(client.getAllowedIpCidr(), remoteIp)) {
            log.warn("client_credentials: IP {} not whitelisted for '{}'", remoteIp, clientId);
            throw OAuthClientAuthenticationException.invalidClient(
                    "Request IP not whitelisted for this client");
        }

        // Atomic UPDATE — bypasses JPA dirty checking + @Version optimistic locking.
        // Critical for high-traffic clients where parallel token requests would
        // otherwise race and trigger OptimisticLockException on every other call.
        clientRepository.updateLastUsed(client.getId(), remoteIp);
        log.info("client_credentials: authenticated '{}' (type={}, ip={})",
                clientId, client.getClientType(), remoteIp);
        return client;
    }

    /**
     * CIDR whitelist check. Empty / null allow list → no restriction.
     *
     * <p>Supports both exact-IP entries and CIDR ranges ({@code 10.0.0.0/8}).
     * For invalid remote IPs (unparseable), returns {@code false} — fail closed.</p>
     */
    boolean isIpAllowed(List<String> allowedCidr, String remoteIp) {
        if (allowedCidr == null || allowedCidr.isEmpty()) {
            return true;
        }
        if (remoteIp == null || remoteIp.isBlank()) {
            return false;
        }
        InetAddress remote;
        try {
            remote = InetAddress.getByName(remoteIp);
        } catch (UnknownHostException ex) {
            return false;
        }
        for (String entry : allowedCidr) {
            if (entry == null || entry.isBlank()) continue;
            if (matchesCidr(entry.trim(), remote)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCidr(String cidr, InetAddress remote) {
        int slash = cidr.indexOf('/');
        try {
            if (slash < 0) {
                return InetAddress.getByName(cidr).equals(remote);
            }
            InetAddress network = InetAddress.getByName(cidr.substring(0, slash));
            int prefix = Integer.parseInt(cidr.substring(slash + 1));
            byte[] netBytes = network.getAddress();
            byte[] remBytes = remote.getAddress();
            if (netBytes.length != remBytes.length) {
                return false;
            }
            int fullBytes = prefix / 8;
            int remainderBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (netBytes[i] != remBytes[i]) return false;
            }
            if (remainderBits == 0) {
                return true;
            }
            int mask = 0xFF << (8 - remainderBits);
            return (netBytes[fullBytes] & mask) == (remBytes[fullBytes] & mask);
        } catch (Exception ex) {
            return false;
        }
    }
}
