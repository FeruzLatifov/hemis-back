package uz.hemis.api.legacy.controller;

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

@Tag(name = "Economic Reports")
@RestController
@RequestMapping("/app/rest/v2/economic-reports")
@RequiredArgsConstructor
@Slf4j
public class EconomicReportController {

    @GetMapping("/financial")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getFinancialReport(
            @RequestParam String university,
            @RequestParam(required = false) Integer year
    ) {
        Map<String, Object> report = new HashMap<>();
        report.put("university", university);
        report.put("year", year);
        report.put("totalRevenue", 0.0);
        report.put("totalExpenses", 0.0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }

    @GetMapping("/budget")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getBudgetReport(@RequestParam String university) {
        Map<String, Object> report = new HashMap<>();
        report.put("university", university);
        report.put("budgetAllocated", 0.0);
        report.put("budgetUsed", 0.0);

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }
}
