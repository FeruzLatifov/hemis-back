package uz.hemis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import uz.hemis.common.dto.EnrollmentDto;
import uz.hemis.common.dto.response.PageResponse;
import uz.hemis.common.dto.response.ResponseWrapper;
import uz.hemis.domain.service.EnrollmentService;

import java.util.UUID;

@RestController
@RequestMapping("/app/rest/v2/enrollments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollment", description = "Enrollment management API")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    @Operation(summary = "Get all enrollments")
    public ResponseEntity<ResponseWrapper<PageResponse<EnrollmentDto>>> getAllEnrollments(
            @PageableDefault(size = 20, sort = "enrollmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EnrollmentDto> page = enrollmentService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get enrollment by ID")
    public ResponseEntity<ResponseWrapper<EnrollmentDto>> getEnrollmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(enrollmentService.findById(id)));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get enrollments by student")
    public ResponseEntity<ResponseWrapper<PageResponse<EnrollmentDto>>> getEnrollmentsByStudent(
            @PathVariable UUID studentId,
            @PageableDefault(size = 20, sort = "enrollmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EnrollmentDto> page = enrollmentService.findByStudent(studentId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/university/{code}")
    @Operation(summary = "Get enrollments by university")
    public ResponseEntity<ResponseWrapper<PageResponse<EnrollmentDto>>> getEnrollmentsByUniversity(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "enrollmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EnrollmentDto> page = enrollmentService.findByUniversity(code, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/specialty/{id}")
    @Operation(summary = "Get enrollments by specialty")
    public ResponseEntity<ResponseWrapper<PageResponse<EnrollmentDto>>> getEnrollmentsBySpecialty(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "enrollmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EnrollmentDto> page = enrollmentService.findBySpecialty(id, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/university/{code}/count")
    @Operation(summary = "Count enrollments by university")
    public ResponseEntity<ResponseWrapper<Long>> countEnrollmentsByUniversity(@PathVariable String code) {
        return ResponseEntity.ok(ResponseWrapper.success(enrollmentService.countByUniversity(code)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Create enrollment")
    public ResponseEntity<ResponseWrapper<EnrollmentDto>> createEnrollment(@Valid @RequestBody EnrollmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(enrollmentService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Update enrollment")
    public ResponseEntity<ResponseWrapper<EnrollmentDto>> updateEnrollment(
            @PathVariable UUID id,
            @Valid @RequestBody EnrollmentDto dto
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(enrollmentService.update(id, dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete enrollment")
    public ResponseEntity<ResponseWrapper<Void>> deactivateEnrollment(@PathVariable UUID id) {
        enrollmentService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Enrollment deactivated successfully"));
    }
}
