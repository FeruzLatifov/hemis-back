package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "University Additional")
@RestController
@RequestMapping("/app/rest/v2/university-additional")
@RequiredArgsConstructor
@Slf4j
public class UniversityAdditionalController {

    @GetMapping("/{universityCode}/infrastructure")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getInfrastructure(@PathVariable String universityCode) {
        Map<String, Object> infrastructure = new HashMap<>();
        infrastructure.put("universityCode", universityCode);
        infrastructure.put("buildings", 0);
        infrastructure.put("laboratories", 0);

        return ResponseEntity.ok(ResponseWrapper.success(infrastructure));
    }

    @GetMapping("/{universityCode}/achievements")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getAchievements(@PathVariable String universityCode) {
        Map<String, Object> achievements = new HashMap<>();
        achievements.put("universityCode", universityCode);
        achievements.put("totalAchievements", 0);

        return ResponseEntity.ok(ResponseWrapper.success(achievements));
    }
}
