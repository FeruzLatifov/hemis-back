// =====================================================
// HEMIS Main Application Module
// =====================================================
// Purpose: Spring Boot main application
// Dependencies: ALL modules
// =====================================================

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Internal dependencies - include ALL modules
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":security"))
    implementation(project(":admin-api"))

    // Spring Boot Starters (versions from BOM)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // PostgreSQL Driver
    runtimeOnly("org.postgresql:postgresql")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // HikariCP (connection pooling - included in spring-boot-starter-data-jpa)
    // No need to declare explicitly

    // Development Tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Configuration Processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")  // For integration tests
}

// =====================================================
// Boot JAR Configuration
// =====================================================

tasks.bootJar {
    archiveFileName.set("hemis-${project.version}.jar")
    mainClass.set("uz.hemis.app.HemisApplication")
}

tasks.bootRun {
    // Set active Spring profile from command line
    // Usage: ./gradlew :app:bootRun -Pprofile=prod
    systemProperty("spring.profiles.active", findProperty("profile") ?: "dev")

    // JVM arguments for development
    jvmArgs = listOf(
        "-Xms512m",
        "-Xmx1024m",
        "-XX:+UseG1GC"
    )
}

// =====================================================
// Custom Tasks
// =====================================================

tasks.register("bootRunProd") {
    group = "application"
    description = "Run the application with production profile"

    doFirst {
        tasks.bootRun.get().systemProperty("spring.profiles.active", "prod")
    }

    finalizedBy(tasks.bootRun)
}

// =====================================================
// Module Role
// =====================================================
// - Main Spring Boot application class (@SpringBootApplication)
// - Application configuration (DataSource, CORS, etc.)
// - REST controllers for legacy API endpoints
// - Exception handlers (@ControllerAdvice)
// - Health checks and metrics
//
// CRITICAL:
// - Runs on port 8080 (legacy CUBA port for compatibility)
// - Connects to ministry.sql database (existing schema)
// - Flyway runs V1__baseline.sql only (no DDL changes)
// - Preserves ALL existing API endpoints (URLs, methods, JSON)
// =====================================================
