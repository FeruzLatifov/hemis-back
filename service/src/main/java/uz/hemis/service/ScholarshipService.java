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
import uz.hemis.common.dto.ScholarshipDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Scholarship;
import uz.hemis.domain.mapper.ScholarshipMapper;
import uz.hemis.domain.repository.ScholarshipRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scholarship Service - Business Logic Layer
 *
 * <p><strong>Legacy Reference:</strong></p>
 * <p>Old-HEMIS: ScholarshipService (student financial aid management)</p>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipMapper scholarshipMapper;

    @Cacheable(value = "scholarships", key = "#id", unless = "#result == null")
    public ScholarshipDto findById(UUID id) {
        log.debug("Finding scholarship by ID: {}", id);
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship", "id", id));
        return scholarshipMapper.toDto(scholarship);
    }

    @Cacheable(value = "scholarships", key = "'code:' + #code", unless = "#result == null")
    public ScholarshipDto findByCode(String code) {
        log.debug("Finding scholarship by code: {}", code);
        Scholarship scholarship = scholarshipRepository.findByScholarshipCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship", "code", code));
        return scholarshipMapper.toDto(scholarship);
    }

    public Page<ScholarshipDto> findAll(Pageable pageable) {
        Page<Scholarship> scholarships = scholarshipRepository.findAll(pageable);
        return scholarships.map(scholarshipMapper::toDto);
    }

    public Page<ScholarshipDto> findByUniversity(String universityCode, Pageable pageable) {
        Page<Scholarship> scholarships = scholarshipRepository.findByUniversity(universityCode, pageable);
        return scholarships.map(scholarshipMapper::toDto);
    }

    public List<ScholarshipDto> findByStudent(UUID studentId) {
        List<Scholarship> scholarships = scholarshipRepository.findByStudent(studentId);
        return scholarshipMapper.toDtoList(scholarships);
    }

    public List<ScholarshipDto> findActiveByStudent(UUID studentId) {
        List<Scholarship> scholarships = scholarshipRepository.findActiveByStudent(studentId);
        return scholarshipMapper.toDtoList(scholarships);
    }

    public Page<ScholarshipDto> findByUniversityAndYear(String universityCode, String year, Pageable pageable) {
        Page<Scholarship> scholarships = scholarshipRepository.findByUniversityAndYear(universityCode, year, pageable);
        return scholarships.map(scholarshipMapper::toDto);
    }

    public long countActiveByUniversityAndYear(String universityCode, String year) {
        return scholarshipRepository.countActiveByUniversityAndYear(universityCode, year);
    }

    public BigDecimal sumAmountByUniversityAndYear(String universityCode, String year) {
        BigDecimal sum = scholarshipRepository.sumAmountByUniversityAndYear(universityCode, year);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Transactional
    @CachePut(value = "scholarships", key = "#result.id")
    public ScholarshipDto create(ScholarshipDto scholarshipDto) {
        log.info("Creating scholarship: {}", scholarshipDto.getScholarshipCode());

        if (scholarshipDto.getScholarshipCode() != null &&
                scholarshipRepository.existsByScholarshipCode(scholarshipDto.getScholarshipCode())) {
            throw new ValidationException("Scholarship with this code already exists", "code", "Code must be unique");
        }

        Scholarship scholarship = scholarshipMapper.toEntity(scholarshipDto);
        Scholarship saved = scholarshipRepository.save(scholarship);
        log.info("Scholarship created: {}", saved.getId());
        return scholarshipMapper.toDto(saved);
    }

    @Transactional
    @CachePut(value = "scholarships", key = "#id")
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
    @CacheEvict(value = "scholarships", allEntries = true)
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
