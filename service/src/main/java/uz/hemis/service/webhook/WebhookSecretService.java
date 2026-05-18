package uz.hemis.service.webhook;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * HMAC secret generation + hashing service.
 *
 * <p><strong>Secret format:</strong> {@code whsec_} + 48 char base64url random (~288 bit entropy).</p>
 *
 * <p><strong>Storage:</strong></p>
 * <ul>
 *   <li>Plain secret → admin UI'ga bir marta qaytariladi + {@link WebhookSecretVault} (in-memory)</li>
 *   <li>Bcrypt hash → {@code webhook_target.secret_hash} (DB)</li>
 *   <li>OTM IT plain secret'ni {@code .env}'ga yozadi (HEMIS_WEBHOOK_SECRET)</li>
 * </ul>
 *
 * @since ADR-0012
 */
@Service
public class WebhookSecretService {

    private static final String PREFIX = "whsec_";
    private static final int SECRET_BYTES = 36;  // 36 bytes → 48 base64 chars
    private static final int BCRYPT_STRENGTH = 12;  // OWASP 2025 (CLAUDE.md)

    private final SecureRandom secureRandom = new SecureRandom();
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    /**
     * Yangi secret generate qilish — {@code whsec_<48 base64url chars>}.
     */
    public String generatePlainSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(bytes);
        String body = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return PREFIX + body;
    }

    /**
     * Bcrypt hash DB saqlash uchun.
     */
    public String hash(String plainSecret) {
        return bcrypt.encode(plainSecret);
    }

    /**
     * Plain secret verify (rotation check).
     */
    public boolean matches(String plainSecret, String hash) {
        return bcrypt.matches(plainSecret, hash);
    }
}
