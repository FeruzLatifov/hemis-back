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
import uz.hemis.service.registry.ProjectRegistryService;
import uz.hemis.service.registry.dto.ProjectDetailDto;
import uz.hemis.service.registry.dto.ProjectDictionariesDto;
import uz.hemis.service.registry.dto.ProjectRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Scientific Project Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/scientific-projects</li>
 *   <li>Frontend route: /science/projects</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/scientific-projects")
@Tag(name = "Registry - Scientific Projects", description = "Scientific Projects Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProjectRegistryController {

    private final ProjectRegistryService projectRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('science.projects.view')")
    @Operation(summary = "Get scientific projects (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved projects",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProjectRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'science.projects.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<ProjectRowDto>>> getProjects(
            @Parameter(description = "Search (name, project number, contract number)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Project type classifier code")
            @RequestParam(required = false) String projectType,
            @Parameter(description = "Active flag")
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "contractDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/scientific-projects - q={}, universityCode={}, projectType={}, active={}, page={}",
                 q, universityCode, projectType, active, pageable.getPageNumber());
        Page<ProjectRowDto> page = projectRegistryService.getProjects(q, universityCode, projectType, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "ProjectRowResponse")
    static class ProjectRowResponseWrapper extends ResponseWrapper<PageResponse<ProjectRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('science.projects.view')")
    @Operation(summary = "Get scientific project detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved project detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProjectDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ResponseWrapper<ProjectDetailDto>> getProjectDetail(
            @Parameter(description = "Project id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/scientific-projects/{}", id);
        return projectRegistryService.getProjectDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "ProjectDetailResponse")
    static class ProjectDetailResponseWrapper extends ResponseWrapper<ProjectDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('science.projects.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProjectDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<ProjectDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/scientific-projects/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(projectRegistryService.getDictionaries()));
    }

    @Schema(name = "ProjectDictionariesResponse")
    static class ProjectDictionariesResponseWrapper extends ResponseWrapper<ProjectDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('science.projects.view')")
    @Operation(summary = "Export scientific projects to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportProjects(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/scientific-projects/export - q={}, universityCode={}", q, universityCode);

        Page<ProjectRowDto> page = projectRegistryService.getProjects(
            q, universityCode, projectType, active, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Nomi,Loyiha raqami,OTM,Loyiha turi,Shartnoma raqami,Shartnoma sanasi,Boshlanish,Tugash,Holat\n");
        for (ProjectRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.name())).append(",");
            csv.append(escapeCsv(d.projectNumber())).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.projectTypeName())).append(",");
            csv.append(escapeCsv(d.contractNumber())).append(",");
            csv.append(escapeCsv(str(d.contractDate()))).append(",");
            csv.append(escapeCsv(str(d.startDate()))).append(",");
            csv.append(escapeCsv(str(d.endDate()))).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "scientific_projects_" + timestamp() + ".csv";
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
