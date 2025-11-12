package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Services")
@RestController
@RequestMapping("/app/rest/v2/services-api")
@RequiredArgsConstructor
@Slf4j
public class ServicesApiController {

    @GetMapping("/available")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getAvailableServices() {
        Map<String, Object> services = new HashMap<>();
        services.put("totalServices", 0);
        services.put("activeServices", 0);

        return ResponseEntity.ok(ResponseWrapper.success(services));
    }

    @GetMapping("/info/{serviceCode}")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getServiceInfo(@PathVariable String serviceCode) {
        Map<String, String> info = new HashMap<>();
        info.put("serviceCode", serviceCode);
        info.put("serviceName", "Service " + serviceCode);
        info.put("status", "active");

        return ResponseEntity.ok(ResponseWrapper.success(info));
    }
}
