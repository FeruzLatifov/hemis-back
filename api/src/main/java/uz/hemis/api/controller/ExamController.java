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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ExamDto;
import uz.hemis.common.dto.response.PageResponse;
import uz.hemis.common.dto.response.ResponseWrapper;
import uz.hemis.domain.service.ExamService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/app/rest/v2/exams")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exam", description = "Exam management API")
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<ExamDto>>> getAllExams(
            @PageableDefault(size = 20, sort = "examDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(examService.findAll(pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ExamDto>> getExamById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(examService.findById(id)));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseWrapper<PageResponse<ExamDto>>> getExamsByCourse(
            @PathVariable UUID courseId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(examService.findByCourse(courseId, pageable))));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ResponseWrapper<PageResponse<ExamDto>>> getExamsByGroup(
            @PathVariable UUID groupId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(examService.findByGroup(groupId, pageable))));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ResponseWrapper<PageResponse<ExamDto>>> getExamsByTeacher(
            @PathVariable UUID teacherId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(examService.findByTeacher(teacherId, pageable))));
    }

    @GetMapping("/published")
    public ResponseEntity<ResponseWrapper<PageResponse<ExamDto>>> getPublishedExams(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(examService.findPublishedExams(pageable))));
    }

    @GetMapping("/group/{groupId}/date/{date}")
    public ResponseEntity<ResponseWrapper<List<ExamDto>>> getExamsByGroupAndDate(
            @PathVariable UUID groupId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(examService.findByGroupAndDate(groupId, date)));
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<ResponseWrapper<Long>> countExamsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ResponseWrapper.success(examService.countByCourse(courseId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ResponseWrapper<ExamDto>> createExam(@Valid @RequestBody ExamDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(examService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ResponseWrapper<ExamDto>> updateExam(@PathVariable UUID id, @Valid @RequestBody ExamDto dto) {
        return ResponseEntity.ok(ResponseWrapper.success(examService.update(id, dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deactivateExam(@PathVariable UUID id) {
        examService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Exam deactivated successfully"));
    }
}
