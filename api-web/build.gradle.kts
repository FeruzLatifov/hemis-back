plugins {
    id("java-library")
}

group = "uz.hemis"

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":service"))
    implementation(project(":security"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // ⭐ Sentry (compileOnly - runtime comes from :app module)
    compileOnly("io.sentry:sentry-spring-boot-4:8.40.0")
    
    // Apache POI for Excel export
    implementation("org.apache.poi:poi:5.5.1")
    implementation("org.apache.poi:poi-ooxml:5.5.1")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(project(":app"))
}

tasks.test {
    useJUnitPlatform()
}
