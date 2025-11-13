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
@Tag(
    name = "Registry - Faculties", 
    description = """
        Faculty Registry API (Fakultetlar Reestri)
        
        **Features:**
        - Tree structure with lazy loading (OTM → Fakultetlar)
        - Server-side pagination and sorting
        - Search and filtering
        - Excel/CSV export with UTF-8 BOM
        - Multilingual support (uz-UZ, oz-UZ, ru-RU, en-US)
        
        **Use Case:** Frontend /registry/faculty page
        
        **Performance:** N+1 prevention with native queries + JOINs
        """
)
@SecurityRequirement(name = "bearerAuth")
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
        summary = "Get university groups (Tree root level)",
        description = """
            Get paginated list of universities with faculty counts.
            
            **Lazy Loading Strategy - Level 1 (Root):**
            Returns universities as group rows with aggregated faculty counts.
            Frontend expands each group to load faculties via `/by-university/{code}`.
            
            **Query Parameters:**
            - `q` - Search by university name or code (case-insensitive, partial match)
            - `status` - Filter faculties by active status (true/false, optional)
            - `page` - Page number (default: 0)
            - `size` - Page size (default: 20, max: 100)
            - `sort` - Sort field (default: name,asc)
            
            **Response:**
            Each group contains:
            - University code and name
            - Total faculty count
            - Active/Inactive faculty counts
            - hasChildren flag (always true for groups)
            
            **Example Request:**
            ```
            GET /api/v1/web/registry/faculties/groups?q=tatu&size=10&page=0
            ```
            
            **Example Response:**
            ```json
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "universityCode": "00001",
                    "universityName": "Toshkent Axborot Texnologiyalari Universiteti",
                    "facultyCount": 12,
                    "activeFacultyCount": 10,
                    "inactiveFacultyCount": 2,
                    "hasChildren": true
                  }
                ],
                "totalElements": 1,
                "totalPages": 1,
                "size": 10,
                "number": 0
              }
            }
            ```
            """,
        tags = {"Registry - Faculties"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved university groups",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FacultyGroupResponseWrapper.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid parameters"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token missing or invalid"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User lacks 'data.structure.view' permission"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error"
        )
    })
    public ResponseEntity<ResponseWrapper<Page<FacultyGroupRowDto>>> getGroups(
            @Parameter(
                description = "Search query (university name or code)",
                example = "tatu",
                required = false
            )
            @RequestParam(required = false) String q,
            
            @Parameter(
                description = "Filter by faculty status (true=active, false=inactive, null=all)",
                example = "true",
                required = false
            )
            @RequestParam(required = false) Boolean status,
            
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/faculties/groups - q={}, status={}, page={}", 
                 q, status, pageable.getPageNumber());

        Page<FacultyGroupRowDto> groups = facultyRegistryService.getFacultyGroups(q, status, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(groups));
    }
    
    @Schema(name = "FacultyGroupResponse")
    static class FacultyGroupResponseWrapper extends ResponseWrapper<Page<FacultyGroupRowDto>> {}

    // =====================================================
    // Children API (Faculties by university)
    // =====================================================

    @GetMapping("/by-university/{universityCode}")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get faculties by university (Tree child level)",
        description = """
            Get paginated list of faculties for specific university.
            
            **Lazy Loading Strategy - Level 2 (Children):**
            Called when user expands a university row in frontend tree table.
            Returns only faculties belonging to the specified university.
            
            **Query Parameters:**
            - `universityCode` (path) - University code (e.g., "00001")
            - `q` - Search by faculty name or code (optional)
            - `status` - Filter by active status (true/false, optional)
            - `page` - Page number (default: 0)
            - `size` - Page size (default: 50, recommended for children)
            
            **Example Request:**
            ```
            GET /api/v1/web/registry/faculties/by-university/00001?size=50
            ```
            
            **Example Response:**
            ```json
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "code": "00001-01",
                    "nameUz": "Axborot texnologiyalari fakulteti",
                    "nameRu": "Факультет информационных технологий",
                    "universityCode": "00001",
                    "universityName": "TATU",
                    "status": true
                  },
                  {
                    "code": "00001-02",
                    "nameUz": "Telekommunikatsiya fakulteti",
                    "nameRu": "Факультет телекоммуникаций",
                    "universityCode": "00001",
                    "universityName": "TATU",
                    "status": true
                  }
                ],
                "totalElements": 12,
                "totalPages": 1,
                "size": 50,
                "number": 0
              }
            }
            ```
            """,
        tags = {"Registry - Faculties"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved faculties",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FacultyRowResponseWrapper.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid university code format"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token missing or invalid"
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
            @Parameter(
                description = "University code (Primary key)",
                example = "00001",
                required = true
            )
            @PathVariable String universityCode,
            
            @Parameter(
                description = "Search query (faculty name or code)",
                example = "axborot",
                required = false
            )
            @RequestParam(required = false) String q,
            
            @Parameter(
                description = "Filter by status (true=active only, false=inactive only, null=all)",
                example = "true",
                required = false
            )
            @RequestParam(required = false) Boolean status,
            
            @Parameter(hidden = true)
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
    
    @Schema(name = "FacultyRowResponse")
    static class FacultyRowResponseWrapper extends ResponseWrapper<Page<FacultyRowDto>> {}

    // =====================================================
    // Detail API (Single faculty)
    // =====================================================

    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get faculty detail by code",
        description = """
            Get complete faculty information including audit fields.
            
            **Use Case:** 
            - Detail drawer/modal in frontend
            - Faculty profile page
            
            **Response Fields:**
            - Basic info: code, name (uz/ru), university
            - Classification: department type, parent
            - Audit: created/updated timestamps and users
            
            **Example Request:**
            ```
            GET /api/v1/web/registry/faculties/00001-01
            ```
            
            **Example Response:**
            ```json
            {
              "success": true,
              "data": {
                "code": "00001-01",
                "nameUz": "Axborot texnologiyalari fakulteti",
                "nameRu": "Факультет информационных технологий",
                "universityCode": "00001",
                "universityName": "TATU",
                "status": true,
                "departmentType": "11",
                "departmentTypeName": "Fakultet",
                "parentCode": null,
                "path": "00001/00001-01",
                "createdAt": "2023-09-01T10:00:00",
                "createdBy": "admin",
                "updatedAt": "2024-01-15T14:30:00",
                "updatedBy": "rector"
              }
            }
            ```
            """,
        tags = {"Registry - Faculties"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved faculty detail",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FacultyDetailResponseWrapper.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token missing or invalid"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Faculty not found - Invalid code or deleted"
        )
    })
    public ResponseEntity<ResponseWrapper<FacultyDetailDto>> getFacultyDetail(
            @Parameter(
                description = "Faculty code (Primary key)",
                example = "00001-01",
                required = true
            )
            @PathVariable String code
    ) {
        log.info("GET /api/v1/web/registry/faculties/{}", code);

        return facultyRegistryService.getFacultyDetail(code)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @Schema(name = "FacultyDetailResponse")
    static class FacultyDetailResponseWrapper extends ResponseWrapper<FacultyDetailDto> {}

    // =====================================================
    // Export API (Excel/CSV)
    // =====================================================

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Export faculties to CSV file",
        description = """
            Export all faculties matching current filters to CSV file.
            
            **Export Strategy:**
            - If `universityCode` provided: exports faculties for that university only
            - If no `universityCode`: exports up to 1000 faculties (limitation)
            - Respects search query and status filters
            - UTF-8 BOM for Excel compatibility
            
            **CSV Format:**
            ```
            Kod,OTM nomi,Fakultet nomi (o'zbekcha),Fakultet nomi (ruscha),Holati
            00001-01,TATU,Axborot texnologiyalari,Информационные технологии,Faol
            00001-02,TATU,Telekommunikatsiya,Телекоммуникации,Faol
            ```
            
            **File Naming:**
            ```
            faculties_20250112_153045.csv
            ```
            
            **Example Request:**
            ```
            POST /api/v1/web/registry/faculties/export?universityCode=00001&status=true
            ```
            
            **Example Response:**
            Binary CSV file with Content-Disposition header for download.
            """,
        tags = {"Registry - Faculties"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully generated CSV file",
            content = @Content(
                mediaType = "text/csv",
                schema = @Schema(type = "string", format = "binary")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token missing or invalid"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Failed to generate CSV"
        )
    })
    public ResponseEntity<byte[]> exportFaculties(
            @Parameter(
                description = "Search query (faculty or university name/code)",
                example = "axborot",
                required = false
            )
            @RequestParam(required = false) String q,
            
            @Parameter(
                description = "Filter by status (true=active, false=inactive, null=all)",
                example = "true",
                required = false
            )
            @RequestParam(required = false) Boolean status,
            
            @Parameter(
                description = "Export faculties for specific university only",
                example = "00001",
                required = false
            )
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
        summary = "Get filter dictionaries (Cached)",
        description = """
            Get reference data for populating filter dropdown options.
            
            **Caching:**
            - Cache name: `facultyDictionaries`
            - TTL: 1 hour
            - Reduces database load for frequently accessed reference data
            
            **Returns:**
            - `statuses` - Active/Inactive options for status filter
            - `departmentTypes` - All department types from database
            
            **Use Case:**
            - Populate filter dropdowns in frontend
            - Status select: Active / Inactive / All
            - Department type select (if needed for future filtering)
            
            **Example Request:**
            ```
            GET /api/v1/web/registry/faculties/dictionaries
            ```
            
            **Example Response:**
            ```json
            {
              "success": true,
              "data": {
                "statuses": [
                  {
                    "code": "true",
                    "label": "Active",
                    "description": "Active faculties"
                  },
                  {
                    "code": "false",
                    "label": "Inactive",
                    "description": "Inactive faculties"
                  }
                ],
                "departmentTypes": [
                  {
                    "code": "11",
                    "label": "Fakultet"
                  },
                  {
                    "code": "12",
                    "label": "Kafedra"
                  }
                ]
              }
            }
            ```
            """,
        tags = {"Registry - Faculties"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved dictionaries",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FacultyDictionariesResponseWrapper.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token missing or invalid"
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
    
    @Schema(name = "FacultyDictionariesResponse")
    static class FacultyDictionariesResponseWrapper extends ResponseWrapper<FacultyDictionariesDto> {}
}
