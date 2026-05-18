package uz.hemis.common.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * HTTP 202 Accepted javob — Univer ga sync queue'ga olinganligini bildirish.
 *
 * <p>Sinxron natija (qancha INSERT/UPDATE bo'lgani) BERILMAYDI — DB write
 * Kafka consumer ichida async sodir bo'ladi. Univer per-row natijani bilmoqchi
 * bo'lsa, {@code batchId} bilan {@code GET /api/v1/university/employees/sync/{batchId}}
 * orqali so'rasin (kelajakda alohida endpoint).</p>
 *
 * <p>{@code rejections} — controller'da PRE-validate qilingan (PINFL format) row'lar.
 * Bu row'lar Kafka'ga qo'yilmagan, async qaytarib bo'lmaydi.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Univer sync queue'ga olindi (async). Per-row natija employee_sync_log jadvalida.")
public record EmployeeSyncAcceptedResponse(
        @Schema(description = "Bu sync chaqiruvi uchun batch identifikator (audit/trace).")
        UUID batchId,

        @Schema(description = "Kafka topic'ga yuborilgan row soni (PINFL format OK).")
        int acceptedCount,

        @Schema(description = "Pre-validation paytida rad etilgan row soni.")
        int rejectedCount,

        @Schema(description = "Rad etilgan row tafsiloti (faqat pre-validation; async DB xatolar emas).")
        List<RejectionDetail> rejections
) implements Serializable {

    @Schema(description = "Pre-validation rad etilgan row.")
    public record RejectionDetail(
            @Schema(description = "PINFL maskalangan formatda (PII himoyasi).")
            String pinflMasked,
            String sourceUid,
            @Schema(description = "Rad etish sababi (masalan: 'Invalid PINFL').")
            String reason
    ) implements Serializable {}
}
