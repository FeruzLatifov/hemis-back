package uz.hemis.security.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.security.service.TokenService;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * OAuth2 Token Controller Unit Tests
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>Password grant - success</li>
 *   <li>Password grant - invalid credentials</li>
 *   <li>Password grant - missing parameters</li>
 *   <li>Refresh token grant - success</li>
 *   <li>Refresh token grant - invalid token</li>
 *   <li>Refresh token grant - expired token</li>
 *   <li>Client authentication - success</li>
 *   <li>Client authentication - failure</li>
 *   <li>Invalid grant type</li>
 *   <li>Missing authorization header</li>
 * </ul>
 *
 * <p><strong>Testing Strategy:</strong></p>
 * <ul>
 *   <li>Unit tests using Mockito</li>
 *   <li>Mock AuthenticationManager and TokenService</li>
 *   <li>Verify all OAuth2 error codes</li>
 *   <li>Validate Basic authentication parsing</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2TokenController Unit Tests")
class OAuth2TokenControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private OAuth2TokenController controller;

    private static final String VALID_BASIC_AUTH = "Basic " + Base64.getEncoder().encodeToString("client:secret".getBytes());
    private static final String INVALID_BASIC_AUTH = "Basic " + Base64.getEncoder().encodeToString("wrong:wrong".getBytes());

    private UserDetails testUser;
    private TokenResponse testTokenResponse;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .username("admin")
                .password("admin")  // In real scenario, this would be BCrypt hashed
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER")
                ))
                .build();

        // Create test token response
        testTokenResponse = TokenResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test-access-token")
                .refreshToken("eyJhbGciOiJIUzI1NiJ9.test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(43200)
                .build();
    }

    // =====================================================
    // Password Grant Tests
    // =====================================================

    @Test
    @DisplayName("Password grant should return token on valid credentials")
    void passwordGrant_ValidCredentials_ShouldReturnToken() {
        // Given
        String grantType = "password";
        String username = "admin";
        String password = "admin";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.generateToken(any(UserDetails.class)))
                .thenReturn(testTokenResponse);

        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                grantType,
                username,
                password,
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(TokenResponse.class);

        TokenResponse tokenResponse = (TokenResponse) response.getBody();
        assertThat(tokenResponse.getAccessToken()).isEqualTo(testTokenResponse.getAccessToken());
        assertThat(tokenResponse.getRefreshToken()).isEqualTo(testTokenResponse.getRefreshToken());
        assertThat(tokenResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(tokenResponse.getExpiresIn()).isEqualTo(43200);

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Password grant should return 401 on invalid credentials")
    void passwordGrant_InvalidCredentials_ShouldReturn401() {
        // Given
        String grantType = "password";
        String username = "admin";
        String password = "wrong_password";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                grantType,
                username,
                password,
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("invalid_grant");
        assertThat(errorResponse.get("error_description")).contains("Invalid username or password");

        // Verify token service was never called
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Password grant should return 400 when username is missing")
    void passwordGrant_MissingUsername_ShouldReturn400() {
        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                "password",
                null,  // missing username
                "password",
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("invalid_request");
        assertThat(errorResponse.get("error_description")).contains("Username and password required");

        // Verify services were never called
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Password grant should return 400 when password is missing")
    void passwordGrant_MissingPassword_ShouldReturn400() {
        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                "password",
                "admin",
                null,  // missing password
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("invalid_request");

        // Verify services were never called
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenService, never()).generateToken(any());
    }

    // =====================================================
    // Refresh Token Grant Tests
    // =====================================================

    @Test
    @DisplayName("Refresh token grant should return new token on valid refresh token")
    void refreshTokenGrant_ValidToken_ShouldReturnNewToken() {
        // Given
        String grantType = "refresh_token";
        String refreshToken = "valid-refresh-token";

        TokenResponse newTokenResponse = TokenResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.new-access-token")
                .refreshToken("eyJhbGciOiJIUzI1NiJ9.new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(43200)
                .build();

        when(tokenService.refreshToken(anyString())).thenReturn(newTokenResponse);

        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                grantType,
                null,
                null,
                refreshToken
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(TokenResponse.class);

        TokenResponse tokenResponse = (TokenResponse) response.getBody();
        assertThat(tokenResponse.getAccessToken()).isEqualTo(newTokenResponse.getAccessToken());
        assertThat(tokenResponse.getRefreshToken()).isEqualTo(newTokenResponse.getRefreshToken());

        // Verify interaction
        verify(tokenService, times(1)).refreshToken(refreshToken);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Refresh token grant should return 401 on invalid token")
    void refreshTokenGrant_InvalidToken_ShouldReturn401() {
        // Given
        String grantType = "refresh_token";
        String refreshToken = "invalid-refresh-token";

        when(tokenService.refreshToken(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                grantType,
                null,
                null,
                refreshToken
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("invalid_grant");
        assertThat(errorResponse.get("error_description")).contains("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("Refresh token grant should return 400 when refresh token is missing")
    void refreshTokenGrant_MissingToken_ShouldReturn400() {
        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                "refresh_token",
                null,
                null,
                null  // missing refresh token
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("invalid_request");
        assertThat(errorResponse.get("error_description")).contains("Refresh token required");

        // Verify service was never called
        verify(tokenService, never()).refreshToken(anyString());
    }

    // =====================================================
    // Client Authentication Tests
    // =====================================================

    @Test
    @DisplayName("Should return 401 when client credentials are invalid")
    void invalidClientCredentials_ShouldReturn401() {
        // When
        ResponseEntity<?> response = controller.token(
                INVALID_BASIC_AUTH,  // wrong client:secret
                "password",
                "admin",
                "admin",
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("invalid_client");
        assertThat(errorResponse.get("error_description")).contains("Invalid client credentials");

        // Verify no authentication attempt was made
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should return 401 when Authorization header is missing")
    void missingAuthorizationHeader_ShouldReturn401() {
        // When
        ResponseEntity<?> response = controller.token(
                null,  // missing Authorization header
                "password",
                "admin",
                "admin",
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("invalid_client");

        // Verify no authentication attempt was made
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Should return 401 when Authorization header has wrong format")
    void wrongAuthorizationHeaderFormat_ShouldReturn401() {
        // When
        ResponseEntity<?> response = controller.token(
                "Bearer some-token",  // wrong format (should be Basic)
                "password",
                "admin",
                "admin",
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("invalid_client");
    }

    // =====================================================
    // Grant Type Tests
    // =====================================================

    @Test
    @DisplayName("Should return 400 for unsupported grant type")
    void unsupportedGrantType_ShouldReturn400() {
        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                "client_credentials",  // unsupported grant type
                null,
                null,
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("unsupported_grant_type");
        assertThat(errorResponse.get("error_description")).contains("Grant type not supported");

        // Verify no services were called
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenService, never()).generateToken(any());
        verify(tokenService, never()).refreshToken(anyString());
    }

    // =====================================================
    // Error Handling Tests
    // =====================================================

    @Test
    @DisplayName("Should return 500 on unexpected error during password grant")
    void passwordGrant_UnexpectedError_ShouldReturn500() {
        // Given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                "password",
                "admin",
                "admin",
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("server_error");
    }

    @Test
    @DisplayName("Should return 500 on unexpected error during token refresh")
    void refreshToken_UnexpectedError_ShouldReturn500() {
        // Given
        when(tokenService.refreshToken(anyString()))
                .thenThrow(new RuntimeException("Token service unavailable"));

        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                "refresh_token",
                null,
                null,
                "valid-token"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertThat(errorResponse.get("error")).isEqualTo("server_error");
        assertThat(errorResponse.get("error_description")).contains("Internal server error");
    }

    // =====================================================
    // OLD-HEMIS Compatibility Tests
    // =====================================================

    @Test
    @DisplayName("Should support OLD-HEMIS client credentials (client:secret)")
    void oldHemisClientCredentials_ShouldWork() {
        // Given
        String oldHemisBasicAuth = "Basic " + Base64.getEncoder().encodeToString("client:secret".getBytes());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenService.generateToken(any())).thenReturn(testTokenResponse);

        // When
        ResponseEntity<?> response = controller.token(
                oldHemisBasicAuth,
                "password",
                "admin",
                "admin",
                null
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(TokenResponse.class);
    }

    @Test
    @DisplayName("Token response should match OLD-HEMIS format")
    void tokenResponse_ShouldMatchOldHemisFormat() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenService.generateToken(any())).thenReturn(testTokenResponse);

        // When
        ResponseEntity<?> response = controller.token(
                VALID_BASIC_AUTH,
                "password",
                "admin",
                "admin",
                null
        );

        // Then
        TokenResponse tokenResponse = (TokenResponse) response.getBody();

        // OLD-HEMIS format validation
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getRefreshToken()).isNotNull();
        assertThat(tokenResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(tokenResponse.getExpiresIn()).isGreaterThan(0);
    }
}
