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
import uz.hemis.service.registry.ScholarshipRegistryService;
import uz.hemis.service.registry.dto.ScholarshipDetailDto;
import uz.hemis.service.registry.dto.ScholarshipDictionariesDto;
import uz.hemis.service.registry.dto.ScholarshipRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scholarship Registry Controller - READ-ONLY frontend UI API for the Scholarships registry.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/scholarships</li>
 *   <li>Frontend route: /students/scholarships</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/scholarships")
@Tag(name = "Registry - Scholarships", description = "Scholarships Registry API (Stipendiyalar Reestri) — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ScholarshipRegistryController {

    private final ScholarshipRegistryService scholarshipRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('students.scholarships.view')")
    @Operation(summary = "Get scholarships (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved scholarships",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ScholarshipRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'students.scholarships.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<ScholarshipRowDto>>> getScholarships(
            @Parameter(description = "Search (student name, decree)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education year classifier code")
            @RequestParam(required = false) String educationYear,
            @Parameter(description = "Stipend category classifier code")
            @RequestParam(required = false) String stipendCategory,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/scholarships - q={}, universityCode={}, educationYear={}, stipendCategory={}, page={}",
                 q, universityCode, educationYear, stipendCategory, pageable.getPageNumber());
        Page<ScholarshipRowDto> page = scholarshipRegistryService.getScholarships(
            q, universityCode, educationYear, stipendCategory, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "ScholarshipRowResponse")
    static class ScholarshipRowResponseWrapper extends ResponseWrapper<PageResponse<ScholarshipRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('students.scholarships.view')")
    @Operation(summary = "Get scholarship detail by id (with monthly amounts)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved scholarship detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ScholarshipDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Scholarship not found")
    })
    public ResponseEntity<ResponseWrapper<ScholarshipDetailDto>> getScholarshipDetail(
            @Parameter(description = "Scholarship id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/scholarships/{}", id);
        return scholarshipRegistryService.getScholarshipDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "ScholarshipDetailResponse")
    static class ScholarshipDetailResponseWrapper extends ResponseWrapper<ScholarshipDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('students.scholarships.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ScholarshipDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<ScholarshipDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/scholarships/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(scholarshipRegistryService.getDictionaries()));
    }

    @Schema(name = "ScholarshipDictionariesResponse")
    static class ScholarshipDictionariesResponseWrapper extends ResponseWrapper<ScholarshipDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('students.scholarships.view')")
    @Operation(summary = "Export scholarships to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportScholarships(
            @Parameter(description = "Search (student name, decree)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education year classifier code")
            @RequestParam(required = false) String educationYear,
            @Parameter(description = "Stipend category classifier code")
            @RequestParam(required = false) String stipendCategory
    ) {
        log.info("POST /api/v1/web/registry/scholarships/export - q={}, universityCode={}, educationYear={}, stipendCategory={}",
                 q, universityCode, educationYear, stipendCategory);

        Page<ScholarshipRowDto> page = scholarshipRegistryService.getScholarships(
            q, universityCode, educationYear, stipendCategory, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Talaba,OTM,O'quv yili,Semestr,Stipendiya toifasi,Stipendiya turi,To'lov shakli,Buyruq,Boshlanish,Tugash,Holati\n");
        for (ScholarshipRowDto s : page.getContent()) {
            csv.append(escapeCsv(s.studentName())).append(",");
            csv.append(escapeCsv(s.universityName())).append(",");
            csv.append(escapeCsv(s.educationYear())).append(",");
            csv.append(escapeCsv(s.semesterNumber())).append(",");
            csv.append(escapeCsv(s.stipendCategory())).append(",");
            csv.append(escapeCsv(s.stipendType())).append(",");
            csv.append(escapeCsv(s.paymentForm())).append(",");
            csv.append(escapeCsv(s.decree())).append(",");
            csv.append(escapeCsv(str(s.startDate()))).append(",");
            csv.append(escapeCsv(str(s.endDate()))).append(",");
            csv.append(Boolean.TRUE.equals(s.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "scholarships_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
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
