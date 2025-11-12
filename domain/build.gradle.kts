// =====================================================
// HEMIS Domain Module
// =====================================================
// Purpose: JPA entities mapped to legacy ministry.sql schema
// Dependencies: common
// =====================================================

dependencies {
    // Internal dependencies
    api(project(":common"))

    // Spring Data JPA (version from BOM)
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // PostgreSQL Driver (version from BOM)
    runtimeOnly("org.postgresql:postgresql")

    // Liquibase for migrations with native rollback support (version from BOM)
    implementation("org.liquibase:liquibase-core")
    // PostgreSQL driver already included via runtimeOnly above

    // Hibernate (version from BOM via spring-boot-starter-data-jpa)
    // No need to declare explicitly

    // Lombok (MUST be before MapStruct)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // MapStruct for Entity ↔ DTO mapping
    // CRITICAL: Version not in BOM, must be explicit
    val mapstructVersion = "1.6.3"
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

    // Lombok + MapStruct integration (MUST be after both)
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Spring Boot Validation
    api("org.springframework.boot:spring-boot-starter-validation")

    // Redis moved to app module (domain layer doesn't need caching)
    // implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // implementation("org.springframework.boot:spring-boot-starter-cache")
}

// =====================================================
// Annotation Processor Configuration
// =====================================================

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-Amapstruct.defaultComponentModel=spring",  // Generate @Component
            "-Amapstruct.unmappedTargetPolicy=WARN"     // Warn on unmapped fields
        )
    )
}

// =====================================================
// Module Role
// =====================================================
// - JPA Entity classes with @Table(name="hemishe_e_*")
// - @Column(name="legacy_name") for all fields
// - Spring Data JPA repositories (NO delete methods)
// - Base entity classes (audit columns pattern)
// - MapStruct mappers for Entity ↔ DTO conversion
//
// CRITICAL CONSTRAINTS:
// - Table/column names MUST match ministry.sql exactly
// - NO schema generation (ddl-auto=none)
// - NO DELETE operations (repository methods prohibited)
// - Liquibase migrations ONLY (Flyway disabled)
// =====================================================
