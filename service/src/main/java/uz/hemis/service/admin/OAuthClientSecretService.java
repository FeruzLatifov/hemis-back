package uz.hemis.service.admin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * OAuth client secret generation + hashing for the OTM API-client admin.
 *
 * <p><strong>Secret format:</strong> {@code csec_} + 48-char base64url random (~288-bit entropy).
 * The plaintext is returned to the admin UI <strong>once</strong> (on create / rotate) and NEVER
 * stored or logged; only the BCrypt hash is persisted in {@code oauth_client.client_secret_hash}.</p>
 *
 * <p>Mirrors {@code WebhookSecretService} (ADR-0012). Verification at runtime is done by
 * {@code OAuthClientAuthenticationService} via the same BCrypt matcher.</p>
 */
@Service
public class OAuthClientSecretService {

    private static final String PREFIX = "csec_";
    private static final int SECRET_BYTES = 36;      // 36 bytes -> 48 base64url chars
    private static final int BCRYPT_STRENGTH = 12;   // OWASP 2025 (security/CLAUDE.md)

    private final SecureRandom secureRandom = new SecureRandom();
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    /** Generate a new plaintext secret — {@code csec_<48 base64url chars>}. Shown to the admin once. */
    public String generatePlainSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(bytes);
        String body = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return PREFIX + body;
    }

    /** BCrypt hash for at-rest storage (client_secret_hash). */
    public String hash(String plainSecret) {
        return bcrypt.encode(plainSecret);
    }
}
