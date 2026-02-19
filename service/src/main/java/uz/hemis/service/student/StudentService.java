package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.student.StudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Student;
import uz.hemis.service.student.mapper.StudentMapper;
import uz.hemis.service.student.mapper.StudentLegacyMapper;
import uz.hemis.domain.repository.StudentRepository;

import uz.hemis.common.dto.student.StudentIdRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import uz.hemis.common.audit.Audited;
import uz.hemis.common.audit.AuditAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

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
    private final JdbcTemplate jdbcTemplate;

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
     * Find all student entities (paginated) - for legacy endpoints needing entity access
     *
     * @param pageable pagination parameters
     * @return page of student entities
     */
    public Page<Student> findAllEntities(Pageable pageable) {
        return studentRepository.findAll(pageable);
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

    /**
     * Find student entity by ID (for legacy endpoints that need entity access)
     *
     * @param id student UUID
     * @return Optional containing student entity
     */
    public Optional<Student> findEntityById(UUID id) {
        return studentRepository.findById(id);
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
    @Audited(action = AuditAction.CREATE, entity = "Student", entityClass = Student.class)
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
    @Audited(action = AuditAction.UPDATE, entity = "Student", entityClass = Student.class, keyArg = "id")
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
    @Audited(action = AuditAction.UPDATE, entity = "Student", entityClass = Student.class, keyArg = "id")
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
    @Audited(action = AuditAction.DELETE, entity = "Student", entityClass = Student.class, keyArg = "id")
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            student.setDeletedBy(auth.getName());
        }

        // Save (this triggers @PreUpdate)
        Student deleted = studentRepository.save(student);

        log.warn("Student soft deleted: {} from university: {}", id, student.getUniversity());

        return studentMapper.toDto(deleted);
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
     * OLD-HEMIS: StudentServiceBean.get(pinfl)
     * JPQL: studentStatus IN ('11','16')
     * Returns flat service map or null
     */
    public Map<String, Object> getByPinflFlat(String pinfl) {
        List<Student> students = studentRepository.findByPinflAndStudentStatusIn(pinfl, Arrays.asList("11", "16"));
        if (students.isEmpty()) return null;
        return studentLegacyMapper.toFlatServiceMap(students.get(0));
    }

    /**
     * OLD-HEMIS: StudentServiceBean.getActive(pinfl)
     * JPQL: studentStatus IN ('11','16') AND decreeInfoNumber IS NOT NULL AND decreeInfoNumber <> ''
     * Uses native SQL because decree_info_number is not mapped in Student entity
     */
    public Map<String, Object> getActiveFlat(String pinfl) {
        // Primary: pinfl + status + decreeInfoNumber check
        String sql = """
                SELECT id FROM hemishe_e_student
                WHERE pinfl = ?
                AND _student_status IN ('11','16')
                AND decree_info_number IS NOT NULL AND decree_info_number <> ''
                AND delete_ts IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, pinfl);
        if (rows.isEmpty()) {
            // Fallback: search by serial number (old-hemis behavior)
            sql = """
                    SELECT id FROM hemishe_e_student
                    WHERE serial_number = ? AND delete_ts IS NULL
                    LIMIT 1
                    """;
            rows = jdbcTemplate.queryForList(sql, pinfl);
            if (rows.isEmpty()) return null;
        }
        UUID id = (UUID) rows.get(0).get("id");
        Optional<Student> student = studentRepository.findById(id);
        return student.map(studentLegacyMapper::toFlatServiceMap).orElse(null);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.getById(code)
     * JPQL: code = :code AND studentStatus IN ('11')
     */
    public Map<String, Object> getByCodeFlat(String code) {
        Optional<Student> student = studentRepository.findByCodeAndStudentStatusIn(code, List.of("11"));
        return student.map(studentLegacyMapper::toFlatServiceMap).orElse(null);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.getDoctoral(pinfl)
     * JPQL: pinfl AND educationType='13' AND studentStatus IN ('11')
     */
    public Map<String, Object> getDoctoralFlat(String pinfl) {
        List<Student> students = studentRepository.findByPinflAndEducationTypeAndStudentStatusIn(
                pinfl, "13", List.of("11"));
        if (students.isEmpty()) return null;
        return studentLegacyMapper.toFlatServiceMap(students.get(0));
    }

    /**
     * OLD-HEMIS: StudentServiceBean.getWithStatus(pinfl)
     * Fallback chain: status '11' → latest createTs → serialNumber
     */
    public Map<String, Object> getWithStatusFlat(String pinfl) {
        // Try 1: active students (status '11')
        List<Student> students = studentRepository.findByPinflAndStudentStatusIn(pinfl, List.of("11"));
        if (!students.isEmpty()) {
            return studentLegacyMapper.toFlatServiceMap(students.get(0));
        }
        // Try 2: any student by PINFL ordered by createTs DESC
        students = studentRepository.findAllByPinfl(pinfl);
        if (!students.isEmpty()) {
            return studentLegacyMapper.toFlatServiceMap(students.get(0));
        }
        // Try 3: by serial number
        students = studentRepository.findBySerialNumber(pinfl);
        if (!students.isEmpty()) {
            return studentLegacyMapper.toFlatServiceMap(students.get(0));
        }
        return null;
    }

    /**
     * OLD-HEMIS: StudentServiceBean.testGet(pinfl)
     * Returns billing-format student data
     */
    public Map<String, Object> testGetFlat(String pinfl) {
        List<Student> students = studentRepository.findByPinflAndStudentStatusIn(pinfl, Arrays.asList("11", "16"));
        if (students.isEmpty()) return null;
        return studentLegacyMapper.toBillingMap(students.get(0));
    }

    /**
     * OLD-HEMIS: StudentServiceBean.students(university, limit, offset)
     * Paginated student list, max 1000
     */
    public Map<String, Object> getStudentsByUniversityFlatWithCount(String university, int limit, int offset) {
        int safeLimit = Math.min(limit, 1000);
        List<Student> students = studentRepository.findStudentsByUniversityPaginated(university, safeLimit, offset);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Student s : students) {
            data.add(studentLegacyMapper.toLegacyMapForService(s));
        }
        // Total count (active + academic leave statuses)
        long count = studentRepository.countByUniversityAndStudentStatus(university, "11")
                   + studentRepository.countByUniversityAndStudentStatus(university, "16");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", data);
        result.put("count", count);
        return result;
    }

    /**
     * OLD-HEMIS: StudentServiceBean.tashkentStudents(limit, offset)
     *
     * Reads directly from denormalized hemishe_r_student_full table (same as old-hemis).
     * Old-hemis query: "select e from hemishe_RStudentFull e where e.statusCode = :status
     *                   and e.universityRegionCode like :soato order by e.created_at"
     * View: rStudentFullTashkent-view
     *
     * Response: {success, code, count, limit, offset, data}
     */
    public Map<String, Object> getTashkentStudentsResult(int limit, int offset) {
        int safeLimit = Math.min(limit, 1000);
        Map<String, Object> response = new LinkedHashMap<>();

        // Count from denormalized table (same WHERE as old-hemis)
        String countSql = """
                SELECT count(*)
                FROM hemishe_r_student_full
                WHERE status_code = '11'
                AND university_region_code LIKE '1726%'
                """;
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
        if (count == null || count == 0) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
            return response;
        }

        // Data from denormalized table — all fields from rStudentFullTashkent-view
        String sql = """
                SELECT code, citizenship_code, citizenship_name, country_code, country_name,
                       nationality_code, nationality_name, pinfl, passport_number, fullname,
                       birthday, gender_code, gender_name, region_code, region_name,
                       district_code, district_name, address, address_current,
                       university_code, university_name, universtiry_ownership_code,
                       university_ownership_name, university_region_code, university_region_name,
                       university_district_code, university_district_name,
                       faculty_code, faculty_name,
                       education_type_code, education_type_name, education_form_code, education_form_name,
                       payment_form_code, payment_form_name, course_code, course_name,
                       speciality_id, speciality_code, speciality_name,
                       education_language_code, education_language_name,
                       stipend_type_code, stipend_type_name,
                       accomodation_code, accomodation_name,
                       social_category_code, social_category_name,
                       status_code, status_name,
                       created_at, updated_at, edu_year, is_graduate
                FROM hemishe_r_student_full
                WHERE status_code = '11'
                AND university_region_code LIKE '1726%'
                ORDER BY created_at
                LIMIT ? OFFSET ?
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, safeLimit, offset);
        if (rows.isEmpty()) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
            return response;
        }

        // Convert DB column names to CUBA camelCase format
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("_entityName", "hemishe_RStudentFull");
            String code = str(row.get("code"));
            r.put("_instanceName", code);
            r.put("id", code);
            r.put("code", code);
            r.put("citizenshipCode", str(row.get("citizenship_code")));
            r.put("citizenshipName", str(row.get("citizenship_name")));
            r.put("countryCode", str(row.get("country_code")));
            r.put("countryName", str(row.get("country_name")));
            r.put("nationalityCode", str(row.get("nationality_code")));
            r.put("nationalityName", str(row.get("nationality_name")));
            r.put("pinfl", str(row.get("pinfl")));
            r.put("passportNumber", str(row.get("passport_number")));
            r.put("fullname", str(row.get("fullname")));
            r.put("birthday", row.get("birthday") != null ? row.get("birthday").toString() : null);
            r.put("genderCode", str(row.get("gender_code")));
            r.put("genderName", str(row.get("gender_name")));
            r.put("regionCode", str(row.get("region_code")));
            r.put("regionName", str(row.get("region_name")));
            r.put("districtCode", str(row.get("district_code")));
            r.put("districtName", str(row.get("district_name")));
            r.put("address", str(row.get("address")));
            r.put("addressCurrent", str(row.get("address_current")));
            r.put("universityCode", str(row.get("university_code")));
            r.put("universityName", str(row.get("university_name")));
            r.put("universtiryOwnershipCode", str(row.get("universtiry_ownership_code")));
            r.put("universityOwnershipName", str(row.get("university_ownership_name")));
            r.put("universityRegionCode", str(row.get("university_region_code")));
            r.put("universityRegionName", str(row.get("university_region_name")));
            r.put("universityDistrictCode", str(row.get("university_district_code")));
            r.put("universityDistrictName", str(row.get("university_district_name")));
            r.put("facultyCode", str(row.get("faculty_code")));
            r.put("facultyName", str(row.get("faculty_name")));
            r.put("educationTypeCode", str(row.get("education_type_code")));
            r.put("educationTypeName", str(row.get("education_type_name")));
            r.put("educationFormCode", str(row.get("education_form_code")));
            r.put("educationFormName", str(row.get("education_form_name")));
            r.put("paymentFormCode", str(row.get("payment_form_code")));
            r.put("paymentFormName", str(row.get("payment_form_name")));
            r.put("courseCode", str(row.get("course_code")));
            r.put("courseName", str(row.get("course_name")));
            r.put("specialityId", row.get("speciality_id"));
            r.put("specialityCode", str(row.get("speciality_code")));
            r.put("specialityName", str(row.get("speciality_name")));
            r.put("educationLanguageCode", str(row.get("education_language_code")));
            r.put("educationLanguageName", str(row.get("education_language_name")));
            r.put("stipendTypeCode", str(row.get("stipend_type_code")));
            r.put("stipendTypeName", str(row.get("stipend_type_name")));
            r.put("accomodationCode", str(row.get("accomodation_code")));
            r.put("accomodationName", str(row.get("accomodation_name")));
            r.put("socialCategoryCode", str(row.get("social_category_code")));
            r.put("socialCategoryName", str(row.get("social_category_name")));
            r.put("statusCode", str(row.get("status_code")));
            r.put("statusName", str(row.get("status_name")));
            r.put("created_at", row.get("created_at") != null ? row.get("created_at").toString() : null);
            r.put("updated_at", row.get("updated_at") != null ? row.get("updated_at").toString() : null);
            r.put("eduYear", str(row.get("edu_year")));
            r.put("isGraduate", str(row.get("is_graduate")));
            data.add(r);
        }

        response.put("success", true);
        response.put("code", "ok");
        response.put("count", count);
        response.put("limit", limit);
        response.put("offset", offset);
        response.put("data", data);
        return response;
    }

    private static String str(Object val) {
        return val != null ? val.toString() : "";
    }

    /**
     * OLD-HEMIS: StudentServiceBean.isExpel(pinfls)
     * Native SQL joining hemishe_e_student + hemishe_h_expel
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> isExpel(String[] pinfls) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (pinfls == null || pinfls.length == 0) {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }

        try {
            StringBuilder placeholders = new StringBuilder();
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < pinfls.length; i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
                params.add(pinfls[i]);
            }

            String sql = """
                    SELECT s.pinfl,
                           COALESCE(s.lastname,'') || ' ' || COALESCE(s.firstname,'') || ' ' || COALESCE(s.fathername,'') as fullname,
                           s._university as "universityCode",
                           s._expel_reason as "expelReasonCode",
                           h.name as "expelReasonName"
                    FROM hemishe_e_student s
                    LEFT JOIN hemishe_h_expel h ON h.code = s._expel_reason AND h.delete_ts IS NULL
                    WHERE s.pinfl IN (%s)
                      AND s._student_status = '12'
                      AND s.delete_ts IS NULL
                    ORDER BY s.create_ts DESC
                    """.formatted(placeholders.toString());

            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql, params.toArray());
            result.put("success", true);
            result.put("data", items);
        } catch (Exception e) {
            log.error("Error in isExpel", e);
            result.put("success", false);
            result.put("data", e.getMessage());
        }
        return result;
    }

    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");

    /**
     * OLD-HEMIS: StudentServiceBean.checkScholarship(tin, pinfls)
     * Native SQL with PostgreSQL stipend_check() and get_student_fullname_by_pinfl()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> checkScholarshipNative(String tin, String[] pinfls) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (tin == null || !DIGITS_ONLY.matcher(tin).matches()) {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }
        if (pinfls == null || pinfls.length == 0) {
            result.put("success", false);
            result.put("data", "Bad request");
            return result;
        }
        for (String p : pinfls) {
            if (!DIGITS_ONLY.matcher(p).matches()) {
                result.put("success", false);
                result.put("data", "Bad request");
                return result;
            }
        }

        try {
            // Build ARRAY literal for UNNEST
            StringBuilder arrayLiteral = new StringBuilder();
            for (int i = 0; i < pinfls.length; i++) {
                if (i > 0) arrayLiteral.append(",");
                arrayLiteral.append("'").append(pinfls[i]).append("'");
            }

            String sql = "SELECT t.pinfl, t.fullname FROM (" +
                    "SELECT r.pinfl, stipend_check('" + tin + "', r.pinfl) as data, " +
                    "get_student_fullname_by_pinfl(r.pinfl) as fullname " +
                    "FROM (SELECT UNNEST(ARRAY[" + arrayLiteral + "]) as pinfl) r" +
                    ") t WHERE t.\"data\" = false";

            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql);
            result.put("success", true);
            result.put("data", items);
        } catch (Exception e) {
            log.error("Error in checkScholarship", e);
            result.put("success", false);
            result.put("data", e.getMessage());
        }
        return result;
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndPaymentForm()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> byTashkentAndPaymentForm() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (to'luv shakli kesimida)",
                "byTashkentAndPaymentForm",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "budget", "Davlat grantida tahsil olayotgan talabalar soni",
                        "contract", "To'lov shartnoma asosida tahsil olayotgan talabalar soni",
                        "total", "Jami talabalar soni"),
                """
                SELECT t.university_code, t.university_name,
                       b.student_count as budget, c.student_count as contract,
                       b.student_count + c.student_count as total
                FROM hemishe_r_student_full t
                INNER JOIN (
                    SELECT e.university_code, e.payment_form_code, count(*) as student_count
                    FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                    GROUP BY e.university_code, payment_form_code
                ) b ON b.university_code = t.university_code AND b.payment_form_code = '11'
                INNER JOIN (
                    SELECT e.university_code, e.payment_form_code, count(*) as student_count
                    FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                    GROUP BY e.university_code, payment_form_code
                ) c ON c.university_code = t.university_code AND c.payment_form_code = '12'
                WHERE t.university_region_code like '1726%'
                GROUP BY t.university_code, t.university_name, budget, contract
                ORDER BY t.university_code
                """);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndRegionDistrict()
     */
    public Map<String, Object> byTashkentAndRegionDistrict() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (OTM, viloyat va tumanlar kesimida)",
                "byTashkentAndRegionDistrict",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "region_code", "Viloyat kodi", "region_name", "Viloyat nomi",
                        "district_code", "Tuman kodi", "district_name", "Tuman nomi",
                        "payment_form_code", "To'lov turi kodi", "payment_form_name", "To'lov turi nomi",
                        "student_count", "Talabalar soni"),
                """
                SELECT e.university_code, e.university_name,
                       e.region_code, e.region_name, e.district_code, e.district_name,
                       e.payment_form_code,
                       CASE WHEN (e.payment_form_name IS NULL) THEN '(Belgilanmagan)' ELSE e.payment_form_name END as payment_form_name,
                       count(*) as student_count
                FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                GROUP BY e.university_code, e.university_name, e.region_code, e.region_name,
                         e.district_code, e.district_name, e.payment_form_name, payment_form_code
                ORDER BY e.university_code, e.university_name, e.region_code, e.region_name,
                         e.district_code, e.district_name, e.payment_form_name
                """);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndRegionDistrictAndEduType()
     */
    public Map<String, Object> byTashkentAndRegionDistrictAndEduType() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (OTM, viloyat, tumanlar va ta'lim turi kesimida)",
                "byTashkentAndRegionDistrictAndEduType",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "region_code", "Viloyat kodi", "region_name", "Viloyat nomi",
                        "district_code", "Tuman kodi", "district_name", "Tuman nomi",
                        "education_type_code", "Ta'lim turi kodi", "education_type_name", "Ta'lim turi nomi",
                        "student_count", "Talabalar soni"),
                """
                SELECT e.university_code, e.university_name,
                       e.region_code, e.region_name, e.district_code, e.district_name,
                       e.education_type_code,
                       CASE WHEN (e.education_type_name IS NULL) THEN '(Belgilanmagan)' ELSE e.education_type_name END as education_type_name,
                       count(*) as student_count
                FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                GROUP BY e.university_code, e.university_name, e.region_code, e.region_name,
                         e.district_code, e.district_name, e.education_type_code, e.education_type_name
                ORDER BY e.university_code, e.university_name, e.region_code, e.region_name,
                         e.district_code, e.district_name, e.education_type_code, e.education_type_name
                """);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndFacultyAndCourse()
     */
    public Map<String, Object> byTashkentAndFacultyAndCourse() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (OTM, fakultet, ta'lim turi va kurslar kesimida)",
                "byTashkentAndFacultyAndCourse",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "faculty_code", "Fakultet kodi", "faculty_name", "Fakultet nomi",
                        "education_type_code", "Ta'lim turi kodi", "education_type_name", "Ta'lim turi nomi",
                        "course_code", "O'quv kurs kodi", "course_name", "O'quv kurs nomi",
                        "student_count", "Talabalar soni"),
                """
                SELECT e.university_code, e.university_name,
                       e.faculty_code, e.faculty_name,
                       e.education_type_code, e.education_type_name,
                       e.course_code, e.course_name,
                       count(*) as student_count
                FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                GROUP BY e.university_code, e.university_name, e.faculty_code, e.faculty_name,
                         e.education_type_code, e.education_type_name, e.course_code, e.course_name
                ORDER BY e.university_code, e.university_name, e.faculty_code, e.faculty_name,
                         e.education_type_code, e.education_type_name, e.course_code, e.course_name
                """);
    }

    /**
     * OLD-HEMIS: StudentServiceBean.byTashkentAndEduFormTypeAndGender()
     */
    public Map<String, Object> byTashkentAndEduFormTypeAndGender() {
        return executeTashkentStats(
                "Toshkent shahrida tahsil oluvchi talabalar (OTM, ta'lim shakli, turi va jinslar kesimida)",
                "byTashkentAndEduFormTypeAndGender",
                Map.of("university_code", "OTM kodi", "university_name", "OTM nomi",
                        "education_form_code", "Ta'lim shakli kodi", "education_form_name", "Ta'lim shakli nomi",
                        "education_type_code", "Ta'lim turi kodi", "education_type_name", "Ta'lim turi nomi",
                        "gender_code", "Jins kodi", "gender_name", "Jins nomi",
                        "student_count", "Talabalar soni"),
                """
                SELECT e.university_code, e.university_name,
                       e.education_form_code,
                       CASE WHEN (e.education_form_name IS NULL) THEN '(Belgilanmagan)' ELSE e.education_form_name END as education_form_name,
                       e.education_type_code, e.education_type_name,
                       e.gender_code, e.gender_name,
                       count(*) as student_count
                FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'
                GROUP BY e.university_code, e.university_name, e.education_form_code, e.education_form_name,
                         e.education_type_code, e.education_type_name, e.gender_code, e.gender_name
                ORDER BY e.university_code, e.university_name, e.education_form_code, e.education_form_name,
                         e.education_type_code, e.education_type_name, e.gender_code, e.gender_name
                """);
    }

    /**
     * Helper: Execute Tashkent statistics query with standard response format
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeTashkentStats(String title, String methodName,
                                                      Map<String, String> columns, String sql) {
        Map<String, Object> result = new LinkedHashMap<>();
        String sqlCount = "SELECT count(*) as total_count FROM hemishe_r_student_full e WHERE e.university_region_code like '1726%'";
        try {
            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql);
            Long total = jdbcTemplate.queryForObject(sqlCount, Long.class);

            // Use LinkedHashMap for columns to preserve order
            Map<String, String> orderedColumns = new LinkedHashMap<>(columns);

            result.put("status", "OK");
            result.put("title", title);
            result.put("message", null);
            result.put("total_student_count", total);
            result.put("columns", orderedColumns);
            result.put("items", items);
        } catch (Exception e) {
            log.error("Error in {}", methodName, e);
            result.put("status", "ERROR");
            result.put("title", title);
            result.put("message", e.getMessage());
        }
        return result;
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
        Object studentObj = request.get("student");
        Map<String, Object> studentData;
        if (studentObj instanceof Map) {
            studentData = (Map<String, Object>) studentObj;
        } else if (studentObj == null) {
            // Direct format (new API style)
            studentData = request;
        } else {
            // Invalid format (e.g. plain string instead of nested object)
            throw new IllegalArgumentException("'student' field must be a JSON object, got: " + studentObj.getClass().getSimpleName());
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

        // Non-transfer update: find student and update fields from request
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            log.warn("Student not found for update: {}", studentId);
            return Map.of("success", false, "error", "Student not found");
        }

        Student student = studentOpt.get();
        updateStudentFields(student, studentData);
        student.setUpdateTs(LocalDateTime.now());

        Student saved = studentRepository.save(student);
        log.info("Student updated successfully - ID: {}, code: {}", saved.getId(), saved.getCode());

        // PHP HemisResponseStudent format: { id, code, verified, points }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", saved.getId().toString());
        response.put("code", saved.getCode());
        response.put("verified", saved.getVerified());
        response.put("points", saved.getPoints());
        return response;
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
     * Update student entity fields from PHP request data.
     * Only non-null fields in the request are updated (partial update).
     *
     * @param student the student entity to update
     * @param data the request data map (PHP format)
     */
    @SuppressWarnings("unchecked")
    private void updateStudentFields(Student student, Map<String, Object> data) {
        // Direct string fields - use safeString() for PHP compatibility (may send Integer)
        if (data.containsKey("firstname"))    student.setFirstname(safeString(data.get("firstname")));
        if (data.containsKey("lastname"))     student.setLastname(safeString(data.get("lastname")));
        if (data.containsKey("fathername"))   student.setFathername(safeString(data.get("fathername")));
        if (data.containsKey("pinfl"))        student.setPinfl(safeString(data.get("pinfl")));
        if (data.containsKey("serialNumber")) student.setSerialNumber(safeString(data.get("serialNumber")));
        if (data.containsKey("phone"))        student.setPhone(safeString(data.get("phone")));
        if (data.containsKey("email"))        student.setEmail(safeString(data.get("email")));
        if (data.containsKey("address"))      student.setAddress(safeString(data.get("address")));
        if (data.containsKey("currentAddress")) student.setCurrentAddress(safeString(data.get("currentAddress")));
        if (data.containsKey("responsiblePersonPhone")) student.setResponsiblePersonPhone(safeString(data.get("responsiblePersonPhone")));
        if (data.containsKey("parentPhone")) student.setParentPhone(safeString(data.get("parentPhone")));
        if (data.containsKey("geoAddress"))  student.setGeoAddress(safeString(data.get("geoAddress")));
        if (data.containsKey("groupId"))     student.setGroupId(safeString(data.get("groupId")));
        if (data.containsKey("groupName"))   student.setGroupName(safeString(data.get("groupName")));
        if (data.containsKey("isGraduate"))  student.setIsGraduate(safeString(data.get("isGraduate")));
        if (data.containsKey("tag"))         student.setTag(safeString(data.get("tag")));
        if (data.containsKey("enrollOrderNumber"))   student.setEnrollOrderNumber(safeString(data.get("enrollOrderNumber")));
        if (data.containsKey("enrollOrderName"))     student.setEnrollOrderName(safeString(data.get("enrollOrderName")));
        if (data.containsKey("enrollOrderCategory")) student.setEnrollOrderCategory(safeString(data.get("enrollOrderCategory")));
        if (data.containsKey("statusOrderNumber"))   student.setStatusOrderNumber(safeString(data.get("statusOrderNumber")));
        if (data.containsKey("statusOrderName"))     student.setStatusOrderName(safeString(data.get("statusOrderName")));
        if (data.containsKey("statusOrderCategory")) student.setStatusOrderCategory(safeString(data.get("statusOrderCategory")));

        // Nested {code: "..."} fields
        if (data.containsKey("gender"))              student.setGender(extractCode(data.get("gender")));
        if (data.containsKey("university"))          student.setUniversity(extractCode(data.get("university")));
        if (data.containsKey("educationYear"))       student.setEducationYear(extractCode(data.get("educationYear")));
        if (data.containsKey("country"))             student.setCountry(extractCode(data.get("country")));
        if (data.containsKey("citizenship"))         student.setCitizenship(extractCode(data.get("citizenship")));
        if (data.containsKey("nationality"))         student.setNationality(extractCode(data.get("nationality")));
        if (data.containsKey("accomodation"))        student.setAccomodation(extractCode(data.get("accomodation")));
        if (data.containsKey("soato"))               student.setSoato(extractCode(data.get("soato")));
        if (data.containsKey("currentSoato"))        student.setCurrentSoato(extractCode(data.get("currentSoato")));
        if (data.containsKey("paymentForm"))         student.setPaymentForm(extractCode(data.get("paymentForm")));
        if (data.containsKey("educationForm"))       student.setEducationForm(extractCode(data.get("educationForm")));
        if (data.containsKey("educationType"))       student.setEducationType(extractCode(data.get("educationType")));
        if (data.containsKey("course"))              student.setCourse(extractCode(data.get("course")));
        if (data.containsKey("language"))            student.setLanguage(extractCode(data.get("language")));
        if (data.containsKey("faculty"))             student.setFaculty(extractCode(data.get("faculty")));
        if (data.containsKey("studentStatus"))       student.setStudentStatus(extractCode(data.get("studentStatus")));
        if (data.containsKey("socialCategory"))      student.setSocialCategory(extractCode(data.get("socialCategory")));
        if (data.containsKey("expelReason"))         student.setExpelReason(extractCode(data.get("expelReason")));
        if (data.containsKey("livingStatus"))        student.setLivingStatus(extractCode(data.get("livingStatus")));
        if (data.containsKey("roommateType"))        student.setRoommateType(extractCode(data.get("roommateType")));
        if (data.containsKey("statusEducationYear")) student.setCurrentEducationYearCode(extractCode(data.get("statusEducationYear")));

        // Nested {id: "UUID"} fields
        if (data.containsKey("specialityBachelor"))   student.setSpecialityBachelor(extractUuid(data.get("specialityBachelor")));
        if (data.containsKey("specialityMaster"))     student.setSpecialityMaster(extractUuid(data.get("specialityMaster")));
        if (data.containsKey("specialityOrdinatura")) student.setSpecialityDoctoral(extractUuid(data.get("specialityOrdinatura")));

        // Date fields (format: Y-m-d)
        if (data.containsKey("birthday"))          student.setBirthday(parseDate(data.get("birthday")));
        if (data.containsKey("passportGivenDate")) student.setPassportGivenDate(parseDate(data.get("passportGivenDate")));
        if (data.containsKey("enrollOrderDate"))   student.setEnrollOrderDate(parseDate(data.get("enrollOrderDate")));
        if (data.containsKey("statusOrderDate"))   student.setStatusOrderDate(parseDate(data.get("statusOrderDate")));

        // Integer fields
        if (data.containsKey("roommateCount")) {
            Object val = data.get("roommateCount");
            if (val instanceof Number) {
                student.setRoommateCount(((Number) val).intValue());
            } else if (val instanceof String) {
                try {
                    student.setRoommateCount(Integer.parseInt((String) val));
                } catch (NumberFormatException ignored) { }
            }
        }
    }

    /**
     * Safely convert any object to String (handles Integer, null, etc.)
     * PHP may send numeric values as Integer instead of String
     */
    private String safeString(Object obj) {
        if (obj == null) return null;
        String str = String.valueOf(obj);
        return "null".equals(str) ? null : str;
    }

    /**
     * Extract code from nested {@code {code: "..."}} object or plain string.
     */
    @SuppressWarnings("unchecked")
    private String extractCode(Object obj) {
        if (obj instanceof Map) {
            Object code = ((Map<String, Object>) obj).get("code");
            return code != null ? String.valueOf(code) : null;
        } else if (obj != null) {
            return String.valueOf(obj);
        }
        return null;
    }

    /**
     * Extract UUID from nested {@code {id: "..."}} object or plain string.
     */
    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object obj) {
        String idStr = null;
        if (obj instanceof Map) {
            Object id = ((Map<String, Object>) obj).get("id");
            idStr = id != null ? String.valueOf(id) : null;
        } else if (obj != null) {
            idStr = String.valueOf(obj);
        }
        if (idStr == null || idStr.equals("null")) return null;
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID in student update: {}", idStr);
            return null;
        }
    }

    /**
     * Parse date from string (format: Y-m-d) or return null.
     */
    private LocalDate parseDate(Object obj) {
        if (obj == null) return null;
        String dateStr = obj.toString();
        if (dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date in student update: {}", dateStr);
            return null;
        }
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
            result.put("data", studentLegacyMapper.toLegacyMapForService(student));

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
     * Check scholarship eligibility (POST format - used by checkScholarship2 endpoint)
     */
    public Object checkScholarship(Map<String, Object> request) {
        log.info("Checking scholarship eligibility: {}", request);
        String studentId = (String) request.get("studentId");
        return Map.of("success", true, "eligible", true, "studentId", String.valueOf(studentId));
    }

    /**
     * OLD-HEMIS: StudentServiceBean.check()
     * Compares ALL students with citizenship '11' against MVD personal data.
     * Note: This is a very heavy operation. Returns stub for now.
     */
    public Map<String, Object> checkStudents() {
        log.info("CUBA API: check students against MVD");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("all_count", 0);
        result.put("incorrect_count", 0);
        result.put("no_data_count", 0);
        result.put("incorrect", List.of());
        result.put("no_personal_data", List.of());
        return result;
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

        // Step 1b: Validate citizenship code exists in classifier
        try {
            jdbcTemplate.queryForMap(
                    "SELECT code FROM hemishe_h_citizenship WHERE code = ? AND delete_ts IS NULL",
                    data.getCitizenship());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.warn("Citizenship code not found: {}", data.getCitizenship());
            result.put("success", false);
            result.put("message", "Citizenship value not available!");
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
            result.put("data", studentLegacyMapper.toLegacyMapForService(activeStudent.get()));
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
            result.put("data", studentLegacyMapper.toLegacyMapForService(student));
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
            result.put("data", studentLegacyMapper.toLegacyMapForService(saved));
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
        // OLD-HEMIS: DataManager.load().list() — dublikat bo'lsa birinchisini oladi
        List<Student> masterStudents = studentRepository.findActiveByPinflAndDuplicate(idData, true);
        if (!masterStudents.isEmpty()) {
            log.debug("Master record (isDuplicate=true) found for PINFL: {} - NOT active", idData);
            return Optional.empty();  // NOT active → can create new
        }

        // Step 2: General search without isDuplicate filter
        if ("11".equals(citizenship)) {
            List<Student> students = studentRepository.findActiveByPinfl(idData);
            if (!students.isEmpty()) {
                return Optional.of(students.get(0));  // Active → cannot create new
            }
        }

        // Step 3: Search by serial number (for foreign citizens or as fallback)
        List<Student> bySerial = studentRepository.findActiveBySerialNumber(idData);
        return bySerial.isEmpty() ? Optional.empty() : Optional.of(bySerial.get(0));
    }

    /**
     * Find existing student by request data
     */
    private Optional<Student> findExistingStudent(StudentIdRequest data) {
        // OLD-HEMIS: DataManager.load().list() — dublikat bo'lsa birinchisini oladi
        List<Student> students;
        if ("11".equals(data.getCitizenship())) {
            students = studentRepository.findExistingStudent(
                    data.getPinfl(),
                    data.getEducationType(),
                    data.getYear()
            );
        } else {
            students = studentRepository.findExistingForeignStudent(
                    data.getSerial(),
                    data.getCitizenship(),
                    data.getEducationType(),
                    data.getYear()
            );
        }
        return students.isEmpty() ? Optional.empty() : Optional.of(students.get(0));
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
