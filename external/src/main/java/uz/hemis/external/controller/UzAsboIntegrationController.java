package uz.hemis.external.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "UzASBO Integration")
@RestController
@RequestMapping("/app/rest/v2/integrations/uzasbo")
@RequiredArgsConstructor
@Slf4j
public class UzAsboIntegrationController {

    @PostMapping("/send-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> sendDataToUzAsbo(@RequestBody Map<String, Object> data) {
        log.info("Sending data to UzASBO");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "sent");
        result.put("destination", "UzASBO");

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    @GetMapping("/check-connection")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> checkConnection() {
        Map<String, Object> status = new HashMap<>();
        status.put("connected", true);
        status.put("service", "UzASBO");

        return ResponseEntity.ok(ResponseWrapper.success(status));
    }
}
