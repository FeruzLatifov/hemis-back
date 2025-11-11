package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * Language Entity - UNIVER Pattern Implementation
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Manage available languages in the system</li>
 *   <li>Support 9 languages with active/inactive toggle</li>
 *   <li>Provide display names in multiple languages (JSONB translations)</li>
 * </ul>
 *
 * <p><strong>UNIVER Pattern:</strong></p>
 * <ul>
 *   <li>Similar to h_language classifier in UNIVER</li>
 *   <li>Position field for custom ordering</li>
 *   <li>Active flag for enable/disable languages</li>
 *   <li>Code field as unique identifier (uz-UZ, ru-RU, etc.)</li>
 * </ul>
 *
 * <p><strong>Supported Languages:</strong></p>
 * <ul>
 *   <li>uz-UZ - O'zbekcha (Lotin)</li>
 *   <li>oz-UZ - Ўзбекча (Kirill)</li>
 *   <li>ru-RU - Русский</li>
 *   <li>en-US - English</li>
 *   <li>kk-UZ - Қарақалпақша</li>
 *   <li>tg-TG - Тоҷикӣ</li>
 *   <li>kz-KZ - Қазақша</li>
 *   <li>tm-TM - Türkmençe</li>
 *   <li>kg-KG - Кыргызча</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * Language language = new Language();
 * language.setCode("uz-UZ");
 * language.setName("O'zbekcha");
 * language.setNativeName("O'zbek tili");
 * language.setActive(true);
 * language.setPosition(1);
 * </pre>
 *
 * @see ModernBaseEntity
 * @since 2.0.0
 */
@Entity
@Table(
    name = "h_language",
    indexes = {
        @Index(name = "idx_language_code", columnList = "code", unique = true),
        @Index(name = "idx_language_active", columnList = "is_active"),
        @Index(name = "idx_language_position", columnList = "position")
    }
)
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language extends ModernBaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Language Constants
    // =====================================================

    public static final String UZBEK_LATIN = "uz-UZ";
    public static final String UZBEK_CYRILLIC = "oz-UZ";
    public static final String RUSSIAN = "ru-RU";
    public static final String ENGLISH = "en-US";
    public static final String KARAKALPAK = "kk-UZ";
    public static final String TAJIK = "tg-TG";
    public static final String KAZAKH = "kz-KZ";
    public static final String TURKMEN = "tm-TM";
    public static final String KYRGYZ = "kg-KG";

    // =====================================================
    // Language Properties
    // =====================================================

    /**
     * Language code (ISO 639-1 + region)
     * <p>Format: language-REGION (e.g., uz-UZ, ru-RU)</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>uz-UZ - Uzbek (Latin script)</li>
     *   <li>oz-UZ - Uzbek (Cyrillic script)</li>
     *   <li>ru-RU - Russian</li>
     *   <li>en-US - English (US)</li>
     * </ul>
     */
    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    /**
     * Display name in English
     * <p>Used for admin panels and internal references</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>Uzbek (Latin)</li>
     *   <li>Russian</li>
     *   <li>English</li>
     * </ul>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Native name of the language
     * <p>How the language is written in its own script</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>O'zbekcha (for uz-UZ)</li>
     *   <li>Ўзбекча (for oz-UZ)</li>
     *   <li>Русский (for ru-RU)</li>
     *   <li>English (for en-US)</li>
     * </ul>
     */
    @Column(name = "native_name", nullable = false, length = 100)
    private String nativeName;

    /**
     * ISO 639-1 language code (2 letters)
     * <p>Used for language detection and fallback logic</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>uz (Uzbek)</li>
     *   <li>ru (Russian)</li>
     *   <li>en (English)</li>
     * </ul>
     */
    @Column(name = "iso_code", length = 2)
    private String isoCode;

    /**
     * Display position/order
     * <p>Lower numbers appear first in UI</p>
     *
     * <p><strong>Default order:</strong></p>
     * <ol>
     *   <li>uz-UZ (position 1)</li>
     *   <li>oz-UZ (position 2)</li>
     *   <li>ru-RU (position 3)</li>
     *   <li>en-US (position 4)</li>
     * </ol>
     */
    @Column(name = "position")
    @Builder.Default
    private Integer position = 999;

    /**
     * Active status flag
     * <p>Inactive languages are not shown in UI language selectors</p>
     *
     * <p><strong>Use cases:</strong></p>
     * <ul>
     *   <li>true - Language is active and available</li>
     *   <li>false - Language is hidden (not yet translated, temporarily disabled)</li>
     * </ul>
     *
     * <p><strong>Note:</strong> Default languages (uz-UZ, oz-UZ, ru-RU) are always enabled</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    /**
     * Right-to-left flag
     * <p>For languages like Arabic, Hebrew, Persian</p>
     */
    @Column(name = "is_rtl")
    @Builder.Default
    private Boolean isRtl = false;

    /**
     * System default flag
     * <p>Cannot be disabled through UI</p>
     */
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if language is active
     *
     * @return true if is_active = true
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Check if language is right-to-left
     *
     * @return true if is_rtl = true
     */
    public boolean isRtl() {
        return Boolean.TRUE.equals(isRtl);
    }

    /**
     * Check if language is system default
     *
     * @return true if is_default = true
     */
    public boolean isDefault() {
        return Boolean.TRUE.equals(isDefault);
    }

    /**
     * Check if this is a default language that cannot be disabled
     *
     * @return true if code is uz-UZ, oz-UZ, or ru-RU
     */
    public boolean isSystemDefault() {
        return UZBEK_LATIN.equals(code)
            || UZBEK_CYRILLIC.equals(code)
            || RUSSIAN.equals(code);
    }

    // =====================================================
    // Object Methods
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Language)) return false;
        Language language = (Language) o;
        return getId() != null && getId().equals(language.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Language{" +
            "id=" + getId() +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", nativeName='" + nativeName + '\'' +
            ", position=" + position +
            ", isActive=" + isActive +
            ", isDefault=" + isDefault +
            '}';
    }
}
