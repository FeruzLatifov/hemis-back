package uz.hemis.api.legacy.controller.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.adapter.JsonNull;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.domain.entity.employee.Teacher;
import uz.hemis.service.legacy.TeacherLegacyService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

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

    private final TeacherLegacyService teacherService;
    private final CubaFilterHelper filterHelper;
    private final LegacySecurityHelper securityHelper;

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/{entityId}")
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

        Optional<Teacher> entity = teacherService.findById(entityId);
        if (entity.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_ETeacher with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        Map<String, Object> result = teacherService.toTeacherMap(entity.get(), returnNulls, view);

        // OLD-HEMIS compatibility: Replace null values with JsonNull.INSTANCE
        if (Boolean.TRUE.equals(returnNulls)) {
            result.replaceAll((k, v) -> v == null ? JsonNull.INSTANCE : v);
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('teachers.edit')")
    @PutMapping("/{entityId}")
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

        Optional<Teacher> existingOpt = teacherService.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Teacher entity = existingOpt.get();
        teacherService.updateTeacherFromMap(entity, body);

        Teacher saved = teacherService.save(entity);

        if (responseView != null) {
            return ResponseEntity.ok(teacherService.toTeacherMap(saved, returnNulls, responseView));
        }

        return ResponseEntity.ok(teacherService.minimalTeacherResponse(saved));
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/search")
    @Operation(summary = "O'qituvchilarni qidirish (GET)", description = "URL parametrlari orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.info("GET /search - filter: [{}], offset: {}, limit: {}", filter, offset, limit);

        // Agar filter bo'sh bo'lsa — to'g'ridan-to'g'ri DB paginatsiya
        if (filterHelper.isEmptyFilter(filter)) {
            int page = offset / Math.max(limit, 1);
            PageRequest pageRequest = PageRequest.of(page, limit);
            return ResponseEntity.ok(teacherService.findAll(pageRequest).getContent().stream()
                .map(e -> teacherService.toTeacherMap(e, returnNulls, view))
                .collect(Collectors.toList()));
        }

        List<Teacher> allEntities = teacherService.findAll();
        log.info("Total teachers in DB: {}", allEntities.size());

        List<Teacher> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        log.info("After filter: {} results", result.size());
        return ResponseEntity.ok(result.stream()
            .map(e -> teacherService.toTeacherMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @PostMapping("/search")
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

        // Agar filter bo'sh yoki conditions bo'sh bo'lsa — to'g'ridan-to'g'ri DB paginatsiya
        if (filterHelper.isEmptyFilter(filterJson)) {
            int page = effectiveOffset / Math.max(effectiveLimit, 1);
            PageRequest pageRequest = PageRequest.of(page, effectiveLimit);
            return ResponseEntity.ok(teacherService.findAll(pageRequest).getContent().stream()
                .map(e -> teacherService.toTeacherMap(e, returnNulls, view))
                .collect(Collectors.toList()));
        }

        List<Teacher> allEntities = teacherService.findAll();
        List<Teacher> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> teacherService.toTeacherMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('teachers.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "O'qituvchini o'chirish (soft delete)",
        description = "ID bo'yicha o'qituvchini o'chiradi. CUBA compatibility: 200 status qaytaradi."
    )
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE teacher id: {}", entityId);
        Optional<Teacher> existing = teacherService.findById(entityId);
        if (existing.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_ETeacher with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }
        try {
            teacherService.softDelete(existing.get());
            log.info("Teacher soft-deleted: {}", entityId);
        } catch (Exception e) {
            log.warn("Teacher delete failed: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping
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

        int safeLimit = Math.max(limit, 1);
        int page = offset / safeLimit;
        PageRequest pageRequest = PageRequest.of(page, safeLimit, sorting);

        var entityPage = teacherService.findAll(pageRequest);
        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> teacherService.toTeacherMap(e, returnNulls, view))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
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
    @PreAuthorize("hasAuthority('teachers.edit')")
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
                Optional<Teacher> existingOpt = teacherService.findById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    Teacher entity = existingOpt.get();
                    teacherService.updateTeacherFromMap(entity, body);
                    Teacher saved = teacherService.save(entity);
                    return ResponseEntity.status(201).body(teacherService.minimalTeacherResponse(saved));
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
            Optional<Teacher> existingOpt = teacherService.findByCode(code);
            if (existingOpt.isPresent()) {
                log.info("POST with existing code={} — performing UPSERT (update)", code);
                Teacher entity = existingOpt.get();
                teacherService.updateTeacherFromMap(entity, body);
                Teacher saved = teacherService.save(entity);
                return ResponseEntity.status(201).body(teacherService.minimalTeacherResponse(saved));
            }
        }

        // Create new teacher
        Teacher entity = new Teacher();

        if (idObj != null) {
            try {
                entity.setId(UUID.fromString(idObj.toString()));
            } catch (IllegalArgumentException e) {
                log.debug("Ignoring invalid UUID id: {}", idObj);
            }
        }

        teacherService.updateTeacherFromMap(entity, body);

        // Auto-generate code if not provided
        if (entity.getCode() == null || entity.getCode().isEmpty()) {
            String universityCode = entity.getUniversity();
            if (universityCode == null || universityCode.isEmpty()) {
                universityCode = securityHelper.getUniversityCodeFromContext();
            }
            if (universityCode == null) {
                universityCode = "520";
            }

            String year = entity.getEmployeeYear();
            if (year == null || year.isEmpty()) {
                year = String.valueOf(LocalDate.now().getYear());
            }

            String gender = entity.getGender();
            if (gender == null || gender.isEmpty()) {
                gender = "11";
            }

            String generatedCode = teacherService.generateUniqueTeacherCode(universityCode, year, gender);
            entity.setCode(generatedCode);
            log.info("Auto-generated teacher code: {}", generatedCode);
        }

        Teacher saved = teacherService.save(entity);

        log.info("Teacher created successfully with id: {}, code: {}", saved.getId(), saved.getCode());
        return ResponseEntity.status(201).body(teacherService.minimalTeacherResponse(saved));
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
}
