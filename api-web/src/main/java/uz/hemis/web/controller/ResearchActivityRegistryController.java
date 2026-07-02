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
import uz.hemis.service.registry.ResearchActivityRegistryService;
import uz.hemis.service.registry.dto.ResearchActivityDetailDto;
import uz.hemis.service.registry.dto.ResearchActivityDictionariesDto;
import uz.hemis.service.registry.dto.ResearchActivityRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Research Activity Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/research-activity</li>
 *   <li>Frontend route: /science/research-activity</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/research-activity")
@Tag(name = "Registry - Research Activity", description = "Research Activity Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ResearchActivityRegistryController {

    private final ResearchActivityRegistryService researchActivityRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('science.research-activity.view')")
    @Operation(summary = "Get research activities (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved research activities",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ResearchActivityRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'science.research-activity.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<ResearchActivityRowDto>>> getActivities(
            @Parameter(description = "Search (link, h-index)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education year classifier code")
            @RequestParam(required = false) String educationYear,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "educationYear", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/research-activity - q={}, universityCode={}, educationYear={}, page={}",
                 q, universityCode, educationYear, pageable.getPageNumber());
        Page<ResearchActivityRowDto> page = researchActivityRegistryService.getActivities(q, universityCode, educationYear, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "ResearchActivityRowResponse")
    static class ResearchActivityRowResponseWrapper extends ResponseWrapper<PageResponse<ResearchActivityRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('science.research-activity.view')")
    @Operation(summary = "Get research activity detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ResearchActivityDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<ResearchActivityDetailDto>> getActivityDetail(
            @Parameter(description = "Research activity id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/research-activity/{}", id);
        return researchActivityRegistryService.getActivityDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "ResearchActivityDetailResponse")
    static class ResearchActivityDetailResponseWrapper extends ResponseWrapper<ResearchActivityDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('science.research-activity.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ResearchActivityDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<ResearchActivityDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/research-activity/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(researchActivityRegistryService.getDictionaries()));
    }

    @Schema(name = "ResearchActivityDictionariesResponse")
    static class ResearchActivityDictionariesResponseWrapper extends ResponseWrapper<ResearchActivityDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('science.research-activity.view')")
    @Operation(summary = "Export research activities to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportActivities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) String educationYear
    ) {
        log.info("POST /api/v1/web/registry/research-activity/export - q={}, universityCode={}, educationYear={}",
                 q, universityCode, educationYear);

        Page<ResearchActivityRowDto> page = researchActivityRegistryService.getActivities(
            q, universityCode, educationYear, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("OTM,O'quv yili,Ilmiy baza,h-indeks,Ilmiy ishlar soni,Iqtiboslar soni,Havola\n");
        for (ResearchActivityRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.educationYear())).append(",");
            csv.append(escapeCsv(d.scholarDatabaseName())).append(",");
            csv.append(escapeCsv(d.hIndex())).append(",");
            csv.append(escapeCsv(d.scientificWorkCount())).append(",");
            csv.append(escapeCsv(d.referenceCount())).append(",");
            csv.append(escapeCsv(d.link())).append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "research_activity_" + timestamp() + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private static String timestamp() {
        return java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
