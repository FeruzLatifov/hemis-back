package uz.hemis.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * University entity (basic version for authentication)
 *
 * Maps to: hemishe_e_university table
 * Purpose: Educational institutions in HEMIS system
 * Note: Full university entity is in domain module, this is simplified version
 */
@Entity
@Table(name = "hemishe_e_university", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class University {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Unique university code
     * CUBA field: code
     * Format: Usually numeric (001, 002, etc.)
     */
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    /**
     * University name in Uzbek
     * CUBA multilanguage pattern: name_uz, name_ru, name_en
     */
    @Column(name = "name_uz", length = 500)
    private String nameUz;

    /**
     * University name in Russian
     */
    @Column(name = "name_ru", length = 500)
    private String nameRu;

    /**
     * University name in English
     */
    @Column(name = "name_en", length = 500)
    private String nameEn;

    /**
     * Short name in Uzbek
     */
    @Column(name = "name_short_uz", length = 255)
    private String shortNameUz;

    /**
     * Short name in Russian
     */
    @Column(name = "name_short_ru", length = 255)
    private String shortNameRu;

    /**
     * Short name in English
     */
    @Column(name = "name_short_en", length = 255)
    private String shortNameEn;

    /**
     * TIN (Tax Identification Number)
     * CUBA field: tin
     */
    @Column(name = "tin", length = 20)
    private String tin;

    /**
     * Active status
     */
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    /**
     * Soft delete timestamp
     */
    @Column(name = "delete_ts")
    private LocalDateTime deletedAt;

    /**
     * Get name by locale
     */
    public String getName(String locale) {
        return switch (locale) {
            case "ru" -> nameRu != null ? nameRu : nameUz;
            case "en" -> nameEn != null ? nameEn : nameUz;
            default -> nameUz;
        };
    }

    /**
     * Get short name by locale
     */
    public String getShortName(String locale) {
        return switch (locale) {
            case "ru" -> shortNameRu != null ? shortNameRu : shortNameUz;
            case "en" -> shortNameEn != null ? shortNameEn : shortNameUz;
            default -> shortNameUz;
        };
    }

    /**
     * Check if university is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && deletedAt == null;
    }
}
