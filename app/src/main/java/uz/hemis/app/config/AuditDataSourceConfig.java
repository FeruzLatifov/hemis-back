package uz.hemis.app.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Audit DataSource Configuration
 *
 * <p>Audit loglar uchun alohida PostgreSQL bazasiga ulanish.
 * Asosiy HEMIS DB dan mustaqil — performans va izolyatsiya uchun.</p>
 *
 * <p>Schema init: app ishga tushganda SQL skriptlar bajariladi
 * (IF NOT EXISTS — idempotent).</p>
 */
@Slf4j
@Configuration
@Profile("!test")
public class AuditDataSourceConfig {

    @Bean(name = "auditDataSource")
    public DataSource auditDataSource(
            @Value("${hemis.audit.datasource.url:jdbc:postgresql://localhost:5434/hemis_audit}") String url,
            @Value("${hemis.audit.datasource.username:hemis_audit}") String username,
            @Value("${hemis.audit.datasource.password:audit_secret}") String password) {

        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();

        dataSource.setPoolName("HikariPool-Audit");
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        // Schema init
        initSchema(dataSource);

        log.info("Audit DataSource configured: {}", url);
        return dataSource;
    }

    @Bean(name = "auditJdbcTemplate")
    public JdbcTemplate auditJdbcTemplate(@Qualifier("auditDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private void initSchema(DataSource dataSource) {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/audit/V001_create_activity_log.sql"));
            populator.addScript(new ClassPathResource("db/audit/V003_create_error_log.sql"));
            populator.addScript(new ClassPathResource("db/audit/V004_create_login_log.sql"));
            populator.setContinueOnError(true);
            populator.execute(dataSource);
            log.info("Audit DB schema initialized successfully");
        } catch (Exception e) {
            log.warn("Audit DB schema initialization failed (audit DB may not be available): {}", e.getMessage());
        }
    }
}
