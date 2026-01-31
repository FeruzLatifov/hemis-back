package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Group;
import uz.hemis.domain.repository.GroupRepository;

import java.util.*;

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

    private final GroupRepository repository;
    private static final String ENTITY_NAME = "hemishe_EUniversityGroup";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean returnNulls) {

        Optional<Group> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update group")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        Optional<Group> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Group entity = existingOpt.get();
        updateFromMap(entity, body);

        Group saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete group")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        Optional<Group> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all groups")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {

        List<Group> all = repository.findAll();
        int fromIndex = Math.min(offset, all.size());
        int toIndex = Math.min(fromIndex + limit, all.size());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Group g : all.subList(fromIndex, toIndex)) {
            result.add(toMap(g, returnNulls));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Create group(s) - OLD-HEMIS sends an ARRAY
     */
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
            updateFromMap(entity, body);

            // UPSERT: check if group already exists
            Optional<Group> existing = Optional.empty();
            if (entity.getUniversity() != null && entity.getEducationType() != null
                    && entity.getEducationYear() != null && entity.getGroupId() != null
                    && entity.getGroupName() != null) {
                existing = repository.findByUniqueKey(
                        entity.getUniversity(), entity.getEducationType(),
                        entity.getEducationYear(), entity.getGroupId(),
                        entity.getGroupName());
            }

            Group toSave;
            if (existing.isPresent()) {
                toSave = existing.get();
                updateFromMap(toSave, body);
            } else {
                toSave = entity;
            }

            Group saved = repository.save(toSave);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_NAME);
            result.put("_instanceName", saved.getGroupName() != null ? saved.getGroupName() : "Group-" + saved.getId());
            result.put("id", saved.getId().toString());
            results.add(result);
        }

        return ResponseEntity.ok(results);
    }

    private Map<String, Object> toMap(Group entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getGroupName() != null ? entity.getGroupName() : "Group-" + entity.getId());
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        putIfNotNull(map, "groupId", entity.getGroupId(), returnNulls);
        putIfNotNull(map, "groupName", entity.getGroupName(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_educationType", entity.getEducationType(), returnNulls);
        putIfNotNull(map, "_educationYear", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);

        return map;
    }

    @SuppressWarnings("unchecked")
    private void updateFromMap(Group entity, Map<String, Object> map) {
        if (map.containsKey("university")) {
            Object obj = map.get("university");
            if (obj instanceof Map) {
                entity.setUniversity(String.valueOf(((Map<String, Object>) obj).get("code")));
            } else {
                entity.setUniversity(String.valueOf(obj));
            }
        }
        if (map.containsKey("_university")) {
            entity.setUniversity(String.valueOf(map.get("_university")));
        }

        if (map.containsKey("educationType")) {
            Object obj = map.get("educationType");
            if (obj instanceof Map) {
                entity.setEducationType(String.valueOf(((Map<String, Object>) obj).get("code")));
            } else {
                entity.setEducationType(String.valueOf(obj));
            }
        }
        if (map.containsKey("_educationType") || map.containsKey("_education_type")) {
            Object val = map.containsKey("_educationType") ? map.get("_educationType") : map.get("_education_type");
            entity.setEducationType(String.valueOf(val));
        }

        if (map.containsKey("educationYear")) {
            Object obj = map.get("educationYear");
            if (obj instanceof Map) {
                entity.setEducationYear(String.valueOf(((Map<String, Object>) obj).get("code")));
            } else {
                entity.setEducationYear(String.valueOf(obj));
            }
        }
        if (map.containsKey("_educationYear") || map.containsKey("_education_year")) {
            Object val = map.containsKey("_educationYear") ? map.get("_educationYear") : map.get("_education_year");
            entity.setEducationYear(String.valueOf(val));
        }

        if (map.containsKey("groupId")) {
            entity.setGroupId(String.valueOf(map.get("groupId")));
        }
        if (map.containsKey("group_id")) {
            entity.setGroupId(String.valueOf(map.get("group_id")));
        }

        if (map.containsKey("groupName")) {
            entity.setGroupName(String.valueOf(map.get("groupName")));
        }
        if (map.containsKey("group_name")) {
            entity.setGroupName(String.valueOf(map.get("group_name")));
        }
        if (map.containsKey("name")) {
            entity.setGroupName(String.valueOf(map.get("name")));
        }

        if (map.containsKey("active")) {
            Object activeObj = map.get("active");
            if (activeObj instanceof Boolean) {
                entity.setActive((Boolean) activeObj);
            } else if (activeObj instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeObj));
            }
        }

        if (entity.getActive() == null) {
            entity.setActive(true);
        }
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
