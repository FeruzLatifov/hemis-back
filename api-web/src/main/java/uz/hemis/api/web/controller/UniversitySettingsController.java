package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "University Settings")
@RestController
@RequestMapping("/app/rest/v2/university-settings")
@RequiredArgsConstructor
@Slf4j
public class UniversitySettingsController {

    @GetMapping("/{universityCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getSettings(@PathVariable String universityCode) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("universityCode", universityCode);
        settings.put("language", "uz");
        settings.put("timezone", "Asia/Tashkent");

        return ResponseEntity.ok(ResponseWrapper.success(settings));
    }

    @PutMapping("/{universityCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> updateSettings(
            @PathVariable String universityCode,
            @RequestBody Map<String, Object> settings
    ) {
        log.info("Updating settings for university: {}", universityCode);

        Map<String, Object> result = new HashMap<>();
        result.put("universityCode", universityCode);
        result.put("status", "updated");

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }
}
