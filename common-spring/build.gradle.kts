// =====================================================
// HEMIS Common-Spring Module
// =====================================================
// Purpose: Spring-specific utilities and converters
// Dependencies: common (pure POJOs), Spring Framework
// =====================================================

dependencies {
    // Common module (pure POJOs)
    api(project(":common"))

    // Spring Data Commons (for Page interface)
    api("org.springframework.data:spring-data-commons")

    // Spring Context (for @Component if needed)
    implementation("org.springframework:spring-context")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// =====================================================
// Module Role
// =====================================================
// - Spring-specific converters (Page → PageResponse)
// - Spring-specific utilities
// - Bridge between common POJOs and Spring Framework
//
// CRITICAL: This module bridges common (framework-agnostic)
// with Spring Framework. Services should use this module
// when they need Spring-specific functionality.
// =====================================================
