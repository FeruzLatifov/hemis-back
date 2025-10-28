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
    "domain",                   // JPA entities (@Table/@Column legacy mapping)
    "api",                      // Public REST API endpoints (/app/rest/v2/*)
    "admin-api",                // Admin endpoints (optional)
    "university-api",           // 🆕 CUBA Services (OLD-HEMIS API - 90 endpoints)
    "government-integration",   // 🆕 External government APIs (6 services)
    "app"                       // Main Spring Boot application
)

// =====================================================
// Gradle Features
// =====================================================

// Enable type-safe project accessors (Gradle 7+)
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
// Module Structure (UPDATED - Modular Monolith)
// =====================================================
// hemis/
// ├── common/                  → Shared code (NO internal dependencies)
// ├── security/                → JWT + OAuth2 (depends on: common, domain)
// ├── domain/                  → JPA entities + repositories (depends on: common)
// ├── api/                     → Public REST API (depends on: common, domain, security)
// ├── admin-api/               → Admin REST API (depends on: common, domain, security)
// ├── university-api/          → 🆕 CUBA Services (90 endpoints, OLD-HEMIS compatible)
// ├── government-integration/  → 🆕 Government APIs (PINFL, Passport, BIMM, etc)
// └── app/                     → Main Boot app (depends on: ALL)
// =====================================================
