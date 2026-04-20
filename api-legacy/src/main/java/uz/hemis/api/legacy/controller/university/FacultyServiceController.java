package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.academic.Faculty;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Faculty Service Controller - CUBA REST API Compatible
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/faculty")
@Tag(name = "49.Fakultetlar", description = "Fakultetlar xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class FacultyServiceController {

    private final UniversityRefLegacyService universityService;

    private static final String ENTITY_NAME = "hemishe_EUniversityDepartment";

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get")
    @Operation(summary = "OTM fakultetlarini olish")
    public ResponseEntity<Map<String, Object>> get(
            @Parameter(description = "OTM kodi", required = true)
            @RequestParam String university) {

        log.info("[CUBA Service] faculty/get: university={}", university);

        List<Faculty> faculties = universityService.findAllFaculty().stream()
                .filter(f -> university.equals(f.getUniversity()))
                .collect(Collectors.toList());

        List<Map<String, Object>> data = faculties.stream()
                .map(this::toOldHemisFormat)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("count", data.size());
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    @Operation(summary = "Get faculties by university")
    public ResponseEntity<Map<String, Object>> list(
            @Parameter(description = "University code", required = true)
            @RequestParam String universityCode) {
        return get(universityCode);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/count")
    @Operation(summary = "Count faculties")
    public ResponseEntity<Map<String, Object>> count(
            @Parameter(description = "University code", required = true)
            @RequestParam String universityCode) {

        log.info("[CUBA Service] faculty/count: universityCode={}", universityCode);

        long count = universityService.findAllFaculty().stream()
                .filter(f -> universityCode.equals(f.getUniversity()))
                .count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toOldHemisFormat(Faculty entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        map.put("version", 4);
        map.put("nameUz", entity.getName());
        map.put("nameRu", entity.getName());
        map.put("status", true);
        return map;
    }
}
