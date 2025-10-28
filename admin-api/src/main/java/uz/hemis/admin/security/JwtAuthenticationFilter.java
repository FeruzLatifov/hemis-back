package uz.hemis.admin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 *
 * Intercepts all HTTP requests and validates JWT token
 * Sets authentication in SecurityContext if token is valid
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extract JWT from request
            String jwt = getJwtFromRequest(request);

            // 2. Validate and process token
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // 3. Extract user ID from token
                String userId = jwtTokenProvider.getUserIdFromToken(jwt);

                // 4. Create authentication token
                // Note: We store userId as principal (not UserDetails)
                // For full user details, service layer will load from database
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList() // TODO: Load authorities from database
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Set authentication for user: {}", userId);
            }

        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            // Don't throw exception, let request continue without authentication
            // Security configuration will block unauthorized requests
        }

        // 6. Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * @param request HTTP request
     * @return JWT token or null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }
}
