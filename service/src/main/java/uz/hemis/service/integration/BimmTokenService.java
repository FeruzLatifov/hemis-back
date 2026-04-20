package uz.hemis.service.integration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.common.port.cache.DistributedCachePort;

import javax.net.ssl.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
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
    private final JdbcTemplate jdbcTemplate;

    private static final String CACHE_KEY = "bimm:oauth2:token";
    private static final String DB_TOKEN_KEY = "bimm-token";
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

        // 2. Try to get from database (old-hemis stores token in hemishe_s_settings)
        String dbToken = getTokenFromDatabase();
        if (dbToken != null && !dbToken.isEmpty()) {
            log.info("Using BIMM token from database (hemishe_s_settings)");
            cachePort.store(CACHE_KEY, dbToken, TOKEN_TTL);
            return dbToken;
        }

        // 3. Fetch new token from BIMM OAuth2 API
        // Uses HttpsURLConnection directly (same as old-hemis MyTokenServiceBean.oauth2Token)
        log.info("Fetching new BIMM OAuth2 token from: {}", oauth2Url);

        try {
            // Old-hemis format: form-urlencoded with empty grant_type, scope, client_id, client_secret
            String body = String.format(
                    "grant_type=&username=%s&password=%s&scope=&client_id=&client_secret=",
                    username, password
            );

            URL urlObj = URI.create(oauth2Url).toURL();
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            try {
                // Per-connection SSL bypass for government APIs with self-signed certs
                if (conn instanceof HttpsURLConnection httpsConn) {
                    SSLContext sc = uz.hemis.service.base.AbstractGovernmentApiService.getGovSslContextStatic();
                    httpsConn.setSSLSocketFactory(sc.getSocketFactory());
                    httpsConn.setHostnameVerifier((hostname, session) -> true);
                }
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
                    String responseStr;
                    try (java.io.InputStream is = conn.getInputStream()) {
                        responseStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }

                    // Parse response — old-hemis uses Gson getBodyAsMap(), we use Jackson
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> responseBody = mapper.readValue(responseStr, Map.class);

                    if (responseBody != null && responseBody.containsKey("access_token")) {
                        String accessToken = responseBody.get("access_token").toString();
                        // Cache token
                        cachePort.store(CACHE_KEY, accessToken, TOKEN_TTL);
                        log.info("BIMM OAuth2 token fetched and cached (TTL: {} seconds)", TOKEN_TTL.getSeconds());
                        return accessToken;
                    } else {
                        log.error("BIMM OAuth2 response missing access_token");
                        return null;
                    }
                } else {
                    log.error("BIMM OAuth2 request failed with status: {}", statusCode);
                    return null;
                }
            } finally {
                conn.disconnect();  // ✅ disconnect har qanday holatda (exception, success, non-200)
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

    /**
     * Get BIMM token from old-hemis database (hemishe_s_settings table).
     * Old-hemis stores the token with key 'bimm-token' and refreshes it daily.
     * This ensures hemis-back can use the same token as old-hemis during migration.
     */
    private String getTokenFromDatabase() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT value_ FROM hemishe_s_settings WHERE key_ = ? AND date_ > now() - interval '10 days'",
                    String.class, DB_TOKEN_KEY
            );
        } catch (Exception e) {
            log.debug("BIMM token not found in database: {}", e.getMessage());
            return null;
        }
    }
}
