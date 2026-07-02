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
import uz.hemis.service.registry.EmployeeJobsRegistryService;
import uz.hemis.service.registry.dto.EmployeeJobsDetailDto;
import uz.hemis.service.registry.dto.EmployeeJobsDictionariesDto;
import uz.hemis.service.registry.dto.EmployeeJobsRowDto;
import uz.hemis.service.util.PageResponses;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Employee Jobs Registry Controller - READ-ONLY frontend UI API.
 *
 * <ul>
 *   <li>URL: /api/v1/web/registry/employee-jobs</li>
 *   <li>Frontend route: /teachers/employee-jobs</li>
 *   <li>Endpoints (GET + export only — NO mutations): list, detail, dictionaries, export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/employee-jobs")
@Tag(name = "Registry - Employee Jobs", description = "Employee Jobs Registry API — READ-ONLY")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EmployeeJobsRegistryController {

    private final EmployeeJobsRegistryService employeeJobsRegistryService;

    @GetMapping
    @PreAuthorize("hasAuthority('teachers.employee-jobs.view')")
    @Operation(summary = "Get employee jobs (paged, filtered)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved employee jobs",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = EmployeeJobsRowResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'teachers.employee-jobs.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<EmployeeJobsRowDto>>> getEmployeeJobs(
            @Parameter(description = "Search (employee name, decree number)")
            @RequestParam(required = false) String q,
            @Parameter(description = "University code")
            @RequestParam(required = false) String universityCode,
            @Parameter(description = "Employee type classifier code")
            @RequestParam(required = false) String employeeType,
            @Parameter(description = "Active status")
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "jobStartDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/employee-jobs - q={}, universityCode={}, employeeType={}, active={}, page={}",
                 q, universityCode, employeeType, active, pageable.getPageNumber());
        Page<EmployeeJobsRowDto> page = employeeJobsRegistryService.getEmployeeJobs(q, universityCode, employeeType, active, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "EmployeeJobsRowResponse")
    static class EmployeeJobsRowResponseWrapper extends ResponseWrapper<PageResponse<EmployeeJobsRowDto>> {}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('teachers.employee-jobs.view')")
    @Operation(summary = "Get employee job detail by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved detail",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = EmployeeJobsDetailResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Employee job not found")
    })
    public ResponseEntity<ResponseWrapper<EmployeeJobsDetailDto>> getEmployeeJobDetail(
            @Parameter(description = "Employee job id (UUID)", required = true)
            @PathVariable @NotBlank String id
    ) {
        log.info("GET /api/v1/web/registry/employee-jobs/{}", id);
        return employeeJobsRegistryService.getEmployeeJobDetail(id)
            .map(detail -> ResponseEntity.ok(ResponseWrapper.success(detail)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Schema(name = "EmployeeJobsDetailResponse")
    static class EmployeeJobsDetailResponseWrapper extends ResponseWrapper<EmployeeJobsDetailDto> {}

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('teachers.employee-jobs.view')")
    @Operation(summary = "Get filter dictionaries (cached)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = EmployeeJobsDictionariesResponseWrapper.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ResponseWrapper<EmployeeJobsDictionariesDto>> getDictionaries() {
        log.info("GET /api/v1/web/registry/employee-jobs/dictionaries");
        return ResponseEntity.ok(ResponseWrapper.success(employeeJobsRegistryService.getDictionaries()));
    }

    @Schema(name = "EmployeeJobsDictionariesResponse")
    static class EmployeeJobsDictionariesResponseWrapper extends ResponseWrapper<EmployeeJobsDictionariesDto> {}

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('teachers.employee-jobs.view')")
    @Operation(summary = "Export employee jobs to CSV (UTF-8 BOM)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV generated",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<byte[]> exportEmployeeJobs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String universityCode,
            @RequestParam(required = false) String employeeType,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("POST /api/v1/web/registry/employee-jobs/export - q={}, universityCode={}, employeeType={}, active={}",
                 q, universityCode, employeeType, active);

        Page<EmployeeJobsRowDto> page = employeeJobsRegistryService.getEmployeeJobs(
            q, universityCode, employeeType, active, Pageable.ofSize(10000));

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Xodim,OTM,Bo'lim,Xodim turi,Lavozim,Holati,Ish boshlagan sana,Ish tugagan sana,Faol\n");
        for (EmployeeJobsRowDto d : page.getContent()) {
            csv.append(escapeCsv(d.employeeName())).append(",");
            csv.append(escapeCsv(d.universityName())).append(",");
            csv.append(escapeCsv(d.departmentName())).append(",");
            csv.append(escapeCsv(d.employeeTypeName())).append(",");
            csv.append(escapeCsv(d.positionName())).append(",");
            csv.append(escapeCsv(d.statusName())).append(",");
            csv.append(escapeCsv(str(d.jobStartDate()))).append(",");
            csv.append(escapeCsv(str(d.jobEndDate()))).append(",");
            csv.append(Boolean.TRUE.equals(d.active()) ? "Faol" : "Nofaol").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "employee_jobs_" + timestamp() + ".csv";
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
