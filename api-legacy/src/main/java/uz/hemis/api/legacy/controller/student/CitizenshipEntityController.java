package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.employee.Citizenship;
import uz.hemis.service.legacy.student.StudentEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Citizenship Entity Controller (CUBA Pattern)
 * Tag: 11.Fuqarolik holatlari
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_HCitizenship</p>
 *
 * <p><strong>Fuqarolik holatlari:</strong></p>
 * <ul>
 *   <li>11 - O'zbekiston Respublikasi fuqarosi</li>
 *   <li>12 - Xorijiy davlat fuqarosi</li>
 *   <li>13 - Fuqaroligi yo'q shaxslar</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "11.Fuqarolik holatlari", description = "Fuqarolik holatlari klassifikatori - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HCitizenship")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class CitizenshipEntityController {

    private final StudentEntityLegacyService studentService;
    private static final String ENTITY_NAME = "hemishe_HCitizenship";

    // =====================================================
    // GET BY ID
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.view')")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta fuqarolik holatini olish",
        description = """
            Kod bo'yicha fuqarolik holati ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HCitizenship/{entityId}
            **Auth:** Bearer token (required)

            **Holatlar:** 11=O'zbekiston fuqarosi, 12=Xorijiy fuqaro, 13=Fuqaroligi yo'q
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Fuqarolik kodi", example = "11")
            @PathVariable String entityId,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET Citizenship by code: {}", entityId);

        Optional<Citizenship> entity = studentService.findCitizenshipById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(studentService.toCitizenshipMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT - UPDATE ENTITY
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.edit')")
    @PutMapping("/{entityId}")
    @Operation(
        summary = "Fuqarolik holatini yangilash",
        description = """
            Fuqarolik holati ma'lumotlarini yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            Faqat yuborilgan maydonlar yangilanadi.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "Fuqarolik kodi", example = "11")
            @PathVariable String entityId,
            @RequestBody Map<String, Object> entityData) {

        log.info("PUT Citizenship - entityId: {}, data: {}", entityId, entityData);

        Optional<Citizenship> existingOpt = studentService.findCitizenshipById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        Citizenship entity = existingOpt.get();
        studentService.updateCitizenshipFromMap(entity, entityData);
        entity.setUpdatedAt(java.time.LocalDateTime.now());

        Citizenship saved = studentService.saveCitizenship(entity);
        log.info("Citizenship updated successfully: {}", entityId);

        return ResponseEntity.ok(studentService.toCitizenshipMinimalMap(saved));
    }

    // =====================================================
    // DELETE - SOFT DELETE ENTITY
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Fuqarolik holatini o'chirish",
        description = "Fuqarolik holatini o'chirish (soft delete)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "Fuqarolik kodi", example = "11")
            @PathVariable String entityId) {

        log.info("DELETE Citizenship - entityId: {}", entityId);

        Optional<Citizenship> existingOpt = studentService.findCitizenshipById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        studentService.deleteCitizenship(existingOpt.get());
        log.info("Citizenship soft deleted successfully: {}", entityId);

        return ResponseEntity.ok().build();
    }

    // =====================================================
    // POST - CREATE NEW ENTITY
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.edit')")
    @PostMapping
    @Operation(
        summary = "Yangi fuqarolik holatini yaratish",
        description = """
            Yangi fuqarolik holati yaratish.

            **OLD-HEMIS Compatible** - CUBA Platform kabi upsert qiladi.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov")
    })
    public ResponseEntity<?> create(@RequestBody Map<String, Object> entityData) {

        log.info("POST Citizenship - data: {}", entityData);

        String code = (String) entityData.get("code");
        String name = (String) entityData.get("name");

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "code is required",
                "details", "code maydoni majburiy"
            ));
        }

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "name is required",
                "details", "name maydoni majburiy"
            ));
        }

        // OLD-HEMIS COMPATIBLE: CUBA Platform does upsert
        Optional<Citizenship> existingOpt = studentService.findCitizenshipById(code);

        Citizenship entity;
        boolean isNew = false;
        boolean wasRestored = false;

        if (existingOpt.isPresent()) {
            entity = existingOpt.get();
            entity.setUpdatedAt(java.time.LocalDateTime.now());
        } else {
            if (studentService.hasSoftDeletedCitizenship(code)) {
                studentService.restoreSoftDeletedCitizenship(code);

                entity = studentService.findCitizenshipById(code)
                    .orElseThrow(() -> new IllegalStateException("Failed to restore entity with code: " + code));
                wasRestored = true;
            } else {
                entity = new Citizenship();
                entity.setCode(code);
                entity.setCreatedAt(java.time.LocalDateTime.now());
                isNew = true;
            }
        }

        entity.setName(name);
        studentService.updateCitizenshipFromMap(entity, entityData);

        if (isNew) {
            entity.setActive(true);
        }

        Citizenship saved = studentService.saveCitizenship(entity);
        log.info("Citizenship {} successfully: {}", isNew ? "created" : (wasRestored ? "restored" : "updated"), code);

        return ResponseEntity.ok(studentService.toCitizenshipMinimalMap(saved));
    }

    // =====================================================
    // GET ALL - LIST ALL ENTITIES
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.view')")
    @GetMapping
    @Operation(
        summary = "Barcha fuqarolik holatlari",
        description = "Sahifalangan fuqarolik holatlari ro'yxatini olish."
    )
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all Citizenship - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.by(Sort.Direction.ASC, "code");
        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), limit, sorting);
        Page<Citizenship> page = studentService.findAllCitizenship(pageRequest);

        List<Map<String, Object>> result = page.getContent().stream()
            .map(e -> studentService.toCitizenshipMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // GET SEARCH
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.view')")
    @GetMapping("/search")
    @Operation(
        summary = "Fuqarolik holatlarini qidirish (GET)",
        description = "URL parametrlari orqali qidirish."
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        log.debug("GET search Citizenship with filter: {}", filter);

        List<Citizenship> entities = studentService.findAllCitizenship();
        entities.sort(Comparator.comparing(Citizenship::getCode));

        if (filter != null && !filter.isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> filterMap = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(filter, Map.class);
                entities = applyCubaFilter(entities, filterMap);
            } catch (Exception e) {
                log.warn("Failed to parse CUBA filter: {}", e.getMessage());
            }
        }

        int fromIndex = Math.min(offset, entities.size());
        int toIndex = Math.min(offset + limit, entities.size());
        List<Citizenship> paginatedEntities = entities.subList(fromIndex, toIndex);

        List<Map<String, Object>> result = paginatedEntities.stream()
            .map(e -> studentService.toCitizenshipMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST SEARCH
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.view')")
    @PostMapping("/search")
    @Operation(
        summary = "Fuqarolik holatlarini qidirish (POST)",
        description = """
            JSON filter orqali qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **CUBA Filter Format:**
            ```json
            {
              "filter": {
                "conditions": [
                  {"property": "code", "operator": "=", "value": "860"}
                ]
              },
              "limit": 50,
              "offset": 0
            }
            ```
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search Citizenship with body: {}", requestBody);

        List<Citizenship> entities = studentService.findAllCitizenship();
        entities.sort(Comparator.comparing(Citizenship::getCode));

        // OLD-HEMIS CUBA format: {"filter": {"conditions": [...]}, "limit": 50, "offset": 0}
        if (requestBody != null && requestBody.containsKey("filter")) {
            entities = applyCubaFilter(entities, requestBody.get("filter"));
        }

        // Apply limit and offset from request body (OLD-HEMIS CUBA format)
        int limit = 50; // default
        int offset = 0; // default
        if (requestBody != null) {
            if (requestBody.containsKey("limit")) {
                Object limitObj = requestBody.get("limit");
                if (limitObj instanceof Number) {
                    limit = ((Number) limitObj).intValue();
                }
            }
            if (requestBody.containsKey("offset")) {
                Object offsetObj = requestBody.get("offset");
                if (offsetObj instanceof Number) {
                    offset = ((Number) offsetObj).intValue();
                }
            }
        }

        // Apply pagination
        int fromIndex = Math.min(offset, entities.size());
        int toIndex = Math.min(offset + limit, entities.size());
        List<Citizenship> paginatedEntities = entities.subList(fromIndex, toIndex);

        List<Map<String, Object>> result = paginatedEntities.stream()
            .map(e -> studentService.toCitizenshipMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    @SuppressWarnings("unchecked")
    private List<Citizenship> applyCubaFilter(List<Citizenship> entities, Object filterObj) {
        if (!(filterObj instanceof Map)) {
            return entities;
        }

        Map<String, Object> filter = (Map<String, Object>) filterObj;
        Object conditionsObj = filter.get("conditions");

        if (!(conditionsObj instanceof List)) {
            return entities;
        }

        List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;

        return entities.stream()
            .filter(entity -> matchesAllConditions(entity, conditions))
            .collect(Collectors.toList());
    }

    private boolean matchesAllConditions(Citizenship entity, List<Map<String, Object>> conditions) {
        for (Map<String, Object> condition : conditions) {
            String property = (String) condition.get("property");
            String operator = (String) condition.get("operator");
            Object value = condition.get("value");

            if (!matchesCondition(entity, property, operator, value)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesCondition(Citizenship entity, String property, String operator, Object value) {
        if (property == null || operator == null) {
            return true;
        }

        Object entityValue = getEntityValue(entity, property);
        String strValue = value != null ? value.toString() : null;
        String strEntityValue = entityValue != null ? entityValue.toString() : null;

        return switch (operator.toLowerCase()) {
            case "=" -> strValue != null && strValue.equals(strEntityValue);
            case "<>", "!=" -> strValue == null || !strValue.equals(strEntityValue);
            case "like" -> strEntityValue != null && strValue != null &&
                strEntityValue.toLowerCase().contains(strValue.toLowerCase().replace("%", ""));
            case "in" -> {
                if (value instanceof List) {
                    yield ((List<?>) value).stream()
                        .anyMatch(v -> v != null && v.toString().equals(strEntityValue));
                }
                yield false;
            }
            default -> true;
        };
    }

    private Object getEntityValue(Citizenship entity, String property) {
        return switch (property.toLowerCase()) {
            case "code", "id" -> entity.getCode();
            case "name" -> entity.getName();
            case "nameen", "name_en" -> entity.getNameEn();
            case "nameru", "name_ru" -> entity.getNameRu();
            case "active" -> entity.isActive();
            case "version" -> entity.getVersion();
            default -> null;
        };
    }
}
