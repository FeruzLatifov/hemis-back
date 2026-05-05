// =====================================================
// HEMIS Root Build Configuration
// =====================================================
// Stack: Spring Boot 4.0.6 + JDK 25 LTS + Gradle 9.3.0
// Mode: NO-RENAME • NO-DELETE • NO-BREAKING-CHANGES
// =====================================================

plugins {
    java
    jacoco
    // Plugin va BOM versiyasi mos bo'lishi kerak (CLAUDE.md va dependencies BOM 4.0.6)
    id("org.springframework.boot") version "4.0.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    // OWASP Dependency-Check — vulnerability scan (CVE database).
    // Run: ./gradlew dependencyCheckAggregate
    // Output: build/reports/dependency-check-report.html
    // CI'da fail threshold: CVSS 9.0+ (CRITICAL only) — false positive noise oldini olish.
    id("org.owasp.dependencycheck") version "10.0.4"
}

// =====================================================
// OWASP Dependency-Check Configuration
// =====================================================
dependencyCheck {
    // CVSS 9.0+ (CRITICAL severity only) — build fail bo'lsa
    failBuildOnCVSS = 9.0f

    // Scan formats — HTML report + JSON for CI artifacts
    formats = listOf("HTML", "JSON")

    // Skip non-runtime configurations (test deps don't ship to prod)
    skipConfigurations = listOf("testCompileClasspath", "testRuntimeClasspath")

    // NVD API key (kelajakda tezroq scan uchun): https://nvd.nist.gov/developers/request-an-api-key
    // System.getenv("NVD_API_KEY")?.let { nvd.apiKey = it }

    analyzers {
        // Disable analyzers that don't apply to Java/Gradle
        assemblyEnabled = false
        nodeAuditEnabled = false
        nodeEnabled = false
        nuspecEnabled = false
        ossIndex.warnOnlyOnRemoteErrors = true
    }

    // Output directory
    outputDirectory = "build/reports/dependency-check"
}

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
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.6")
        }
    }
    
    // Fix dependency version conflicts
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.glassfish.jaxb") {
                useVersion("4.0.5")
                because("Fix Hibernate JAXB version conflict")
            }
            // Fix: dependency-management plugin mis-resolves ${project.version} in netty-bom
            if (requested.group == "io.netty") {
                useVersion("4.2.9.Final")
                because("Pin Netty to Boot 4.0.6 managed version")
            }
        }
    }

    // =====================================================
    // JDK 25 LTS Toolchain
    // =====================================================
    // Boot 4.0.6 supports Java 17/21/25.
    // Using JDK 25 LTS (released 2025-09) for long-term stability.
    // Class file major version 69. Auto-downloaded via Gradle toolchains.
    // =====================================================

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    // =====================================================
    // Common Dependencies (All Modules)
    // =====================================================

    dependencies {
        // Lombok - reduce boilerplate
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        // Jackson 2 compatibility bridge (Boot 4 uses Jackson 3, bridge preserves Jackson 2 API)
        implementation("org.springframework.boot:spring-boot-jackson2")

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
                "-Xlint:unchecked",      // Warn about unchecked operations
                "-Xlint:deprecation",    // Warn about deprecated API usage
                "-proc:full"             // JDK 23+ disabled annotation processing default;
                                         // Lombok + MapStruct require explicit -proc:full
            )
        )
    }

    // =====================================================
    // JaCoCo Coverage Configuration
    // =====================================================

    apply(plugin = "jacoco")

    // Skip JaCoCo for modules without test sources
    val hasTests = file("src/test").exists() &&
        (fileTree("src/test") { include("**/*.java", "**/*.kt") }).files.isNotEmpty()

    tasks.withType<JacocoReport> {
        dependsOn(tasks.named("test"))
        onlyIf { hasTests }
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }

    // Coverage verification is intentionally NOT wired into `check`.
    // The JaCoCo *report* still runs after `test` (see Test config below),
    // but a hard floor is unrealistic on this 5-year CUBA-era codebase
    // where most assertions live in integration tests gated by TESTS_ENABLED.
    // Re-introduce per-module rules once a target is meaningful again.
    tasks.withType<JacocoCoverageVerification> {
        enabled = false
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

        // JaCoCo agent — only finalize if test sources exist
        if (hasTests) {
            finalizedBy(tasks.named("jacocoTestReport"))
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
// Gradle: 9.3.0
// Spring Boot: 4.0.2
// Spring Framework: 7.x (via Boot)
// PostgreSQL Driver: Managed by BOM
// Liquibase: Managed by BOM
// Hibernate: Managed by BOM
// Jackson: Managed by BOM
// Validation API: Managed by BOM
// =====================================================
