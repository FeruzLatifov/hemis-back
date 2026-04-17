package uz.hemis.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuditableEntity Unit Tests
 *
 * <p>AuditableEntity is abstract (used by User, Menu, and other business entities),
 * so we use a minimal concrete subclass ({@link TestAuditableEntity}) for testing.</p>
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>UUID id generation in {@code @PrePersist} callback</li>
 *   <li>createdAt initialisation in {@code @PrePersist} callback</li>
 *   <li>updatedAt update in {@code @PreUpdate} callback</li>
 *   <li>Soft delete via {@code softDelete()} and {@code restore()}</li>
 *   <li>isDeleted logic based on deletedAt</li>
 *   <li>Modern column naming convention (created_at, updated_at, deleted_at)</li>
 * </ul>
 */
@DisplayName("AuditableEntity Tests")
class AuditableEntityTest {

    // ------------------------------------------------------------------
    // Minimal concrete subclass used only in this test
    // ------------------------------------------------------------------
    static class TestAuditableEntity extends AuditableEntity {
        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
        }
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
            TestAuditableEntity entity = new TestAuditableEntity();

            assertThat(entity.getId()).isNull();
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
            assertThat(entity.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("New instance should not be deleted")
        void newInstanceShouldNotBeDeleted() {
            TestAuditableEntity entity = new TestAuditableEntity();

            assertThat(entity.isDeleted()).isFalse();
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
            TestAuditableEntity entity = new TestAuditableEntity();
            assertThat(entity.getId()).isNull();

            entity.onCreate();

            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getId()).isInstanceOf(UUID.class);
        }

        @Test
        @DisplayName("Should NOT overwrite existing id")
        void shouldNotOverwriteExistingId() {
            TestAuditableEntity entity = new TestAuditableEntity();
            UUID predefined = UUID.fromString("22222222-2222-2222-2222-222222222222");
            entity.setId(predefined);

            entity.onCreate();

            assertThat(entity.getId()).isEqualTo(predefined);
        }

        @Test
        @DisplayName("Should set createdAt to approximately now when null")
        void shouldSetCreatedAtWhenNull() {
            TestAuditableEntity entity = new TestAuditableEntity();
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            entity.onCreate();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(entity.getCreatedAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("Should NOT overwrite existing createdAt")
        void shouldNotOverwriteExistingCreatedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            LocalDateTime predefined = LocalDateTime.of(2023, 1, 1, 0, 0);
            entity.setCreatedAt(predefined);

            entity.onCreate();

            assertThat(entity.getCreatedAt()).isEqualTo(predefined);
        }

        @Test
        @DisplayName("Each new entity should get a unique UUID")
        void eachNewEntityShouldGetUniqueUuid() {
            TestAuditableEntity entity1 = new TestAuditableEntity();
            TestAuditableEntity entity2 = new TestAuditableEntity();

            entity1.onCreate();
            entity2.onCreate();

            assertThat(entity1.getId()).isNotEqualTo(entity2.getId());
        }
    }

    // ==================================================================
    // PreUpdate (onUpdate)
    // ==================================================================

    @Nested
    @DisplayName("onUpdate (PreUpdate)")
    class OnUpdate {

        @Test
        @DisplayName("Should set updatedAt to approximately now")
        void shouldSetUpdatedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            assertThat(entity.getUpdatedAt()).isNull();

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            entity.onUpdate();
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertThat(entity.getUpdatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(before);
            assertThat(entity.getUpdatedAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("Should overwrite previous updatedAt on subsequent calls")
        void shouldOverwritePreviousUpdatedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            entity.setUpdatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));

            entity.onUpdate();

            assertThat(entity.getUpdatedAt()).isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
        }

        @Test
        @DisplayName("onUpdate should not affect createdAt")
        void onUpdateShouldNotAffectCreatedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            LocalDateTime createdTs = LocalDateTime.of(2024, 6, 1, 10, 0);
            entity.setCreatedAt(createdTs);

            entity.onUpdate();

            assertThat(entity.getCreatedAt()).isEqualTo(createdTs);
        }
    }

    // ==================================================================
    // softDelete
    // ==================================================================

    @Nested
    @DisplayName("softDelete method")
    class SoftDeleteMethod {

        @Test
        @DisplayName("softDelete should set deletedAt to approximately now")
        void softDeleteShouldSetDeletedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            entity.softDelete();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(entity.getDeletedAt()).isNotNull();
            assertThat(entity.getDeletedAt()).isAfterOrEqualTo(before);
            assertThat(entity.getDeletedAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("softDelete should cause isDeleted to return true")
        void softDeleteShouldCauseIsDeletedTrue() {
            TestAuditableEntity entity = new TestAuditableEntity();
            assertThat(entity.isDeleted()).isFalse();

            entity.softDelete();

            assertThat(entity.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Calling softDelete twice should update deletedAt timestamp")
        void callingSoftDeleteTwiceShouldUpdateTimestamp() {
            TestAuditableEntity entity = new TestAuditableEntity();
            entity.setDeletedAt(LocalDateTime.of(2020, 1, 1, 0, 0));

            entity.softDelete();

            assertThat(entity.getDeletedAt()).isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
        }
    }

    // ==================================================================
    // restore
    // ==================================================================

    @Nested
    @DisplayName("restore method")
    class RestoreMethod {

        @Test
        @DisplayName("restore should clear deletedAt")
        void restoreShouldClearDeletedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            entity.softDelete();
            assertThat(entity.getDeletedAt()).isNotNull();

            entity.restore();

            assertThat(entity.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("restore should cause isDeleted to return false")
        void restoreShouldCauseIsDeletedFalse() {
            TestAuditableEntity entity = new TestAuditableEntity();
            entity.softDelete();
            assertThat(entity.isDeleted()).isTrue();

            entity.restore();

            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Calling restore on non-deleted entity should be safe (no-op)")
        void callingRestoreOnNonDeletedEntityShouldBeNoOp() {
            TestAuditableEntity entity = new TestAuditableEntity();
            assertThat(entity.getDeletedAt()).isNull();

            entity.restore();

            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.isDeleted()).isFalse();
        }
    }

    // ==================================================================
    // isDeleted logic
    // ==================================================================

    @Nested
    @DisplayName("isDeleted")
    class IsDeleted {

        @Test
        @DisplayName("Should return false when deletedAt is null")
        void shouldReturnFalseWhenDeletedAtIsNull() {
            TestAuditableEntity entity = new TestAuditableEntity();

            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should return true when deletedAt is set to any timestamp")
        void shouldReturnTrueWhenDeletedAtIsSet() {
            TestAuditableEntity entity = new TestAuditableEntity();
            entity.setDeletedAt(LocalDateTime.of(2000, 1, 1, 0, 0));

            assertThat(entity.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Full lifecycle: create -> delete -> restore -> delete")
        void fullLifecycle() {
            TestAuditableEntity entity = new TestAuditableEntity();
            entity.onCreate();

            assertThat(entity.isDeleted()).isFalse();

            entity.softDelete();
            assertThat(entity.isDeleted()).isTrue();

            entity.restore();
            assertThat(entity.isDeleted()).isFalse();

            entity.softDelete();
            assertThat(entity.isDeleted()).isTrue();
        }
    }

    // ==================================================================
    // Field accessors (manual set/get)
    // ==================================================================

    @Nested
    @DisplayName("field accessors")
    class FieldAccessors {

        @Test
        @DisplayName("Should set and get id manually")
        void shouldSetAndGetId() {
            TestAuditableEntity entity = new TestAuditableEntity();
            UUID id = UUID.randomUUID();
            entity.setId(id);

            assertThat(entity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get createdAt manually")
        void shouldSetAndGetCreatedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            LocalDateTime ts = LocalDateTime.of(2024, 5, 10, 8, 30);
            entity.setCreatedAt(ts);

            assertThat(entity.getCreatedAt()).isEqualTo(ts);
        }

        @Test
        @DisplayName("Should set and get updatedAt manually")
        void shouldSetAndGetUpdatedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            LocalDateTime ts = LocalDateTime.of(2024, 9, 1, 16, 45);
            entity.setUpdatedAt(ts);

            assertThat(entity.getUpdatedAt()).isEqualTo(ts);
        }

        @Test
        @DisplayName("Should set and get deletedAt manually")
        void shouldSetAndGetDeletedAt() {
            TestAuditableEntity entity = new TestAuditableEntity();
            LocalDateTime ts = LocalDateTime.of(2025, 2, 14, 23, 59);
            entity.setDeletedAt(ts);

            assertThat(entity.getDeletedAt()).isEqualTo(ts);
        }
    }
}
