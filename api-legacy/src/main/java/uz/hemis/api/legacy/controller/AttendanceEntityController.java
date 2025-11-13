package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.adapter.LegacyEntityAdapter;
import uz.hemis.common.dto.AttendanceDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.service.AttendanceService;

import java.util.*;

@Tag(name = "Attendance")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RAcademicAttendance")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AttendanceEntityController {

    private final AttendanceService attendanceService;
    private final LegacyEntityAdapter adapter;
    
    private static final String ENTITY_NAME = "hemishe_RAcademicAttendance";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get attendance by ID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId, 
            @RequestParam(required = false) Boolean returnNulls) {
        
        try {
            AttendanceDto dto = attendanceService.findById(entityId);
            return ResponseEntity.ok(adapter.toMap(dto, ENTITY_NAME, returnNulls));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update attendance")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId, 
            @RequestBody Map<String, Object> body, 
            @RequestParam(required = false) Boolean returnNulls) {
        
        try {
            AttendanceDto dto = adapter.fromMap(body, AttendanceDto.class);
            AttendanceDto updated = attendanceService.update(entityId, dto);
            return ResponseEntity.ok(adapter.toMap(updated, ENTITY_NAME, returnNulls));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete attendance (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        try {
            attendanceService.softDelete(entityId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search attendance (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter, 
            @RequestParam(required = false) Boolean returnNulls) {
        
        List<AttendanceDto> dtos = attendanceService.findAll(Pageable.unpaged()).getContent();
        return ResponseEntity.ok(adapter.toMapList(dtos, ENTITY_NAME, returnNulls));
    }

    @PostMapping("/search")
    @Operation(summary = "Search attendance (POST)")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter, 
            @RequestParam(required = false) Boolean returnNulls) {
        
        List<AttendanceDto> dtos = attendanceService.findAll(Pageable.unpaged()).getContent();
        return ResponseEntity.ok(adapter.toMapList(dtos, ENTITY_NAME, returnNulls));
    }

    @GetMapping
    @Operation(summary = "Get all attendance")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") Integer offset, 
            @RequestParam(defaultValue = "50") Integer limit, 
            @RequestParam(required = false) String sort, 
            @RequestParam(required = false) Boolean returnNulls) {
        
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            sorting = Sort.by(
                parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC, 
                parts[0]
            );
        }
        
        PageRequest pageRequest = PageRequest.of(offset / limit, limit, sorting);
        Page<AttendanceDto> page = attendanceService.findAll(pageRequest);
        
        return ResponseEntity.ok(adapter.toMapList(page.getContent(), ENTITY_NAME, returnNulls));
    }

    @PostMapping
    @Operation(summary = "Create attendance")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body, 
            @RequestParam(required = false) Boolean returnNulls) {
        
        AttendanceDto dto = adapter.fromMap(body, AttendanceDto.class);
        AttendanceDto created = attendanceService.create(dto);
        
        return ResponseEntity.ok(adapter.toMap(created, ENTITY_NAME, returnNulls));
    }
}
