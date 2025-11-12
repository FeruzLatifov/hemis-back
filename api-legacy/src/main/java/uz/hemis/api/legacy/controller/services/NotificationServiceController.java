package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.EmailRequest;
import uz.hemis.common.dto.VerifyCodeRequest;
import uz.hemis.service.integration.NotificationIntegrationService;

import java.util.Map;

/**
 * Notification Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/mail/*} and {@code /services/send/*}</p>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Send email notifications</li>
 *   <li>Send SMS verification codes</li>
 *   <li>User communication</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "Xabarlar", description = "Email va SMS xabar yuborish xizmatlari")
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class NotificationServiceController {

    private final NotificationIntegrationService notificationIntegrationService;

    /**
     * Send email
     *
     * <p><strong>Endpoint:</strong> POST /services/mail/send</p>
     *
     * @param request Email request (to, subject, body)
     * @return Success status
     */
    @Operation(
        summary = "Email yuborish",
        description = "Foydalanuvchiga email xabar yuborish"
    )
    @PostMapping("/mail/send")
    public ResponseEntity<Map<String, Object>> sendEmail(
        @RequestBody(description = "Email ma'lumotlari", required = true)
        @org.springframework.web.bind.annotation.RequestBody EmailRequest request
    ) {
        log.info("POST /services/mail/send - to: {}", request.getTo());
        return ResponseEntity.ok(notificationIntegrationService.sendEmail(request));
    }

    /**
     * Send SMS verification code
     *
     * <p><strong>Endpoint:</strong> POST /services/send/verifyCode</p>
     *
     * @param request Verify code request (phone number, code type)
     * @return Success status and code ID
     */
    @Operation(
        summary = "SMS tasdiqlash kodi yuborish",
        description = "Telefon raqamiga SMS orqali tasdiqlash kodi yuborish"
    )
    @PostMapping("/send/verifyCode")
    public ResponseEntity<Map<String, Object>> sendVerifyCode(
        @RequestBody(description = "SMS ma'lumotlari", required = true)
        @org.springframework.web.bind.annotation.RequestBody VerifyCodeRequest request
    ) {
        log.info("POST /services/send/verifyCode - phone: {}", request.getPhone());
        return ResponseEntity.ok(notificationIntegrationService.sendVerificationCode(request));
    }
}
