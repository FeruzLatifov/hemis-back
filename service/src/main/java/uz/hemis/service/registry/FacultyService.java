package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.university.FacultyDetailDto;
import uz.hemis.common.dto.university.FacultyDictionariesDto;
import uz.hemis.common.dto.university.FacultyGroupRowDto;
import uz.hemis.common.dto.university.FacultyRowDto;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.projection.FacultyDetailRow;
import uz.hemis.domain.repository.projection.FacultyExportRow;
import uz.hemis.domain.repository.projection.FacultyRow;
import uz.hemis.domain.repository.projection.UniversityGroupRow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FacultyService {

    private final FacultyRepository facultyRepository;

    /** Hard limit — memory exhaustion oldini olish. */
    private static final int EXPORT_HARD_LIMIT = 10_000;

    /**
     * Get university groups (root level of tree view)
     */
    public Page<FacultyGroupRowDto> getUniversityGroups(String searchQuery, Boolean status, Pageable pageable) {
        log.debug("Getting university groups: q={}, status={}, page={}", searchQuery, status, pageable);
        return facultyRepository.findUniversityGroups(searchQuery, status, pageable)
                .map(this::mapToGroupRow);
    }

    /**
     * Get faculties by university (lazy load children when group expanded)
     */
    public Page<FacultyRowDto> getFacultiesByUniversity(
        String universityCode,
        String searchQuery,
        Boolean status,
        Pageable pageable
    ) {
        log.debug("Getting faculties for university {}: q={}, status={}, page={}",
            universityCode, searchQuery, status, pageable);
        return facultyRepository.findByUniversityCode(universityCode, searchQuery, status, pageable)
                .map(this::mapToFacultyRow);
    }

    /**
     * Get faculty detail by ID
     */
    public Optional<FacultyDetailDto> getFacultyById(UUID id) {
        log.debug("Getting faculty detail: id={}", id);
        FacultyDetailRow row = facultyRepository.findFacultyDetailById(id);
        return row == null ? Optional.empty() : Optional.of(mapToDetailDto(row));
    }

    /**
     * Get all faculties for export (with filters applied, hard limit 10000)
     */
    public List<FacultyExportRow> getFacultiesForExport(
        String universityCode,
        String searchQuery,
        Boolean status
    ) {
        log.debug("Getting faculties for export: university={}, q={}, status={}",
            universityCode, searchQuery, status);
        List<FacultyExportRow> rows = facultyRepository.findAllForExport(
                universityCode, searchQuery, status, EXPORT_HARD_LIMIT);
        if (rows.size() == EXPORT_HARD_LIMIT) {
            log.warn("Faculty export hit hard limit ({}). Filter may be too broad — refine criteria.",
                    EXPORT_HARD_LIMIT);
        }
        return rows;
    }

    /**
     * Get dictionaries for filters (cached)
     */
    @Cacheable(value = "faculty-dictionaries", key = "'all'")
    public FacultyDictionariesDto getDictionaries() {
        log.debug("Getting faculty dictionaries");

        List<Map<String, Object>> statuses = Arrays.asList(
            Map.of("value", true, "labelKey", "filters.statusActive"),
            Map.of("value", false, "labelKey", "filters.statusInactive")
        );

        return FacultyDictionariesDto.builder()
            .statuses(statuses)
            .build();
    }

    // ======================================================================
    // Projection → DTO mapping (type-safe getters, no Map manipulation)
    // ======================================================================

    private FacultyGroupRowDto mapToGroupRow(UniversityGroupRow row) {
        long active = row.getActivecount() != null ? row.getActivecount() : 0L;
        long inactive = row.getInactivecount() != null ? row.getInactivecount() : 0L;
        String statusSummary = String.format("Faol: %d, Nofaol: %d", active, inactive);
        return FacultyGroupRowDto.builder()
            .universityId(row.getUniversityid())
            .universityName(row.getUniversityname())
            .facultyCount(row.getFacultycount() != null ? row.getFacultycount() : 0L)
            .statusSummary(statusSummary)
            .hasChildren(true)
            .build();
    }

    private FacultyRowDto mapToFacultyRow(FacultyRow row) {
        return FacultyRowDto.builder()
            .id(row.getId())
            .code(row.getCode())
            .nameUz(row.getNameuz())
            .nameRu(row.getNameru())
            .shortName(row.getShortname())
            .universityId(row.getUniversityid())
            .active(Boolean.TRUE.equals(row.getActive()))
            .build();
    }

    private FacultyDetailDto mapToDetailDto(FacultyDetailRow row) {
        return FacultyDetailDto.builder()
            .id(row.getId())
            .code(row.getCode())
            .name(row.getName())
            .shortName(row.getShortname())
            .universityCode(row.getUniversitycode())
            .universityName(row.getUniversityname())
            .facultyType(row.getFacultytype())
            .active(Boolean.TRUE.equals(row.getActive()))
            .createdAt(row.getCreatedat())
            .createdBy(row.getCreatedby())
            .updatedAt(row.getUpdatedat())
            .updatedBy(row.getUpdatedby())
            .build();
    }
}
