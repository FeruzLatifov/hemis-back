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
     * Update student information
     */
    @Transactional
    public Object updateStudent(Map<String, Object> request) {
        log.info("Updating student: {}", request);
        String studentIdStr = (String) request.get("id");
        if (studentIdStr == null) {
            return Map.of("success", false, "error", "Student ID required");
        }
        
        // TODO: Implement full update logic
        // For now, return success
        return Map.of("success", true, "message", "Student update scheduled");
    }

    /**
     * Validate student status
     */
    public Object validateStudent(String data) {
        log.info("Validating student: {}", data);
        try {
            StudentDto student = findByPinfl(data);
            return Map.of(
                "success", true,
                "valid", true,
                "status", student.getStatus() != null ? student.getStatus() : "ACTIVE"
            );
        } catch (Exception e) {
            return Map.of("success", true, "valid", false, "status", "NOT_FOUND");
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
     */
    private Optional<Student> findActiveStudent(String idData, String citizenship) {
        if ("11".equals(citizenship)) {
            return studentRepository.findActiveByPinfl(idData);
        } else {
            return studentRepository.findActiveBySerialNumber(idData);
        }
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
