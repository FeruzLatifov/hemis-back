package uz.hemis.api.legacy.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.*;

/**
 * Send Service Controller - CUBA REST API Compatible
 *
 * <p>OLD-HEMIS: SendServiceBean.sendEmailNative(id, email, verify_code)</p>
 * <p>Sends password reset verification code via SMTP (mail.hemis.uz)</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/send")
@Tag(name = "98.Xabarlar", description = "Email va SMS yuborish xizmatlari")
@Slf4j
public class SendServiceController {

    @Value("${hemis.mail.smtp.host:mail.hemis.uz}")
    private String smtpHost;

    @Value("${hemis.mail.smtp.port:587}")
    private String smtpPort;

    @Value("${hemis.mail.smtp.username:no-reply@hemis.uz}")
    private String smtpUsername;

    @Value("${hemis.mail.smtp.password:}")
    private String smtpPassword;

    /**
     * Send email with verification code (password reset)
     *
     * <p>OLD-HEMIS: SendServiceBean.sendEmailNative(id, email, verify_code)</p>
     * <p>SMTP: mail.hemis.uz:587 (STARTTLS)</p>
     * <p>Subject: "HEMIS tizimi parolini tiklash"</p>
     * <p>Always returns {"result": "OK"} (old-hemis compatible)</p>
     */
    @GetMapping("/sendEmailNative")
    @Operation(summary = "Parol tiklash emaili yuborish", description = "Foydalanuvchiga parol tiklash uchun tasdiqlash kodi yuboradi")
    public ResponseEntity<?> sendEmailNative(
            @Parameter(description = "Foydalanuvchi ID") @RequestParam String id,
            @Parameter(description = "Email manzil") @RequestParam String email,
            @Parameter(description = "Tasdiqlash kodi") @RequestParam String verify_code) {

        log.info("[CUBA Service] send/sendEmailNative: id={}, email={}", id, email);

        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", true);
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", smtpHost);
            prop.put("mail.smtp.port", smtpPort);
            prop.put("mail.smtp.ssl.trust", smtpHost);

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("HEMIS tizimi parolini tiklash");

            String htmlContent = String.format(
                    "<html><body>" +
                    "<p>HEMIS tizimida <b>%s</b> ID li foydalanuvchining parolini yangilash uchun tasdiqlash kodi:</p>" +
                    "<p><h2>%s</h2></p>" +
                    "</body></html>",
                    id, verify_code);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);
            Transport.send(message);

            log.info("Verification email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Error sending email to: {}", email, e);
        }

        // Old-hemis always returns {"result": "OK"} even on failure
        return ResponseEntity.ok(Collections.singletonMap("result", "OK"));
    }
}
