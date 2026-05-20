package uz.hemis.domain.entity.webhook;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;

/**
 * Webhook target — 254 ta OTM Univer'ga HMAC callback uchun secret + per-OTM tuning.
 *
 * <p><strong>URL convention (2026-05-18):</strong> Callback URL'ni bu yerda
 * saqlamaymiz — har OTM uchun bir xil suffix dubl bo'lar edi. URL deyiladi:
 * {@code ${webhook.callback.protocol}://{university.student_url}${webhook.callback.suffix}}
 * — masalan {@code https://student.adu.uz/rest/v1/hemis-callback/event}.
 * {@code student_url} {@code hemishe_e_university} jadvalidan keladi (254/254 to'ldirilgan).</p>
 *
 * <p><strong>Active flag:</strong> {@code hemishe_e_university.active}'dan keladi —
 * bu yerda alohida flag dubl bo'lar edi.</p>
 *
 * <p><strong>Secret saqlash:</strong> Plain secret faqat generate paytida UI'da
 * bir marta ko'rsatiladi. Markazda {@code secret_hash} (bcrypt) saqlanadi.
 * Univer o'z {@code .env}'iga yozadi va har request'da signature hisoblaydi.</p>
 *
 * <p><strong>Auditing:</strong> {@link AuditableEntity} — modern schema audit columns
 * ({@code created_at/by}, {@code updated_at/by}, {@code deleted_at/by}, {@code version}).</p>
 *
 * @see V015_create_webhook_infrastructure.sql
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

    /**
     * HMAC secret bcrypt hash. Plain qiymat hech qachon saqlanmaydi —
     * generate paytida UI'da ko'rsatiladi, Univer {@code .env}'ga yozadi.
     */
    @Column(name = "secret_hash", nullable = false, length = 255)
    private String secretHash;

    /** Inson o'qiy oladigan tavsif (ixtiyoriy). */
    @Column(name = "description", length = 255)
    private String description;

    /** HTTP request timeout (millisekund). Default 30s. */
    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs = 30000;

    /** Maksimal retry urinish. Bekor qilinsa event DLQ topic'ga tushadi. */
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
}
