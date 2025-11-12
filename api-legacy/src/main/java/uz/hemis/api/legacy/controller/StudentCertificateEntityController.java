package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.StudentCertificate;
import uz.hemis.domain.repository.StudentCertificateRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentCertificate")
@RequiredArgsConstructor
@Tag(name = "Certificates")
public class StudentCertificateEntityController {

    private final StudentCertificateRepository studentCertificateRepository;

    @GetMapping("/{id}")
    @Operation(summary = "Get student certificate by ID")
    public ResponseEntity<Map<String, Object>> getStudentCertificate(
            @Parameter(description = "Student certificate ID") @PathVariable UUID id) {
        Optional<StudentCertificate> studentCertificateOpt = studentCertificateRepository.findById(id);
        if (studentCertificateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(studentCertificateOpt.get()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student certificate")
    public ResponseEntity<Map<String, Object>> updateStudentCertificate(
            @Parameter(description = "Student certificate ID") @PathVariable UUID id,
            @RequestBody Map<String, Object> studentCertificateData) {
        Optional<StudentCertificate> studentCertificateOpt = studentCertificateRepository.findById(id);
        if (studentCertificateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StudentCertificate studentCertificate = studentCertificateOpt.get();
        updateStudentCertificateFromMap(studentCertificate, studentCertificateData);
        studentCertificate.setUpdateTs(LocalDateTime.now());
        studentCertificateRepository.save(studentCertificate);

        return ResponseEntity.ok(toMap(studentCertificate));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student certificate")
    public ResponseEntity<Void> deleteStudentCertificate(
            @Parameter(description = "Student certificate ID") @PathVariable UUID id) {
        Optional<StudentCertificate> studentCertificateOpt = studentCertificateRepository.findById(id);
        if (studentCertificateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StudentCertificate studentCertificate = studentCertificateOpt.get();
        studentCertificate.setDeleteTs(LocalDateTime.now());
        studentCertificateRepository.save(studentCertificate);

        return ResponseEntity.noContent().build();
    }

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

        Pageable pageable = PageRequest.of(offset / limit, limit, sorting);
        Page<StudentCertificate> page = studentCertificateRepository.findAll(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    @Operation(summary = "Search student certificates with filter")
    public ResponseEntity<Map<String, Object>> searchStudentCertificatesPost(
            @RequestBody Map<String, Object> searchParams) {

        int limit = (int) searchParams.getOrDefault("limit", 50);
        int offset = (int) searchParams.getOrDefault("offset", 0);
        String sortParam = (String) searchParams.get("sort");

        Sort sorting = Sort.unsorted();
        if (sortParam != null && !sortParam.isEmpty()) {
            String[] sortParts = sortParam.split(",");
            if (sortParts.length == 2) {
                sorting = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
            }
        }

        Pageable pageable = PageRequest.of(offset / limit, limit, sorting);
        Page<StudentCertificate> page = studentCertificateRepository.findAll(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("total", page.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all student certificates")
    public ResponseEntity<List<Map<String, Object>>> getAllStudentCertificates(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") int offset) {

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<StudentCertificate> page = studentCertificateRepository.findAll(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create student certificate")
    public ResponseEntity<Map<String, Object>> createStudentCertificate(
            @RequestBody Map<String, Object> studentCertificateData) {

        StudentCertificate studentCertificate = new StudentCertificate();
        studentCertificate.setId(UUID.randomUUID());
        studentCertificate.setCreateTs(LocalDateTime.now());
        studentCertificate.setUpdateTs(LocalDateTime.now());

        updateStudentCertificateFromMap(studentCertificate, studentCertificateData);
        studentCertificateRepository.save(studentCertificate);

        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(studentCertificate));
    }

    private Map<String, Object> toMap(StudentCertificate studentCertificate) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", studentCertificate.getId());
        map.put("_entityName", "hemishe_EStudentCertificate");
        map.put("_instanceName", studentCertificate.getSerialNumber());
        map.put("university", studentCertificate.getUniversity());
        map.put("student", studentCertificate.getStudent());
        map.put("certificateType", studentCertificate.getCertificateType());
        map.put("certificateName", studentCertificate.getCertificateName());
        map.put("certificateGrade", studentCertificate.getCertificateGrade());
        map.put("certificateSubject", studentCertificate.getCertificateSubject());
        map.put("issueDate", studentCertificate.getIssueDate());
        map.put("validDate", studentCertificate.getValidDate());
        map.put("serialNumber", studentCertificate.getSerialNumber());
        map.put("active", studentCertificate.getActive());
        map.put("createTs", studentCertificate.getCreateTs());
        map.put("updateTs", studentCertificate.getUpdateTs());
        map.put("deleteTs", studentCertificate.getDeleteTs());
        return map;
    }

    private void updateStudentCertificateFromMap(StudentCertificate studentCertificate, Map<String, Object> data) {
        if (data.containsKey("university")) {
            Object universityObj = data.get("university");
            studentCertificate.setUniversity(universityObj != null ? UUID.fromString(universityObj.toString()) : null);
        }
        if (data.containsKey("student")) {
            Object studentObj = data.get("student");
            studentCertificate.setStudent(studentObj != null ? UUID.fromString(studentObj.toString()) : null);
        }
        if (data.containsKey("certificateType")) {
            Object certificateTypeObj = data.get("certificateType");
            studentCertificate.setCertificateType(certificateTypeObj != null ? UUID.fromString(certificateTypeObj.toString()) : null);
        }
        if (data.containsKey("certificateName")) {
            Object certificateNameObj = data.get("certificateName");
            studentCertificate.setCertificateName(certificateNameObj != null ? UUID.fromString(certificateNameObj.toString()) : null);
        }
        if (data.containsKey("certificateGrade")) {
            Object certificateGradeObj = data.get("certificateGrade");
            studentCertificate.setCertificateGrade(certificateGradeObj != null ? UUID.fromString(certificateGradeObj.toString()) : null);
        }
        if (data.containsKey("certificateSubject")) {
            Object certificateSubjectObj = data.get("certificateSubject");
            studentCertificate.setCertificateSubject(certificateSubjectObj != null ? UUID.fromString(certificateSubjectObj.toString()) : null);
        }
        if (data.containsKey("issueDate")) {
            Object issueDateObj = data.get("issueDate");
            studentCertificate.setIssueDate(issueDateObj != null ? LocalDate.parse(issueDateObj.toString()) : null);
        }
        if (data.containsKey("validDate")) {
            Object validDateObj = data.get("validDate");
            studentCertificate.setValidDate(validDateObj != null ? LocalDate.parse(validDateObj.toString()) : null);
        }
        if (data.containsKey("serialNumber")) {
            studentCertificate.setSerialNumber((String) data.get("serialNumber"));
        }
        if (data.containsKey("active")) {
            studentCertificate.setActive((Boolean) data.get("active"));
        }
    }
}
