package uz.hemis.api.legacy.controller;

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
import uz.hemis.service.EmploymentService;
import uz.hemis.common.dto.EmploymentDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "Employment")
@RestController
@RequestMapping("/app/rest/v2/employments")
@RequiredArgsConstructor
@Slf4j
public class EmploymentController {

    private final EmploymentService employmentService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<EmploymentDto>>> getAllEmployments(
            @PageableDefault(size = 20, sort = "employmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EmploymentDto> employments = employmentService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(employments)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<EmploymentDto>> getEmploymentById(@PathVariable UUID id) {
        EmploymentDto employment = employmentService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(employment));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<EmploymentDto>> getEmploymentByCode(@PathVariable String code) {
        EmploymentDto employment = employmentService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(employment));
    }

    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<EmploymentDto>>> getEmploymentsByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "employmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EmploymentDto> employments = employmentService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(employments)));
    }

    @GetMapping(params = "student")
    public ResponseEntity<ResponseWrapper<List<EmploymentDto>>> getEmploymentsByStudent(
            @RequestParam("student") UUID studentId
    ) {
        List<EmploymentDto> employments = employmentService.findByStudent(studentId);
        return ResponseEntity.ok(ResponseWrapper.success(employments));
    }

    @GetMapping(value = "/active", params = "student")
    public ResponseEntity<ResponseWrapper<List<EmploymentDto>>> getActiveEmploymentsByStudent(
            @RequestParam("student") UUID studentId
    ) {
        List<EmploymentDto> employments = employmentService.findActiveByStudent(studentId);
        return ResponseEntity.ok(ResponseWrapper.success(employments));
    }

    @GetMapping(value = "/active", params = "university")
    public ResponseEntity<ResponseWrapper<List<EmploymentDto>>> getActiveEmploymentsByUniversity(
            @RequestParam("university") String universityCode
    ) {
        List<EmploymentDto> employments = employmentService.findActiveByUniversity(universityCode);
        return ResponseEntity.ok(ResponseWrapper.success(employments));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<EmploymentDto>> createEmployment(@Valid @RequestBody EmploymentDto employmentDto) {
        EmploymentDto created = employmentService.create(employmentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<EmploymentDto>> updateEmployment(
            @PathVariable UUID id,
            @Valid @RequestBody EmploymentDto employmentDto
    ) {
        EmploymentDto updated = employmentService.update(id, employmentDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }
}
