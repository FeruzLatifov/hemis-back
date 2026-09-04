package uz.hemis.service.classifier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.hemis.domain.entity.classifier.HSpeciality;
import uz.hemis.domain.entity.classifier.ReviewStatus;
import uz.hemis.domain.repository.HEducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.domain.repository.HSpecialityYearRepository;
import uz.hemis.domain.repository.UniversitySpecialityAttachmentRepository;
import uz.hemis.service.classifier.dto.SpecialityUpdateDto;
import uz.hemis.service.outbox.OutboxEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The review status is the distribution switch for 230 OTMs, so both directions are gated.
 *
 * <p>Approving a row publishes it (outbox → webhook fanout); un-approving retracts it. Neither is an
 * editing action, so {@code classifiers.speciality.edit} alone must not move the switch — that is
 * what {@code classifiers.speciality.approve} is for, and it is the whole reason the field is frozen
 * in the UI for a role that lacks it (TECH_STAFF, "Texnik xodim"). This test pins the server side of
 * that contract: the UI freeze is a convenience, this is the enforcement.</p>
 *
 * <p>The third case is the one that makes the frozen UI workable: an editor's form posts the row's
 * CURRENT status back untouched, and re-sending the same value is not a change — it must pass, or
 * every ordinary name/year edit by an editor would 403.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HSpecialityService.update() — review status needs .approve in BOTH directions")
class HSpecialityServiceApproveGuardTest {

    private static final UUID ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final String APPROVE = "classifiers.speciality.approve";
    private static final String EDIT = "classifiers.speciality.edit";

    @Mock private HSpecialityRepository repository;
    @Mock private HSpecialityYearRepository yearRepository;
    @Mock private HEducationTypeRepository educationTypeRepository;
    @Mock private UniversitySpecialityAttachmentRepository attachmentRepository;
    @Mock private OutboxEventPublisher outboxPublisher;

    @InjectMocks private HSpecialityService service;

    @BeforeEach
    void stubLoads() {
        when(repository.findAllChildren(ID)).thenReturn(List.of());
        when(educationTypeRepository.findAll()).thenReturn(List.of());
        when(yearRepository.findBySpecialityIds(any())).thenReturn(List.of());
        when(repository.save(any(HSpeciality.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("an editor cannot approve — the promotion is refused before anything is saved")
    void editorCannotApprove() {
        authenticateWith(EDIT);
        row(ReviewStatus.NEEDS_REVIEW);

        assertThatThrownBy(() -> service.update(ID, dtoWithStatus("APPROVED")))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining(APPROVE);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("an editor cannot un-approve either — retracting from the OTMs is the same decision")
    void editorCannotDemote() {
        authenticateWith(EDIT);
        row(ReviewStatus.APPROVED);

        assertThatThrownBy(() -> service.update(ID, dtoWithStatus("NEEDS_REVIEW")))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining(APPROVE);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("re-sending the SAME status is not a change — an editor's ordinary save still works")
    void editorMayResendTheCurrentStatus() {
        authenticateWith(EDIT);
        HSpeciality s = row(ReviewStatus.APPROVED);

        service.update(ID, dtoWithStatus("APPROVED"));

        verify(repository).save(s);
        assertThat(s.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);
    }

    @Test
    @DisplayName("an approver moves the switch in both directions")
    void approverMayApproveAndUnapprove() {
        authenticateWith(EDIT, APPROVE);
        HSpeciality s = row(ReviewStatus.NEEDS_REVIEW);

        service.update(ID, dtoWithStatus("APPROVED"));
        assertThat(s.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);

        service.update(ID, dtoWithStatus("NEEDS_REVIEW"));
        assertThat(s.getReviewStatus()).isEqualTo(ReviewStatus.NEEDS_REVIEW);
    }

    @Test
    @DisplayName("an editor may move an APPROVED row — moving is editing, and it withdraws the row")
    void editorMayMoveAnApprovedRow() {
        // Moving used to be refused outright, back when it was the only way to reach NEEDS_REVIEW
        // without .approve. Now EVERY change to distributed content does that, so singling out the
        // placement picker only made the rule harder to explain: whoever may edit may move.
        authenticateWith(EDIT);
        HSpeciality s = row(ReviewStatus.APPROVED);
        s.setHierarchyLevel(2);

        service.update(ID, dtoWithPlacement(1));

        verify(repository).save(s);
        assertThat(s.getReviewStatus()).isEqualTo(ReviewStatus.NEEDS_REVIEW);
    }

    @Test
    @DisplayName("changing the content revokes the approval — even when the approver asks to keep it")
    void anyContentChangeRevokesTheApproval() {
        // An approval is an approval OF CONTENT. Renaming an APPROVED row and leaving the status
        // alone used to ship the new text to 230 OTMs under a sign-off nobody gave it.
        authenticateWith(EDIT, APPROVE);
        HSpeciality s = row(ReviewStatus.APPROVED);

        service.update(ID, dtoRenamedTo("Matematika va informatika"));

        assertThat(s.getReviewStatus()).isEqualTo(ReviewStatus.NEEDS_REVIEW);
    }

    @Test
    @DisplayName("an editor may fix an approved row — the fix simply withdraws it until re-approval")
    void editorMayEditAnApprovedRowAndItIsWithdrawn() {
        authenticateWith(EDIT);
        HSpeciality s = row(ReviewStatus.APPROVED);

        // The form posts the row's current status back untouched; the content is what changed.
        service.update(ID, dtoRenamedTo("Matematika (tuzatilgan)"));

        verify(repository).save(s);
        assertThat(s.getReviewStatus()).isEqualTo(ReviewStatus.NEEDS_REVIEW);
    }

    /** nameUz changed, status re-sent unchanged — an ordinary content edit. */
    private static SpecialityUpdateDto dtoRenamedTo(String nameUz) {
        return new SpecialityUpdateDto(null, nameUz, null, null, null, null, "APPROVED", null, null, null);
    }

    /** nameUz (required) + a placement change; no explicit status. */
    private static SpecialityUpdateDto dtoWithPlacement(Integer level) {
        return new SpecialityUpdateDto(null, "Matematika", null, null, null, null, null, level, null, null);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private HSpeciality row(ReviewStatus status) {
        HSpeciality s = new HSpeciality();
        s.setId(ID);
        s.setCode("60110100");
        s.setNameUz("Matematika");
        s.setEducationType("11");
        s.setHierarchyLevel(1);
        s.setActive(true);
        s.setReviewStatus(status);
        when(repository.findById(ID)).thenReturn(Optional.of(s));
        return s;
    }

    /** Only nameUz (required) and the status: every other field null so no other branch runs. */
    private static SpecialityUpdateDto dtoWithStatus(String status) {
        return new SpecialityUpdateDto(null, "Matematika", null, null, null, null, status, null, null, null);
    }

    private static void authenticateWith(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("curator", "n/a",
                        java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList()));
    }
}
