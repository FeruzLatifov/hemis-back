package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.webhook.WebhookAckService;

/**
 * Univer → markaz apply-status ack endpoint (K2).
 *
 * <p>Univer webhook event'ni apply qilgach (yoki xato bo'lgach) natijani HMAC-imzolangan POST
 * bilan shu yerga yuboradi. Auth — HMAC ({@code X-Hemis-Signature}), JWT emas →
 * {@code SecurityConfig} permitAll, verify {@link WebhookAckService} ichida (secret_enc bilan).
 * "Delivered != applied" observability gap'ini yopadi (ADR-0012 K2).</p>
 */
@RestController
@RequestMapping("/api/v1/university")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhook javobi (apply-status)", description = "Univer apply-status callback (HMAC-signed)")
public class WebhookAckController {

    private final WebhookAckService ackService;

    @PostMapping("/hemis-events/ack")
    @Operation(summary = "Apply-status ack (univer → markaz, HMAC-signed)")
    public ResponseEntity<ResponseWrapper<Void>> ack(
            @RequestHeader(value = "X-Hemis-Signature", required = false) String signature,
            @RequestHeader(value = "X-Hemis-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Hemis-University-Code", required = false) String universityCode,
            @RequestBody String rawBody) {
        ackService.processAck(universityCode, signature, timestamp, rawBody);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Ack recorded"));
    }
}
