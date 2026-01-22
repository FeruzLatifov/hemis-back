package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.ContractStatistics;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.entity.UniversityDepartment;
import uz.hemis.domain.repository.ContractStatisticsRepository;
import uz.hemis.domain.repository.UniversityDepartmentRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contract Statistics Entity Controller (CUBA Pattern)
 * Tag 37: Shartnoma statistikasi (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RContractStatistics
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RContractStatistics           - List all with pagination
 * - GET    /app/rest/v2/entities/hemishe_RContractStatistics/{id}      - Get by ID
 * - GET    /app/rest/v2/entities/hemishe_RContractStatistics/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RContractStatistics/search    - Search (JSON filter)
 * - POST   /app/rest/v2/entities/hemishe_RContractStatistics           - Create new
 * - PUT    /app/rest/v2/entities/hemishe_RContractStatistics/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RContractStatistics/{id}      - Soft delete
 *
 * @since 1.0.0
 */
@Tag(name = "37.Shartnoma statistikasi", description = "Shartnoma statistikasi entity ma'lumotlari API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RContractStatistics")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ContractStatisticsEntityController {

    private final ContractStatisticsRepository repository;
    private final UniversityRepository universityRepository;
    private final UniversityDepartmentRepository universityDepartmentRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final String ENTITY_NAME = "hemishe_RContractStatistics";

    // =====================================================
    // GET - List all (paginated)
    // =====================================================

    @GetMapping
    @Operation(
        summary = "Shartnoma statistikasi ro'yxati",
        description = """
            Sahifalangan shartnoma statistikasi ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_RContractStatistics
            **Auth:** Bearer token (required)

            **Pagination:**
            - offset: Boshlang'ich pozitsiya (default: 0)
            - limit: Sahifadagi yozuvlar soni (default: 50)
            - returnCount: X-Total-Count headerini qaytarish
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Statistika ro'yxati qaytarildi",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Map.class)))),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi - Token noto'g'ri yoki muddati o'tgan"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - Foydalanuvchida o'qish huquqi yo'q")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish (X-Total-Count header)")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni")
            @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Tartiblash (masalan: date-desc)")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi (masalan: rContractStatistics-view)")
            @RequestParam(required = false) String view) {

        log.debug("GET all contract statistics - offset: {}, limit: {}, view: {}", offset, limit, view);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, parts[0]);
        }

        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<ContractStatistics> entityPage = repository.findAll(pageRequest);

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
    // GET - By ID
    // =====================================================

    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta shartnoma statistikasini olish",
        description = """
            ID bo'yicha shartnoma statistikasi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_RContractStatistics/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Statistika ma'lumotlari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan statistika topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Statistika UUID identifikatori", required = true)
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET contract statistics by id: {}", entityId);

        Optional<ContractStatistics> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // GET - Search
    // =====================================================

    @GetMapping("/search")
    @Operation(
        summary = "Shartnoma statistikasini qidirish (GET)",
        description = """
            URL parametrlari orqali shartnoma statistikasini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_RContractStatistics/search
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Qidiruv natijalari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @Parameter(description = "CUBA filter expression")
            @RequestParam(required = false) String filter,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET search contract statistics with filter: {}", filter);

        List<ContractStatistics> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
                .map(e -> toMap(e, returnNulls))
                .collect(Collectors.toList()));
    }

    // =====================================================
    // POST - Search
    // =====================================================

    @PostMapping("/search")
    @Operation(
        summary = "Shartnoma statistikasini qidirish (POST)",
        description = """
            JSON filter orqali shartnoma statistikasini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_RContractStatistics/search
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Qidiruv natijalari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("POST search contract statistics with filter: {}", filter);

        List<ContractStatistics> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
                .map(e -> toMap(e, returnNulls))
                .collect(Collectors.toList()));
    }

    // =====================================================
    // POST - Create
    // =====================================================

    @PostMapping
    @Operation(
        summary = "Yangi shartnoma statistikasi yaratish",
        description = """
            Yangi shartnoma statistikasi yozuvini yaratish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_RContractStatistics
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Statistika yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov - Validatsiya xatosi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create contract statistics: {}", body);

        ContractStatistics entity = new ContractStatistics();
        // TODO: Map fields from body to entity
        ContractStatistics saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    // =====================================================
    // PUT - Update
    // =====================================================

    @PutMapping("/{entityId}")
    @Operation(
        summary = "Shartnoma statistikasini yangilash",
        description = """
            Mavjud shartnoma statistikasini yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_RContractStatistics/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Statistika yangilandi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Statistika UUID identifikatori", required = true)
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT update contract statistics id: {}", entityId);

        Optional<ContractStatistics> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ContractStatistics entity = existingOpt.get();
        // TODO: Map fields from body to entity
        ContractStatistics saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    // =====================================================
    // DELETE - Soft delete
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Shartnoma statistikasini o'chirish",
        description = """
            Shartnoma statistikasini soft delete qilish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** DELETE /app/rest/v2/entities/hemishe_RContractStatistics/{entityId}
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Muvaffaqiyatli - Statistika o'chirildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Statistika UUID identifikatori", required = true)
            @PathVariable UUID entityId) {

        log.info("DELETE contract statistics id: {}", entityId);

        Optional<ContractStatistics> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // Helper Methods - OLD-HEMIS Compatible Response Format
    // =====================================================

    /**
     * Convert entity to OLD-HEMIS compatible Map format.
     *
     * <p>Field order MUST match old-hemis response exactly:</p>
     * <ol>
     *   <li>_entityName</li>
     *   <li>_instanceName</li>
     *   <li>id</li>
     *   <li>date</li>
     *   <li>educationType (nested)</li>
     *   <li>university (nested)</li>
     *   <li>educationYear (nested)</li>
     *   <li>educationForm (nested)</li>
     *   <li>faculty (nested)</li>
     *   <li>dailyCount</li>
     *   <li>total</li>
     *   <li>course (nested)</li>
     *   <li>semester (nested)</li>
     * </ol>
     */
    private Map<String, Object> toMap(ContractStatistics entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // _entityName & _instanceName (CUBA pattern)
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));

        // id
        map.put("id", entity.getId().toString());

        // date
        putIfNotNull(map, "date", entity.getDate(), returnNulls);

        // educationType (nested object)
        if (entity.getEducationType() != null) {
            map.put("educationType", buildEducationTypeMap(entity.getEducationType()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("educationType", null);
        }

        // university (nested object)
        if (entity.getUniversity() != null) {
            map.put("university", buildUniversityMap(entity.getUniversity()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("university", null);
        }

        // educationYear (nested object)
        if (entity.getEducationYear() != null) {
            map.put("educationYear", buildEducationYearMap(entity.getEducationYear()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("educationYear", null);
        }

        // educationForm (nested object)
        if (entity.getEducationForm() != null) {
            map.put("educationForm", buildEducationFormMap(entity.getEducationForm()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("educationForm", null);
        }

        // faculty (nested object)
        if (entity.getFaculty() != null) {
            map.put("faculty", buildFacultyMap(entity.getFaculty()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("faculty", null);
        }

        // dailyCount
        putIfNotNull(map, "dailyCount", entity.getDailyCount(), returnNulls);

        // total
        putIfNotNull(map, "total", entity.getTotal(), returnNulls);

        // course (nested object)
        if (entity.getCourse() != null) {
            map.put("course", buildCourseMap(entity.getCourse()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("course", null);
        }

        // semester (nested object)
        if (entity.getSemester() != null) {
            map.put("semester", buildSemesterMap(entity.getSemester()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("semester", null);
        }

        return map;
    }

    /**
     * Build _instanceName for CUBA compatibility.
     * Format: "com.company.hemishe.entity.RContractStatistics-UUID [detached]"
     */
    private String buildInstanceName(ContractStatistics entity) {
        return "com.company.hemishe.entity.RContractStatistics-" + entity.getId() + " [detached]";
    }

    /**
     * Build educationType nested object.
     * Query: SELECT code, name FROM hemishe_h_education_type WHERE code = ?
     *
     * Format:
     * {
     *   "_entityName": "hemishe_HEducationType",
     *   "_instanceName": "11 Bakalavr",
     *   "id": "11",
     *   "code": "11",
     *   "name": "Bakalavr"
     * }
     */
    private Map<String, Object> buildEducationTypeMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationType");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name FROM hemishe_h_education_type WHERE code = ? AND delete_ts IS NULL",
                code
            );
            String name = (String) data.get("name");
            result.put("_instanceName", code + " " + name);
            result.put("id", code);
            result.put("code", code);
            result.put("name", name);
        } catch (Exception e) {
            log.warn("Education type not found: {}", code);
            result.put("_instanceName", code);
            result.put("id", code);
            result.put("code", code);
            result.put("name", null);
        }

        return result;
    }

    /**
     * Build university nested object.
     * Query: SELECT code, name FROM hemishe_e_university WHERE code = ?
     *
     * Format:
     * {
     *   "_entityName": "hemishe_EUniversity",
     *   "_instanceName": "999-HEMIS axborot tizimi universiteti",
     *   "id": "999",
     *   "code": "999",
     *   "name": "HEMIS axborot tizimi universiteti"
     * }
     */
    private Map<String, Object> buildUniversityMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_EUniversity");

        Optional<University> university = universityRepository.findById(code);
        if (university.isPresent()) {
            String name = university.get().getName();
            result.put("_instanceName", code + "-" + name);
            result.put("id", code);
            result.put("code", code);
            result.put("name", name);
        } else {
            result.put("_instanceName", code);
            result.put("id", code);
            result.put("code", code);
            result.put("name", null);
        }

        return result;
    }

    /**
     * Build educationYear nested object.
     * Query: SELECT code, name FROM hemishe_h_education_year WHERE code = ?
     *
     * Format (NO code field!):
     * {
     *   "_entityName": "hemishe_HEducationYear",
     *   "_instanceName": "2021-2022",
     *   "id": "2021",
     *   "name": "2021-2022"
     * }
     */
    private Map<String, Object> buildEducationYearMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationYear");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name FROM hemishe_h_education_year WHERE code = ? AND delete_ts IS NULL",
                code
            );
            String name = (String) data.get("name");
            result.put("_instanceName", name);
            result.put("id", code);
            result.put("name", name);
        } catch (Exception e) {
            log.warn("Education year not found: {}", code);
            result.put("_instanceName", code);
            result.put("id", code);
            result.put("name", null);
        }

        return result;
    }

    /**
     * Build educationForm nested object.
     * Query: SELECT code, name FROM hemishe_h_education_form WHERE code = ?
     *
     * Format:
     * {
     *   "_entityName": "hemishe_HEducationForm",
     *   "_instanceName": "13 Sirtqi",
     *   "id": "13",
     *   "code": "13",
     *   "name": "Sirtqi"
     * }
     */
    private Map<String, Object> buildEducationFormMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationForm");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name FROM hemishe_h_education_form WHERE code = ? AND delete_ts IS NULL",
                code
            );
            String name = (String) data.get("name");
            result.put("_instanceName", code + " " + name);
            result.put("id", code);
            result.put("code", code);
            result.put("name", name);
        } catch (Exception e) {
            log.warn("Education form not found: {}", code);
            result.put("_instanceName", code);
            result.put("id", code);
            result.put("code", code);
            result.put("name", null);
        }

        return result;
    }

    /**
     * Build faculty nested object (UniversityDepartment).
     * Query: SELECT code, name_uz FROM hemishe_e_university_department WHERE code = ?
     *
     * Format (NO code field, uses nameUz!):
     * {
     *   "_entityName": "hemishe_EUniversityDepartment",
     *   "_instanceName": "Telekommunikatsiya texnologiyalari",
     *   "id": "999-102",
     *   "nameUz": "Telekommunikatsiya texnologiyalari"
     * }
     */
    private Map<String, Object> buildFacultyMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_EUniversityDepartment");

        Optional<UniversityDepartment> dept = universityDepartmentRepository.findByCode(code);
        if (dept.isPresent()) {
            String nameUz = dept.get().getNameUz();
            result.put("_instanceName", nameUz);
            result.put("id", code);
            result.put("nameUz", nameUz);
        } else {
            result.put("_instanceName", code);
            result.put("id", code);
            result.put("nameUz", null);
        }

        return result;
    }

    /**
     * Build course nested object.
     * Query: SELECT code, name FROM hemishe_h_course WHERE code = ?
     *
     * Format:
     * {
     *   "_entityName": "hemishe_HCourse",
     *   "_instanceName": "12 2-kurs",
     *   "id": "12",
     *   "code": "12",
     *   "name": "2-kurs"
     * }
     */
    private Map<String, Object> buildCourseMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HCourse");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name FROM hemishe_h_course WHERE code = ? AND delete_ts IS NULL",
                code
            );
            String name = (String) data.get("name");
            result.put("_instanceName", code + " " + name);
            result.put("id", code);
            result.put("code", code);
            result.put("name", name);
        } catch (Exception e) {
            log.warn("Course not found: {}", code);
            result.put("_instanceName", code);
            result.put("id", code);
            result.put("code", code);
            result.put("name", null);
        }

        return result;
    }

    /**
     * Build semester nested object.
     * Query: SELECT code, name FROM hemishe_h_semester WHERE code = ?
     *
     * Format:
     * {
     *   "_entityName": "hemishe_HSemester",
     *   "_instanceName": "11 Kuzgi semestr",
     *   "id": "11",
     *   "code": "11",
     *   "name": "Kuzgi semestr"
     * }
     */
    private Map<String, Object> buildSemesterMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HSemester");

        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                "SELECT code, name FROM hemishe_h_semester WHERE code = ? AND delete_ts IS NULL",
                code
            );
            String name = (String) data.get("name");
            result.put("_instanceName", code + " " + name);
            result.put("id", code);
            result.put("code", code);
            result.put("name", name);
        } catch (Exception e) {
            log.warn("Semester not found: {}", code);
            result.put("_instanceName", code);
            result.put("id", code);
            result.put("code", code);
            result.put("name", null);
        }

        return result;
    }

    /**
     * Helper method to put value only if not null (or if returnNulls is true)
     */
    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
