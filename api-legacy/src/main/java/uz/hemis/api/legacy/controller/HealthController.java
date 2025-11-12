package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health")
@RestController
@RequestMapping("/app/rest/v2/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    @GetMapping
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "HEMIS");
        response.put("version", "2.0.0");

        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @GetMapping("/ping")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Service is running");

        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @GetMapping("/version")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getVersion() {
        Map<String, String> version = new HashMap<>();
        version.put("application", "HEMIS");
        version.put("version", "2.0.0");
        version.put("build", LocalDateTime.now().toString());

        return ResponseEntity.ok(ResponseWrapper.success(version));
    }
}
