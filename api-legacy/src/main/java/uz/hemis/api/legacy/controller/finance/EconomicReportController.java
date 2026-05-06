package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "65.Xo'jalik hisobot", description = "Iqtisodiy hisobotlar")
@RestController
@RequestMapping("/app/rest/v2/economic-reports")
@RequiredArgsConstructor
@Slf4j
public class EconomicReportController {

    @Operation(summary = "get financial report")
    @GetMapping("/financial")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getFinancialReport(
            @RequestParam String university,
            @RequestParam(required = false) Integer year
    ) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("university", university);
        report.put("year", year);
        report.put("totalRevenue", 0.0);
        report.put("totalExpenses", 0.0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }

    @Operation(summary = "get budget report")
    @GetMapping("/budget")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getBudgetReport(@RequestParam String university) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("university", university);
        report.put("budgetAllocated", 0.0);
        report.put("budgetUsed", 0.0);

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }
}
