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
import uz.hemis.common.dto.UniversityDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.mapper.UniversityMapper;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for UniversityService
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>Use Mockito to mock dependencies (repository, mapper)</li>
 *   <li>Test business logic in isolation</li>
 *   <li>Verify validation rules (code uniqueness, TIN uniqueness)</li>
 *   <li>Test exception scenarios</li>
 *   <li>Test VARCHAR Primary Key behavior</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityService Unit Tests")
class UniversityServiceTest {

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private UniversityMapper universityMapper;

    @InjectMocks
    private UniversityService universityService;

    private University testUniversity;
    private UniversityDto testUniversityDto;

    @BeforeEach
    void setUp() {
        // Create test university entity (VARCHAR PK)
        testUniversity = new University();
        testUniversity.setCode("UNI001");
        testUniversity.setTin("1234567890");
        testUniversity.setName("Test University");
        testUniversity.setAddress("123 Test Street");
        testUniversity.setUniversityType("11"); // State
        testUniversity.setOwnership("11"); // State
        testUniversity.setActive(true);

        // Create test university DTO
        testUniversityDto = new UniversityDto();
        testUniversityDto.setCode("UNI001");
        testUniversityDto.setTin("1234567890");
        testUniversityDto.setName("Test University");
        testUniversityDto.setAddress("123 Test Street");
        testUniversityDto.setUniversityType("11");
        testUniversityDto.setOwnership("11");
        testUniversityDto.setActive(true);
    }

    // =====================================================
    // Read Operations Tests
    // =====================================================

    @Test
    @DisplayName("findByCode - should return university when found")
    void findByCode_WhenUniversityExists_ShouldReturnUniversityDto() {
        // Given
        when(universityRepository.findById("UNI001")).thenReturn(Optional.of(testUniversity));
        when(universityMapper.toDto(testUniversity)).thenReturn(testUniversityDto);

        // When
        UniversityDto result = universityService.findByCode("UNI001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("UNI001");
        assertThat(result.getName()).isEqualTo("Test University");
        verify(universityRepository).findById("UNI001");
        verify(universityMapper).toDto(testUniversity);
    }

    @Test
    @DisplayName("findByCode - should throw ResourceNotFoundException when not found")
    void findByCode_WhenUniversityNotFound_ShouldThrowException() {
        // Given
        when(universityRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> universityService.findByCode("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("code")
                .hasMessageContaining("NONEXISTENT");

        verify(universityRepository).findById("NONEXISTENT");
        verify(universityMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("findByTin - should return university when found")
    void findByTin_WhenUniversityExists_ShouldReturnUniversityDto() {
        // Given
        when(universityRepository.findByTin("1234567890")).thenReturn(Optional.of(testUniversity));
        when(universityMapper.toDto(testUniversity)).thenReturn(testUniversityDto);

        // When
        UniversityDto result = universityService.findByTin("1234567890");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTin()).isEqualTo("1234567890");
        verify(universityRepository).findByTin("1234567890");
    }

    @Test
    @DisplayName("findAll - should return paginated universities")
    void findAll_ShouldReturnPageOfUniversities() {
        // Given
        List<University> universities = Arrays.asList(testUniversity);
        Page<University> page = new PageImpl<>(universities);
        Pageable pageable = PageRequest.of(0, 20);

        when(universityRepository.findAll(pageable)).thenReturn(page);
        when(universityMapper.toDto(testUniversity)).thenReturn(testUniversityDto);

        // When
        Page<UniversityDto> result = universityService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("UNI001");
        verify(universityRepository).findAll(pageable);
    }

    @Test
    @DisplayName("findByName - should return universities matching name")
    void findByName_ShouldReturnMatchingUniversities() {
        // Given
        List<University> universities = Arrays.asList(testUniversity);
        Page<University> page = new PageImpl<>(universities);
        Pageable pageable = PageRequest.of(0, 20);

        when(universityRepository.findByNameContainingIgnoreCase("Test", pageable)).thenReturn(page);
        when(universityMapper.toDto(testUniversity)).thenReturn(testUniversityDto);

        // When
        Page<UniversityDto> result = universityService.findByName("Test", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(universityRepository).findByNameContainingIgnoreCase("Test", pageable);
    }

    @Test
    @DisplayName("findActiveUniversities - should return only active universities")
    void findActiveUniversities_ShouldReturnActiveOnly() {
        // Given
        List<University> universities = Arrays.asList(testUniversity);
        Page<University> page = new PageImpl<>(universities);
        Pageable pageable = PageRequest.of(0, 20);

        when(universityRepository.findByActiveTrue(pageable)).thenReturn(page);
        when(universityMapper.toDto(testUniversity)).thenReturn(testUniversityDto);

        // When
        Page<UniversityDto> result = universityService.findActiveUniversities(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(dto -> dto.getActive() == true);
        verify(universityRepository).findByActiveTrue(pageable);
    }

    @Test
    @DisplayName("countActive - should return count of active universities")
    void countActive_ShouldReturnCount() {
        // Given
        when(universityRepository.countActiveUniversities()).thenReturn(150L);

        // When
        long count = universityService.countActive();

        // Then
        assertThat(count).isEqualTo(150L);
        verify(universityRepository).countActiveUniversities();
    }

    // =====================================================
    // Create Operation Tests
    // =====================================================

    @Test
    @DisplayName("create - should create university successfully")
    void create_WithValidData_ShouldCreateUniversity() {
        // Given
        when(universityRepository.existsByCode("UNI001")).thenReturn(false);
        when(universityRepository.findByTin("1234567890")).thenReturn(Optional.empty());
        when(universityMapper.toEntity(testUniversityDto)).thenReturn(testUniversity);
        when(universityRepository.save(testUniversity)).thenReturn(testUniversity);
        when(universityMapper.toDto(testUniversity)).thenReturn(testUniversityDto);

        // When
        UniversityDto result = universityService.create(testUniversityDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("UNI001");
        verify(universityRepository).existsByCode("UNI001");
        verify(universityRepository).save(testUniversity);
    }

    @Test
    @DisplayName("create - should throw ValidationException when code is null")
    void create_WithNullCode_ShouldThrowValidationException() {
        // Given
        testUniversityDto.setCode(null);

        // When/Then
        assertThatThrownBy(() -> universityService.create(testUniversityDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code is required");

        verify(universityRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should throw ValidationException when code already exists")
    void create_WithDuplicateCode_ShouldThrowValidationException() {
        // Given
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> universityService.create(testUniversityDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");

        verify(universityRepository).existsByCode("UNI001");
        verify(universityRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should throw ValidationException when TIN already exists")
    void create_WithDuplicateTin_ShouldThrowValidationException() {
        // Given
        University existingUniversity = new University();
        existingUniversity.setCode("UNI002");
        existingUniversity.setTin("1234567890");

        when(universityRepository.existsByCode("UNI001")).thenReturn(false);
        when(universityRepository.findByTin("1234567890")).thenReturn(Optional.of(existingUniversity));

        // When/Then
        assertThatThrownBy(() -> universityService.create(testUniversityDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("TIN")
                .hasMessageContaining("already exists");

        verify(universityRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should throw ValidationException when name is null")
    void create_WithNullName_ShouldThrowValidationException() {
        // Given
        testUniversityDto.setName(null);
        when(universityRepository.existsByCode("UNI001")).thenReturn(false);
        when(universityRepository.findByTin("1234567890")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> universityService.create(testUniversityDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name is required");

        verify(universityRepository, never()).save(any());
    }

    // =====================================================
    // Update Operation Tests
    // =====================================================

    @Test
    @DisplayName("update - should update university successfully")
    void update_WithValidData_ShouldUpdateUniversity() {
        // Given
        UniversityDto updateDto = new UniversityDto();
        updateDto.setName("Updated University");
        updateDto.setAddress("New Address");

        when(universityRepository.findById("UNI001")).thenReturn(Optional.of(testUniversity));
        when(universityRepository.save(testUniversity)).thenReturn(testUniversity);
        when(universityMapper.toDto(testUniversity)).thenReturn(testUniversityDto);

        // When
        UniversityDto result = universityService.update("UNI001", updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(universityRepository).findById("UNI001");
        verify(universityRepository).save(testUniversity);
        assertThat(testUniversity.getName()).isEqualTo("Updated University");
        assertThat(testUniversity.getAddress()).isEqualTo("New Address");
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when university not found")
    void update_WhenUniversityNotFound_ShouldThrowException() {
        // Given
        when(universityRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> universityService.update("NONEXISTENT", testUniversityDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("NONEXISTENT");

        verify(universityRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should throw ValidationException when TIN is duplicate")
    void update_WithDuplicateTin_ShouldThrowValidationException() {
        // Given
        University anotherUniversity = new University();
        anotherUniversity.setCode("UNI002");

        UniversityDto updateDto = new UniversityDto();
        updateDto.setTin("9876543210"); // Different TIN

        when(universityRepository.findById("UNI001")).thenReturn(Optional.of(testUniversity));
        when(universityRepository.existsByTinAndCodeNot("9876543210", "UNI001")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> universityService.update("UNI001", updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("TIN")
                .hasMessageContaining("already exists");

        verify(universityRepository, never()).save(any());
    }

    // =====================================================
    // Partial Update Tests
    // =====================================================

    @Test
    @DisplayName("partialUpdate - should update only non-null fields")
    void partialUpdate_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        UniversityDto partialDto = new UniversityDto();
        partialDto.setName("Partially Updated");
        // Other fields null

        when(universityRepository.findById("UNI001")).thenReturn(Optional.of(testUniversity));
        when(universityRepository.save(testUniversity)).thenReturn(testUniversity);
        when(universityMapper.toDto(testUniversity)).thenReturn(testUniversityDto);
        doNothing().when(universityMapper).updateEntityFromDto(partialDto, testUniversity);

        // When
        UniversityDto result = universityService.partialUpdate("UNI001", partialDto);

        // Then
        assertThat(result).isNotNull();
        verify(universityMapper).updateEntityFromDto(partialDto, testUniversity);
        verify(universityRepository).save(testUniversity);
    }

    // =====================================================
    // Soft Delete Tests
    // =====================================================

    @Test
    @DisplayName("softDelete - should set deleteTs timestamp")
    void softDelete_ShouldSetDeleteTimestamp() {
        // Given
        when(universityRepository.findById("UNI001")).thenReturn(Optional.of(testUniversity));
        when(universityRepository.save(testUniversity)).thenReturn(testUniversity);

        // When
        universityService.softDelete("UNI001");

        // Then
        assertThat(testUniversity.getDeleteTs()).isNotNull();
        verify(universityRepository).findById("UNI001");
        verify(universityRepository).save(testUniversity);
    }

    @Test
    @DisplayName("softDelete - should throw ResourceNotFoundException when not found")
    void softDelete_WhenUniversityNotFound_ShouldThrowException() {
        // Given
        when(universityRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> universityService.softDelete("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(universityRepository, never()).save(any());
    }

    @Test
    @DisplayName("restore - should clear deleteTs timestamp")
    void restore_ShouldClearDeleteTimestamp() {
        // Given
        testUniversity.setDeleteTs(LocalDateTime.now());
        when(universityRepository.findById("UNI001")).thenReturn(Optional.of(testUniversity));
        when(universityRepository.save(testUniversity)).thenReturn(testUniversity);

        // When
        universityService.restore("UNI001");

        // Then
        assertThat(testUniversity.getDeleteTs()).isNull();
        assertThat(testUniversity.getDeletedBy()).isNull();
        verify(universityRepository).save(testUniversity);
    }
}
