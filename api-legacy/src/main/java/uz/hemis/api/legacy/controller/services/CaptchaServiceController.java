package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.common.dto.CaptchaResponse;
import uz.hemis.service.CaptchaService;

/**
 * Captcha Service Controller
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong> Port qilingan endpointlar.</p>
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/captcha/*}</p>
 *
 * <p><strong>Security:</strong> PUBLIC - authentication talab qilinmaydi</p>
 * <ul>
 *   <li>Captcha olish uchun login qilish shart emas</li>
 *   <li>Login sahifasida ishlatiladi</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /app/rest/v2/services/captcha/getNumericCaptcha - Numeric captcha generatsiya qilish</li>
 * </ul>
 *
 * @author HEMIS Backend Team
 * @since 2025-11-19
 */
@RestController
@RequestMapping("/app/rest/v2/services")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "03.Captcha", description = "🔢 Captcha generatsiya va validatsiya - login sahifasi uchun xavfsizlik")
public class CaptchaServiceController {

    private final CaptchaService captchaService;

    /**
     * Generate numeric captcha
     * <p>
     * <strong>Old-hemis endpoint:</strong> GET /app/rest/v2/services/captcha/getNumericCaptcha
     * </p>
     * <p><strong>PUBLIC:</strong> Authentication talab qilinmaydi (login sahifasida ishlatiladi)</p>
     *
     * @return CaptchaResponse with base64 PNG image and metadata
     */
    @Operation(
            summary = "Numeric captcha generatsiya qilish",
            description = """
                    **Old-hemis endpoint:** `GET /app/rest/v2/services/captcha/getNumericCaptcha`

                    **Vazifa:**
                    - 5 xonali numeric captcha yaratadi
                    - PNG rasmni base64 formatida qaytaradi
                    - Redis da 300 soniya (5 daqiqa) saqlaydi
                    - Login sahifasida xavfsizlik uchun ishlatiladi

                    **PUBLIC endpoint:** Authentication talab qilinmaydi.

                    **Javob:**
                    - `id`: Captcha uchun unique identifier (UUID)
                    - `image`: Base64-encoded PNG image (data URI)
                    - `captchaId`: Redis key (validatsiya uchun kerak)
                    - `captchaType`: "numeric"
                    - `captchaValue`: FAQAT development rejimida qaytariladi (production da null)
                    - `expiresIn`: Amal qilish muddati (300 soniya)

                    **Qanday ishlaydi:**
                    1. Random 5 xonali raqam generatsiya qilinadi
                    2. PNG rasm yaratiladi (200x60 px)
                    3. Redis da saqlanadi: `captcha:{captchaId}` → `{value}` (TTL: 300s)
                    4. Base64-encoded image qaytariladi

                    **Foydalanish:**
                    ```bash
                    # Captcha olish (token kerak emas)
                    curl http://localhost:8081/app/rest/v2/services/captcha/getNumericCaptcha

                    # Javobda captchaId va image keladi
                    # Image ni HTML img tag da ko'rsatish:
                    <img src="data:image/png;base64,iVBORw0KG..." />

                    # Login da captchaId va user-entered value yuboriladi
                    ```

                    **OLD-HEMIS COMPATIBILITY:** ✅ 100% mos - barcha fieldlar va format bir xil.
                    """,
            tags = {"03.Captcha"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Captcha muvaffaqiyatli yaratildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CaptchaResponse.class),
                            examples = @ExampleObject(
                                    name = "Captcha Response",
                                    summary = "Numeric captcha javob namunasi",
                                    description = "Old-hemis bilan 100% mos javob",
                                    value = """
                                            {
                                              "id": "2fd3fd0f-9f39-4d6f-e239-5a646ce2a495",
                                              "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAAA8CAYAAAA...",
                                              "captchaId": "9a3370b9-fca3-423f-8311-85ba90b5f4cb",
                                              "captchaType": "numeric",
                                              "captchaValue": null,
                                              "expiresIn": 300
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "❌ Server xatosi - captcha generatsiya qilishda xatolik"
            )
    })
    @GetMapping("/captcha/getNumericCaptcha")
    public ResponseEntity<CaptchaResponse> getNumericCaptcha() {
        log.info("🔢 GET /app/rest/v2/services/captcha/getNumericCaptcha - Generating numeric captcha");

        CaptchaResponse response = captchaService.generateNumericCaptcha();

        log.info("✅ Captcha generated: captchaId={}, expiresIn={}s",
                response.getCaptchaId(), response.getExpiresIn());

        return ResponseEntity.ok(response);
    }
}
