package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.EmployeeCertificate;
import uz.hemis.domain.repository.EmployeeCertificateRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Employee Certificate Entity Controller for OLD-HEMIS compatibility.
 * <p>
 * Note: The endpoint path contains intentional typo "EEmpoyeeCertificate" to match OLD-HEMIS exactly.
 * </p>
 */
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EEmpoyeeCertificate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "68.Sertifikat", description = "Xodim sertifikatlari")
public class EmployeeCertificateEntityController {

    private final EmployeeCertificateRepository employeeCertificateRepository;

    @GetMapping("/{id}")
    @Operation(summary = "Xodim sertifikatini ID bo'yicha olish")
    public ResponseEntity<Map<String, Object>> getEmployeeCertificate(
            @Parameter(description = "Employee certificate ID") @PathVariable UUID id) {
        Optional<EmployeeCertificate> opt = employeeCertificateRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(opt.get()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Xodim sertifikatini yangilash")
    public ResponseEntity<Map<String, Object>> updateEmployeeCertificate(
            @Parameter(description = "Employee certificate ID") @PathVariable UUID id,
            @RequestBody Map<String, Object> data) {
        Optional<EmployeeCertificate> opt = employeeCertificateRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmployeeCertificate cert = opt.get();
        updateFromMap(cert, data);
        cert.setUpdateTs(LocalDateTime.now());
        employeeCertificateRepository.save(cert);

        return ResponseEntity.ok(toMap(cert));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xodim sertifikatini o'chirish")
    public ResponseEntity<Void> deleteEmployeeCertificate(
            @Parameter(description = "Employee certificate ID") @PathVariable UUID id) {
        Optional<EmployeeCertificate> opt = employeeCertificateRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmployeeCertificate cert = opt.get();
        cert.setDeleteTs(LocalDateTime.now());
        employeeCertificateRepository.save(cert);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Xodim sertifikatlari ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAllEmployeeCertificates(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") int offset) {

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<EmployeeCertificate> page = employeeCertificateRepository.findAll(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @Operation(summary = "Xodim sertifikatlarini qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchEmployeeCertificates(
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
        Page<EmployeeCertificate> page = employeeCertificateRepository.findAll(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Xodim sertifikatini yaratish/upsert")
    public ResponseEntity<Map<String, Object>> createEmployeeCertificate(
            @RequestBody Map<String, Object> data) {

        // CUBA UPSERT: if body contains 'id' and entity exists, update instead of create
        if (data.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(data.get("id").toString());
                var existingOpt = employeeCertificateRepository.findById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    EmployeeCertificate cert = existingOpt.get();
                    updateFromMap(cert, data);
                    cert.setUpdateTs(LocalDateTime.now());
                    employeeCertificateRepository.save(cert);
                    return ResponseEntity.ok(toMap(cert));
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID format for id: {}", data.get("id"));
            }
        }

        EmployeeCertificate cert = new EmployeeCertificate();
        cert.setId(UUID.randomUUID());
        cert.setCreateTs(LocalDateTime.now());
        cert.setUpdateTs(LocalDateTime.now());

        updateFromMap(cert, data);
        employeeCertificateRepository.save(cert);

        return ResponseEntity.ok(toMap(cert));
    }

    private Map<String, Object> toMap(EmployeeCertificate cert) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", cert.getId());
        map.put("_entityName", "hemishe_EEmpoyeeCertificate");
        map.put("_instanceName", cert.getSerialNumber());
        map.put("university", cert.getUniversity());
        map.put("employee", cert.getEmployee());
        map.put("certificateType", cert.getCertificateType());
        map.put("certificateName", cert.getCertificateName());
        map.put("certificateGrade", cert.getCertificateGrade());
        map.put("certificateSubject", cert.getCertificateSubject());
        map.put("issueDate", cert.getIssueDate());
        map.put("validDate", cert.getValidDate());
        map.put("serialNumber", cert.getSerialNumber());
        map.put("active", cert.getActive());
        map.put("createTs", cert.getCreateTs());
        map.put("updateTs", cert.getUpdateTs());
        map.put("deleteTs", cert.getDeleteTs());
        return map;
    }

    private String toStr(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Object code = map.get("code");
            if (code != null) return code.toString();
            Object id = map.get("id");
            if (id != null) return id.toString();
            return value.toString();
        }
        return value.toString();
    }

    private void updateFromMap(EmployeeCertificate cert, Map<String, Object> data) {
        if (data.containsKey("university")) {
            cert.setUniversity(toStr(data.get("university")));
        }
        if (data.containsKey("employee")) {
            Object empObj = data.get("employee");
            cert.setEmployee(empObj != null ? UUID.fromString(toStr(empObj)) : null);
        }
        if (data.containsKey("certificateType")) {
            cert.setCertificateType(toStr(data.get("certificateType")));
        }
        if (data.containsKey("certificateName")) {
            cert.setCertificateName(toStr(data.get("certificateName")));
        }
        if (data.containsKey("certificateGrade")) {
            cert.setCertificateGrade(toStr(data.get("certificateGrade")));
        }
        if (data.containsKey("certificateSubject")) {
            cert.setCertificateSubject(toStr(data.get("certificateSubject")));
        }
        if (data.containsKey("issueDate")) {
            Object issueDateObj = data.get("issueDate");
            cert.setIssueDate(issueDateObj != null ? LocalDate.parse(issueDateObj.toString()) : null);
        }
        if (data.containsKey("validDate")) {
            Object validDateObj = data.get("validDate");
            cert.setValidDate(validDateObj != null ? LocalDate.parse(validDateObj.toString()) : null);
        }
        if (data.containsKey("serialNumber")) {
            cert.setSerialNumber(toStr(data.get("serialNumber")));
        }
        if (data.containsKey("active")) {
            cert.setActive((Boolean) data.get("active"));
        }
    }
}
