package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.DissertationDefense;
import uz.hemis.domain.entity.DoctoralStudent;
import uz.hemis.domain.repository.DissertationDefenseRepository;
import uz.hemis.domain.repository.DoctoralStudentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dissertation Defense Entity Controller (CUBA Pattern)
 * Tag: 17.Dissertasiya himoyalari
 * Entity: hemishe_EDissertationDefense
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EDissertationDefense/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EDissertationDefense/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EDissertationDefense/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EDissertationDefense/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EDissertationDefense/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EDissertationDefense           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EDissertationDefense           - Create new
 *
 * @since 2.0.0
 */
@Tag(name = "17.Dissertasiya himoyalari", description = "Dissertasiya himoyalari entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EDissertationDefense")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DissertationDefenseEntityController {

    private final DissertationDefenseRepository repository;
    private final DoctoralStudentRepository doctoralStudentRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final String ENTITY_NAME = "hemishe_EDissertationDefense";
    private static final String DOCTORAL_STUDENT_ENTITY_NAME = "hemishe_EDoctorateStudent";
    private static final String SPECIALITY_DOCTORAL_ENTITY_NAME = "hemishe_HSpecialityDoctoral";
    private static final String VIEW_DISSERTATION_DEFENSE = "eDissertationDefense-view";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    // =====================================================
    // GET /{entityId} - Bitta dissertasiya himoyasini olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Bitta dissertasiya himoyasini olish",
        description = """
            ID bo'yicha dissertasiya himoyasi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Dissertasiya himoyasi ma'lumotlari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan dissertasiya himoyasi topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Dissertasiya himoyasi UUID identifikatori")
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET dissertation defense by id: {}", entityId);

        Optional<DissertationDefense> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EDissertationDefense/{} - topilmadi", entityId);
            // OLD-HEMIS format
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls, view));
    }

    // =====================================================
    // PUT /{entityId} - Dissertasiya himoyasini yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Dissertasiya himoyasini yangilash",
        description = """
            Mavjud dissertasiya himoyasi ma'lumotlarini qisman yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Dissertasiya himoyasi yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT dissertation defense id: {}", entityId);

        Optional<DissertationDefense> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("PUT /entities/hemishe_EDissertationDefense/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        DissertationDefense entity = existingOpt.get();
        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        DissertationDefense saved = repository.save(entity);

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE /{entityId} - Dissertasiya himoyasini o'chirish (soft delete)
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Dissertasiya himoyasini o'chirish",
        description = """
            Dissertasiya himoyasini soft delete qilish (delete_ts ni belgilaydi).

            **OLD-HEMIS Compatible** - 200 OK qaytaradi (204 emas!)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE dissertation defense id: {}", entityId);

        Optional<DissertationDefense> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("DELETE /entities/hemishe_EDissertationDefense/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        repository.delete(entity.get());
        log.info("DELETE /entities/hemishe_EDissertationDefense/{} - muvaffaqiyatli o'chirildi", entityId);

        // OLD-HEMIS: 200 OK (not 204)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Dissertasiya himoyalarini qidirish (GET)",
        description = "URL parametrlari orqali dissertasiya himoyalarini qidirish"
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search dissertation defense with filter: {}", filter);

        List<DissertationDefense> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Dissertasiya himoyalarini qidirish (POST)",
        description = "JSON filter orqali dissertasiya himoyalarini qidirish"
    )
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search dissertation defense with filter: {}", filter);

        List<DissertationDefense> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha dissertasiya himoyalari ro'yxati (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    @Operation(
        summary = "Barcha dissertasiya himoyalari ro'yxati",
        description = "Sahifalangan dissertasiya himoyalari ro'yxatini olish"
    )
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all dissertation defense - offset: {}, limit: {}", offset, limit);

        // Agar limit null bo'lsa, barcha yozuvlarni qaytarish
        if (limit == null) {
            List<DissertationDefense> allEntities = repository.findAll();
            List<Map<String, Object>> result = allEntities.stream()
                .map(e -> toMap(e, returnNulls, view))
                .collect(Collectors.toList());

            if (Boolean.TRUE.equals(returnCount)) {
                return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(result.size()))
                    .body(result);
            }
            return ResponseEntity.ok(result);
        }

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "DESC".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<DissertationDefense> entityPage = repository.findAll(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST - Yangi dissertasiya himoyasi yaratish
    // =====================================================

    @PostMapping
    @Transactional
    @Operation(
        summary = "Dissertasiya himoyasi yaratish",
        description = """
            Yangi dissertasiya himoyasi yozuvini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EDissertationDefense
            **Auth:** Bearer token (required)

            **Misol request body:**
            ```json
            {
                "defenseDate": "2024-06-15",
                "defense_place": "Toshkent Davlat Texnika Universiteti",
                "approvedDate": "2024-07-01",
                "diplomaNumber": "01 № 123456",
                "diplomaGivenDate": "2024-07-15",
                "diplomaGivenByWhom": "TDTU",
                "registerNumber": "12345",
                "active": true,
                "doctorateStudent": { "id": "uuid-here" },
                "speciality": { "id": "uuid-here" }
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EDissertationDefense\",\"_instanceName\":\"01 № 123456\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dissertasiya himoyasi ma'lumotlari",
                required = true)
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new dissertation defense");
        log.debug("Request body: {}", body);

        DissertationDefense entity = new DissertationDefense();

        // Agar id berilgan bo'lsa, ishlatish (OLD-HEMIS pattern)
        if (body.containsKey("id")) {
            try {
                entity.setId(UUID.fromString(body.get("id").toString()));
            } catch (Exception e) {
                log.warn("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        updateFromMap(entity, body);

        // Version va timestamps
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        DissertationDefense saved = repository.save(entity);
        log.info("Dissertation defense created with id: {}", saved.getId());

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private String buildInstanceName(DissertationDefense entity) {
        // OLD-HEMIS format: "com.company.hemishe.entity.EDissertationDefense-{id} [detached]"
        return "com.company.hemishe.entity.EDissertationDefense-" + entity.getId() + " [detached]";
    }

    /**
     * Entity -> OLD-HEMIS Map formatiga o'girish
     *
     * view=eDissertationDefense-view bo'lganda to'liq nested objectlarni qaytaradi:
     * - doctorateStudent: _entityName, _instanceName, id, thirdName, secondName, firstName
     * - speciality: _entityName, _instanceName, id, name
     *
     * OLD-HEMIS response field order:
     * _entityName, _instanceName, id, active, defenseDate, doctorateStudent,
     * approvedDate, diplomaNumber, speciality, defense_place, registerNumber, diplomaGivenByWhom
     */
    private Map<String, Object> toMap(DissertationDefense entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));

        // ID
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // Fields in OLD-HEMIS order (view=eDissertationDefense-view)
        putIfNotNull(map, "active", entity.getActive(), returnNulls);

        // defenseDate - date format
        if (entity.getDefenseDate() != null) {
            map.put("defenseDate", entity.getDefenseDate().format(DATE_FORMAT));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("defenseDate", null);
        }

        // doctorateStudent - to'liq nested object (view=eDissertationDefense-view)
        if (entity.getDoctorateStudent() != null) {
            if (VIEW_DISSERTATION_DEFENSE.equals(view)) {
                map.put("doctorateStudent", buildDoctoralStudentMap(entity.getDoctorateStudent()));
            } else {
                Map<String, Object> docStudentMap = new LinkedHashMap<>();
                docStudentMap.put("id", entity.getDoctorateStudent().toString());
                map.put("doctorateStudent", docStudentMap);
            }
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("doctorateStudent", null);
        }

        // approvedDate - date format
        if (entity.getApprovedDate() != null) {
            map.put("approvedDate", entity.getApprovedDate().format(DATE_FORMAT));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("approvedDate", null);
        }

        putIfNotNull(map, "diplomaNumber", entity.getDiplomaNumber(), returnNulls);

        // speciality - to'liq nested object (view=eDissertationDefense-view)
        if (entity.getSpeciality() != null) {
            if (VIEW_DISSERTATION_DEFENSE.equals(view)) {
                map.put("speciality", getSpecialityDoctoralMap(entity.getSpeciality()));
            } else {
                Map<String, Object> specMap = new LinkedHashMap<>();
                specMap.put("id", entity.getSpeciality().toString());
                map.put("speciality", specMap);
            }
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("speciality", null);
        }

        // defense_place - OLD-HEMIS uses snake_case!
        putIfNotNull(map, "defense_place", entity.getDefensePlace(), returnNulls);

        putIfNotNull(map, "registerNumber", entity.getRegisterNumber(), returnNulls);
        putIfNotNull(map, "diplomaGivenByWhom", entity.getDiplomaGivenByWhom(), returnNulls);

        // diplomaGivenDate - date format (only if returnNulls or has value)
        if (entity.getDiplomaGivenDate() != null) {
            map.put("diplomaGivenDate", entity.getDiplomaGivenDate().format(DATE_FORMAT));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("diplomaGivenDate", null);
        }

        // Optional fields (not in OLD-HEMIS eDissertationDefense-view response)
        // putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        // putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        // putIfNotNull(map, "position", entity.getPosition(), returnNulls);
        // putIfNotNull(map, "uId", entity.getUId(), returnNulls);
        // putIfNotNull(map, "translations", entity.getTranslations(), returnNulls);

        return map;
    }

    /**
     * DoctoralStudent entity'dan nested object yaratish
     * OLD-HEMIS format: _entityName, _instanceName, id, thirdName, secondName, firstName
     */
    private Map<String, Object> buildDoctoralStudentMap(UUID doctoralStudentId) {
        Map<String, Object> map = new LinkedHashMap<>();

        Optional<DoctoralStudent> studentOpt = doctoralStudentRepository.findById(doctoralStudentId);
        if (studentOpt.isPresent()) {
            DoctoralStudent student = studentOpt.get();
            map.put("_entityName", DOCTORAL_STUDENT_ENTITY_NAME);
            // _instanceName = "FAMILIYA ISM"
            String instanceName = buildDoctoralStudentInstanceName(student);
            map.put("_instanceName", instanceName);
            map.put("id", student.getId().toString());
            map.put("thirdName", student.getThirdName());
            map.put("secondName", student.getSecondName());
            map.put("firstName", student.getFirstName());
        } else {
            // Entity topilmadi - faqat ID qaytarish
            map.put("id", doctoralStudentId.toString());
        }

        return map;
    }

    /**
     * DoctoralStudent instance name yaratish: "FAMILIYA ISM"
     */
    private String buildDoctoralStudentInstanceName(DoctoralStudent student) {
        StringBuilder sb = new StringBuilder();
        if (student.getSecondName() != null) {
            sb.append(student.getSecondName());
        }
        if (student.getFirstName() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(student.getFirstName());
        }
        return sb.toString();
    }

    /**
     * SpecialityDoctoral entity'dan nested object yaratish (JdbcTemplate)
     * OLD-HEMIS format: _entityName, _instanceName, id, name
     */
    private Map<String, Object> getSpecialityDoctoralMap(UUID specialityId) {
        Map<String, Object> map = new LinkedHashMap<>();

        try {
            String sql = "SELECT id, name FROM hemishe_h_speciality_doctoral WHERE id = ? AND delete_ts IS NULL";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, specialityId);

            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
                String name = (String) row.get("name");
                map.put("_entityName", SPECIALITY_DOCTORAL_ENTITY_NAME);
                map.put("_instanceName", name != null ? name : "");
                map.put("id", specialityId.toString());
                map.put("name", name);
            } else {
                // Entity topilmadi - faqat ID qaytarish
                map.put("id", specialityId.toString());
            }
        } catch (Exception e) {
            log.warn("SpecialityDoctoral {} ni olishda xatolik: {}", specialityId, e.getMessage());
            map.put("id", specialityId.toString());
        }

        return map;
    }

    /**
     * OLD-HEMIS Map -> Entity ga o'girish
     */
    private void updateFromMap(DissertationDefense entity, Map<String, Object> map) {
        // defenseDate
        if (map.containsKey("defenseDate")) {
            entity.setDefenseDate(parseDate(map.get("defenseDate")));
        }

        // defense_place - OLD-HEMIS uses snake_case
        if (map.containsKey("defense_place")) {
            entity.setDefensePlace(getStringValue(map.get("defense_place")));
        }
        // Also support camelCase
        if (map.containsKey("defensePlace")) {
            entity.setDefensePlace(getStringValue(map.get("defensePlace")));
        }

        // approvedDate
        if (map.containsKey("approvedDate")) {
            entity.setApprovedDate(parseDate(map.get("approvedDate")));
        }

        // diplomaNumber
        if (map.containsKey("diplomaNumber")) {
            entity.setDiplomaNumber(getStringValue(map.get("diplomaNumber")));
        }

        // diplomaGivenDate
        if (map.containsKey("diplomaGivenDate")) {
            entity.setDiplomaGivenDate(parseDate(map.get("diplomaGivenDate")));
        }

        // diplomaGivenByWhom
        if (map.containsKey("diplomaGivenByWhom")) {
            entity.setDiplomaGivenByWhom(getStringValue(map.get("diplomaGivenByWhom")));
        }

        // registerNumber
        if (map.containsKey("registerNumber")) {
            entity.setRegisterNumber(getStringValue(map.get("registerNumber")));
        }

        // filename
        if (map.containsKey("filename")) {
            entity.setFilename(getStringValue(map.get("filename")));
        }

        // position
        if (map.containsKey("position")) {
            entity.setPosition(getIntegerValue(map.get("position")));
        }

        // active
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }

        // translations
        if (map.containsKey("translations")) {
            entity.setTranslations(getStringValue(map.get("translations")));
        }

        // doctorateStudent (nested object with "id")
        if (map.containsKey("doctorateStudent")) {
            entity.setDoctorateStudent(extractUuid(map.get("doctorateStudent")));
        }

        // speciality (nested object with "id")
        if (map.containsKey("speciality")) {
            entity.setSpeciality(extractUuid(map.get("speciality")));
        }
    }

    // =====================================================
    // Utility Methods
    // =====================================================

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    /**
     * CUBA format: {"id": "uuid-string"} - faqat Map qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id instanceof String str && !str.isEmpty()) {
                try {
                    return UUID.fromString(str);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String getStringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getBooleanValue(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private LocalDate parseDate(Object value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value.toString(), DATE_FORMAT);
        } catch (Exception e) {
            log.warn("Invalid date format: {}", value);
            return null;
        }
    }
}
