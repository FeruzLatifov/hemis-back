package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.StudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Student;
import uz.hemis.domain.mapper.StudentMapper;
import uz.hemis.domain.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Student Service - Business Logic Layer
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Business logic and validation</li>
 *   <li>Transaction management</li>
 *   <li>Entity ↔ DTO conversion (via StudentMapper)</li>
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    // =====================================================
    // Read Operations (Read-Only Transactions)
    // =====================================================

    /**
     * Find student by ID
     *
     * @param id student ID
     * @return student DTO
     * @throws ResourceNotFoundException if not found
     */
    @Cacheable(value = "students", key = "#id", unless = "#result == null")
    public StudentDto findById(UUID id) {
        log.debug("Finding student by ID: {} (cache miss)", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        return studentMapper.toDto(student);
    }

    /**
     * Find MASTER student by PINFL
     *
     * <p><strong>CRITICAL:</strong> Returns only master record (isDuplicate = true)</p>
     * <p>This matches old-HEMIS behavior for student lookup by PINFL</p>
     *
     * @param pinfl personal identification number
     * @return master student DTO
     * @throws ResourceNotFoundException if master record not found
     */
    @Cacheable(value = "students", key = "'pinfl:' + #pinfl", unless = "#result == null")
    public StudentDto findByPinfl(String pinfl) {
        log.debug("Finding master student by PINFL: {} (cache miss)", pinfl);

        Student student = studentRepository.findMasterByPinfl(pinfl)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "pinfl", pinfl));

        return studentMapper.toDto(student);
    }

    /**
     * Find ALL students with same PINFL (including duplicates)
     *
     * <p><strong>Use case:</strong> Finding transfer history, duplicate detection</p>
     *
     * @param pinfl personal identification number
     * @return list of all students with this PINFL (master + duplicates)
     */
    public List<StudentDto> findAllByPinfl(String pinfl) {
        log.debug("Finding all students (including duplicates) by PINFL: {}", pinfl);

        List<Student> students = studentRepository.findAllByPinfl(pinfl);
        return studentMapper.toDtoList(students);
    }

    /**
     * Find all students (paginated)
     *
     * @param pageable pagination parameters
     * @return page of student DTOs
     */
    public Page<StudentDto> findAll(Pageable pageable) {
        log.debug("Finding all students with pagination: {}", pageable);

        Page<Student> students = studentRepository.findAll(pageable);
        return students.map(studentMapper::toDto);
    }

    /**
     * Find students by university code
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return page of student DTOs
     */
    public Page<StudentDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding students by university: {}", universityCode);

        Page<Student> students = studentRepository.findByUniversity(universityCode, pageable);
        return students.map(studentMapper::toDto);
    }

    /**
     * Find active students by university
     *
     * @param universityCode university code
     * @return list of student DTOs
     */
    public List<StudentDto> findActiveByUniversity(String universityCode) {
        log.debug("Finding active students by university: {}", universityCode);

        List<Student> students = studentRepository.findActiveByUniversity(universityCode);
        return studentMapper.toDtoList(students);
    }

    /**
     * Count active students by university
     *
     * @param universityCode university code
     * @return count
     */
    public long countActiveByUniversity(String universityCode) {
        log.debug("Counting active students for university: {}", universityCode);

        return studentRepository.countActiveByUniversity(universityCode);
    }

    /**
     * Check if MASTER student exists for PINFL
     *
     * <p><strong>CRITICAL:</strong> Checks only for master record (isDuplicate = true)</p>
     *
     * @param pinfl personal identification number
     * @return true if master record exists
     */
    public boolean existsByPinfl(String pinfl) {
        return studentRepository.existsMasterByPinfl(pinfl);
    }

    // =====================================================
    // Write Operations (Read-Write Transactions)
    // =====================================================

    /**
     * Create new student with old-HEMIS duplicate detection logic
     *
     * <p><strong>CRITICAL - Duplicate Detection (old-HEMIS compatible):</strong></p>
     * <ul>
     *   <li>PINFL is NOT UNIQUE! Multiple students can have same PINFL</li>
     *   <li>isDuplicate flag manages master vs duplicate records</li>
     *   <li>Only ONE student per PINFL can have isDuplicate=true (master)</li>
     *   <li>If master exists, returns existing master (old-HEMIS behavior)</li>
     *   <li>If no master exists, creates new master with isDuplicate=true</li>
     * </ul>
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>Code must be unique (not PINFL!)</li>
     *   <li>Required fields must be present</li>
     * </ul>
     *
     * @param studentDto student data
     * @return created student DTO (or existing master if PINFL duplicate)
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "students", key = "#result.id")
    public StudentDto create(StudentDto studentDto) {
        log.info("Creating new student with PINFL: {}", studentDto.getPinfl());

        // =====================================================
        // STEP 1: Duplicate Detection (Old-HEMIS Logic)
        // =====================================================
        // If PINFL provided, check for existing MASTER record
        // If master exists, return it (old-HEMIS compatibility)
        // =====================================================
        if (studentDto.getPinfl() != null && !studentDto.getPinfl().isEmpty()) {
            var existingMaster = studentRepository.findMasterByPinfl(studentDto.getPinfl());

            if (existingMaster.isPresent()) {
                log.warn("Master student already exists for PINFL: {}. Returning existing master (old-HEMIS behavior)",
                        studentDto.getPinfl());
                log.warn("Existing master ID: {}, Code: {}",
                        existingMaster.get().getId(),
                        existingMaster.get().getCode());

                // Return existing master (old-HEMIS behavior)
                // Alternative: Throw exception if you want to enforce uniqueness
                return studentMapper.toDto(existingMaster.get());
            }
        }

        // =====================================================
        // STEP 2: Validate Business Key (CODE is unique)
        // =====================================================
        // Note: CODE is the true unique identifier, NOT PINFL!
        // =====================================================
        if (studentDto.getCode() != null && studentRepository.findByCode(studentDto.getCode()).isPresent()) {
            throw new ValidationException(
                    "Student with this CODE already exists",
                    "code",
                    "Student code must be unique"
            );
        }

        // =====================================================
        // STEP 3: Create New Master Student
        // =====================================================
        // Convert DTO to Entity
        Student student = studentMapper.toEntity(studentDto);

        // Mark as MASTER record (critical for duplicate detection)
        student.setIsDuplicate(true);

        log.info("Creating NEW master student for PINFL: {}", studentDto.getPinfl());

        // ID will be generated by @PrePersist
        // Audit fields (createTs, createdBy) set by @PrePersist

        // Save
        Student saved = studentRepository.save(student);

        log.info("Master student created successfully - ID: {}, Code: {}, PINFL: {}",
                saved.getId(),
                saved.getCode(),
                saved.getPinfl());

        return studentMapper.toDto(saved);
    }

    /**
     * Update existing student
     *
     * <p><strong>Business Validations:</strong></p>
     * <ul>
     *   <li>Student must exist</li>
     *   <li>Code uniqueness (if changed) - Code is true unique identifier</li>
     *   <li>PINFL is NOT validated for uniqueness (old-HEMIS compatibility)</li>
     * </ul>
     *
     * <p><strong>CRITICAL - isDuplicate Preservation:</strong></p>
     * <ul>
     *   <li>isDuplicate flag is NOT updated via this method</li>
     *   <li>Use dedicated method for master/duplicate management</li>
     * </ul>
     *
     * @param id student ID
     * @param studentDto student data
     * @return updated student DTO
     * @throws ResourceNotFoundException if student not found
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "students", key = "#id")
    @CacheEvict(value = "students", key = "'pinfl:' + #result.pinfl")
    public StudentDto update(UUID id, StudentDto studentDto) {
        log.info("Updating student with ID: {}", id);

        // Find existing student
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Validate CODE uniqueness (if changed)
        // CODE is the true unique identifier, NOT PINFL!
        if (studentDto.getCode() != null &&
                !studentDto.getCode().equals(existing.getCode())) {
            var existingByCode = studentRepository.findByCode(studentDto.getCode());
            if (existingByCode.isPresent() && !existingByCode.get().getId().equals(id)) {
                throw new ValidationException(
                        "Student with this CODE already exists",
                        "code",
                        "Student code must be unique"
                );
            }
        }

        // CRITICAL: Do NOT validate PINFL uniqueness!
        // Old-HEMIS allows duplicate PINFLs (managed via isDuplicate flag)

        // Update entity from DTO (ignores audit fields)
        studentMapper.updateEntityFromDto(studentDto, existing);

        // updateTs and updatedBy will be set by @PreUpdate

        // Save
        Student updated = studentRepository.save(existing);

        log.info("Student updated successfully: {}", id);

        return studentMapper.toDto(updated);
    }

    /**
     * Partial update (PATCH)
     *
     * <p>Only non-null fields in DTO are updated</p>
     *
     * @param id student ID
     * @param studentDto partial student data
     * @return updated student DTO
     * @throws ResourceNotFoundException if student not found
     */
    @Transactional
    public StudentDto partialUpdate(UUID id, StudentDto studentDto) {
        log.info("Partial update for student ID: {}", id);

        // Find existing student
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Partial update (null values ignored)
        studentMapper.partialUpdate(studentDto, existing);

        // Save
        Student updated = studentRepository.save(existing);

        log.info("Student partially updated: {}", id);

        return studentMapper.toDto(updated);
    }

    // =====================================================
    // Soft Delete (NOT Physical DELETE)
    // =====================================================

    /**
     * Soft delete student
     *
     * <p><strong>CRITICAL - Soft Delete Only:</strong></p>
     * <ul>
     *   <li>Sets deleteTs = NOW()</li>
     *   <li>Sets deletedBy = current user</li>
     *   <li>NO physical DELETE from database</li>
     *   <li>Student still exists but filtered by @Where clause</li>
     * </ul>
     *
     * @param id student ID
     * @throws ResourceNotFoundException if student not found
     */
    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public void softDelete(UUID id) {
        log.warn("Soft deleting student ID: {}", id);

        // Find existing student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Check if already deleted
        if (student.isDeleted()) {
            log.warn("Student already deleted: {}", id);
            return;
        }

        // Set soft delete fields
        student.setDeleteTs(LocalDateTime.now());
        // TODO: Set deletedBy from SecurityContext
        // student.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save (this triggers @PreUpdate)
        studentRepository.save(student);

        log.warn("Student soft deleted: {}", id);
    }

    /**
     * Restore soft-deleted student
     *
     * <p>Clears deleteTs and deletedBy fields</p>
     *
     * @param id student ID
     * @throws ResourceNotFoundException if student not found
     */
    @Transactional
    public void restore(UUID id) {
        log.info("Restoring soft-deleted student ID: {}", id);

        // Note: Need to find including deleted records
        // This requires a custom query or removing @Where temporarily
        // For now, assume we can access deleted records via special method

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (!student.isDeleted()) {
            log.warn("Student is not deleted, nothing to restore: {}", id);
            return;
        }

        // Clear soft delete fields
        student.setDeleteTs(null);
        student.setDeletedBy(null);

        // Save
        studentRepository.save(student);

        log.info("Student restored: {}", id);
    }

    // =====================================================
    // NOTE: NO PHYSICAL DELETE METHOD
    // =====================================================
    // public void delete(UUID id) { ... }  ← PROHIBITED
    //
    // Physical DELETE is not allowed (NDG).
    // Use softDelete() instead.
    // =====================================================
}
