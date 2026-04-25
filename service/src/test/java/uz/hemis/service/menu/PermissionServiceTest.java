package uz.hemis.service.menu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.security.Permission;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PermissionService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getUserPermissions - returns sorted permission codes, handles missing user</li>
 *   <li>hasPermission - exact match, wildcard (*), resource wildcard (resource.*), no match</li>
 *   <li>canAccessPath - path normalization, path-to-permission conversion</li>
 *   <li>canAccessPath(String username, String path) - username resolution</li>
 * </ul>
 *
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PermissionService permissionService;

    private UUID userId;
    private User user;
    private Role adminRole;
    private Role viewerRole;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Build permissions
        Permission studentsView = Permission.builder()
                .resource("students")
                .action(uz.hemis.domain.entity.enums.PermissionAction.VIEW)
                .code("students.view")
                .name("View Students")
                .category(uz.hemis.domain.entity.enums.PermissionCategory.CORE)
                .build();

        Permission studentsCreate = Permission.builder()
                .resource("students")
                .action(uz.hemis.domain.entity.enums.PermissionAction.CREATE)
                .code("students.create")
                .name("Create Students")
                .category(uz.hemis.domain.entity.enums.PermissionCategory.CORE)
                .build();

        Permission reportsView = Permission.builder()
                .resource("reports")
                .action(uz.hemis.domain.entity.enums.PermissionAction.VIEW)
                .code("reports.view")
                .name("View Reports")
                .category(uz.hemis.domain.entity.enums.PermissionCategory.REPORTS)
                .build();

        Permission reportsExport = Permission.builder()
                .resource("reports")
                .action(uz.hemis.domain.entity.enums.PermissionAction.EXPORT)
                .code("reports.export")
                .name("Export Reports")
                .category(uz.hemis.domain.entity.enums.PermissionCategory.REPORTS)
                .build();

        // Build roles
        adminRole = Role.builder()
                .code("OTM_API")
                .name("University Administrator")
                .active(true)
                .permissions(new HashSet<>(Set.of(studentsView, studentsCreate, reportsView, reportsExport)))
                .build();

        viewerRole = Role.builder()
                .code("VIEWER")
                .name("Viewer")
                .active(true)
                .permissions(new HashSet<>(Set.of(studentsView, reportsView)))
                .build();

        // Build user
        user = new User();
        user.setId(userId);
        user.setUsername("test_admin");
        user.setPassword("$2a$10$hashedpassword");
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(adminRole)));
    }

    // =====================================================
    // getUserPermissions tests
    // =====================================================

    @Nested
    @DisplayName("getUserPermissions")
    class GetUserPermissions {

        @Test
        @DisplayName("returns sorted permission codes from all roles")
        void returnsSortedPermissionCodes() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            List<String> permissions = permissionService.getUserPermissions(userId);

            // Then
            assertThat(permissions).isNotEmpty();
            assertThat(permissions).contains("students.view", "students.create", "reports.view", "reports.export");
            // Verify sorted order
            assertThat(permissions).isSorted();

            verify(userRepository).findByIdWithPermissions(userId);
        }

        @Test
        @DisplayName("merges permissions from multiple roles (no duplicates)")
        void mergesPermissionsFromMultipleRoles() {
            // Given - user has both admin and viewer roles
            user.setRoles(new HashSet<>(Set.of(adminRole, viewerRole)));
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            List<String> permissions = permissionService.getUserPermissions(userId);

            // Then - students.view and reports.view appear in both roles but should be deduplicated
            assertThat(permissions).containsOnlyOnce("students.view");
            assertThat(permissions).containsOnlyOnce("reports.view");
            assertThat(permissions).contains("students.create", "reports.export");

            verify(userRepository).findByIdWithPermissions(userId);
        }

        @Test
        @DisplayName("user not found - returns empty list")
        void userNotFound_returnsEmptyList() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findByIdWithPermissions(unknownId)).thenReturn(Optional.empty());

            // When
            List<String> permissions = permissionService.getUserPermissions(unknownId);

            // Then
            assertThat(permissions).isEmpty();

            verify(userRepository).findByIdWithPermissions(unknownId);
        }

        @Test
        @DisplayName("user with no roles - returns empty list")
        void userWithNoRoles_returnsEmptyList() {
            // Given
            User noRolesUser = new User();
            noRolesUser.setId(userId);
            noRolesUser.setUsername("no_roles_user");
            noRolesUser.setPassword("$2a$10$hashed");
            noRolesUser.setEnabled(true);
            noRolesUser.setRoles(new HashSet<>());

            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(noRolesUser));

            // When
            List<String> permissions = permissionService.getUserPermissions(userId);

            // Then
            assertThat(permissions).isEmpty();
        }

        @Test
        @DisplayName("user with inactive role - permissions from inactive role are excluded")
        void userWithInactiveRole_permissionsExcluded() {
            // Given - deactivate the admin role
            Role inactiveRole = Role.builder()
                    .code("INACTIVE_ROLE")
                    .name("Inactive Role")
                    .active(false) // inactive
                    .permissions(new HashSet<>(Set.of(
                            Permission.builder()
                                    .code("secret.manage")
                                    .resource("secret")
                                    .action(uz.hemis.domain.entity.enums.PermissionAction.MANAGE)
                                    .name("Manage Secrets")
                                    .build()
                    )))
                    .build();

            user.setRoles(new HashSet<>(Set.of(viewerRole, inactiveRole)));
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            List<String> permissions = permissionService.getUserPermissions(userId);

            // Then - should NOT contain secret.manage from inactive role
            assertThat(permissions).doesNotContain("secret.manage");
            // Should contain permissions from active viewerRole
            assertThat(permissions).contains("students.view", "reports.view");
        }
    }

    // =====================================================
    // hasPermission tests
    // =====================================================

    @Nested
    @DisplayName("hasPermission")
    class HasPermission {

        @Test
        @DisplayName("exact match - returns true")
        void exactMatch_returnsTrue() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean result = permissionService.hasPermission(userId, "students.view");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("no match - returns false")
        void noMatch_returnsFalse() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean result = permissionService.hasPermission(userId, "users.manage");

            // Then
            assertThat(result).isFalse();
        }

        // NOTE: Legacy wildcard tests (superAdminWildcard, resourceWildcard) removed after
        // Permission.action migration to enum in Batch 4 — wildcards are no longer storable.
        // Super admin capability is now expressed via SUPER_ADMIN role (see Role.code).

        @Test
        @DisplayName("null or empty permission required - returns true (no permission needed)")
        void nullOrEmptyPermission_returnsTrue() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean nullResult = permissionService.hasPermission(userId, null);
            boolean emptyResult = permissionService.hasPermission(userId, "");

            // Then
            assertThat(nullResult).isTrue();
            assertThat(emptyResult).isTrue();
        }

        @Test
        @DisplayName("user not found - returns false for any permission")
        void userNotFound_returnsFalse() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findByIdWithPermissions(unknownId)).thenReturn(Optional.empty());

            // When
            boolean result = permissionService.hasPermission(unknownId, "students.view");

            // Then
            assertThat(result).isFalse();
        }

    }

    // =====================================================
    // canAccessPath(UUID, String) tests
    // =====================================================

    @Nested
    @DisplayName("canAccessPath(UUID, String)")
    class CanAccessPathById {

        @Test
        @DisplayName("simple path /students - converts to students.view")
        void simplePath_convertsToStudentsView() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean result = permissionService.canAccessPath(userId, "/students");

            // Then
            assertThat(result).isTrue(); // user has students.view
        }

        @Test
        @DisplayName("path with action /students/create - converts to students.create")
        void pathWithAction_convertsToStudentsCreate() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean result = permissionService.canAccessPath(userId, "/students/create");

            // Then
            assertThat(result).isTrue(); // user has students.create
        }

        @Test
        @DisplayName("path normalization - leading and trailing slashes removed")
        void pathNormalization_slashesRemoved() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean withLeadingSlash = permissionService.canAccessPath(userId, "/students");
            boolean withTrailingSlash = permissionService.canAccessPath(userId, "students/");
            boolean withBothSlashes = permissionService.canAccessPath(userId, "/students/");
            boolean withMultipleSlashes = permissionService.canAccessPath(userId, "///students///");

            // Then - all should resolve to "students.view"
            assertThat(withLeadingSlash).isTrue();
            assertThat(withTrailingSlash).isTrue();
            assertThat(withBothSlashes).isTrue();
            assertThat(withMultipleSlashes).isTrue();
        }

        @Test
        @DisplayName("unauthorized path - returns false")
        void unauthorizedPath_returnsFalse() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean result = permissionService.canAccessPath(userId, "/users/manage");

            // Then
            assertThat(result).isFalse(); // user does NOT have users.manage
        }

        @Test
        @DisplayName("path without explicit action defaults to view")
        void pathWithoutAction_defaultsToView() {
            // Given
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean reportsAccess = permissionService.canAccessPath(userId, "/reports");

            // Then - should check reports.view (default action)
            assertThat(reportsAccess).isTrue(); // user has reports.view
        }
    }

    // =====================================================
    // canAccessPath(String username, String path) tests
    // =====================================================

    @Nested
    @DisplayName("canAccessPath(String, String)")
    class CanAccessPathByUsername {

        @Test
        @DisplayName("valid username - resolves user and checks permission")
        void validUsername_resolvesAndChecks() {
            // Given
            when(userRepository.findByUsername("test_admin")).thenReturn(Optional.of(user));
            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(user));

            // When
            boolean result = permissionService.canAccessPath("test_admin", "/students");

            // Then
            assertThat(result).isTrue();

            verify(userRepository).findByUsername("test_admin");
            verify(userRepository).findByIdWithPermissions(userId);
        }

        @Test
        @DisplayName("unknown username - throws IllegalArgumentException")
        void unknownUsername_throwsIllegalArgumentException() {
            // Given
            when(userRepository.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> permissionService.canAccessPath("nonexistent_user", "/students"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found")
                    .hasMessageContaining("nonexistent_user");

            verify(userRepository).findByUsername("nonexistent_user");
            verify(userRepository, never()).findByIdWithPermissions(any());
        }
    }
}
