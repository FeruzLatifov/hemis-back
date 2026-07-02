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
import uz.hemis.service.registry.UniversitySpecialityRegistryService;
import uz.hemis.service.registry.dto.SpecialityDetailDto;
import uz.hemis.service.registry.dto.SpecialityDictionariesDto;
import uz.hemis.service.registry.dto.SpecialityRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * University Speciality Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/university-specialities</li>
 *   <li>Frontend route: /institutions/university-specialities</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/university-specialities")
@Tag(name = "Registry - University Specialities", description = "University Specialities Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UniversitySpecialityRegistryController {

    private final UniversitySpecialityRegistryService universitySpecialityRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('institutions.university-specialities.view')")
    @Operation(summary = "Get university specialities (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved specialities",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SpecialityRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'institutions.university-specialities.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialityRowDto>>> getSpecialities(
            @Parameter(description = "Search (speciality code, speciality name)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Education type classifier code")
            @RequestParam(required = false) String educationType,
            @Parameter(description = "Active status")
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "specialityName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/university-specialities - q={}, universityCode={}, educationType={}, active={}, page={}",
                 q, universityCode, educationType, active, pageable.getPageNumber());
        Page<SpecialityRowDto> page = universitySpecialityRegistryService.getSpecialities(q, universityCode, educationType, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "UniversitySpecialityRowResponse")
    static class SpecialityRowResponseWrapper extends ResponseWrapper<PageResponse<SpecialityRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.university-specialities.view')")
    @Operation(summary = "Get university speciality detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SpecialityDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Speciality not found")
    })
    public ResponseEntity<ResponseWrapper<SpecialityDetailDto>> getSpecialityDetail(
            @Parameter(description = "Speciality id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/university-specialities/{}", id);
        return universitySpecialityRegistryService.getSpecialityDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "UniversitySpecialityDetailResponse")
    static class SpecialityDetailResponseWrapper extends ResponseWrapper<SpecialityDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('institutions.university-specialities.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SpecialityDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<SpecialityDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/university-specialities/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(universitySpecialityRegistryService.getDictionaries()));
    }

    @Schema(name = "UniversitySpecialityDictionariesResponse")
    static class SpecialityDictionariesResponseWrapper extends ResponseWrapper<SpecialityDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('institutions.university-specialities.view')")
    @Operation(summary = "Export university specialities to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportSpecialities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) String educationType,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/university-specialities/export - q={}, universityCode={}, educationType={}, active={}",
                 q, universityCode, educationType, active);

        Page<SpecialityRowDto> page = universitySpecialityRegistryService.getSpecialities(
            q, universityCode, educationType, active, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Mutaxassislik kodi,Mutaxassislik nomi,OTM,Ta'lim turi,O'quv yili,Faol\n");
        for (SpecialityRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.specialityCode())).append(",");
            csv.append(escapeCsv(d.specialityName())).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.educationTypeName())).append(",");
            csv.append(escapeCsv(d.educationYear())).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "university_specialities_" + timestamp() + ".csv";
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
