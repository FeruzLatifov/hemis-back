package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * System Message Entity - UNIVER Pattern Implementation
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Master table for system messages (UI labels, buttons, errors, etc.)</li>
 *   <li>Category-based organization (app, menu, button, label, message, error, validation)</li>
 *   <li>Default message in Uzbek (fallback when translation not found)</li>
 * </ul>
 *
 * <p><strong>UNIVER Pattern:</strong></p>
 * <ul>
 *   <li>Similar to e_system_message in UNIVER</li>
 *   <li>EAV pattern for multi-language support</li>
 *   <li>OneToMany relationship with SystemMessageTranslation</li>
 *   <li>Composite PK in child table (message_id, language)</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * SystemMessage message = new SystemMessage();
 * message.setMessageKey("button.save");
 * message.setCategory("button");
 * message.setMessage("Saqlash");  // Default Uzbek
 * message.setActive(true);
 * </pre>
 *
 * @see SystemMessageTranslation
 * @see ModernBaseEntity
 * @since 2.0.0
 */
@Entity
@Table(
    name = "system_messages",
    indexes = {
        @Index(name = "idx_system_messages_category", columnList = "category"),
        @Index(name = "idx_system_messages_key", columnList = "message_key", unique = true),
        @Index(name = "idx_system_messages_active", columnList = "is_active")
    }
)
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMessage extends ModernBaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Message Classification
    // =====================================================

    /**
     * Message category for organization
     * <p>Values: app, menu, button, label, message, error, validation</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>app - General application messages (welcome, logout)</li>
     *   <li>menu - Navigation menu items</li>
     *   <li>button - Button labels (save, cancel, delete)</li>
     *   <li>label - Form field labels</li>
     *   <li>message - User notifications (success, info)</li>
     *   <li>error - Error messages</li>
     *   <li>validation - Form validation messages</li>
     * </ul>
     */
    @Column(name = "category", nullable = false, length = 64)
    private String category;

    /**
     * Unique message key for programmatic access
     * <p>Format: category.specific_name (lowercase, numbers, underscore only)</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>app.student_name</li>
     *   <li>button.save</li>
     *   <li>error.not_found</li>
     *   <li>validation.required</li>
     * </ul>
     */
    @Column(name = "message_key", nullable = false, unique = true, length = 255)
    private String messageKey;

    /**
     * Default message text in Uzbek (Latin)
     * <p>Used as fallback when translation not found for requested language</p>
     *
     * <p><strong>Fallback Logic:</strong></p>
     * <ol>
     *   <li>Try to find translation for requested language (e.g., ru-RU)</li>
     *   <li>If not found, try language without region (e.g., ru)</li>
     *   <li>If still not found, return this default message (uz)</li>
     * </ol>
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Active status flag
     * <p>Inactive messages are not returned by API</p>
     *
     * <p><strong>Use cases:</strong></p>
     * <ul>
     *   <li>true - Message is active and visible to users</li>
     *   <li>false - Message is hidden (temporarily disabled, being reviewed)</li>
     * </ul>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // =====================================================
    // Relationships
    // =====================================================

    /**
     * Translations for this message in different languages
     * <p>OneToMany relationship with CASCADE delete</p>
     *
     * <p><strong>Relationship:</strong></p>
     * <ul>
     *   <li>One SystemMessage → Many SystemMessageTranslation</li>
     *   <li>Cascade: ALL (when message deleted, translations deleted too)</li>
     *   <li>Orphan removal: true (when translation removed from set, it's deleted from DB)</li>
     *   <li>Fetch: LAZY (translations loaded only when accessed)</li>
     * </ul>
     *
     * <p><strong>JSON Serialization:</strong></p>
     * <p>The SystemMessageTranslation.systemMessage field is marked with @JsonIgnore
     * to prevent circular reference during JSON serialization.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * SystemMessage message = systemMessageRepository.findById(id).get();
     * Set&lt;SystemMessageTranslation&gt; translations = message.getTranslations();
     * // Translations loaded here (LAZY)
     * </pre>
     */
    @OneToMany(
        mappedBy = "systemMessage",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<SystemMessageTranslation> translations = new HashSet<>();

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Add translation to this message
     * <p>Maintains bidirectional relationship</p>
     *
     * @param translation Translation to add
     */
    public void addTranslation(SystemMessageTranslation translation) {
        translations.add(translation);
        translation.setSystemMessage(this);
    }

    /**
     * Remove translation from this message
     * <p>Maintains bidirectional relationship</p>
     *
     * @param translation Translation to remove
     */
    public void removeTranslation(SystemMessageTranslation translation) {
        translations.remove(translation);
        translation.setSystemMessage(null);
    }

    /**
     * Get translation for specific language
     * <p>Returns null if translation not found</p>
     *
     * @param language Language code (e.g., ru-RU, en-US)
     * @return Translation text or null
     */
    public String getTranslation(String language) {
        return translations.stream()
            .filter(t -> t.getLanguage().equals(language))
            .findFirst()
            .map(SystemMessageTranslation::getTranslation)
            .orElse(null);
    }

    /**
     * Get translation with fallback logic
     * <p>UNIVER pattern: language-region → language → default</p>
     *
     * <p><strong>Fallback sequence:</strong></p>
     * <ol>
     *   <li>Try exact match (e.g., ru-RU)</li>
     *   <li>Try language only (e.g., ru)</li>
     *   <li>Return default message (Uzbek)</li>
     * </ol>
     *
     * @param language Language code (e.g., ru-RU)
     * @return Translation text (never null)
     */
    public String getTranslationWithFallback(String language) {
        // Try exact match (e.g., ru-RU)
        String translation = getTranslation(language);
        if (translation != null) {
            return translation;
        }

        // Try language without region (e.g., ru)
        if (language.contains("-")) {
            String languageOnly = language.split("-")[0];
            translation = translations.stream()
                .filter(t -> t.getLanguage().startsWith(languageOnly))
                .findFirst()
                .map(SystemMessageTranslation::getTranslation)
                .orElse(null);
            if (translation != null) {
                return translation;
            }
        }

        // Fallback to default message (Uzbek)
        return message;
    }

    /**
     * Check if message is active
     *
     * @return true if is_active = true
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    // =====================================================
    // Object Methods
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemMessage)) return false;
        SystemMessage that = (SystemMessage) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SystemMessage{" +
            "id=" + getId() +
            ", category='" + category + '\'' +
            ", messageKey='" + messageKey + '\'' +
            ", message='" + message + '\'' +
            ", isActive=" + isActive +
            ", translationsCount=" + (translations != null ? translations.size() : 0) +
            '}';
    }
}
