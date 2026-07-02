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
import uz.hemis.service.registry.AttachedSpecialityRegistryService;
import uz.hemis.service.registry.dto.AttachedSpecialityCreateDto;
import uz.hemis.service.registry.dto.AttachedSpecialityDetailDto;
import uz.hemis.service.registry.dto.AttachedSpecialityDictionariesDto;
import uz.hemis.service.registry.dto.AttachedSpecialityRowDto;
import uz.hemis.service.registry.dto.AttachedSpecialityUpdateDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Attached-Speciality Registry Controller — Frontend UI API.
 *
 * <p><strong>Card:</strong> University specialities
 * (menu {@code inst-attached-specialities}, route {@code /institutions/attached-specialities}).</p>
 *
 * <p>CENTRAL-MINISTRY CRUD — the ministry attaches classifier specialities to
 * universities. Unlike Faculties/Departments (read-only, OTM-owned), this card
 * supports create/update/delete. URL: {@code /api/v1/web/registry/attached-specialities}.</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/attached-specialities")
@Tag(
        name = "Registry - Attached Specialities",
        description = """
                University Specialities Registry API (OTM mutaxassisliklari)

                **Type:** CENTRAL-MINISTRY CRUD (ministry attaches classifier specialities to universities)

                **Features:**
                - Server-side pagination, sorting, search and filtering
                - Create / update / delete (soft delete)
                - Duplicate guard (409) — no DB unique constraint
                - CSV export with UTF-8 BOM
                - Cached filter/form dictionaries

                **Use Case:** Frontend /institutions/attached-specialities page
                """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AttachedSpecialityRegistryController {

    private final AttachedSpecialityRegistryService service;

    // =====================================================
    // List
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAuthority('institutions.attached-specialities.view')")
    @Operation(
            summary = "List attached specialities (paginated, filtered)",
            description = """
                    Paginated list of university↔speciality attachments with resolved names.

                    **Query Parameters:**
                    - `q` — search by university or speciality name (case-insensitive, partial)
                    - `universityCode` — filter by university code
                    - `educationType` — filter by education type code
                    - `educationForm` — filter by education form code
                    - `active` — filter by active flag (true/false)
                    - `page`, `size`, `sort` — standard paging (default: universityName,asc)
                    """,
            tags = {"Registry - Attached Specialities"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachedSpecialityRowResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.attached-specialities.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<AttachedSpecialityRowDto>>> list(
            @Parameter(description = "Search query (university or speciality name)", example = "informatika")
            @RequestParam(required = false) String q,

            @Parameter(description = "University code", example = "00001")
            @RequestParam(required = false) String universityCode,

            @Parameter(description = "Education type code", example = "11")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Education form code", example = "11")
            @RequestParam(required = false) String educationForm,

            @Parameter(description = "Active flag filter", example = "true")
            @RequestParam(required = false) Boolean active,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "universityName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/attached-specialities - q={}, universityCode={}, educationType={}, educationForm={}, active={}, page={}",
                q, universityCode, educationType, educationForm, active, pageable.getPageNumber());

        Page<AttachedSpecialityRowDto> page = service.list(q, universityCode, educationType, educationForm, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "AttachedSpecialityRowResponse")
    static class AttachedSpecialityRowResponseWrapper extends ResponseWrapper<PageResponse<AttachedSpecialityRowDto>> {
    }

    // =====================================================
    // Detail
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.attached-specialities.view')")
    @Operation(
            summary = "Get attached-speciality detail by id",
            description = "Returns a single attachment with resolved names and audit fields.",
            tags = {"Registry - Attached Specialities"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachedSpecialityDetailResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<AttachedSpecialityDetailDto>> getDetail(
            @Parameter(description = "Attachment id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("GET /api/v1/web/registry/attached-specialities/{}", id);
        return ResponseEntity.ok(ResponseWrapper.success(service.getDetail(id)));
    }

    @Schema(name = "AttachedSpecialityDetailResponse")
    static class AttachedSpecialityDetailResponseWrapper extends ResponseWrapper<AttachedSpecialityDetailDto> {
    }

    // =====================================================
    // Create
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('institutions.attached-specialities.create')")
    @Operation(
            summary = "Create attached speciality",
            description = """
                    Attach a classifier speciality to a university.

                    `specialityLevel` (BACHELOR|MASTER|ORDINATURA|DOCTORAL) decides which
                    `_speciality_*` column receives `specialityId`; the other three are NULLed.

                    Rejected with **409 Conflict** if an active identical attachment exists.
                    """,
            tags = {"Registry - Attached Specialities"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachedSpecialityDetailResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.attached-specialities.create'"),
            @ApiResponse(responseCode = "409", description = "Duplicate attachment")
    })
    public ResponseEntity<ResponseWrapper<AttachedSpecialityDetailDto>> create(
            @Valid @RequestBody AttachedSpecialityCreateDto request
    ) {
        log.info("POST /api/v1/web/registry/attached-specialities - university={}, level={}",
                request.universityCode(), request.specialityLevel());
        AttachedSpecialityDetailDto created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    // =====================================================
    // Update
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.attached-specialities.edit')")
    @Operation(
            summary = "Update attached speciality",
            description = "Update an existing attachment. Same duplicate guard (409) as create, excluding self.",
            tags = {"Registry - Attached Specialities"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachedSpecialityDetailResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.attached-specialities.edit'"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate attachment")
    })
    public ResponseEntity<ResponseWrapper<AttachedSpecialityDetailDto>> update(
            @Parameter(description = "Attachment id (UUID)", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AttachedSpecialityUpdateDto request
    ) {
        log.info("PUT /api/v1/web/registry/attached-specialities/{}", id);
        AttachedSpecialityDetailDto updated = service.update(id, request);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // Delete (soft)
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.attached-specialities.delete')")
    @Operation(
            summary = "Delete attached speciality (soft delete)",
            description = "Sets delete_ts instead of physical deletion.",
            tags = {"Registry - Attached Specialities"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.attached-specialities.delete'"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Attachment id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/web/registry/attached-specialities/{}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // Dictionaries
    // =====================================================

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('institutions.attached-specialities.view')")
    @Operation(
            summary = "Get filter/form dictionaries (cached)",
            description = "Universities, education types, education forms, and specialities per level.",
            tags = {"Registry - Attached Specialities"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachedSpecialityDictionariesResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<AttachedSpecialityDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/attached-specialities/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(service.getDictionaries()));
    }

    @Schema(name = "AttachedSpecialityDictionariesResponse")
    static class AttachedSpecialityDictionariesResponseWrapper extends ResponseWrapper<AttachedSpecialityDictionariesDto> {
    }

    // =====================================================
    // Export (CSV, UTF-8 BOM)
    // =====================================================

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('institutions.attached-specialities.view')")
    @Operation(
            summary = "Export attached specialities to CSV",
            description = "Export rows matching the current filters. UTF-8 BOM for Excel compatibility.",
            tags = {"Registry - Attached Specialities"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file",
                    content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<byte[]> export(
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "University code") @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education type code") @RequestParam(required = false) String educationType,
            @Parameter(description = "Education form code") @RequestParam(required = false) String educationForm,
            @Parameter(description = "Active flag filter") @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/attached-specialities/export - q={}, universityCode={}, educationType={}, educationForm={}, active={}",
                q, universityCode, educationType, educationForm, active);

        List<AttachedSpecialityRowDto> rows = service.export(q, universityCode, educationType, educationForm, active);
        byte[] csvBytes = generateCsvFile(rows);

        String filename = "attached_specialities_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(csvBytes.length);

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    private byte[] generateCsvFile(List<AttachedSpecialityRowDto> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append('﻿'); // UTF-8 BOM
        csv.append("OTM kodi,OTM nomi,Ta'lim turi,Ta'lim shakli,Daraja,Mutaxassislik,Holati\n");
        for (AttachedSpecialityRowDto r : rows) {
            csv.append(escapeCsv(r.universityCode())).append(",");
            csv.append(escapeCsv(r.universityName())).append(",");
            csv.append(escapeCsv(r.educationTypeName())).append(",");
            csv.append(escapeCsv(r.educationFormName())).append(",");
            csv.append(escapeCsv(r.specialityLevel())).append(",");
            csv.append(escapeCsv(r.specialityName())).append(",");
            csv.append(Boolean.TRUE.equals(r.active()) ? "Faol" : "Nofaol");
            csv.append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
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
