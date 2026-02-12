package uz.hemis.service.integration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.common.port.cache.DistributedCachePort;

import javax.net.ssl.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * GUVD Token Service
 * <p>
 * GUVD e-gov API uchun OAuth2 token olish va saqlash xizmati.
 * Tokenlar Redis da cache qilinadi (1 soat).
 * </p>
 *
 * <p><strong>Configuration (from .env):</strong></p>
 * <ul>
 *   <li>GUVD_OAUTH2_URL - OAuth2 token endpoint URL</li>
 *   <li>GUVD_CLIENT_ID - OAuth2 client ID</li>
 *   <li>GUVD_CLIENT_SECRET - OAuth2 client secret</li>
 *   <li>GUVD_USERNAME - GUVD API username</li>
 *   <li>GUVD_PASSWORD - GUVD API password</li>
 * </ul>
 *
 * @author HEMIS Backend Team
 * @since 2025-11-21
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuvdTokenService {

    private final DistributedCachePort cachePort;

    // Cache key
    private static final String CACHE_KEY = "guvd:oauth2:token";
    private static final Duration TOKEN_TTL = Duration.ofHours(1); // 1 hour cache

    // Configuration from .env
    @Value("${hemis.integration.guvd.oauth2.url:https://iskm.egov.uz:9444/oauth2/token}")
    private String oauth2Url;

    @Value("${hemis.integration.guvd.oauth2.client-id:}")
    private String clientId;

    @Value("${hemis.integration.guvd.oauth2.client-secret:}")
    private String clientSecret;

    @Value("${hemis.integration.guvd.oauth2.username:}")
    private String username;

    @Value("${hemis.integration.guvd.oauth2.password:}")
    private String password;

    @PostConstruct
    void validateCredentials() {
        if (clientId == null || clientId.isBlank()) {
            log.warn("GUVD OAuth2 client-id is not configured (hemis.integration.guvd.oauth2.client-id)");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            log.warn("GUVD OAuth2 client-secret is not configured (hemis.integration.guvd.oauth2.client-secret)");
        }
        if (username == null || username.isBlank()) {
            log.warn("GUVD OAuth2 username is not configured (hemis.integration.guvd.oauth2.username)");
        }
        if (password == null || password.isBlank()) {
            log.warn("GUVD OAuth2 password is not configured (hemis.integration.guvd.oauth2.password)");
        }
    }

    /**
     * Get GUVD OAuth2 token
     * <p>
     * Token is cached in Redis for 1 hour.
     * If cache is expired or empty, fetches new token from GUVD OAuth2 API.
     * </p>
     *
     * <p>Old-hemis pattern: MyTokenServiceBean.oauth2Token(url, client, secret, username, password)</p>
     *
     * @return OAuth2 access token
     */
    @SuppressWarnings("unchecked")
    public String getToken() {
        // 1. Try to get from cache
        String cachedToken = cachePort.<String>retrieve(CACHE_KEY).orElse(null);
        if (cachedToken != null && !cachedToken.isEmpty()) {
            log.debug("Using cached GUVD token");
            return cachedToken;
        }

        // 2. Fetch new token from GUVD OAuth2 API
        log.info("Fetching new GUVD OAuth2 token from: {}", oauth2Url);

        try {
            // Disable SSL verification (government APIs may have self-signed certs)
            if (oauth2Url.startsWith("https")) {
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
            }

            // Old-hemis pattern: Basic Auth + form-urlencoded
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            String body = String.format("grant_type=password&username=%s&password=%s", username, password);

            java.net.URL urlObj = URI.create(oauth2Url).toURL();
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Authorization", "Basic " + encodedCredentials);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                String responseStr = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> responseBody = mapper.readValue(responseStr, Map.class);

                if (responseBody != null && responseBody.containsKey("access_token")) {
                    String accessToken = responseBody.get("access_token").toString();
                    cachePort.store(CACHE_KEY, accessToken, TOKEN_TTL);
                    log.info("GUVD OAuth2 token fetched and cached (TTL: {} seconds)", TOKEN_TTL.getSeconds());
                    return accessToken;
                } else {
                    log.error("GUVD OAuth2 response missing access_token");
                    return null;
                }
            } else {
                conn.disconnect();
                log.error("GUVD OAuth2 request failed with status: {}", statusCode);
                return null;
            }

        } catch (Exception e) {
            log.error("Error fetching GUVD OAuth2 token", e);
            return null;
        }
    }

    /**
     * Invalidate cached token (force refresh on next request)
     */
    public void invalidateToken() {
        cachePort.delete(CACHE_KEY);
        log.info("🗑️ GUVD token cache invalidated");
    }
}
