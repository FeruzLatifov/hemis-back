package uz.hemis.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.admin.dto.auth.*;
import uz.hemis.admin.dto.common.MultilingualString;
import uz.hemis.admin.dto.university.UniversityDTO;
import uz.hemis.admin.dto.user.AdminUserDTO;
import uz.hemis.admin.entity.*;
import uz.hemis.admin.exception.AuthenticationException;
import uz.hemis.admin.repository.*;
import uz.hemis.admin.security.JwtTokenProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Authentication Service
 *
 * Handles user authentication, login, logout, token refresh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UniversityUserRepository universityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate user and generate JWT tokens
     *
     * @param request Login credentials
     * @return Login response with tokens and user info
     * @throws AuthenticationException if authentication fails
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // 1. Find user by username
        User user = userRepository.findByUsernameAndDeletedAtIsNull(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.getUsername());
                    return new AuthenticationException("Invalid username or password");
                });

        // 2. Check if user is active
        if (!user.isActive()) {
            log.warn("Login failed - user account disabled: {}", request.getUsername());
            throw new AuthenticationException("User account is disabled");
        }

        // 3. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for user: {}", request.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        // 4. Get university association
        UniversityUser universityUser = universityUserRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.error("Login failed - user has no university assigned: {}", request.getUsername());
                    return new AuthenticationException("User has no university assigned");
                });

        University university = universityUser.getUniversity();

        // 5. Check if university is active
        if (!university.isActive()) {
            log.warn("Login failed - university is inactive: {}", university.getCode());
            throw new AuthenticationException("University is inactive");
        }

        // 6. Generate tokens with permissions
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                university.getId(),
                user.getPermissionStrings()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 7. Load permissions (TODO: implement proper permission loading)
        List<PermissionDTO> permissions = loadUserPermissions(user);

        // 8. Build response
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(buildAdminUserDTO(user, request.getLocale()))
                .university(buildUniversityDTO(university))
                .permissions(permissions)
                .build();

        log.info("Login successful for user: {} (university: {})", request.getUsername(), university.getCode());
        return response;
    }

    /**
     * Refresh access token using refresh token
     *
     * @param refreshToken Refresh token
     * @return New token pair
     * @throws AuthenticationException if refresh token is invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        // 1. Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Token refresh failed - invalid refresh token");
            throw new AuthenticationException("Invalid refresh token");
        }

        // 2. Check token type
        if (!jwtTokenProvider.validateTokenWithType(refreshToken, "refresh")) {
            log.warn("Token refresh failed - wrong token type");
            throw new AuthenticationException("Invalid token type");
        }

        // 3. Extract user ID
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 4. Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed - user not found: {}", userId);
                    return new AuthenticationException("User not found");
                });

        // 5. Check if user is active
        if (!user.isActive()) {
            log.warn("Token refresh failed - user account disabled: {}", user.getUsername());
            throw new AuthenticationException("User account is disabled");
        }

        // 6. Get university
        UniversityUser universityUser = universityUserRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Token refresh failed - user has no university assigned: {}", user.getUsername());
                    return new AuthenticationException("User has no university assigned");
                });

        University university = universityUser.getUniversity();

        // 7. Generate new tokens with permissions
        String newToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                university.getId(),
                user.getPermissionStrings()
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 8. Build response
        LoginResponse response = LoginResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .user(buildAdminUserDTO(user, user.getLocale()))
                .university(buildUniversityDTO(university))
                .permissions(loadUserPermissions(user))
                .build();

        log.info("Token refresh successful for user: {}", user.getUsername());
        return response;
    }

    /**
     * Logout user
     * TODO: Implement token blacklisting if needed
     *
     * @param userId User ID from token
     */
    public void logout(String userId) {
        log.info("Logout for user: {}", userId);
        // TODO: Add token to blacklist (Redis or database)
        // For now, client-side token removal is sufficient
    }

    /**
     * Get current authenticated user
     *
     * @param userId User ID from token
     * @return Admin user DTO
     */
    @Transactional(readOnly = true)
    public AdminUserDTO getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return buildAdminUserDTO(user, user.getLocale());
    }

    /**
     * Build AdminUserDTO from User entity
     */
    private AdminUserDTO buildAdminUserDTO(User user, String locale) {
        return AdminUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .locale(locale != null ? locale : user.getLocale())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Build UniversityDTO from University entity
     */
    private UniversityDTO buildUniversityDTO(University university) {
        return UniversityDTO.builder()
                .id(university.getId())
                .code(university.getCode())
                .name(MultilingualString.builder()
                        .uz(university.getNameUz())
                        .ru(university.getNameRu())
                        .en(university.getNameEn())
                        .build())
                .shortName(MultilingualString.builder()
                        .uz(university.getShortNameUz())
                        .ru(university.getShortNameRu())
                        .en(university.getShortNameEn())
                        .build())
                .tin(university.getTin())
                .active(university.getActive())
                .build();
    }

    /**
     * Load user permissions from roles
     *
     * @param user User entity with loaded roles
     * @return List of permissions grouped by entity
     */
    private List<PermissionDTO> loadUserPermissions(User user) {
        log.debug("Loading permissions for user: {}", user.getUsername());

        // Get all permissions from all user roles
        var allPermissions = user.getAllPermissions();

        if (allPermissions.isEmpty()) {
            log.warn("User {} has no permissions assigned", user.getUsername());
            return new ArrayList<>();
        }

        // Group permissions by entity
        var permissionsByEntity = new java.util.HashMap<String, java.util.List<String>>();

        for (Permission perm : allPermissions) {
            if (perm.isAllowed()) {
                String entity = perm.getEntity();
                String action = perm.getAction();

                permissionsByEntity
                    .computeIfAbsent(entity, k -> new ArrayList<>())
                    .add(action);
            }
        }

        // Convert to PermissionDTO list
        List<PermissionDTO> permissions = new ArrayList<>();
        permissionsByEntity.forEach((entity, actions) -> {
            permissions.add(PermissionDTO.builder()
                .entity(entity)
                .actions(actions)
                .build());
        });

        log.debug("Loaded {} permission groups for user: {}", permissions.size(), user.getUsername());
        return permissions;
    }
}
