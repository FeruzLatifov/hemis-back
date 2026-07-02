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
import uz.hemis.service.registry.PublicationPropertyRegistryService;
import uz.hemis.service.registry.dto.PublicationPropertyDetailDto;
import uz.hemis.service.registry.dto.PublicationPropertyDictionariesDto;
import uz.hemis.service.registry.dto.PublicationPropertyRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Intellectual Property Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/intellectual</li>
 *   <li>Frontend route: /science/intellectual</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/intellectual")
@Tag(name = "Registry - Intellectual Property", description = "Intellectual Property Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicationPropertyRegistryController {

    private final PublicationPropertyRegistryService publicationPropertyRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('science.intellectual.view')")
    @Operation(summary = "Get intellectual property (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved intellectual property",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PublicationPropertyRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'science.intellectual.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<PublicationPropertyRowDto>>> getProperties(
            @Parameter(description = "Search (name, authors, numbers)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Patent type code")
            @RequestParam(required = false) String patentType,
            @Parameter(description = "Active status")
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "propertyDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/intellectual - q={}, universityCode={}, patentType={}, active={}, page={}",
                 q, universityCode, patentType, active, pageable.getPageNumber());
        Page<PublicationPropertyRowDto> page = publicationPropertyRegistryService.getProperties(q, universityCode, patentType, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "PublicationPropertyRowResponse")
    static class PublicationPropertyRowResponseWrapper extends ResponseWrapper<PageResponse<PublicationPropertyRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('science.intellectual.view')")
    @Operation(summary = "Get intellectual property detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PublicationPropertyDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<PublicationPropertyDetailDto>> getPropertyDetail(
            @Parameter(description = "Property id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/intellectual/{}", id);
        return publicationPropertyRegistryService.getPropertyDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "PublicationPropertyDetailResponse")
    static class PublicationPropertyDetailResponseWrapper extends ResponseWrapper<PublicationPropertyDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('science.intellectual.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PublicationPropertyDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<PublicationPropertyDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/intellectual/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(publicationPropertyRegistryService.getDictionaries()));
    }

    @Schema(name = "PublicationPropertyDictionariesResponse")
    static class PublicationPropertyDictionariesResponseWrapper extends ResponseWrapper<PublicationPropertyDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('science.intellectual.view')")
    @Operation(summary = "Export intellectual property to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportProperties(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) String patentType,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/intellectual/export - q={}, universityCode={}, patentType={}, active={}",
                 q, universityCode, patentType, active);

        Page<PublicationPropertyRowDto> page = publicationPropertyRegistryService.getProperties(
            q, universityCode, patentType, active, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Nomi,Mualliflar,Muallif soni,OTM,Patent turi,Raqamlar,Sana,Davlat,Faol\n");
        for (PublicationPropertyRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.name())).append(",");
            csv.append(escapeCsv(d.authors())).append(",");
            csv.append(escapeCsv(d.authorCounts() != null ? d.authorCounts().toString() : "")).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.patentTypeName())).append(",");
            csv.append(escapeCsv(d.numbers())).append(",");
            csv.append(escapeCsv(str(d.propertyDate()))).append(",");
            csv.append(escapeCsv(d.countryCode())).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "intellectual_property_" + timestamp() + ".csv";
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
