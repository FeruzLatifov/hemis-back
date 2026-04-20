package uz.hemis.service.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import uz.hemis.common.dto.student.StudentIdRequest;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.domain.repository.StudentRepository;
import uz.hemis.service.student.mapper.StudentLegacyMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentEnrollmentService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>generateStudentId - new student creation, existing student return</li>
 *   <li>updateStudent - non-transfer saves, field updates</li>
 *   <li>validateStudent - active, not active, not found</li>
 * </ul>
 *
 * @since 2.1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentEnrollmentService Unit Tests")
class StudentEnrollmentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentLegacyMapper studentLegacyMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StudentEnrollmentService studentEnrollmentService;

    private UUID studentId;
    private Student student;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();

        student = new Student();
        student.setId(studentId);
        student.setCode("40124230001");
        student.setPinfl("12345678901234");
        student.setFirstname("Jasur");
        student.setLastname("Karimov");
        student.setUniversity("401");
        student.setEducationType("23");
        student.setEducationYear("2024");
        student.setStudentStatus("11");
        student.setIsDuplicate(false);
        student.setVerified(true);
        student.setPoints("85");
    }

    // =====================================================
    // generateStudentId tests
    // =====================================================

    @Nested
    @DisplayName("generateStudentId")
    class GenerateStudentId {

        @Test
        @DisplayName("creates new student and returns success when no existing found")
        @SuppressWarnings("unchecked")
        void createsNewStudent_whenNoExisting() {
            StudentIdRequest request = new StudentIdRequest();
            request.setCitizenship("11");
            request.setPinfl("12345678901234");
            request.setSerial("AA1234567");
            request.setYear("2024");
            request.setEducationType("11");
            request.setEducationForm("11");

            Map<String, Object> citizenshipRow = Map.of("code", "11");
            when(jdbcTemplate.queryForMap(anyString(), eq("11"))).thenReturn(citizenshipRow);

            // No active student found
            when(studentRepository.findActiveByPinflAndDuplicate("12345678901234", true))
                    .thenReturn(List.of());
            when(studentRepository.findActiveByPinfl("12345678901234"))
                    .thenReturn(List.of());
            when(studentRepository.findActiveBySerialNumber("12345678901234"))
                    .thenReturn(List.of());

            // No existing student found
            when(studentRepository.findExistingStudent("12345678901234", "11", "2024"))
                    .thenReturn(List.of());

            // Code generation
            when(studentRepository.countForIdGeneration("401", "11", "2024")).thenReturn(0L);
            when(studentRepository.existsByCode(anyString())).thenReturn(false);

            // Save
            Student savedStudent = new Student();
            savedStudent.setCode("40124110001");
            when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);
            when(studentRepository.markPreviousMastersAsDuplicates("12345678901234")).thenReturn(0);

            Map<String, Object> legacyMap = new LinkedHashMap<>();
            legacyMap.put("code", "40124110001");
            when(studentLegacyMapper.toLegacyMapForService(any(Student.class))).thenReturn(legacyMap);

            Map<String, Object> result = studentEnrollmentService.generateStudentId(request, "401");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("is_new")).isEqualTo(true);
            assertThat(result.get("unique_id")).isNotNull();

            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("returns existing student when found by PINFL and education type/year")
        @SuppressWarnings("unchecked")
        void returnsExisting_whenFoundByPinfl() {
            StudentIdRequest request = new StudentIdRequest();
            request.setCitizenship("11");
            request.setPinfl("12345678901234");
            request.setSerial("AA1234567");
            request.setYear("2024");
            request.setEducationType("23");
            request.setEducationForm("11");

            Map<String, Object> citizenshipRow = Map.of("code", "11");
            when(jdbcTemplate.queryForMap(anyString(), eq("11"))).thenReturn(citizenshipRow);

            // No active student
            when(studentRepository.findActiveByPinflAndDuplicate("12345678901234", true))
                    .thenReturn(List.of());
            when(studentRepository.findActiveByPinfl("12345678901234"))
                    .thenReturn(List.of());
            when(studentRepository.findActiveBySerialNumber("12345678901234"))
                    .thenReturn(List.of());

            // Existing student found
            when(studentRepository.findExistingStudent("12345678901234", "23", "2024"))
                    .thenReturn(List.of(student));

            Map<String, Object> legacyMap = new LinkedHashMap<>();
            legacyMap.put("code", "40124230001");
            when(studentLegacyMapper.toLegacyMapForService(student)).thenReturn(legacyMap);

            Map<String, Object> result = studentEnrollmentService.generateStudentId(request, "401");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("is_new")).isEqualTo(false);
            assertThat(result.get("unique_id")).isEqualTo("40124230001");

            verify(studentRepository, never()).save(any());
        }
    }

    // =====================================================
    // updateStudent tests
    // =====================================================

    @Nested
    @DisplayName("updateStudent")
    class UpdateStudent {

        @Test
        @DisplayName("updates student fields for non-transfer request")
        void savesFields_forNonTransfer() {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("id", studentId.toString());
            request.put("firstname", "UpdatedName");
            request.put("lastname", "UpdatedLast");

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentRepository.save(any(Student.class))).thenReturn(student);

            Object result = studentEnrollmentService.updateStudent(request);

            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(Map.class);

            verify(studentRepository).findById(studentId);
            verify(studentRepository).save(any(Student.class));
        }
    }

    // =====================================================
    // validateStudent tests
    // =====================================================

    @Nested
    @DisplayName("validateStudent")
    class ValidateStudent {

        @Test
        @DisplayName("returns active code when student has active status")
        @SuppressWarnings("unchecked")
        void returnsActive_whenStudentActive() {
            student.setStudentStatus("11");

            when(studentRepository.findByPinflOrSerialNumber("12345678901234"))
                    .thenReturn(List.of(student));

            Map<String, Object> legacyMap = new LinkedHashMap<>();
            legacyMap.put("code", "40124230001");
            when(studentLegacyMapper.toLegacyMapForService(student)).thenReturn(legacyMap);

            Map<String, Object> result = studentEnrollmentService.validateStudent("12345678901234");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("code")).isEqualTo("active");
            assertThat(result.get("message")).asString().contains("active");
        }

        @Test
        @DisplayName("returns not_active code when student not found")
        void returnsNotActive_whenStudentNotFound() {
            when(studentRepository.findByPinflOrSerialNumber("99999999999999"))
                    .thenReturn(List.of());

            Map<String, Object> result = studentEnrollmentService.validateStudent("99999999999999");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("code")).isEqualTo("not_active");
            assertThat(result.get("message")).asString().contains("not found");
        }
    }
}
