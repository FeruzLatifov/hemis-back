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
import uz.hemis.common.dto.document.DiplomaDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.finance.Diploma;
import uz.hemis.domain.repository.DiplomaRepository;
import uz.hemis.service.document.mapper.DiplomaMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DiplomaService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>findById - found and not-found scenarios</li>
 *   <li>create - unique diploma number/hash and duplicate validation</li>
 *   <li>update - found and not-found scenarios</li>
 *   <li>softDelete - active, already-deleted, and not-found scenarios</li>
 *   <li>existsByDiplomaNumber - delegation to repository</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiplomaService Unit Tests")
class DiplomaServiceTest {

    @Mock
    private DiplomaRepository diplomaRepository;

    @Mock
    private DiplomaMapper diplomaMapper;

    @InjectMocks
    private DiplomaService diplomaService;

    private UUID diplomaId;
    private Diploma diploma;
    private DiplomaDto diplomaDto;

    @BeforeEach
    void setUp() {
        diplomaId = UUID.randomUUID();

        diploma = new Diploma();
        diploma.setId(diplomaId);
        diploma.setDiplomaNumber("UZ-2025-TUIT-00001");
        diploma.setDiplomaHash("abc123hash");
        diploma.setUniversity("TUIT");
        diploma.setStatus("ISSUED");

        diplomaDto = new DiplomaDto();
        diplomaDto.setId(diplomaId);
        diplomaDto.setDiplomaNumber("UZ-2025-TUIT-00001");
        diplomaDto.setDiplomaHash("abc123hash");
        diplomaDto.setUniversity("TUIT");
        diplomaDto.setStatus("ISSUED");
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
        when(diplomaRepository.findById(diplomaId)).thenReturn(Optional.of(diploma));
        when(diplomaMapper.toDto(diploma)).thenReturn(diplomaDto);

        // When
        DiplomaDto result = diplomaService.findById(diplomaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(diplomaId);
        assertThat(result.getDiplomaNumber()).isEqualTo("UZ-2025-TUIT-00001");
        assertThat(result.getDiplomaHash()).isEqualTo("abc123hash");

        verify(diplomaRepository).findById(diplomaId);
        verify(diplomaMapper).toDto(diploma);
    }

    @Test
    @DisplayName("findById - not found - throws ResourceNotFoundException")
    void findById_notFound_throwsResourceNotFoundException() {
        // Given
        UUID missingId = UUID.randomUUID();
        when(diplomaRepository.findById(missingId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> diplomaService.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Diploma")
                .hasMessageContaining("id");

        verify(diplomaRepository).findById(missingId);
        verify(diplomaMapper, never()).toDto(any());
    }

    // =====================================================
    // create tests
    // =====================================================

    @Test
    @DisplayName("create - unique number and hash - saves and returns DTO")
    void create_uniqueNumber_savesAndReturnsDto() {
        // Given
        DiplomaDto inputDto = new DiplomaDto();
        inputDto.setDiplomaNumber("UZ-2025-TUIT-00002");
        inputDto.setDiplomaHash("newHash456");
        inputDto.setUniversity("TUIT");

        Diploma newEntity = new Diploma();
        newEntity.setDiplomaNumber("UZ-2025-TUIT-00002");
        newEntity.setDiplomaHash("newHash456");

        Diploma savedEntity = new Diploma();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setDiplomaNumber("UZ-2025-TUIT-00002");
        savedEntity.setDiplomaHash("newHash456");

        DiplomaDto savedDto = new DiplomaDto();
        savedDto.setId(savedEntity.getId());
        savedDto.setDiplomaNumber("UZ-2025-TUIT-00002");
        savedDto.setDiplomaHash("newHash456");

        when(diplomaRepository.existsByDiplomaNumber("UZ-2025-TUIT-00002")).thenReturn(false);
        when(diplomaRepository.existsByDiplomaHash("newHash456")).thenReturn(false);
        when(diplomaMapper.toEntity(inputDto)).thenReturn(newEntity);
        when(diplomaRepository.save(newEntity)).thenReturn(savedEntity);
        when(diplomaMapper.toDto(savedEntity)).thenReturn(savedDto);

        // When
        DiplomaDto result = diplomaService.create(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiplomaNumber()).isEqualTo("UZ-2025-TUIT-00002");
        assertThat(result.getDiplomaHash()).isEqualTo("newHash456");
        assertThat(result.getId()).isEqualTo(savedEntity.getId());

        verify(diplomaRepository).existsByDiplomaNumber("UZ-2025-TUIT-00002");
        verify(diplomaRepository).existsByDiplomaHash("newHash456");
        verify(diplomaMapper).toEntity(inputDto);
        verify(diplomaRepository).save(newEntity);
        verify(diplomaMapper).toDto(savedEntity);
    }

    @Test
    @DisplayName("create - duplicate number - throws ValidationException")
    void create_duplicateNumber_throwsValidationException() {
        // Given
        DiplomaDto inputDto = new DiplomaDto();
        inputDto.setDiplomaNumber("UZ-2025-TUIT-00001");
        inputDto.setDiplomaHash("uniqueHash");

        when(diplomaRepository.existsByDiplomaNumber("UZ-2025-TUIT-00001")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> diplomaService.create(inputDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("number already exists");

        verify(diplomaRepository).existsByDiplomaNumber("UZ-2025-TUIT-00001");
        verify(diplomaRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - duplicate hash - throws ValidationException")
    void create_duplicateHash_throwsValidationException() {
        // Given
        DiplomaDto inputDto = new DiplomaDto();
        inputDto.setDiplomaNumber("UZ-2025-TUIT-99999");
        inputDto.setDiplomaHash("abc123hash");

        when(diplomaRepository.existsByDiplomaNumber("UZ-2025-TUIT-99999")).thenReturn(false);
        when(diplomaRepository.existsByDiplomaHash("abc123hash")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> diplomaService.create(inputDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("hash already exists");

        verify(diplomaRepository).existsByDiplomaNumber("UZ-2025-TUIT-99999");
        verify(diplomaRepository).existsByDiplomaHash("abc123hash");
        verify(diplomaRepository, never()).save(any());
    }

    // =====================================================
    // update tests
    // =====================================================

    @Test
    @DisplayName("update - found - updates and returns DTO")
    void update_found_updatesAndReturnsDto() {
        // Given
        DiplomaDto updateDto = new DiplomaDto();
        updateDto.setDiplomaNumber("UZ-2025-TUIT-00001"); // same number - no uniqueness conflict
        updateDto.setDiplomaHash("abc123hash"); // same hash - no uniqueness conflict
        updateDto.setStatus("REGISTERED");

        Diploma updatedEntity = new Diploma();
        updatedEntity.setId(diplomaId);
        updatedEntity.setDiplomaNumber("UZ-2025-TUIT-00001");
        updatedEntity.setDiplomaHash("abc123hash");
        updatedEntity.setStatus("REGISTERED");

        DiplomaDto resultDto = new DiplomaDto();
        resultDto.setId(diplomaId);
        resultDto.setDiplomaNumber("UZ-2025-TUIT-00001");
        resultDto.setDiplomaHash("abc123hash");
        resultDto.setStatus("REGISTERED");

        when(diplomaRepository.findById(diplomaId)).thenReturn(Optional.of(diploma));
        when(diplomaRepository.save(diploma)).thenReturn(updatedEntity);
        when(diplomaMapper.toDto(updatedEntity)).thenReturn(resultDto);

        // When
        DiplomaDto result = diplomaService.update(diplomaId, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(diplomaId);
        assertThat(result.getStatus()).isEqualTo("REGISTERED");

        verify(diplomaRepository).findById(diplomaId);
        verify(diplomaMapper).updateEntityFromDto(updateDto, diploma);
        verify(diplomaRepository).save(diploma);
        verify(diplomaMapper).toDto(updatedEntity);
    }

    @Test
    @DisplayName("update - not found - throws ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        // Given
        UUID missingId = UUID.randomUUID();
        DiplomaDto updateDto = new DiplomaDto();
        updateDto.setDiplomaNumber("UZ-2025-TUIT-00099");

        when(diplomaRepository.findById(missingId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> diplomaService.update(missingId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Diploma")
                .hasMessageContaining("id");

        verify(diplomaRepository).findById(missingId);
        verify(diplomaRepository, never()).save(any());
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

        when(diplomaRepository.findById(diplomaId)).thenReturn(Optional.of(diploma));
        when(diplomaRepository.save(diploma)).thenReturn(diploma);

        // When
        diplomaService.softDelete(diplomaId);

        // Then
        assertThat(diploma.getDeleteTs()).isNotNull();
        assertThat(diploma.getDeletedBy()).isEqualTo("admin");

        verify(diplomaRepository).findById(diplomaId);
        verify(diplomaRepository).save(diploma);
    }

    @Test
    @DisplayName("softDelete - already deleted - skips without saving")
    void softDelete_alreadyDeleted_skips() {
        // Given
        diploma.setDeleteTs(java.time.LocalDateTime.now().minusDays(1));
        diploma.setDeletedBy("previous-admin");

        when(diplomaRepository.findById(diplomaId)).thenReturn(Optional.of(diploma));

        // When
        diplomaService.softDelete(diplomaId);

        // Then - should NOT overwrite existing delete fields and should NOT save
        verify(diplomaRepository).findById(diplomaId);
        verify(diplomaRepository, never()).save(any());
    }

    // =====================================================
    // existsByDiplomaNumber tests
    // =====================================================

    @Test
    @DisplayName("existsByDiplomaNumber - delegates to repository")
    void existsByDiplomaNumber_delegatesToRepository() {
        // Given
        when(diplomaRepository.existsByDiplomaNumber("UZ-2025-TUIT-00001")).thenReturn(true);
        when(diplomaRepository.existsByDiplomaNumber("UZ-2025-NONEXIST")).thenReturn(false);

        // When / Then
        assertThat(diplomaService.existsByDiplomaNumber("UZ-2025-TUIT-00001")).isTrue();
        assertThat(diplomaService.existsByDiplomaNumber("UZ-2025-NONEXIST")).isFalse();

        verify(diplomaRepository).existsByDiplomaNumber("UZ-2025-TUIT-00001");
        verify(diplomaRepository).existsByDiplomaNumber("UZ-2025-NONEXIST");
    }
}
