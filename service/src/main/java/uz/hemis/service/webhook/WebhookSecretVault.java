package uz.hemis.service.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.hemis.domain.entity.webhook.WebhookTarget;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Webhook secret vault — plain HMAC secret in-memory storage.
 *
 * <p><strong>Sabab:</strong> {@code webhook_target.secret_hash} bcrypt hash sifatida
 * saqlanadi (markaz hech qachon plain secret'ni DB'da saqlamaydi). Lekin signature
 * hisoblash uchun plain secret kerak. Vault bu nuans'ni hal qiladi:</p>
 *
 * <ul>
 *   <li>Secret generate qilinganda admin UI plain qiymatni vault'ga (cache) + DB'ga
 *       (AES-256-GCM {@code secret_enc}) yozadi va Univer'ga bir marta ko'rsatadi</li>
 *   <li>Vault — in-memory cache. Cache miss bo'lsa (K1, 2026-05-26) {@code secret_enc}'dan
 *       lazy decrypt qilib rehydrate qiladi → <strong>application restart'da yo'qolmaydi</strong></li>
 * </ul>
 *
 * <p><strong>Production refactor (kelajakda):</strong> HashiCorp Vault / AWS Secrets
 * Manager / Kubernetes Secret bilan integratsiya (kalit boshqaruvi). Hozir — DB-da AES-256-GCM.</p>
 *
 * @since ADR-0012 (K1: DB persistence)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookSecretVault {

    private final WebhookSecretCipher cipher;
    private final ConcurrentMap<String, String> universitySecrets = new ConcurrentHashMap<>();

    /**
     * Plain secret saqlash (admin UI secret regenerate paytida chaqiriladi).
     *
     * @param universityCode  OTM identifikator
     * @param plainSecret     {@code whsec_xxx} format
     */
    public void store(String universityCode, String plainSecret) {
        universitySecrets.put(universityCode, plainSecret);
        log.info("Webhook secret stored for university={}", universityCode);
    }

    /**
     * Plain secret olish (dispatch paytida HMAC signature uchun).
     *
     * @throws WebhookSecretMissingException  agar secret restart'dan keyin yo'q bo'lsa
     */
    public String resolve(WebhookTarget target) {
        String code = target.getUniversityCode();
        String secret = universitySecrets.get(code);
        if (secret != null) {
            return secret;
        }
        // Restart-safe (K1): cache miss → DB'dagi shifrlangan secret_enc'dan rehydrate.
        if (target.getSecretEnc() != null && !target.getSecretEnc().isBlank()) {
            String plain = cipher.decrypt(target.getSecretEnc());
            universitySecrets.put(code, plain);
            log.debug("Webhook secret rehydrated from DB for university={}", code);
            return plain;
        }
        throw new WebhookSecretMissingException(
                "Plain secret unavailable for " + code + " — secret_enc bo'sh. "
                        + "Admin regenerate qilishi kerak: /api/v1/web/admin/webhooks/{id}/regenerate-secret"
        );
    }

    /** Secret o'chirish (target deletion). */
    public void remove(String universityCode) {
        universitySecrets.remove(universityCode);
    }

    /** Aktiv secret soni (monitoring). */
    public int size() {
        return universitySecrets.size();
    }

    public static class WebhookSecretMissingException extends RuntimeException {
        public WebhookSecretMissingException(String message) {
            super(message);
        }
    }
}
