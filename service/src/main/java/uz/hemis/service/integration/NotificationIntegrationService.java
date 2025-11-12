package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationIntegrationService {
    public Map<String, Object> sendEmail(EmailRequest request) {
        log.info("Sending email to: {}", request.getTo());
        return Map.of("success", true, "messageId", UUID.randomUUID().toString());
    }
    public Map<String, Object> sendVerificationCode(VerifyCodeRequest request) {
        log.info("Sending verification code to: {}", request.getPhone());
        return Map.of("success", true, "codeId", UUID.randomUUID().toString());
    }
}
