package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BaseEntity Unit Tests
 *
 * <p>BaseEntity is abstract, so we use a minimal concrete subclass
 * ({@link TestConcreteEntity}) to exercise its fields and lifecycle hooks.</p>
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>UUID id generation in {@code @PrePersist} callback</li>
 *   <li>Version initialisation in {@code @PrePersist} callback</li>
 *   <li>Audit timestamp fields (createTs, updateTs)</li>
 *   <li>Audit user fields (createdBy, updatedBy, deletedBy)</li>
 *   <li>Soft delete logic (deleteTs / isDeleted)</li>
 *   <li>equals / hashCode contract based on id</li>
 * </ul>
 */
@DisplayName("BaseEntity Tests")
class BaseEntityTest {

    // ------------------------------------------------------------------
    // Minimal concrete subclass used only in this test
    // ------------------------------------------------------------------
    @Entity
    @Table(name = "test_entity")
    static class TestConcreteEntity extends BaseEntity {
    }

    // ==================================================================
    // Default state
    // ==================================================================

    @Nested
    @DisplayName("default state")
    class DefaultState {

        @Test
        @DisplayName("New instance should have all fields null")
        void newInstanceShouldHaveAllFieldsNull() {
            TestConcreteEntity entity = new TestConcreteEntity();

            assertThat(entity.getId()).isNull();
            assertThat(entity.getVersion()).isNull();
            assertThat(entity.getCreateTs()).isNull();
            assertThat(entity.getCreatedBy()).isNull();
            assertThat(entity.getUpdateTs()).isNull();
            assertThat(entity.getUpdatedBy()).isNull();
            assertThat(entity.getDeleteTs()).isNull();
            assertThat(entity.getDeletedBy()).isNull();
        }
    }

    // ==================================================================
    // PrePersist (onCreate)
    // ==================================================================

    @Nested
    @DisplayName("onCreate (PrePersist)")
    class OnCreate {

        @Test
        @DisplayName("Should generate UUID id when id is null")
        void shouldGenerateUuidWhenIdIsNull() {
            TestConcreteEntity entity = new TestConcreteEntity();
            assertThat(entity.getId()).isNull();

            entity.onCreate();

            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getId()).isInstanceOf(UUID.class);
        }

        @Test
        @DisplayName("Should NOT overwrite existing id")
        void shouldNotOverwriteExistingId() {
            TestConcreteEntity entity = new TestConcreteEntity();
            UUID predefined = UUID.fromString("11111111-1111-1111-1111-111111111111");
            entity.setId(predefined);

            entity.onCreate();

            assertThat(entity.getId()).isEqualTo(predefined);
        }

        @Test
        @DisplayName("Should initialise version to 1 when version is null")
        void shouldInitialiseVersionToOne() {
            TestConcreteEntity entity = new TestConcreteEntity();
            assertThat(entity.getVersion()).isNull();

            entity.onCreate();

            assertThat(entity.getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should NOT overwrite existing version")
        void shouldNotOverwriteExistingVersion() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setVersion(5);

            entity.onCreate();

            assertThat(entity.getVersion()).isEqualTo(5);
        }

        @Test
        @DisplayName("Calling onCreate twice should generate different ids only if id was null")
        void callingOnCreateTwiceShouldKeepFirstId() {
            TestConcreteEntity entity = new TestConcreteEntity();

            entity.onCreate();
            UUID firstId = entity.getId();

            entity.onCreate();
            assertThat(entity.getId()).isEqualTo(firstId);
        }
    }

    // ==================================================================
    // Soft delete
    // ==================================================================

    @Nested
    @DisplayName("soft delete (isDeleted)")
    class SoftDelete {

        @Test
        @DisplayName("isDeleted should return false when deleteTs is null")
        void isDeletedShouldReturnFalseWhenDeleteTsIsNull() {
            TestConcreteEntity entity = new TestConcreteEntity();

            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("isDeleted should return true when deleteTs is set")
        void isDeletedShouldReturnTrueWhenDeleteTsIsSet() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setDeleteTs(LocalDateTime.of(2025, 6, 1, 12, 0));

            assertThat(entity.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Setting deleteTs back to null should restore isDeleted to false")
        void clearingDeleteTsShouldRestoreIsDeleted() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setDeleteTs(LocalDateTime.now());
            assertThat(entity.isDeleted()).isTrue();

            entity.setDeleteTs(null);
            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should set and get deletedBy audit field")
        void shouldSetAndGetDeletedBy() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setDeleteTs(LocalDateTime.now());
            entity.setDeletedBy("admin");

            assertThat(entity.getDeletedBy()).isEqualTo("admin");
        }
    }

    // ==================================================================
    // Audit fields (manual setters)
    // ==================================================================

    @Nested
    @DisplayName("audit fields")
    class AuditFields {

        @Test
        @DisplayName("Should set and get createdBy")
        void shouldSetAndGetCreatedBy() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setCreatedBy("system");

            assertThat(entity.getCreatedBy()).isEqualTo("system");
        }

        @Test
        @DisplayName("Should set and get updatedBy")
        void shouldSetAndGetUpdatedBy() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setUpdatedBy("moderator");

            assertThat(entity.getUpdatedBy()).isEqualTo("moderator");
        }

        @Test
        @DisplayName("Should set and get createTs manually")
        void shouldSetAndGetCreateTs() {
            TestConcreteEntity entity = new TestConcreteEntity();
            LocalDateTime ts = LocalDateTime.of(2024, 3, 15, 9, 30);
            entity.setCreateTs(ts);

            assertThat(entity.getCreateTs()).isEqualTo(ts);
        }

        @Test
        @DisplayName("Should set and get updateTs manually")
        void shouldSetAndGetUpdateTs() {
            TestConcreteEntity entity = new TestConcreteEntity();
            LocalDateTime ts = LocalDateTime.of(2024, 7, 20, 14, 0);
            entity.setUpdateTs(ts);

            assertThat(entity.getUpdateTs()).isEqualTo(ts);
        }
    }

    // ==================================================================
    // Version field
    // ==================================================================

    @Nested
    @DisplayName("version field")
    class VersionField {

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setVersion(42);

            assertThat(entity.getVersion()).isEqualTo(42);
        }
    }

    // ==================================================================
    // equals / hashCode
    // ==================================================================

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Two entities with same id should be equal")
        void twoEntitiesWithSameIdShouldBeEqual() {
            UUID id = UUID.randomUUID();

            TestConcreteEntity a = new TestConcreteEntity();
            a.setId(id);

            TestConcreteEntity b = new TestConcreteEntity();
            b.setId(id);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("Two entities with different ids should not be equal")
        void twoEntitiesWithDifferentIdsShouldNotBeEqual() {
            TestConcreteEntity a = new TestConcreteEntity();
            a.setId(UUID.randomUUID());

            TestConcreteEntity b = new TestConcreteEntity();
            b.setId(UUID.randomUUID());

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Entity with null id should not equal entity with non-null id")
        void entityWithNullIdShouldNotEqualEntityWithId() {
            TestConcreteEntity a = new TestConcreteEntity();

            TestConcreteEntity b = new TestConcreteEntity();
            b.setId(UUID.randomUUID());

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Entity should equal itself (reflexive)")
        void entityShouldEqualItself() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setId(UUID.randomUUID());

            assertThat(entity).isEqualTo(entity);
        }

        @Test
        @DisplayName("Entity should not equal null")
        void entityShouldNotEqualNull() {
            TestConcreteEntity entity = new TestConcreteEntity();
            entity.setId(UUID.randomUUID());

            assertThat(entity).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode of entity with null id should be 0")
        void hashCodeOfNullIdShouldBeZero() {
            TestConcreteEntity entity = new TestConcreteEntity();

            assertThat(entity.hashCode()).isEqualTo(0);
        }
    }
}
