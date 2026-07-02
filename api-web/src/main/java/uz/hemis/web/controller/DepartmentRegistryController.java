package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.registry.DepartmentRegistryService;
import uz.hemis.service.registry.dto.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Department Registry Controller - Frontend UI API
 *
 * <p><strong>Purpose:</strong> API for hemis-front Department Registry page</p>
 * <ul>
 *   <li>URL: /api/v1/web/registry/departments</li>
 *   <li>Frontend: /registry/department</li>
 *   <li>Features: Lazy tree loading, Export, Dictionaries</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ol>
 *   <li>GET /groups - University groups with department counts</li>
 *   <li>GET /by-university/{code} - Departments by university</li>
 *   <li>GET /{code} - Department detail</li>
 *   <li>POST /export - Export to Excel/CSV</li>
 *   <li>GET /dictionaries - Filter options</li>
 * </ol>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/departments")
@Tag(
    name = "Registry - Departments",
    description = """
        Department Registry API (Kafedralar Reestri)

        **Features:**
        - Tree structure with lazy loading (OTM → Kafedralar)
        - Server-side pagination and sorting
        - Search and filtering
        - Excel/CSV export with UTF-8 BOM
        - Multilingual support (uz-UZ, oz-UZ, ru-RU, en-US)

        **Use Case:** Frontend /registry/department page

        **Performance:** N+1 prevention with native queries + JOINs
        """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DepartmentRegistryController {

    private final DepartmentRegistryService departmentRegistryService;

    // =====================================================
    // Groups API (Universities with department counts)
    // =====================================================

    @GetMapping("/groups")
    @PreAuthorize("hasAuthority('institutions.departments.view')")
    @Operation(
        summary = "Get university groups (Tree root level)",
        description = """
            Get paginated list of universities with department counts.

            **Lazy Loading Strategy - Level 1 (Root):**
            Returns universities as group rows with aggregated department counts.
            Frontend expands each group to load departments via `/by-university/{code}`.

            **Query Parameters:**
            - `q` - Search by university name or code (case-insensitive, partial match)
            - `status` - Filter departments by active status (true/false, optional)
            - `page` - Page number (default: 0)
            - `size` - Page size (default: 20, max: 100)
            - `sort` - Sort field (default: name,asc)

            **Response:**
            Each group contains:
            - University code and name
            - Total department count
            - Active/Inactive department counts
            - hasChildren flag (always true for groups)

            **Example Request:**
            ```
            GET /api/v1/web/registry/departments/groups?q=tatu&size=10&page=0
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
                    "departmentCount": 12,
                    "activeDepartmentCount": 10,
                    "inactiveDepartmentCount": 2,
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
        tags = {"Registry - Departments"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved university groups",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepartmentGroupResponseWrapper.class)
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
    public ResponseEntity<ResponseWrapper<PageResponse<DepartmentGroupRowDto>>> getGroups(
            @Parameter(
                description = "Search query (university name or code)",
                example = "tatu",
                required = false
            )
            @RequestParam(required = false) String q,

            @Parameter(
                description = "Filter by department status (true=active, false=inactive, null=all)",
                example = "true",
                required = false
            )
            @RequestParam(required = false) Boolean status,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/departments/groups - q={}, status={}, page={}",
                 q, status, pageable.getPageNumber());

        Page<DepartmentGroupRowDto> groups = departmentRegistryService.getDepartmentGroups(q, status, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(groups)));
    }

    @Schema(name = "DepartmentGroupResponse")
    static class DepartmentGroupResponseWrapper extends ResponseWrapper<PageResponse<DepartmentGroupRowDto>> {}

    // =====================================================
    // Children API (Departments by university)
    // =====================================================

    @GetMapping("/by-university/{universityCode}")
    @PreAuthorize("hasAuthority('institutions.departments.view')")
    @Operation(
        summary = "Get departments by university (Tree child level)",
        description = """
            Get paginated list of departments for specific university.

            **Lazy Loading Strategy - Level 2 (Children):**
            Called when user expands a university row in frontend tree table.
            Returns only departments belonging to the specified university.

            **Query Parameters:**
            - `universityCode` (path) - University code (e.g., "00001")
            - `q` - Search by department name or code (optional)
            - `status` - Filter by active status (true/false, optional)
            - `page` - Page number (default: 0)
            - `size` - Page size (default: 50, recommended for children)

            **Example Request:**
            ```
            GET /api/v1/web/registry/departments/by-university/00001?size=50
            ```

            **Example Response:**
            ```json
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "code": "00001-01",
                    "nameUz": "Axborot texnologiyalari kafedrasi",
                    "nameRu": "Кафедра информационных технологий",
                    "universityCode": "00001",
                    "universityName": "TATU",
                    "status": true
                  },
                  {
                    "code": "00001-02",
                    "nameUz": "Telekommunikatsiya kafedrasi",
                    "nameRu": "Кафедра телекоммуникаций",
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
        tags = {"Registry - Departments"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved departments",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepartmentRowResponseWrapper.class)
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
    public ResponseEntity<ResponseWrapper<PageResponse<DepartmentRowDto>>> getDepartmentsByUniversity(
            @Parameter(
                description = "University code (Primary key)",
                example = "00001",
                required = true
            )
            @PathVariable @NotBlank String universityCode,

            @Parameter(
                description = "Search query (department name or code)",
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
        log.info("GET /api/v1/web/registry/departments/by-university/{} - q={}, status={}",
                 universityCode, q, status);

        Page<DepartmentRowDto> departments = departmentRegistryService.getDepartmentsByUniversity(
            universityCode, q, status, pageable
        );
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(departments)));
    }

    @Schema(name = "DepartmentRowResponse")
    static class DepartmentRowResponseWrapper extends ResponseWrapper<PageResponse<DepartmentRowDto>> {}

    // =====================================================
    // Detail API (Single department)
    // =====================================================

    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('institutions.departments.view')")
    @Operation(
        summary = "Get department detail by code",
        description = """
            Get complete department information including audit fields.

            **Use Case:**
            - Detail drawer/modal in frontend
            - Department profile page

            **Response Fields:**
            - Basic info: code, name (uz/ru), university
            - Classification: department type, parent
            - Audit: created/updated timestamps and users

            **Example Request:**
            ```
            GET /api/v1/web/registry/departments/00001-01
            ```

            **Example Response:**
            ```json
            {
              "success": true,
              "data": {
                "code": "00001-01",
                "nameUz": "Axborot texnologiyalari kafedrasi",
                "nameRu": "Кафедра информационных технологий",
                "universityCode": "00001",
                "universityName": "TATU",
                "status": true,
                "departmentType": "12",
                "departmentTypeName": "Kafedra",
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
        tags = {"Registry - Departments"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved department detail",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepartmentDetailResponseWrapper.class)
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
            description = "Department not found - Invalid code or deleted"
        )
    })
    public ResponseEntity<ResponseWrapper<DepartmentDetailDto>> getDepartmentDetail(
            @Parameter(
                description = "Department code (Primary key)",
                example = "00001-01",
                required = true
            )
            @PathVariable @NotBlank String code
    ) {
        log.info("GET /api/v1/web/registry/departments/{}", code);

        return departmentRegistryService.getDepartmentDetail(code)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "DepartmentDetailResponse")
    static class DepartmentDetailResponseWrapper extends ResponseWrapper<DepartmentDetailDto> {}

    // =====================================================
    // Export API (Excel/CSV)
    // =====================================================

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('institutions.departments.view')")
    @Operation(
        summary = "Export departments to CSV file",
        description = """
            Export all departments matching current filters to CSV file.

            **Export Strategy:**
            - If `universityCode` provided: exports departments for that university only
            - If no `universityCode`: exports up to 1000 departments (limitation)
            - Respects search query and status filters
            - UTF-8 BOM for Excel compatibility

            **CSV Format:**
            ```
            Kod,OTM nomi,Kafedra nomi (o'zbekcha),Kafedra nomi (ruscha),Holati
            00001-01,TATU,Axborot texnologiyalari,Информационные технологии,Faol
            00001-02,TATU,Telekommunikatsiya,Телекоммуникации,Faol
            ```

            **File Naming:**
            ```
            departments_20250112_153045.csv
            ```

            **Example Request:**
            ```
            POST /api/v1/web/registry/departments/export?universityCode=00001&status=true
            ```

            **Example Response:**
            Binary CSV file with Content-Disposition header for download.
            """,
        tags = {"Registry - Departments"}
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
    public ResponseEntity<byte[]> exportDepartments(
            @Parameter(
                description = "Search query (department or university name/code)",
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
                description = "Export departments for specific university only",
                example = "00001",
                required = false
            )
            @RequestParam(required = false) String universityCode
    ) {
        log.info("POST /api/v1/web/registry/departments/export - q={}, status={}, universityCode={}",
                 q, status, universityCode);

        try {
            byte[] csvBytes = generateCsvFile(q, status, universityCode);

            String filename = "departments_" +
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
            csv.append('﻿');

            // CSV Header
            csv.append("Kod,OTM nomi,Kafedra nomi (o'zbekcha),Kafedra nomi (ruscha),Holati\n");

            // Fetch all data (no pagination for export)
            Pageable unpaged = Pageable.unpaged();
            Page<DepartmentRowDto> departments;

            if (universityCode != null) {
                departments = departmentRegistryService.getDepartmentsByUniversity(universityCode, q, status, unpaged);
            } else {
                // Export first 1000 departments (limitation without full aggregation)
                Page<DepartmentGroupRowDto> groups = departmentRegistryService.getDepartmentGroups(q, status, Pageable.ofSize(100));
                if (!groups.getContent().isEmpty()) {
                    departments = departmentRegistryService.getDepartmentsByUniversity(
                        groups.getContent().get(0).getUniversityCode(),
                        q, status, Pageable.ofSize(1000)
                    );
                } else {
                    departments = Page.empty();
                }
            }

            // Fill data rows
            for (DepartmentRowDto department : departments.getContent()) {
                csv.append(escapeCsv(department.getCode())).append(",");
                csv.append(escapeCsv(department.getUniversityName())).append(",");
                csv.append(escapeCsv(department.getNameUz())).append(",");
                csv.append(escapeCsv(department.getNameRu() != null ? department.getNameRu() : "")).append(",");
                csv.append(Boolean.TRUE.equals(department.getStatus()) ? "Faol" : "Nofaol");
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
    @PreAuthorize("hasAuthority('institutions.departments.view')")
    @Operation(
        summary = "Get filter dictionaries (Cached)",
        description = """
            Get reference data for populating filter dropdown options.

            **Caching:**
            - Cache name: `departmentDictionaries`
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
            GET /api/v1/web/registry/departments/dictionaries
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
                    "description": "Active departments"
                  },
                  {
                    "code": "false",
                    "label": "Inactive",
                    "description": "Inactive departments"
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
        tags = {"Registry - Departments"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved dictionaries",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepartmentDictionariesResponseWrapper.class)
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
    public ResponseEntity<ResponseWrapper<DepartmentDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/departments/dictionaries");

        DepartmentDictionariesDto dictionaries = departmentRegistryService.getDictionaries();
        return ResponseEntity.ok(ResponseWrapper.success(dictionaries));
    }

    @Schema(name = "DepartmentDictionariesResponse")
    static class DepartmentDictionariesResponseWrapper extends ResponseWrapper<DepartmentDictionariesDto> {}
}
