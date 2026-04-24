package uz.hemis.security.service;

import lombok.Getter;

/**
 * OAuth 2.0 authentication error for B2B client_credentials grant flow.
 *
 * <p>Maps to RFC 6749 §5.2 error codes ({@code invalid_client},
 * {@code unsupported_grant_type}, {@code invalid_grant}, …).</p>
 *
 * <p>The {@link #oauthError} field lets the controller layer serialise the
 * error in OAuth-standard JSON without leaking internal state.</p>
 *
 * @since 2.1.0
 */
@Getter
public class OAuthClientAuthenticationException extends RuntimeException {

    private final String oauthError;

    public OAuthClientAuthenticationException(String oauthError, String description) {
        super(description);
        this.oauthError = oauthError;
    }

    public static OAuthClientAuthenticationException invalidClient(String description) {
        return new OAuthClientAuthenticationException("invalid_client", description);
    }

    public static OAuthClientAuthenticationException unsupportedGrant(String description) {
        return new OAuthClientAuthenticationException("unsupported_grant_type", description);
    }

    public static OAuthClientAuthenticationException invalidGrant(String description) {
        return new OAuthClientAuthenticationException("invalid_grant", description);
    }

    public static OAuthClientAuthenticationException invalidRequest(String description) {
        return new OAuthClientAuthenticationException("invalid_request", description);
    }

    /** RFC 6749 §5.2 — requested scope is invalid, unknown, or exceeds the granted scope. */
    public static OAuthClientAuthenticationException invalidScope(String description) {
        return new OAuthClientAuthenticationException("invalid_scope", description);
    }
}
