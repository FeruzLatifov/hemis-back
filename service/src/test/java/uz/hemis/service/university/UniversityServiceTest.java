package uz.hemis.service.university;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.hemis.common.dto.university.UniversityDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.university.mapper.UniversityMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UniversityService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>findByCode - found and not-found scenarios</li>
 *   <li>findByTin - found and not-found scenarios</li>
 *   <li>create - unique code/tin, duplicate code, duplicate tin, missing code, missing name</li>
 *   <li>update - found and not-found scenarios</li>
 *   <li>softDelete - sets deleteTs/deletedBy, not-found scenario</li>
 *   <li>countActive - delegation to repository</li>
 *   <li>existsByCode - delegation to repository</li>
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

    /**
     * Mocked tenant guard — real implementation talab qiladi JWT SecurityContext.
     * Test'lar faqat business logic'ni tekshiradi (cross-tenant scenario alohida
     * UniversityBuildingSyncServiceTest'da). Default void no-op behavior kifoya.
     */
    @Mock
    private uz.hemis.service.security.TenantGuard tenantGuard;

    @InjectMocks
    private UniversityService universityService;

    private static final String CODE = "TATU01";
    private static final String TIN = "123456789";
    private static final String NAME = "Tashkent University of Information Technologies";

    private University university;
    private UniversityDto universityDto;

    @BeforeEach
    void setUp() {
        university = new University();
        university.setCode(CODE);
        university.setTin(TIN);
        university.setName(NAME);
        university.setActive(true);

        universityDto = new UniversityDto();
        universityDto.setCode(CODE);
        universityDto.setTin(TIN);
        universityDto.setName(NAME);
        universityDto.setActive(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =====================================================
    // findByCode
    // =====================================================

    @Nested
    @DisplayName("findByCode")
    class FindByCode {

        @Test
        @DisplayName("found - returns DTO")
        void findByCode_found_returnsDto() {
            when(universityRepository.findById(CODE)).thenReturn(Optional.of(university));
            when(universityMapper.toDto(university)).thenReturn(universityDto);

            UniversityDto result = universityService.findByCode(CODE);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(CODE);
            assertThat(result.getTin()).isEqualTo(TIN);
            assertThat(result.getName()).isEqualTo(NAME);
            verify(universityRepository).findById(CODE);
            verify(universityMapper).toDto(university);
        }

        @Test
        @DisplayName("not found - throws ResourceNotFoundException")
        void findByCode_notFound_throwsResourceNotFoundException() {
            when(universityRepository.findById(CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> universityService.findByCode(CODE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("University")
                    .hasMessageContaining("code")
                    .hasMessageContaining(CODE);

            verify(universityRepository).findById(CODE);
            verifyNoInteractions(universityMapper);
        }
    }

    // =====================================================
    // findByTin
    // =====================================================

    @Nested
    @DisplayName("findByTin")
    class FindByTin {

        @Test
        @DisplayName("found - returns DTO")
        void findByTin_found_returnsDto() {
            when(universityRepository.findByTin(TIN)).thenReturn(Optional.of(university));
            when(universityMapper.toDto(university)).thenReturn(universityDto);

            UniversityDto result = universityService.findByTin(TIN);

            assertThat(result).isNotNull();
            assertThat(result.getTin()).isEqualTo(TIN);
            assertThat(result.getCode()).isEqualTo(CODE);
            verify(universityRepository).findByTin(TIN);
            verify(universityMapper).toDto(university);
        }

        @Test
        @DisplayName("not found - throws ResourceNotFoundException")
        void findByTin_notFound_throwsResourceNotFoundException() {
            when(universityRepository.findByTin(TIN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> universityService.findByTin(TIN))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("University")
                    .hasMessageContaining("tin")
                    .hasMessageContaining(TIN);

            verify(universityRepository).findByTin(TIN);
            verifyNoInteractions(universityMapper);
        }
    }

    // =====================================================
    // create
    // =====================================================

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("unique code and tin - saves and returns DTO")
        void create_uniqueCode_savesAndReturnsDto() {
            when(universityRepository.existsByCode(CODE)).thenReturn(false);
            when(universityRepository.findByTin(TIN)).thenReturn(Optional.empty());
            when(universityMapper.toEntity(universityDto)).thenReturn(university);
            when(universityRepository.save(university)).thenReturn(university);
            when(universityMapper.toDto(university)).thenReturn(universityDto);

            UniversityDto result = universityService.create(universityDto);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(CODE);
            assertThat(result.getName()).isEqualTo(NAME);
            verify(universityRepository).existsByCode(CODE);
            verify(universityRepository).findByTin(TIN);
            verify(universityMapper).toEntity(universityDto);
            verify(universityRepository).save(university);
            verify(universityMapper).toDto(university);
        }

        @Test
        @DisplayName("duplicate code - throws ValidationException")
        void create_duplicateCode_throwsValidationException() {
            when(universityRepository.existsByCode(CODE)).thenReturn(true);

            assertThatThrownBy(() -> universityService.create(universityDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("validation failed")
                    .extracting(ex -> ((ValidationException) ex).getErrors())
                    .satisfies(errors -> {
                        @SuppressWarnings("unchecked")
                        var map = (java.util.Map<String, String>) errors;
                        assertThat(map).containsKey("code");
                        assertThat(map.get("code")).contains(CODE);
                    });

            verify(universityRepository).existsByCode(CODE);
            verify(universityRepository, never()).save(any());
        }

        @Test
        @DisplayName("duplicate tin - throws ValidationException")
        void create_duplicateTin_throwsValidationException() {
            University existingWithSameTin = new University();
            existingWithSameTin.setCode("OTHER01");
            existingWithSameTin.setTin(TIN);

            when(universityRepository.existsByCode(CODE)).thenReturn(false);
            when(universityRepository.findByTin(TIN)).thenReturn(Optional.of(existingWithSameTin));

            assertThatThrownBy(() -> universityService.create(universityDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("validation failed")
                    .extracting(ex -> ((ValidationException) ex).getErrors())
                    .satisfies(errors -> {
                        @SuppressWarnings("unchecked")
                        var map = (java.util.Map<String, String>) errors;
                        assertThat(map).containsKey("tin");
                        assertThat(map.get("tin")).contains(TIN);
                    });

            verify(universityRepository, never()).save(any());
        }

        @Test
        @DisplayName("null code - throws ValidationException")
        void create_nullCode_throwsValidationException() {
            universityDto.setCode(null);

            assertThatThrownBy(() -> universityService.create(universityDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("code is required");

            verify(universityRepository, never()).save(any());
        }

        @Test
        @DisplayName("blank code - throws ValidationException")
        void create_blankCode_throwsValidationException() {
            universityDto.setCode("   ");

            assertThatThrownBy(() -> universityService.create(universityDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("code is required");

            verify(universityRepository, never()).save(any());
        }

        @Test
        @DisplayName("null name - throws ValidationException")
        void create_nullName_throwsValidationException() {
            universityDto.setName(null);

            when(universityRepository.existsByCode(CODE)).thenReturn(false);
            when(universityRepository.findByTin(TIN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> universityService.create(universityDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("name is required");

            verify(universityRepository, never()).save(any());
        }

        @Test
        @DisplayName("null tin - skips tin uniqueness check")
        void create_nullTin_skipsTinCheck() {
            universityDto.setTin(null);

            when(universityRepository.existsByCode(CODE)).thenReturn(false);
            when(universityMapper.toEntity(universityDto)).thenReturn(university);
            when(universityRepository.save(university)).thenReturn(university);
            when(universityMapper.toDto(university)).thenReturn(universityDto);

            UniversityDto result = universityService.create(universityDto);

            assertThat(result).isNotNull();
            verify(universityRepository, never()).findByTin(any());
            verify(universityRepository).save(university);
        }
    }

    // =====================================================
    // update
    // =====================================================

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("found - updates and returns DTO")
        void update_found_updatesAndReturnsDto() {
            UniversityDto updateDto = new UniversityDto();
            updateDto.setName("Updated University Name");
            updateDto.setTin("987654321");
            updateDto.setAddress("New Address");

            University updatedEntity = new University();
            updatedEntity.setCode(CODE);
            updatedEntity.setName("Updated University Name");
            updatedEntity.setTin("987654321");
            updatedEntity.setAddress("New Address");

            UniversityDto resultDto = new UniversityDto();
            resultDto.setCode(CODE);
            resultDto.setName("Updated University Name");
            resultDto.setTin("987654321");
            resultDto.setAddress("New Address");

            when(universityRepository.findById(CODE)).thenReturn(Optional.of(university));
            when(universityRepository.existsByTinAndCodeNot("987654321", CODE)).thenReturn(false);
            when(universityRepository.save(university)).thenReturn(updatedEntity);
            when(universityMapper.toDto(updatedEntity)).thenReturn(resultDto);

            UniversityDto result = universityService.update(CODE, updateDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated University Name");
            assertThat(result.getTin()).isEqualTo("987654321");
            assertThat(result.getAddress()).isEqualTo("New Address");
            verify(universityRepository).findById(CODE);
            verify(universityRepository).save(university);
        }

        @Test
        @DisplayName("not found - throws ResourceNotFoundException")
        void update_notFound_throwsResourceNotFoundException() {
            when(universityRepository.findById(CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> universityService.update(CODE, universityDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("University")
                    .hasMessageContaining("code")
                    .hasMessageContaining(CODE);

            verify(universityRepository).findById(CODE);
            verify(universityRepository, never()).save(any());
        }

        @Test
        @DisplayName("duplicate tin on update - throws ValidationException")
        void update_duplicateTin_throwsValidationException() {
            UniversityDto updateDto = new UniversityDto();
            updateDto.setTin("999999999");

            when(universityRepository.findById(CODE)).thenReturn(Optional.of(university));
            when(universityRepository.existsByTinAndCodeNot("999999999", CODE)).thenReturn(true);

            assertThatThrownBy(() -> universityService.update(CODE, updateDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("validation failed")
                    .extracting(ex -> ((ValidationException) ex).getErrors())
                    .satisfies(errors -> {
                        @SuppressWarnings("unchecked")
                        var map = (java.util.Map<String, String>) errors;
                        assertThat(map).containsKey("tin");
                    });

            verify(universityRepository, never()).save(any());
        }

        @Test
        @DisplayName("same tin on update - skips tin uniqueness check")
        void update_sameTin_skipsTinUniquenessCheck() {
            UniversityDto updateDto = new UniversityDto();
            updateDto.setTin(TIN); // same as existing
            updateDto.setName("Updated Name");

            University updatedEntity = new University();
            updatedEntity.setCode(CODE);
            updatedEntity.setTin(TIN);
            updatedEntity.setName("Updated Name");

            UniversityDto resultDto = new UniversityDto();
            resultDto.setCode(CODE);
            resultDto.setTin(TIN);
            resultDto.setName("Updated Name");

            when(universityRepository.findById(CODE)).thenReturn(Optional.of(university));
            when(universityRepository.save(university)).thenReturn(updatedEntity);
            when(universityMapper.toDto(updatedEntity)).thenReturn(resultDto);

            UniversityDto result = universityService.update(CODE, updateDto);

            assertThat(result).isNotNull();
            // existsByTinAndCodeNot should NOT be called when tin is unchanged
            verify(universityRepository, never()).existsByTinAndCodeNot(any(), any());
            verify(universityRepository).save(university);
        }
    }

    // =====================================================
    // softDelete
    // =====================================================

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("found - sets deleteTs and deletedBy from SecurityContext")
        void softDelete_found_setsDeleteFields() {
            // Set up SecurityContext with authenticated user
            TestingAuthenticationToken auth =
                    new TestingAuthenticationToken("admin_user", "password");
            SecurityContextHolder.getContext().setAuthentication(auth);

            when(universityRepository.findById(CODE)).thenReturn(Optional.of(university));
            when(universityRepository.save(any(University.class))).thenReturn(university);

            universityService.softDelete(CODE);

            ArgumentCaptor<University> captor = ArgumentCaptor.forClass(University.class);
            verify(universityRepository).save(captor.capture());

            University saved = captor.getValue();
            assertThat(saved.getDeleteTs()).isNotNull();
            assertThat(saved.getDeleteTs()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(saved.getDeletedBy()).isEqualTo("admin_user");
        }

        @Test
        @DisplayName("no authentication - sets deletedBy to 'system'")
        void softDelete_noAuth_setsDeletedBySystem() {
            // SecurityContext is empty (no authentication set)
            SecurityContextHolder.clearContext();

            when(universityRepository.findById(CODE)).thenReturn(Optional.of(university));
            when(universityRepository.save(any(University.class))).thenReturn(university);

            universityService.softDelete(CODE);

            ArgumentCaptor<University> captor = ArgumentCaptor.forClass(University.class);
            verify(universityRepository).save(captor.capture());

            University saved = captor.getValue();
            assertThat(saved.getDeleteTs()).isNotNull();
            assertThat(saved.getDeletedBy()).isEqualTo("system");
        }

        @Test
        @DisplayName("already deleted - still overwrites deleteTs (no skip)")
        void softDelete_alreadyDeleted_overwritesDeleteTs() {
            // NOTE: Due to @SQLRestriction("delete_ts IS NULL") on the entity,
            // already-deleted records are filtered out by Hibernate and findById
            // will return Optional.empty(), causing ResourceNotFoundException.
            // However, if the record is somehow fetched (e.g., bypassing filter),
            // softDelete will overwrite the deleteTs value.
            university.setDeleteTs(LocalDateTime.of(2025, 1, 1, 0, 0));
            university.setDeletedBy("previous_user");

            TestingAuthenticationToken auth =
                    new TestingAuthenticationToken("admin_user", "password");
            SecurityContextHolder.getContext().setAuthentication(auth);

            when(universityRepository.findById(CODE)).thenReturn(Optional.of(university));
            when(universityRepository.save(any(University.class))).thenReturn(university);

            universityService.softDelete(CODE);

            ArgumentCaptor<University> captor = ArgumentCaptor.forClass(University.class);
            verify(universityRepository).save(captor.capture());

            University saved = captor.getValue();
            assertThat(saved.getDeleteTs()).isNotNull();
            assertThat(saved.getDeleteTs()).isAfter(LocalDateTime.of(2025, 1, 1, 0, 0));
            assertThat(saved.getDeletedBy()).isEqualTo("admin_user");
        }

        @Test
        @DisplayName("not found - throws ResourceNotFoundException")
        void softDelete_notFound_throwsResourceNotFoundException() {
            when(universityRepository.findById(CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> universityService.softDelete(CODE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("University")
                    .hasMessageContaining("code")
                    .hasMessageContaining(CODE);

            verify(universityRepository, never()).save(any());
        }
    }

    // =====================================================
    // countActive
    // =====================================================

    @Nested
    @DisplayName("countActive")
    class CountActive {

        @Test
        @DisplayName("delegates to repository countActiveUniversities")
        void countActive_delegatesToRepository() {
            when(universityRepository.countActiveUniversities()).thenReturn(42L);

            long count = universityService.countActive();

            assertThat(count).isEqualTo(42L);
            verify(universityRepository).countActiveUniversities();
        }

        @Test
        @DisplayName("returns zero when no active universities")
        void countActive_noActive_returnsZero() {
            when(universityRepository.countActiveUniversities()).thenReturn(0L);

            long count = universityService.countActive();

            assertThat(count).isZero();
            verify(universityRepository).countActiveUniversities();
        }
    }

    // =====================================================
    // existsByCode (via repository)
    // =====================================================

    @Nested
    @DisplayName("existsByCode")
    class ExistsByCode {

        @Test
        @DisplayName("existing code - delegates to repository and returns true")
        void existsByCode_exists_delegatesToRepository() {
            when(universityRepository.existsByCode(CODE)).thenReturn(true);

            boolean result = universityRepository.existsByCode(CODE);

            assertThat(result).isTrue();
            verify(universityRepository).existsByCode(CODE);
        }

        @Test
        @DisplayName("non-existing code - delegates to repository and returns false")
        void existsByCode_notExists_delegatesToRepository() {
            when(universityRepository.existsByCode("NONEXISTENT")).thenReturn(false);

            boolean result = universityRepository.existsByCode("NONEXISTENT");

            assertThat(result).isFalse();
            verify(universityRepository).existsByCode("NONEXISTENT");
        }
    }
}
