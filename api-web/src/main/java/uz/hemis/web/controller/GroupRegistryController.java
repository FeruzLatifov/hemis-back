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
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.registry.GroupRegistryService;
import uz.hemis.service.registry.dto.*;
import uz.hemis.service.util.PageResponses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Group Registry Controller - Frontend UI API for the Study Groups (Guruhlar) registry.
 *
 * <p><strong>Purpose:</strong> READ-ONLY API for the hemis-front Study Groups page.</p>
 * <ul>
 *   <li>URL: /api/v1/web/registry/groups</li>
 *   <li>Frontend route: /students/groups (STUDENTS domain)</li>
 *   <li>Features: Lazy tree loading, Export, Dictionaries</li>
 * </ul>
 *
 * <p><strong>Endpoints (GET + export only — no mutations):</strong></p>
 * <ol>
 *   <li>GET /groups - University groups with study-group counts</li>
 *   <li>GET /by-university/{universityCode} - Study groups by university</li>
 *   <li>GET /{id} - Study group detail</li>
 *   <li>GET /dictionaries - Filter options</li>
 *   <li>POST /export - Export to CSV</li>
 * </ol>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/groups")
@Tag(
    name = "Registry - Study Groups",
    description = """
        Study Groups Registry API (Guruhlar Reestri)

        **Features:**
        - Tree structure with lazy loading (OTM → Guruhlar)
        - Server-side pagination and sorting
        - Search and filtering (education type / education year / status)
        - CSV export with UTF-8 BOM
        - READ-ONLY (OTM-owned academic data synced from Univer)

        **Use Case:** Frontend /students/groups page

        **Performance:** N+1 prevention with native queries + JOINs
        """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class GroupRegistryController {

    private final GroupRegistryService groupRegistryService;

    // =====================================================
    // Groups API (Universities with study-group counts)
    // =====================================================

    @GetMapping("/groups")
    @PreAuthorize("hasAuthority('students.groups.view')")
    @Operation(
        summary = "Get university groups (Tree root level)",
        description = """
            Get paginated list of universities with study-group counts.

            **Lazy Loading Strategy - Level 1 (Root):**
            Returns universities as group rows with aggregated study-group counts.
            Frontend expands each group to load study groups via `/by-university/{code}`.

            **Query Parameters:**
            - `q` - Search by university name or code (case-insensitive, partial match)
            - `status` - Filter groups by active status (true/false, optional)
            - `page` / `size` / `sort` - Standard pagination
            """,
        tags = {"Registry - Study Groups"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved university groups",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GroupGroupResponseWrapper.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User lacks 'students.groups.view' permission"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<GroupGroupRowDto>>> getGroups(
            @Parameter(description = "Search query (university name or code)")
            @RequestParam(required = false) String q,

            @Parameter(description = "Filter by group status (true=active, false=inactive, null=all)")
            @RequestParam(required = false) Boolean status,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/groups/groups - q={}, status={}, page={}",
                 q, status, pageable.getPageNumber());

        Page<GroupGroupRowDto> groups = groupRegistryService.getGroupGroups(q, status, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(groups)));
    }

    @Schema(name = "GroupGroupResponse")
    static class GroupGroupResponseWrapper extends ResponseWrapper<PageResponse<GroupGroupRowDto>> {}

    // =====================================================
    // Children API (Study groups by university)
    // =====================================================

    @GetMapping("/by-university/{universityCode}")
    @PreAuthorize("hasAuthority('students.groups.view')")
    @Operation(
        summary = "Get study groups by university (Tree child level)",
        description = """
            Get paginated list of study groups for a specific university.

            **Lazy Loading Strategy - Level 2 (Children):**
            Called when user expands a university row in the frontend tree table.

            **Query Parameters:**
            - `universityCode` (path) - University code (e.g., "00001")
            - `q` - Search by group name or external group id (optional)
            - `educationType` - Filter by education-type classifier code (optional)
            - `educationYear` - Filter by education-year classifier code (optional)
            - `status` - Filter by active status (true/false, optional)
            - `page` / `size` - Standard pagination
            """,
        tags = {"Registry - Study Groups"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved study groups",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GroupRowResponseWrapper.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid university code format"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "University not found")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<GroupRegistryRowDto>>> getGroupsByUniversity(
            @Parameter(description = "University code", required = true)
            @PathVariable @NotBlank String universityCode,

            @Parameter(description = "Search query (group name or external group id)")
            @RequestParam(required = false) String q,

            @Parameter(description = "Filter by education-type classifier code")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Filter by education-year classifier code")
            @RequestParam(required = false) String educationYear,

            @Parameter(description = "Filter by status (true=active only, false=inactive only, null=all)")
            @RequestParam(required = false) Boolean status,

            @Parameter(hidden = true)
            @PageableDefault(size = 50, sort = "groupName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/groups/by-university/{} - q={}, educationType={}, educationYear={}, status={}",
                 universityCode, q, educationType, educationYear, status);

        Page<GroupRegistryRowDto> groups = groupRegistryService.getGroupsByUniversity(
            universityCode, q, educationType, educationYear, status, pageable
        );
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(groups)));
    }

    @Schema(name = "GroupRowResponse")
    static class GroupRowResponseWrapper extends ResponseWrapper<PageResponse<GroupRegistryRowDto>> {}

    // =====================================================
    // Detail API (Single study group)
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('students.groups.view')")
    @Operation(
        summary = "Get study-group detail by id",
        description = """
            Get complete study-group information by UUID.

            **Use Case:** Detail drawer/modal in frontend.

            **Note:** The source table has no audit columns, so no created/updated fields are returned.
            """,
        tags = {"Registry - Study Groups"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved study-group detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GroupDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Study group not found - Invalid id")
    })
    public ResponseEntity<ResponseWrapper<GroupDetailDto>> getGroupDetail(
            @Parameter(description = "Group id (UUID primary key)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/groups/{}", id);

        return groupRegistryService.getGroupDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "GroupDetailResponse")
    static class GroupDetailResponseWrapper extends ResponseWrapper<GroupDetailDto> {}

    // =====================================================
    // Dictionaries API (Reference data)
    // =====================================================

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('students.groups.view')")
    @Operation(
        summary = "Get filter dictionaries (Cached)",
        description = """
            Get reference data for populating filter dropdown options.

            **Caching:**
            - Cache name: `groupDictionaries`
            - TTL: 6 hours

            **Returns:**
            - `educationTypes` - Education-type classifier options
            - `educationYears` - Education-year classifier options
            - `statuses` - Active/Inactive options for status filter
            """,
        tags = {"Registry - Study Groups"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GroupDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<GroupDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/groups/dictionaries");

        GroupDictionariesDto dictionaries = groupRegistryService.getDictionaries();
        return ResponseEntity.ok(ResponseWrapper.success(dictionaries));
    }

    @Schema(name = "GroupDictionariesResponse")
    static class GroupDictionariesResponseWrapper extends ResponseWrapper<GroupDictionariesDto> {}

    // =====================================================
    // Export API (CSV)
    // =====================================================

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('students.groups.view')")
    @Operation(
        summary = "Export study groups to CSV file",
        description = """
            Export study groups matching current filters to a CSV file.

            **Export Strategy:**
            - If `universityCode` provided: exports study groups for that university only
            - If no `universityCode`: exports up to 1000 study groups of the first matching university (limitation)
            - Respects search query and filters
            - UTF-8 BOM for Excel compatibility

            **CSV Format:**
            ```
            Guruh ID,Guruh nomi,OTM nomi,Ta'lim turi,O'quv yili,Holati
            ```
            """,
        tags = {"Registry - Study Groups"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully generated CSV file",
            content = @Content(mediaType = "text/csv",
                schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error - Failed to generate CSV")
    })
    public ResponseEntity<byte[]> exportGroups(
            @Parameter(description = "Search query (group name or external group id)")
            @RequestParam(required = false) String q,

            @Parameter(description = "Filter by education-type classifier code")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Filter by education-year classifier code")
            @RequestParam(required = false) String educationYear,

            @Parameter(description = "Filter by status (true=active, false=inactive, null=all)")
            @RequestParam(required = false) Boolean status,

            @Parameter(description = "Export study groups for specific university only")
            @RequestParam(required = false) String universityCode
    ) {
        log.info("POST /api/v1/web/registry/groups/export - q={}, educationType={}, educationYear={}, status={}, universityCode={}",
                 q, educationType, educationYear, status, universityCode);

        try {
            byte[] csvBytes = generateCsvFile(q, educationType, educationYear, status, universityCode);

            String filename = "groups_" +
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

    private byte[] generateCsvFile(String q, String educationType, String educationYear,
                                   Boolean status, String universityCode) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            StringBuilder csv = new StringBuilder();

            // UTF-8 BOM for Excel compatibility
            csv.append('﻿');

            // CSV Header
            csv.append("Guruh ID,Guruh nomi,OTM nomi,Ta'lim turi,O'quv yili,Holati\n");

            Page<GroupRegistryRowDto> groups;

            if (universityCode != null) {
                groups = groupRegistryService.getGroupsByUniversity(
                    universityCode, q, educationType, educationYear, status, Pageable.ofSize(1000));
            } else {
                // Export first 1000 study groups of the first matching university (limitation without full aggregation)
                Page<GroupGroupRowDto> universities =
                    groupRegistryService.getGroupGroups(q, status, Pageable.ofSize(100));
                if (!universities.getContent().isEmpty()) {
                    groups = groupRegistryService.getGroupsByUniversity(
                        universities.getContent().get(0).universityCode(),
                        q, educationType, educationYear, status, Pageable.ofSize(1000)
                    );
                } else {
                    groups = Page.empty();
                }
            }

            for (GroupRegistryRowDto group : groups.getContent()) {
                csv.append(escapeCsv(group.groupId())).append(",");
                csv.append(escapeCsv(group.groupName())).append(",");
                csv.append(escapeCsv(group.universityName())).append(",");
                csv.append(escapeCsv(group.educationTypeName())).append(",");
                csv.append(escapeCsv(group.educationYearName())).append(",");
                csv.append(Boolean.TRUE.equals(group.active()) ? "Faol" : "Nofaol");
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
}
