package uz.hemis.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.hemis.service.config.HemisApiProperties;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HEMIS API Integration Service
 *
 * <p>Tashqi api.hemis.uz API ga proxy chaqiruvlar</p>
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * <p><strong>Authentication Flow:</strong></p>
 * <ol>
 *   <li>POST /api/user/auth with username/password from .env</li>
 *   <li>Receive JWT token in response</li>
 *   <li>Cache token for ~23 hours</li>
 *   <li>Auto-refresh when expired</li>
 * </ol>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /api/integration/hemis/studentAndContractInfo/{pinfl} - Contract info</li>
 * </ul>
 *
 * <p><strong>Configuration (.env):</strong></p>
 * <pre>
 * HEMIS_API_BASE_URL=https://api.hemis.uz
 * HEMIS_API_USERNAME=hemis_integration
 * HEMIS_API_PASSWORD=your-secret-password
 * HEMIS_API_TIMEOUT=30
 * </pre>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(HemisApiProperties.class)
public class HemisApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HemisApiProperties properties;

    // Token caching (23 hours validity)
    private String cachedToken = null;
    private LocalDateTime tokenExpiresAt = null;

    /**
     * Get student and contract information from api.hemis.uz
     *
     * <p><strong>OLD-HEMIS Logic (StudentServiceBean.contractInfo):</strong></p>
     * <ol>
     *   <li>Get token via getApiHemisToken() (login with username/password)</li>
     *   <li>Call https://api.hemis.uz/api/integration/hemis/studentAndContractInfo/{pinfl}</li>
     *   <li>Return response as Map</li>
     * </ol>
     *
     * <p><strong>Response example:</strong></p>
     * <pre>
     * {
     *   "statusCode": 200,
     *   "message": "Muvaffaqiyatli bajarildi",
     *   "timeStamp": "2025-11-29T06:18:22.817959598",
     *   "object": {
     *     "institutionType": "Oliy ta'lim",
     *     "pinfl": "61111065190052",
     *     "fullName": "...",
     *     "contractNumber": "...",
     *     "contractDate": "...",
     *     "eduOrganizationId": 316,
     *     "eduOrganization": "...",
     *     "eduSpeciality": "...",
     *     "eduContractSum": 8623000,
     *     "gpa": 3.86,
     *     "debit": 2300000,
     *     "credit": 6323000,
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param pinfl Student PINFL
     * @return Contract info map (from api.hemis.uz)
     */
    public Map<String, Object> getContractInfo(String pinfl) {
        log.info("[HEMIS API] Getting contract info for PINFL: {}", pinfl);

        // Check if API is configured
        if (!properties.isConfigured()) {
            log.error("[HEMIS API] API credentials not configured! Set HEMIS_API_USERNAME and HEMIS_API_PASSWORD");
            return errorResponse("credentials_not_configured",
                    "HEMIS API credentials not configured. Set HEMIS_API_USERNAME and HEMIS_API_PASSWORD in .env");
        }

        try {
            // Get token (cached or fresh)
            String token = getApiHemisToken();
            if (token == null) {
                return errorResponse("auth_failed", "Failed to authenticate with api.hemis.uz");
            }

            // Disable SSL verification (api.hemis.uz may have self-signed cert)
            disableSslVerification();

            // Build URL
            String url = String.format("%s/api/integration/hemis/studentAndContractInfo/%s",
                    properties.getBaseUrl(), pinfl);
            log.debug("[HEMIS API] Request URL: {}", url);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");
            headers.setBearerAuth(token);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Call external API
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);

            String body = response.getBody();
            log.debug("[HEMIS API] Response status: {}", response.getStatusCode());

            // Parse JSON response
            if (body == null || body.isEmpty()) {
                log.warn("[HEMIS API] Empty response for PINFL: {}", pinfl);
                return errorResponse("empty_response", "Empty response from HEMIS API");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(body, LinkedHashMap.class);
            log.info("[HEMIS API] Contract info retrieved successfully for PINFL: {}", pinfl);
            return result;

        } catch (Exception e) {
            log.error("[HEMIS API] Error getting contract info for PINFL: {} - {}", pinfl, e.getMessage(), e);
            return errorResponse("api_error", e.getMessage());
        }
    }

    /**
     * Get API token from api.hemis.uz (with caching)
     *
     * <p><strong>OLD-HEMIS Logic (MyTokenServiceBean.getApiHemisToken):</strong></p>
     * <ol>
     *   <li>Check cached token (valid for 23 hours)</li>
     *   <li>If expired or missing, call POST /api/user/auth with username/password</li>
     *   <li>Cache new token with timestamp</li>
     *   <li>Return token</li>
     * </ol>
     *
     * @return JWT token or null if authentication fails
     */
    private synchronized String getApiHemisToken() {
        // Check if cached token is still valid (23 hours)
        if (cachedToken != null && tokenExpiresAt != null) {
            if (LocalDateTime.now().isBefore(tokenExpiresAt)) {
                log.debug("[HEMIS API] Using cached token (expires: {})", tokenExpiresAt);
                return cachedToken;
            }
            log.info("[HEMIS API] Token expired, refreshing...");
        }

        // Get fresh token
        try {
            disableSslVerification();

            String authUrl = properties.getBaseUrl() + "/api/user/auth";
            log.info("[HEMIS API] Authenticating at: {}", authUrl);

            // Build auth request body
            Map<String, String> authBody = new LinkedHashMap<>();
            authBody.put("username", properties.getUsername());
            authBody.put("password", properties.getPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(authBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    authUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), LinkedHashMap.class);

                if (responseMap.containsKey("token")) {
                    cachedToken = responseMap.get("token").toString();
                    // Token valid for 23 hours
                    tokenExpiresAt = LocalDateTime.now().plusHours(23);
                    log.info("[HEMIS API] Authentication successful, token cached until: {}", tokenExpiresAt);
                    return cachedToken;
                }
            }

            log.error("[HEMIS API] Authentication failed: {}", response.getBody());
            return null;

        } catch (Exception e) {
            log.error("[HEMIS API] Authentication error: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Build error response
     */
    private Map<String, Object> errorResponse(String code, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("code", code);
        error.put("message", message);
        return error;
    }

    /**
     * Disable SSL verification
     *
     * <p><strong>WARNING:</strong> Only use for trusted APIs with self-signed certs</p>
     * <p>In production, add the certificate to Java truststore instead</p>
     */
    private void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("[HEMIS API] Failed to disable SSL verification", e);
        }
    }
}
