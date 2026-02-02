package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.ScholarshipDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Scholarship;
import uz.hemis.service.mapper.ScholarshipMapper;
import uz.hemis.domain.repository.ScholarshipRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scholarship Service - Business Logic Layer
 *
 * Table: hemishe_e_student_scholarship_full
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipMapper scholarshipMapper;

    public ScholarshipDto findById(UUID id) {
        log.debug("Finding scholarship by ID: {}", id);
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship", "id", id));
        return scholarshipMapper.toDto(scholarship);
    }

    public Page<ScholarshipDto> findAll(Pageable pageable) {
        Page<Scholarship> scholarships = scholarshipRepository.findAll(pageable);
        return scholarships.map(scholarshipMapper::toDto);
    }

    public List<ScholarshipDto> findByStudent(UUID studentId) {
        List<Scholarship> scholarships = scholarshipRepository.findByStudent(studentId);
        return scholarshipMapper.toDtoList(scholarships);
    }

    public List<ScholarshipDto> findByUniversity(String universityCode) {
        List<Scholarship> scholarships = scholarshipRepository.findByUniversity(universityCode);
        return scholarshipMapper.toDtoList(scholarships);
    }

    @Transactional
    public ScholarshipDto create(ScholarshipDto scholarshipDto) {
        log.info("Creating scholarship");
        Scholarship scholarship = scholarshipMapper.toEntity(scholarshipDto);
        Scholarship saved = scholarshipRepository.save(scholarship);
        log.info("Scholarship created: {}", saved.getId());
        return scholarshipMapper.toDto(saved);
    }

    @Transactional
    public ScholarshipDto update(UUID id, ScholarshipDto scholarshipDto) {
        log.info("Updating scholarship: {}", id);
        Scholarship existing = scholarshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship", "id", id));
        scholarshipMapper.updateEntityFromDto(scholarshipDto, existing);
        Scholarship updated = scholarshipRepository.save(existing);
        log.info("Scholarship updated: {}", id);
        return scholarshipMapper.toDto(updated);
    }

    @Transactional
    public void softDelete(UUID id) {
        log.warn("Soft deleting scholarship: {}", id);
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship", "id", id));

        if (scholarship.isDeleted()) {
            log.warn("Scholarship already deleted: {}", id);
            return;
        }

        scholarship.setDeleteTs(LocalDateTime.now());
        scholarshipRepository.save(scholarship);
        log.warn("Scholarship soft deleted: {}", id);
    }
}
