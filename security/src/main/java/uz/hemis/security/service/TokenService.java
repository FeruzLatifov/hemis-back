package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.TokenResponse;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Token Service - OAuth2 Token Management with Redis Storage
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Token format: UUID (same as OLD-HEMIS)</li>
 *   <li>Storage: Redis (same as OLD-HEMIS)</li>
 *   <li>Expiration: 43199 seconds / 12 hours (same as OLD-HEMIS)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Token validity (OLD-HEMIS: 43199 seconds = 12 hours - 1 second)
    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 43199;
    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 2592000; // 30 days

    // Redis key prefixes
    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    /**
     * Generate OAuth2 token pair (access + refresh)
     *
     * @param userDetails authenticated user details
     * @return TokenResponse (OLD-HEMIS format)
     */
    public TokenResponse generateToken(UserDetails userDetails) {
        log.info("Generating token for user: {}", userDetails.getUsername());

        // Generate UUIDs
        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        // Store access token in Redis
        Map<String, Object> accessTokenData = new HashMap<>();
        accessTokenData.put("username", userDetails.getUsername());
        accessTokenData.put("roles", extractRoles(userDetails));
        accessTokenData.put("scope", "rest-api");
        accessTokenData.put("token_type", "bearer");

        String accessKey = ACCESS_TOKEN_PREFIX + accessToken;
        redisTemplate.opsForHash().putAll(accessKey, accessTokenData);
        redisTemplate.expire(accessKey, ACCESS_TOKEN_VALIDITY_SECONDS, TimeUnit.SECONDS);

        log.debug("Access token stored in Redis: {} (expires in {} seconds)",
                  accessKey, ACCESS_TOKEN_VALIDITY_SECONDS);

        // Store refresh token in Redis
        Map<String, Object> refreshTokenData = new HashMap<>();
        refreshTokenData.put("username", userDetails.getUsername());
        refreshTokenData.put("access_token", accessToken);

        String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForHash().putAll(refreshKey, refreshTokenData);
        redisTemplate.expire(refreshKey, REFRESH_TOKEN_VALIDITY_SECONDS, TimeUnit.SECONDS);

        log.debug("Refresh token stored in Redis: {} (expires in {} days)",
                  refreshKey, REFRESH_TOKEN_VALIDITY_SECONDS / 86400);

        // Build response (OLD-HEMIS format)
        return TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("bearer")
                .refreshToken(refreshToken)
                .expiresIn((int) ACCESS_TOKEN_VALIDITY_SECONDS)
                .scope("rest-api")
                .build();
    }

    /**
     * Validate access token
     *
     * @param token access token UUID
     * @return true if valid and not expired
     */
    public boolean validateToken(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.debug("Token valid: {}", token);
            return true;
        }

        log.warn("Token invalid or expired: {}", token);
        return false;
    }

    /**
     * Get token details from Redis
     *
     * @param token access token UUID
     * @return token data map (username, roles, etc.)
     */
    public Map<Object, Object> getTokenData(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);

        if (data.isEmpty()) {
            log.warn("Token not found: {}", token);
            return Collections.emptyMap();
        }

        return data;
    }

    /**
     * Refresh access token using refresh token
     *
     * @param refreshToken refresh token UUID
     * @param userDetails user details for new token
     * @return new TokenResponse
     */
    public TokenResponse refreshToken(String refreshToken, UserDetails userDetails) {
        log.info("Refreshing token for user: {}", userDetails.getUsername());

        String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;

        // Check if refresh token exists
        Boolean exists = redisTemplate.hasKey(refreshKey);
        if (!Boolean.TRUE.equals(exists)) {
            log.warn("Refresh token invalid or expired: {}", refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Get old access token and revoke it
        Map<Object, Object> refreshData = redisTemplate.opsForHash().entries(refreshKey);
        String oldAccessToken = (String) refreshData.get("access_token");
        if (oldAccessToken != null) {
            revokeToken(oldAccessToken);
        }

        // Generate new token pair
        return generateToken(userDetails);
    }

    /**
     * Revoke token (delete from Redis)
     *
     * @param token access token UUID
     */
    public void revokeToken(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("Token revoked: {}", token);
        } else {
            log.warn("Token not found for revocation: {}", token);
        }
    }

    /**
     * Revoke all tokens for a user (logout from all devices)
     *
     * @param username username
     */
    public void revokeAllUserTokens(String username) {
        log.info("Revoking all tokens for user: {}", username);

        // Note: This requires scanning Redis keys
        // In production, consider maintaining a user→tokens mapping
        Set<String> keys = redisTemplate.keys(ACCESS_TOKEN_PREFIX + "*");

        if (keys != null) {
            keys.forEach(key -> {
                Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
                if (username.equals(data.get("username"))) {
                    redisTemplate.delete(key);
                    log.debug("Revoked token: {}", key);
                }
            });
        }
    }

    /**
     * Extract roles from UserDetails authorities
     *
     * @param userDetails user details
     * @return comma-separated roles string
     */
    private String extractRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
