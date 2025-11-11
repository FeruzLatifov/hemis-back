package uz.hemis.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import uz.hemis.common.dto.TokenResponse;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TokenService Tests
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>JWT access token generation</li>
 *   <li>JWT refresh token generation</li>
 *   <li>Token refresh flow</li>
 *   <li>Token validation and decoding</li>
 *   <li>JWT claims verification</li>
 *   <li>Expiration handling</li>
 *   <li>Invalid token scenarios</li>
 * </ul>
 *
 * <p><strong>Testing Strategy:</strong></p>
 * <ul>
 *   <li>Spring Boot integration test (real JwtEncoder/JwtDecoder)</li>
 *   <li>Real JWT generation and validation</li>
 *   <li>Claims extraction and verification</li>
 *   <li>Stateless token flow testing</li>
 * </ul>
 *
 * @since 1.0.0
 */
@SpringBootTest(classes = {
    TokenService.class,
    TestSecurityConfig.class  // Provides JwtEncoder/JwtDecoder beans
})
@TestPropertySource(properties = {
    "hemis.security.jwt.expiration=3600",        // 1 hour for tests
    "hemis.security.jwt.refresh-expiration=7200", // 2 hours for tests
    "hemis.security.jwt.issuer=hemis-test",
    "hemis.security.jwt.secret=test-secret-key-minimum-256-bits-required-for-hmac-sha256"
})
@DisplayName("TokenService Tests")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        // Create test user with multiple roles
        testUser = User.builder()
                .username("test_user")
                .password("password")  // Not used in token generation
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_UNIVERSITY_ADMIN")
                ))
                .build();
    }

    // =====================================================
    // Access Token Generation Tests
    // =====================================================

    @Test
    @DisplayName("generateToken should create valid JWT access token")
    void generateToken_ShouldCreateValidJWT() {
        // When
        TokenResponse response = tokenService.generateToken(testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getAccessToken()).startsWith("eyJ"); // JWT prefix
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600); // From test properties
        assertThat(response.getScope()).isEqualTo("rest-api");
    }

    @Test
    @DisplayName("JWT access token should contain correct claims")
    void accessToken_ShouldContainCorrectClaims() {
        // When
        TokenResponse response = tokenService.generateToken(testUser);
        Jwt jwt = jwtDecoder.decode(response.getAccessToken());

        // Then: Verify standard claims
        assertThat(jwt.getSubject()).isEqualTo("test_user");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("hemis-test");
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isNotNull();

        // Then: Verify custom claims
        assertThat(jwt.getClaimAsString("username")).isEqualTo("test_user");
        assertThat(jwt.getClaimAsString("scope")).isEqualTo("rest-api");

        // Then: Verify roles claim (comma-separated)
        String roles = jwt.getClaimAsString("roles");
        assertThat(roles).isNotNull();
        assertThat(roles).contains("ROLE_ADMIN");
        assertThat(roles).contains("ROLE_USER");
        assertThat(roles).contains("ROLE_UNIVERSITY_ADMIN");
    }

    @Test
    @DisplayName("JWT access token should have correct expiration time")
    void accessToken_ShouldHaveCorrectExpiration() {
        // When
        TokenResponse response = tokenService.generateToken(testUser);
        Jwt jwt = jwtDecoder.decode(response.getAccessToken());

        // Then
        Instant issuedAt = jwt.getIssuedAt();
        Instant expiresAt = jwt.getExpiresAt();

        assertThat(issuedAt).isNotNull();
        assertThat(expiresAt).isNotNull();

        // Should expire 3600 seconds (1 hour) after issuance
        long expirationSeconds = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
        assertThat(expirationSeconds).isEqualTo(3600);
    }

    @Test
    @DisplayName("JWT access token should be decodable and valid")
    void accessToken_ShouldBeDecodableAndValid() {
        // When
        TokenResponse response = tokenService.generateToken(testUser);
        String token = response.getAccessToken();

        // Then: Should decode without exception
        Jwt jwt = jwtDecoder.decode(token);
        assertThat(jwt).isNotNull();

        // Then: Should have valid signature (implicit in jwtDecoder.decode())
        assertThat(jwt.getTokenValue()).isEqualTo(token);
    }

    @Test
    @DisplayName("JWT access token payload should be base64 encoded")
    void accessToken_PayloadShouldBeBase64Encoded() {
        // When
        TokenResponse response = tokenService.generateToken(testUser);
        String token = response.getAccessToken();

        // Then: JWT format = header.payload.signature
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);

        // Then: Decode payload (without padding)
        String payload = parts[1];
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        String decodedPayload = new String(decodedBytes);

        // Then: Should contain expected JSON fields
        assertThat(decodedPayload).contains("\"sub\"");
        assertThat(decodedPayload).contains("\"iss\"");
        assertThat(decodedPayload).contains("\"exp\"");
        assertThat(decodedPayload).contains("\"roles\"");
        assertThat(decodedPayload).contains("test_user");
    }

    // =====================================================
    // Refresh Token Generation Tests
    // =====================================================

    @Test
    @DisplayName("generateRefreshToken should create valid JWT refresh token")
    void generateRefreshToken_ShouldCreateValidJWT() {
        // When
        String refreshToken = tokenService.generateRefreshToken(testUser);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).startsWith("eyJ");

        // Decode and verify
        Jwt jwt = jwtDecoder.decode(refreshToken);
        assertThat(jwt.getSubject()).isEqualTo("test_user");
        assertThat(jwt.getClaimAsString("type")).isEqualTo("refresh");
    }

    @Test
    @DisplayName("Refresh token should have longer expiration than access token")
    void refreshToken_ShouldHaveLongerExpiration() {
        // When
        TokenResponse accessTokenResponse = tokenService.generateToken(testUser);
        String refreshToken = tokenService.generateRefreshToken(testUser);

        // Then
        Jwt accessJwt = jwtDecoder.decode(accessTokenResponse.getAccessToken());
        Jwt refreshJwt = jwtDecoder.decode(refreshToken);

        long accessExpiration = accessJwt.getExpiresAt().getEpochSecond() - accessJwt.getIssuedAt().getEpochSecond();
        long refreshExpiration = refreshJwt.getExpiresAt().getEpochSecond() - refreshJwt.getIssuedAt().getEpochSecond();

        // Refresh token (7200s) should be longer than access token (3600s)
        assertThat(refreshExpiration).isGreaterThan(accessExpiration);
        assertThat(refreshExpiration).isEqualTo(7200); // 2 hours from test properties
    }

    @Test
    @DisplayName("Refresh token should contain 'type=refresh' claim")
    void refreshToken_ShouldHaveTypeClaim() {
        // When
        String refreshToken = tokenService.generateRefreshToken(testUser);
        Jwt jwt = jwtDecoder.decode(refreshToken);

        // Then
        assertThat(jwt.getClaimAsString("type")).isEqualTo("refresh");
    }

    // =====================================================
    // Token Refresh Flow Tests
    // =====================================================

    @Test
    @DisplayName("refreshToken should generate new access and refresh tokens")
    void refreshToken_ShouldGenerateNewTokens() throws InterruptedException {
        // Given: Original tokens
        String originalRefreshToken = tokenService.generateRefreshToken(testUser);

        // Wait 1 second to ensure different timestamps
        Thread.sleep(1000);

        // When: Refresh using refresh token
        TokenResponse newTokens = tokenService.refreshToken(originalRefreshToken);

        // Then
        assertThat(newTokens).isNotNull();
        assertThat(newTokens.getAccessToken()).isNotNull();
        assertThat(newTokens.getRefreshToken()).isNotNull();
        assertThat(newTokens.getAccessToken()).startsWith("eyJ");
        assertThat(newTokens.getRefreshToken()).startsWith("eyJ");

        // New refresh token should be different from original (due to new timestamp)
        assertThat(newTokens.getRefreshToken()).isNotEqualTo(originalRefreshToken);
    }

    @Test
    @DisplayName("refreshToken should preserve user information")
    void refreshToken_ShouldPreserveUserInfo() {
        // Given
        String originalRefreshToken = tokenService.generateRefreshToken(testUser);

        // When
        TokenResponse newTokens = tokenService.refreshToken(originalRefreshToken);

        // Then: Decode new access token
        Jwt newAccessJwt = jwtDecoder.decode(newTokens.getAccessToken());

        // Should preserve username and roles
        assertThat(newAccessJwt.getSubject()).isEqualTo("test_user");
        assertThat(newAccessJwt.getClaimAsString("username")).isEqualTo("test_user");

        String roles = newAccessJwt.getClaimAsString("roles");
        assertThat(roles).contains("ROLE_ADMIN");
        assertThat(roles).contains("ROLE_USER");
        assertThat(roles).contains("ROLE_UNIVERSITY_ADMIN");
    }

    @Test
    @DisplayName("refreshToken should throw exception for invalid token")
    void refreshToken_InvalidToken_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> tokenService.refreshToken("invalid-token-xyz"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("refreshToken should reject access token (not refresh token)")
    void refreshToken_AccessToken_ShouldBeRejected() {
        // Given: Create access token (not refresh token)
        TokenResponse accessTokenResponse = tokenService.generateToken(testUser);
        String accessToken = accessTokenResponse.getAccessToken();

        // When/Then: Should reject because it's not a refresh token
        assertThatThrownBy(() -> tokenService.refreshToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a refresh token");
    }

    @Test
    @DisplayName("refreshToken should generate new tokens with fresh timestamps")
    void refreshToken_ShouldHaveFreshTimestamps() throws InterruptedException {
        // Given
        String originalRefreshToken = tokenService.generateRefreshToken(testUser);
        Jwt originalJwt = jwtDecoder.decode(originalRefreshToken);

        // Wait 1 second
        Thread.sleep(1000);

        // When
        TokenResponse newTokens = tokenService.refreshToken(originalRefreshToken);
        Jwt newAccessJwt = jwtDecoder.decode(newTokens.getAccessToken());
        Jwt newRefreshJwt = jwtDecoder.decode(newTokens.getRefreshToken());

        // Then: New tokens should have later issuedAt timestamps
        assertThat(newAccessJwt.getIssuedAt()).isAfter(originalJwt.getIssuedAt());
        assertThat(newRefreshJwt.getIssuedAt()).isAfter(originalJwt.getIssuedAt());
    }

    // =====================================================
    // Token Validation Tests
    // =====================================================

    @Test
    @DisplayName("JwtDecoder should validate token signature")
    void jwtDecoder_ShouldValidateSignature() {
        // Given: Valid token
        TokenResponse response = tokenService.generateToken(testUser);
        String validToken = response.getAccessToken();

        // When/Then: Should decode without exception
        Jwt jwt = jwtDecoder.decode(validToken);
        assertThat(jwt).isNotNull();

        // Given: Invalid token (tampered signature)
        String[] parts = validToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalid-signature";

        // When/Then: Should reject tampered token
        assertThatThrownBy(() -> jwtDecoder.decode(tamperedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("JwtDecoder should reject malformed tokens")
    void jwtDecoder_ShouldRejectMalformedTokens() {
        // Test various malformed tokens
        String[] malformedTokens = {
                "not-a-jwt",
                "only.two.parts",
                "",
                "eyJhbGciOiJIUzI1NiJ9"  // Only header
        };

        for (String malformedToken : malformedTokens) {
            assertThatThrownBy(() -> jwtDecoder.decode(malformedToken))
                    .isInstanceOf(JwtException.class);
        }
    }

    // =====================================================
    // Multiple Users Test
    // =====================================================

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given: Two different users
        UserDetails user1 = User.builder()
                .username("user1")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        UserDetails user2 = User.builder()
                .username("user2")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        // When
        TokenResponse token1 = tokenService.generateToken(user1);
        TokenResponse token2 = tokenService.generateToken(user2);

        // Then: Tokens should be different
        assertThat(token1.getAccessToken()).isNotEqualTo(token2.getAccessToken());

        // Decode and verify
        Jwt jwt1 = jwtDecoder.decode(token1.getAccessToken());
        Jwt jwt2 = jwtDecoder.decode(token2.getAccessToken());

        assertThat(jwt1.getSubject()).isEqualTo("user1");
        assertThat(jwt2.getSubject()).isEqualTo("user2");
        assertThat(jwt1.getClaimAsString("roles")).isEqualTo("ROLE_USER");
        assertThat(jwt2.getClaimAsString("roles")).isEqualTo("ROLE_ADMIN");
    }

    // =====================================================
    // Edge Cases
    // =====================================================

    @Test
    @DisplayName("Should handle user with no roles")
    void shouldHandleUserWithNoRoles() {
        // Given: User with empty authorities
        UserDetails userWithNoRoles = User.builder()
                .username("no_role_user")
                .password("password")
                .authorities(List.of())
                .build();

        // When
        TokenResponse response = tokenService.generateToken(userWithNoRoles);

        // Then: Should still generate valid token
        assertThat(response.getAccessToken()).isNotNull();

        Jwt jwt = jwtDecoder.decode(response.getAccessToken());
        assertThat(jwt.getSubject()).isEqualTo("no_role_user");

        // Roles claim should be empty string
        String roles = jwt.getClaimAsString("roles");
        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("Should handle user with single role")
    void shouldHandleUserWithSingleRole() {
        // Given
        UserDetails userWithOneRole = User.builder()
                .username("single_role_user")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_VIEWER")))
                .build();

        // When
        TokenResponse response = tokenService.generateToken(userWithOneRole);

        // Then
        Jwt jwt = jwtDecoder.decode(response.getAccessToken());
        assertThat(jwt.getClaimAsString("roles")).isEqualTo("ROLE_VIEWER");
    }

    @Test
    @DisplayName("TokenResponse should match OAuth2 format")
    void tokenResponse_ShouldMatchOAuth2Format() {
        // When
        TokenResponse response = tokenService.generateToken(testUser);

        // Then: OAuth2 standard fields
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");  // Must be "Bearer"
        assertThat(response.getExpiresIn()).isGreaterThan(0);
        assertThat(response.getScope()).isNotNull();

        // Optional fields in our implementation
        assertThat(response.getRefreshToken()).isNull();  // Only in refreshToken() method
    }
}
