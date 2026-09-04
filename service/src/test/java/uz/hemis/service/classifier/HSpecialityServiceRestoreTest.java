package uz.hemis.service.classifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.exception.BusinessRuleException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.classifier.HSpeciality;
import uz.hemis.domain.entity.classifier.ReviewStatus;
import uz.hemis.domain.repository.HEducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.domain.repository.HSpecialityYearRepository;
import uz.hemis.domain.repository.UniversitySpecialityAttachmentRepository;
import uz.hemis.service.classifier.dto.SpecialityNodeDto;
import uz.hemis.service.outbox.OutboxEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link HSpecialityService#restore(UUID)} unit tests — the other half of the soft delete (M013).
 *
 * <p>Restore is the only read in the service that must SEE a deleted row, so it loads through the
 * native {@code findByIdIncludingDeleted} escape hatch ({@code @SQLRestriction} makes the ordinary
 * {@code findById} blind to exactly these rows). Two shapes of refusal are pinned here:</p>
 * <ul>
 *   <li><b>404</b> — no deleted speciality under this id. Two distinct causes collapse to the same
 *       answer on purpose: the id is unknown, or the row is alive (nothing to undelete). A live row
 *       must never be "restored" into a silent no-op that reports success.</li>
 *   <li><b>422</b> — the row exists and is deleted, but bringing it back would break an invariant:
 *       a live row has taken its identity slot ({@code uq_h_speciality_identity_live} is PARTIAL, so
 *       the DB allowed that), or its parent is itself deleted (the orphan would surface as a
 *       top-level root in {@code buildTree}).</li>
 * </ul>
 *
 * <p>Both 422 paths must be refused BEFORE {@code restore()} clears the stamp — a half-applied
 * restore that then dies on a raw 23505 would leave the row live and duplicated. Every guard test
 * therefore asserts the row is still deleted afterwards.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HSpecialityService.restore() — soft-delete undo")
class HSpecialityServiceRestoreTest {

    private static final UUID ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PARENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final String EDU_TYPE = "11";
    private static final String CODE = "60110100";
    private static final String NAME = "Matematika";
    /** What the DB's {@code h_speciality_fold(name_uz)} generated column holds for {@link #NAME}. */
    private static final String NAME_SEARCH = "matematika";

    @Mock private HSpecialityRepository repository;
    @Mock private HSpecialityYearRepository yearRepository;
    @Mock private HEducationTypeRepository educationTypeRepository;
    @Mock private UniversitySpecialityAttachmentRepository attachmentRepository;
    @Mock private OutboxEventPublisher outboxPublisher;

    @InjectMocks
    private HSpecialityService service;

    private HSpeciality speciality;

    @BeforeEach
    void setUp() {
        speciality = deletedRow();
    }

    @Test
    @DisplayName("deleted row, slot free, live parent — the stamp is cleared and the node returned")
    void restore_shouldClearStamp_whenNothingBlocks() {
        when(repository.findByIdIncludingDeleted(ID)).thenReturn(Optional.of(speciality));
        when(repository.countLiveIdentity(EDU_TYPE, CODE, NAME_SEARCH)).thenReturn(0L);
        // restore() ends with getById(id) — the row is live again by then, so findById sees it.
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));
        when(repository.findAllChildren(ID)).thenReturn(List.of());
        when(yearRepository.findBySpecialityIds(List.of(ID))).thenReturn(List.of());
        when(educationTypeRepository.findAll()).thenReturn(List.of());

        SpecialityNodeDto restored = service.restore(ID);

        assertThat(speciality.getDeletedAt()).as("deleted_at must be cleared").isNull();
        assertThat(speciality.getDeletedBy()).as("deleted_by must be cleared too").isNull();
        assertThat(speciality.isDeleted()).isFalse();
        assertThat(restored.id()).isEqualTo(ID.toString());
        assertThat(restored.code()).isEqualTo(CODE);
        // A NEEDS_REVIEW row is not distributable and was not distributable before — no PUSH.
        verifyNoInteractions(outboxPublisher);
        // Restore never touches the years: they were never removed.
        verify(yearRepository, never()).deleteBySpecialityId(ID);
    }

    @Test
    @DisplayName("unknown id — 404, nothing else is queried")
    void restore_shouldThrowNotFound_whenIdIsUnknown() {
        when(repository.findByIdIncludingDeleted(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restore(ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("HSpeciality")
                .hasMessageContaining("id");

        verifyNoInteractions(yearRepository, educationTypeRepository, attachmentRepository, outboxPublisher);
    }

    @Test
    @DisplayName("row is NOT deleted — 404 (a live row has nothing to undelete)")
    void restore_shouldThrowNotFound_whenRowIsAlive() {
        speciality.restore(); // alive: no deleted_at
        when(repository.findByIdIncludingDeleted(ID)).thenReturn(Optional.of(speciality));

        assertThatThrownBy(() -> service.restore(ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("HSpeciality (deleted)");

        // The identity guard is never reached — there is no restore to guard.
        verify(repository, never())
                .countLiveIdentity(EDU_TYPE, CODE, NAME_SEARCH);
        verifyNoInteractions(outboxPublisher);
    }

    @Test
    @DisplayName("a live row took the identity slot — 422 SPECIALITY_RESTORE_IDENTITY_TAKEN, stamp kept")
    void restore_shouldThrowBusinessRule_whenIdentityIsTaken() {
        when(repository.findByIdIncludingDeleted(ID)).thenReturn(Optional.of(speciality));
        when(repository.countLiveIdentity(EDU_TYPE, CODE, NAME_SEARCH)).thenReturn(1L);

        assertThatThrownBy(() -> service.restore(ID))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_RESTORE_IDENTITY_TAKEN"))
                // The colliding identity is named, so the admin can find the row to rename.
                .hasMessageContaining(CODE)
                .hasMessageContaining(NAME);

        assertThat(speciality.isDeleted()).as("a refused restore must leave the row deleted").isTrue();
        verifyNoInteractions(outboxPublisher);
    }

    @Test
    @DisplayName("parent is itself deleted — 422 SPECIALITY_RESTORE_PARENT_DELETED, stamp kept")
    void restore_shouldThrowBusinessRule_whenParentIsDeleted() {
        HSpeciality parent = new HSpeciality();
        parent.setId(PARENT_ID);
        speciality.setParent(parent);

        when(repository.findByIdIncludingDeleted(ID)).thenReturn(Optional.of(speciality));
        when(repository.countLiveIdentity(EDU_TYPE, CODE, NAME_SEARCH)).thenReturn(0L);
        // findById is @SQLRestriction-filtered: empty == the parent is soft-deleted (or gone).
        when(repository.findById(PARENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restore(ID))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_RESTORE_PARENT_DELETED"));

        assertThat(speciality.isDeleted()).as("a refused restore must leave the row deleted").isTrue();
        verifyNoInteractions(outboxPublisher);
    }

    /** A NEEDS_REVIEW row already stamped by {@code delete()} — the input every test starts from. */
    private static HSpeciality deletedRow() {
        HSpeciality s = new HSpeciality();
        s.setId(ID);
        s.setCode(CODE);
        s.setNameUz(NAME);
        s.setNameSearch(NAME_SEARCH); // DB-generated in production; set by hand for the mock
        s.setEducationType(EDU_TYPE);
        s.setReviewStatus(ReviewStatus.NEEDS_REVIEW);
        s.setHierarchyLevel(1);
        s.setActive(true);
        s.softDelete();
        s.setDeletedBy("ministry.curator");
        return s;
    }
}
