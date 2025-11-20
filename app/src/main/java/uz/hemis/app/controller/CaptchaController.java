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

@Tag(name = "Public APIs - Captcha", description = "CAPTCHA generation and verification for public forms")
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
    // Legacy CUBA Service Endpoints - MOVED TO api-legacy module
    // =====================================================
    // DEPRECATED: These endpoints have been moved to CaptchaServiceController
    // in the api-legacy module with proper implementation (Redis, PNG generation)
    //
    // Old location: CaptchaController (app module) - basic placeholder
    // New location: CaptchaServiceController (api-legacy module) - full implementation
    //
    // Deleted:
    // - GET /services/captcha/getNumericCaptcha -> CaptchaServiceController
    // - GET /services/captcha/getArithmeticCaptcha -> (not ported yet)
}
