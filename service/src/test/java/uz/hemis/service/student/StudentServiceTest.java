package uz.hemis.service.student;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.hemis.common.dto.student.StudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Student;
import uz.hemis.domain.repository.StudentRepository;
import uz.hemis.service.student.mapper.StudentLegacyMapper;
import uz.hemis.service.student.mapper.StudentMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>findById - found and not-found scenarios</li>
 *   <li>findByPinfl - found and not-found scenarios</li>
 *   <li>findAll - paginated results</li>
 *   <li>findByUniversity - paginated results</li>
 *   <li>existsByPinfl - delegation to repository</li>
 *   <li>countActiveByUniversity - delegation to repository</li>
 *   <li>create - new student, existing master, existing in program</li>
 *   <li>softDelete - authorization checks, already deleted, not found</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Unit Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private StudentLegacyMapper studentLegacyMapper;

    @InjectMocks
    private StudentService studentService;

    private UUID studentId;
    private Student student;
    private StudentDto studentDto;

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

        studentDto = new StudentDto();
        studentDto.setId(studentId);
        studentDto.setCode("40124230001");
        studentDto.setPinfl("12345678901234");
        studentDto.setFirstname("Jasur");
        studentDto.setLastname("Karimov");
        studentDto.setUniversity("401");
        studentDto.setEducationType("23");
        studentDto.setEducationYear("2024");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =====================================================
    // findById tests
    // =====================================================

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns DTO when student found")
        void returnsDto_whenFound() {
            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            StudentDto result = studentService.findById(studentId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(studentId);
            assertThat(result.getCode()).isEqualTo("40124230001");
            assertThat(result.getPinfl()).isEqualTo("12345678901234");

            verify(studentRepository).findById(studentId);
            verify(studentMapper).toDto(student);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when student not found")
        void throwsException_whenNotFound() {
            UUID missingId = UUID.randomUUID();
            when(studentRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.findById(missingId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student")
                    .hasMessageContaining("id");

            verify(studentRepository).findById(missingId);
            verify(studentMapper, never()).toDto(any());
        }
    }

    // =====================================================
    // findByPinfl tests
    // =====================================================

    @Nested
    @DisplayName("findByPinfl")
    class FindByPinfl {

        @Test
        @DisplayName("returns DTO when master student found by PINFL")
        void returnsDto_whenMasterFound() {
            String pinfl = "12345678901234";
            when(studentRepository.findMasterByPinfl(pinfl)).thenReturn(Optional.of(student));
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            StudentDto result = studentService.findByPinfl(pinfl);

            assertThat(result).isNotNull();
            assertThat(result.getPinfl()).isEqualTo(pinfl);

            verify(studentRepository).findMasterByPinfl(pinfl);
            verify(studentMapper).toDto(student);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when PINFL not found")
        void throwsException_whenPinflNotFound() {
            String pinfl = "99999999999999";
            when(studentRepository.findMasterByPinfl(pinfl)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.findByPinfl(pinfl))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student")
                    .hasMessageContaining("pinfl");

            verify(studentRepository).findMasterByPinfl(pinfl);
            verify(studentMapper, never()).toDto(any());
        }
    }

    // =====================================================
    // findAllByPinfl tests
    // =====================================================

    @Nested
    @DisplayName("findAllByPinfl")
    class FindAllByPinfl {

        @Test
        @DisplayName("returns list of DTOs including duplicates")
        void returnsDtoList_includingDuplicates() {
            String pinfl = "12345678901234";
            Student duplicate = new Student();
            duplicate.setId(UUID.randomUUID());
            duplicate.setPinfl(pinfl);
            duplicate.setIsDuplicate(true);

            List<Student> students = List.of(student, duplicate);
            List<StudentDto> dtos = List.of(studentDto, new StudentDto());

            when(studentRepository.findAllByPinfl(pinfl)).thenReturn(students);
            when(studentMapper.toDtoList(students)).thenReturn(dtos);

            List<StudentDto> result = studentService.findAllByPinfl(pinfl);

            assertThat(result).hasSize(2);
            verify(studentRepository).findAllByPinfl(pinfl);
            verify(studentMapper).toDtoList(students);
        }
    }

    // =====================================================
    // findAll tests
    // =====================================================

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns paginated student DTOs")
        void returnsPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);

            when(studentRepository.findAll(pageable)).thenReturn(page);
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            Page<StudentDto> result = studentService.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(studentRepository).findAll(pageable);
        }

        @Test
        @DisplayName("returns empty page when no students exist")
        void returnsEmptyPage_whenNoStudents() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(studentRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<StudentDto> result = studentService.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    // =====================================================
    // findByUniversity tests
    // =====================================================

    @Nested
    @DisplayName("findByUniversity")
    class FindByUniversity {

        @Test
        @DisplayName("returns paginated students for university")
        void returnsPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);

            when(studentRepository.findByUniversity("401", pageable)).thenReturn(page);
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            Page<StudentDto> result = studentService.findByUniversity("401", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUniversity()).isEqualTo("401");

            verify(studentRepository).findByUniversity("401", pageable);
        }
    }

    // =====================================================
    // existsByPinfl tests
    // =====================================================

    @Nested
    @DisplayName("existsByPinfl")
    class ExistsByPinfl {

        @Test
        @DisplayName("returns true when master student exists")
        void returnsTrue_whenExists() {
            when(studentRepository.existsMasterByPinfl("12345678901234")).thenReturn(true);

            assertThat(studentService.existsByPinfl("12345678901234")).isTrue();

            verify(studentRepository).existsMasterByPinfl("12345678901234");
        }

        @Test
        @DisplayName("returns false when master student does not exist")
        void returnsFalse_whenNotExists() {
            when(studentRepository.existsMasterByPinfl("99999999999999")).thenReturn(false);

            assertThat(studentService.existsByPinfl("99999999999999")).isFalse();

            verify(studentRepository).existsMasterByPinfl("99999999999999");
        }
    }

    // =====================================================
    // countActiveByUniversity tests
    // =====================================================

    @Nested
    @DisplayName("countActiveByUniversity")
    class CountActiveByUniversity {

        @Test
        @DisplayName("delegates to repository and returns count")
        void delegatesToRepository() {
            when(studentRepository.countActiveByUniversity("401")).thenReturn(1500L);

            long count = studentService.countActiveByUniversity("401");

            assertThat(count).isEqualTo(1500L);
            verify(studentRepository).countActiveByUniversity("401");
        }

        @Test
        @DisplayName("returns zero for unknown university")
        void returnsZero_forUnknownUniversity() {
            when(studentRepository.countActiveByUniversity("NONEXIST")).thenReturn(0L);

            long count = studentService.countActiveByUniversity("NONEXIST");

            assertThat(count).isEqualTo(0L);
        }
    }

    // =====================================================
    // create tests
    // =====================================================

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("returns existing master when active master found for PINFL")
        void returnsExistingMaster_whenActiveMasterExists() {
            StudentDto inputDto = new StudentDto();
            inputDto.setPinfl("12345678901234");
            inputDto.setUniversity("401");
            inputDto.setEducationType("23");
            inputDto.setEducationYear("2024");

            when(studentRepository.findActiveMasterByPinfl("12345678901234"))
                    .thenReturn(Optional.of(student));
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            StudentDto result = studentService.create(inputDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(studentId);
            verify(studentRepository).findActiveMasterByPinfl("12345678901234");
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns existing student when duplicate found in same program")
        void returnsExistingStudent_whenDuplicateInProgram() {
            StudentDto inputDto = new StudentDto();
            inputDto.setPinfl("12345678901234");
            inputDto.setUniversity("401");
            inputDto.setEducationType("23");
            inputDto.setEducationYear("2024");

            when(studentRepository.findActiveMasterByPinfl("12345678901234"))
                    .thenReturn(Optional.empty());
            when(studentRepository.findExistingStudentForDuplicateCheck("12345678901234", "23", "2024"))
                    .thenReturn(Optional.of(student));
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            StudentDto result = studentService.create(inputDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(studentId);
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates new student when no existing found")
        void createsNewStudent_whenNoExisting() {
            StudentDto inputDto = new StudentDto();
            inputDto.setPinfl("99999999999999");
            inputDto.setUniversity("401");
            inputDto.setEducationType("23");
            inputDto.setEducationYear("2024");
            inputDto.setCode(null);

            Student newEntity = new Student();
            newEntity.setId(UUID.randomUUID());

            when(studentRepository.findActiveMasterByPinfl("99999999999999"))
                    .thenReturn(Optional.empty());
            when(studentRepository.findExistingStudentForDuplicateCheck("99999999999999", "23", "2024"))
                    .thenReturn(Optional.empty());
            when(studentRepository.countForIdGeneration("401", "23", "2024")).thenReturn(0L);
            when(studentRepository.existsByCode(anyString())).thenReturn(false);
            when(studentRepository.markPreviousMastersAsDuplicates("99999999999999")).thenReturn(0);
            when(studentMapper.toEntity(inputDto)).thenReturn(newEntity);
            when(studentRepository.save(any(Student.class))).thenReturn(newEntity);
            when(studentMapper.toDto(newEntity)).thenReturn(studentDto);

            StudentDto result = studentService.create(inputDto);

            assertThat(result).isNotNull();
            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("throws ValidationException when provided code already exists")
        void throwsValidation_whenCodeAlreadyExists() {
            StudentDto inputDto = new StudentDto();
            inputDto.setPinfl(null);
            inputDto.setUniversity("401");
            inputDto.setEducationType("23");
            inputDto.setEducationYear("2024");
            inputDto.setCode("EXISTING_CODE");

            when(studentRepository.existsByCode("EXISTING_CODE")).thenReturn(true);

            assertThatThrownBy(() -> studentService.create(inputDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("CODE already exists");

            verify(studentRepository, never()).save(any());
        }
    }

    // =====================================================
    // softDelete tests
    // =====================================================

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("sets deleteTs and deletedBy when authorized")
        void setsDeleteFields_whenAuthorized() {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("admin", null)
            );

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentRepository.save(student)).thenReturn(student);
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            StudentDto result = studentService.softDelete(studentId, "401");

            assertThat(student.getDeleteTs()).isNotNull();
            assertThat(student.getDeletedBy()).isEqualTo("admin");
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("throws ValidationException when user from different university")
        void throwsValidation_whenUnauthorized() {
            student.setUniversity("999");

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

            assertThatThrownBy(() -> studentService.softDelete(studentId, "401"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Access denied");

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("allows admin (null university) to delete any student")
        void allowsAdmin_whenNullUniversity() {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("superadmin", null)
            );

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentRepository.save(student)).thenReturn(student);
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            StudentDto result = studentService.softDelete(studentId, null);

            assertThat(student.getDeleteTs()).isNotNull();
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("skips save when student already deleted")
        void skips_whenAlreadyDeleted() {
            student.setDeleteTs(LocalDateTime.now().minusDays(1));
            student.setDeletedBy("previous-admin");

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentMapper.toDto(student)).thenReturn(studentDto);

            StudentDto result = studentService.softDelete(studentId, "401");

            assertThat(result).isNotNull();
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when student not found")
        void throwsException_whenNotFound() {
            UUID missingId = UUID.randomUUID();
            when(studentRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.softDelete(missingId, "401"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(studentRepository, never()).save(any());
        }
    }
}
