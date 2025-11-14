package uz.hemis.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

/**
 * Test Application for Integration Tests.
 *
 * This minimal Spring Boot application is used for running integration tests
 * in the service module.
 *
 * Uses real PostgreSQL database from .env (DB_MASTER_* variables)
 * NO master/replica routing in tests (DataSourceConfig disabled with @Profile("!test"))
 *
 * Excludes external API services that require RestTemplate and external dependencies
 *
 * @author Senior Architect
 * @since 2025-11-13
 */
@SpringBootApplication
@ComponentScan(
    basePackages = {
        "uz.hemis.service",
        "uz.hemis.domain",
        "uz.hemis.common",
        "uz.hemis.security"
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = {
            "uz\\.hemis\\.service\\.external\\..*",        // External API services
            "uz\\.hemis\\.service\\..*CubaService",        // Cuba integration services
            "uz\\.hemis\\.service\\.PassportDataService",  // Passport service
            "uz\\.hemis\\.service\\.PersonalDataService",  // Personal data service
            "uz\\.hemis\\.service\\.BimmService",          // BIMM service
            "uz\\.hemis\\.service\\.SocialService",        // Social service
            "uz\\.hemis\\.service\\.GovernmentMinorApiService" // Government API
        }
    )
)
@EnableJpaRepositories(basePackages = "uz.hemis.domain.repository")
@EntityScan(basePackages = "uz.hemis.domain.entity")
public class TestApplication {

    /**
     * Mock RestTemplate bean for tests
     * (Prevents services from failing due to missing RestTemplate)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
