package uz.hemis.domain.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.security.SecUser;
import uz.hemis.domain.repository.SecUserRepository;
import uz.hemis.common.dto.security.LegacyLoadedUser;
import uz.hemis.common.port.security.LegacyUserLoadingPort;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Legacy User Loading Adapter - Implements LegacyUserLoadingPort
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This adapter is in domain module (outer layer)</li>
 *   <li>Implements port defined in security module (inner layer)</li>
 *   <li>Converts SecUser JPA entity to security DTO</li>
 * </ul>
 *
 * <p><strong>CUBA Compatibility:</strong></p>
 * <ul>
 *   <li>Queries sec_user table</li>
 *   <li>Maps CUBA groups to Spring Security roles</li>
 *   <li>Handles soft delete pattern</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LegacyUserLoadingAdapter implements LegacyUserLoadingPort {

    private final SecUserRepository secUserRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<LegacyLoadedUser> findActiveByLogin(String login) {
        log.debug("Loading active legacy user by login: {}", login);

        return secUserRepository.findByLoginAndActiveTrue(login)
                .map(this::toLegacyLoadedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LegacyLoadedUser> findByLogin(String login) {
        log.debug("Loading legacy user by login: {}", login);

        return secUserRepository.findByLogin(login)
                .map(this::toLegacyLoadedUser);
    }

    /**
     * Convert SecUser entity to LegacyLoadedUser DTO
     *
     * <p>Maps CUBA groups to Spring Security authorities</p>
     */
    private LegacyLoadedUser toLegacyLoadedUser(SecUser user) {
        Set<String> authorities = mapAuthorities(user);

        return LegacyLoadedUser.builder()
                .id(user.getId())
                .login(user.getLogin())
                .password(user.getPassword())
                .active(user.getActive() != null && user.getActive())
                .deleted(user.isDeleted())
                .groupNames(user.getGroupNames())
                .systemAdmin(user.isSystemAdmin())
                .universityCode(user.getUniversityCode())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .authorities(authorities)
                .build();
    }

    /**
     * Map CUBA groups to Spring Security authorities
     */
    private Set<String> mapAuthorities(SecUser user) {
        Set<String> authorities = new HashSet<>();

        // Map from CUBA group_names field
        if (user.getGroupNames() != null && !user.getGroupNames().isEmpty()) {
            String[] groups = user.getGroupNames().split(",");
            for (String group : groups) {
                String trimmedGroup = group.trim();
                if (!trimmedGroup.isEmpty()) {
                    String role = mapGroupToRole(trimmedGroup);
                    authorities.add(role);
                }
            }
        }

        // Add based on system admin status
        if (user.isSystemAdmin()) {
            authorities.add("ROLE_ADMIN");
        } else if (user.getUniversityCode() != null) {
            authorities.add("ROLE_OTM_API");
        }

        // Fallback if no roles assigned
        if (authorities.isEmpty()) {
            authorities.add("ROLE_USER");
        }

        return authorities;
    }

    /**
     * Map CUBA group name to Spring Security role
     */
    private String mapGroupToRole(String groupName) {
        String lowerGroupName = groupName.toLowerCase();

        if (lowerGroupName.contains("admin")) {
            return "ROLE_ADMIN";
        } else if (lowerGroupName.contains("university")) {
            return "ROLE_OTM_API";
        } else {
            return "ROLE_USER";
        }
    }
}
