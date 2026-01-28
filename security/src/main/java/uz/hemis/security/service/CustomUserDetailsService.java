package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.security.LoadedUser;
import uz.hemis.common.port.security.UserLoadingPort;

import java.util.Collection;
import java.util.stream.Collectors;

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

    private final UserLoadingPort userLoadingPort;

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Load user via port (Clean Architecture)
        LoadedUser user = userLoadingPort.findByUsername(username)
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
                user.isEnabled(),
                !user.isAccountNonLocked());

        // Authorities are pre-computed by adapter
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        log.debug("User {} has {} authorities: {}", username, authorities.size(), authorities);

        // Build Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())  // BCrypt hashed
                .authorities(authorities)
                .accountExpired(false)  // We don't have account expiration
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(false)  // We don't have password expiration
                .disabled(!user.isEnabled())
                .build();
    }

    // NOTE: Authority parsing is now done by UserLoadingAdapter (Clean Architecture)
    // This service only converts LoadedUser DTO to Spring Security UserDetails
}
