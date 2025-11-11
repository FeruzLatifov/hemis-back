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

    // App module for testing (provides @SpringBootApplication)
    testImplementation(project(":app"))
    
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.test {
    useJUnitPlatform()
}
