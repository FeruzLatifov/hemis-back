package uz.hemis.api.legacy.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "53.Healthcheck")
@RestController
@RequestMapping("/app/rest/v2/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    @Operation(summary = "Health status — UP/DOWN ko'rsatkich")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "HEMIS");
        response.put("version", "2.0.0");

        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Operation(summary = "Ping — connectivity test")
    @GetMapping("/ping")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> ping() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Service is running");

        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Operation(summary = "get version")
    @GetMapping("/version")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getVersion() {
        Map<String, String> version = new LinkedHashMap<>();
        version.put("application", "HEMIS");
        version.put("version", "2.0.0");
        version.put("build", LocalDateTime.now().toString());

        return ResponseEntity.ok(ResponseWrapper.success(version));
    }

}
