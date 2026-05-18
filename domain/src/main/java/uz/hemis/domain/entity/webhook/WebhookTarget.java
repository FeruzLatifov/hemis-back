package uz.hemis.domain.entity.webhook;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;

/**
 * Webhook target — 224 ta OTM Univer'ning callback URL ro'yxati.
 *
 * <p>Markaz event sodir bo'lganda (klassifikator update, qoida push) bu jadvaldan
 * faol OTM URL'lari olinadi va REST callback yuboriladi. Univer endpoint'i HMAC
 * SHA-256 signature orqali autentifikatsiya qiladi.</p>
 *
 * <p><strong>Secret saqlash:</strong> Plain secret faqat generate paytida UI'da
 * bir marta ko'rsatiladi. Markazda {@code secret_hash} (bcrypt) saqlanadi.
 * Univer o'z {@code .env}'iga yozadi va har request'da signature hisoblaydi.</p>
 *
 * <p><strong>Auditing:</strong> {@link AuditableEntity} — modern schema audit columns
 * ({@code created_at/by}, {@code updated_at/by}, {@code deleted_at/by}, {@code version}).</p>
 *
 * @see V016_create_webhook_infrastructure.sql
 * @since ADR-0012
 */
@Entity
@Table(name = "webhook_target")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class WebhookTarget extends AuditableEntity {

    /** OTM identifikator — hemis_NNN dagi NNN qism (per-OTM UNIQUE). */
    @Column(name = "university_code", nullable = false, length = 10)
    private String universityCode;

    /** Univer-side qabul qiluvchi URL (HTTPS production, http://localhost dev). */
    @Column(name = "callback_url", nullable = false, length = 500)
    private String callbackUrl;

    /**
     * HMAC secret bcrypt hash. Plain qiymat hech qachon saqlanmaydi —
     * generate paytida UI'da ko'rsatiladi, Univer {@code .env}'ga yozadi.
     */
    @Column(name = "secret_hash", nullable = false, length = 255)
    private String secretHash;

    /** Inson o'qiy oladigan tavsif (ixtiyoriy). */
    @Column(name = "description", length = 255)
    private String description;

    /** {@code FALSE} = consumer event yubormaydi (offline, manual disable, debug). */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /** HTTP request timeout (millisekund). Default 30s. */
    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs = 30000;

    /** Maksimal retry urinish. Bekor qilinsa event DLQ topic'ga tushadi. */
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 5;

    /** Aktiv va o'chirilmagan target ekanligini tekshirish. */
    @Transient
    public boolean isDeliverable() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }
}
