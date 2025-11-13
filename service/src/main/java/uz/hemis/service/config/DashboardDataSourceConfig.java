package uz.hemis.service.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Dashboard Read-Only DataSource Configuration
 * 
 * Creates a dedicated DataSource that connects ONLY to REPLICA database
 * - All dashboard statistics read from replica
 * - Zero load on master database
 * - Perfect for read-heavy analytics queries
 * 
 * Configuration from .env:
 * - DB_REPLICA_HOST
 * - DB_REPLICA_PORT
 * - DB_REPLICA_NAME
 * - DB_REPLICA_USERNAME
 * - DB_REPLICA_PASSWORD
 */
@Configuration
@Slf4j
public class DashboardDataSourceConfig {

    /**
     * Dashboard-specific DataSource (Read-Only Replica)
     * 
     * This DataSource is SEPARATE from main application DataSource
     * and connects directly to REPLICA database
     */
    @Bean(name = "dashboardDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.dashboard")
    public DataSource dashboardDataSource() {
        log.info("Configuring DASHBOARD DataSource (Read-Only Replica)");
        
        HikariDataSource dataSource = new HikariDataSource();
        
        // Connection config from .env
        dataSource.setJdbcUrl(String.format(
            "jdbc:postgresql://%s:%s/%s",
            System.getenv().getOrDefault("DB_REPLICA_HOST", "localhost"),
            System.getenv().getOrDefault("DB_REPLICA_PORT", "5432"),
            System.getenv().getOrDefault("DB_REPLICA_NAME", "test_hemis")
        ));
        dataSource.setUsername(System.getenv().getOrDefault("DB_REPLICA_USERNAME", "postgres"));
        dataSource.setPassword(System.getenv().getOrDefault("DB_REPLICA_PASSWORD", "postgres"));
        dataSource.setDriverClassName("org.postgresql.Driver");
        
        // Pool configuration (optimized for analytics)
        dataSource.setPoolName("HEMIS-Dashboard-Replica-Pool");
        dataSource.setMaximumPoolSize(5);        // Small pool for dashboard only
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(10000);   // 10 seconds
        dataSource.setIdleTimeout(300000);        // 5 minutes
        dataSource.setMaxLifetime(900000);        // 15 minutes
        dataSource.setReadOnly(true);             // ✅ ENFORCE READ-ONLY
        
        // Validation
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setValidationTimeout(5000);
        
        log.info("Dashboard DataSource configured: {}:{}/{}",
            dataSource.getJdbcUrl(),
            dataSource.getUsername(),
            "(read-only replica)"
        );
        
        return dataSource;
    }

    /**
     * Dashboard-specific JdbcTemplate
     * 
     * Uses dashboardDataSource (replica) for all queries
     * DashboardService will use this template
     */
    @Bean(name = "dashboardJdbcTemplate")
    public JdbcTemplate dashboardJdbcTemplate(
            @Qualifier("dashboardDataSource") DataSource dashboardDataSource
    ) {
        log.info("Creating Dashboard JdbcTemplate (backed by replica)");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dashboardDataSource);
        jdbcTemplate.setQueryTimeout(30);  // 30 seconds max per query
        return jdbcTemplate;
    }
}
