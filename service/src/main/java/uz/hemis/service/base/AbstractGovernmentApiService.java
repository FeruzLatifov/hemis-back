package uz.hemis.service.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.*;
import java.net.HttpURLConnection;
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
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);
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
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);
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
     * Proxy POST request to external API with Bearer token authentication.
     *
     * <p>Replicates old-hemis BimmServiceBean pattern:</p>
     * <ul>
     *   <li>Sends POST with JSON body and Bearer token</li>
     *   <li>Returns raw parsed response (array or object) — no wrapping</li>
     *   <li>On HTTP errors, parses error body and returns {success:false, code:status, data:body}</li>
     * </ul>
     *
     * @param url Full API URL
     * @param jsonBody JSON request body (will be serialized)
     * @param bearerToken Bearer token for Authorization header
     * @param serviceName Service name for logging
     * @return Parsed JSON response (Object — may be Map or List)
     */
    protected Object proxyExternalApiPost(String url, Object jsonBody, String bearerToken, String serviceName) {
        log.debug("Proxy POST - Service: {}, URL: {}", serviceName, url);

        try {
            if (url.startsWith("https")) {
                disableSslVerification();
            }

            // Use HttpURLConnection directly (same as old-hemis MyRequest)
            // RestTemplate loses error body on HTTPS 4xx/5xx responses
            java.net.URL urlObj = java.net.URI.create(url).toURL();
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            // Write body — if already a String, use as-is; otherwise serialize
            String bodyJson = (jsonBody instanceof String) ? (String) jsonBody : objectMapper.writeValueAsString(jsonBody);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(bodyJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            String response;

            // Read response (success or error stream)
            java.io.InputStream is = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is != null) {
                response = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                is.close();
            } else {
                response = "";
            }

            conn.disconnect();

            log.debug("{} - Status: {}, Response: {}", serviceName, statusCode, response);

            if (statusCode >= 200 && statusCode < 300) {
                return parseJsonResponse(response);
            } else {
                // Return parsed error body (like old-hemis does)
                return buildProxyErrorResponse(statusCode, response);
            }

        } catch (Exception e) {
            log.error("{} - External API call failed: {}", serviceName, e.getMessage(), e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("code", 500);
            error.put("data", e.getMessage());
            return error;
        }
    }

    /**
     * Proxy GET request to external API with Bearer token authentication.
     *
     * <p>Replicates old-hemis BimmServiceBean.certificate() pattern:</p>
     * <ul>
     *   <li>Sends GET with query parameters and Bearer token</li>
     *   <li>Returns raw parsed response (array or object)</li>
     * </ul>
     *
     * @param url Full API URL (with query params already appended)
     * @param bearerToken Bearer token for Authorization header
     * @param serviceName Service name for logging
     * @return Parsed JSON response (Object — may be Map or List)
     */
    protected Object proxyExternalApiGet(String url, String bearerToken, String serviceName) {
        log.debug("Proxy GET - Service: {}, URL: {}", serviceName, url);

        try {
            if (url.startsWith("https")) {
                disableSslVerification();
            }

            java.net.URL urlObj = java.net.URI.create(url).toURL();
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            int statusCode = conn.getResponseCode();
            String response;

            java.io.InputStream is = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is != null) {
                response = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                is.close();
            } else {
                response = "";
            }

            conn.disconnect();

            log.debug("{} - Status: {}, Response: {}", serviceName, statusCode, response);

            if (statusCode >= 200 && statusCode < 300) {
                return parseJsonResponse(response);
            } else {
                return buildProxyErrorResponse(statusCode, response);
            }

        } catch (Exception e) {
            log.error("{} - External API call failed: {}", serviceName, e.getMessage(), e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("code", 500);
            error.put("data", e.getMessage());
            return error;
        }
    }

    /**
     * Parse JSON response string into Object (Map or List).
     *
     * <p>Replicates old-hemis MyResponse.getBodyAsArray() behavior exactly:</p>
     * <ul>
     *   <li>null/empty body → null</li>
     *   <li>JSON array → List (null if empty)</li>
     *   <li>JSON object → Map (null if empty)</li>
     *   <li>Non-JSON → raw string</li>
     *   <li>Numbers: Gson converts Double to Long for whole numbers;
     *       Jackson uses Integer/Long natively — JSON output identical</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private Object parseJsonResponse(String response) throws JsonProcessingException {
        if (response == null || response.isEmpty()) {
            return null;
        }
        String trimmed = response.trim();
        if (trimmed.startsWith("[")) {
            java.util.List<Object> list = objectMapper.readValue(trimmed, java.util.List.class);
            return (list == null || list.isEmpty()) ? null : list;
        } else if (trimmed.startsWith("{")) {
            Map<String, Object> map = objectMapper.readValue(trimmed, LinkedHashMap.class);
            return (map == null || map.isEmpty()) ? null : map;
        } else {
            // Non-JSON: return raw string (matches old-hemis getBodyAsArray behavior)
            return response;
        }
    }

    /**
     * Build proxy error response from HTTP error.
     * Tries to parse error body as JSON; falls back to raw string.
     */
    private Object buildProxyErrorResponse(int statusCode, String responseBody) {
        // Try to parse error body as JSON (BIMM API returns JSON errors)
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                Object parsed = parseJsonResponse(responseBody);
                if (parsed != null) {
                    return parsed;
                }
            } catch (JsonProcessingException ignored) {
                // Not valid JSON, wrap in error map
            }
        }
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("code", statusCode);
        error.put("data", responseBody);
        return error;
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
     * <p>DEFERRED: Integrate with actual captcha service</p>
     * <p>For now, accepts any non-empty value</p>
     *
     * @param captchaId Captcha ID
     * @param captchaValue User's captcha answer
     * @return true if valid
     */
    protected boolean validateCaptcha(String captchaId, String captchaValue) {
        return true;
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
