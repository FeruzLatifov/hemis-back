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
import uz.hemis.service.registry.CertificateRegistryService;
import uz.hemis.service.registry.dto.CertificateDetailDto;
import uz.hemis.service.registry.dto.CertificateDictionariesDto;
import uz.hemis.service.registry.dto.CertificateRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Certificate Registry Controller - READ-ONLY frontend UI API for the Certificates registry.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/certificates</li>
 *   <li>Frontend route: /students/certificates</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/certificates")
@Tag(name = "Registry - Certificates", description = "Certificates Registry API (Sertifikatlar Reestri) — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CertificateRegistryController {

    private final CertificateRegistryService certificateRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('students.certificates.view')")
    @Operation(summary = "Get certificates (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved certificates",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CertificateRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'students.certificates.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<CertificateRowDto>>> getCertificates(
            @Parameter(description = "Search (serial number, student name)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Certificate type classifier code")
            @RequestParam(required = false) String certificateType,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "issueDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/certificates - q={}, universityCode={}, certificateType={}, page={}",
                 q, universityCode, certificateType, pageable.getPageNumber());
        Page<CertificateRowDto> page = certificateRegistryService.getCertificates(
            q, universityCode, certificateType, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "CertificateRowResponse")
    static class CertificateRowResponseWrapper extends ResponseWrapper<PageResponse<CertificateRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('students.certificates.view')")
    @Operation(summary = "Get certificate detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved certificate detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CertificateDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    public ResponseEntity<ResponseWrapper<CertificateDetailDto>> getCertificateDetail(
            @Parameter(description = "Certificate id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/certificates/{}", id);
        return certificateRegistryService.getCertificateDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "CertificateDetailResponse")
    static class CertificateDetailResponseWrapper extends ResponseWrapper<CertificateDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('students.certificates.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CertificateDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<CertificateDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/certificates/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(certificateRegistryService.getDictionaries()));
    }

    @Schema(name = "CertificateDictionariesResponse")
    static class CertificateDictionariesResponseWrapper extends ResponseWrapper<CertificateDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('students.certificates.view')")
    @Operation(summary = "Export certificates to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportCertificates(
            @Parameter(description = "Search (serial number, student name)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Certificate type classifier code")
            @RequestParam(required = false) String certificateType
    ) {
        log.info("POST /api/v1/web/registry/certificates/export - q={}, universityCode={}, certificateType={}",
                 q, universityCode, certificateType);

        Page<CertificateRowDto> page = certificateRegistryService.getCertificates(
            q, universityCode, certificateType, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Talaba,OTM,Sertifikat turi,Sertifikat nomi,Baho,Seriya raqami,Berilgan sana,Amal qilish sanasi,Holati\n");
        for (CertificateRowDto c : page.getContent()) {
            csv.append(escapeCsv(c.studentName())).append(",");
            csv.append(escapeCsv(c.universityName())).append(",");
            csv.append(escapeCsv(c.certificateTypeName())).append(",");
            csv.append(escapeCsv(c.certificateNameLabel())).append(",");
            csv.append(escapeCsv(c.certificateGradeName())).append(",");
            csv.append(escapeCsv(c.serialNumber())).append(",");
            csv.append(escapeCsv(str(c.issueDate()))).append(",");
            csv.append(escapeCsv(str(c.validDate()))).append(",");
            csv.append(Boolean.TRUE.equals(c.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "certificates_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
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
