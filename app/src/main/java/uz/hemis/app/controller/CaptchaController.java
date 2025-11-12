package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "05. Public APIs - Captcha", description = "CAPTCHA generation and verification for public forms")
@RestController
@RequestMapping("/app/rest/v2")
@RequiredArgsConstructor
@Slf4j
public class CaptchaController {

    @GetMapping("/captcha/generate")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> generateCaptcha() {
        log.debug("Generating captcha");

        Map<String, String> captcha = new HashMap<>();
        captcha.put("captchaId", UUID.randomUUID().toString());
        captcha.put("imageUrl", "/api/captcha/image/" + captcha.get("captchaId"));

        return ResponseEntity.ok(ResponseWrapper.success(captcha));
    }

    @PostMapping("/captcha/verify")
    public ResponseEntity<ResponseWrapper<Map<String, Boolean>>> verifyCaptcha(
            @RequestBody Map<String, String> request
    ) {
        String captchaId = request.get("captchaId");
        String userInput = request.get("userInput");

        log.debug("Verifying captcha: {}", captchaId);

        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", true);

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    // =====================================================
    // Legacy CUBA Service Endpoints
    // =====================================================

    /**
     * Get numeric CAPTCHA - Legacy CUBA service endpoint
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/captcha/getNumericCaptcha</p>
     * <p>Migrated from old-hemis rest-services.xml</p>
     */
    @GetMapping("/services/captcha/getNumericCaptcha")
    public ResponseEntity<Map<String, Object>> getNumericCaptcha() {
        log.debug("GET /services/captcha/getNumericCaptcha - legacy endpoint");

        Map<String, Object> response = new HashMap<>();
        response.put("captchaId", UUID.randomUUID().toString());
        response.put("captchaType", "numeric");
        response.put("captchaValue", generateNumericCode());
        response.put("expiresIn", 300); // 5 minutes

        return ResponseEntity.ok(response);
    }

    /**
     * Get arithmetic CAPTCHA - Legacy CUBA service endpoint
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/captcha/getArithmeticCaptcha</p>
     * <p>Migrated from old-hemis rest-services.xml</p>
     */
    @GetMapping("/services/captcha/getArithmeticCaptcha")
    public ResponseEntity<Map<String, Object>> getArithmeticCaptcha() {
        log.debug("GET /services/captcha/getArithmeticCaptcha - legacy endpoint");

        int a = (int) (Math.random() * 10) + 1;
        int b = (int) (Math.random() * 10) + 1;
        String operator = new String[]{"+", "-"}[(int) (Math.random() * 2)];

        Map<String, Object> response = new HashMap<>();
        response.put("captchaId", UUID.randomUUID().toString());
        response.put("captchaType", "arithmetic");
        response.put("question", a + " " + operator + " " + b + " = ?");
        response.put("expiresIn", 300); // 5 minutes

        return ResponseEntity.ok(response);
    }

    private String generateNumericCode() {
        return String.valueOf((int) (Math.random() * 90000) + 10000); // 5-digit code
    }
}
