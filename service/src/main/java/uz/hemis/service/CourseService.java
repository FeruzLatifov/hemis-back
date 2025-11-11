package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.CourseDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Course;
import uz.hemis.domain.mapper.CourseMapper;
import uz.hemis.domain.repository.CourseRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Course business logic
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong> Soft delete ONLY</p>
 *
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UniversityRepository universityRepository;

    @Transactional
    public CourseDto create(CourseDto dto) {
        log.debug("Creating course: {}", dto);
        validateForCreate(dto);
        Course course = courseMapper.toEntity(dto);
        Course saved = courseRepository.save(course);
        log.info("Created course: id={}, code={}", saved.getId(), saved.getCode());
        return courseMapper.toDto(saved);
    }

    private void validateForCreate(CourseDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            errors.put("code", "Course code is required");
        }
        if (dto.getCode() != null && courseRepository.existsByCode(dto.getCode())) {
            errors.put("code", "Course with code '" + dto.getCode() + "' already exists");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            errors.put("name", "Course name is required");
        }
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Course validation failed", errors);
        }
    }

    public CourseDto findById(UUID id) {
        log.debug("Finding course by id: {}", id);
        return courseRepository.findById(id)
                .map(courseMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    public CourseDto findByCode(String code) {
        log.debug("Finding course by code: {}", code);
        return courseRepository.findByCode(code)
                .map(courseMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + code));
    }

    public Page<CourseDto> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable).map(courseMapper::toDto);
    }

    public Page<CourseDto> findByUniversity(String universityCode, Pageable pageable) {
        return courseRepository.findByUniversity(universityCode, pageable).map(courseMapper::toDto);
    }

    public List<CourseDto> findAllByUniversity(String universityCode) {
        return courseRepository.findAllByUniversity(universityCode)
                .stream().map(courseMapper::toDto).collect(Collectors.toList());
    }

    public Page<CourseDto> findBySubject(UUID subjectId, Pageable pageable) {
        return courseRepository.findBySubject(subjectId, pageable).map(courseMapper::toDto);
    }

    public Page<CourseDto> findByNameContaining(String name, Pageable pageable) {
        return courseRepository.findByNameContainingIgnoreCase(name, pageable).map(courseMapper::toDto);
    }

    public Page<CourseDto> findActive(Pageable pageable) {
        return courseRepository.findByActiveTrue(pageable).map(courseMapper::toDto);
    }

    public Page<CourseDto> findByCourseType(String typeCode, Pageable pageable) {
        return courseRepository.findByCourseType(typeCode, pageable).map(courseMapper::toDto);
    }

    public Page<CourseDto> findByAssessmentType(String assessmentCode, Pageable pageable) {
        return courseRepository.findByAssessmentType(assessmentCode, pageable).map(courseMapper::toDto);
    }

    public Page<CourseDto> findElectiveCourses(Pageable pageable) {
        return courseRepository.findElectiveCourses(pageable).map(courseMapper::toDto);
    }

    public Page<CourseDto> findBySemester(Integer semester, Pageable pageable) {
        return courseRepository.findBySemester(semester, pageable).map(courseMapper::toDto);
    }

    public Page<CourseDto> findByUniversityAndSemester(String universityCode, Integer semester, Pageable pageable) {
        return courseRepository.findByUniversityAndSemester(universityCode, semester, pageable).map(courseMapper::toDto);
    }

    @Transactional
    public CourseDto update(UUID id, CourseDto dto) {
        log.debug("Updating course: id={}", id);
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
        validateForUpdate(id, dto);
        courseMapper.updateEntityFromDto(dto, existing);
        Course updated = courseRepository.save(existing);
        log.info("Updated course: id={}, code={}", updated.getId(), updated.getCode());
        return courseMapper.toDto(updated);
    }

    private void validateForUpdate(UUID id, CourseDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            if (courseRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
                errors.put("code", "Course with code '" + dto.getCode() + "' already exists");
            }
        }
        if (dto.getUniversity() != null && !dto.getUniversity().isBlank()) {
            if (!universityRepository.existsByCode(dto.getUniversity())) {
                errors.put("university", "University with code '" + dto.getUniversity() + "' not found");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Course validation failed", errors);
        }
    }

    @Transactional
    public void softDelete(UUID id) {
        log.debug("Soft deleting course: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
        course.setDeleteTs(LocalDateTime.now());
        courseRepository.save(course);
        log.info("Soft deleted course: id={}, code={}", course.getId(), course.getCode());
    }

    public long countByUniversity(String universityCode) {
        return courseRepository.countByUniversity(universityCode);
    }

    public long countBySubject(UUID subjectId) {
        return courseRepository.countBySubject(subjectId);
    }

    public boolean existsByCode(String code) {
        return courseRepository.existsByCode(code);
    }
}
