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
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.registry.DiplomaBlankDistributionService;
import uz.hemis.service.registry.dto.DiplomaBlankDistributionDictionariesDto;
import uz.hemis.service.registry.dto.DiplomaBlankDistributionRequestDto;
import uz.hemis.service.registry.dto.DiplomaBlankDistributionRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Diploma Blank Distribution Controller — CENTRAL-MINISTRY CRUD.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/diploma-blank-distribution</li>
 *   <li>Frontend route: /institutions/diploma-blank-distribution</li>
 *   <li>Endpoints: list, detail, create (201), update, delete (soft), dictionaries, export</li>
 * </ul>
 *
 * <p>The ministry manages serial-range allocations centrally; OTMs read via existing
 * legacy endpoints (NO fanout / outbox / webhook).</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/diploma-blank-distribution")
@Tag(name = "Registry - Diploma Blank Distribution",
        description = "Diploma-blank distribution registry (blank taqsimoti) — CENTRAL-MINISTRY CRUD")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DiplomaBlankDistributionController {

    private final DiplomaBlankDistributionService service;

    @GetMapping
    @PreAuthorize("hasAuthority('institutions.diploma-blank-distribution.view')")
    @Operation(summary = "List diploma-blank distributions (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DistributionRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.diploma-blank-distribution.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaBlankDistributionRowDto>>> list(
            @Parameter(description = "Search (blank series, university name, note)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education-year code")
            @RequestParam(required = false) String educationYear,
            @Parameter(description = "Blank-category code")
            @RequestParam(required = false) String blankCategory,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "distributionDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/diploma-blank-distribution - q={}, universityCode={}, educationYear={}, blankCategory={}, page={}",
                q, universityCode, educationYear, blankCategory, pageable.getPageNumber());
        Page<DiplomaBlankDistributionRowDto> page = service.list(q, universityCode, educationYear, blankCategory, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "DiplomaBlankDistributionRowResponse")
    static class DistributionRowResponseWrapper extends ResponseWrapper<PageResponse<DiplomaBlankDistributionRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.diploma-blank-distribution.view')")
    @Operation(summary = "Get distribution detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DistributionDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<DiplomaBlankDistributionRowDto>> getDetail(
            @Parameter(description = "Distribution id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("GET /api/v1/web/registry/diploma-blank-distribution/{}", id);
        return ResponseEntity.ok(ResponseWrapper.success(service.getDetail(id)));
    }

    @Schema(name = "DiplomaBlankDistributionDetailResponse")
    static class DistributionDetailResponseWrapper extends ResponseWrapper<DiplomaBlankDistributionRowDto> {}

    @PostMapping
    @PreAuthorize("hasAuthority('institutions.diploma-blank-distribution.create')")
    @Operation(summary = "Create diploma-blank distribution")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DistributionDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "400", description = "Validation error (e.g. end < start)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.diploma-blank-distribution.create'")
    })
    public ResponseEntity<ResponseWrapper<DiplomaBlankDistributionRowDto>> create(
            @Valid @RequestBody DiplomaBlankDistributionRequestDto request
    ) {
        log.info("POST /api/v1/web/registry/diploma-blank-distribution - university={}", request.universityCode());
        DiplomaBlankDistributionRowDto created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.diploma-blank-distribution.edit')")
    @Operation(summary = "Update diploma-blank distribution")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DistributionDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "400", description = "Validation error (e.g. end < start)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.diploma-blank-distribution.edit'"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<DiplomaBlankDistributionRowDto>> update(
            @Parameter(description = "Distribution id (UUID)", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody DiplomaBlankDistributionRequestDto request
    ) {
        log.info("PUT /api/v1/web/registry/diploma-blank-distribution/{}", id);
        DiplomaBlankDistributionRowDto updated = service.update(id, request);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.diploma-blank-distribution.delete')")
    @Operation(summary = "Delete diploma-blank distribution (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.diploma-blank-distribution.delete'"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Distribution id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/web/registry/diploma-blank-distribution/{}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('institutions.diploma-blank-distribution.view')")
    @Operation(summary = "Get filter/form dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DistributionDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<DiplomaBlankDistributionDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/diploma-blank-distribution/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(service.getDictionaries()));
    }

    @Schema(name = "DiplomaBlankDistributionDictionariesResponse")
    static class DistributionDictionariesResponseWrapper extends ResponseWrapper<DiplomaBlankDistributionDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('institutions.diploma-blank-distribution.view')")
    @Operation(summary = "Export diploma-blank distributions to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV file",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<byte[]> export(
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "University code") @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education-year code") @RequestParam(required = false) String educationYear,
            @Parameter(description = "Blank-category code") @RequestParam(required = false) String blankCategory
    ) {
        log.info("POST /api/v1/web/registry/diploma-blank-distribution/export - q={}, universityCode={}, educationYear={}, blankCategory={}",
                q, universityCode, educationYear, blankCategory);

        List<DiplomaBlankDistributionRowDto> rows = service.export(q, universityCode, educationYear, blankCategory);

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("OTM,O'quv yili,Ta'lim turi,Blank kategoriyasi,Seriya,Boshi,Oxiri,Soni,Holati,Taqsimot sanasi,Izoh\n");
        for (DiplomaBlankDistributionRowDto r : rows) {
            csv.append(escapeCsv(r.universityName())).append(",");
            csv.append(escapeCsv(r.educationYearName())).append(",");
            csv.append(escapeCsv(r.educationTypeName())).append(",");
            csv.append(escapeCsv(r.blankCategoryName())).append(",");
            csv.append(escapeCsv(r.blankSeria())).append(",");
            csv.append(escapeCsv(numStr(r.blankStartNumber()))).append(",");
            csv.append(escapeCsv(numStr(r.blankEndNumber()))).append(",");
            csv.append(escapeCsv(numStr(r.quantity()))).append(",");
            csv.append(escapeCsv(r.generateStatusName())).append(",");
            csv.append(escapeCsv(r.distributionDate() != null ? r.distributionDate().toString() : "")).append(",");
            csv.append(escapeCsv(r.note())).append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "diploma_blank_distribution_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private static String numStr(Integer n) {
        return n != null ? n.toString() : "";
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
