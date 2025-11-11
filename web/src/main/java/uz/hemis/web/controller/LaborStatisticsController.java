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

@Tag(name = "Labor Statistics")
@RestController
@RequestMapping("/app/rest/v2/labor-statistics")
@RequiredArgsConstructor
@Slf4j
public class LaborStatisticsController {

    @GetMapping("/employment")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getEmploymentStatistics(
            @RequestParam String university,
            @RequestParam(required = false) String academicYear
    ) {
        log.debug("Getting employment statistics for university: {}, year: {}", university, academicYear);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("reportType", "employment");
        statistics.put("university", university);
        statistics.put("academicYear", academicYear);
        statistics.put("employmentRate", 0.0);
        statistics.put("totalGraduates", 0);
        statistics.put("employed", 0);
        statistics.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(statistics));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'HR')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStaffStatistics(
            @RequestParam String university
    ) {
        log.debug("Getting staff statistics for university: {}", university);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("reportType", "staff");
        statistics.put("university", university);
        statistics.put("totalStaff", 0);
        statistics.put("academicStaff", 0);
        statistics.put("administrativeStaff", 0);
        statistics.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(statistics));
    }

    @GetMapping("/workload")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'DEAN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getWorkloadStatistics(
            @RequestParam String university,
            @RequestParam(required = false) String academicYear
    ) {
        log.debug("Getting workload statistics for university: {}, year: {}", university, academicYear);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("reportType", "workload");
        statistics.put("university", university);
        statistics.put("academicYear", academicYear);
        statistics.put("averageWorkload", 0.0);
        statistics.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(statistics));
    }
}
