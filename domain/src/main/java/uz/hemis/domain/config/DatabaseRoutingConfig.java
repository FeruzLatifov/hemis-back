package uz.hemis.domain.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Master-Replica Database Routing Configuration
 *
 * Pattern:
 * - READ operations (SELECT) → Replica database(s)
 * - WRITE operations (INSERT/UPDATE) → Master database
 * - Soft DELETE → Master database (updates delete_ts)
 *
 * Benefits:
 * - Horizontal scaling for read-heavy workload (200+ universities)
 * - Reduce master database load
 * - High availability (if replica fails, fallback to master)
 * - Zero application code changes (uses @Transactional(readOnly))
 *
 * Configuration:
 * - spring.datasource.master.* - Master database config
 * - spring.datasource.replica.* - Replica database config(s)
 *
 * CRITICAL:
 * - Replication lag awareness needed (1-2 seconds typical)
 * - Strong consistency operations must use master
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.routing.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class DatabaseRoutingConfig {

    /**
     * Master DataSource Configuration (Write Operations)
     */
    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.master.hikari")
    public DataSource masterDataSource() {
        log.info("Configuring MASTER database for write operations");
        HikariDataSource dataSource = masterDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        dataSource.setPoolName("HEMIS-Master-Pool");
        dataSource.setMaximumPoolSize(20);  // Master handles writes
        dataSource.setMinimumIdle(5);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }

    /**
     * Replica DataSource Configuration (Read Operations)
     */
    @Bean
    @ConfigurationProperties("spring.datasource.replica")
    public DataSourceProperties replicaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.replica.hikari")
    public DataSource replicaDataSource() {
        log.info("Configuring REPLICA database for read operations");
        HikariDataSource dataSource = replicaDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        dataSource.setPoolName("HEMIS-Replica-Pool");
        dataSource.setMaximumPoolSize(50);  // Replica handles most reads
        dataSource.setMinimumIdle(10);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setReadOnly(true);  // Enforce read-only

        return dataSource;
    }

    /**
     * Routing DataSource that switches between master and replica
     */
    @Bean
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource
    ) {
        log.info("Configuring DataSource routing (Master-Replica pattern)");

        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER, masterDataSource);
        targetDataSources.put(DataSourceType.REPLICA, replicaDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);  // Fallback to master

        return routingDataSource;
    }

    /**
     * Lazy Connection Proxy to defer actual connection selection until execution
     * CRITICAL: Required for proper routing based on @Transactional(readOnly)
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        log.info("Configuring Lazy Connection DataSource Proxy");
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
