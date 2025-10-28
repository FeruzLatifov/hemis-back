package uz.hemis.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.FacultyDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Faculty;
import uz.hemis.domain.mapper.FacultyMapper;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Faculty business logic
 *
 * <p><strong>CRUD Operations:</strong></p>
 * <ul>
 *   <li>CREATE: Validates code uniqueness, university existence</li>
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
 *   <li>facultyType: Optional classifier reference</li>
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
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final FacultyMapper facultyMapper;
    private final UniversityRepository universityRepository;

    // =====================================================
    // CREATE
    // =====================================================

    /**
     * Create new faculty
     *
     * <p><strong>Validations:</strong></p>
     * <ul>
     *   <li>code: Required, unique</li>
     *   <li>name: Required</li>
     *   <li>university: Must exist if provided</li>
     * </ul>
     *
     * @param dto FacultyDto
     * @return Created FacultyDto with generated ID
     * @throws ValidationException if validation fails
     */
    @Transactional
    public FacultyDto create(FacultyDto dto) {
        log.debug("Creating faculty: {}", dto);

        // Validate
        validateForCreate(dto);

        // Map DTO → Entity
        Faculty faculty = facultyMapper.toEntity(dto);

        // Save
        Faculty saved = facultyRepository.save(faculty);
        log.info("Created faculty: id={}, code={}", saved.getId(), saved.getCode());

        // Map Entity → DTO
        return facultyMapper.toDto(saved);
    }

    /**
     * Validate faculty for creation
     *
     * @param dto FacultyDto
     * @throws ValidationException if validation fails
     */
    private void validateForCreate(FacultyDto dto) {
        Map<String, String> errors = new HashMap<>();

        // Validation: code required
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            errors.put("code", "Faculty code is required");
        }

        // Validation: code uniqueness
        if (dto.getCode() != null && facultyRepository.existsByCode(dto.getCode())) {
            errors.put("code", "Faculty with code '" + dto.getCode() + "' already exists");
        }

        // Validation: name required
        if (dto.getName() == null || dto.getName().isBlank()) {
            errors.put("name", "Faculty name is required");
        }

        // Validation: university exists (if provided)
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Faculty validation failed", errors);
        }
    }

    // =====================================================
    // READ
    // =====================================================

    /**
     * Find faculty by ID
     *
     * @param id Faculty UUID
     * @return FacultyDto
     * @throws ResourceNotFoundException if not found
     */
    public FacultyDto findById(UUID id) {
        log.debug("Finding faculty by id: {}", id);

        return facultyRepository.findById(id)
                .map(facultyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + id));
    }

    /**
     * Find faculty by code
     *
     * @param code Faculty code
     * @return FacultyDto
     * @throws ResourceNotFoundException if not found
     */
    public FacultyDto findByCode(String code) {
        log.debug("Finding faculty by code: {}", code);

        return facultyRepository.findByCode(code)
                .map(facultyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + code));
    }

    /**
     * Find all faculties (paginated)
     *
     * @param pageable Pagination parameters
     * @return Page of FacultyDto
     */
    public Page<FacultyDto> findAll(Pageable pageable) {
        log.debug("Finding all faculties: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        return facultyRepository.findAll(pageable)
                .map(facultyMapper::toDto);
    }

    /**
     * Find faculties by university
     *
     * @param universityCode University code
     * @param pageable       Pagination parameters
     * @return Page of FacultyDto
     */
    public Page<FacultyDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding faculties by university: {}", universityCode);

        return facultyRepository.findByUniversity(universityCode, pageable)
                .map(facultyMapper::toDto);
    }

    /**
     * Find all faculties by university (no pagination)
     *
     * @param universityCode University code
     * @return List of FacultyDto
     */
    public List<FacultyDto> findAllByUniversity(String universityCode) {
        log.debug("Finding all faculties by university (no pagination): {}", universityCode);

        return facultyRepository.findAllByUniversity(universityCode)
                .stream()
                .map(facultyMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find faculties by name (partial match, case-insensitive)
     *
     * @param name     Name to search
     * @param pageable Pagination parameters
     * @return Page of FacultyDto
     */
    public Page<FacultyDto> findByNameContaining(String name, Pageable pageable) {
        log.debug("Finding faculties by name containing: {}", name);

        return facultyRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(facultyMapper::toDto);
    }

    /**
     * Find active faculties
     *
     * @param pageable Pagination parameters
     * @return Page of FacultyDto
     */
    public Page<FacultyDto> findActive(Pageable pageable) {
        log.debug("Finding active faculties");

        return facultyRepository.findByActiveTrue(pageable)
                .map(facultyMapper::toDto);
    }

    /**
     * Find faculties by type
     *
     * @param typeCode Faculty type code
     * @param pageable Pagination parameters
     * @return Page of FacultyDto
     */
    public Page<FacultyDto> findByType(String typeCode, Pageable pageable) {
        log.debug("Finding faculties by type: {}", typeCode);

        return facultyRepository.findByFacultyType(typeCode, pageable)
                .map(facultyMapper::toDto);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    /**
     * Update existing faculty
     *
     * <p>Partial update: Only non-null DTO fields are updated.</p>
     *
     * @param id  Faculty UUID
     * @param dto FacultyDto with updated values
     * @return Updated FacultyDto
     * @throws ResourceNotFoundException if not found
     * @throws ValidationException       if validation fails
     */
    @Transactional
    public FacultyDto update(UUID id, FacultyDto dto) {
        log.debug("Updating faculty: id={}", id);

        // Find existing
        Faculty existing = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + id));

        // Validate
        validateForUpdate(id, dto);

        // Apply updates (partial)
        facultyMapper.updateEntityFromDto(dto, existing);

        // Save
        Faculty updated = facultyRepository.save(existing);
        log.info("Updated faculty: id={}, code={}", updated.getId(), updated.getCode());

        return facultyMapper.toDto(updated);
    }

    /**
     * Validate faculty for update
     *
     * @param id  Faculty ID being updated
     * @param dto FacultyDto
     * @throws ValidationException if validation fails
     */
    private void validateForUpdate(UUID id, FacultyDto dto) {
        Map<String, String> errors = new HashMap<>();

        // Validation: code uniqueness (excluding current record)
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            if (facultyRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
                errors.put("code", "Faculty with code '" + dto.getCode() + "' already exists");
            }
        }

        // Validation: university exists (if provided)
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Faculty validation failed", errors);
        }
    }

    // =====================================================
    // DELETE (SOFT DELETE ONLY)
    // =====================================================

    /**
     * Soft delete faculty (sets deleteTs, NO physical DELETE)
     *
     * <p><strong>CRITICAL - NO-DELETE Constraint:</strong></p>
     * <ul>
     *   <li>NO physical DELETE operation</li>
     *   <li>Sets deleteTs = current timestamp</li>
     *   <li>Record remains in database (hidden by @Where clause)</li>
     * </ul>
     *
     * @param id Faculty UUID
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public void softDelete(UUID id) {
        log.debug("Soft deleting faculty: {}", id);

        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + id));

        // Soft delete: set deleteTs
        faculty.setDeleteTs(LocalDateTime.now());
        facultyRepository.save(faculty);

        log.info("Soft deleted faculty: id={}, code={}", faculty.getId(), faculty.getCode());
    }

    // =====================================================
    // COUNT & EXISTENCE
    // =====================================================

    /**
     * Count faculties by university
     *
     * @param universityCode University code
     * @return Count of faculties
     */
    public long countByUniversity(String universityCode) {
        return facultyRepository.countByUniversity(universityCode);
    }

    /**
     * Check if faculty with code exists
     *
     * @param code Faculty code
     * @return true if exists
     */
    public boolean existsByCode(String code) {
        return facultyRepository.existsByCode(code);
    }
}
