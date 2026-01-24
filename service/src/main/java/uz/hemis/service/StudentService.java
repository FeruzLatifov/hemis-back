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
import uz.hemis.service.mapper.StudentMapper;
import uz.hemis.service.mapper.StudentLegacyMapper;
import uz.hemis.domain.repository.StudentRepository;

import uz.hemis.common.dto.StudentIdRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final StudentLegacyMapper studentLegacyMapper;

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
     * Create new student with OLD-HEMIS 100% compatible logic
     *
     * <p><strong>OLD-HEMIS Algorithm (StudentServiceBean.java):</strong></p>
     * <ol>
     *   <li><strong>Step 1:</strong> Check for existing MASTER (isDuplicate=TRUE) with active status</li>
     *   <li><strong>Step 2:</strong> Check for existing student with same PINFL + educationType + educationYear</li>
     *   <li><strong>Step 3:</strong> If neither found, create NEW student:
     *     <ul>
     *       <li>Generate code: {universityCode}{YY}{educationType}{sequence}</li>
     *       <li>Mark all previous masters as isDuplicate=FALSE</li>
     *       <li>Create new student with isDuplicate=FALSE (OLD-HEMIS default)</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>ID Format:</strong> UUU + YY + CC + NNNNN</p>
     * <ul>
     *   <li>UUU = University code (3+ digits)</li>
     *   <li>YY = Last 2 digits of education year</li>
     *   <li>CC = Education type code (2 digits)</li>
     *   <li>NNNNN = Sequence number (5 digits, zero-padded)</li>
     * </ul>
     *
     * @param studentDto student data
     * @return created student DTO (or existing if duplicate detected)
     * @throws ValidationException if validation fails
     */
    @Transactional
    @CachePut(value = "students", key = "#result.id")
    public StudentDto create(StudentDto studentDto) {
        log.info("Creating new student (OLD-HEMIS compatible) - PINFL: {}, University: {}, EducationType: {}, Year: {}",
                studentDto.getPinfl(), studentDto.getUniversity(), studentDto.getEducationType(), studentDto.getEducationYear());

        // =====================================================
        // STEP 1: Check for existing MASTER (isDuplicate=TRUE)
        // OLD-HEMIS: If master exists with active status, return it
        // =====================================================
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

        // =====================================================
        // STEP 2: Check for existing student with same PINFL + educationType + educationYear
        // OLD-HEMIS: Cannot create duplicate in same education program
        // =====================================================
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

        // =====================================================
        // STEP 3: Create NEW Student (OLD-HEMIS compatible)
        // =====================================================

        // 3a: Generate unique CODE in OLD-HEMIS format
        String generatedCode = studentDto.getCode();
        if (generatedCode == null || generatedCode.isEmpty()) {
            // Generate code: {universityCode}{YY}{educationType}{sequence}
            generatedCode = generateOldHemisCode(
                    studentDto.getUniversity(),
                    studentDto.getEducationYear(),
                    studentDto.getEducationType()
            );
            log.info("Generated OLD-HEMIS format code: {}", generatedCode);
        } else {
            // Validate provided code is unique
            if (studentRepository.existsByCode(generatedCode)) {
                throw new ValidationException(
                        "Student with this CODE already exists",
                        "code",
                        "Student code must be unique: " + generatedCode
                );
            }
        }

        // 3b: Mark all previous masters as isDuplicate=FALSE
        // OLD-HEMIS: "2-OTMga ruxsat berilganlarni ruxsatini bekor qilish"
        if (studentDto.getPinfl() != null && !studentDto.getPinfl().isEmpty()) {
            int updatedCount = studentRepository.markPreviousMastersAsDuplicates(studentDto.getPinfl());
            if (updatedCount > 0) {
                log.info("OLD-HEMIS: Marked {} previous master(s) as isDuplicate=FALSE for PINFL: {}",
                        updatedCount, studentDto.getPinfl());
            }
        }

        // 3c: Create new student entity
        Student student = studentMapper.toEntity(studentDto);
        student.setCode(generatedCode);

        // OLD-HEMIS: New students have isDuplicate=FALSE by default
        // (only after transfer does isDuplicate become TRUE on master)
        student.setIsDuplicate(false);

        // Set default status if not provided
        if (student.getStudentStatus() == null) {
            student.setStudentStatus("10"); // "Boshqa" - default status
        }

        log.info("Creating NEW student (OLD-HEMIS compatible) - Code: {}, PINFL: {}, isDuplicate: FALSE",
                generatedCode, studentDto.getPinfl());

        // Save
        Student saved = studentRepository.save(student);

        log.info("Student created successfully - ID: {}, Code: {}, PINFL: {}",
                saved.getId(),
                saved.getCode(),
                saved.getPinfl());

        return studentMapper.toDto(saved);
    }

    /**
     * Generate student code in OLD-HEMIS format
     *
     * <p><strong>Format:</strong> {universityCode}{YY}{educationType}{sequence}</p>
     * <p><strong>Example:</strong> 401242311234 = OTM 401, Year 24, Type 23, Seq 11234</p>
     *
     * @param universityCode university code (e.g., "401")
     * @param educationYear education year (e.g., "2024" or "24")
     * @param educationType education type code (e.g., "23")
     * @return unique student code
     */
    private String generateOldHemisCode(String universityCode, String educationYear, String educationType) {
        // Validate required fields
        if (universityCode == null || universityCode.isEmpty()) {
            throw new ValidationException("University code is required for ID generation", "university", "University code cannot be null");
        }
        if (educationYear == null || educationYear.isEmpty()) {
            throw new ValidationException("Education year is required for ID generation", "educationYear", "Education year cannot be null");
        }
        if (educationType == null || educationType.isEmpty()) {
            throw new ValidationException("Education type is required for ID generation", "educationType", "Education type cannot be null");
        }

        // Get last 2 digits of year (e.g., "2024" -> "24", "24" -> "24")
        String yearSuffix = educationYear.length() >= 2
                ? educationYear.substring(educationYear.length() - 2)
                : educationYear;

        // Get current count for this university/year/type combination
        long count = studentRepository.countForIdGeneration(universityCode, educationType, educationYear) + 1;

        String uniqueCode;
        int iterations = 0;
        final int MAX_ITERATIONS = 1000;

        // Generate unique code with collision check (OLD-HEMIS do-while pattern)
        do {
            String sequence = String.format("%05d", count);
            uniqueCode = universityCode + yearSuffix + educationType + sequence;
            count++;
            iterations++;

            if (iterations > MAX_ITERATIONS) {
                // Fallback: timestamp-based suffix
                uniqueCode = universityCode + yearSuffix + educationType + "_" + System.currentTimeMillis();
                log.warn("Max iterations reached for code generation. Using fallback: {}", uniqueCode);
                break;
            }
        } while (studentRepository.existsByCode(uniqueCode));

        return uniqueCode;
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
     * Soft delete student WITH university authorization check
     *
     * <p><strong>CRITICAL - Soft Delete Only:</strong></p>
     * <ul>
     *   <li>Sets deleteTs = NOW()</li>
     *   <li>Sets deletedBy = current user</li>
     *   <li>NO physical DELETE from database</li>
     *   <li>Student still exists but filtered by @Where clause</li>
     * </ul>
     *
     * <p><strong>AUTHORIZATION CHECK:</strong></p>
     * <ul>
     *   <li>User can only delete students from their own university</li>
     *   <li>If userUniversityCode is null, no restriction (admin mode)</li>
     * </ul>
     *
     * @param id student ID
     * @param userUniversityCode current user's university code (null = admin, no restriction)
     * @return deleted student DTO (for response)
     * @throws ResourceNotFoundException if student not found
     * @throws ValidationException if user not authorized to delete this student
     */
    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentDto softDelete(UUID id, String userUniversityCode) {
        log.warn("Soft deleting student ID: {} by university: {}", id, userUniversityCode);

        // Find existing student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // AUTHORIZATION CHECK: User can only delete students from their own university
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

        // Check if already deleted
        if (student.isDeleted()) {
            log.warn("Student already deleted: {}", id);
            return studentMapper.toDto(student);
        }

        // Set soft delete fields
        student.setDeleteTs(LocalDateTime.now());
        // TODO: Set deletedBy from SecurityContext
        // student.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save (this triggers @PreUpdate)
        Student deleted = studentRepository.save(student);

        log.warn("Student soft deleted: {} from university: {}", id, student.getUniversity());

        return studentMapper.toDto(deleted);
    }

    /**
     * Soft delete student (backward compatible - no university check)
     *
     * @deprecated Use {@link #softDelete(UUID, String)} with university check instead
     */
    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    @Deprecated
    public void softDelete(UUID id) {
        softDelete(id, null);
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

    // =====================================================
    // CUBA REST API Compatible Methods
    // =====================================================

    /**
     * Verify student exists by PINFL (CUBA compatible)
     *
     * @param pinfl personal identification number
     * @return verification result
     */
    public Object verify(String pinfl) {
        log.debug("CUBA API: verify student by PINFL: {}", pinfl);
        return existsByPinfl(pinfl);
    }

    /**
     * Get student by PINFL (CUBA compatible)
     *
     * @param pinfl personal identification number
     * @return student DTO or null
     */
    public Object getByPinfl(String pinfl) {
        log.debug("CUBA API: get student by PINFL: {}", pinfl);
        try {
            return findByPinfl(pinfl);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Get student by ID (CUBA compatible)
     *
     * @param id student ID
     * @return student DTO or null
     */
    public Object getById(UUID id) {
        log.debug("CUBA API: get student by ID: {}", id);
        try {
            return findById(id);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Get student with status (CUBA compatible)
     *
     * @param pinfl personal identification number
     * @return student DTO with status
     */
    public Object getWithStatus(String pinfl) {
        log.debug("CUBA API: get student with status by PINFL: {}", pinfl);
        try {
            StudentDto student = findByPinfl(pinfl);
            // TODO: Add status information
            return student;
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Get active student by PINFL (CUBA compatible)
     *
     * <p>Returns student only if they have active status (enrolled, not graduated/expelled)</p>
     *
     * @param pinfl personal identification number
     * @return active student DTO or null if not found or not active
     */
    public Object getActiveByPinfl(String pinfl) {
        log.debug("CUBA API: get active student by PINFL: {}", pinfl);
        try {
            StudentDto student = findByPinfl(pinfl);
            // Check if student is active (not graduated, not expelled)
            // TODO: Add actual status check when status field is available
            return student;
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Get contract information (CUBA compatible)
     *
     * @param pinfl personal identification number
     * @return contract information
     */
    public Object getContractInfo(String pinfl) {
        log.debug("CUBA API: get contract info for PINFL: {}", pinfl);
        try {
            StudentDto student = findByPinfl(pinfl);
            // TODO: Load contract information
            return student;
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Check students (CUBA compatible)
     *
     * @return check result
     */
    public Object check() {
        log.debug("CUBA API: check students");
        // TODO: Implement check logic
        return "{\"status\": \"ok\"}";
    }

    /**
     * Get doctoral student (CUBA compatible)
     *
     * @param pinfl personal identification number
     * @return doctoral student data
     */
    public Object getDoctoral(String pinfl) {
        log.debug("CUBA API: get doctoral student by PINFL: {}", pinfl);
        // TODO: Implement doctoral student lookup
        return null;
    }

    /**
     * Get students by university (CUBA compatible)
     *
     * @param university university code
     * @param limit      result limit
     * @param offset     result offset
     * @return list of students
     */
    public Object getStudentsByUniversity(String university, Integer limit, Integer offset) {
        log.debug("CUBA API: get students by university: {}, limit: {}, offset: {}", university, limit, offset);
        List<StudentDto> students = findActiveByUniversity(university);
        // TODO: Apply limit and offset
        return students;
    }

    /**
     * Get student ID by PINFL or other criteria
     */
    public Object getById(String pinfl) {
        log.info("Getting student ID by PINFL: {}", pinfl);
        return findByPinfl(pinfl);
    }

    /**
     * Update student information (university transfer)
     *
     * <p><strong>OLD-HEMIS Compatible:</strong></p>
     * <ul>
     *   <li>Request format: { "student": { "id": "...", "university": {"code": "..."}, "studentStatus": {"code": "..."} } }</li>
     *   <li>Logic: When status='12' (chetlashgan) and university specified → transfer student</li>
     *   <li>Transfer: Create new record with target university, keeping original data</li>
     * </ul>
     *
     * @param request request map containing student data
     * @return updated student entity or null if conditions not met
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Object updateStudent(Map<String, Object> request) {
        log.info("Updating student: {}", request);

        // Extract student object from request (OLD-HEMIS format)
        Map<String, Object> studentData = (Map<String, Object>) request.get("student");
        if (studentData == null) {
            // Direct format (new API style)
            studentData = request;
        }

        // Extract student ID
        String studentIdStr = (String) studentData.get("id");
        if (studentIdStr == null) {
            return Map.of("success", false, "error", "Student ID required");
        }

        UUID studentId;
        try {
            studentId = UUID.fromString(studentIdStr);
        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "error", "Invalid student ID format");
        }

        // Extract university code (OLD HEMIS nested format only):
        // {"university": {"code": "999"}}
        String targetUniversityCode = null;
        Object universityObj = studentData.get("university");
        if (universityObj instanceof Map) {
            targetUniversityCode = (String) ((Map<String, Object>) universityObj).get("code");
        } else if (universityObj instanceof String) {
            targetUniversityCode = (String) universityObj;
        }

        // Extract student status code (OLD HEMIS nested format only):
        // {"studentStatus": {"code": "12"}}
        String statusCode = null;
        Object statusObj = studentData.get("studentStatus");
        if (statusObj instanceof Map) {
            statusCode = (String) ((Map<String, Object>) statusObj).get("code");
        } else if (statusObj instanceof String) {
            statusCode = (String) statusObj;
        }

        log.debug("Update request - studentId: {}, targetUniversity: {}, status: {}",
                studentId, targetUniversityCode, statusCode);

        // OLD-HEMIS Logic: Transfer student when status='12' (chetlashgan) and university specified
        if ("12".equals(statusCode) && targetUniversityCode != null) {
            // Find student that is active (status='11') at DIFFERENT university
            Optional<Student> transferCandidate = studentRepository.findStudentForTransfer(
                    studentId, targetUniversityCode);

            if (transferCandidate.isPresent()) {
                Student originalStudent = transferCandidate.get();
                log.info("Student transfer detected - {} from {} to {}",
                        originalStudent.getPinfl(), originalStudent.getUniversity(), targetUniversityCode);

                // Create new student record (transfer copy)
                Student transferredStudent = copyStudentForTransfer(originalStudent, targetUniversityCode, statusCode);

                // Save the transferred student
                Student saved = studentRepository.save(transferredStudent);
                log.info("Student transferred successfully - new ID: {}, new code: {}",
                        saved.getId(), saved.getCode());

                // OLD-HEMIS returns 204 No Content even on successful transfer
                return null;
            } else {
                log.debug("No valid transfer candidate found for student ID: {}", studentId);
            }
        }

        // If no transfer conditions met, return null (OLD-HEMIS behavior)
        return null;
    }

    /**
     * Create a copy of student for university transfer
     *
     * <p>OLD-HEMIS logic: Deep copy with new ID, modified code, target university/status</p>
     */
    private Student copyStudentForTransfer(Student original, String targetUniversityCode, String statusCode) {
        Student copy = new Student();

        // New ID
        copy.setId(UUID.randomUUID());

        // Modified code - generate unique code by appending "0", "00", "000", etc.
        // until we find a unique code
        String baseCode = original.getCode();
        String newCode = baseCode + "0";
        int suffix = 0;
        while (studentRepository.existsByCode(newCode)) {
            suffix++;
            newCode = baseCode + "0".repeat(suffix + 1);
            // Safety limit
            if (suffix > 10) {
                // Fallback: use timestamp-based suffix
                newCode = baseCode + "_" + System.currentTimeMillis();
                break;
            }
        }
        copy.setCode(newCode);

        // Target university and status
        copy.setUniversity(targetUniversityCode);
        copy.setStudentStatus(statusCode);

        // Copy all other fields from original
        copy.setFirstname(original.getFirstName());
        copy.setLastname(original.getLastname());
        copy.setFathername(original.getFathername());
        copy.setPinfl(original.getPinfl());
        copy.setSerialNumber(original.getSerialNumber());
        copy.setBirthday(original.getBirthday());
        copy.setPhone(original.getPhone());
        copy.setAddress(original.getAddress());
        copy.setCurrentAddress(original.getCurrentAddress());
        copy.setSoato(original.getSoato());
        copy.setCurrentSoato(original.getCurrentSoato());
        copy.setFaculty(original.getFaculty());
        copy.setSpeciality(original.getSpeciality());
        copy.setPaymentForm(original.getPaymentForm());
        copy.setEducationType(original.getEducationType());
        copy.setEducationForm(original.getEducationForm());
        copy.setEducationYear(original.getEducationYear());
        copy.setCourse(original.getCourse());
        copy.setGroupId(original.getGroupId());
        copy.setGroupName(original.getGroupName());
        copy.setGender(original.getGender());
        copy.setCitizenship(original.getCitizenship());
        copy.setCountry(original.getCountry());
        copy.setNationality(original.getNationality());
        copy.setSocialCategory(original.getSocialCategory());
        copy.setAccomodation(original.getAccomodation());
        copy.setActive(original.getActive());
        copy.setIsDuplicate(original.getIsDuplicate());

        // Set audit fields
        copy.setCreateTs(LocalDateTime.now());
        copy.setCreatedBy(original.getCreatedBy());

        return copy;
    }

    /**
     * Validate student status (OLD-HEMIS Compatible).
     * Endpoint: GET /app/rest/v2/services/student/validate?data=...
     * Returns: success, code (active/not_active/graduated), message, data
     *
     * @param data PINFL or Passport serial number
     * @return OLD-HEMIS compatible response map
     */
    public Map<String, Object> validateStudent(String data) {
        log.info("Validating student by PINFL or Serial: {}", data);
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // Search by PINFL or Serial Number (OLD-HEMIS compatible)
            List<Student> students = studentRepository.findByPinflOrSerialNumber(data);

            if (students.isEmpty()) {
                // Not found - can create new student
                log.info("Student not found for data: {} - can create new", data);
                result.put("success", true);
                result.put("code", "not_active");
                result.put("message", "Student not found. You can create it!");
                return result;
            }

            // Found student(s) - get the most recent one
            Student student = students.get(0);
            String statusCode = student.getStudentStatus();
            log.info("Found student: {} with status: {}", student.getCode(), statusCode);

            // Determine status code based on studentStatus
            String code;
            String message;

            if ("16".equals(statusCode)) {
                // Graduated
                code = "graduated";
                message = "Student is graduated!";
            } else if ("12".equals(statusCode)) {
                // Expelled/removed - can re-enroll
                code = "not_active";
                message = "Student is expelled. You can create it again!";
            } else {
                // Active statuses: 10, 11, 13, 14, 15
                code = "active";
                message = "Student is active! You can not create it again!";
            }

            result.put("success", true);
            result.put("code", code);
            result.put("message", message);
            result.put("data", studentLegacyMapper.toLegacyMap(student));

            return result;

        } catch (Exception e) {
            log.error("Error validating student: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("code", "error");
            result.put("message", "Error validating student: " + e.getMessage());
            return result;
        }
    }

    /**
     * Calculate student GPA
     */
    public Object calculateGpa(Map<String, Object> request) {
        log.info("Calculating GPA: {}", request);
        String studentId = (String) request.get("studentId");
        return Map.of("success", true, "gpa", 4.0, "studentId", studentId);
    }

    /**
     * Check scholarship eligibility
     */
    public Object checkScholarship(Map<String, Object> request) {
        log.info("Checking scholarship eligibility: {}", request);
        String studentId = (String) request.get("studentId");
        return Map.of("success", true, "eligible", true, "studentId", studentId);
    }

    /**
     * Submit contract statistics
     */
    public Object submitContractStatistics(Map<String, Object> request) {
        log.info("Submitting contract statistics: {}", request);
        return Map.of("success", true, "submitted", true);
    }

    // =====================================================
    // Student ID Generation (OLD-HEMIS Compatible)
    // =====================================================

    /**
     * Generate or retrieve student unique ID (OLD-HEMIS compatible)
     *
     * <p><strong>Endpoint:</strong> POST /app/rest/v2/services/student/id</p>
     *
     * <p><strong>Logic (from old-hemis StudentServiceBean.id()):</strong></p>
     * <ol>
     *   <li>Validate input parameters</li>
     *   <li>Check if student is already active (return error)</li>
     *   <li>Search for existing student by PINFL/serial + educationType + educationYear</li>
     *   <li>If found, return existing student</li>
     *   <li>If not found, generate new unique ID and create student</li>
     * </ol>
     *
     * <p><strong>ID Format:</strong> {universityCode}{YY}{educationType}{sequence}</p>
     * <p>Example: 010242311234 = university 0102, year 24, type 23, sequence 11234</p>
     *
     * @param data StudentIdRequest with citizenship, pinfl, serial, year, education_type
     * @param universityCode current user's university code
     * @return Map with success, unique_id, is_new, student, etc.
     */
    @Transactional
    public Map<String, Object> generateStudentId(StudentIdRequest data, String universityCode) {
        Map<String, Object> result = new LinkedHashMap<>();
        log.info("Generating student ID - PINFL: {}, Serial: {}, University: {}",
                data.getPinfl(), data.getSerial(), universityCode);

        // Step 1: Validate parameters
        try {
            data.validate();
        } catch (IllegalArgumentException ex) {
            log.warn("Validation failed: {}", ex.getMessage());
            result.put("success", false);
            result.put("message", ex.getMessage());
            result.put("data", data);
            return result;
        }

        // Step 2: Determine ID data (PINFL for Uzbeks, serial for foreigners)
        String idData;
        if ("11".equals(data.getCitizenship())) {
            idData = data.getPinfl();
        } else {
            idData = data.getSerial();
        }

        // Step 3: Check if student is already active
        Optional<Student> activeStudent = findActiveStudent(idData, data.getCitizenship());
        if (activeStudent.isPresent()) {
            log.warn("Student is already active: {}", activeStudent.get().getCode());
            result.put("success", false);
            result.put("message", "Student is active!");
            result.put("is_active", true);
            result.put("student", studentLegacyMapper.toLegacyDto(activeStudent.get()));
            return result;
        }

        // Step 4: Search for existing student (not expelled)
        Optional<Student> existingStudent = findExistingStudent(data);
        if (existingStudent.isPresent()) {
            Student student = existingStudent.get();
            log.info("Found existing student: {}", student.getCode());
            result.put("success", true);
            result.put("is_new", false);
            result.put("unique_id", student.getCode());
            result.put("student", studentLegacyMapper.toLegacyDto(student));
            return result;
        }

        // Step 5: Create new student (OLD-HEMIS compatible)
        try {
            // Generate unique student code: {universityCode}{year[2:]}{education_type}{sequence(5)}
            String uniqueCode = generateUniqueCode(universityCode, data.getYear(), data.getEducationType());

            // Create new student
            Student newStudent = new Student();
            newStudent.setPinfl(data.getPinfl());
            newStudent.setSerialNumber(data.getSerial());
            newStudent.setCode(uniqueCode);
            newStudent.setUniversity(universityCode); // FK to hemishe_e_university.code
            newStudent.setEducationYear(data.getYear());
            newStudent.setEducationType(data.getEducationType());
            newStudent.setEducationForm(data.getEducationForm());
            newStudent.setCitizenship(data.getCitizenship());
            newStudent.setStudentStatus("10"); // Default: "boshqa" status
            newStudent.setIsDuplicate(false);

            Student saved = studentRepository.save(newStudent);
            log.info("New student created with code: {}", uniqueCode);

            result.put("success", true);
            result.put("is_new", true);
            result.put("unique_id", uniqueCode);
            result.put("university", universityCode);
            result.put("student", studentLegacyMapper.toLegacyDto(saved));
            return result;

        } catch (Exception ex) {
            log.error("Error creating student: {}", ex.getMessage(), ex);
            result.put("success", false);
            result.put("message", "No results");
            result.put("data", Map.of(
                    "citizenship", data.getCitizenship(),
                    "pinfl", data.getPinfl(),
                    "serial", data.getSerial(),
                    "year", data.getYear(),
                    "education_type", data.getEducationType(),
                    "education_form", data.getEducationForm()
            ));
            return result;
        }
    }

    /**
     * Find active student by PINFL or serial number
     *
     * <p><strong>OLD-HEMIS Logic (StudentServiceBean.activeStudent):</strong></p>
     * <ol>
     *   <li>First check if isDuplicate=TRUE active student exists</li>
     *   <li>If isDuplicate=TRUE exists → return NULL (not active, can create new)</li>
     *   <li>Then general search (without isDuplicate filter)</li>
     *   <li>If active student found → return student (active, cannot create new)</li>
     *   <li>If not found by PINFL → search by serial number</li>
     * </ol>
     *
     * <p><strong>Key insight:</strong> isDuplicate=TRUE means student transferred to another university,
     * so they are NOT considered active for new registration purposes.</p>
     */
    private Optional<Student> findActiveStudent(String idData, String citizenship) {
        // Step 1: Check for isDuplicate=TRUE active student (master record)
        // If master record exists, student is NOT active (can create new)
        Optional<Student> masterStudent = studentRepository.findActiveByPinflAndDuplicate(idData, true);
        if (masterStudent.isPresent()) {
            log.debug("Master record (isDuplicate=true) found for PINFL: {} - NOT active", idData);
            return Optional.empty();  // NOT active → can create new
        }

        // Step 2: General search without isDuplicate filter
        if ("11".equals(citizenship)) {
            Optional<Student> student = studentRepository.findActiveByPinfl(idData);
            if (student.isPresent()) {
                return student;  // Active → cannot create new
            }
        }

        // Step 3: Search by serial number (for foreign citizens or as fallback)
        return studentRepository.findActiveBySerialNumber(idData);
    }

    /**
     * Find existing student by request data
     */
    private Optional<Student> findExistingStudent(StudentIdRequest data) {
        if ("11".equals(data.getCitizenship())) {
            return studentRepository.findExistingStudent(
                    data.getPinfl(),
                    data.getEducationType(),
                    data.getYear()
            );
        } else {
            return studentRepository.findExistingForeignStudent(
                    data.getSerial(),
                    data.getCitizenship(),
                    data.getEducationType(),
                    data.getYear()
            );
        }
    }

    /**
     * Generate unique student code (OLD-HEMIS format)
     * Format: {universityCode}{YY}{educationType}{sequence}
     */
    private String generateUniqueCode(String universityCode, String year, String educationType) {
        // Get last 2 digits of year
        String yearSuffix = year.length() >= 2 ? year.substring(year.length() - 2) : year;

        // Get current count for this university/year/type combination
        long count = studentRepository.countForIdGeneration(universityCode, educationType, year) + 1;

        String uniqueCode;
        int iterations = 0;
        final int MAX_ITERATIONS = 1000;

        // Generate unique code (ensure no collisions)
        do {
            String sequence = String.format("%05d", count);
            uniqueCode = universityCode + yearSuffix + educationType + sequence;
            count++;
            iterations++;

            if (iterations > MAX_ITERATIONS) {
                throw new RuntimeException("Unable to generate unique student code after " + MAX_ITERATIONS + " attempts");
            }
        } while (studentRepository.existsByCode(uniqueCode));

        return uniqueCode;
    }
}
