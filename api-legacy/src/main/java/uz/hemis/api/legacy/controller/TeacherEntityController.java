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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Teacher;
import uz.hemis.domain.repository.TeacherRepository;
import uz.hemis.domain.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher Entity Controller (CUBA Pattern)
 * Tag 05: O'qituvchilar (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_ETeacher
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_ETeacher
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_ETeacher/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_ETeacher/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_ETeacher/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_ETeacher/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_ETeacher/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_ETeacher           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_ETeacher           - Create new
 */
@Tag(name = "05.O'qituvchi")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_ETeacher")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class TeacherEntityController {

    private final TeacherRepository repository;
    private final UserRepository userRepository;
    private final CubaFilterHelper filterHelper;
    private final JdbcTemplate jdbcTemplate;
    private static final String ENTITY_NAME = "hemishe_ETeacher";

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Bitta o'qituvchi ma'lumotlarini olish",
        description = "ID bo'yicha bitta o'qituvchi ma'lumotlarini qaytaradi. view=_local - faqat asosiy fieldlar."
    )
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qo'shish") @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("GET teacher by id: {}, view: {}", entityId, view);

        Optional<Teacher> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls, view));
    }

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "O'qituvchi ma'lumotlarini o'zgartirish",
        description = "Mavjud o'qituvchi ma'lumotlarini yangilaydi. Faqat yuborilgan fieldlar o'zgaradi (partial update)."
    )
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view,
            @Parameter(description = "Response view — berilsa to'liq entity qaytariladi") @RequestParam(required = false) String responseView) {

        log.debug("PUT teacher id: {}, body keys: {}", entityId, body.keySet());

        Optional<Teacher> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Teacher entity = existingOpt.get();
        updateFromMap(entity, body);

        Teacher saved = repository.save(entity);

        // OLD-HEMIS COMPATIBLE:
        // responseView berilsa → to'liq entity qaytarish (shu viewda)
        // responseView yo'q → minimal response (faqat _entityName, _instanceName, id)
        if (responseView != null) {
            return ResponseEntity.ok(toMap(saved, returnNulls, responseView));
        }

        // Minimal response - old-hemis PUT format
        Map<String, Object> minimalResponse = new LinkedHashMap<>();
        minimalResponse.put("_entityName", ENTITY_NAME);
        String instanceName = saved.getFullName() != null && !saved.getFullName().isEmpty() ?
            saved.getFullName() : "Teacher-" + saved.getId();
        minimalResponse.put("_instanceName", instanceName);
        minimalResponse.put("id", saved.getId() != null ? saved.getId().toString() : null);
        return ResponseEntity.ok(minimalResponse);
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "O'qituvchilarni qidirish (GET)", description = "URL parametrlari orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.info("GET /search - filter: [{}], offset: {}, limit: {}", filter, offset, limit);

        List<Teacher> allEntities = repository.findAll();
        log.info("Total teachers in DB: {}", allEntities.size());

        List<Teacher> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        log.info("After filter: {} results", result.size());
        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "O'qituvchilarni qidirish (POST)", description = "JSON filter orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Offset") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Limit") @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        int effectiveOffset = filterHelper.extractInt(body, "offset", offset, 0);
        int effectiveLimit = filterHelper.extractInt(body, "limit", limit, 50);
        String filterJson = filterHelper.extractFilterFromBody(body);

        log.debug("POST search - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<Teacher> allEntities = repository.findAll();
        List<Teacher> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "O'qituvchini o'chirish (soft delete)",
        description = "ID bo'yicha o'qituvchini o'chiradi. CUBA compatibility: 200 status qaytaradi."
    )
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE teacher id: {}", entityId);
        Optional<Teacher> existing = repository.findById(entityId);
        if (existing.isEmpty()) {
            Map<String, Object> error = new java.util.LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_ETeacher with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }
        try {
            Teacher entity = existing.get();
            entity.setDeleteTs(java.time.LocalDateTime.now());
            repository.save(entity);
            log.info("Teacher soft-deleted: {}", entityId);
        } catch (Exception e) {
            log.warn("Teacher delete failed: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Barcha o'qituvchilar ro'yxati", description = "Sahifalangan ro'yxat qaytaradi")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset (boshlanish nuqtasi)") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit (sahifa hajmi)") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Saralash (field-asc/desc)") @RequestParam(required = false) String sort,
            @Parameter(description = "Dinamik atributlar") @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("GET all teachers - offset: {}, limit: {}, view: {}", offset, limit, view);

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
        Page<Teacher> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    /**
     * Yangi o'qituvchi yaratish
     *
     * <p><strong>URL:</strong> {@code POST /app/rest/v2/entities/hemishe_ETeacher}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * <p><strong>Request format:</strong></p>
     * <pre>
     * {
     *   "firstname": "Islom",
     *   "lastname": "Karimov",
     *   "fathername": "Abdug'aniyevich",
     *   "pinfl": "12345678901234",
     *   "birthday": "1985-03-15",
     *   "_gender": "11",
     *   "_citizenship": "11",
     *   "_university": "520"
     * }
     * </pre>
     *
     * @param body O'qituvchi ma'lumotlari
     * @param returnNulls Null qiymatlarni qaytarish (default: false)
     * @param view View nomi (_local, _minimal, default)
     * @return Yaratilgan o'qituvchi ma'lumotlari
     */
    @PostMapping
    @Operation(
            summary = "Yangi o'qituvchi yaratish",
            description = """
                Yangi o'qituvchi yozuvini yaratish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** POST /app/rest/v2/entities/hemishe_ETeacher
                **Auth:** Bearer token (required)
                **Content-Type:** application/json

                **Shaxsiy ma'lumotlar:**
                - firstname - Ism
                - lastname - Familiya
                - fathername - Otasining ismi
                - pinfl - PINFL (14 raqam)
                - birthday - Tug'ilgan sana (YYYY-MM-DD)
                - serialNumber - Passport seriya raqami

                **Reference kodlar:**
                - _gender - Jins kodi (11=erkak, 12=ayol)
                - _citizenship - Fuqarolik kodi (11=O'zbekiston)
                - _university - OTM kodi
                - _academic_degree - Ilmiy daraja kodi
                - _academic_rank - Ilmiy unvon kodi

                **Qo'shimcha:**
                - phone - Telefon raqami
                - address - Manzil
                - employeeYear - Ishga kirgan yili
                - code - O'qituvchi kodi (auto-generate qilinadi)
                """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Yangi o'qituvchi ma'lumotlari",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TeacherCreateRequest.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Yangi o'qituvchi",
                            value = """
                                {
                                  "firstname": "Islom",
                                  "lastname": "Karimov",
                                  "fathername": "Abdug'aniyevich",
                                  "pinfl": "32305967340015",
                                  "birthday": "1985-03-15",
                                  "serialNumber": "AA1234567",
                                  "_gender": "11",
                                  "_citizenship": "11",
                                  "_university": "520",
                                  "_academic_degree": "12",
                                  "_academic_rank": "13",
                                  "phone": "+998901234567",
                                  "address": "Toshkent sh."
                                }
                                """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - O'qituvchi yaratildi",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                        {
                                          "_entityName": "hemishe_ETeacher",
                                          "_instanceName": "Karimov Islom Abdug'aniyevich",
                                          "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                          "firstname": "Islom",
                                          "lastname": "Karimov",
                                          "fathername": "Abdug'aniyevich",
                                          "pinfl": "32305967340015"
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov parametrlari"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.info("POST /app/rest/v2/entities/hemishe_ETeacher - Create/Upsert teacher");
        log.debug("Request body keys: {}", body.keySet());

        // CUBA UPSERT: if body contains 'id' and teacher exists, update instead of create
        Object idObj = body.get("id");
        if (idObj != null) {
            try {
                UUID existingId = UUID.fromString(idObj.toString());
                Optional<Teacher> existingOpt = repository.findById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    Teacher entity = existingOpt.get();
                    updateFromMap(entity, body);
                    Teacher saved = repository.save(entity);
                    return ResponseEntity.status(201).body(minimalResponse(saved));
                }
                log.info("POST with id={} — teacher not found by ID, checking code", existingId);
            } catch (IllegalArgumentException e) {
                log.debug("POST id='{}' is not a valid UUID, proceeding", idObj);
            }
        }

        // CUBA UPSERT: if body contains 'code' and teacher with that code exists, update
        Object codeObj = body.get("code");
        if (codeObj != null) {
            String code = codeObj.toString();
            Optional<Teacher> existingOpt = repository.findByCode(code);
            if (existingOpt.isPresent()) {
                log.info("POST with existing code={} — performing UPSERT (update)", code);
                Teacher entity = existingOpt.get();
                updateFromMap(entity, body);
                Teacher saved = repository.save(entity);
                return ResponseEntity.status(201).body(minimalResponse(saved));
            }
        }

        // Create new teacher
        Teacher entity = new Teacher();

        // Set ID from body if provided
        if (idObj != null) {
            try {
                entity.setId(UUID.fromString(idObj.toString()));
            } catch (IllegalArgumentException e) {
                log.debug("Ignoring invalid UUID id: {}", idObj);
            }
        }

        updateFromMap(entity, body);

        // Auto-generate code if not provided
        if (entity.getCode() == null || entity.getCode().isEmpty()) {
            String universityCode = entity.getUniversity();
            if (universityCode == null || universityCode.isEmpty()) {
                // Try to get from authenticated user
                universityCode = getUniversityCodeFromContext();
            }
            if (universityCode == null) {
                universityCode = "520"; // Default fallback
            }

            String year = entity.getEmployeeYear();
            if (year == null || year.isEmpty()) {
                year = String.valueOf(LocalDate.now().getYear());
            }

            String gender = entity.getGender();
            if (gender == null || gender.isEmpty()) {
                gender = "11"; // Default male
            }

            String generatedCode = generateUniqueTeacherCode(universityCode, year, gender);
            entity.setCode(generatedCode);
            log.info("Auto-generated teacher code: {}", generatedCode);
        }

        Teacher saved = repository.save(entity);

        log.info("Teacher created successfully with id: {}, code: {}", saved.getId(), saved.getCode());
        return ResponseEntity.status(201).body(minimalResponse(saved));
    }

    /**
     * Swagger schema uchun request class
     */
    @io.swagger.v3.oas.annotations.media.Schema(description = "O'qituvchi yaratish so'rovi")
    public static class TeacherCreateRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Ism", example = "Islom")
        public String firstname;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Familiya", example = "Karimov")
        public String lastname;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Otasining ismi", example = "Abdug'aniyevich")
        public String fathername;

        @io.swagger.v3.oas.annotations.media.Schema(description = "PINFL (14 raqam)", example = "32305967340015")
        public String pinfl;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Tug'ilgan sana (YYYY-MM-DD)", example = "1985-03-15")
        public String birthday;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Passport seriya raqami", example = "AA1234567")
        public String serialNumber;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Jins kodi (11=erkak, 12=ayol)", example = "11")
        public String _gender;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Fuqarolik kodi (11=O'zbekiston)", example = "11")
        public String _citizenship;

        @io.swagger.v3.oas.annotations.media.Schema(description = "OTM kodi", example = "520")
        public String _university;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Ilmiy daraja kodi", example = "12")
        public String _academic_degree;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Ilmiy unvon kodi", example = "13")
        public String _academic_rank;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Telefon raqami", example = "+998901234567")
        public String phone;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Manzil", example = "Toshkent sh.")
        public String address;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Ishga kirgan yili", example = "2015")
        public String employeeYear;
    }

    /**
     * Teacher entity ni CUBA Map formatiga o'tkazish
     *
     * view=_local: faqat oddiy (scalar) fieldlar
     * view=eTeacher-view: barcha fieldlar + nested classifier objectlar
     * default (null): oddiy fieldlar + fullname + version
     */
    private Map<String, Object> toMap(Teacher entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();

        // CUBA metadata
        map.put("_entityName", ENTITY_NAME);

        String instanceName = entity.getFullName() != null && !entity.getFullName().isEmpty() ?
            entity.getFullName() : "Teacher-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        boolean isViewMode = view != null && view.contains("eTeacher");

        if ("_local".equals(view)) {
            // _local view: faqat scalar fieldlar (old-hemis tartibi)
            putIfNotNull(map, "pinfl", entity.getPinfl(), returnNulls);
            putIfNotNull(map, "birthday", entity.getBirthDate(), returnNulls);
            putIfNotNull(map, "firstname", entity.getFirstName(), returnNulls);
            putIfNotNull(map, "code", entity.getCode(), returnNulls);
            putIfNotNull(map, "tag", entity.getTag(), returnNulls);
            putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
            putIfNotNull(map, "address", entity.getAddress(), returnNulls);
            putIfNotNull(map, "lastname", entity.getSecondName(), returnNulls);
            putIfNotNull(map, "fathername", entity.getThirdName(), returnNulls);
            putIfNotNull(map, "phone", entity.getPhone(), returnNulls);
            putIfNotNull(map, "employeeYear", entity.getEmployeeYear(), returnNulls);
        } else {
            // Default va eTeacher-view: barcha fieldlar
            putIfNotNull(map, "pinfl", entity.getPinfl(), returnNulls);
            putIfNotNull(map, "birthday", entity.getBirthDate(), returnNulls);
            putIfNotNull(map, "firstname", entity.getFirstName(), returnNulls);
            putIfNotNull(map, "lastname", entity.getSecondName(), returnNulls);
            putIfNotNull(map, "fathername", entity.getThirdName(), returnNulls);
            putIfNotNull(map, "code", entity.getCode(), returnNulls);
            putIfNotNull(map, "tag", entity.getTag(), returnNulls);
            putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
            putIfNotNull(map, "address", entity.getAddress(), returnNulls);
            putIfNotNull(map, "phone", entity.getPhone(), returnNulls);
            putIfNotNull(map, "employeeYear", entity.getEmployeeYear(), returnNulls);
            putIfNotNull(map, "fullname", entity.getFullName(), returnNulls);
            putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        }

        // eTeacher-view: nested classifier objectlarni qo'shish
        if (isViewMode) {
            putClassifier(map, "citizenship", entity.getCitizenship(),
                    "hemishe_HCitizenship", "hemishe_h_citizenship", returnNulls);
            putClassifier(map, "gender", entity.getGender(),
                    "hemishe_HGender", "hemishe_h_gender", returnNulls);
            putClassifier(map, "academicDegree", entity.getAcademicDegree(),
                    "hemishe_HAcademicDegree", "hemishe_h_academic_degree", returnNulls);
            putClassifier(map, "academicRank", entity.getAcademicRank(),
                    "hemishe_HAcademicRank", "hemishe_h_academic_rank", returnNulls);
            putClassifier(map, "position", entity.getPosition(),
                    "hemishe_HTeacherPositionType", "hemishe_h_teacher_position_type", returnNulls);
            putClassifier(map, "employeeType", entity.getEmployeeType(),
                    "hemishe_HUniversityEmployeeType", "hemishe_h_university_employee_type", returnNulls);
            putClassifier(map, "employmentForm", entity.getEmploymentForm(),
                    "hemishe_HEmploymentForm", "hemishe_h_employment_form", returnNulls);
            putClassifier(map, "universityEmploymentForm", entity.getUniversityEmploymentForm(),
                    "hemishe_HUniversityEmployeeForm", "hemishe_h_university_employee_form", returnNulls);

            // university - entity, not classifier
            putUniversity(map, entity.getUniversity(), returnNulls);

            // department - entity with nested university
            putDepartment(map, entity.getDepartment(), returnNulls);

            // soato - special structure with parent_code
            putSoato(map, "soatoRegion", entity.getSoatoRegion(), returnNulls);
            putSoato(map, "soatoDistrict", entity.getSoatoDistrict(), returnNulls);

            // jobs - OneToMany relation (separate query)
            putJobs(map, entity.getCode(), entity.getUniversity(), returnNulls);
        }

        return map;
    }

    /**
     * CUBA POST response format: faqat {_entityName, _instanceName, id}
     */
    private Map<String, Object> minimalResponse(Teacher entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        String instanceName = entity.getFullName() != null && !entity.getFullName().isEmpty() ?
            entity.getFullName() : "Teacher-" + entity.getId();
        map.put("_instanceName", instanceName);
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);
        return map;
    }

    // Backward compatibility uchun overload
    private Map<String, Object> toMap(Teacher entity, Boolean returnNulls) {
        return toMap(entity, returnNulls, null);
    }

    // =============================================
    // Nested object builders for eTeacher-view
    // =============================================

    private void putClassifier(Map<String, Object> map, String key, String code,
                               String entityName, String tableName, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put(key, null);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT * FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL",
                    code);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            String name = row.get("name") != null ? row.get("name").toString() : "";
            nested.put("_instanceName", code + " " + name);
            nested.put("id", code);
            nested.put("code", code);
            if (row.containsKey("name")) nested.put("name", row.get("name"));
            if (row.containsKey("name_ru")) nested.put("nameRu", row.get("name_ru"));
            if (row.containsKey("name_en")) nested.put("nameEn", row.get("name_en"));
            if (row.containsKey("active")) nested.put("active", row.get("active"));
            map.put(key, nested);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", entityName);
            nested.put("id", code);
            nested.put("code", code);
            map.put(key, nested);
        }
    }

    private void putUniversity(Map<String, Object> map, String code, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("university", null);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversity");
            nested.put("_instanceName", row.get("name"));
            nested.put("id", code);
            nested.put("code", code);
            nested.put("name", row.get("name"));
            map.put("university", nested);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversity");
            nested.put("id", code);
            nested.put("code", code);
            map.put("university", nested);
        }
    }

    private void putDepartment(Map<String, Object> map, String code, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("department", null);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, university_code FROM hemishe_e_university_department WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversityDepartment");
            nested.put("_instanceName", row.get("name_uz"));
            nested.put("id", code);
            nested.put("code", code);
            nested.put("name", row.get("name_uz"));

            // department ichida university nested object
            String uniCode = row.get("university_code") != null ? row.get("university_code").toString() : null;
            if (uniCode != null) {
                try {
                    Map<String, Object> uniRow = jdbcTemplate.queryForMap(
                            "SELECT code, name FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL", uniCode);
                    Map<String, Object> uniNested = new LinkedHashMap<>();
                    uniNested.put("_entityName", "hemishe_EUniversity");
                    uniNested.put("_instanceName", uniRow.get("name"));
                    uniNested.put("id", uniCode);
                    uniNested.put("code", uniCode);
                    uniNested.put("name", uniRow.get("name"));
                    nested.put("university", uniNested);
                } catch (EmptyResultDataAccessException ex) {
                    // skip
                }
            }

            map.put("department", nested);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_EUniversityDepartment");
            nested.put("id", code);
            nested.put("code", code);
            map.put("department", nested);
        }
    }

    private void putSoato(Map<String, Object> map, String key, String code, Boolean returnNulls) {
        if (code == null || code.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) map.put(key, null);
            return;
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT code, name_uz, name_ru, parent_code, active FROM hemishe_h_soato WHERE code = ? AND delete_ts IS NULL", code);
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("_entityName", "hemishe_HSoato");
            nested.put("_instanceName", code + " - " + row.get("name_uz"));
            nested.put("id", code);
            nested.put("code", code);
            nested.put("active", row.get("active"));
            nested.put("name_ru", row.get("name_ru"));
            nested.put("name_uz", row.get("name_uz"));
            map.put(key, nested);
        } catch (EmptyResultDataAccessException e) {
            if (Boolean.TRUE.equals(returnNulls)) map.put(key, null);
        }
    }

    private void putJobs(Map<String, Object> map, String teacherCode, String universityCode, Boolean returnNulls) {
        if (teacherCode == null) {
            if (Boolean.TRUE.equals(returnNulls)) map.put("jobs", Collections.emptyList());
            return;
        }
        try {
            // EEmployeeJobs jadvalidan teacher code va university bo'yicha qidirish
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, version, code, _university, _department, _position, _employee_type, " +
                    "_employment_form, _university_employment_form, tag, start_date, end_date, active " +
                    "FROM hemishe_e_employee_jobs WHERE code = ? AND delete_ts IS NULL",
                    teacherCode);
            if (!rows.isEmpty()) {
                List<Map<String, Object>> jobsList = new ArrayList<>();
                for (Map<String, Object> row : rows) {
                    Map<String, Object> job = new LinkedHashMap<>();
                    job.put("_entityName", "hemishe_EEmployeeJobs");
                    job.put("id", row.get("id"));
                    job.put("code", row.get("code"));
                    job.put("active", row.get("active"));
                    job.put("version", row.get("version"));
                    jobsList.add(job);
                }
                map.put("jobs", jobsList);
            } else {
                if (Boolean.TRUE.equals(returnNulls)) map.put("jobs", Collections.emptyList());
            }
        } catch (Exception e) {
            log.debug("Could not fetch jobs for teacher {}: {}", teacherCode, e.getMessage());
            if (Boolean.TRUE.equals(returnNulls)) map.put("jobs", Collections.emptyList());
        }
    }

    /**
     * Map dan Teacher entity ga fieldlarni o'tkazish (partial update)
     * CUBA Platform bilan 100% mos format
     *
     * Qabul qilinadigan field nomlari:
     * - firstname, lastname, fathername - ismlar
     * - pinfl, serialNumber - identifikatorlar
     * - birthday - tug'ilgan sana (YYYY-MM-DD)
     * - phone, address - kontakt
     * - employeeYear, code, tag - qo'shimcha
     * - _citizenship, _gender, _university - reference kodlar
     * - _academic_degree, _academic_rank - ilmiy darajalar
     */
    private void updateFromMap(Teacher entity, Map<String, Object> map) {
        // Personal info
        if (map.containsKey("firstname")) {
            entity.setFirstname(toStr(map.get("firstname")));
        }
        if (map.containsKey("lastname")) {
            entity.setLastname(toStr(map.get("lastname")));
        }
        if (map.containsKey("fathername")) {
            entity.setFathername(toStr(map.get("fathername")));
        }

        // Identifiers
        if (map.containsKey("pinfl")) {
            entity.setPinfl(toStr(map.get("pinfl")));
        }
        if (map.containsKey("serialNumber")) {
            entity.setSerialNumber(toStr(map.get("serialNumber")));
        }

        // Birthday - YYYY-MM-DD formatda (PHP may send "1986-05-01 +07" with timezone)
        if (map.containsKey("birthday")) {
            Object birthdayObj = map.get("birthday");
            if (birthdayObj instanceof String birthdayStr) {
                // Strip timezone offset if present (e.g., "1986-05-01 +07" → "1986-05-01")
                String dateStr = birthdayStr.trim();
                int spaceIdx = dateStr.indexOf(' ');
                if (spaceIdx > 0) {
                    dateStr = dateStr.substring(0, spaceIdx);
                }
                try {
                    entity.setBirthday(java.time.LocalDate.parse(dateStr));
                } catch (Exception e) {
                    log.warn("Could not parse birthday '{}': {}", birthdayStr, e.getMessage());
                }
            }
        }

        // Contact
        if (map.containsKey("phone")) {
            entity.setPhone(toStr(map.get("phone")));
        }
        if (map.containsKey("address")) {
            entity.setAddress(toStr(map.get("address")));
        }

        // Additional
        if (map.containsKey("employeeYear")) {
            entity.setEmployeeYear(toStr(map.get("employeeYear")));
        }
        if (map.containsKey("code")) {
            entity.setCode(toStr(map.get("code")));
        }
        if (map.containsKey("tag")) {
            entity.setTag(toStr(map.get("tag")));
        }

        // References: handle both CUBA underscore-prefixed flat format AND
        // PHP nested object format (e.g., gender: {code: "11"} or _gender: "11")
        entity.setCitizenship(extractRefCode(map, "citizenship", "_citizenship", entity.getCitizenship()));
        entity.setGender(extractRefCode(map, "gender", "_gender", entity.getGender()));
        entity.setUniversity(extractRefCode(map, "university", "_university", entity.getUniversity()));
        entity.setAcademicDegree(extractRefCode(map, "academicDegree", "_academic_degree", entity.getAcademicDegree()));
        entity.setAcademicRank(extractRefCode(map, "academicRank", "_academic_rank", entity.getAcademicRank()));
        entity.setDepartment(extractRefCode(map, "department", "_department", entity.getDepartment()));
        entity.setPosition(extractRefCode(map, "position", "_position", entity.getPosition()));
        entity.setEmployeeType(extractRefCode(map, "employeeType", "_employee_type", entity.getEmployeeType()));
        entity.setEmploymentForm(extractRefCode(map, "employmentForm", "_employment_form", entity.getEmploymentForm()));
        entity.setUniversityEmploymentForm(extractRefCode(map, "universityEmploymentForm", "_university_employment_form", entity.getUniversityEmploymentForm()));
        entity.setSoatoRegion(extractRefCode(map, "soatoRegion", "_soato_region", entity.getSoatoRegion()));
        entity.setSoatoDistrict(extractRefCode(map, "soatoDistrict", "_soato_district", entity.getSoatoDistrict()));
    }

    /**
     * Extract reference code from map — supports both formats:
     * 1. Nested object: "gender": {"code": "11"} (PHP sync format)
     * 2. Flat underscore: "_gender": "11" (CUBA format)
     * 3. Flat non-underscore: "gender": "11" (direct format)
     */
    @SuppressWarnings("unchecked")
    private String extractRefCode(Map<String, Object> map, String fieldName, String underscoreFieldName, String currentValue) {
        // First check non-underscore field (PHP sync format)
        if (map.containsKey(fieldName)) {
            Object val = map.get(fieldName);
            if (val instanceof Map) {
                Object code = ((Map<String, Object>) val).get("code");
                return code != null ? code.toString() : currentValue;
            }
            return toStr(val);
        }
        // Then check underscore-prefixed field (CUBA format)
        if (map.containsKey(underscoreFieldName)) {
            Object val = map.get(underscoreFieldName);
            if (val instanceof Map) {
                Object code = ((Map<String, Object>) val).get("code");
                return code != null ? code.toString() : currentValue;
            }
            return toStr(val);
        }
        return currentValue;
    }

    private String toStr(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        return String.valueOf(value);
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    /**
     * Generate unique teacher code with retry logic
     *
     * <p><strong>Format:</strong> {universityCode}{YY}{gender}{sequence}</p>
     * <p>Example: "5202511001" = university 520, year 2025, male (11), sequence 001</p>
     *
     * @param universityCode university code (e.g., "520")
     * @param year full year or 2-digit year (e.g., "2025" or "25")
     * @param gender gender code ("11" = male, "12" = female)
     * @return unique code (e.g., "5202511001")
     */
    private String generateUniqueTeacherCode(String universityCode, String year, String gender) {
        // Extract 2-digit year
        String yearSuffix = year;
        if (year != null && year.length() == 4) {
            yearSuffix = year.substring(2); // "2025" -> "25"
        }

        // Build prefix: {universityCode}{YY}{gender}
        String prefix = universityCode + yearSuffix + gender;

        // Find the max existing code with this prefix to avoid race conditions
        String maxCode = repository.findMaxCodeByPrefix(prefix + "%");

        long sequence;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            // Extract sequence from existing code (last 3 digits)
            String seqStr = maxCode.substring(prefix.length());
            try {
                sequence = Long.parseLong(seqStr) + 1;
            } catch (NumberFormatException e) {
                // Fallback to count-based
                sequence = repository.countByCodePrefix(prefix) + 1;
            }
        } else {
            // No existing codes with this prefix
            sequence = 1;
        }

        // Generate sequence (3 digits)
        String sequenceStr = String.format("%03d", sequence);

        return prefix + sequenceStr;
    }

    /**
     * Extract university code from security context
     */
    private String getUniversityCodeFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        // JWT token dan username olish
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaimAsString("username");
            if (username == null) {
                username = jwt.getSubject();
            }

            // User dan university olish
            if (username != null) {
                try {
                    UUID userId = UUID.fromString(jwt.getSubject());
                    var userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent() && userOpt.get().getUniversity() != null) {
                        return userOpt.get().getUniversity().getCode();
                    }
                } catch (Exception e) {
                    log.debug("Could not get university from user: {}", e.getMessage());
                }
            }
        }

        // Fallback: default test university for development
        log.warn("Using default university code for development");
        return "520";
    }
}
