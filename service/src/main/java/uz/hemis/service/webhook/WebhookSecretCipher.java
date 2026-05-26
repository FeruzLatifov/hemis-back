package uz.hemis.service.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Webhook plain secret shifrlash — AES-256-GCM ({@link Encryptors#delux}).
 *
 * <p><strong>K1 (2026-05-26):</strong> markaz har outbound webhook'ni plain HMAC secret bilan
 * imzolaydi. Avval plain secret faqat in-memory {@code WebhookSecretVault} da edi → application
 * restart'da 224 OTM ning hammasi uchun imzo sindirardi. Endi plain secret
 * {@code webhook_target.secret_enc} ustunida AES-256-GCM bilan shifrlangan holda saqlanadi
 * va restart'dan keyin vault DB'dan lazy decrypt qilib rehydrate qiladi.</p>
 *
 * <p><strong>Kalit boshqaruvi:</strong> {@code HEMIS_WEBHOOK_SECRET_ENCRYPTION_KEY} prod'da
 * MAJBURIY (env/KMS). Kalit yo'qolsa — barcha secret deshifrlanmaydi, rotation kerak.
 * Kelajak: HashiCorp Vault / AWS Secrets Manager / K8s Secret bilan almashtirish.</p>
 *
 * @since ADR-0012 (K1)
 */
@Component
public class WebhookSecretCipher {

    private final TextEncryptor encryptor;

    public WebhookSecretCipher(
            @Value("${hemis.webhook.secret-encryption.key}") String key,
            @Value("${hemis.webhook.secret-encryption.salt}") String salt) {
        // Encryptors.delux → AES-256-GCM, hex-encoded TextEncryptor. salt hex-encoded bo'lishi shart.
        this.encryptor = Encryptors.delux(key, salt);
    }

    /** Plain secret → AES-256-GCM hex ciphertext (DB secret_enc ga yoziladi). */
    public String encrypt(String plainSecret) {
        return encryptor.encrypt(plainSecret);
    }

    /** secret_enc hex ciphertext → plain secret (imzo qo'yish uchun). */
    public String decrypt(String encrypted) {
        return encryptor.decrypt(encrypted);
    }
}
