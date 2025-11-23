package uz.hemis.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
@Slf4j
public class GuvdTokenService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Constructor with @Qualifier to resolve RedisTemplate bean ambiguity
     *
     * @param restTemplate RestTemplate for HTTP calls
     * @param redisTemplate Redis template (stringRedisTemplate bean)
     */
    public GuvdTokenService(
            RestTemplate restTemplate,
            @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    // Redis cache key
    private static final String REDIS_KEY = "guvd:oauth2:token";
    private static final Duration TOKEN_TTL = Duration.ofHours(1); // 1 hour cache

    // Configuration from .env
    @Value("${hemis.integration.guvd.oauth2.url:https://iskm.egov.uz:9444/oauth2/token}")
    private String oauth2Url;

    @Value("${hemis.integration.guvd.oauth2.client-id:lfZJDHdJm9Sw7UNsI0uUJzLZp9Ea}")
    private String clientId;

    @Value("${hemis.integration.guvd.oauth2.client-secret:s3cJ2Q3t0zswsMNXzE6nIh3KV8Ua}")
    private String clientSecret;

    @Value("${hemis.integration.guvd.oauth2.username:minvuz_user1}")
    private String username;

    @Value("${hemis.integration.guvd.oauth2.password:m1nvuz!@#}")
    private String password;

    /**
     * Get GUVD OAuth2 token
     * <p>
     * Token is cached in Redis for 1 hour.
     * If cache is expired or empty, fetches new token from GUVD OAuth2 API.
     * </p>
     *
     * @return OAuth2 access token
     */
    public String getToken() {
        // 1. Try to get from cache
        String cachedToken = redisTemplate.opsForValue().get(REDIS_KEY);
        if (cachedToken != null && !cachedToken.isEmpty()) {
            log.debug("✅ Using cached GUVD token");
            return cachedToken;
        }

        // 2. Fetch new token from GUVD OAuth2 API
        log.info("🔄 Fetching new GUVD OAuth2 token from: {}", oauth2Url);

        try {
            // Build Basic Auth header
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedCredentials);

            // Build form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("username", username);
            formData.add("password", password);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

            // Call OAuth2 endpoint
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    oauth2Url,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String accessToken = (String) responseBody.get("access_token");

                if (accessToken != null && !accessToken.isEmpty()) {
                    // 3. Cache token in Redis
                    redisTemplate.opsForValue().set(REDIS_KEY, accessToken, TOKEN_TTL);
                    log.info("✅ GUVD OAuth2 token fetched and cached (TTL: {} seconds)", TOKEN_TTL.getSeconds());
                    return accessToken;
                } else {
                    log.error("❌ GUVD OAuth2 response missing access_token");
                    return null;
                }
            } else {
                log.error("❌ GUVD OAuth2 request failed with status: {}", response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Error fetching GUVD OAuth2 token", e);
            return null;
        }
    }

    /**
     * Invalidate cached token (force refresh on next request)
     */
    public void invalidateToken() {
        redisTemplate.delete(REDIS_KEY);
        log.info("🗑️ GUVD token cache invalidated");
    }
}
