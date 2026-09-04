package uz.hemis.app.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The role ladder the ministry actually works with (seed S038), asserted against the real schema.
 *
 * <p>Three human tiers: SUPER_ADMIN owns the platform, ADMIN ("Administrator") runs it
 * day to day, TECH_STAFF ("Texnik xodim") operates the classifiers. The boundary between
 * them is data — rows in {@code role_permission} — so nothing in the Java code stops a later seed,
 * or a click in the role editor, from handing an operator the approve button or an administrator
 * the ability to grant themselves permissions. These tests are that stop.</p>
 *
 * <p>The lists below are deliberately a second copy of S038's: a seed that verifies itself only
 * proves it ran, not that it says what we meant. If a permission legitimately changes tier, both
 * copies change — and the diff shows a security boundary moving, which is the point.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri="
})
@DisplayName("Role model — SUPER_ADMIN > Administrator > Texnik xodim")
class RoleModelIntegrationTest extends AbstractIntegrationTest {

    /** Actions whose misuse cannot be undone from inside the app — SUPER_ADMIN only (S038). */
    private static final Set<String> SUPER_ADMIN_ONLY = Set.of(
            "roles.create", "roles.edit", "roles.delete", "roles.manage", "permissions.manage",
            "webhook.create", "webhook.update", "webhook.delete", "webhook.manage",
            "oauth-clients.manage",
            "settings.edit", "system.menus.manage",
            "pinfl.view", "outbox.manage",
            // Registry deletions the OTM owns and will not re-send. universities.delete is NOT
            // here: an OTM is soft-deleted (delete_ts) and universities.restore brings it back, so
            // the criterion this list is built from - misuse that cannot be undone from inside the
            // app - stops applying to it (S038).
            "students.delete", "teachers.delete");

    /** What technical staff may do today: add and correct classifiers, attach them to OTMs. */
    private static final Set<String> TECH_STAFF = Set.of(
            "dashboard.view", "system.menu.view",
            "classifiers.view", "classifiers.edit",
            "classifiers.diploma.view", "classifiers.education.view", "classifiers.employee.view",
            "classifiers.financial.view", "classifiers.general.view", "classifiers.science.view",
            "classifiers.structure.view", "classifiers.student.view", "classifiers.study.view",
            "classifiers.speciality.view", "classifiers.speciality.create", "classifiers.speciality.edit",
            "institutions.view",
            "institutions.speciality-attachments.view", "institutions.speciality-attachments.create",
            "audit.history.view");

    @Autowired private DataSource dataSource;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("SUPER_ADMIN holds every permission — it is the break-glass role")
    void superAdminHoldsEverything() {
        List<String> missing = jdbc.queryForList("""
                SELECT p.code
                  FROM permission p
                 WHERE p.deleted_at IS NULL
                   AND NOT EXISTS (SELECT 1 FROM role_permission rp
                                     JOIN role r ON r.id = rp.role_id
                                    WHERE rp.permission_id = p.id AND r.code = 'SUPER_ADMIN')
                 ORDER BY 1
                """, String.class);

        assertThat(missing)
                .as("A permission SUPER_ADMIN does not hold is a door nobody can open: every new "
                        + "permission seed must grant it explicitly (S004's CROSS JOIN already ran).")
                .isEmpty();
    }

    @Test
    @DisplayName("Administrator holds none of the platform/security-critical permissions")
    void administratorStopsAtTheSecurityBoundary() {
        Set<String> held = new TreeSet<>(permissionsOf("ADMIN"));
        held.retainAll(SUPER_ADMIN_ONLY);

        assertThat(held)
                .as("""
                    These stay with SUPER_ADMIN because an administrator holding them can undo the \
                    boundary itself: grant themselves any permission (roles.*/permissions.manage), \
                    rotate or redirect the 224-OTM integration channel (webhook.*/oauth-clients.manage), \
                    rewrite the platform's structure for everyone (settings/menus), read raw PINFL, \
                    discard queued events, or delete student/teacher rows the OTM owns and will \
                    not re-send.""")
                .isEmpty();
    }

    @Test
    @DisplayName("Texnik xodim can add and correct classifiers, but not approve, delete or restore")
    void technicalStaffIsAMakerNotAChecker() {
        Set<String> held = new TreeSet<>(permissionsOf("TECH_STAFF"));

        assertThat(held)
                .as("Technical staff work on classifiers for now; each further duty is granted "
                        + "deliberately (S038), not inherited from a `classifiers.%%` wildcard.")
                .containsExactlyInAnyOrderElementsOf(new TreeSet<>(TECH_STAFF));

        assertThat(held)
                .as("""
                    Approval is what distributes a speciality to 230 OTMs, and delete also gates \
                    restore + the deleted list (SpecialityClassifierController). An operator who \
                    holds either is both maker and checker.""")
                .doesNotContain("classifiers.speciality.approve", "classifiers.speciality.delete",
                        "classifiers.delete", "institutions.speciality-attachments.delete");
    }

    @Test
    @DisplayName("each tier is contained in the one above it")
    void tiersNest() {
        Set<String> superAdmin = permissionsOf("SUPER_ADMIN");
        Set<String> administrator = permissionsOf("ADMIN");
        Set<String> technicalStaff = permissionsOf("TECH_STAFF");

        assertThat(superAdmin)
                .as("SUPER_ADMIN must be able to do anything an Administrator can do")
                .containsAll(administrator);
        assertThat(administrator)
                .as("An Administrator must be able to do anything technical staff can do — "
                        + "otherwise nobody senior can reproduce, review or fix the operator's work")
                .containsAll(technicalStaff);
    }

    @Test
    @DisplayName("the audit log is admin-tier reading — audit.view stops at ADMIN")
    void auditViewStaysWithTheAdminTiers() {
        List<String> holders = jdbc.queryForList("""
                SELECT r.code
                  FROM role_permission rp
                  JOIN role r ON r.id = rp.role_id
                  JOIN permission p ON p.id = rp.permission_id
                 WHERE p.code = 'audit.view' AND r.deleted_at IS NULL
                 ORDER BY 1
                """, String.class);

        assertThat(holders)
                .as("""
                    The audit log carries usernames, IPs and the before/after of everyone's work, and \
                    AuditLogController gates on this permission alone (a USER token has no ROLE_* for \
                    a role check to test) — so this list IS the audience.""")
                .containsExactlyInAnyOrder("SUPER_ADMIN", "ADMIN");
    }

    @Test
    @DisplayName("the two audit reads have different audiences — the journal is not the record")
    void auditReadsAreTwoCapabilities() {
        // audit.view opens the whole journal (every user, every IP, every snapshot); audit.history.view
        // opens one record's history in the registries its holder curates. An operator gets the second.
        assertThat(permissionsOf("TECH_STAFF"))
                .contains("audit.history.view")
                .doesNotContain("audit.view");
        assertThat(permissionsOf("ADMIN")).contains("audit.view", "audit.history.view");
        assertThat(permissionsOf("SUPER_ADMIN")).contains("audit.view", "audit.history.view");
    }

    private Set<String> permissionsOf(String roleCode) {
        return new TreeSet<>(jdbc.queryForList("""
                SELECT p.code
                  FROM role_permission rp
                  JOIN role r ON r.id = rp.role_id
                  JOIN permission p ON p.id = rp.permission_id
                 WHERE r.code = ? AND r.deleted_at IS NULL AND p.deleted_at IS NULL
                """, String.class, roleCode));
    }
}
