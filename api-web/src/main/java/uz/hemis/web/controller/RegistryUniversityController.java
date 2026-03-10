package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.university.UniversityDto;
import uz.hemis.service.registry.UniversityRegistryService;
import uz.hemis.service.registry.dto.UniversityDictionariesDto;
import uz.hemis.service.registry.dto.UniversityRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Registry University Controller - Frontend UI API
 *
 * <p><strong>Purpose:</strong> API for hemis-front Registry page</p>
 * <ul>
 *   <li>URL: /api/v1/web/registry/universities</li>
 *   <li>Frontend: /registry/e-reestr/university</li>
 *   <li>Features: Advanced filtering, Export, Dictionaries</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This is DIFFERENT from legacy UniversityController!</p>
 * <ul>
 *   <li>Legacy API: /app/rest/v2/universities (CRUD operations)</li>
 *   <li>Registry API: /api/v1/web/registry/universities (UI operations)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/universities")
@Tag(
    name = "Registry - Universities",
    description = """
        University Registry API (Muassasalar Reestri)

        **Data Source:** READ REPLICA Database (zero master load)

        **Features:**
        - Advanced filtering (region, ownership, type)
        - Server-side pagination and sorting
        - Search by code, name, TIN
        - CSV export with filters
        - Dictionaries for filter dropdowns

        **Use Case:** Frontend /registry/e-reestr/university page

        **Performance:**
        - @Transactional(readOnly=true) → Reads from REPLICA
        - Cached dictionaries
        - Optimized JPA queries

        **Difference from Legacy API:**
        - Legacy: /app/rest/v2/universities (CRUD for universitet)
        - Registry: /api/v1/web/registry/universities (View only for admin)
        """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RegistryUniversityController {

    private final UniversityRegistryService universityRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('institutions.universities.view')")
    @Operation(
        summary = "Get universities list (with advanced filters)",
        description = """
            Get paginated list of universities with multi-field filtering.

            **Data Source:** READ REPLICA Database (DB_REPLICA_HOST)

            **Query Parameters:**
            - `q` - Search by code, name, or TIN (partial match, case-insensitive)
            - `regionId` - Filter by SOATO region code (exact match)
            - `ownershipId` - Filter by ownership type code
            - `typeId` - Filter by university type code
            - `page` - Page number (default: 0)
            - `size` - Page size (default: 20, max: 100)
            - `sort` - Sort field (default: name,asc)
            """,
        tags = {"Registry - Universities"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved universities list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User lacks 'institutions.universities.view' permission")
    })
    public ResponseEntity<ResponseWrapper<Page<UniversityDto>>> getUniversities(
            @Parameter(description = "Search query (code, name, or TIN)", example = "tatu")
            @RequestParam(required = false) String q,

            @Parameter(description = "Search field: code, name, tin, or all (default: all)", example = "all")
            @RequestParam(required = false) String searchField,

            @Parameter(description = "Region ID (SOATO code)", example = "26")
            @RequestParam(required = false) String regionId,

            @Parameter(description = "Ownership type code", example = "11")
            @RequestParam(required = false) String ownershipId,

            @Parameter(description = "University type code", example = "11")
            @RequestParam(required = false) String typeId,

            @Parameter(description = "Activity status code", example = "10")
            @RequestParam(required = false) String activityStatusId,

            @Parameter(description = "Belongs to code", example = "10")
            @RequestParam(required = false) String belongsToId,

            @Parameter(description = "Contract category code", example = "10")
            @RequestParam(required = false) String contractCategoryId,

            @Parameter(description = "HEMIS version type code", example = "11")
            @RequestParam(required = false) String versionTypeId,

            @Parameter(description = "District SOATO code (7-digit)", example = "1726269")
            @RequestParam(required = false) String districtId,

            @Parameter(description = "Active status filter", example = "true")
            @RequestParam(required = false) String active,

            @Parameter(description = "GPA edit flag", example = "true")
            @RequestParam(required = false) String gpaEdit,

            @Parameter(description = "Accreditation edit flag", example = "true")
            @RequestParam(required = false) String accreditationEdit,

            @Parameter(description = "Add student flag", example = "true")
            @RequestParam(required = false) String addStudent,

            @Parameter(description = "Allow grouping flag", example = "true")
            @RequestParam(required = false) String allowGrouping,

            @Parameter(description = "Allow transfer outside flag", example = "true")
            @RequestParam(required = false) String allowTransferOutside,

            @Parameter(description = "OneID login flag", example = "true")
            @RequestParam(required = false) String oneId,

            @Parameter(description = "Grading system flag", example = "true")
            @RequestParam(required = false) String gradingSystem,

            @Parameter(description = "Add foreign student flag", example = "true")
            @RequestParam(required = false) String addForeignStudent,

            @Parameter(description = "Add transfer student flag", example = "true")
            @RequestParam(required = false) String addTransferStudent,

            @Parameter(description = "Add academic mobile student flag", example = "true")
            @RequestParam(required = false) String addAcademicMobileStudent,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/universities - q={}, searchField={}, regionId={}, ownershipId={}, typeId={}, page={}",
                 q, searchField, regionId, ownershipId, typeId, pageable.getPageNumber());

        Page<UniversityDto> universities = universityRegistryService.searchUniversities(
                q, searchField, regionId, ownershipId, typeId, activityStatusId, belongsToId, contractCategoryId, versionTypeId,
                districtId, active, gpaEdit, accreditationEdit, addStudent, allowGrouping, allowTransferOutside,
                oneId, gradingSystem, addForeignStudent, addTransferStudent, addAcademicMobileStudent, pageable
        );

        return ResponseEntity.ok(ResponseWrapper.success(universities));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.universities.view')")
    @Operation(
        summary = "Get university by ID",
        description = "Get detailed information about a specific university by its code"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved university details"),
        @ApiResponse(responseCode = "404", description = "University not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<UniversityDto>> getUniversity(
            @Parameter(description = "University code")
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/universities/{}", id);

        UniversityDto university = universityRegistryService.getUniversityById(id);

        if (university == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseWrapper.error("University not found"));
        }

        return ResponseEntity.ok(ResponseWrapper.success(university));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('institutions.universities.view')")
    @Operation(
        summary = "Export universities to CSV",
        description = """
            Export filtered universities list to CSV format.
            Same filters as GET /universities endpoint.
            Includes UTF-8 BOM for Excel compatibility.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully exported universities",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportUniversities(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String q,

            @Parameter(description = "Search field: code, name, tin, or all (default: all)")
            @RequestParam(required = false) String searchField,

            @Parameter(description = "Region ID")
            @RequestParam(required = false) String regionId,

            @Parameter(description = "Ownership type code")
            @RequestParam(required = false) String ownershipId,

            @Parameter(description = "University type code")
            @RequestParam(required = false) String typeId,

            @Parameter(description = "Activity status code")
            @RequestParam(required = false) String activityStatusId,

            @Parameter(description = "Belongs to code")
            @RequestParam(required = false) String belongsToId,

            @Parameter(description = "Contract category code")
            @RequestParam(required = false) String contractCategoryId,

            @Parameter(description = "HEMIS version type code")
            @RequestParam(required = false) String versionTypeId,

            @Parameter(description = "District SOATO code")
            @RequestParam(required = false) String districtId,

            @Parameter(description = "Active status filter")
            @RequestParam(required = false) String active,

            @Parameter(description = "GPA edit flag")
            @RequestParam(required = false) String gpaEdit,

            @Parameter(description = "Accreditation edit flag")
            @RequestParam(required = false) String accreditationEdit,

            @Parameter(description = "Add student flag")
            @RequestParam(required = false) String addStudent,

            @Parameter(description = "Allow grouping flag")
            @RequestParam(required = false) String allowGrouping,

            @Parameter(description = "Allow transfer outside flag")
            @RequestParam(required = false) String allowTransferOutside,

            @Parameter(description = "OneID login flag")
            @RequestParam(required = false) String oneId,

            @Parameter(description = "Grading system flag")
            @RequestParam(required = false) String gradingSystem,

            @Parameter(description = "Add foreign student flag")
            @RequestParam(required = false) String addForeignStudent,

            @Parameter(description = "Add transfer student flag")
            @RequestParam(required = false) String addTransferStudent,

            @Parameter(description = "Add academic mobile student flag")
            @RequestParam(required = false) String addAcademicMobileStudent
    ) {
        log.info("POST /api/v1/web/registry/universities/export - q={}, searchField={}, regionId={}, ownershipId={}, typeId={}",
                 q, searchField, regionId, ownershipId, typeId);

        List<UniversityDto> universities = universityRegistryService.exportUniversities(
                q, searchField, regionId, ownershipId, typeId, activityStatusId, belongsToId, contractCategoryId, versionTypeId,
                districtId, active, gpaEdit, accreditationEdit, addStudent, allowGrouping, allowTransferOutside,
                oneId, gradingSystem, addForeignStudent, addTransferStudent, addAcademicMobileStudent
        );

        byte[] csvBytes = generateCsvFile(universities);
        String filename = "universities_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('institutions.universities.view')")
    @Operation(
        summary = "Get filter dictionaries (cached)",
        description = "Get available values for filter select inputs (ownerships, types, regions). Data loaded from database and cached."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<UniversityDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/universities/dictionaries");

        UniversityDictionariesDto dictionaries = universityRegistryService.getDictionaries();
        return ResponseEntity.ok(ResponseWrapper.success(dictionaries));
    }

    // =====================================================
    // CRUD Operations (CREATE, UPDATE, DELETE)
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('universities.edit')")
    @Operation(
        summary = "Create new university",
        description = """
            Create a new university in the system.

            **Data Target:** MASTER Database (write operation)

            **Required Fields:**
            - code (unique university code)
            - name (university name)

            **Permissions:** universities.edit
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "University created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or code already exists"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<UniversityDto>> createUniversity(
            @Valid @RequestBody UniversityRequestDto request
    ) {
        log.info("POST /api/v1/web/registry/universities - Creating: {}", request.getName());

        UniversityDto created = universityRegistryService.createUniversity(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(created));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasAuthority('universities.edit')")
    @Operation(
        summary = "Update existing university",
        description = """
            Update an existing university by code.

            **Data Target:** MASTER Database (write operation)

            **Permissions:** universities.edit
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "University updated successfully"),
        @ApiResponse(responseCode = "404", description = "University not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<UniversityDto>> updateUniversity(
            @PathVariable @NotBlank String code,
            @Valid @RequestBody UniversityRequestDto request
    ) {
        log.info("PUT /api/v1/web/registry/universities/{} - Updating", code);

        UniversityDto updated = universityRegistryService.updateUniversity(code, request);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasAuthority('universities.delete')")
    @Operation(
        summary = "Delete university (soft delete)",
        description = """
            Soft delete a university by code.
            Sets delete_ts timestamp instead of physical deletion.

            **Data Target:** MASTER Database (write operation)

            **Permissions:** universities.delete
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "University deleted successfully"),
        @ApiResponse(responseCode = "404", description = "University not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<Void> deleteUniversity(@PathVariable @NotBlank String code) {
        log.info("DELETE /api/v1/web/registry/universities/{}", code);

        universityRegistryService.deleteUniversity(code);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * Generate CSV file content with UTF-8 BOM, code-to-name resolution
     */
    private byte[] generateCsvFile(List<UniversityDto> universities) {
        // Load name maps for code → name resolution
        Map<String, String> regionNames = universityRegistryService.getRegionNameMap();
        Map<String, String> ownershipNames = universityRegistryService.getOwnershipNameMap();
        Map<String, String> typeNames = universityRegistryService.getUniversityTypeNameMap();

        StringBuilder csv = new StringBuilder();

        // UTF-8 BOM for Excel compatibility (MANDATORY)
        csv.append('\uFEFF');
        csv.append("Kod,Nomi,STIR,Manzil,Hudud,Mulkchilik shakli,Turi\n");

        for (UniversityDto university : universities) {
            csv.append(escapeCsv(university.getCode())).append(",");
            csv.append(escapeCsv(university.getName())).append(",");
            csv.append(escapeCsv(university.getTin())).append(",");
            csv.append(escapeCsv(university.getAddress())).append(",");
            csv.append(escapeCsv(resolveName(university.getSoatoRegion(), regionNames))).append(",");
            csv.append(escapeCsv(resolveName(university.getOwnership(), ownershipNames))).append(",");
            csv.append(escapeCsv(resolveName(university.getUniversityType(), typeNames))).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Resolve a classifier code to its display name
     */
    private String resolveName(String code, Map<String, String> nameMap) {
        if (code == null || code.isBlank()) return "";
        return nameMap.getOrDefault(code, code);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
