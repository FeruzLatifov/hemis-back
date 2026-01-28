package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Faculty;
import uz.hemis.domain.repository.FacultyRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Faculty Service Controller - CUBA REST API Compatible
 *
 * <p><strong>OLD-HEMIS FORMAT BILAN 100% MOSLIK!</strong></p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/faculty/*}</p>
 *
 * <p><strong>Response format:</strong></p>
 * <pre>
 * {
 *     "success": true,
 *     "data": [
 *         {
 *             "_entityName": "hemishe_EUniversityDepartment",
 *             "id": "301-101",
 *             "code": "301-101",
 *             "version": 3,
 *             "nameUz": "...",
 *             "nameRu": "..."
 *         }
 *     ]
 * }
 * </pre>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/faculty")
@Tag(name = "49.Fakultetlar", description = "Fakultetlar xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class FacultyServiceController {

    private final FacultyRepository facultyRepository;

    private static final String ENTITY_NAME = "hemishe_EUniversityDepartment";

    /**
     * Get faculties by university
     *
     * <p><strong>URL:</strong> {@code GET /services/faculty/get?university=301}</p>
     *
     * <p><strong>OLD-HEMIS:</strong> Bu endpoint university parametri bilan fakultetlar RO'YXATINI qaytaradi!</p>
     *
     * @param university University code
     * @return OLD-HEMIS formatida fakultetlar ro'yxati
     */
    @GetMapping("/get")
    @Operation(
        summary = "OTM fakultetlarini olish",
        description = "OTM kodi bo'yicha fakultetlar ro'yxatini qaytaradi (OLD-HEMIS format)"
    )
    public ResponseEntity<Map<String, Object>> get(
            @Parameter(description = "OTM kodi", required = true, example = "301")
            @RequestParam String university) {

        log.info("[CUBA Service] faculty/get: university={}", university);

        // Repository dan olish
        List<Faculty> faculties = facultyRepository.findAll().stream()
                .filter(f -> university.equals(f.getUniversity()))
                .collect(Collectors.toList());

        // OLD-HEMIS formatiga o'tkazish
        List<Map<String, Object>> data = faculties.stream()
                .map(this::toOldHemisFormat)
                .collect(Collectors.toList());

        // OLD-HEMIS response wrapper
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * Get faculties by university (alias for /get)
     */
    @GetMapping("/list")
    @Operation(
        summary = "Get faculties by university",
        description = "Returns list of faculties for specific university (CUBA compatible)"
    )
    public ResponseEntity<Map<String, Object>> list(
            @Parameter(description = "University code", required = true, example = "301")
            @RequestParam String universityCode) {
        // /list endpoint /get bilan bir xil ishlaydi
        return get(universityCode);
    }

    /**
     * Count faculties by university
     */
    @GetMapping("/count")
    @Operation(
        summary = "Count faculties",
        description = "Returns count of faculties for university (CUBA compatible)"
    )
    public ResponseEntity<Map<String, Object>> count(
            @Parameter(description = "University code", required = true, example = "301")
            @RequestParam String universityCode) {

        log.info("[CUBA Service] faculty/count: universityCode={}", universityCode);

        long count = facultyRepository.findAll().stream()
                .filter(f -> universityCode.equals(f.getUniversity()))
                .count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Entity ni OLD-HEMIS formatiga o'tkazish
     *
     * OLD-HEMIS maydonlari:
     * - _entityName: "hemishe_EUniversityDepartment"
     * - id: kod (301-101)
     * - code: kod (301-101)
     * - version: 3
     * - nameUz: O'zbekcha nomi
     * - nameRu: Ruscha nomi
     */
    private Map<String, Object> toOldHemisFormat(Faculty entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        map.put("version", 3);
        map.put("nameUz", entity.getName());
        map.put("nameRu", entity.getName()); // Ruscha nomi ham shu bo'lsin (legacy)
        return map;
    }
}

