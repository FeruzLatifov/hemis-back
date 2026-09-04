package uz.hemis.app.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * The branch of S038 that only fires on a database nobody has: both MINISTRY_ADMIN and ADMIN present.
 *
 * <p>It exists because S001 is {@code runOnChange} — editing it re-creates MINISTRY_ADMIN after the
 * rename, and the seed then has to fold the re-created row into ADMIN instead of leaving two admin
 * roles behind. That path is unreachable from a normal migration run, so it is unreachable from
 * every other test: it gets exercised here by putting the database into that exact state and
 * re-running the seed by hand.</p>
 *
 * <p>What it pins, beyond "it does not crash": the users of the legacy row end up on ADMIN (nobody
 * loses their access in a rename), the legacy row is retired rather than left as a second admin, and
 * the seed's own verification — which counts roles by code — does not mistake the retired row for a
 * live role missing its grants and abort the whole migration.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri="
})
@DisplayName("S038 merge branch — a re-created MINISTRY_ADMIN folds into ADMIN")
class AccessControlSeedMergeIntegrationTest extends AbstractIntegrationTest {

    private static final String SEED = "db/changelog/changesets/seed/S038_seed_access_control.sql";

    @Autowired private DataSource dataSource;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("users move to ADMIN, the legacy row retires, and the verification does not abort")
    void mergeBranchFoldsTheLegacyRole() {
        UUID legacyRoleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // The state S001's re-run produces: the old code back alongside the new one.
        jdbc.update("""
                INSERT INTO role (id, code, name, description, role_type, active, created_by)
                VALUES (?, 'MINISTRY_ADMIN', 'Vazirlik Administrator', 'legacy', 'SYSTEM', TRUE, 'test')
                """, legacyRoleId);
        jdbc.update("""
                INSERT INTO users (id, username, password, user_type, enabled, account_non_locked, created_by)
                VALUES (?, 'merge_probe', 'x', 'MINISTRY', TRUE, TRUE, 'test')
                """, userId);
        jdbc.update("INSERT INTO user_role (user_id, role_id, assigned_by) VALUES (?, ?, 'test')",
                userId, legacyRoleId);

        assertThatCode(this::runSeed)
                .as("the seed must survive the state its own merge branch was written for")
                .doesNotThrowAnyException();

        UUID adminRoleId = jdbc.queryForObject(
                "SELECT id FROM role WHERE code = 'ADMIN' AND deleted_at IS NULL", UUID.class);

        List<UUID> rolesOfUser = jdbc.queryForList(
                "SELECT role_id FROM user_role WHERE user_id = ?", UUID.class, userId);
        assertThat(rolesOfUser)
                .as("a rename must not cost anyone their access")
                .containsExactly(adminRoleId);

        Integer liveLegacy = jdbc.queryForObject(
                "SELECT count(*) FROM role WHERE code = 'MINISTRY_ADMIN' AND deleted_at IS NULL",
                Integer.class);
        assertThat(liveLegacy)
                .as("the retired row must not linger as a second admin role")
                .isZero();
    }

    /** Runs the seed the way Liquibase does — the whole file, as one script. */
    private void runSeed() {
        try (var in = new ClassPathResource(SEED).getInputStream()) {
            jdbc.execute(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot run " + SEED, e);
        }
    }
}
