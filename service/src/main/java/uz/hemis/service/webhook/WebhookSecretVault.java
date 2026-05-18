package uz.hemis.service.webhook;

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
 *   <li>Secret generate qilinganda admin UI plain qiymatni vault'ga qo'shadi va Univer'ga ko'rsatadi</li>
 *   <li>Vault in-memory cache (ConcurrentHashMap) — application restart'da yo'qoladi</li>
 *   <li>Restart'dan keyin admin UI yangidan secret rotation qilishi kerak</li>
 * </ul>
 *
 * <p><strong>Production refactor (kelajakda):</strong> HashiCorp Vault / AWS Secrets
 * Manager / Kubernetes Secret bilan integratsiya. Hozir MVP — in-memory.</p>
 *
 * @since ADR-0012
 */
@Component
@Slf4j
public class WebhookSecretVault {

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
        String secret = universitySecrets.get(target.getUniversityCode());
        if (secret == null) {
            throw new WebhookSecretMissingException(
                    "Plain secret unavailable for " + target.getUniversityCode() +
                            " — admin must regenerate via /api/v1/web/admin/webhooks/{id}/regenerate-secret"
            );
        }
        return secret;
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
