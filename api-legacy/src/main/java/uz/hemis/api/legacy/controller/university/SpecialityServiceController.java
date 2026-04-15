package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.util.*;

/**
 * Speciality Service Controller - CUBA REST API Compatible
 *
 * <p>Delegates to UniversityRefLegacyService for business logic.</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/speciality")
@Tag(name = "50.Mutaxassisliklar", description = "Mutaxassisliklar xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class SpecialityServiceController {

    private final UniversityRefLegacyService universityRefLegacyService;

    @GetMapping("/get")
    @Operation(summary = "Get specialities by university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getByUniversity(
            @Parameter(description = "University code", required = true)
            @RequestParam String university,
            @Parameter(description = "Speciality type")
            @RequestParam(required = false) String type) {

        log.info("[CUBA Service] speciality/get: university={}, type={}", university, type);

        Map<String, Object> response = universityRefLegacyService.getSpecialitiesByUniversity(university, type);

        return ResponseEntity.ok(response);
    }
}
