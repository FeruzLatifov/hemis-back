package uz.hemis.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.enums.UserType;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.PasswordHistory;
import uz.hemis.domain.entity.Role;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.PasswordHistoryRepository;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.common.port.cache.CacheEvictionPort;
import uz.hemis.service.admin.dto.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Administration Service
 *
 * <p>Provides full CRUD operations for user management with hierarchical scope validation.</p>
 *
 * <p><strong>Scope Hierarchy:</strong></p>
 * <ul>
 *   <li>SUPER_ADMIN — can manage all users</li>
 *   <li>MINISTRY_ADMIN — can manage all users except SUPER_ADMIN</li>
 *   <li>UNIVERSITY_ADMIN — can only manage users within own university</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final CacheEvictionPort cacheEvictionPort;

    // =====================================================
    // READ OPERATIONS
    // =====================================================

    /**
     * Get paginated list of users with filters
     *
     * <p>UNIVERSITY_ADMIN automatically filtered to own university</p>
     */
    @Transactional(readOnly = true)
    public Page<UserAdminResponse> getUsers(String search, String role, String university,
                                            Boolean enabled, Pageable pageable, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);

        // UNIVERSITY_ADMIN: force filter to own university
        String effectiveUniversity = university;
        if (caller.hasRoleByCode("UNIVERSITY_ADMIN") && !caller.isSuperAdmin()
                && !caller.hasRoleByCode("MINISTRY_ADMIN")) {
            if (caller.getEntityCode() == null || caller.getEntityCode().isBlank()) {
                throw new AccessDeniedException("University admin has no university assigned");
            }
            effectiveUniversity = caller.getEntityCode();
        }

        // Convert null strings to empty strings to avoid Hibernate bytea parameter binding issue
        String effectiveSearch = (search == null || search.isBlank()) ? "" : search.trim();
        String effectiveRole = (role == null) ? "" : role;
        effectiveUniversity = (effectiveUniversity == null) ? "" : effectiveUniversity;

        Page<User> users = userRepository.findAllFiltered(
                effectiveSearch, effectiveRole, effectiveUniversity, enabled, pageable);

        return users.map(this::toResponse);
    }

    /**
     * Get user by ID with full details
     */
    @Transactional(readOnly = true)
    public UserAdminResponse getUserById(UUID id, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);
        User target = userRepository.findByIdWithRolesAndUniversity(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        validateReadScope(caller, target);
        return toResponse(target);
    }

    // =====================================================
    // WRITE OPERATIONS
    // =====================================================

    /**
     * Create a new user
     */
    @Transactional
    @Audited(action = AuditAction.CREATE, entity = "User", entityClass = User.class)
    public UserAdminResponse createUser(UserCreateRequest request, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);

        // Validate unique username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // UNIVERSITY_ADMIN: force entityCode to own university
        final String entityCode;
        if (caller.hasRoleByCode("UNIVERSITY_ADMIN") && !caller.isSuperAdmin()
                && !caller.hasRoleByCode("MINISTRY_ADMIN")) {
            entityCode = caller.getEntityCode();
        } else {
            entityCode = request.getEntityCode();
        }

        // Validate roles
        Set<Role> roles = resolveAndValidateRoles(request.getRoleIds(), caller);

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);

        // Set university FK and userType
        if (entityCode != null && !entityCode.isBlank()) {
            University university = universityRepository.findById(entityCode)
                    .orElseThrow(() -> new BadRequestException("University not found: " + entityCode));
            user.setUniversity(university);
            user.setEntityCode(entityCode);
            user.setUserType(UserType.UNIVERSITY);
        } else {
            user.setUserType(UserType.SYSTEM);
        }

        // Assign roles
        for (Role role : roles) {
            user.addRole(role);
        }

        User saved = userRepository.save(user);

        // Save initial password to history
        PasswordHistory ph = new PasswordHistory();
        ph.setUser(saved);
        ph.setPasswordHash(saved.getPassword());
        passwordHistoryRepository.save(ph);

        log.info("User created: username={}, entityCode={}, roles={}, by={}",
                saved.getUsername(), saved.getEntityCode(),
                roles.stream().map(Role::getCode).collect(Collectors.joining(",")),
                callerUserId);

        return toResponse(saved);
    }

    /**
     * Update user profile and roles
     */
    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "User", entityClass = User.class, keyArg = "id")
    public UserAdminResponse updateUser(UUID id, UserUpdateRequest request, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);
        User target = userRepository.findByIdWithRolesAndUniversity(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        validateWriteScope(caller, target);

        // Block self role-modification (privilege escalation prevention)
        if (id.equals(callerUserId) && request.getRoleIds() != null) {
            throw new BadRequestException("Cannot modify your own roles");
        }

        // Update profile fields
        if (request.getFullName() != null) {
            target.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            target.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            target.setPhone(request.getPhone());
        }

        // Update entityCode + university FK (only SUPER_ADMIN and MINISTRY_ADMIN can change)
        if (request.getEntityCode() != null) {
            if (caller.isSuperAdmin() || caller.hasRoleByCode("MINISTRY_ADMIN")) {
                if (request.getEntityCode().isBlank()) {
                    target.setEntityCode(null);
                    target.setUniversity(null);
                    target.setUserType(UserType.SYSTEM);
                } else {
                    University uni = universityRepository.findById(request.getEntityCode())
                            .orElseThrow(() -> new BadRequestException("University not found: " + request.getEntityCode()));
                    target.setEntityCode(request.getEntityCode());
                    target.setUniversity(uni);
                    target.setUserType(UserType.UNIVERSITY);
                }
            }
            // UNIVERSITY_ADMIN cannot change entityCode — silently ignored
        }

        // Update roles
        if (request.getRoleIds() != null) {
            Set<Role> newRoles = resolveAndValidateRoles(request.getRoleIds(), caller);

            // Clear existing roles
            new HashSet<>(target.getRoles()).forEach(target::removeRole);

            // Assign new roles
            for (Role role : newRoles) {
                target.addRole(role);
            }

            // Evict permission cache since roles changed
            cacheEvictionPort.evictPermissionsForUser(id.toString());
            cacheEvictionPort.evictMenuForUser(id.toString());
        }

        User saved = userRepository.save(target);
        log.info("User updated: id={}, username={}, by={}", id, saved.getUsername(), callerUserId);

        return toResponse(saved);
    }

    /**
     * Change user password
     */
    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "User", entityClass = User.class, keyArg = "id")
    public void changePassword(UUID id, String newPassword, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);
        User target = userRepository.findByIdWithRolesAndUniversity(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        validateWriteScope(caller, target);

        // Check password history — prevent reuse of last 5 passwords
        List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserOrderByCreatedAtDesc(target);
        for (PasswordHistory ph : history) {
            if (passwordEncoder.matches(newPassword, ph.getPasswordHash())) {
                throw new BadRequestException("Password was recently used. Choose a different password.");
            }
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        target.setPassword(encodedPassword);
        userRepository.save(target);

        // Save to password history
        PasswordHistory ph = new PasswordHistory();
        ph.setUser(target);
        ph.setPasswordHash(encodedPassword);
        passwordHistoryRepository.save(ph);

        log.info("Password changed for user: id={}, username={}, by={}", id, target.getUsername(), callerUserId);
    }

    /**
     * Toggle user enabled/disabled status
     */
    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "User", entityClass = User.class, keyArg = "id")
    public UserAdminResponse toggleStatus(UUID id, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);
        User target = userRepository.findByIdWithRolesAndUniversity(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        validateWriteScope(caller, target);

        // Self-disable prohibition
        if (id.equals(callerUserId)) {
            throw new BadRequestException("Cannot disable your own account");
        }

        target.setEnabled(!target.getEnabled());
        User saved = userRepository.save(target);

        // Evict cache if disabled
        if (!saved.getEnabled()) {
            cacheEvictionPort.evictPermissionsForUser(id.toString());
            cacheEvictionPort.evictMenuForUser(id.toString());
        }

        log.info("User status toggled: id={}, enabled={}, by={}", id, saved.getEnabled(), callerUserId);
        return toResponse(saved);
    }

    /**
     * Unlock a locked account
     */
    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "User", entityClass = User.class, keyArg = "id")
    public UserAdminResponse unlockAccount(UUID id, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);
        User target = userRepository.findByIdWithRolesAndUniversity(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        validateWriteScope(caller, target);

        target.setAccountNonLocked(true);
        target.setFailedAttempts(0);
        User saved = userRepository.save(target);

        log.info("Account unlocked: id={}, username={}, by={}", id, saved.getUsername(), callerUserId);
        return toResponse(saved);
    }

    /**
     * Soft delete a user
     */
    @Transactional
    @Audited(action = AuditAction.DELETE, entity = "User", entityClass = User.class, keyArg = "id")
    public void softDelete(UUID id, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);
        User target = userRepository.findByIdWithRolesAndUniversity(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        validateWriteScope(caller, target);

        // Self-delete prohibition
        if (id.equals(callerUserId)) {
            throw new BadRequestException("Cannot delete your own account");
        }

        target.setEnabled(false);
        target.softDelete();
        userRepository.save(target);

        // Evict permission + menu cache
        cacheEvictionPort.evictPermissionsForUser(id.toString());
        cacheEvictionPort.evictMenuForUser(id.toString());

        log.info("User soft-deleted: id={}, username={}, by={}", id, target.getUsername(), callerUserId);
    }

    // =====================================================
    // DICTIONARY ENDPOINTS
    // =====================================================

    /**
     * Get all active roles for dropdown
     */
    @Transactional(readOnly = true)
    public List<RoleSummary> getActiveRoles(UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);
        List<Role> roles = roleRepository.findAllActive();

        // UNIVERSITY_ADMIN: filter out SYSTEM roles they can't assign
        if (!caller.isSuperAdmin() && !caller.hasRoleByCode("MINISTRY_ADMIN")
                && caller.hasRoleByCode("UNIVERSITY_ADMIN")) {
            roles = roles.stream()
                    .filter(r -> !r.isSystemRole())
                    .collect(Collectors.toList());
        }

        return roles.stream()
                .map(this::toRoleSummary)
                .collect(Collectors.toList());
    }

    /**
     * Get permissions for a specific role
     */
    @Transactional(readOnly = true)
    public List<String> getRolePermissions(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        return role.getPermissions().stream()
                .map(p -> p.getCode())
                .sorted()
                .collect(Collectors.toList());
    }

    // =====================================================
    // SCOPE VALIDATION
    // =====================================================

    private void validateReadScope(User caller, User target) {
        // SUPER_ADMIN — no restrictions
        if (caller.isSuperAdmin()) return;

        // MINISTRY_ADMIN — can see all users
        if (caller.hasRoleByCode("MINISTRY_ADMIN")) return;

        // UNIVERSITY_ADMIN — only own university
        if (caller.hasRoleByCode("UNIVERSITY_ADMIN")) {
            if (!Objects.equals(caller.getEntityCode(), target.getEntityCode())) {
                throw new AccessDeniedException("Cannot access users from another university");
            }
            return;
        }

        throw new AccessDeniedException("No permission for user management");
    }

    private void validateWriteScope(User caller, User target) {
        // SUPER_ADMIN — no restrictions
        if (caller.isSuperAdmin()) return;

        // MINISTRY_ADMIN — all users except SUPER_ADMIN
        if (caller.hasRoleByCode("MINISTRY_ADMIN")) {
            if (target.isSuperAdmin()) {
                throw new AccessDeniedException("Cannot modify SUPER_ADMIN");
            }
            return;
        }

        // UNIVERSITY_ADMIN — only own university, no system role users
        if (caller.hasRoleByCode("UNIVERSITY_ADMIN")) {
            if (!Objects.equals(caller.getEntityCode(), target.getEntityCode())) {
                throw new AccessDeniedException("Cannot access users from another university");
            }
            if (target.getRoles().stream().anyMatch(Role::isSystemRole)) {
                throw new AccessDeniedException("Cannot modify system role users");
            }
            return;
        }

        throw new AccessDeniedException("No permission for user management");
    }

    /**
     * Resolve role IDs to entities and validate caller can assign them
     */
    private Set<Role> resolveAndValidateRoles(Set<UUID> roleIds, User caller) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("At least one role is required");
        }

        Set<Role> roles = new HashSet<>();
        for (UUID roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

            if (!role.isActive()) {
                throw new BadRequestException("Role is not active: " + role.getCode());
            }

            // MINISTRY_ADMIN cannot assign SUPER_ADMIN role
            if (caller.hasRoleByCode("MINISTRY_ADMIN") && !caller.isSuperAdmin()) {
                if ("SUPER_ADMIN".equals(role.getCode())) {
                    throw new AccessDeniedException("Cannot assign SUPER_ADMIN role");
                }
            }

            // UNIVERSITY_ADMIN cannot assign SYSTEM roles
            if (caller.hasRoleByCode("UNIVERSITY_ADMIN") && !caller.isSuperAdmin()
                    && !caller.hasRoleByCode("MINISTRY_ADMIN")) {
                if (role.isSystemRole()) {
                    throw new AccessDeniedException("Cannot assign system role: " + role.getCode());
                }
            }

            roles.add(role);
        }

        return roles;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private User findCallerWithPermissions(UUID callerUserId) {
        return userRepository.findByIdWithRolesAndUniversity(callerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", callerUserId));
    }

    private UserAdminResponse toResponse(User user) {
        List<RoleSummary> roles = user.getRoles().stream()
                .map(this::toRoleSummary)
                .sorted(Comparator.comparing(RoleSummary::getCode))
                .collect(Collectors.toList());

        String universityName = null;
        if (user.getUniversity() != null) {
            universityName = user.getUniversity().getName();
        }

        return UserAdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .entityCode(user.getEntityCode())
                .universityName(universityName)
                .userType(user.getUserType() != null ? user.getUserType().name() : null)
                .enabled(user.getEnabled())
                .accountNonLocked(user.getAccountNonLocked())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private RoleSummary toRoleSummary(Role role) {
        return RoleSummary.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .roleType(role.getRoleType())
                .build();
    }
}
