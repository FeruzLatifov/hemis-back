package uz.hemis.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.common.port.security.UserIdentificationPort;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TokenService} (the legacy auth / JWT service).
 *
 * <p><strong>Why "LegacyAuthServiceTest"?</strong></p>
 * <p>TokenService is the backend's central JWT authentication service that maintains
 * backward compatibility with the legacy (OLD-HEMIS) OAuth2 token format.
 * This test class exercises it with mocked dependencies (JwtEncoder, JwtDecoder,
 * UserPermissionCacheService, UserIdentificationPort) so that no Spring context,
 * Redis, or database is required.</p>
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>Access token generation (claims, format, expiration)</li>
 *   <li>Refresh token generation (type claim, expiration)</li>
 *   <li>Token refresh flow (happy path, wrong type, missing subject)</li>
 *   <li>Permission caching during token generation</li>
 *   <li>User-not-found error propagation</li>
 *   <li>OAuth2 response format compliance (bearer, rest-api, expires_in)</li>
 * </ul>
 *
 * @since 2.0.0
 * @see TokenService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LegacyAuthService (TokenService) Unit Tests")
class LegacyAuthServiceTest {

    // =====================================================
    // Mocks
    // =====================================================

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserPermissionCacheService permissionCacheService;

    @Mock
    private UserIdentificationPort userIdentificationPort;

    @Captor
    private ArgumentCaptor<JwtEncoderParameters> encoderParamsCaptor;

    // System under test
    private TokenService tokenService;

    // =====================================================
    // Test fixtures
    // =====================================================

    private static final UUID TEST_USER_ID = UUID.fromString("60885987-1b61-4247-94c7-dff348347f93");
    private static final String TEST_USERNAME = "admin";
    private static final long ACCESS_TOKEN_VALIDITY = 3600L;   // 1 hour
    private static final long REFRESH_TOKEN_VALIDITY = 7200L;  // 2 hours
    private static final String ISSUER = "hemis-test";

    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(jwtEncoder, jwtDecoder, permissionCacheService, userIdentificationPort);

        // Inject @Value fields via reflection
        ReflectionTestUtils.setField(tokenService, "accessTokenValiditySeconds", ACCESS_TOKEN_VALIDITY);
        ReflectionTestUtils.setField(tokenService, "refreshTokenValiditySeconds", REFRESH_TOKEN_VALIDITY);
        ReflectionTestUtils.setField(tokenService, "issuer", ISSUER);

        // Default test user
        testUser = User.builder()
                .username(TEST_USERNAME)
                .password("encoded-password")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER")
                ))
                .build();
    }

    // =====================================================
    // Helper: stub the common encoding pipeline
    // =====================================================

    /**
     * Configures the mocks so that userIdentificationPort returns the given userId
     * and jwtEncoder returns a fake token with the given value.
     */
    private void stubTokenGeneration(UUID userId, String fakeTokenValue) {
        when(userIdentificationPort.getUserIdByUsername(TEST_USERNAME))
                .thenReturn(Optional.of(userId));
        when(userIdentificationPort.getUserInfoByUsername(TEST_USERNAME))
                .thenReturn(Optional.of(new UserIdentificationPort.UserInfo(userId, "Test User")));

        Jwt fakeJwt = Jwt.withTokenValue(fakeTokenValue)
                .header("alg", "HS256")
                .subject(userId.toString())
                .claim("username", TEST_USERNAME)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_VALIDITY))
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(fakeJwt);
    }

    // =============================================================
    //  1. Access Token Generation
    // =============================================================

    @Nested
    @DisplayName("generateToken (access token)")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should return TokenResponse with access token, refresh token, and OAuth2 fields")
        void generateToken_returnsCompleteOAuth2Response() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "eyJ.access.token");

            // When
            TokenResponse response = tokenService.generateToken(testUser);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("eyJ.access.token");
            assertThat(response.getRefreshToken()).isEqualTo("eyJ.access.token"); // same stub
            assertThat(response.getTokenType()).isEqualTo("bearer");
            assertThat(response.getScope()).isEqualTo("rest-api");
            assertThat(response.getExpiresIn()).isEqualTo((int) (ACCESS_TOKEN_VALIDITY - 2));
        }

        @Test
        @DisplayName("Should resolve userId via UserIdentificationPort")
        void generateToken_resolvesUserIdViaPort() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token-value");

            // When
            tokenService.generateToken(testUser);

            // Then: access uses getUserInfoByUsername, refresh uses getUserIdByUsername
            verify(userIdentificationPort).getUserInfoByUsername(TEST_USERNAME);
            verify(userIdentificationPort).getUserIdByUsername(TEST_USERNAME);
        }

        @Test
        @DisplayName("Should cache permissions in Redis during token generation")
        void generateToken_cachesPermissionsInRedis() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token-value");

            // When
            tokenService.generateToken(testUser);

            // Then: permissions should be cached with the userId
            verify(permissionCacheService).cacheUserPermissions(eq(TEST_USER_ID), any());
        }

        @Test
        @DisplayName("Cached permissions should include all user authorities")
        void generateToken_cachedPermissionsContainAllAuthorities() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token-value");

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<String>> permissionsCaptor = ArgumentCaptor.forClass(Set.class);

            // When
            tokenService.generateToken(testUser);

            // Then
            verify(permissionCacheService).cacheUserPermissions(eq(TEST_USER_ID), permissionsCaptor.capture());
            Set<String> cachedPermissions = permissionsCaptor.getValue();
            assertThat(cachedPermissions).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        }

        @Test
        @DisplayName("Should encode JWT twice (access + refresh)")
        void generateToken_encodesTwice() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token-value");

            // When
            tokenService.generateToken(testUser);

            // Then: one call for access token, one for refresh token
            verify(jwtEncoder, times(2)).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("JWT claims should contain userId as subject, username, and scope")
        void generateToken_jwtClaimsAreCorrect() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token-value");

            // When
            tokenService.generateToken(testUser);

            // Then: capture the first encode call (access token)
            verify(jwtEncoder, atLeastOnce()).encode(encoderParamsCaptor.capture());
            JwtEncoderParameters params = encoderParamsCaptor.getAllValues().get(0);

            JwtClaimsSet claims = params.getClaims();
            assertThat(claims.getSubject()).isEqualTo(TEST_USER_ID.toString());
            assertThat(claims.<String>getClaim("iss")).isEqualTo(ISSUER);
            assertThat(claims.<String>getClaim("username")).isEqualTo(TEST_USERNAME);
            assertThat(claims.<String>getClaim("scope")).isEqualTo("rest-api");
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiresAt()).isNotNull();
        }

        @Test
        @DisplayName("Access token expiration should be accessTokenValiditySeconds from now")
        void generateToken_expirationIsCorrect() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token-value");

            // When
            tokenService.generateToken(testUser);

            // Then
            verify(jwtEncoder, atLeastOnce()).encode(encoderParamsCaptor.capture());
            JwtClaimsSet claims = encoderParamsCaptor.getAllValues().get(0).getClaims();

            long diff = claims.getExpiresAt().getEpochSecond() - claims.getIssuedAt().getEpochSecond();
            assertThat(diff).isEqualTo(ACCESS_TOKEN_VALIDITY);
        }

        @Test
        @DisplayName("Should throw when user is not found in either table")
        void generateToken_userNotFound_throws() {
            // Given
            when(userIdentificationPort.getUserInfoByUsername(TEST_USERNAME))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> tokenService.generateToken(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // =============================================================
    //  2. Refresh Token Generation
    // =============================================================

    @Nested
    @DisplayName("generateRefreshToken")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should return a non-null JWT string")
        void generateRefreshToken_returnsTokenString() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "eyJ.refresh.token");

            // When
            String refreshToken = tokenService.generateRefreshToken(testUser);

            // Then
            assertThat(refreshToken).isNotNull().isEqualTo("eyJ.refresh.token");
        }

        @Test
        @DisplayName("Refresh token claims should have type=refresh")
        void generateRefreshToken_containsTypeClaim() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "eyJ.refresh.token");

            // When
            tokenService.generateRefreshToken(testUser);

            // Then
            verify(jwtEncoder).encode(encoderParamsCaptor.capture());
            JwtClaimsSet claims = encoderParamsCaptor.getValue().getClaims();
            assertThat(claims.<String>getClaim("type")).isEqualTo("refresh");
        }

        @Test
        @DisplayName("Refresh token expiration should use refreshTokenValiditySeconds")
        void generateRefreshToken_expirationUsesRefreshValidity() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "eyJ.refresh.token");

            // When
            tokenService.generateRefreshToken(testUser);

            // Then
            verify(jwtEncoder).encode(encoderParamsCaptor.capture());
            JwtClaimsSet claims = encoderParamsCaptor.getValue().getClaims();

            long diff = claims.getExpiresAt().getEpochSecond() - claims.getIssuedAt().getEpochSecond();
            assertThat(diff).isEqualTo(REFRESH_TOKEN_VALIDITY);
        }

        @Test
        @DisplayName("Refresh token subject should be userId (UUID)")
        void generateRefreshToken_subjectIsUserId() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "eyJ.refresh.token");

            // When
            tokenService.generateRefreshToken(testUser);

            // Then
            verify(jwtEncoder).encode(encoderParamsCaptor.capture());
            JwtClaimsSet claims = encoderParamsCaptor.getValue().getClaims();
            assertThat(claims.getSubject()).isEqualTo(TEST_USER_ID.toString());
        }
    }

    // =============================================================
    //  3. Token Refresh Flow
    // =============================================================

    @Nested
    @DisplayName("refreshToken (token refresh flow)")
    class RefreshTokenFlowTests {

        @Test
        @DisplayName("Should decode refresh token, generate new access + refresh tokens")
        void refreshToken_happyPath() {
            // Given: a decoded refresh JWT
            Jwt decodedRefresh = Jwt.withTokenValue("old-refresh-token")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .claim("username", TEST_USERNAME)
                    .claim("type", "refresh")
                    .issuedAt(Instant.now().minusSeconds(600))
                    .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY))
                    .build();
            when(jwtDecoder.decode("old-refresh-token")).thenReturn(decodedRefresh);

            // Stub encoder for new tokens
            Jwt newJwt = Jwt.withTokenValue("new-access-token")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_VALIDITY))
                    .build();
            when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(newJwt);

            // When
            TokenResponse response = tokenService.refreshToken("old-refresh-token");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-access-token"); // same stub
            assertThat(response.getTokenType()).isEqualTo("bearer");
            assertThat(response.getScope()).isEqualTo("rest-api");
            assertThat(response.getExpiresIn()).isEqualTo((int) (ACCESS_TOKEN_VALIDITY - 2));
        }

        @Test
        @DisplayName("Should reject token that is not a refresh token (missing type=refresh)")
        void refreshToken_rejectsNonRefreshToken() {
            // Given: a JWT without type=refresh
            Jwt accessJwt = Jwt.withTokenValue("an-access-token")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .claim("username", TEST_USERNAME)
                    .claim("scope", "rest-api")
                    // no "type" claim
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_VALIDITY))
                    .build();
            when(jwtDecoder.decode("an-access-token")).thenReturn(accessJwt);

            // When / Then
            assertThatThrownBy(() -> tokenService.refreshToken("an-access-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not a refresh token");
        }

        @Test
        @DisplayName("Should throw when refresh token has empty subject")
        void refreshToken_emptySubject_throws() {
            // Given: refresh JWT with empty subject
            Jwt emptySubjectJwt = Jwt.withTokenValue("bad-refresh")
                    .header("alg", "HS256")
                    .subject("")
                    .claim("type", "refresh")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY))
                    .build();
            when(jwtDecoder.decode("bad-refresh")).thenReturn(emptySubjectJwt);

            // When / Then
            assertThatThrownBy(() -> tokenService.refreshToken("bad-refresh"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId not found");
        }

        @Test
        @DisplayName("Should wrap JwtException as IllegalArgumentException")
        void refreshToken_invalidJwt_throwsIllegalArgument() {
            // Given: decoder throws JwtException
            when(jwtDecoder.decode("garbage-token"))
                    .thenThrow(new JwtException("Invalid JWT"));

            // When / Then
            assertThatThrownBy(() -> tokenService.refreshToken("garbage-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid or expired refresh token")
                    .hasCauseInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Should encode exactly two JWTs during refresh (access + refresh)")
        void refreshToken_encodesTwice() {
            // Given
            Jwt decodedRefresh = Jwt.withTokenValue("old-refresh")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .claim("username", TEST_USERNAME)
                    .claim("type", "refresh")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY))
                    .build();
            when(jwtDecoder.decode("old-refresh")).thenReturn(decodedRefresh);

            Jwt stub = Jwt.withTokenValue("new-token")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_VALIDITY))
                    .build();
            when(jwtEncoder.encode(any())).thenReturn(stub);

            // When
            tokenService.refreshToken("old-refresh");

            // Then
            verify(jwtEncoder, times(2)).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("New access token claims should contain userId, username, and scope")
        void refreshToken_newAccessTokenClaimsCorrect() {
            // Given
            Jwt decodedRefresh = Jwt.withTokenValue("old-refresh")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .claim("username", TEST_USERNAME)
                    .claim("type", "refresh")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY))
                    .build();
            when(jwtDecoder.decode("old-refresh")).thenReturn(decodedRefresh);

            Jwt stub = Jwt.withTokenValue("new-token")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_VALIDITY))
                    .build();
            when(jwtEncoder.encode(any())).thenReturn(stub);

            // When
            tokenService.refreshToken("old-refresh");

            // Then: capture the first encode call (access token)
            verify(jwtEncoder, times(2)).encode(encoderParamsCaptor.capture());
            JwtClaimsSet accessClaims = encoderParamsCaptor.getAllValues().get(0).getClaims();

            assertThat(accessClaims.getSubject()).isEqualTo(TEST_USER_ID.toString());
            assertThat(accessClaims.<String>getClaim("username")).isEqualTo(TEST_USERNAME);
            assertThat(accessClaims.<String>getClaim("scope")).isEqualTo("rest-api");
            assertThat(accessClaims.<String>getClaim("iss")).isEqualTo(ISSUER);
        }

        @Test
        @DisplayName("New refresh token claims should have type=refresh")
        void refreshToken_newRefreshTokenHasTypeClaim() {
            // Given
            Jwt decodedRefresh = Jwt.withTokenValue("old-refresh")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .claim("username", TEST_USERNAME)
                    .claim("type", "refresh")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY))
                    .build();
            when(jwtDecoder.decode("old-refresh")).thenReturn(decodedRefresh);

            Jwt stub = Jwt.withTokenValue("new-token")
                    .header("alg", "HS256")
                    .subject(TEST_USER_ID.toString())
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY))
                    .build();
            when(jwtEncoder.encode(any())).thenReturn(stub);

            // When
            tokenService.refreshToken("old-refresh");

            // Then: capture the second encode call (refresh token)
            verify(jwtEncoder, times(2)).encode(encoderParamsCaptor.capture());
            JwtClaimsSet refreshClaims = encoderParamsCaptor.getAllValues().get(1).getClaims();

            assertThat(refreshClaims.<String>getClaim("type")).isEqualTo("refresh");
        }
    }

    // =============================================================
    //  4. OAuth2 Response Format Compliance
    // =============================================================

    @Nested
    @DisplayName("OAuth2 response format compliance")
    class OAuth2FormatTests {

        @Test
        @DisplayName("token_type should be lowercase 'bearer' for OLD-HEMIS compatibility")
        void tokenType_isLowercaseBearer() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token");

            // When
            TokenResponse response = tokenService.generateToken(testUser);

            // Then
            assertThat(response.getTokenType()).isEqualTo("bearer");
        }

        @Test
        @DisplayName("expires_in should be accessTokenValiditySeconds minus 2 (OLD-HEMIS quirk)")
        void expiresIn_isValidityMinusTwo() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token");

            // When
            TokenResponse response = tokenService.generateToken(testUser);

            // Then: OLD-HEMIS uses expiration - 2 seconds
            assertThat(response.getExpiresIn()).isEqualTo((int) (ACCESS_TOKEN_VALIDITY - 2));
        }

        @Test
        @DisplayName("scope should be 'rest-api'")
        void scope_isRestApi() {
            // Given
            stubTokenGeneration(TEST_USER_ID, "token");

            // When
            TokenResponse response = tokenService.generateToken(testUser);

            // Then
            assertThat(response.getScope()).isEqualTo("rest-api");
        }
    }

    // =============================================================
    //  5. Edge Cases
    // =============================================================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("User with no authorities should still produce a valid token")
        void generateToken_noAuthorities_succeeds() {
            // Given
            UserDetails noRoleUser = User.builder()
                    .username(TEST_USERNAME)
                    .password("pw")
                    .authorities(Collections.emptyList())
                    .build();

            stubTokenGeneration(TEST_USER_ID, "no-role-token");

            // When
            TokenResponse response = tokenService.generateToken(noRoleUser);

            // Then
            assertThat(response.getAccessToken()).isNotNull();

            // Permissions cached should be empty set
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
            verify(permissionCacheService).cacheUserPermissions(eq(TEST_USER_ID), captor.capture());
            assertThat(captor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("User with many authorities should cache all of them")
        void generateToken_manyAuthorities_allCached() {
            // Given
            List<SimpleGrantedAuthority> manyAuthorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_TEACHER"),
                    new SimpleGrantedAuthority("ROLE_OTM_API"),
                    new SimpleGrantedAuthority("student:read"),
                    new SimpleGrantedAuthority("student:create"),
                    new SimpleGrantedAuthority("employee:read")
            );
            UserDetails richUser = User.builder()
                    .username(TEST_USERNAME)
                    .password("pw")
                    .authorities(manyAuthorities)
                    .build();

            stubTokenGeneration(TEST_USER_ID, "rich-token");

            // When
            tokenService.generateToken(richUser);

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
            verify(permissionCacheService).cacheUserPermissions(eq(TEST_USER_ID), captor.capture());
            assertThat(captor.getValue()).hasSize(7);
        }

        @Test
        @DisplayName("Different users should trigger separate port lookups")
        void generateToken_differentUsers_separatePortCalls() {
            // Given: two users
            UUID userId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID userId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

            when(userIdentificationPort.getUserIdByUsername("user1")).thenReturn(Optional.of(userId1));
            when(userIdentificationPort.getUserIdByUsername("user2")).thenReturn(Optional.of(userId2));
            when(userIdentificationPort.getUserInfoByUsername("user1"))
                    .thenReturn(Optional.of(new UserIdentificationPort.UserInfo(userId1, "User One")));
            when(userIdentificationPort.getUserInfoByUsername("user2"))
                    .thenReturn(Optional.of(new UserIdentificationPort.UserInfo(userId2, "User Two")));

            Jwt stub = Jwt.withTokenValue("t")
                    .header("alg", "HS256")
                    .subject("x")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            when(jwtEncoder.encode(any())).thenReturn(stub);

            UserDetails user1 = User.builder().username("user1").password("pw").authorities(List.of()).build();
            UserDetails user2 = User.builder().username("user2").password("pw").authorities(List.of()).build();

            // When
            tokenService.generateToken(user1);
            tokenService.generateToken(user2);

            // Then: access uses getUserInfoByUsername, refresh uses getUserIdByUsername
            verify(userIdentificationPort).getUserInfoByUsername("user1");
            verify(userIdentificationPort).getUserInfoByUsername("user2");
            verify(userIdentificationPort).getUserIdByUsername("user1");
            verify(userIdentificationPort).getUserIdByUsername("user2");
            verify(permissionCacheService).cacheUserPermissions(eq(userId1), any());
            verify(permissionCacheService).cacheUserPermissions(eq(userId2), any());
        }
    }
}
