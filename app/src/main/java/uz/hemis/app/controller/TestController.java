package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Services")
@RestController
@RequestMapping("/app/rest/v2/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    @GetMapping("/echo")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> echo(@RequestParam(defaultValue = "Hello") String message) {
        Map<String, String> response = new HashMap<>();
        response.put("echo", message);
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @GetMapping("/status")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getTestStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "test");
        status.put("status", "running");
        status.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ResponseWrapper.success(status));
    }
}
