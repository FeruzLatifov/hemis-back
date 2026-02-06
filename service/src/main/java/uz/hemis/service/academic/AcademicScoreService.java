package uz.hemis.service.academic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.academic.AcademicScoreDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.AcademicScore;
import uz.hemis.domain.repository.AcademicScoreRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for AcademicScore entity operations.
 * Provides CRUD operations with CUBA soft-delete pattern support.
 *
 * <p>Used by GradeEntityController for hemishe_RAcademicScore endpoint.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicScoreService {

    private final AcademicScoreRepository repository;

    /**
     * Find academic score by ID.
     *
     * @param id the entity ID
     * @return the DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public AcademicScoreDto findById(UUID id) {
        log.debug("Finding academic score by id: {}", id);
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicScore", "id", id));
    }

    /**
     * Find all academic scores with pagination.
     *
     * @param pageable pagination info
     * @return page of DTOs
     */
    @Transactional(readOnly = true)
    public Page<AcademicScoreDto> findAll(Pageable pageable) {
        log.debug("Finding all academic scores, pageable: {}", pageable);
        return repository.findAll(pageable).map(this::toDto);
    }

    /**
     * Create new academic score.
     *
     * @param dto the DTO with data
     * @return created DTO
     */
    @Transactional
    public AcademicScoreDto create(AcademicScoreDto dto) {
        log.debug("Creating academic score: {}", dto);
        AcademicScore entity = toEntity(dto);
        entity.setId(UUID.randomUUID());
        entity.setCreateTs(LocalDateTime.now());
        AcademicScore saved = repository.save(entity);
        return toDto(saved);
    }

    /**
     * Update existing academic score.
     *
     * @param id the entity ID
     * @param dto the DTO with updated data
     * @return updated DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public AcademicScoreDto update(UUID id, AcademicScoreDto dto) {
        log.debug("Updating academic score id: {}", id);
        AcademicScore existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicScore", "id", id));

        updateEntityFromDto(existing, dto);
        existing.setUpdateTs(LocalDateTime.now());
        AcademicScore saved = repository.save(existing);
        return toDto(saved);
    }

    /**
     * Soft delete academic score (CUBA pattern).
     *
     * @param id the entity ID
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public void softDelete(UUID id) {
        log.debug("Soft deleting academic score id: {}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("AcademicScore", "id", id);
        }
        repository.softDelete(id, "system", LocalDateTime.now());
    }

    // =====================================================
    // Mapping Methods
    // =====================================================

    private AcademicScoreDto toDto(AcademicScore entity) {
        return AcademicScoreDto.builder()
                .id(entity.getId())
                .universityCode(entity.getUniversityCode())
                .universityName(entity.getUniversityName())
                .facultyCode(entity.getFacultyCode())
                .facultyName(entity.getFacultyName())
                .educationTypeCode(entity.getEducationTypeCode())
                .educationTypeName(entity.getEducationTypeName())
                .educationYearCode(entity.getEducationYearCode())
                .educationYearName(entity.getEducationYearName())
                .semesterTypeCode(entity.getSemesterTypeCode())
                .semesterTypeName(entity.getSemesterTypeName())
                .courseCode(entity.getCourseCode())
                .courseName(entity.getCourseName())
                .tableType(entity.getTableType())
                .scorePercent(entity.getScorePercent())
                .scoreType(entity.getScoreType())
                .debitorCount(entity.getDebitorCount())
                .updateDate(entity.getUpdateDate())
                .createTs(entity.getCreateTs())
                .createdBy(entity.getCreatedBy())
                .updateTs(entity.getUpdateTs())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .build();
    }

    private AcademicScore toEntity(AcademicScoreDto dto) {
        AcademicScore entity = new AcademicScore();
        updateEntityFromDto(entity, dto);
        return entity;
    }

    private void updateEntityFromDto(AcademicScore entity, AcademicScoreDto dto) {
        if (dto.getUniversityCode() != null) entity.setUniversityCode(dto.getUniversityCode());
        if (dto.getUniversityName() != null) entity.setUniversityName(dto.getUniversityName());
        if (dto.getFacultyCode() != null) entity.setFacultyCode(dto.getFacultyCode());
        if (dto.getFacultyName() != null) entity.setFacultyName(dto.getFacultyName());
        if (dto.getEducationTypeCode() != null) entity.setEducationTypeCode(dto.getEducationTypeCode());
        if (dto.getEducationTypeName() != null) entity.setEducationTypeName(dto.getEducationTypeName());
        if (dto.getEducationYearCode() != null) entity.setEducationYearCode(dto.getEducationYearCode());
        if (dto.getEducationYearName() != null) entity.setEducationYearName(dto.getEducationYearName());
        if (dto.getSemesterTypeCode() != null) entity.setSemesterTypeCode(dto.getSemesterTypeCode());
        if (dto.getSemesterTypeName() != null) entity.setSemesterTypeName(dto.getSemesterTypeName());
        if (dto.getCourseCode() != null) entity.setCourseCode(dto.getCourseCode());
        if (dto.getCourseName() != null) entity.setCourseName(dto.getCourseName());
        if (dto.getTableType() != null) entity.setTableType(dto.getTableType());
        if (dto.getScorePercent() != null) entity.setScorePercent(dto.getScorePercent());
        if (dto.getScoreType() != null) entity.setScoreType(dto.getScoreType());
        if (dto.getDebitorCount() != null) entity.setDebitorCount(dto.getDebitorCount());
        if (dto.getUpdateDate() != null) entity.setUpdateDate(dto.getUpdateDate());
    }
}
