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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.domain.entity.DoctoralStudent;
import uz.hemis.domain.repository.DoctoralStudentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Doctoral Student Entity Controller (CUBA Pattern)
 * Tag: 16.Ilmiy doktorant talabalari
 * Entity: hemishe_EDoctorateStudent
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EDoctorateStudent/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EDoctorateStudent/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EDoctorateStudent/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EDoctorateStudent/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EDoctorateStudent/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EDoctorateStudent           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EDoctorateStudent           - Create new
 *
 * @since 2.0.0
 */
@Tag(name = "16.Ilmiy doktorant talabalari", description = "Doktorant talabalar entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EDoctorateStudent")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DoctoralStudentEntityController {

    private final DoctoralStudentRepository repository;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EDoctorateStudent";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    // =====================================================
    // GET /{entityId} - Bitta doktorant ma'lumotlarini olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Bitta doktorant ma'lumotlarini olish",
        description = """
            ID bo'yicha doktorant talaba ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EDoctorateStudent/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Doktorant ma'lumotlari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan doktorant topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Doktorant UUID identifikatori")
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET doctoral student by id: {}", entityId);

        Optional<DoctoralStudent> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EDoctorateStudent/{} - topilmadi", entityId);
            // OLD-HEMIS format
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Doktorant ma'lumotlarini yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Doktorant ma'lumotlarini yangilash",
        description = """
            Mavjud doktorant ma'lumotlarini qisman yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_EDoctorateStudent/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Doktorant yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT doctoral student id: {}", entityId);

        Optional<DoctoralStudent> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("PUT /entities/hemishe_EDoctorateStudent/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        DoctoralStudent entity = existingOpt.get();
        updateFromMap(entity, body);

        DoctoralStudent saved = repository.save(entity);

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE /{entityId} - Doktorantni o'chirish (soft delete)
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Doktorantni o'chirish",
        description = """
            Doktorantni soft delete qilish (delete_ts ni belgilaydi).

            **OLD-HEMIS Compatible** - 200 OK qaytaradi (204 emas!)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE doctoral student id: {}", entityId);

        Optional<DoctoralStudent> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("DELETE /entities/hemishe_EDoctorateStudent/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        repository.delete(entity.get());
        log.info("DELETE /entities/hemishe_EDoctorateStudent/{} - muvaffaqiyatli o'chirildi", entityId);

        // OLD-HEMIS: 200 OK (not 204)
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Doktorantlarni qidirish (GET)",
        description = "URL parametrlari orqali doktorantlarni qidirish"
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<DoctoralStudent> allEntities = repository.findAll();
        List<DoctoralStudent> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Doktorantlarni qidirish (POST)",
        description = "JSON filter orqali doktorantlarni qidirish"
    )
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

        List<DoctoralStudent> allEntities = repository.findAll();
        List<DoctoralStudent> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha doktorantlar ro'yxati (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    @Operation(
        summary = "Barcha doktorantlar ro'yxati",
        description = "Sahifalangan doktorantlar ro'yxatini olish"
    )
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all doctoral students - offset: {}, limit: {}", offset, limit);

        // Agar limit null bo'lsa, barcha yozuvlarni qaytarish
        if (limit == null) {
            List<DoctoralStudent> allEntities = repository.findAll();
            List<Map<String, Object>> result = allEntities.stream()
                .map(e -> toMap(e, returnNulls))
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
        Page<DoctoralStudent> entityPage = repository.findAll(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST - Yangi doktorant yaratish
    // =====================================================

    @PostMapping
    @Transactional
    @Operation(
        summary = "Doktorant talaba yaratish",
        description = """
            Yangi doktorant talaba yozuvini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EDoctorateStudent
            **Auth:** Bearer token (required)

            **Misol request body:**
            ```json
            {
                "firstName": "MARDONBEK",
                "secondName": "ALIYEV",
                "thirdName": "KARIMOVICH",
                "dirthDate": "1985-05-21",
                "dissertationTheme": "Dissertatsiya mavzusi",
                "homeAddress": "Toshkent shahar",
                "acceptedDate": "2019-09-09",
                "paymentForm": { "code": "11" },
                "nationality": { "code": "1161" },
                "gender": { "code": "11" },
                "country": { "code": "UZ" },
                "province": "Toshkent",
                "district": "Olmazor",
                "soato": { "code": "1726" },
                "doctoralStudentType": { "code": "11" },
                "doctorateStudentStatus": { "code": "11" },
                "level": "1",
                "speciality": { "id": "73136bb8-1258-4c77-91b2-3005f75a9ea7" }
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EDoctorateStudent\",\"_instanceName\":\"ALIYEV MARDONBEK\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Doktorant ma'lumotlari",
                required = true)
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create/upsert doctoral student");
        log.debug("Request body: {}", body);

        // CUBA UPSERT: if body contains 'id' and entity exists, update instead of create
        if (body.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(body.get("id").toString());
                var existingOpt = repository.findById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    DoctoralStudent entity = existingOpt.get();
                    updateFromMap(entity, body);
                    entity.setUpdateTs(LocalDateTime.now());
                    DoctoralStudent saved = repository.save(entity);
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("_entityName", ENTITY_NAME);
                    response.put("_instanceName", buildInstanceName(saved));
                    response.put("id", saved.getId().toString());
                    return ResponseEntity.ok(response);
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        DoctoralStudent entity = new DoctoralStudent();

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

        DoctoralStudent saved = repository.save(entity);
        log.info("Doctoral student created with id: {}", saved.getId());

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

    private String buildInstanceName(DoctoralStudent entity) {
        String fullName = entity.getFullName();
        return fullName != null ? fullName : "DoctoralStudent-" + entity.getId();
    }

    /**
     * Entity -> OLD-HEMIS Map formatiga o'girish
     */
    private Map<String, Object> toMap(DoctoralStudent entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));

        // ID
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // Personal info (OLD-HEMIS field names)
        putIfNotNull(map, "firstName", entity.getFirstName(), returnNulls);
        putIfNotNull(map, "secondName", entity.getSecondName(), returnNulls);
        putIfNotNull(map, "thirdName", entity.getThirdName(), returnNulls);
        putIfNotNull(map, "passportNumber", entity.getPassportNumber(), returnNulls);
        putIfNotNull(map, "passportPin", entity.getPassportPin(), returnNulls);

        // birthDate - corrected field name
        if (entity.getBirthDate() != null) {
            map.put("birthDate", entity.getBirthDate().format(DATE_FORMAT));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("birthDate", null);
        }

        // Academic info
        putIfNotNull(map, "dissertationTheme", entity.getDissertationTheme(), returnNulls);
        putIfNotNull(map, "homeAddress", entity.getHomeAddress(), returnNulls);

        if (entity.getAcceptedDate() != null) {
            map.put("acceptedDate", entity.getAcceptedDate().format(DATE_FORMAT));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("acceptedDate", null);
        }

        putIfNotNull(map, "studentIdNumber", entity.getStudentIdNumber(), returnNulls);

        // Classifiers as nested objects (OLD-HEMIS format)
        putClassifier(map, "paymentForm", entity.getPaymentForm(), returnNulls);
        putClassifier(map, "nationality", entity.getNationality(), returnNulls);
        putClassifier(map, "gender", entity.getGender(), returnNulls);
        putClassifier(map, "country", entity.getCountry(), returnNulls);
        putClassifier(map, "soato", entity.getSoato(), returnNulls);
        putClassifier(map, "doctoralStudentType", entity.getDoctoralStudentType(), returnNulls);
        putClassifier(map, "doctorateStudentStatus", entity.getDoctorateStudentStatus(), returnNulls);
        putClassifier(map, "scienceBranch", entity.getScienceBranch(), returnNulls);

        // Location
        putIfNotNull(map, "province", entity.getProvince(), returnNulls);
        putIfNotNull(map, "district", entity.getDistrict(), returnNulls);

        // Other fields
        putIfNotNull(map, "level", entity.getLevel(), returnNulls);
        putIfNotNull(map, "university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "department", entity.getDepartment(), returnNulls);
        putIfNotNull(map, "position", entity.getPosition(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "uId", entity.getUId(), returnNulls);

        // Speciality as nested object with id
        if (entity.getSpeciality() != null) {
            Map<String, Object> specMap = new LinkedHashMap<>();
            specMap.put("id", entity.getSpeciality().toString());
            map.put("speciality", specMap);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("speciality", null);
        }

        // Audit fields
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        return map;
    }

    /**
     * OLD-HEMIS Map -> Entity ga o'girish
     */
    private void updateFromMap(DoctoralStudent entity, Map<String, Object> map) {
        // Personal info
        if (map.containsKey("firstName")) {
            entity.setFirstName(getStringValue(map.get("firstName")));
        }
        if (map.containsKey("secondName")) {
            entity.setSecondName(getStringValue(map.get("secondName")));
        }
        if (map.containsKey("thirdName")) {
            entity.setThirdName(getStringValue(map.get("thirdName")));
        }
        if (map.containsKey("passportNumber")) {
            entity.setPassportNumber(getStringValue(map.get("passportNumber")));
        }
        if (map.containsKey("passportPin")) {
            entity.setPassportPin(getStringValue(map.get("passportPin")));
        }

        // dirthDate - OLD-HEMIS typo
        if (map.containsKey("dirthDate")) {
            entity.setBirthDate(parseDate(map.get("dirthDate")));
        }
        // Also support correct spelling
        if (map.containsKey("birthDate")) {
            entity.setBirthDate(parseDate(map.get("birthDate")));
        }

        // Academic info
        if (map.containsKey("dissertationTheme")) {
            entity.setDissertationTheme(getStringValue(map.get("dissertationTheme")));
        }
        if (map.containsKey("homeAddress")) {
            entity.setHomeAddress(getStringValue(map.get("homeAddress")));
        }
        if (map.containsKey("acceptedDate")) {
            entity.setAcceptedDate(parseDate(map.get("acceptedDate")));
        }
        if (map.containsKey("studentIdNumber")) {
            entity.setStudentIdNumber(getStringValue(map.get("studentIdNumber")));
        }

        // Classifiers (nested objects with "code")
        entity.setPaymentForm(extractCode(map.get("paymentForm")));
        entity.setNationality(extractCode(map.get("nationality")));
        entity.setGender(extractCode(map.get("gender")));
        entity.setCountry(extractCode(map.get("country")));
        entity.setSoato(extractCode(map.get("soato")));
        entity.setDoctoralStudentType(extractCode(map.get("doctoralStudentType")));
        entity.setDoctorateStudentStatus(extractCode(map.get("doctorateStudentStatus")));
        entity.setScienceBranch(extractCode(map.get("scienceBranch")));

        // Location
        if (map.containsKey("province")) {
            entity.setProvince(getStringValue(map.get("province")));
        }
        if (map.containsKey("district")) {
            entity.setDistrict(getStringValue(map.get("district")));
        }

        // Other fields
        if (map.containsKey("level")) {
            Object levelVal = map.get("level");
            if (levelVal instanceof Map) {
                entity.setLevel(extractCode(levelVal));
            } else {
                entity.setLevel(getStringValue(levelVal));
            }
        }
        if (map.containsKey("university")) {
            // OLD-HEMIS: university {code: "401"} formatda keladi - extractCode bilan olish kerak
            entity.setUniversity(extractCode(map.get("university")));
        }
        if (map.containsKey("department")) {
            Object deptVal = map.get("department");
            if (deptVal instanceof Map) {
                entity.setDepartment(extractCode(deptVal));
            } else {
                entity.setDepartment(getStringValue(deptVal));
            }
        }
        if (map.containsKey("position")) {
            entity.setPosition(getIntegerValue(map.get("position")));
        }
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }

        // Speciality (nested object with "id")
        if (map.containsKey("speciality")) {
            entity.setSpeciality(extractUuid(map.get("speciality")));
        }

        // Education year
        if (map.containsKey("educationYear")) {
            Object eyVal = map.get("educationYear");
            if (eyVal instanceof Map) {
                entity.setEducationYear(extractCode(eyVal));
            } else {
                entity.setEducationYear(getStringValue(eyVal));
            }
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

    private void putClassifier(Map<String, Object> map, String key, String code, Boolean returnNulls) {
        if (code != null) {
            Map<String, Object> classifier = new LinkedHashMap<>();
            classifier.put("code", code);
            map.put(key, classifier);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    /**
     * CUBA format: {"code": "string"} - faqat Map qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object code = nested.get("code");
            return code != null ? code.toString() : null;
        }
        return null;
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
            String dateStr = value.toString().trim();
            // Handle timezone offset like "1986-02-15 +06"
            int spaceIdx = dateStr.indexOf(' ');
            if (spaceIdx > 0) {
                dateStr = dateStr.substring(0, spaceIdx);
            }
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            log.warn("Invalid date format: {}", value);
            return null;
        }
    }
}
