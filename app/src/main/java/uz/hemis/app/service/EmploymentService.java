package uz.hemis.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.EmploymentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Employment;
import uz.hemis.domain.mapper.EmploymentMapper;
import uz.hemis.domain.repository.EmploymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final EmploymentMapper employmentMapper;

    @Cacheable(value = "employments", key = "#id", unless = "#result == null")
    public EmploymentDto findById(UUID id) {
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "id", id));
        return employmentMapper.toDto(employment);
    }

    @Cacheable(value = "employments", key = "'code:' + #code", unless = "#result == null")
    public EmploymentDto findByCode(String code) {
        Employment employment = employmentRepository.findByEmploymentCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "code", code));
        return employmentMapper.toDto(employment);
    }

    public Page<EmploymentDto> findAll(Pageable pageable) {
        return employmentRepository.findAll(pageable).map(employmentMapper::toDto);
    }

    public Page<EmploymentDto> findByUniversity(String universityCode, Pageable pageable) {
        return employmentRepository.findByUniversity(universityCode, pageable).map(employmentMapper::toDto);
    }

    public List<EmploymentDto> findByStudent(UUID studentId) {
        return employmentMapper.toDtoList(employmentRepository.findByStudent(studentId));
    }

    public List<EmploymentDto> findActiveByStudent(UUID studentId) {
        return employmentMapper.toDtoList(employmentRepository.findActiveByStudent(studentId));
    }

    public List<EmploymentDto> findActiveByUniversity(String universityCode) {
        return employmentMapper.toDtoList(employmentRepository.findActiveByUniversity(universityCode));
    }

    public long countActiveByUniversity(String universityCode) {
        return employmentRepository.countActiveByUniversity(universityCode);
    }

    public long countSpecialtyRelatedByUniversity(String universityCode) {
        return employmentRepository.countSpecialtyRelatedByUniversity(universityCode);
    }

    public Page<EmploymentDto> findByUniversityAndStatus(String universityCode, String status, Pageable pageable) {
        return employmentRepository.findByUniversityAndStatus(universityCode, status, pageable).map(employmentMapper::toDto);
    }

    @Transactional
    @CachePut(value = "employments", key = "#result.id")
    public EmploymentDto create(EmploymentDto employmentDto) {
        log.info("Creating employment: {}", employmentDto.getEmploymentCode());

        if (employmentDto.getEmploymentCode() != null &&
                employmentRepository.existsByEmploymentCode(employmentDto.getEmploymentCode())) {
            throw new ValidationException("Employment with this code already exists", "code", "Code must be unique");
        }

        Employment employment = employmentMapper.toEntity(employmentDto);
        Employment saved = employmentRepository.save(employment);
        log.info("Employment created: {}", saved.getId());
        return employmentMapper.toDto(saved);
    }

    @Transactional
    @CachePut(value = "employments", key = "#id")
    public EmploymentDto update(UUID id, EmploymentDto employmentDto) {
        log.info("Updating employment: {}", id);

        Employment existing = employmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "id", id));

        employmentMapper.updateEntityFromDto(employmentDto, existing);
        Employment updated = employmentRepository.save(existing);
        log.info("Employment updated: {}", id);
        return employmentMapper.toDto(updated);
    }

    @Transactional
    @CacheEvict(value = "employments", allEntries = true)
    public void softDelete(UUID id) {
        log.warn("Soft deleting employment: {}", id);
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "id", id));

        if (employment.isDeleted()) {
            log.warn("Employment already deleted: {}", id);
            return;
        }

        employment.setDeleteTs(LocalDateTime.now());
        employmentRepository.save(employment);
        log.warn("Employment soft deleted: {}", id);
    }
}
