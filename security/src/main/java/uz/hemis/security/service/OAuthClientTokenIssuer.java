package uz.hemis.security.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.domain.entity.security.OAuthClient;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Reusable {@code grant_type=client_credentials} handler.
 *
 * <p>Lives in {@code security} so module-specific controllers
 * ({@code api-university}, {@code api-external}, …) can expose the OAuth token
 * endpoint at audience-specific URLs without duplicating the critical auth logic.</p>
 *
 * <p>Responsibilities:</p>
 * <ol>
 *   <li>Parse the {@code Authorization: Basic} header → {@code (client_id, client_secret)}</li>
 *   <li>Authenticate via {@link OAuthClientAuthenticationService} (covers IP whitelist, secret, grant)</li>
 *   <li>Issue JWT via {@link TokenService#issueClientToken(OAuthClient)}</li>
 *   <li>Map {@link OAuthClientAuthenticationException} → RFC 6749 §5.2 error responses</li>
 * </ol>
 *
 * <p>Controllers should be thin: parse HTTP, delegate here, return.</p>
 *
 * @since 2.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthClientTokenIssuer {

    static final String GRANT_CLIENT_CREDENTIALS = "client_credentials";
    /** Per-IP rate limit prefix — separate from login limit. */
    private static final String OAUTH_RATE_LIMIT_PREFIX = "ratelimit:oauth:";
    /** Per-IP token request limit (OWASP A07 brute-force protection). */
    private static final int OAUTH_MAX_ATTEMPTS = 60;
    private static final long OAUTH_WINDOW_MINUTES = 15;

    private final OAuthClientAuthenticationService authenticationService;
    private final TokenService tokenService;
    private final RateLimitService rateLimitService;
    private final uz.hemis.security.util.ClientIpResolver clientIpResolver;

    /**
     * Issue a machine token for the supplied request.
     *
     * @param authorizationHeader raw {@code Authorization} header value
     * @param grantType requested grant — must equal {@code client_credentials}
     * @param requestedScope optional space-delimited scope (RFC 6749 §3.3) — narrows
     *                       the issued token to a subset of the client's granted scopes;
     *                       {@code null} / blank → token covers full granted scope
     * @param request servlet request (used to derive caller IP)
     * @return OAuth-compliant {@link ResponseEntity} (token on 200, RFC error otherwise)
     */
    public ResponseEntity<?> issue(String authorizationHeader,
                                   String grantType,
                                   String requestedScope,
                                   HttpServletRequest request) {
        String remoteIp = clientIpResolver.resolve(request);

        // OWASP A07 (audit P0.E2) — per-IP rate limit BEFORE auth.
        // Prevents brute-force of client_id/secret pairs by repeated requests.
        if (!rateLimitService.isAllowed(
                OAUTH_RATE_LIMIT_PREFIX, remoteIp, OAUTH_MAX_ATTEMPTS, OAUTH_WINDOW_MINUTES)) {
            log.warn("OAuth token endpoint rate limit exceeded: ip={}", remoteIp);
            return error(HttpStatus.TOO_MANY_REQUESTS, "rate_limited",
                    "Too many token requests. Retry after the rate-limit window resets.");
        }

        if (grantType == null || grantType.isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "invalid_request", "grant_type is required");
        }
        if (!GRANT_CLIENT_CREDENTIALS.equals(grantType)) {
            return error(HttpStatus.BAD_REQUEST, "unsupported_grant_type",
                    "Only 'client_credentials' is supported on this endpoint");
        }

        String[] basic = parseBasicCredentials(authorizationHeader);
        if (basic == null) {
            return error(HttpStatus.UNAUTHORIZED, "invalid_client",
                    "Basic authorization header required");
        }

        try {
            OAuthClient client = authenticationService.authenticate(
                    basic[0], basic[1], grantType, remoteIp);
            TokenResponse token = tokenService.issueClientToken(client, requestedScope);
            return ResponseEntity.ok(token);
        } catch (OAuthClientAuthenticationException ex) {
            HttpStatus status = switch (ex.getOauthError()) {
                case "unsupported_grant_type", "invalid_scope", "invalid_request"
                        -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.UNAUTHORIZED;
            };
            return error(status, ex.getOauthError(), ex.getMessage());
        } catch (Exception ex) {
            log.error("client_credentials: unexpected error", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "server_error", "Internal server error");
        }
    }

    /**
     * Parse {@code Basic <base64>} header → {@code [clientId, clientSecret]} or {@code null}.
     */
    static String[] parseBasicCredentials(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(authorization.substring("Basic ".length())));
            String[] parts = decoded.split(":", 2);
            if (parts.length != 2 || parts[0].isBlank()) {
                return null;
            }
            return parts;
        } catch (Exception ex) {
            return null;
        }
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String code, String description) {
        Map<String, String> body = new HashMap<>(2);
        body.put("error", code);
        body.put("error_description", description);
        return ResponseEntity.status(status).body(body);
    }
}
