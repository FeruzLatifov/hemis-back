rootProject.name = "hemis"

// =====================================================
// Multi-Module Configuration
// =====================================================
// CRITICAL: Module names must match directory names exactly
// NO RENAME - module structure is frozen for stability
// =====================================================

include(
    "common",                   // Shared utilities, DTOs (legacy JSON field names)
    "security",                 // JWT OAuth2 Resource Server
    "domain",                   // JPA entities (@Table/@Column legacy mapping) + Liquibase migrations
    // "infrastructure-persistence", // DISABLED: Kotlin plugin issue, migrations in domain module
    "service",                  // Service layer
    "api-legacy",               // 🎯 Legacy CUBA-compatible APIs (/app/rest/*)
    "api-web",                  // 🎯 Modern Web/UI APIs (/api/v1/web/*)
    "api-external",             // 🎯 S2S integrations (gov, education, finance)
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// =====================================================
// Module Structure (Clean Architecture v2.0.0)
// =====================================================
// hemis/
// ├── common/                  → Shared code (NO internal dependencies)
// ├── security/                → JWT + OAuth2 (depends on: common, domain)
// ├── domain/                  → JPA entities + repositories (depends on: common)
// ├── service/                 → Business logic layer (depends on: domain, common)
// ├── api-legacy/              → 🎯 CUBA entity APIs /app/rest/* (56 controllers)
// ├── api-web/                 → 🎯 Modern Web APIs /api/v1/web/* (30 controllers)
// ├── api-external/            → 🎯 S2S integrations (6 controllers)
// └── app/                     → Main Boot app (5 auth/public controllers only)
// =====================================================
