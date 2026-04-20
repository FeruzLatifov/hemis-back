package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.student.Group;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Group Entity Controller (CUBA Pattern)
 * Entity: hemishe_EUniversityGroup
 *
 * Table: hemishe_e_university_group (7 columns: id, _university, _education_type, _education_year, group_id, group_name, active)
 */
@Tag(name = "51.Guruhlar", description = "Guruhlar entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EUniversityGroup")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class GroupEntityController {

    private final UniversityRefLegacyService universityRefService;

    @PreAuthorize("hasAuthority('universities.view')")
    @GetMapping("/{entityId}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean returnNulls) {

        Optional<Group> entity = universityRefService.findGroupById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(universityRefService.toGroupMap(entity.get(), returnNulls));
    }

    @PreAuthorize("hasAuthority('universities.edit')")
    @PutMapping("/{entityId}")
    @Operation(summary = "Update group")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        Optional<Group> existingOpt = universityRefService.findGroupById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Group entity = existingOpt.get();
        universityRefService.updateGroupFromMap(entity, body);

        Group saved = universityRefService.saveGroup(entity);
        return ResponseEntity.ok(universityRefService.toGroupMap(saved, returnNulls));
    }

    @PreAuthorize("hasAuthority('universities.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete group")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        Optional<Group> entity = universityRefService.findGroupById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        universityRefService.deleteGroup(entity.get());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('universities.view')")
    @GetMapping
    @Operation(summary = "Get all groups")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {

        List<Group> all = universityRefService.findAllGroup();
        int fromIndex = Math.min(offset, all.size());
        int toIndex = Math.min(fromIndex + limit, all.size());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Group g : all.subList(fromIndex, toIndex)) {
            result.add(universityRefService.toGroupMap(g, returnNulls));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Create group(s) - OLD-HEMIS sends an ARRAY
     */
    @PreAuthorize("hasAuthority('universities.edit')")
    @PostMapping
    @Operation(summary = "Create group(s)")
    public ResponseEntity<?> create(
            @RequestBody Object rawBody,
            @RequestParam(required = false) Boolean returnNulls) {

        // OLD-HEMIS sends array, handle both array and single object
        List<Map<String, Object>> bodyList;
        if (rawBody instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) rawBody;
            bodyList = list;
        } else if (rawBody instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> single = (Map<String, Object>) rawBody;
            bodyList = List.of(single);
        } else {
            return ResponseEntity.badRequest().build();
        }

        log.debug("POST create group(s) - count: {}", bodyList.size());

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> body : bodyList) {
            Group entity = new Group();
            entity.setId(UUID.randomUUID());
            universityRefService.updateGroupFromMap(entity, body);

            // UPSERT: check if group already exists
            Optional<Group> existing = Optional.empty();
            if (entity.getUniversity() != null && entity.getEducationType() != null
                    && entity.getEducationYear() != null && entity.getGroupId() != null
                    && entity.getGroupName() != null) {
                existing = universityRefService.findGroupByUniqueKey(
                        entity.getUniversity(), entity.getEducationType(),
                        entity.getEducationYear(), entity.getGroupId(),
                        entity.getGroupName());
            }

            Group toSave;
            if (existing.isPresent()) {
                toSave = existing.get();
                universityRefService.updateGroupFromMap(toSave, body);
            } else {
                toSave = entity;
            }

            Group saved = universityRefService.saveGroup(toSave);
            results.add(universityRefService.toGroupMinimalMap(saved));
        }

        return ResponseEntity.ok(results);
    }
}
