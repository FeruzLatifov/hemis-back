package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.SpecialtyService;

/**
 * Speciality Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/speciality/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/speciality")
@Tag(name = "Speciality Service API", description = "CUBA compatible speciality service endpoints")
@RequiredArgsConstructor
@Slf4j
public class SpecialityServiceController {

    private final SpecialtyService specialtyService;

    /**
     * Get specialities by university
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/speciality/get}</p>
     *
     * @param university university code
     * @param type speciality type (optional)
     * @return list of specialities
     */
    @GetMapping("/get")
    @Operation(summary = "Get specialities by university", description = "Returns list of specialities for given university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getByUniversity(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String university,
            @Parameter(description = "Speciality type", required = false)
            @RequestParam(required = false) String type) {
        log.info("[CUBA Service] speciality/get: university={}, type={}", university, type);
        return ResponseEntity.ok(specialtyService.getByUniversity(university, type));
    }
}
