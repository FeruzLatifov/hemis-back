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
import java.util.UUID;

@Tag(name = "Academic Reports")
@RestController
@RequestMapping("/app/rest/v2/academic-reports")
@RequiredArgsConstructor
@Slf4j
public class AcademicReportController {

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'DEAN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStudentReport(
            @RequestParam(required = false) String university,
            @RequestParam(required = false) UUID faculty,
            @RequestParam(required = false) String academicYear
    ) {
        log.debug("Generating student report for university: {}, faculty: {}, year: {}",
                  university, faculty, academicYear);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "students");
        report.put("university", university);
        report.put("faculty", faculty);
        report.put("academicYear", academicYear);
        report.put("totalStudents", 0);
        report.put("activeStudents", 0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }

    @GetMapping("/grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'DEAN', 'TEACHER')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getGradeReport(
            @RequestParam(required = false) String university,
            @RequestParam(required = false) UUID specialty,
            @RequestParam(required = false) String academicYear
    ) {
        log.debug("Generating grade report for university: {}, specialty: {}, year: {}",
                  university, specialty, academicYear);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "grades");
        report.put("university", university);
        report.put("specialty", specialty);
        report.put("academicYear", academicYear);
        report.put("averageGrade", 0.0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN', 'DEAN', 'TEACHER')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getAttendanceReport(
            @RequestParam(required = false) String university,
            @RequestParam(required = false) UUID group,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        log.debug("Generating attendance report for university: {}, group: {}, period: {} to {}",
                  university, group, startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "attendance");
        report.put("university", university);
        report.put("group", group);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("attendanceRate", 0.0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getSummaryReport(
            @RequestParam String university,
            @RequestParam(required = false) String academicYear
    ) {
        log.debug("Generating summary report for university: {}, year: {}", university, academicYear);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "summary");
        report.put("university", university);
        report.put("academicYear", academicYear);
        report.put("totalStudents", 0);
        report.put("totalTeachers", 0);
        report.put("totalCourses", 0);
        report.put("generatedAt", LocalDate.now());

        return ResponseEntity.ok(ResponseWrapper.success(report));
    }
}
