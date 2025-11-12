package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.TeacherDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Teacher;
import uz.hemis.service.mapper.TeacherMapper;
import uz.hemis.domain.repository.TeacherRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Teacher Service - Business Logic Layer
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Business logic and validation</li>
 *   <li>Transaction management</li>
 *   <li>Entity ↔ DTO conversion (via TeacherMapper)</li>
 *   <li>Soft delete implementation</li>
 * </ul>
 *
 * <p><strong>CRITICAL - NO DELETE OPERATIONS:</strong></p>
 * <ul>
 *   <li>NDG (Non-Deletion Guarantee) enforced</li>
 *   <li>Soft delete only (set deleteTs)</li>
 *   <li>NO physical DELETE from database</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final UniversityRepository universityRepository;

    // =====================================================
    // Read Operations (Read-Only Transactions)
    // =====================================================

    /**
     * Find teacher by ID
     *
     * @param id teacher ID
     * @return teacher DTO
     * @throws ResourceNotFoundException if not found
     */
    public TeacherDto findById(UUID id) {
        log.debug("Finding teacher by ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        return teacherMapper.toDto(teacher);
    }

    /**
     * Find all teachers (paginated)
     *
     * @param pageable pagination parameters
     * @return page of teacher DTOs
     */
    public Page<TeacherDto> findAll(Pageable pageable) {
        log.debug("Finding all teachers with pagination: {}", pageable);

        Page<Teacher> teachers = teacherRepository.findAll(pageable);
        return teachers.map(teacherMapper::toDto);
    }

    /**
     * Find teachers by university code
     *
     * @param universityCode university code
     * @param pageable pagination
     * @return page of teacher DTOs
     */
    public Page<TeacherDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding teachers by university: {}", universityCode);

        Page<Teacher> teachers = teacherRepository.findByUniversity(universityCode, pageable);
        return teachers.map(teacherMapper::toDto);
    }

    /**
     * Find all teachers by university (no pagination)
     *
     * @param universityCode university code
     * @return list of teacher DTOs
     */
    public List<TeacherDto> findAllByUniversity(String universityCode) {
        log.debug("Finding all teachers by university: {}", universityCode);

        List<Teacher> teachers = teacherRepository.findAllByUniversity(universityCode);
        return teacherMapper.toDtoList(teachers);
    }

    /**
     * Find teachers by lastname (partial match)
     *
     * @param lastname last name (partial)
     * @param pageable pagination
     * @return page of teacher DTOs
     */
    public Page<TeacherDto> findByLastname(String lastname, Pageable pageable) {
        log.debug("Finding teachers by lastname: {}", lastname);

        Page<Teacher> teachers = teacherRepository.findByLastnameContainingIgnoreCase(lastname, pageable);
        return teachers.map(teacherMapper::toDto);
    }

    /**
     * Find teachers by full name (partial match on any name field)
     *
     * @param name search term
     * @param pageable pagination
     * @return page of teacher DTOs
     */
    public Page<TeacherDto> findByName(String name, Pageable pageable) {
        log.debug("Finding teachers by name: {}", name);

        Page<Teacher> teachers = teacherRepository.findByNameContainingIgnoreCase(name, pageable);
        return teachers.map(teacherMapper::toDto);
    }

    /**
     * Find teachers by academic degree
     *
     * @param degreeCode academic degree code
     * @param pageable pagination
     * @return page of teacher DTOs
     */
    public Page<TeacherDto> findByAcademicDegree(String degreeCode, Pageable pageable) {
        log.debug("Finding teachers by academic degree: {}", degreeCode);

        Page<Teacher> teachers = teacherRepository.findByAcademicDegree(degreeCode, pageable);
        return teachers.map(teacherMapper::toDto);
    }

    /**
     * Find teachers by academic rank
     *
     * @param rankCode academic rank code
     * @param pageable pagination
     * @return page of teacher DTOs
     */
    public Page<TeacherDto> findByAcademicRank(String rankCode, Pageable pageable) {
        log.debug("Finding teachers by academic rank: {}", rankCode);

        Page<Teacher> teachers = teacherRepository.findByAcademicRank(rankCode, pageable);
        return teachers.map(teacherMapper::toDto);
    }

    /**
     * Find professors by university
     *
     * @param universityCode university code
     * @param pageable pagination
     * @return page of professor DTOs
     */
    public Page<TeacherDto> findProfessorsByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding professors by university: {}", universityCode);

        Page<Teacher> professors = teacherRepository.findProfessorsByUniversity(universityCode, pageable);
        return professors.map(teacherMapper::toDto);
    }

    /**
     * Count teachers by university
     *
     * @param universityCode university code
     * @return count of teachers
     */
    public long countByUniversity(String universityCode) {
        return teacherRepository.countByUniversity(universityCode);
    }

    /**
     * Count professors by university
     *
     * @param universityCode university code
     * @return count of professors
     */
    public long countProfessorsByUniversity(String universityCode) {
        return teacherRepository.countProfessorsByUniversity(universityCode);
    }

    /**
     * Find teacher by PINFL and University (composite key)
     *
     * <p><strong>CRITICAL:</strong> This respects composite uniqueness.</p>
     * <p>Same PINFL can exist at different universities.</p>
     *
     * @param pinfl personal identification number
     * @param universityCode university code
     * @return teacher DTO if found
     * @throws ResourceNotFoundException if not found
     */
    public TeacherDto findByPinflAndUniversity(String pinfl, String universityCode) {
        log.debug("Finding teacher by PINFL: {} and University: {}", pinfl, universityCode);

        Teacher teacher = teacherRepository.findByPinflAndUniversity(pinfl, universityCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teacher",
                        "pinfl + university",
                        pinfl + " at " + universityCode
                ));

        return teacherMapper.toDto(teacher);
    }

    /**
     * Find ALL teachers with same PINFL (across all universities)
     *
     * <p><strong>Use case:</strong> View teaching history across universities</p>
     *
     * @param pinfl personal identification number
     * @return list of teacher DTOs
     */
    public List<TeacherDto> findAllByPinfl(String pinfl) {
        log.debug("Finding all teachers (all universities) by PINFL: {}", pinfl);

        List<Teacher> teachers = teacherRepository.findAllByPinfl(pinfl);
        return teacherMapper.toDtoList(teachers);
    }

    // =====================================================
    // Write Operations (Transactional)
    // =====================================================

    /**
     * Create new teacher with composite PINFL + University validation
     *
     * <p><strong>CRITICAL - Composite Uniqueness (old-HEMIS compatible):</strong></p>
     * <ul>
     *   <li>Teacher has composite unique constraint: (PINFL, University)</li>
     *   <li>Same person (PINFL) CAN teach at MULTIPLE universities</li>
     *   <li>But CANNOT have duplicate records at SAME university</li>
     *   <li>PINFL alone is NOT unique globally</li>
     * </ul>
     *
     * @param dto teacher DTO
     * @return created teacher DTO
     * @throws ValidationException if validation fails
     */
    @Transactional
    public TeacherDto create(TeacherDto dto) {
        log.info("Creating new teacher: {} {} at university: {}",
                dto.getFirstname(), dto.getLastname(), dto.getUniversity());

        // Validation: Basic fields required
        Map<String, String> errors = new HashMap<>();

        if (dto.getFirstname() == null || dto.getFirstname().isBlank()) {
            errors.put("firstname", "First name is required");
        }
        if (dto.getLastname() == null || dto.getLastname().isBlank()) {
            errors.put("lastname", "Last name is required");
        }

        // Validation: University exists (if provided)
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }

        // =====================================================
        // CRITICAL: Composite Unique Validation (PINFL + University)
        // =====================================================
        // Check if teacher with same PINFL already exists at THIS university
        // Note: Same PINFL at DIFFERENT university is allowed!
        // =====================================================
        if (dto.getPinfl() != null && !dto.getPinfl().isBlank() &&
            dto.getUniversity() != null && !dto.getUniversity().isBlank()) {

            if (teacherRepository.existsByPinflAndUniversity(dto.getPinfl(), dto.getUniversity())) {
                errors.put("pinfl", "Teacher with this PINFL already exists at university '" +
                        dto.getUniversity() + "'");

                // Log for clarity (same person may teach at other universities)
                long universityCount = teacherRepository.countUniversitiesByPinfl(dto.getPinfl());
                log.warn("Teacher with PINFL {} already exists at {} universities, including {}",
                        dto.getPinfl(), universityCount, dto.getUniversity());
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Teacher validation failed", errors);
        }

        // Convert DTO to entity
        Teacher teacher = teacherMapper.toEntity(dto);

        // Save
        Teacher saved = teacherRepository.save(teacher);

        log.info("Teacher created successfully with ID: {} (PINFL: {}, University: {})",
                saved.getId(), saved.getPinfl(), saved.getUniversity());
        return teacherMapper.toDto(saved);
    }

    /**
     * Update existing teacher with composite uniqueness validation
     *
     * <p><strong>Composite Uniqueness Check:</strong></p>
     * <ul>
     *   <li>If PINFL or University changes, validate composite constraint</li>
     *   <li>Same person CAN teach at different universities</li>
     *   <li>But CANNOT have duplicates at same university</li>
     * </ul>
     *
     * @param id teacher ID
     * @param dto teacher DTO with updated values
     * @return updated teacher DTO
     * @throws ResourceNotFoundException if not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    public TeacherDto update(UUID id, TeacherDto dto) {
        log.info("Updating teacher with ID: {}", id);

        // Find existing
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        Map<String, String> errors = new HashMap<>();

        // Validation: University exists (if provided and changed)
        if (dto.getUniversity() != null && !dto.getUniversity().equals(existing.getUniversity())) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }

        // =====================================================
        // CRITICAL: Composite Unique Validation (PINFL + University)
        // =====================================================
        // If PINFL or University changes, check composite constraint
        // =====================================================
        String newPinfl = dto.getPinfl() != null ? dto.getPinfl() : existing.getPinfl();
        String newUniversity = dto.getUniversity() != null ? dto.getUniversity() : existing.getUniversity();

        boolean pinflChanged = dto.getPinfl() != null && !dto.getPinfl().equals(existing.getPinfl());
        boolean universityChanged = dto.getUniversity() != null && !dto.getUniversity().equals(existing.getUniversity());

        if ((pinflChanged || universityChanged) && newPinfl != null && newUniversity != null) {
            // Check if another teacher exists with this PINFL at this university
            var existingTeacher = teacherRepository.findByPinflAndUniversity(newPinfl, newUniversity);

            if (existingTeacher.isPresent() && !existingTeacher.get().getId().equals(id)) {
                errors.put("pinfl", "Teacher with this PINFL already exists at university '" +
                        newUniversity + "'");
                log.warn("Composite constraint violation: PINFL {} already exists at university {}",
                        newPinfl, newUniversity);
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Teacher validation failed", errors);
        }

        // Update fields
        if (dto.getFirstname() != null) existing.setFirstname(dto.getFirstname());
        if (dto.getLastname() != null) existing.setLastname(dto.getLastname());
        if (dto.getFathername() != null) existing.setFathername(dto.getFathername());
        if (dto.getBirthday() != null) existing.setBirthday(dto.getBirthday());
        if (dto.getGender() != null) existing.setGender(dto.getGender());
        if (dto.getPinfl() != null) existing.setPinfl(dto.getPinfl());
        if (dto.getUniversity() != null) existing.setUniversity(dto.getUniversity());
        if (dto.getAcademicDegree() != null) existing.setAcademicDegree(dto.getAcademicDegree());
        if (dto.getAcademicRank() != null) existing.setAcademicRank(dto.getAcademicRank());

        // Save
        Teacher updated = teacherRepository.save(existing);

        log.info("Teacher updated successfully: {}", id);
        return teacherMapper.toDto(updated);
    }

    /**
     * Partial update (PATCH) - only update non-null fields
     *
     * @param id teacher ID
     * @param dto teacher DTO with fields to update
     * @return updated teacher DTO
     * @throws ResourceNotFoundException if not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    public TeacherDto partialUpdate(UUID id, TeacherDto dto) {
        log.info("Partially updating teacher with ID: {}", id);

        // Find existing
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        // Validation: University exists (if provided and changed)
        if (dto.getUniversity() != null && !dto.getUniversity().equals(existing.getUniversity())) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                Map<String, String> errors = new HashMap<>();
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
                throw new ValidationException("Teacher validation failed", errors);
            }
        }

        // Use mapper for partial update (only non-null fields)
        teacherMapper.updateEntityFromDto(dto, existing);

        // Save
        Teacher updated = teacherRepository.save(existing);

        log.info("Teacher partially updated successfully: {}", id);
        return teacherMapper.toDto(updated);
    }

    // =====================================================
    // Soft Delete (NO Physical DELETE)
    // =====================================================

    /**
     * Soft delete teacher (set deleteTs)
     *
     * <p><strong>CRITICAL - NDG:</strong> Physical DELETE is PROHIBITED!</p>
     * <p>This method only sets deleteTs timestamp.</p>
     *
     * @param id teacher ID
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public void softDelete(UUID id) {
        log.info("Soft deleting teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        // Set soft delete timestamp (via BaseEntity)
        teacher.setDeleteTs(LocalDateTime.now());
        // TODO: Set deletedBy from SecurityContext

        teacherRepository.save(teacher);

        log.info("Teacher soft deleted successfully: {}", id);
    }

    /**
     * Restore soft-deleted teacher (clear deleteTs)
     *
     * @param id teacher ID
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public void restore(UUID id) {
        log.info("Restoring teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        // Clear soft delete fields (via BaseEntity)
        teacher.setDeleteTs(null);
        teacher.setDeletedBy(null);

        teacherRepository.save(teacher);

        log.info("Teacher restored successfully: {}", id);
    }
}
