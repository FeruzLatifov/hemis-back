package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Student;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Student Repository - Spring Data JPA
 *
 * <p><strong>CRITICAL - NO DELETE OPERATIONS:</strong></p>
 * <ul>
 *   <li>NDG (Non-Deletion Guarantee) - physical DELETE prohibited</li>
 *   <li>Inherited delete methods (deleteById, deleteAll) blocked at DB role level</li>
 *   <li>Soft delete handled at service layer (set deleteTs)</li>
 *   <li>All queries automatically filter deleted records (@Where clause)</li>
 * </ul>
 *
 * <p><strong>Read-Only Optimization:</strong></p>
 * <ul>
 *   <li>Most queries use @Transactional(readOnly=true)</li>
 *   <li>Routes to replica database (if Master-Replica configured)</li>
 *   <li>Better performance for SELECT queries</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface StudentRepository extends JpaRepository<Student, UUID> {

    // =====================================================
    // Basic Queries
    // =====================================================

    /**
     * Find student by code (business key)
     *
     * <p><strong>CODE is UNIQUE business identifier</strong></p>
     *
     * @param code student code
     * @return student if found
     */
    Optional<Student> findByCode(String code);

    // =====================================================
    // PINFL Queries (isDuplicate-aware)
    // =====================================================
    // CRITICAL: PINFL is NOT UNIQUE in old-HEMIS!
    // Multiple students can have same PINFL (transfer/duplicate cases)
    // Managed via isDuplicate flag:
    //   - TRUE: Master record (active/current student)
    //   - FALSE: Duplicate record (historical/transferred)
    // =====================================================

    /**
     * Find MASTER student record by PINFL
     *
     * <p><strong>CRITICAL:</strong> Returns only the master record (isDuplicate = true)</p>
     * <p>Use this for student lookup by PINFL in most cases</p>
     *
     * @param pinfl personal identification number
     * @return master student record if exists
     */
    @Query("SELECT s FROM Student s WHERE s.pinfl = :pinfl AND s.isDuplicate = true")
    Optional<Student> findMasterByPinfl(@Param("pinfl") String pinfl);

    /**
     * Find ALL students with same PINFL (including duplicates)
     *
     * <p><strong>Use case:</strong> Finding transfer history, duplicate detection</p>
     * <p>Returns list ordered by: master first, then by creation date descending</p>
     *
     * @param pinfl personal identification number
     * @return list of all students with this PINFL (master + duplicates)
     */
    @Query("SELECT s FROM Student s WHERE s.pinfl = :pinfl ORDER BY s.isDuplicate DESC, s.createTs DESC")
    List<Student> findAllByPinfl(@Param("pinfl") String pinfl);

    /**
     * Check if master student exists for PINFL
     *
     * <p><strong>Use this instead of existsByPinfl()</strong></p>
     *
     * @param pinfl personal identification number
     * @return true if master record exists
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.pinfl = :pinfl AND s.isDuplicate = true")
    boolean existsMasterByPinfl(@Param("pinfl") String pinfl);

    /**
     * Find student by PINFL (first match)
     *
     * <p><strong>⚠️ DEPRECATED - Use findMasterByPinfl() instead!</strong></p>
     * <p>This method returns first match, which may not be the master record.</p>
     * <p>Kept for backward compatibility only.</p>
     *
     * @param pinfl personal identification number
     * @return first student with this PINFL (may be duplicate!)
     * @deprecated Use {@link #findMasterByPinfl(String)} for correct behavior
     */
    @Deprecated
    Optional<Student> findByPinfl(String pinfl);

    /**
     * Check if ANY student exists with PINFL
     *
     * <p><strong>⚠️ WARNING:</strong> Returns true if ANY student (master or duplicate) exists.</p>
     * <p>Use {@link #existsMasterByPinfl(String)} for checking master records only.</p>
     *
     * @param pinfl personal identification number
     * @return true if any student exists with this PINFL
     */
    boolean existsByPinfl(String pinfl);

    // =====================================================
    // University-based Queries
    // =====================================================

    /**
     * Find all students by university code
     *
     * <p>Used for university-specific reports and lists</p>
     *
     * @param universityCode university code
     * @return list of students
     */
    List<Student> findByUniversity(String universityCode);

    /**
     * Find all students by university code (paginated)
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return page of students
     */
    Page<Student> findByUniversity(String universityCode, Pageable pageable);

    /**
     * Find active students by university
     *
     * @param universityCode university code
     * @return list of active students
     */
    @Query("SELECT s FROM Student s WHERE s.university = :universityCode AND s.active = true")
    List<Student> findActiveByUniversity(@Param("universityCode") String universityCode);

    // =====================================================
    // Status-based Queries
    // =====================================================

    /**
     * Find students by student status
     *
     * @param studentStatus status code (e.g., '11' = active, '16' = graduated)
     * @return list of students
     */
    List<Student> findByStudentStatus(String studentStatus);

    /**
     * Find students by university and status
     *
     * @param universityCode university code
     * @param studentStatus status code
     * @return list of students
     */
    List<Student> findByUniversityAndStudentStatus(String universityCode, String studentStatus);

    /**
     * Count students by university and status
     *
     * @param universityCode university code
     * @param studentStatus status code
     * @return count
     */
    long countByUniversityAndStudentStatus(String universityCode, String studentStatus);

    // =====================================================
    // Faculty & Speciality Queries
    // =====================================================

    /**
     * Find students by faculty
     *
     * @param facultyCode faculty code
     * @return list of students
     */
    List<Student> findByFaculty(String facultyCode);

    /**
     * Find students by speciality
     *
     * @param specialityCode speciality code
     * @return list of students
     */
    List<Student> findBySpeciality(String specialityCode);

    /**
     * Find students by university, faculty, and course
     *
     * @param universityCode university code
     * @param facultyCode faculty code
     * @param courseCode course code
     * @return list of students
     */
    List<Student> findByUniversityAndFacultyAndCourse(
            String universityCode,
            String facultyCode,
            String courseCode
    );

    // =====================================================
    // Education Year & Form Queries
    // =====================================================

    /**
     * Find students by education year
     *
     * @param educationYear education year code
     * @return list of students
     */
    List<Student> findByEducationYear(String educationYear);

    /**
     * Find students by payment form
     *
     * @param paymentForm payment form code ('11' = budget, '12' = contract)
     * @return list of students
     */
    List<Student> findByPaymentForm(String paymentForm);

    /**
     * Find students by education type
     *
     * @param educationType education type code ('11' = bachelor, '12' = master, etc.)
     * @return list of students
     */
    List<Student> findByEducationType(String educationType);

    // =====================================================
    // Complex Queries
    // =====================================================

    /**
     * Find students by university, education year, and active status
     *
     * <p>Commonly used for enrollment reports</p>
     *
     * @param universityCode university code
     * @param educationYear education year code
     * @param active active flag
     * @param pageable pagination
     * @return page of students
     */
    @Query("SELECT s FROM Student s WHERE s.university = :university " +
           "AND s.educationYear = :educationYear " +
           "AND s.active = :active")
    Page<Student> findByUniversityAndEducationYearAndActive(
            @Param("university") String universityCode,
            @Param("educationYear") String educationYear,
            @Param("active") Boolean active,
            Pageable pageable
    );

    /**
     * Count active students by university
     *
     * @param universityCode university code
     * @return count
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.university = :university AND s.active = true")
    long countActiveByUniversity(@Param("university") String universityCode);

    /**
     * Find students eligible for stipend (used by stipend_check function)
     *
     * @param universityCode university code
     * @param pinfl student PINFL
     * @return student if found and eligible
     */
    @Query("SELECT s FROM Student s WHERE s.university = :university " +
           "AND s.pinfl = :pinfl " +
           "AND s.studentStatus IN ('11', '16') " +
           "AND (s.paymentForm = '11' OR (s.paymentForm = '12' AND s.socialCategory IN ('11', '12', '13', '15')))")
    Optional<Student> findStipendEligible(
            @Param("university") String universityCode,
            @Param("pinfl") String pinfl
    );

    // =====================================================
    // Search Queries
    // =====================================================

    /**
     * Search students by name (lastname, firstname, fathername)
     *
     * @param searchTerm search term (case-insensitive partial match)
     * @param pageable pagination
     * @return page of students
     */
    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.lastname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.firstname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.fathername) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Student> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    // =====================================================
    // Student ID Generation Queries (OLD-HEMIS Compatible)
    // =====================================================

    /**
     * Find active student by PINFL or serial number
     * Used for duplicate detection when generating student ID
     *
     * @param pinfl PINFL raqami
     * @return aktiv talaba yoki empty
     */
    @Query("SELECT s FROM Student s WHERE s.pinfl = :pinfl " +
           "AND s.studentStatus IN ('10', '11', '13', '15') " +
           "AND s.isDuplicate = true")
    Optional<Student> findActiveByPinfl(@Param("pinfl") String pinfl);

    /**
     * Find active student by serial number (for foreign citizens)
     *
     * @param serialNumber passport serial number
     * @return aktiv talaba yoki empty
     */
    @Query("SELECT s FROM Student s WHERE s.serialNumber = :serialNumber " +
           "AND s.studentStatus IN ('10', '11', '13', '15')")
    Optional<Student> findActiveBySerialNumber(@Param("serialNumber") String serialNumber);

    /**
     * Find existing student by PINFL, education type and year (for returning existing record)
     *
     * @param pinfl PINFL raqami
     * @param educationType ta'lim turi kodi
     * @param educationYear ta'lim yili kodi
     * @return talaba yoki empty
     */
    @Query("SELECT s FROM Student s WHERE s.pinfl = :pinfl " +
           "AND s.educationType = :educationType " +
           "AND s.educationYear = :educationYear " +
           "AND s.studentStatus <> '12' " +
           "AND s.isDuplicate = false")
    Optional<Student> findExistingStudent(
            @Param("pinfl") String pinfl,
            @Param("educationType") String educationType,
            @Param("educationYear") String educationYear);

    /**
     * Find existing student by serial number for foreign citizens
     *
     * @param serialNumber passport serial number
     * @param citizenship fuqarolik kodi
     * @param educationType ta'lim turi kodi
     * @param educationYear ta'lim yili kodi
     * @return talaba yoki empty
     */
    @Query("SELECT s FROM Student s WHERE s.serialNumber = :serialNumber " +
           "AND s.citizenship = :citizenship " +
           "AND s.educationType = :educationType " +
           "AND s.educationYear = :educationYear " +
           "AND s.studentStatus <> '12'")
    Optional<Student> findExistingForeignStudent(
            @Param("serialNumber") String serialNumber,
            @Param("citizenship") String citizenship,
            @Param("educationType") String educationType,
            @Param("educationYear") String educationYear);

    /**
     * Count students for generating unique code
     *
     * @param universityCode universitet kodi
     * @param educationType ta'lim turi kodi
     * @param educationYear ta'lim yili kodi
     * @return talabalar soni
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.university = :university " +
           "AND s.educationType = :educationType " +
           "AND s.educationYear = :educationYear")
    long countForIdGeneration(
            @Param("university") String universityCode,
            @Param("educationType") String educationType,
            @Param("educationYear") String educationYear);

    /**
     * Check if student code exists
     *
     * @param code student unique code
     * @return true if exists
     */
    boolean existsByCode(String code);

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // The following inherited methods are available but PROHIBITED:
    // - void deleteById(UUID id)
    // - void delete(Student entity)
    // - void deleteAll()
    // - void deleteAllById(Iterable<? extends UUID> ids)
    //
    // These methods will FAIL at database level:
    // - Database role has NO DELETE permission
    // - Application enforces NDG (Non-Deletion Guarantee)
    //
    // For soft delete:
    // - Use service layer to set deleteTs = NOW()
    // - Queries automatically exclude deleted records (@Where clause)
    // =====================================================
}
