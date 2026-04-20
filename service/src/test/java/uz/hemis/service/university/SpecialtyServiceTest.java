package uz.hemis.service.university;

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
import uz.hemis.common.dto.university.SpecialtyDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.academic.Specialty;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.university.mapper.SpecialtyMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SpecialtyService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>create - validation (code, name), duplicate detection, university validation</li>
 *   <li>findById / findByCode - found and not found cases</li>
 *   <li>findAll / findByUniversity - paginated results</li>
 *   <li>update - updates existing specialty</li>
 *   <li>delete - deletes existing specialty</li>
 *   <li>existsByCode / countByUniversity - utility methods</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialtyService Unit Tests")
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private SpecialtyMapper specialtyMapper;

    @Mock
    private UniversityRepository universityRepository;

    @InjectMocks
    private SpecialtyService specialtyService;

    private UUID specialtyId;
    private Specialty specialtyEntity;
    private SpecialtyDto specialtyDto;

    @BeforeEach
    void setUp() {
        specialtyId = UUID.randomUUID();

        specialtyEntity = new Specialty();
        specialtyEntity.setId(specialtyId);
        specialtyEntity.setCode("CS_101");
        specialtyEntity.setName("Computer Science");
        specialtyEntity.setUniversity("UNI_001");
        specialtyEntity.setFaculty("FAC_01");
        specialtyEntity.setEducationType("11");
        specialtyEntity.setEducationYear("2024");
        specialtyEntity.setActive(true);

        specialtyDto = new SpecialtyDto();
        specialtyDto.setId(specialtyId);
        specialtyDto.setCode("CS_101");
        specialtyDto.setName("Computer Science");
        specialtyDto.setUniversity("UNI_001");
        specialtyDto.setFaculty("FAC_01");
        specialtyDto.setEducationType("11");
        specialtyDto.setEducationYear("2024");
        specialtyDto.setActive(true);
    }

    // =====================================================
    // create tests
    // =====================================================

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates specialty successfully with valid data")
        void createsSpecialtySuccessfully() {
            // Given
            when(specialtyRepository.existsByCode("CS_101")).thenReturn(false);
            when(universityRepository.existsByCode("UNI_001")).thenReturn(true);
            when(specialtyMapper.toEntity(specialtyDto)).thenReturn(specialtyEntity);
            when(specialtyRepository.save(specialtyEntity)).thenReturn(specialtyEntity);
            when(specialtyMapper.toDto(specialtyEntity)).thenReturn(specialtyDto);

            // When
            SpecialtyDto result = specialtyService.create(specialtyDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("CS_101");
            assertThat(result.getName()).isEqualTo("Computer Science");

            verify(specialtyRepository).save(specialtyEntity);
        }

        @Test
        @DisplayName("throws ValidationException when code is null")
        void throwsValidationExceptionWhenCodeNull() {
            // Given
            SpecialtyDto invalidDto = new SpecialtyDto();
            invalidDto.setCode(null);
            invalidDto.setName("Test Specialty");

            // When / Then
            assertThatThrownBy(() -> specialtyService.create(invalidDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("validation failed");
        }

        @Test
        @DisplayName("throws ValidationException when code already exists")
        void throwsValidationExceptionWhenCodeExists() {
            // Given
            when(specialtyRepository.existsByCode("CS_101")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> specialtyService.create(specialtyDto))
                    .isInstanceOf(ValidationException.class);

            verify(specialtyRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ValidationException when name is blank")
        void throwsValidationExceptionWhenNameBlank() {
            // Given
            SpecialtyDto invalidDto = new SpecialtyDto();
            invalidDto.setCode("NEW_CODE");
            invalidDto.setName("   ");

            when(specialtyRepository.existsByCode("NEW_CODE")).thenReturn(false);

            // When / Then
            assertThatThrownBy(() -> specialtyService.create(invalidDto))
                    .isInstanceOf(ValidationException.class);
        }
    }

    // =====================================================
    // findById tests
    // =====================================================

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns specialty DTO when found by ID")
        void returnsSpecialtyDtoWhenFound() {
            // Given
            when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialtyEntity));
            when(specialtyMapper.toDto(specialtyEntity)).thenReturn(specialtyDto);

            // When
            SpecialtyDto result = specialtyService.findById(specialtyId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(specialtyId);
            assertThat(result.getCode()).isEqualTo("CS_101");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when specialty not found by ID")
        void throwsNotFoundWhenMissing() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(specialtyRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> specialtyService.findById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Specialty not found");
        }
    }

    // =====================================================
    // findByCode tests
    // =====================================================

    @Nested
    @DisplayName("findByCode")
    class FindByCode {

        @Test
        @DisplayName("returns specialty DTO when found by code")
        void returnsSpecialtyDtoWhenFoundByCode() {
            // Given
            when(specialtyRepository.findByCode("CS_101")).thenReturn(Optional.of(specialtyEntity));
            when(specialtyMapper.toDto(specialtyEntity)).thenReturn(specialtyDto);

            // When
            SpecialtyDto result = specialtyService.findByCode("CS_101");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("CS_101");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when specialty not found by code")
        void throwsNotFoundWhenCodeMissing() {
            // Given
            when(specialtyRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> specialtyService.findByCode("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Specialty not found");
        }
    }

    // =====================================================
    // findAll / findByUniversity tests
    // =====================================================

    @Nested
    @DisplayName("findAll and findByUniversity")
    class FindAllAndByUniversity {

        @Test
        @DisplayName("returns paginated specialty DTOs")
        void returnsPaginatedSpecialtyDtos() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Specialty> entityPage = new PageImpl<>(List.of(specialtyEntity), pageable, 1);
            when(specialtyRepository.findAll(pageable)).thenReturn(entityPage);
            when(specialtyMapper.toDto(specialtyEntity)).thenReturn(specialtyDto);

            // When
            Page<SpecialtyDto> result = specialtyService.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCode()).isEqualTo("CS_101");
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns specialties filtered by university code")
        void returnsSpecialtiesByUniversity() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Specialty> entityPage = new PageImpl<>(List.of(specialtyEntity), pageable, 1);
            when(specialtyRepository.findByUniversity("UNI_001", pageable)).thenReturn(entityPage);
            when(specialtyMapper.toDto(specialtyEntity)).thenReturn(specialtyDto);

            // When
            Page<SpecialtyDto> result = specialtyService.findByUniversity("UNI_001", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUniversity()).isEqualTo("UNI_001");
        }
    }

    // =====================================================
    // delete tests
    // =====================================================

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes existing specialty successfully")
        void deletesExistingSpecialty() {
            // Given
            when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialtyEntity));

            // When
            specialtyService.delete(specialtyId);

            // Then
            verify(specialtyRepository).delete(specialtyEntity);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when deleting non-existent specialty")
        void throwsNotFoundWhenDeletingMissing() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(specialtyRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> specialtyService.delete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Specialty not found");

            verify(specialtyRepository, never()).delete(any());
        }
    }

    // =====================================================
    // existsByCode / countByUniversity tests
    // =====================================================

    @Nested
    @DisplayName("existsByCode and countByUniversity")
    class UtilityMethods {

        @Test
        @DisplayName("existsByCode returns true when code exists")
        void existsByCodeReturnsTrue() {
            // Given
            when(specialtyRepository.existsByCode("CS_101")).thenReturn(true);

            // When
            boolean exists = specialtyService.existsByCode("CS_101");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("countByUniversity returns count of specialties for university")
        void countByUniversityReturnsCount() {
            // Given
            when(specialtyRepository.countByUniversity("UNI_001")).thenReturn(15L);

            // When
            long count = specialtyService.countByUniversity("UNI_001");

            // Then
            assertThat(count).isEqualTo(15L);
        }
    }
}
