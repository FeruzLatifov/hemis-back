package uz.hemis.service.academic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.dto.academic.ExamDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.student.Exam;
import uz.hemis.service.academic.mapper.ExamMapper;
import uz.hemis.service.security.TenantGuard;
import uz.hemis.domain.repository.ExamRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final TenantGuard tenantGuard;

    @Audited(action = AuditAction.CREATE, entity = "Exam", entityClass = Exam.class)
    @Transactional
    public ExamDto create(ExamDto dto) {
        // OWASP A01 — caller cannot create Exam for foreign OTM (mass-assignment).
        if (dto.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(dto.getUniversity());
        }
        Exam exam = examMapper.toEntity(dto);
        return examMapper.toDto(examRepository.save(exam));
    }

    public ExamDto findById(UUID id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
        // OWASP A01 BOLA — caller must own the exam's university.
        if (exam.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(exam.getUniversity());
        }
        return examMapper.toDto(exam);
    }

    public Page<ExamDto> findAll(Pageable pageable) {
        return examRepository.findAll(pageable).map(examMapper::toDto);
    }

    public Page<ExamDto> findByCourse(UUID courseId, Pageable pageable) {
        return examRepository.findByCourse(courseId, pageable).map(examMapper::toDto);
    }

    public Page<ExamDto> findByGroup(UUID groupId, Pageable pageable) {
        return examRepository.findByGroup(groupId, pageable).map(examMapper::toDto);
    }

    public Page<ExamDto> findByTeacher(UUID teacherId, Pageable pageable) {
        return examRepository.findByTeacher(teacherId, pageable).map(examMapper::toDto);
    }

    public Page<ExamDto> findPublishedExams(Pageable pageable) {
        return examRepository.findPublishedExams(pageable).map(examMapper::toDto);
    }

    public List<ExamDto> findByGroupAndDate(UUID groupId, LocalDate date) {
        return examRepository.findByGroupAndDate(groupId, date)
                .stream().map(examMapper::toDto).collect(Collectors.toList());
    }

    public long countByCourse(UUID courseId) {
        return examRepository.countByCourse(courseId);
    }

    @Audited(action = AuditAction.UPDATE, entity = "Exam", entityClass = Exam.class, keyArg = "id")
    @Transactional
    public ExamDto update(UUID id, ExamDto dto) {
        Exam existing = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
        // OWASP A01 BOLA — caller must own the exam's university.
        if (existing.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(existing.getUniversity());
        }
        // Mass-assignment defense — body cannot relocate exam to foreign OTM.
        String originalUniversity = existing.getUniversity();
        examMapper.updateEntityFromDto(dto, existing);
        existing.setUniversity(originalUniversity); // pin to original
        return examMapper.toDto(examRepository.save(existing));
    }

    @Audited(action = AuditAction.DELETE, entity = "Exam", entityClass = Exam.class, keyArg = "id")
    @Transactional
    public void softDelete(UUID id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
        // OWASP A01 BOLA — caller must own the exam's university.
        if (exam.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(exam.getUniversity());
        }
        exam.setDeleteTs(LocalDateTime.now());
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        exam.setDeletedBy(auth != null ? auth.getName() : "system");
        examRepository.save(exam);
    }
}
