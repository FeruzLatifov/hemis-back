package uz.hemis.security.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uz.hemis.security.config.SecurityProperties;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("RateLimitFilter — counter va per-IP/login limit testlari")
class RateLimitFilterTest {

    private SecurityProperties properties;
    private RateLimitFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        SecurityProperties.RateLimit rl = properties.getRateLimit();
        rl.setEnabled(true);
        rl.setRequestsPerMinute(100);
        rl.setRequestsPerMinutePerIp(50);
        rl.setLoginRequestsPerMinutePerIp(3);
        rl.setGlobalRequestsPerMinute(10000);

        filter = new RateLimitFilter(properties);
        chain = mock(FilterChain.class);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Per-IP limit (anonymous foydalanuvchi)")
    class PerIpLimit {

        @Test
        @DisplayName("Anonymous user IP'dan 50 ta request o'tadi, 51-chisi 429 qaytaradi")
        void anonymous_exceedsIpLimit_returns429() throws Exception {
            for (int i = 0; i < 50; i++) {
                MockHttpServletRequest req = newRequest("/api/v1/web/menu", "192.0.2.1");
                MockHttpServletResponse resp = new MockHttpServletResponse();
                filter.doFilter(req, resp, chain);
                assertThat(resp.getStatus()).isNotEqualTo(429);
            }

            // 51-request — limit oshadi
            MockHttpServletRequest req51 = newRequest("/api/v1/web/menu", "192.0.2.1");
            MockHttpServletResponse resp51 = new MockHttpServletResponse();
            filter.doFilter(req51, resp51, chain);

            assertThat(resp51.getStatus()).isEqualTo(429);
            assertThat(resp51.getContentAsString()).contains("rate_limit_exceeded");
            assertThat(resp51.getHeader("Retry-After")).isEqualTo("60");
            verify(chain, times(50)).doFilter(any(), any());
        }

        @Test
        @DisplayName("Boshqa IP — alohida counter, mustaqil")
        void differentIp_independentCounter() throws Exception {
            // IP1 dan 50 ta — limit'gacha
            for (int i = 0; i < 50; i++) {
                filter.doFilter(newRequest("/api/v1/web/menu", "192.0.2.1"),
                        new MockHttpServletResponse(), chain);
            }
            // IP2 dan 1 ta — alohida counter
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilter(newRequest("/api/v1/web/menu", "192.0.2.2"), resp, chain);

            assertThat(resp.getStatus()).isNotEqualTo(429);
            verify(chain, times(51)).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Login endpoint stricter limit (brute-force protection)")
    class LoginEndpointLimit {

        @Test
        @DisplayName("Login endpoint 3 ta request o'tadi, 4-chisi 429")
        void loginEndpoint_exceedsLimit_returns429() throws Exception {
            for (int i = 0; i < 3; i++) {
                MockHttpServletResponse resp = new MockHttpServletResponse();
                filter.doFilter(newRequest("/api/v1/web/auth/login", "192.0.2.10"), resp, chain);
                assertThat(resp.getStatus()).isNotEqualTo(429);
            }

            MockHttpServletResponse resp4 = new MockHttpServletResponse();
            filter.doFilter(newRequest("/api/v1/web/auth/login", "192.0.2.10"), resp4, chain);

            assertThat(resp4.getStatus()).isEqualTo(429);
            assertThat(resp4.getContentAsString()).contains("Login attempt limit");
            verify(chain, times(3)).doFilter(any(), any());
        }

        @Test
        @DisplayName("Boshqa endpoint — login stricter limit qo'llanmaydi")
        void nonLoginEndpoint_useGeneralIpLimit() throws Exception {
            // /api/v1/web/menu — login emas, per-IP general (50)
            for (int i = 0; i < 4; i++) {
                MockHttpServletResponse resp = new MockHttpServletResponse();
                filter.doFilter(newRequest("/api/v1/web/menu", "192.0.2.20"), resp, chain);
                assertThat(resp.getStatus()).isNotEqualTo(429);
            }
            verify(chain, times(4)).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Authenticated user (per-university limit)")
    class AuthenticatedUserLimit {

        @Test
        @DisplayName("JWT user — university counter ishlatadi, IP counter emas")
        void authenticatedUser_universityCounterPath() throws Exception {
            setAuthenticatedTenant("401");

            // 50 ta request — IP limit (50) avval erishilmaydi, chunki university counter ishlatamiz
            for (int i = 0; i < 60; i++) {
                MockHttpServletResponse resp = new MockHttpServletResponse();
                filter.doFilter(newRequest("/api/v1/web/menu", "192.0.2.30"), resp, chain);
                assertThat(resp.getStatus()).isNotEqualTo(429);
            }

            // 60 ta request o'tdi (university limit 100, IP limit 50 bypass'i tekshirildi)
            verify(chain, times(60)).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("shouldNotFilter — public endpoint'lar")
    class ShouldNotFilter {

        @Test
        @DisplayName("/actuator/health — filter chetlab o'tiladi")
        void actuatorHealth_isSkipped() {
            MockHttpServletRequest req = newRequest("/actuator/health", "192.0.2.40");
            assertThat(filter.shouldNotFilter(req)).isTrue();
        }

        @Test
        @DisplayName("/swagger-ui — filter chetlab o'tiladi")
        void swaggerUi_isSkipped() {
            MockHttpServletRequest req = newRequest("/swagger-ui/index.html", "192.0.2.41");
            assertThat(filter.shouldNotFilter(req)).isTrue();
        }

        @Test
        @DisplayName("API endpoint — filter qo'llaniladi")
        void apiEndpoint_isFiltered() {
            MockHttpServletRequest req = newRequest("/api/v1/web/menu", "192.0.2.42");
            assertThat(filter.shouldNotFilter(req)).isFalse();
        }
    }

    @Nested
    @DisplayName("Rate limit disabled — har bir request bypass")
    class DisabledMode {

        @Test
        @DisplayName("Disabled — limit ham bypass")
        void disabled_doesNotEnforceLimits() throws Exception {
            properties.getRateLimit().setEnabled(false);

            for (int i = 0; i < 200; i++) {
                MockHttpServletResponse resp = new MockHttpServletResponse();
                filter.doFilter(newRequest("/api/v1/web/menu", "192.0.2.50"), resp, chain);
                assertThat(resp.getStatus()).isNotEqualTo(429);
            }
            verify(chain, times(200)).doFilter(any(), any());
        }
    }

    private MockHttpServletRequest newRequest(String path, String ip) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI(path);
        req.setRemoteAddr(ip);
        return req;
    }

    private void setAuthenticatedTenant(String universityCode) {
        Jwt jwt = Jwt.withTokenValue("test")
                .header("alg", "none")
                .subject("user-123")
                .claim("university_code", universityCode)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
