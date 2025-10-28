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
import uz.hemis.common.dto.TeacherDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Teacher;
import uz.hemis.domain.mapper.TeacherMapper;
import uz.hemis.domain.repository.TeacherRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TeacherService
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>Use Mockito to mock dependencies (repository, mapper, universityRepository)</li>
 *   <li>Test business logic in isolation</li>
 *   <li>Verify validation rules (firstname/lastname required, university exists)</li>
 *   <li>Test exception scenarios</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Unit Tests")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private UniversityRepository universityRepository;

    @InjectMocks
    private TeacherService teacherService;

    private UUID testId;
    private Teacher testTeacher;
    private TeacherDto testTeacherDto;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        // Create test teacher entity
        testTeacher = new Teacher();
        testTeacher.setId(testId);
        testTeacher.setFirstname("John");
        testTeacher.setLastname("Doe");
        testTeacher.setFathername("Smith");
        testTeacher.setBirthday(LocalDate.of(1980, 1, 1));
        testTeacher.setGender("11"); // Male
        testTeacher.setUniversity("UNI001");
        testTeacher.setAcademicDegree("12"); // Doctor
        testTeacher.setAcademicRank("14"); // Professor

        // Create test teacher DTO
        testTeacherDto = new TeacherDto();
        testTeacherDto.setId(testId);
        testTeacherDto.setFirstname("John");
        testTeacherDto.setLastname("Doe");
        testTeacherDto.setFathername("Smith");
        testTeacherDto.setBirthday(LocalDate.of(1980, 1, 1));
        testTeacherDto.setGender("11");
        testTeacherDto.setUniversity("UNI001");
        testTeacherDto.setAcademicDegree("12");
        testTeacherDto.setAcademicRank("14");
        testTeacherDto.setFullName("Doe John Smith");
    }

    // =====================================================
    // Read Operations Tests
    // =====================================================

    @Test
    @DisplayName("findById - should return teacher when found")
    void findById_WhenTeacherExists_ShouldReturnTeacherDto() {
        // Given
        when(teacherRepository.findById(testId)).thenReturn(Optional.of(testTeacher));
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);

        // When
        TeacherDto result = teacherService.findById(testId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getFirstname()).isEqualTo("John");
        assertThat(result.getLastname()).isEqualTo("Doe");
        verify(teacherRepository).findById(testId);
        verify(teacherMapper).toDto(testTeacher);
    }

    @Test
    @DisplayName("findById - should throw ResourceNotFoundException when not found")
    void findById_WhenTeacherNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(teacherRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> teacherService.findById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Teacher")
                .hasMessageContaining("id");

        verify(teacherRepository).findById(nonExistentId);
        verify(teacherMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("findAll - should return paginated teachers")
    void findAll_ShouldReturnPageOfTeachers() {
        // Given
        List<Teacher> teachers = Arrays.asList(testTeacher);
        Page<Teacher> page = new PageImpl<>(teachers);
        Pageable pageable = PageRequest.of(0, 20);

        when(teacherRepository.findAll(pageable)).thenReturn(page);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);

        // When
        Page<TeacherDto> result = teacherService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(testId);
        verify(teacherRepository).findAll(pageable);
    }

    @Test
    @DisplayName("findByUniversity - should return teachers for given university")
    void findByUniversity_ShouldReturnTeachersOfUniversity() {
        // Given
        List<Teacher> teachers = Arrays.asList(testTeacher);
        Page<Teacher> page = new PageImpl<>(teachers);
        Pageable pageable = PageRequest.of(0, 20);

        when(teacherRepository.findByUniversity("UNI001", pageable)).thenReturn(page);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);

        // When
        Page<TeacherDto> result = teacherService.findByUniversity("UNI001", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUniversity()).isEqualTo("UNI001");
        verify(teacherRepository).findByUniversity("UNI001", pageable);
    }

    @Test
    @DisplayName("findAllByUniversity - should return all teachers without pagination")
    void findAllByUniversity_ShouldReturnAllTeachers() {
        // Given
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherRepository.findAllByUniversity("UNI001")).thenReturn(teachers);
        when(teacherMapper.toDtoList(teachers)).thenReturn(Arrays.asList(testTeacherDto));

        // When
        List<TeacherDto> result = teacherService.findAllByUniversity("UNI001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(teacherRepository).findAllByUniversity("UNI001");
    }

    @Test
    @DisplayName("findByLastname - should return teachers matching lastname")
    void findByLastname_ShouldReturnMatchingTeachers() {
        // Given
        List<Teacher> teachers = Arrays.asList(testTeacher);
        Page<Teacher> page = new PageImpl<>(teachers);
        Pageable pageable = PageRequest.of(0, 20);

        when(teacherRepository.findByLastnameContainingIgnoreCase("Doe", pageable)).thenReturn(page);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);

        // When
        Page<TeacherDto> result = teacherService.findByLastname("Doe", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(teacherRepository).findByLastnameContainingIgnoreCase("Doe", pageable);
    }

    @Test
    @DisplayName("countByUniversity - should return count of teachers")
    void countByUniversity_ShouldReturnCount() {
        // Given
        when(teacherRepository.countByUniversity("UNI001")).thenReturn(50L);

        // When
        long count = teacherService.countByUniversity("UNI001");

        // Then
        assertThat(count).isEqualTo(50L);
        verify(teacherRepository).countByUniversity("UNI001");
    }

    @Test
    @DisplayName("countProfessorsByUniversity - should return count of professors")
    void countProfessorsByUniversity_ShouldReturnProfessorCount() {
        // Given
        when(teacherRepository.countProfessorsByUniversity("UNI001")).thenReturn(10L);

        // When
        long count = teacherService.countProfessorsByUniversity("UNI001");

        // Then
        assertThat(count).isEqualTo(10L);
        verify(teacherRepository).countProfessorsByUniversity("UNI001");
    }

    // =====================================================
    // Create Operation Tests
    // =====================================================

    @Test
    @DisplayName("create - should create teacher successfully")
    void create_WithValidData_ShouldCreateTeacher() {
        // Given
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);
        when(teacherMapper.toEntity(testTeacherDto)).thenReturn(testTeacher);
        when(teacherRepository.save(testTeacher)).thenReturn(testTeacher);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);

        // When
        TeacherDto result = teacherService.create(testTeacherDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstname()).isEqualTo("John");
        assertThat(result.getLastname()).isEqualTo("Doe");
        verify(universityRepository).existsByCode("UNI001");
        verify(teacherRepository).save(testTeacher);
    }

    @Test
    @DisplayName("create - should throw ValidationException when firstname is null")
    void create_WithNullFirstname_ShouldThrowValidationException() {
        // Given
        testTeacherDto.setFirstname(null);

        // When/Then
        assertThatThrownBy(() -> teacherService.create(testTeacherDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name is required");

        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should throw ValidationException when lastname is null")
    void create_WithNullLastname_ShouldThrowValidationException() {
        // Given
        testTeacherDto.setLastname(null);

        // When/Then
        assertThatThrownBy(() -> teacherService.create(testTeacherDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Last name is required");

        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should throw ValidationException when university does not exist")
    void create_WithNonExistentUniversity_ShouldThrowValidationException() {
        // Given
        when(universityRepository.existsByCode("UNI001")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> teacherService.create(testTeacherDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("not found");

        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should succeed when university is not provided")
    void create_WithoutUniversity_ShouldSucceed() {
        // Given
        testTeacherDto.setUniversity(null);
        when(teacherMapper.toEntity(testTeacherDto)).thenReturn(testTeacher);
        when(teacherRepository.save(testTeacher)).thenReturn(testTeacher);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);

        // When
        TeacherDto result = teacherService.create(testTeacherDto);

        // Then
        assertThat(result).isNotNull();
        verify(universityRepository, never()).existsByCode(any());
        verify(teacherRepository).save(testTeacher);
    }

    // =====================================================
    // Update Operation Tests
    // =====================================================

    @Test
    @DisplayName("update - should update teacher successfully")
    void update_WithValidData_ShouldUpdateTeacher() {
        // Given
        TeacherDto updateDto = new TeacherDto();
        updateDto.setFirstname("Jane");
        updateDto.setLastname("Smith");
        updateDto.setUniversity("UNI002");

        when(teacherRepository.findById(testId)).thenReturn(Optional.of(testTeacher));
        when(universityRepository.existsByCode("UNI002")).thenReturn(true);
        when(teacherRepository.save(testTeacher)).thenReturn(testTeacher);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);

        // When
        TeacherDto result = teacherService.update(testId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(teacherRepository).findById(testId);
        verify(universityRepository).existsByCode("UNI002");
        verify(teacherRepository).save(testTeacher);
        assertThat(testTeacher.getFirstname()).isEqualTo("Jane");
        assertThat(testTeacher.getLastname()).isEqualTo("Smith");
        assertThat(testTeacher.getUniversity()).isEqualTo("UNI002");
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when teacher not found")
    void update_WhenTeacherNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(teacherRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> teacherService.update(nonExistentId, testTeacherDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Teacher");

        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should throw ValidationException when university does not exist")
    void update_WithNonExistentUniversity_ShouldThrowValidationException() {
        // Given
        TeacherDto updateDto = new TeacherDto();
        updateDto.setUniversity("NONEXISTENT");

        when(teacherRepository.findById(testId)).thenReturn(Optional.of(testTeacher));
        when(universityRepository.existsByCode("NONEXISTENT")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> teacherService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("not found");

        verify(teacherRepository, never()).save(any());
    }

    // =====================================================
    // Partial Update Tests
    // =====================================================

    @Test
    @DisplayName("partialUpdate - should update only non-null fields")
    void partialUpdate_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        TeacherDto partialDto = new TeacherDto();
        partialDto.setAcademicRank("13"); // Docent
        // Other fields null

        when(teacherRepository.findById(testId)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.save(testTeacher)).thenReturn(testTeacher);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);
        doNothing().when(teacherMapper).updateEntityFromDto(partialDto, testTeacher);

        // When
        TeacherDto result = teacherService.partialUpdate(testId, partialDto);

        // Then
        assertThat(result).isNotNull();
        verify(teacherMapper).updateEntityFromDto(partialDto, testTeacher);
        verify(teacherRepository).save(testTeacher);
    }

    // =====================================================
    // Soft Delete Tests
    // =====================================================

    @Test
    @DisplayName("softDelete - should set deleteTs timestamp")
    void softDelete_ShouldSetDeleteTimestamp() {
        // Given
        when(teacherRepository.findById(testId)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.save(testTeacher)).thenReturn(testTeacher);

        // When
        teacherService.softDelete(testId);

        // Then
        assertThat(testTeacher.getDeleteTs()).isNotNull();
        verify(teacherRepository).findById(testId);
        verify(teacherRepository).save(testTeacher);
    }

    @Test
    @DisplayName("softDelete - should throw ResourceNotFoundException when not found")
    void softDelete_WhenTeacherNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(teacherRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> teacherService.softDelete(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("restore - should clear deleteTs timestamp")
    void restore_ShouldClearDeleteTimestamp() {
        // Given
        testTeacher.setDeleteTs(LocalDateTime.now());
        when(teacherRepository.findById(testId)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.save(testTeacher)).thenReturn(testTeacher);

        // When
        teacherService.restore(testId);

        // Then
        assertThat(testTeacher.getDeleteTs()).isNull();
        assertThat(testTeacher.getDeletedBy()).isNull();
        verify(teacherRepository).save(testTeacher);
    }
}
