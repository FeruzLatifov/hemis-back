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
import uz.hemis.service.registry.DiplomaRegistryService;
import uz.hemis.service.registry.dto.DiplomaDetailDto;
import uz.hemis.service.registry.dto.DiplomaDictionariesDto;
import uz.hemis.service.registry.dto.DiplomaRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Diploma Registry Controller - READ-ONLY frontend UI API for the Diplomas registry.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/diplomas</li>
 *   <li>Frontend route: /students/diplomas</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/diplomas")
@Tag(name = "Registry - Diplomas", description = "Diplomas Registry API (Diplomlar Reestri) — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DiplomaRegistryController {

    private final DiplomaRegistryService diplomaRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('students.diplomas.view')")
    @Operation(summary = "Get diplomas (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved diplomas",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiplomaRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'students.diplomas.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaRowDto>>> getDiplomas(
            @Parameter(description = "Search (diploma number, register number, student name)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education year classifier code")
            @RequestParam(required = false) String educationYear,
            @Parameter(description = "Verify value")
            @RequestParam(required = false) String verify,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "registerDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/diplomas - q={}, universityCode={}, educationYear={}, verify={}, page={}",
                 q, universityCode, educationYear, verify, pageable.getPageNumber());
        Page<DiplomaRowDto> page = diplomaRegistryService.getDiplomas(q, universityCode, educationYear, verify, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "DiplomaRowResponse")
    static class DiplomaRowResponseWrapper extends ResponseWrapper<PageResponse<DiplomaRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('students.diplomas.view')")
    @Operation(summary = "Get diploma detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved diploma detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiplomaDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Diploma not found")
    })
    public ResponseEntity<ResponseWrapper<DiplomaDetailDto>> getDiplomaDetail(
            @Parameter(description = "Diploma id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/diplomas/{}", id);
        return diplomaRegistryService.getDiplomaDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "DiplomaDetailResponse")
    static class DiplomaDetailResponseWrapper extends ResponseWrapper<DiplomaDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('students.diplomas.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiplomaDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<DiplomaDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/diplomas/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(diplomaRegistryService.getDictionaries()));
    }

    @Schema(name = "DiplomaDictionariesResponse")
    static class DiplomaDictionariesResponseWrapper extends ResponseWrapper<DiplomaDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('students.diplomas.view')")
    @Operation(summary = "Export diplomas to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportDiplomas(
            @Parameter(description = "Search (diploma number, register number, student name)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education year classifier code")
            @RequestParam(required = false) String educationYear,
            @Parameter(description = "Verify value")
            @RequestParam(required = false) String verify
    ) {
        log.info("POST /api/v1/web/registry/diplomas/export - q={}, universityCode={}, educationYear={}, verify={}",
                 q, universityCode, educationYear, verify);

        Page<DiplomaRowDto> page = diplomaRegistryService.getDiplomas(
            q, universityCode, educationYear, verify, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Diplom raqami,Reestr raqami,Reestr sanasi,Talaba,OTM,Mutaxassislik,O'quv yili,Bitirgan sana,O'rtacha baho,Tasdiq,Holati\n");
        for (DiplomaRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.diplomaNumber())).append(",");
            csv.append(escapeCsv(d.registerNumber())).append(",");
            csv.append(escapeCsv(str(d.registerDate()))).append(",");
            csv.append(escapeCsv(d.studentName())).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.specialityName())).append(",");
            csv.append(escapeCsv(d.educationYear())).append(",");
            csv.append(escapeCsv(str(d.graduationDate()))).append(",");
            csv.append(escapeCsv(d.avgGrade())).append(",");
            csv.append(escapeCsv(d.verify())).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "diplomas_" + timestamp() + ".csv";
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
