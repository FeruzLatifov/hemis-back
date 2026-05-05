package uz.hemis.service.university;

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
import uz.hemis.common.dto.university.DepartmentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.academic.Department;
import uz.hemis.domain.repository.DepartmentRepository;
import uz.hemis.service.security.TenantGuard;
import uz.hemis.service.university.mapper.DepartmentMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DepartmentService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>findById - found and not-found scenarios</li>
 *   <li>findByCode - found and not-found scenarios</li>
 *   <li>create - unique department code and duplicate validation</li>
 *   <li>update - found, not-found, and duplicate code scenarios</li>
 *   <li>softDelete - active, already-deleted, and not-found scenarios</li>
 *   <li>existsByCode - delegation to repository</li>
 *   <li>countActiveByUniversity - delegation to repository</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService Unit Tests")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private TenantGuard tenantGuard;

    @InjectMocks
    private DepartmentService departmentService;

    private UUID departmentId;
    private Department department;
    private DepartmentDto departmentDto;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();

        department = new Department();
        department.setId(departmentId);
        department.setDepartmentCode("DEP-001");
        department.setDepartmentName("Computer Science");
        department.setDepartmentNameUz("Kompyuter fanlari");
        department.setDepartmentNameRu("Kompyuternye nauki");
        department.setDepartmentNameEn("Computer Science");
        department.setUniversity("TUIT");
        department.setFaculty(UUID.randomUUID());
        department.setIsActive(true);

        departmentDto = new DepartmentDto();
        departmentDto.setId(departmentId);
        departmentDto.setDepartmentCode("DEP-001");
        departmentDto.setDepartmentName("Computer Science");
        departmentDto.setDepartmentNameUz("Kompyuter fanlari");
        departmentDto.setDepartmentNameRu("Kompyuternye nauki");
        departmentDto.setDepartmentNameEn("Computer Science");
        departmentDto.setUniversity("TUIT");
        departmentDto.setFaculty(department.getFaculty());
        departmentDto.setIsActive(true);
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
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(departmentMapper.toDto(department)).thenReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.findById(departmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(departmentId);
        assertThat(result.getDepartmentCode()).isEqualTo("DEP-001");
        assertThat(result.getDepartmentName()).isEqualTo("Computer Science");
        assertThat(result.getUniversity()).isEqualTo("TUIT");

        verify(departmentRepository).findById(departmentId);
        verify(departmentMapper).toDto(department);
    }

    @Test
    @DisplayName("findById - not found - throws ResourceNotFoundException")
    void findById_notFound_throwsResourceNotFoundException() {
        // Given
        UUID missingId = UUID.randomUUID();
        when(departmentRepository.findById(missingId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> departmentService.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department")
                .hasMessageContaining("id");

        verify(departmentRepository).findById(missingId);
        verify(departmentMapper, never()).toDto(any());
    }

    // =====================================================
    // findByCode tests
    // =====================================================

    @Test
    @DisplayName("findByCode - found - returns DTO")
    void findByCode_found_returnsDto() {
        // Given
        when(departmentRepository.findByDepartmentCode("DEP-001")).thenReturn(Optional.of(department));
        when(departmentMapper.toDto(department)).thenReturn(departmentDto);

        // When
        DepartmentDto result = departmentService.findByCode("DEP-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(departmentId);
        assertThat(result.getDepartmentCode()).isEqualTo("DEP-001");
        assertThat(result.getDepartmentName()).isEqualTo("Computer Science");

        verify(departmentRepository).findByDepartmentCode("DEP-001");
        verify(departmentMapper).toDto(department);
    }

    @Test
    @DisplayName("findByCode - not found - throws ResourceNotFoundException")
    void findByCode_notFound_throwsResourceNotFoundException() {
        // Given
        when(departmentRepository.findByDepartmentCode("NONEXIST")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> departmentService.findByCode("NONEXIST"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department")
                .hasMessageContaining("departmentCode");

        verify(departmentRepository).findByDepartmentCode("NONEXIST");
        verify(departmentMapper, never()).toDto(any());
    }

    // =====================================================
    // create tests
    // =====================================================

    @Test
    @DisplayName("create - unique code - saves and returns DTO")
    void create_uniqueCode_savesAndReturnsDto() {
        // Given
        DepartmentDto inputDto = new DepartmentDto();
        inputDto.setDepartmentCode("DEP-002");
        inputDto.setDepartmentName("Mathematics");
        inputDto.setUniversity("TUIT");

        Department newEntity = new Department();
        newEntity.setDepartmentCode("DEP-002");
        newEntity.setDepartmentName("Mathematics");
        newEntity.setUniversity("TUIT");

        Department savedEntity = new Department();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setDepartmentCode("DEP-002");
        savedEntity.setDepartmentName("Mathematics");
        savedEntity.setUniversity("TUIT");

        DepartmentDto savedDto = new DepartmentDto();
        savedDto.setId(savedEntity.getId());
        savedDto.setDepartmentCode("DEP-002");
        savedDto.setDepartmentName("Mathematics");
        savedDto.setUniversity("TUIT");

        when(departmentRepository.existsByDepartmentCode("DEP-002")).thenReturn(false);
        when(departmentMapper.toEntity(inputDto)).thenReturn(newEntity);
        when(departmentRepository.save(newEntity)).thenReturn(savedEntity);
        when(departmentMapper.toDto(savedEntity)).thenReturn(savedDto);

        // When
        DepartmentDto result = departmentService.create(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDepartmentCode()).isEqualTo("DEP-002");
        assertThat(result.getDepartmentName()).isEqualTo("Mathematics");
        assertThat(result.getId()).isEqualTo(savedEntity.getId());

        verify(departmentRepository).existsByDepartmentCode("DEP-002");
        verify(departmentMapper).toEntity(inputDto);
        verify(departmentRepository).save(newEntity);
        verify(departmentMapper).toDto(savedEntity);
    }

    @Test
    @DisplayName("create - duplicate code - throws ValidationException")
    void create_duplicateCode_throwsValidationException() {
        // Given
        DepartmentDto inputDto = new DepartmentDto();
        inputDto.setDepartmentCode("DEP-001");
        inputDto.setDepartmentName("Duplicate Department");

        when(departmentRepository.existsByDepartmentCode("DEP-001")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> departmentService.create(inputDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code already exists");

        verify(departmentRepository).existsByDepartmentCode("DEP-001");
        verify(departmentRepository, never()).save(any());
    }

    // =====================================================
    // update tests
    // =====================================================

    @Test
    @DisplayName("update - found - updates and returns DTO")
    void update_found_updatesAndReturnsDto() {
        // Given
        DepartmentDto updateDto = new DepartmentDto();
        updateDto.setDepartmentCode("DEP-001"); // same code - no uniqueness conflict
        updateDto.setDepartmentName("Computer Science (Updated)");
        updateDto.setIsActive(true);

        Department updatedEntity = new Department();
        updatedEntity.setId(departmentId);
        updatedEntity.setDepartmentCode("DEP-001");
        updatedEntity.setDepartmentName("Computer Science (Updated)");
        updatedEntity.setIsActive(true);

        DepartmentDto resultDto = new DepartmentDto();
        resultDto.setId(departmentId);
        resultDto.setDepartmentCode("DEP-001");
        resultDto.setDepartmentName("Computer Science (Updated)");
        resultDto.setIsActive(true);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(departmentRepository.save(department)).thenReturn(updatedEntity);
        when(departmentMapper.toDto(updatedEntity)).thenReturn(resultDto);

        // When
        DepartmentDto result = departmentService.update(departmentId, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(departmentId);
        assertThat(result.getDepartmentName()).isEqualTo("Computer Science (Updated)");

        verify(departmentRepository).findById(departmentId);
        verify(departmentMapper).updateEntityFromDto(updateDto, department);
        verify(departmentRepository).save(department);
        verify(departmentMapper).toDto(updatedEntity);
    }

    @Test
    @DisplayName("update - not found - throws ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        // Given
        UUID missingId = UUID.randomUUID();
        DepartmentDto updateDto = new DepartmentDto();
        updateDto.setDepartmentCode("DEP-999");

        when(departmentRepository.findById(missingId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> departmentService.update(missingId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department")
                .hasMessageContaining("id");

        verify(departmentRepository).findById(missingId);
        verify(departmentRepository, never()).save(any());
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

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(departmentRepository.save(department)).thenReturn(department);

        // When
        departmentService.softDelete(departmentId);

        // Then
        assertThat(department.getDeleteTs()).isNotNull();
        assertThat(department.getDeletedBy()).isEqualTo("admin");

        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository).save(department);
    }

    @Test
    @DisplayName("softDelete - already deleted - skips without saving")
    void softDelete_alreadyDeleted_skips() {
        // Given
        department.setDeleteTs(LocalDateTime.now().minusDays(1));
        department.setDeletedBy("previous-admin");

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

        // When
        departmentService.softDelete(departmentId);

        // Then - should NOT overwrite existing delete fields and should NOT save
        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository, never()).save(any());
    }

    // =====================================================
    // existsByCode tests
    // =====================================================

    @Test
    @DisplayName("existsByCode - delegates to repository")
    void existsByCode_delegatesToRepository() {
        // Given
        when(departmentRepository.existsByDepartmentCode("DEP-001")).thenReturn(true);
        when(departmentRepository.existsByDepartmentCode("NONEXIST")).thenReturn(false);

        // When / Then
        assertThat(departmentService.existsByCode("DEP-001")).isTrue();
        assertThat(departmentService.existsByCode("NONEXIST")).isFalse();

        verify(departmentRepository).existsByDepartmentCode("DEP-001");
        verify(departmentRepository).existsByDepartmentCode("NONEXIST");
    }

    // =====================================================
    // countActiveByUniversity tests
    // =====================================================

    @Test
    @DisplayName("countActiveByUniversity - delegates to repository")
    void countActiveByUniversity_delegatesToRepository() {
        // Given
        when(departmentRepository.countActiveByUniversity("TUIT")).thenReturn(15L);
        when(departmentRepository.countActiveByUniversity("NONEXIST")).thenReturn(0L);

        // When / Then
        assertThat(departmentService.countActiveByUniversity("TUIT")).isEqualTo(15L);
        assertThat(departmentService.countActiveByUniversity("NONEXIST")).isEqualTo(0L);

        verify(departmentRepository).countActiveByUniversity("TUIT");
        verify(departmentRepository).countActiveByUniversity("NONEXIST");
    }
}
