package uz.hemis.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Liquibase Filename Fix Listener
 *
 * <p><strong>@deprecated</strong> This workaround is no longer needed.</p>
 * <p>The root cause has been fixed by adding {@code logicalFilePath} to all changesets
 * in db.changelog-master.yaml. This ensures consistent changeset identity regardless
 * of whether migrations are run via Gradle or Spring Boot.</p>
 *
 * <p><strong>Original Problem:</strong></p>
 * <ul>
 *   <li>Gradle tasks write: {@code src/main/resources/db/changelog/db.changelog-master.yaml}</li>
 *   <li>Spring Boot writes: {@code db/changelog/db.changelog-master.yaml}</li>
 *   <li>Result: Rollback only removes Gradle entries, bootRun entries remain!</li>
 * </ul>
 *
 * <p><strong>Proper Solution (implemented):</strong></p>
 * <p>All changesets now use {@code logicalFilePath: db/changelog/db.changelog-master.yaml}</p>
 * <p>Migration M003_fix_changelog_filenames standardizes existing entries.</p>
 *
 * <p><strong>Configuration:</strong></p>
 * <pre>
 * # application-dev.yml - can be removed
 * liquibase:
 *   fix-filename: false  # or remove entirely
 * </pre>
 *
 * @author hemis-team
 * @since 2.0.0
 * @deprecated Since 2.1.0 - Use logicalFilePath in db.changelog-master.yaml instead.
 *             This class will be removed in version 3.0.0.
 */
@Deprecated(since = "2.1.0", forRemoval = true)
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "liquibase.fix-filename", havingValue = "true", matchIfMissing = false)
public class LiquibaseFilenameFixListener {

    private final JdbcTemplate jdbcTemplate;

    private static final String INCORRECT_FILENAME = "db/changelog/db.changelog-master.yaml";
    private static final String CORRECT_FILENAME = "src/main/resources/db/changelog/db.changelog-master.yaml";

    /**
     * Fix Liquibase filename after application startup
     * 
     * <p>This ensures rollback works correctly by standardizing filename format.</p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void fixLiquibaseFilenames() {
        try {
            int updated = jdbcTemplate.update(
                "UPDATE databasechangelog " +
                "SET filename = ? " +
                "WHERE filename = ?",
                CORRECT_FILENAME,
                INCORRECT_FILENAME
            );

            if (updated > 0) {
                log.info("✅ Fixed {} Liquibase filename(s) in databasechangelog", updated);
                log.debug("Changed: {} → {}", INCORRECT_FILENAME, CORRECT_FILENAME);
            } else {
                log.debug("✅ Liquibase filenames already correct");
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to fix Liquibase filenames: {}", e.getMessage());
            // Don't fail startup - this is non-critical
        }
    }
}
