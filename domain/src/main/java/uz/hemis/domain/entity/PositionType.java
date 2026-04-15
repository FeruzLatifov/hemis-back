package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PositionType — lavozim turi klassifikatori
 *
 * <p>14 ta guruh: Rektorat, Akademik, Administrativ, Moliyaviy, va boshqalar.</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "position_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionType implements Serializable {

    @Id
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "name_ru", length = 255)
    private String nameRu;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
