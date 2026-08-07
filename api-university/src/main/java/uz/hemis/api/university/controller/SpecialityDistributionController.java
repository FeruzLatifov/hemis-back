package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.service.classifier.HSpecialityService;
import uz.hemis.service.classifier.dto.SpecialityDistItemDto;

import java.util.List;

/**
 * Speciality classifier DISTRIBUTION to OTMs — bootstrap PULL snapshot.
 *
 * <p>The NEW-university-facing counterpart of the modern PUSH fanout: a Univer coming online
 * (or re-syncing) pulls the full APPROVED {@code h_speciality} set at once, in the same FLAT v1
 * shape the push delivers per row. Global reference data → authenticated but <strong>NOT
 * tenant-scoped</strong> (every OTM receives the identical set; {@code universityCode} is never a
 * row filter here). Additive channel — the frozen legacy classifier pull
 * ({@code /app/rest/v2/services/classifiers/*}, {@code ClassifierLegacyService.OLD_CLASSIFIER_MAP})
 * and the 175/175 contract are untouched.</p>
 *
 * <p>Audience: 224 Univer Yii2 backends ({@code ClientType.UNIVERSITY_BACKEND}) over OAuth
 * {@code client_credentials}; secured by {@code .anyRequest().authenticated()} (no SecurityConfig change).</p>
 *
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/v1/university/classifiers")
@Tag(name = "University - Classifier Distribution",
        description = "OTM bootstrap PULL of the unified speciality classifier (APPROVED FLAT v1 snapshot)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class SpecialityDistributionController {

    private final HSpecialityService specialityService;

    @GetMapping("/speciality")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Pull the unified speciality classifier snapshot (APPROVED, FLAT v1)",
            description = """
                    Full APPROVED, code-bearing, active h_speciality set for OTM bootstrap/re-sync.
                    `educationType` (hemishe_h_education_type code: 11=Bakalavr, 12=Magistr) optionally
                    narrows the pull; omit for both. Global reference data — identical for every OTM.
                    """
    )
    public ResponseEntity<ResponseWrapper<List<SpecialityDistItemDto>>> speciality(
            @Parameter(description = "Education type filter (11=Bakalavr, 12=Magistr)", example = "11")
            @RequestParam(required = false) String educationType
    ) {
        List<SpecialityDistItemDto> items = specialityService.getDistributionSnapshot(educationType);
        log.info("OTM speciality-classifier pull: educationType={}, items={}", educationType, items.size());
        return ResponseEntity.ok(ResponseWrapper.success(items));
    }
}
