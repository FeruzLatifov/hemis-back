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

    /** Advisory-lock key: only one pod applies audit DDL at a time during a rolling deploy. */
    private static final long AUDIT_DDL_LOCK = 82026_0830L;

    /** Applied in order, once each, recorded in schema_version. */
    private static final java.util.List<String> AUDIT_SCRIPTS = java.util.List.of(
            "V001_create_activity_log.sql",
            "V002_create_error_log.sql",
            "V003_create_login_log.sql",
            "V004_activity_log_scope_key.sql",
            "V005_activity_log_immutability.sql");


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

    /**
     * Apply the audit DDL once per script, under a cluster-wide lock, and fail loudly.
     *
     * <p>Three properties this replaces a bare {@code ResourceDatabasePopulator} for:</p>
     * <ul>
     *   <li><b>Versioning.</b> Every script used to re-execute on every pod start. Harmless while the
     *       scripts were {@code IF NOT EXISTS}, but it means a fleet restart replays DDL against a
     *       live audit table for no reason. A {@code schema_version} ledger runs each script once.</li>
     *   <li><b>One writer.</b> Rolling deploys start pods concurrently; two sessions issuing DDL on
     *       the same table deadlock or fail. A session-level advisory lock serialises them.</li>
     *   <li><b>Fail loudly.</b> {@code continueOnError=true} used to swallow a failure and still log
     *       "initialized successfully" — an unapplied column then turned every audit INSERT into a
     *       silent no-op, i.e. the trail everyone believes exists quietly stops. The contract the
     *       write path depends on is asserted here instead, and a failure is logged at ERROR.</li>
     * </ul>
     *
     * <p>Startup is never blocked: the audit DB is a side channel, so a failure disables auditing and
     * says so, rather than taking the ministry API down with it.</p>
     */
    private void initSchema(DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        try {
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    script_name VARCHAR(200) PRIMARY KEY,
                    applied_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )""");

            // 8_2026_0830 — an arbitrary but stable key; any pod applying audit DDL takes this lock.
            jdbc.queryForObject("SELECT pg_advisory_lock(?)", Object.class, AUDIT_DDL_LOCK);
            try {
                for (String script : AUDIT_SCRIPTS) {
                    applyOnce(jdbc, script);
                }
            } finally {
                jdbc.queryForObject("SELECT pg_advisory_unlock(?)", Object.class, AUDIT_DDL_LOCK);
            }

            assertWriteContract(jdbc);
            log.info("Audit DB schema up to date ({} scripts)", AUDIT_SCRIPTS.size());
        } catch (Exception e) {
            log.error("AUDIT SCHEMA NOT APPLIED — activity/login/error logging will not work: {}",
                    e.getMessage(), e);
        }
    }

    /** Runs a script unless the ledger already records it; records it only when it succeeded. */
    private void applyOnce(JdbcTemplate jdbc, String script) {
        Integer done = jdbc.queryForObject(
                "SELECT count(*) FROM schema_version WHERE script_name = ?", Integer.class, script);
        if (done != null && done > 0) {
            return;
        }
        String sql;
        try (var in = new ClassPathResource("db/audit/" + script).getInputStream()) {
            sql = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read audit script " + script, e);
        }
        // Whole file in one call by default: the driver runs it as a simple multi-statement query,
        // which keeps a PL/pgSQL body (`$$ ... ; ... $$`) intact — splitting on ";" cuts a function in
        // half, and that is exactly how V005's trigger function failed the first time this ran.
        //
        // The exception is CONCURRENTLY, which PostgreSQL refuses inside a multi-statement block, so
        // those scripts are executed statement by statement. A script must not need both: a naive
        // split cannot be trusted around dollar-quoting, so the combination is refused loudly rather
        // than mis-executed.
        if (!sql.contains("CONCURRENTLY")) {
            jdbc.execute(sql);
        } else if (sql.contains("$$")) {
            throw new IllegalStateException("Audit script " + script
                    + " mixes CONCURRENTLY with a dollar-quoted body — split it into two scripts");
        } else {
            for (String statement : sql.split(";\\s*\\n")) {
                // Strip the chunk's own leading comment lines before deciding whether anything is
                // left. Testing startsWith("--") on the RAW chunk skipped every statement that has
                // a comment above it — which in V004 is the ALTER and the CREATE INDEX, leaving only
                // a COMMENT ON that then failed on the column it was supposed to describe. The audit
                // trail stopped there: no scope_key column, so every INSERT threw and was swallowed.
                String trimmed = statement.strip().lines()
                        .dropWhile(line -> line.isBlank() || line.stripLeading().startsWith("--"))
                        .collect(java.util.stream.Collectors.joining("\n"))
                        .strip();
                if (trimmed.isEmpty()) {
                    continue;
                }
                jdbc.execute(trimmed);
            }
        }
        jdbc.update("INSERT INTO schema_version (script_name) VALUES (?) ON CONFLICT DO NOTHING", script);
        log.info("Audit DB script applied: {}", script);
    }

    /** The columns the write path binds. If one is missing, every INSERT would fail silently later. */
    private void assertWriteContract(JdbcTemplate jdbc) {
        Integer columns = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns
                 WHERE table_name = 'activity_log'
                   AND column_name IN ('scope_key', 'old_value', 'new_value', 'changed_fields')
                """, Integer.class);
        if (columns == null || columns < 4) {
            throw new IllegalStateException(
                    "activity_log is missing columns the audit writer binds (found " + columns + " of 4)");
        }
    }
}
