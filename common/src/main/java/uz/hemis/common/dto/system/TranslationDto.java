package uz.hemis.common.dto.system;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Translation DTO - without circular references
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "id", "createdAt", "category", "messageKey", "message",
    "isActive", "translations"
})
public class TranslationDto {

    private UUID id;
    private LocalDateTime createdAt;
    private String category;
    private String messageKey;
    private String message;  // Default uz-UZ
    private Boolean isActive;

    // Translations as simple map: language -> translation
    private Map<String, String> translations;
}
