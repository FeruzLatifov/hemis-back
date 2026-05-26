package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.webhook.WebhookApplyResultDto;
import uz.hemis.common.dto.webhook.WebhookDeliveryLogDto;
import uz.hemis.common.dto.webhook.WebhookSecretResponse;
import uz.hemis.common.dto.webhook.WebhookTargetCreateRequest;
import uz.hemis.common.dto.webhook.WebhookTargetDto;
import uz.hemis.common.dto.webhook.WebhookTargetUpdateRequest;
import uz.hemis.service.webhook.WebhookTargetService;

import java.util.List;
import java.util.UUID;

/**
 * Webhook target admin REST API.
 *
 * <p>224 OTM Univer webhook URL'larini admin UI orqali boshqarish — markaz event
 * sodir bo'lganda qaysi Univer'larga REST callback yuborilishini bu yerda sozlash.</p>
 *
 * @since ADR-0012
 */
@RestController
@RequestMapping("/api/v1/web/admin/webhooks")
@Tag(name = "Webhook Management", description = "224 OTM webhook URL + secret + delivery log boshqaruvi (admin only)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class WebhookTargetController {

    private final WebhookTargetService service;

    // =====================================================
    // List + Get
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAuthority('webhook.view')")
    @Operation(summary = "Barcha webhook target'lar ro'yxati")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Target ro'yxati"),
            @ApiResponse(responseCode = "401", description = "Token yo'q"),
            @ApiResponse(responseCode = "403", description = "Permission yetarli emas")
    })
    public ResponseEntity<ResponseWrapper<List<WebhookTargetDto>>> listAll() {
        return ResponseEntity.ok(ResponseWrapper.success(service.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('webhook.view')")
    @Operation(summary = "Webhook target ID bo'yicha")
    public ResponseEntity<ResponseWrapper<WebhookTargetDto>> findById(
            @Parameter(description = "Webhook target UUID") @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.findById(id)));
    }

    @GetMapping("/by-university/{universityCode}")
    @PreAuthorize("hasAuthority('webhook.view')")
    @Operation(summary = "Webhook target OTM kod bo'yicha")
    public ResponseEntity<ResponseWrapper<WebhookTargetDto>> findByUniversity(
            @Parameter(example = "337") @PathVariable String universityCode
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.findByUniversityCode(universityCode)));
    }

    // =====================================================
    // Create + secret regeneration
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('webhook.create')")
    @Operation(
            summary = "Yangi webhook target qo'shish + secret generate",
            description = "Plain secret faqat shu javobda bir marta qaytariladi. OTM IT uni .env'ga yozishi shart."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Yaratildi"),
            @ApiResponse(responseCode = "400", description = "Validation xato"),
            @ApiResponse(responseCode = "409", description = "OTM uchun target allaqachon mavjud")
    })
    public ResponseEntity<ResponseWrapper<WebhookSecretResponse>> create(
            @Valid @RequestBody WebhookTargetCreateRequest request
    ) {
        WebhookSecretResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(response, "Webhook target created — save the plain secret!"));
    }

    @PostMapping("/{id}/regenerate-secret")
    @PreAuthorize("hasAuthority('webhook.manage')")
    @Operation(
            summary = "HMAC secret rotation",
            description = "Yangi plain secret yaratish. Eski secret bekor qilinadi. OTM IT yangi qiymatni .env'ga yozishi shart."
    )
    public ResponseEntity<ResponseWrapper<WebhookSecretResponse>> regenerateSecret(@PathVariable UUID id) {
        log.warn("Webhook secret rotation requested: target={}", id);
        return ResponseEntity.ok(ResponseWrapper.success(
                service.regenerateSecret(id),
                "Secret regenerated — update Univer .env immediately"
        ));
    }

    // =====================================================
    // Update + Delete
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('webhook.update')")
    @Operation(summary = "Webhook target yangilash (partial)")
    public ResponseEntity<ResponseWrapper<WebhookTargetDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody WebhookTargetUpdateRequest request
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('webhook.delete')")
    @Operation(summary = "Webhook target soft delete (deleted_at)")
    public ResponseEntity<ResponseWrapper<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseWrapper.success((Void) null, "Webhook target deleted"));
    }

    // =====================================================
    // Delivery log views
    // =====================================================

    @GetMapping("/{id}/deliveries")
    @PreAuthorize("hasAuthority('webhook.view')")
    @Operation(summary = "Target uchun delivery tarix")
    public ResponseEntity<ResponseWrapper<Page<WebhookDeliveryLogDto>>> deliveries(
            @PathVariable UUID id,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.findDeliveriesByTarget(id, pageable)));
    }

    @GetMapping("/events/{eventId}/deliveries")
    @PreAuthorize("hasAuthority('webhook.view')")
    @Operation(summary = "Event uchun barcha attempt'lar (224 OTM × N attempt)")
    public ResponseEntity<ResponseWrapper<List<WebhookDeliveryLogDto>>> deliveriesByEvent(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.findDeliveriesByEvent(eventId)));
    }

    @GetMapping("/dlq")
    @PreAuthorize("hasAuthority('webhook.view')")
    @Operation(summary = "Dead Letter Queue — qabul qilinmagan event'lar (manual review)")
    public ResponseEntity<ResponseWrapper<Page<WebhookDeliveryLogDto>>> dlq(
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.findDlqEntries(pageable)));
    }

    // =====================================================
    // Apply result views (K2) — "qaysi OTM da apply fail bo'ldi" (delivered != applied)
    // =====================================================

    @GetMapping("/apply-results")
    @PreAuthorize("hasAuthority('webhook.view')")
    @Operation(summary = "Univer apply natijalari (ack) — status filter ixtiyoriy ('applied'/'failed')")
    public ResponseEntity<ResponseWrapper<Page<WebhookApplyResultDto>>> applyResults(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.listApplyResults(status, pageable)));
    }

    @GetMapping("/events/{eventId}/apply-results")
    @PreAuthorize("hasAuthority('webhook.view')")
    @Operation(summary = "Event uchun OTM apply natijalari (delivered != applied drill-down)")
    public ResponseEntity<ResponseWrapper<List<WebhookApplyResultDto>>> applyResultsByEvent(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.applyResultsByEvent(eventId)));
    }

    // =====================================================
    // Sandbox — manual test
    // =====================================================

    @PostMapping("/{id}/test")
    @PreAuthorize("hasAuthority('webhook.manage')")
    @Operation(
            summary = "Sandbox: target'ga test event yuborish",
            description = "Admin Univer endpoint sog'lig'ini tekshirish uchun synthetic webhook yuboradi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test yuborildi (log entry qaytadi)"),
            @ApiResponse(responseCode = "404", description = "Target topilmadi")
    })
    public ResponseEntity<ResponseWrapper<WebhookDeliveryLogDto>> sendTestEvent(@PathVariable UUID id) {
        WebhookDeliveryLogDto log = service.sendTestEvent(id);
        return ResponseEntity.ok(ResponseWrapper.success(log, "Test event dispatched"));
    }
}
