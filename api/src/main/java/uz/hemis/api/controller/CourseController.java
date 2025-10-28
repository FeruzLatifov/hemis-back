package uz.hemis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uz.hemis.common.dto.CourseDto;
import uz.hemis.common.dto.response.PageResponse;
import uz.hemis.common.dto.response.ResponseWrapper;
import uz.hemis.domain.service.CourseService;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Course operations
 *
 * <p><strong>Base URL:</strong> /app/rest/v2/courses</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course", description = "Course management API")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get all courses")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getAllCourses(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> page = courseService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<ResponseWrapper<CourseDto>> getCourseById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(courseService.findById(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get course by code")
    public ResponseEntity<ResponseWrapper<CourseDto>> getCourseByCode(@PathVariable String code) {
        return ResponseEntity.ok(ResponseWrapper.success(courseService.findByCode(code)));
    }

    @GetMapping("/university/{code}")
    @Operation(summary = "Get courses by university")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesByUniversity(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CourseDto> page = courseService.findByUniversity(code, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/university/{code}/all")
    @Operation(summary = "Get all courses by university (no pagination)")
    public ResponseEntity<ResponseWrapper<List<CourseDto>>> getAllCoursesByUniversity(@PathVariable String code) {
        return ResponseEntity.ok(ResponseWrapper.success(courseService.findAllByUniversity(code)));
    }

    @GetMapping("/subject/{id}")
    @Operation(summary = "Get courses by subject")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesBySubject(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CourseDto> page = courseService.findBySubject(id, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses by name")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> searchCourses(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CourseDto> page = courseService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active courses")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getActiveCourses(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CourseDto> page = courseService.findActive(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/elective")
    @Operation(summary = "Get elective courses")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getElectiveCourses(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CourseDto> page = courseService.findElectiveCourses(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/semester/{semester}")
    @Operation(summary = "Get courses by semester")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesBySemester(
            @PathVariable Integer semester,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CourseDto> page = courseService.findBySemester(semester, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/university/{code}/semester/{semester}")
    @Operation(summary = "Get courses by university and semester")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesByUniversityAndSemester(
            @PathVariable String code,
            @PathVariable Integer semester,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CourseDto> page = courseService.findByUniversityAndSemester(code, semester, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/university/{code}/count")
    @Operation(summary = "Count courses by university")
    public ResponseEntity<ResponseWrapper<Long>> countCoursesByUniversity(@PathVariable String code) {
        return ResponseEntity.ok(ResponseWrapper.success(courseService.countByUniversity(code)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Create course")
    public ResponseEntity<ResponseWrapper<CourseDto>> createCourse(@Valid @RequestBody CourseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(courseService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Update course")
    public ResponseEntity<ResponseWrapper<CourseDto>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseDto dto
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(courseService.update(id, dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete course")
    public ResponseEntity<ResponseWrapper<Void>> deactivateCourse(@PathVariable UUID id) {
        courseService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Course deactivated successfully"));
    }
}
