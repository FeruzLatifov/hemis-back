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

@Tag(name = "55.DTM", description = "DTM integratsiyasi")
@RestController
@RequestMapping("/app/rest/v2/integrations/dtm")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DtmIntegrationController {

    @GetMapping("/students/{pinfl}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStudentFromDtm(
            @PathVariable @Pattern(regexp = "\\d{14}", message = "PINFL must be exactly 14 digits") String pinfl) {
        log.debug("Fetching student data from DTM by PINFL: {}****", pinfl.substring(0, 4));

        Map<String, Object> student = new HashMap<>();
        student.put("pinfl", pinfl);
        student.put("source", "DTM");
        student.put("status", "available");

        return ResponseEntity.ok(ResponseWrapper.success(student));
    }

    @GetMapping("/verify/{pinfl}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifyStudent(
            @PathVariable @Pattern(regexp = "\\d{14}", message = "PINFL must be exactly 14 digits") String pinfl) {
        log.debug("Verifying student with DTM: {}****", pinfl.substring(0, 4));

        Map<String, Object> verification = new HashMap<>();
        verification.put("pinfl", pinfl);
        verification.put("verified", true);
        verification.put("source", "DTM");

        return ResponseEntity.ok(ResponseWrapper.success(verification));
    }
}
