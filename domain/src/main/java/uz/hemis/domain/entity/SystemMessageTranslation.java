package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * System Message Translation Entity - UNIVER Pattern Implementation
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Store translations for system messages in different languages</li>
 *   <li>Composite PK: (message_id, language) - one translation per language per message</li>
 *   <li>ManyToOne relationship with SystemMessage</li>
 * </ul>
 *
 * <p><strong>UNIVER Pattern:</strong></p>
 * <ul>
 *   <li>Similar to e_system_message_translation in UNIVER</li>
 *   <li>Composite PK ensures data integrity</li>
 *   <li>CASCADE delete when parent message deleted</li>
 *   <li>No soft delete (translations don't need it)</li>
 * </ul>
 *
 * <p><strong>Supported Languages (9 total):</strong></p>
 * <ul>
 *   <li>uz-UZ - O'zbek (lotin)</li>
 *   <li>oz-UZ - Ўзбек (kirill)</li>
 *   <li>ru-RU - Русский</li>
 *   <li>en-US - English</li>
 *   <li>kk-UZ - Қазақ</li>
 *   <li>tg-TG - Тоҷикӣ</li>
 *   <li>kz-KZ - Қазақ</li>
 *   <li>tm-TM - Türkmen</li>
 *   <li>kg-KG - Кыргыз</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * SystemMessageTranslation translation = new SystemMessageTranslation();
 * translation.setMessageId(message.getId());
 * translation.setLanguage("ru-RU");
 * translation.setTranslation("Сохранить");
 * translation.setSystemMessage(message);
 * </pre>
 *
 * @see SystemMessage
 * @see SystemMessageTranslationId
 * @since 2.0.0
 */
@Entity
@Table(
    name = "message_translations",
    indexes = {
        @Index(name = "idx_message_translations_language", columnList = "language"),
        @Index(name = "idx_message_translations_message_id", columnList = "message_id")
    }
)
@IdClass(SystemMessageTranslationId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMessageTranslation implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Composite Primary Key
    // =====================================================

    /**
     * Reference to SystemMessage ID (part of composite PK)
     * <p>Must match field name in SystemMessageTranslationId</p>
     */
    @Id
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    /**
     * Language code (part of composite PK)
     * <p>Format: xx-XX (e.g., uz-UZ, ru-RU, en-US)</p>
     * <p>Must match field name in SystemMessageTranslationId</p>
     */
    @Id
    @Column(name = "language", nullable = false, length = 16)
    private String language;

    // =====================================================
    // Translation Content
    // =====================================================

    /**
     * Translated message text for this language
     * <p>Cannot be null - every translation must have content</p>
     *
     * <p><strong>Example:</strong></p>
     * <ul>
     *   <li>Original (uz): "Saqlash"</li>
     *   <li>ru-RU: "Сохранить"</li>
     *   <li>en-US: "Save"</li>
     * </ul>
     */
    @Column(name = "translation", nullable = false, columnDefinition = "TEXT")
    private String translation;

    // =====================================================
    // Relationship
    // =====================================================

    /**
     * Reference to parent SystemMessage
     * <p>ManyToOne relationship with foreign key to message_id</p>
     *
     * <p><strong>Relationship details:</strong></p>
     * <ul>
     *   <li>Many translations → One system message</li>
     *   <li>@MapsId("messageId") - maps messageId field to systemMessage.id</li>
     *   <li>Fetch: LAZY - parent loaded only when accessed</li>
     *   <li>Optional: false - translation must have parent message</li>
     * </ul>
     *
     * <p><strong>Important:</strong></p>
     * Do NOT use @JoinColumn(insertable=false, updatable=false)
     * because @MapsId handles the FK mapping correctly.
     *
     * <p><strong>JSON Serialization:</strong></p>
     * NOTE: This entity is not directly serialized to JSON.
     * Use TranslationDto in the web layer instead to avoid circular references.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", referencedColumnName = "id", nullable = false)
    @MapsId("messageId")
    private SystemMessage systemMessage;

    // =====================================================
    // Audit Fields (Not from ModernBaseEntity)
    // =====================================================
    // Note: Translations don't extend ModernBaseEntity because:
    // 1. They use composite PK (not single UUID)
    // 2. They don't need soft delete
    // 3. They don't need separate ID field

    /**
     * Creation timestamp
     * <p>Set automatically on insert</p>
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     * <p>Set automatically on update</p>
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =====================================================
    // JPA Lifecycle Hooks
    // =====================================================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if this translation is for specified language
     *
     * @param lang Language code to check
     * @return true if language matches
     */
    public boolean isLanguage(String lang) {
        return this.language != null && this.language.equals(lang);
    }

    /**
     * Check if language starts with specified prefix
     * <p>Useful for fallback: "ru-RU" starts with "ru"</p>
     *
     * @param prefix Language prefix (e.g., "ru")
     * @return true if language starts with prefix
     */
    public boolean languageStartsWith(String prefix) {
        return this.language != null && this.language.startsWith(prefix);
    }

    // =====================================================
    // Object Methods
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemMessageTranslation)) return false;
        SystemMessageTranslation that = (SystemMessageTranslation) o;
        return messageId != null && messageId.equals(that.messageId) &&
               language != null && language.equals(that.language);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SystemMessageTranslation{" +
            "messageId=" + messageId +
            ", language='" + language + '\'' +
            ", translation='" + (translation != null && translation.length() > 50 ?
                translation.substring(0, 50) + "..." : translation) + '\'' +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
