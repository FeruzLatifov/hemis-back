package uz.hemis.api.web.controller;

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
import uz.hemis.service.CourseService;
import uz.hemis.common.dto.CourseDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "Courses")
@RestController
@RequestMapping("/app/rest/v2/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getAllCourses(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(courses)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<CourseDto>> getCourseById(@PathVariable UUID id) {
        CourseDto course = courseService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(course));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<CourseDto>> getCourseByCode(@PathVariable String code) {
        CourseDto course = courseService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(course));
    }

    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(courses)));
    }

    @GetMapping(params = "universityAll")
    public ResponseEntity<ResponseWrapper<List<CourseDto>>> getAllCoursesByUniversity(
            @RequestParam("universityAll") String universityCode
    ) {
        List<CourseDto> courses = courseService.findAllByUniversity(universityCode);
        return ResponseEntity.ok(ResponseWrapper.success(courses));
    }

    @GetMapping(params = "subject")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesBySubject(
            @RequestParam("subject") UUID subjectId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findBySubject(subjectId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(courses)));
    }

    @GetMapping(params = "search")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> searchCoursesByName(
            @RequestParam("search") String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(courses)));
    }

    @GetMapping(params = "active")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getActiveCourses(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findActive(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(courses)));
    }

    @GetMapping(params = "semester")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesBySemester(
            @RequestParam("semester") Integer semester,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findBySemester(semester, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(courses)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<CourseDto>> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto created = courseService.create(courseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<CourseDto>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseDto courseDto
    ) {
        CourseDto updated = courseService.update(id, courseDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
