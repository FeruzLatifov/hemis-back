package uz.hemis.service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.hemis.domain.entity.Role;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.service.menu.MenuService;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Menu Cache Warmup Component - ROLE-BASED + HYBRID Strategy
 *
 * <p><strong>Industry Best Practice (Google, Netflix, Amazon):</strong></p>
 * <ul>
 *   <li>Role-Based Warmup - Dynamic, no hardcoded usernames</li>
 *   <li>Hybrid Strategy - Critical roles sync, others lazy</li>
 *   <li>Smart Pre-caching - 1-2 users per role (Pareto 80/20)</li>
 *   <li>Blocking Warmup - Application ready only after critical warmup</li>
 * </ul>
 *
 * <p><strong>Warmup Strategy:</strong></p>
 * <ul>
 *   <li>Mode 1 - ROLE_BASED (recommended): Dynamic discovery from database</li>
 *   <li>Mode 2 - STATIC: Hardcoded usernames (fallback/override)</li>
 *   <li>Critical Roles: SUPER_ADMIN, MINISTRY_ADMIN, UNIVERSITY_ADMIN (synchronous)</li>
 *   <li>Other Roles: VIEWER, REPORT_VIEWER (lazy loading or async warmup)</li>
 * </ul>
 *
 * <p><strong>Configuration Example:</strong></p>
 * <pre>
 * cache:
 *   warmup:
 *     menu:
 *       enabled: true
 *       mode: role-based  # role-based or static
 *       critical-roles:   # Sync warmup for VIP users
 *         - SUPER_ADMIN
 *         - MINISTRY_ADMIN
 *         - UNIVERSITY_ADMIN
 *       max-users-per-role: 1  # Sample size
 *       sample-usernames: []    # Fallback for static mode
 * </pre>
 *
 * <p><strong>Benefits:</strong></p>
 * <ul>
 *   <li>No hardcoded usernames - automatic scaling</li>
 *   <li>New role added → automatic warmup</li>
 *   <li>User deleted → no problem, picks another user</li>
 *   <li>Minimal cache entries (rol count × languages)</li>
 *   <li>100% coverage for role patterns</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after I18nService warmup (@Order(1))
public class MenuCacheWarmup {

    private final MenuService menuService;
    private final MenuCacheWarmupProperties properties;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LanguageProperties languageProperties;

    /**
     * Warm up menu cache at application startup
     *
     * <p><strong>Execution Flow:</strong></p>
     * <ol>
     *   <li>Check if warmup enabled</li>
     *   <li>Determine mode (role-based vs static)</li>
     *   <li>Collect sample usernames</li>
     *   <li>Warmup menu for each user × each language</li>
     *   <li>Log statistics and failures</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Individual failures logged but don't stop warmup</li>
     *   <li>Application continues even if entire warmup fails</li>
     *   <li>Lazy loading fallback ensures service availability</li>
     * </ul>
     */
    @PostConstruct
    public void warmupMenuCache() {
        if (!properties.isEnabled()) {
            log.info("⏭️  Menu cache warmup is DISABLED (cache.warmup.menu.enabled=false)");
            return;
        }

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;

        // ✅ Get supported languages from config (centralized)
        List<String> supportedLanguages = languageProperties.getSupported();

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🔥 Menu Cache Warmup - TWO-LEVEL CACHE (L1+L2)");
        log.info("   Mode: {}", properties.getMode().toUpperCase());
        log.info("   Languages: {} (from config)", supportedLanguages);

        // Collect sample usernames based on mode
        List<String> sampleUsernames = collectSampleUsernames();

        log.info("   Sample Users: {}", sampleUsernames.size());
        log.info("   Total Combinations: {} (users × languages)",
            sampleUsernames.size() * supportedLanguages.size());
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ✅ FIX: Warmup using userId (matches production cache key!)
        for (String username : sampleUsernames) {
            try {
                // Get userId for cache key alignment
                User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

                UUID userId = user.getId();

                // ✅ Use supported languages from config
                for (String language : supportedLanguages) {
                    try {
                        log.debug("📥 Warming up menu: userId={}, language={}", userId, language);

                        // ✅ CORRECT: Use userId:locale key (matches production!)
                        menuService.getMenuForUser(userId, language);

                        successCount++;
                        log.debug("✅ Menu cached: userId={} - {}", userId, language);

                    } catch (Exception e) {
                        // Unexpected error - log but continue warmup
                        log.error("❌ Menu warmup failed: userId={}, language={}, error={}",
                            userId, language, e.getMessage());
                        failCount++;
                    }
                }
            } catch (IllegalArgumentException e) {
                // User not found - expected for some edge cases
                log.warn("⚠️  User not found (skipping): {} - {}", username, e.getMessage());
                failCount++;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("✅ Menu Cache Warmup Completed");
        log.info("   Success: {} menu entries", successCount);
        log.info("   Failed: {} (user not found or errors)", failCount);
        log.info("   Time: {}ms", elapsed);
        log.info("   Cache layers: L1 (Caffeine) + L2 (Redis)");
        log.info("   Status: {} users pre-cached for instant login",
            successCount / Math.max(supportedLanguages.size(), 1));
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (failCount > 0) {
            log.warn("⚠️  {} warmup failures occurred (see logs above). " +
                "Lazy loading will handle missing cache entries.", failCount);
        }
    }

    /**
     * Collect sample usernames based on configuration mode
     *
     * <p><strong>Mode: ROLE_BASED</strong></p>
     * <ul>
     *   <li>Query all active roles from database</li>
     *   <li>For critical roles: get sample users (synchronous)</li>
     *   <li>For other roles: skip or async warmup</li>
     *   <li>Returns 1-2 usernames per critical role</li>
     * </ul>
     *
     * <p><strong>Mode: STATIC</strong></p>
     * <ul>
     *   <li>Use hardcoded usernames from config</li>
     *   <li>Fallback for testing or override</li>
     * </ul>
     *
     * @return List of sample usernames to warmup
     */
    private List<String> collectSampleUsernames() {
        if ("role-based".equalsIgnoreCase(properties.getMode())) {
            return collectUsernamesByRole();
        } else if ("static".equalsIgnoreCase(properties.getMode())) {
            return properties.getSampleUsernames();
        } else {
            log.warn("⚠️  Unknown warmup mode: {}. Falling back to STATIC mode.", properties.getMode());
            return properties.getSampleUsernames();
        }
    }

    /**
     * Collect usernames by role (ROLE_BASED mode)
     *
     * <p><strong>Algorithm:</strong></p>
     * <ol>
     *   <li>Query all active roles from database</li>
     *   <li>Filter critical roles (SUPER_ADMIN, MINISTRY_ADMIN, etc.)</li>
     *   <li>For each critical role: find N sample users</li>
     *   <li>Deduplicate usernames (same user may have multiple roles)</li>
     * </ol>
     *
     * @return List of usernames (deduplicated)
     */
    private List<String> collectUsernamesByRole() {
        List<String> collectedUsernames = new ArrayList<>();

        try {
            // Get all active roles
            List<Role> allRoles = roleRepository.findAllActive();
            log.info("   📊 Found {} active roles in database", allRoles.size());

            // Process critical roles
            for (Role role : allRoles) {
                if (isCriticalRole(role.getCode())) {
                    log.debug("   🔑 Processing critical role: {} ({})", role.getCode(), role.getName());

                    // Find sample users for this role
                    List<String> roleUsers = userRepository.findSampleUsernamesByRoleCode(
                        role.getCode(),
                        properties.getMaxUsersPerRole()
                    );

                    if (!roleUsers.isEmpty()) {
                        collectedUsernames.addAll(roleUsers);
                        log.info("   ✅ Role {}: {} sample user(s)", role.getCode(), roleUsers.size());
                    } else {
                        log.warn("   ⚠️  Role {}: no users found (skipping)", role.getCode());
                    }
                } else {
                    log.debug("   ⏭️  Skipping non-critical role: {}", role.getCode());
                }
            }

            // Deduplicate (same user may have multiple critical roles)
            List<String> deduplicated = new ArrayList<>(new java.util.LinkedHashSet<>(collectedUsernames));
            if (deduplicated.size() < collectedUsernames.size()) {
                log.info("   🔄 Deduplicated: {} → {} users (removed {} duplicates)",
                    collectedUsernames.size(), deduplicated.size(),
                    collectedUsernames.size() - deduplicated.size());
            }

            return deduplicated;

        } catch (Exception e) {
            log.error("❌ Failed to collect usernames by role. Falling back to static mode.", e);
            return properties.getSampleUsernames();
        }
    }

    /**
     * Check if role is critical (requires synchronous warmup)
     *
     * @param roleCode Role code
     * @return true if critical
     */
    private boolean isCriticalRole(String roleCode) {
        return properties.getCriticalRoles().contains(roleCode);
    }
}

/**
 * Configuration Properties for Menu Cache Warmup
 *
 * <p><strong>Configuration Example:</strong></p>
 * <pre>
 * cache:
 *   warmup:
 *     menu:
 *       enabled: true
 *       mode: role-based
 *       critical-roles:
 *         - SUPER_ADMIN
 *         - MINISTRY_ADMIN
 *         - UNIVERSITY_ADMIN
 *       max-users-per-role: 1
 *       sample-usernames: []  # Fallback
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "cache.warmup.menu")
class MenuCacheWarmupProperties {

    /**
     * Enable/disable menu cache warmup
     * Default: true (enabled)
     */
    private boolean enabled = true;

    /**
     * Warmup mode: role-based or static
     * Default: role-based (dynamic discovery)
     */
    private String mode = "role-based";

    /**
     * Critical roles for synchronous warmup
     * Default: SUPER_ADMIN, MINISTRY_ADMIN, UNIVERSITY_ADMIN
     */
    private List<String> criticalRoles = new ArrayList<>(Arrays.asList(
        "SUPER_ADMIN",
        "MINISTRY_ADMIN",
        "UNIVERSITY_ADMIN"
    ));

    /**
     * Maximum users per role to warmup
     * Default: 1 (minimize startup time)
     */
    private int maxUsersPerRole = 1;

    /**
     * Sample usernames for STATIC mode (fallback)
     * Default: empty (use role-based mode)
     */
    private List<String> sampleUsernames = new ArrayList<>();

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<String> getCriticalRoles() {
        return criticalRoles;
    }

    public void setCriticalRoles(List<String> criticalRoles) {
        this.criticalRoles = criticalRoles;
    }

    public int getMaxUsersPerRole() {
        return maxUsersPerRole;
    }

    public void setMaxUsersPerRole(int maxUsersPerRole) {
        this.maxUsersPerRole = maxUsersPerRole;
    }

    public List<String> getSampleUsernames() {
        return sampleUsernames;
    }

    public void setSampleUsernames(List<String> sampleUsernames) {
        this.sampleUsernames = sampleUsernames;
    }
}
