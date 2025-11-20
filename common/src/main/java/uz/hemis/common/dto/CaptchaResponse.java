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
 * <p><strong>Port:</strong> 100% old-hemis bilan mos - barcha fieldlar bir xil.</p>
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
     */
    @JsonProperty("id")
    private String id;

    /**
     * Base64-encoded PNG image (data URI scheme)
     */
    @JsonProperty("image")
    private String image;

    /**
     * Captcha ID - Redis da saqlash uchun key
     */
    @JsonProperty("captchaId")
    private String captchaId;

    /**
     * Captcha type - "numeric" yoki boshqa turlar
     */
    @JsonProperty("captchaType")
    private String captchaType;

    /**
     * Captcha qiymati - tekshirish uchun (faqat development/test rejimida qaytariladi)
     * Production da bu field null bo'lishi kerak!
     */
    @JsonProperty("captchaValue")
    private String captchaValue;

    /**
     * Captcha amal qilish muddati (soniyalarda)
     */
    @JsonProperty("expiresIn")
    private Integer expiresIn;
}
