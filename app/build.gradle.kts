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
    // Internal dependencies - Clean Architecture (v2.0.0 Modular Monolith)
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":security"))
    implementation(project(":service"))
    // Controllers moved to api-* modules (Clean Architecture separation)
    implementation(project(":api-legacy"))      // CUBA-compatible entity APIs
    implementation(project(":api-web"))         // Modern Web/UI APIs
    implementation(project(":api-external"))    // S2S integration APIs

    // Spring Boot Starters (versions from BOM)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis") {
        exclude(group = "io.lettuce", module = "lettuce-core")
    }
    implementation("redis.clients:jedis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // PostgreSQL Driver
    runtimeOnly("org.postgresql:postgresql")

    // Liquibase runtime (align with Gradle plugin runtime)
    implementation("org.liquibase:liquibase-core:4.31.1")

    // SpringDoc OpenAPI (Swagger)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    // ⭐ Sentry Error Tracking & Performance Monitoring
    // Yangilandi: 7.0.0 → 8.16.0 (latest, BeanPostProcessor warning fixed)
    implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.16.0")
    implementation("io.sentry:sentry-logback:8.16.0")

    // HikariCP (connection pooling - included in spring-boot-starter-data-jpa)
    // No need to declare explicitly

    // Development Tools (DISABLED - causes restart loops with I18n warmup)
    // developmentOnly("org.springframework.boot:spring-boot-devtools")  // ✅ Disabled to prevent restart loops

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
    
    // Load environment variables from .env file
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") && it.contains("=") }
            .forEach { line ->
                val (key, value) = line.split("=", limit = 2)
                environment(key.trim(), value.trim())
            }
    }
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
