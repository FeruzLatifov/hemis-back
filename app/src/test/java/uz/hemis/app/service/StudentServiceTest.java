package uz.hemis.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uz.hemis.common.dto.StudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Student;
import uz.hemis.domain.mapper.StudentMapper;
import uz.hemis.domain.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for StudentService
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>Use Mockito to mock dependencies (repository, mapper)</li>
 *   <li>Test business logic in isolation</li>
 *   <li>Verify validation rules</li>
 *   <li>Test exception scenarios</li>
 * </ul>
 *
 * <p><strong>Coverage:</strong></p>
 * <ul>
 *   <li>Read operations (findById, findByPinfl, findAll, etc.)</li>
 *   <li>Write operations (create, update, partialUpdate)</li>
 *   <li>Soft delete and restore</li>
 *   <li>Validation (PINFL uniqueness)</li>
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

    @InjectMocks
    private StudentService studentService;

    private UUID testId;
    private Student testStudent;
    private StudentDto testStudentDto;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        // Create test student entity
        testStudent = new Student();
        testStudent.setId(testId);
        testStudent.setCode("STU001");
        testStudent.setPinfl("12345678901234");
        testStudent.setUniversity("UNI001");
        testStudent.setFirstname("John");
        testStudent.setLastname("Doe");
        testStudent.setFathername("Smith");

        // Create test student DTO
        testStudentDto = new StudentDto();
        testStudentDto.setId(testId);
        testStudentDto.setCode("STU001");
        testStudentDto.setPinfl("12345678901234");
        testStudentDto.setUniversity("UNI001");
        testStudentDto.setFirstname("John");
        testStudentDto.setLastname("Doe");
        testStudentDto.setFathername("Smith");
    }

    // =====================================================
    // Read Operations Tests
    // =====================================================

    @Test
    @DisplayName("findById - should return student when found")
    void findById_WhenStudentExists_ShouldReturnStudentDto() {
        // Given
        when(studentRepository.findById(testId)).thenReturn(Optional.of(testStudent));
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        // When
        StudentDto result = studentService.findById(testId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getCode()).isEqualTo("STU001");
        assertThat(result.getPinfl()).isEqualTo("12345678901234");

        verify(studentRepository, times(1)).findById(testId);
        verify(studentMapper, times(1)).toDto(testStudent);
    }

    @Test
    @DisplayName("findById - should throw ResourceNotFoundException when not found")
    void findById_WhenStudentNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(studentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.findById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student")
                .hasMessageContaining("id")
                .hasMessageContaining(nonExistentId.toString());

        verify(studentRepository, times(1)).findById(nonExistentId);
        verify(studentMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("findByPinfl - should return student when found")
    void findByPinfl_WhenStudentExists_ShouldReturnStudentDto() {
        // Given
        String pinfl = "12345678901234";
        when(studentRepository.findByPinfl(pinfl)).thenReturn(Optional.of(testStudent));
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        // When
        StudentDto result = studentService.findByPinfl(pinfl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPinfl()).isEqualTo(pinfl);

        verify(studentRepository, times(1)).findByPinfl(pinfl);
        verify(studentMapper, times(1)).toDto(testStudent);
    }

    @Test
    @DisplayName("findByPinfl - should throw ResourceNotFoundException when not found")
    void findByPinfl_WhenStudentNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        String nonExistentPinfl = "99999999999999";
        when(studentRepository.findByPinfl(nonExistentPinfl)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.findByPinfl(nonExistentPinfl))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student")
                .hasMessageContaining("pinfl");

        verify(studentRepository, times(1)).findByPinfl(nonExistentPinfl);
    }

    @Test
    @DisplayName("findAll - should return paginated students")
    void findAll_ShouldReturnPagedStudents() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Student> students = Arrays.asList(testStudent);
        Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

        when(studentRepository.findAll(pageable)).thenReturn(studentPage);
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        // When
        Page<StudentDto> result = studentService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(testId);

        verify(studentRepository, times(1)).findAll(pageable);
        verify(studentMapper, times(1)).toDto(testStudent);
    }

    @Test
    @DisplayName("findByUniversity - should return students filtered by university")
    void findByUniversity_ShouldReturnFilteredStudents() {
        // Given
        String universityCode = "UNI001";
        Pageable pageable = PageRequest.of(0, 20);
        List<Student> students = Arrays.asList(testStudent);
        Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

        when(studentRepository.findByUniversity(universityCode, pageable)).thenReturn(studentPage);
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        // When
        Page<StudentDto> result = studentService.findByUniversity(universityCode, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUniversity()).isEqualTo(universityCode);

        verify(studentRepository, times(1)).findByUniversity(universityCode, pageable);
    }

    @Test
    @DisplayName("findActiveByUniversity - should return active students only")
    void findActiveByUniversity_ShouldReturnActiveStudents() {
        // Given
        String universityCode = "UNI001";
        List<Student> students = Arrays.asList(testStudent);

        when(studentRepository.findActiveByUniversity(universityCode)).thenReturn(students);
        when(studentMapper.toDtoList(students)).thenReturn(Arrays.asList(testStudentDto));

        // When
        List<StudentDto> result = studentService.findActiveByUniversity(universityCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUniversity()).isEqualTo(universityCode);

        verify(studentRepository, times(1)).findActiveByUniversity(universityCode);
        verify(studentMapper, times(1)).toDtoList(students);
    }

    @Test
    @DisplayName("existsByPinfl - should return true when student exists")
    void existsByPinfl_WhenStudentExists_ShouldReturnTrue() {
        // Given
        String pinfl = "12345678901234";
        when(studentRepository.existsByPinfl(pinfl)).thenReturn(true);

        // When
        boolean result = studentService.existsByPinfl(pinfl);

        // Then
        assertThat(result).isTrue();
        verify(studentRepository, times(1)).existsByPinfl(pinfl);
    }

    // =====================================================
    // Write Operations Tests
    // =====================================================

    @Test
    @DisplayName("create - should create student successfully")
    void create_WithValidData_ShouldCreateStudent() {
        // Given
        when(studentRepository.existsByPinfl(testStudentDto.getPinfl())).thenReturn(false);
        when(studentMapper.toEntity(testStudentDto)).thenReturn(testStudent);
        when(studentRepository.save(testStudent)).thenReturn(testStudent);
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        // When
        StudentDto result = studentService.create(testStudentDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("STU001");

        verify(studentRepository, times(1)).existsByPinfl(testStudentDto.getPinfl());
        verify(studentMapper, times(1)).toEntity(testStudentDto);
        verify(studentRepository, times(1)).save(testStudent);
        verify(studentMapper, times(1)).toDto(testStudent);
    }

    @Test
    @DisplayName("create - should throw ValidationException when PINFL already exists")
    void create_WithDuplicatePinfl_ShouldThrowValidationException() {
        // Given
        when(studentRepository.existsByPinfl(testStudentDto.getPinfl())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> studentService.create(testStudentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("PINFL")
                .hasMessageContaining("already exists");

        verify(studentRepository, times(1)).existsByPinfl(testStudentDto.getPinfl());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should update student successfully")
    void update_WithValidData_ShouldUpdateStudent() {
        // Given
        StudentDto updateDto = new StudentDto();
        updateDto.setFirstname("Jane");
        updateDto.setLastname("Smith");
        updateDto.setPinfl("12345678901234"); // Same PINFL

        when(studentRepository.findById(testId)).thenReturn(Optional.of(testStudent));
        when(studentRepository.existsByPinfl(updateDto.getPinfl())).thenReturn(false);
        doNothing().when(studentMapper).updateEntityFromDto(updateDto, testStudent);
        when(studentRepository.save(testStudent)).thenReturn(testStudent);
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        // When
        StudentDto result = studentService.update(testId, updateDto);

        // Then
        assertThat(result).isNotNull();

        verify(studentRepository, times(1)).findById(testId);
        verify(studentMapper, times(1)).updateEntityFromDto(updateDto, testStudent);
        verify(studentRepository, times(1)).save(testStudent);
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when student not found")
    void update_WhenStudentNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(studentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.update(nonExistentId, testStudentDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student")
                .hasMessageContaining("id");

        verify(studentRepository, times(1)).findById(nonExistentId);
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should throw ValidationException when changing PINFL to existing one")
    void update_WithDuplicatePinfl_ShouldThrowValidationException() {
        // Given
        StudentDto updateDto = new StudentDto();
        updateDto.setPinfl("99999999999999"); // Different PINFL

        when(studentRepository.findById(testId)).thenReturn(Optional.of(testStudent));
        when(studentRepository.existsByPinfl(updateDto.getPinfl())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> studentService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("PINFL")
                .hasMessageContaining("already exists");

        verify(studentRepository, times(1)).findById(testId);
        verify(studentRepository, times(1)).existsByPinfl(updateDto.getPinfl());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("partialUpdate - should update only provided fields")
    void partialUpdate_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        StudentDto partialDto = new StudentDto();
        partialDto.setFirstname("Jane");
        // Other fields are null

        when(studentRepository.findById(testId)).thenReturn(Optional.of(testStudent));
        doNothing().when(studentMapper).partialUpdate(partialDto, testStudent);
        when(studentRepository.save(testStudent)).thenReturn(testStudent);
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        // When
        StudentDto result = studentService.partialUpdate(testId, partialDto);

        // Then
        assertThat(result).isNotNull();

        verify(studentRepository, times(1)).findById(testId);
        verify(studentMapper, times(1)).partialUpdate(partialDto, testStudent);
        verify(studentRepository, times(1)).save(testStudent);
    }

    // =====================================================
    // Soft Delete Tests
    // =====================================================

    @Test
    @DisplayName("softDelete - should set deleteTs timestamp")
    void softDelete_WhenStudentExists_ShouldSetDeleteTimestamp() {
        // Given
        when(studentRepository.findById(testId)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(testStudent)).thenReturn(testStudent);

        // When
        studentService.softDelete(testId);

        // Then
        assertThat(testStudent.getDeleteTs()).isNotNull();
        assertThat(testStudent.getDeleteTs()).isBeforeOrEqualTo(LocalDateTime.now());

        verify(studentRepository, times(1)).findById(testId);
        verify(studentRepository, times(1)).save(testStudent);
    }

    @Test
    @DisplayName("softDelete - should throw ResourceNotFoundException when student not found")
    void softDelete_WhenStudentNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(studentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.softDelete(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student");

        verify(studentRepository, times(1)).findById(nonExistentId);
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("softDelete - should not fail when student already deleted")
    void softDelete_WhenAlreadyDeleted_ShouldNotFail() {
        // Given
        testStudent.setDeleteTs(LocalDateTime.now().minusDays(1));
        when(studentRepository.findById(testId)).thenReturn(Optional.of(testStudent));

        // When
        studentService.softDelete(testId);

        // Then
        verify(studentRepository, times(1)).findById(testId);
        verify(studentRepository, never()).save(any()); // Should not save if already deleted
    }

    @Test
    @DisplayName("restore - should clear deleteTs timestamp")
    void restore_WhenStudentDeleted_ShouldClearDeleteTimestamp() {
        // Given
        testStudent.setDeleteTs(LocalDateTime.now().minusDays(1));
        testStudent.setDeletedBy("admin");

        when(studentRepository.findById(testId)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(testStudent)).thenReturn(testStudent);

        // When
        studentService.restore(testId);

        // Then
        assertThat(testStudent.getDeleteTs()).isNull();
        assertThat(testStudent.getDeletedBy()).isNull();

        verify(studentRepository, times(1)).findById(testId);
        verify(studentRepository, times(1)).save(testStudent);
    }

    @Test
    @DisplayName("restore - should not fail when student not deleted")
    void restore_WhenNotDeleted_ShouldNotFail() {
        // Given
        testStudent.setDeleteTs(null); // Not deleted
        when(studentRepository.findById(testId)).thenReturn(Optional.of(testStudent));

        // When
        studentService.restore(testId);

        // Then
        verify(studentRepository, times(1)).findById(testId);
        verify(studentRepository, never()).save(any()); // Should not save if not deleted
    }

    // =====================================================
    // NOTE: NO PHYSICAL DELETE TESTS
    // =====================================================
    // Physical DELETE is prohibited (NDG)
    // Only soft delete is tested
    // =====================================================
}
