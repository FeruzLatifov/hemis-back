// =====================================================
// HEMIS Admin API Module
// =====================================================
// Purpose: Admin panel REST endpoints (optional)
// Dependencies: common, domain, security
// =====================================================

dependencies {
    // Internal dependencies
    api(project(":common"))
    api(project(":domain"))
    api(project(":security"))

    // Spring Boot Web (version from BOM)
    api("org.springframework.boot:spring-boot-starter-web")

    // Spring Data JPA (version from BOM)
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Security (version from BOM)
    api("org.springframework.boot:spring-boot-starter-security")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JWT - JSON Web Token library
    val jwtVersion = "0.12.6"
    implementation("io.jsonwebtoken:jjwt-api:${jwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jwtVersion}")

    // BCrypt password encoder (included in Spring Security)
    // No additional dependency needed

    // MapStruct for DTO mapping
    val mapstructVersion = "1.6.3"
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // SpringDoc OpenAPI (Swagger UI)
    val springdocVersion = "2.7.0"
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}")
}

// =====================================================
// Annotation Processor Configuration
// =====================================================

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=ERROR"  // Strict mapping for admin API
        )
    )
}

// =====================================================
// Module Role
// =====================================================
// - Admin REST endpoints (if needed separate from public API)
// - Service layer for admin operations
// - Request/Response DTOs
// - Admin-specific business logic
//
// CRITICAL:
// - NO DELETE endpoints (NDG - Non-Deletion Guarantee)
// - All endpoints require authentication (JWT)
// - Role-based access control (ADMIN role)
// - Audit logging for all operations
// =====================================================
