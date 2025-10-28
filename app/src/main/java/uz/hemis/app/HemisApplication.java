package uz.hemis.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * HEMIS Main Application
 *
 * <p>Spring Boot 3.5.7 + JDK 25 LTS refactoring of HEMIS backend.</p>
 *
 * <p><strong>CRITICAL CONSTRAINTS:</strong></p>
 * <ul>
 *   <li>API Contract FROZEN - no breaking changes to URLs, JSON, status codes</li>
 *   <li>Database Schema FROZEN - no DROP, RENAME, TRUNCATE operations</li>
 *   <li>Replication Safe - schema changes prohibited</li>
 *   <li>NDG (Non-Deletion Guarantee) - no physical DELETE queries</li>
 * </ul>
 *
 * <p><strong>Module Scanning:</strong></p>
 * <ul>
 *   <li>Components: uz.hemis (all modules)</li>
 *   <li>Entities: uz.hemis.domain.entity</li>
 *   <li>Repositories: uz.hemis.domain.repository</li>
 * </ul>
 *
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
    "uz.hemis.common",
    "uz.hemis.security",
    "uz.hemis.domain",
    "uz.hemis.admin",
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
