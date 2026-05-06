package uz.hemis.api.legacy.controller.academic;

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
import uz.hemis.service.academic.CourseService;
import uz.hemis.common.dto.academic.CourseDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "11.Fanlar", description = "Fanlar boshqaruvi")
@RestController
@RequestMapping("/app/rest/v2/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Barcha fanlar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getAllCourses(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(courses)));
    }

    @Operation(summary = "Fanni ID bo'yicha topish")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<CourseDto>> getCourseById(@PathVariable UUID id) {
        CourseDto course = courseService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(course));
    }

    @Operation(summary = "Fanni kod bo'yicha topish")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<CourseDto>> getCourseByCode(@PathVariable String code) {
        CourseDto course = courseService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(course));
    }

    @Operation(summary = "OTM bo'yicha fanlar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(courses)));
    }

    @Operation(summary = "OTM bo'yicha BARCHA fanlar (paginatsiyasiz)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "universityAll")
    public ResponseEntity<ResponseWrapper<List<CourseDto>>> getAllCoursesByUniversity(
            @RequestParam("universityAll") String universityCode
    ) {
        List<CourseDto> courses = courseService.findAllByUniversity(universityCode);
        return ResponseEntity.ok(ResponseWrapper.success(courses));
    }

    @Operation(summary = "Subject bo'yicha fanlar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "subject")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesBySubject(
            @RequestParam("subject") UUID subjectId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findBySubject(subjectId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(courses)));
    }

    @Operation(summary = "Fan nomi bo'yicha qidirish (partial match)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "search")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> searchCoursesByName(
            @RequestParam("search") String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(courses)));
    }

    @Operation(summary = "Aktiv fanlar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "active")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getActiveCourses(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findActive(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(courses)));
    }

    @Operation(summary = "Semestr bo'yicha fanlar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "semester")
    public ResponseEntity<ResponseWrapper<PageResponse<CourseDto>>> getCoursesBySemester(
            @RequestParam("semester") Integer semester,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CourseDto> courses = courseService.findBySemester(semester, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(courses)));
    }

    @Operation(summary = "Yangi fan yaratish", description = "ADMIN yoki OTM_API roli kerak.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseWrapper<CourseDto>> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto created = courseService.create(courseDto);
        return ResponseEntity.ok(ResponseWrapper.success(created));
    }

    @Operation(summary = "Fanni yangilash", description = "ADMIN yoki OTM_API roli kerak.")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseWrapper<CourseDto>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseDto courseDto
    ) {
        CourseDto updated = courseService.update(id, courseDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @Operation(summary = "Fanni soft delete", description = "ADMIN roli kerak. delete_ts qo'yiladi, ma'lumot saqlanadi.")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseWrapper<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
