// =====================================================
// HEMIS Root Build Configuration
// =====================================================
// Stack: Spring Boot 3.5.7 + JDK 25 LTS + Gradle 8.10.2
// Mode: NO-RENAME • NO-DELETE • NO-BREAKING-CHANGES
// =====================================================

plugins {
    java
    id("org.springframework.boot") version "3.5.7" apply false
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
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.7")
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
    // JDK 25 LTS Toolchain
    // =====================================================
    // CRITICAL: Boot 3.5.7 supports Java 17-25
    // If third-party libs fail on JDK 25, fallback to 21 LTS
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
// Java: JDK 25 LTS
// Gradle: 8.10.2
// Spring Boot: 3.5.7
// Spring Framework: 6.2.x (via Boot)
// PostgreSQL Driver: Managed by BOM
// Flyway: Managed by BOM
// Hibernate: Managed by BOM
// Jackson: Managed by BOM
// Validation API: Managed by BOM
// =====================================================
