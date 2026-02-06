plugins {
    id("java-library")
}

group = "uz.hemis"

dependencies {
    // Shared modules
    implementation(project(":common"))
    implementation(project(":domain"))

    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-aspects")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Redis for distributed cache (L2 - shared across pods)
    implementation("org.springframework.boot:spring-boot-starter-data-redis") {
        exclude(group = "io.lettuce", module = "lettuce-core")
    }
    implementation("redis.clients:jedis")

    // Caffeine for L1 JVM cache (per-pod, ultra-fast)
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Security (for SecurityContextHolder in audit trail)
    implementation("org.springframework.security:spring-security-core")

    // Swagger/OpenAPI annotations for DTOs
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-api:3.0.1")

    // Lombok (MUST be before MapStruct)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // MapStruct for Entity ↔ DTO mapping
    val mapstructVersion = "1.6.3"
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

    // Lombok + MapStruct integration (MUST be after both)
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

// =====================================================
// Annotation Processor Configuration for MapStruct
// =====================================================
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-Amapstruct.defaultComponentModel=spring",  // Generate @Component
            "-Amapstruct.unmappedTargetPolicy=WARN"     // Warn on unmapped fields
        )
    )
}
