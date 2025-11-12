package uz.hemis.api.web.controller;

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
import uz.hemis.service.DoctoralStudentService;
import uz.hemis.common.dto.DoctoralStudentDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "Doctoral Students")
@RestController
@RequestMapping("/app/rest/v2/doctoral-students")
@RequiredArgsConstructor
@Slf4j
public class DoctoralStudentController {

    private final DoctoralStudentService doctoralStudentService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<DoctoralStudentDto>>> getAllDoctoralStudents(
            @PageableDefault(size = 20, sort = "admissionDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(doctoralStudents)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<DoctoralStudentDto>> getDoctoralStudentById(@PathVariable UUID id) {
        DoctoralStudentDto doctoralStudent = doctoralStudentService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudent));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<DoctoralStudentDto>> getDoctoralStudentByCode(@PathVariable String code) {
        DoctoralStudentDto doctoralStudent = doctoralStudentService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudent));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseWrapper<DoctoralStudentDto>> getDoctoralStudentByStudent(@PathVariable UUID studentId) {
        DoctoralStudentDto doctoralStudent = doctoralStudentService.findByStudent(studentId);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudent));
    }

    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<DoctoralStudentDto>>> getDoctoralStudentsByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "admissionDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(doctoralStudents)));
    }

    @GetMapping(value = "/active", params = "university")
    public ResponseEntity<ResponseWrapper<List<DoctoralStudentDto>>> getActiveDoctoralStudentsByUniversity(
            @RequestParam("university") String universityCode
    ) {
        List<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findActiveByUniversity(universityCode);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudents));
    }

    @GetMapping(params = "advisor")
    public ResponseEntity<ResponseWrapper<List<DoctoralStudentDto>>> getDoctoralStudentsByAdvisor(
            @RequestParam("advisor") UUID advisorId
    ) {
        List<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findByScientificAdvisor(advisorId);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudents));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<DoctoralStudentDto>> createDoctoralStudent(
            @Valid @RequestBody DoctoralStudentDto doctoralStudentDto
    ) {
        DoctoralStudentDto created = doctoralStudentService.create(doctoralStudentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<DoctoralStudentDto>> updateDoctoralStudent(
            @PathVariable UUID id,
            @Valid @RequestBody DoctoralStudentDto doctoralStudentDto
    ) {
        DoctoralStudentDto updated = doctoralStudentService.update(id, doctoralStudentDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }
}
