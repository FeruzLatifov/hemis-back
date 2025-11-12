package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * System Configuration Entity - UNIVER Pattern
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Store system-wide configuration settings</li>
 *   <li>Key-value storage for dynamic settings</li>
 *   <li>Support for language toggles, features, API keys, etc.</li>
 * </ul>
 *
 * <p><strong>UNIVER Pattern:</strong></p>
 * <ul>
 *   <li>Similar to e_system_config in UNIVER</li>
 *   <li>Path-based key (e.g., system.language.uz-UZ)</li>
 *   <li>Text value for flexibility</li>
 *   <li>Redis cache for performance</li>
 * </ul>
 *
 * <p><strong>Example Configurations:</strong></p>
 * <ul>
 *   <li>system.language.uz-UZ = true (enable Uzbek Latin)</li>
 *   <li>system.language.en-US = false (disable English)</li>
 *   <li>system.default_language = uz-UZ</li>
 *   <li>app.maintenance_mode = false</li>
 *   <li>api.rate_limit = 1000</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * SystemConfiguration config = new SystemConfiguration();
 * config.setPath("system.language.uz-UZ");
 * config.setValue("true");
 * config.setDescription("Enable Uzbek (Latin) language");
 * </pre>
 *
 * @see ModernBaseEntity
 * @since 2.0.0
 */
@Entity
@Table(
    name = "configurations",
    indexes = {
        @Index(name = "idx_configurations_path", columnList = "path", unique = true),
        @Index(name = "idx_configurations_category", columnList = "category")
    }
)
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfiguration extends ModernBaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Configuration Properties
    // =====================================================

    /**
     * Configuration path (unique key)
     * <p>Format: category.subcategory.key</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>system.language.uz-UZ</li>
     *   <li>system.language.ru-RU</li>
     *   <li>system.default_language</li>
     *   <li>app.maintenance_mode</li>
     *   <li>security.session_timeout</li>
     * </ul>
     */
    @Column(name = "path", nullable = false, unique = true, length = 255)
    private String path;

    /**
     * Configuration value (as string)
     * <p>Stored as TEXT for flexibility</p>
     *
     * <p><strong>Type Examples:</strong></p>
     * <ul>
     *   <li>Boolean: "true" or "false"</li>
     *   <li>Number: "1000", "3.14"</li>
     *   <li>String: "uz-UZ", "password123"</li>
     *   <li>JSON: '{"key": "value"}'</li>
     * </ul>
     */
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    /**
     * Configuration category for organization
     * <p>Used for grouping in admin UI</p>
     *
     * <p><strong>Categories:</strong></p>
     * <ul>
     *   <li>system - System-wide settings</li>
     *   <li>language - Language management</li>
     *   <li>security - Security settings</li>
     *   <li>app - Application settings</li>
     *   <li>integration - External integration settings</li>
     * </ul>
     */
    @Column(name = "category", length = 64)
    private String category;

    /**
     * Human-readable description
     * <p>Helps admins understand what this configuration does</p>
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Data type hint
     * <p>Used for UI rendering (checkbox, input, select, etc.)</p>
     *
     * <p><strong>Types:</strong></p>
     * <ul>
     *   <li>boolean - Checkbox</li>
     *   <li>number - Number input</li>
     *   <li>string - Text input</li>
     *   <li>password - Password input</li>
     *   <li>json - JSON editor</li>
     * </ul>
     */
    @Column(name = "value_type", length = 32)
    @Builder.Default
    private String valueType = "string";

    /**
     * Is this config editable through UI?
     * <p>Some configs (like system.version) should not be editable</p>
     */
    @Column(name = "is_editable")
    @Builder.Default
    private Boolean isEditable = true;

    /**
     * Is this a sensitive value (password, API key)?
     * <p>Sensitive values are masked in admin UI</p>
     */
    @Column(name = "is_sensitive")
    @Builder.Default
    private Boolean isSensitive = false;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Get value as boolean
     *
     * @return true if value is "true", "1", "yes"
     */
    public boolean getBooleanValue() {
        if (value == null) return false;
        return "true".equalsIgnoreCase(value)
            || "1".equals(value)
            || "yes".equalsIgnoreCase(value);
    }

    /**
     * Get value as integer
     *
     * @return Integer value or null if invalid
     */
    public Integer getIntegerValue() {
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get value as long
     *
     * @return Long value or null if invalid
     */
    public Long getLongValue() {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Check if config is editable
     *
     * @return true if is_editable = true
     */
    public boolean isEditable() {
        return Boolean.TRUE.equals(isEditable);
    }

    /**
     * Check if config is sensitive
     *
     * @return true if is_sensitive = true
     */
    public boolean isSensitive() {
        return Boolean.TRUE.equals(isSensitive);
    }

    // =====================================================
    // Object Methods
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemConfiguration)) return false;
        SystemConfiguration that = (SystemConfiguration) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SystemConfiguration{" +
            "id=" + getId() +
            ", path='" + path + '\'' +
            ", value='" + (isSensitive() ? "***" : value) + '\'' +
            ", category='" + category + '\'' +
            ", valueType='" + valueType + '\'' +
            '}';
    }
}
