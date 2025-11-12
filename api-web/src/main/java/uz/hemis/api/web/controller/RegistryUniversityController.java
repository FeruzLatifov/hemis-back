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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.UniversityDto;
import uz.hemis.service.registry.UniversityRegistryService;

import java.nio.charset.StandardCharsets;
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
@Tag(name = "Universities Registry", description = "University Registry API for Frontend UI")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@Slf4j
public class RegistryUniversityController {

    private final UniversityRegistryService universityRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('registry.e-reestr.view')")
    @Operation(
        summary = "Get universities list",
        description = """
            Get paginated list of universities with filtering and sorting.

            **Filters:**
            - q: Search by code, name, or TIN
            - regionId: Filter by SOATO region code
            - ownershipId: Filter by ownership type
            - typeId: Filter by university type

            **Sorting:**
            - Default: name,asc
            - Available: code, name, tin
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved universities list"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        )
    })
    public ResponseEntity<ResponseWrapper<Page<UniversityDto>>> getUniversities(
            @Parameter(description = "Search query (code, name, TIN)")
            @RequestParam(required = false) String q,

            @Parameter(description = "Region ID (SOATO code)")
            @RequestParam(required = false) String regionId,

            @Parameter(description = "Ownership type code")
            @RequestParam(required = false) String ownershipId,

            @Parameter(description = "University type code")
            @RequestParam(required = false) String typeId,

            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/universities - q={}, regionId={}, ownershipId={}, typeId={}, page={}",
                 q, regionId, ownershipId, typeId, pageable.getPageNumber());

        Page<UniversityDto> universities = universityRegistryService.searchUniversities(
                q, regionId, ownershipId, typeId, pageable
        );

        return ResponseEntity.ok(ResponseWrapper.success(universities));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('registry.e-reestr.view')")
    @Operation(
        summary = "Get university by ID",
        description = "Get detailed information about a specific university by its code"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved university details"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "University not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        )
    })
    public ResponseEntity<ResponseWrapper<UniversityDto>> getUniversity(
            @Parameter(description = "University code")
            @PathVariable String id
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
    @PreAuthorize("hasAuthority('registry.e-reestr.view')")
    @Operation(
        summary = "Export universities to CSV",
        description = """
            Export filtered universities list to CSV format.
            Same filters as GET /universities endpoint.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported universities"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        )
    })
    public ResponseEntity<byte[]> exportUniversities(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String q,

            @Parameter(description = "Region ID")
            @RequestParam(required = false) String regionId,

            @Parameter(description = "Ownership type code")
            @RequestParam(required = false) String ownershipId,

            @Parameter(description = "University type code")
            @RequestParam(required = false) String typeId
    ) {
        log.info("POST /api/v1/web/registry/universities/export - q={}, regionId={}, ownershipId={}, typeId={}",
                 q, regionId, ownershipId, typeId);

        List<UniversityDto> universities = universityRegistryService.exportUniversities(
                q, regionId, ownershipId, typeId
        );

        StringBuilder csv = new StringBuilder();
        csv.append("Code,Name,TIN,Address,Region,Ownership,Type\n");

        for (UniversityDto university : universities) {
            csv.append(escapeCsv(university.getCode())).append(",");
            csv.append(escapeCsv(university.getName())).append(",");
            csv.append(escapeCsv(university.getTin())).append(",");
            csv.append(escapeCsv(university.getAddress())).append(",");
            csv.append(escapeCsv(university.getSoatoRegion())).append(",");
            csv.append(escapeCsv(university.getOwnership())).append(",");
            csv.append(escapeCsv(university.getUniversityType())).append("\n");
        }

        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "universities.csv");
        headers.setContentLength(csvBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('registry.e-reestr.view')")
    @Operation(
        summary = "Get filter dictionaries",
        description = "Get available values for filter select inputs (ownerships, types, regions)"
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
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getDictionaries() {
        log.info("GET /api/v1/web/registry/universities/dictionaries");

        Map<String, Object> dictionaries = universityRegistryService.getDictionaries();
        return ResponseEntity.ok(ResponseWrapper.success(dictionaries));
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
