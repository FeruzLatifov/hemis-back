package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.finance.ScholarshipService;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.finance.ScholarshipDto;

import java.util.List;
import java.util.UUID;

/**
 * Scholarship REST Controller - API Layer
 */
@Tag(name = "62.Stipendiya", description = "Stipendiya boshqaruvi")
@RestController
@RequestMapping("/app/rest/v2/scholarships")
@RequiredArgsConstructor
@Slf4j
public class ScholarshipController {

    private final ScholarshipService scholarshipService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<ScholarshipDto>>> getAllScholarships(
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ScholarshipDto> scholarships = scholarshipService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(scholarships)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ScholarshipDto>> getScholarshipById(@PathVariable UUID id) {
        ScholarshipDto scholarship = scholarshipService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(scholarship));
    }

    @GetMapping(params = "student")
    public ResponseEntity<ResponseWrapper<List<ScholarshipDto>>> getScholarshipsByStudent(
            @RequestParam("student") UUID studentId
    ) {
        List<ScholarshipDto> scholarships = scholarshipService.findByStudent(studentId);
        return ResponseEntity.ok(ResponseWrapper.success(scholarships));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<ScholarshipDto>> createScholarship(
            @Valid @RequestBody ScholarshipDto scholarshipDto
    ) {
        ScholarshipDto created = scholarshipService.create(scholarshipDto);
        return ResponseEntity.ok(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<ScholarshipDto>> updateScholarship(
            @PathVariable UUID id,
            @Valid @RequestBody ScholarshipDto scholarshipDto
    ) {
        ScholarshipDto updated = scholarshipService.update(id, scholarshipDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }
}
