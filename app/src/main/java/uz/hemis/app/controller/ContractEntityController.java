package uz.hemis.app.controller;

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
import uz.hemis.domain.entity.Contract;
import uz.hemis.domain.repository.ContractRepository;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Contracts")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RContractStatistics")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ContractEntityController {

    private final ContractRepository repository;
    private static final String ENTITY_NAME = "hemishe_RContractStatistics";

    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes, @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        Optional<Contract> entity = repository.findById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        Optional<Contract> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        Contract entity = existingOpt.get();
        Contract saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        Optional<Contract> entity = repository.findById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(@RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls, @RequestParam(required = false) String view) {
        List<Contract> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(@RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls, @RequestParam(required = false) String view) {
        List<Contract> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam(required = false) Boolean returnCount,
            @RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort, @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls, @RequestParam(required = false) String view) {
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, parts[0]);
        }
        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<Contract> entityPage = repository.findAll(pageRequest);
        return ResponseEntity.ok(entityPage.getContent().stream().map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        Contract entity = new Contract();
        Contract saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(Contract entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getContractNumber() != null ? entity.getContractNumber() : "Contract-" + entity.getId());
        map.put("id", entity.getId());
        putIfNotNull(map, "contractNumber", entity.getContractNumber(), returnNulls);
        putIfNotNull(map, "_student", entity.getStudent(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_educationYear", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "_contractType", entity.getContractType(), returnNulls);
        putIfNotNull(map, "contractSum", entity.getContractSum(), returnNulls);
        putIfNotNull(map, "paidSum", entity.getPaidSum(), returnNulls);
        putIfNotNull(map, "remainingSum", entity.getRemainingSum(), returnNulls);
        putIfNotNull(map, "contractDate", entity.getContractDate(), returnNulls);
        putIfNotNull(map, "startDate", entity.getStartDate(), returnNulls);
        putIfNotNull(map, "endDate", entity.getEndDate(), returnNulls);
        putIfNotNull(map, "_paymentForm", entity.getPaymentForm(), returnNulls);
        putIfNotNull(map, "_status", entity.getStatus(), returnNulls);
        putIfNotNull(map, "contractorName", entity.getContractorName(), returnNulls);
        putIfNotNull(map, "contractorPassport", entity.getContractorPassport(), returnNulls);
        putIfNotNull(map, "contractorAddress", entity.getContractorAddress(), returnNulls);
        putIfNotNull(map, "contractorPhone", entity.getContractorPhone(), returnNulls);
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

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
