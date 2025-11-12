package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptchaIntegrationService {
    public Map<String, Object> generateArithmeticCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        return Map.of("success", true, "captchaId", captchaId, "image", "base64...");
    }
    public Map<String, Object> generateNumericCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        return Map.of("success", true, "captchaId", captchaId, "image", "base64...");
    }
}
