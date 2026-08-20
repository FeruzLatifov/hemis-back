package uz.hemis.service.classifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.repository.HEducationFormRepository;
import uz.hemis.domain.repository.HEducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.UniversitySpecialityAttachmentRepository;
import uz.hemis.service.classifier.dto.SpecialityAttachedUniversityDto;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The delete-blocker list behind {@code GET /classifiers/speciality/{id}/attachments}.
 *
 * <p>It must name every OTM that keeps {@code fk_univ_spec_attach_spec} from letting the classifier
 * row go — including an OTM whose registry row is gone (orphan code), which blocks all the same.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialityAttachmentService — attached universities (delete blockers)")
class SpecialityAttachmentServiceAttachedUniversitiesTest {

    private static final UUID SPECIALITY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock private UniversitySpecialityAttachmentRepository repository;
    @Mock private HSpecialityRepository specialityRepository;
    @Mock private HEducationTypeRepository educationTypeRepository;
    @Mock private HEducationFormRepository educationFormRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private ScopeResolver scopeResolver;

    @InjectMocks
    private SpecialityAttachmentService service;

    @Test
    @DisplayName("two OTMs: names resolved, counters and repository order preserved")
    void attachedUniversities_shouldResolveNamesAndKeepOrder() {
        when(repository.countAllBySpecialityIdGroupedByUniversity(SPECIALITY_ID))
                .thenReturn(List.<Object[]>of(blocker("301", 3, 2), blocker("337", 1, 1)));
        when(universityRepository.findAllById(anyIterable()))
                .thenReturn(List.of(university("301", "Andijon davlat universiteti"),
                        university("337", "Toshkent davlat texnika universiteti")));

        List<SpecialityAttachedUniversityDto> result = service.attachedUniversities(SPECIALITY_ID);

        assertThat(result).containsExactly(
                new SpecialityAttachedUniversityDto("301", "Andijon davlat universiteti", 3, 2),
                new SpecialityAttachedUniversityDto("337", "Toshkent davlat texnika universiteti", 1, 1));
    }

    @Test
    @DisplayName("code missing from the university registry: the code itself is the name (orphan still blocks)")
    void attachedUniversities_shouldFallBackToCode_whenUniversityIsUnknown() {
        when(repository.countAllBySpecialityIdGroupedByUniversity(SPECIALITY_ID))
                .thenReturn(List.<Object[]>of(blocker("562", 2, 0)));
        when(universityRepository.findAllById(anyIterable())).thenReturn(List.of());

        List<SpecialityAttachedUniversityDto> result = service.attachedUniversities(SPECIALITY_ID);

        assertThat(result).containsExactly(new SpecialityAttachedUniversityDto("562", "562", 2, 0));
    }

    @Test
    @DisplayName("no attachments: empty list, no name lookup at all")
    void attachedUniversities_shouldReturnEmptyList_whenNothingIsAttached() {
        when(repository.countAllBySpecialityIdGroupedByUniversity(SPECIALITY_ID)).thenReturn(List.of());

        List<SpecialityAttachedUniversityDto> result = service.attachedUniversities(SPECIALITY_ID);

        assertThat(result).isEmpty();
        verify(universityRepository, never()).findAllById(anyIterable());
        // Global reference data — the blocker list is never narrowed by the caller's OTM scope.
        verifyNoInteractions(scopeResolver);
    }

    /** One grouped-query row: positional {@code [university_code, total, live]}. */
    private static Object[] blocker(String code, long total, long live) {
        return new Object[]{code, total, live};
    }

    private static University university(String code, String name) {
        University u = new University();
        u.setCode(code);
        u.setName(name);
        return u;
    }
}
