package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.legacy.UniversityDepartmentLegacyService;

import java.util.*;

/**
 * Cathedra (Department) Service Controller - CUBA REST API Compatible
 *
 * <p>Delegates to UniversityDepartmentLegacyService for business logic.</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/cathedra")
@Tag(name = "07.OTM bo'linmalari", description = "CUBA compatible cathedra (department) service endpoints")
@RequiredArgsConstructor
@Slf4j
public class CathedraServiceController {

    private final UniversityDepartmentLegacyService universityDepartmentLegacyService;

    @GetMapping("/get")
    @Operation(summary = "Get cathedras by university", description = "Returns list of cathedras (departments) for given university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getByUniversity(
            @Parameter(description = "University code", required = true)
            @RequestParam String university) {
        log.info("[CUBA Service] cathedra/get: university={}", university);

        Map<String, Object> response = universityDepartmentLegacyService.getCathedrasByUniversity(university);

        return ResponseEntity.ok(response);
    }
}
