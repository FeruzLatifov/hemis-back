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

@Tag(name = "Captcha")
@RestController
@RequestMapping("/app/rest/v2/captcha")
@RequiredArgsConstructor
@Slf4j
public class CaptchaController {

    @GetMapping("/generate")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> generateCaptcha() {
        log.debug("Generating captcha");

        Map<String, String> captcha = new HashMap<>();
        captcha.put("captchaId", UUID.randomUUID().toString());
        captcha.put("imageUrl", "/api/captcha/image/" + captcha.get("captchaId"));

        return ResponseEntity.ok(ResponseWrapper.success(captcha));
    }

    @PostMapping("/verify")
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
}
