package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Group;
import uz.hemis.domain.repository.GroupRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Group Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/group/*}</p>
 *
 * <p>OLD-HEMIS Response Format:</p>
 * <pre>
 * {
 *     "success": true,
 *     "data": [
 *         {
 *             "_entityName": "hemishe_EUniversityGroup",
 *             "id": "88d77617-eb47-a99c-d46b-5dcaa57347cc",
 *             "groupId": "1",
 *             "active": true,
 *             "version": 1,
 *             "groupName": "125-21"
 *         }
 *     ]
 * }
 * </pre>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/group")
@Tag(name = "51.Guruhlar", description = "Guruhlar xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class GroupServiceController {

    private final GroupRepository groupRepository;
    private static final String ENTITY_NAME = "hemishe_EUniversityGroup";

    /**
     * Get groups by university
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/group/get}</p>
     *
     * @param university university code
     * @param type education type code (optional)
     * @param year academic year (optional)
     * @return list of groups wrapped in {"success": true, "data": [...]}
     */
    @GetMapping("/get")
    @Operation(summary = "Get groups by university", description = "Returns list of groups for given university in OLD-HEMIS format")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getByUniversity(
            @Parameter(description = "University code", required = true, example = "999")
            @RequestParam String university,
            @Parameter(description = "Education type code", required = false, example = "11")
            @RequestParam(required = false) String type,
            @Parameter(description = "Academic year", required = false, example = "2021")
            @RequestParam(required = false) Integer year) {

        log.info("[CUBA Service] group/get: university={}, type={}, year={}", university, type, year);

        // Fetch all groups and filter in memory (matching OLD-HEMIS behavior)
        List<Group> allGroups = groupRepository.findAll();

        // Filter by university
        List<Group> filtered = allGroups.stream()
            .filter(g -> university.equals(g.getUniversity()))
            .collect(Collectors.toList());

        // Filter by educationType if provided
        if (type != null && !type.isEmpty()) {
            filtered = filtered.stream()
                .filter(g -> type.equals(g.getEducationType()))
                .collect(Collectors.toList());
        }

        // Filter by academicYear if provided
        if (year != null) {
            String yearStr = String.valueOf(year);
            filtered = filtered.stream()
                .filter(g -> g.getAcademicYear() != null && g.getAcademicYear().contains(yearStr))
                .collect(Collectors.toList());
        }

        // Convert to OLD-HEMIS format
        List<Map<String, Object>> data = new ArrayList<>();
        int index = 1;
        for (Group group : filtered) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("_entityName", ENTITY_NAME);
            item.put("id", group.getId().toString());
            // groupId - use index or extract from name if numeric pattern exists
            item.put("groupId", extractGroupId(group, index));
            item.put("active", group.getActive() != null ? group.getActive() : true);
            item.put("version", 1);
            item.put("groupName", group.getName());
            data.add(item);
            index++;
        }

        // Build response wrapper
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        log.debug("[CUBA Service] group/get: returning {} groups", data.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Extract groupId from group.
     * Attempts to extract a numeric suffix from the group name (e.g., "125-21" -> could use as-is or parse).
     * Falls back to using the index position.
     */
    private String extractGroupId(Group group, int index) {
        // If the group name contains a numeric pattern, try to use part of it
        // For now, use the index as groupId to match OLD-HEMIS behavior
        return String.valueOf(index);
    }
}
