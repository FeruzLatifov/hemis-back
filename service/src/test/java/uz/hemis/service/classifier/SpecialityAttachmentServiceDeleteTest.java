package uz.hemis.service.classifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.classifier.UniversitySpecialityAttachment;
import uz.hemis.domain.repository.HEducationFormRepository;
import uz.hemis.domain.repository.HEducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.UniversitySpecialityAttachmentRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SpecialityAttachmentService#delete(UUID)} — detaching is a HARD delete.
 *
 * <p>Nothing references an attachment, so the row goes for good instead of staying behind as an
 * invisible {@code deleted_at} row that still held {@code fk_univ_spec_attach_spec} and blocked the
 * classifier delete with "attached to 3 OTMs" while the registry showed nothing. Withdrawing the
 * permission while keeping the record is a {@code status} change, not a delete — so no test here
 * expects a {@code save}.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialityAttachmentService.delete()")
class SpecialityAttachmentServiceDeleteTest {

    private static final UUID ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final String UNIVERSITY_CODE = "337";

    @Mock private UniversitySpecialityAttachmentRepository repository;
    @Mock private HSpecialityRepository specialityRepository;
    @Mock private HEducationTypeRepository educationTypeRepository;
    @Mock private HEducationFormRepository educationFormRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private ScopeResolver scopeResolver;

    @InjectMocks
    private SpecialityAttachmentService service;

    private UniversitySpecialityAttachment attachment;

    @BeforeEach
    void setUp() {
        attachment = new UniversitySpecialityAttachment();
        attachment.setId(ID);
        attachment.setUniversityCode(UNIVERSITY_CODE);
        attachment.setSpecialityId(UUID.randomUUID());
        attachment.setEducationForm("11");
        attachment.setEduYear(2026);
        attachment.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("in scope — the row is physically deleted, never saved back")
    void delete_shouldHardDeleteRow_whenInScope() {
        when(repository.findById(ID)).thenReturn(Optional.of(attachment));
        when(scopeResolver.currentScope()).thenReturn(AccessScope.global());

        service.delete(ID);

        verify(repository).delete(attachment);
        // No soft delete: a saved-back row would be an invisible FK blocker.
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("out of scope — 403, nothing deleted (cross-OTM IDOR closed)")
    void delete_shouldDenyAndKeepRow_whenOutOfScope() {
        when(repository.findById(ID)).thenReturn(Optional.of(attachment));
        when(scopeResolver.currentScope()).thenReturn(AccessScope.restrictedTo("301"));

        assertThatThrownBy(() -> service.delete(ID)).isInstanceOf(AccessDeniedException.class);

        verify(repository, never()).delete(any(UniversitySpecialityAttachment.class));
    }

    @Test
    @DisplayName("unknown id — ResourceNotFoundException, scope never consulted")
    void delete_shouldThrowNotFound_whenIdIsUnknown() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SpecialityAttachment");

        verify(repository, never()).delete(any(UniversitySpecialityAttachment.class));
    }
}
