package uz.hemis.service.classifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.domain.repository.HEducationFormRepository;
import uz.hemis.domain.repository.HEducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.UniversitySpecialityAttachmentRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The free-text speciality filter ({@code q}) of the attachment registry — what the service hands
 * the repository as {@code qLike} / {@code qFolded} / {@code qId}.
 *
 * <p>The three binds ARE the filter: they are null together when there is nothing to search for
 * (that null is what switches the OR off in the query), the folded one must go through the same
 * fold that seeded {@code name_search}, and a pasted UUID must arrive as an exact id — so they are
 * pinned here, with no database involved.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialityAttachmentService — free-text speciality search (q)")
class SpecialityAttachmentServiceSearchTest {

    @Mock private UniversitySpecialityAttachmentRepository repository;
    @Mock private HSpecialityRepository specialityRepository;
    @Mock private HEducationTypeRepository educationTypeRepository;
    @Mock private HEducationFormRepository educationFormRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private ScopeResolver scopeResolver;

    @InjectMocks
    private SpecialityAttachmentService service;

    @Test
    @DisplayName("no letter and no digit: every search bind is null (no filter at all)")
    void search_shouldNotFilter_whenQueryHasNoLetterOrDigit() {
        Binds binds = listWith("  --  ");

        assertThat(binds.like()).isNull();
        assertThat(binds.folded()).isNull();
        assertThat(binds.id()).isNull();
    }

    @Test
    @DisplayName("blank query: same as no query")
    void search_shouldNotFilter_whenQueryIsBlank() {
        Binds binds = listWith("   ");

        assertThat(binds.like()).isNull();
        assertThat(binds.folded()).isNull();
        assertThat(binds.id()).isNull();
    }

    @Test
    @DisplayName("plain text: trimmed lower-case code pattern + apostrophe-folded name pattern")
    void search_shouldBindTrimmedAndFoldedPatterns() {
        Binds binds = listWith("  O'zbek tili  ");

        assertThat(binds.like()).isEqualTo("%o'zbek tili%");
        // Folded exactly like the generated name_search column (apostrophe -> space, collapsed).
        assertThat(binds.folded()).isEqualTo("%o zbek tili%");
        assertThat(binds.id()).isNull();
    }

    @Test
    @DisplayName("pasted UUID: bound as an exact speciality id (case-insensitive), patterns still bound")
    void search_shouldBindExactId_whenQueryIsUuid() {
        Binds binds = listWith("AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE");

        assertThat(binds.id()).isEqualTo(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        assertThat(binds.like()).isEqualTo("%aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee%");
        assertThat(binds.folded()).isEqualTo("%aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee%");
    }

    /** Runs an unrestricted (ministry) list with only {@code q} set and captures the search binds. */
    private Binds listWith(String q) {
        when(scopeResolver.currentScope()).thenReturn(AccessScope.global());
        when(repository.searchAll(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        service.list(null, null, q, null, null, null, null, PageRequest.of(0, 20));

        ArgumentCaptor<String> like = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> folded = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> id = ArgumentCaptor.forClass(UUID.class);
        verify(repository).searchAll(isNull(), like.capture(), folded.capture(), id.capture(),
                isNull(), isNull(), isNull(), isNull(), any());
        return new Binds(like.getValue(), folded.getValue(), id.getValue());
    }

    private record Binds(String like, String folded, UUID id) {
    }
}
