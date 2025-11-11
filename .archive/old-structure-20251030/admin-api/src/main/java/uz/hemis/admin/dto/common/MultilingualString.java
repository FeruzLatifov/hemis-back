package uz.hemis.admin.dto.common;

import lombok.*;

/**
 * Multilingual string for internationalization (i18n)
 *
 * Supports 3 languages: Uzbek, Russian, English
 * Used for entity names, labels, and other translatable content
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultilingualString {

    /**
     * Uzbek translation
     */
    private String uz;

    /**
     * Russian translation
     */
    private String ru;

    /**
     * English translation
     */
    private String en;

    /**
     * Create from single Uzbek value
     */
    public static MultilingualString of(String uz) {
        return MultilingualString.builder()
                .uz(uz)
                .ru(uz)
                .en(uz)
                .build();
    }

    /**
     * Create with all translations
     */
    public static MultilingualString of(String uz, String ru, String en) {
        return MultilingualString.builder()
                .uz(uz)
                .ru(ru)
                .en(en)
                .build();
    }

    /**
     * Get translation by locale
     */
    public String get(String locale) {
        return switch (locale) {
            case "ru" -> ru != null ? ru : uz;
            case "en" -> en != null ? en : uz;
            default -> uz;
        };
    }

    /**
     * Check if has any translation
     */
    public boolean hasValue() {
        return uz != null || ru != null || en != null;
    }
}
