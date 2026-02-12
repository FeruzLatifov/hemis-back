package uz.hemis.web.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * HTTPOnly cookie management for Web Authentication.
 *
 * @since 2.0.0
 */
@Service
public class WebAuthCookieService {

    private static final int ACCESS_MAX_AGE = 900;       // 15 minutes
    private static final int REFRESH_MAX_AGE = 604800;   // 7 days

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.security.cookie.same-site:Strict}")
    private String cookieSameSite;

    /**
     * Set access + refresh token HTTPOnly cookies on the response.
     */
    public void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addCookie(buildCookie("accessToken", accessToken, ACCESS_MAX_AGE));
        response.addCookie(buildCookie("refreshToken", refreshToken, REFRESH_MAX_AGE));
    }

    /**
     * Clear auth cookies (on logout).
     */
    public void clearAuthCookies(HttpServletResponse response) {
        response.addCookie(buildCookie("accessToken", null, 0));
        response.addCookie(buildCookie("refreshToken", null, 0));
    }

    private Cookie buildCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", cookieSameSite);
        return cookie;
    }
}
