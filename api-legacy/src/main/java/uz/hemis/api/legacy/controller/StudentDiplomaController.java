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
import uz.hemis.domain.entity.Diploma;
import uz.hemis.domain.repository.DiplomaRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Student Diploma Controller
 * Tag 12: Diplomlar
 *
 * CUBA Platform REST API compatible controller for managing student diplomas
 * Entity: hemishe_EStudentDiploma
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EStudentDiploma/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EStudentDiploma/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EStudentDiploma           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EStudentDiploma           - Create new
 */
@Tag(name = "01. Legacy Entity APIs - Diplomas", description = "CUBA-compatible CRUD operations for student diploma entity (hemishe_EStudentDiploma)")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentDiploma")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class StudentDiplomaController {

    private final DiplomaRepository diplomaRepository;

    private static final String ENTITY_NAME = "hemishe_EStudentDiploma";

    /**
     * Get diploma by ID
     * GET /app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}
     */
    @GetMapping("/{entityId}")
    @Operation(summary = "Get diploma by ID", description = "Returns a single diploma by its UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET diploma by id: {}", entityId);

        Optional<Diploma> diploma = diplomaRepository.findById(entityId);
        if (diploma.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(diploma.get(), returnNulls));
    }

    /**
     * Update diploma
     * PUT /app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}
     */
    @PutMapping("/{entityId}")
    @Operation(summary = "Update diploma", description = "Updates an existing diploma")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT diploma id: {}", entityId);

        Optional<Diploma> existingOpt = diplomaRepository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Diploma diploma = existingOpt.get();
        updateFromMap(diploma, body);

        Diploma saved = diplomaRepository.save(diploma);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    /**
     * Soft delete diploma
     * DELETE /app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}
     */
    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete diploma", description = "Soft deletes a diploma (sets deleteTs)")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE diploma id: {}", entityId);

        Optional<Diploma> diploma = diplomaRepository.findById(entityId);
        if (diploma.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Soft delete
        diplomaRepository.delete(diploma.get());
        return ResponseEntity.noContent().build();
    }

    /**
     * Search diplomas (URL parameters)
     * GET /app/rest/v2/entities/hemishe_EStudentDiploma/search?filter=...
     */
    @GetMapping("/search")
    @Operation(summary = "Search diplomas (GET)", description = "Search diplomas using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search diplomas with filter: {}", filter);

        List<Diploma> diplomas = diplomaRepository.findAll();

        return ResponseEntity.ok(
            diplomas.stream()
                .map(d -> toMap(d, returnNulls))
                .collect(Collectors.toList())
        );
    }

    /**
     * Search diplomas (JSON filter)
     * POST /app/rest/v2/entities/hemishe_EStudentDiploma/search
     */
    @PostMapping("/search")
    @Operation(summary = "Search diplomas (POST)", description = "Search diplomas using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search diplomas with filter: {}", filter);

        List<Diploma> diplomas = diplomaRepository.findAll();

        return ResponseEntity.ok(
            diplomas.stream()
                .map(d -> toMap(d, returnNulls))
                .collect(Collectors.toList())
        );
    }

    /**
     * Get all diplomas with pagination
     * GET /app/rest/v2/entities/hemishe_EStudentDiploma
     */
    @GetMapping
    @Operation(summary = "Get all diplomas", description = "Returns paginated list of diplomas")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort field and direction (e.g. 'issueDate-desc')") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all diplomas - offset: {}, limit: {}", offset, limit);

        // Parse sort parameter
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);

        Page<Diploma> diplomaPage = diplomaRepository.findAll(pageRequest);

        List<Map<String, Object>> result = diplomaPage.getContent().stream()
            .map(d -> toMap(d, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Create new diploma
     * POST /app/rest/v2/entities/hemishe_EStudentDiploma
     */
    @PostMapping
    @Operation(summary = "Create diploma", description = "Creates a new diploma")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new diploma");

        Diploma diploma = new Diploma();
        updateFromMap(diploma, body);

        Diploma saved = diplomaRepository.save(diploma);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    /**
     * Convert Diploma entity to CUBA-compatible Map
     */
    private Map<String, Object> toMap(Diploma diploma, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // CUBA standard fields
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", diploma.getDiplomaNumber() != null ?
            diploma.getDiplomaNumber() : "Diploma-" + diploma.getId());

        // ID is always present
        map.put("id", diploma.getId());

        // All other fields
        putIfNotNull(map, "diplomaNumber", diploma.getDiplomaNumber(), returnNulls);
        putIfNotNull(map, "student", diploma.getStudent(), returnNulls);
        putIfNotNull(map, "university", diploma.getUniversity(), returnNulls);
        putIfNotNull(map, "specialty", diploma.getSpecialty(), returnNulls);
        putIfNotNull(map, "diplomaBlank", diploma.getDiplomaBlank(), returnNulls);
        putIfNotNull(map, "serialNumber", diploma.getSerialNumber(), returnNulls);
        putIfNotNull(map, "diplomaType", diploma.getDiplomaType(), returnNulls);
        putIfNotNull(map, "issueDate", diploma.getIssueDate(), returnNulls);
        putIfNotNull(map, "registrationDate", diploma.getRegistrationDate(), returnNulls);
        putIfNotNull(map, "graduationYear", diploma.getGraduationYear(), returnNulls);
        putIfNotNull(map, "qualification", diploma.getQualification(), returnNulls);
        putIfNotNull(map, "averageGrade", diploma.getAverageGrade(), returnNulls);
        putIfNotNull(map, "honors", diploma.getHonors(), returnNulls);
        putIfNotNull(map, "diplomaHash", diploma.getDiplomaHash(), returnNulls);
        putIfNotNull(map, "rectorName", diploma.getRectorName(), returnNulls);
        putIfNotNull(map, "status", diploma.getStatus(), returnNulls);
        putIfNotNull(map, "qrCode", diploma.getQrCode(), returnNulls);
        putIfNotNull(map, "verificationUrl", diploma.getVerificationUrl(), returnNulls);
        putIfNotNull(map, "notes", diploma.getNotes(), returnNulls);

        // BaseEntity fields
        putIfNotNull(map, "createTs", diploma.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", diploma.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", diploma.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", diploma.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", diploma.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", diploma.getDeletedBy(), returnNulls);

        return map;
    }

    /**
     * Update Diploma entity from Map
     */
    private void updateFromMap(Diploma diploma, Map<String, Object> map) {
        if (map.containsKey("diplomaNumber")) diploma.setDiplomaNumber((String) map.get("diplomaNumber"));
        if (map.containsKey("student")) diploma.setStudent(UUID.fromString((String) map.get("student")));
        if (map.containsKey("university")) diploma.setUniversity((String) map.get("university"));
        if (map.containsKey("specialty")) diploma.setSpecialty(UUID.fromString((String) map.get("specialty")));
        if (map.containsKey("diplomaBlank")) diploma.setDiplomaBlank(UUID.fromString((String) map.get("diplomaBlank")));
        if (map.containsKey("serialNumber")) diploma.setSerialNumber((String) map.get("serialNumber"));
        if (map.containsKey("diplomaType")) diploma.setDiplomaType((String) map.get("diplomaType"));
        if (map.containsKey("graduationYear")) diploma.setGraduationYear((Integer) map.get("graduationYear"));
        if (map.containsKey("qualification")) diploma.setQualification((String) map.get("qualification"));
        if (map.containsKey("averageGrade")) diploma.setAverageGrade((Double) map.get("averageGrade"));
        if (map.containsKey("honors")) diploma.setHonors((String) map.get("honors"));
        if (map.containsKey("diplomaHash")) diploma.setDiplomaHash((String) map.get("diplomaHash"));
        if (map.containsKey("rectorName")) diploma.setRectorName((String) map.get("rectorName"));
        if (map.containsKey("status")) diploma.setStatus((String) map.get("status"));
        if (map.containsKey("qrCode")) diploma.setQrCode((String) map.get("qrCode"));
        if (map.containsKey("verificationUrl")) diploma.setVerificationUrl((String) map.get("verificationUrl"));
        if (map.containsKey("notes")) diploma.setNotes((String) map.get("notes"));
    }

    /**
     * Helper method to add field to map only if not null or if returnNulls is true
     */
    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
