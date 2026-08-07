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
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.classifier.SpecialityAttachmentService;
import uz.hemis.service.classifier.dto.SpecialityAttachmentCreateDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentRowDto;
import uz.hemis.service.util.PageResponses;

import java.util.UUID;

/**
 * Speciality Attachment Controller — Frontend UI API.
 *
 * <p><strong>Card:</strong> Attach unified-classifier specialities to OTMs
 * ({@code h_speciality_attachment}). "Har OTM'ga o'ziga tegishlisini biriktirish."</p>
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
                - Attach (create) / detach (soft delete)
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
                    - `status` — filter by attachment status (e.g. ACTIVE)
                    - `page`, `size`, `sort` — standard paging (default: universityCode,asc)
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

            @Parameter(description = "Attachment status", example = "ACTIVE")
            @RequestParam(required = false) String status,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "universityCode", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("GET /api/v1/web/registry/speciality-attachments - universityCode={}, specialityId={}, status={}, page={}",
                universityCode, specialityId, status, pageable.getPageNumber());
        Page<SpecialityAttachmentRowDto> page = service.list(universityCode, specialityId, status, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(page)));
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
            description = "Rejected 403 if the target OTM is outside the caller's scope, 409 if a duplicate exists.",
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attached"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - lacks permission or out of scope"),
            @ApiResponse(responseCode = "404", description = "Speciality not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate attachment")
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
    // Delete (detach, soft)
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('institutions.speciality-attachments.delete')")
    @Operation(
            summary = "Detach a speciality from an OTM (soft delete)",
            description = "Sets deleted_at instead of physical deletion. 403 if out of scope.",
            tags = {"Registry - Speciality Attachments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Detached"),
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
