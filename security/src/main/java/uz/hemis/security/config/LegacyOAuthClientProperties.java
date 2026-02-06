package uz.hemis.security.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Legacy OAuth client properties (OLD-HEMIS compatibility).
 *
 * <p>Holds Basic auth client credentials used by 200+ legacy clients.</p>
 *
 * <p>CRITICAL: clientId and clientSecret MUST be configured via
 * environment variables or application-{profile}.yml.
 * The application will fail to start if they are blank.</p>
 */
@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "hemis.security.oauth")
public class LegacyOAuthClientProperties {

    /**
     * Client identifier — MUST be set via configuration.
     */
    @NotBlank(message = "hemis.security.oauth.client-id must be configured (env: OAUTH_CLIENT_ID)")
    private String clientId = "";

    /**
     * Client secret — MUST be set via configuration.
     */
    @NotBlank(message = "hemis.security.oauth.client-secret must be configured (env: OAUTH_CLIENT_SECRET)")
    private String clientSecret = "";

    /**
     * Scope returned in OAuth responses (default: rest-api).
     */
    private String scope = "rest-api";
}
