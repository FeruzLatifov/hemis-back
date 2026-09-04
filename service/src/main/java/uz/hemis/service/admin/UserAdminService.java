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
import uz.hemis.common.exception.ConflictException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.security.Permission;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.common.port.cache.CacheEvictionPort;
import uz.hemis.service.admin.dto.*;

import java.time.LocalDate;
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
 *   <li>ADMIN — can manage all users except SUPER_ADMIN</li>
 *   <li>OTM_API — can only manage users within own university</li>
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
    private final CacheEvictionPort cacheEvictionPort;
    private final LoginNameGenerator loginNameGenerator;

    // =====================================================
    // READ OPERATIONS
    // =====================================================

    /**
     * Get paginated list of users with filters
     *
     * <p>OTM_API automatically filtered to own university</p>
     */
    @Transactional(readOnly = true)
    public Page<UserAdminResponse> getUsers(String search, String role, String university,
                                            Boolean enabled, Pageable pageable, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);

        // OTM_API: force filter to own university
        String effectiveUniversity = university;
        if (caller.hasRoleByCode("OTM_API") && !caller.isSuperAdmin()
                && !caller.hasRoleByCode("ADMIN")) {
            if (caller.getUniversityCode() == null || caller.getUniversityCode().isBlank()) {
                throw new AccessDeniedException("University admin has no university assigned");
            }
            effectiveUniversity = caller.getUniversityCode();
        }

        // Convert null strings to empty strings to avoid Hibernate bytea parameter binding issue
        String effectiveSearch = (search == null || search.isBlank()) ? "" : search.trim();
        String effectiveRole = (role == null) ? "" : role;
        effectiveUniversity = (effectiveUniversity == null) ? "" : effectiveUniversity;

        Page<User> users = userRepository.findAllFiltered(
                effectiveSearch, effectiveRole, effectiveUniversity, enabled, pageable);

        boolean canViewPinfl = caller.hasPermission("pinfl.view");
        return users.map(u -> toResponse(u, canViewPinfl));
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
        return toResponse(target, caller.hasPermission("pinfl.view"));
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
        // Takroriy parol — hech narsa qilishdan OLDIN. Maydon ixtiyoriy (eski API mijozlari uni
        // yubormaydi), lekin yuborilgan bo'lsa mos kelishi shart — changePassword bilan bir xil
        // semantika va bir xil xabar.
        String confirmPassword = request.getConfirmPassword();
        if (confirmPassword != null && !confirmPassword.isBlank()
                && !confirmPassword.equals(request.getPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        User caller = findCallerWithPermissions(callerUserId);

        boolean isUniversityLogin = "UNIVERSITY_LOGIN".equalsIgnoreCase(request.getAccountType());

        // Resolve login username per account type:
        //  - UNIVERSITY_LOGIN: manual service login (old-hemis password-grant account)
        //  - PERSON (default):  login = ism_familiya slug (PINFL EMAS); PINFL o'z ustunida qoladi
        //    va bitta-shaxs-bitta-akkaunt tekshiruvini bajaradi (dublikatda 409).
        // Band login ikkala tarmoqda ham 409 (400 emas) — dublikat PINFL bilan bir xil semantika.
        final String username;
        if (isUniversityLogin) {
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                throw new BadRequestException("Username is required for a university login");
            }
            username = request.getUsername().trim();
            if (userRepository.existsByUsernameIgnoreCase(username)) {
                throw new ConflictException("Username already exists");
            }
        } else {
            String pinfl = request.getPinfl();
            if (pinfl == null || !pinfl.matches("^\\d{14}$")) {
                throw new BadRequestException("PINFL (14 digits) is required for a person account");
            }
            if (userRepository.existsByPinfl(pinfl)) {
                throw new ConflictException("A user with this PINFL already exists");
            }
            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                // Operator taklif qilingan loginni qo'lda tahrirlagan — uni hurmat qilamiz.
                // Band bo'lsa 409: FE boshqasini tanlashi uchun (jimgina suffiks qo'shmaymiz —
                // operator kutgan login boshqa loginga aylanib qolmasin).
                username = request.getUsername().trim();
                if (userRepository.existsByUsernameIgnoreCase(username)) {
                    throw new ConflictException("Username already exists");
                }
            } else {
                username = loginNameGenerator.generate(request.getFirstName(), request.getLastName());
            }
        }

        // OTM_API: force universityCode to own university
        final String universityCode;
        if (caller.hasRoleByCode("OTM_API") && !caller.isSuperAdmin()
                && !caller.hasRoleByCode("ADMIN")) {
            universityCode = caller.getUniversityCode();
        } else {
            universityCode = request.getUniversityCode();
        }

        // Validate roles
        Set<Role> roles = resolveAndValidateRoles(request.getRoleIds(), caller);

        // Create user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);

        if (isUniversityLogin) {
            // Service/integration login — no person identity.
            user.setFullName(request.getFullName());
        } else {
            // Person — PINFL + GUVD passport-data autofilled fields.
            user.setPinfl(request.getPinfl());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setMiddleName(request.getMiddleName());
            user.setFullName(resolveFullName(request));
            user.setBirthDate(parseDate(request.getBirthDate()));
            user.setBirthPlace(request.getBirthPlace());
            user.setPassport(request.getPassport());
            user.setPassportGivePlace(request.getPassportGivePlace());
            user.setPassportIssuedDate(parseDate(request.getPassportIssuedDate()));
            user.setPassportExpiryDate(parseDate(request.getPassportExpiryDate()));
            user.setGender(request.getGender());
            user.setNationality(request.getNationality());
            user.setAddress(request.getAddress());
            user.setPhoto(request.getPhoto());
        }

        // Set university FK and userType
        if (universityCode != null && !universityCode.isBlank()) {
            University uni = universityRepository.findById(universityCode)
                    .orElseThrow(() -> new BadRequestException("University not found: " + universityCode));
            user.setUniversity(uni);
            user.setUserType(UserType.UNIVERSITY);
        } else {
            user.setUserType(UserType.SYSTEM);
        }

        // Assign roles
        for (Role role : roles) {
            user.addRole(role);
        }

        User saved = userRepository.save(user);

        // PII-safe: log the UUID id, never the username/PINFL (login is a name slug — still PII).
        log.info("User created: id={}, accountType={}, universityCode={}, roles={}, by={}",
                saved.getId(), isUniversityLogin ? "UNIVERSITY_LOGIN" : "PERSON", saved.getUniversityCode(),
                roles.stream().map(Role::getCode).collect(Collectors.joining(",")),
                callerUserId);

        return toResponse(saved, caller.hasPermission("pinfl.view"));
    }

    /** Prefer an explicit full name; otherwise compose "Last First Middle". */
    private static String resolveFullName(UserCreateRequest r) {
        if (r.getFullName() != null && !r.getFullName().isBlank()) {
            return r.getFullName().trim();
        }
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{r.getLastName(), r.getFirstName(), r.getMiddleName()}) {
            if (part != null && !part.isBlank()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(part.trim());
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    /** Parse ISO {@code yyyy-MM-dd}; null/blank/invalid → null (autofill is best-effort). */
    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
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

        // Self role-modification prevention — silently ignore (don't throw)
        // Frontend always sends roleIds; when editing self, just skip role changes
        boolean isSelfEdit = id.equals(callerUserId);
        if (isSelfEdit && request.getRoleIds() != null) {
            log.info("Self-edit detected for user {}, ignoring roleIds", id);
            request.setRoleIds(null);
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

        // Update university FK (only SUPER_ADMIN and ADMIN can change)
        if (request.getUniversityCode() != null) {
            if (caller.isSuperAdmin() || caller.hasRoleByCode("ADMIN")) {
                if (request.getUniversityCode().isBlank()) {
                    target.setUniversity(null);
                    target.setUserType(UserType.SYSTEM);
                } else {
                    University uni = universityRepository.findById(request.getUniversityCode())
                            .orElseThrow(() -> new BadRequestException("University not found: " + request.getUniversityCode()));
                    target.setUniversity(uni);
                    target.setUserType(UserType.UNIVERSITY);
                }
            }
            // OTM_API cannot change university — silently ignored
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

            // Evict permission + menu + scope cache since roles/university changed
            cacheEvictionPort.evictPermissionsForUser(id.toString());
            cacheEvictionPort.evictMenuForUser(id.toString());
            cacheEvictionPort.evictScopeForUser(id.toString());
        }

        User saved = userRepository.save(target);
        log.info("User updated: id={}, by={}", id, callerUserId);

        return toResponse(saved, caller.hasPermission("pinfl.view"));
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(UUID id, String newPassword, UUID callerUserId) {
        User caller = findCallerWithPermissions(callerUserId);
        User target = userRepository.findByIdWithRolesAndUniversity(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        validateWriteScope(caller, target);

        boolean hadPassword = target.getPassword() != null;
        target.setPassword(passwordEncoder.encode(newPassword));

        userRepository.saveAndFlush(target);

        // PII-safe: do NOT log password hash prefixes (even partial — they aid offline bruteforce
        // when correlated with leaked databases). Audit trail covers the actual change.
        log.info("PASSWORD CHANGE: id={}, hadPriorPassword={}, by={}",
                id, hadPassword, callerUserId);
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
            cacheEvictionPort.evictScopeForUser(id.toString());
        }

        log.info("User status toggled: id={}, enabled={}, by={}", id, saved.getEnabled(), callerUserId);
        return toResponse(saved, caller.hasPermission("pinfl.view"));
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
        target.setLockedAt(null);
        User saved = userRepository.save(target);

        log.info("Account unlocked: id={}, by={}", id, callerUserId);
        return toResponse(saved, caller.hasPermission("pinfl.view"));
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

        // Evict permission + menu + scope cache
        cacheEvictionPort.evictPermissionsForUser(id.toString());
        cacheEvictionPort.evictMenuForUser(id.toString());
        cacheEvictionPort.evictScopeForUser(id.toString());

        log.info("User soft-deleted: id={}, by={}", id, callerUserId);
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

        // OTM_API: filter out SYSTEM roles they can't assign
        if (!caller.isSuperAdmin() && !caller.hasRoleByCode("ADMIN")
                && caller.hasRoleByCode("OTM_API")) {
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
        // Eager fetch — LAZY collection N+1 query xavfini bartaraf etadi
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        return role.getPermissions().stream()
                .map(p -> p.getCode())
                .sorted()
                .toList();
    }

    // =====================================================
    // SCOPE VALIDATION
    // =====================================================

    private void validateReadScope(User caller, User target) {
        // SUPER_ADMIN — no restrictions
        if (caller.isSuperAdmin()) return;

        // ADMIN — can see all users
        if (caller.hasRoleByCode("ADMIN")) return;

        // OTM_API — only own university
        if (caller.hasRoleByCode("OTM_API")) {
            if (!Objects.equals(caller.getUniversityCode(), target.getUniversityCode())) {
                throw new AccessDeniedException("Cannot access users from another university");
            }
            return;
        }

        throw new AccessDeniedException("No permission for user management");
    }

    private void validateWriteScope(User caller, User target) {
        // SUPER_ADMIN — no restrictions
        if (caller.isSuperAdmin()) return;

        // ADMIN — all users except SUPER_ADMIN
        if (caller.hasRoleByCode("ADMIN")) {
            if (target.isSuperAdmin()) {
                throw new AccessDeniedException("Cannot modify SUPER_ADMIN");
            }
            return;
        }

        // OTM_API — only own university, no system role users
        if (caller.hasRoleByCode("OTM_API")) {
            if (!Objects.equals(caller.getUniversityCode(), target.getUniversityCode())) {
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
    /**
     * Refuse to assign a role that carries a permission the caller does not have.
     *
     * <p>SUPER_ADMIN is exempt — it holds everything by definition, and the check would be a no-op.
     * The role is loaded with its permissions (the caller's own set is already resolved for the
     * request), and the first extra permission names itself in the refusal so the operator can see
     * what to ask for rather than guessing.</p>
     */
    private void assertCallerCanGrant(User caller, Role role) {
        if (caller.isSuperAdmin()) {
            return;
        }
        Set<String> callerPermissions = caller.getAllPermissions().stream()
                .map(Permission::getCode)
                .collect(java.util.stream.Collectors.toSet());
        if (callerPermissions.contains("*")) {
            return;
        }
        Role withPermissions = roleRepository.findByIdWithPermissions(role.getId()).orElse(role);
        String beyondCaller = withPermissions.getPermissions().stream()
                .map(Permission::getCode)
                .filter(code -> !callerPermissions.contains(code))
                .sorted()
                .findFirst()
                .orElse(null);
        if (beyondCaller != null) {
            throw new AccessDeniedException(
                    "Cannot assign role " + role.getCode() + ": it grants '" + beyondCaller
                            + "', which you do not hold");
        }
    }

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

            // ADMIN cannot assign SUPER_ADMIN role
            if (caller.hasRoleByCode("ADMIN") && !caller.isSuperAdmin()) {
                if ("SUPER_ADMIN".equals(role.getCode())) {
                    throw new AccessDeniedException("Cannot assign SUPER_ADMIN role");
                }
            }

            // OTM_API cannot assign SYSTEM roles
            if (caller.hasRoleByCode("OTM_API") && !caller.isSuperAdmin()
                    && !caller.hasRoleByCode("ADMIN")) {
                if (role.isSystemRole()) {
                    throw new AccessDeniedException("Cannot assign system role: " + role.getCode());
                }
            }

            // Privilege ceiling: you cannot hand out what you do not hold.
            //
            // Naming SUPER_ADMIN above is not enough — the boundary S038 draws (an administrator has
            // no roles.manage, no webhook secrets, no registry deletes) is worth nothing if the same
            // administrator can create a user, give them OTM_API (which does hold students.delete),
            // and log in as them. Comparing permission SETS closes every such route at once, and it
            // needs no list to maintain: whatever a role holds, the assigner must hold too.
            assertCallerCanGrant(caller, role);

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

    /**
     * @param canViewPinfl whether the CALLER holds {@code pinfl.view} — when false, the PII PINFL
     *                     is omitted from the response (read-gated; least privilege).
     */
    private UserAdminResponse toResponse(User user, boolean canViewPinfl) {
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
                .pinfl(canViewPinfl ? user.getPinfl() : null)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .passport(canViewPinfl ? user.getPassport() : null)
                .birthDate(user.getBirthDate())
                .birthPlace(user.getBirthPlace())
                .gender(user.getGender())
                .nationality(user.getNationality())
                .address(user.getAddress())
                .universityCode(user.getUniversityCode())
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
