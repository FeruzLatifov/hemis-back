package uz.hemis.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.EnrollmentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Enrollment;
import uz.hemis.domain.mapper.EnrollmentMapper;
import uz.hemis.domain.repository.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final UniversityRepository universityRepository;
    private final SpecialtyRepository specialtyRepository;
    private final FacultyRepository facultyRepository;

    @Transactional
    public EnrollmentDto create(EnrollmentDto dto) {
        validateForCreate(dto);
        Enrollment enrollment = enrollmentMapper.toEntity(dto);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return enrollmentMapper.toDto(saved);
    }

    private void validateForCreate(EnrollmentDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getEnrollmentNumber() != null && enrollmentRepository.existsByEnrollmentNumber(dto.getEnrollmentNumber())) {
            errors.put("enrollmentNumber", "Enrollment number already exists");
        }
        if (dto.getUniversity() != null && !universityRepository.existsByCode(dto.getUniversity())) {
            errors.put("university", "University not found");
        }
        if (dto.getSpecialty() != null && !specialtyRepository.existsById(dto.getSpecialty())) {
            errors.put("specialty", "Specialty not found");
        }
        if (dto.getFaculty() != null && !facultyRepository.existsById(dto.getFaculty())) {
            errors.put("faculty", "Faculty not found");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Enrollment validation failed", errors);
        }
    }

    public EnrollmentDto findById(UUID id) {
        return enrollmentRepository.findById(id)
                .map(enrollmentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + id));
    }

    public Page<EnrollmentDto> findAll(Pageable pageable) {
        return enrollmentRepository.findAll(pageable).map(enrollmentMapper::toDto);
    }

    public Page<EnrollmentDto> findByStudent(UUID studentId, Pageable pageable) {
        return enrollmentRepository.findByStudent(studentId, pageable).map(enrollmentMapper::toDto);
    }

    public Page<EnrollmentDto> findByUniversity(String universityCode, Pageable pageable) {
        return enrollmentRepository.findByUniversity(universityCode, pageable).map(enrollmentMapper::toDto);
    }

    public Page<EnrollmentDto> findBySpecialty(UUID specialtyId, Pageable pageable) {
        return enrollmentRepository.findBySpecialty(specialtyId, pageable).map(enrollmentMapper::toDto);
    }

    @Transactional
    public EnrollmentDto update(UUID id, EnrollmentDto dto) {
        Enrollment existing = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + id));
        enrollmentMapper.updateEntityFromDto(dto, existing);
        return enrollmentMapper.toDto(enrollmentRepository.save(existing));
    }

    @Transactional
    public void softDelete(UUID id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + id));
        enrollment.setDeleteTs(LocalDateTime.now());
        enrollmentRepository.save(enrollment);
    }

    public long countByUniversity(String universityCode) {
        return enrollmentRepository.countByUniversity(universityCode);
    }
}
