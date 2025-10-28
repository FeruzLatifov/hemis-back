package uz.hemis.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.SpecialtyDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Specialty;
import uz.hemis.domain.mapper.SpecialtyMapper;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Specialty business logic
 *
 * <p><strong>CRUD Operations:</strong></p>
 * <ul>
 *   <li>CREATE: Validates code uniqueness, university/faculty existence</li>
 *   <li>READ: Single (by ID/code), paginated list, filtered queries</li>
 *   <li>UPDATE: Partial update support, validates code uniqueness (excluding self)</li>
 *   <li>DELETE: SOFT DELETE ONLY (sets deleteTs, NO physical DELETE)</li>
 * </ul>
 *
 * <p><strong>Validation Rules:</strong></p>
 * <ul>
 *   <li>code: Required, unique within system</li>
 *   <li>name: Required</li>
 *   <li>university: Must exist in hemishe_e_university if provided</li>
 *   <li>faculty: Must exist in hemishe_e_faculty if provided</li>
 *   <li>educationType: Optional ('11' = Bachelor, '12' = Master, '13' = PhD)</li>
 *   <li>educationForm: Optional ('11' = Full-time, '12' = Part-time, '13' = Evening, '14' = Distance)</li>
 * </ul>
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong></p>
 * <ul>
 *   <li>NO physical DELETE operations</li>
 *   <li>Soft delete ONLY via softDelete() method</li>
 *   <li>Deleted records remain in database with deleteTs timestamp</li>
 * </ul>
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
    private final FacultyRepository facultyRepository;

    // =====================================================
    // CREATE
    // =====================================================

    /**
     * Create new specialty
     *
     * <p><strong>Validations:</strong></p>
     * <ul>
     *   <li>code: Required, unique</li>
     *   <li>name: Required</li>
     *   <li>university: Must exist if provided</li>
     *   <li>faculty: Must exist if provided</li>
     * </ul>
     *
     * @param dto SpecialtyDto
     * @return Created SpecialtyDto with generated ID
     * @throws ValidationException if validation fails
     */
    @Transactional
    public SpecialtyDto create(SpecialtyDto dto) {
        log.debug("Creating specialty: {}", dto);

        // Validate
        validateForCreate(dto);

        // Map DTO → Entity
        Specialty specialty = specialtyMapper.toEntity(dto);

        // Save
        Specialty saved = specialtyRepository.save(specialty);
        log.info("Created specialty: id={}, code={}", saved.getId(), saved.getCode());

        // Map Entity → DTO
        return specialtyMapper.toDto(saved);
    }

    /**
     * Validate specialty for creation
     *
     * @param dto SpecialtyDto
     * @throws ValidationException if validation fails
     */
    private void validateForCreate(SpecialtyDto dto) {
        Map<String, String> errors = new HashMap<>();

        // Validation: code required
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            errors.put("code", "Specialty code is required");
        }

        // Validation: code uniqueness
        if (dto.getCode() != null && specialtyRepository.existsByCode(dto.getCode())) {
            errors.put("code", "Specialty with code '" + dto.getCode() + "' already exists");
        }

        // Validation: name required
        if (dto.getName() == null || dto.getName().isBlank()) {
            errors.put("name", "Specialty name is required");
        }

        // Validation: university exists (if provided)
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }

        // Validation: faculty exists (if provided)
        if (dto.getFaculty() != null) {
            if (!facultyRepository.existsById(dto.getFaculty())) {
                errors.put("faculty", "Faculty with id '" + dto.getFaculty() + "' not found");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Specialty validation failed", errors);
        }
    }

    // =====================================================
    // READ
    // =====================================================

    /**
     * Find specialty by ID
     *
     * @param id Specialty UUID
     * @return SpecialtyDto
     * @throws ResourceNotFoundException if not found
     */
    public SpecialtyDto findById(UUID id) {
        log.debug("Finding specialty by id: {}", id);

        return specialtyRepository.findById(id)
                .map(specialtyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + id));
    }

    /**
     * Find specialty by code
     *
     * @param code Specialty code
     * @return SpecialtyDto
     * @throws ResourceNotFoundException if not found
     */
    public SpecialtyDto findByCode(String code) {
        log.debug("Finding specialty by code: {}", code);

        return specialtyRepository.findByCode(code)
                .map(specialtyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + code));
    }

    /**
     * Find all specialties (paginated)
     *
     * @param pageable Pagination parameters
     * @return Page of SpecialtyDto
     */
    public Page<SpecialtyDto> findAll(Pageable pageable) {
        log.debug("Finding all specialties: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        return specialtyRepository.findAll(pageable)
                .map(specialtyMapper::toDto);
    }

    /**
     * Find specialties by university
     *
     * @param universityCode University code
     * @param pageable       Pagination parameters
     * @return Page of SpecialtyDto
     */
    public Page<SpecialtyDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding specialties by university: {}", universityCode);

        return specialtyRepository.findByUniversity(universityCode, pageable)
                .map(specialtyMapper::toDto);
    }

    /**
     * Find specialties by faculty
     *
     * @param facultyId Faculty UUID
     * @param pageable  Pagination parameters
     * @return Page of SpecialtyDto
     */
    public Page<SpecialtyDto> findByFaculty(UUID facultyId, Pageable pageable) {
        log.debug("Finding specialties by faculty: {}", facultyId);

        return specialtyRepository.findByFaculty(facultyId, pageable)
                .map(specialtyMapper::toDto);
    }

    /**
     * Find all specialties by faculty (no pagination)
     *
     * @param facultyId Faculty UUID
     * @return List of SpecialtyDto
     */
    public List<SpecialtyDto> findAllByFaculty(UUID facultyId) {
        log.debug("Finding all specialties by faculty (no pagination): {}", facultyId);

        return specialtyRepository.findAllByFaculty(facultyId)
                .stream()
                .map(specialtyMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find specialties by name (partial match, case-insensitive)
     *
     * @param name     Name to search
     * @param pageable Pagination parameters
     * @return Page of SpecialtyDto
     */
    public Page<SpecialtyDto> findByNameContaining(String name, Pageable pageable) {
        log.debug("Finding specialties by name containing: {}", name);

        return specialtyRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(specialtyMapper::toDto);
    }

    /**
     * Find active specialties
     *
     * @param pageable Pagination parameters
     * @return Page of SpecialtyDto
     */
    public Page<SpecialtyDto> findActive(Pageable pageable) {
        log.debug("Finding active specialties");

        return specialtyRepository.findByActiveTrue(pageable)
                .map(specialtyMapper::toDto);
    }

    /**
     * Find specialties by education type
     *
     * @param typeCode Education type code ('11' = Bachelor, '12' = Master, '13' = PhD)
     * @param pageable Pagination parameters
     * @return Page of SpecialtyDto
     */
    public Page<SpecialtyDto> findByEducationType(String typeCode, Pageable pageable) {
        log.debug("Finding specialties by education type: {}", typeCode);

        return specialtyRepository.findByEducationType(typeCode, pageable)
                .map(specialtyMapper::toDto);
    }

    /**
     * Find specialties by education form
     *
     * @param formCode Education form code ('11' = Full-time, '12' = Part-time, etc.)
     * @param pageable Pagination parameters
     * @return Page of SpecialtyDto
     */
    public Page<SpecialtyDto> findByEducationForm(String formCode, Pageable pageable) {
        log.debug("Finding specialties by education form: {}", formCode);

        return specialtyRepository.findByEducationForm(formCode, pageable)
                .map(specialtyMapper::toDto);
    }

    /**
     * Find specialties by university and education type
     *
     * @param universityCode University code
     * @param typeCode       Education type code
     * @param pageable       Pagination parameters
     * @return Page of SpecialtyDto
     */
    public Page<SpecialtyDto> findByUniversityAndEducationType(
            String universityCode,
            String typeCode,
            Pageable pageable
    ) {
        log.debug("Finding specialties by university={} and educationType={}", universityCode, typeCode);

        return specialtyRepository.findByUniversityAndEducationType(universityCode, typeCode, pageable)
                .map(specialtyMapper::toDto);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    /**
     * Update existing specialty
     *
     * <p>Partial update: Only non-null DTO fields are updated.</p>
     *
     * @param id  Specialty UUID
     * @param dto SpecialtyDto with updated values
     * @return Updated SpecialtyDto
     * @throws ResourceNotFoundException if not found
     * @throws ValidationException       if validation fails
     */
    @Transactional
    public SpecialtyDto update(UUID id, SpecialtyDto dto) {
        log.debug("Updating specialty: id={}", id);

        // Find existing
        Specialty existing = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + id));

        // Validate
        validateForUpdate(id, dto);

        // Apply updates (partial)
        specialtyMapper.updateEntityFromDto(dto, existing);

        // Save
        Specialty updated = specialtyRepository.save(existing);
        log.info("Updated specialty: id={}, code={}", updated.getId(), updated.getCode());

        return specialtyMapper.toDto(updated);
    }

    /**
     * Validate specialty for update
     *
     * @param id  Specialty ID being updated
     * @param dto SpecialtyDto
     * @throws ValidationException if validation fails
     */
    private void validateForUpdate(UUID id, SpecialtyDto dto) {
        Map<String, String> errors = new HashMap<>();

        // Validation: code uniqueness (excluding current record)
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            if (specialtyRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
                errors.put("code", "Specialty with code '" + dto.getCode() + "' already exists");
            }
        }

        // Validation: university exists (if provided)
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }

        // Validation: faculty exists (if provided)
        if (dto.getFaculty() != null) {
            if (!facultyRepository.existsById(dto.getFaculty())) {
                errors.put("faculty", "Faculty with id '" + dto.getFaculty() + "' not found");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Specialty validation failed", errors);
        }
    }

    // =====================================================
    // DELETE (SOFT DELETE ONLY)
    // =====================================================

    /**
     * Soft delete specialty (sets deleteTs, NO physical DELETE)
     *
     * <p><strong>CRITICAL - NO-DELETE Constraint:</strong></p>
     * <ul>
     *   <li>NO physical DELETE operation</li>
     *   <li>Sets deleteTs = current timestamp</li>
     *   <li>Record remains in database (hidden by @Where clause)</li>
     * </ul>
     *
     * @param id Specialty UUID
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public void softDelete(UUID id) {
        log.debug("Soft deleting specialty: {}", id);

        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found: " + id));

        // Soft delete: set deleteTs
        specialty.setDeleteTs(LocalDateTime.now());
        specialtyRepository.save(specialty);

        log.info("Soft deleted specialty: id={}, code={}", specialty.getId(), specialty.getCode());
    }

    // =====================================================
    // COUNT & EXISTENCE
    // =====================================================

    /**
     * Count specialties by university
     *
     * @param universityCode University code
     * @return Count of specialties
     */
    public long countByUniversity(String universityCode) {
        return specialtyRepository.countByUniversity(universityCode);
    }

    /**
     * Count specialties by faculty
     *
     * @param facultyId Faculty UUID
     * @return Count of specialties
     */
    public long countByFaculty(UUID facultyId) {
        return specialtyRepository.countByFaculty(facultyId);
    }

    /**
     * Check if specialty with code exists
     *
     * @param code Specialty code
     * @return true if exists
     */
    public boolean existsByCode(String code) {
        return specialtyRepository.existsByCode(code);
    }
}
