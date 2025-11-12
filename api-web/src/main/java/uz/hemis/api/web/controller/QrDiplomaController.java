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
import java.util.UUID;

@Tag(name = "QR Diplomas")
@RestController
@RequestMapping("/app/rest/v2/qr-diploma")
@RequiredArgsConstructor
@Slf4j
public class QrDiplomaController {

    @PostMapping("/generate/{diplomaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> generateQrCode(@PathVariable UUID diplomaId) {
        log.info("Generating QR code for diploma: {}", diplomaId);

        Map<String, String> result = new HashMap<>();
        result.put("diplomaId", diplomaId.toString());
        result.put("qrCode", "QR-" + System.currentTimeMillis());
        result.put("qrImageUrl", "/api/qr-diploma/image/" + result.get("qrCode"));

        return ResponseEntity.ok(ResponseWrapper.success(result));
    }

    @GetMapping("/verify")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> verifyQrCode(@RequestParam String qrCode) {
        Map<String, Object> verification = new HashMap<>();
        verification.put("qrCode", qrCode);
        verification.put("valid", true);

        return ResponseEntity.ok(ResponseWrapper.success(verification));
    }
}
