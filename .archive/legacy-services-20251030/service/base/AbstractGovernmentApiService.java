package uz.hemis.app.service.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract Base Class for Government API Services
 *
 * <p><strong>OPTIMIZATION - DRY Principle:</strong></p>
 * <ul>
 *   <li>Eliminates code duplication across government services</li>
 *   <li>Common SSL handling (one place)</li>
 *   <li>Common error/success response builders (one place)</li>
 *   <li>Generic external API call method (reusable)</li>
 * </ul>
 *
 * <p><strong>IMPORTANT:</strong></p>
 * <ul>
 *   <li>This is INTERNAL optimization only</li>
 *   <li>External API (URLs, request/response format) unchanged</li>
 *   <li>100% backward compatible</li>
 * </ul>
 *
 * <p><strong>Subclasses:</strong></p>
 * <ul>
 *   <li>PersonalDataService</li>
 *   <li>PassportDataService</li>
 *   <li>BimmService</li>
 *   <li>SocialService</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractGovernmentApiService {

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Call external government API (GET request)
     *
     * <p>Generic method that handles:</p>
     * <ul>
     *   <li>SSL verification disabling (for self-signed certs)</li>
     *   <li>URL building with query parameters</li>
     *   <li>HTTP call execution</li>
     *   <li>Response parsing (JSON to Map)</li>
     *   <li>Error handling ("no" response detection)</li>
     * </ul>
     *
     * @param baseUrl Base URL of external API
     * @param params Query parameters (key-value pairs)
     * @param notFoundFlag Flag name for "not found" case (e.g., "has_disability")
     * @param serviceName Service name for logging
     * @return API response map
     */
    protected Map<String, Object> callExternalApi(String baseUrl, Map<String, String> params,
                                                   String notFoundFlag, String serviceName) {
        log.debug("Calling external API - Service: {}, URL: {}", serviceName, baseUrl);

        try {
            // Disable SSL verification (government APIs may have self-signed certs)
            disableSslVerification();

            // Build URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
            if (params != null) {
                params.forEach(builder::queryParam);
            }
            String url = builder.toUriString();

            log.debug("Full URL: {}", url);

            // Call external API
            String response = restTemplate.getForObject(url, String.class);

            log.debug("External API response: {}", response);

            // Check if response is "no" (not found / invalid data)
            if (isNoResponse(response)) {
                log.info("{} - Data not found or invalid", serviceName);

                if (notFoundFlag != null) {
                    // Return success with false flag (e.g., has_disability: false)
                    return buildFlagResponse(notFoundFlag, false);
                } else {
                    // Return error response
                    return errorResponse("not_found", "Data not found");
                }
            }

            // Parse JSON response
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(response, LinkedHashMap.class);
            data.put("success", true);

            if (notFoundFlag != null) {
                data.put(notFoundFlag, true);
            }

            log.info("{} - Data retrieved successfully", serviceName);
            return data;

        } catch (Exception e) {
            log.error("{} - External API call failed: {}", serviceName, e.getMessage(), e);
            return errorResponse("service_not_available", serviceName + " not available");
        }
    }

    /**
     * Call external government API with POST method
     *
     * @param baseUrl Base URL
     * @param params Query parameters
     * @param body Request body
     * @param notFoundFlag Flag name for not found case
     * @param serviceName Service name for logging
     * @return API response map
     */
    protected Map<String, Object> callExternalApiPost(String baseUrl, Map<String, String> params,
                                                       Object body, String notFoundFlag, String serviceName) {
        log.debug("Calling external API (POST) - Service: {}, URL: {}", serviceName, baseUrl);

        try {
            disableSslVerification();

            // Build URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
            if (params != null) {
                params.forEach(builder::queryParam);
            }
            String url = builder.toUriString();

            // Create request entity
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

            // Call external API
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            String response = responseEntity.getBody();

            if (isNoResponse(response)) {
                log.info("{} - Data not found or invalid", serviceName);
                return notFoundFlag != null ? buildFlagResponse(notFoundFlag, false) : errorResponse("not_found", "Data not found");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(response, LinkedHashMap.class);
            data.put("success", true);

            if (notFoundFlag != null) {
                data.put(notFoundFlag, true);
            }

            log.info("{} - Data retrieved successfully", serviceName);
            return data;

        } catch (Exception e) {
            log.error("{} - External API call failed: {}", serviceName, e.getMessage(), e);
            return errorResponse("service_not_available", serviceName + " not available");
        }
    }

    /**
     * Check if response indicates "not found" or "invalid"
     *
     * <p>Government APIs often return string "no" or "No" when data not found</p>
     *
     * @param response Response string
     * @return true if response is "no"
     */
    protected boolean isNoResponse(String response) {
        return response != null && response.replaceAll("[^a-zA-Z0-9]", "").equalsIgnoreCase("no");
    }

    /**
     * Build success response with boolean flag
     *
     * <p>Used for checks like: has_disability, in_poverty_register, etc.</p>
     *
     * @param flagName Flag name
     * @param value Flag value (true/false)
     * @return Success response map
     */
    protected Map<String, Object> buildFlagResponse(String flagName, boolean value) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put(flagName, value);
        return response;
    }

    /**
     * Build error response
     *
     * <p>Standard error format for all government services</p>
     *
     * @param code Error code (e.g., "service_not_available", "not_found", "invalid_captcha")
     * @param message Error message
     * @return Error response map
     */
    protected Map<String, Object> errorResponse(String code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("code", code);
        error.put("message", message);
        return error;
    }

    /**
     * Build success response (generic)
     *
     * @param data Data to include in response
     * @return Success response map
     */
    protected Map<String, Object> successResponse(Map<String, Object> data) {
        data.put("success", true);
        return data;
    }

    /**
     * Disable SSL certificate verification
     *
     * <p><strong>WARNING:</strong> This is insecure!</p>
     * <p>Only use for government APIs with self-signed certificates</p>
     * <p>In production, add government certificate to Java truststore:</p>
     * <pre>
     * keytool -import -alias gov-api -file cert.crt -keystore $JAVA_HOME/lib/security/cacerts
     * </pre>
     *
     * <p><strong>OPTIMIZATION:</strong> Centralized SSL handling (one place, not duplicated)</p>
     */
    protected void disableSslVerification() {
        try {
            // Create trust manager that trusts all certificates
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

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Disable hostname verification
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Failed to disable SSL verification", e);
        }
    }

    /**
     * Validate captcha (stub implementation)
     *
     * <p>TODO: Integrate with actual captcha service</p>
     * <p>For now, accepts any non-empty value</p>
     *
     * @param captchaId Captcha ID
     * @param captchaValue User's captcha answer
     * @return true if valid
     */
    protected boolean validateCaptcha(String captchaId, String captchaValue) {
        // TODO: Implement actual captcha validation with captcha service
        // For now, simple check: non-empty value
        boolean valid = captchaValue != null && !captchaValue.isEmpty();

        if (!valid) {
            log.warn("Captcha validation failed - ID: {}, Value: {}", captchaId, captchaValue);
        }

        return valid;
    }

    /**
     * Build query parameters map
     *
     * <p>Helper method to build parameter map for external API calls</p>
     *
     * @return New HashMap for parameters
     */
    protected Map<String, String> params() {
        return new HashMap<>();
    }

    /**
     * Add parameter to map (builder pattern)
     *
     * @param params Parameters map
     * @param key Parameter key
     * @param value Parameter value
     * @return Parameters map (for chaining)
     */
    protected Map<String, String> addParam(Map<String, String> params, String key, String value) {
        if (value != null) {
            params.put(key, value);
        }
        return params;
    }
}
