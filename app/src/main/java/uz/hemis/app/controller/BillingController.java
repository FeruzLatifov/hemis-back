package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Billing")
@RestController
@RequestMapping("/app/rest/v2/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getInvoices(
            @RequestParam(required = false) String university
    ) {
        Map<String, Object> invoices = new HashMap<>();
        invoices.put("university", university);
        invoices.put("totalInvoices", 0);
        invoices.put("totalAmount", 0.0);

        return ResponseEntity.ok(ResponseWrapper.success(invoices));
    }

    @PostMapping("/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> processPayment(@RequestBody Map<String, Object> payment) {
        log.info("Processing payment");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "processed");
        result.put("transactionId", "TXN-" + System.currentTimeMillis());

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    @GetMapping("/balance/{studentId}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStudentBalance(@PathVariable UUID studentId) {
        Map<String, Object> balance = new HashMap<>();
        balance.put("studentId", studentId);
        balance.put("balance", 0.0);
        balance.put("currency", "UZS");

        return ResponseEntity.ok(ResponseWrapper.success(balance));
    }
}
