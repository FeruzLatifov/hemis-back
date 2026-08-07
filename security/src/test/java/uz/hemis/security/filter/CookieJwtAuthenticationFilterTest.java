package uz.hemis.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uz.hemis.security.service.TokenBlacklistService;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CookieJwtAuthenticationFilter}.
 *
 * <p><strong>Scope:</strong> token extraction priority (header vs cookie),
 * blacklist enforcement, JWT decode failures, no-token pass-through, and
 * existing authentication preservation.</p>
 *
 * <p><strong>Strategy:</strong> pure Mockito + {@code MockHttpServletRequest} —
 * no Spring context. The chain proceeds for valid/absent/undecodable tokens, but a
 * blacklisted (revoked) token hard-fails with 401 and stops the chain so the OAuth2
 * resource-server filter cannot re-authenticate it.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CookieJwtAuthenticationFilter Tests")
class CookieJwtAuthenticationFilterTest {

    @Mock private JwtDecoder jwtDecoder;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private FilterChain filterChain;

    private CookieJwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new CookieJwtAuthenticationFilter(jwtDecoder, tokenBlacklistService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================
    // No-token pass-through (anonymous access)
    // =========================================================

    @Test
    @DisplayName("No header, no cookie → SecurityContext untouched + chain proceeds")
    void noToken_chainProceedsWithoutAuthentication() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtDecoder, never()).decode(anyString());
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    // =========================================================
    // Happy path — valid JWT via Authorization header
    // =========================================================

    @Test
    @DisplayName("Valid JWT in Authorization header → SecurityContext populated with JwtAuthenticationToken")
    void validBearerHeader_setsAuthentication() throws Exception {
        Jwt jwt = jwtFor("user-jti-123", "user@hemis.uz");
        request.addHeader("Authorization", "Bearer eyJraWQiOiJ0ZXN0In0.signed.jwt");
        when(jwtDecoder.decode("eyJraWQiOiJ0ZXN0In0.signed.jwt")).thenReturn(jwt);
        when(tokenBlacklistService.isBlacklisted("user-jti-123")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(((JwtAuthenticationToken) auth).getToken().getSubject()).isEqualTo("user@hemis.uz");
        verify(tokenBlacklistService).isBlacklisted("user-jti-123");
        verify(filterChain).doFilter(request, response);
    }

    // =========================================================
    // Happy path — valid JWT via cookie (fallback)
    // =========================================================

    @Test
    @DisplayName("Valid JWT in cookie (no header) → SecurityContext populated")
    void validCookie_setsAuthentication() throws Exception {
        Jwt jwt = jwtFor("cookie-jti-456", "admin@hemis.uz");
        request.setCookies(new Cookie("accessToken", "cookie.jwt.value"));
        when(jwtDecoder.decode("cookie.jwt.value")).thenReturn(jwt);
        when(tokenBlacklistService.isBlacklisted("cookie-jti-456")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(((JwtAuthenticationToken) auth).getToken().getSubject()).isEqualTo("admin@hemis.uz");
    }

    // =========================================================
    // Header takes priority over cookie
    // =========================================================

    @Test
    @DisplayName("Both header AND cookie → header wins (cookie ignored)")
    void headerAndCookie_headerWins() throws Exception {
        Jwt headerJwt = jwtFor("header-jti", "header-user@hemis.uz");
        request.addHeader("Authorization", "Bearer header.token");
        request.setCookies(new Cookie("accessToken", "cookie.token"));
        when(jwtDecoder.decode("header.token")).thenReturn(headerJwt);
        when(tokenBlacklistService.isBlacklisted("header-jti")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(((JwtAuthenticationToken) auth).getToken().getSubject()).isEqualTo("header-user@hemis.uz");
        verify(jwtDecoder, times(1)).decode("header.token");
        verify(jwtDecoder, never()).decode("cookie.token");
    }

    // =========================================================
    // Blacklist enforcement (logout revocation)
    // =========================================================

    @Test
    @DisplayName("Blacklisted JWT (logged-out token) → 401, SecurityContext empty, chain STOPS")
    void blacklistedToken_isRejected() throws Exception {
        Jwt jwt = jwtFor("revoked-jti", "user@hemis.uz");
        request.addHeader("Authorization", "Bearer revoked.jwt");
        when(jwtDecoder.decode("revoked.jwt")).thenReturn(jwt);
        when(tokenBlacklistService.isBlacklisted("revoked-jti")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .as("blacklisted token must not grant access")
            .isNull();
        // Hard-fail: the chain must NOT proceed to the OAuth2 resource-server filter, which would
        // otherwise re-authenticate the revoked token (the blacklist-bypass fix).
        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    // =========================================================
    // Token without JTI (legacy) — must still authenticate
    // =========================================================

    @Test
    @DisplayName("Valid JWT without JTI claim → authenticate (no blacklist check)")
    void noJtiClaim_authenticatesWithoutBlacklistCheck() throws Exception {
        Jwt jwt = Jwt.withTokenValue("legacy.jwt")
            .header("alg", "HS256")
            .subject("legacy@hemis.uz")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            // No "jti" claim — production must still authenticate
            .build();
        request.addHeader("Authorization", "Bearer legacy.jwt");
        when(jwtDecoder.decode("legacy.jwt")).thenReturn(jwt);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .isInstanceOf(JwtAuthenticationToken.class);
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    // =========================================================
    // JwtException (expired / bad signature / malformed)
    // =========================================================

    @Test
    @DisplayName("JwtException during decode → SecurityContext empty, chain still proceeds")
    void jwtDecodeFailure_swallowsExceptionAndProceeds() throws Exception {
        request.addHeader("Authorization", "Bearer expired.jwt");
        when(jwtDecoder.decode("expired.jwt")).thenThrow(new JwtException("Jwt expired"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // =========================================================
    // Unexpected runtime error (e.g. Redis down) — fail-open per current contract
    // =========================================================

    @Test
    @DisplayName("Unexpected exception (e.g. Redis down) → log only, chain proceeds, no auth set")
    void unexpectedException_doesNotCrashChain() throws Exception {
        Jwt jwt = jwtFor("jti-x", "user@hemis.uz");
        request.addHeader("Authorization", "Bearer x");
        when(jwtDecoder.decode("x")).thenReturn(jwt);
        when(tokenBlacklistService.isBlacklisted("jti-x"))
            .thenThrow(new RuntimeException("Redis connection refused"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // =========================================================
    // Existing authentication is preserved (filter is no-op)
    // =========================================================

    @Test
    @DisplayName("If SecurityContext already authenticated → filter does NOT overwrite")
    void existingAuthentication_isPreserved() throws Exception {
        Authentication existing = new TestingAuthenticationToken("pre-auth-user", "creds", "ROLE_USER");
        existing.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(existing);

        request.addHeader("Authorization", "Bearer some.jwt");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verify(jwtDecoder, never()).decode(anyString());
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    // =========================================================
    // Authorization header WITHOUT Bearer prefix — ignored, falls to cookie
    // =========================================================

    @Test
    @DisplayName("Authorization header without 'Bearer ' prefix → ignored, cookie checked as fallback")
    void nonBearerAuthHeader_fallsToCookie() throws Exception {
        Jwt jwt = jwtFor("cookie-jti", "via-cookie@hemis.uz");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");  // Basic, not Bearer
        request.setCookies(new Cookie("accessToken", "cookie.jwt"));
        when(jwtDecoder.decode("cookie.jwt")).thenReturn(jwt);
        when(tokenBlacklistService.isBlacklisted("cookie-jti")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .isInstanceOf(JwtAuthenticationToken.class);
        verify(jwtDecoder, times(1)).decode("cookie.jwt");
    }

    // =========================================================
    // Cookie array present but no 'accessToken' cookie → no auth
    // =========================================================

    @Test
    @DisplayName("Cookies present but no 'accessToken' → no authentication")
    void otherCookieNames_areIgnored() throws Exception {
        request.setCookies(new Cookie("session", "xyz"), new Cookie("locale", "uz-UZ"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtDecoder, never()).decode(anyString());
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Jwt jwtFor(String jti, String subject) {
        return Jwt.withTokenValue("test-token")
            .header("alg", "HS256")
            .subject(subject)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .claims(c -> c.putAll(Map.of(
                "jti", jti,
                "sub", subject
            )))
            .build();
    }
}
