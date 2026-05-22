// =====================================================
// HEMIS Main Application Module
// =====================================================
// Purpose: Spring Boot main application
// Dependencies: ALL modules
// =====================================================

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    // Sentry Gradle Plugin — source context upload (stacktrace'da kod ko'rinishi).
    // Self-hosted Sentry: sentry.e-edu.uz. Source upload faqat SENTRY_AUTH_TOKEN
    // berilganda va CI=true bo'lganda — lokal dev iteratsiyani sekinlashtirmaydi.
    id("io.sentry.jvm.gradle") version "3.12.0"
}

// =====================================================
// Sentry Gradle Plugin Configuration
// =====================================================
sentry {
    // Self-hosted Sentry URL — SENTRY_URL env var orqali Sentry CLI'ga uzatiladi
    // (.env'da: SENTRY_URL=https://sentry.e-edu.uz). Plugin'da url property yo'q.

    // Organization va project (Sentry UI'da yaratilgan)
    org.set("sentry")
    projectName.set("java-spring-boot")

    // Source context — token va CI bo'lganda upload qiladi
    val sentryAuth = System.getenv("SENTRY_AUTH_TOKEN")
    val hasValidToken = !sentryAuth.isNullOrBlank() && sentryAuth != "test"
    includeSourceContext.set(hasValidToken && System.getenv("CI") != null)
    if (hasValidToken) {
        authToken.set(sentryAuth)
    }

    // SDK qo'lda dependency sifatida qo'shilgan (sentry-spring-boot-4:8.40.0).
    // autoInstallation false — version konflikt'ni oldini olish.
    autoInstallation.enabled.set(false)
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
    implementation(project(":api-university"))  // University APIs

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

    // Structured JSON logging (prod) — CloudWatch/ELK/Datadog ingestion
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // DEFERRED: Prometheus + OTel tracing — Spring Boot 4.0.2 da dep version conflicts.
    // Observability stack Spring Boot BOM stabillashgach qayta qo'shiladi.
    // Log pattern [%X{traceId:-}] tayyor — bridge qo'shilsa avtomatik ishlaydi.

    // Spring Modulith — explicit modul boundary deklaratsiyasi va verification
    implementation(platform("org.springframework.modulith:spring-modulith-bom:1.4.0"))
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")

    // DEFERRED: Togglz (feature flags) — 4.5.0 Spring Boot 3.x uchun.
    // Spring Boot 4.0 da EndpointExposure.CLOUD_FOUNDRY enum olib tashlangan,
    // Togglz auto-configuration crash qiladi. Spring Boot 4 uchun 5.x chiqqach qo'shiladi.

    // Apache HttpClient5 (for RestTemplate with custom SSL/connection pooling)
    implementation("org.apache.httpcomponents.client5:httpclient5")

    // PostgreSQL Driver
    runtimeOnly("org.postgresql:postgresql")

    // Liquibase runtime (align with Gradle plugin runtime)
    implementation("org.liquibase:liquibase-core")

    // SpringDoc OpenAPI (Swagger)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // ⭐ Sentry Error Tracking & Performance Monitoring
    // Spring Boot 4.x uchun sentry-spring-boot-4 ishlatiladi
    implementation("io.sentry:sentry-spring-boot-4:8.40.0")
    implementation("io.sentry:sentry-logback:8.40.0")

    // Micrometer Tracing — distributed tracing (traceId/spanId MDC injection).
    // Logback pattern (logback-spring.xml) %X{traceId}/%X{spanId} avtomatik to'ladi.
    // Sentry bridge — span'lar Sentry transaction'lariga ulanadi.
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.sentry:sentry-opentelemetry-bootstrap:8.40.0")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")

    // HikariCP (connection pooling - included in spring-boot-starter-data-jpa)
    // No need to declare explicitly

    // Development Tools (DISABLED - causes restart loops with I18n warmup)
    // developmentOnly("org.springframework.boot:spring-boot-devtools")  // ✅ Disabled to prevent restart loops

    // Configuration Processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")  // For light-weight unit integration tests

    // TestContainers — real Postgres/Redis in tests (Batch 26)
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    // ArchUnit — compile-time arxitektura qoidalari (modul boundary, paket naming)
    // 1.4.2: Java 25 class file (major 69) support
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.2")
}

// =====================================================
// Boot JAR Configuration
// =====================================================

tasks.bootJar {
    archiveFileName.set("hemis-${project.version}.jar")
    mainClass.set("uz.hemis.app.HemisApplication")
}

// =====================================================
// Build Info — /actuator/info endpoint uchun
// =====================================================
// Spring Boot Actuator /actuator/info endpoint'da version + build time + main class
// ko'rsatadi. Production deploy'da qaysi versiya ishlayotganini bilish uchun foydali.
springBoot {
    buildInfo {
        properties {
            additional.set(
                mapOf(
                    "module" to "hemis-back",
                    "java" to System.getProperty("java.version"),
                )
            )
        }
    }
}

tasks.bootRun {
    // Set active Spring profile from command line
    // Usage: ./gradlew :app:bootRun -Pprofile=prod
    systemProperty("spring.profiles.active", findProperty("profile") ?: "dev")

    // JVM arguments for development
    //
    // --add-opens flags: Lombok 1.18.46 sun.misc.Unsafe.objectFieldOffset orqali
    // private field'larga reflective kiradi (lombok.permit.Permit). Java 25'da
    // bu terminally-deprecated, "WARNING" stderr'ga yoziladi. --add-opens flag'lari
    // module boundary'ni explicit ochib, JVM'ning warning chiqarish mantig'ini
    // chetlab o'tadi. Lombok upstream MethodHandles.Lookup'ga o'tmaguncha vaqtinchalik.
    jvmArgs = listOf(
        "-Xms512m",
        "-Xmx1024m",
        "-XX:+UseG1GC",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"
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
