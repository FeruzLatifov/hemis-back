package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.SecUser;
import uz.hemis.domain.repository.SecUserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SecUser UserDetailsService Implementation (CUBA Compatible)
 *
 * <p><strong>CRITICAL - old-hemis Compatibility:</strong></p>
 * <ul>
 *   <li>Loads users from sec_user table (shared with old-hemis)</li>
 *   <li>Compatible with CUBA Platform user structure</li>
 *   <li>Password verification using BCrypt (same as old-hemis)</li>
 *   <li>Soft delete pattern (delete_ts check)</li>
 * </ul>
 *
 * <p><strong>Design Philosophy:</strong></p>
 * <ul>
 *   <li>READ-ONLY access to sec_user (no writes)</li>
 *   <li>All user management done by old-hemis</li>
 *   <li>Parallel authentication: old-hemis (session) + hemis-back (JWT)</li>
 *   <li>Role mapping from CUBA groups to Spring Security authorities</li>
 * </ul>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Load user from sec_user for Spring Security authentication</li>
 *   <li>Convert SecUser entity to Spring Security UserDetails</li>
 *   <li>Map CUBA groups to Spring Security roles</li>
 *   <li>Enforce account status checks (active, not deleted, not locked)</li>
 * </ul>
 *
 * <p><strong>Used By:</strong></p>
 * <ul>
 *   <li>OAuth2 Token Endpoint (/app/rest/v2/oauth/token)</li>
 *   <li>Spring Security authentication manager</li>
 *   <li>JWT authentication filter</li>
 * </ul>
 *
 * <p><strong>@Primary Annotation:</strong></p>
 * <ul>
 *   <li>Marks this as the default UserDetailsService</li>
 *   <li>Overrides CustomUserDetailsService (hemishe_user)</li>
 *   <li>Used for authentication when multiple implementations exist</li>
 * </ul>
 *
 * @since 1.0.0
 * @see SecUser
 * @see SecUserRepository
 */
@Service("secUserDetailsService")
// @Primary removed - HybridUserDetailsService is now the primary implementation
@RequiredArgsConstructor
@Slf4j
public class SecUserDetailsService implements UserDetailsService {

    private final SecUserRepository secUserRepository;

    /**
     * Load user by username for authentication (CUBA compatible)
     *
     * <p><strong>Process:</strong></p>
     * <ol>
     *   <li>Query sec_user table by login (case-insensitive)</li>
     *   <li>Check if user exists and is active</li>
     *   <li>Check soft delete status (delete_ts)</li>
     *   <li>Map CUBA groups to Spring Security roles</li>
     *   <li>Create Spring Security UserDetails object</li>
     *   <li>Return UserDetails (Spring Security handles password verification)</li>
     * </ol>
     *
     * <p><strong>SQL Query:</strong></p>
     * <pre>
     * SELECT * FROM sec_user
     * WHERE LOWER(login) = LOWER(:username)
     *   AND delete_ts IS NULL
     *   AND active = true
     * </pre>
     *
     * <p><strong>Returned UserDetails Properties:</strong></p>
     * <ul>
     *   <li>username - sec_user.login</li>
     *   <li>password - sec_user.password (BCrypt hash)</li>
     *   <li>authorities - mapped from CUBA groups</li>
     *   <li>accountNonExpired - true (CUBA doesn't have expiration)</li>
     *   <li>accountNonLocked - true (CUBA doesn't use locking)</li>
     *   <li>credentialsNonExpired - inverse of change_password_at_logon</li>
     *   <li>enabled - sec_user.active</li>
     * </ul>
     *
     * <p><strong>Security Notes:</strong></p>
     * <ul>
     *   <li>Case-insensitive username lookup (login_lc column)</li>
     *   <li>Soft delete check (delete_ts IS NULL)</li>
     *   <li>Active status check (active = true)</li>
     *   <li>Password verification by Spring Security (BCrypt)</li>
     * </ul>
     *
     * @param username login username (case-insensitive)
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found or inactive
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading sec_user by username: {}", username);

        // Load user from sec_user table (shared with old-hemis)
        SecUser user = secUserRepository.findByLoginAndActiveTrue(username)
                .orElseThrow(() -> {
                    log.warn("SecUser not found or inactive: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Additional check: user should not be deleted
        if (user.isDeleted()) {
            log.warn("Attempt to login with deleted sec_user: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        log.debug("SecUser found: {} (active: {}, university: {}, dtype: {})",
                username,
                user.getActive(),
                user.getUniversityCode(),
                user.getDtype());

        // Map CUBA groups to Spring Security authorities
        Collection<? extends GrantedAuthority> authorities = mapAuthorities(user);

        log.debug("SecUser {} has {} authorities: {}", username, authorities.size(), authorities);

        // Build Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())  // sec_user.login
                .password(user.getPassword())  // sec_user.password (BCrypt)
                .authorities(authorities)  // Mapped from CUBA groups
                .accountExpired(false)  // CUBA doesn't have account expiration
                .accountLocked(!user.isAccountNonLocked())  // CUBA doesn't use locking (always false)
                .credentialsExpired(!user.isCredentialsNonExpired())  // From change_password_at_logon
                .disabled(!user.isEnabled())  // sec_user.active
                .build();
    }

    /**
     * Map CUBA groups/roles to Spring Security authorities
     *
     * <p><strong>CUBA Group Mapping Strategy:</strong></p>
     * <ul>
     *   <li>CUBA uses sec_group table for role management</li>
     *   <li>sec_user.group_names contains comma-separated group names</li>
     *   <li>We map group names to Spring Security roles with ROLE_ prefix</li>
     *   <li>If no groups assigned, return empty list (no access)</li>
     * </ul>
     *
     * <p><strong>Role Mapping Examples:</strong></p>
     * <pre>
     * CUBA Group Name          → Spring Security Role
     * ─────────────────────────────────────────────────
     * "Administrators"         → ROLE_ADMIN
     * "University Admins"      → ROLE_UNIVERSITY_ADMIN
     * "Users"                  → ROLE_USER
     * null/empty               → [] (no roles)
     * </pre>
     *
     * <p><strong>Special Cases:</strong></p>
     * <ul>
     *   <li>System admin (university = NULL): Add ROLE_ADMIN</li>
     *   <li>University admin (university != NULL): Add ROLE_UNIVERSITY_ADMIN</li>
     *   <li>No groups: Add ROLE_USER as fallback</li>
     * </ul>
     *
     * @param user SecUser entity from database
     * @return collection of GrantedAuthority (Spring Security roles)
     */
    private Collection<? extends GrantedAuthority> mapAuthorities(SecUser user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Strategy 1: Map from CUBA group_names field
        if (user.getGroupNames() != null && !user.getGroupNames().isEmpty()) {
            String[] groups = user.getGroupNames().split(",");
            for (String group : groups) {
                String trimmedGroup = group.trim();
                if (!trimmedGroup.isEmpty()) {
                    // Map CUBA group name to Spring Security role
                    String role = mapGroupToRole(trimmedGroup);
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            }
        }

        // Strategy 2: Map based on university (system admin vs university admin)
        if (user.isSystemAdmin()) {
            // System admin (no university restriction)
            if (!hasRole(authorities, "ROLE_ADMIN")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                log.debug("Added ROLE_ADMIN (system admin: university = NULL)");
            }
        } else if (user.getUniversityCode() != null) {
            // University admin (specific OTM)
            if (!hasRole(authorities, "ROLE_UNIVERSITY_ADMIN")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_UNIVERSITY_ADMIN"));
                log.debug("Added ROLE_UNIVERSITY_ADMIN (university: {})", user.getUniversityCode());
            }
        }

        // Fallback: If no roles assigned, add ROLE_USER
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            log.debug("Added ROLE_USER (fallback - no groups assigned)");
        }

        return authorities;
    }

    /**
     * Map CUBA group name to Spring Security role
     *
     * <p><strong>Mapping Rules:</strong></p>
     * <pre>
     * Contains "admin" (case-insensitive)     → ROLE_ADMIN
     * Contains "university" (case-insensitive) → ROLE_UNIVERSITY_ADMIN
     * Otherwise                                → ROLE_USER
     * </pre>
     *
     * @param groupName CUBA group name
     * @return Spring Security role name (with ROLE_ prefix)
     */
    private String mapGroupToRole(String groupName) {
        String lowerGroupName = groupName.toLowerCase();

        if (lowerGroupName.contains("admin")) {
            return "ROLE_ADMIN";
        } else if (lowerGroupName.contains("university")) {
            return "ROLE_UNIVERSITY_ADMIN";
        } else {
            return "ROLE_USER";
        }
    }

    /**
     * Check if authorities list contains a specific role
     *
     * @param authorities list of GrantedAuthority
     * @param role role name to check
     * @return true if role exists in list
     */
    private boolean hasRole(List<GrantedAuthority> authorities, String role) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }

    // =====================================================
    // NOTE: READ-ONLY SERVICE
    // =====================================================
    // This service ONLY reads from sec_user table
    // NO CREATE/UPDATE/DELETE operations
    // All user management done by old-hemis (CUBA Platform)
    //
    // To manage users:
    // 1. Open old-hemis: http://localhost:8080/app/
    // 2. Navigate to: Administration > Users
    // 3. Use CUBA admin UI for user management
    // =====================================================
}
