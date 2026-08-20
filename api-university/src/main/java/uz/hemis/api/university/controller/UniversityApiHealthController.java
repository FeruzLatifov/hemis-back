package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for {@code api-university} module.
 *
 * <p>Separate from Spring Boot Actuator health — this is a lightweight module-level probe
 * used by the ministry and partner systems for connectivity tests.</p>
 */
@Tag(name = "00.Tizim holati", description = "api-university module health check")
@RestController
@RequestMapping("/api/v1/university")
public class UniversityApiHealthController {

    @Operation(summary = "Module health", description = "Returns UP if the api-university module is alive.")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "module", "api-university"
        ));
    }
}
