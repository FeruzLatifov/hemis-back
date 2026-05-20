package uz.hemis.service.registry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uz.hemis.common.dto.university.FacultyDetailDto;
import uz.hemis.common.dto.university.FacultyDictionariesDto;
import uz.hemis.common.dto.university.FacultyRowDto;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.projection.FacultyDetailRow;
import uz.hemis.domain.repository.projection.FacultyExportRow;
import uz.hemis.domain.repository.projection.FacultyRow;
import uz.hemis.domain.repository.projection.UniversityGroupRow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FacultyService — registry + dictionaries + export")
class FacultyServiceTest {

    @Mock private FacultyRepository repository;

    @InjectMocks
    private FacultyService service;

    @Test
    void getUniversityGroups_mapsToDto() {
        UniversityGroupRow row = mock(UniversityGroupRow.class);
        when(row.getUniversityid()).thenReturn("337");
        when(row.getUniversityname()).thenReturn("Andijon DU");
        when(row.getFacultycount()).thenReturn(8L);
        when(row.getActivecount()).thenReturn(7L);
        when(row.getInactivecount()).thenReturn(1L);

        when(repository.findUniversityGroups(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(row)));

        var result = service.getUniversityGroups("Andijon", true, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getUniversityName()).isEqualTo("Andijon DU");
        assertThat(result.getContent().get(0).getFacultyCount()).isEqualTo(8L);
        assertThat(result.getContent().get(0).getStatusSummary()).contains("Faol: 7", "Nofaol: 1");
        assertThat(result.getContent().get(0).getHasChildren()).isTrue();
    }

    @Test
    void getUniversityGroups_nullCounts_defaultsToZero() {
        UniversityGroupRow row = mock(UniversityGroupRow.class);
        when(row.getActivecount()).thenReturn(null);
        when(row.getInactivecount()).thenReturn(null);
        when(row.getFacultycount()).thenReturn(null);

        when(repository.findUniversityGroups(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(row)));

        var result = service.getUniversityGroups(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getFacultyCount()).isZero();
        assertThat(result.getContent().get(0).getStatusSummary()).contains("Faol: 0");
    }

    @Test
    void getFacultiesByUniversity_mapsToRowDto() {
        FacultyRow row = mock(FacultyRow.class);
        UUID id = UUID.randomUUID();
        when(row.getId()).thenReturn(id);
        when(row.getCode()).thenReturn("F-001");
        when(row.getNameuz()).thenReturn("Informatika fakulteti");
        when(row.getNameru()).thenReturn("Факультет информатики");
        when(row.getShortname()).thenReturn("IF");
        when(row.getActive()).thenReturn(true);

        when(repository.findByUniversityCode(eq("337"), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(row)));

        var result = service.getFacultiesByUniversity("337", null, true, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        FacultyRowDto dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getCode()).isEqualTo("F-001");
        assertThat(dto.getNameUz()).isEqualTo("Informatika fakulteti");
        assertThat(dto.getActive()).isTrue();
    }

    @Test
    void getFacultyById_found() {
        UUID id = UUID.randomUUID();
        FacultyDetailRow row = mock(FacultyDetailRow.class);
        when(row.getId()).thenReturn(id);
        when(row.getCode()).thenReturn("F-001");
        when(row.getName()).thenReturn("Faculty 1");
        when(row.getActive()).thenReturn(true);
        when(row.getCreatedat()).thenReturn(LocalDateTime.now());

        when(repository.findFacultyDetailById(id)).thenReturn(row);

        Optional<FacultyDetailDto> result = service.getFacultyById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("F-001");
        assertThat(result.get().getActive()).isTrue();
    }

    @Test
    void getFacultyById_notFound_emptyOptional() {
        UUID id = UUID.randomUUID();
        when(repository.findFacultyDetailById(id)).thenReturn(null);

        assertThat(service.getFacultyById(id)).isEmpty();
    }

    @Test
    void getFacultiesForExport_underLimit() {
        FacultyExportRow row = mock(FacultyExportRow.class);
        when(repository.findAllForExport(anyString(), any(), any(), anyInt()))
                .thenReturn(List.of(row, row));

        List<FacultyExportRow> result = service.getFacultiesForExport("337", "Inf", true);

        assertThat(result).hasSize(2);
    }

    @Test
    void getFacultiesForExport_hardLimitHit_warnLogged() {
        // Build a list of EXPORT_HARD_LIMIT size to trigger the warn branch.
        FacultyExportRow row = mock(FacultyExportRow.class);
        List<FacultyExportRow> hugeList = new java.util.ArrayList<>(10_000);
        for (int i = 0; i < 10_000; i++) hugeList.add(row);

        when(repository.findAllForExport(any(), any(), any(), anyInt()))
                .thenReturn(hugeList);

        List<FacultyExportRow> result = service.getFacultiesForExport(null, null, null);
        assertThat(result).hasSize(10_000);
    }

    @Test
    void getDictionaries_statusOptions() {
        FacultyDictionariesDto result = service.getDictionaries();

        assertThat(result.getStatuses()).hasSize(2);
        @SuppressWarnings("unchecked")
        Map<String, Object> active = (Map<String, Object>) result.getStatuses().get(0);
        assertThat(active).containsEntry("value", true);
        assertThat(active).containsEntry("labelKey", "filters.statusActive");
    }
}
