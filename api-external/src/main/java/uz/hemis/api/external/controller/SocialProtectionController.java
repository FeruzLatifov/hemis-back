package uz.hemis.api.external.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "61.Ijtimoiy himoya", description = "Ijtimoiy himoya integratsiyasi")
@RestController
@RequestMapping("/app/rest/v2/integrations/social-protection")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SocialProtectionController {

    @GetMapping("/benefits/{pinfl}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> checkSocialBenefits(
            @PathVariable @Pattern(regexp = "\\d{14}", message = "PINFL must be exactly 14 digits") String pinfl) {
        log.debug("Checking social benefits for PINFL: {}****", pinfl.substring(0, 4));

        Map<String, Object> benefits = new HashMap<>();
        benefits.put("pinfl", pinfl);
        benefits.put("hasBenefits", false);
        benefits.put("source", "Social Protection System");

        return ResponseEntity.ok(ResponseWrapper.success(benefits));
    }

    @GetMapping("/verify/{pinfl}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifySocialStatus(
            @PathVariable @Pattern(regexp = "\\d{14}", message = "PINFL must be exactly 14 digits") String pinfl) {
        Map<String, Object> status = new HashMap<>();
        status.put("pinfl", pinfl);
        status.put("verified", true);

        return ResponseEntity.ok(ResponseWrapper.success(status));
    }
}
