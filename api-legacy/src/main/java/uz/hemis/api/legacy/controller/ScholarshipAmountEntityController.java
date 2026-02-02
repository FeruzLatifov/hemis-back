package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.ScholarshipAmount;
import uz.hemis.domain.repository.ScholarshipAmountRepository;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ScholarshipAmount Entity Controller (CUBA Pattern)
 * Entity: hemishe_EStudentScholarshipAmount
 */
@Tag(name = "62.Stipendiya", description = "Stipendiya summa entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentScholarshipAmount")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ScholarshipAmountEntityController {

    private final ScholarshipAmountRepository repository;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EStudentScholarshipAmount";

    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        log.debug("GET scholarshipAmount by id: {}", entityId);
        Optional<ScholarshipAmount> entity = repository.findById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("PUT scholarshipAmount id: {}", entityId);
        Optional<ScholarshipAmount> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        ScholarshipAmount entity = existingOpt.get();
        updateFromMap(entity, body);
        ScholarshipAmount saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE scholarshipAmount id: {}", entityId);
        Optional<ScholarshipAmount> entity = repository.findById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<ScholarshipAmount> allEntities = repository.findAll();
        List<ScholarshipAmount> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Offset") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Limit") @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        int effectiveOffset = filterHelper.extractInt(body, "offset", offset, 0);
        int effectiveLimit = filterHelper.extractInt(body, "limit", limit, 50);
        String filterJson = filterHelper.extractFilterFromBody(body);

        log.debug("POST search - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<ScholarshipAmount> allEntities = repository.findAll();
        List<ScholarshipAmount> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        log.debug("GET all scholarshipAmounts - offset: {}, limit: {}", offset, limit);
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, parts[0]);
        }
        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<ScholarshipAmount> entityPage = repository.findAll(pageRequest);
        return ResponseEntity.ok(entityPage.getContent().stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("POST create new scholarshipAmount");
        ScholarshipAmount entity = new ScholarshipAmount();
        updateFromMap(entity, body);
        ScholarshipAmount saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(ScholarshipAmount entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", "ScholarshipAmount-" + entity.getId());
        map.put("id", entity.getId());
        putIfNotNull(map, "_studentScholarship", entity.getStudentScholarship(), returnNulls);
        putIfNotNull(map, "month", entity.getMonth(), returnNulls);
        putIfNotNull(map, "summa", entity.getSumma(), returnNulls);
        putIfNotNull(map, "localId", entity.getLocalId(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        return map;
    }

    private void updateFromMap(ScholarshipAmount entity, Map<String, Object> map) {
        if (map.containsKey("_studentScholarship")) {
            Object val = map.get("_studentScholarship");
            if (val != null) entity.setStudentScholarship(UUID.fromString(val.toString()));
        }
        if (map.containsKey("month")) {
            Object val = map.get("month");
            if (val != null) entity.setMonth(LocalDate.parse(val.toString()));
        }
        if (map.containsKey("summa")) {
            Object val = map.get("summa");
            if (val != null) entity.setSumma(Double.parseDouble(val.toString()));
        }
        if (map.containsKey("localId")) {
            Object val = map.get("localId");
            entity.setLocalId(val != null ? val.toString() : null);
        }
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
