package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.GroupService;

/**
 * Group Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/group/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/group")
@Tag(name = "Group Service API", description = "CUBA compatible group service endpoints")
@RequiredArgsConstructor
@Slf4j
public class GroupServiceController {

    private final GroupService groupService;

    /**
     * Get groups by university
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/group/get}</p>
     *
     * @param university university code
     * @param type group type (optional)
     * @param year academic year (optional)
     * @return list of groups
     */
    @GetMapping("/get")
    @Operation(summary = "Get groups by university", description = "Returns list of groups for given university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getByUniversity(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String university,
            @Parameter(description = "Group type", required = false)
            @RequestParam(required = false) String type,
            @Parameter(description = "Academic year", required = false, example = "2024")
            @RequestParam(required = false) Integer year) {
        log.info("[CUBA Service] group/get: university={}, type={}, year={}", university, type, year);
        return ResponseEntity.ok(groupService.getByUniversity(university, type, year));
    }
}
