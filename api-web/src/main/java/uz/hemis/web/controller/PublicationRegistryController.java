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
import uz.hemis.service.registry.PublicationRegistryService;
import uz.hemis.service.registry.dto.PublicationDetailDto;
import uz.hemis.service.registry.dto.PublicationDictionariesDto;
import uz.hemis.service.registry.dto.PublicationRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Scientific Publication Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/publications</li>
 *   <li>Frontend route: /science/publications</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/publications")
@Tag(name = "Registry - Scientific Publications", description = "Scientific Publications Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicationRegistryController {

    private final PublicationRegistryService publicationRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('science.publications.view')")
    @Operation(summary = "Get scientific publications (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved publications",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PublicationRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'science.publications.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<PublicationRowDto>>> getPublications(
            @Parameter(description = "Search (name, authors, source name, doi)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Publication type classifier code")
            @RequestParam(required = false) String publicationType,
            @Parameter(description = "Issue year")
            @RequestParam(required = false) Integer issueYear,
            @Parameter(description = "Active flag")
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "issueYear", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/publications - q={}, universityCode={}, publicationType={}, issueYear={}, active={}, page={}",
                 q, universityCode, publicationType, issueYear, active, pageable.getPageNumber());
        Page<PublicationRowDto> page = publicationRegistryService.getPublications(
            q, universityCode, publicationType, issueYear, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "PublicationRowResponse")
    static class PublicationRowResponseWrapper extends ResponseWrapper<PageResponse<PublicationRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('science.publications.view')")
    @Operation(summary = "Get scientific publication detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved publication detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PublicationDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Publication not found")
    })
    public ResponseEntity<ResponseWrapper<PublicationDetailDto>> getPublicationDetail(
            @Parameter(description = "Publication id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/publications/{}", id);
        return publicationRegistryService.getPublicationDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "PublicationDetailResponse")
    static class PublicationDetailResponseWrapper extends ResponseWrapper<PublicationDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('science.publications.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PublicationDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<PublicationDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/publications/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(publicationRegistryService.getDictionaries()));
    }

    @Schema(name = "PublicationDictionariesResponse")
    static class PublicationDictionariesResponseWrapper extends ResponseWrapper<PublicationDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('science.publications.view')")
    @Operation(summary = "Export scientific publications to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportPublications(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) String publicationType,
            @RequestParam(required = false) Integer issueYear,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/publications/export - q={}, universityCode={}", q, universityCode);

        Page<PublicationRowDto> page = publicationRegistryService.getPublications(
            q, universityCode, publicationType, issueYear, active, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Nomi,Mualliflar,Mualliflar soni,Manba,Nashr yili,OTM,Nashr turi,DOI,Holat\n");
        for (PublicationRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.name())).append(",");
            csv.append(escapeCsv(d.authors())).append(",");
            csv.append(escapeCsv(str(d.authorCounts()))).append(",");
            csv.append(escapeCsv(d.sourceName())).append(",");
            csv.append(escapeCsv(str(d.issueYear()))).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.publicationTypeName())).append(",");
            csv.append(escapeCsv(d.doi())).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "publications_" + timestamp() + ".csv";
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
