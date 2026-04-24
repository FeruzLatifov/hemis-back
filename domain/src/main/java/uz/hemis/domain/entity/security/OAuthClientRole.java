package uz.hemis.domain.entity.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Junction entity: {@link OAuthClient} ↔ {@link Role}.
 *
 * <p>Mirrors the {@code user_role} pattern but for machine accounts, so humans
 * and machines share the same {@link Role} / {@code role_permission} / {@code permission}
 * authorization tables (Single Source of Truth — rules.md #3).</p>
 *
 * <p>Includes audit fields ({@code granted_by}, {@code granted_at}) that wouldn't
 * fit a plain {@code @ManyToMany @JoinTable} — hence explicit junction entity.</p>
 *
 * <p><strong>Table:</strong> {@code oauth_client_role} (V003)</p>
 *
 * @since 2.1.0
 */
@Entity
@Table(name = "oauth_client_role")
@Getter
@Setter
public class OAuthClientRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private OAuthClientRoleId id;

    @MapsId("clientId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private OAuthClient client;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /** Subject that granted this role (username or system actor). */
    @Column(name = "granted_by", length = 50)
    private String grantedBy;

    /** Timestamp of grant. Populated in {@link #onCreate()} if left unset. */
    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @PrePersist
    protected void onCreate() {
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuthClientRole that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
