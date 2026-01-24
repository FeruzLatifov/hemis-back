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
    public ResponseEntity<?> getById(
            @Parameter(description = "Entity UUID", example = "00000000-0000-0000-0000-000000000000")
            @PathVariable("entityId") UUID entityId,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("GET hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            // OLD-HEMIS compatible 404 format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
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
    public ResponseEntity<?> update(
            @Parameter(description = "Entity UUID")
            @PathVariable("entityId") UUID entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("UPDATE hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            // OLD-HEMIS compatible 404 format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
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
    public ResponseEntity<?> delete(
            @Parameter(description = "Entity UUID")
            @PathVariable("entityId") UUID entityId) {

        log.info("DELETE hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            // OLD-HEMIS compatible 404 format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
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
        // OLD-HEMIS CUBA format - {"code": "..."} va camelCase field nomlari

        // university: {"code": "301"}
        if (body.containsKey("university")) {
            entity.setUniversity(extractCode(body.get("university")));
        }

        // educationYear: {"code": "2021"}
        if (body.containsKey("educationYear")) {
            entity.setEducationYear(extractCode(body.get("educationYear")));
        }

        // country: {"code": "AF"}
        if (body.containsKey("country")) {
            entity.setCountry(extractCode(body.get("country")));
        }

        // educationType: {"code": "11"}
        if (body.containsKey("educationType")) {
            entity.setEducationType(extractCode(body.get("educationType")));
        }

        // exchangeDocument - oddiy string
        if (body.containsKey("exchangeDocument")) {
            Object val = body.get("exchangeDocument");
            entity.setExchangeDocument(val != null ? val.toString() : null);
        }

        // exchangeType - oddiy string (income/outcome)
        if (body.containsKey("exchangeType")) {
            Object val = body.get("exchangeType");
            entity.setExchangeType(val != null ? val.toString() : null);
        }

        // studentFullname - oddiy string
        if (body.containsKey("studentFullname")) {
            Object val = body.get("studentFullname");
            entity.setStudentFullname(val != null ? val.toString() : null);
        }

        // exchangeUniversityName - oddiy string
        if (body.containsKey("exchangeUniversityName")) {
            Object val = body.get("exchangeUniversityName");
            entity.setExchangeUniversityName(val != null ? val.toString() : null);
        }

        // specialityName - oddiy string
        if (body.containsKey("specialityName")) {
            Object val = body.get("specialityName");
            entity.setSpecialityName(val != null ? val.toString() : null);
        }

        // specialityCode - oddiy string
        if (body.containsKey("specialityCode")) {
            Object val = body.get("specialityCode");
            entity.setSpecialityCode(val != null ? val.toString() : null);
        }
    }

    /**
     * OLD-HEMIS CUBA format: {"code": "string"} yoki to'g'ridan-to'g'ri string
     * Entity FK lar uchun (university, educationYear, country, educationType)
     */
    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof String str) {
            return str.isEmpty() ? null : str;
        }
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            // Avval "code" ni tekshir (OLD-HEMIS formati)
            Object code = nested.get("code");
            if (code != null) {
                return code.toString();
            }
            // Keyin "id" ni tekshir (alternativ format)
            Object id = nested.get("id");
            if (id != null) {
                return id.toString();
            }
        }
        return value.toString();
    }

    private Map<String, Object> toMap(AdministrativeStudent2 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS CUBA format - faqat shu maydonlar qaytariladi
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId());

        // OLD-HEMIS response format - FK fieldlar QAYTARILMAYDI (default view)
        // Faqat oddiy string maydonlar qaytariladi
        putIfNotNull(map, "exchangeDocument", entity.getExchangeDocument(), returnNulls);
        putIfNotNull(map, "exchangeType", entity.getExchangeType(), returnNulls);
        putIfNotNull(map, "studentFullname", entity.getStudentFullname(), returnNulls);
        putIfNotNull(map, "version", 1, returnNulls); // CUBA version field
        putIfNotNull(map, "exchangeUniversityName", entity.getExchangeUniversityName(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);

        return map;
    }

    private String buildInstanceName(AdministrativeStudent2 entity) {
        // OLD-HEMIS CUBA format: "com.company.hemishe.entity.RIAdministrativeStudent2-{id} [detached]"
        return "com.company.hemishe.entity.RIAdministrativeStudent2-" + entity.getId() + " [detached]";
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
