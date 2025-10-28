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
import uz.hemis.common.dto.GradeDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Grade;
import uz.hemis.domain.mapper.GradeMapper;
import uz.hemis.domain.repository.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock private GradeRepository gradeRepository;
    @Mock private GradeMapper gradeMapper;
    @Mock private CourseRepository courseRepository;
    @Mock private UniversityRepository universityRepository;
    @InjectMocks private GradeService gradeService;

    private Grade testGrade;
    private GradeDto testGradeDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testGrade = new Grade();
        testGrade.setId(testId);
        testGrade.setGradeValue(85);
        testGrade.setGradeLetter("A");
        testGrade.setGradePoints(4.0);
        testGrade.setIsPassed(true);
        testGrade.setIsFinalized(false);

        testGradeDto = new GradeDto();
        testGradeDto.setId(testId);
        testGradeDto.setGradeValue(85);
        testGradeDto.setGradeLetter("A");
        testGradeDto.setGradePoints(4.0);
        testGradeDto.setIsPassed(true);
        testGradeDto.setIsFinalized(false);
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedGrade() {
        when(gradeMapper.toEntity(testGradeDto)).thenReturn(testGrade);
        when(gradeRepository.save(testGrade)).thenReturn(testGrade);
        when(gradeMapper.toDto(testGrade)).thenReturn(testGradeDto);

        GradeDto result = gradeService.create(testGradeDto);

        assertThat(result).isNotNull();
        assertThat(result.getGradeValue()).isEqualTo(85);
        verify(gradeRepository).save(testGrade);
    }

    @Test
    void create_WithNonExistentCourse_ShouldThrowValidationException() {
        UUID courseId = UUID.randomUUID();
        testGradeDto.setCourse(courseId);
        when(courseRepository.existsById(courseId)).thenReturn(false);

        assertThatThrownBy(() -> gradeService.create(testGradeDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void findById_WithExistingId_ShouldReturnGrade() {
        when(gradeRepository.findById(testId)).thenReturn(Optional.of(testGrade));
        when(gradeMapper.toDto(testGrade)).thenReturn(testGradeDto);

        GradeDto result = gradeService.findById(testId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(gradeRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.findById(testId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_ShouldReturnPageOfGrades() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Grade> page = new PageImpl<>(Arrays.asList(testGrade));

        when(gradeRepository.findAll(pageable)).thenReturn(page);
        when(gradeMapper.toDto(testGrade)).thenReturn(testGradeDto);

        Page<GradeDto> result = gradeService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByStudent_ShouldReturnPageOfGrades() {
        UUID studentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Grade> page = new PageImpl<>(Arrays.asList(testGrade));

        when(gradeRepository.findByStudent(studentId, pageable)).thenReturn(page);
        when(gradeMapper.toDto(testGrade)).thenReturn(testGradeDto);

        Page<GradeDto> result = gradeService.findByStudent(studentId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void calculateGPA_ShouldReturnGPAValue() {
        UUID studentId = UUID.randomUUID();
        when(gradeRepository.calculateGPA(studentId)).thenReturn(3.75);

        Double gpa = gradeService.calculateGPA(studentId);

        assertThat(gpa).isEqualTo(3.75);
        verify(gradeRepository).calculateGPA(studentId);
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedGrade() {
        GradeDto updateDto = new GradeDto();
        updateDto.setGradeValue(90);

        when(gradeRepository.findById(testId)).thenReturn(Optional.of(testGrade));
        when(gradeRepository.save(testGrade)).thenReturn(testGrade);
        when(gradeMapper.toDto(testGrade)).thenReturn(testGradeDto);

        GradeDto result = gradeService.update(testId, updateDto);

        assertThat(result).isNotNull();
        verify(gradeMapper).updateEntityFromDto(updateDto, testGrade);
    }

    @Test
    void update_WhenFinalized_ShouldThrowValidationException() {
        testGrade.setIsFinalized(true);
        when(gradeRepository.findById(testId)).thenReturn(Optional.of(testGrade));

        assertThatThrownBy(() -> gradeService.update(testId, new GradeDto()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("finalized");
    }

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        when(gradeRepository.findById(testId)).thenReturn(Optional.of(testGrade));

        gradeService.softDelete(testId);

        assertThat(testGrade.getDeleteTs()).isNotNull();
        verify(gradeRepository).save(testGrade);
    }

    @Test
    void countByStudent_ShouldReturnCount() {
        UUID studentId = UUID.randomUUID();
        when(gradeRepository.countByStudent(studentId)).thenReturn(15L);

        long count = gradeService.countByStudent(studentId);

        assertThat(count).isEqualTo(15L);
    }
}
