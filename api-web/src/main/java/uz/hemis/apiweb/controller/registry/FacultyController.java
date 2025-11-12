package uz.hemis.apiweb.controller.registry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uz.hemis.common.dto.*;
import uz.hemis.service.registry.FacultyService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/web/registry/faculties")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Faculties", description = "Faculty Registry API - Tree view with lazy loading")
@SecurityRequirement(name = "Bearer Authentication")
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping("/groups")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get university groups (root level)",
        description = "Returns university groupings with faculty counts for tree view root level. " +
                     "Supports server-side pagination, sorting, and filtering."
    )
    public ResponseEntity<PageResponse<FacultyGroupRowDto>> getUniversityGroups(
        @Parameter(description = "Search query (university name or code)")
        @RequestParam(required = false) String q,
        
        @Parameter(description = "Filter by status (true=active, false=inactive)")
        @RequestParam(required = false) Boolean status,
        
        @PageableDefault(size = 20, sort = "universityName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/faculties/groups - q={}, status={}, page={}", q, status, pageable);

        Page<FacultyGroupRowDto> groups = facultyService.getUniversityGroups(q, status, pageable);

        PageResponse<FacultyGroupRowDto> response = PageResponse.<FacultyGroupRowDto>builder()
            .content(groups.getContent())
            .page(groups.getNumber())
            .size(groups.getSize())
            .totalElements(groups.getTotalElements())
            .totalPages(groups.getTotalPages())
            .first(groups.isFirst())
            .last(groups.isLast())
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-university/{universityId}")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get faculties by university (lazy load children)",
        description = "Returns faculties for specific university when tree node is expanded. " +
                     "Supports server-side pagination, sorting, and filtering."
    )
    public ResponseEntity<PageResponse<FacultyRowDto>> getFacultiesByUniversity(
        @Parameter(description = "University code (primary key)", example = "00001")
        @PathVariable String universityId,
        
        @Parameter(description = "Search query (faculty name or code)")
        @RequestParam(required = false) String q,
        
        @Parameter(description = "Filter by status (true=active, false=inactive)")
        @RequestParam(required = false) Boolean status,
        
        @PageableDefault(size = 50, sort = "nameUz", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/faculties/by-university/{} - q={}, status={}, page={}", 
            universityId, q, status, pageable);

        Page<FacultyRowDto> faculties = facultyService.getFacultiesByUniversity(universityId, q, status, pageable);

        PageResponse<FacultyRowDto> response = PageResponse.<FacultyRowDto>builder()
            .content(faculties.getContent())
            .page(faculties.getNumber())
            .size(faculties.getSize())
            .totalElements(faculties.getTotalElements())
            .totalPages(faculties.getTotalPages())
            .first(faculties.isFirst())
            .last(faculties.isLast())
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get faculty detail by ID",
        description = "Returns complete faculty information including university details, type, audit info, etc."
    )
    public ResponseEntity<FacultyDetailDto> getFacultyById(
        @Parameter(description = "Faculty UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id
    ) {
        log.info("GET /api/v1/web/registry/faculties/{}", id);

        FacultyDetailDto faculty = facultyService.getFacultyById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

        return ResponseEntity.ok(faculty);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Export faculties to Excel",
        description = "Exports filtered faculty list to Excel file. " +
                     "Applies same filters as list endpoints (university, search query, status)."
    )
    public void exportFaculties(
        @Parameter(description = "University code filter")
        @RequestParam(required = false) String universityCode,
        
        @Parameter(description = "Search query (faculty name or code)")
        @RequestParam(required = false) String q,
        
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) Boolean status,
        
        HttpServletResponse response
    ) throws IOException {
        log.info("POST /api/v1/web/registry/faculties/export - university={}, q={}, status={}", 
            universityCode, q, status);

        List<Map<String, Object>> faculties = facultyService.getFacultiesForExport(universityCode, q, status);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("faculties_%s.xlsx", timestamp);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Faculties");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            String[] headers = {"№", "OTM nomi", "Kod", "Nomi", "Qisqa nomi", "Holati", "Yaratilgan"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Map<String, Object> faculty : faculties) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(getStringValue(faculty, "universityname"));
                row.createCell(2).setCellValue(getStringValue(faculty, "code"));
                row.createCell(3).setCellValue(getStringValue(faculty, "nameuz"));
                row.createCell(4).setCellValue(getStringValue(faculty, "shortname"));
                row.createCell(5).setCellValue(getBooleanValue(faculty, "active") ? "Faol" : "Nofaol");
                
                Object createdAt = faculty.get("createdat");
                if (createdAt != null) {
                    Cell cell = row.createCell(6);
                    cell.setCellValue(createdAt.toString());
                    cell.setCellStyle(dateStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }

        log.info("Exported {} faculties to Excel", faculties.size());
    }

    @GetMapping("/dictionaries")
    @PreAuthorize("hasAuthority('data.structure.view')")
    @Operation(
        summary = "Get dictionaries for filters",
        description = "Returns dropdown options for faculty filters (status, etc.). " +
                     "Results are cached for performance."
    )
    public ResponseEntity<FacultyDictionariesDto> getDictionaries() {
        log.info("GET /api/v1/web/registry/faculties/dictionaries");

        FacultyDictionariesDto dictionaries = facultyService.getDictionaries();

        return ResponseEntity.ok(dictionaries);
    }

    // ======================================================================
    // Helper Methods
    // ======================================================================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
}

