package uz.hemis.api.legacy.controller.student;

import uz.hemis.api.legacy.adapter.LegacyResponseHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uz.hemis.domain.entity.student.StudentCertificate;
import uz.hemis.service.legacy.student.StudentEntityLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentCertificate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "68.Sertifikat", description = "Talaba sertifikatlari")
@SecurityRequirement(name = "bearerAuth")
public class StudentCertificateEntityController {

    private final StudentEntityLegacyService studentService;

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/{id}")
    @Operation(summary = "Get student certificate by ID")
    public ResponseEntity<Map<String, Object>> getStudentCertificate(
            @Parameter(description = "Student certificate ID") @PathVariable UUID id,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        Optional<StudentCertificate> studentCertificateOpt = studentService.findStudentCertificateById(id);
        if (studentCertificateOpt.isEmpty()) {
            return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity hemishe_EStudentCertificate with id " + id + " not found"));
        }
        return ResponseEntity.ok(studentService.toStudentCertificateMap(studentCertificateOpt.get(), returnNulls, view));
    }

    @PreAuthorize("hasAuthority('students.edit')")
    @PutMapping("/{id}")
    @Operation(summary = "Update student certificate")
    public ResponseEntity<Map<String, Object>> updateStudentCertificate(
            @Parameter(description = "Student certificate ID") @PathVariable UUID id,
            @RequestBody Map<String, Object> studentCertificateData) {
        Optional<StudentCertificate> studentCertificateOpt = studentService.findStudentCertificateById(id);
        if (studentCertificateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StudentCertificate studentCertificate = studentCertificateOpt.get();
        studentService.updateStudentCertificateFromMap(studentCertificate, studentCertificateData);
        studentCertificate.setUpdateTs(LocalDateTime.now());
        StudentCertificate saved = studentService.saveStudentCertificate(studentCertificate);

        return ResponseEntity.ok(studentService.toStudentCertificateMap(saved, false));
    }

    @PreAuthorize("hasAuthority('students.delete')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student certificate")
    public ResponseEntity<Void> deleteStudentCertificate(
            @Parameter(description = "Student certificate ID") @PathVariable UUID id) {
        Optional<StudentCertificate> studentCertificateOpt = studentService.findStudentCertificateById(id);
        if (studentCertificateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        studentService.deleteStudentCertificate(studentCertificateOpt.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/search")
    @Operation(summary = "Search student certificates")
    public ResponseEntity<List<Map<String, Object>>> searchStudentCertificates(
            @Parameter(description = "Filter") @RequestParam(required = false) String filter,
            @Parameter(description = "View") @RequestParam(required = false) String view,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort) {

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                sorting = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
            }
        }

        Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1), sorting);
        Page<StudentCertificate> page = studentService.findAllStudentCertificate(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(e -> studentService.toStudentCertificateMap(e, false, view))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('students.view')")
    @PostMapping("/search")
    @Operation(summary = "Search student certificates with filter")
    public ResponseEntity<Map<String, Object>> searchStudentCertificatesPost(
            @RequestBody Map<String, Object> searchParams) {

        int limit = (int) searchParams.getOrDefault("limit", 50);
        int offset = (int) searchParams.getOrDefault("offset", 0);
        String sortParam = (String) searchParams.get("sort");
        String viewParam = (String) searchParams.get("view");

        Sort sorting = Sort.unsorted();
        if (sortParam != null && !sortParam.isEmpty()) {
            String[] sortParts = sortParam.split(",");
            if (sortParts.length == 2) {
                sorting = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
            }
        }

        Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1), sorting);
        Page<StudentCertificate> page = studentService.findAllStudentCertificate(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(e -> studentService.toStudentCertificateMap(e, false, viewParam))
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", result);
        response.put("total", page.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping
    @Operation(summary = "Get all student certificates")
    public ResponseEntity<List<Map<String, Object>>> getAllStudentCertificates(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "View") @RequestParam(required = false) String view) {

        Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1));
        Page<StudentCertificate> page = studentService.findAllStudentCertificate(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(e -> studentService.toStudentCertificateMap(e, false, view))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('students.edit')")
    @PostMapping
    @Operation(summary = "Create/upsert student certificate")
    public ResponseEntity<Map<String, Object>> createStudentCertificate(
            @RequestBody Map<String, Object> studentCertificateData) {

        // CUBA UPSERT: if body contains 'id' and entity exists, update instead of create
        if (studentCertificateData.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(studentCertificateData.get("id").toString());
                var existingOpt = studentService.findStudentCertificateById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    StudentCertificate cert = existingOpt.get();
                    studentService.updateStudentCertificateFromMap(cert, studentCertificateData);
                    cert.setUpdateTs(LocalDateTime.now());
                    StudentCertificate saved = studentService.saveStudentCertificate(cert);
                    return ResponseEntity.ok(studentService.toStudentCertificateMap(saved, false));
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID format for id: {}", studentCertificateData.get("id"));
            }
        }

        StudentCertificate studentCertificate = new StudentCertificate();
        studentCertificate.setId(UUID.randomUUID());
        studentCertificate.setCreateTs(LocalDateTime.now());
        studentCertificate.setUpdateTs(LocalDateTime.now());

        studentService.updateStudentCertificateFromMap(studentCertificate, studentCertificateData);
        StudentCertificate saved = studentService.saveStudentCertificate(studentCertificate);

        return ResponseEntity.ok(studentService.toStudentCertificateMap(saved, false));
    }
}
