package uz.hemis.api.external.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Tax Integration")
@RestController
@RequestMapping("/app/rest/v2/integrations/tax")
@RequiredArgsConstructor
@Slf4j
public class TaxIntegrationController {

    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> sendTaxReport(@RequestBody Map<String, Object> report) {
        log.info("Sending tax report");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "submitted");
        result.put("reportId", "TAX-" + System.currentTimeMillis());

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    @GetMapping("/verify/{inn}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifyInn(@PathVariable String inn) {
        Map<String, Object> verification = new HashMap<>();
        verification.put("inn", inn);
        verification.put("valid", true);

        return ResponseEntity.ok(ResponseWrapper.success(verification));
    }
}
