package uz.hemis.service.menu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.Permission;
import uz.hemis.domain.entity.Role;
import uz.hemis.domain.entity.User;
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
                .id(UUID.randomUUID())
                .resource("students")
                .action("view")
                .code("students.view")
                .name("View Students")
                .category("CORE")
                .build();

        Permission studentsCreate = Permission.builder()
                .id(UUID.randomUUID())
                .resource("students")
                .action("create")
                .code("students.create")
                .name("Create Students")
                .category("CORE")
                .build();

        Permission reportsView = Permission.builder()
                .id(UUID.randomUUID())
                .resource("reports")
                .action("view")
                .code("reports.view")
                .name("View Reports")
                .category("REPORTS")
                .build();

        Permission reportsExport = Permission.builder()
                .id(UUID.randomUUID())
                .resource("reports")
                .action("export")
                .code("reports.export")
                .name("Export Reports")
                .category("REPORTS")
                .build();

        // Build roles
        adminRole = Role.builder()
                .id(UUID.randomUUID())
                .code("OTM_API")
                .name("University Administrator")
                .active(true)
                .permissions(new HashSet<>(Set.of(studentsView, studentsCreate, reportsView, reportsExport)))
                .build();

        viewerRole = Role.builder()
                .id(UUID.randomUUID())
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
        user.setRoleSet(new HashSet<>(Set.of(adminRole)));
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
            user.setRoleSet(new HashSet<>(Set.of(adminRole, viewerRole)));
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
            noRolesUser.setRoleSet(new HashSet<>());

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
                    .id(UUID.randomUUID())
                    .code("INACTIVE_ROLE")
                    .name("Inactive Role")
                    .active(false) // inactive
                    .permissions(new HashSet<>(Set.of(
                            Permission.builder()
                                    .id(UUID.randomUUID())
                                    .code("secret.manage")
                                    .resource("secret")
                                    .action("manage")
                                    .name("Manage Secrets")
                                    .build()
                    )))
                    .build();

            user.setRoleSet(new HashSet<>(Set.of(viewerRole, inactiveRole)));
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

        @Test
        @DisplayName("super admin wildcard (*) - grants all permissions")
        void superAdminWildcard_grantsAllPermissions() {
            // Given - create user with wildcard permission
            Permission wildcardPermission = Permission.builder()
                    .id(UUID.randomUUID())
                    .resource("*")
                    .action("*")
                    .code("*")
                    .name("Super Admin")
                    .build();

            Role superAdminRole = Role.builder()
                    .id(UUID.randomUUID())
                    .code("SUPER_ADMIN")
                    .name("Super Admin")
                    .active(true)
                    .permissions(new HashSet<>(Set.of(wildcardPermission)))
                    .build();

            User superAdmin = new User();
            superAdmin.setId(userId);
            superAdmin.setUsername("super_admin");
            superAdmin.setPassword("$2a$10$hashed");
            superAdmin.setEnabled(true);
            superAdmin.setRoleSet(new HashSet<>(Set.of(superAdminRole)));

            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(superAdmin));

            // When
            boolean canViewStudents = permissionService.hasPermission(userId, "students.view");
            boolean canManageUsers = permissionService.hasPermission(userId, "users.manage");
            boolean canDeleteAnything = permissionService.hasPermission(userId, "anything.delete");

            // Then
            assertThat(canViewStudents).isTrue();
            assertThat(canManageUsers).isTrue();
            assertThat(canDeleteAnything).isTrue();
        }

        @Test
        @DisplayName("resource wildcard (students.*) - grants all actions on resource")
        void resourceWildcard_grantsAllActionsOnResource() {
            // Given - create user with students.* permission
            Permission studentsWildcard = Permission.builder()
                    .id(UUID.randomUUID())
                    .resource("students")
                    .action("*")
                    .code("students.*")
                    .name("All Student Permissions")
                    .build();

            Role roleWithWildcard = Role.builder()
                    .id(UUID.randomUUID())
                    .code("STUDENT_MANAGER")
                    .name("Student Manager")
                    .active(true)
                    .permissions(new HashSet<>(Set.of(studentsWildcard)))
                    .build();

            User wildcardUser = new User();
            wildcardUser.setId(userId);
            wildcardUser.setUsername("student_manager");
            wildcardUser.setPassword("$2a$10$hashed");
            wildcardUser.setEnabled(true);
            wildcardUser.setRoleSet(new HashSet<>(Set.of(roleWithWildcard)));

            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(wildcardUser));

            // When
            boolean canView = permissionService.hasPermission(userId, "students.view");
            boolean canCreate = permissionService.hasPermission(userId, "students.create");
            boolean canDelete = permissionService.hasPermission(userId, "students.delete");
            boolean canViewReports = permissionService.hasPermission(userId, "reports.view");

            // Then
            assertThat(canView).isTrue();
            assertThat(canCreate).isTrue();
            assertThat(canDelete).isTrue();
            assertThat(canViewReports).isFalse(); // different resource
        }

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

        @Test
        @DisplayName("wildcard does not partially match resource prefix")
        void wildcardDoesNotPartiallyMatchPrefix() {
            // Given - user has "student.*" (note: singular)
            Permission studentWildcard = Permission.builder()
                    .id(UUID.randomUUID())
                    .code("student.*")
                    .resource("student")
                    .action("*")
                    .name("Student wildcard")
                    .build();

            Role roleWithPartial = Role.builder()
                    .id(UUID.randomUUID())
                    .code("PARTIAL")
                    .name("Partial")
                    .active(true)
                    .permissions(new HashSet<>(Set.of(studentWildcard)))
                    .build();

            User partialUser = new User();
            partialUser.setId(userId);
            partialUser.setUsername("partial_user");
            partialUser.setPassword("$2a$10$hashed");
            partialUser.setEnabled(true);
            partialUser.setRoleSet(new HashSet<>(Set.of(roleWithPartial)));

            when(userRepository.findByIdWithPermissions(userId)).thenReturn(Optional.of(partialUser));

            // When - "student.*" should NOT match "students.view" (different resource)
            boolean matchesStudents = permissionService.hasPermission(userId, "students.view");
            boolean matchesStudent = permissionService.hasPermission(userId, "student.view");

            // Then
            assertThat(matchesStudents).isFalse(); // "student.*" != "students.*"
            assertThat(matchesStudent).isTrue();   // "student.*" matches "student.view"
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
