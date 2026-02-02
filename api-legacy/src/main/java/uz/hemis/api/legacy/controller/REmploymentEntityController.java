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
import uz.hemis.domain.entity.REmployment;
import uz.hemis.domain.repository.REmploymentRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Bandlik Statistikasi Entity Controller
 *
 * <p><strong>OLD-HEMIS Compatible REST API</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_REmployment</li>
 *   <li>Table: hemishe_r_employment</li>
 *   <li>Base URL: /app/rest/v2/entities/hemishe_REmployment</li>
 * </ul>
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
@Tag(name = "37.Bandlik statistikasi", description = "Bitiruvchilar bandlik statistikasi (hemishe_REmployment)")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_REmployment")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class REmploymentEntityController {

    private final REmploymentRepository repository;

    private static final String ENTITY_NAME = "hemishe_REmployment";

    // =============================
    // GET by ID
    // =============================
    @GetMapping("/{entityId}")
    @Operation(
            summary = "Bandlik yozuvini ID bo'yicha olish",
            description = """
                    Bandlik statistikasi yozuvini UUID bo'yicha olish.

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

        log.info("GET hemishe_REmployment: {}", entityId);

        Optional<REmployment> entity = repository.findById(entityId);
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
            summary = "Bandlik yozuvini yangilash",
            description = """
                    Mavjud bandlik statistikasi yozuvini yangilash.

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

        log.info("UPDATE hemishe_REmployment: {}", entityId);

        Optional<REmployment> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        REmployment entity = existingOpt.get();
        updateEntityFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        REmployment saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    // =============================
    // DELETE (hard delete - jadvalda soft delete yo'q)
    // =============================
    @DeleteMapping("/{entityId}")
    @Operation(
            summary = "Bandlik yozuvini o'chirish",
            description = """
                    Bandlik statistikasi yozuvini o'chirish.
                    Bu jadvalda soft delete yo'q, shuning uchun yozuv to'liq o'chiriladi.

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

        log.info("DELETE hemishe_REmployment: {}", entityId);

        Optional<REmployment> existingOpt = repository.findById(entityId);
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
            summary = "Bandlik yozuvlarini qidirish (GET)",
            description = """
                    CUBA format filter bilan qidirish.

                    **Filter formati:**
                    ```json
                    {"conditions":[{"property":"universityCode","operator":"=","value":"401"}]}
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

        log.info("SEARCH hemishe_REmployment (GET) - filter: {}", filter);

        Page<REmployment> page = repository.findAll(
                PageRequest.of(offset / Math.max(limit, 1), limit, Sort.by(Sort.Direction.DESC, "createTs"))
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (REmployment entity : page.getContent()) {
            result.add(toMap(entity, returnNulls));
        }

        return ResponseEntity.ok(result);
    }

    // =============================
    // SEARCH (POST)
    // =============================
    @PostMapping("/search")
    @Operation(
            summary = "Bandlik yozuvlarini qidirish (POST)",
            description = """
                    CUBA format filter bilan qidirish (POST body).

                    **Body formati (OLD-HEMIS compatible):**
                    ```json
                    {"filter":{"conditions":[{"property":"qty","operator":"notEmpty"}]},"limit":10,"offset":0}
                    ```

                    **Yoki query params bilan:**
                    POST /search?limit=10&offset=0
                    Body: {"filter":{"conditions":[...]}}
                    """
    )
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @Parameter(description = "Limit (query param yoki body ichida)")
            @RequestParam(value = "limit", required = false) Integer limitParam,
            @Parameter(description = "Offset (query param yoki body ichida)")
            @RequestParam(value = "offset", required = false) Integer offsetParam,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_REmployment (POST) - filter: {}", filterBody);

        // OLD-HEMIS compatible: limit/offset ni avval body dan olish, keyin query param dan
        int limit = 50;  // default
        int offset = 0;  // default

        if (filterBody != null) {
            // Body dan limit olish
            if (filterBody.containsKey("limit")) {
                Object bodyLimit = filterBody.get("limit");
                if (bodyLimit instanceof Number) {
                    limit = ((Number) bodyLimit).intValue();
                }
            }
            // Body dan offset olish
            if (filterBody.containsKey("offset")) {
                Object bodyOffset = filterBody.get("offset");
                if (bodyOffset instanceof Number) {
                    offset = ((Number) bodyOffset).intValue();
                }
            }
        }

        // Query param mavjud bo'lsa, u ustunlik oladi
        if (limitParam != null) {
            limit = limitParam;
        }
        if (offsetParam != null) {
            offset = offsetParam;
        }

        Page<REmployment> page = repository.findAll(
                PageRequest.of(offset / Math.max(limit, 1), limit, Sort.by(Sort.Direction.DESC, "createTs"))
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (REmployment entity : page.getContent()) {
            result.add(toMap(entity, returnNulls));
        }

        return ResponseEntity.ok(result);
    }

    // =============================
    // LIST ALL (GET)
    // =============================
    @GetMapping
    @Operation(
            summary = "Barcha bandlik yozuvlarini olish",
            description = """
                    Barcha bandlik statistikasi yozuvlarini sahifalangan holda olish.

                    **OLD-HEMIS Compatible** - CUBA REST API format
                    """
    )
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listAll(
            @Parameter(description = "Limit (default: 50)")
            @RequestParam(value = "limit", defaultValue = "50") int limit,
            @Parameter(description = "Offset (default: 0)")
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @Parameter(description = "Umumiy sonni qaytarish")
            @RequestParam(value = "returnCount", required = false) Boolean returnCount,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("LIST ALL hemishe_REmployment");

        Page<REmployment> page = repository.findAll(
                PageRequest.of(offset / Math.max(limit, 1), limit, Sort.by(Sort.Direction.DESC, "createTs"))
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (REmployment entity : page.getContent()) {
            result.add(toMap(entity, returnNulls));
        }

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    // =============================
    // CREATE (POST) - OLD-HEMIS massiv formatini qo'llab-quvvatlaydi
    // =============================
    @PostMapping
    @Operation(
            summary = "Yangi bandlik yozuvi yaratish",
            description = """
                    Yangi bandlik statistikasi yozuvi yaratish.

                    **OLD-HEMIS Compatible** - CUBA REST API format

                    **Body misoli (massiv yoki bitta obyekt):**
                    ```json
                    [
                      {
                        "uId": "401",
                        "qty": 5,
                        "university": {"code": "401"},
                        "department": {"code": "401-01"},
                        "educationYear": {"code": "2024"},
                        "educationType": {"code": "11"},
                        "educationForm": {"code": "11"},
                        "paymentForm": {"code": "11"},
                        "gender": {"code": "11"},
                        "workplaceCompatibility": {"code": "11"},
                        "graduateInactiveType": {"code": "13"},
                        "graduateFieldsType": {"code": "31"}
                      }
                    ]
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

        log.info("CREATE hemishe_REmployment: {}", body);

        // OLD-HEMIS xulqi:
        // - Bitta obyekt yuborilsa → bitta obyekt qaytaradi
        // - Massiv yuborilsa → massiv qaytaradi (hatto bitta element bo'lsa ham)
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
            // UPSERT: Avval unique key bo'yicha mavjud yozuvni qidirish
            String departmentCode = extractCodeMultiKey(item, "department", "_department");
            String educationYearCode = extractCodeMultiKey(item, "educationYear", "_educationYear");
            String educationTypeCode = extractCodeMultiKey(item, "educationType", "_educationType");
            String educationFormCode = extractCodeMultiKey(item, "educationForm", "_educationForm");
            String paymentFormCode = extractCodeMultiKey(item, "paymentForm", "_paymentForm");
            String genderCode = extractCodeMultiKey(item, "gender", "_gender");
            String workplaceCompatibilityCode = extractCodeMultiKey(item, "workplaceCompatibility", "_workplaceCompatibility");
            String graduateFieldsTypeCode = extractCodeMultiKey(item, "graduateFieldsType", "_graduateFieldsType");
            String graduateInactiveTypeCode = extractCodeMultiKey(item, "graduateInactiveType", "_graduateInactiveType");

            // Mavjud yozuvni topish yoki yangi yaratish
            REmployment entity = repository.findByDepartmentCodeAndEducationYearCodeAndEducationTypeCodeAndEducationFormCodeAndPaymentFormCodeAndGenderCodeAndWorkplaceCompatibilityCodeAndGraduateFieldsTypeCodeAndGraduateInactiveTypeCode(
                    departmentCode, educationYearCode, educationTypeCode, educationFormCode,
                    paymentFormCode, genderCode, workplaceCompatibilityCode,
                    graduateFieldsTypeCode, graduateInactiveTypeCode
            ).orElse(new REmployment());

            // Entity'ni yangilash
            updateEntityFromMap(entity, item);

            REmployment saved = repository.save(entity);

            // OLD-HEMIS format response - minimal
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("_entityName", ENTITY_NAME);
            response.put("_instanceName", "com.company.hemishe.entity.REmployment-" + saved.getId() + " [detached]");
            response.put("id", saved.getId().toString());
            results.add(response);
        }

        // OLD-HEMIS compatibility:
        // - Massiv yuborilgan → massiv qaytarish
        // - Bitta obyekt yuborilgan → bitta obyekt qaytarish
        if (isArrayRequest) {
            return ResponseEntity.status(201).body(results);
        }
        return ResponseEntity.status(201).body(results.get(0));
    }

    // =============================
    // HELPER METHODS
    // =============================

    private void updateEntityFromMap(REmployment entity, Map<String, Object> body) {
        // uId - String yoki Number bo'lishi mumkin
        if (body.containsKey("uId")) {
            Object val = body.get("uId");
            if (val instanceof Number) {
                entity.setUId(((Number) val).longValue());
            } else if (val instanceof String) {
                entity.setUId(Long.parseLong((String) val));
            }
        }

        // qty - Number bo'lishi kerak
        if (body.containsKey("qty")) {
            Object val = body.get("qty");
            if (val instanceof Number) {
                entity.setQty(((Number) val).intValue());
            }
        }

        // FK fields - OLD-HEMIS: "university" yoki "_university" formatlarini qo'llab-quvvatlash
        // CUBA format: {"code": "value"} yoki {"id": "value"}
        entity.setUniversityCode(extractCodeMultiKey(body, "university", "_university"));
        entity.setDepartmentCode(extractCodeMultiKey(body, "department", "_department"));
        entity.setEducationYearCode(extractCodeMultiKey(body, "educationYear", "_educationYear"));
        entity.setEducationTypeCode(extractCodeMultiKey(body, "educationType", "_educationType"));
        entity.setEducationFormCode(extractCodeMultiKey(body, "educationForm", "_educationForm"));
        entity.setPaymentFormCode(extractCodeMultiKey(body, "paymentForm", "_paymentForm"));
        entity.setGenderCode(extractCodeMultiKey(body, "gender", "_gender"));
        entity.setWorkplaceCompatibilityCode(extractCodeMultiKey(body, "workplaceCompatibility", "_workplaceCompatibility"));
        entity.setGraduateFieldsTypeCode(extractCodeMultiKey(body, "graduateFieldsType", "_graduateFieldsType"));
        entity.setGraduateInactiveTypeCode(extractCodeMultiKey(body, "graduateInactiveType", "_graduateInactiveType"));
    }

    /**
     * OLD-HEMIS formatida key nomlar underscore bilan yoki underscore'siz bo'lishi mumkin
     */
    private String extractCodeMultiKey(Map<String, Object> body, String key1, String key2) {
        if (body.containsKey(key1)) {
            return extractCode(body.get(key1));
        }
        if (body.containsKey(key2)) {
            return extractCode(body.get(key2));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object code = nested.get("code");
            if (code != null) return code.toString();
            Object id = nested.get("id");
            if (id != null) return id.toString();
        }
        return null;
    }

    private Map<String, Object> toMap(REmployment entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId());

        putIfNotNull(map, "uId", entity.getUId(), returnNulls);
        putIfNotNull(map, "qty", entity.getQty(), returnNulls);

        // FK fields - CUBA nested format
        putNestedRef(map, "_university", entity.getUniversityCode(), returnNulls);
        putNestedRef(map, "_department", entity.getDepartmentCode(), returnNulls);
        putNestedRef(map, "_educationYear", entity.getEducationYearCode(), returnNulls);
        putNestedRef(map, "_educationType", entity.getEducationTypeCode(), returnNulls);
        putNestedRef(map, "_educationForm", entity.getEducationFormCode(), returnNulls);
        putNestedRef(map, "_paymentForm", entity.getPaymentFormCode(), returnNulls);
        putNestedRef(map, "_gender", entity.getGenderCode(), returnNulls);
        putNestedRef(map, "_workplaceCompatibility", entity.getWorkplaceCompatibilityCode(), returnNulls);
        putNestedRef(map, "_graduateFieldsType", entity.getGraduateFieldsTypeCode(), returnNulls);
        putNestedRef(map, "_graduateInactiveType", entity.getGraduateInactiveTypeCode(), returnNulls);

        // Audit fields
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);

        return map;
    }

    private String buildInstanceName(REmployment entity) {
        StringBuilder sb = new StringBuilder("Employment");
        if (entity.getUniversityCode() != null) {
            sb.append("-").append(entity.getUniversityCode());
        }
        if (entity.getEducationYearCode() != null) {
            sb.append("-").append(entity.getEducationYearCode());
        }
        if (entity.getQty() != null) {
            sb.append(" (").append(entity.getQty()).append(")");
        }
        return sb.toString();
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    private void putNestedRef(Map<String, Object> map, String key, String code, Boolean returnNulls) {
        if (code != null) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("code", code);
            map.put(key, nested);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }
}
