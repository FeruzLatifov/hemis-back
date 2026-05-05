package uz.hemis.service.academic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.dto.academic.GradeDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.student.Grade;
import uz.hemis.service.academic.mapper.GradeMapper;
import uz.hemis.service.security.TenantGuard;
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
    private final TenantGuard tenantGuard;

    @Audited(action = AuditAction.CREATE, entity = "Grade", entityClass = Grade.class)
    @Transactional
    public GradeDto create(GradeDto dto) {
        validateForCreate(dto);
        // OWASP A01 — caller cannot create Grade for foreign OTM (mass-assignment).
        if (dto.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(dto.getUniversity());
        }
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
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + id));
        // OWASP A01 BOLA — caller must own the grade's university.
        if (grade.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(grade.getUniversity());
        }
        return gradeMapper.toDto(grade);
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

    @Audited(action = AuditAction.UPDATE, entity = "Grade", entityClass = Grade.class, keyArg = "id")
    @Transactional
    public GradeDto update(UUID id, GradeDto dto) {
        Grade existing = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + id));
        // OWASP A01 BOLA — caller must own the grade's university.
        if (existing.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(existing.getUniversity());
        }
        // Re-fetch with @Version to fail concurrent updates (optimistic lock).
        // If another transaction finalized between read and update, save() will throw
        // OptimisticLockException — caller retries with fresh state. Race window closed.
        if (Boolean.TRUE.equals(existing.getIsFinalized())) {
            throw new ValidationException("Cannot update finalized grade");
        }
        // Mass-assignment defense — body cannot relocate grade to foreign OTM.
        String originalUniversity = existing.getUniversity();
        gradeMapper.updateEntityFromDto(dto, existing);
        existing.setUniversity(originalUniversity); // pin to original
        return gradeMapper.toDto(gradeRepository.save(existing));
    }

    @Audited(action = AuditAction.DELETE, entity = "Grade", entityClass = Grade.class, keyArg = "id")
    @Transactional
    public void softDelete(UUID id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + id));
        // OWASP A01 BOLA — caller must own the grade's university.
        if (grade.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(grade.getUniversity());
        }
        grade.setDeleteTs(LocalDateTime.now());
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        grade.setDeletedBy(auth != null ? auth.getName() : "system");
        gradeRepository.save(grade);
    }

    public long countByStudent(UUID studentId) {
        return gradeRepository.countByStudent(studentId);
    }
}
