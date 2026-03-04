package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.ApiMspdTokenService;
import uz.hemis.service.student.SocialService;

import java.util.*;

/**
 * Social Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with Social Protection services via API-MSPD (172.18.9.171)</p>
 * <p>OLD-HEMIS response formatiga 100% mos</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/social")
@Tag(name = "61.Ijtimoiy himoya", description = "Ijtimoiy himoya xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class SocialServiceController {

    private final ApiMspdTokenService apiMspdTokenService;
    private final SocialService socialService;

    /**
     * Check single register status
     */
    @GetMapping("/singleRegister")
    @Operation(summary = "Yagona reestr tekshirish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> singleRegister(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] social/singleRegister: pinfl={}", pinfl);
        return ResponseEntity.ok(socialService.singleRegister(pinfl));
    }

    /**
     * Get full daftar information
     */
    @GetMapping("/daftarFull")
    @Operation(summary = "To'liq daftar ma'lumotlari")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> daftarFull(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] social/daftarFull: pinfl={}", pinfl);
        return ResponseEntity.ok(socialService.daftarFull(pinfl));
    }

    /**
     * Get short daftar information
     */
    @GetMapping("/daftarShort")
    @Operation(summary = "Qisqacha daftar ma'lumotlari")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> daftarShort(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] social/daftarShort: pinfl={}", pinfl);
        return ResponseEntity.ok(socialService.daftarShort(pinfl));
    }

    /**
     * Get women support information
     */
    @GetMapping("/women")
    @Operation(summary = "Ayollar qo'llab-quvvatlash ma'lumotlari")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> women(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl,
            @Parameter(description = "Hujjat seriya va raqami", required = false) @RequestParam(required = false) String sn) {
        log.info("[CUBA Service] social/women: pinfl={}, sn={}", pinfl, sn);
        return ResponseEntity.ok(socialService.women(pinfl, sn));
    }

    /**
     * Get youth support information
     */
    @GetMapping("/young")
    @Operation(summary = "Yoshlar qo'llab-quvvatlash ma'lumotlari")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> young(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl,
            @Parameter(description = "Seriya", required = false) @RequestParam(required = false) String seria,
            @Parameter(description = "Raqam", required = false) @RequestParam(required = false) String number) {
        log.info("[CUBA Service] social/young: pinfl={}, seria={}, number={}", pinfl, seria, number);
        return ResponseEntity.ok(socialService.young(pinfl, seria, number));
    }

    /**
     * VTEK disability check via egov.uz
     *
     * <p>OLD-HEMIS: SocialServiceBean.vtek(pinfl, birthDocument, birthDate)</p>
     * <p>POST https://apimgw.egov.uz:8243/minzdrav/vtek/v4/birthdoc</p>
     * <p>Auth: Bearer token from GUVD token service</p>
     */
    @GetMapping("/vtek")
    @Operation(summary = "VTEK nogironlik tekshirish", description = "E-gov orqali VTEK nogironlik ma'lumotlarini tekshirish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> vtek(
            @Parameter(description = "PINFL") @RequestParam String pinfl,
            @Parameter(description = "Tug'ilgan sana (yyyy-MM-dd)") @RequestParam(required = false) String birthDate,
            @Parameter(description = "Tug'ilganlik guvohnomasi raqami") @RequestParam(required = false) String birthDocument) {

        log.info("[CUBA Service] social/vtek: pinfl={}, birthDate={}, birthDocument={}", pinfl, birthDate, birthDocument);

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        try {
            String token = apiMspdTokenService.getAccessToken();
            if (token == null) {
                result.put("success", false);
                result.put("data", "API-MSPD token not available");
                return ResponseEntity.ok(result);
            }

            // API-MSPD /disability/disability-pinfl-document/ yoki /disability-document-birth-date/
            String baseUrl = apiMspdTokenService.getBaseUrl();
            String url;
            Map<String, String> body = new LinkedHashMap<>();

            if (pinfl != null && !pinfl.isBlank()) {
                // PINFL + document
                url = baseUrl + "/disability/disability-pinfl-document/";
                body.put("pinfl", pinfl);
                body.put("document", birthDocument != null ? birthDocument : "");
            } else {
                // document + birth_date
                url = baseUrl + "/disability/disability-document-birth-date/";
                body.put("document", birthDocument != null ? birthDocument : "");
                body.put("birth_date", birthDate != null ? birthDate : "");
            }

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            org.springframework.http.HttpEntity<Map<String, String>> entity =
                    new org.springframework.http.HttpEntity<>(body, headers);

            log.info("Calling API-MSPD: POST {}", url);
            var response = new org.springframework.web.client.RestTemplate()
                    .postForEntity(url, entity, Object.class);

            result.put("success", true);
            result.put("data", response.getBody());

        } catch (Exception e) {
            log.error("Error calling VTEK API via API-MSPD", e);
            result.put("success", false);
            result.put("data", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
