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
import uz.hemis.common.dto.CurriculumDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.domain.service.CurriculumService;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Curriculum operations
 *
 * <p><strong>Base URL:</strong> /app/rest/v2/curricula</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/curricula")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Curriculum", description = "Curriculum management API")
public class CurriculumController {

    private final CurriculumService curriculumService;

    @GetMapping
    @Operation(summary = "Get all curricula")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> getAllCurricula(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get curriculum by ID")
    public ResponseEntity<ResponseWrapper<CurriculumDto>> getCurriculumById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(curriculumService.findById(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get curriculum by code")
    public ResponseEntity<ResponseWrapper<CurriculumDto>> getCurriculumByCode(@PathVariable String code) {
        return ResponseEntity.ok(ResponseWrapper.success(curriculumService.findByCode(code)));
    }

    @GetMapping("/university/{code}")
    @Operation(summary = "Get curricula by university")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> getCurriculaByUniversity(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findByUniversity(code, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/university/{code}/all")
    @Operation(summary = "Get all curricula by university")
    public ResponseEntity<ResponseWrapper<List<CurriculumDto>>> getAllCurriculaByUniversity(@PathVariable String code) {
        return ResponseEntity.ok(ResponseWrapper.success(curriculumService.findAllByUniversity(code)));
    }

    @GetMapping("/specialty/{id}")
    @Operation(summary = "Get curricula by specialty")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> getCurriculaBySpecialty(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findBySpecialty(id, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/specialty/{id}/all")
    @Operation(summary = "Get all curricula by specialty")
    public ResponseEntity<ResponseWrapper<List<CurriculumDto>>> getAllCurriculaBySpecialty(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(curriculumService.findAllBySpecialty(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search curricula by name")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> searchCurricula(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active curricula")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> getActiveCurricula(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findActive(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/approved")
    @Operation(summary = "Get approved curricula")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> getApprovedCurricula(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findApprovedCurricula(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/academic-year/{year}")
    @Operation(summary = "Get curricula by academic year")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> getCurriculaByAcademicYear(
            @PathVariable String year,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findByAcademicYear(year, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/education-type/{code}")
    @Operation(summary = "Get curricula by education type")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> getCurriculaByEducationType(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findByEducationType(code, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/university/{code}/academic-year/{year}")
    @Operation(summary = "Get curricula by university and academic year")
    public ResponseEntity<ResponseWrapper<PageResponse<CurriculumDto>>> getCurriculaByUniversityAndYear(
            @PathVariable String code,
            @PathVariable String year,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<CurriculumDto> page = curriculumService.findByUniversityAndAcademicYear(code, year, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(page)));
    }

    @GetMapping("/university/{code}/count")
    @Operation(summary = "Count curricula by university")
    public ResponseEntity<ResponseWrapper<Long>> countCurriculaByUniversity(@PathVariable String code) {
        return ResponseEntity.ok(ResponseWrapper.success(curriculumService.countByUniversity(code)));
    }

    @GetMapping("/specialty/{id}/count")
    @Operation(summary = "Count curricula by specialty")
    public ResponseEntity<ResponseWrapper<Long>> countCurriculaBySpecialty(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseWrapper.success(curriculumService.countBySpecialty(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Create curriculum")
    public ResponseEntity<ResponseWrapper<CurriculumDto>> createCurriculum(@Valid @RequestBody CurriculumDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(curriculumService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Update curriculum")
    public ResponseEntity<ResponseWrapper<CurriculumDto>> updateCurriculum(
            @PathVariable UUID id,
            @Valid @RequestBody CurriculumDto dto
    ) {
        return ResponseEntity.ok(ResponseWrapper.success(curriculumService.update(id, dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete curriculum")
    public ResponseEntity<ResponseWrapper<Void>> deactivateCurriculum(@PathVariable UUID id) {
        curriculumService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Curriculum deactivated successfully"));
    }
}
