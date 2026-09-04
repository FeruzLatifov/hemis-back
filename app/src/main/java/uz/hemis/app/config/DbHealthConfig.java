package uz.hemis.app.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.CompositeHealthContributor;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.boot.jdbc.health.DataSourceHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Which databases decide whether this pod is ready.
 *
 * <p>Spring Boot's auto-configured {@code db} contributor probes EVERY {@code DataSource} bean in the
 * context. Once the audit datasources joined the context, a blip on the audit database — a side
 * channel for the activity log, deliberately isolated in its own instance (ADR-0003) — turned the
 * readiness probe red and took the ministry API out of the load balancer, while every business
 * request would have been served perfectly well.</p>
 *
 * <p>Declaring the contributor ourselves wins: the auto-configuration backs off on
 * {@code @ConditionalOnMissingBean(name = "dbHealthContributor")}. Audit availability is still
 * observable — it just does not gate traffic.</p>
 */
@Configuration
// Same guard as DataSourceConfig: the integration harness supplies its own datasource, so the
// master/replica beans this contributor names exist only outside the test profile.
@Profile("!test")
public class DbHealthConfig {

    @Bean(name = "dbHealthContributor")
    public HealthContributor dbHealthContributor(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource) {
        Map<String, HealthContributor> business = new LinkedHashMap<>();
        business.put("master", new DataSourceHealthIndicator(masterDataSource));
        business.put("replica", new DataSourceHealthIndicator(replicaDataSource));
        return CompositeHealthContributor.fromMap(business);
    }
}
