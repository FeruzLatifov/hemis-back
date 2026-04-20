package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Personal Data Service Controller - CUBA REST API Compatible
 *
 * <p>OLD-HEMIS: PersonalDataServiceBean.getData(pinfl, serial)</p>
 * <p>Calls: https://talaba.edu.uz/api/my_edu_uz/student_mvd_hemis.php</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/personal-data")
@Tag(name = "63.Shaxsiy ma'lumotlar", description = "MVD shaxsiy ma'lumotlarni tekshirish")
@Slf4j
public class PersonalDataServiceController {

    private static final String EXTERNAL_URL = "https://talaba.edu.uz/api/my_edu_uz/student_mvd_hemis.php";
    private static final String TOKEN = "12345";

    /**
     * Get personal data from MVD via talaba.edu.uz
     *
     * <p>OLD-HEMIS: PersonalDataServiceBean.getData(pinfl, serial)</p>
     * <p>Calls: GET https://talaba.edu.uz/api/my_edu_uz/student_mvd_hemis.php?TOKEN=12345&amp;pinfl=X&amp;p_seriya=Y</p>
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getData")
    @Operation(summary = "MVD shaxsiy ma'lumotlari", description = "PINFL va passport seriya bo'yicha MVD dan shaxsiy ma'lumotlarni olish")
    public Map<String, Object> getData(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl,
            @Parameter(description = "Passport seriya va raqami", required = true) @RequestParam String serial) {

        log.info("[CUBA Service] personal-data/getData: pinfl={}, serial={}", pinfl, serial);

        String requestUrl = EXTERNAL_URL + "?TOKEN=" + TOKEN + "&pinfl=" + pinfl + "&p_seriya=" + serial;

        try {
            // Per-connection SSL bypass for government APIs with self-signed certs
            HttpsURLConnection connection = (HttpsURLConnection) URI.create(requestUrl).toURL().openConnection();
            javax.net.ssl.SSLContext sc = uz.hemis.service.base.AbstractGovernmentApiService.getGovSslContextStatic();
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier((hostname, session) -> true);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.connect();

            String jsonText;
            try (InputStream is = connection.getInputStream()) {
                jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } finally {
                connection.disconnect();
            }

            // Old-hemis: if response is "no" -> return null -> incorrect_data
            if (jsonText.replaceAll("[^a-zA-Z0-9]", "").equals("no") || jsonText.isBlank()) {
                Map<String, Object> errorMap = new LinkedHashMap<>();
                errorMap.put("success", false);
                errorMap.put("code", "incorrect_data");
                errorMap.put("message", "Incorrect PINFL or passport number");
                return errorMap;
            }

            // Parse JSON response
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = mapper.readValue(jsonText, Map.class);
            resultMap.put("success", true);
            return resultMap;

        } catch (Exception e) {
            log.error("Error calling personal data service", e);
            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("success", false);
            errorMap.put("code", "service_not_available");
            errorMap.put("message", "Personal data service not available");
            return errorMap;
        }
    }
}
