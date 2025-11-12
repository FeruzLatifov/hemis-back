package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Mail")
@RestController
@RequestMapping("/app/rest/v2/mail")
@RequiredArgsConstructor
@Slf4j
public class MailController {

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> sendEmail(
            @Valid @RequestBody Map<String, String> request
    ) {
        String to = request.get("to");
        String subject = request.get("subject");
        String body = request.get("body");

        log.info("Sending email to: {} with subject: {}", to, subject);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "sent");
        response.put("to", to);
        response.put("subject", subject);
        response.put("sentAt", LocalDateTime.now());
        response.put("messageId", "msg-" + System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(response));
    }

    @GetMapping("/status/{messageId}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getEmailStatus(
            @PathVariable String messageId
    ) {
        log.debug("Getting email status for messageId: {}", messageId);

        Map<String, Object> status = new HashMap<>();
        status.put("messageId", messageId);
        status.put("status", "delivered");
        status.put("deliveredAt", LocalDateTime.now());

        return ResponseEntity.ok(ResponseWrapper.success(status));
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getEmailTemplates() {
        Map<String, String> templates = new HashMap<>();
        templates.put("welcome", "Xush kelibsiz email shabloni");
        templates.put("verification", "Tasdiqlash email shabloni");
        templates.put("notification", "Xabarnoma email shabloni");

        return ResponseEntity.ok(ResponseWrapper.success(templates));
    }
}
