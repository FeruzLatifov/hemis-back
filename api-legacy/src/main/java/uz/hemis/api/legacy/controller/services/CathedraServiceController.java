package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.DepartmentService;

/**
 * Cathedra (Department) Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/cathedra/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/cathedra")
@Tag(name = "Cathedra Service API", description = "CUBA compatible cathedra (department) service endpoints")
@RequiredArgsConstructor
@Slf4j
public class CathedraServiceController {

    private final DepartmentService departmentService;

    /**
     * Get cathedras (departments) by university
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/cathedra/get}</p>
     *
     * @param university university code
     * @return list of cathedras
     */
    @GetMapping("/get")
    @Operation(summary = "Get cathedras by university", description = "Returns list of cathedras (departments) for given university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getByUniversity(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String university) {
        log.info("[CUBA Service] cathedra/get: university={}", university);
        return ResponseEntity.ok(departmentService.getByUniversity(university));
    }
}
