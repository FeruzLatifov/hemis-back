package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetailsService Implementation
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Load user from database for Spring Security authentication</li>
 *   <li>Convert User entity to Spring Security UserDetails</li>
 *   <li>Parse roles from comma-separated string to GrantedAuthority list</li>
 * </ul>
 *
 * <p><strong>Used By:</strong></p>
 * <ul>
 *   <li>OAuth2 Token Endpoint (username/password authentication)</li>
 *   <li>Spring Security authentication manager</li>
 * </ul>
 *
 * <p><strong>Security Notes:</strong></p>
 * <ul>
 *   <li>Loads user even if disabled (enable check in authentication)</li>
 *   <li>Loads user even if locked (lock check in authentication)</li>
 *   <li>Password is BCrypt hashed (Spring Security handles verification)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service("customUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username for authentication
     *
     * <p><strong>Process:</strong></p>
     * <ol>
     *   <li>Query database for user by username</li>
     *   <li>Throw exception if user not found</li>
     *   <li>Parse roles from comma-separated string</li>
     *   <li>Create Spring Security UserDetails object</li>
     *   <li>Return UserDetails (Spring Security handles password verification)</li>
     * </ol>
     *
     * <p><strong>Returned UserDetails Properties:</strong></p>
     * <ul>
     *   <li>username - user login</li>
     *   <li>password - BCrypt hashed password</li>
     *   <li>authorities - list of GrantedAuthority (roles)</li>
     *   <li>accountNonExpired - always true (we don't have expiration)</li>
     *   <li>accountNonLocked - from user.accountNonLocked</li>
     *   <li>credentialsNonExpired - always true (we don't have password expiration)</li>
     *   <li>enabled - from user.enabled</li>
     * </ul>
     *
     * @param username login username
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Load user from database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Check if user is deleted (soft delete check)
        if (user.isDeleted()) {
            log.warn("Attempt to login with deleted user: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        log.debug("User found: {} (enabled: {}, locked: {})",
                username,
                user.getEnabled(),
                !user.getAccountNonLocked());

        // Parse roles from new permission system (Set<Role>) or fallback to old (String roles)
        Collection<? extends GrantedAuthority> authorities = parseAuthoritiesFromUser(user);

        log.debug("User {} has {} authorities: {}", username, authorities.size(), authorities);

        // Build Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())  // BCrypt hashed
                .authorities(authorities)
                .accountExpired(false)  // We don't have account expiration
                .accountLocked(!user.getAccountNonLocked())  // From user.accountNonLocked
                .credentialsExpired(false)  // We don't have password expiration
                .disabled(!user.getEnabled())  // From user.enabled
                .build();
    }

    /**
     * Parse authorities from User (handles both new Set<Role> and old String roles)
     *
     * <p>HYBRID APPROACH - Checks new permission system first, falls back to old</p>
     *
     * @param user User entity
     * @return collection of GrantedAuthority
     */
    private Collection<? extends GrantedAuthority> parseAuthoritiesFromUser(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // OPTION 1: New permission system (Set<Role>)
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            log.debug("Loading authorities from new permission system (Set<Role>)");

            user.getRoles().forEach(role -> {
                // Add role as authority (e.g., "ROLE_SUPER_ADMIN")
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));

                // Add all permissions from this role
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(permission -> {
                        authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                    });
                }
            });

            return authorities;
        }

        // OPTION 2: Fallback to old String roles field (deprecated)
        // Note: Cannot use user.getRoles() here as it returns Set<Role>
        // Would need user.roles direct field access (not available via getter)
        log.debug("No roles found in new permission system");

        // For now, return empty if no new roles
        // Later: Add hybrid service to check sec_user table

        log.warn("User {} has no roles assigned", user.getUsername());
        return List.of();
    }

    /**
     * Parse authorities from comma-separated roles string (DEPRECATED - for backward compatibility)
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>"ROLE_ADMIN" → [ROLE_ADMIN]</li>
     *   <li>"ROLE_ADMIN,ROLE_UNIVERSITY_ADMIN" → [ROLE_ADMIN, ROLE_UNIVERSITY_ADMIN]</li>
     *   <li>null or empty → []</li>
     * </ul>
     *
     * @param roles comma-separated roles string
     * @return collection of GrantedAuthority
     */
    private Collection<? extends GrantedAuthority> parseAuthoritiesFromString(String roles) {
        if (roles == null || roles.trim().isEmpty()) {
            return List.of();
        }

        // Split by comma and create SimpleGrantedAuthority for each role
        List<GrantedAuthority> authorities = new ArrayList<>();
        String[] roleArray = roles.split(",");

        for (String role : roleArray) {
            String trimmedRole = role.trim();
            if (!trimmedRole.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority(trimmedRole));
            }
        }

        return authorities;
    }
}
