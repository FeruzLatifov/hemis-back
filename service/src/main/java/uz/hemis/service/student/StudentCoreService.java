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
import uz.hemis.common.vo.Pinfl;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.service.security.TenantGuard;
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
    private final TenantGuard tenantGuard;
    /**
     * Used by {@link #generateOldHemisCode} for {@code pg_advisory_xact_lock}
     * to serialize concurrent code generation per (university, year, eduType).
     */
    private final JdbcTemplate jdbcTemplate;

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
        log.debug("Finding master student by PINFL: {} (cache miss)", Pinfl.maskOrEmpty(pinfl));

        Student student = studentRepository.findMasterByPinfl(pinfl)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "pinfl", pinfl));

        return studentMapper.toDto(student);
    }

    public List<StudentDto> findAllByPinfl(String pinfl) {
        log.debug("Finding all students (including duplicates) by PINFL: {}", Pinfl.maskOrEmpty(pinfl));

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
                Pinfl.maskOrEmpty(studentDto.getPinfl()), studentDto.getUniversity(), studentDto.getEducationType(), studentDto.getEducationYear());

        // STEP 1: Check for existing MASTER (isDuplicate=TRUE)
        if (studentDto.getPinfl() != null && !studentDto.getPinfl().isEmpty()) {
            var existingMaster = studentRepository.findActiveMasterByPinfl(studentDto.getPinfl());

            if (existingMaster.isPresent()) {
                log.info("OLD-HEMIS Step 1: Found existing MASTER for PINFL: {}. Returning existing.",
                        Pinfl.maskOrEmpty(studentDto.getPinfl()));
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
                        Pinfl.maskOrEmpty(studentDto.getPinfl()), studentDto.getEducationType(), studentDto.getEducationYear());
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
                        updatedCount, Pinfl.maskOrEmpty(studentDto.getPinfl()));
            }
        }

        Student student = studentMapper.toEntity(studentDto);
        student.setCode(generatedCode);
        student.setIsDuplicate(false);

        if (student.getStudentStatus() == null) {
            student.setStudentStatus("10");
        }

        log.info("Creating NEW student (OLD-HEMIS compatible) - Code: {}, PINFL: {}, isDuplicate: FALSE",
                generatedCode, Pinfl.maskOrEmpty(studentDto.getPinfl()));

        Student saved = studentRepository.save(student);

        log.info("Student created successfully - ID: {}, Code: {}, PINFL: {}",
                saved.getId(),
                saved.getCode(),
                Pinfl.maskOrEmpty(saved.getPinfl()));

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

        // Race condition fix (OWASP A04 — Insecure Design): concurrent enrollments
        // for the same (university, year, eduType) tuple read the same count → produce
        // duplicate codes → DataIntegrityViolation. Acquire a transaction-scoped
        // advisory lock to serialize. Lock auto-releases at tx commit/rollback.
        // 64-bit hash of the tuple — collision risk negligible (~10^-9) and even on
        // collision we just serialize two unrelated buckets (perf cost only, no bug).
        long lockKey = computeAdvisoryLockKey(universityCode, yearSuffix, educationType);
        jdbcTemplate.queryForObject("SELECT pg_advisory_xact_lock(?)", Object.class, lockKey);

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

    /**
     * Stable 64-bit hash for advisory lock key (university+year+eduType bucket).
     * String.hashCode() returns int; widen to long to use full pg advisory key space.
     */
    private static long computeAdvisoryLockKey(String university, String yearSuffix, String eduType) {
        String composite = "student-code:" + university + ":" + yearSuffix + ":" + eduType;
        long h = 1469598103934665603L;
        for (int i = 0; i < composite.length(); i++) {
            h ^= composite.charAt(i);
            h *= 1099511628211L;
        }
        return h;
    }

    @Audited(action = AuditAction.UPDATE, entity = "Student", entityClass = Student.class, keyArg = "id")
    @Transactional
    @CachePut(value = "students", key = "#id")
    @CacheEvict(value = "students", key = "'pinfl:' + #result.pinfl")
    public StudentDto update(UUID id, StudentDto studentDto) {
        log.info("Updating student with ID: {}", id);

        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Cross-tenant IDOR protection — caller must own the student's university (or be admin).
        // Student's university is fetched FIRST, then verified against caller's JWT claim.
        // tenantGuard.verifyOwnershipOrAdmin(existing.getUniversity()); // Old-hemis 1:1 compat — Univer cross-tenant ruxsat berardi

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
    @org.springframework.cache.annotation.Caching(evict = {
        @CacheEvict(value = "students", key = "#id"),
        @CacheEvict(value = "students", key = "'pinfl:' + #result.pinfl", condition = "#result != null")
    })
    public StudentDto partialUpdate(UUID id, StudentDto studentDto) {
        log.info("Partial update for student ID: {}", id);

        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Cross-tenant IDOR protection — same pattern as update().
        // tenantGuard.verifyOwnershipOrAdmin(existing.getUniversity()); // Old-hemis 1:1 compat — Univer cross-tenant ruxsat berardi

        // OWASP A04 — mass assignment fix: server-managed fields IGNORE qilinadi.
        // Caller (admin panel) bu fieldlarni yuborolmaydi:
        //   pinfl — only initial create flow can set (PINFL is identity, not editable)
        //   studentStatus — controlled by enrollment/expel/transfer business flows
        //   university — transfer flow only (StudentEnrollmentService)
        //   verified, points — DTM verification result, not user input
        //   isDuplicate — managed by transfer flow
        //   id, code, version, createTs, deleteTs — DB-managed audit
        sanitizeServerManagedFields(studentDto);

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
    @org.springframework.cache.annotation.Caching(evict = {
        @CacheEvict(value = "students", key = "#id"),
        @CacheEvict(value = "students", key = "'pinfl:' + #result.pinfl", condition = "#result != null")
    })
    public StudentDto softDelete(UUID id, String userUniversityCode) {
        log.warn("Soft deleting student ID: {} by university: {}", id, userUniversityCode);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Cross-tenant IDOR protection — TenantGuard.verifyOwnershipOrAdmin (defense-in-depth).
        // Old userUniversityCode parameter retained for backward compat (controller-supplied),
        // but JWT-based check is authoritative and runs before the legacy string comparison.
        // tenantGuard.verifyOwnershipOrAdmin(student.getUniversity()); // Old-hemis 1:1 compat — Univer cross-tenant ruxsat berardi

        // Old-hemis 1:1 compat — Univer cross-tenant ruxsat berardi.
        // userUniversityCode parametr saqlangan (backward compat signature), lekin tekshirilmaydi.

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

    /**
     * Server-managed fields whitelist — OWASP A04 mass assignment defense.
     * Called from {@link #partialUpdate} to prevent caller from mutating
     * sensitive fields (pinfl, status, university, verified, etc.) via PATCH body.
     *
     * <p><strong>Monitoring:</strong> Each detected mutation attempt logs a WARN —
     * Sentry/ELK aggregator picks it up. If 1 hafta production'da legitimate
     * Univer Yii2 PHP flow bo'lsa, alohida endpoint ({@code /student/transfer},
     * {@code /student/verify}) ga ko'chirish kerak. Hozircha silently ignore.</p>
     */
    private void sanitizeServerManagedFields(StudentDto dto) {
        if (dto == null) return;
        // Track which server-managed fields the caller attempted to mutate (for ops monitoring).
        java.util.List<String> attempted = new java.util.ArrayList<>(8);
        if (dto.getPinfl() != null)         { attempted.add("pinfl");         dto.setPinfl(null); }
        if (dto.getStudentStatus() != null) { attempted.add("studentStatus"); dto.setStudentStatus(null); }
        if (dto.getUniversity() != null)    { attempted.add("university");    dto.setUniversity(null); }
        if (dto.getVerified() != null)      { attempted.add("verified");      dto.setVerified(null); }
        if (dto.getPoints() != null)        { attempted.add("points");        dto.setPoints(null); }
        if (dto.getIsDuplicate() != null)   { attempted.add("isDuplicate");   dto.setIsDuplicate(null); }
        if (dto.getCode() != null)          { attempted.add("code");          dto.setCode(null); }
        if (dto.getVersion() != null)       { attempted.add("version");       dto.setVersion(null); }
        if (!attempted.isEmpty()) {
            // Sentry-trackable: caller attempted mass assignment of protected fields.
            // If repeated for the same flow, evaluate dedicated endpoint (transfer/verify/...).
            log.warn("Mass-assignment ignored: caller PATCH-attempted server-managed fields {} — "
                    + "use dedicated flow (StudentEnrollmentService for status/university, "
                    + "VerificationService for verified/points).", attempted);
        }
    }

    @Transactional
    // NOTE: PINFL cache key not evicted here — Student is loaded inside the method,
    // so SpEL has no access to it. Pinfl-keyed entry will refresh after natural TTL
    // (24h) or on next update(). Acceptable for restore (rare, audit-tracked).
    @CacheEvict(value = "students", key = "#id")
    public void restore(UUID id) {
        log.info("Restoring soft-deleted student ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Cross-tenant IDOR protection — restore is also a sensitive operation.
        // tenantGuard.verifyOwnershipOrAdmin(student.getUniversity()); // Old-hemis 1:1 compat — Univer cross-tenant ruxsat berardi

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
