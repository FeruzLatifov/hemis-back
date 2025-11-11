plugins {
    id("java-library")
}

group = "uz.hemis"
version = "2.0.0"

dependencies {
    // Shared modules
    implementation(project(":common"))
    implementation(project(":security"))
    implementation(project(":service"))
    implementation(project(":domain"))

    // Spring Boot Web (for REST controllers)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    // HTTP Client for external integrations (disabled due to netty conflict - will use RestTemplate for now)
    // implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.test {
    useJUnitPlatform()
}
