package uz.hemis.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.security.service.TokenService;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Token Endpoint Controller
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>URL: POST /app/rest/v2/oauth/token (EXACT match)</li>
 *   <li>Authentication: Basic (client:secret)</li>
 *   <li>Grant Types: password, refresh_token</li>
 *   <li>Response: OLD-HEMIS JSON format</li>
 * </ul>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/oauth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "OAuth2 authentication: login (password grant), refresh token, logout")
public class OAuth2TokenController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    // Client credentials (OLD-HEMIS: client:secret)
    private static final String CLIENT_ID = "client";
    private static final String CLIENT_SECRET = "secret";

    /**
     * OAuth2 Token Endpoint
     *
     * <p><strong>OLD-HEMIS URL:</strong> POST /app/rest/v2/oauth/token</p>
     *
     * <p><strong>Request (Password Grant):</strong></p>
     * <pre>
     * POST /app/rest/v2/oauth/token
     * Authorization: Basic Y2xpZW50OnNlY3JldA==
     * Content-Type: application/x-www-form-urlencoded
     *
     * grant_type=password&username=admin&password=admin
     * </pre>
     *
     * <p><strong>Request (Refresh Token):</strong></p>
     * <pre>
     * POST /app/rest/v2/oauth/token
     * Authorization: Basic Y2xpZW50OnNlY3JldA==
     * Content-Type: application/x-www-form-urlencoded
     *
     * grant_type=refresh_token&refresh_token={uuid}
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "access_token": "f1041fac-58cd-491a-a37d-212393a838f3",
     *   "token_type": "bearer",
     *   "refresh_token": "34583dda-9410-4410-95ff-cc0824656766",
     *   "expires_in": 43199,
     *   "scope": "rest-api"
     * }
     * </pre>
     *
     * @param authorization Basic auth header
     * @param grantType grant_type parameter (password or refresh_token)
     * @param username username (for password grant)
     * @param password password (for password grant)
     * @param refreshToken refresh_token (for refresh_token grant)
     * @return TokenResponse or error
     */
    @Operation(
        summary = "OAuth2 Token Endpoint",
        description = """
            Get access token using OAuth2 password grant or refresh token grant.

            **Password Grant:**
            - grant_type=password
            - username=your_username
            - password=your_password

            **Refresh Token Grant:**
            - grant_type=refresh_token
            - refresh_token=your_refresh_token

            **Authorization Header:**
            - Basic Y2xpZW50OnNlY3JldA== (client:secret)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "access_token": "eyJhbGciOiJIUzI1NiJ9...",
                      "refresh_token": "eyJhbGciOiJIUzI1NiJ9...",
                      "token_type": "Bearer",
                      "expires_in": 86400
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid grant type or missing parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials or client authentication failed")
    })
    @PostMapping(value = "/token", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> token(
            @Parameter(description = "Basic authentication (Base64: client:secret)", example = "Basic Y2xpZW50OnNlY3JldA==")
            @RequestHeader(value = "Authorization", required = false) String authorization,

            @Parameter(description = "Grant type", example = "password", required = true)
            @RequestParam("grant_type") String grantType,

            @Parameter(description = "Username (for password grant)", example = "admin")
            @RequestParam(value = "username", required = false) String username,

            @Parameter(description = "Password (for password grant)", example = "admin")
            @RequestParam(value = "password", required = false) String password,

            @Parameter(description = "Refresh token (for refresh_token grant)")
            @RequestParam(value = "refresh_token", required = false) String refreshToken
    ) {
        log.info("Token request - grant_type: {}, username: {}", grantType, username);

        try {
            // Validate client credentials (Basic auth)
            if (!validateClientCredentials(authorization)) {
                log.warn("Invalid client credentials");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("invalid_client", "Invalid client credentials"));
            }

            // Handle grant types
            if ("password".equals(grantType)) {
                return handlePasswordGrant(username, password);
            } else if ("refresh_token".equals(grantType)) {
                return handleRefreshTokenGrant(refreshToken);
            } else {
                log.warn("Unsupported grant type: {}", grantType);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorResponse("unsupported_grant_type", "Grant type not supported: " + grantType));
            }

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {}", username);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("invalid_grant", "Invalid username or password"));

        } catch (Exception e) {
            log.error("Token generation error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("server_error", "Internal server error"));
        }
    }

    /**
     * Handle password grant type
     *
     * @param username username
     * @param password password
     * @return TokenResponse
     */
    private ResponseEntity<?> handlePasswordGrant(String username, String password) {
        if (username == null || password == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse("invalid_request", "Username and password required"));
        }

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generate token
        TokenResponse tokenResponse = tokenService.generateToken(userDetails);

        log.info("Token generated for user: {}", username);

        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Handle refresh token grant type
     *
     * @param refreshToken refresh token JWT
     * @return TokenResponse
     */
    private ResponseEntity<?> handleRefreshTokenGrant(String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse("invalid_request", "Refresh token required"));
        }

        try {
            // Validate and refresh token using TokenService
            TokenResponse newToken = tokenService.refreshToken(refreshToken);

            log.info("Token refreshed successfully");

            return ResponseEntity.ok(newToken);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid or expired refresh token");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("invalid_grant", "Invalid or expired refresh token"));
        } catch (Exception e) {
            log.error("Refresh token error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("server_error", "Internal server error during token refresh"));
        }
    }

    /**
     * Validate client credentials from Basic auth header
     *
     * @param authorization Authorization header value
     * @return true if valid
     */
    private boolean validateClientCredentials(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return false;
        }

        try {
            String base64Credentials = authorization.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":", 2);

            if (parts.length != 2) {
                return false;
            }

            String clientId = parts[0];
            String clientSecret = parts[1];

            return CLIENT_ID.equals(clientId) && CLIENT_SECRET.equals(clientSecret);

        } catch (Exception e) {
            log.warn("Failed to parse Basic auth header", e);
            return false;
        }
    }

    /**
     * Build error response (OAuth2 format)
     *
     * @param error error code
     * @param description error description
     * @return error map
     */
    private Map<String, String> errorResponse(String error, String description) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("error_description", description);
        return response;
    }
}
