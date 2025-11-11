package uz.hemis.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uz.hemis.common.dto.FacultyDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Faculty;
import uz.hemis.domain.mapper.FacultyMapper;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FacultyService
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>CREATE: Success, validation failures (duplicate code, missing fields, non-existent university)</li>
 *   <li>READ: By ID, by code, pagination, filtering (university, name, active, type)</li>
 *   <li>UPDATE: Success, validation (duplicate code, non-existent university)</li>
 *   <li>SOFT DELETE: Success, not found</li>
 *   <li>COUNT & EXISTENCE: By university, by code</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private FacultyMapper facultyMapper;

    @Mock
    private UniversityRepository universityRepository;

    @InjectMocks
    private FacultyService facultyService;

    private Faculty testFaculty;
    private FacultyDto testFacultyDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        // Setup test entity
        testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setCode("FAC001");
        testFaculty.setName("Faculty of Computer Science");
        testFaculty.setShortName("FCS");
        testFaculty.setUniversity("UNI001");
        testFaculty.setFacultyType("FAC_TYPE_01");
        testFaculty.setActive(true);

        // Setup test DTO
        testFacultyDto = new FacultyDto();
        testFacultyDto.setId(testId);
        testFacultyDto.setCode("FAC001");
        testFacultyDto.setName("Faculty of Computer Science");
        testFacultyDto.setShortName("FCS");
        testFacultyDto.setUniversity("UNI001");
        testFacultyDto.setFacultyType("FAC_TYPE_01");
        testFacultyDto.setActive(true);
    }

    // =====================================================
    // CREATE Tests
    // =====================================================

    @Test
    void create_WithValidData_ShouldReturnCreatedFaculty() {
        // Arrange
        when(facultyRepository.existsByCode("FAC001")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);
        when(facultyMapper.toEntity(testFacultyDto)).thenReturn(testFaculty);
        when(facultyRepository.save(testFaculty)).thenReturn(testFaculty);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        FacultyDto result = facultyService.create(testFacultyDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("FAC001");
        assertThat(result.getName()).isEqualTo("Faculty of Computer Science");

        verify(facultyRepository).existsByCode("FAC001");
        verify(universityRepository).existsByCode("UNI001");
        verify(facultyMapper).toEntity(testFacultyDto);
        verify(facultyRepository).save(testFaculty);
        verify(facultyMapper).toDto(testFaculty);
    }

    @Test
    void create_WithDuplicateCode_ShouldThrowValidationException() {
        // Arrange
        when(facultyRepository.existsByCode("FAC001")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> facultyService.create(testFacultyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");

        verify(facultyRepository).existsByCode("FAC001");
        verify(facultyRepository, never()).save(any());
    }

    @Test
    void create_WithNullCode_ShouldThrowValidationException() {
        // Arrange
        testFacultyDto.setCode(null);

        // Act & Assert
        assertThatThrownBy(() -> facultyService.create(testFacultyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code is required");

        verify(facultyRepository, never()).save(any());
    }

    @Test
    void create_WithBlankCode_ShouldThrowValidationException() {
        // Arrange
        testFacultyDto.setCode("   ");

        // Act & Assert
        assertThatThrownBy(() -> facultyService.create(testFacultyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code is required");

        verify(facultyRepository, never()).save(any());
    }

    @Test
    void create_WithNullName_ShouldThrowValidationException() {
        // Arrange
        testFacultyDto.setName(null);
        when(facultyRepository.existsByCode("FAC001")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> facultyService.create(testFacultyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name is required");

        verify(facultyRepository, never()).save(any());
    }

    @Test
    void create_WithNonExistentUniversity_ShouldThrowValidationException() {
        // Arrange
        when(facultyRepository.existsByCode("FAC001")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> facultyService.create(testFacultyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("not found");

        verify(facultyRepository, never()).save(any());
    }

    @Test
    void create_WithNullUniversity_ShouldSucceed() {
        // Arrange
        testFacultyDto.setUniversity(null);
        testFaculty.setUniversity(null);

        when(facultyRepository.existsByCode("FAC001")).thenReturn(false);
        when(facultyMapper.toEntity(testFacultyDto)).thenReturn(testFaculty);
        when(facultyRepository.save(testFaculty)).thenReturn(testFaculty);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        FacultyDto result = facultyService.create(testFacultyDto);

        // Assert
        assertThat(result).isNotNull();
        verify(universityRepository, never()).existsByCode(any());
    }

    // =====================================================
    // READ Tests
    // =====================================================

    @Test
    void findById_WithExistingId_ShouldReturnFaculty() {
        // Arrange
        when(facultyRepository.findById(testId)).thenReturn(Optional.of(testFaculty));
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        FacultyDto result = facultyService.findById(testId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getCode()).isEqualTo("FAC001");

        verify(facultyRepository).findById(testId);
        verify(facultyMapper).toDto(testFaculty);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(facultyRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> facultyService.findById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Faculty not found");

        verify(facultyRepository).findById(testId);
        verify(facultyMapper, never()).toDto(any());
    }

    @Test
    void findByCode_WithExistingCode_ShouldReturnFaculty() {
        // Arrange
        when(facultyRepository.findByCode("FAC001")).thenReturn(Optional.of(testFaculty));
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        FacultyDto result = facultyService.findByCode("FAC001");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("FAC001");

        verify(facultyRepository).findByCode("FAC001");
        verify(facultyMapper).toDto(testFaculty);
    }

    @Test
    void findByCode_WithNonExistingCode_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(facultyRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> facultyService.findByCode("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Faculty not found");

        verify(facultyRepository).findByCode("INVALID");
    }

    @Test
    void findAll_ShouldReturnPageOfFaculties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Faculty> faculties = Arrays.asList(testFaculty);
        Page<Faculty> page = new PageImpl<>(faculties, pageable, faculties.size());

        when(facultyRepository.findAll(pageable)).thenReturn(page);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        Page<FacultyDto> result = facultyService.findAll(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("FAC001");

        verify(facultyRepository).findAll(pageable);
    }

    @Test
    void findByUniversity_ShouldReturnPageOfFaculties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Faculty> faculties = Arrays.asList(testFaculty);
        Page<Faculty> page = new PageImpl<>(faculties, pageable, faculties.size());

        when(facultyRepository.findByUniversity("UNI001", pageable)).thenReturn(page);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        Page<FacultyDto> result = facultyService.findByUniversity("UNI001", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(facultyRepository).findByUniversity("UNI001", pageable);
    }

    @Test
    void findAllByUniversity_ShouldReturnListOfFaculties() {
        // Arrange
        List<Faculty> faculties = Arrays.asList(testFaculty);

        when(facultyRepository.findAllByUniversity("UNI001")).thenReturn(faculties);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        List<FacultyDto> result = facultyService.findAllByUniversity("UNI001");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("FAC001");

        verify(facultyRepository).findAllByUniversity("UNI001");
    }

    @Test
    void findByNameContaining_ShouldReturnPageOfFaculties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Faculty> faculties = Arrays.asList(testFaculty);
        Page<Faculty> page = new PageImpl<>(faculties, pageable, faculties.size());

        when(facultyRepository.findByNameContainingIgnoreCase("Computer", pageable)).thenReturn(page);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        Page<FacultyDto> result = facultyService.findByNameContaining("Computer", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(facultyRepository).findByNameContainingIgnoreCase("Computer", pageable);
    }

    @Test
    void findActive_ShouldReturnPageOfActiveFaculties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Faculty> faculties = Arrays.asList(testFaculty);
        Page<Faculty> page = new PageImpl<>(faculties, pageable, faculties.size());

        when(facultyRepository.findByActiveTrue(pageable)).thenReturn(page);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        Page<FacultyDto> result = facultyService.findActive(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(facultyRepository).findByActiveTrue(pageable);
    }

    @Test
    void findByType_ShouldReturnPageOfFaculties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Faculty> faculties = Arrays.asList(testFaculty);
        Page<Faculty> page = new PageImpl<>(faculties, pageable, faculties.size());

        when(facultyRepository.findByFacultyType("FAC_TYPE_01", pageable)).thenReturn(page);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        Page<FacultyDto> result = facultyService.findByType("FAC_TYPE_01", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(facultyRepository).findByFacultyType("FAC_TYPE_01", pageable);
    }

    // =====================================================
    // UPDATE Tests
    // =====================================================

    @Test
    void update_WithValidData_ShouldReturnUpdatedFaculty() {
        // Arrange
        FacultyDto updateDto = new FacultyDto();
        updateDto.setName("Updated Faculty Name");

        when(facultyRepository.findById(testId)).thenReturn(Optional.of(testFaculty));
        when(facultyRepository.save(testFaculty)).thenReturn(testFaculty);
        when(facultyMapper.toDto(testFaculty)).thenReturn(testFacultyDto);

        // Act
        FacultyDto result = facultyService.update(testId, updateDto);

        // Assert
        assertThat(result).isNotNull();

        verify(facultyRepository).findById(testId);
        verify(facultyMapper).updateEntityFromDto(updateDto, testFaculty);
        verify(facultyRepository).save(testFaculty);
        verify(facultyMapper).toDto(testFaculty);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        FacultyDto updateDto = new FacultyDto();
        when(facultyRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> facultyService.update(testId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Faculty not found");

        verify(facultyRepository).findById(testId);
        verify(facultyRepository, never()).save(any());
    }

    @Test
    void update_WithDuplicateCode_ShouldThrowValidationException() {
        // Arrange
        FacultyDto updateDto = new FacultyDto();
        updateDto.setCode("FAC002");

        when(facultyRepository.findById(testId)).thenReturn(Optional.of(testFaculty));
        when(facultyRepository.existsByCodeAndIdNot("FAC002", testId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> facultyService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");

        verify(facultyRepository, never()).save(any());
    }

    @Test
    void update_WithNonExistentUniversity_ShouldThrowValidationException() {
        // Arrange
        FacultyDto updateDto = new FacultyDto();
        updateDto.setUniversity("UNI999");

        when(facultyRepository.findById(testId)).thenReturn(Optional.of(testFaculty));
        when(universityRepository.existsByCode("UNI999")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> facultyService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("not found");

        verify(facultyRepository, never()).save(any());
    }

    // =====================================================
    // SOFT DELETE Tests
    // =====================================================

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        // Arrange
        when(facultyRepository.findById(testId)).thenReturn(Optional.of(testFaculty));
        when(facultyRepository.save(testFaculty)).thenReturn(testFaculty);

        // Act
        facultyService.softDelete(testId);

        // Assert
        assertThat(testFaculty.getDeleteTs()).isNotNull();
        verify(facultyRepository).findById(testId);
        verify(facultyRepository).save(testFaculty);
    }

    @Test
    void softDelete_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(facultyRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> facultyService.softDelete(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Faculty not found");

        verify(facultyRepository).findById(testId);
        verify(facultyRepository, never()).save(any());
    }

    // =====================================================
    // COUNT & EXISTENCE Tests
    // =====================================================

    @Test
    void countByUniversity_ShouldReturnCount() {
        // Arrange
        when(facultyRepository.countByUniversity("UNI001")).thenReturn(5L);

        // Act
        long count = facultyService.countByUniversity("UNI001");

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(facultyRepository).countByUniversity("UNI001");
    }

    @Test
    void existsByCode_WhenExists_ShouldReturnTrue() {
        // Arrange
        when(facultyRepository.existsByCode("FAC001")).thenReturn(true);

        // Act
        boolean exists = facultyService.existsByCode("FAC001");

        // Assert
        assertThat(exists).isTrue();
        verify(facultyRepository).existsByCode("FAC001");
    }

    @Test
    void existsByCode_WhenNotExists_ShouldReturnFalse() {
        // Arrange
        when(facultyRepository.existsByCode("INVALID")).thenReturn(false);

        // Act
        boolean exists = facultyService.existsByCode("INVALID");

        // Assert
        assertThat(exists).isFalse();
        verify(facultyRepository).existsByCode("INVALID");
    }
}
