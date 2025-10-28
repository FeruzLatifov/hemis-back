package uz.hemis.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.ExamDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Exam;
import uz.hemis.domain.mapper.ExamMapper;
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

    @Transactional
    public ExamDto create(ExamDto dto) {
        Exam exam = examMapper.toEntity(dto);
        return examMapper.toDto(examRepository.save(exam));
    }

    public ExamDto findById(UUID id) {
        return examRepository.findById(id)
                .map(examMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
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

    @Transactional
    public ExamDto update(UUID id, ExamDto dto) {
        Exam existing = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
        examMapper.updateEntityFromDto(dto, existing);
        return examMapper.toDto(examRepository.save(existing));
    }

    @Transactional
    public void softDelete(UUID id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
        exam.setDeleteTs(LocalDateTime.now());
        examRepository.save(exam);
    }
}
