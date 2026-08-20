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
 * {@link HSpecialityService#delete(UUID)} unit tests — the three guards and their order.
 *
 * <p>A hard delete is admissible only for a NEEDS_REVIEW curation row that nothing depends on,
 * so every test here pins one refusal reason: status (the row is distributed), children
 * (FK {@code ON DELETE RESTRICT}), OTM attachments (idem, revoked rows included).</p>
 *
 * <p>Guard order matters as much as the guards themselves — an APPROVED parent must be told
 * about its status first (the actionable fact), not about its subtree.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HSpecialityService.delete()")
class HSpecialityServiceDeleteTest {

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

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
    }

    @Test
    @DisplayName("NEEDS_REVIEW, no children, no attachments — years and row are deleted")
    void delete_shouldRemoveRowAndYears_whenNeedsReviewAndUnreferenced() {
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));
        when(repository.findAllChildren(ID)).thenReturn(List.of());
        when(attachmentRepository.countAllBySpecialityId(ID)).thenReturn(0L);

        service.delete(ID);

        verify(yearRepository).deleteBySpecialityId(ID);
        verify(repository).delete(speciality);
        // A NEEDS_REVIEW row was never distributed — no PUSH on delete.
        verifyNoInteractions(outboxPublisher);
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
    @DisplayName("APPROVED — 422 SPECIALITY_DELETE_APPROVED_FORBIDDEN, nothing deleted")
    void delete_shouldThrowBusinessRule_whenStatusIsApproved() {
        speciality.setReviewStatus(ReviewStatus.APPROVED);
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_DELETE_APPROVED_FORBIDDEN"));

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

        verify(repository, never()).delete(any());
        verifyNoInteractions(attachmentRepository);
    }

    @Test
    @DisplayName("attached to an OTM — 422 SPECIALITY_ATTACHED_TO_UNIVERSITY, nothing deleted")
    void delete_shouldThrowBusinessRule_whenAttachedToUniversity() {
        when(repository.findById(ID)).thenReturn(Optional.of(speciality));
        when(repository.findAllChildren(ID)).thenReturn(List.of());
        when(attachmentRepository.countAllBySpecialityId(ID)).thenReturn(3L);

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_ATTACHED_TO_UNIVERSITY"))
                .hasMessageContaining("3");

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

        // Short-circuit proof: the guard order is status → children → attachments.
        verify(repository, never()).findAllChildren(any());
        verifyNoInteractions(attachmentRepository);
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
