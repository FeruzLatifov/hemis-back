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

@Tag(name = "OAK Integration")
@RestController
@RequestMapping("/app/rest/v2/integrations/oak")
@RequiredArgsConstructor
@Slf4j
public class OakIntegrationController {

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> syncWithOak(@RequestBody Map<String, String> request) {
        log.info("Syncing data with OAK");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "synced");
        result.put("source", "OAK");
        result.put("recordsSynced", 0);

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getOakStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connected", true);
        status.put("lastSync", "2025-10-31");

        return ResponseEntity.ok(ResponseWrapper.success(status));
    }
}
