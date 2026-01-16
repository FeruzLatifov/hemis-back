package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.security.LegacyLoadedUser;
import uz.hemis.common.port.security.LegacyUserLoadingPort;

import java.util.Collection;
import java.util.stream.Collectors;

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

    private final LegacyUserLoadingPort legacyUserLoadingPort;

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading sec_user by username: {}", username);

        // Load user via port (Clean Architecture)
        LegacyLoadedUser user = legacyUserLoadingPort.findActiveByLogin(username)
                .orElseThrow(() -> {
                    log.warn("SecUser not found or inactive: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Additional check: user should not be deleted
        if (user.isDeleted()) {
            log.warn("Attempt to login with deleted sec_user: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        log.debug("SecUser found: {} (active: {}, university: {}, systemAdmin: {})",
                username,
                user.isActive(),
                user.getUniversityCode(),
                user.isSystemAdmin());

        // Authorities are pre-computed by adapter
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        log.debug("SecUser {} has {} authorities: {}", username, authorities.size(), authorities);

        // Build Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isActive())
                .build();
    }

    // NOTE: Authority mapping is now done by LegacyUserLoadingAdapter (Clean Architecture)
    // This service only converts LegacyLoadedUser DTO to Spring Security UserDetails
    //
    // User management is done by old-hemis (CUBA Platform):
    // 1. Open old-hemis: http://localhost:8081/app/
    // 2. Navigate to: Administration > Users
    // 3. Use CUBA admin UI for user management
}
