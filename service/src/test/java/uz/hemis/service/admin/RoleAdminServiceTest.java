package uz.hemis.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uz.hemis.common.enums.RoleType;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.port.cache.CacheEvictionPort;
import uz.hemis.domain.entity.security.Permission;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.repository.PermissionRepository;
import uz.hemis.domain.repository.RoleRepository;
import uz.hemis.service.admin.dto.RoleCreateRequest;
import uz.hemis.service.admin.dto.RoleResponse;
import uz.hemis.service.admin.dto.RoleUpdateRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleAdminService — RBAC CRUD + system role protection")
class RoleAdminServiceTest {

    @Mock private RoleRepository roleRepository;
    @Mock private PermissionRepository permissionRepository;
    @Mock private CacheEvictionPort cacheEvictionPort;

    @InjectMocks
    private RoleAdminService service;

    private UUID roleId;
    private Role customRole;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        customRole = new Role();
        customRole.setId(roleId);
        customRole.setCode("CUSTOM_ROLE");
        customRole.setName("Custom Role");
        customRole.setRoleType(RoleType.CUSTOM);
        customRole.setActive(true);

        lenient().when(roleRepository.countUsersByRoleId(any(UUID.class))).thenReturn(0);
    }

    @Test
    @DisplayName("getById — topilgan, permissions + user count bilan response")
    void getById_found() {
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(customRole));
        when(roleRepository.countUsersByRoleId(roleId)).thenReturn(5);

        RoleResponse response = service.getById(roleId);

        assertThat(response.getId()).isEqualTo(roleId);
        assertThat(response.getCode()).isEqualTo("CUSTOM_ROLE");
        assertThat(response.getUsersCount()).isEqualTo(5);
    }

    @Test
    void getById_notFound() {
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(roleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create — happy path, CUSTOM type force, cache evict")
    void create_happyPath() {
        RoleCreateRequest req = new RoleCreateRequest();
        req.setCode("NEW_ROLE");
        req.setName("New Role");

        when(roleRepository.existsByCode("NEW_ROLE")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
            Role saved = inv.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        RoleResponse response = service.create(req);

        assertThat(response.getCode()).isEqualTo("NEW_ROLE");
        verify(roleRepository).save(any(Role.class));
        // No permissions → no cache evict
        verify(cacheEvictionPort, never()).evictAllPermissions();
    }

    @Test
    @DisplayName("create — permissions bilan, cache evict chaqiriladi")
    void create_withPermissions_evictsCache() {
        UUID permId = UUID.randomUUID();
        Permission p = new Permission();
        p.setId(permId);
        p.setCode("students.view");

        RoleCreateRequest req = new RoleCreateRequest();
        req.setCode("NEW_ROLE");
        req.setName("New Role");
        req.setPermissionIds(java.util.Set.of(permId));

        when(roleRepository.existsByCode("NEW_ROLE")).thenReturn(false);
        when(permissionRepository.findById(permId)).thenReturn(Optional.of(p));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
            Role saved = inv.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        service.create(req);

        verify(cacheEvictionPort).evictAllPermissions();
    }

    @Test
    @DisplayName("create — duplicate code → BadRequestException")
    void create_duplicateCode() {
        RoleCreateRequest req = new RoleCreateRequest();
        req.setCode("EXISTING");
        req.setName("X");
        when(roleRepository.existsByCode("EXISTING")).thenReturn(true);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("create — DB unique constraint violation → BadRequestException")
    void create_dbConstraintViolation() {
        RoleCreateRequest req = new RoleCreateRequest();
        req.setCode("NEW_ROLE");
        req.setName("X");
        when(roleRepository.existsByCode("NEW_ROLE")).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenThrow(new DataIntegrityViolationException("Unique violation"));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("update — system role o'zgartirib bo'lmaydi")
    void update_systemRole_throws() {
        Role systemRole = new Role();
        systemRole.setId(roleId);
        systemRole.setCode("SUPER_ADMIN");
        systemRole.setRoleType(RoleType.SYSTEM);

        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(systemRole));

        RoleUpdateRequest req = new RoleUpdateRequest();
        req.setName("Hacked");

        assertThatThrownBy(() -> service.update(roleId, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("system role");

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("update — empty name → BadRequestException")
    void update_emptyName_throws() {
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(customRole));

        RoleUpdateRequest req = new RoleUpdateRequest();
        req.setName("   ");

        assertThatThrownBy(() -> service.update(roleId, req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("delete — system role → BadRequestException")
    void delete_systemRole_throws() {
        Role systemRole = new Role();
        systemRole.setId(roleId);
        systemRole.setCode("SUPER_ADMIN");
        systemRole.setRoleType(RoleType.SYSTEM);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(systemRole));

        assertThatThrownBy(() -> service.delete(roleId))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("delete — user'larga assigned bo'lsa, throws")
    void delete_assignedToUsers_throws() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(customRole));
        when(roleRepository.countUsersByRoleId(roleId)).thenReturn(3);

        assertThatThrownBy(() -> service.delete(roleId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("3 users");
    }

    @Test
    @DisplayName("delete — happy path, soft delete (deletedAt + active=false)")
    void delete_happyPath() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(customRole));
        when(roleRepository.countUsersByRoleId(roleId)).thenReturn(0);

        service.delete(roleId);

        assertThat(customRole.getDeletedAt()).isNotNull();
        assertThat(customRole.getActive()).isFalse();
        verify(roleRepository).save(customRole);
    }

    @Test
    @DisplayName("delete — not found")
    void delete_notFound() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(roleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getAllPermissions — kategoriya + code bo'yicha tartiblash")
    void getAllPermissions_sorted() {
        Permission p1 = new Permission();
        p1.setCode("students.view");
        Permission p2 = new Permission();
        p2.setCode("admin.full");

        when(permissionRepository.findAllActive()).thenReturn(List.of(p1, p2));

        List<RoleResponse.PermissionResponse> result = service.getAllPermissions();

        assertThat(result).hasSize(2);
        // category null bo'lganda code bo'yicha alphabetical
        assertThat(result.get(0).getCode()).isEqualTo("admin.full");
    }
}
