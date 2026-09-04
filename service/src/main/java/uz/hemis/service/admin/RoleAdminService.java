package uz.hemis.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.enums.RoleCode;
import uz.hemis.common.enums.RoleType;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.security.Permission;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.repository.PermissionRepository;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.common.port.cache.CacheEvictionPort;
import uz.hemis.service.admin.dto.*;

import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleAdminService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CacheEvictionPort cacheEvictionPort;

    /**
     * In-memory pagination is acceptable because total role count is expected to be &lt;100.
     * If role count grows significantly, migrate to database-level pagination with specification queries.
     */
    @Transactional(readOnly = true)
    public Page<RoleResponse> getAll(String search, Pageable pageable) {
        List<Role> allRoles = new ArrayList<>(roleRepository.findAllActiveWithPermissions());

        // Build user count map (single query instead of N+1)
        Map<UUID, Integer> userCountMap = new HashMap<>();
        for (Object[] row : roleRepository.countUsersPerRole()) {
            userCountMap.put((UUID) row[0], ((Number) row[1]).intValue());
        }

        // Filter by search
        if (search != null && !search.isBlank()) {
            String lowerSearch = search.toLowerCase().trim();
            allRoles = allRoles.stream()
                    .filter(r -> r.getCode().toLowerCase().contains(lowerSearch)
                            || r.getName().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Sort
        if (pageable.getSort().isSorted()) {
            for (var order : pageable.getSort()) {
                Comparator<Role> cmp = switch (order.getProperty()) {
                    case "code" -> Comparator.comparing(Role::getCode, String.CASE_INSENSITIVE_ORDER);
                    case "roleType" -> Comparator.comparing(r -> r.getRoleType().name());
                    case "createdAt" -> Comparator.comparing(Role::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
                    default -> Comparator.comparing(Role::getName, String.CASE_INSENSITIVE_ORDER);
                };
                allRoles.sort(order.isDescending() ? cmp.reversed() : cmp);
            }
        } else {
            allRoles.sort(Comparator.comparing(Role::getName, String.CASE_INSENSITIVE_ORDER));
        }

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allRoles.size());
        List<Role> pageContent = start < allRoles.size()
                ? allRoles.subList(start, end)
                : Collections.emptyList();

        List<RoleResponse> responses = pageContent.stream()
                .map(role -> toResponse(role, userCountMap.getOrDefault(role.getId(), 0)))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, allRoles.size());
    }

    @Transactional(readOnly = true)
    public RoleResponse getById(UUID id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        int usersCount = roleRepository.countUsersByRoleId(id);
        return toResponse(role, usersCount);
    }

    @Transactional
    @Audited(action = AuditAction.CREATE, entity = "Role", entityClass = Role.class)
    public RoleResponse create(RoleCreateRequest request) {
        // Validate unique code
        if (roleRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Role code already exists: " + request.getCode());
        }

        // Cannot create SYSTEM type roles
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setRoleType(RoleType.CUSTOM);
        role.setActive(true);

        // Assign permissions
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            for (UUID permissionId : request.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));
                role.addPermission(permission);
            }
        }

        try {
            Role saved = roleRepository.save(role);
            log.info("Role created: code={}, name={}", saved.getCode(), saved.getName());

            // Evict permission caches if role has permissions
            if (!role.getPermissions().isEmpty()) {
                cacheEvictionPort.evictAllPermissions();
            }

            return toResponse(saved, 0);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Role code already exists: " + request.getCode());
        }
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "Role", entityClass = Role.class, keyArg = "id")
    public RoleResponse update(UUID id, RoleUpdateRequest request) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Every role is editable by whoever holds roles.manage — SYSTEM ones included: an
        // administrator who cannot adjust ADMIN or VIEWER has to reach for a migration to change
        // who sees what. The one exception is the break-glass role itself: the access model (seed
        // S038) rests on SUPER_ADMIN holding every permission, and editing it from inside the app
        // is how a platform locks itself out of its own recovery path. It stays readable.
        if (RoleCode.SUPER_ADMIN.getCode().equals(role.getCode())) {
            throw new BadRequestException("Cannot modify the SUPER_ADMIN role — it is the recovery path for every other role");
        }

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new BadRequestException("Role name cannot be empty");
            }
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        // Update permissions
        if (request.getPermissionIds() != null) {
            // Clear existing permissions
            new HashSet<>(role.getPermissions()).forEach(role::removePermission);

            // Assign new permissions
            for (UUID permissionId : request.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));
                role.addPermission(permission);
            }

            // Evict all permission caches since role permissions changed
            cacheEvictionPort.evictAllPermissions();
        }

        Role saved = roleRepository.save(role);
        log.info("Role updated: id={}, code={}", id, saved.getCode());

        int usersCount = roleRepository.countUsersByRoleId(id);
        return toResponse(saved, usersCount);
    }

    @Transactional
    @Audited(action = AuditAction.DELETE, entity = "Role", entityClass = Role.class, keyArg = "id")
    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Cannot delete SYSTEM roles
        if (role.isSystemRole()) {
            throw new BadRequestException("Cannot delete system role: " + role.getCode());
        }

        // Cannot delete role assigned to users (count query — no N+1)
        int usersCount = roleRepository.countUsersByRoleId(id);
        if (usersCount > 0) {
            throw new BadRequestException("Cannot delete role assigned to " + usersCount + " users. Remove users first.");
        }

        // Soft delete
        role.setDeletedAt(LocalDateTime.now());
        role.setActive(false);
        roleRepository.save(role);

        log.info("Role soft-deleted: id={}, code={}", id, role.getCode());
    }

    @Transactional(readOnly = true)
    public List<RoleResponse.PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAllActive();
        return permissions.stream()
                .map(this::toPermissionResponse)
                .sorted(Comparator.comparing(RoleResponse.PermissionResponse::getCategory, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(RoleResponse.PermissionResponse::getCode))
                .collect(Collectors.toList());
    }

    // ── Helpers ──

    private RoleResponse toResponse(Role role, int usersCount) {
        List<RoleResponse.PermissionResponse> permissions = role.getPermissions() != null
                ? role.getPermissions().stream()
                .map(this::toPermissionResponse)
                .sorted(Comparator.comparing(RoleResponse.PermissionResponse::getCode))
                .collect(Collectors.toList())
                : Collections.emptyList();

        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .roleType(role.getRoleType())
                .active(role.getActive())
                .permissions(permissions)
                .usersCount(usersCount)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private RoleResponse.PermissionResponse toPermissionResponse(Permission permission) {
        return RoleResponse.PermissionResponse.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .category(permission.getCategory() != null ? permission.getCategory().name() : null)
                .resource(permission.getResource())
                .action(permission.getAction() != null ? permission.getAction().name().toLowerCase() : null)
                .build();
    }
}
