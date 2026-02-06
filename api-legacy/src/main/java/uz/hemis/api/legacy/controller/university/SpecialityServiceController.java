package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Specialty;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Speciality Service Controller - CUBA REST API Compatible
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/speciality")
@Tag(name = "50.Mutaxassisliklar", description = "Mutaxassisliklar xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class SpecialityServiceController {

    private final UniversityRefLegacyService universityService;

    private static final String ENTITY_NAME = "hemishe_EUniversitySpeciality";

    @GetMapping("/get")
    @Operation(summary = "Get specialities by university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getByUniversity(
            @Parameter(description = "University code", required = true)
            @RequestParam String university,
            @Parameter(description = "Speciality type")
            @RequestParam(required = false) String type) {

        log.info("[CUBA Service] speciality/get: university={}, type={}", university, type);

        List<Specialty> specialties = universityService.findAllSpecialty().stream()
                .filter(s -> university.equals(s.getUniversity()))
                .filter(s -> type == null || type.equals(s.getEducationType()))
                .collect(Collectors.toList());

        List<Map<String, Object>> data = specialties.stream()
                .map(this::toOldHemisFormat)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

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
