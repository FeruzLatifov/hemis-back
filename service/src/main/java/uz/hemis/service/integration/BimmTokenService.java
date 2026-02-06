package uz.hemis.service.integration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.common.port.cache.DistributedCachePort;

import javax.net.ssl.*;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;

/**
 * BIMM Token Service
 * <p>
 * BIMM (api-mspd.edu.uz) API uchun OAuth2 token olish va saqlash xizmati.
 * Tokenlar Redis da cache qilinadi (10 kun).
 * </p>
 *
 * <p>Old-hemis pattern: MyTokenServiceBean.getBimmToken()</p>
 * <ul>
 *   <li>OAuth2 password grant: POST https://api-mspd.edu.uz/auth/token</li>
 *   <li>Form body: grant_type=&amp;username=X&amp;password=Y&amp;scope=&amp;client_id=&amp;client_secret=</li>
 *   <li>Token TTL: 864000 seconds (10 days)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BimmTokenService {

    private final DistributedCachePort cachePort;

    private static final String CACHE_KEY = "bimm:oauth2:token";
    private static final Duration TOKEN_TTL = Duration.ofSeconds(864000); // 10 days

    @Value("${hemis.integration.bimm.oauth2.url:https://api-mspd.edu.uz/auth/token}")
    private String oauth2Url;

    @Value("${hemis.integration.bimm.oauth2.username:}")
    private String username;

    @Value("${hemis.integration.bimm.oauth2.password:}")
    private String password;

    @PostConstruct
    void validateCredentials() {
        if (username == null || username.isBlank()) {
            log.warn("BIMM OAuth2 username is not configured (hemis.integration.bimm.oauth2.username)");
        }
        if (password == null || password.isBlank()) {
            log.warn("BIMM OAuth2 password is not configured (hemis.integration.bimm.oauth2.password)");
        }
    }

    /**
     * Get BIMM OAuth2 token
     * <p>
     * Token is cached in Redis for 10 days (864000 seconds).
     * If cache is expired or empty, fetches new token from BIMM OAuth2 API.
     * </p>
     *
     * <p>Old-hemis format: grant_type=&amp;username=X&amp;password=Y&amp;scope=&amp;client_id=&amp;client_secret=</p>
     *
     * @return OAuth2 access token
     */
    @SuppressWarnings("unchecked")
    public String getToken() {
        // 1. Try to get from cache
        String cachedToken = cachePort.<String>retrieve(CACHE_KEY).orElse(null);
        if (cachedToken != null && !cachedToken.isEmpty()) {
            log.debug("Using cached BIMM token");
            return cachedToken;
        }

        // 2. Fetch new token from BIMM OAuth2 API
        // Uses HttpsURLConnection directly (same as old-hemis MyTokenServiceBean.oauth2Token)
        log.info("Fetching new BIMM OAuth2 token from: {}", oauth2Url);

        try {
            // Disable SSL verification (old-hemis MyRequest does this per-request)
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            // Old-hemis format: form-urlencoded with empty grant_type, scope, client_id, client_secret
            String body = String.format(
                    "grant_type=&username=%s&password=%s&scope=&client_id=&client_secret=",
                    username, password
            );

            URL urlObj = new URL(oauth2Url);
            HttpsURLConnection conn = (HttpsURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                String responseStr = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();

                // Parse response — old-hemis uses Gson getBodyAsMap(), we use Jackson
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> responseBody = mapper.readValue(responseStr, Map.class);

                if (responseBody != null && responseBody.containsKey("access_token")) {
                    String accessToken = responseBody.get("access_token").toString();
                    // 3. Cache token
                    cachePort.store(CACHE_KEY, accessToken, TOKEN_TTL);
                    log.info("BIMM OAuth2 token fetched and cached (TTL: {} seconds)", TOKEN_TTL.getSeconds());
                    return accessToken;
                } else {
                    log.error("BIMM OAuth2 response missing access_token");
                    return null;
                }
            } else {
                conn.disconnect();
                log.error("BIMM OAuth2 request failed with status: {}", statusCode);
                return null;
            }

        } catch (Exception e) {
            log.error("Error fetching BIMM OAuth2 token", e);
            return null;
        }
    }

    /**
     * Invalidate cached token (force refresh on next request)
     */
    public void invalidateToken() {
        cachePort.delete(CACHE_KEY);
        log.info("BIMM token cache invalidated");
    }
}
