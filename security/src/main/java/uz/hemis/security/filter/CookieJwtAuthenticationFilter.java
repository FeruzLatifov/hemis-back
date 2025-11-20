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
import uz.hemis.security.service.TokenBlacklistService;

import java.io.IOException;

/**
 * Cookie-based JWT Authentication Filter with Token Blacklist Support
 *
 * <p><strong>Industry Best Practice - JWT Authentication with Revocation</strong></p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Extracts JWT from Authorization header or HTTPOnly cookie</li>
 *   <li>Validates JWT signature and expiry</li>
 *   <li>Checks token blacklist (logout revocation)</li>
 *   <li>Sets Spring Security authentication context</li>
 * </ul>
 *
 * <p><strong>Token Sources (Priority Order):</strong></p>
 * <ol>
 *   <li>Authorization header: {@code Authorization: Bearer <token>}</li>
 *   <li>Cookie: {@code accessToken=<token>}</li>
 * </ol>
 *
 * <p><strong>Security Flow:</strong></p>
 * <pre>
 * 1. Extract token from request (header/cookie)
 * 2. Decode and validate JWT signature
 * 3. Extract JTI (JWT ID) from token
 * 4. Check if JTI is blacklisted (Redis)
 * 5. If blacklisted → reject (logged out token)
 * 6. If valid → set authentication in SecurityContext
 * </pre>
 *
 * <p><strong>Performance:</strong></p>
 * <ul>
 *   <li>JWT decode: ~1ms</li>
 *   <li>Redis blacklist check: ~1ms</li>
 *   <li>Total overhead: ~2-3ms per authenticated request</li>
 * </ul>
 *
 * @author HEMIS Development Team
 * @since 2.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CookieJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistService tokenBlacklistService;

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
                // 1. Decode and validate JWT signature
                Jwt jwt = jwtDecoder.decode(token);

                // 2. Extract JTI (JWT ID) from token
                String jti = jwt.getId();

                // 3. Check if token is blacklisted (logout revocation)
                if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
                    log.warn("🚨 Blacklisted token attempt: jti={}, subject={}", jti, jwt.getSubject());
                    // Don't set authentication → request will be treated as unauthenticated
                    // This is correct behavior: logged-out tokens should not grant access
                } else {
                    // 4. Token is valid and not blacklisted → authenticate
                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("✅ JWT authentication successful: subject={}, jti={}", jwt.getSubject(), jti);
                }
            }
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error during JWT authentication", e);
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
