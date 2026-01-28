package uz.hemis.domain.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import uz.hemis.domain.entity.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Student Repository Tests
 *
 * <p><strong>@DataJpaTest</strong> - JPA repository testing slice:</p>
 * <ul>
 *   <li>Auto-configures in-memory H2 database</li>
 *   <li>Scans for @Entity classes</li>
 *   <li>Configures Spring Data JPA repositories</li>
 *   <li>Provides TestEntityManager for setup</li>
 *   <li>Transactions rollback after each test</li>
 * </ul>
 *
 * <p><strong>CRITICAL - Read-Only Tests:</strong></p>
 * <ul>
 *   <li>No DELETE operation tests (NDG - Non-Deletion Guarantee)</li>
 *   <li>Tests verify soft delete filtering (@Where clause)</li>
 *   <li>Focus on SELECT queries and business logic</li>
 * </ul>
 *
 * @since 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Student Repository Tests")
class StudentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRepository studentRepository;

    // =====================================================
    // Helper Methods
    // =====================================================

    private Student createTestStudent(String code, String pinfl, String universityCode) {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        student.setVersion(1);
        student.setCode(code);
        student.setPinfl(pinfl);
        student.setFirstname("Test");
        student.setLastname("Student");
        student.setFathername("Testovich");
        student.setUniversity(universityCode);
        student.setStudentStatus("11"); // Active
        student.setPaymentForm("11");   // Budget
        student.setEducationType("11"); // Bachelor
        student.setEducationForm("11"); // Full-time
        student.setCourse("1");
        student.setEducationYear("2024");
        student.setActive(true);
        student.setBirthday(LocalDate.of(2000, 1, 1));
        student.setCreateTs(LocalDateTime.now());
        student.setCreatedBy("test");
        return student;
    }

    // =====================================================
    // Basic CRUD Tests
    // =====================================================

    @Test
    @DisplayName("Should save and find student by ID")
    void shouldSaveAndFindById() {
        // Given
        Student student = createTestStudent("STU001", "12345678901234", "UNI001");

        // When
        Student saved = entityManager.persistAndFlush(student);
        entityManager.clear();

        Optional<Student> found = studentRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("STU001");
        assertThat(found.get().getPinfl()).isEqualTo("12345678901234");
    }

    @Test
    @DisplayName("Should find student by PINFL")
    void shouldFindByPinfl() {
        // Given
        Student student = createTestStudent("STU002", "98765432109876", "UNI001");
        entityManager.persistAndFlush(student);
        entityManager.clear();

        // When
        Optional<Student> found = studentRepository.findByPinfl("98765432109876");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("STU002");
    }

    @Test
    @DisplayName("Should find student by code")
    void shouldFindByCode() {
        // Given
        Student student = createTestStudent("STU003", "11111111111111", "UNI001");
        entityManager.persistAndFlush(student);
        entityManager.clear();

        // When
        Optional<Student> found = studentRepository.findByCode("STU003");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPinfl()).isEqualTo("11111111111111");
    }

    @Test
    @DisplayName("Should check if student exists by PINFL")
    void shouldCheckExistenceByPinfl() {
        // Given
        Student student = createTestStudent("STU004", "22222222222222", "UNI001");
        entityManager.persistAndFlush(student);

        // When
        boolean exists = studentRepository.existsByPinfl("22222222222222");
        boolean notExists = studentRepository.existsByPinfl("99999999999999");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    // =====================================================
    // University-based Query Tests
    // =====================================================

    @Test
    @DisplayName("Should find students by university")
    void shouldFindByUniversity() {
        // Given
        Student student1 = createTestStudent("STU005", "33333333333333", "UNI001");
        Student student2 = createTestStudent("STU006", "44444444444444", "UNI001");
        Student student3 = createTestStudent("STU007", "55555555555555", "UNI002");

        entityManager.persist(student1);
        entityManager.persist(student2);
        entityManager.persist(student3);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Student> uni001Students = studentRepository.findByUniversity("UNI001");
        List<Student> uni002Students = studentRepository.findByUniversity("UNI002");

        // Then
        assertThat(uni001Students).hasSize(2);
        assertThat(uni002Students).hasSize(1);
    }

    @Test
    @DisplayName("Should find active students by university")
    void shouldFindActiveByUniversity() {
        // Given
        Student activeStudent = createTestStudent("STU008", "66666666666666", "UNI001");
        activeStudent.setActive(true);

        Student inactiveStudent = createTestStudent("STU009", "77777777777777", "UNI001");
        inactiveStudent.setActive(false);

        entityManager.persist(activeStudent);
        entityManager.persist(inactiveStudent);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Student> activeStudents = studentRepository.findActiveByUniversity("UNI001");

        // Then
        assertThat(activeStudents).hasSize(1);
        assertThat(activeStudents.get(0).getCode()).isEqualTo("STU008");
    }

    @Test
    @DisplayName("Should count active students by university")
    void shouldCountActiveByUniversity() {
        // Given
        Student student1 = createTestStudent("STU010", "88888888888888", "UNI001");
        student1.setActive(true);

        Student student2 = createTestStudent("STU011", "99999999999999", "UNI001");
        student2.setActive(true);

        entityManager.persist(student1);
        entityManager.persist(student2);
        entityManager.flush();

        // When
        long count = studentRepository.countActiveByUniversity("UNI001");

        // Then
        assertThat(count).isEqualTo(2);
    }

    // =====================================================
    // Status-based Query Tests
    // =====================================================

    @Test
    @DisplayName("Should find students by student status")
    void shouldFindByStudentStatus() {
        // Given
        Student activeStudent = createTestStudent("STU012", "11122233344455", "UNI001");
        activeStudent.setStudentStatus("11"); // Active

        Student graduatedStudent = createTestStudent("STU013", "22233344455566", "UNI001");
        graduatedStudent.setStudentStatus("16"); // Graduated

        entityManager.persist(activeStudent);
        entityManager.persist(graduatedStudent);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Student> activeStudents = studentRepository.findByStudentStatus("11");
        List<Student> graduatedStudents = studentRepository.findByStudentStatus("16");

        // Then
        assertThat(activeStudents).hasSize(1);
        assertThat(graduatedStudents).hasSize(1);
    }

    @Test
    @DisplayName("Should find students by university and status")
    void shouldFindByUniversityAndStudentStatus() {
        // Given
        Student student = createTestStudent("STU014", "33344455566677", "UNI001");
        student.setStudentStatus("11");

        entityManager.persistAndFlush(student);
        entityManager.clear();

        // When
        List<Student> found = studentRepository.findByUniversityAndStudentStatus("UNI001", "11");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCode()).isEqualTo("STU014");
    }

    // =====================================================
    // Soft Delete Filter Test
    // =====================================================

    @Test
    @DisplayName("Should not find deleted students (@Where clause)")
    void shouldNotFindDeletedStudents() {
        // Given
        Student activeStudent = createTestStudent("STU015", "44455566677788", "UNI001");
        activeStudent.setDeleteTs(null);

        Student deletedStudent = createTestStudent("STU016", "55566677788899", "UNI001");
        deletedStudent.setDeleteTs(LocalDateTime.now());
        deletedStudent.setDeletedBy("test");

        entityManager.persist(activeStudent);
        entityManager.persist(deletedStudent);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Student> allStudents = studentRepository.findAll();
        Optional<Student> foundDeleted = studentRepository.findByCode("STU016");

        // Then
        assertThat(allStudents).hasSize(1); // Only active student
        assertThat(allStudents.get(0).getCode()).isEqualTo("STU015");
        assertThat(foundDeleted).isEmpty(); // Deleted student filtered out
    }

    // =====================================================
    // Stipend Eligibility Test
    // =====================================================

    @Test
    @DisplayName("Should find stipend eligible student")
    void shouldFindStipendEligibleStudent() {
        // Given
        Student eligibleStudent = createTestStudent("STU017", "66677788899900", "UNI001");
        eligibleStudent.setStudentStatus("11"); // Active
        eligibleStudent.setPaymentForm("11");   // Budget (eligible)

        Student ineligibleStudent = createTestStudent("STU018", "77788899900011", "UNI001");
        ineligibleStudent.setStudentStatus("11");
        ineligibleStudent.setPaymentForm("12");   // Contract
        ineligibleStudent.setSocialCategory("99"); // Not eligible

        entityManager.persist(eligibleStudent);
        entityManager.persist(ineligibleStudent);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Student> eligible = studentRepository.findStipendEligible("UNI001", "66677788899900");
        Optional<Student> ineligible = studentRepository.findStipendEligible("UNI001", "77788899900011");

        // Then
        assertThat(eligible).isPresent();
        assertThat(ineligible).isEmpty();
    }

    // =====================================================
    // NOTE: NO DELETE TESTS
    // =====================================================
    // Physical DELETE operations are PROHIBITED (NDG)
    // Database role prevents DELETE permission
    // Soft delete tested via deleteTs filtering above
    // =====================================================
}
