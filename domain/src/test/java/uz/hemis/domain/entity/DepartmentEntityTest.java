package uz.hemis.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.hemis.domain.entity.academic.Department;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Department Entity Unit Tests
 *
 * <p>Tests Lombok-generated getters/setters, convenience methods,
 * and inherited soft delete logic from BaseEntity.</p>
 *
 * <p>Department extends BaseEntity (UUID primary key)
 * and uses @NoArgsConstructor / @AllArgsConstructor.</p>
 */
@DisplayName("Department Entity Tests")
class DepartmentEntityTest {

    @Test
    @DisplayName("Should create instance with no-arg constructor")
    void shouldCreateInstance() {
        Department department = new Department();

        assertThat(department).isNotNull();
        assertThat(department.getId()).isNull();
        assertThat(department.getDepartmentCode()).isNull();
        assertThat(department.getDepartmentName()).isNull();
    }

    @Test
    @DisplayName("Should set and get key business fields")
    void shouldSetAndGetKeyFields() {
        Department department = new Department();
        UUID facultyId = UUID.randomUUID();

        department.setDepartmentCode("CS-001");
        department.setDepartmentName("Computer Science");
        department.setDepartmentNameEn("Computer Science");
        department.setUniversity("TATU");
        department.setFaculty(facultyId);
        department.setIsActive(true);
        department.setEmail("cs@tatu.uz");

        assertThat(department.getDepartmentCode()).isEqualTo("CS-001");
        assertThat(department.getDepartmentName()).isEqualTo("Computer Science");
        assertThat(department.getDepartmentNameEn()).isEqualTo("Computer Science");
        assertThat(department.getUniversity()).isEqualTo("TATU");
        assertThat(department.getFaculty()).isEqualTo(facultyId);
        assertThat(department.getIsActive()).isTrue();
        assertThat(department.getEmail()).isEqualTo("cs@tatu.uz");
    }

    @Test
    @DisplayName("Convenience methods getCode/getName/isActive should delegate correctly")
    void convenienceMethodsShouldDelegate() {
        Department department = new Department();
        department.setDepartmentCode("MATH-002");
        department.setDepartmentName("Mathematics");
        department.setIsActive(true);

        // getCode() delegates to getDepartmentCode()
        assertThat(department.getCode()).isEqualTo("MATH-002");
        // getName() delegates to getDepartmentName()
        assertThat(department.getName()).isEqualTo("Mathematics");
        // getActive() delegates to isActive field
        assertThat(department.getActive()).isTrue();
        // isActive() wraps Boolean
        assertThat(department.isActive()).isTrue();

        // isActive with null should return false
        department.setIsActive(null);
        assertThat(department.isActive()).isFalse();
    }

    @Test
    @DisplayName("isDeleted should return true when deleteTs is set (inherited from BaseEntity)")
    void isDeletedShouldWorkFromBaseEntity() {
        Department department = new Department();

        assertThat(department.isDeleted()).isFalse();

        department.setDeleteTs(LocalDateTime.now());
        assertThat(department.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should inherit UUID id and version from BaseEntity")
    void shouldInheritBaseEntityFields() {
        Department department = new Department();
        UUID id = UUID.randomUUID();

        department.setId(id);
        department.setVersion(3);
        department.setCreatedBy("system");

        assertThat(department.getId()).isEqualTo(id);
        assertThat(department.getVersion()).isEqualTo(3);
        assertThat(department.getCreatedBy()).isEqualTo("system");
    }
}
