package uz.hemis.security.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

/**
 * Legacy OAuth client properties (OLD-HEMIS compatibility).
 *
 * <p>Holds Basic auth client credentials used by 200+ legacy clients
 * (Univer Yii2 PHP, CUBA legacy desktop, Postman).</p>
 *
 * <p><strong>Validation (non-blocking):</strong></p>
 * <ul>
 *   <li>Blank → boot fail ({@code @NotBlank}) — config error</li>
 *   <li>Weak default (e.g. {@code client/secret}, {@code admin/admin}) or short
 *       length in prod → <b>ERROR log</b> (boot continues so the 200+ legacy
 *       clients are not cut off). Rotate at the next maintenance window.</li>
 * </ul>
 *
 * <p>Background: prior {@code k8s-secret.env} template shipped with default
 * {@code OAUTH_CLIENT_ID=client / OAUTH_CLIENT_SECRET=secret} — an attacker
 * who reaches {@code POST /app/rest/v2/oauth/token} with Basic auth
 * {@code Basic Y2xpZW50OnNlY3JldA==} could begin a password-grant brute-force.
 * This guard surfaces the misconfiguration in logs without crashing the
 * legacy auth chain.</p>
 */
@Getter
@Setter
@Slf4j
@Component
@Validated
@ConfigurationProperties(prefix = "hemis.security.oauth")
public class LegacyOAuthClientProperties implements EnvironmentAware {

    /** Known weak/sample credential values — refused in prod profile. */
    private static final Set<String> WEAK_VALUES = Set.of(
        "client", "secret", "default", "changeme", "change-me",
        "admin", "password", "test", "demo", "example", "sample",
        "12345", "123456", "qwerty", "root", "hemis"
    );

    private static final int MIN_SECRET_LENGTH = 16;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /** Client identifier — MUST be set via configuration. */
    @NotBlank(message = "hemis.security.oauth.client-id must be configured (env: OAUTH_CLIENT_ID)")
    private String clientId = "";

    /** Client secret — MUST be set via configuration. */
    @NotBlank(message = "hemis.security.oauth.client-secret must be configured (env: OAUTH_CLIENT_SECRET)")
    private String clientSecret = "";

    /** Scope returned in OAuth responses (default: rest-api). */
    private String scope = "rest-api";

    /**
     * Non-blocking credential strength inspection. Auth chain continues
     * working in every profile; this only surfaces misconfiguration in logs
     * so operators can rotate at a maintenance window without an emergency.
     */
    @PostConstruct
    void validateCredentialStrength() {
        boolean isProd = isProdProfile();
        String idIssue = inspect("client-id", clientId, isProd);
        String secretIssue = inspect("client-secret", clientSecret, isProd);

        if (isProd && (idIssue != null || secretIssue != null)) {
            log.error("[SECURITY] Weak legacy OAuth credentials detected in PROD profile. {}"
                    + " — rotate OAUTH_CLIENT_ID / OAUTH_CLIENT_SECRET to non-default values "
                    + "(≥ {} chars, not in sample list). Auth chain remains active to avoid "
                    + "disrupting 200+ legacy clients; schedule a maintenance window.",
                joinIssues(idIssue, secretIssue),
                MIN_SECRET_LENGTH
            );
        }
    }

    private String inspect(String field, String value, boolean isProd) {
        String trimmed = value == null ? "" : value.trim();
        if (WEAK_VALUES.contains(trimmed.toLowerCase())) {
            String msg = "hemis.security.oauth." + field + "='" + trimmed
                + "' is a known weak/sample value";
            if (!isProd) {
                log.warn("[SECURITY] {} (dev profile — allowed, MUST rotate before prod)", msg);
            }
            return msg;
        }
        if (isProd && trimmed.length() < MIN_SECRET_LENGTH) {
            return "hemis.security.oauth." + field + " length "
                + trimmed.length() + " < recommended " + MIN_SECRET_LENGTH;
        }
        return null;
    }

    private String joinIssues(String a, String b) {
        if (a != null && b != null) return a + " ; " + b;
        return a != null ? a : (b != null ? b : "");
    }

    private boolean isProdProfile() {
        if (environment == null) {
            return false;  // plain `new` (unit test) — skip prod-specific guard
        }
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
