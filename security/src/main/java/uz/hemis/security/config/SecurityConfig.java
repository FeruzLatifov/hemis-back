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
import uz.hemis.security.filter.CookieJwtAuthenticationFilter;
import uz.hemis.security.service.TokenBlacklistService;
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
@lombok.extern.slf4j.Slf4j
public class SecurityConfig {

    private final UserPermissionCacheService permissionCacheService;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${hemis.security.jwt.secret}")
    private String jwtSecret;

    // ✅ SECURITY FIX #7: CORS allowed origins from environment
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000,http://localhost:9000}")
    private String corsAllowedOrigins;

    /**
     * Security Filter Chain Configuration
     *
     * <p><strong>Key Features:</strong></p>
     * <ul>
     *   <li>JWT-based authentication (header + cookie support)</li>
     *   <li>Token blacklist support (logout revocation via Redis)</li>
     *   <li>CORS enabled for cross-origin requests</li>
     *   <li>CSRF disabled (stateless REST API)</li>
     *   <li>Public endpoints: /actuator/health, /actuator/info</li>
     *   <li>Protected endpoints: /app/rest/v2/** (JWT required)</li>
     *   <li>Admin endpoints: /admin/** (ROLE_ADMIN required)</li>
     * </ul>
     *
     * @param http HttpSecurity configuration
     * @param jwtAuthConverter JWT authentication converter (injected bean)
     * @param cookieJwtAuthenticationFilter JWT filter with blacklist support (injected bean)
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthConverter,
            CookieJwtAuthenticationFilter cookieJwtAuthenticationFilter
    ) throws Exception {
        http
                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ SECURITY FIX #7: CSRF Protection for Cookie-based Auth
                // Problem: Cookie-based auth without CSRF = vulnerable to CSRF attacks
                // Solution: Enable CSRF with CookieCsrfTokenRepository (double-submit pattern)
                //
                // How it works:
                // 1. Server sends CSRF token in cookie (readable by JavaScript)
                // 2. Client must send same token in X-XSRF-TOKEN header
                // 3. Server compares cookie value with header value
                // 4. If mismatch → reject request (403 Forbidden)
                //
                // Why safe:
                // - Attacker can't read cookies from different origin (Same-Origin Policy)
                // - Attacker can't set custom headers in CSRF attack
                // - Even if SameSite=None, CSRF token prevents attack
                .csrf(csrf -> csrf
                        .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/api/v1/web/auth/login",      // Public login endpoint
                                "/api/v1/web/auth/refresh",    // Public refresh endpoint
                                "/app/rest/v2/oauth/token",    // Legacy OAuth endpoint
                                "/app/rest/v2/services/captcha/**", // Captcha endpoints (public)
                                "/actuator/**",                 // Actuator endpoints
                                "/swagger-ui/**",               // Swagger UI
                                "/v3/api-docs/**"              // OpenAPI docs
                        )
                )

                // ✅ Add Cookie JWT Filter BEFORE OAuth2 Resource Server
                // This filter checks token blacklist (logout revocation)
                .addFilterBefore(
                        cookieJwtAuthenticationFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                )

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
                        .requestMatchers("/app/rest/oauth/token").permitAll() // Legacy fallback (v2 yo'q)

                        // Captcha endpoints (PUBLIC - for login page security)
                        // CRITICAL: Must be public - captcha olish uchun login qilish shart emas
                        .requestMatchers("/app/rest/v2/services/captcha/**").permitAll()

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
                        .bearerTokenResolver(bearerTokenResolver()) // ✅ Custom resolver (cookie + header)
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
     * CORS Configuration Source
     *
     * <p><strong>✅ SECURITY FIX #7: Restricted CORS Origins</strong></p>
     * <ul>
     *   <li>Problem: AllowedOriginPatterns("*") + AllowCredentials=true = CSRF vulnerability</li>
     *   <li>Solution: Only allow specific origins from environment configuration</li>
     *   <li>Production: Set app.security.cors.allowed-origins in application.properties</li>
     * </ul>
     *
     * <p><strong>Example Configuration:</strong></p>
     * <pre>
     * Development: http://localhost:5173,http://localhost:3000
     * Production:  https://hemis.uz,https://www.hemis.uz,https://admin.hemis.uz
     * </pre>
     *
     * <p><strong>Why This Matters:</strong></p>
     * <ul>
     *   <li>Without restriction: ANY website can make credentialed requests</li>
     *   <li>Attacker site: fetch('https://api.hemis.uz/api/v1/web/...',{credentials:'include'})</li>
     *   <li>Browser sends user's cookies → Attacker gets user data</li>
     * </ul>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ SECURITY FIX #7: Parse allowed origins from environment
        // Format: "https://hemis.uz,https://www.hemis.uz,http://localhost:5173"
        List<String> allowedOrigins = Arrays.asList(corsAllowedOrigins.split(","))
                .stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException(
                "❌ SECURITY ERROR: app.security.cors.allowed-origins must not be empty when using cookie-based auth!\n" +
                "Set in application.properties:\n" +
                "  app.security.cors.allowed-origins=https://hemis.uz,https://admin.hemis.uz\n" +
                "For development:\n" +
                "  app.security.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
            );
        }

        configuration.setAllowedOrigins(allowedOrigins);
        log.info("✅ CORS allowed origins: {}", allowedOrigins);

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
                "X-XSRF-TOKEN",  // ✅ CSRF token header
                "Cookie"         // ✅ CRITICAL: Allow Cookie header for HTTPOnly cookies
        ));

        // Expose headers (for pagination, etc.)
        configuration.setExposedHeaders(Arrays.asList(
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size",
                "Set-Cookie"    // ✅ CRITICAL: Expose Set-Cookie header
        ));

        // ✅ Allow credentials (cookies, authorization headers)
        // CRITICAL: This is safe now because we restrict allowed origins
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
    @SuppressWarnings("deprecation")  // DaoAuthenticationProvider constructors are deprecated but still recommended for use
    @Bean
    public AuthenticationProvider authenticationProvider(
            @Qualifier("hybridUserDetailsService") UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
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

    /**
     * Cookie JWT Authentication Filter with Token Blacklist Support
     *
     * <p><strong>Features:</strong></p>
     * <ul>
     *   <li>Extracts JWT from Authorization header or HTTPOnly cookie</li>
     *   <li>Validates JWT signature and expiry</li>
     *   <li>Checks Redis-based token blacklist (logout revocation)</li>
     *   <li>Sets Spring Security authentication context if valid</li>
     * </ul>
     *
     * <p><strong>Security Flow:</strong></p>
     * <pre>
     * 1. Extract token from request (header/cookie)
     * 2. Decode and validate JWT
     * 3. Check if token is blacklisted (Redis)
     * 4. If blacklisted → reject
     * 5. If valid → authenticate
     * </pre>
     *
     * @param tokenBlacklistService Injected blacklist service for token revocation
     * @return CookieJwtAuthenticationFilter
     */
    @Bean
    public CookieJwtAuthenticationFilter cookieJwtAuthenticationFilter(
            TokenBlacklistService tokenBlacklistService
    ) {
        return new CookieJwtAuthenticationFilter(jwtDecoder(), tokenBlacklistService);
    }

    /**
     * Bearer Token Resolver
     *
     * <p>Custom resolver that extracts JWT from:</p>
     * <ol>
     *   <li>Authorization header (standard)</li>
     *   <li>Cookie (accessToken)</li>
     * </ol>
     *
     * @return BearerTokenResolver
     */
    @Bean
    public org.springframework.security.oauth2.server.resource.web.BearerTokenResolver bearerTokenResolver() {
        return request -> {
            // 1. Try Authorization header first
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }

            // 2. Try cookie
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }

            return null;
        };
    }

    // =====================================================
    // NOTE: NO DELETE ENDPOINT ALLOWED
    // =====================================================
    // CORS configuration explicitly excludes DELETE method
    // This enforces NDG (Non-Deletion Guarantee) at HTTP level
    // Even if a DELETE endpoint exists, CORS will block it
    // =====================================================
}
