package uz.hemis.service.university;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.dto.university.SpecialtyDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.academic.Specialty;
import uz.hemis.service.university.mapper.SpecialtyMapper;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Specialty business logic
 *
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;
    private final UniversityRepository universityRepository;

    @Audited(action = AuditAction.CREATE, entity = "Specialty", entityClass = Specialty.class)
    @Transactional
    public SpecialtyDto create(SpecialtyDto dto) {
        log.debug("Creating specialty: {}", dto);
        validateForCreate(dto);
        Specialty specialty = specialtyMapper.toEntity(dto);
        Specialty saved = specialtyRepository.save(specialty);
        log.info("Created specialty: id={}, code={}", saved.getId(), saved.getCode());
        return specialtyMapper.toDto(saved);
    }

    private void validateForCreate(SpecialtyDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            errors.put("code", "Specialty code is required");
        }
        if (dto.getCode() != null && specialtyRepository.existsByCode(dto.getCode())) {
            errors.put("code", "Specialty with code '" + dto.getCode() + "' already exists");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            errors.put("name", "Specialty name is required");
        }
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Specialty validation failed", errors);
        }
    }

    public SpecialtyDto findById(UUID id) {
        return specialtyRepository.findById(id)
                .map(specialtyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + id));
    }

    public SpecialtyDto findByCode(String code) {
        return specialtyRepository.findByCode(code)
                .map(specialtyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + code));
    }

    public Page<SpecialtyDto> findAll(Pageable pageable) {
        return specialtyRepository.findAll(pageable).map(specialtyMapper::toDto);
    }

    public Page<SpecialtyDto> findByUniversity(String universityCode, Pageable pageable) {
        return specialtyRepository.findByUniversity(universityCode, pageable).map(specialtyMapper::toDto);
    }

    public Page<SpecialtyDto> findByFaculty(String facultyCode, Pageable pageable) {
        return specialtyRepository.findByFaculty(facultyCode, pageable).map(specialtyMapper::toDto);
    }

    public List<SpecialtyDto> findAllByFaculty(String facultyCode) {
        return specialtyRepository.findAllByFaculty(facultyCode)
                .stream()
                .map(specialtyMapper::toDto)
                .collect(Collectors.toList());
    }

    public Page<SpecialtyDto> findByNameContaining(String name, Pageable pageable) {
        return specialtyRepository.findByNameContainingIgnoreCase(name, pageable).map(specialtyMapper::toDto);
    }

    public Page<SpecialtyDto> findActive(Pageable pageable) {
        return specialtyRepository.findByActiveTrue(pageable).map(specialtyMapper::toDto);
    }

    public Page<SpecialtyDto> findByEducationType(String typeCode, Pageable pageable) {
        return specialtyRepository.findByEducationType(typeCode, pageable).map(specialtyMapper::toDto);
    }

    public Page<SpecialtyDto> findByUniversityAndEducationType(String universityCode, String typeCode, Pageable pageable) {
        return specialtyRepository.findByUniversityAndEducationType(universityCode, typeCode, pageable).map(specialtyMapper::toDto);
    }

    @Audited(action = AuditAction.UPDATE, entity = "Specialty", entityClass = Specialty.class, keyArg = "id")
    @Transactional
    public SpecialtyDto update(UUID id, SpecialtyDto dto) {
        Specialty existing = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + id));
        specialtyMapper.updateEntityFromDto(dto, existing);
        Specialty updated = specialtyRepository.save(existing);
        return specialtyMapper.toDto(updated);
    }

    @Audited(action = AuditAction.DELETE, entity = "Specialty", entityClass = Specialty.class, keyArg = "id")
    @Transactional
    public void delete(UUID id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + id));
        specialtyRepository.delete(specialty);
        log.info("Deleted specialty: id={}, code={}", specialty.getId(), specialty.getCode());
    }

    public long countByUniversity(String universityCode) {
        return specialtyRepository.countByUniversity(universityCode);
    }

    public long countByFaculty(String facultyCode) {
        return specialtyRepository.countByFaculty(facultyCode);
    }

    public boolean existsByCode(String code) {
        return specialtyRepository.existsByCode(code);
    }

    public Object getByUniversity(String university, String type) {
        Page<SpecialtyDto> page = findByUniversity(university, Pageable.unpaged());
        return page.getContent();
    }
}
