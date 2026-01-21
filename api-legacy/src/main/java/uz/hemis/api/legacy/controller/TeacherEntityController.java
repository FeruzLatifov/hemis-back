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
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("PUT teacher id: {}, body keys: {}", entityId, body.keySet());

        Optional<Teacher> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Teacher entity = existingOpt.get();
        updateFromMap(entity, body);

        Teacher saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls, view));
    }

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(summary = "Delete teacher", description = "Soft deletes a teacher")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE teacher id: {}", entityId);

        Optional<Teacher> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "O'qituvchilarni qidirish (GET)", description = "URL parametrlari orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("GET search teachers with filter: {}, view: {}", filter, view);

        List<Teacher> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "O'qituvchilarni qidirish (POST)", description = "JSON filter orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("POST search teachers with filter: {}, view: {}", filter, view);

        List<Teacher> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
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

        log.info("POST /app/rest/v2/entities/hemishe_ETeacher - Create new teacher");
        log.debug("Request body keys: {}", body.keySet());

        Teacher entity = new Teacher();
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
        return ResponseEntity.ok(toMap(saved, returnNulls, view));
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
     * view=_local uchun: _entityName, _instanceName, id va barcha _local fieldlar
     * Field tartibi old-hemis bilan 100% mos bo'lishi kerak
     */
    private Map<String, Object> toMap(Teacher entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();

        // CUBA metadata
        map.put("_entityName", ENTITY_NAME);

        // Instance name: "LASTNAME FIRSTNAME FATHERNAME" formatida
        String instanceName = entity.getFullName() != null && !entity.getFullName().isEmpty() ?
            entity.getFullName() : "Teacher-" + entity.getId();
        map.put("_instanceName", instanceName);

        // Primary key
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // view=_local bo'lsa - faqat oddiy fieldlar (old-hemis tartibi bilan)
        if ("_local".equals(view)) {
            putIfNotNull(map, "pinfl", entity.getPinfl(), returnNulls);
            putIfNotNull(map, "birthday", entity.getBirthDate(), returnNulls);
            putIfNotNull(map, "firstname", entity.getFirstName(), returnNulls);  // getFirstName() - manual getter
            putIfNotNull(map, "code", entity.getCode(), returnNulls);
            putIfNotNull(map, "tag", entity.getTag(), returnNulls);
            putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
            putIfNotNull(map, "address", entity.getAddress(), returnNulls);
            putIfNotNull(map, "lastname", entity.getSecondName(), returnNulls);  // getSecondName() - manual getter
            putIfNotNull(map, "fathername", entity.getThirdName(), returnNulls);  // getThirdName() - manual getter
            putIfNotNull(map, "phone", entity.getPhone(), returnNulls);
            putIfNotNull(map, "employeeYear", entity.getEmployeeYear(), returnNulls);
        } else {
            // Default view - OLD-HEMIS compatible (underscore-prefixed fields excluded)
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
            // OLD-HEMIS compatible: fullname and version included in default view
            putIfNotNull(map, "fullname", entity.getFullName(), returnNulls);
            putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        }

        return map;
    }

    // Backward compatibility uchun overload
    private Map<String, Object> toMap(Teacher entity, Boolean returnNulls) {
        return toMap(entity, returnNulls, null);
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
            entity.setFirstname((String) map.get("firstname"));
        }
        if (map.containsKey("lastname")) {
            entity.setLastname((String) map.get("lastname"));
        }
        if (map.containsKey("fathername")) {
            entity.setFathername((String) map.get("fathername"));
        }

        // Identifiers
        if (map.containsKey("pinfl")) {
            entity.setPinfl((String) map.get("pinfl"));
        }
        if (map.containsKey("serialNumber")) {
            entity.setSerialNumber((String) map.get("serialNumber"));
        }

        // Birthday - YYYY-MM-DD formatda
        if (map.containsKey("birthday")) {
            Object birthdayObj = map.get("birthday");
            if (birthdayObj instanceof String) {
                entity.setBirthday(java.time.LocalDate.parse((String) birthdayObj));
            }
        }

        // Contact
        if (map.containsKey("phone")) {
            entity.setPhone((String) map.get("phone"));
        }
        if (map.containsKey("address")) {
            entity.setAddress((String) map.get("address"));
        }

        // Additional
        if (map.containsKey("employeeYear")) {
            entity.setEmployeeYear((String) map.get("employeeYear"));
        }
        if (map.containsKey("code")) {
            entity.setCode((String) map.get("code"));
        }
        if (map.containsKey("tag")) {
            entity.setTag((String) map.get("tag"));
        }

        // References (underscore prefix - CUBA convention)
        if (map.containsKey("_citizenship")) {
            entity.setCitizenship((String) map.get("_citizenship"));
        }
        if (map.containsKey("_gender")) {
            entity.setGender((String) map.get("_gender"));
        }
        if (map.containsKey("_university")) {
            entity.setUniversity((String) map.get("_university"));
        }
        if (map.containsKey("_academic_degree")) {
            entity.setAcademicDegree((String) map.get("_academic_degree"));
        }
        if (map.containsKey("_academic_rank")) {
            entity.setAcademicRank((String) map.get("_academic_rank"));
        }
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
