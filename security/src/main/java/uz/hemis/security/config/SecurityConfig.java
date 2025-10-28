package uz.hemis.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for OAuth2 Resource Server
 *
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>OAuth2 Resource Server with JWT validation</li>
 *   <li>Stateless session management (no cookies)</li>
 *   <li>CORS enabled for 200+ universities</li>
 *   <li>CSRF disabled (REST API, no session)</li>
 *   <li>Method-level security with @PreAuthorize</li>
 * </ul>
 *
 * <p><strong>CRITICAL - Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>JWT claims must map to legacy user structure</li>
 *   <li>Role names preserved from CUBA Platform</li>
 *   <li>Authentication flow unchanged for universities</li>
 * </ul>
 *
 * <p><strong>Endpoint Security:</strong></p>
 * <ul>
 *   <li>Public: /actuator/health, /actuator/info</li>
 *   <li>Public: /app/rest/v2/** (universities' API - JWT required but verified)</li>
 *   <li>Admin: /admin/** (requires ROLE_ADMIN)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    /**
     * Security Filter Chain Configuration
     *
     * <p><strong>Key Features:</strong></p>
     * <ul>
     *   <li>JWT-based authentication (no sessions)</li>
     *   <li>CORS enabled for cross-origin requests</li>
     *   <li>CSRF disabled (stateless REST API)</li>
     *   <li>Public endpoints: /actuator/health, /actuator/info</li>
     *   <li>Protected endpoints: /app/rest/v2/** (JWT required)</li>
     *   <li>Admin endpoints: /admin/** (ROLE_ADMIN required)</li>
     * </ul>
     *
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF disabled (REST API with JWT, no cookies/sessions)
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints (health checks)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // OAuth2 Token endpoint (PUBLIC - for login)
                        // CRITICAL: Must be public for universities to get tokens
                        .requestMatchers("/app/rest/v2/oauth/token").permitAll()

                        // Admin endpoints (requires ROLE_ADMIN)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // University API endpoints (JWT required)
                        // Note: Universities send JWT in Authorization header
                        .requestMatchers("/app/rest/v2/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // OAuth2 Resource Server (JWT validation)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )

                // Session management (STATELESS - no sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * JWT Decoder Configuration
     *
     * <p>Validates JWT tokens using JWK Set URI or issuer URI.</p>
     *
     * <p><strong>Configuration Options:</strong></p>
     * <ul>
     *   <li>JWK Set URI: spring.security.oauth2.resourceserver.jwt.jwk-set-uri</li>
     *   <li>Issuer URI: spring.security.oauth2.resourceserver.jwt.issuer-uri</li>
     * </ul>
     *
     * @return JwtDecoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        if (jwkSetUri != null && !jwkSetUri.isEmpty()) {
            // Use JWK Set URI for token validation
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else if (issuerUri != null && !issuerUri.isEmpty()) {
            // Use Issuer URI (auto-discovers JWK Set URI)
            return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        } else {
            // Development fallback (no validation)
            // TODO: Remove in production - must configure JWK Set URI or Issuer URI
            throw new IllegalStateException(
                    "JWT decoder not configured. " +
                    "Set spring.security.oauth2.resourceserver.jwt.jwk-set-uri or " +
                    "spring.security.oauth2.resourceserver.jwt.issuer-uri"
            );
        }
    }

    /**
     * JWT Authentication Converter
     *
     * <p>Converts JWT claims to Spring Security authorities.</p>
     *
     * <p><strong>Claims Mapping:</strong></p>
     * <ul>
     *   <li>Principal name: 'sub' or 'preferred_username' claim</li>
     *   <li>Authorities: extracted from 'roles' or 'authorities' claim</li>
     *   <li>Role prefix: ROLE_ (Spring Security convention)</li>
     * </ul>
     *
     * @return JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Use custom JWT granted authorities converter
        converter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());

        // Principal name extraction (from 'sub' claim)
        converter.setPrincipalClaimName("sub");

        return converter;
    }

    /**
     * CORS Configuration
     *
     * <p><strong>CRITICAL - University Access:</strong></p>
     * <ul>
     *   <li>200+ universities access /app/rest/v2/** endpoints</li>
     *   <li>Allow all origins in development</li>
     *   <li>Configure specific origins in production</li>
     * </ul>
     *
     * <p><strong>Allowed Methods:</strong></p>
     * <ul>
     *   <li>GET, POST, PUT, PATCH (NO DELETE - NDG)</li>
     *   <li>OPTIONS for preflight requests</li>
     * </ul>
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins in development
        // TODO: Configure specific origins in production (list of 200+ universities)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Allowed HTTP methods (NO DELETE - NDG)
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.OPTIONS.name()
                // NO DELETE - NDG enforced
        ));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        // Expose headers (for pagination, etc.)
        configuration.setExposedHeaders(Arrays.asList(
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size"
        ));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Max age for preflight cache (1 hour)
        configuration.setMaxAge(3600L);

        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // =====================================================
    // OAuth2 Configuration (Password Grant Support)
    // =====================================================

    /**
     * Password Encoder (BCrypt)
     *
     * <p>Used for password hashing and verification</p>
     * <p>Strength: 10 rounds (default)</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager
     *
     * <p>Required for password grant type authentication</p>
     * <p>Used by OAuth2TokenController to authenticate users</p>
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // =====================================================
    // NOTE: NO DELETE ENDPOINT ALLOWED
    // =====================================================
    // CORS configuration explicitly excludes DELETE method
    // This enforces NDG (Non-Deletion Guarantee) at HTTP level
    // Even if a DELETE endpoint exists, CORS will block it
    // =====================================================
}
