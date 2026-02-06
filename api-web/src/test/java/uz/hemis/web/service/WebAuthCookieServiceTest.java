package uz.hemis.web.service;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebAuthCookieService")
class WebAuthCookieServiceTest {

    private final WebAuthCookieService service = createService(false, "Lax");

    @Test
    @DisplayName("setAuthCookies sets both access and refresh cookies")
    void setAuthCookies() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        service.setAuthCookies(response, "access-token-123", "refresh-token-456");

        Cookie access = response.getCookie("accessToken");
        Cookie refresh = response.getCookie("refreshToken");

        assertThat(access).isNotNull();
        assertThat(access.getValue()).isEqualTo("access-token-123");
        assertThat(access.isHttpOnly()).isTrue();
        assertThat(access.getMaxAge()).isEqualTo(900);

        assertThat(refresh).isNotNull();
        assertThat(refresh.getValue()).isEqualTo("refresh-token-456");
        assertThat(refresh.isHttpOnly()).isTrue();
        assertThat(refresh.getMaxAge()).isEqualTo(604800);
    }

    @Test
    @DisplayName("clearAuthCookies sets maxAge=0 for both cookies")
    void clearAuthCookies() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        service.clearAuthCookies(response);

        Cookie access = response.getCookie("accessToken");
        Cookie refresh = response.getCookie("refreshToken");

        assertThat(access).isNotNull();
        assertThat(access.getMaxAge()).isEqualTo(0);
        assertThat(refresh).isNotNull();
        assertThat(refresh.getMaxAge()).isEqualTo(0);
    }

    private static WebAuthCookieService createService(boolean secure, String sameSite) {
        WebAuthCookieService svc = new WebAuthCookieService();
        try {
            Field secureField = WebAuthCookieService.class.getDeclaredField("cookieSecure");
            secureField.setAccessible(true);
            secureField.set(svc, secure);
            Field sameSiteField = WebAuthCookieService.class.getDeclaredField("cookieSameSite");
            sameSiteField.setAccessible(true);
            sameSiteField.set(svc, sameSite);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return svc;
    }
}
