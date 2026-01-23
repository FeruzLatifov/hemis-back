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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.AdministrativeStudent2;
import uz.hemis.domain.repository.AdministrativeStudent2Repository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Inspeksiya Administrative Student2 Entity Controller
 *
 * <p><strong>OLD-HEMIS Compatible REST API</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_RIAdministrativeStudent2</li>
 *   <li>Table: hemishe_ri_administrative_student2</li>
 *   <li>Base URL: /app/rest/v2/entities/hemishe_RIAdministrativeStudent2</li>
 * </ul>
 *
 * <p><strong>Ma'lumot:</strong></p>
 * Xorij OTMlari bilan akademik almashinuv dasturlari (talabalar tomonidan).
 * Рейтинг аниқланаётган йилда хорижий олий таълим муассасалари билан
 * академик алмашув дастурлари (талабалар томонидан) тўғрисида маълумот.
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET / - Barcha yozuvlarni olish (paginated)</li>
 *   <li>GET /{entityId} - ID bo'yicha olish</li>
 *   <li>POST / - Yangi yozuv yaratish</li>
 *   <li>PUT /{entityId} - Yozuvni yangilash</li>
 *   <li>DELETE /{entityId} - Yozuvni o'chirish (soft delete)</li>
 *   <li>GET /search - Qidirish (GET)</li>
 *   <li>POST /search - Qidirish (POST)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "39.Inspeksiya administrative student", description = "Xorij OTMlari bilan akademik almashinuv dasturlari - talabalar (hemishe_RIAdministrativeStudent2)")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeStudent2")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeStudent2EntityController {

    private final AdministrativeStudent2Repository repository;

    private static final String ENTITY_NAME = "hemishe_RIAdministrativeStudent2";

    // =============================
    // GET by ID
    // =============================
    @GetMapping("/{entityId}")
    @Operation(
            summary = "Yozuvni ID bo'yicha olish",
            description = """
                    Akademik almashinuv yozuvini UUID bo'yicha olish.

                    **OLD-HEMIS Compatible** - CUBA REST API format
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Yozuv topildi"),
            @ApiResponse(responseCode = "404", description = "Yozuv topilmadi")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Entity UUID", example = "00000000-0000-0000-0000-000000000000")
            @PathVariable("entityId") UUID entityId,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("GET hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =============================
    // UPDATE (PUT)
    // =============================
    @PutMapping("/{entityId}")
    @Operation(
            summary = "Yozuvni yangilash",
            description = """
                    Mavjud akademik almashinuv yozuvini yangilash.

                    **OLD-HEMIS Compatible** - CUBA REST API format
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Yozuv yangilandi"),
            @ApiResponse(responseCode = "404", description = "Yozuv topilmadi")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Entity UUID")
            @PathVariable("entityId") UUID entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("UPDATE hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeStudent2 entity = existingOpt.get();
        updateEntityFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        AdministrativeStudent2 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    // =============================
    // DELETE (soft delete)
    // =============================
    @DeleteMapping("/{entityId}")
    @Operation(
            summary = "Yozuvni o'chirish",
            description = """
                    Akademik almashinuv yozuvini o'chirish (soft delete).

                    **OLD-HEMIS Compatible** - CUBA REST API format (bo'sh javob)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Yozuv o'chirildi (bo'sh javob)"),
            @ApiResponse(responseCode = "404", description = "Yozuv topilmadi")
    })
    @Transactional
    public ResponseEntity<Void> delete(
            @Parameter(description = "Entity UUID")
            @PathVariable("entityId") UUID entityId) {

        log.info("DELETE hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(existingOpt.get());

        // OLD-HEMIS compatible: bo'sh javob qaytarish
        return ResponseEntity.ok().build();
    }

    // =============================
    // SEARCH (GET)
    // =============================
    @GetMapping("/search")
    @Operation(
            summary = "Yozuvlarni qidirish (GET)",
            description = """
                    CUBA format filter bilan qidirish.

                    **Filter formati:**
                    ```json
                    {"conditions":[{"property":"_university","operator":"=","value":"uuid"}]}
                    ```
                    """
    )
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @Parameter(description = "CUBA JSON filter yoki matn")
            @RequestParam(value = "filter", required = false) String filter,
            @Parameter(description = "Limit")
            @RequestParam(value = "limit", defaultValue = "50") int limit,
            @Parameter(description = "Offset")
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_RIAdministrativeStudent2 (GET) - filter: {}", filter);

        Page<AdministrativeStudent2> page = repository.findAll(
                PageRequest.of(offset / Math.max(limit, 1), limit, Sort.by(Sort.Direction.DESC, "createTs"))
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (AdministrativeStudent2 entity : page.getContent()) {
            result.add(toMap(entity, returnNulls));
        }

        return ResponseEntity.ok(result);
    }

    // =============================
    // SEARCH (POST)
    // =============================
    @PostMapping("/search")
    @Operation(
            summary = "Yozuvlarni qidirish (POST)",
            description = """
                    CUBA format filter bilan qidirish (POST body).

                    **Body misoli:**
                    ```json
                    {
                      "filter": {
                        "conditions": [{"property":"_university","operator":"=","value":"uuid"}]
                      },
                      "limit": 50,
                      "offset": 0
                    }
                    ```
                    """
    )
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @Parameter(description = "Limit")
            @RequestParam(value = "limit", required = false) Integer limitParam,
            @Parameter(description = "Offset")
            @RequestParam(value = "offset", required = false) Integer offsetParam,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_RIAdministrativeStudent2 (POST) - filter: {}", filterBody);

        // OLD-HEMIS compatible: limit/offset ni avval body dan olish, keyin query param dan
        int limit = 50;
        int offset = 0;

        if (filterBody != null) {
            if (filterBody.containsKey("limit")) {
                Object bodyLimit = filterBody.get("limit");
                if (bodyLimit instanceof Number) {
                    limit = ((Number) bodyLimit).intValue();
                }
            }
            if (filterBody.containsKey("offset")) {
                Object bodyOffset = filterBody.get("offset");
                if (bodyOffset instanceof Number) {
                    offset = ((Number) bodyOffset).intValue();
                }
            }
        }
        // Query param overrides body
        if (limitParam != null) limit = limitParam;
        if (offsetParam != null) offset = offsetParam;

        Page<AdministrativeStudent2> page = repository.findAll(
                PageRequest.of(offset / Math.max(limit, 1), limit, Sort.by(Sort.Direction.DESC, "createTs"))
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (AdministrativeStudent2 entity : page.getContent()) {
            result.add(toMap(entity, returnNulls));
        }

        return ResponseEntity.ok(result);
    }

    // =============================
    // GET ALL (paginated)
    // =============================
    @GetMapping
    @Operation(
            summary = "Barcha yozuvlarni olish",
            description = """
                    Barcha akademik almashinuv yozuvlarini olish (paginated).

                    **OLD-HEMIS Compatible** - CUBA REST API format
                    """
    )
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Jami sonni qaytarish (X-Total-Count header)")
            @RequestParam(value = "returnCount", required = false) Boolean returnCount,
            @Parameter(description = "Offset")
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @Parameter(description = "Limit")
            @RequestParam(value = "limit", defaultValue = "50") int limit,
            @Parameter(description = "Sort (field-direction)")
            @RequestParam(value = "sort", required = false) String sort,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("LIST ALL hemishe_RIAdministrativeStudent2");

        Sort sorting = Sort.by(Sort.Direction.DESC, "createTs");
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        Page<AdministrativeStudent2> page = repository.findAll(
                PageRequest.of(offset / Math.max(limit, 1), limit, sorting)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (AdministrativeStudent2 entity : page.getContent()) {
            result.add(toMap(entity, returnNulls));
        }

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    // =============================
    // CREATE (POST) - OLD-HEMIS compatible
    // =============================
    @PostMapping
    @Operation(
            summary = "Yangi yozuv yaratish",
            description = """
                    Yangi akademik almashinuv yozuvi yaratish.

                    **OLD-HEMIS Compatible** - CUBA REST API format

                    **Body misoli:**
                    ```json
                    {
                      "_university": {"id": "uuid-string"},
                      "_education_year": {"id": "uuid-string"},
                      "exchange_document": "Shartnoma raqami",
                      "student_fullname": "Talaba FIO",
                      "_country": {"id": "uuid-string"},
                      "exchange_university_name": "Harvard University",
                      "education_type": {"id": "uuid-string"},
                      "speciality_code": "5110100",
                      "speciality_name": "Informatika va axborot texnologiyalari",
                      "exchange_type": "Bir semestr"
                    }
                    ```
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Yozuv yaratildi",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov")
    })
    @Transactional
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> create(
            @RequestBody Object body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("CREATE hemishe_RIAdministrativeStudent2: {}", body);

        // OLD-HEMIS xulqi:
        // - Bitta obyekt yuborilsa → bitta obyekt qaytaradi
        // - Massiv yuborilsa → massiv qaytaradi
        boolean isArrayRequest = body instanceof List;

        List<Map<String, Object>> items;
        if (body instanceof List) {
            items = (List<Map<String, Object>>) body;
        } else if (body instanceof Map) {
            items = List.of((Map<String, Object>) body);
        } else {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Invalid request body");
            error.put("details", "Body must be an array or object");
            return ResponseEntity.badRequest().body(error);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> item : items) {
            AdministrativeStudent2 entity = new AdministrativeStudent2();
            updateEntityFromMap(entity, item);
            entity.setCreateTs(LocalDateTime.now());

            AdministrativeStudent2 saved = repository.save(entity);

            // OLD-HEMIS format response
            results.add(toMap(saved, returnNulls));
        }

        // OLD-HEMIS compatibility:
        // - Massiv yuborilgan → massiv qaytarish
        // - Bitta obyekt yuborilgan → bitta obyekt qaytarish
        if (isArrayRequest) {
            return ResponseEntity.ok(results);
        }
        return ResponseEntity.ok(results.get(0));
    }

    // =============================
    // HELPER METHODS
    // =============================

    private void updateEntityFromMap(AdministrativeStudent2 entity, Map<String, Object> body) {
        // _university - UUID (CUBA format: {"id": "uuid"})
        if (body.containsKey("_university")) {
            entity.setUniversity(extractUuid(body.get("_university")));
        }

        // _education_year - UUID
        if (body.containsKey("_education_year")) {
            entity.setEducationYear(extractUuid(body.get("_education_year")));
        }

        // exchange_document - String
        if (body.containsKey("exchange_document")) {
            Object val = body.get("exchange_document");
            entity.setExchangeDocument(val != null ? val.toString() : null);
        }

        // student_fullname - String
        if (body.containsKey("student_fullname")) {
            Object val = body.get("student_fullname");
            entity.setStudentFullname(val != null ? val.toString() : null);
        }

        // _country - UUID
        if (body.containsKey("_country")) {
            entity.setCountry(extractUuid(body.get("_country")));
        }

        // exchange_university_name - String
        if (body.containsKey("exchange_university_name")) {
            Object val = body.get("exchange_university_name");
            entity.setExchangeUniversityName(val != null ? val.toString() : null);
        }

        // education_type - UUID (underscore-siz ham qo'llab-quvvatlash)
        if (body.containsKey("education_type")) {
            entity.setEducationType(extractUuid(body.get("education_type")));
        } else if (body.containsKey("_education_type")) {
            entity.setEducationType(extractUuid(body.get("_education_type")));
        }

        // speciality_code - String
        if (body.containsKey("speciality_code")) {
            Object val = body.get("speciality_code");
            entity.setSpecialityCode(val != null ? val.toString() : null);
        }

        // speciality_name - String
        if (body.containsKey("speciality_name")) {
            Object val = body.get("speciality_name");
            entity.setSpecialityName(val != null ? val.toString() : null);
        }

        // exchange_type - String
        if (body.containsKey("exchange_type")) {
            Object val = body.get("exchange_type");
            entity.setExchangeType(val != null ? val.toString() : null);
        }
    }

    /**
     * CUBA format: {"id": "uuid-string"} - faqat Map qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        if (value instanceof String str && !str.isEmpty()) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
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

    private Map<String, Object> toMap(AdministrativeStudent2 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId());

        // Entity-specific fields
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_education_year", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "exchange_document", entity.getExchangeDocument(), returnNulls);
        putIfNotNull(map, "student_fullname", entity.getStudentFullname(), returnNulls);
        putIfNotNull(map, "_country", entity.getCountry(), returnNulls);
        putIfNotNull(map, "exchange_university_name", entity.getExchangeUniversityName(), returnNulls);
        putIfNotNull(map, "education_type", entity.getEducationType(), returnNulls);
        putIfNotNull(map, "speciality_code", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "speciality_name", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "exchange_type", entity.getExchangeType(), returnNulls);

        // BaseEntity audit fields
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    private String buildInstanceName(AdministrativeStudent2 entity) {
        StringBuilder sb = new StringBuilder();
        if (entity.getStudentFullname() != null) {
            sb.append(entity.getStudentFullname());
        }
        if (entity.getExchangeUniversityName() != null) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(entity.getExchangeUniversityName());
        }
        if (sb.length() == 0) {
            sb.append("AdministrativeStudent2-").append(entity.getId());
        }
        return sb.toString();
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
