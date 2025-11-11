package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.DoctoralStudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.DoctoralStudent;
import uz.hemis.domain.mapper.DoctoralStudentMapper;
import uz.hemis.domain.repository.DoctoralStudentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DoctoralStudentService {

    private final DoctoralStudentRepository doctoralStudentRepository;
    private final DoctoralStudentMapper doctoralStudentMapper;

    @Cacheable(value = "doctoralStudents", key = "#id", unless = "#result == null")
    public DoctoralStudentDto findById(UUID id) {
        DoctoralStudent doctoralStudent = doctoralStudentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DoctoralStudent", "id", id));
        return doctoralStudentMapper.toDto(doctoralStudent);
    }

    @Cacheable(value = "doctoralStudents", key = "'code:' + #code", unless = "#result == null")
    public DoctoralStudentDto findByCode(String code) {
        DoctoralStudent doctoralStudent = doctoralStudentRepository.findByDoctoralCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("DoctoralStudent", "code", code));
        return doctoralStudentMapper.toDto(doctoralStudent);
    }

    public DoctoralStudentDto findByStudent(UUID studentId) {
        DoctoralStudent doctoralStudent = doctoralStudentRepository.findByStudent(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctoralStudent", "studentId", studentId));
        return doctoralStudentMapper.toDto(doctoralStudent);
    }

    public Page<DoctoralStudentDto> findAll(Pageable pageable) {
        return doctoralStudentRepository.findAll(pageable).map(doctoralStudentMapper::toDto);
    }

    public Page<DoctoralStudentDto> findByUniversity(String universityCode, Pageable pageable) {
        return doctoralStudentRepository.findByUniversity(universityCode, pageable).map(doctoralStudentMapper::toDto);
    }

    public List<DoctoralStudentDto> findActiveByUniversity(String universityCode) {
        return doctoralStudentMapper.toDtoList(doctoralStudentRepository.findActiveByUniversity(universityCode));
    }

    public List<DoctoralStudentDto> findByScientificAdvisor(UUID advisorId) {
        return doctoralStudentMapper.toDtoList(doctoralStudentRepository.findByScientificAdvisor(advisorId));
    }

    public long countActiveByUniversity(String universityCode) {
        return doctoralStudentRepository.countActiveByUniversity(universityCode);
    }

    @Transactional
    @CachePut(value = "doctoralStudents", key = "#result.id")
    public DoctoralStudentDto create(DoctoralStudentDto doctoralStudentDto) {
        log.info("Creating doctoral student: {}", doctoralStudentDto.getDoctoralCode());

        if (doctoralStudentDto.getDoctoralCode() != null &&
                doctoralStudentRepository.existsByDoctoralCode(doctoralStudentDto.getDoctoralCode())) {
            throw new ValidationException("Doctoral student with this code already exists", "code", "Code must be unique");
        }

        DoctoralStudent doctoralStudent = doctoralStudentMapper.toEntity(doctoralStudentDto);
        DoctoralStudent saved = doctoralStudentRepository.save(doctoralStudent);
        log.info("Doctoral student created: {}", saved.getId());
        return doctoralStudentMapper.toDto(saved);
    }

    @Transactional
    @CachePut(value = "doctoralStudents", key = "#id")
    public DoctoralStudentDto update(UUID id, DoctoralStudentDto doctoralStudentDto) {
        log.info("Updating doctoral student: {}", id);

        DoctoralStudent existing = doctoralStudentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DoctoralStudent", "id", id));

        doctoralStudentMapper.updateEntityFromDto(doctoralStudentDto, existing);
        DoctoralStudent updated = doctoralStudentRepository.save(existing);
        log.info("Doctoral student updated: {}", id);
        return doctoralStudentMapper.toDto(updated);
    }

    @Transactional
    @CacheEvict(value = "doctoralStudents", allEntries = true)
    public void softDelete(UUID id) {
        log.warn("Soft deleting doctoral student: {}", id);
        DoctoralStudent doctoralStudent = doctoralStudentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DoctoralStudent", "id", id));

        if (doctoralStudent.isDeleted()) {
            log.warn("Doctoral student already deleted: {}", id);
            return;
        }

        doctoralStudent.setDeleteTs(LocalDateTime.now());
        doctoralStudentRepository.save(doctoralStudent);
        log.warn("Doctoral student soft deleted: {}", id);
    }
}
