/*
 * ============================================================================
 * UNIVERSITY API MODULE - BUILD CONFIGURATION
 * ============================================================================
 *
 * VAZIFASI:
 * ---------
 * Bu module OLD-HEMIS da ishlatilgan CUBA Services pattern'ini Spring Boot 3.5.7 ga
 * portlash uchun mo'ljallangan. 200 ta universitet bu API'larni ishlatadi.
 *
 * QANDAY KOD JOYLASHGAN:
 * ----------------------
 * 1. ServicesController.java - 90 ta CUBA endpoint (REST API)
 *    - URL Pattern: /app/rest/v2/services/hemishe_{ServiceName}/{methodName}
 *    - Example: /app/rest/v2/services/hemishe_StudentService/verify
 *
 * 2. CUBA Service Classes - 9 ta service class:
 *    - StudentCubaService.java - Talaba ma'lumotlarini boshqarish
 *    - TeacherCubaService.java - O'qituvchi ma'lumotlarini boshqarish
 *    - ReferenceDataCubaService.java - Reference ma'lumotlar
 *    - ClassifiersCubaService.java - Klassifikatorlar (fakultet, yo'nalish, etc)
 *    - DocumentCubaService.java - Hujjatlar (diplom, shartnoma, etc)
 *    - IntegrationCubaService.java - Tashqi integratsiyalar
 *    - UtilityCubaService.java - Yordamchi funksiyalar
 *    - SessionTokenService.java - Session va token boshqaruvi
 *    - UniversitySettingsService.java - Universitet sozlamalari
 *
 * DEPENDENCIES:
 * -------------
 * - :common - DTO'lar, exception'lar, utility'lar
 * - :security - JWT/OAuth2 autentifikatsiya
 * - :domain - JPA entity'lar, repository'lar
 * - Spring Boot Web - REST API uchun
 * - Spring Boot Validation - Input validation uchun
 *
 * MUHIM:
 * ------
 * Bu module JAR sifatida compile qilinadi (bootJar disabled).
 * App module bu JAR'ni dependency sifatida ishlatadi.
 */

plugins {
    id("java")
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "uz.hemis"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // ============================================
    // INTERNAL MODULES (Boshqa modullar)
    // ============================================
    implementation(project(":common"))      // Shared DTO, Utils
    implementation(project(":security"))    // JWT/OAuth2
    implementation(project(":domain"))      // JPA Entities

    // ============================================
    // SPRING BOOT DEPENDENCIES
    // ============================================
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ============================================
    // LOMBOK (Boilerplate kodlarni kamaytirish)
    // ============================================
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // ============================================
    // TESTING
    // ============================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// ============================================
// JAR CONFIGURATION
// ============================================
tasks.bootJar {
    enabled = false  // Bu module executable JAR emas
}

tasks.jar {
    enabled = true   // Bu module library JAR
}
