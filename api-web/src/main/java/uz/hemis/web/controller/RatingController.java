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
import uz.hemis.service.rating.AcademicRatingService;
import uz.hemis.service.rating.AdministrativeRatingService;
import uz.hemis.service.rating.GpaRatingService;
import uz.hemis.service.rating.ScientificRatingService;

import java.util.concurrent.TimeUnit;

/**
 * Ministry RATING leaderboards API.
 *
 * <p>Four ranked-by-university leaderboard cards over the SAME shared contract as the analytics
 * reports ({@link ReportDto} = {@code kpis[]} + {@code blocks[]}). Each rating returns headline KPIs,
 * a pre-sorted {@code 'table'} block with an injected ordinal {@code Rank} column (ORDER BY the
 * ranking metric DESC), and a top-15 {@code 'bar'} block. All read the READ REPLICA through the
 * rating services and are cached ("ratings", 30 min). Every endpoint is gated by its per-rating
 * permission. Read-only aggregation — no mutations, no migrations.</p>
 */
@RestController
@RequestMapping("/api/v1/web/ratings")
@Tag(name = "🏆 Ministry Ratings", description = "University leaderboard cards (administrative, academic, scientific, GPA)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final AdministrativeRatingService administrativeRatingService;
    private final AcademicRatingService academicRatingService;
    private final ScientificRatingService scientificRatingService;
    private final GpaRatingService gpaRatingService;

    @GetMapping("/administrative")
    @PreAuthorize("hasAuthority('rating.administrative.view')")
    @Operation(summary = "Administrative rating",
            description = "Ranks universities by the TOTAL count of central RI administrative indicator rows "
                    + "(hemishe_ri_administrative_*). KPIs (universities ranked / top university / indicators) + "
                    + "ranked leaderboard table + top-15 bar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'rating.administrative.view'")
    })
    public ResponseEntity<ResponseWrapper<ReportDto>> administrative(
            @Parameter(description = "Education year code (optional)")
            @RequestParam(required = false) Integer educationYear,
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode) {
        return ok(administrativeRatingService.build(educationYear, universityCode));
    }

    @GetMapping("/academic")
    @PreAuthorize("hasAuthority('rating.academic.view')")
    @Operation(summary = "Academic rating",
            description = "Ranks universities by AVG(score_percent) over hemishe_r_academic_score. "
                    + "KPIs (average score / top university / debtors) + ranked leaderboard table + top-15 bar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'rating.academic.view'")
    })
    public ResponseEntity<ResponseWrapper<ReportDto>> academic(
            @Parameter(description = "Education year code (optional)")
            @RequestParam(required = false) Integer educationYear,
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode) {
        return ok(academicRatingService.build(educationYear, universityCode));
    }

    @GetMapping("/scientific")
    @PreAuthorize("hasAuthority('rating.scientific.view')")
    @Operation(summary = "Scientific rating",
            description = "Ranks universities by publications + projects + doctoral students "
                    + "(publication_scientific, project, doctorate_student). KPIs (total publications / total "
                    + "projects / top university) + ranked leaderboard table + top-15 bar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'rating.scientific.view'")
    })
    public ResponseEntity<ResponseWrapper<ReportDto>> scientific(
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode) {
        return ok(scientificRatingService.build(universityCode));
    }

    @GetMapping("/gpa")
    @PreAuthorize("hasAuthority('rating.gpa.view')")
    @Operation(summary = "Student GPA rating",
            description = "Ranks universities by AVG(CAST(gpa AS numeric)) over hemishe_e_student_gpa "
                    + "(non-numeric GPA excluded). KPIs (average GPA / top university / students counted) + "
                    + "ranked leaderboard table + top-15 bar. KPI/bar GPA values are ×100 (hundredths).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'rating.gpa.view'")
    })
    public ResponseEntity<ResponseWrapper<ReportDto>> gpa(
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode) {
        return ok(gpaRatingService.build(universityCode));
    }

    private ResponseEntity<ResponseWrapper<ReportDto>> ok(ReportDto rating) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic().mustRevalidate())
                .body(ResponseWrapper.success(rating));
    }
}
