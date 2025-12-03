// =====================================================
// HEMIS Root Build Configuration
// =====================================================
// Stack: Spring Boot 3.5.8 + JDK 21 LTS + Gradle 9.2.1
// Mode: NO-RENAME • NO-DELETE • NO-BREAKING-CHANGES
// =====================================================

plugins {
    java
    id("org.springframework.boot") version "3.5.8" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

// Ensure Netty version is pinned (protect against external gradle.properties overrides)
extra["netty.version"] = "4.1.115.Final"

// =====================================================
// Project Metadata
// =====================================================

allprojects {
    group = "uz.hemis"
    version = "1.0.0"
}

// =====================================================
// Subproject Configuration
// =====================================================

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")

    // Import for dependency management extension
    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.8")
            mavenBom("io.netty:netty-bom:4.1.115.Final")
        }
    }
    
    // Fix JAXB dependencies globally
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.glassfish.jaxb") {
                useVersion("4.0.5")
                because("Fix Hibernate 6.6 JAXB version conflict")
            }
        }
    }

    // =====================================================
    // JDK 21 LTS Toolchain
    // =====================================================
    // CRITICAL: Boot 3.5.8 supports Java 17-24
    // Using JDK 21 LTS for long-term stability
    // =====================================================

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    // =====================================================
    // Common Dependencies (All Modules)
    // =====================================================

    dependencies {
        // Lombok - reduce boilerplate
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        // Testing - JUnit 5 + Spring Boot Test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    // =====================================================
    // Compiler Configuration
    // =====================================================

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(
            listOf(
                "-parameters",           // Preserve parameter names for Spring
                "-Xlint:unchecked",     // Warn about unchecked operations
                "-Xlint:deprecation"    // Warn about deprecated API usage
            )
        )
    }

    // =====================================================
    // Test Configuration
    // =====================================================

    tasks.withType<Test> {
        useJUnitPlatform()

        // Load .env vars for tests and gate by TESTS_ENABLED
        val envFile = rootProject.file(".env")
        val envMap = mutableMapOf<String, String>()
        if (envFile.exists()) {
            envFile.readLines()
                .filter { it.isNotBlank() && !it.trim().startsWith("#") && it.contains("=") }
                .forEach { line ->
                    val (k, v) = line.split("=", limit = 2)
                    envMap[k.trim()] = v.trim()
                }
        }
        environment(envMap)

        val testsEnabled = (System.getenv("TESTS_ENABLED") ?: envMap["TESTS_ENABLED"] ?: "false")
            .equals("true", ignoreCase = true)
        if (!testsEnabled) {
            doFirst {
                throw GradleException("Test qilish o'chirilgan. TESTS_ENABLED=true ni .env da yoqing.")
            }
        }

        // Parallel test execution
        maxParallelForks = Runtime.getRuntime().availableProcessors()

        // Test logging
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = false
        }
    }
}

// =====================================================
// Root Project Tasks
// =====================================================

tasks.register("cleanAll") {
    group = "build"
    description = "Clean all subprojects"
    dependsOn(subprojects.map { it.tasks.named("clean") })
}

tasks.register("buildAll") {
    group = "build"
    description = "Build all subprojects"
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.register("testAll") {
    group = "verification"
    description = "Run tests in all subprojects"
    dependsOn(subprojects.map { it.tasks.named("test") })
}

// =====================================================
// Technology Stack (Managed by Spring Boot BOM)
// =====================================================
// Java: JDK 21 LTS
// Gradle: 9.2.1
// Spring Boot: 3.5.8
// Spring Framework: 6.2.x (via Boot)
// PostgreSQL Driver: Managed by BOM
// Liquibase: Managed by BOM
// Hibernate: Managed by BOM
// Jackson: Managed by BOM
// Validation API: Managed by BOM
// =====================================================
