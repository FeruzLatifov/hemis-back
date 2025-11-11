package uz.hemis.web.controller;

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
import uz.hemis.service.SpecialtyService;
import uz.hemis.common.dto.SpecialtyDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "Specialties")
@RestController
@RequestMapping("/app/rest/v2/specialties")
@RequiredArgsConstructor
@Slf4j
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getAllSpecialties(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(specialties)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> getSpecialtyById(@PathVariable UUID id) {
        SpecialtyDto specialty = specialtyService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(specialty));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> getSpecialtyByCode(@PathVariable String code) {
        SpecialtyDto specialty = specialtyService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(specialty));
    }

    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(specialties)));
    }

    @GetMapping(params = "faculty")
    public ResponseEntity<ResponseWrapper<List<SpecialtyDto>>> getSpecialtiesByFaculty(
            @RequestParam("faculty") UUID facultyId
    ) {
        List<SpecialtyDto> specialties = specialtyService.findAllByFaculty(facultyId);
        return ResponseEntity.ok(ResponseWrapper.success(specialties));
    }

    @GetMapping(params = "search")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> searchSpecialtiesByName(
            @RequestParam("search") String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(specialties)));
    }

    @GetMapping(params = "active")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getActiveSpecialties(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findActive(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(specialties)));
    }

    @GetMapping(params = "educationType")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByEducationType(
            @RequestParam("educationType") String educationTypeCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findByEducationType(educationTypeCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(specialties)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> createSpecialty(@Valid @RequestBody SpecialtyDto specialtyDto) {
        SpecialtyDto created = specialtyService.create(specialtyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> updateSpecialty(
            @PathVariable UUID id,
            @Valid @RequestBody SpecialtyDto specialtyDto
    ) {
        SpecialtyDto updated = specialtyService.update(id, specialtyDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteSpecialty(@PathVariable UUID id) {
        specialtyService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
