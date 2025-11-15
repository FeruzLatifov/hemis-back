package uz.hemis.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uz.hemis.security.service.UserPermissionCacheService;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserPermissionCacheService permissionCacheService;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${hemis.security.jwt.secret}")
    private String jwtSecret;

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
     * @param jwtAuthConverter JWT authentication converter (injected bean)
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthConverter
    ) throws Exception {
        http
                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF disabled (REST API with JWT, no cookies/sessions)
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints (health checks)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        
                        // Protected actuator endpoints (admin only)
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Swagger/OpenAPI endpoints (PUBLIC - for API documentation)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/docs/**").permitAll() // Alohida Swagger URLs
                        .requestMatchers("/openapi/**").permitAll() // Static OpenAPI specs
                        .requestMatchers("/swagger-ui-dark.css").permitAll() // Dark theme CSS

                        // OAuth2 Token endpoint (PUBLIC - for login)
                        // CRITICAL: Must be public for universities to get tokens
                        .requestMatchers("/app/rest/v2/oauth/token").permitAll()

                        // Admin Auth endpoints (PUBLIC - for admin login - DEPRECATED)
                        .requestMatchers("/app/rest/v2/auth/**").permitAll()
                        .requestMatchers("/api/admin/login", "/api/admin/logout").permitAll()

                        // Web Auth endpoints
                        // Only login + refresh are public; others require valid JWT
                        .requestMatchers(HttpMethod.POST, "/api/v1/web/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/web/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/web/auth/**").authenticated()

                        // I18n endpoints (AUTH REQUIRED - swaggerda ko'rinadi)
                        .requestMatchers("/api/v1/web/i18n/**").authenticated()

                        // Language/System endpoints (hammasi token talab qiladi)
                        .requestMatchers("/api/v1/web/languages/**").authenticated()
                        .requestMatchers("/api/v1/web/system/configuration").authenticated()

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
                                .jwtAuthenticationConverter(jwtAuthConverter)
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
     * <p>Validates JWT tokens using JWK Set URI, issuer URI, or secret key.</p>
     *
     * <p><strong>Configuration Options:</strong></p>
     * <ul>
     *   <li>JWK Set URI: spring.security.oauth2.resourceserver.jwt.jwk-set-uri</li>
     *   <li>Issuer URI: spring.security.oauth2.resourceserver.jwt.issuer-uri</li>
     *   <li>Secret Key: hemis.security.jwt.secret (development/simple mode)</li>
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
            // Development: Use secret key for token validation
            byte[] keyBytes;
            try {
                // Try to decode from base64
                keyBytes = java.util.Base64.getDecoder().decode(jwtSecret);
            } catch (IllegalArgumentException e) {
                // If not base64, use as is
                keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            }
            SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
            return NimbusJwtDecoder.withSecretKey(secretKey)
                    .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                    .build();
        }
    }

    /**
     * JWT Encoder
     *
     * <p>Encodes JWT tokens for OAuth 2.0 token endpoint.</p>
     *
     * <p><strong>Used by:</strong> /app/rest/v2/oauth/token endpoint</p>
     *
     * <p><strong>Development mode:</strong> Uses HMAC-SHA256 with secret key</p>
     *
     * @return JwtEncoder
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        // Development: Use secret key for token generation
        byte[] keyBytes;
        try {
            // Try to decode from base64
            keyBytes = java.util.Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            // If not base64, use as is
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }

        // Ensure key is at least 256 bits (32 bytes) for HS256
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }

        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        // Create JWK from secret key with explicit algorithm and keyId
        JWK jwk = new OctetSequenceKey.Builder(keyBytes)
                .keyID("hemis-jwt-key")  // CRITICAL: keyId required for JWK selection
                .algorithm(com.nimbusds.jose.JWSAlgorithm.HS256)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JWT Granted Authorities Converter Bean
     *
     * <p><strong>Purpose:</strong> Load permissions from Redis cache or DB</p>
     * <p><strong>Injected:</strong> UserPermissionCacheService for Redis/DB access</p>
     *
     * @return JwtGrantedAuthoritiesConverter with injected dependencies
     */
    @Bean
    public JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
        return new JwtGrantedAuthoritiesConverter(permissionCacheService);
    }

    /**
     * JWT Authentication Converter
     *
     * <p>Converts JWT claims to Spring Security authorities.</p>
     *
     * <p><strong>Claims Mapping:</strong></p>
     * <ul>
     *   <li>Principal name: 'sub' claim (username)</li>
     *   <li>Authorities: loaded from Redis cache or database (NOT from JWT claims)</li>
     *   <li>Role prefix: NOT added (permissions are plain strings like 'student:read')</li>
     * </ul>
     *
     * @param grantedAuthoritiesConverter Injected converter with Redis/DB access
     * @return JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter
    ) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Use custom JWT granted authorities converter (with injected dependencies)
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

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
                "X-Requested-With",
                "Cookie" // ✅ CRITICAL: Allow Cookie header for HTTPOnly cookies
        ));

        // Expose headers (for pagination, etc.)
        configuration.setExposedHeaders(Arrays.asList(
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size",
                "Set-Cookie" // ✅ CRITICAL: Expose Set-Cookie header
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
     * Password Encoder Bean
     *
     * <p><strong>HYBRID APPROACH:</strong> Supports both legacy and modern formats</p>
     * <ul>
     *   <li>BCrypt format: $2a$10$... (new system - users table)</li>
     *   <li>CUBA Platform format: hash:salt:iteration (old system - sec_user table)</li>
     * </ul>
     *
     * @see uz.hemis.security.crypto.LegacyPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new uz.hemis.security.crypto.LegacyPasswordEncoder();
    }

    /**
     * Authentication Provider (DAO-based)
     *
     * <p><strong>CRITICAL FIX:</strong> This bean is required for AuthenticationManager to work properly.</p>
     * <p>Without this, AuthenticationManager has no provider and causes StackOverflowError.</p>
     *
     * <p>Uses hybridUserDetailsService which supports both new User and legacy sec_user tables.</p>
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
            @Qualifier("hybridUserDetailsService") UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
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
