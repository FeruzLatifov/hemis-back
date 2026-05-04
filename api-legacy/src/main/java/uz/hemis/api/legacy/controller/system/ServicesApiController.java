package uz.hemis.api.legacy.controller.system;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "70.Qo'shimcha xizmatlar", description = "Xizmatlar API")
@RestController
@RequestMapping("/app/rest/v2/services-api")
@RequiredArgsConstructor
@Slf4j
public class ServicesApiController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/available")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getAvailableServices() {
        Map<String, Object> services = new LinkedHashMap<>();
        services.put("totalServices", 0);
        services.put("activeServices", 0);

        return ResponseEntity.ok(ResponseWrapper.success(services));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info/{serviceCode}")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> getServiceInfo(@PathVariable String serviceCode) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("serviceCode", serviceCode);
        info.put("serviceName", "Service " + serviceCode);
        info.put("status", "active");

        return ResponseEntity.ok(ResponseWrapper.success(info));
    }
}
