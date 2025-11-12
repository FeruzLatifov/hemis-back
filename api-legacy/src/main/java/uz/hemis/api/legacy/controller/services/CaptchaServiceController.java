package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.CaptchaIntegrationService;

import java.util.Map;

/**
 * Captcha Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/captcha/*}</p>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Generates captcha images for security</li>
 *   <li>Supports arithmetic (2+3=?) and numeric captchas</li>
 *   <li>Used in passport validation and sensitive operations</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "Captcha", description = "Captcha yaratish va tekshirish xizmatlari")
@RestController
@RequestMapping("/services/captcha")
@RequiredArgsConstructor
@Slf4j
public class CaptchaServiceController {

    private final CaptchaIntegrationService captchaIntegrationService;

    /**
     * Get arithmetic captcha (e.g., 2 + 3 = ?)
     *
     * <p><strong>Endpoint:</strong> GET /services/captcha/getArithmeticCaptcha</p>
     *
     * @return Captcha image (Base64) and session ID
     */
    @Operation(
        summary = "Matematik captcha olish",
        description = "Matematik amallar bilan captcha yaratish (masalan: 2 + 3 = ?)"
    )
    @GetMapping(value = "/getArithmeticCaptcha", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getArithmeticCaptcha() {
        log.info("GET /services/captcha/getArithmeticCaptcha");
        return ResponseEntity.ok(captchaIntegrationService.generateArithmeticCaptcha());
    }

    /**
     * Get numeric captcha (random digits)
     *
     * <p><strong>Endpoint:</strong> GET /services/captcha/getNumericCaptcha</p>
     *
     * @return Captcha image (Base64) and session ID
     */
    @Operation(
        summary = "Raqamli captcha olish",
        description = "Tasodifiy raqamlar bilan captcha yaratish"
    )
    @GetMapping(value = "/getNumericCaptcha", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getNumericCaptcha() {
        log.info("GET /services/captcha/getNumericCaptcha");
        return ResponseEntity.ok(captchaIntegrationService.generateNumericCaptcha());
    }
}
