package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.student.StudentDto;
import uz.hemis.common.dto.student.StudentIdRequest;
import uz.hemis.domain.entity.student.Student;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Student Service - Facade
 *
 * <p>Delegates to specialized services while preserving the original API contract.
 * Controllers can continue to use StudentService without any changes.</p>
 *
 * <p><strong>Delegate services:</strong></p>
 * <ul>
 *   <li>{@link StudentCoreService} - CRUD, lookup, soft delete</li>
 *   <li>{@link StudentLegacyApiService} - CUBA REST API compatible flat/legacy map methods</li>
 *   <li>{@link StudentStatisticsService} - Tashkent statistics queries</li>
 *   <li>{@link StudentEnrollmentService} - Update, validate, check, ID generation</li>
 *   <li>{@link StudentScholarshipService} - Scholarship checks, expel queries</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentCoreService coreService;
    private final StudentLegacyApiService legacyApiService;
    private final StudentStatisticsService statisticsService;
    private final StudentEnrollmentService enrollmentService;
    private final StudentScholarshipService scholarshipService;

    // =====================================================
    // Core Service delegates
    // =====================================================

    public StudentDto findById(UUID id) {
        return coreService.findById(id);
    }

    public StudentDto findByPinfl(String pinfl) {
        return coreService.findByPinfl(pinfl);
    }

    public List<StudentDto> findAllByPinfl(String pinfl) {
        return coreService.findAllByPinfl(pinfl);
    }

    public Page<StudentDto> findAll(Pageable pageable) {
        return coreService.findAll(pageable);
    }

    public Page<Student> findAllEntities(Pageable pageable) {
        return coreService.findAllEntities(pageable);
    }

    public Page<StudentDto> findByUniversity(String universityCode, Pageable pageable) {
        return coreService.findByUniversity(universityCode, pageable);
    }

    public List<StudentDto> findActiveByUniversity(String universityCode) {
        return coreService.findActiveByUniversity(universityCode);
    }

    public long countActiveByUniversity(String universityCode) {
        return coreService.countActiveByUniversity(universityCode);
    }

    public boolean existsByPinfl(String pinfl) {
        return coreService.existsByPinfl(pinfl);
    }

    public Optional<Student> findEntityById(UUID id) {
        return coreService.findEntityById(id);
    }

    public StudentDto create(StudentDto studentDto) {
        return coreService.create(studentDto);
    }

    public StudentDto update(UUID id, StudentDto studentDto) {
        return coreService.update(id, studentDto);
    }

    public StudentDto partialUpdate(UUID id, StudentDto studentDto) {
        return coreService.partialUpdate(id, studentDto);
    }

    public StudentDto softDelete(UUID id, String userUniversityCode) {
        return coreService.softDelete(id, userUniversityCode);
    }

    public void restore(UUID id) {
        coreService.restore(id);
    }

    // =====================================================
    // Legacy API Service delegates
    // =====================================================

    public Object verify(String pinfl) {
        return legacyApiService.verify(pinfl);
    }

    public Map<String, Object> getByPinflFlat(String pinfl) {
        return legacyApiService.getByPinflFlat(pinfl);
    }

    public Map<String, Object> getActiveFlat(String pinfl) {
        return legacyApiService.getActiveFlat(pinfl);
    }

    public Map<String, Object> getByCodeFlat(String code) {
        return legacyApiService.getByCodeFlat(code);
    }

    public Map<String, Object> getDoctoralFlat(String pinfl) {
        return legacyApiService.getDoctoralFlat(pinfl);
    }

    public Map<String, Object> getWithStatusFlat(String pinfl) {
        return legacyApiService.getWithStatusFlat(pinfl);
    }

    public Map<String, Object> testGetFlat(String pinfl) {
        return legacyApiService.testGetFlat(pinfl);
    }

    public Map<String, Object> getStudentsByUniversityFlatWithCount(String university, int limit, int offset) {
        return legacyApiService.getStudentsByUniversityFlatWithCount(university, limit, offset);
    }

    public Map<String, Object> getTashkentStudentsResult(int limit, int offset) {
        return legacyApiService.getTashkentStudentsResult(limit, offset);
    }

    // =====================================================
    // Scholarship Service delegates
    // =====================================================

    public Map<String, Object> isExpel(String[] pinfls) {
        return scholarshipService.isExpel(pinfls);
    }

    public Map<String, Object> checkScholarshipNative(String tin, String[] pinfls) {
        return scholarshipService.checkScholarshipNative(tin, pinfls);
    }

    public Object checkScholarship(Map<String, Object> request) {
        return scholarshipService.checkScholarship(request);
    }

    // =====================================================
    // Statistics Service delegates
    // =====================================================

    public Map<String, Object> byTashkentAndPaymentForm() {
        return statisticsService.byTashkentAndPaymentForm();
    }

    public Map<String, Object> byTashkentAndRegionDistrict() {
        return statisticsService.byTashkentAndRegionDistrict();
    }

    public Map<String, Object> byTashkentAndRegionDistrictAndEduType() {
        return statisticsService.byTashkentAndRegionDistrictAndEduType();
    }

    public Map<String, Object> byTashkentAndFacultyAndCourse() {
        return statisticsService.byTashkentAndFacultyAndCourse();
    }

    public Map<String, Object> byTashkentAndEduFormTypeAndGender() {
        return statisticsService.byTashkentAndEduFormTypeAndGender();
    }

    // =====================================================
    // Enrollment Service delegates
    // =====================================================

    public Object updateStudent(Map<String, Object> request) {
        return enrollmentService.updateStudent(request);
    }

    public Map<String, Object> validateStudent(String data) {
        return enrollmentService.validateStudent(data);
    }

    public Map<String, Object> checkStudents() {
        return enrollmentService.checkStudents();
    }

    public Map<String, Object> generateStudentId(StudentIdRequest data, String universityCode) {
        return enrollmentService.generateStudentId(data, universityCode);
    }
}
