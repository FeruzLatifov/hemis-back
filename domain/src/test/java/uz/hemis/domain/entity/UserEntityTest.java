package uz.hemis.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User Entity Unit Tests
 *
 * <p>Tests Lombok-generated getters/setters, business methods,
 * and inherited soft delete logic from ModernBaseEntity.</p>
 *
 * <p>User extends ModernBaseEntity (UUID primary key, modern column names:
 * created_at, updated_at, deleted_at).</p>
 */
@DisplayName("User Entity Tests")
class UserEntityTest {

    @Test
    @DisplayName("Should create instance with default field values")
    void shouldCreateInstanceWithDefaults() {
        User user = new User();

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPassword()).isNull();
        // Default values from field initializers
        assertThat(user.getEnabled()).isTrue();
        assertThat(user.getAccountNonLocked()).isTrue();
        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getRoleSet()).isEmpty();
    }

    @Test
    @DisplayName("Should set and get key authentication and profile fields")
    void shouldSetAndGetKeyFields() {
        User user = new User();

        user.setUsername("tatu_admin");
        user.setPassword("$2a$10$hashedPasswordHere");
        user.setEnabled(true);
        user.setEntityCode("TATU");
        user.setFullName("Karimov Abdulla Rashidovich");
        user.setEmail("admin@tatu.uz");
        user.setPhone("+998901234567");

        assertThat(user.getUsername()).isEqualTo("tatu_admin");
        assertThat(user.getPassword()).isEqualTo("$2a$10$hashedPasswordHere");
        assertThat(user.getEnabled()).isTrue();
        assertThat(user.getEntityCode()).isEqualTo("TATU");
        assertThat(user.getFullName()).isEqualTo("Karimov Abdulla Rashidovich");
        assertThat(user.getEmail()).isEqualTo("admin@tatu.uz");
        assertThat(user.getPhone()).isEqualTo("+998901234567");
    }

    @Test
    @DisplayName("isAccountActive should check enabled, deleted, and locked status")
    void isAccountActiveShouldCheckAllConditions() {
        User user = new User();
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        // deletedAt is null by default

        // All conditions met -> active
        assertThat(user.isAccountActive()).isTrue();

        // Disabled -> not active
        user.setEnabled(false);
        assertThat(user.isAccountActive()).isFalse();

        // Re-enable, but soft delete -> not active
        user.setEnabled(true);
        user.setDeletedAt(LocalDateTime.now());
        assertThat(user.isAccountActive()).isFalse();

        // Restore, but lock account -> not active
        user.setDeletedAt(null);
        user.setAccountNonLocked(false);
        assertThat(user.isAccountActive()).isFalse();
    }

    @Test
    @DisplayName("isDeleted should use deletedAt from ModernBaseEntity")
    void isDeletedShouldUseModernBaseEntity() {
        User user = new User();

        assertThat(user.isDeleted()).isFalse();

        user.setDeletedAt(LocalDateTime.now());
        assertThat(user.isDeleted()).isTrue();

        // softDelete and restore from ModernBaseEntity
        user.restore();
        assertThat(user.isDeleted()).isFalse();

        user.softDelete();
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("hasRole should check transient roles string field")
    void hasRoleShouldCheckRolesString() {
        User user = new User();

        // null roles -> false
        assertThat(user.hasRole("ROLE_ADMIN")).isFalse();

        user.setRoles("ROLE_ADMIN,ROLE_USER");

        assertThat(user.hasRole("ROLE_ADMIN")).isTrue();
        assertThat(user.hasRole("ADMIN")).isTrue(); // auto-prepends ROLE_
        assertThat(user.isSystemAdmin()).isTrue();
        assertThat(user.isUniversityAdmin()).isFalse();

        user.setRoles("ROLE_UNIVERSITY_ADMIN");
        assertThat(user.isUniversityAdmin()).isTrue();
        assertThat(user.isSystemAdmin()).isFalse();
    }
}
