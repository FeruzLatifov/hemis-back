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

@Tag(name = "Social Protection")
@RestController
@RequestMapping("/app/rest/v2/integrations/social-protection")
@RequiredArgsConstructor
@Slf4j
public class SocialProtectionController {

    @GetMapping("/benefits/{pinfl}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> checkSocialBenefits(@PathVariable String pinfl) {
        log.debug("Checking social benefits for PINFL: {}", pinfl);

        Map<String, Object> benefits = new HashMap<>();
        benefits.put("pinfl", pinfl);
        benefits.put("hasBenefits", false);
        benefits.put("source", "Social Protection System");

        return ResponseEntity.ok(ResponseWrapper.success(benefits));
    }

    @GetMapping("/verify/{pinfl}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifySocialStatus(@PathVariable String pinfl) {
        Map<String, Object> status = new HashMap<>();
        status.put("pinfl", pinfl);
        status.put("verified", true);

        return ResponseEntity.ok(ResponseWrapper.success(status));
    }
}
