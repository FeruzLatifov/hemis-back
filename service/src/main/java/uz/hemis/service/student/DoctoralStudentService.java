package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.student.DoctoralStudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.student.DoctoralStudent;
import uz.hemis.service.student.mapper.DoctoralStudentMapper;
import uz.hemis.domain.repository.DoctoralStudentRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for DoctoralStudent entity operations
 *
 * Table: hemishe_e_doctorate_student
 * All methods match actual entity fields from ministry.sql
 */
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

    /**
     * Find by student ID number
     */
    @Cacheable(value = "doctoralStudents", key = "'studentIdNumber:' + #studentIdNumber", unless = "#result == null")
    public DoctoralStudentDto findByStudentIdNumber(String studentIdNumber) {
        DoctoralStudent doctoralStudent = doctoralStudentRepository.findByStudentIdNumber(studentIdNumber)
                .orElseThrow(() -> new ResourceNotFoundException("DoctoralStudent", "studentIdNumber", studentIdNumber));
        return doctoralStudentMapper.toDto(doctoralStudent);
    }

    /**
     * Find by passport PIN (PINFL)
     */
    @Cacheable(value = "doctoralStudents", key = "'passportPin:' + #passportPin", unless = "#result == null")
    public DoctoralStudentDto findByPassportPin(String passportPin) {
        DoctoralStudent doctoralStudent = doctoralStudentRepository.findByPassportPin(passportPin)
                .orElseThrow(() -> new ResourceNotFoundException("DoctoralStudent", "passportPin", passportPin));
        return doctoralStudentMapper.toDto(doctoralStudent);
    }

    public Page<DoctoralStudentDto> findAll(Pageable pageable) {
        return doctoralStudentRepository.findAll(pageable).map(doctoralStudentMapper::toDto);
    }

    public Page<DoctoralStudentDto> findByUniversity(String university, Pageable pageable) {
        return doctoralStudentRepository.findByUniversity(university, pageable).map(doctoralStudentMapper::toDto);
    }

    public List<DoctoralStudentDto> findActiveByUniversity(String university) {
        return doctoralStudentMapper.toDtoList(doctoralStudentRepository.findActiveByUniversity(university));
    }

    /**
     * Find by doctoral student type
     */
    public List<DoctoralStudentDto> findByDoctoralStudentType(String doctoralStudentType) {
        return doctoralStudentMapper.toDtoList(doctoralStudentRepository.findByDoctoralStudentType(doctoralStudentType));
    }

    /**
     * Find by doctorate student status
     */
    public List<DoctoralStudentDto> findByDoctorateStudentStatus(String doctorateStudentStatus) {
        return doctoralStudentMapper.toDtoList(doctoralStudentRepository.findByDoctorateStudentStatus(doctorateStudentStatus));
    }

    public long countActiveByUniversity(String university) {
        return doctoralStudentRepository.countActiveByUniversity(university);
    }

    @Transactional
    @Caching(
        put = { @CachePut(value = "doctoralStudents", key = "#result.id") },
        evict = {
            // Alias key evict: studentIdNumber:X va passportPin:X eski yozuv (null)
            // cache'lab qolgan bo'lsa, yangi yozuv ko'rinmas edi.
            @CacheEvict(value = "doctoralStudents", key = "'studentIdNumber:' + #result.studentIdNumber",
                        condition = "#result.studentIdNumber != null"),
            @CacheEvict(value = "doctoralStudents", key = "'passportPin:' + #result.passportPin",
                        condition = "#result.passportPin != null")
        }
    )
    public DoctoralStudentDto create(DoctoralStudentDto doctoralStudentDto) {
        log.info("Creating doctoral student");

        // Validate unique constraints
        if (doctoralStudentDto.getPassportPin() != null &&
                doctoralStudentRepository.existsByPassportPin(doctoralStudentDto.getPassportPin())) {
            throw new ValidationException("Doctoral student with this passport PIN already exists", "passportPin", "Passport PIN must be unique");
        }

        if (doctoralStudentDto.getStudentIdNumber() != null &&
                doctoralStudentRepository.existsByStudentIdNumber(doctoralStudentDto.getStudentIdNumber())) {
            throw new ValidationException("Doctoral student with this student ID number already exists", "studentIdNumber", "Student ID number must be unique");
        }

        DoctoralStudent doctoralStudent = doctoralStudentMapper.toEntity(doctoralStudentDto);
        DoctoralStudent saved = doctoralStudentRepository.save(doctoralStudent);
        log.info("Doctoral student created: {}", saved.getId());
        return doctoralStudentMapper.toDto(saved);
    }

    @Transactional
    @Caching(
        put = { @CachePut(value = "doctoralStudents", key = "#id") },
        evict = {
            // Alias key'lar: ham eski qiymat (DB'da update'dan oldin) ham yangi qiymat
            // (DTO'dagi) — DTO va DB farq qilishi mumkin (PIN o'zgardi). Ikkalasi ham evict.
            @CacheEvict(value = "doctoralStudents", key = "'studentIdNumber:' + #doctoralStudentDto.studentIdNumber",
                        condition = "#doctoralStudentDto.studentIdNumber != null"),
            @CacheEvict(value = "doctoralStudents", key = "'passportPin:' + #doctoralStudentDto.passportPin",
                        condition = "#doctoralStudentDto.passportPin != null"),
            @CacheEvict(value = "doctoralStudents", key = "'studentIdNumber:' + #result.studentIdNumber",
                        condition = "#result != null && #result.studentIdNumber != null"),
            @CacheEvict(value = "doctoralStudents", key = "'passportPin:' + #result.passportPin",
                        condition = "#result != null && #result.passportPin != null")
        }
    )
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

    // =====================================================
    // Legacy Entity Access (for CUBA-compatible controllers)
    // =====================================================

    /**
     * Find entity by passport PIN (PINFL) - for legacy endpoints
     * Returns Optional instead of throwing exception
     */
    public Optional<DoctoralStudent> findEntityByPassportPin(String passportPin) {
        return doctoralStudentRepository.findByPassportPin(passportPin);
    }

    /**
     * Find entity by passport number - for legacy endpoints
     */
    public Optional<DoctoralStudent> findEntityByPassportNumber(String passportNumber) {
        return doctoralStudentRepository.findByPassportNumber(passportNumber);
    }

    /**
     * Count all doctoral students
     */
    public long count() {
        return doctoralStudentRepository.count();
    }

    /**
     * Save entity directly (for legacy endpoints)
     */
    @Transactional
    public DoctoralStudent saveEntity(DoctoralStudent entity) {
        return doctoralStudentRepository.save(entity);
    }

    /**
     * Convert DoctoralStudent to CUBA-compatible Map
     */
    public Map<String, Object> toStudentMap(DoctoralStudent s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId().toString());
        map.put("studentIdNumber", s.getStudentIdNumber());
        map.put("passportPin", s.getPassportPin());
        map.put("passportNumber", s.getPassportNumber());
        map.put("firstName", s.getFirstName());
        map.put("secondName", s.getSecondName());
        map.put("thirdName", s.getThirdName());
        return map;
    }
}
