package uz.hemis.api.legacy.controller;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.security.config.LegacyOAuthClientProperties;
import uz.hemis.security.service.TokenService;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Legacy OAuth2 Token Controller (OLD-HEMIS Compatibility)
 *
 * <p><strong>CRITICAL - OLD-HEMIS Integration:</strong></p>
 * <ul>
 *   <li>URL: POST /app/rest/v2/oauth/token - Asosiy endpoint</li>
 *   <li>URL: POST /app/rest/oauth/token - Legacy fallback (v2 yo'q)</li>
 *   <li>Authentication: Basic Y2xpZW50OnNlY3JldA== (client:secret)</li>
 *   <li>Grant Types: password, refresh_token</li>
 *   <li>Response: OLD-HEMIS JSON format (snake_case)</li>
 *   <li>Token muddati: 43199 sekund (12 soat - old-hemis format)</li>
 * </ul>
 *
 * <p><strong>Postman Collection compatibility:</strong></p>
 * <pre>
 * POST /app/rest/v2/oauth/token
 * Authorization: Basic Y2xpZW50OnNlY3JldA==
 * Content-Type: application/x-www-form-urlencoded
 *
 * grant_type=password&username=your_username&password=your_password
 * </pre>
 *
 * <p><strong>Response format (old-hemis):</strong></p>
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
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "01.Token",
    description = "OAuth2 autentifikatsiya - token olish, yangilash. " +
                  "Old-hemis loyihasidagi foydalanuvchilar uchun uzluksiz xizmat."
)
public class LegacyOAuthTokenController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final LegacyOAuthClientProperties oauthClientProperties;

    /**
     * Token olish (Password Grant) - Asosiy endpoint
     *
     * <p><strong>OLD-HEMIS URL:</strong> POST /app/rest/v2/oauth/token</p>
     *
     * <p><strong>So'rov (Password Grant):</strong></p>
     * <pre>
     * POST /app/rest/v2/oauth/token
     * Authorization: Basic Y2xpZW50OnNlY3JldA==
     * Content-Type: application/x-www-form-urlencoded
     *
     * grant_type=password&username=your_username&password=your_password
     * </pre>
     *
     * <p><strong>Javob:</strong></p>
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
     * @param authorization Basic auth header (Base64: client:secret)
     * @param grantType grant_type parametri (password yoki refresh_token)
     * @param username foydalanuvchi nomi (password grant uchun)
     * @param password parol (password grant uchun)
     * @param refreshToken yangilash tokeni (refresh_token grant uchun)
     * @return TokenResponse yoki xatolik
     */
    @Operation(
        summary = "Token olish",
        description = """
            Foydalanuvchi login va paroli yordamida TOKEN kalitini olish.

            **MUHIM:** Bu endpoint `application/x-www-form-urlencoded` formatida ishlaydi.
            Swagger UI dan test qilish uchun:
            1. "Try it out" tugmasini bosing
            2. Authorization header: `Basic Y2xpZW50OnNlY3JldA==`
            3. Request body (form-urlencoded):
               - grant_type=password
               - username=your_username
               - password=your_password

            **cURL misol:**
            ```bash
            curl -X POST "http://localhost:8081/app/rest/v2/oauth/token" \\
              -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \\
              -H "Content-Type: application/x-www-form-urlencoded" \\
              -d "grant_type=password&username=feruz&password=your_password"
            ```

            **Postman da:**
            - Method: POST
            - URL: /app/rest/v2/oauth/token
            - Headers: Authorization: Basic Y2xpZW50OnNlY3JldA==
            - Body: x-www-form-urlencoded
              - grant_type: password
              - username: your_username
              - password: your_password
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token muvaffaqiyatli yaratildi",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class),
                examples = @ExampleObject(
                    name = "Muvaffaqiyatli javob",
                    value = """
                        {
                          "access_token": "f1041fac-58cd-491a-a37d-212393a838f3",
                          "token_type": "bearer",
                          "refresh_token": "34583dda-9410-4410-95ff-cc0824656766",
                          "expires_in": 43199,
                          "scope": "rest-api"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Noto'g'ri so'rov - grant_type yoki parametrlar xato",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "invalid_request",
                          "error_description": "Username and password required"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Autentifikatsiya xatosi - login/parol yoki client noto'g'ri",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "invalid_grant",
                          "error_description": "Invalid username or password"
                        }
                        """
                )
            )
        )
    })
    @PostMapping(
        value = {
            "/app/rest/v2/oauth/token",  // Asosiy endpoint (old-hemis)
            "/app/rest/oauth/token"       // Legacy fallback (v2 yo'q)
        },
        consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<?> token(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "refresh_token", required = false) String refreshToken
    ) {
        log.info("Legacy token request - grant_type: {}, username: {}", grantType, username);

        try {
            // Client autentifikatsiyasini tekshirish (Basic auth)
            if (!validateClientCredentials(authorization)) {
                log.warn("Invalid client credentials");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("invalid_client", "Invalid client credentials"));
            }

            // Grant turlarini qayta ishlash
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
     * Password grant turini qayta ishlash
     *
     * @param username foydalanuvchi nomi
     * @param password parol
     * @return TokenResponse yoki xatolik
     */
    private ResponseEntity<?> handlePasswordGrant(String username, String password) {
        if (username == null || password == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse("invalid_request", "Username and password required"));
        }

        // Foydalanuvchini autentifikatsiya qilish
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Token generatsiya qilish
        TokenResponse tokenResponse = tokenService.generateToken(userDetails);

        // OLD-HEMIS token muddati: 2591998 sekund (30 kun - 2 sekund)
        // Old-hemis bilan birga bir: 30 * 24 * 60 * 60 - 2 = 2591998
        tokenResponse.setExpiresIn(2591998);

        log.info("Legacy token generated for user: {}", username);

        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Refresh token grant turini qayta ishlash
     *
     * @param refreshToken yangilash tokeni
     * @return TokenResponse yoki xatolik
     */
    private ResponseEntity<?> handleRefreshTokenGrant(String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse("invalid_request", "Refresh token required"));
        }

        try {
            // TokenService yordamida tokenni yangilash
            TokenResponse newToken = tokenService.refreshToken(refreshToken);

            // OLD-HEMIS token muddati: 2591998 sekund (30 kun - 2 sekund)
            // Old-hemis bilan birga bir: 30 * 24 * 60 * 60 - 2 = 2591998
            newToken.setExpiresIn(2591998);

            log.info("Legacy token refreshed successfully");

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
     * Client autentifikatsiyasini tekshirish (Basic auth)
     *
     * @param authorization Authorization header qiymati
     * @return true - to'g'ri, false - noto'g'ri
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

            return oauthClientProperties.getClientId().equals(clientId)
                    && oauthClientProperties.getClientSecret().equals(clientSecret);

        } catch (Exception e) {
            log.warn("Failed to parse Basic auth header", e);
            return false;
        }
    }

    /**
     * Xatolik javobini yaratish (OAuth2 format)
     *
     * @param error xatolik kodi
     * @param description xatolik tavsifi
     * @return xatolik map
     */
    private Map<String, String> errorResponse(String error, String description) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("error_description", description);
        return response;
    }
}
