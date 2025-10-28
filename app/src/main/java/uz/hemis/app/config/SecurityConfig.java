package uz.hemis.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uz.hemis.app.security.RateLimitFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration
 *
 * <p><strong>CRITICAL - Production Security:</strong></p>
 * <ul>
 *   <li>JWT token validation for all API endpoints</li>
 *   <li>OAuth2 resource server configuration</li>
 *   <li>University-based authorization</li>
 *   <li>CORS configuration for trusted domains</li>
 * </ul>
 *
 * <p><strong>Security Features:</strong></p>
 * <ul>
 *   <li>Stateless sessions (no server-side session storage)</li>
 *   <li>JWT token validation on every request</li>
 *   <li>Method-level security with @PreAuthorize</li>
 *   <li>CSRF disabled (stateless API)</li>
 * </ul>
 *
 * <p><strong>Configuration:</strong></p>
 * <pre>
 * # application.properties
 * spring.security.oauth2.resourceserver.jwt.issuer-uri=https://auth.hemis.uz/realms/hemis
 * spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://auth.hemis.uz/realms/hemis/protocol/openid-connect/certs
 * </pre>
 *
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final RateLimitFilter rateLimitFilter;

    /**
     * Main security filter chain
     *
     * <p><strong>Security Rules:</strong></p>
     * <ul>
     *   <li>All /app/rest/v2/services/** endpoints require authentication</li>
     *   <li>JWT token must be valid and not expired</li>
     *   <li>Actuator endpoints require ADMIN role</li>
     *   <li>Health check endpoints are public</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (stateless API with JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session management (JWT-based)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints (health checks)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // CUBA REST API endpoints - require authentication
                        .requestMatchers("/app/rest/v2/services/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // OAuth2 Resource Server (JWT)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                        )
                )

                // Add rate limiting filter before authentication
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWT Decoder configuration
     *
     * <p>Validates JWT tokens from OAuth2 authorization server</p>
     *
     * @return JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Use JWK Set URI from OAuth2 authorization server
        return NimbusJwtDecoder
                .withJwkSetUri(securityProperties.getJwkSetUri())
                .build();
    }

    /**
     * CORS configuration
     *
     * <p><strong>Allowed Origins:</strong></p>
     * <ul>
     *   <li>University frontend applications</li>
     *   <li>Admin dashboard</li>
     *   <li>Mobile applications</li>
     * </ul>
     *
     * <p><strong>Security:</strong></p>
     * <ul>
     *   <li>Only specific origins allowed (no wildcard *)</li>
     *   <li>Credentials allowed (cookies, authorization headers)</li>
     *   <li>Preflight requests cached for performance</li>
     * </ul>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins from configuration
        configuration.setAllowedOrigins(securityProperties.getAllowedOrigins());

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "X-University-Code"
        ));

        // Expose headers
        configuration.setExposedHeaders(Arrays.asList(
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size"
        ));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/app/rest/v2/services/**", configuration);
        source.registerCorsConfiguration("/actuator/**", configuration);

        return source;
    }
}
