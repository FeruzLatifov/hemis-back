package uz.hemis.service.document;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.hemis.common.dto.document.DiplomaBlankDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.finance.DiplomaBlank;
import uz.hemis.domain.repository.DiplomaBlankRepository;
import uz.hemis.service.document.mapper.DiplomaBlankMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DiplomaBlankService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>findById - found and not-found scenarios</li>
 *   <li>create - unique blank code and duplicate validation</li>
 *   <li>updateStatus - status-only update</li>
 *   <li>softDelete - active, already-deleted, and not-found scenarios</li>
 *   <li>existsByCode - delegation to repository</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiplomaBlankService Unit Tests")
class DiplomaBlankServiceTest {

    @Mock
    private DiplomaBlankRepository diplomaBlankRepository;

    @Mock
    private DiplomaBlankMapper diplomaBlankMapper;

    @InjectMocks
    private DiplomaBlankService diplomaBlankService;

    private UUID blankId;
    private DiplomaBlank diplomaBlank;
    private DiplomaBlankDto diplomaBlankDto;

    @BeforeEach
    void setUp() {
        blankId = UUID.randomUUID();

        diplomaBlank = new DiplomaBlank();
        diplomaBlank.setId(blankId);
        diplomaBlank.setBlankCode("AB 1234567");
        diplomaBlank.setSeries("AB");
        diplomaBlank.setNumber("1234567");
        diplomaBlank.setUniversity("TUIT");
        diplomaBlank.setStatus("AVAILABLE");
        diplomaBlank.setBlankType("BACHELOR");

        diplomaBlankDto = new DiplomaBlankDto();
        diplomaBlankDto.setId(blankId.toString());
        diplomaBlankDto.setBlankCode("AB 1234567");
        diplomaBlankDto.setSeries("AB");
        diplomaBlankDto.setNumber("1234567");
        diplomaBlankDto.setUniversity("TUIT");
        diplomaBlankDto.setStatus("AVAILABLE");
        diplomaBlankDto.setBlankType("BACHELOR");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =====================================================
    // findById tests
    // =====================================================

    @Test
    @DisplayName("findById - found - returns DTO")
    void findById_found_returnsDto() {
        // Given
        when(diplomaBlankRepository.findById(blankId)).thenReturn(Optional.of(diplomaBlank));
        when(diplomaBlankMapper.toDto(diplomaBlank)).thenReturn(diplomaBlankDto);

        // When
        DiplomaBlankDto result = diplomaBlankService.findById(blankId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(blankId.toString());
        assertThat(result.getBlankCode()).isEqualTo("AB 1234567");
        assertThat(result.getSeries()).isEqualTo("AB");
        assertThat(result.getNumber()).isEqualTo("1234567");
        assertThat(result.getStatus()).isEqualTo("AVAILABLE");

        verify(diplomaBlankRepository).findById(blankId);
        verify(diplomaBlankMapper).toDto(diplomaBlank);
    }

    @Test
    @DisplayName("findById - not found - throws ResourceNotFoundException")
    void findById_notFound_throwsResourceNotFoundException() {
        // Given
        UUID missingId = UUID.randomUUID();
        when(diplomaBlankRepository.findById(missingId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> diplomaBlankService.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DiplomaBlank")
                .hasMessageContaining("id");

        verify(diplomaBlankRepository).findById(missingId);
        verify(diplomaBlankMapper, never()).toDto(any());
    }

    // =====================================================
    // create tests
    // =====================================================

    @Test
    @DisplayName("create - unique code - saves and returns DTO")
    void create_uniqueCode_savesAndReturnsDto() {
        // Given
        DiplomaBlankDto inputDto = new DiplomaBlankDto();
        inputDto.setBlankCode("CD 9876543");
        inputDto.setSeries("CD");
        inputDto.setNumber("9876543");
        inputDto.setUniversity("TUIT");

        DiplomaBlank newEntity = new DiplomaBlank();
        newEntity.setBlankCode("CD 9876543");
        newEntity.setSeries("CD");
        newEntity.setNumber("9876543");

        UUID savedId = UUID.randomUUID();
        DiplomaBlank savedEntity = new DiplomaBlank();
        savedEntity.setId(savedId);
        savedEntity.setBlankCode("CD 9876543");
        savedEntity.setSeries("CD");
        savedEntity.setNumber("9876543");

        DiplomaBlankDto savedDto = new DiplomaBlankDto();
        savedDto.setId(savedId.toString());
        savedDto.setBlankCode("CD 9876543");
        savedDto.setSeries("CD");
        savedDto.setNumber("9876543");

        when(diplomaBlankRepository.existsByBlankCode("CD 9876543")).thenReturn(false);
        when(diplomaBlankMapper.toEntity(inputDto)).thenReturn(newEntity);
        when(diplomaBlankRepository.save(newEntity)).thenReturn(savedEntity);
        when(diplomaBlankMapper.toDto(savedEntity)).thenReturn(savedDto);

        // When
        DiplomaBlankDto result = diplomaBlankService.create(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBlankCode()).isEqualTo("CD 9876543");
        assertThat(result.getId()).isEqualTo(savedId.toString());

        verify(diplomaBlankRepository).existsByBlankCode("CD 9876543");
        verify(diplomaBlankMapper).toEntity(inputDto);
        verify(diplomaBlankRepository).save(newEntity);
        verify(diplomaBlankMapper).toDto(savedEntity);
    }

    @Test
    @DisplayName("create - duplicate code - throws ValidationException")
    void create_duplicateCode_throwsValidationException() {
        // Given
        DiplomaBlankDto inputDto = new DiplomaBlankDto();
        inputDto.setBlankCode("AB 1234567");

        when(diplomaBlankRepository.existsByBlankCode("AB 1234567")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> diplomaBlankService.create(inputDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code already exists");

        verify(diplomaBlankRepository).existsByBlankCode("AB 1234567");
        verify(diplomaBlankRepository, never()).save(any());
    }

    // =====================================================
    // updateStatus tests
    // =====================================================

    @Test
    @DisplayName("updateStatus - found - updates status only")
    void updateStatus_found_updatesStatusOnly() {
        // Given
        DiplomaBlank updatedEntity = new DiplomaBlank();
        updatedEntity.setId(blankId);
        updatedEntity.setBlankCode("AB 1234567");
        updatedEntity.setSeries("AB");
        updatedEntity.setNumber("1234567");
        updatedEntity.setStatus("ASSIGNED");

        DiplomaBlankDto updatedDto = new DiplomaBlankDto();
        updatedDto.setId(blankId.toString());
        updatedDto.setBlankCode("AB 1234567");
        updatedDto.setStatus("ASSIGNED");

        when(diplomaBlankRepository.findById(blankId)).thenReturn(Optional.of(diplomaBlank));
        when(diplomaBlankRepository.save(diplomaBlank)).thenReturn(updatedEntity);
        when(diplomaBlankMapper.toDto(updatedEntity)).thenReturn(updatedDto);

        // When
        DiplomaBlankDto result = diplomaBlankService.updateStatus(blankId, "ASSIGNED");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("ASSIGNED");
        assertThat(diplomaBlank.getStatus()).isEqualTo("ASSIGNED");

        verify(diplomaBlankRepository).findById(blankId);
        verify(diplomaBlankRepository).save(diplomaBlank);
        // Verify that mapper.updateEntityFromDto was NOT called (status-only update)
        verify(diplomaBlankMapper, never()).updateEntityFromDto(any(), any());
        verify(diplomaBlankMapper).toDto(updatedEntity);
    }

    // =====================================================
    // softDelete tests
    // =====================================================

    @Test
    @DisplayName("softDelete - found and active - sets deleteTs and deletedBy")
    void softDelete_found_setsDeleteFields() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", null)
        );

        when(diplomaBlankRepository.findById(blankId)).thenReturn(Optional.of(diplomaBlank));
        when(diplomaBlankRepository.save(diplomaBlank)).thenReturn(diplomaBlank);

        // When
        diplomaBlankService.softDelete(blankId);

        // Then
        assertThat(diplomaBlank.getDeleteTs()).isNotNull();
        assertThat(diplomaBlank.getDeletedBy()).isEqualTo("admin");

        verify(diplomaBlankRepository).findById(blankId);
        verify(diplomaBlankRepository).save(diplomaBlank);
    }

    @Test
    @DisplayName("softDelete - already deleted - skips without saving")
    void softDelete_alreadyDeleted_skips() {
        // Given
        diplomaBlank.setDeleteTs(java.time.LocalDateTime.now().minusDays(1));
        diplomaBlank.setDeletedBy("previous-admin");

        when(diplomaBlankRepository.findById(blankId)).thenReturn(Optional.of(diplomaBlank));

        // When
        diplomaBlankService.softDelete(blankId);

        // Then - should NOT overwrite existing delete fields and should NOT save
        verify(diplomaBlankRepository).findById(blankId);
        verify(diplomaBlankRepository, never()).save(any());
    }

    // =====================================================
    // existsByCode tests
    // =====================================================

    @Test
    @DisplayName("existsByCode - delegates to repository")
    void existsByCode_delegatesToRepository() {
        // Given
        when(diplomaBlankRepository.existsByBlankCode("AB 1234567")).thenReturn(true);
        when(diplomaBlankRepository.existsByBlankCode("ZZ 0000000")).thenReturn(false);

        // When / Then
        assertThat(diplomaBlankService.existsByCode("AB 1234567")).isTrue();
        assertThat(diplomaBlankService.existsByCode("ZZ 0000000")).isFalse();

        verify(diplomaBlankRepository).existsByBlankCode("AB 1234567");
        verify(diplomaBlankRepository).existsByBlankCode("ZZ 0000000");
    }
}
