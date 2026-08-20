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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.classifier.SpecialityAttachmentService;
import uz.hemis.service.classifier.dto.ClassifierOptionDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentBulkCreateDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentBulkResultDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentCreateDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentFilterOptionsDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentRowDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentUpdateDto;
import uz.hemis.service.util.PageResponses;
import uz.hemis.web.export.XlsxStreamExporter;
import uz.hemis.web.export.XlsxSupport;

import java.util.List;
import java.util.UUID;

/**
 * Speciality Attachment Controller — Frontend UI API.
 *
 * <p><strong>Card:</strong> Attach unified-classifier specialities to OTMs
 * ({@code university_speciality_attachment}). "Har OTM'ga o'ziga tegishlisini biriktirish."</p>
 *
 * <p><strong>Tenant-scoped (fail-closed):</strong> every read and write is confined to
 * the caller's server-derived {@link uz.hemis.common.auth.AccessScope} — an OTM caller
 * sees/attaches only its own OTM, a ministry caller any. This closes the cross-OTM IDOR.
 * URL: {@code /api/v1/web/registry/speciality-attachments}.</p>
 *
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/v1/web/registry/speciality-attachments")
@Tag(
        name = "Registry - Speciality Attachments",
        description = """
                Speciality→OTM Attachment API (OTM'ga umumiy klassifikator mutaxassisligini biriktirish)

                **Type:** CENTRAL-MINISTRY CRUD, tenant-scoped fail-closed (cross-OTM IDOR closed)

                **Features:**
                - Server-side pagination + filtering (universityCode / specialityId / status)
                - Attach (create) / detach (hard delete)
                - Duplicate guard (409) on (OTM, speciality, education form)
                - Scope guard: OTM caller confined to its own OTM; ministry sees all
                """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SpecialityAttachmentController {

    private final SpecialityAttachmentService service;
    private final XlsxStreamExporter xlsxExporter;

    // =====================================================
    // List
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.view')")
    @Operation(
            summary = "List speciality attachments (paginated, scope-filtered)",
            description = """
                    Tenant-scoped list of speciality→OTM attachments with resolved speciality names.

                    **Query Parameters:**
                    - `universityCode` — filter by OTM code (validated against the caller's scope)
                    - `specialityId` — filter by speciality (UUID)
                    - `q` — free-text speciality search: speciality CODE or uz NAME substring
                      (case- and apostrophe-insensitive), or a pasted speciality UUID (exact match)
                    - `status` — filter by attachment status (e.g. ACTIVE)
                    - `page`, `size`, `sort` — standard paging (default: universityCode,asc)

                    The `/export` endpoint takes the SAME filters (`q` included) and streams exactly
                    the rows this list returns.
                    """,
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved",
                    content = @Content(schema = @Schema(implementation = SpecialityAttachmentRowResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission or out of scope")
    })
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialityAttachmentRowDto>>> list(
            @Parameter(description = "University code", example = "00001")
            @RequestParam(required = false) String universityCode,

            @Parameter(description = "Speciality id (UUID)")
            @RequestParam(required = false) UUID specialityId,

            @Parameter(description = "Mutaxassislik kodi, nomi yoki UUID bo'yicha qidiruv", example = "60110100")
            @RequestParam(required = false) String q,

            @Parameter(description = "Attachment status", example = "ACTIVE")
            @RequestParam(required = false) String status,

            @Parameter(description = "Education type code (11=Bakalavr, 12=Magistr)", example = "11")
            @RequestParam(required = false) String educationType,

            @Parameter(description = "Education form code (11=Kunduzgi, 12=Kechki, 16=Masofaviy)", example = "11")
            @RequestParam(required = false) String educationForm,

            @Parameter(description = "Academic year (start year, e.g. 2026 = 2026-2027)", example = "2026")
            @RequestParam(required = false) Integer eduYear,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "universityCode", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/speciality-attachments - universityCode={}, specialityId={}, q={}, status={}, educationType={}, educationForm={}, eduYear={}, page={}",
                universityCode, specialityId, q, status, educationType, educationForm, eduYear, pageable.getPageNumber());
        Page<SpecialityAttachmentRowDto> page = service.list(universityCode, specialityId, q, status, educationType, educationForm, eduYear, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
    }

    // =====================================================
    // Filter options (only values present in attachments)
    // =====================================================

    @GetMapping("/filter-options")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.view')")
    @Operation(
            summary = "Filter-dropdown options present in attachments",
            description = """
                    Universities, education types and education forms that ACTUALLY occur in the
                    caller's in-scope attachments (never the full classifier) — so a filter never
                    offers a choice that returns zero rows (e.g. only the OTMs with attachments).
                    """,
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Options retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission or out of scope")
    })
    public ResponseEntity<ResponseWrapper<SpecialityAttachmentFilterOptionsDto>> filterOptions() {
        log.info("GET /api/v1/web/registry/speciality-attachments/filter-options");
        return ResponseEntity.ok(ResponseWrapper.success(service.filterOptions()));
    }

    // =====================================================
    // Education-form dictionary (ALL forms from the h_education_form classifier)
    // =====================================================

    @GetMapping("/education-forms")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.view')")
    @Operation(
            summary = "Education-form classifier options (h_education_form)",
            description = """
                    All active education forms from the modern `h_education_form` classifier
                    (Kunduzgi, Kechki, Sirtqi, Masofaviy, ... — NOT a hard-coded list), for the
                    attach/edit picker. Multilingual (name/nameRu/nameEn).
                    """,
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Forms retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission")
    })
    public ResponseEntity<ResponseWrapper<List<ClassifierOptionDto>>> educationForms() {
        log.info("GET /api/v1/web/registry/speciality-attachments/education-forms");
        return ResponseEntity.ok(ResponseWrapper.success(service.listEducationForms()));
    }

    // =====================================================
    // Education-type dictionary (ALL types from the h_education_type classifier)
    // =====================================================

    @GetMapping("/education-types")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.view')")
    @Operation(
            summary = "Education-type classifier options (h_education_type)",
            description = """
                    All active education types from the modern `h_education_type` classifier
                    (Bakalavr, Magistr, Ordinatura, Doktorantura PhD/DSc — NOT a hard-coded list),
                    for the attach picker. Multilingual (name/nameRu/nameEn). Note: the speciality
                    classifier itself currently carries only Bakalavr/Magistr specialities.
                    """,
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Types retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission")
    })
    public ResponseEntity<ResponseWrapper<List<ClassifierOptionDto>>> educationTypes() {
        log.info("GET /api/v1/web/registry/speciality-attachments/education-types");
        return ResponseEntity.ok(ResponseWrapper.success(service.listEducationTypes()));
    }

    // =====================================================
    // Export (streaming .xlsx, no row cap)
    // =====================================================

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.view')")
    @Operation(
            summary = "Export speciality attachments to Excel (.xlsx)",
            description = "Streams ALL rows matching the current filters as a professional .xlsx "
                    + "(no row cap; constant memory via SXSSF; formula-injection-safe). Takes the SAME "
                    + "filters as the list endpoint - `q` included (speciality code / uz name substring, "
                    + "or a pasted speciality UUID) - so the workbook holds exactly what the grid shows. "
                    + "Omit all filters to export everything in scope.",
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workbook streamed",
                    content = @Content(mediaType = XlsxSupport.XLSX_CONTENT_TYPE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission or out of scope")
    })
    public ResponseEntity<StreamingResponseBody> export(
            @Parameter(description = "University code") @RequestParam(required = false) String universityCode,
            @Parameter(description = "Speciality id (UUID)") @RequestParam(required = false) UUID specialityId,
            @Parameter(description = "Mutaxassislik kodi, nomi yoki UUID bo'yicha qidiruv", example = "60110100")
            @RequestParam(required = false) String q,
            @Parameter(description = "Attachment status", example = "ACTIVE") @RequestParam(required = false) String status,
            @Parameter(description = "Education type code") @RequestParam(required = false) String educationType,
            @Parameter(description = "Education form code") @RequestParam(required = false) String educationForm,
            @Parameter(description = "Academic year (start year, e.g. 2026)") @RequestParam(required = false) Integer eduYear
    ) {
        log.info("GET /api/v1/web/registry/speciality-attachments/export - universityCode={}, q={}, status={}, educationType={}, educationForm={}, eduYear={}",
                universityCode, q, status, educationType, educationForm, eduYear);
        // Classifier-style tree (mirrors the /classifiers/speciality export): "Ierarxiya darajasi | Kod |
        // Mutaxassislik". Rows already arrive parent-before-child (repo ORDER BY code), so no separate
        // "parent" column is needed — the L4 "Ichki yo'nalish" name is real-Excel-indented one level under
        // its L3 "Yo'nalish" parent, which sits directly above it.
        return xlsxExporter.export(
                "biriktirilgan_mutaxassisliklar",
                "Biriktirilgan mutaxassisliklar",
                List.of("OTM kodi", "OTM nomi", "O'quv yili", "Ierarxiya darajasi", "Kod", "Mutaxassislik",
                        "Ta'lim turi", "Ta'lim shakli", "Holati"),
                new int[]{14, 44, 12, 18, 14, 60, 16, 16, 10},
                pageable -> service.list(universityCode, specialityId, q, status, educationType, educationForm, eduYear, pageable),
                r -> new String[]{
                        r.universityCode(),
                        r.universityName(),
                        r.eduYear() != null ? r.eduYear() + "-" + (r.eduYear() + 1) : "",
                        levelLabelUz(r.hierarchyLevel()),
                        r.specialityCode(),
                        r.specialityName(),
                        r.educationTypeName(),
                        r.educationFormName(),
                        r.status()
                },
                5, // indent the "Mutaxassislik" column (0-based index 5)
                r -> r.hierarchyLevel() != null && r.hierarchyLevel() == 4 ? 1 : 0); // L4 sits one level under L3
    }

    /** h_speciality taxonomy level → Uzbek label for the Excel export. */
    private static String levelLabelUz(Integer level) {
        if (level == null) {
            return "";
        }
        return switch (level) {
            case 1 -> "Bilim sohasi";
            case 2 -> "Ta'lim sohasi";
            case 3 -> "Yo'nalish";
            case 4 -> "Ichki yo'nalish";
            default -> String.valueOf(level);
        };
    }

    @Schema(name = "SpecialityAttachmentRowResponse")
    static class SpecialityAttachmentRowResponseWrapper extends ResponseWrapper<PageResponse<SpecialityAttachmentRowDto>> {
    }

    // =====================================================
    // Detail
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.view')")
    @Operation(summary = "Get attachment by id", tags = {"Registry - Speciality Attachments"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - out of scope"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ResponseWrapper<SpecialityAttachmentRowDto>> getById(
            @Parameter(description = "Attachment id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("GET /api/v1/web/registry/speciality-attachments/{}", id);
        return ResponseEntity.ok(ResponseWrapper.success(service.getById(id)));
    }

    // =====================================================
    // Create (attach)
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.create')")
    @Operation(
            summary = "Attach a speciality to an OTM",
            description = "Rejected 403 if the target OTM is outside the caller's scope, 409 if a duplicate exists. "
                    + "Only an APPROVED + active classifier speciality may be attached (an unapproved row has not "
                    + "been distributed to the OTMs yet) - otherwise 422 `SPECIALITY_NOT_APPROVED`.",
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attached"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission or out of scope"),
            @ApiResponse(responseCode = "404", description = "Speciality not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate attachment"),
            @ApiResponse(responseCode = "422", description = "Business rule - SPECIALITY_NOT_APPROVED "
                    + "(speciality is not APPROVED/active, so it is not distributed to the OTMs)")
    })
    public ResponseEntity<ResponseWrapper<SpecialityAttachmentRowDto>> create(
            @Valid @RequestBody SpecialityAttachmentCreateDto request
    ) {
        log.info("POST /api/v1/web/registry/speciality-attachments - university={}, specialityId={}",
                request.universityCode(), request.specialityId());
        SpecialityAttachmentRowDto created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    // =====================================================
    // Bulk create (attach one speciality in several education forms at once)
    // =====================================================

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.create')")
    @Operation(
            summary = "Attach a speciality to an OTM in several education forms at once",
            description = "One live row per education form. Forms already attached (same speciality + year) "
                    + "are skipped (never a 409 for the whole batch); the response reports both the created "
                    + "rows and the skipped forms. 403 if the target OTM is outside the caller's scope. "
                    + "Only an APPROVED + active classifier speciality may be attached (an unapproved row has not "
                    + "been distributed to the OTMs yet) - otherwise 422 `SPECIALITY_NOT_APPROVED`.",
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attached (see created / skipped)"),
            @ApiResponse(responseCode = "400", description = "Validation error / unknown education form"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission or out of scope"),
            @ApiResponse(responseCode = "404", description = "Speciality not found"),
            @ApiResponse(responseCode = "422", description = "Business rule - SPECIALITY_NOT_APPROVED "
                    + "(speciality is not APPROVED/active, so it is not distributed to the OTMs)")
    })
    public ResponseEntity<ResponseWrapper<SpecialityAttachmentBulkResultDto>> createBulk(
            @Valid @RequestBody SpecialityAttachmentBulkCreateDto request
    ) {
        log.info("POST /api/v1/web/registry/speciality-attachments/bulk - university={}, specialityId={}, forms={}",
                request.universityCode(), request.specialityId(),
                request.educationForms() != null ? request.educationForms().size() : 0);
        SpecialityAttachmentBulkResultDto result = service.createBulk(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(result));
    }

    // =====================================================
    // Update (edit)
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.create')")
    @Operation(
            summary = "Edit a speciality attachment",
            description = "Mutable: speciality (same education type), education form, academic year, status "
                    + "(Faol/Nofaol). University + education type are fixed. 403 out of scope, 409 duplicate. "
                    + "The re-pointed speciality must be APPROVED + active - otherwise 422 `SPECIALITY_NOT_APPROVED`.",
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission or out of scope"),
            @ApiResponse(responseCode = "404", description = "Attachment or speciality not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate attachment"),
            @ApiResponse(responseCode = "422", description = "Business rule - SPECIALITY_NOT_APPROVED "
                    + "(speciality is not APPROVED/active, so it is not distributed to the OTMs)")
    })
    public ResponseEntity<ResponseWrapper<SpecialityAttachmentRowDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SpecialityAttachmentUpdateDto request
    ) {
        log.info("PUT /api/v1/web/registry/speciality-attachments/{} - specialityId={}, status={}",
                id, request.specialityId(), request.status());
        SpecialityAttachmentRowDto updated = service.update(id, request);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // Delete (detach, hard)
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.delete')")
    @Operation(
            summary = "Detach a speciality from an OTM (hard delete)",
            description = """
                    Physically removes the attachment row — nothing references it, and it is simply
                    re-created if the OTM is allowed to run the speciality again. To withdraw the
                    permission while keeping the record, set `status` to `REVOKED` via `PUT /{id}`
                    instead. 403 if out of scope.
                    """,
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Detached - the attachment row is gone"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - out of scope"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Attachment id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/web/registry/speciality-attachments/{}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
