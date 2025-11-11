package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.GradeDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Grade;
import uz.hemis.domain.mapper.GradeMapper;
import uz.hemis.domain.repository.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;
    private final CourseRepository courseRepository;
    private final UniversityRepository universityRepository;

    @Transactional
    public GradeDto create(GradeDto dto) {
        validateForCreate(dto);
        Grade grade = gradeMapper.toEntity(dto);
        Grade saved = gradeRepository.save(grade);
        return gradeMapper.toDto(saved);
    }

    private void validateForCreate(GradeDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getCourse() != null && !courseRepository.existsById(dto.getCourse())) {
            errors.put("course", "Course not found");
        }
        if (dto.getUniversity() != null && !universityRepository.existsByCode(dto.getUniversity())) {
            errors.put("university", "University not found");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Grade validation failed", errors);
        }
    }

    public GradeDto findById(UUID id) {
        return gradeRepository.findById(id)
                .map(gradeMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + id));
    }

    public Page<GradeDto> findAll(Pageable pageable) {
        return gradeRepository.findAll(pageable).map(gradeMapper::toDto);
    }

    public Page<GradeDto> findByStudent(UUID studentId, Pageable pageable) {
        return gradeRepository.findByStudent(studentId, pageable).map(gradeMapper::toDto);
    }

    public Page<GradeDto> findByCourse(UUID courseId, Pageable pageable) {
        return gradeRepository.findByCourse(courseId, pageable).map(gradeMapper::toDto);
    }

    public Page<GradeDto> findByStudentAndCourse(UUID studentId, UUID courseId, Pageable pageable) {
        return gradeRepository.findByStudentAndCourse(studentId, courseId, pageable).map(gradeMapper::toDto);
    }

    public Double calculateGPA(UUID studentId) {
        return gradeRepository.calculateGPA(studentId);
    }

    @Transactional
    public GradeDto update(UUID id, GradeDto dto) {
        Grade existing = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + id));
        if (Boolean.TRUE.equals(existing.getIsFinalized())) {
            throw new ValidationException("Cannot update finalized grade");
        }
        gradeMapper.updateEntityFromDto(dto, existing);
        return gradeMapper.toDto(gradeRepository.save(existing));
    }

    @Transactional
    public void softDelete(UUID id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + id));
        grade.setDeleteTs(LocalDateTime.now());
        gradeRepository.save(grade);
    }

    public long countByStudent(UUID studentId) {
        return gradeRepository.countByStudent(studentId);
    }
}
