package uz.hemis.service.classifier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.hemis.common.exception.BusinessRuleException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.classifier.HSpeciality;
import uz.hemis.domain.entity.classifier.ReviewStatus;
import uz.hemis.domain.repository.HEducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.domain.repository.HSpecialityYearRepository;
import uz.hemis.domain.repository.UniversitySpecialityAttachmentRepository;
import uz.hemis.service.outbox.OutboxEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link HSpecialityService#delete(UUID)} unit tests — the SOFT delete (M013) and its three guards.
 *
 * <p>Delete never removes the row: 224 OTMs and the legacy student tables reference a speciality by
 * UUID, so the service only stamps {@code deleted_at}/{@code deleted_by} and lets
 * {@code @SQLRestriction("deleted_at IS NULL")} hide it from every JPQL read. Two things follow, and
 * both are asserted here rather than assumed: {@code repository.delete()} is never called, and the
 * edition years are left alone — {@code fk_h_speciality_year_spec ON DELETE CASCADE} can no longer
 * fire, which is precisely what makes {@link HSpecialityService#restore(UUID)} return an intact row.</p>
 *
 * <p>A soft delete is admissible only for a NEEDS_REVIEW curation row that nothing depends on, so
 * every guard test here pins one refusal reason: status (the row is distributed), children
 * (a parent keeps its subtree), OTM attachments (an OTM is allowed to run it).</p>
 *
 * <p>Guard order matters as much as the guards themselves — an APPROVED parent must be told
 * about its status first (the actionable fact), not about its subtree.</p>
 *
 * <p><b>Why every guard test asserts {@code getDeletedAt() == null}:</b> under a soft delete the old
 * {@code verify(repository, never()).delete(any())} became vacuously true — the happy path does not
 * call it either — so it can no longer distinguish a refusal from a successful delete. The stamp is
 * the only observable that separates them.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HSpecialityService.delete() — soft delete")
class HSpecialityServiceDeleteTest {

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** The authenticated curator — {@code deleted_by} must record who pressed the button. */
    private static final String CURATOR = "ministry.curator";

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
        speciality = row(ID, "60110100", "Matematika", ReviewStatus.NEEDS_REVIEW);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CURATOR, "n/a", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("NEEDS_REVIEW, no children, no attachments — the row is STAMPED, not removed")
    void delete_shouldStampRowAndKeepYears_whenNeedsReviewAndUnreferenced() {
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));
        when(repository.findAllChildren(ID)).thenReturn(List.of());
        when(attachmentRepository.countBySpecialityId(ID)).thenReturn(0L);

        service.delete(ID);

        assertThat(speciality.getDeletedAt()).as("deleted_at must be stamped").isNotNull();
        assertThat(speciality.isDeleted()).isTrue();
        assertThat(speciality.getDeletedBy())
                .as("deleted_by records the authenticated caller")
                .isEqualTo(CURATOR);

        // The row survives physically — restore() has something to bring back.
        verify(repository, never()).delete(any());
        // Years are KEPT: the CASCADE can never fire again, so a restored row keeps its editions.
        verify(yearRepository, never()).deleteBySpecialityId(any());
        verifyNoInteractions(yearRepository);
        // A NEEDS_REVIEW row was never distributed — no PUSH on delete.
        verifyNoInteractions(outboxPublisher);
    }

    @Test
    @DisplayName("no authenticated caller — deleted_by falls back to 'system'")
    void delete_shouldStampSystem_whenNoAuthentication() {
        SecurityContextHolder.clearContext();
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));
        when(repository.findAllChildren(ID)).thenReturn(List.of());
        when(attachmentRepository.countBySpecialityId(ID)).thenReturn(0L);

        service.delete(ID);

        assertThat(speciality.getDeletedAt()).isNotNull();
        assertThat(speciality.getDeletedBy()).isEqualTo("system");
    }

    @Test
    @DisplayName("attachment blocks — neither the stamp nor the years are touched")
    void delete_shouldChangeNothing_whenAttachmentBlocks() {
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));
        when(repository.findAllChildren(ID)).thenReturn(List.of());
        when(attachmentRepository.countBySpecialityId(ID)).thenReturn(2L);
        when(attachmentRepository.countBySpecialityIdGroupedByUniversity(ID))
                .thenReturn(List.<Object[]>of(blocker("301")));

        assertThatThrownBy(() -> service.delete(ID)).isInstanceOf(BusinessRuleException.class);

        assertThat(speciality.getDeletedAt()).as("a refused delete must not stamp the row").isNull();
        verify(repository, never()).delete(any());
        verify(yearRepository, never()).deleteBySpecialityId(any());
    }

    @Test
    @DisplayName("unknown id — ResourceNotFoundException")
    void delete_shouldThrowNotFound_whenIdIsUnknown() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("HSpeciality")
                .hasMessageContaining("id");

        verify(repository, never()).delete(any());
        verifyNoInteractions(yearRepository, attachmentRepository);
    }

    @Test
    @DisplayName("APPROVED — 422 SPECIALITY_DELETE_APPROVED_FORBIDDEN, the row is not stamped")
    void delete_shouldThrowBusinessRule_whenStatusIsApproved() {
        speciality.setReviewStatus(ReviewStatus.APPROVED);
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_DELETE_APPROVED_FORBIDDEN"))
                // The message tells the admin the one action that unblocks them.
                .hasMessageContaining("demote it back to 'Needs review' first");

        assertThat(speciality.getDeletedAt()).as("a refused delete must not stamp the row").isNull();
        verify(repository, never()).delete(any());
        verify(yearRepository, never()).deleteBySpecialityId(any());
    }

    @Test
    @DisplayName("has children — 422 SPECIALITY_HAS_CHILDREN_DELETE_FIRST, message lists them")
    void delete_shouldThrowBusinessRule_whenChildrenExist() {
        HSpeciality child = row(UUID.randomUUID(), "60110101", "Amaliy matematika", ReviewStatus.NEEDS_REVIEW);
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));
        when(repository.findAllChildren(ID)).thenReturn(List.of(child));

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_HAS_CHILDREN_DELETE_FIRST"))
                .hasMessageContaining("60110101")
                .hasMessageContaining("Amaliy matematika");

        assertThat(speciality.getDeletedAt()).as("a refused delete must not stamp the row").isNull();
        assertThat(child.getDeletedAt()).as("the blocking child is untouched too").isNull();
        verify(repository, never()).delete(any());
        verifyNoInteractions(attachmentRepository);
    }

    @Test
    @DisplayName("attached to an OTM — 422 SPECIALITY_ATTACHED_TO_UNIVERSITY, the row is not stamped")
    void delete_shouldThrowBusinessRule_whenAttachedToUniversity() {
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));
        when(repository.findAllChildren(ID)).thenReturn(List.of());
        when(attachmentRepository.countBySpecialityId(ID)).thenReturn(3L);
        when(attachmentRepository.countBySpecialityIdGroupedByUniversity(ID))
                .thenReturn(List.<Object[]>of(blocker("301"), blocker("337")));

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_ATTACHED_TO_UNIVERSITY"))
                .hasMessageContaining("3")
                // The blocking OTM codes are named — the admin knows where to detach.
                .hasMessageContaining("301")
                .hasMessageContaining("337");

        assertThat(speciality.getDeletedAt()).as("a refused delete must not stamp the row").isNull();
        verify(repository, never()).delete(any());
        verify(yearRepository, never()).deleteBySpecialityId(any());
    }

    @Test
    @DisplayName("APPROVED + children — the status guard wins (children are never queried)")
    void delete_shouldReportStatusFirst_whenApprovedRowAlsoHasChildren() {
        speciality.setReviewStatus(ReviewStatus.APPROVED);
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_DELETE_APPROVED_FORBIDDEN"));

        assertThat(speciality.getDeletedAt()).isNull();
        // Short-circuit proof: the guard order is status → children → attachments.
        verify(repository, never()).findAllChildren(any());
        verifyNoInteractions(attachmentRepository);
    }

    /** One blocking OTM row of the grouped attachment query: positional [code, count]. */
    private static Object[] blocker(String universityCode) {
        return new Object[]{universityCode, 1L};
    }

    private static HSpeciality row(UUID id, String code, String nameUz, ReviewStatus status) {
        HSpeciality s = new HSpeciality();
        s.setId(id);
        s.setCode(code);
        s.setNameUz(nameUz);
        s.setEducationType("11");
        s.setReviewStatus(status);
        return s;
    }
}
