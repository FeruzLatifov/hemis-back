package uz.hemis.api.legacy.controller.university;

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
import uz.hemis.service.university.SpecialtyService;
import uz.hemis.common.dto.university.SpecialtyDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "50.Mutaxassisliklar", description = "Mutaxassisliklar boshqaruvi")
@RestController
@RequestMapping("/app/rest/v2/specialties")
@RequiredArgsConstructor
@Slf4j
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @Operation(summary = "Barcha mutaxassisliklar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getAllSpecialties(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(specialties)));
    }

    @Operation(summary = "Mutaxassislikni ID bo'yicha topish")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> getSpecialtyById(@PathVariable UUID id) {
        SpecialtyDto specialty = specialtyService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(specialty));
    }

    @Operation(summary = "Mutaxassislikni unique code bo'yicha topish")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> getSpecialtyByCode(@PathVariable String code) {
        SpecialtyDto specialty = specialtyService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(specialty));
    }

    @Operation(summary = "OTM bo'yicha mutaxassisliklar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(specialties)));
    }

    @Operation(summary = "Fakultet bo'yicha mutaxassisliklar (paginatsiyasiz)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "faculty")
    public ResponseEntity<ResponseWrapper<List<SpecialtyDto>>> getSpecialtiesByFaculty(
            @RequestParam("faculty") String facultyCode
    ) {
        List<SpecialtyDto> specialties = specialtyService.findAllByFaculty(facultyCode);
        return ResponseEntity.ok(ResponseWrapper.success(specialties));
    }

    @Operation(summary = "Mutaxassislik nomi bo'yicha qidirish (partial match)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "search")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> searchSpecialtiesByName(
            @RequestParam("search") String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(specialties)));
    }

    @Operation(summary = "Aktiv mutaxassisliklar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "active")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getActiveSpecialties(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findActive(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(specialties)));
    }

    @Operation(summary = "Ta'lim turi bo'yicha mutaxassisliklar", description = "Bakalavr, Magistratura, Doktorlik (PhD/DSc).")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "educationType")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByEducationType(
            @RequestParam("educationType") String educationTypeCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SpecialtyDto> specialties = specialtyService.findByEducationType(educationTypeCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(specialties)));
    }

    @Operation(summary = "Yangi mutaxassislik yaratish")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OTM_API')")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> createSpecialty(@Valid @RequestBody SpecialtyDto specialtyDto) {
        SpecialtyDto created = specialtyService.create(specialtyDto);
        return ResponseEntity.ok(ResponseWrapper.success(created));
    }

    @Operation(summary = "Mutaxassislikni yangilash")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OTM_API')")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> updateSpecialty(
            @PathVariable UUID id,
            @Valid @RequestBody SpecialtyDto specialtyDto
    ) {
        SpecialtyDto updated = specialtyService.update(id, specialtyDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @Operation(summary = "Mutaxassislikni soft delete", description = "delete_ts qo'yiladi.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteSpecialty(@PathVariable UUID id) {
        specialtyService.delete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
