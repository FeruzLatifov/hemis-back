rootProject.name = "hemis"

// =====================================================
// Multi-Module Configuration
// =====================================================
// CRITICAL: Module names must match directory names exactly
// NO RENAME - module structure is frozen for stability
// =====================================================

include(
    "common",                   // Shared utilities, DTOs, Port interfaces
    "security",                 // JWT OAuth2 Resource Server
    "domain",                   // JPA entities + Port adapters + Liquibase
    // "infrastructure-persistence", // DISABLED: Kotlin plugin issue, migrations in domain module
    "service",                  // Service layer + Cache adapters
    "api-legacy",               // 🎯 Legacy CUBA-compatible APIs (/app/rest/*)
    "api-web",                  // 🎯 Modern Web APIs (/api/v1/web/*)
    "api-external",             // 🎯 S2S integrations (gov, education, finance)
    "api-university",           // 🎯 University APIs (/api/v1/university/*)
    "app"                       // Main Spring Boot application
)

// =====================================================
// Gradle Features
// =====================================================

// Type-safe project accessors are default since Gradle 9

// =====================================================
// Dependency Resolution Management
// =====================================================

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()
    }
}

// =====================================================
// Toolchain Management (Fix for Liquibase tasks)
// =====================================================

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// =====================================================
// Module Structure (Clean Architecture v2.0.0)
// =====================================================
// hemis/
// ├── common/                  → DTOs, Port interfaces, Exceptions
// ├── security/                → JWT + OAuth2 (uses common ports)
// ├── domain/                  → JPA entities + Port adapters
// ├── service/                 → Business logic + Cache adapters
// ├── api-legacy/              → 🎯 CUBA entity APIs /app/rest/*
// ├── api-web/                 → 🎯 Modern Web APIs /api/v1/web/*
// ├── api-external/            → 🎯 S2S integrations
// ├── api-university/          → 🎯 University APIs /api/v1/university/*
// └── app/                     → Main Spring Boot application
// =====================================================
