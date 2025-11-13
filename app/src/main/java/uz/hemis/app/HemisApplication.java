package uz.hemis.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * HEMIS Main Application v2.0.0 - Clean Architecture
 *
 * <p>Spring Boot 3.5.7 + JDK 21 LTS - Modular Monolith Architecture</p>
 *
 * <p><strong>CRITICAL CONSTRAINTS:</strong></p>
 * <ul>
 *   <li>API Contract FROZEN - no breaking changes to URLs, JSON, status codes</li>
 *   <li>Database Schema FROZEN - no DROP, RENAME, TRUNCATE operations</li>
 *   <li>Replication Safe - schema changes prohibited</li>
 *   <li>NDG (Non-Deletion Guarantee) - no physical DELETE queries</li>
 * </ul>
 *
 * <p><strong>Clean Architecture - Module Layers:</strong></p>
 * <ul>
 *   <li>common/ - Shared utilities, DTOs (no dependencies)</li>
 *   <li>domain/ - JPA entities + repositories (depends on: common)</li>
 *   <li>security/ - JWT OAuth2 Resource Server (depends on: common, domain)</li>
 *   <li>service/ - Business logic layer (depends on: domain, common)</li>
 *   <li>api-legacy/ - CUBA-compatible entity APIs (depends on: service, security)</li>
 *   <li>api-web/ - Modern Web/UI APIs (depends on: service, security)</li>
 *   <li>api-external/ - S2S integration APIs (depends on: service, security)</li>
 *   <li>app/ - Main Spring Boot application (depends on: all api-* modules)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@EnableCaching
@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication(scanBasePackages = {
    "uz.hemis.common",
    "uz.hemis.security",
    "uz.hemis.domain",
    "uz.hemis.service",
    "uz.hemis.api.legacy",     // CUBA entity APIs (/app/rest/v2/entities/*)
    "uz.hemis.api.web",        // Modern UI APIs (/app/rest/v2/*, /api/v1/web/*)
    "uz.hemis.api.external",   // S2S integrations
    "uz.hemis.app"
})
@EntityScan(basePackages = "uz.hemis.domain.entity")
@EnableJpaRepositories(basePackages = "uz.hemis.domain.repository")
public class HemisApplication {

    /**
     * Main entry point
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(HemisApplication.class, args);
    }
}
