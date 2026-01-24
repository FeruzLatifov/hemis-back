package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Specialty;
import uz.hemis.domain.repository.SpecialtyRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Speciality Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/speciality/*}</p>
 *
 * <p><strong>OLD-HEMIS FORMAT BILAN 100% MOSLIK!</strong></p>
 *
 * <p><strong>Response format:</strong></p>
 * <pre>
 * {
 *     "success": true,
 *     "data": [
 *         {
 *             "_entityName": "hemishe_EUniversitySpeciality",
 *             "id": "...",
 *             "specialityCode": "...",
 *             "specialityName": "...",
 *             "active": true,
 *             "version": 1
 *         }
 *     ]
 * }
 * </pre>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/speciality")
@Tag(name = "50.Mutaxassisliklar", description = "Mutaxassisliklar xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class SpecialityServiceController {

    private final SpecialtyRepository specialtyRepository;

    private static final String ENTITY_NAME = "hemishe_EUniversitySpeciality";

    /**
     * Get specialities by university
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/speciality/get}</p>
     *
     * @param university university code
     * @param type speciality type (optional)
     * @return OLD-HEMIS formatida javob
     */
    @GetMapping("/get")
    @Operation(summary = "Get specialities by university", description = "Returns list of specialities for given university (OLD-HEMIS format)")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getByUniversity(
            @Parameter(description = "University code", required = true, example = "999")
            @RequestParam String university,
            @Parameter(description = "Speciality type", required = false)
            @RequestParam(required = false) String type) {

        log.info("[CUBA Service] speciality/get: university={}, type={}", university, type);

        // Repository dan olish
        List<Specialty> specialties = specialtyRepository.findAll().stream()
                .filter(s -> university.equals(s.getUniversity()))
                .filter(s -> type == null || type.equals(s.getEducationType()))
                .collect(Collectors.toList());

        // OLD-HEMIS formatiga o'tkazish
        List<Map<String, Object>> data = specialties.stream()
                .map(this::toOldHemisFormat)
                .collect(Collectors.toList());

        // OLD-HEMIS response wrapper
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * Entity ni OLD-HEMIS formatiga o'tkazish
     *
     * OLD-HEMIS maydonlari:
     * - _entityName: "hemishe_EUniversitySpeciality"
     * - id: UUID
     * - specialityCode: kod (code emas!)
     * - specialityName: nom (name emas!)
     * - active: faol holati
     * - version: versiya
     */
    private Map<String, Object> toOldHemisFormat(Specialty entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        map.put("active", entity.getActive() != null ? entity.getActive() : true);
        map.put("version", 1);
        map.put("specialityCode", entity.getCode());
        map.put("specialityName", entity.getName());
        return map;
    }
}
