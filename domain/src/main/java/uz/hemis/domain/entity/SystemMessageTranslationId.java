package uz.hemis.domain.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite Primary Key for SystemMessageTranslation
 *
 * <p><strong>UNIVER Pattern:</strong></p>
 * <ul>
 *   <li>Composite PK: (message_id, language)</li>
 *   <li>Similar to e_system_message_translation in UNIVER</li>
 *   <li>Ensures one translation per language per message</li>
 * </ul>
 *
 * <p><strong>JPA Requirements:</strong></p>
 * <ul>
 *   <li>Must implement Serializable</li>
 *   <li>Must have equals() and hashCode()</li>
 *   <li>Must have no-arg constructor</li>
 *   <li>Fields must match @Id fields in entity</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * SystemMessageTranslationId id = new SystemMessageTranslationId(messageId, "ru-RU");
 * SystemMessageTranslation translation = repository.findById(id).orElse(null);
 * </pre>
 *
 * @see SystemMessageTranslation
 * @since 2.0.0
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class SystemMessageTranslationId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Reference to SystemMessage ID
     * <p>Must match field name in SystemMessageTranslation entity</p>
     */
    private UUID messageId;

    /**
     * Language code (e.g., uz-UZ, ru-RU, en-US)
     * <p>Must match field name in SystemMessageTranslation entity</p>
     */
    private String language;

    @Override
    public String toString() {
        return "SystemMessageTranslationId{" +
            "messageId=" + messageId +
            ", language='" + language + '\'' +
            '}';
    }
}
