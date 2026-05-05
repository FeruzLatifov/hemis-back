package uz.hemis.service.menu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PermissionService}.
 *
 * <p>PermissionService delegates permission loading to {@link UserPermissionLoader}
 * (separate bean — Spring AOP self-invocation trap fix). Tests mock the loader directly.</p>
 *
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionLoader permissionLoader;

    @InjectMocks
    private PermissionService permissionService;

    private UUID userId;
    private User user;
    private List<String> adminPermissions;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        adminPermissions = List.of("reports.export", "reports.view", "students.create", "students.view");

        user = new User();
        user.setId(userId);
        user.setUsername("test_admin");
        user.setPassword("$2a$10$hashedpassword");
        user.setEnabled(true);
    }

    // =====================================================
    // getUserPermissions tests
    // =====================================================

    @Nested
    @DisplayName("getUserPermissions")
    class GetUserPermissions {

        @Test
        @DisplayName("delegates to loader and returns sorted permission codes")
        void delegatesToLoader() {
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            List<String> permissions = permissionService.getUserPermissions(userId);

            assertThat(permissions).containsExactly("reports.export", "reports.view", "students.create", "students.view");
            verify(permissionLoader).load(userId);
        }

        @Test
        @DisplayName("user not found - returns empty list (loader returns empty)")
        void userNotFound_returnsEmptyList() {
            UUID unknownId = UUID.randomUUID();
            when(permissionLoader.load(unknownId)).thenReturn(List.of());

            List<String> permissions = permissionService.getUserPermissions(unknownId);

            assertThat(permissions).isEmpty();
            verify(permissionLoader).load(unknownId);
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
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            boolean result = permissionService.hasPermission(userId, "students.view");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("no match - returns false")
        void noMatch_returnsFalse() {
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            boolean result = permissionService.hasPermission(userId, "users.manage");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null or empty permission required - returns true (no permission needed)")
        void nullOrEmptyPermission_returnsTrue() {
            // Loader hech qachon chaqirilmaydi (early return) — lenient stubbing kerak emas

            boolean nullResult = permissionService.hasPermission(userId, null);
            boolean emptyResult = permissionService.hasPermission(userId, "");

            assertThat(nullResult).isTrue();
            assertThat(emptyResult).isTrue();
        }

        @Test
        @DisplayName("user not found - returns false for any permission")
        void userNotFound_returnsFalse() {
            UUID unknownId = UUID.randomUUID();
            when(permissionLoader.load(unknownId)).thenReturn(List.of());

            boolean result = permissionService.hasPermission(unknownId, "students.view");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("super admin wildcard '*' grants all permissions")
        void superAdminWildcard_grantsAll() {
            when(permissionLoader.load(userId)).thenReturn(List.of("*"));

            boolean result = permissionService.hasPermission(userId, "anything.does-not-exist");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("resource wildcard 'students.*' grants any students.X")
        void resourceWildcard_grantsResourceActions() {
            when(permissionLoader.load(userId)).thenReturn(List.of("students.*"));

            assertThat(permissionService.hasPermission(userId, "students.view")).isTrue();
            assertThat(permissionService.hasPermission(userId, "students.create")).isTrue();
            assertThat(permissionService.hasPermission(userId, "users.view")).isFalse();
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
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            boolean result = permissionService.canAccessPath(userId, "/students");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("path with action /students/create - converts to students.create")
        void pathWithAction_convertsToStudentsCreate() {
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            boolean result = permissionService.canAccessPath(userId, "/students/create");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("path normalization - leading and trailing slashes removed")
        void pathNormalization_slashesRemoved() {
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            assertThat(permissionService.canAccessPath(userId, "/students")).isTrue();
            assertThat(permissionService.canAccessPath(userId, "students/")).isTrue();
            assertThat(permissionService.canAccessPath(userId, "/students/")).isTrue();
            assertThat(permissionService.canAccessPath(userId, "///students///")).isTrue();
        }

        @Test
        @DisplayName("unauthorized path - returns false")
        void unauthorizedPath_returnsFalse() {
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            boolean result = permissionService.canAccessPath(userId, "/users/manage");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("path without explicit action defaults to view")
        void pathWithoutAction_defaultsToView() {
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            boolean reportsAccess = permissionService.canAccessPath(userId, "/reports");

            assertThat(reportsAccess).isTrue();
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
            when(userRepository.findByUsername("test_admin")).thenReturn(Optional.of(user));
            when(permissionLoader.load(userId)).thenReturn(adminPermissions);

            boolean result = permissionService.canAccessPath("test_admin", "/students");

            assertThat(result).isTrue();

            verify(userRepository).findByUsername("test_admin");
            verify(permissionLoader).load(userId);
        }

        @Test
        @DisplayName("unknown username - throws IllegalArgumentException")
        void unknownUsername_throwsIllegalArgumentException() {
            when(userRepository.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.canAccessPath("nonexistent_user", "/students"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found")
                    .hasMessageContaining("nonexistent_user");

            verify(userRepository).findByUsername("nonexistent_user");
            verify(permissionLoader, never()).load(any());
        }
    }
}
