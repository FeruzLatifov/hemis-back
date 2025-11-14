plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    // Domain layer - business models and ports
    api(project(":domain"))
    implementation(project(":common"))

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    
    // Database drivers
    runtimeOnly("org.postgresql:postgresql")
    
    // Connection pooling
    implementation("com.zaxxer:HikariCP")
    
    // Database migration
    implementation("org.liquibase:liquibase-core")
    
    // Utilities
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
