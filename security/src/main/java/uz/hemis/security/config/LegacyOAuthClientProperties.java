package uz.hemis.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Legacy OAuth client properties (OLD-HEMIS compatibility).
 *
 * <p>Holds Basic auth client credentials used by 200+ legacy clients.</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "hemis.security.oauth")
public class LegacyOAuthClientProperties {

    /**
     * Client identifier (default: client).
     */
    private String clientId = "client";

    /**
     * Client secret (default: secret).
     */
    private String clientSecret = "secret";

    /**
     * Scope returned in OAuth responses (default: rest-api).
     */
    private String scope = "rest-api";
}
