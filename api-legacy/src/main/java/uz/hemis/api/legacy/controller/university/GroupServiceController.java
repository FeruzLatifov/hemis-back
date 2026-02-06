package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Group;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Group Service Controller - CUBA REST API Compatible
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/group")
@Tag(name = "51.Guruhlar", description = "Guruhlar xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class GroupServiceController {

    private final UniversityRefLegacyService universityService;
    private static final String ENTITY_NAME = "hemishe_EUniversityGroup";

    @GetMapping("/get")
    @Operation(summary = "Get groups by university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getByUniversity(
            @Parameter(description = "University code", required = true)
            @RequestParam String university,
            @Parameter(description = "Education type code")
            @RequestParam(required = false) String type,
            @Parameter(description = "Academic year")
            @RequestParam(required = false) Integer year) {

        log.info("[CUBA Service] group/get: university={}, type={}, year={}", university, type, year);

        List<Group> filtered = universityService.findAllGroup().stream()
            .filter(g -> university.equals(g.getUniversity()))
            .collect(Collectors.toList());

        if (type != null && !type.isEmpty()) {
            filtered = filtered.stream()
                .filter(g -> type.equals(g.getEducationType()))
                .collect(Collectors.toList());
        }

        if (year != null) {
            String yearStr = String.valueOf(year);
            filtered = filtered.stream()
                .filter(g -> g.getEducationYear() != null && g.getEducationYear().contains(yearStr))
                .collect(Collectors.toList());
        }

        List<Map<String, Object>> data = new ArrayList<>();
        int index = 1;
        for (Group group : filtered) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("_entityName", ENTITY_NAME);
            item.put("id", group.getId().toString());
            item.put("groupId", group.getGroupId() != null ? group.getGroupId() : String.valueOf(index));
            item.put("active", group.getActive() != null ? group.getActive() : true);
            item.put("version", 1);
            item.put("groupName", group.getGroupName());
            data.add(item);
            index++;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
