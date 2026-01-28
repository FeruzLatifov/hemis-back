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
import uz.hemis.common.dto.EnrollmentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Enrollment;
import uz.hemis.domain.mapper.EnrollmentMapper;
import uz.hemis.domain.repository.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private EnrollmentMapper enrollmentMapper;
    @Mock private UniversityRepository universityRepository;
    @Mock private SpecialtyRepository specialtyRepository;
    @Mock private FacultyRepository facultyRepository;
    @InjectMocks private EnrollmentService enrollmentService;

    private Enrollment testEnrollment;
    private EnrollmentDto testEnrollmentDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testEnrollment = new Enrollment();
        testEnrollment.setId(testId);
        testEnrollment.setEnrollmentNumber("ENR2024001");
        testEnrollment.setUniversity("UNI001");
        testEnrollment.setActive(true);

        testEnrollmentDto = new EnrollmentDto();
        testEnrollmentDto.setId(testId);
        testEnrollmentDto.setEnrollmentNumber("ENR2024001");
        testEnrollmentDto.setUniversity("UNI001");
        testEnrollmentDto.setActive(true);
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedEnrollment() {
        when(enrollmentRepository.existsByEnrollmentNumber("ENR2024001")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);
        when(enrollmentMapper.toEntity(testEnrollmentDto)).thenReturn(testEnrollment);
        when(enrollmentRepository.save(testEnrollment)).thenReturn(testEnrollment);
        when(enrollmentMapper.toDto(testEnrollment)).thenReturn(testEnrollmentDto);

        EnrollmentDto result = enrollmentService.create(testEnrollmentDto);

        assertThat(result).isNotNull();
        verify(enrollmentRepository).save(testEnrollment);
    }

    @Test
    void create_WithDuplicateEnrollmentNumber_ShouldThrowValidationException() {
        when(enrollmentRepository.existsByEnrollmentNumber("ENR2024001")).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.create(testEnrollmentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_WithNonExistentUniversity_ShouldThrowValidationException() {
        when(enrollmentRepository.existsByEnrollmentNumber("ENR2024001")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.create(testEnrollmentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University not found");
    }

    @Test
    void findById_WithExistingId_ShouldReturnEnrollment() {
        when(enrollmentRepository.findById(testId)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentMapper.toDto(testEnrollment)).thenReturn(testEnrollmentDto);

        EnrollmentDto result = enrollmentService.findById(testId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(enrollmentRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.findById(testId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_ShouldReturnPageOfEnrollments() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Enrollment> page = new PageImpl<>(Arrays.asList(testEnrollment));

        when(enrollmentRepository.findAll(pageable)).thenReturn(page);
        when(enrollmentMapper.toDto(testEnrollment)).thenReturn(testEnrollmentDto);

        Page<EnrollmentDto> result = enrollmentService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByStudent_ShouldReturnPageOfEnrollments() {
        UUID studentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Enrollment> page = new PageImpl<>(Arrays.asList(testEnrollment));

        when(enrollmentRepository.findByStudent(studentId, pageable)).thenReturn(page);
        when(enrollmentMapper.toDto(testEnrollment)).thenReturn(testEnrollmentDto);

        Page<EnrollmentDto> result = enrollmentService.findByStudent(studentId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedEnrollment() {
        EnrollmentDto updateDto = new EnrollmentDto();
        updateDto.setActive(false);

        when(enrollmentRepository.findById(testId)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(testEnrollment)).thenReturn(testEnrollment);
        when(enrollmentMapper.toDto(testEnrollment)).thenReturn(testEnrollmentDto);

        EnrollmentDto result = enrollmentService.update(testId, updateDto);

        assertThat(result).isNotNull();
        verify(enrollmentMapper).updateEntityFromDto(updateDto, testEnrollment);
    }

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        when(enrollmentRepository.findById(testId)).thenReturn(Optional.of(testEnrollment));

        enrollmentService.softDelete(testId);

        assertThat(testEnrollment.getDeleteTs()).isNotNull();
        verify(enrollmentRepository).save(testEnrollment);
    }

    @Test
    void countByUniversity_ShouldReturnCount() {
        when(enrollmentRepository.countByUniversity("UNI001")).thenReturn(10L);

        long count = enrollmentService.countByUniversity("UNI001");

        assertThat(count).isEqualTo(10L);
    }
}
