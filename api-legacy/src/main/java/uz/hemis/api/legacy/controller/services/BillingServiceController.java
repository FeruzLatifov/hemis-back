package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.InvoiceRequest;
import uz.hemis.common.dto.ScholarshipBillingRequest;
import uz.hemis.service.integration.BillingIntegrationService;

import java.util.Map;

/**
 * Billing Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/billing/*}</p>
 *
 * <p><strong>CRITICAL - Financial Operations:</strong></p>
 * <ul>
 *   <li>Integration with payment systems</li>
 *   <li>Invoice generation for student payments</li>
 *   <li>Scholarship disbursement to UzASBO</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "Billing", description = "To'lov va stipendiya hisob-kitob xizmatlari")
@RestController
@RequestMapping("/services/billing")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class BillingServiceController {

    private final BillingIntegrationService billingIntegrationService;

    /**
     * Generate invoice for student payment
     *
     * <p><strong>Endpoint:</strong> POST /services/billing/invoice</p>
     *
     * @param request Invoice request (student ID, amount, description)
     * @return Invoice data with payment URL
     */
    @Operation(
        summary = "Hisob-faktura yaratish",
        description = "Talaba to'lovi uchun hisob-faktura yaratish va to'lov tizimiga yuborish"
    )
    @PostMapping("/invoice")
    public ResponseEntity<Map<String, Object>> createInvoice(
        @RequestBody(description = "Hisob-faktura ma'lumotlari", required = true)
        @org.springframework.web.bind.annotation.RequestBody InvoiceRequest request
    ) {
        log.info("POST /services/billing/invoice - studentId: {}, amount: {}", 
            request.getStudentId(), request.getAmount());
        return ResponseEntity.ok(billingIntegrationService.createInvoice(request));
    }

    /**
     * Process scholarship payment
     *
     * <p><strong>Endpoint:</strong> POST /services/billing/scholarship</p>
     *
     * @param request Scholarship request (student list, period, amount)
     * @return Success status and transaction ID
     */
    @Operation(
        summary = "Stipendiya to'lovi",
        description = "Talabalar uchun stipendiya to'lovini UzASBO tizimiga yuborish"
    )
    @PostMapping("/scholarship")
    public ResponseEntity<Map<String, Object>> processScholarship(
        @RequestBody(description = "Stipendiya to'lovi ma'lumotlari", required = true)
        @org.springframework.web.bind.annotation.RequestBody ScholarshipBillingRequest request
    ) {
        log.info("POST /services/billing/scholarship - period: {}, students: {}", 
            request.getPeriod(), request.getStudents() != null ? request.getStudents().size() : 0);
        return ResponseEntity.ok(billingIntegrationService.processScholarshipPayment(request));
    }
}
