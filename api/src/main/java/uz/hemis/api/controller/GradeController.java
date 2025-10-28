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
import uz.hemis.common.dto.GradeDto;
import uz.hemis.common.dto.response.PageResponse;
import uz.hemis.common.dto.response.ResponseWrapper;
import uz.hemis.domain.service.GradeService;

import java.util.UUID;

@RestController
@RequestMapping("/app/rest/v2/grades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Grade", description = "Grade management API")
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    @Operation(summary = "Get all grades")
    public ResponseEntity<ResponseWrapper<PageResponse<GradeDto>>> getAllGrades(
            @PageableDefault(size = 20, sort = "gradeDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GradeDto> page = gradeService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get grade by ID")
    public ResponseEntity<ResponseWrapper<GradeDto>> getGradeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(gradeService.findById(id)));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get grades by student")
    public ResponseEntity<ResponseWrapper<PageResponse<GradeDto>>> getGradesByStudent(
            @PathVariable UUID studentId,
            @PageableDefault(size = 20, sort = "gradeDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GradeDto> page = gradeService.findByStudent(studentId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get grades by course")
    public ResponseEntity<ResponseWrapper<PageResponse<GradeDto>>> getGradesByCourse(
            @PathVariable UUID courseId,
            @PageableDefault(size = 20, sort = "gradeDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GradeDto> page = gradeService.findByCourse(courseId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    @Operation(summary = "Get grades by student and course")
    public ResponseEntity<ResponseWrapper<PageResponse<GradeDto>>> getGradesByStudentAndCourse(
            @PathVariable UUID studentId,
            @PathVariable UUID courseId,
            @PageableDefault(size = 20, sort = "gradeDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GradeDto> page = gradeService.findByStudentAndCourse(studentId, courseId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/student/{studentId}/gpa")
    @Operation(summary = "Calculate student GPA")
    public ResponseEntity<ResponseWrapper<Double>> calculateStudentGPA(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ResponseWrapper.success(gradeService.calculateGPA(studentId)));
    }

    @GetMapping("/student/{studentId}/count")
    @Operation(summary = "Count grades by student")
    public ResponseEntity<ResponseWrapper<Long>> countGradesByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ResponseWrapper.success(gradeService.countByStudent(studentId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'TEACHER')")
    @Operation(summary = "Create grade")
    public ResponseEntity<ResponseWrapper<GradeDto>> createGrade(@Valid @RequestBody GradeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(gradeService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'TEACHER')")
    @Operation(summary = "Update grade")
    public ResponseEntity<ResponseWrapper<GradeDto>> updateGrade(
            @PathVariable UUID id,
            @Valid @RequestBody GradeDto dto
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(gradeService.update(id, dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete grade")
    public ResponseEntity<ResponseWrapper<Void>> deactivateGrade(@PathVariable UUID id) {
        gradeService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Grade deactivated successfully"));
    }
}
