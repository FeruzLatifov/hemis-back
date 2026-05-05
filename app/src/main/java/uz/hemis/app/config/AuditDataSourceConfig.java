package uz.hemis.app.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Audit DataSource Configuration — Master/Replica
 *
 * <p>Audit loglar uchun alohida PostgreSQL bazasiga ulanish.
 * Asosiy HEMIS DB dan mustaqil — performans va izolyatsiya uchun.</p>
 *
 * <p><strong>Master/Replica routing:</strong></p>
 * <ul>
 *   <li>WRITE (INSERT) → auditMasterDataSource → auditJdbcTemplate</li>
 *   <li>READ (SELECT) → auditReplicaDataSource → auditReadJdbcTemplate</li>
 * </ul>
 *
 * <p>Agar replica URL berilmasa, master URL fallback sifatida ishlatiladi
 * (backward compatible — bitta datasource bilan ham ishlaydi).</p>
 *
 * <p>Schema init: faqat master da bajariladi (replica streaming replication orqali oladi).</p>
 */
@Slf4j
@Configuration
@Profile("!test")
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditDataSourceConfig {

    /**
     * Audit Master DataSource — WRITE operations (INSERT)
     * AuditRepository bu datasource dan foydalanadi.
     */
    @Bean(name = "auditDataSource")
    public DataSource auditDataSource(
            @Value("${hemis.audit.datasource.master.url:${hemis.audit.datasource.url:jdbc:postgresql://localhost:5434/hemis_audit}}") String url,
            @Value("${hemis.audit.datasource.master.username:${hemis.audit.datasource.username:hemis_audit}}") String username,
            @Value("${hemis.audit.datasource.master.password:${hemis.audit.datasource.password:}}") String password) {

        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();

        dataSource.setPoolName("HikariPool-Audit-Master");
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        // Schema init faqat master da
        initSchema(dataSource);

        log.info("Audit MASTER DataSource configured: {}", url);
        return dataSource;
    }

    /**
     * Audit Replica DataSource — READ operations (SELECT)
     * AuditService bu datasource dan foydalanadi.
     *
     * <p>Agar replica URL berilmasa, master URL ishlatiladi (fallback).</p>
     */
    @Bean(name = "auditReplicaDataSource")
    public DataSource auditReplicaDataSource(
            @Value("${hemis.audit.datasource.replica.url:${hemis.audit.datasource.master.url:${hemis.audit.datasource.url:jdbc:postgresql://localhost:5434/hemis_audit}}}") String url,
            @Value("${hemis.audit.datasource.replica.username:${hemis.audit.datasource.master.username:${hemis.audit.datasource.username:hemis_audit}}}") String username,
            @Value("${hemis.audit.datasource.replica.password:${hemis.audit.datasource.master.password:${hemis.audit.datasource.password:}}}") String password) {

        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();

        dataSource.setPoolName("HikariPool-Audit-Replica");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setReadOnly(true);

        log.info("Audit REPLICA DataSource configured: {}", url);
        return dataSource;
    }

    /**
     * Audit Master JdbcTemplate — AuditRepository yozish uchun ishlatadi
     */
    @Bean(name = "auditJdbcTemplate")
    public JdbcTemplate auditJdbcTemplate(@Qualifier("auditDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Audit Replica JdbcTemplate — AuditService o'qish uchun ishlatadi
     */
    @Bean(name = "auditReadJdbcTemplate")
    public JdbcTemplate auditReadJdbcTemplate(@Qualifier("auditReplicaDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private void initSchema(DataSource dataSource) {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/audit/V001_create_activity_log.sql"));
            populator.addScript(new ClassPathResource("db/audit/V002_create_error_log.sql"));
            populator.addScript(new ClassPathResource("db/audit/V003_create_login_log.sql"));
            populator.setContinueOnError(true);
            populator.execute(dataSource);
            log.info("Audit DB schema initialized successfully");
        } catch (Exception e) {
            log.warn("Audit DB schema initialization failed (audit DB may not be available): {}", e.getMessage());
        }
    }
}
