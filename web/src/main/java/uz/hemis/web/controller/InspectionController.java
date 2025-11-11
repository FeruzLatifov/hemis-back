package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Inspection")
@RestController
@RequestMapping("/app/rest/v2/inspections")
@RequiredArgsConstructor
@Slf4j
public class InspectionController {

    @GetMapping("/quality-control")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSPECTOR')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getQualityControlReport(
            @RequestParam String university,
            @RequestParam(required = false) String academicYear
    ) {
        log.debug("Generating quality control report for university: {}, year: {}", university, academicYear);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "quality_control");
        report.put("university", university);
        report.put("academicYear", academicYear);
        report.put("inspectionScore", 0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }

    @GetMapping("/compliance")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSPECTOR')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getComplianceReport(
            @RequestParam String university
    ) {
        log.debug("Generating compliance report for university: {}", university);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "compliance");
        report.put("university", university);
        report.put("complianceRate", 0.0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getAuditReport(
            @RequestParam String university,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        log.debug("Generating audit report for university: {}, period: {} to {}",
                  university, startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "audit");
        report.put("university", university);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("auditFindings", 0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }
}
