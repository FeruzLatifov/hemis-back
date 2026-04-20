package uz.hemis.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uz.hemis.domain.entity.university.University;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * University Entity Unit Tests
 *
 * <p>Tests Lombok-generated getters/setters, soft delete logic,
 * and business methods on the University entity.</p>
 *
 * <p>University uses a VARCHAR primary key (code), not UUID.
 * It does NOT extend BaseEntity.</p>
 */
@DisplayName("University Entity Tests")
class UniversityEntityTest {

    @Test
    @DisplayName("Should create instance with default constructor")
    void shouldCreateInstance() {
        University university = new University();

        assertThat(university).isNotNull();
        assertThat(university.getCode()).isNull();
        assertThat(university.getName()).isNull();
        assertThat(university.getActive()).isNull();
    }

    @Test
    @DisplayName("Should set and get key business fields")
    void shouldSetAndGetKeyFields() {
        University university = new University();

        university.setCode("TATU");
        university.setName("Tashkent University of Information Technologies");
        university.setTin("123456789");
        university.setActive(true);
        university.setUniversityType("11");
        university.setAddress("Amir Temur ko'chasi, Tashkent");

        assertThat(university.getCode()).isEqualTo("TATU");
        assertThat(university.getName()).isEqualTo("Tashkent University of Information Technologies");
        assertThat(university.getTin()).isEqualTo("123456789");
        assertThat(university.getActive()).isTrue();
        assertThat(university.getUniversityType()).isEqualTo("11");
        assertThat(university.getAddress()).isEqualTo("Amir Temur ko'chasi, Tashkent");
    }

    @Test
    @DisplayName("isDeleted should return true when deleteTs is set")
    void isDeletedShouldReturnTrueWhenDeleteTsSet() {
        University university = new University();

        assertThat(university.isDeleted()).isFalse();

        university.setDeleteTs(LocalDateTime.now());
        assertThat(university.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("isActive should check both active flag and soft delete")
    void isActiveShouldCheckBothFlagAndSoftDelete() {
        University university = new University();

        // active=null, deleteTs=null -> not active (Boolean.TRUE.equals(null) is false)
        assertThat(university.isActive()).isFalse();

        // active=true, deleteTs=null -> active
        university.setActive(true);
        assertThat(university.isActive()).isTrue();

        // active=true, deleteTs=set -> not active (soft deleted)
        university.setDeleteTs(LocalDateTime.now());
        assertThat(university.isActive()).isFalse();

        // active=false, deleteTs=null -> not active
        university.setDeleteTs(null);
        university.setActive(false);
        assertThat(university.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should set and get soft delete audit fields")
    void shouldSetAndGetSoftDeleteFields() {
        University university = new University();
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 30, 0);

        university.setDeleteTs(now);
        university.setDeletedBy("admin");

        assertThat(university.getDeleteTs()).isEqualTo(now);
        assertThat(university.getDeletedBy()).isEqualTo("admin");
    }
}
