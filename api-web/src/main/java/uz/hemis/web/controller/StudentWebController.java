package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.student.DuplicateGroupDetailDto;
import uz.hemis.common.dto.student.DuplicateGroupDto;
import uz.hemis.common.dto.student.DuplicateStatsDto;
import uz.hemis.common.dto.student.StudentDictionariesDto;
import uz.hemis.common.dto.student.SpecialityStatsDto;
import uz.hemis.common.dto.student.SpecialitySummaryDto;
import uz.hemis.common.dto.student.StudentDto;
import uz.hemis.common.dto.student.StudentListDto;
import uz.hemis.common.dto.student.StudentStatsDto;
import uz.hemis.service.student.StudentWebService;

import java.util.UUID;

/**
 * Student Web Controller - Frontend UI API
 *
 * <p><strong>Purpose:</strong> API for hemis-front Students page</p>
 * <ul>
 *   <li>URL: /api/v1/web/students</li>
 *   <li>Frontend: /students</li>
 *   <li>Features: Multi-criteria filtering, Search, Pagination, Statistics</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This is DIFFERENT from legacy StudentController!</p>
 * <ul>
 *   <li>Legacy API: /app/rest/v2/services/student/* (CRUD operations)</li>
 *   <li>Web API: /api/v1/web/students (UI read operations)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/students")
@Tag(
    name = "Students - Web UI",
    description = """
        Student Management API for hemis-front

        **Data Source:** READ REPLICA Database (zero master load)

        **Features:**
        - Multi-criteria filtering (university, education type, payment form, course, etc.)
        - Server-side pagination and sorting
        - Full-text search by name, code, PINFL
        - Statistics for dashboard cards

        **Use Case:** Frontend /students page

        **Difference from Legacy API:**
        - Legacy: /app/rest/v2/services/student/* (CRUD for universities)
        - Web: /api/v1/web/students (View only for admin)
        """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class StudentWebController {

    private final StudentWebService studentWebService;

    @GetMapping
    @PreAuthorize("hasAuthority('students.view')")
    @Operation(
        summary = "Get students list (with advanced filters)",
        description = """
            Get paginated list of students with multi-field filtering.

            **Query Parameters:**
            - `q` - Search by name, code, or PINFL (partial match, case-insensitive)
            - `university` - Filter by university code
            - `educationType` - Filter by education type code ('11'=bachelor, '12'=master, '13'=doctoral)
            - `paymentForm` - Filter by payment form code ('11'=grant, '12'=contract)
            - `studentStatus` - Filter by student status code ('11'=active, '16'=graduated)
            - `course` - Filter by course code ('1','2','3','4')
            - `faculty` - Filter by faculty code
            - `educationForm` - Filter by education form code ('11'=full-time, '12'=part-time)
            - `educationYear` - Filter by education year code
            - `gender` - Filter by gender code ('1'=male, '2'=female)
            - `page` - Page number (default: 0)
            - `size` - Page size (default: 20, max: 100)
            - `sort` - Sort field (default: code,desc)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved students list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User lacks 'students.view' permission")
    })
    public ResponseEntity<ResponseWrapper<Page<StudentListDto>>> getStudents(
            @Parameter(description = "Search query (code or PINFL prefix)", example = "6023010")
            @RequestParam(required = false) String q,

            @Parameter(description = "Search field: 'code' or 'pinfl'. If omitted, searches both.", example = "code")
            @RequestParam(required = false) String searchField,

            @Parameter(description = "University code", example = "401")
            @RequestParam(required = false) String university,

            @Parameter(description = "Education type code", example = "11")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Payment form code", example = "11")
            @RequestParam(required = false) String paymentForm,

            @Parameter(description = "Student status code", example = "11")
            @RequestParam(required = false) String studentStatus,

            @Parameter(description = "Course code", example = "1")
            @RequestParam(required = false) String course,

            @Parameter(description = "Faculty code")
            @RequestParam(required = false) String faculty,

            @Parameter(description = "Education form code", example = "11")
            @RequestParam(required = false) String educationForm,

            @Parameter(description = "Education year code", example = "2024")
            @RequestParam(required = false) String educationYear,

            @Parameter(description = "Gender code", example = "1")
            @RequestParam(required = false) String gender,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/students - q={}, university={}, educationType={}, paymentForm={}, page={}",
                q, university, educationType, paymentForm, pageable.getPageNumber());

        Page<StudentListDto> students = studentWebService.searchStudents(
                q, searchField, university, educationType, paymentForm, studentStatus,
                course, faculty, educationForm, educationYear, gender, pageable
        );

        return ResponseEntity.ok(ResponseWrapper.success(students));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('students.view')")
    @Operation(
        summary = "Get student by ID",
        description = "Get detailed information about a specific student by UUID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved student details"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<StudentDto>> getStudent(
            @Parameter(description = "Student UUID")
            @PathVariable UUID id
    ) {
        log.info("GET /api/v1/web/students/{}", id);

        StudentDto student = studentWebService.getStudentById(id);
        return ResponseEntity.ok(ResponseWrapper.success(student));
    }

    @GetMapping("/by-code/{code}")
    @PreAuthorize("hasAuthority('students.view')")
    @Operation(
        summary = "Get student by code",
        description = "Get student by business code (e.g., '401242311234')"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved student"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<StudentDto>> getStudentByCode(
            @Parameter(description = "Student code", example = "401242311234")
            @PathVariable String code
    ) {
        log.info("GET /api/v1/web/students/by-code/{}", code);

        StudentDto student = studentWebService.getStudentByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(student));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('students.view')")
    @Operation(
        summary = "Get student statistics",
        description = "Get aggregated statistics (total, grant, contract, graduate counts) for dashboard cards"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<StudentStatsDto>> getStats(
            @Parameter(description = "University code (optional, for university-specific stats)")
            @RequestParam(required = false) String university
    ) {
        log.info("GET /api/v1/web/students/stats - university={}", university);

        StudentStatsDto stats = studentWebService.getStats(university);
        return ResponseEntity.ok(ResponseWrapper.success(stats));
    }

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('students.view')")
    @Operation(
        summary = "Get filter dictionaries (cached)",
        description = "Get available values for student filter selects (courses, statuses, payment forms, etc.). Data loaded from classifier tables and cached."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<StudentDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/students/dictionaries");

        StudentDictionariesDto dictionaries = studentWebService.getDictionaries();
        return ResponseEntity.ok(ResponseWrapper.success(dictionaries));
    }

    // =====================================================
    // Speciality Directions
    // =====================================================

    @GetMapping("/directions")
    @PreAuthorize("hasAuthority('students.directions.view')")
    @Operation(
        summary = "Get speciality directions with student statistics",
        description = """
            Get paginated list of specialities (bachelor, master, ordinatura)
            with student count breakdown by status (active, graduated, expelled).

            **Filters:**
            - `search` - Search by speciality code or name (ILIKE)
            - `educationType` - Filter: BACHELOR, MASTER, ORDINATURA
            - `hasStudents` - true = only with students, false = only without
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved directions"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User lacks 'students.directions.view' permission")
    })
    public ResponseEntity<ResponseWrapper<Page<SpecialityStatsDto>>> getDirections(
            @Parameter(description = "Search by speciality code or name")
            @RequestParam(required = false) String search,

            @Parameter(description = "Education type filter: BACHELOR, MASTER, ORDINATURA")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Has students filter: true/false")
            @RequestParam(required = false) Boolean hasStudents,

            @Parameter(hidden = true)
            @PageableDefault(size = 20)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/students/directions - search={}, educationType={}, hasStudents={}, page={}",
                search, educationType, hasStudents, pageable.getPageNumber());

        Page<SpecialityStatsDto> directions = studentWebService.getSpecialityStats(
                search, educationType, hasStudents, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(directions));
    }

    @GetMapping("/directions/summary")
    @PreAuthorize("hasAuthority('students.directions.view')")
    @Operation(
        summary = "Get speciality directions summary statistics",
        description = "Get aggregated statistics for speciality directions header cards"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved directions summary"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<SpecialitySummaryDto>> getDirectionsSummary() {
        log.info("GET /api/v1/web/students/directions/summary");

        SpecialitySummaryDto summary = studentWebService.getSpecialitySummary();
        return ResponseEntity.ok(ResponseWrapper.success(summary));
    }

    // =====================================================
    // Duplicate Detection
    // =====================================================

    @GetMapping("/duplicates/stats")
    @PreAuthorize("hasAuthority('students.view')")
    @Operation(
        summary = "Get duplicate PINFL statistics",
        description = "Get aggregated statistics about students with duplicate PINFLs"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved duplicate statistics"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<DuplicateStatsDto>> getDuplicateStats(
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String university
    ) {
        log.info("GET /api/v1/web/students/duplicates/stats - university={}", university);

        DuplicateStatsDto stats = studentWebService.getDuplicateStats(university);
        return ResponseEntity.ok(ResponseWrapper.success(stats));
    }

    @GetMapping("/duplicates")
    @PreAuthorize("hasAuthority('students.view')")
    @Operation(
        summary = "Get duplicate PINFL groups (with reason analysis)",
        description = """
            Get paginated list of duplicate PINFL groups with reason categorization.
            Each group contains all students sharing the same PINFL.

            **Reason categories:**
            - `NORMAL` — 0-1 active record, rest graduated/expelled (not a real problem)
            - `CROSS_UNIVERSITY` — active in 2+ universities simultaneously (serious issue)
            - `SAME_UNIVERSITY` — 2+ active in same university, same education type (data error)
            - `MULTI_LEVEL` — active in same university, different education types e.g. bachelor+master (needs review)

            Filter by `reason` parameter to see only specific categories.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved duplicate groups"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<Page<DuplicateGroupDto>>> getDuplicates(
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String university,

            @Parameter(description = "Reason filter: NORMAL, CROSS_UNIVERSITY, SAME_UNIVERSITY, MULTI_LEVEL")
            @RequestParam(required = false) String reason,

            @Parameter(hidden = true)
            @PageableDefault(size = 20)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/students/duplicates - university={}, reason={}, page={}", university, reason, pageable.getPageNumber());

        Page<DuplicateGroupDto> duplicates = studentWebService.getDuplicates(university, reason, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(duplicates));
    }

    @GetMapping("/duplicates/groups/{pinfl}")
    @PreAuthorize("hasAuthority('students.view')")
    @Operation(
        summary = "Dublikat guruh tafsilotlari",
        description = "Bitta PINFL uchun to'liq dublikat tahlili — barcha yozuvlar classifier nomlari bilan, " +
                "sabab tushuntirishi va tavsiya."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved group detail"),
        @ApiResponse(responseCode = "404", description = "No students found for this PINFL")
    })
    public ResponseEntity<ResponseWrapper<DuplicateGroupDetailDto>> getDuplicateGroupDetail(
            @Parameter(description = "PINFL to analyze")
            @PathVariable String pinfl
    ) {
        log.info("GET /api/v1/web/students/duplicates/groups/{}", pinfl.length() > 4 ? pinfl.substring(0, 4) + "****" : pinfl);

        DuplicateGroupDetailDto detail = studentWebService.getDuplicateGroupDetail(pinfl);
        return ResponseEntity.ok(ResponseWrapper.success(detail));
    }
}
