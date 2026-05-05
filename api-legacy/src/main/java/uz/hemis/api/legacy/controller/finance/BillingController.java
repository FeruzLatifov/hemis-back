package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Billing endpoints — STUB controller.
 *
 * <p><strong>WARNING (OWASP A04):</strong> Bu controller fake/stub implementation edi —
 * {@code processPayment} har doim {@code status=processed} qaytarib, hech qanday
 * persistence qilmas edi. UI'da "to'langan" deb ko'rinardi, real pul harakatsiz =
 * <strong>phishing vector</strong>. {@code getInvoices}/{@code getStudentBalance} ham
 * bo'sh json qaytaradi (mock).</p>
 *
 * <p>Real billing service yoziladigan vaqtgacha — {@code processPayment} 503 qaytaradi
 * ({@code Service Unavailable}). Read endpoint'lar deprecated belgilab qoldirilgan,
 * lekin compatibility uchun ishlaydi (frontend old contract).</p>
 *
 * @since 1.0.0
 * @deprecated Real billing service kerak — bu controller fake.
 */
@Tag(name = "63.Billing", description = "To'lov hisob-kitobi (STUB)")
@RestController
@RequestMapping("/app/rest/v2/billing")
@RequiredArgsConstructor
@Slf4j
@Deprecated(since = "2.5.0", forRemoval = false)
public class BillingController {

    @Operation(summary = "get invoices (stub)")
    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OTM_API')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getInvoices(
            @RequestParam(required = false) String university
    ) {
        Map<String, Object> invoices = new LinkedHashMap<>();
        invoices.put("university", university);
        invoices.put("totalInvoices", 0);
        invoices.put("totalAmount", 0.0);
        invoices.put("note", "Billing stub — real service not yet implemented");
        return ResponseEntity.ok(ResponseWrapper.success(invoices));
    }

    @Operation(summary = "process payment — DISABLED (stub returned fake transaction)")
    @PostMapping("/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> processPayment(
            @RequestBody Map<String, Object> payment) {
        // SECURITY (A04): eski stub har doim "processed" qaytarar edi → phishing.
        // Endi 503 — real billing servisi ulanmagan.
        log.warn("SECURITY: processPayment endpoint called but disabled. Caller must use real billing flow.");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "unavailable");
        result.put("error", "Billing service not implemented — endpoint disabled to prevent fake confirmations.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ResponseWrapper.error("Billing service not implemented. Contact admin."));
    }

    @Operation(summary = "get student balance (stub)")
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/balance/{studentId}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStudentBalance(@PathVariable UUID studentId) {
        Map<String, Object> balance = new LinkedHashMap<>();
        balance.put("studentId", studentId);
        balance.put("balance", 0.0);
        balance.put("currency", "UZS");
        balance.put("note", "Balance stub — real billing service not yet implemented");
        return ResponseEntity.ok(ResponseWrapper.success(balance));
    }
}
