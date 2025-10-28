package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Translation Entity
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Multi-language translations for UI labels</li>
 *   <li>System messages and notifications</li>
 *   <li>Dynamic content localization</li>
 * </ul>
 *
 * <p><strong>Examples:</strong></p>
 * <ul>
 *   <li>login.title → "Tizimga kirish" (uz), "Вход в систему" (ru), "Login" (en)</li>
 *   <li>student.status.active → "Faol" (uz), "Активный" (ru), "Active" (en)</li>
 *   <li>error.not_found → "Topilmadi" (uz), "Не найдено" (ru), "Not found" (en)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "h_translation", indexes = {
    @Index(name = "idx_translation_key", columnList = "translation_key"),
    @Index(name = "idx_translation_module", columnList = "module")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Translation extends BaseEntity {

    /**
     * Translation key (unique identifier)
     * Examples: "login.title", "student.status.active", "error.not_found"
     */
    @Column(name = "translation_key", nullable = false, unique = true, length = 255)
    private String translationKey;

    /**
     * Module/category
     * Examples: "auth", "student", "teacher", "common"
     */
    @Column(name = "module", length = 50)
    private String module;

    /**
     * Translation in Uzbek
     */
    @Column(name = "text_uz", nullable = false, length = 1000)
    private String textUz;

    /**
     * Translation in Russian
     */
    @Column(name = "text_ru", length = 1000)
    private String textRu;

    /**
     * Translation in English
     */
    @Column(name = "text_en", length = 1000)
    private String textEn;

    /**
     * Description/notes
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Active flag
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
