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
 * <p><strong>Problem:</strong></p>
 * <ul>
 *   <li>Gradle tasks write: {@code src/main/resources/db/changelog/db.changelog-master.yaml}</li>
 *   <li>Spring Boot writes: {@code db/changelog/db.changelog-master.yaml}</li>
 *   <li>Result: Rollback only removes Gradle entries, bootRun entries remain!</li>
 * </ul>
 * 
 * <p><strong>Solution:</strong></p>
 * <p>Auto-fix filename after bootRun to match Gradle task format.</p>
 * 
 * <p><strong>Configuration:</strong></p>
 * <pre>
 * # application-dev.yml
 * liquibase:
 *   fix-filename: true
 * </pre>
 * 
 * @author hemis-team
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "liquibase.fix-filename", havingValue = "true")
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
