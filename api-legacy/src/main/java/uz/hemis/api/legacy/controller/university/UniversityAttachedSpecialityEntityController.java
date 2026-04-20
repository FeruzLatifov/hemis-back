package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.hemis.domain.entity.university.UniversityAttachedSpeciality;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.net.URI;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * UniversityAttachedSpeciality Entity Controller (CUBA Pattern)
 *
 * <p>URL: /app/rest/v2/entities/hemishe_EUniversityAttachedSpeciality</p>
 * <p>CRUD: GET list, GET by id, POST, PUT, DELETE</p>
 */
@Tag(name = "15.OTM", description = "OTM ga biriktirilgan mutaxassisliklar")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EUniversityAttachedSpeciality")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityAttachedSpecialityEntityController {

    private final UniversityRefLegacyService universityRefService;

    private static final String ENTITY_NAME = "hemishe_EUniversityAttachedSpeciality";

    @PreAuthorize("hasAuthority('universities.view')")
    @GetMapping
    @Operation(summary = "OTM biriktirilgan mutaxassisliklar ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view (e.g. eUniversityAttachedSpeciality-view)")
            @RequestParam(required = false) String view) {

        List<UniversityAttachedSpeciality> all = universityRefService.findAllUniversityAttachedSpeciality();
        int from = Math.min(offset, all.size());
        int to = Math.min(from + limit, all.size());
        List<Map<String, Object>> result = new ArrayList<>();
        for (UniversityAttachedSpeciality e : all.subList(from, to)) {
            result.add(universityRefService.toUniversityAttachedSpecialityMap(e, returnNulls, view));
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('universities.view')")
    @GetMapping("/{entityId}")
    @Operation(summary = "ID bo'yicha olish")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view")
            @RequestParam(required = false) String view) {

        return universityRefService.findUniversityAttachedSpecialityById(entityId)
                .map(e -> ResponseEntity.ok(universityRefService.toUniversityAttachedSpecialityMap(e, returnNulls, view)))
                .orElseGet(() -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("error", "Entity not found");
                    error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
                    return ResponseEntity.status(404).body(error);
                });
    }

    @PreAuthorize("hasAuthority('universities.edit')")
    @PostMapping
    @Operation(summary = "Yangi yozuv yaratish")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            HttpServletRequest request) {

        UniversityAttachedSpeciality entity = new UniversityAttachedSpeciality();
        universityRefService.updateUniversityAttachedSpecialityFromMap(entity, body);
        UniversityAttachedSpeciality saved = universityRefService.saveUniversityAttachedSpeciality(entity);

        URI location = ServletUriComponentsBuilder
                .fromRequestUri(request)
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(universityRefService.toUniversityAttachedSpecialityMinimalMap(saved));
    }

    @PreAuthorize("hasAuthority('universities.edit')")
    @PutMapping("/{entityId}")
    @Operation(summary = "Yangilash")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view")
            @RequestParam(required = false) String view) {

        Optional<UniversityAttachedSpeciality> existingOpt = universityRefService.findUniversityAttachedSpecialityById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        UniversityAttachedSpeciality entity = existingOpt.get();
        universityRefService.updateUniversityAttachedSpecialityFromMap(entity, body);
        UniversityAttachedSpeciality saved = universityRefService.saveUniversityAttachedSpeciality(entity);
        return ResponseEntity.ok(universityRefService.toUniversityAttachedSpecialityMap(saved, returnNulls, view));
    }

    @PreAuthorize("hasAuthority('universities.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "O'chirish (taqiqlangan)")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Deletion forbidden");
        error.put("details", "Deletion of the " + ENTITY_NAME + " is forbidden");
        return ResponseEntity.status(403).body(error);
    }
}
