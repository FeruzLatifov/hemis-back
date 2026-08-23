package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.outbox.OutboxEventDto;
import uz.hemis.common.dto.outbox.OutboxStatsDto;
import uz.hemis.service.outbox.OutboxAdminService;

import java.util.UUID;

/**
 * Outbox observability admin REST API.
 *
 * <p>Outbox queue inspect, retry, discard. Yagona joydan admin pending/DLQ event'larni
 * ko'radi va manual aralashish qiladi (poll qotib qolgan bo'lsa, Kafka topic invalid
 * bo'lsa, payload xato bo'lsa).</p>
 *
 * @since 2026-05-19
 */
@RestController
@RequestMapping("/api/v1/web/admin/outbox")
@Tag(name = "Outbox Admin", description = "Outbox queue observability + manual retry/discard")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OutboxAdminController {

    private final OutboxAdminService service;

    @GetMapping
    @PreAuthorize("hasAuthority('outbox.view')")
    @Operation(summary = "Outbox event'lar ro'yxati (status filter + pagination)")
    public ResponseEntity<ResponseWrapper<Page<OutboxEventDto>>> list(
            @Parameter(description = "PENDING | RETRYING | DLQ | PUBLISHED | all")
            @RequestParam(required = false) String status,
            @Parameter()
            @RequestParam(required = false) String aggregateType,
            @PageableDefault(size = 25, sort = "occurredAt") Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(service.list(status, aggregateType, pageable)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('outbox.view')")
    @Operation(summary = "Outbox health overview (count + oldest pending age)")
    public ResponseEntity<ResponseWrapper<OutboxStatsDto>> stats() {
        return ResponseEntity.ok(ResponseWrapper.success(service.stats()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('outbox.view')")
    @Operation(summary = "Outbox event tafsiloti (to'liq payload bilan)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Event topilmadi")
    })
    public ResponseEntity<ResponseWrapper<OutboxEventDto>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(service.findById(id, true)));
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasAuthority('outbox.manage')")
    @Operation(
            summary = "Manual retry — retry_count reset",
            description = "DLQ row'ni qaytadan polling navbatiga qaytarish (retry_count = 0)."
    )
    public ResponseEntity<ResponseWrapper<OutboxEventDto>> retry(@PathVariable UUID id) {
        log.warn("Outbox manual retry requested: id={}", id);
        return ResponseEntity.ok(ResponseWrapper.success(service.retry(id),
                "Outbox event re-queued for publish"));
    }

    @PostMapping("/{id}/discard")
    @PreAuthorize("hasAuthority('outbox.manage')")
    @Operation(
            summary = "Discard — published_at qo'yib qo'yish (skip publish)",
            description = "Event'ni Kafka'ga jo'natmasdan tashlab qo'yish (poison pill, eskirgan event). Soft delete emas, audit'da qoladi."
    )
    public ResponseEntity<ResponseWrapper<OutboxEventDto>> discard(
            @PathVariable UUID id,
            @RequestParam(required = false)
            @Pattern(regexp = "^[\\p{L}\\p{N} ._:'\\-]{0,255}$", message = "Reason: 0-255 chars")
            String reason
    ) {
        log.warn("Outbox manual discard requested: id={} reason={}", id, reason);
        return ResponseEntity.ok(ResponseWrapper.success(service.discard(id, reason),
                "Outbox event discarded"));
    }
}
