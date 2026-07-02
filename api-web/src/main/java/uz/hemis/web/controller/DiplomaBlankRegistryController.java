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
import uz.hemis.service.registry.DiplomaBlankRegistryService;
import uz.hemis.service.registry.dto.DiplomaBlankDetailDto;
import uz.hemis.service.registry.dto.DiplomaBlankDictionariesDto;
import uz.hemis.service.registry.dto.DiplomaBlankRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Diploma Blank Registry Controller — READ-ONLY frontend UI API for the Diploma-blanks registry.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/diploma-blanks</li>
 *   <li>Frontend route: /institutions/diploma-blanks</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/diploma-blanks")
@Tag(name = "Registry - Diploma Blanks", description = "Diploma Blanks Registry API (Diplom blankalari) — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DiplomaBlankRegistryController {

    private final DiplomaBlankRegistryService service;

    @GetMapping
    @PreAuthorize("hasAuthority('institutions.diploma-blanks.view')")
    @Operation(summary = "Get diploma blanks (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved diploma blanks",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiplomaBlankRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.diploma-blanks.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaBlankRowDto>>> getBlanks(
            @Parameter(description = "Search (blank code, series, number)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Status code")
            @RequestParam(required = false) String status,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "receivedDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/diploma-blanks - q={}, universityCode={}, status={}, page={}",
                 q, universityCode, status, pageable.getPageNumber());
        Page<DiplomaBlankRowDto> page = service.getBlanks(q, universityCode, status, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "DiplomaBlankRowResponse")
    static class DiplomaBlankRowResponseWrapper extends ResponseWrapper<PageResponse<DiplomaBlankRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.diploma-blanks.view')")
    @Operation(summary = "Get diploma-blank detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiplomaBlankDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Diploma blank not found")
    })
    public ResponseEntity<ResponseWrapper<DiplomaBlankDetailDto>> getBlankDetail(
            @Parameter(description = "Diploma-blank id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/diploma-blanks/{}", id);
        return service.getBlankDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "DiplomaBlankDetailResponse")
    static class DiplomaBlankDetailResponseWrapper extends ResponseWrapper<DiplomaBlankDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('institutions.diploma-blanks.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiplomaBlankDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<DiplomaBlankDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/diploma-blanks/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(service.getDictionaries()));
    }

    @Schema(name = "DiplomaBlankDictionariesResponse")
    static class DiplomaBlankDictionariesResponseWrapper extends ResponseWrapper<DiplomaBlankDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('institutions.diploma-blanks.view')")
    @Operation(summary = "Export diploma blanks to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportBlanks(
            @Parameter(description = "Search (blank code, series, number)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Status code")
            @RequestParam(required = false) String status
    ) {
        log.info("POST /api/v1/web/registry/diploma-blanks/export - q={}, universityCode={}, status={}",
                 q, universityCode, status);

        Page<DiplomaBlankRowDto> page = service.getBlanks(q, universityCode, status, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Blank kodi,Seriya,Raqam,OTM,Blank turi,Holati,Qabul sanasi,Berilgan sana,O'quv yili,Faol\n");
        for (DiplomaBlankRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.blankCode())).append(",");
            csv.append(escapeCsv(d.series())).append(",");
            csv.append(escapeCsv(d.number())).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.blankType())).append(",");
            csv.append(escapeCsv(d.statusCode())).append(",");
            csv.append(escapeCsv(str(d.receivedDate()))).append(",");
            csv.append(escapeCsv(str(d.issuedDate()))).append(",");
            csv.append(escapeCsv(d.academicYear() != null ? d.academicYear().toString() : "")).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "diploma_blanks_" + timestamp() + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private static String timestamp() {
        return java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private static String str(LocalDate d) {
        return d != null ? d.toString() : "";
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
