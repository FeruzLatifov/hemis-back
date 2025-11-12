package uz.hemis.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Language DTO - System language configuration
 * 
 * <p>Used for language management API responses</p>
 * 
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDto {
    
    private String code;           // uz-UZ, ru-RU, en-US, etc.
    private String name;           // English name: "Uzbek (Latin)"
    private String nativeName;     // Native name: "O'zbekcha"
    private String isoCode;        // ISO 639-1: uz, ru, en
    private Integer position;      // Display order
    private Boolean isActive;      // Enabled in system
    private Boolean isDefault;     // Default for new users
    private Boolean isRtl;         // Right-to-left script
    private Boolean canDisable;    // Can be disabled by admin
}
