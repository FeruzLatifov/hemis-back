package uz.hemis.service.student;

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
import uz.hemis.common.dto.student.EnrollmentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Enrollment;
import uz.hemis.domain.repository.EnrollmentRepository;
import uz.hemis.domain.repository.FacultyRepository;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.student.mapper.EnrollmentMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EnrollmentService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>create - validation, successful creation</li>
 *   <li>findById - found and not-found scenarios</li>
 *   <li>findAll - paginated results</li>
 *   <li>findByStudent - paginated results</li>
 *   <li>findByUniversity - paginated results</li>
 *   <li>update - found and not-found scenarios</li>
 *   <li>softDelete - sets deleteTs, not-found</li>
 *   <li>countByUniversity - delegation to repository</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Unit Tests")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private UUID enrollmentId;
    private UUID studentId;
    private UUID specialtyId;
    private UUID facultyId;
    private Enrollment enrollment;
    private EnrollmentDto enrollmentDto;

    @BeforeEach
    void setUp() {
        enrollmentId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
        facultyId = UUID.randomUUID();

        enrollment = new Enrollment();
        enrollment.setId(enrollmentId);
        enrollment.setEnrollmentNumber("ENR-2024-001");
        enrollment.setStudent(studentId);
        enrollment.setUniversity("401");
        enrollment.setSpecialty(specialtyId);
        enrollment.setFaculty(facultyId);
        enrollment.setEnrollmentDate(LocalDate.of(2024, 9, 1));
        enrollment.setAcademicYear("2024/2025");
        enrollment.setCourse(1);
        enrollment.setEducationType("11");
        enrollment.setActive(true);

        enrollmentDto = new EnrollmentDto();
        enrollmentDto.setId(enrollmentId);
        enrollmentDto.setEnrollmentNumber("ENR-2024-001");
        enrollmentDto.setStudent(studentId);
        enrollmentDto.setUniversity("401");
        enrollmentDto.setSpecialty(specialtyId);
        enrollmentDto.setFaculty(facultyId);
        enrollmentDto.setEnrollmentDate(LocalDate.of(2024, 9, 1));
        enrollmentDto.setAcademicYear("2024/2025");
        enrollmentDto.setCourse(1);
        enrollmentDto.setEducationType("11");
        enrollmentDto.setActive(true);
    }

    // =====================================================
    // create tests
    // =====================================================

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates enrollment successfully with valid data")
        void createsSuccessfully_withValidData() {
            EnrollmentDto inputDto = new EnrollmentDto();
            inputDto.setEnrollmentNumber("ENR-2024-002");
            inputDto.setUniversity("401");
            inputDto.setSpecialty(specialtyId);
            inputDto.setFaculty(facultyId);

            when(enrollmentRepository.existsByEnrollmentNumber("ENR-2024-002")).thenReturn(false);
            when(universityRepository.existsByCode("401")).thenReturn(true);
            when(specialtyRepository.existsById(specialtyId)).thenReturn(true);
            when(facultyRepository.existsById(facultyId)).thenReturn(true);
            when(enrollmentMapper.toEntity(inputDto)).thenReturn(enrollment);
            when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
            when(enrollmentMapper.toDto(enrollment)).thenReturn(enrollmentDto);

            EnrollmentDto result = enrollmentService.create(inputDto);

            assertThat(result).isNotNull();
            assertThat(result.getEnrollmentNumber()).isEqualTo("ENR-2024-001");

            verify(enrollmentRepository).save(enrollment);
        }

        @Test
        @DisplayName("throws ValidationException when enrollment number already exists")
        void throwsValidation_whenDuplicateNumber() {
            EnrollmentDto inputDto = new EnrollmentDto();
            inputDto.setEnrollmentNumber("ENR-2024-001");

            when(enrollmentRepository.existsByEnrollmentNumber("ENR-2024-001")).thenReturn(true);

            assertThatThrownBy(() -> enrollmentService.create(inputDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("validation failed");

            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ValidationException when university not found")
        void throwsValidation_whenUniversityNotFound() {
            EnrollmentDto inputDto = new EnrollmentDto();
            inputDto.setEnrollmentNumber("ENR-2024-003");
            inputDto.setUniversity("NONEXIST");

            when(enrollmentRepository.existsByEnrollmentNumber("ENR-2024-003")).thenReturn(false);
            when(universityRepository.existsByCode("NONEXIST")).thenReturn(false);

            assertThatThrownBy(() -> enrollmentService.create(inputDto))
                    .isInstanceOf(ValidationException.class);

            verify(enrollmentRepository, never()).save(any());
        }
    }

    // =====================================================
    // findById tests
    // =====================================================

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns DTO when enrollment found")
        void returnsDto_whenFound() {
            when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
            when(enrollmentMapper.toDto(enrollment)).thenReturn(enrollmentDto);

            EnrollmentDto result = enrollmentService.findById(enrollmentId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(enrollmentId);
            assertThat(result.getEnrollmentNumber()).isEqualTo("ENR-2024-001");

            verify(enrollmentRepository).findById(enrollmentId);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when enrollment not found")
        void throwsException_whenNotFound() {
            UUID missingId = UUID.randomUUID();
            when(enrollmentRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.findById(missingId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Enrollment not found");

            verify(enrollmentMapper, never()).toDto(any());
        }
    }

    // =====================================================
    // findAll tests
    // =====================================================

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns paginated enrollment DTOs")
        void returnsPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Enrollment> page = new PageImpl<>(List.of(enrollment), pageable, 1);

            when(enrollmentRepository.findAll(pageable)).thenReturn(page);
            when(enrollmentMapper.toDto(enrollment)).thenReturn(enrollmentDto);

            Page<EnrollmentDto> result = enrollmentService.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    // =====================================================
    // findByStudent tests
    // =====================================================

    @Nested
    @DisplayName("findByStudent")
    class FindByStudent {

        @Test
        @DisplayName("returns paginated enrollments for student")
        void returnsPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Enrollment> page = new PageImpl<>(List.of(enrollment), pageable, 1);

            when(enrollmentRepository.findByStudent(studentId, pageable)).thenReturn(page);
            when(enrollmentMapper.toDto(enrollment)).thenReturn(enrollmentDto);

            Page<EnrollmentDto> result = enrollmentService.findByStudent(studentId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStudent()).isEqualTo(studentId);

            verify(enrollmentRepository).findByStudent(studentId, pageable);
        }
    }

    // =====================================================
    // findByUniversity tests
    // =====================================================

    @Nested
    @DisplayName("findByUniversity")
    class FindByUniversity {

        @Test
        @DisplayName("returns paginated enrollments for university")
        void returnsPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Enrollment> page = new PageImpl<>(List.of(enrollment), pageable, 1);

            when(enrollmentRepository.findByUniversity("401", pageable)).thenReturn(page);
            when(enrollmentMapper.toDto(enrollment)).thenReturn(enrollmentDto);

            Page<EnrollmentDto> result = enrollmentService.findByUniversity("401", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(enrollmentRepository).findByUniversity("401", pageable);
        }

        @Test
        @DisplayName("returns empty page when no enrollments for university")
        void returnsEmptyPage_whenNoEnrollments() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Enrollment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(enrollmentRepository.findByUniversity("NONEXIST", pageable)).thenReturn(emptyPage);

            Page<EnrollmentDto> result = enrollmentService.findByUniversity("NONEXIST", pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // =====================================================
    // update tests
    // =====================================================

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates enrollment successfully")
        void updatesSuccessfully() {
            EnrollmentDto updateDto = new EnrollmentDto();
            updateDto.setCourse(2);
            updateDto.setAcademicYear("2025/2026");

            Enrollment updatedEntity = new Enrollment();
            updatedEntity.setId(enrollmentId);
            updatedEntity.setCourse(2);

            EnrollmentDto updatedDto = new EnrollmentDto();
            updatedDto.setId(enrollmentId);
            updatedDto.setCourse(2);

            when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
            when(enrollmentRepository.save(enrollment)).thenReturn(updatedEntity);
            when(enrollmentMapper.toDto(updatedEntity)).thenReturn(updatedDto);

            EnrollmentDto result = enrollmentService.update(enrollmentId, updateDto);

            assertThat(result).isNotNull();
            assertThat(result.getCourse()).isEqualTo(2);

            verify(enrollmentMapper).updateEntityFromDto(updateDto, enrollment);
            verify(enrollmentRepository).save(enrollment);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when enrollment not found")
        void throwsException_whenNotFound() {
            UUID missingId = UUID.randomUUID();
            EnrollmentDto updateDto = new EnrollmentDto();

            when(enrollmentRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.update(missingId, updateDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Enrollment not found");

            verify(enrollmentRepository, never()).save(any());
        }
    }

    // =====================================================
    // softDelete tests
    // =====================================================

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("sets deleteTs and saves enrollment")
        void setsDeleteTs_andSaves() {
            when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
            when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);

            enrollmentService.softDelete(enrollmentId);

            assertThat(enrollment.getDeleteTs()).isNotNull();
            verify(enrollmentRepository).save(enrollment);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when enrollment not found")
        void throwsException_whenNotFound() {
            UUID missingId = UUID.randomUUID();
            when(enrollmentRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.softDelete(missingId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Enrollment not found");

            verify(enrollmentRepository, never()).save(any());
        }
    }

    // =====================================================
    // countByUniversity tests
    // =====================================================

    @Nested
    @DisplayName("countByUniversity")
    class CountByUniversity {

        @Test
        @DisplayName("delegates to repository and returns count")
        void delegatesToRepository() {
            when(enrollmentRepository.countByUniversity("401")).thenReturn(320L);

            long count = enrollmentService.countByUniversity("401");

            assertThat(count).isEqualTo(320L);
            verify(enrollmentRepository).countByUniversity("401");
        }

        @Test
        @DisplayName("returns zero for unknown university")
        void returnsZero_forUnknownUniversity() {
            when(enrollmentRepository.countByUniversity("NONEXIST")).thenReturn(0L);

            long count = enrollmentService.countByUniversity("NONEXIST");

            assertThat(count).isEqualTo(0L);
        }
    }
}
