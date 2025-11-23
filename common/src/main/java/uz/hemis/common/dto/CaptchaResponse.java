package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Captcha Response DTO
 * <p>
 * Old-hemis /app/rest/v2/services/captcha/getNumericCaptcha endpointining javobi.
 * </p>
 * <p><strong>Port:</strong> 100% old-hemis bilan mos - FAQAT {id, image} fieldlari.</p>
 * <p><strong>Old-hemis response:</strong> <code>{"id": "uuid", "image": "data:image/png;base64,..."}</code></p>
 *
 * @author HEMIS Backend Team
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {

    /**
     * UUID - Captcha uchun unique identifier
     * <p>Old-hemis: Bu ID captcha image uchun identifier.</p>
     */
    @JsonProperty("id")
    private String id;

    /**
     * Base64-encoded PNG image (data URI scheme)
     * <p>Old-hemis: <code>data:image/png;base64,iVBORw0KGgo...</code></p>
     * <p>Format: 200×60px PNG with 5-digit numeric captcha</p>
     */
    @JsonProperty("image")
    private String image;
}
