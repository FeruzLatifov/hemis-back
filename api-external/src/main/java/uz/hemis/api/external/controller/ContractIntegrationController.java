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

@Tag(name = "Contract Integration")
@RestController
@RequestMapping("/app/rest/v2/integrations/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractIntegrationController {

    @PostMapping("/sign")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> signContract(@RequestBody Map<String, Object> contract) {
        log.info("Signing contract");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "signed");
        result.put("contractId", "CNT-" + System.currentTimeMillis());

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    @GetMapping("/verify/{contractId}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifyContract(@PathVariable String contractId) {
        Map<String, Object> verification = new HashMap<>();
        verification.put("contractId", contractId);
        verification.put("valid", true);

        return ResponseEntity.ok(ResponseWrapper.success(verification));
    }
}
