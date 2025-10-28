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
import uz.hemis.common.dto.ScheduleDto;
import uz.hemis.common.dto.response.PageResponse;
import uz.hemis.common.dto.response.ResponseWrapper;
import uz.hemis.domain.service.ScheduleService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/app/rest/v2/schedules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule", description = "Schedule management API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "Get all schedules")
    public ResponseEntity<ResponseWrapper<PageResponse<ScheduleDto>>> getAllSchedules(
            @PageableDefault(size = 20, sort = "scheduleDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(scheduleService.findAll(pageable))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get schedule by ID")
    public ResponseEntity<ResponseWrapper<ScheduleDto>> getScheduleById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(scheduleService.findById(id)));
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get schedules by group")
    public ResponseEntity<ResponseWrapper<PageResponse<ScheduleDto>>> getSchedulesByGroup(
            @PathVariable UUID groupId,
            @PageableDefault(size = 20, sort = "scheduleDate") Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(scheduleService.findByGroup(groupId, pageable))));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get schedules by teacher")
    public ResponseEntity<ResponseWrapper<PageResponse<ScheduleDto>>> getSchedulesByTeacher(
            @PathVariable UUID teacherId,
            @PageableDefault(size = 20, sort = "scheduleDate") Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(scheduleService.findByTeacher(teacherId, pageable))));
    }

    @GetMapping("/group/{groupId}/date/{date}")
    @Operation(summary = "Get schedules by group and date")
    public ResponseEntity<ResponseWrapper<List<ScheduleDto>>> getSchedulesByGroupAndDate(
            @PathVariable UUID groupId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(scheduleService.findByGroupAndDate(groupId, date)));
    }

    @GetMapping("/teacher/{teacherId}/date/{date}")
    @Operation(summary = "Get schedules by teacher and date")
    public ResponseEntity<ResponseWrapper<List<ScheduleDto>>> getSchedulesByTeacherAndDate(
            @PathVariable UUID teacherId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(scheduleService.findByTeacherAndDate(teacherId, date)));
    }

    @GetMapping("/group/{groupId}/count")
    @Operation(summary = "Count schedules by group")
    public ResponseEntity<ResponseWrapper<Long>> countSchedulesByGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(ResponseWrapper.success(scheduleService.countByGroup(groupId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Create schedule")
    public ResponseEntity<ResponseWrapper<ScheduleDto>> createSchedule(@Valid @RequestBody ScheduleDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(scheduleService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Update schedule")
    public ResponseEntity<ResponseWrapper<ScheduleDto>> updateSchedule(@PathVariable UUID id, @Valid @RequestBody ScheduleDto dto) {
        return ResponseEntity.ok(ResponseWrapper.success(scheduleService.update(id, dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete schedule")
    public ResponseEntity<ResponseWrapper<Void>> deactivateSchedule(@PathVariable UUID id) {
        scheduleService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Schedule deactivated successfully"));
    }
}
