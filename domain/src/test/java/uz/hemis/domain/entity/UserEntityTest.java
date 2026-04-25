package uz.hemis.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.hemis.domain.entity.security.Role;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.entity.university.University;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User Entity Unit Tests
 *
 * <p>Tests Lombok-generated getters/setters, business methods,
 * and inherited soft delete logic from AuditableEntity.</p>
 *
 * <p>User extends AuditableEntity (UUID primary key, modern column names:
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
        assertThat(user.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("Should set and get key authentication and profile fields")
    void shouldSetAndGetKeyFields() {
        User user = new User();

        user.setUsername("tatu_admin");
        user.setPassword("$2a$10$hashedPasswordHere");
        user.setEnabled(true);
        University university = new University();
        university.setCode("TATU");
        user.setUniversity(university);
        user.setFullName("Karimov Abdulla Rashidovich");
        user.setEmail("admin@tatu.uz");
        user.setPhone("+998901234567");

        assertThat(user.getUsername()).isEqualTo("tatu_admin");
        assertThat(user.getPassword()).isEqualTo("$2a$10$hashedPasswordHere");
        assertThat(user.getEnabled()).isTrue();
        assertThat(user.getUniversityCode()).isEqualTo("TATU");
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
    @DisplayName("isDeleted should use deletedAt from AuditableEntity")
    void isDeletedShouldUseAuditableEntity() {
        User user = new User();

        assertThat(user.isDeleted()).isFalse();

        user.setDeletedAt(LocalDateTime.now());
        assertThat(user.isDeleted()).isTrue();

        // softDelete and restore from AuditableEntity
        user.restore();
        assertThat(user.isDeleted()).isFalse();

        user.softDelete();
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("hasRole should check roles (many-to-many)")
    void hasRoleShouldCheckRoles() {
        User user = new User();

        // empty roles -> false
        assertThat(user.hasRole("ADMIN")).isFalse();
        assertThat(user.isSystemAdmin()).isFalse();

        Role superAdmin = Role.builder().code("SUPER_ADMIN").name("Super Admin").build();
        user.getRoles().add(superAdmin);

        assertThat(user.hasRole("SUPER_ADMIN")).isTrue();
        assertThat(user.hasRole("ROLE_SUPER_ADMIN")).isTrue(); // ROLE_ prefix is stripped
        assertThat(user.isSystemAdmin()).isTrue();
        assertThat(user.isUniversityAdmin()).isFalse();

        user.getRoles().clear();
        user.getRoles().add(Role.builder().code("UNIVERSITY_ADMIN").name("University Admin").build());
        assertThat(user.isUniversityAdmin()).isTrue();
        assertThat(user.isSystemAdmin()).isFalse();
    }
}
