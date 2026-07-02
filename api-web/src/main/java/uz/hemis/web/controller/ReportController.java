package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.report.ReportDto;
import uz.hemis.service.report.InstitutionReportService;
import uz.hemis.service.report.ScientificReportService;
import uz.hemis.service.report.StudentReportService;
import uz.hemis.service.report.TeacherReportService;

import java.util.concurrent.TimeUnit;

/**
 * Ministry Analytics Reports API.
 *
 * <p>Four report cards over one shared contract ({@link ReportDto} = {@code kpis[]} + {@code blocks[]}).
 * All read the READ REPLICA through the report services and are cached ("reports", 30 min). Every
 * endpoint is gated by its seeded per-report permission. Read-only aggregation — no mutations.</p>
 *
 * <p>Academic + Economic reports are intentionally deferred (data-blocked).</p>
 */
@RestController
@RequestMapping("/api/v1/web/reports")
@Tag(name = "📑 Ministry Reports", description = "Analytics report cards (students, institutions, scientific, teachers)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final StudentReportService studentReportService;
    private final InstitutionReportService institutionReportService;
    private final ScientificReportService scientificReportService;
    private final TeacherReportService teacherReportService;

    @GetMapping("/students")
    @PreAuthorize("hasAuthority('reports.students.view')")
    @Operation(summary = "Students report",
            description = "KPIs (total/grant/contract/male/female) + blocks (by education type/form, gender, "
                    + "payment form, region, top universities). Source: hemishe_r_student_full (active, not expelled).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'reports.students.view'")
    })
    public ResponseEntity<ResponseWrapper<ReportDto>> students(
            @Parameter(description = "Academic year (optional; defaults to current on the frontend)")
            @RequestParam(required = false) Integer educationYear,
            @Parameter(description = "Education type classifier code (optional)")
            @RequestParam(required = false) String educationType,
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode) {
        return ok(studentReportService.build(educationYear, educationType, universityCode));
    }

    @GetMapping("/institutions")
    @PreAuthorize("hasAuthority('reports.institutions.view')")
    @Operation(summary = "Institutions report",
            description = "KPIs (institutions/faculties/cathedras) + blocks (by ownership, university type, "
                    + "region, university structure). Source: hemishe_e_university + departments.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'reports.institutions.view'")
    })
    public ResponseEntity<ResponseWrapper<ReportDto>> institutions(
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode) {
        return ok(institutionReportService.build(universityCode));
    }

    @GetMapping("/scientific")
    @PreAuthorize("hasAuthority('reports.research.view')")
    @Operation(summary = "Scientific activity report",
            description = "KPIs (publications/projects/doctoral students) + blocks (publications & projects "
                    + "by type and by university). Sources: publication_scientific, project, doctorate_student.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'reports.research.view'")
    })
    public ResponseEntity<ResponseWrapper<ReportDto>> scientific(
            @Parameter(description = "Publication issue year (optional; applies to publications)")
            @RequestParam(required = false) Integer educationYear,
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode) {
        return ok(scientificReportService.build(educationYear, universityCode));
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAuthority('reports.teachers.view')")
    @Operation(summary = "Teachers report",
            description = "KPIs (total teachers/PhD holders/professors) + blocks (by academic degree, rank, "
                    + "age band, gender, university). Source: hemishe_e_teacher.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'reports.teachers.view'")
    })
    public ResponseEntity<ResponseWrapper<ReportDto>> teachers(
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Academic degree classifier code (optional)")
            @RequestParam(required = false) String academicDegree) {
        return ok(teacherReportService.build(universityCode, academicDegree));
    }

    private ResponseEntity<ResponseWrapper<ReportDto>> ok(ReportDto report) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic().mustRevalidate())
                .body(ResponseWrapper.success(report));
    }
}
