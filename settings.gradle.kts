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
    "service",                  // Service layer
    "external",                 // 🆕 External S2S APIs (government, education, financial)
    "web",                      // 🆕 Web APIs (Admin Panel + UI CRUD operations)
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
// Module Structure (UPDATED - Modular Monolith v2.0.0)
// =====================================================
// hemis/
// ├── common/                  → Shared code (NO internal dependencies)
// ├── security/                → JWT + OAuth2 (depends on: common, domain)
// ├── domain/                  → JPA entities + repositories (depends on: common)
// ├── service/                 → Business logic layer
// ├── external/                → 🆕 S2S APIs (government, education, financial integrations)
// ├── web/                     → 🆕 Web APIs (UI CRUD operations - 140 endpoints)
// ├── admin/                   → 🆕 Admin APIs (system management)
// └── app/                     → Main Boot app (depends on: ALL)
// =====================================================
