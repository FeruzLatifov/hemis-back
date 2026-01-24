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
import uz.hemis.service.DoctoralStudentService;
import uz.hemis.common.dto.DoctoralStudentDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

/**
 * Controller for DoctoralStudent API
 *
 * This is a modern API controller (not legacy CUBA format).
 * For legacy CUBA format, see DoctoralStudentEntityController.
 */
@Tag(name = "04.Talaba", description = "Doktorant talabalar API (zamonaviy format)")
@RestController
@RequestMapping("/app/rest/v2/doctoral-students")
@RequiredArgsConstructor
@Slf4j
public class DoctoralStudentController {

    private final DoctoralStudentService doctoralStudentService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<DoctoralStudentDto>>> getAllDoctoralStudents(
            @PageableDefault(size = 20, sort = "acceptedDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(doctoralStudents)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<DoctoralStudentDto>> getDoctoralStudentById(@PathVariable UUID id) {
        DoctoralStudentDto doctoralStudent = doctoralStudentService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudent));
    }

    @GetMapping("/student-id/{studentIdNumber}")
    public ResponseEntity<ResponseWrapper<DoctoralStudentDto>> getDoctoralStudentByStudentIdNumber(
            @PathVariable String studentIdNumber
    ) {
        DoctoralStudentDto doctoralStudent = doctoralStudentService.findByStudentIdNumber(studentIdNumber);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudent));
    }

    @GetMapping("/passport-pin/{passportPin}")
    public ResponseEntity<ResponseWrapper<DoctoralStudentDto>> getDoctoralStudentByPassportPin(
            @PathVariable String passportPin
    ) {
        DoctoralStudentDto doctoralStudent = doctoralStudentService.findByPassportPin(passportPin);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudent));
    }

    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<DoctoralStudentDto>>> getDoctoralStudentsByUniversity(
            @RequestParam("university") String university,
            @PageableDefault(size = 20, sort = "acceptedDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findByUniversity(university, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.of(doctoralStudents)));
    }

    @GetMapping(value = "/active", params = "university")
    public ResponseEntity<ResponseWrapper<List<DoctoralStudentDto>>> getActiveDoctoralStudentsByUniversity(
            @RequestParam("university") String university
    ) {
        List<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findActiveByUniversity(university);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudents));
    }

    @GetMapping(params = "doctoralStudentType")
    public ResponseEntity<ResponseWrapper<List<DoctoralStudentDto>>> getDoctoralStudentsByType(
            @RequestParam("doctoralStudentType") String doctoralStudentType
    ) {
        List<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findByDoctoralStudentType(doctoralStudentType);
        return ResponseEntity.ok(ResponseWrapper.success(doctoralStudents));
    }

    @GetMapping(params = "doctorateStudentStatus")
    public ResponseEntity<ResponseWrapper<List<DoctoralStudentDto>>> getDoctoralStudentsByStatus(
            @RequestParam("doctorateStudentStatus") String doctorateStudentStatus
    ) {
        List<DoctoralStudentDto> doctoralStudents = doctoralStudentService.findByDoctorateStudentStatus(doctorateStudentStatus);
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
