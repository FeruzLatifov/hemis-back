package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.hemis.domain.entity.UniversityAttachedSpeciality;
import uz.hemis.domain.repository.UniversityAttachedSpecialityRepository;

import uz.hemis.api.legacy.adapter.JsonNull;

import java.net.URI;
import java.util.*;

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

    private final UniversityAttachedSpecialityRepository repository;

    private static final String ENTITY_NAME = "hemishe_EUniversityAttachedSpeciality";
    private static final String CUBA_CLASS = "com.company.hemishe.entity.EUniversityAttachedSpeciality";

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "OTM biriktirilgan mutaxassisliklar ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(required = false) Boolean returnNulls) {

        List<UniversityAttachedSpeciality> all = repository.findAll();
        int from = Math.min(offset, all.size());
        int to = Math.min(from + limit, all.size());
        List<Map<String, Object>> result = new ArrayList<>();
        for (UniversityAttachedSpeciality e : all.subList(from, to)) {
            result.add(toMap(e, returnNulls));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(summary = "ID bo'yicha olish")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean returnNulls) {

        return repository.findById(entityId)
                .map(e -> ResponseEntity.ok(toMap(e, returnNulls)))
                .orElseGet(() -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("error", "Entity not found");
                    error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
                    return ResponseEntity.status(404).body(error);
                });
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Yangi yozuv yaratish")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            HttpServletRequest request) {

        UniversityAttachedSpeciality entity = new UniversityAttachedSpeciality();
        updateFromMap(entity, body);
        UniversityAttachedSpeciality saved = repository.save(entity);

        URI location = ServletUriComponentsBuilder
                .fromRequestUri(request)
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        Map<String, Object> minimalResponse = new LinkedHashMap<>();
        minimalResponse.put("_entityName", ENTITY_NAME);
        minimalResponse.put("_instanceName", instanceName(saved));
        minimalResponse.put("id", saved.getId() != null ? saved.getId().toString() : null);

        return ResponseEntity.created(location).body(minimalResponse);
    }

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(summary = "Yangilash")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        Optional<UniversityAttachedSpeciality> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        UniversityAttachedSpeciality entity = existingOpt.get();
        updateFromMap(entity, body);
        UniversityAttachedSpeciality saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "O'chirish (taqiqlangan)")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Deletion forbidden");
        error.put("details", "Deletion of the " + ENTITY_NAME + " is forbidden");
        return ResponseEntity.status(403).body(error);
    }

    private String instanceName(UniversityAttachedSpeciality e) {
        return CUBA_CLASS + "-" + (e.getId() != null ? e.getId().toString() : "") + " [detached]";
    }

    private Map<String, Object> toMap(UniversityAttachedSpeciality e, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", instanceName(e));
        map.put("id", e.getId() != null ? e.getId().toString() : null);

        putIfNotNull(map, "active", e.getActive(), returnNulls);
        putIfNotNull(map, "version", e.getVersion(), returnNulls);
        putIfNotNull(map, "deletedBy", e.getDeletedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", e.getDeleteTs() != null ? e.getDeleteTs().toString() : null, returnNulls);

        return map;
    }

    @SuppressWarnings("unchecked")
    private void updateFromMap(UniversityAttachedSpeciality entity, Map<String, Object> map) {
        if (map.containsKey("university")) {
            entity.setUniversity(extractCode(map.get("university")));
        }
        if (map.containsKey("educationForm")) {
            entity.setEducationForm(extractCode(map.get("educationForm")));
        }
        if (map.containsKey("specialityBachelor")) {
            entity.setSpecialityBachelor(extractUUID(map.get("specialityBachelor")));
        }
        if (map.containsKey("specialityMaster")) {
            entity.setSpecialityMaster(extractUUID(map.get("specialityMaster")));
        }
        if (map.containsKey("specialityOrdinatura")) {
            entity.setSpecialityOrdinatura(extractUUID(map.get("specialityOrdinatura")));
        }
        if (map.containsKey("specialityDoctoral")) {
            entity.setSpecialityDoctoral(extractUUID(map.get("specialityDoctoral")));
        }
        if (map.containsKey("active")) {
            entity.setActive(toBoolean(map.get("active")));
        }
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object code = nested.get("code");
            return code != null ? code.toString() : null;
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private UUID extractUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        if (value instanceof String) {
            String str = (String) value;
            return str.isEmpty() ? null : UUID.fromString(str);
        }
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            return id != null ? UUID.fromString(id.toString()) : null;
        }
        return null;
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) {
            String str = (String) value;
            if ("true".equalsIgnoreCase(str) || "1".equals(str)) return true;
            if ("false".equalsIgnoreCase(str) || "0".equals(str)) return false;
        }
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return null;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, JsonNull.INSTANCE);
        }
    }
}
