package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import uz.hemis.domain.entity.Scholarship;
import uz.hemis.domain.repository.ScholarshipRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Scholarship Entity Controller (CUBA Pattern)
 * Entity: hemishe_EStudentScholarshipFull
 */
@Tag(name = "Scholarships")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentScholarshipFull")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ScholarshipEntityController {

    private final ScholarshipRepository repository;
    private static final String ENTITY_NAME = "hemishe_EStudentScholarshipFull";

    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        log.debug("GET scholarship by id: {}", entityId);
        Optional<Scholarship> entity = repository.findById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("PUT scholarship id: {}", entityId);
        Optional<Scholarship> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        Scholarship entity = existingOpt.get();
        updateFromMap(entity, body);
        Scholarship saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE scholarship id: {}", entityId);
        Optional<Scholarship> entity = repository.findById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        log.debug("GET search scholarships with filter: {}", filter);
        List<Scholarship> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        log.debug("POST search scholarships with filter: {}", filter);
        List<Scholarship> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
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
        log.debug("GET all scholarships - offset: {}, limit: {}", offset, limit);
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, parts[0]);
        }
        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<Scholarship> entityPage = repository.findAll(pageRequest);
        return ResponseEntity.ok(entityPage.getContent().stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("POST create new scholarship");
        Scholarship entity = new Scholarship();
        updateFromMap(entity, body);
        Scholarship saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(Scholarship entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getScholarshipCode() != null ? entity.getScholarshipCode() : "Scholarship-" + entity.getId());
        map.put("id", entity.getId());
        putIfNotNull(map, "scholarshipCode", entity.getScholarshipCode(), returnNulls);
        putIfNotNull(map, "_student", entity.getStudent(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_educationYear", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "semester", entity.getSemester(), returnNulls);
        putIfNotNull(map, "_scholarshipType", entity.getScholarshipType(), returnNulls);
        putIfNotNull(map, "amount", entity.getAmount(), returnNulls);
        putIfNotNull(map, "startDate", entity.getStartDate(), returnNulls);
        putIfNotNull(map, "endDate", entity.getEndDate(), returnNulls);
        putIfNotNull(map, "paymentDate", entity.getPaymentDate(), returnNulls);
        putIfNotNull(map, "_status", entity.getStatus(), returnNulls);
        putIfNotNull(map, "orderNumber", entity.getOrderNumber(), returnNulls);
        putIfNotNull(map, "orderDate", entity.getOrderDate(), returnNulls);
        putIfNotNull(map, "approvedBy", entity.getApprovedBy(), returnNulls);
        putIfNotNull(map, "_paymentMethod", entity.getPaymentMethod(), returnNulls);
        putIfNotNull(map, "bankAccount", entity.getBankAccount(), returnNulls);
        putIfNotNull(map, "bankCode", entity.getBankCode(), returnNulls);
        putIfNotNull(map, "transactionRef", entity.getTransactionRef(), returnNulls);
        putIfNotNull(map, "reason", entity.getReason(), returnNulls);
        putIfNotNull(map, "notes", entity.getNotes(), returnNulls);
        putIfNotNull(map, "isActive", entity.getIsActive(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        return map;
    }

    private void updateFromMap(Scholarship entity, Map<String, Object> map) {
        // TODO: Add field mappings
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
