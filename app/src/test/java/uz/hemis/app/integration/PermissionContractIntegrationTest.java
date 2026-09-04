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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Access-control contract: the ONE permission code must line up across all three layers that use it.
 *
 * <p>A permission code lives in three places — the endpoint ({@code @PreAuthorize("hasAuthority('X')")}),
 * the menu row ({@code menu.permission}) and the seed that grants it ({@code permission} /
 * {@code role_permission}). Nothing forced them to agree, and the drift shipped: {@code menu.code='dashboard'}
 * was seeded with {@code permission = NULL}, {@code MenuService.hasPermission(null, …)} returns
 * {@code true} for a NULL requirement, so every authenticated user saw "Bosh sahifa" while
 * {@code GET /api/v1/web/dashboard/stats} answered 403 for the one role without {@code dashboard.view}.
 * A user-visible dead end that no unit test could see, because each layer was correct on its own.</p>
 *
 * <p>These three assertions are the guardrail: they read the real schema built by the real changelog
 * (Testcontainers + Liquibase, {@link AbstractIntegrationTest}) and the real controller sources, so the
 * next seed that forgets a permission fails the build instead of a user's first click.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri="
})
@DisplayName("Permission contract — endpoint ↔ menu ↔ seed")
class PermissionContractIntegrationTest extends AbstractIntegrationTest {

    /** Matches {@code hasAuthority('x')} and {@code hasAnyAuthority('x', 'y')} inside a @PreAuthorize. */
    private static final Pattern AUTHORITY_CALL = Pattern.compile("has(?:Any)?Authority\\(([^)]*)\\)");
    private static final Pattern QUOTED_CODE = Pattern.compile("'([^']+)'");

    /** API modules whose controllers are gated by permission codes. */
    private static final List<String> API_MODULES =
            List.of("api-web", "api-university", "api-external", "api-legacy");

    @Autowired private DataSource dataSource;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("every menu row names the permission its page requires (no NULL — deny by default)")
    void everyMenuRowNamesAPermission() {
        List<String> permissionless = jdbc.queryForList(
                "SELECT code FROM menu WHERE permission IS NULL AND deleted_at IS NULL ORDER BY code",
                String.class);

        assertThat(permissionless)
                .as("""
                    A menu row with permission = NULL is shown to EVERY authenticated user \
                    (MenuService.hasPermission returns true for a null requirement) while the page \
                    behind it stays gated by @PreAuthorize — the menu is visible, the API answers 403. \
                    Give the row the permission its page requires (S### seed).""")
                .isEmpty();
    }

    @Test
    @DisplayName("every menu.permission exists as a permission row")
    void everyMenuPermissionExists() {
        List<String> orphans = jdbc.queryForList("""
                SELECT DISTINCT m.permission
                  FROM menu m
                 WHERE m.deleted_at IS NULL
                   AND m.permission IS NOT NULL
                   AND NOT EXISTS (SELECT 1 FROM permission p
                                    WHERE p.code = m.permission AND p.deleted_at IS NULL)
                 ORDER BY 1
                """, String.class);

        assertThat(orphans)
                .as("""
                    These menu rows point at a permission code that no seed creates, so no role can \
                    ever hold it and the entry is dead for everyone — a typo in the menu seed, or a \
                    permission seed that was never written.""")
                .isEmpty();
    }

    @Test
    @DisplayName("every @PreAuthorize authority exists as a permission row")
    void everyEndpointAuthorityExists() {
        Set<String> seeded = new TreeSet<>(jdbc.queryForList(
                "SELECT code FROM permission WHERE deleted_at IS NULL", String.class));

        Map<String, String> unknown = new LinkedHashMap<>();  // code → first file that requires it
        scanAuthorityCodes().forEach((code, file) -> {
            if (!seeded.contains(code)) unknown.put(code, file);
        });

        assertThat(unknown)
                .as("""
                    These endpoints require an authority that no seed creates. Nobody can hold it, so \
                    the endpoint answers 403 for every caller — including SUPER_ADMIN. Add the \
                    permission and its grants in an S### seed, or fix the code in @PreAuthorize.""")
                .isEmpty();
    }

    /**
     * Every authority code required by a controller, mapped to the file that first requires it.
     *
     * <p>Reads the sources rather than the Spring metadata on purpose: {@code @PreAuthorize} SpEL is
     * evaluated per request, so a bad code is invisible until someone calls that endpoint.</p>
     */
    private Map<String, String> scanAuthorityCodes() {
        Path repoRoot = repoRoot();
        Map<String, String> codes = new LinkedHashMap<>();

        for (String module : API_MODULES) {
            Path sources = repoRoot.resolve(module).resolve("src/main/java");
            if (!Files.isDirectory(sources)) continue;

            try (Stream<Path> files = Files.walk(sources)) {
                files.filter(p -> p.toString().endsWith(".java")).forEach(file -> {
                    String content = read(file);
                    Matcher call = AUTHORITY_CALL.matcher(content);
                    while (call.find()) {
                        Matcher code = QUOTED_CODE.matcher(call.group(1));
                        while (code.find()) {
                            codes.putIfAbsent(code.group(1), repoRoot.relativize(file).toString());
                        }
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot scan " + sources, e);
            }
        }

        assertThat(codes)
                .as("No @PreAuthorize authority found at all — the scan root is wrong, "
                        + "so this test would pass without checking anything")
                .isNotEmpty();
        return codes;
    }

    /** Walks up from the module working directory to the repository root (the one holding settings.gradle.kts). */
    private Path repoRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.getParent();
        }
        assertThat(current).as("repository root (settings.gradle.kts) not found above the test working directory").isNotNull();
        return current;
    }

    private String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }
}
