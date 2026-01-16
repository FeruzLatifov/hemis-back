package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.TeacherDto;
import uz.hemis.common.dto.TeacherIdRequest;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.EmployeeJobs;
import uz.hemis.domain.entity.Teacher;
import uz.hemis.service.mapper.TeacherMapper;
import uz.hemis.domain.repository.EmployeeJobsRepository;
import uz.hemis.domain.repository.TeacherRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    private final EmployeeJobsRepository employeeJobsRepository;

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

    // =====================================================
    // Teacher ID Generation (OLD-HEMIS Compatible)
    // =====================================================

    /**
     * Generate or retrieve Teacher ID (OLD-HEMIS Compatible)
     *
     * <p><strong>OLD-HEMIS URL:</strong> POST /app/rest/v2/services/teacher/id</p>
     *
     * <p><strong>Logic:</strong></p>
     * <ol>
     *   <li>Validate input parameters</li>
     *   <li>For O'zbekiston citizens (citizenship=11): search by PINFL</li>
     *   <li>For foreigners: search by serialNumber + citizenship</li>
     *   <li>If found: return existing teacher with is_new=false</li>
     *   <li>If not found: create new teacher with generated code, is_new=true</li>
     * </ol>
     *
     * <p><strong>ID Format:</strong> {universityCode}{YY}{gender}{sequence}</p>
     * <p>Example: "3801911001" = university 380, year 2019, male (11), sequence 001</p>
     *
     * @param request TeacherIdRequest with citizenship, pinfl, serial, year, gender
     * @param universityCode university code from authenticated user
     * @return Map with teacher data and is_new flag
     */
    @Transactional
    public Map<String, Object> generateTeacherId(TeacherIdRequest request, String universityCode) {
        log.info("Generating teacher ID - University: {}, PINFL: {}, Serial: {}",
                universityCode, request.getPinfl(), request.getSerial());

        // 1. Validate request parameters
        try {
            request.validate();
        } catch (IllegalArgumentException e) {
            log.error("Validation failed: {}", e.getMessage());
            Map<String, Object> errorResult = new LinkedHashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }

        // 2. Search for existing teacher
        Optional<Teacher> existingTeacher;

        if ("11".equals(request.getCitizenship())) {
            // O'zbekiston fuqarosi - PINFL bo'yicha qidirish
            existingTeacher = teacherRepository.findByPinfl(request.getPinfl());
        } else {
            // Chet el fuqarosi - serialNumber + citizenship bo'yicha qidirish
            existingTeacher = teacherRepository.findBySerialNumberAndCitizenship(
                    request.getSerial(),
                    request.getCitizenship()
            );
        }

        // 3. If found - return existing
        if (existingTeacher.isPresent()) {
            Teacher teacher = existingTeacher.get();
            log.info("Found existing teacher: {} (ID: {})", teacher.getFullName(), teacher.getId());
            return buildTeacherResponse(teacher, false);
        }

        // 4. Create new teacher with generated code
        Teacher newTeacher = new Teacher();
        newTeacher.setPinfl(request.getPinfl());
        newTeacher.setSerialNumber(request.getSerial());
        newTeacher.setCitizenship(request.getCitizenship());
        newTeacher.setGender(request.getGender());
        newTeacher.setEmployeeYear(request.getYear());
        newTeacher.setUniversity(universityCode);

        // Generate unique code
        String generatedCode = generateUniqueTeacherCode(universityCode, request.getYear(), request.getGender());
        newTeacher.setCode(generatedCode);

        // Save
        Teacher savedTeacher = teacherRepository.save(newTeacher);
        log.info("Created new teacher with code: {} (ID: {})", generatedCode, savedTeacher.getId());

        return buildTeacherResponse(savedTeacher, true);
    }

    /**
     * Generate unique teacher code
     *
     * <p><strong>Format:</strong> {universityCode}{YY}{gender}{sequence}</p>
     * <p>Example: "3801911001" = university 380, year 2019, male (11), sequence 001</p>
     *
     * @param universityCode university code (e.g., "380")
     * @param year full year or 2-digit year (e.g., "2019" or "19")
     * @param gender gender code ("11" = male, "12" = female)
     * @return unique code (e.g., "3801911001")
     */
    private String generateUniqueTeacherCode(String universityCode, String year, String gender) {
        // Extract 2-digit year
        String yearSuffix = year;
        if (year != null && year.length() == 4) {
            yearSuffix = year.substring(2); // "2019" -> "19"
        }

        // Build prefix: {universityCode}{YY}{gender}
        String prefix = universityCode + yearSuffix + gender;

        // Count existing teachers with this prefix
        long existingCount = teacherRepository.countByCodePrefix(prefix);

        // Generate sequence (3 digits)
        long sequence = existingCount + 1;
        String sequenceStr = String.format("%03d", sequence);

        return prefix + sequenceStr;
    }

    /**
     * Build teacher response Map for API (OLD-HEMIS format)
     *
     * @param teacher Teacher entity
     * @param isNew true if newly created
     * @return response Map
     */
    private Map<String, Object> buildTeacherResponse(Teacher teacher, boolean isNew) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("success", true);
        result.put("is_new", isNew);
        // OLD-HEMIS compatible: unique_id = teacher code
        result.put("unique_id", teacher.getCode());

        Map<String, Object> teacherMap = new LinkedHashMap<>();
        teacherMap.put("_entityName", "hemishe_ETeacher");
        teacherMap.put("id", teacher.getId() != null ? teacher.getId().toString() : null);
        teacherMap.put("pinfl", teacher.getPinfl());
        teacherMap.put("birthday", teacher.getBirthDate());
        teacherMap.put("firstname", teacher.getFirstName());
        teacherMap.put("code", teacher.getCode());

        // Gender as nested object (OLD-HEMIS format)
        if (teacher.getGender() != null) {
            Map<String, Object> genderMap = new LinkedHashMap<>();
            genderMap.put("_entityName", "hemishe_HGender");
            genderMap.put("id", teacher.getGender());
            genderMap.put("code", teacher.getGender());
            genderMap.put("name", "11".equals(teacher.getGender()) ? "Erkak" : "Ayol");
            teacherMap.put("gender", genderMap);
        }

        // University as nested object (OLD-HEMIS format)
        if (teacher.getUniversity() != null) {
            Map<String, Object> universityMap = new LinkedHashMap<>();
            universityMap.put("_entityName", "hemishe_EUniversity");
            universityMap.put("id", teacher.getUniversity());
            universityMap.put("code", teacher.getUniversity());
            teacherMap.put("university", universityMap);
        }

        teacherMap.put("tag", teacher.getTag());
        teacherMap.put("serialNumber", teacher.getSerialNumber());
        teacherMap.put("address", teacher.getAddress());

        // Citizenship as nested object (OLD-HEMIS format)
        if (teacher.getCitizenship() != null) {
            Map<String, Object> citizenshipMap = new LinkedHashMap<>();
            citizenshipMap.put("_entityName", "hemishe_HCitizenship");
            citizenshipMap.put("id", teacher.getCitizenship());
            citizenshipMap.put("code", teacher.getCitizenship());
            citizenshipMap.put("name", "11".equals(teacher.getCitizenship()) ?
                "O'zbekiston Respublikasi fuqarosi" : "Chet el fuqarosi");
            teacherMap.put("citizenship", citizenshipMap);
        }

        teacherMap.put("lastname", teacher.getSecondName());
        teacherMap.put("fathername", teacher.getThirdName());
        teacherMap.put("phone", teacher.getPhone());
        teacherMap.put("employeeYear", teacher.getEmployeeYear());
        teacherMap.put("fullname", teacher.getFullName());

        result.put("teacher", teacherMap);

        // OLD-HEMIS compatible: personal_data stub
        Map<String, Object> personalData = new LinkedHashMap<>();
        personalData.put("success", false);
        personalData.put("code", "service_not_available");
        personalData.put("message", "Personal data service not available");
        personalData.put("exception", "talaba.edu.uz");
        result.put("personal_data", personalData);

        return result;
    }

    // =====================================================
    // Add Job (OLD-HEMIS Compatible)
    // =====================================================

    /**
     * Xodimga yangi lavozim qo'shish (OLD-HEMIS Compatible)
     *
     * <p><strong>OLD-HEMIS URL:</strong> POST /app/rest/v2/services/teacher/addJob</p>
     *
     * <p><strong>Logic:</strong></p>
     * <ol>
     *   <li>Employee (Teacher) ni ID bo'yicha topish</li>
     *   <li>Agar topilmasa, soft-deleted bo'lsa restore qilish</li>
     *   <li>Agar xodim asosiy shtat birligida ishlasa va yangi job ham asosiy shtat bo'lsa - xato</li>
     *   <li>Yangi job yaratish va saqlash</li>
     * </ol>
     *
     * @param jobData Job ma'lumotlari (employee, university, department, etc.)
     * @return Map with success, message, data
     */
    @Transactional
    public Map<String, Object> addJob(Map<String, Object> jobData) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. Extract job data from wrapper
        @SuppressWarnings("unchecked")
        Map<String, Object> job = (Map<String, Object>) jobData.get("job");
        if (job == null) {
            job = jobData; // Fallback: direct job object without wrapper
        }

        // 2. Extract employee ID
        @SuppressWarnings("unchecked")
        Map<String, Object> employeeData = (Map<String, Object>) job.get("employee");
        if (employeeData == null || employeeData.get("id") == null) {
            result.put("success", false);
            result.put("message", "Employee ID kiritilmagan!");
            return result;
        }

        UUID employeeId;
        try {
            employeeId = UUID.fromString(employeeData.get("id").toString());
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", "Employee ID noto'g'ri formatda!");
            return result;
        }

        // 3. Find employee (teacher)
        Optional<Teacher> teacherOpt = teacherRepository.findById(employeeId);
        Teacher employee;

        if (teacherOpt.isPresent()) {
            employee = teacherOpt.get();
        } else {
            // Try to find soft-deleted and restore
            Optional<Teacher> deletedTeacher = teacherRepository.findDeletedById(employeeId);
            if (deletedTeacher.isPresent()) {
                employee = deletedTeacher.get();
                employee.setDeleteTs(null);
                employee.setDeletedBy(null);
                teacherRepository.save(employee);
                log.info("Restored soft-deleted teacher: {}", employeeId);
            } else {
                result.put("success", false);
                result.put("message", "Bunday xodim vazirlik tizimidan mavjud emas!");
                return result;
            }
        }

        // 4. Extract codes from nested objects
        String universityCode = extractCode(job, "university");
        String departmentCode = extractCode(job, "department");
        String employeeFormCode = extractCode(job, "employeeForm");
        String employeeStatusCode = extractCode(job, "employeeStatus");
        String employeeTypeCode = extractCode(job, "employeeType");
        String employeeRateCode = extractCode(job, "employeeRate");
        String employeePositionCode = extractCode(job, "employeePosition");

        LocalDate jobStartDate = parseDate(job.get("jobStartDate"));
        LocalDate jobEndDate = parseDate(job.get("jobEndDate"));

        // 5. Check if employee already has main job (asosiy shtat birligida)
        // Main form code = "11", working status = "11"
        if ("11".equals(employeeFormCode) && "11".equals(employeeStatusCode)) {
            Optional<EmployeeJobs> existingMainJob = employeeJobsRepository.findMainJobByEmployee(
                    employeeId, "11", "11"
            );

            if (existingMainJob.isPresent()) {
                EmployeeJobs mainJob = existingMainJob.get();
                StringBuilder details = new StringBuilder();
                details.append(employee.getFullName()).append(" ");

                // Get university and department names from codes
                String uniName = mainJob.getUniversity() != null ? mainJob.getUniversity() : "unknown";
                String deptName = mainJob.getDepartment() != null ? mainJob.getDepartment() : "unknown";

                details.append(uniName).append("ning ");
                details.append(deptName).append("da asosiy shtat birligida ishlamoqda");

                result.put("success", false);
                result.put("message", details.toString());
                return result;
            }
        }

        // 6. Create new job
        try {
            EmployeeJobs newJob = new EmployeeJobs();
            newJob.setEmployee(employeeId);
            newJob.setUniversity(universityCode);
            newJob.setDepartment(departmentCode);
            newJob.setEmployeeForm(employeeFormCode);
            newJob.setEmployeeStatus(employeeStatusCode);
            newJob.setEmployeeType(employeeTypeCode);
            newJob.setEmployeeRate(employeeRateCode);
            newJob.setEmployeePosition(employeePositionCode);
            newJob.setJobStartDate(jobStartDate);
            newJob.setJobEndDate(jobEndDate);

            EmployeeJobs savedJob = employeeJobsRepository.save(newJob);

            result.put("success", true);
            result.put("message", "Xodim lavozimi saqlandi");
            result.put("data", buildJobResponse(savedJob));

            log.info("Created new job for employee {} at university {}", employeeId, universityCode);
            return result;

        } catch (Exception ex) {
            log.error("Error creating job for employee {}: {}", employeeId, ex.getMessage());
            result.put("success", false);
            result.put("message", "Xodimda bunday lavozim mavjud");
            result.put("data", ex.getMessage());
            return result;
        }
    }

    /**
     * Extract code from nested object
     * Example: {"university": {"code": "401"}} -> "401"
     */
    @SuppressWarnings("unchecked")
    private String extractCode(Map<String, Object> data, String fieldName) {
        Object field = data.get(fieldName);
        if (field instanceof Map) {
            Map<String, Object> fieldMap = (Map<String, Object>) field;
            Object code = fieldMap.get("code");
            return code != null ? code.toString() : null;
        }
        return field != null ? field.toString() : null;
    }

    /**
     * Parse date from string or LocalDate
     */
    private LocalDate parseDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }
        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        }
        try {
            return LocalDate.parse(dateObj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Build job response Map for API
     */
    private Map<String, Object> buildJobResponse(EmployeeJobs job) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", "hemishe_EEmployeeJobs");
        response.put("id", job.getId() != null ? job.getId().toString() : null);
        response.put("employee", job.getEmployee() != null ? job.getEmployee().toString() : null);
        response.put("university", job.getUniversity());
        response.put("department", job.getDepartment());
        response.put("employeeForm", job.getEmployeeForm());
        response.put("employeeStatus", job.getEmployeeStatus());
        response.put("employeeType", job.getEmployeeType());
        response.put("employeeRate", job.getEmployeeRate());
        response.put("employeePosition", job.getEmployeePosition());
        response.put("jobStartDate", job.getJobStartDate());
        response.put("jobEndDate", job.getJobEndDate());
        return response;
    }
}
