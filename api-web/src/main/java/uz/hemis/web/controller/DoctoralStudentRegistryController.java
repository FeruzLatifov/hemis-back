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
import uz.hemis.service.registry.DoctoralStudentRegistryService;
import uz.hemis.service.registry.dto.DoctoralStudentDetailDto;
import uz.hemis.service.registry.dto.DoctoralStudentDictionariesDto;
import uz.hemis.service.registry.dto.DoctoralStudentRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Researcher Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/researchers</li>
 *   <li>Frontend route: /science/researchers</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/researchers")
@Tag(name = "Registry - Researchers", description = "Researchers Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DoctoralStudentRegistryController {

    private final DoctoralStudentRegistryService researcherRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('science.researchers.view')")
    @Operation(summary = "Get researchers (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved researchers",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DoctoralStudentRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'science.researchers.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<DoctoralStudentRowDto>>> getResearchers(
            @Parameter(description = "Search (full name, student id number, dissertation theme)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Science branch classifier code")
            @RequestParam(required = false) String scienceBranch,
            @Parameter(description = "Doctoral student type classifier code")
            @RequestParam(required = false) String doctoralStudentType,
            @Parameter(description = "Doctorate student status classifier code")
            @RequestParam(required = false) String status,
            @Parameter(description = "Active flag")
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "acceptedDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/researchers - q={}, universityCode={}, scienceBranch={}, doctoralStudentType={}, status={}, active={}, page={}",
                 q, universityCode, scienceBranch, doctoralStudentType, status, active, pageable.getPageNumber());
        Page<DoctoralStudentRowDto> page = researcherRegistryService.getResearchers(
            q, universityCode, scienceBranch, doctoralStudentType, status, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "DoctoralStudentRowResponse")
    static class DoctoralStudentRowResponseWrapper extends ResponseWrapper<PageResponse<DoctoralStudentRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('science.researchers.view')")
    @Operation(summary = "Get researcher detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved researcher detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DoctoralStudentDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Researcher not found")
    })
    public ResponseEntity<ResponseWrapper<DoctoralStudentDetailDto>> getResearcherDetail(
            @Parameter(description = "Researcher id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/researchers/{}", id);
        return researcherRegistryService.getResearcherDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "DoctoralStudentDetailResponse")
    static class DoctoralStudentDetailResponseWrapper extends ResponseWrapper<DoctoralStudentDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('science.researchers.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DoctoralStudentDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<DoctoralStudentDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/researchers/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(researcherRegistryService.getDictionaries()));
    }

    @Schema(name = "DoctoralStudentDictionariesResponse")
    static class DoctoralStudentDictionariesResponseWrapper extends ResponseWrapper<DoctoralStudentDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('science.researchers.view')")
    @Operation(summary = "Export researchers to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportResearchers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) String scienceBranch,
            @RequestParam(required = false) String doctoralStudentType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/researchers/export - q={}, universityCode={}", q, universityCode);

        Page<DoctoralStudentRowDto> page = researcherRegistryService.getResearchers(
            q, universityCode, scienceBranch, doctoralStudentType, status, active, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("F.I.O,Talaba ID raqami,OTM,Fan sohasi,Doktorant turi,Holati,Qabul sanasi,Holat\n");
        for (DoctoralStudentRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.fullName())).append(",");
            csv.append(escapeCsv(d.studentIdNumber())).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.scienceBranchName())).append(",");
            csv.append(escapeCsv(d.doctoralStudentTypeName())).append(",");
            csv.append(escapeCsv(d.statusName())).append(",");
            csv.append(escapeCsv(str(d.acceptedDate()))).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "researchers_" + timestamp() + ".csv";
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
