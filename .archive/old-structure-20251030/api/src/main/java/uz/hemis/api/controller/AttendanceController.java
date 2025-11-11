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
import uz.hemis.common.dto.AttendanceDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.domain.service.AttendanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/app/rest/v2/attendances")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance", description = "Attendance management API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<AttendanceDto>>> getAllAttendances(
            @PageableDefault(size = 20, sort = "attendanceDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(attendanceService.findAll(pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AttendanceDto>> getAttendanceById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(attendanceService.findById(id)));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseWrapper<PageResponse<AttendanceDto>>> getAttendancesByStudent(
            @PathVariable UUID studentId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(attendanceService.findByStudent(studentId, pageable))));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseWrapper<PageResponse<AttendanceDto>>> getAttendancesByCourse(
            @PathVariable UUID courseId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(attendanceService.findByCourse(courseId, pageable))));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ResponseWrapper<PageResponse<AttendanceDto>>> getAttendancesByGroup(
            @PathVariable UUID groupId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(attendanceService.findByGroup(groupId, pageable))));
    }

    @GetMapping("/group/{groupId}/date/{date}")
    public ResponseEntity<ResponseWrapper<List<AttendanceDto>>> getAttendancesByGroupAndDate(
            @PathVariable UUID groupId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(attendanceService.findByGroupAndDate(groupId, date)));
    }

    @GetMapping("/student/{studentId}/present-count")
    public ResponseEntity<ResponseWrapper<Long>> countPresentByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ResponseWrapper.success(attendanceService.countPresentByStudent(studentId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ResponseWrapper<AttendanceDto>> createAttendance(@Valid @RequestBody AttendanceDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(attendanceService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ResponseWrapper<AttendanceDto>> updateAttendance(@PathVariable UUID id, @Valid @RequestBody AttendanceDto dto) {
        return ResponseEntity.ok(ResponseWrapper.success(attendanceService.update(id, dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deactivateAttendance(@PathVariable UUID id) {
        attendanceService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Attendance deactivated successfully"));
    }
}
