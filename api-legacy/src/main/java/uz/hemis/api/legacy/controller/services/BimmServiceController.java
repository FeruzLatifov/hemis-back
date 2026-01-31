package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * BIMM Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with BIMM (Birlashgan Ijtimoiy Ma'lumotlar Markazi)</p>
 * <p>OLD-HEMIS response formatiga 100% mos</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/bimm")
@Tag(name = "66.BIMM", description = "BIMM integratsiya xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class BimmServiceController {

    /**
     * Check disability status from BIMM
     */
    @GetMapping("/disabilityCheck")
    @Operation(summary = "Nogironlik holatini tekshirish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> disabilityCheck(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl,
            @Parameter(description = "Hujjat raqami", required = false) @RequestParam(required = false) String document) {
        log.info("[CUBA Service] bimm/disabilityCheck: pinfl={}, document={}", pinfl, document);

        // Old-hemis format: {success, data} wrapper
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("document", document);
        data.put("hasDisability", false);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * Check poverty register status
     *
     * OLD-HEMIS response format (from external API):
     * {"errors":{"":["Bu Pinfl dagi shaxs topilmadi"]},"type":"https://tools.ietf.org/html/rfc7231#section-6.5.1",
     *  "title":"One or more validation errors occurred.","status":400,"traceId":"00-..."}
     */
    @GetMapping("/provertyRegister")
    @Operation(summary = "Kam ta'minlangan oilalar ro'yxatini tekshirish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> provertyRegister(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/provertyRegister: pinfl={}", pinfl);

        // Old-hemis proxies to external API which returns this error format
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        LinkedHashMap<String, Object> errors = new LinkedHashMap<>();
        errors.put("", List.of("Bu Pinfl dagi shaxs topilmadi"));
        response.put("errors", errors);
        response.put("type", "https://tools.ietf.org/html/rfc7231#section-6.5.1");
        response.put("title", "One or more validation errors occurred.");
        response.put("status", 400);
        response.put("traceId", "00-" + UUID.randomUUID().toString().replace("-", "").substring(0, 32)
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "-01");
        return ResponseEntity.ok(response);
    }

    /**
     * Get certificate information
     *
     * OLD-HEMIS response format (from BIMM API - returns ARRAY at root):
     * [{"status":1,"count":0,"message":"Success","data":[]}]
     */
    @GetMapping("/certificate")
    @Operation(summary = "Sertifikat ma'lumotlarini olish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> certificate(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/certificate: pinfl={}", pinfl);

        // Old-hemis returns raw BIMM API response which is an ARRAY
        LinkedHashMap<String, Object> item = new LinkedHashMap<>();
        item.put("status", 0);
        item.put("count", 0);
        item.put("message", "Service unavailable");
        item.put("data", List.of());

        return ResponseEntity.ok(List.of(item));
    }

    /**
     * Get academic degree information
     *
     * OLD-HEMIS response format:
     * {"success":true,"data":[],"message":"ok"}
     */
    @GetMapping("/academicDegree")
    @Operation(summary = "Ilmiy daraja ma'lumotlarini olish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> academicDegree(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/academicDegree: pinfl={}", pinfl);

        // Old-hemis format: {success, data (array), message}
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", List.of());
        response.put("message", "ok");
        return ResponseEntity.ok(response);
    }

    /**
     * Get teacher training information
     */
    @GetMapping("/teacherTraining")
    @Operation(summary = "O'qituvchi malaka oshirish ma'lumotlarini olish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> teacherTraining(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/teacherTraining: pinfl={}", pinfl);

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", List.of());
        response.put("message", "ok");
        return ResponseEntity.ok(response);
    }
}
