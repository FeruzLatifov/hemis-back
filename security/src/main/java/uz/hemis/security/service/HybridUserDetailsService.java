package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hybrid UserDetailsService - Supports Both Old and New Authentication Systems
 *
 * <p><strong>Migration Strategy - Strangler Fig Pattern:</strong></p>
 * <ul>
 *   <li>Gradually migrate users from sec_user (old) to users (new)</li>
 *   <li>Support authentication from both tables during migration</li>
 *   <li>Eventually deprecate sec_user table</li>
 * </ul>
 *
 * <p><strong>Authentication Flow:</strong></p>
 * <ol>
 *   <li>Try NEW system first: users table (CustomUserDetailsService)</li>
 *   <li>If not found, fall back to OLD system: sec_user table (SecUserDetailsService)</li>
 *   <li>This ensures zero downtime during migration</li>
 * </ol>
 *
 * <p><strong>Why Hybrid Approach?</strong></p>
 * <ul>
 *   <li><strong>Zero Downtime:</strong> Users can login during migration</li>
 *   <li><strong>Gradual Migration:</strong> Migrate users in batches (10 → 50 → 100 → all)</li>
 *   <li><strong>Rollback Safety:</strong> Can revert to old system if issues occur</li>
 *   <li><strong>Testing:</strong> Test new system with pilot users first</li>
 * </ul>
 *
 * <p><strong>Current Migration Status:</strong></p>
 * <ul>
 *   <li>Old system (sec_user): 340 active users</li>
 *   <li>New system (users): 339 users (1 admin + 338 migrated)</li>
 *   <li>Migration: ~99% complete</li>
 * </ul>
 *
 * <p><strong>Architecture Decision:</strong></p>
 * <ul>
 *   <li>NEW tables created (users, roles, permissions)</li>
 *   <li>OLD tables preserved (sec_user, sec_role, sec_permission)</li>
 *   <li>Parallel authentication during migration period</li>
 *   <li>No changes to old-hemis system (zero risk)</li>
 * </ul>
 *
 * <p><strong>Performance Optimization:</strong></p>
 * <ul>
 *   <li>NEW system checked first (most users already migrated)</li>
 *   <li>OLD system fallback only for unmigrated users (~1%)</li>
 *   <li>Average overhead: < 5ms for unmigrated users</li>
 * </ul>
 *
 * <p><strong>Future Decommissioning:</strong></p>
 * <ul>
 *   <li>When migration 100% complete:</li>
 *   <li>1. Remove @Primary annotation from this class</li>
 *   <li>2. Add @Primary to CustomUserDetailsService</li>
 *   <li>3. Remove old system fallback logic</li>
 *   <li>4. Archive sec_user table</li>
 * </ul>
 *
 * @since 1.0.0
 * @see CustomUserDetailsService - New system (users table)
 * @see SecUserDetailsService - Old system (sec_user table)
 */
@Service("hybridUserDetailsService")
@Primary  // Make this the default UserDetailsService
@RequiredArgsConstructor
@Slf4j
public class HybridUserDetailsService implements UserDetailsService {

    private final CustomUserDetailsService customUserDetailsService;
    private final SecUserDetailsService secUserDetailsService;

    /**
     * Load user by username - Hybrid approach (new + old systems)
     *
     * <p><strong>Algorithm:</strong></p>
     * <pre>
     * 1. TRY: Load from users table (new system)
     *    ├─ If found → Return UserDetails
     *    └─ If not found → Continue to step 2
     *
     * 2. FALLBACK: Load from sec_user table (old system)
     *    ├─ If found → Return UserDetails
     *    └─ If not found → Throw UsernameNotFoundException
     *
     * 3. FAIL: User not found in either system
     *    └─ Throw UsernameNotFoundException
     * </pre>
     *
     * <p><strong>Logging Strategy:</strong></p>
     * <ul>
     *   <li>INFO: When user found in NEW system (success)</li>
     *   <li>WARN: When fallback to OLD system (migration pending)</li>
     *   <li>ERROR: When user not found in either system (login failure)</li>
     * </ul>
     *
     * <p><strong>Security Notes:</strong></p>
     * <ul>
     *   <li>Same password verification for both systems (BCrypt)</li>
     *   <li>Same account status checks (enabled, locked, expired)</li>
     *   <li>Different permission systems (new: resource.action, old: CUBA Type 20)</li>
     * </ul>
     *
     * <p><strong>Example Scenarios:</strong></p>
     *
     * <p><strong>Scenario 1: User migrated to new system</strong></p>
     * <pre>
     * Username: admin
     * → Try NEW: Found in users table ✓
     * → Return: UserDetails with 31 authorities (1 role + 30 permissions)
     * → Skip OLD: No fallback needed
     * → Result: SUCCESS (new system)
     * </pre>
     *
     * <p><strong>Scenario 2: User NOT migrated yet</strong></p>
     * <pre>
     * Username: otm999
     * → Try NEW: Not found in users table ✗
     * → Fallback OLD: Found in sec_user table ✓
     * → Return: UserDetails with CUBA authorities
     * → Result: SUCCESS (old system fallback)
     * </pre>
     *
     * <p><strong>Scenario 3: User does not exist</strong></p>
     * <pre>
     * Username: hacker123
     * → Try NEW: Not found in users table ✗
     * → Fallback OLD: Not found in sec_user table ✗
     * → Result: FAIL (UsernameNotFoundException)
     * </pre>
     *
     * @param username login username
     * @return UserDetails from either new or old system
     * @throws UsernameNotFoundException if user not found in both systems
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("🔐 HYBRID AUTH: Attempting to load user: {}", username);

        // ========================================
        // STEP 1: Try NEW system (users table)
        // ========================================
        try {
            log.debug("→ Trying NEW system (users table)...");
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            log.info("✅ SUCCESS - NEW system: User '{}' found in users table (authorities: {})",
                    username,
                    userDetails.getAuthorities().size());

            return userDetails;

        } catch (UsernameNotFoundException e) {
            // User not found in new system, continue to old system fallback
            log.debug("→ User '{}' not found in NEW system, trying fallback to OLD system...", username);
        }

        // ========================================
        // STEP 2: Fallback to OLD system (sec_user table)
        // ========================================
        try {
            log.debug("→ Trying OLD system (sec_user table)...");
            UserDetails userDetails = secUserDetailsService.loadUserByUsername(username);

            log.warn("⚠️  FALLBACK - OLD system: User '{}' found in sec_user table (authorities: {})",
                    username,
                    userDetails.getAuthorities().size());

            log.warn("📋 ACTION REQUIRED: Migrate user '{}' to new system", username);

            return userDetails;

        } catch (UsernameNotFoundException e) {
            // User not found in old system either
            log.error("❌ FAILURE: User '{}' not found in both NEW and OLD systems", username);
        }

        // ========================================
        // STEP 3: User not found in either system
        // ========================================
        log.error("🚫 AUTHENTICATION FAILED: User '{}' does not exist", username);
        throw new UsernameNotFoundException("User not found: " + username);
    }

    // =====================================================
    // MIGRATION STATISTICS
    // =====================================================
    /**
     * Get migration statistics (for monitoring dashboard)
     *
     * Example usage:
     * <pre>
     * Map<String, Object> stats = hybridUserDetailsService.getMigrationStats();
     * // → {
     * //     "new_users": 339,
     * //     "old_users": 340,
     * //     "migration_percentage": 99.7,
     * //     "pending_migration": 1
     * //   }
     * </pre>
     *
     * @return migration statistics map
     */
    // TODO: Implement getMigrationStats() method for monitoring dashboard
    // =====================================================
}
