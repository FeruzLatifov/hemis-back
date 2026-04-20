package uz.hemis.domain.entity.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.hemis.domain.entity.base.ImmutableEntity;

import java.time.LocalDateTime;

/**
 * Password reset token — append-only. Bir marta ishlatiladi ({@code used=true}) yoki
 * muddati tugaydi ({@code expiresAt}).
 *
 * <p>Audit ustunlari ({@code createdAt}, {@code createdBy}) {@link ImmutableEntity} dan
 * meros — Spring Data JPA Auditing avtomatik to'ldiradi.</p>
 */
@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken extends ImmutableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
