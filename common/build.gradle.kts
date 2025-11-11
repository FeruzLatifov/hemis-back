// =====================================================
// HEMIS Common Module
// =====================================================
// Purpose: Shared utilities, DTOs, constants, exceptions
// Dependencies: NONE (base module)
// =====================================================

dependencies {
    // Spring Context (for @Component, @Service annotations)
    api("org.springframework:spring-context")

    // Spring Boot Autoconfigure (for @ConditionalOnProperty)
    api("org.springframework.boot:spring-boot-autoconfigure")

    // Spring Data Commons (for Page interface)
    api("org.springframework.data:spring-data-commons")

    // Jackson for JSON (DTO serialization with @JsonProperty)
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Jakarta Validation API
    api("jakarta.validation:jakarta.validation-api")

    // SLF4J for logging
    api("org.slf4j:slf4j-api")
}

// =====================================================
// Module Role
// =====================================================
// - DTOs with @JsonProperty for legacy JSON field names
// - Common constants (table names, status codes, etc.)
// - Utility classes (date formatters, converters, etc.)
// - Custom exceptions
// - Response wrappers (ApiResponse, PageResponse, etc.)
//
// CRITICAL: This module has NO dependencies on other internal modules.
// It can be used by all other modules without circular dependencies.
// =====================================================
