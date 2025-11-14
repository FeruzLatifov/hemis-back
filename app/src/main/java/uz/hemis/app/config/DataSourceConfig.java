package uz.hemis.app.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * DataSource Routing Configuration
 *
 * Routes database connections based on transaction type:
 * - @Transactional(readOnly=true) → REPLICA datasource
 * - @Transactional or @Transactional(readOnly=false) → MASTER datasource
 *
 * Benefits:
 * - Read scalability: Heavy read operations use replica
 * - Write consistency: All writes go to master
 * - Automatic routing: No code changes needed in services
 *
 * NOTE: Only active in production and dev profiles
 * Test profile uses simple H2 datasource (no routing)
 *
 * @author Senior Architect
 * @since 2025-11-13
 */
@Slf4j
@Configuration
@Profile("!test")  // Do NOT load in test profile
public class DataSourceConfig {

    /**
     * Master DataSource - for WRITE operations (CREATE, UPDATE, DELETE)
     */
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master.hikari")
    public DataSource masterDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .url(String.format(
                "jdbc:postgresql://%s:%s/%s",
                System.getenv().get("DB_MASTER_HOST"),
                System.getenv().get("DB_MASTER_PORT"),
                System.getenv().get("DB_MASTER_NAME")
            ))
            .username(System.getenv().getOrDefault("DB_MASTER_USERNAME", "postgres"))
            .password(System.getenv().getOrDefault("DB_MASTER_PASSWORD", "postgres"))
            .driverClassName("org.postgresql.Driver")
            .build();

        dataSource.setPoolName("HikariPool-Master");
        dataSource.setReadOnly(false);  // Master allows WRITE

        log.info("✅ Master DataSource configured (WRITE mode)");
        return dataSource;
    }

    /**
     * Replica DataSource - for READ operations (SELECT)
     */
    @Bean(name = "replicaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.replica.hikari")
    public DataSource replicaDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .url(String.format(
                "jdbc:postgresql://%s:%s/%s",
                System.getenv().get("DB_REPLICA_HOST") != null ? System.getenv().get("DB_REPLICA_HOST") : System.getenv().get("DB_MASTER_HOST"),
                System.getenv().get("DB_REPLICA_PORT") != null ? System.getenv().get("DB_REPLICA_PORT") : System.getenv().get("DB_MASTER_PORT"),
                System.getenv().get("DB_REPLICA_NAME") != null ? System.getenv().get("DB_REPLICA_NAME") : System.getenv().get("DB_MASTER_NAME")
            ))
            .username(System.getenv().getOrDefault("DB_REPLICA_USERNAME", "postgres"))
            .password(System.getenv().getOrDefault("DB_REPLICA_PASSWORD", "postgres"))
            .driverClassName("org.postgresql.Driver")
            .build();

        dataSource.setPoolName("HikariPool-Replica");
        dataSource.setReadOnly(true);  // Replica is READ-ONLY

        log.info("✅ Replica DataSource configured (READ-ONLY mode)");
        return dataSource;
    }

    /**
     * Routing DataSource - automatically selects master or replica
     * based on @Transactional(readOnly) attribute
     */
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource) {

        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource);
        dataSourceMap.put("replica", replicaDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);  // Default: MASTER

        log.info("✅ DataSource routing configured (read→replica, write→master)");
        return routingDataSource;
    }

    /**
     * Primary DataSource - uses LazyConnectionDataSourceProxy
     * to defer connection acquisition until first database operation
     * (needed for proper transaction synchronization)
     */
    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    /**
     * Custom AbstractRoutingDataSource implementation
     * Determines which datasource to use based on transaction read-only flag
     */
    static class RoutingDataSource extends AbstractRoutingDataSource {

        @Override
        protected Object determineCurrentLookupKey() {
            boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

            String dataSourceKey = isReadOnly ? "replica" : "master";

            if (log.isDebugEnabled()) {
                log.debug("📍 Routing to: {} (readOnly={})", dataSourceKey, isReadOnly);
            }

            return dataSourceKey;
        }
    }
}
