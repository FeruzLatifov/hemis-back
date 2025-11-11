package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Translation Entity
 * Stores multi-language translations for the application
 *
 * @author System Architect
 */
@Entity
@Table(
    name = "translations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_translations_key_locale",
            columnNames = {"translation_key", "locale"}
        )
    },
    indexes = {
        @Index(name = "idx_translations_key", columnList = "translation_key"),
        @Index(name = "idx_translations_locale", columnList = "locale"),
        @Index(name = "idx_translations_category", columnList = "category")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "translation_key", nullable = false, length = 255)
    private String key;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "translation_value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "category", length = 50)
    @Builder.Default
    private String category = "GENERAL";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
