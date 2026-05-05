package uz.hemis.domain.entity.university;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.math.BigDecimal;

/**
 * University public profile — contacts, social media, documents.
 *
 * <p><strong>Table:</strong> university_profile (1:1 with university)</p>
 *
 * <p>Managed by admin panel or synced from univer (230 universities).
 * Files stored in MinIO (S3-compatible), database stores metadata only.</p>
 *
 * <p>Extends AuditableEntity — soft delete via deleted_at/deleted_by.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_profile")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"socialLinks", "documents", "phone", "email"})
public class UniversityProfile extends AuditableEntity {

    @Column(name = "university_code", nullable = false, unique = true)
    private String universityCode;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email")
    private String email;

    /** JSONB — flexible social media links */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    private String socialLinks;

    /** MinIO object key for logo */
    @Column(name = "logo_key", length = 500)
    private String logoKey;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** JSONB array — document metadata (file stored in MinIO) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documents", columnDefinition = "jsonb")
    private String documents;

    /** External map link (Google/Yandex Maps) — pasted by admin */
    @Column(name = "map_url", columnDefinition = "TEXT")
    private String mapUrl;

    /** WGS84 latitude — extracted from map_url or entered manually */
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    /** WGS84 longitude — extracted from map_url or entered manually */
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "source_uid")
    private String sourceUid;

    @Column(name = "hash", length = 64)
    private String hash;
}
