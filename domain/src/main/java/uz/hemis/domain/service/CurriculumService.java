package uz.hemis.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.CurriculumDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Curriculum;
import uz.hemis.domain.mapper.CurriculumMapper;
import uz.hemis.domain.repository.CurriculumRepository;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Curriculum business logic
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong> Soft delete ONLY</p>
 *
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final CurriculumMapper curriculumMapper;
    private final UniversityRepository universityRepository;
    private final SpecialtyRepository specialtyRepository;

    @Transactional
    public CurriculumDto create(CurriculumDto dto) {
        log.debug("Creating curriculum: {}", dto);
        validateForCreate(dto);
        Curriculum curriculum = curriculumMapper.toEntity(dto);
        Curriculum saved = curriculumRepository.save(curriculum);
        log.info("Created curriculum: id={}, code={}", saved.getId(), saved.getCode());
        return curriculumMapper.toDto(saved);
    }

    private void validateForCreate(CurriculumDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            errors.put("code", "Curriculum code is required");
        }
        if (dto.getCode() != null && curriculumRepository.existsByCode(dto.getCode())) {
            errors.put("code", "Curriculum with code '" + dto.getCode() + "' already exists");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            errors.put("name", "Curriculum name is required");
        }
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }
        if (dto.getSpecialty() != null) {
            if (!specialtyRepository.existsById(dto.getSpecialty())) {
                errors.put("specialty", "Specialty with id '" + dto.getSpecialty() + "' not found");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Curriculum validation failed", errors);
        }
    }

    public CurriculumDto findById(UUID id) {
        log.debug("Finding curriculum by id: {}", id);
        return curriculumRepository.findById(id)
                .map(curriculumMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found: " + id));
    }

    public CurriculumDto findByCode(String code) {
        log.debug("Finding curriculum by code: {}", code);
        return curriculumRepository.findByCode(code)
                .map(curriculumMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found: " + code));
    }

    public Page<CurriculumDto> findAll(Pageable pageable) {
        return curriculumRepository.findAll(pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findByUniversity(String universityCode, Pageable pageable) {
        return curriculumRepository.findByUniversity(universityCode, pageable).map(curriculumMapper::toDto);
    }

    public List<CurriculumDto> findAllByUniversity(String universityCode) {
        return curriculumRepository.findAllByUniversity(universityCode)
                .stream().map(curriculumMapper::toDto).collect(Collectors.toList());
    }

    public Page<CurriculumDto> findBySpecialty(UUID specialtyId, Pageable pageable) {
        return curriculumRepository.findBySpecialty(specialtyId, pageable).map(curriculumMapper::toDto);
    }

    public List<CurriculumDto> findAllBySpecialty(UUID specialtyId) {
        return curriculumRepository.findAllBySpecialty(specialtyId)
                .stream().map(curriculumMapper::toDto).collect(Collectors.toList());
    }

    public Page<CurriculumDto> findByNameContaining(String name, Pageable pageable) {
        return curriculumRepository.findByNameContainingIgnoreCase(name, pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findActive(Pageable pageable) {
        return curriculumRepository.findByActiveTrue(pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findApprovedCurricula(Pageable pageable) {
        return curriculumRepository.findApprovedCurricula(pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findByAcademicYear(String year, Pageable pageable) {
        return curriculumRepository.findByAcademicYear(year, pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findByEducationType(String typeCode, Pageable pageable) {
        return curriculumRepository.findByEducationType(typeCode, pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findByEducationForm(String formCode, Pageable pageable) {
        return curriculumRepository.findByEducationForm(formCode, pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findByCurriculumType(String typeCode, Pageable pageable) {
        return curriculumRepository.findByCurriculumType(typeCode, pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findByUniversityAndAcademicYear(String universityCode, String year, Pageable pageable) {
        return curriculumRepository.findByUniversityAndAcademicYear(universityCode, year, pageable).map(curriculumMapper::toDto);
    }

    public Page<CurriculumDto> findBySpecialtyAndAcademicYear(UUID specialtyId, String year, Pageable pageable) {
        return curriculumRepository.findBySpecialtyAndAcademicYear(specialtyId, year, pageable).map(curriculumMapper::toDto);
    }

    @Transactional
    public CurriculumDto update(UUID id, CurriculumDto dto) {
        log.debug("Updating curriculum: id={}", id);
        Curriculum existing = curriculumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found: " + id));
        validateForUpdate(id, dto);
        curriculumMapper.updateEntityFromDto(dto, existing);
        Curriculum updated = curriculumRepository.save(existing);
        log.info("Updated curriculum: id={}, code={}", updated.getId(), updated.getCode());
        return curriculumMapper.toDto(updated);
    }

    private void validateForUpdate(UUID id, CurriculumDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            if (curriculumRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
                errors.put("code", "Curriculum with code '" + dto.getCode() + "' already exists");
            }
        }
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }
        if (dto.getSpecialty() != null) {
            if (!specialtyRepository.existsById(dto.getSpecialty())) {
                errors.put("specialty", "Specialty with id '" + dto.getSpecialty() + "' not found");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Curriculum validation failed", errors);
        }
    }

    @Transactional
    public void softDelete(UUID id) {
        log.debug("Soft deleting curriculum: {}", id);
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found: " + id));
        curriculum.setDeleteTs(LocalDateTime.now());
        curriculumRepository.save(curriculum);
        log.info("Soft deleted curriculum: id={}, code={}", curriculum.getId(), curriculum.getCode());
    }

    public long countByUniversity(String universityCode) {
        return curriculumRepository.countByUniversity(universityCode);
    }

    public long countBySpecialty(UUID specialtyId) {
        return curriculumRepository.countBySpecialty(specialtyId);
    }

    public boolean existsByCode(String code) {
        return curriculumRepository.existsByCode(code);
    }
}
