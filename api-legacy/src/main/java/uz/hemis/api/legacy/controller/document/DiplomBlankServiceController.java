package uz.hemis.api.legacy.controller.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.legacy.DiplomBlankLegacyService;

import java.util.*;

/**
 * Diplom-Blank Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/diplom-blank/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/diplom-blank")
@Tag(name = "Diplom-Blank Service API", description = "CUBA compatible diploma blank service endpoints")
@RequiredArgsConstructor
@Slf4j
public class DiplomBlankServiceController {

    private final DiplomBlankLegacyService diplomBlankService;

    /**
     * Get diploma blanks by university and year
     */
    @GetMapping("/get")
    @Operation(summary = "Get diploma blanks", description = "Returns diploma blanks for given university and year")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> get(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String university,
            @Parameter(description = "Academic year", required = true, example = "2024")
            @RequestParam Integer year) {
        log.info("[CUBA Service] diplom-blank/get: university={}, year={}", university, year);

        try {
            List<Map<String, Object>> blanks = diplomBlankService.getDiplomBlanks(university, year);

            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Ok");
            response.put("data", blanks);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in diplom-blank/get: {}", e.getMessage(), e);
            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Set diploma blank status
     */
    @GetMapping("/setStatus")
    @Operation(summary = "Set diploma blank status", description = "Updates status of diploma blank")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> setStatus(
            @Parameter(description = "Diploma blank code", required = true)
            @RequestParam String blankCode,
            @Parameter(description = "Status code", required = true)
            @RequestParam String statusCode,
            @Parameter(description = "Reason for status change", required = false)
            @RequestParam(required = false) String reason) {
        log.info("[CUBA Service] diplom-blank/setStatus: blankCode={}, statusCode={}, reason={}",
                blankCode, statusCode, reason);

        try {
            // Check if blank exists
            String id = diplomBlankService.findDiplomBlankId(blankCode);

            if (id == null) {
                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("message", "Diplom blank not found!");
                return ResponseEntity.ok(response);
            }

            // Update status
            diplomBlankService.updateDiplomBlankStatus(id, statusCode, reason);

            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Diplom blank status updated!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in diplom-blank/setStatus: {}", e.getMessage(), e);
            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

}
