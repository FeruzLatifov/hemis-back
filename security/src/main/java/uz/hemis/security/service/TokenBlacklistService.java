package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token Blacklist Service
 *
 * <p><strong>Industry Best Practice - Token Revocation</strong></p>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Immediately revoke JWT tokens on logout</li>
 *   <li>Prevent reuse of logged-out tokens</li>
 *   <li>Redis-based distributed blacklist (scales horizontally)</li>
 *   <li>Automatic expiry (TTL = token expiry time)</li>
 * </ul>
 *
 * <p><strong>How It Works:</strong></p>
 * <pre>
 * User Logout:
 * 1. Extract token from request
 * 2. Parse token expiry time (exp claim)
 * 3. Add token to Redis blacklist with TTL
 * 4. Token automatically removed after expiry
 *
 * Request Authentication:
 * 1. Extract token from request
 * 2. Check if token in blacklist
 * 3. If blacklisted → reject (401 Unauthorized)
 * 4. If not blacklisted → continue authentication
 * </pre>
 *
 * <p><strong>Industry References:</strong></p>
 * <ul>
 *   <li>Google OAuth2: Token revocation endpoint</li>
 *   <li>Facebook Login: Logout API invalidates tokens</li>
 *   <li>AWS Cognito: Token revocation</li>
 *   <li>Auth0: Token blacklisting for logout</li>
 * </ul>
 *
 * <p><strong>Performance:</strong></p>
 * <ul>
 *   <li>Redis GET: ~1ms (distributed cache)</li>
 *   <li>Redis SET with TTL: ~1ms</li>
 *   <li>No database overhead</li>
 *   <li>Scales horizontally</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * Add token to blacklist
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>User logout</li>
     *   <li>Admin revokes user session</li>
     *   <li>Security incident response</li>
     * </ul>
     *
     * <p><strong>TTL Strategy:</strong></p>
     * <pre>
     * TTL = token expiry time - current time
     * - Token expires in 15 min → TTL = 15 min
     * - Token expires in 5 min → TTL = 5 min
     * - Token already expired → Don't add (no point)
     * </pre>
     *
     * @param jti JWT ID (unique token identifier)
     * @param expiryTime Token expiry time from 'exp' claim
     */
    public void addToBlacklist(String jti, Instant expiryTime) {
        if (jti == null || jti.isEmpty()) {
            log.warn("⚠️ Cannot blacklist token: JTI is null or empty");
            return;
        }

        // Calculate TTL (time until token expires)
        Instant now = Instant.now();
        long ttlSeconds = Duration.between(now, expiryTime).getSeconds();

        // Don't blacklist if token already expired
        if (ttlSeconds <= 0) {
            log.debug("Token already expired, no need to blacklist: jti={}", jti);
            return;
        }

        // Add to Redis with TTL
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "revoked", ttlSeconds, TimeUnit.SECONDS);

        log.info("✅ Token blacklisted: jti={}, TTL={}s", jti, ttlSeconds);
    }

    /**
     * Check if token is blacklisted
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Redis GET: O(1) complexity</li>
     *   <li>Average latency: ~1ms</li>
     *   <li>Called on every authenticated request</li>
     * </ul>
     *
     * @param jti JWT ID (unique token identifier)
     * @return true if token is blacklisted (should reject), false otherwise
     */
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }

        String key = BLACKLIST_PREFIX + jti;
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.warn("⚠️ Blacklisted token attempt: jti={}", jti);
            return true;
        }

        return false;
    }

    /**
     * Remove token from blacklist (rare use case)
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Admin reinstates revoked session</li>
     *   <li>Testing/debugging</li>
     * </ul>
     *
     * @param jti JWT ID (unique token identifier)
     */
    public void removeFromBlacklist(String jti) {
        if (jti == null || jti.isEmpty()) {
            return;
        }

        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.delete(key);

        log.info("Token removed from blacklist: jti={}", jti);
    }

    /**
     * Clear all blacklisted tokens (admin operation)
     *
     * <p><strong>WARNING:</strong> Use with caution!</p>
     * <ul>
     *   <li>Only for emergency situations</li>
     *   <li>Logged-out users can login again with old tokens</li>
     * </ul>
     */
    public void clearAllBlacklist() {
        redisTemplate.keys(BLACKLIST_PREFIX + "*")
            .forEach(redisTemplate::delete);

        log.warn("⚠️ ALL blacklisted tokens cleared (admin operation)");
    }
}
