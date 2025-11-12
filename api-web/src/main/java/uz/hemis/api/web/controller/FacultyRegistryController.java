package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.registry.FacultyRegistryService;
import uz.hemis.service.registry.dto.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Faculty Registry Controller - Frontend UI API
 *
 * <p><strong>Purpose:</strong> API for hemis-front Faculty Registry page</p>
 * <ul>
 *   <li>URL: /api/v1/web/registry/faculties</li>
 *   <li>Frontend: /registry/faculty</li>
 *   <li>Features: Lazy tree loading, Export, Dictionaries</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ol>
 *   <li>GET /groups - University groups with faculty counts</li>
 *   <li>GET /by-university/{code} - Faculties by university</li>
 *   <li>GET /{code} - Faculty detail</li>
 *   <li>POST /export - Export to Excel/CSV</li>
 *   <li>GET /dictionaries - Filter options</li>
 * </ol>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/faculties")
@Tag(name = "Faculties", description = "Faculty Registry API for Frontend UI")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@Slf4j
public class FacultyRegistryController {

    private final FacultyRegistryService facultyRegistryService;

    // =====================================================
    // Groups API (Universities with faculty counts)
    // =====================================================

    @GetMapping("/groups")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get university groups",
        description = """
            Get paginated list of universities with faculty counts (Lazy loading - Level 1).
            
            **Features:**
            - Search by university name or code
            - Filter by faculty status
            - Server-side pagination and sorting
            
            **Use Case:** Display root rows in tree table
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved university groups"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        )
    })
    public ResponseEntity<ResponseWrapper<Page<FacultyGroupRowDto>>> getGroups(
            @Parameter(description = "Search query (university name/code)")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filter by faculty status (true=active, false=inactive)")
            @RequestParam(required = false) Boolean status,
            
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/faculties/groups - q={}, status={}, page={}", 
                 q, status, pageable.getPageNumber());

        Page<FacultyGroupRowDto> groups = facultyRegistryService.getFacultyGroups(q, status, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(groups));
    }

    // =====================================================
    // Children API (Faculties by university)
    // =====================================================

    @GetMapping("/by-university/{universityCode}")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get faculties by university",
        description = """
            Get paginated list of faculties for a specific university (Lazy loading - Level 2).
            
            **Features:**
            - Search by faculty name or code
            - Filter by status
            - Server-side pagination and sorting
            
            **Use Case:** Display child rows when university is expanded
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved faculties"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "University not found"
        )
    })
    public ResponseEntity<ResponseWrapper<Page<FacultyRowDto>>> getFacultiesByUniversity(
            @Parameter(description = "University code", required = true)
            @PathVariable String universityCode,
            
            @Parameter(description = "Search query (faculty name/code)")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filter by status (true=active, false=inactive)")
            @RequestParam(required = false) Boolean status,
            
            @PageableDefault(size = 50, sort = "nameUz", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/faculties/by-university/{} - q={}, status={}", 
                 universityCode, q, status);

        Page<FacultyRowDto> faculties = facultyRegistryService.getFacultiesByUniversity(
            universityCode, q, status, pageable
        );
        return ResponseEntity.ok(ResponseWrapper.success(faculties));
    }

    // =====================================================
    // Detail API (Single faculty)
    // =====================================================

    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get faculty detail",
        description = """
            Get detailed information for a specific faculty.
            
            **Use Case:** Display in detail drawer/modal
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved faculty detail"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Faculty not found"
        )
    })
    public ResponseEntity<ResponseWrapper<FacultyDetailDto>> getFacultyDetail(
            @Parameter(description = "Faculty code", required = true)
            @PathVariable String code
    ) {
        log.info("GET /api/v1/web/registry/faculties/{}", code);

        return facultyRegistryService.getFacultyDetail(code)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================
    // Export API (Excel/CSV)
    // =====================================================

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Export faculties to CSV",
        description = """
            Export faculties to CSV file with current filter/sort parameters.
            
            **Features:**
            - Respects current filters (q, status)
            - UTF-8 encoding for Cyrillic
            
            **Use Case:** Export button in frontend
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully generated CSV file",
            content = @Content(mediaType = "text/csv")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        )
    })
    public ResponseEntity<byte[]> exportFaculties(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) Boolean status,
            
            @Parameter(description = "University code (optional)")
            @RequestParam(required = false) String universityCode
    ) {
        log.info("POST /api/v1/web/registry/faculties/export - q={}, status={}, universityCode={}", 
                 q, status, universityCode);

        try {
            byte[] csvBytes = generateCsvFile(q, status, universityCode);
            
            String filename = "faculties_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                             ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
            headers.setContentLength(csvBytes.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
                
        } catch (IOException e) {
            log.error("Error generating CSV file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] generateCsvFile(String q, Boolean status, String universityCode) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            StringBuilder csv = new StringBuilder();
            
            // UTF-8 BOM for Excel compatibility
            csv.append('\uFEFF');
            
            // CSV Header
            csv.append("Kod,OTM nomi,Fakultet nomi (o'zbekcha),Fakultet nomi (ruscha),Holati\n");
            
            // Fetch all data (no pagination for export)
            Pageable unpaged = Pageable.unpaged();
            Page<FacultyRowDto> faculties;
            
            if (universityCode != null) {
                faculties = facultyRegistryService.getFacultiesByUniversity(universityCode, q, status, unpaged);
            } else {
                // Export first 1000 faculties (limitation without full aggregation)
                Page<FacultyGroupRowDto> groups = facultyRegistryService.getFacultyGroups(q, status, Pageable.ofSize(100));
                if (!groups.getContent().isEmpty()) {
                    faculties = facultyRegistryService.getFacultiesByUniversity(
                        groups.getContent().get(0).getUniversityCode(),
                        q, status, Pageable.ofSize(1000)
                    );
                } else {
                    faculties = Page.empty();
                }
            }
            
            // Fill data rows
            for (FacultyRowDto faculty : faculties.getContent()) {
                csv.append(escapeCsv(faculty.getCode())).append(",");
                csv.append(escapeCsv(faculty.getUniversityName())).append(",");
                csv.append(escapeCsv(faculty.getNameUz())).append(",");
                csv.append(escapeCsv(faculty.getNameRu() != null ? faculty.getNameRu() : "")).append(",");
                csv.append(Boolean.TRUE.equals(faculty.getStatus()) ? "Faol" : "Nofaol");
                csv.append("\n");
            }
            
            out.write(csv.toString().getBytes(StandardCharsets.UTF_8));
            return out.toByteArray();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // =====================================================
    // Dictionaries API (Reference data)
    // =====================================================

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get dictionaries",
        description = """
            Get reference data for faculty filters (cached).
            
            **Returns:**
            - Status options (Active/Inactive)
            - Department types
            
            **Use Case:** Populate filter dropdowns
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved dictionaries"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        )
    })
    public ResponseEntity<ResponseWrapper<FacultyDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/faculties/dictionaries");

        FacultyDictionariesDto dictionaries = facultyRegistryService.getDictionaries();
        return ResponseEntity.ok(ResponseWrapper.success(dictionaries));
    }
}
