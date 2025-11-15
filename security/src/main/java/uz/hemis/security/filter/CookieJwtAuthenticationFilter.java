package uz.hemis.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Cookie-based JWT Authentication Filter
 *
 * <p>This filter extracts JWT token from cookies and validates it.</p>
 *
 * <p><strong>Token Sources (Priority Order):</strong></p>
 * <ol>
 *   <li>Authorization header: {@code Authorization: Bearer <token>}</li>
 *   <li>Cookie: {@code accessToken=<token>}</li>
 * </ol>
 *
 * @author HEMIS Development Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CookieJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            String token = extractToken(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Jwt jwt = jwtDecoder.decode(token);
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authentication successful for subject: {}", jwt.getSubject());
            }
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Try Authorization header first
        String token = extractTokenFromHeader(request);
        if (token != null) {
            log.debug("Token extracted from Authorization header");
            return token;
        }

        // 2. Try cookie
        token = extractTokenFromCookie(request);
        if (token != null) {
            log.debug("Token extracted from cookie");
            return token;
        }

        return null;
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return false; // Process all requests
    }
}
