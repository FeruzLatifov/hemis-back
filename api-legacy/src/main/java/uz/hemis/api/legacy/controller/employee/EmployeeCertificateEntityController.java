package uz.hemis.api.legacy.controller.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.EmployeeCertificate;
import uz.hemis.service.legacy.employee.EmployeeRefLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

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

    private final EmployeeRefLegacyService employeeRefService;

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/{id}")
    @Operation(summary = "Xodim sertifikatini ID bo'yicha olish")
    public ResponseEntity<Map<String, Object>> getEmployeeCertificate(
            @Parameter(description = "Employee certificate ID") @PathVariable UUID id) {
        Optional<EmployeeCertificate> opt = employeeRefService.findEmployeeCertificateById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found", "details", "Entity hemishe_EEmpoyeeCertificate with id " + id + " not found"));
        }
        return ResponseEntity.ok(employeeRefService.toEmployeeCertificateMap(opt.get()));
    }

    @PreAuthorize("hasAuthority('teachers.edit')")
    @PutMapping("/{id}")
    @Operation(summary = "Xodim sertifikatini yangilash")
    public ResponseEntity<Map<String, Object>> updateEmployeeCertificate(
            @Parameter(description = "Employee certificate ID") @PathVariable UUID id,
            @RequestBody Map<String, Object> data) {
        Optional<EmployeeCertificate> opt = employeeRefService.findEmployeeCertificateById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmployeeCertificate cert = opt.get();
        employeeRefService.updateEmployeeCertificateFromMap(cert, data);
        cert.setUpdateTs(LocalDateTime.now());
        employeeRefService.saveEmployeeCertificate(cert);

        return ResponseEntity.ok(employeeRefService.toEmployeeCertificateMap(cert));
    }

    @PreAuthorize("hasAuthority('teachers.delete')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Xodim sertifikatini o'chirish")
    public ResponseEntity<Void> deleteEmployeeCertificate(
            @Parameter(description = "Employee certificate ID") @PathVariable UUID id) {
        Optional<EmployeeCertificate> opt = employeeRefService.findEmployeeCertificateById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        employeeRefService.softDeleteEmployeeCertificate(opt.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping
    @Operation(summary = "Xodim sertifikatlari ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAllEmployeeCertificates(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") int offset) {

        Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1));
        Page<EmployeeCertificate> page = employeeRefService.findAllEmployeeCertificate(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(employeeRefService::toEmployeeCertificateMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('teachers.view')")
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

        Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1), sorting);
        Page<EmployeeCertificate> page = employeeRefService.findAllEmployeeCertificate(pageable);

        List<Map<String, Object>> result = page.getContent().stream()
                .map(employeeRefService::toEmployeeCertificateMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('teachers.edit')")
    @PostMapping
    @Operation(summary = "Xodim sertifikatini yaratish/upsert")
    public ResponseEntity<Map<String, Object>> createEmployeeCertificate(
            @RequestBody Map<String, Object> data) {

        EmployeeCertificate cert = employeeRefService.createOrUpsertEmployeeCertificate(data);
        return ResponseEntity.ok(employeeRefService.toEmployeeCertificateMap(cert));
    }
}
