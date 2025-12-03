package uz.hemis.service.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * HEMIS API Configuration Properties
 *
 * <p><strong>Best Practice:</strong> All external API credentials from environment variables</p>
 *
 * <p><strong>.env Configuration:</strong></p>
 * <pre>
 * # HEMIS API (api.hemis.uz) Configuration
 * HEMIS_API_BASE_URL=https://api.hemis.uz
 * HEMIS_API_USERNAME=hemis_integration
 * HEMIS_API_PASSWORD=your-secret-password
 * HEMIS_API_TIMEOUT=30
 * </pre>
 *
 * <p><strong>Or application.yml:</strong></p>
 * <pre>
 * hemis:
 *   api:
 *     base-url: ${HEMIS_API_BASE_URL:https://api.hemis.uz}
 *     username: ${HEMIS_API_USERNAME:}
 *     password: ${HEMIS_API_PASSWORD:}
 *     timeout: ${HEMIS_API_TIMEOUT:30}
 * </pre>
 *
 * <p><strong>Authentication Flow:</strong></p>
 * <ol>
 *   <li>POST /api/user/auth with username/password</li>
 *   <li>Receive JWT token in response</li>
 *   <li>Use token for subsequent API calls</li>
 *   <li>Token valid for ~23 hours, auto-refresh when expired</li>
 * </ol>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "hemis.api")
@Validated
@Getter
@Setter
public class HemisApiProperties {

    /**
     * Base URL for api.hemis.uz
     * Default: https://api.hemis.uz
     */
    private String baseUrl = "https://api.hemis.uz";

    /**
     * API Username for authentication
     * <p><strong>CRITICAL:</strong> Must be set in environment, NEVER commit to code!</p>
     */
    private String username = "";

    /**
     * API Password for authentication
     * <p><strong>CRITICAL:</strong> Must be set in environment, NEVER commit to code!</p>
     */
    private String password = "";

    /**
     * Request timeout in seconds
     * Default: 30 seconds
     */
    private int timeout = 30;

    /**
     * Check if API credentials are properly configured
     */
    public boolean isConfigured() {
        return username != null && !username.isEmpty()
                && password != null && !password.isEmpty();
    }
}
