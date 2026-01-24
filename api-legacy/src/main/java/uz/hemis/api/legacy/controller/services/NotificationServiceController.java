package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Notification Service Controller
 *
 * <p><strong>Note:</strong> Email and verification code endpoints have been moved to
 * {@link MailServiceController} for OLD-HEMIS compatibility.</p>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Additional notification services (future expansion)</li>
 * </ul>
 *
 * @since 2.0.0
 * @see MailServiceController
 */
@Tag(name = "70.Qo'shimcha xizmatlar", description = "Qo'shimcha xabar yuborish xizmatlari")
@RestController
@RequestMapping("/services/notification")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class NotificationServiceController {

    // Email and verification code endpoints have been moved to MailServiceController
    // for OLD-HEMIS compatibility with proper response format.
    // This controller is reserved for future notification services.
}
