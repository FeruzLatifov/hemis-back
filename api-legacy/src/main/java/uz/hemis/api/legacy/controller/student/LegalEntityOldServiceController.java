package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Legal Entity Old Service Controller - CUBA REST API Compatible
 *
 * <p>OLD-HEMIS: LeganEntityServiceBean.get(tin)</p>
 * <p>Calls GNK legal entity API via e-gov gateway</p>
 * <p>Note: old-hemis class had typo "Legan" instead of "Legal"</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/legalentity-old")
@Tag(name = "64.Yuridik shaxslar", description = "GNK yuridik shaxs ma'lumotlari")
@Slf4j
public class LegalEntityOldServiceController {

    @Value("${hemis.integration.gnk.api.url:https://apimgw.egov.uz:8243/gnk/service/legalentity/v1}")
    private String gnkApiUrl;

    @Value("${hemis.integration.gnk.oauth2.url:https://iskm.egov.uz:9444/oauth2/token}")
    private String gnkTokenUrl;

    @Value("${hemis.integration.gnk.oauth2.client-id:5kl5wLLzyoS4pDT7EeibMLcZojga}")
    private String gnkClientId;

    @Value("${hemis.integration.gnk.oauth2.client-secret:PUNfVZ4mrTFKtkZTdRxxNuHAHMEa}")
    private String gnkClientSecret;

    @Value("${hemis.integration.gnk.oauth2.username:minvuz_user1}")
    private String gnkUsername;

    @Value("${hemis.integration.gnk.oauth2.password:m1nvuz!@#}")
    private String gnkPassword;

    /**
     * Get legal entity information by TIN
     *
     * <p>OLD-HEMIS: LeganEntityServiceBean.get(tin)</p>
     * <p>1. Get OAuth2 token from e-gov</p>
     * <p>2. POST {"tin":"X"} to GNK API</p>
     * <p>3. Return {success: true/false, data: response}</p>
     */
    @GetMapping("/get")
    @Operation(summary = "Yuridik shaxs ma'lumotlari (GNK)", description = "INN bo'yicha GNK dan yuridik shaxs ma'lumotlarini olish")
    public ResponseEntity<?> get(
            @Parameter(description = "INN (TIN)", required = true) @RequestParam String tin) {

        log.info("[CUBA Service] legalentity-old/get: tin={}", tin);

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        try {
            // 1. Get OAuth2 token
            String token = getGnkToken();
            if (token == null) {
                result.put("success", false);
                result.put("data", "Internal error");
                return ResponseEntity.ok(result);
            }

            // 2. Call GNK API
            String body = String.format("{\"tin\":\"%s\"}", tin);
            String responseJson = callGnkApi(body, token);

            if (responseJson == null || responseJson.isEmpty()) {
                result.put("success", false);
                result.put("data", "Internal error");
                return ResponseEntity.ok(result);
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = mapper.readValue(responseJson, Map.class);

            if (responseMap.isEmpty()) {
                result.put("success", false);
                result.put("data", "Internal error");
                return ResponseEntity.ok(result);
            }

            if (!responseMap.containsKey("company")) {
                result.put("success", false);
            } else {
                result.put("success", true);
            }
            result.put("data", responseMap);

        } catch (Exception e) {
            log.error("Error calling GNK API for TIN: {}", tin, e);
            result.put("success", false);
            result.put("data", "Internal error");
        }

        return ResponseEntity.ok(result);
    }

    private String getGnkToken() {
        try {
            String basic = Base64.getEncoder().encodeToString(
                    (gnkClientSecret + ":" + gnkClientId).getBytes(StandardCharsets.UTF_8));
            String auth = "Basic " + basic;
            String urlParameters = "grant_type=password&username=" + gnkUsername + "&password=" + gnkPassword;

            HttpsURLConnection connection = (HttpsURLConnection) URI.create(gnkTokenUrl).toURL().openConnection();
            applyGovSsl(connection);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Authorization", auth);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(urlParameters.getBytes(StandardCharsets.UTF_8));
            }
            connection.connect();

            String jsonText = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            connection.disconnect();

            if (jsonText.isEmpty()) return null;

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = mapper.readValue(jsonText, Map.class);
            return (String) responseMap.get("access_token");

        } catch (Exception e) {
            log.error("Error getting GNK token", e);
            return null;
        }
    }

    private String callGnkApi(String body, String token) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) URI.create(gnkApiUrl).toURL().openConnection();
            applyGovSsl(connection);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            connection.connect();

            String jsonText = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            connection.disconnect();
            return jsonText.isEmpty() ? null : jsonText;

        } catch (Exception e) {
            log.error("Error calling GNK API", e);
            return null;
        }
    }

    /**
     * Apply per-connection SSL bypass for government APIs with self-signed certs.
     */
    private void applyGovSsl(HttpsURLConnection conn) {
        javax.net.ssl.SSLContext sc = uz.hemis.service.base.AbstractGovernmentApiService.getGovSslContextStatic();
        conn.setSSLSocketFactory(sc.getSocketFactory());
        conn.setHostnameVerifier((hostname, session) -> true);
    }
}
