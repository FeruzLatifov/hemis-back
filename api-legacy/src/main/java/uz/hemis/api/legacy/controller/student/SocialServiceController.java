package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.GuvdTokenService;
import uz.hemis.service.student.SocialService;

import java.util.*;

/**
 * Social Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with Social Protection services</p>
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

    private final GuvdTokenService guvdTokenService;
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
            String token = guvdTokenService.getToken();
            if (token == null) {
                result.put("success", false);
                result.put("data", "GUVD token not available");
                return ResponseEntity.ok(result);
            }

            String body = String.format("""
                {
                    "transaction_id":"1234",
                    "pin":"%s",
                    "purpose":"",
                    "consent":"true",
                    "birth_document":"%s",
                    "birth_date": "%s"
                }""",
                    pinfl,
                    birthDocument != null ? birthDocument : "",
                    birthDate != null ? birthDate : "");

            // Use AbstractGovernmentApiService pattern (HttpsURLConnection)
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    }
            };
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            javax.net.ssl.HttpsURLConnection conn = (javax.net.ssl.HttpsURLConnection) java.net.URI.create("https://apimgw.egov.uz:8243/minzdrav/vtek/v4/birthdoc").toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            java.io.InputStream stream = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String responseStr = stream != null
                    ? new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
                    : "";
            conn.disconnect();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object responseData;
            try {
                responseData = mapper.readValue(responseStr, Object.class);
            } catch (Exception ex) {
                responseData = responseStr;
            }

            result.put("success", true);
            result.put("data", responseData);

        } catch (Exception e) {
            log.error("Error calling VTEK API", e);
            result.put("success", false);
            result.put("data", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
