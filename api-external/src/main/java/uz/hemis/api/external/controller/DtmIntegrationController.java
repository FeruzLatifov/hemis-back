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

@Tag(name = "DTM Integration")
@RestController
@RequestMapping("/app/rest/v2/integrations/dtm")
@RequiredArgsConstructor
@Slf4j
public class DtmIntegrationController {

    @GetMapping("/students/{pinfl}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStudentFromDtm(@PathVariable String pinfl) {
        log.debug("Fetching student data from DTM by PINFL: {}", pinfl);

        Map<String, Object> student = new HashMap<>();
        student.put("pinfl", pinfl);
        student.put("source", "DTM");
        student.put("status", "available");

        return ResponseEntity.ok(ResponseWrapper.success(student));
    }

    @GetMapping("/verify/{pinfl}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifyStudent(@PathVariable String pinfl) {
        log.debug("Verifying student with DTM: {}", pinfl);

        Map<String, Object> verification = new HashMap<>();
        verification.put("pinfl", pinfl);
        verification.put("verified", true);
        verification.put("source", "DTM");

        return ResponseEntity.ok(ResponseWrapper.success(verification));
    }
}
