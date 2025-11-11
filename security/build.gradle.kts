// =====================================================
// HEMIS Security Module
// =====================================================
// Purpose: JWT OAuth2 Resource Server configuration
// Dependencies: common, domain
// =====================================================

dependencies {
    // Internal dependencies
    api(project(":common"))
    api(project(":domain"))

    // Spring Security (version from BOM)
    api("org.springframework.boot:spring-boot-starter-security")

    // OAuth2 Resource Server for JWT (version from BOM)
    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // OAuth2 Authorization Server (for token generation - OLD-HEMIS compatibility)
    // Spring Authorization Server 1.3.x compatible with Spring Boot 3.5.x
    api("org.springframework.security:spring-security-oauth2-authorization-server:1.3.2")

    // Spring Web (for REST controllers)
    api("org.springframework.boot:spring-boot-starter-web")

    // Spring Data JPA (for user repository)
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // SpringDoc OpenAPI for Swagger annotations
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    // Redis for permission caching (re-enabled with Jedis, excluding Lettuce to avoid netty conflicts)
    api("org.springframework.boot:spring-boot-starter-data-redis") {
        exclude(group = "io.lettuce", module = "lettuce-core")
    }
    api("redis.clients:jedis:5.1.0")
    api("org.springframework.boot:spring-boot-starter-cache")

    // Testing
    testImplementation("org.springframework.security:spring-security-test")
}

// =====================================================
// Module Role
// =====================================================
// - OAuth2 Resource Server configuration
// - JWT token validation (using spring-security-oauth2-jose)
// - JwtAuthenticationConverter for claims mapping
// - UserDetailsService implementation
// - SecurityFilterChain configuration
// - Role-based authorization (@PreAuthorize support)
//
// CRITICAL:
// - NO custom JWT library (use Spring Boot's built-in support)
// - JWT validation via JWK Set URI or public key
// - Claims mapping to legacy user structure
// - Preserve existing authentication flow
// =====================================================
