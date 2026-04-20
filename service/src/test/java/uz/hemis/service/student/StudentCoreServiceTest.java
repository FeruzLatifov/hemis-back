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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.hemis.common.dto.student.StudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.domain.repository.StudentRepository;
import uz.hemis.service.student.mapper.StudentMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentCoreService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>findById - found and not-found scenarios</li>
 *   <li>findByPinfl - found scenario</li>
 *   <li>create - new student creation</li>
 *   <li>update - existing student update</li>
 *   <li>softDelete - authorization checks, success and failure</li>
 *   <li>partialUpdate - partial field update</li>
 * </ul>
 *
 * @since 2.1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentCoreService Unit Tests")
class StudentCoreServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentCoreService studentCoreService;

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

            StudentDto result = studentCoreService.findById(studentId);

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

            assertThatThrownBy(() -> studentCoreService.findById(missingId))
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

            StudentDto result = studentCoreService.findByPinfl(pinfl);

            assertThat(result).isNotNull();
            assertThat(result.getPinfl()).isEqualTo(pinfl);

            verify(studentRepository).findMasterByPinfl(pinfl);
            verify(studentMapper).toDto(student);
        }
    }

    // =====================================================
    // create tests
    // =====================================================

    @Nested
    @DisplayName("create")
    class Create {

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

            StudentDto result = studentCoreService.create(inputDto);

            assertThat(result).isNotNull();
            verify(studentRepository).save(any(Student.class));
        }
    }

    // =====================================================
    // update tests
    // =====================================================

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates student successfully")
        void updatesSuccessfully() {
            StudentDto updateDto = new StudentDto();
            updateDto.setFirstname("Updated");
            updateDto.setCode("40124230001");

            Student updatedEntity = new Student();
            updatedEntity.setId(studentId);
            updatedEntity.setFirstname("Updated");

            StudentDto updatedDto = new StudentDto();
            updatedDto.setId(studentId);
            updatedDto.setFirstname("Updated");

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentRepository.save(student)).thenReturn(updatedEntity);
            when(studentMapper.toDto(updatedEntity)).thenReturn(updatedDto);

            StudentDto result = studentCoreService.update(studentId, updateDto);

            assertThat(result).isNotNull();
            assertThat(result.getFirstname()).isEqualTo("Updated");

            verify(studentMapper).updateEntityFromDto(updateDto, student);
            verify(studentRepository).save(student);
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

            StudentDto result = studentCoreService.softDelete(studentId, "401");

            assertThat(student.getDeleteTs()).isNotNull();
            assertThat(student.getDeletedBy()).isEqualTo("admin");
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("throws ValidationException when user from different university")
        void throwsValidation_whenUnauthorized() {
            student.setUniversity("999");

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

            assertThatThrownBy(() -> studentCoreService.softDelete(studentId, "401"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Access denied");

            verify(studentRepository, never()).save(any());
        }
    }

    // =====================================================
    // partialUpdate tests
    // =====================================================

    @Nested
    @DisplayName("partialUpdate")
    class PartialUpdate {

        @Test
        @DisplayName("partially updates student successfully")
        void updatesPartially() {
            StudentDto patchDto = new StudentDto();
            patchDto.setFirstname("Patched");

            Student patchedEntity = new Student();
            patchedEntity.setId(studentId);
            patchedEntity.setFirstname("Patched");

            StudentDto patchedDto = new StudentDto();
            patchedDto.setId(studentId);
            patchedDto.setFirstname("Patched");

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentRepository.save(student)).thenReturn(patchedEntity);
            when(studentMapper.toDto(patchedEntity)).thenReturn(patchedDto);

            StudentDto result = studentCoreService.partialUpdate(studentId, patchDto);

            assertThat(result).isNotNull();
            assertThat(result.getFirstname()).isEqualTo("Patched");

            verify(studentMapper).partialUpdate(patchDto, student);
            verify(studentRepository).save(student);
        }
    }
}
