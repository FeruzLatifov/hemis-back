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
import uz.hemis.service.registry.MethodicalRegistryService;
import uz.hemis.service.registry.dto.MethodicalDetailDto;
import uz.hemis.service.registry.dto.MethodicalDictionariesDto;
import uz.hemis.service.registry.dto.MethodicalRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Methodical Publication Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/methodical</li>
 *   <li>Frontend route: /science/methodical</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/methodical")
@Tag(name = "Registry - Methodical Publications", description = "Methodical Publications Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MethodicalRegistryController {

    private final MethodicalRegistryService methodicalRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('science.methodical.view')")
    @Operation(summary = "Get methodical publications (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved methodical publications",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MethodicalRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'science.methodical.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<MethodicalRowDto>>> getMethodicals(
            @Parameter(description = "Search (name, authors, publisher)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Methodical type classifier code")
            @RequestParam(required = false) String methodicalType,
            @Parameter(description = "Issue year")
            @RequestParam(required = false) Integer issueYear,
            @Parameter(description = "Active flag")
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "issueYear", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/methodical - q={}, universityCode={}, methodicalType={}, issueYear={}, active={}, page={}",
                 q, universityCode, methodicalType, issueYear, active, pageable.getPageNumber());
        Page<MethodicalRowDto> page = methodicalRegistryService.getMethodicals(
            q, universityCode, methodicalType, issueYear, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "MethodicalRowResponse")
    static class MethodicalRowResponseWrapper extends ResponseWrapper<PageResponse<MethodicalRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('science.methodical.view')")
    @Operation(summary = "Get methodical publication detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved methodical publication detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MethodicalDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Methodical publication not found")
    })
    public ResponseEntity<ResponseWrapper<MethodicalDetailDto>> getMethodicalDetail(
            @Parameter(description = "Methodical publication id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/methodical/{}", id);
        return methodicalRegistryService.getMethodicalDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "MethodicalDetailResponse")
    static class MethodicalDetailResponseWrapper extends ResponseWrapper<MethodicalDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('science.methodical.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MethodicalDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<MethodicalDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/methodical/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(methodicalRegistryService.getDictionaries()));
    }

    @Schema(name = "MethodicalDictionariesResponse")
    static class MethodicalDictionariesResponseWrapper extends ResponseWrapper<MethodicalDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('science.methodical.view')")
    @Operation(summary = "Export methodical publications to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportMethodicals(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) String methodicalType,
            @RequestParam(required = false) Integer issueYear,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/methodical/export - q={}, universityCode={}", q, universityCode);

        Page<MethodicalRowDto> page = methodicalRegistryService.getMethodicals(
            q, universityCode, methodicalType, issueYear, active, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Nomi,Mualliflar,Mualliflar soni,Nashriyot,Nashr yili,Manba,OTM,Uslubiy tur,Holat\n");
        for (MethodicalRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.name())).append(",");
            csv.append(escapeCsv(d.authors())).append(",");
            csv.append(escapeCsv(str(d.authorCounts()))).append(",");
            csv.append(escapeCsv(d.publisher())).append(",");
            csv.append(escapeCsv(str(d.issueYear()))).append(",");
            csv.append(escapeCsv(d.sourceName())).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.methodicalTypeName())).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "methodical_" + timestamp() + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private static String timestamp() {
        return java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private static String str(Integer i) {
        return i != null ? i.toString() : "";
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
