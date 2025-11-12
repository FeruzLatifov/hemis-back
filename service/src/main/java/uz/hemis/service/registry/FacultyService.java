package uz.hemis.service.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.*;
import uz.hemis.domain.repository.FacultyRepository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FacultyService {

    private final FacultyRepository facultyRepository;

    /**
     * Get university groups (root level of tree view)
     */
    public Page<FacultyGroupRowDto> getUniversityGroups(String searchQuery, Boolean status, Pageable pageable) {
        log.debug("Getting university groups: q={}, status={}, page={}", searchQuery, status, pageable);

        Page<Map<String, Object>> result = facultyRepository.findUniversityGroups(searchQuery, status, pageable);

        List<FacultyGroupRowDto> groups = result.getContent().stream()
            .map(this::mapToGroupRow)
            .collect(Collectors.toList());

        return new PageImpl<>(groups, pageable, result.getTotalElements());
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

        Page<Map<String, Object>> result = facultyRepository.findByUniversityCode(
            universityCode, searchQuery, status, pageable
        );

        List<FacultyRowDto> faculties = result.getContent().stream()
            .map(this::mapToFacultyRow)
            .collect(Collectors.toList());

        return new PageImpl<>(faculties, pageable, result.getTotalElements());
    }

    /**
     * Get faculty detail by ID
     */
    public Optional<FacultyDetailDto> getFacultyById(UUID id) {
        log.debug("Getting faculty detail: id={}", id);

        Map<String, Object> result = facultyRepository.findFacultyDetailById(id);
        
        if (result == null || result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToDetailDto(result));
    }

    /**
     * Get all faculties for export (with filters applied)
     */
    public List<Map<String, Object>> getFacultiesForExport(
        String universityCode,
        String searchQuery,
        Boolean status
    ) {
        log.debug("Getting faculties for export: university={}, q={}, status={}", 
            universityCode, searchQuery, status);

        return facultyRepository.findAllForExport(universityCode, searchQuery, status);
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
    // Private Mapping Methods
    // ======================================================================

    private FacultyGroupRowDto mapToGroupRow(Map<String, Object> row) {
        Long activeCount = getLongValue(row, "activecount");
        Long inactiveCount = getLongValue(row, "inactivecount");
        
        String statusSummary = String.format("Faol: %d, Nofaol: %d", activeCount, inactiveCount);

        return FacultyGroupRowDto.builder()
            .universityId(getStringValue(row, "universityid"))
            .universityName(getStringValue(row, "universityname"))
            .facultyCount(getLongValue(row, "facultycount"))
            .statusSummary(statusSummary)
            .hasChildren(true)
            .build();
    }

    private FacultyRowDto mapToFacultyRow(Map<String, Object> row) {
        return FacultyRowDto.builder()
            .id(getUUIDValue(row, "id"))
            .code(getStringValue(row, "code"))
            .nameUz(getStringValue(row, "nameuz"))
            .nameRu(getStringValue(row, "nameru"))
            .shortName(getStringValue(row, "shortname"))
            .universityId(getStringValue(row, "universityid"))
            .active(getBooleanValue(row, "active"))
            .build();
    }

    private FacultyDetailDto mapToDetailDto(Map<String, Object> row) {
        return FacultyDetailDto.builder()
            .id(getUUIDValue(row, "id"))
            .code(getStringValue(row, "code"))
            .name(getStringValue(row, "name"))
            .shortName(getStringValue(row, "shortname"))
            .universityCode(getStringValue(row, "universitycode"))
            .universityName(getStringValue(row, "universityname"))
            .facultyType(getStringValue(row, "facultytype"))
            .active(getBooleanValue(row, "active"))
            .createdAt(getLocalDateTimeValue(row, "createdat"))
            .createdBy(getStringValue(row, "createdby"))
            .updatedAt(getLocalDateTimeValue(row, "updatedat"))
            .updatedBy(getStringValue(row, "updatedby"))
            .build();
    }

    // ======================================================================
    // Helper Methods for Type Conversion
    // ======================================================================

    private String getStringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }

    private UUID getUUIDValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        if (value instanceof String) return UUID.fromString((String) value);
        return null;
    }

    private Long getLongValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof BigInteger) return ((BigInteger) value).longValue();
        return Long.parseLong(value.toString());
    }

    private Boolean getBooleanValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof Timestamp) return ((Timestamp) value).toLocalDateTime();
        return null;
    }
}

