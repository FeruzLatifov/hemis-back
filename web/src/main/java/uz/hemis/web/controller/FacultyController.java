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
import uz.hemis.service.FacultyService;
import uz.hemis.common.dto.FacultyDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "Faculty")
@RestController
@RequestMapping("/app/rest/v2/faculties")
@RequiredArgsConstructor
@Slf4j
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> getAllFaculties(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<FacultyDto> faculties = facultyService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(faculties)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<FacultyDto>> getFacultyById(@PathVariable UUID id) {
        FacultyDto faculty = facultyService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(faculty));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<FacultyDto>> getFacultyByCode(@PathVariable String code) {
        FacultyDto faculty = facultyService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(faculty));
    }

    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> getFacultiesByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<FacultyDto> faculties = facultyService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(faculties)));
    }

    @GetMapping(params = "universityAll")
    public ResponseEntity<ResponseWrapper<List<FacultyDto>>> getAllFacultiesByUniversity(
            @RequestParam("universityAll") String universityCode
    ) {
        List<FacultyDto> faculties = facultyService.findAllByUniversity(universityCode);
        return ResponseEntity.ok(ResponseWrapper.success(faculties));
    }

    @GetMapping(params = "search")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> searchFacultiesByName(
            @RequestParam("search") String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<FacultyDto> faculties = facultyService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(faculties)));
    }

    @GetMapping(params = "active")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> getActiveFaculties(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<FacultyDto> faculties = facultyService.findActive(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(faculties)));
    }

    @GetMapping(params = "type")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> getFacultiesByType(
            @RequestParam("type") String typeCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<FacultyDto> faculties = facultyService.findByType(typeCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(faculties)));
    }

    @GetMapping(params = "countByUniversity")
    public ResponseEntity<ResponseWrapper<Long>> countFacultiesByUniversity(
            @RequestParam("countByUniversity") String universityCode
    ) {
        long count = facultyService.countByUniversity(universityCode);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<FacultyDto>> createFaculty(@Valid @RequestBody FacultyDto facultyDto) {
        FacultyDto created = facultyService.create(facultyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<FacultyDto>> updateFaculty(
            @PathVariable UUID id,
            @Valid @RequestBody FacultyDto facultyDto
    ) {
        FacultyDto updated = facultyService.update(id, facultyDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteFaculty(@PathVariable UUID id) {
        facultyService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
