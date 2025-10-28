package uz.hemis.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Security Configuration Properties
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Externalize security configuration</li>
 *   <li>OAuth2/JWT settings</li>
 *   <li>CORS allowed origins</li>
 *   <li>Rate limiting settings</li>
 * </ul>
 *
 * <p><strong>Configuration in application.properties:</strong></p>
 * <pre>
 * # OAuth2 Resource Server
 * security.oauth2.issuer-uri=https://auth.hemis.uz/realms/hemis
 * security.oauth2.jwk-set-uri=https://auth.hemis.uz/realms/hemis/protocol/openid-connect/certs
 *
 * # CORS
 * security.cors.allowed-origins=https://university1.uz,https://university2.uz,https://admin.hemis.uz
 *
 * # Rate Limiting
 * security.rate-limit.enabled=true
 * security.rate-limit.requests-per-minute=100
 * security.rate-limit.burst-capacity=200
 * </pre>
 *
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {

    // =====================================================
    // OAuth2 / JWT Configuration
    // =====================================================

    /**
     * OAuth2 authorization server settings
     */
    private OAuth2 oauth2 = new OAuth2();

    @Getter
    @Setter
    public static class OAuth2 {
        /**
         * OAuth2 issuer URI
         * Example: https://auth.hemis.uz/realms/hemis
         */
        private String issuerUri;

        /**
         * JWK Set URI for JWT signature validation
         * Example: https://auth.hemis.uz/realms/hemis/protocol/openid-connect/certs
         */
        private String jwkSetUri;

        /**
         * JWT token expiration time in seconds
         * Default: 3600 (1 hour)
         */
        private int tokenExpirationSeconds = 3600;

        /**
         * Refresh token expiration time in seconds
         * Default: 86400 (24 hours)
         */
        private int refreshTokenExpirationSeconds = 86400;
    }

    // =====================================================
    // CORS Configuration
    // =====================================================

    /**
     * CORS settings
     */
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Cors {
        /**
         * Allowed origins for CORS
         * Example: https://university1.uz, https://university2.uz, https://admin.hemis.uz
         */
        private List<String> allowedOrigins = new ArrayList<>();

        /**
         * Enable CORS
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Max age for preflight requests in seconds
         * Default: 3600 (1 hour)
         */
        private long maxAge = 3600;
    }

    // =====================================================
    // Rate Limiting Configuration
    // =====================================================

    /**
     * Rate limiting settings
     */
    private RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class RateLimit {
        /**
         * Enable rate limiting
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Requests per minute per university
         * Default: 100
         */
        private int requestsPerMinute = 100;

        /**
         * Burst capacity (max requests in short time)
         * Default: 200
         */
        private int burstCapacity = 200;

        /**
         * Global rate limit (all universities combined)
         * Default: 1000 requests per minute
         */
        private int globalRequestsPerMinute = 1000;
    }

    // =====================================================
    // API Security Configuration
    // =====================================================

    /**
     * API security settings
     */
    private Api api = new Api();

    @Getter
    @Setter
    public static class Api {
        /**
         * Require HTTPS in production
         * Default: true
         */
        private boolean requireHttps = true;

        /**
         * Enable request logging
         * Default: true
         */
        private boolean logRequests = true;

        /**
         * Enable response logging
         * Default: false (may contain sensitive data)
         */
        private boolean logResponses = false;

        /**
         * Maximum request size in bytes
         * Default: 10MB
         */
        private long maxRequestSize = 10_485_760; // 10 MB
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Get JWK Set URI with fallback to issuer-uri
     */
    public String getJwkSetUri() {
        if (oauth2.getJwkSetUri() != null && !oauth2.getJwkSetUri().isEmpty()) {
            return oauth2.getJwkSetUri();
        }

        // Fallback: construct from issuer-uri
        if (oauth2.getIssuerUri() != null && !oauth2.getIssuerUri().isEmpty()) {
            return oauth2.getIssuerUri() + "/protocol/openid-connect/certs";
        }

        throw new IllegalStateException("Either security.oauth2.jwk-set-uri or security.oauth2.issuer-uri must be configured");
    }

    /**
     * Get allowed origins with fallback to empty list
     */
    public List<String> getAllowedOrigins() {
        if (cors.getAllowedOrigins().isEmpty()) {
            // Development fallback
            return List.of("http://localhost:3000", "http://localhost:8080");
        }
        return cors.getAllowedOrigins();
    }

    /**
     * Check if rate limiting is enabled
     */
    public boolean isRateLimitEnabled() {
        return rateLimit.isEnabled();
    }

    /**
     * Check if HTTPS is required
     */
    public boolean isHttpsRequired() {
        return api.isRequireHttps();
    }
}
