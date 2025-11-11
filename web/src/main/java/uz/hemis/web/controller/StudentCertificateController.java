package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Certificates")
@RestController
@RequestMapping("/app/rest/v2/student-certificates")
@RequiredArgsConstructor
@Slf4j
public class StudentCertificateController {

    @PostMapping("/generate/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'DEAN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> generateCertificate(
            @PathVariable UUID studentId,
            @RequestParam String certificateType
    ) {
        log.info("Generating certificate for student: {}", studentId);

        Map<String, Object> certificate = new HashMap<>();
        certificate.put("studentId", studentId);
        certificate.put("certificateType", certificateType);
        certificate.put("certificateId", "CERT-" + System.currentTimeMillis());
        certificate.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(certificate));
    }

    @GetMapping("/verify/{certificateId}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifyCertificate(@PathVariable String certificateId) {
        Map<String, Object> verification = new HashMap<>();
        verification.put("certificateId", certificateId);
        verification.put("valid", true);
        verification.put("verifiedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(verification));
    }
}
