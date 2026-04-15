package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.student.StudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Student;
import uz.hemis.service.student.mapper.StudentMapper;
import uz.hemis.domain.repository.StudentRepository;

import uz.hemis.common.audit.Audited;
import uz.hemis.common.audit.AuditAction;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Student Core Service - CRUD and basic lookup operations
 *
 * <p>Extracted from StudentService as part of service decomposition.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentCoreService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    // =====================================================
    // Read Operations (Read-Only Transactions)
    // =====================================================

    @Cacheable(value = "students", key = "#id", unless = "#result == null")
    public StudentDto findById(UUID id) {
        log.debug("Finding student by ID: {} (cache miss)", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        return studentMapper.toDto(student);
    }

    @Cacheable(value = "students", key = "'pinfl:' + #pinfl", unless = "#result == null")
    public StudentDto findByPinfl(String pinfl) {
        log.debug("Finding master student by PINFL: {} (cache miss)", pinfl);

        Student student = studentRepository.findMasterByPinfl(pinfl)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "pinfl", pinfl));

        return studentMapper.toDto(student);
    }

    public List<StudentDto> findAllByPinfl(String pinfl) {
        log.debug("Finding all students (including duplicates) by PINFL: {}", pinfl);

        List<Student> students = studentRepository.findAllByPinfl(pinfl);
        return studentMapper.toDtoList(students);
    }

    public Page<StudentDto> findAll(Pageable pageable) {
        log.debug("Finding all students with pagination: {}", pageable);

        Page<Student> students = studentRepository.findAll(pageable);
        return students.map(studentMapper::toDto);
    }

    public Page<Student> findAllEntities(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Page<StudentDto> findByUniversity(String universityCode, Pageable pageable) {
        log.debug("Finding students by university: {}", universityCode);

        Page<Student> students = studentRepository.findByUniversity(universityCode, pageable);
        return students.map(studentMapper::toDto);
    }

    public List<StudentDto> findActiveByUniversity(String universityCode) {
        log.debug("Finding active students by university: {}", universityCode);

        List<Student> students = studentRepository.findActiveByUniversity(universityCode);
        return studentMapper.toDtoList(students);
    }

    public long countActiveByUniversity(String universityCode) {
        log.debug("Counting active students for university: {}", universityCode);

        return studentRepository.countActiveByUniversity(universityCode);
    }

    public boolean existsByPinfl(String pinfl) {
        return studentRepository.existsMasterByPinfl(pinfl);
    }

    public Optional<Student> findEntityById(UUID id) {
        return studentRepository.findById(id);
    }

    // =====================================================
    // Write Operations (Read-Write Transactions)
    // =====================================================

    @Audited(action = AuditAction.CREATE, entity = "Student", entityClass = Student.class)
    @Transactional
    @CachePut(value = "students", key = "#result.id")
    public StudentDto create(StudentDto studentDto) {
        log.info("Creating new student (OLD-HEMIS compatible) - PINFL: {}, University: {}, EducationType: {}, Year: {}",
                studentDto.getPinfl(), studentDto.getUniversity(), studentDto.getEducationType(), studentDto.getEducationYear());

        // STEP 1: Check for existing MASTER (isDuplicate=TRUE)
        if (studentDto.getPinfl() != null && !studentDto.getPinfl().isEmpty()) {
            var existingMaster = studentRepository.findActiveMasterByPinfl(studentDto.getPinfl());

            if (existingMaster.isPresent()) {
                log.info("OLD-HEMIS Step 1: Found existing MASTER for PINFL: {}. Returning existing.",
                        studentDto.getPinfl());
                log.info("Existing master - ID: {}, Code: {}, Status: {}",
                        existingMaster.get().getId(),
                        existingMaster.get().getCode(),
                        existingMaster.get().getStudentStatus());

                return studentMapper.toDto(existingMaster.get());
            }
        }

        // STEP 2: Check for existing student with same PINFL + educationType + educationYear
        if (studentDto.getPinfl() != null && !studentDto.getPinfl().isEmpty()
                && studentDto.getEducationType() != null && studentDto.getEducationYear() != null) {

            var existingInProgram = studentRepository.findExistingStudentForDuplicateCheck(
                    studentDto.getPinfl(),
                    studentDto.getEducationType(),
                    studentDto.getEducationYear());

            if (existingInProgram.isPresent()) {
                log.info("OLD-HEMIS Step 2: Found existing student in same program. PINFL: {}, Type: {}, Year: {}",
                        studentDto.getPinfl(), studentDto.getEducationType(), studentDto.getEducationYear());
                log.info("Existing student - ID: {}, Code: {}",
                        existingInProgram.get().getId(),
                        existingInProgram.get().getCode());

                return studentMapper.toDto(existingInProgram.get());
            }
        }

        // STEP 3: Create NEW Student (OLD-HEMIS compatible)
        String generatedCode = studentDto.getCode();
        if (generatedCode == null || generatedCode.isEmpty()) {
            generatedCode = generateOldHemisCode(
                    studentDto.getUniversity(),
                    studentDto.getEducationYear(),
                    studentDto.getEducationType()
            );
            log.info("Generated OLD-HEMIS format code: {}", generatedCode);
        } else {
            if (studentRepository.existsByCode(generatedCode)) {
                throw new ValidationException(
                        "Student with this CODE already exists",
                        "code",
                        "Student code must be unique: " + generatedCode
                );
            }
        }

        // Mark all previous masters as isDuplicate=FALSE
        if (studentDto.getPinfl() != null && !studentDto.getPinfl().isEmpty()) {
            int updatedCount = studentRepository.markPreviousMastersAsDuplicates(studentDto.getPinfl());
            if (updatedCount > 0) {
                log.info("OLD-HEMIS: Marked {} previous master(s) as isDuplicate=FALSE for PINFL: {}",
                        updatedCount, studentDto.getPinfl());
            }
        }

        Student student = studentMapper.toEntity(studentDto);
        student.setCode(generatedCode);
        student.setIsDuplicate(false);

        if (student.getStudentStatus() == null) {
            student.setStudentStatus("10");
        }

        log.info("Creating NEW student (OLD-HEMIS compatible) - Code: {}, PINFL: {}, isDuplicate: FALSE",
                generatedCode, studentDto.getPinfl());

        Student saved = studentRepository.save(student);

        log.info("Student created successfully - ID: {}, Code: {}, PINFL: {}",
                saved.getId(),
                saved.getCode(),
                saved.getPinfl());

        return studentMapper.toDto(saved);
    }

    /**
     * Generate student code in OLD-HEMIS format
     */
    private String generateOldHemisCode(String universityCode, String educationYear, String educationType) {
        if (universityCode == null || universityCode.isEmpty()) {
            throw new ValidationException("University code is required for ID generation", "university", "University code cannot be null");
        }
        if (educationYear == null || educationYear.isEmpty()) {
            throw new ValidationException("Education year is required for ID generation", "educationYear", "Education year cannot be null");
        }
        if (educationType == null || educationType.isEmpty()) {
            throw new ValidationException("Education type is required for ID generation", "educationType", "Education type cannot be null");
        }

        String yearSuffix = educationYear.length() >= 2
                ? educationYear.substring(educationYear.length() - 2)
                : educationYear;

        long count = studentRepository.countForIdGeneration(universityCode, educationType, educationYear) + 1;

        String uniqueCode;
        int iterations = 0;
        final int MAX_ITERATIONS = 1000;

        do {
            String sequence = String.format("%05d", count);
            uniqueCode = universityCode + yearSuffix + educationType + sequence;
            count++;
            iterations++;

            if (iterations > MAX_ITERATIONS) {
                uniqueCode = universityCode + yearSuffix + educationType + "_" + System.currentTimeMillis();
                log.warn("Max iterations reached for code generation. Using fallback: {}", uniqueCode);
                break;
            }
        } while (studentRepository.existsByCode(uniqueCode));

        return uniqueCode;
    }

    @Audited(action = AuditAction.UPDATE, entity = "Student", entityClass = Student.class, keyArg = "id")
    @Transactional
    @CachePut(value = "students", key = "#id")
    @CacheEvict(value = "students", key = "'pinfl:' + #result.pinfl")
    public StudentDto update(UUID id, StudentDto studentDto) {
        log.info("Updating student with ID: {}", id);

        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

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

        studentMapper.updateEntityFromDto(studentDto, existing);

        Student updated = studentRepository.save(existing);

        log.info("Student updated successfully: {}", id);

        return studentMapper.toDto(updated);
    }

    @Audited(action = AuditAction.UPDATE, entity = "Student", entityClass = Student.class, keyArg = "id")
    @Transactional
    public StudentDto partialUpdate(UUID id, StudentDto studentDto) {
        log.info("Partial update for student ID: {}", id);

        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        studentMapper.partialUpdate(studentDto, existing);

        Student updated = studentRepository.save(existing);

        log.info("Student partially updated: {}", id);

        return studentMapper.toDto(updated);
    }

    // =====================================================
    // Soft Delete (NOT Physical DELETE)
    // =====================================================

    @Audited(action = AuditAction.DELETE, entity = "Student", entityClass = Student.class, keyArg = "id")
    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentDto softDelete(UUID id, String userUniversityCode) {
        log.warn("Soft deleting student ID: {} by university: {}", id, userUniversityCode);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (userUniversityCode != null && !userUniversityCode.isEmpty()) {
            String studentUniversity = student.getUniversity();
            if (studentUniversity != null && !studentUniversity.equals(userUniversityCode)) {
                log.error("AUTHORIZATION FAILED: User from university {} tried to delete student from university {}",
                        userUniversityCode, studentUniversity);
                throw new ValidationException(
                        "Access denied",
                        "university",
                        "You can only delete students from your own university. " +
                        "Student belongs to university: " + studentUniversity + ", your university: " + userUniversityCode
                );
            }
        }

        if (student.isDeleted()) {
            log.warn("Student already deleted: {}", id);
            return studentMapper.toDto(student);
        }

        student.setDeleteTs(LocalDateTime.now());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            student.setDeletedBy(auth.getName());
        }

        Student deleted = studentRepository.save(student);

        log.warn("Student soft deleted: {} from university: {}", id, student.getUniversity());

        return studentMapper.toDto(deleted);
    }

    @Transactional
    public void restore(UUID id) {
        log.info("Restoring soft-deleted student ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (!student.isDeleted()) {
            log.warn("Student is not deleted, nothing to restore: {}", id);
            return;
        }

        student.setDeleteTs(null);
        student.setDeletedBy(null);

        studentRepository.save(student);

        log.info("Student restored: {}", id);
    }
}
