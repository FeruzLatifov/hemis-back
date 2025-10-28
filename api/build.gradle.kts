// =====================================================
// HEMIS API Module
// =====================================================
// Purpose: Public REST endpoints for students, teachers, etc.
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

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Swagger/OpenAPI documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // MapStruct for DTO mapping
    val mapstructVersion = "1.6.3"
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
}

// =====================================================
// Annotation Processor Configuration
// =====================================================

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=WARN"  // Warn on unmapped fields
        )
    )
}

// =====================================================
// Module Role
// =====================================================
// - Public REST endpoints (/app/rest/v2/*)
// - Student, Teacher, Course, Exam, Attendance controllers
// - Service layer integration
// - Request/Response DTOs with legacy JSON field names
// - Swagger/OpenAPI documentation
//
// CRITICAL:
// - NO DELETE endpoints (NDG - Non-Deletion Guarantee)
// - Legacy URLs preserved (/app/rest/v2/*)
// - All endpoints require authentication (JWT)
// - Role-based access control (@PreAuthorize)
// - Backward compatible with 200+ universities
// =====================================================
