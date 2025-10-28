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
import uz.hemis.common.dto.SpecialtyDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Specialty;
import uz.hemis.domain.mapper.SpecialtyMapper;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SpecialtyService
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>CREATE: Success, validation failures (duplicate code, missing fields, non-existent university/faculty)</li>
 *   <li>READ: By ID, by code, pagination, filtering (university, faculty, education type/form, name, active)</li>
 *   <li>UPDATE: Success, validation (duplicate code, non-existent university/faculty)</li>
 *   <li>SOFT DELETE: Success, not found</li>
 *   <li>COUNT & EXISTENCE: By university, by faculty, by code</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private SpecialtyMapper specialtyMapper;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private SpecialtyService specialtyService;

    private Specialty testSpecialty;
    private SpecialtyDto testSpecialtyDto;
    private UUID testId;
    private UUID testFacultyId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testFacultyId = UUID.randomUUID();

        // Setup test entity
        testSpecialty = new Specialty();
        testSpecialty.setId(testId);
        testSpecialty.setCode("SPEC001");
        testSpecialty.setName("Computer Science");
        testSpecialty.setShortName("CS");
        testSpecialty.setUniversity("UNI001");
        testSpecialty.setFaculty(testFacultyId);
        testSpecialty.setSpecialtyType("SPEC_TYPE_01");
        testSpecialty.setEducationType("11");  // Bachelor
        testSpecialty.setEducationForm("11");  // Full-time
        testSpecialty.setStudyPeriod("4");
        testSpecialty.setActive(true);

        // Setup test DTO
        testSpecialtyDto = new SpecialtyDto();
        testSpecialtyDto.setId(testId);
        testSpecialtyDto.setCode("SPEC001");
        testSpecialtyDto.setName("Computer Science");
        testSpecialtyDto.setShortName("CS");
        testSpecialtyDto.setUniversity("UNI001");
        testSpecialtyDto.setFaculty(testFacultyId);
        testSpecialtyDto.setSpecialtyType("SPEC_TYPE_01");
        testSpecialtyDto.setEducationType("11");
        testSpecialtyDto.setEducationForm("11");
        testSpecialtyDto.setStudyPeriod("4");
        testSpecialtyDto.setActive(true);
    }

    // =====================================================
    // CREATE Tests
    // =====================================================

    @Test
    void create_WithValidData_ShouldReturnCreatedSpecialty() {
        // Arrange
        when(specialtyRepository.existsByCode("SPEC001")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);
        when(facultyRepository.existsById(testFacultyId)).thenReturn(true);
        when(specialtyMapper.toEntity(testSpecialtyDto)).thenReturn(testSpecialty);
        when(specialtyRepository.save(testSpecialty)).thenReturn(testSpecialty);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        SpecialtyDto result = specialtyService.create(testSpecialtyDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SPEC001");
        assertThat(result.getName()).isEqualTo("Computer Science");
        assertThat(result.getEducationType()).isEqualTo("11");

        verify(specialtyRepository).existsByCode("SPEC001");
        verify(universityRepository).existsByCode("UNI001");
        verify(facultyRepository).existsById(testFacultyId);
        verify(specialtyMapper).toEntity(testSpecialtyDto);
        verify(specialtyRepository).save(testSpecialty);
        verify(specialtyMapper).toDto(testSpecialty);
    }

    @Test
    void create_WithDuplicateCode_ShouldThrowValidationException() {
        // Arrange
        when(specialtyRepository.existsByCode("SPEC001")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.create(testSpecialtyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");

        verify(specialtyRepository).existsByCode("SPEC001");
        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void create_WithNullCode_ShouldThrowValidationException() {
        // Arrange
        testSpecialtyDto.setCode(null);

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.create(testSpecialtyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code is required");

        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void create_WithBlankCode_ShouldThrowValidationException() {
        // Arrange
        testSpecialtyDto.setCode("   ");

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.create(testSpecialtyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code is required");

        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void create_WithNullName_ShouldThrowValidationException() {
        // Arrange
        testSpecialtyDto.setName(null);
        when(specialtyRepository.existsByCode("SPEC001")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.create(testSpecialtyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name is required");

        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void create_WithNonExistentUniversity_ShouldThrowValidationException() {
        // Arrange
        when(specialtyRepository.existsByCode("SPEC001")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.create(testSpecialtyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("not found");

        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void create_WithNonExistentFaculty_ShouldThrowValidationException() {
        // Arrange
        when(specialtyRepository.existsByCode("SPEC001")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);
        when(facultyRepository.existsById(testFacultyId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.create(testSpecialtyDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Faculty")
                .hasMessageContaining("not found");

        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void create_WithNullUniversityAndFaculty_ShouldSucceed() {
        // Arrange
        testSpecialtyDto.setUniversity(null);
        testSpecialtyDto.setFaculty(null);
        testSpecialty.setUniversity(null);
        testSpecialty.setFaculty(null);

        when(specialtyRepository.existsByCode("SPEC001")).thenReturn(false);
        when(specialtyMapper.toEntity(testSpecialtyDto)).thenReturn(testSpecialty);
        when(specialtyRepository.save(testSpecialty)).thenReturn(testSpecialty);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        SpecialtyDto result = specialtyService.create(testSpecialtyDto);

        // Assert
        assertThat(result).isNotNull();
        verify(universityRepository, never()).existsByCode(any());
        verify(facultyRepository, never()).existsById(any());
    }

    // =====================================================
    // READ Tests
    // =====================================================

    @Test
    void findById_WithExistingId_ShouldReturnSpecialty() {
        // Arrange
        when(specialtyRepository.findById(testId)).thenReturn(Optional.of(testSpecialty));
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        SpecialtyDto result = specialtyService.findById(testId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getCode()).isEqualTo("SPEC001");

        verify(specialtyRepository).findById(testId);
        verify(specialtyMapper).toDto(testSpecialty);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(specialtyRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.findById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found");

        verify(specialtyRepository).findById(testId);
        verify(specialtyMapper, never()).toDto(any());
    }

    @Test
    void findByCode_WithExistingCode_ShouldReturnSpecialty() {
        // Arrange
        when(specialtyRepository.findByCode("SPEC001")).thenReturn(Optional.of(testSpecialty));
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        SpecialtyDto result = specialtyService.findByCode("SPEC001");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SPEC001");

        verify(specialtyRepository).findByCode("SPEC001");
        verify(specialtyMapper).toDto(testSpecialty);
    }

    @Test
    void findByCode_WithNonExistingCode_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(specialtyRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.findByCode("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found");

        verify(specialtyRepository).findByCode("INVALID");
    }

    @Test
    void findAll_ShouldReturnPageOfSpecialties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        Page<Specialty> page = new PageImpl<>(specialties, pageable, specialties.size());

        when(specialtyRepository.findAll(pageable)).thenReturn(page);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        Page<SpecialtyDto> result = specialtyService.findAll(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("SPEC001");

        verify(specialtyRepository).findAll(pageable);
    }

    @Test
    void findByUniversity_ShouldReturnPageOfSpecialties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        Page<Specialty> page = new PageImpl<>(specialties, pageable, specialties.size());

        when(specialtyRepository.findByUniversity("UNI001", pageable)).thenReturn(page);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        Page<SpecialtyDto> result = specialtyService.findByUniversity("UNI001", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(specialtyRepository).findByUniversity("UNI001", pageable);
    }

    @Test
    void findByFaculty_ShouldReturnPageOfSpecialties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        Page<Specialty> page = new PageImpl<>(specialties, pageable, specialties.size());

        when(specialtyRepository.findByFaculty(testFacultyId, pageable)).thenReturn(page);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        Page<SpecialtyDto> result = specialtyService.findByFaculty(testFacultyId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(specialtyRepository).findByFaculty(testFacultyId, pageable);
    }

    @Test
    void findAllByFaculty_ShouldReturnListOfSpecialties() {
        // Arrange
        List<Specialty> specialties = Arrays.asList(testSpecialty);

        when(specialtyRepository.findAllByFaculty(testFacultyId)).thenReturn(specialties);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        List<SpecialtyDto> result = specialtyService.findAllByFaculty(testFacultyId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("SPEC001");

        verify(specialtyRepository).findAllByFaculty(testFacultyId);
    }

    @Test
    void findByNameContaining_ShouldReturnPageOfSpecialties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        Page<Specialty> page = new PageImpl<>(specialties, pageable, specialties.size());

        when(specialtyRepository.findByNameContainingIgnoreCase("Computer", pageable)).thenReturn(page);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        Page<SpecialtyDto> result = specialtyService.findByNameContaining("Computer", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(specialtyRepository).findByNameContainingIgnoreCase("Computer", pageable);
    }

    @Test
    void findActive_ShouldReturnPageOfActiveSpecialties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        Page<Specialty> page = new PageImpl<>(specialties, pageable, specialties.size());

        when(specialtyRepository.findByActiveTrue(pageable)).thenReturn(page);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        Page<SpecialtyDto> result = specialtyService.findActive(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(specialtyRepository).findByActiveTrue(pageable);
    }

    @Test
    void findByEducationType_ShouldReturnPageOfSpecialties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        Page<Specialty> page = new PageImpl<>(specialties, pageable, specialties.size());

        when(specialtyRepository.findByEducationType("11", pageable)).thenReturn(page);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        Page<SpecialtyDto> result = specialtyService.findByEducationType("11", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEducationType()).isEqualTo("11");

        verify(specialtyRepository).findByEducationType("11", pageable);
    }

    @Test
    void findByEducationForm_ShouldReturnPageOfSpecialties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        Page<Specialty> page = new PageImpl<>(specialties, pageable, specialties.size());

        when(specialtyRepository.findByEducationForm("11", pageable)).thenReturn(page);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        Page<SpecialtyDto> result = specialtyService.findByEducationForm("11", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEducationForm()).isEqualTo("11");

        verify(specialtyRepository).findByEducationForm("11", pageable);
    }

    @Test
    void findByUniversityAndEducationType_ShouldReturnPageOfSpecialties() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Specialty> specialties = Arrays.asList(testSpecialty);
        Page<Specialty> page = new PageImpl<>(specialties, pageable, specialties.size());

        when(specialtyRepository.findByUniversityAndEducationType("UNI001", "11", pageable)).thenReturn(page);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        Page<SpecialtyDto> result = specialtyService.findByUniversityAndEducationType("UNI001", "11", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(specialtyRepository).findByUniversityAndEducationType("UNI001", "11", pageable);
    }

    // =====================================================
    // UPDATE Tests
    // =====================================================

    @Test
    void update_WithValidData_ShouldReturnUpdatedSpecialty() {
        // Arrange
        SpecialtyDto updateDto = new SpecialtyDto();
        updateDto.setName("Updated Specialty Name");

        when(specialtyRepository.findById(testId)).thenReturn(Optional.of(testSpecialty));
        when(specialtyRepository.save(testSpecialty)).thenReturn(testSpecialty);
        when(specialtyMapper.toDto(testSpecialty)).thenReturn(testSpecialtyDto);

        // Act
        SpecialtyDto result = specialtyService.update(testId, updateDto);

        // Assert
        assertThat(result).isNotNull();

        verify(specialtyRepository).findById(testId);
        verify(specialtyMapper).updateEntityFromDto(updateDto, testSpecialty);
        verify(specialtyRepository).save(testSpecialty);
        verify(specialtyMapper).toDto(testSpecialty);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        SpecialtyDto updateDto = new SpecialtyDto();
        when(specialtyRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.update(testId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found");

        verify(specialtyRepository).findById(testId);
        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void update_WithDuplicateCode_ShouldThrowValidationException() {
        // Arrange
        SpecialtyDto updateDto = new SpecialtyDto();
        updateDto.setCode("SPEC002");

        when(specialtyRepository.findById(testId)).thenReturn(Optional.of(testSpecialty));
        when(specialtyRepository.existsByCodeAndIdNot("SPEC002", testId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");

        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void update_WithNonExistentUniversity_ShouldThrowValidationException() {
        // Arrange
        SpecialtyDto updateDto = new SpecialtyDto();
        updateDto.setUniversity("UNI999");

        when(specialtyRepository.findById(testId)).thenReturn(Optional.of(testSpecialty));
        when(universityRepository.existsByCode("UNI999")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("not found");

        verify(specialtyRepository, never()).save(any());
    }

    @Test
    void update_WithNonExistentFaculty_ShouldThrowValidationException() {
        // Arrange
        UUID newFacultyId = UUID.randomUUID();
        SpecialtyDto updateDto = new SpecialtyDto();
        updateDto.setFaculty(newFacultyId);

        when(specialtyRepository.findById(testId)).thenReturn(Optional.of(testSpecialty));
        when(facultyRepository.existsById(newFacultyId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Faculty")
                .hasMessageContaining("not found");

        verify(specialtyRepository, never()).save(any());
    }

    // =====================================================
    // SOFT DELETE Tests
    // =====================================================

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        // Arrange
        when(specialtyRepository.findById(testId)).thenReturn(Optional.of(testSpecialty));
        when(specialtyRepository.save(testSpecialty)).thenReturn(testSpecialty);

        // Act
        specialtyService.softDelete(testId);

        // Assert
        assertThat(testSpecialty.getDeleteTs()).isNotNull();
        verify(specialtyRepository).findById(testId);
        verify(specialtyRepository).save(testSpecialty);
    }

    @Test
    void softDelete_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(specialtyRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> specialtyService.softDelete(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found");

        verify(specialtyRepository).findById(testId);
        verify(specialtyRepository, never()).save(any());
    }

    // =====================================================
    // COUNT & EXISTENCE Tests
    // =====================================================

    @Test
    void countByUniversity_ShouldReturnCount() {
        // Arrange
        when(specialtyRepository.countByUniversity("UNI001")).thenReturn(10L);

        // Act
        long count = specialtyService.countByUniversity("UNI001");

        // Assert
        assertThat(count).isEqualTo(10L);
        verify(specialtyRepository).countByUniversity("UNI001");
    }

    @Test
    void countByFaculty_ShouldReturnCount() {
        // Arrange
        when(specialtyRepository.countByFaculty(testFacultyId)).thenReturn(5L);

        // Act
        long count = specialtyService.countByFaculty(testFacultyId);

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(specialtyRepository).countByFaculty(testFacultyId);
    }

    @Test
    void existsByCode_WhenExists_ShouldReturnTrue() {
        // Arrange
        when(specialtyRepository.existsByCode("SPEC001")).thenReturn(true);

        // Act
        boolean exists = specialtyService.existsByCode("SPEC001");

        // Assert
        assertThat(exists).isTrue();
        verify(specialtyRepository).existsByCode("SPEC001");
    }

    @Test
    void existsByCode_WhenNotExists_ShouldReturnFalse() {
        // Arrange
        when(specialtyRepository.existsByCode("INVALID")).thenReturn(false);

        // Act
        boolean exists = specialtyService.existsByCode("INVALID");

        // Assert
        assertThat(exists).isFalse();
        verify(specialtyRepository).existsByCode("INVALID");
    }
}
