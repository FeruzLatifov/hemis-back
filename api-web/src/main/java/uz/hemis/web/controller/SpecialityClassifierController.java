package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.domain.entity.classifier.ReviewStatus;
import uz.hemis.service.classifier.HSpecialityService;
import uz.hemis.service.classifier.SpecialityAttachmentService;
import uz.hemis.service.classifier.dto.ClassifierOptionDto;
import uz.hemis.service.classifier.dto.SpecialityAttachedUniversityDto;
import uz.hemis.service.classifier.dto.SpecialityCreateDto;
import uz.hemis.service.classifier.dto.SpecialityDuplicateCheckDto;
import uz.hemis.service.classifier.dto.SpecialityNodeDto;
import uz.hemis.service.classifier.dto.SpecialityRowDto;
import uz.hemis.service.classifier.dto.SpecialityUpdateDto;
import uz.hemis.service.shared.I18nService;
import uz.hemis.service.util.PageResponses;
import uz.hemis.web.service.SpecialityExcelExporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.UUID;

/**
 * Unified Speciality Classifier Controller — Frontend UI API.
 *
 * <p><strong>Card:</strong> Speciality classifier (bakalavr/magistr), the merged
 * {@code h_speciality} table (5367 xlsx-approved + 53 {@code NEEDS_REVIEW}).
 * ADDITIVE — it does not touch the frozen {@code hemishe_h_speciality_bachelor/_master}
 * tables or the 175/175 legacy contract.</p>
 *
 * <p>Global reference data (one source of truth distributed unchanged to 224 OTMs) →
 * permission-guarded only, NOT tenant-scoped. URL: {@code /api/v1/web/classifiers/speciality}.</p>
 *
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/v1/web/classifiers/speciality")
@Tag(
        name = "Classifiers - Speciality",
        description = """
                Unified Speciality Classifier API (bakalavr/magistr birlashtirilgan klassifikator)

                **Type:** Global reference data (permission-guarded, not tenant-scoped)

                **Features:**
                - Hierarchical tree by education type (11=Bakalavr / 12=Magistr)
                - Paginated flat list with education-type + review-status + text filters
                - Curation edit (fix code/name/type/years, promote NEEDS_REVIEW → APPROVED)
                - Delete a NEEDS_REVIEW row (childless + unattached only)

                **Additive:** does NOT modify the frozen bachelor/master tables or the 175/175 contract.
                """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SpecialityClassifierController {

    private final HSpecialityService service;
    // The attachment registry owns the "which OTMs is it attached to" read — this card only
    // consumes it (as delete blockers); it is NOT moved onto HSpecialityService.
    private final SpecialityAttachmentService attachmentService;
    private final SpecialityExcelExporter exporter;
    private final I18nService i18nService;

    // =====================================================
    // Tree
    // =====================================================

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('classifiers.speciality.view')")
    @Operation(
            summary = "Speciality tree (by education level)",
            description = """
                    Full hierarchical tree built from the `parent_id` self-reference,
                    each node carrying its normalized years and children.

                    **Query Parameters:**
                    - `educationType` — 11 (Bakalavr) | 12 (Magistr) (omit = all types)
                    - `year` — keep only the edition of that year (leaves carrying it + their ancestors); omit = all years
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tree retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.view'")
    })
    public ResponseEntity<ResponseWrapper<List<SpecialityNodeDto>>> tree(
            @Parameter(description = "Education type filter (hemishe_h_education_type code): 11=Bakalavr, 12=Magistr", example = "11")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Edition year filter", example = "2024")
            @RequestParam(required = false) Integer year
    ) {
        log.info("GET /api/v1/web/classifiers/speciality/tree - educationType={}, year={}", educationType, year);
        return ResponseEntity.ok(ResponseWrapper.success(service.getTree(educationType, year)));
    }

    // =====================================================
    // Available years (dropdown source)
    // =====================================================

    @GetMapping("/years")
    @PreAuthorize("hasAuthority('classifiers.speciality.view')")
    @Operation(
            summary = "Distinct edition years (year-filter dropdown source)",
            description = """
                    The distinct set of edition years present in the active classifier, newest first —
                    the option set for the year filter. Optionally scoped to one education level.

                    **Query Parameters:**
                    - `educationType` — 11 (Bakalavr) | 12 (Magistr) (omit = all types)
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Years retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.view'")
    })
    public ResponseEntity<ResponseWrapper<List<Integer>>> years(
            @Parameter(description = "Education type filter (11=Bakalavr, 12=Magistr)", example = "11")
            @RequestParam(required = false) String educationType
    ) {
        log.info("GET /api/v1/web/classifiers/speciality/years - educationType={}", educationType);
        return ResponseEntity.ok(ResponseWrapper.success(service.availableYears(educationType)));
    }

    // =====================================================
    // Education-type dictionary (Create/Edit picker source)
    // =====================================================

    @GetMapping("/education-types")
    @PreAuthorize("hasAuthority('classifiers.speciality.view')")
    @Operation(
            summary = "Education-type options for the Create/Edit picker (h_education_type)",
            description = """
                    The education types this classifier admits — Bakalavr (11) and Magistr (12) — read
                    from the modern `h_education_type` classifier (NOT a hard-coded list), multilingual
                    (name/nameRu/nameEn). Feeds the Ta'lim turi dropdown in the add/edit dialogs. Served
                    under `classifiers.speciality.view` so the classifier page needs no cross-feature
                    permission.
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Types retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.view'")
    })
    public ResponseEntity<ResponseWrapper<List<ClassifierOptionDto>>> educationTypes() {
        log.info("GET /api/v1/web/classifiers/speciality/education-types");
        return ResponseEntity.ok(ResponseWrapper.success(service.listEducationTypes()));
    }

    // =====================================================
    // Export (.xlsx)
    // =====================================================

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('classifiers.speciality.view')")
    @Operation(
            summary = "Export classifier to Excel (.xlsx)",
            description = """
                    The classifier as a professional `.xlsx` — one worksheet per education level,
                    each with a provenance band (title, applied filters, generated-at, per-status
                    counts), a frozen auto-filtered header, and the tree flattened depth-first in
                    display order (level 1 → 2 → 3 → 4, newest edition year first, then ascending
                    code) with native collapsible row grouping. Built fully in-memory (no temp file).

                    **What-you-see-is-what-you-export:** every filter (`year`, `reviewStatus`, `q`)
                    is applied with ancestor retention, so a filtered export mirrors the grid while
                    keeping the hierarchy intact. All filters omitted ⇒ the whole classifier.

                    **Query Parameters:**
                    - `educationType` — 11 (Bakalavr) | 12 (Magistr) (omit = both, two sheets)
                    - `year` — keep only the edition of that year (omit = all years)
                    - `reviewStatus` — APPROVED | NEEDS_REVIEW (omit = all; APPROVED = the OTM snapshot)
                    - `q` — free-text on name/code (omit = no text filter)
                    - `lang` — label language, e.g. uz-UZ / ru-RU / en-US (default uz-UZ)
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workbook streamed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.view'")
    })
    public ResponseEntity<byte[]> export(
            @Parameter(description = "Education type filter (11=Bakalavr, 12=Magistr; omit = both, two sheets)", example = "11")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Edition year filter", example = "2024")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Review-status filter (omit = all)", example = "APPROVED")
            @RequestParam(required = false) ReviewStatus reviewStatus,

            @Parameter(description = "Free-text filter on name or code")
            @RequestParam(required = false) String q,

            @Parameter(description = "Label language", example = "uz-UZ")
            @RequestParam(defaultValue = "uz-UZ") String lang
    ) {
        long t0 = System.nanoTime();
        SpecialityExcelExporter.Labels labels = buildLabels(lang);
        // A query with no letter/digit (e.g. only apostrophes, which fold to empty) is not a
        // meaningful text filter — treat it as absent so it neither degrades to match-all nor
        // gets advertised in the provenance band as an applied filter.
        String effectiveQ = (q != null && q.chars().anyMatch(Character::isLetterOrDigit)) ? q : null;

        // One worksheet per education type. Omitting educationType ⇒ both (Bakalavr + Magistr) in one file.
        SequencedMap<String, List<SpecialityNodeDto>> sheets = new LinkedHashMap<>();
        if ("12".equals(educationType)) {
            sheets.put(labels.master(), service.getTreeFiltered("12", reviewStatus, effectiveQ, year));
        } else if ("11".equals(educationType)) {
            sheets.put(labels.bachelor(), service.getTreeFiltered("11", reviewStatus, effectiveQ, year));
        } else {
            sheets.put(labels.bachelor(), service.getTreeFiltered("11", reviewStatus, effectiveQ, year));
            sheets.put(labels.master(), service.getTreeFiltered("12", reviewStatus, effectiveQ, year));
        }

        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        byte[] xlsx = exporter.toXlsx(sheets, labels, generatedAt,
                buildFiltersText(labels, year, reviewStatus, effectiveQ));

        int rows = sheets.values().stream().mapToInt(SpecialityClassifierController::countNodes).sum();
        log.info("GET /export done - educationType={}, year={}, status={}, q={}, lang={}, sheets={}, rows={}, bytes={}, ms={}",
                educationType, year, reviewStatus, effectiveQ, lang, sheets.size(), rows, xlsx.length,
                (System.nanoTime() - t0) / 1_000_000);

        String suffix = "11".equals(educationType) ? "_bakalavr"
                : "12".equals(educationType) ? "_magistr" : "";
        String filename = "mutaxassislik_klassifikatori" + suffix + ".xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .cacheControl(CacheControl.noStore()) // download must reflect the latest curated state
                .body(xlsx);
    }

    /** Localized label bundle for the export, resolved from {@code lang} via the i18n message store. */
    private SpecialityExcelExporter.Labels buildLabels(String lang) {
        return new SpecialityExcelExporter.Labels(
                List.of(
                        i18nService.getMessage("Level", lang),
                        i18nService.getMessage("Hierarchy level", lang),
                        i18nService.getMessage("Code", lang),
                        i18nService.getMessage("Parent code", lang),
                        i18nService.getMessage("Name", lang) + " (UZ)",
                        i18nService.getMessage("Name", lang) + " (OZ)",
                        i18nService.getMessage("Name", lang) + " (RU)",
                        i18nService.getMessage("Name", lang) + " (EN)",
                        i18nService.getMessage("Education level", lang),
                        i18nService.getMessage("Status", lang),
                        i18nService.getMessage("Years", lang)
                ),
                Map.of(
                        1, i18nService.getMessage("Field of knowledge", lang),
                        2, i18nService.getMessage("Field of education", lang),
                        3, i18nService.getMessage("Direction", lang),
                        4, i18nService.getMessage("Sub-direction", lang)
                ),
                i18nService.getMessage("Bachelor", lang),
                i18nService.getMessage("Master", lang),
                i18nService.getMessage("Approved", lang),
                i18nService.getMessage("Needs review", lang),
                i18nService.getMessage("Speciality classifier", lang),
                i18nService.getMessage("Generated", lang),
                i18nService.getMessage("Total", lang),
                i18nService.getMessage("Filters", lang),
                i18nService.getMessage("No filter", lang)
        );
    }

    /** Human-readable applied-filter summary for the provenance band (blank ⇒ whole classifier). */
    private static String buildFiltersText(SpecialityExcelExporter.Labels labels, Integer year,
                                           ReviewStatus reviewStatus, String q) {
        List<String> parts = new ArrayList<>();
        if (year != null) {
            parts.add(year.toString());
        }
        if (reviewStatus == ReviewStatus.APPROVED) {
            parts.add(labels.approved());
        } else if (reviewStatus == ReviewStatus.NEEDS_REVIEW) {
            parts.add(labels.needsReview());
        }
        if (q != null && !q.isBlank()) {
            parts.add("\"" + q.trim() + "\"");
        }
        return String.join("; ", parts);
    }

    /** Total node count of a tree (for export instrumentation). */
    private static int countNodes(List<SpecialityNodeDto> nodes) {
        int n = 0;
        for (SpecialityNodeDto node : nodes) {
            n++;
            if (node.children() != null && !node.children().isEmpty()) {
                n += countNodes(node.children());
            }
        }
        return n;
    }

    // =====================================================
    // List (paginated, filtered)
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAuthority('classifiers.speciality.view')")
    @Operation(
            summary = "List specialities (paginated, filtered)",
            description = """
                    Flat paginated list for the curation grid.

                    **Query Parameters:**
                    - `educationType` — 11 (Bakalavr) | 12 (Magistr)
                    - `reviewStatus` — APPROVED | NEEDS_REVIEW ("to'g'rilash kerak" filter)
                    - `q` — case-insensitive search on name / code
                    - `year` — keep only rows carrying that edition year
                    - `page`, `size`, `sort` — standard paging (default: code,asc)
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved",
                    content = @Content(schema = @Schema(implementation = SpecialityRowResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.view'")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialityRowDto>>> list(
            @Parameter(description = "Education type filter (11=Bakalavr, 12=Magistr)", example = "12")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Review status filter", example = "NEEDS_REVIEW")
            @RequestParam(required = false) ReviewStatus reviewStatus,

            @Parameter(description = "Search query (name or code)", example = "informatika")
            @RequestParam(required = false) String q,

            @Parameter(description = "Edition year filter", example = "2024")
            @RequestParam(required = false) Integer year,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/classifiers/speciality - educationType={}, reviewStatus={}, q={}, year={}, page={}",
                educationType, reviewStatus, q, year, pageable.getPageNumber());
        Page<SpecialityRowDto> page = service.list(educationType, reviewStatus, q, year, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    @Schema(name = "SpecialityRowResponse")
    static class SpecialityRowResponseWrapper extends ResponseWrapper<PageResponse<SpecialityRowDto>> {
    }

    // =====================================================
    // Detail
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('classifiers.speciality.view')")
    @Operation(
            summary = "Get speciality by id (with years and direct children)",
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<SpecialityNodeDto>> getById(
            @Parameter(description = "Speciality id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("GET /api/v1/web/classifiers/speciality/{}", id);
        return ResponseEntity.ok(ResponseWrapper.success(service.getById(id)));
    }

    // =====================================================
    // Delete blockers — attached OTMs
    // =====================================================

    @GetMapping("/{id}/attachments")
    @PreAuthorize("hasAuthority('classifiers.speciality.view')")
    @Operation(
            summary = "Universities this speciality is attached to (delete blockers)",
            description = """
                    The OTMs that block a delete of this speciality, grouped by university and
                    ordered by OTM code — the named counterpart of the `SPECIALITY_ATTACHED_TO_UNIVERSITY`
                    guard on `DELETE /{id}` (same source), so the delete dialog can list them the way it
                    already lists blocking sub-directions instead of only saying "N attachment(s)".

                    Attachments have no soft delete, so every row listed here is one the admin can
                    see in the registry and actually detach — the list and the guard count can never
                    disagree.

                    `count` — attachment rows at that OTM (one per education form / academic year).

                    Empty array = nothing is attached and this guard will not fire.
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attached universities retrieved (empty array if none)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.view'"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<List<SpecialityAttachedUniversityDto>>> attachments(
            @Parameter(description = "Speciality id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("GET /api/v1/web/classifiers/speciality/{}/attachments", id);
        return ResponseEntity.ok(ResponseWrapper.success(attachmentService.attachedUniversities(id)));
    }

    // =====================================================
    // Duplicate check (advisory, for the add form)
    // =====================================================

    @GetMapping("/duplicates")
    @PreAuthorize("hasAuthority('classifiers.speciality.view')")
    @Operation(
            summary = "Duplicate check for the manual add form (advisory)",
            description = """
                    Existing active rows whose code equals `code` OR whose folded name equals `name`,
                    scoped to `educationType`. Advisory ONLY — code is intentionally non-unique, so
                    this never blocks a create; it lets the admin see what already exists. A match under
                    `parentId` is flagged as a sibling collision (the strongest signal).
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.view'")
    })
    public ResponseEntity<ResponseWrapper<SpecialityDuplicateCheckDto>> duplicates(
            @Parameter(description = "Code to check", example = "60110100")
            @RequestParam(required = false) String code,

            @Parameter(description = "Name (uz) to check")
            @RequestParam(required = false) String name,

            @Parameter(description = "Education type scope (11=Bakalavr, 12=Magistr)", example = "11")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Chosen parent id — matches under it are flagged as sibling collisions")
            @RequestParam(required = false) UUID parentId
    ) {
        log.info("GET /api/v1/web/classifiers/speciality/duplicates - code={}, hasName={}, educationType={}",
                code, name != null && !name.isBlank(), educationType);
        return ResponseEntity.ok(ResponseWrapper.success(
                service.findDuplicates(code, name, educationType, parentId)));
    }

    // =====================================================
    // Create (manual add)
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('classifiers.speciality.create')")
    @Operation(
            summary = "Create a speciality (manual add)",
            description = """
                    Manually add a new speciality row. It is born NEEDS_REVIEW, so it is NOT
                    distributed to the 224 OTMs until promoted to APPROVED via PUT /{id}.

                    **Placement:** `parentId` omitted → a top-level (level 1) node; otherwise a
                    child whose hierarchy level is derived as `parent.hierarchyLevel + 1`. A child
                    must share its parent's education level.
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.create'"),
            @ApiResponse(responseCode = "404", description = "Parent not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate - same code and name already exist"),
            @ApiResponse(responseCode = "422", description = "Business rule violation (parent/child level mismatch)")
    })
    public ResponseEntity<ResponseWrapper<SpecialityNodeDto>> create(
            @Valid @RequestBody SpecialityCreateDto request
    ) {
        log.info("POST /api/v1/web/classifiers/speciality - parentId={}", request.parentId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(service.create(request)));
    }

    // =====================================================
    // Update (curate + promote)
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('classifiers.speciality.edit')")
    @Operation(
            summary = "Curate a speciality (fix + promote)",
            description = """
                    Fix code/name/level/years and optionally promote NEEDS_REVIEW → APPROVED.
                    Years are fully replaced when supplied.
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.edit'"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<SpecialityNodeDto>> update(
            @Parameter(description = "Speciality id (UUID)", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody SpecialityUpdateDto request
    ) {
        log.info("PUT /api/v1/web/classifiers/speciality/{}", id);
        return ResponseEntity.ok(ResponseWrapper.success(service.update(id, request)));
    }

    // =====================================================
    // Delete (curation backlog only)
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('classifiers.speciality.delete')")
    @Operation(
            summary = "Delete a speciality (NEEDS_REVIEW only)",
            description = """
                    Physically removes a speciality row together with its edition years. Scoped to the
                    curation backlog on purpose — a row the OTMs have ever seen is never removed.

                    **Guards (each a 422 with a machine-readable rule code):**
                    - `SPECIALITY_DELETE_APPROVED_FORBIDDEN` — the row is APPROVED (part of the
                      distributed snapshot); retire it via `PUT /{id}` (demote / deactivate), which
                      retracts it from the 224 OTMs instead of orphaning it.
                    - `SPECIALITY_HAS_CHILDREN_DELETE_FIRST` — it still has sub-directions
                      (deactivated ones included); delete them first, or move them under another
                      parent via `PUT /{id}` (hierarchyLevel + parentId). The message names them.
                    - `SPECIALITY_ATTACHED_TO_UNIVERSITY` — an OTM is currently allowed to run it;
                      detach it in the speciality-attachments registry first. The message names the
                      first OTM codes; `GET /{id}/attachments` lists them all with their names.
                      Every blocking attachment is visible there — the table has no soft delete.

                    Irreversible — there is no soft delete on classifier rows.
                    """,
            tags = {"Classifiers - Speciality"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'classifiers.speciality.delete'"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation (approved / has children / attached)")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Speciality id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/web/classifiers/speciality/{}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
