package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.*;

/**
 * User Legacy Service - CUBA compatible operations for user/auth endpoints
 *
 * Handles user lookup operations for legacy API endpoints:
 * - /app/rest/v2/user/me
 * - /app/rest/v2/user/validate
 * - /app/rest/v2/userInfo
 * - /app/rest/user/info
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserLegacyService {

    private final UserRepository userRepository;

    // ==================== User Lookup ====================

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByIdWithUniversity(UUID id) {
        return userRepository.findByIdWithUniversity(id);
    }

    public Optional<User> findByUsernameWithRoles(String username) {
        return userRepository.findByUsernameWithRoles(username);
    }

    /**
     * Find university code by user ID (for legacy endpoints)
     */
    public Optional<String> findUniversityCodeById(UUID userId) {
        return userRepository.findUniversityCodeById(userId);
    }

    // ==================== Response Mapping ====================

    /**
     * Build user response map for /app/rest/v2/user/me endpoint
     */
    public Map<String, Object> toUserMeResponse(User user) {
        // CUBA klient (Univer Yii2 PHP) field tartibni kutadi — LinkedHashMap MAJBURIY
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId().toString());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("roles", user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().stream().map(Role::getCode).toArray(String[]::new)
                : new String[0]);
        response.put("university", user.getUniversityCode());
        response.put("enabled", user.getEnabled());
        response.put("accountNonLocked", user.getAccountNonLocked());
        return response;
    }

    /**
     * Build validate token response
     */
    public Map<String, Object> toValidateResponse(User user) {
        // CUBA klient field tartibni kutadi — LinkedHashMap MAJBURIY
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valid", true);
        response.put("userId", user.getId().toString());
        response.put("username", user.getUsername());
        return response;
    }

    /**
     * Get university name from user
     * OLD-HEMIS format: returns university name or empty string
     */
    public String getUniversityName(User user) {
        if (user.getUniversity() != null && user.getUniversity().getName() != null) {
            return user.getUniversity().getName();
        }
        return "";
    }

    /**
     * Build instance name (old-hemis format)
     * Format: "{universityName} [{login}]"
     */
    public String buildInstanceName(User user, String universityName) {
        if (universityName != null && !universityName.isEmpty()) {
            return universityName + " [" + user.getUsername() + "]";
        }
        return user.getUsername() + " [" + user.getUsername() + "]";
    }
}
