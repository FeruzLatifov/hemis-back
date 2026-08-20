package uz.hemis.service.classifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.common.exception.BusinessRuleException;
import uz.hemis.domain.entity.classifier.HSpeciality;
import uz.hemis.domain.entity.classifier.ReviewStatus;
import uz.hemis.domain.repository.HEducationFormRepository;
import uz.hemis.domain.repository.HEducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.UniversitySpecialityAttachmentRepository;
import uz.hemis.service.classifier.dto.SpecialityAttachmentBulkCreateDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentCreateDto;

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
 * The "only a distributed speciality may be attached" guard on both write paths
 * ({@code create} / {@code createBulk}).
 *
 * <p>A NEEDS_REVIEW (or deactivated) classifier row has never been shipped to the 224 OTMs, so an
 * attachment pointing at it would reference a speciality the OTM does not have. The UI filter is
 * not enough — a direct API POST must be refused too, which is what these tests pin.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialityAttachmentService — APPROVED-only attach guard")
class SpecialityAttachmentServiceApprovedGuardTest {

    private static final UUID SPECIALITY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String UNIVERSITY_CODE = "337";

    @Mock private UniversitySpecialityAttachmentRepository repository;
    @Mock private HSpecialityRepository specialityRepository;
    @Mock private HEducationTypeRepository educationTypeRepository;
    @Mock private HEducationFormRepository educationFormRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private ScopeResolver scopeResolver;

    @InjectMocks
    private SpecialityAttachmentService service;

    private HSpeciality speciality;

    @BeforeEach
    void setUp() {
        speciality = new HSpeciality();
        speciality.setId(SPECIALITY_ID);
        speciality.setCode("60110100");
        speciality.setNameUz("Matematika");
        speciality.setEducationType("11");
        speciality.setReviewStatus(ReviewStatus.NEEDS_REVIEW);
        speciality.setActive(true);

        when(scopeResolver.currentScope()).thenReturn(AccessScope.global());
        when(specialityRepository.findById(SPECIALITY_ID)).thenReturn(Optional.of(speciality));
    }

    @Test
    @DisplayName("create — NEEDS_REVIEW speciality: 422 SPECIALITY_NOT_APPROVED, nothing saved")
    void create_shouldThrowBusinessRule_whenSpecialityIsNotApproved() {
        SpecialityAttachmentCreateDto dto =
                new SpecialityAttachmentCreateDto(UNIVERSITY_CODE, SPECIALITY_ID, "11", 2026, "ACTIVE");

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_NOT_APPROVED"))
                .hasMessageContaining("60110100");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("createBulk — NEEDS_REVIEW speciality: 422 SPECIALITY_NOT_APPROVED, nothing saved")
    void createBulk_shouldThrowBusinessRule_whenSpecialityIsNotApproved() {
        SpecialityAttachmentBulkCreateDto dto = new SpecialityAttachmentBulkCreateDto(
                UNIVERSITY_CODE, SPECIALITY_ID, List.of("11", "12"), 2026, "ACTIVE");

        assertThatThrownBy(() -> service.createBulk(dto))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_NOT_APPROVED"));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create — APPROVED but deactivated speciality: refused the same way")
    void create_shouldThrowBusinessRule_whenSpecialityIsInactive() {
        speciality.setReviewStatus(ReviewStatus.APPROVED);
        speciality.setActive(false);
        SpecialityAttachmentCreateDto dto =
                new SpecialityAttachmentCreateDto(UNIVERSITY_CODE, SPECIALITY_ID, "11", 2026, "ACTIVE");

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        e -> assertThat(e.getRuleCode()).isEqualTo("SPECIALITY_NOT_APPROVED"));

        verify(repository, never()).save(any());
    }
}
