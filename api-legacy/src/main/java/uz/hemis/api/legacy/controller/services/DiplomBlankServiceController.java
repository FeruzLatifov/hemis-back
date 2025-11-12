package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Diplom-Blank Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/diplom-blank/*}</p>
 *
 * <p><strong>Methods:</strong></p>
 * <ul>
 *   <li>get - Get diploma blanks by university and year</li>
 *   <li>setStatus - Update diploma blank status</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/diplom-blank")
@Tag(name = "Diplom-Blank Service API", description = "CUBA compatible diploma blank service endpoints")
@RequiredArgsConstructor
@Slf4j
public class DiplomBlankServiceController {

    /**
     * Get diploma blanks by university and year
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/diplom-blank/get}</p>
     *
     * @param university university code
     * @param year academic year
     * @return list of diploma blanks
     */
    @GetMapping("/get")
    @Operation(
        summary = "Get diploma blanks",
        description = "Returns diploma blanks for given university and year"
    )
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> get(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String university,
            @Parameter(description = "Academic year", required = true, example = "2024")
            @RequestParam Integer year) {
        log.info("[CUBA Service] diplom-blank/get: university={}, year={}", university, year);
        
        // TODO: Implement with DiplomaBlankService
        return ResponseEntity.ok().body(java.util.Map.of(
            "university", university,
            "year", year,
            "blanks", java.util.List.of(),
            "message", "Diplom-blank service - implementation pending"
        ));
    }

    /**
     * Set diploma blank status
     *
     * <p><strong>URL:</strong> {@code POST /app/rest/v2/services/diplom-blank/setStatus}</p>
     *
     * @param blankCode diploma blank code
     * @param statusCode new status code
     * @param reason optional reason for status change
     * @return operation result
     */
    @PostMapping("/setStatus")
    @Operation(
        summary = "Set diploma blank status",
        description = "Updates status of diploma blank"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setStatus(
            @Parameter(description = "Diploma blank code", required = true, example = "AA1234567")
            @RequestParam String blankCode,
            @Parameter(description = "Status code", required = true, example = "ACTIVE")
            @RequestParam String statusCode,
            @Parameter(description = "Reason for status change", required = false)
            @RequestParam(required = false) String reason) {
        log.info("[CUBA Service] diplom-blank/setStatus: blankCode={}, statusCode={}, reason={}", 
            blankCode, statusCode, reason);
        
        // TODO: Implement with DiplomaBlankService
        return ResponseEntity.ok().body(java.util.Map.of(
            "blankCode", blankCode,
            "statusCode", statusCode,
            "success", true,
            "message", "Status updated (implementation pending)"
        ));
    }
}
