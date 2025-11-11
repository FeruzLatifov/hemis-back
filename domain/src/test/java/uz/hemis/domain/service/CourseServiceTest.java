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
import uz.hemis.common.dto.CourseDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Course;
import uz.hemis.domain.mapper.CourseMapper;
import uz.hemis.domain.repository.CourseRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseMapper courseMapper;
    @Mock private UniversityRepository universityRepository;
    @InjectMocks private CourseService courseService;

    private Course testCourse;
    private CourseDto testCourseDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCourse = new Course();
        testCourse.setId(testId);
        testCourse.setCode("CS101");
        testCourse.setName("Introduction to Computer Science");
        testCourse.setUniversity("UNI001");
        testCourse.setCreditCount(3);
        testCourse.setActive(true);

        testCourseDto = new CourseDto();
        testCourseDto.setId(testId);
        testCourseDto.setCode("CS101");
        testCourseDto.setName("Introduction to Computer Science");
        testCourseDto.setUniversity("UNI001");
        testCourseDto.setCreditCount(3);
        testCourseDto.setActive(true);
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedCourse() {
        when(courseRepository.existsByCode("CS101")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);
        when(courseMapper.toEntity(testCourseDto)).thenReturn(testCourse);
        when(courseRepository.save(testCourse)).thenReturn(testCourse);
        when(courseMapper.toDto(testCourse)).thenReturn(testCourseDto);

        CourseDto result = courseService.create(testCourseDto);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("CS101");
        verify(courseRepository).save(testCourse);
    }

    @Test
    void create_WithDuplicateCode_ShouldThrowValidationException() {
        when(courseRepository.existsByCode("CS101")).thenReturn(true);

        assertThatThrownBy(() -> courseService.create(testCourseDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
        verify(courseRepository, never()).save(any());
    }

    @Test
    void create_WithNullCode_ShouldThrowValidationException() {
        testCourseDto.setCode(null);

        assertThatThrownBy(() -> courseService.create(testCourseDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code is required");
    }

    @Test
    void create_WithNullName_ShouldThrowValidationException() {
        testCourseDto.setName(null);
        when(courseRepository.existsByCode("CS101")).thenReturn(false);

        assertThatThrownBy(() -> courseService.create(testCourseDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    void create_WithNonExistentUniversity_ShouldThrowValidationException() {
        when(courseRepository.existsByCode("CS101")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(false);

        assertThatThrownBy(() -> courseService.create(testCourseDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("not found");
    }

    @Test
    void findById_WithExistingId_ShouldReturnCourse() {
        when(courseRepository.findById(testId)).thenReturn(Optional.of(testCourse));
        when(courseMapper.toDto(testCourse)).thenReturn(testCourseDto);

        CourseDto result = courseService.findById(testId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        verify(courseRepository).findById(testId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(courseRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void findByCode_WithExistingCode_ShouldReturnCourse() {
        when(courseRepository.findByCode("CS101")).thenReturn(Optional.of(testCourse));
        when(courseMapper.toDto(testCourse)).thenReturn(testCourseDto);

        CourseDto result = courseService.findByCode("CS101");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("CS101");
    }

    @Test
    void findAll_ShouldReturnPageOfCourses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(Arrays.asList(testCourse));

        when(courseRepository.findAll(pageable)).thenReturn(page);
        when(courseMapper.toDto(testCourse)).thenReturn(testCourseDto);

        Page<CourseDto> result = courseService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByUniversity_ShouldReturnPageOfCourses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(Arrays.asList(testCourse));

        when(courseRepository.findByUniversity("UNI001", pageable)).thenReturn(page);
        when(courseMapper.toDto(testCourse)).thenReturn(testCourseDto);

        Page<CourseDto> result = courseService.findByUniversity("UNI001", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAllByUniversity_ShouldReturnListOfCourses() {
        when(courseRepository.findAllByUniversity("UNI001")).thenReturn(Arrays.asList(testCourse));
        when(courseMapper.toDto(testCourse)).thenReturn(testCourseDto);

        List<CourseDto> result = courseService.findAllByUniversity("UNI001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("CS101");
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedCourse() {
        CourseDto updateDto = new CourseDto();
        updateDto.setName("Updated Course Name");

        when(courseRepository.findById(testId)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(testCourse)).thenReturn(testCourse);
        when(courseMapper.toDto(testCourse)).thenReturn(testCourseDto);

        CourseDto result = courseService.update(testId, updateDto);

        assertThat(result).isNotNull();
        verify(courseMapper).updateEntityFromDto(updateDto, testCourse);
        verify(courseRepository).save(testCourse);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(courseRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.update(testId, new CourseDto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_WithDuplicateCode_ShouldThrowValidationException() {
        CourseDto updateDto = new CourseDto();
        updateDto.setCode("CS102");

        when(courseRepository.findById(testId)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByCodeAndIdNot("CS102", testId)).thenReturn(true);

        assertThatThrownBy(() -> courseService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        when(courseRepository.findById(testId)).thenReturn(Optional.of(testCourse));

        courseService.softDelete(testId);

        assertThat(testCourse.getDeleteTs()).isNotNull();
        verify(courseRepository).save(testCourse);
    }

    @Test
    void softDelete_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(courseRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.softDelete(testId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void countByUniversity_ShouldReturnCount() {
        when(courseRepository.countByUniversity("UNI001")).thenReturn(5L);

        long count = courseService.countByUniversity("UNI001");

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void existsByCode_WhenExists_ShouldReturnTrue() {
        when(courseRepository.existsByCode("CS101")).thenReturn(true);

        boolean exists = courseService.existsByCode("CS101");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_WhenNotExists_ShouldReturnFalse() {
        when(courseRepository.existsByCode("INVALID")).thenReturn(false);

        boolean exists = courseService.existsByCode("INVALID");

        assertThat(exists).isFalse();
    }
}
