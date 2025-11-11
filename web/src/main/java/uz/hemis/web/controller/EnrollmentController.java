package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.EnrollmentService;
import uz.hemis.common.dto.EnrollmentDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.UUID;

@Tag(name = "Enrollment")
@RestController
@RequestMapping("/app/rest/v2/enrollments")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<EnrollmentDto>>> getAllEnrollments(
            @PageableDefault(size = 20, sort = "enrollmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EnrollmentDto> enrollments = enrollmentService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(enrollments)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<EnrollmentDto>> getEnrollmentById(@PathVariable UUID id) {
        EnrollmentDto enrollment = enrollmentService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(enrollment));
    }

    @GetMapping(params = "student")
    public ResponseEntity<ResponseWrapper<PageResponse<EnrollmentDto>>> getEnrollmentsByStudent(
            @RequestParam("student") UUID studentId,
            @PageableDefault(size = 20, sort = "enrollmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EnrollmentDto> enrollments = enrollmentService.findByStudent(studentId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(enrollments)));
    }

    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<EnrollmentDto>>> getEnrollmentsByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "enrollmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EnrollmentDto> enrollments = enrollmentService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(enrollments)));
    }

    @GetMapping(params = "specialty")
    public ResponseEntity<ResponseWrapper<PageResponse<EnrollmentDto>>> getEnrollmentsBySpecialty(
            @RequestParam("specialty") UUID specialtyId,
            @PageableDefault(size = 20, sort = "enrollmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EnrollmentDto> enrollments = enrollmentService.findBySpecialty(specialtyId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(enrollments)));
    }

    @GetMapping(params = "countByUniversity")
    public ResponseEntity<ResponseWrapper<Long>> countEnrollmentsByUniversity(
            @RequestParam("countByUniversity") String universityCode
    ) {
        long count = enrollmentService.countByUniversity(universityCode);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<EnrollmentDto>> createEnrollment(@Valid @RequestBody EnrollmentDto enrollmentDto) {
        EnrollmentDto created = enrollmentService.create(enrollmentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<EnrollmentDto>> updateEnrollment(
            @PathVariable UUID id,
            @Valid @RequestBody EnrollmentDto enrollmentDto
    ) {
        EnrollmentDto updated = enrollmentService.update(id, enrollmentDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteEnrollment(@PathVariable UUID id) {
        enrollmentService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
