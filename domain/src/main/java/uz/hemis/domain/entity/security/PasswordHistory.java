package uz.hemis.domain.entity.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.hemis.domain.entity.base.ImmutableEntity;

/**
 * Password history — append-only log. Parolni qayta ishlatishni oldini olish uchun.
 *
 * <p>Audit ustunlari ({@code createdAt}, {@code createdBy}) {@link ImmutableEntity} dan
 * meros — Spring Data JPA Auditing avtomatik to'ldiradi.</p>
 */
@Entity
@Table(name = "password_history")
@Getter
@Setter
@NoArgsConstructor
public class PasswordHistory extends ImmutableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}
