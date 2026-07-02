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
import uz.hemis.service.registry.DissertationDefenseRegistryService;
import uz.hemis.service.registry.dto.DissertationDefenseDetailDto;
import uz.hemis.service.registry.dto.DissertationDefenseDictionariesDto;
import uz.hemis.service.registry.dto.DissertationDefenseRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dissertation Defense Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/dissertation-defense</li>
 *   <li>Frontend route: /science/dissertation-defense</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/dissertation-defense")
@Tag(name = "Registry - Dissertation Defense", description = "Dissertation Defense Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DissertationDefenseRegistryController {

    private final DissertationDefenseRegistryService dissertationDefenseRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('science.dissertation-defense.view')")
    @Operation(summary = "Get dissertation defenses (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved defenses",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DissertationDefenseRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'science.dissertation-defense.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<DissertationDefenseRowDto>>> getDefenses(
            @Parameter(description = "Search (diploma number, register number, student name)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Active status")
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "defenseDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/dissertation-defense - q={}, universityCode={}, active={}, page={}",
                 q, universityCode, active, pageable.getPageNumber());
        Page<DissertationDefenseRowDto> page = dissertationDefenseRegistryService.getDefenses(q, universityCode, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "DissertationDefenseRowResponse")
    static class DissertationDefenseRowResponseWrapper extends ResponseWrapper<PageResponse<DissertationDefenseRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('science.dissertation-defense.view')")
    @Operation(summary = "Get dissertation defense detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DissertationDefenseDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Defense not found")
    })
    public ResponseEntity<ResponseWrapper<DissertationDefenseDetailDto>> getDefenseDetail(
            @Parameter(description = "Defense id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/dissertation-defense/{}", id);
        return dissertationDefenseRegistryService.getDefenseDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "DissertationDefenseDetailResponse")
    static class DissertationDefenseDetailResponseWrapper extends ResponseWrapper<DissertationDefenseDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('science.dissertation-defense.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DissertationDefenseDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<DissertationDefenseDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/dissertation-defense/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(dissertationDefenseRegistryService.getDictionaries()));
    }

    @Schema(name = "DissertationDefenseDictionariesResponse")
    static class DissertationDefenseDictionariesResponseWrapper extends ResponseWrapper<DissertationDefenseDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('science.dissertation-defense.view')")
    @Operation(summary = "Export dissertation defenses to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportDefenses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/dissertation-defense/export - q={}, universityCode={}, active={}",
                 q, universityCode, active);

        Page<DissertationDefenseRowDto> page = dissertationDefenseRegistryService.getDefenses(
            q, universityCode, active, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Talaba,OTM,Mutaxassislik,Himoya sanasi,Diplom raqami,Reestr raqami,Tasdiqlangan sana,Faol\n");
        for (DissertationDefenseRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.studentName())).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.specialityCode())).append(",");
            csv.append(escapeCsv(str(d.defenseDate()))).append(",");
            csv.append(escapeCsv(d.diplomaNumber())).append(",");
            csv.append(escapeCsv(d.registerNumber())).append(",");
            csv.append(escapeCsv(str(d.approvedDate()))).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "dissertation_defense_" + timestamp() + ".csv";
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
