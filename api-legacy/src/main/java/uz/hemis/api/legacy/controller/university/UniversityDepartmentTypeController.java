package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.HUniversityDepartmentType;
import uz.hemis.service.legacy.ClassifierLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OTM Bo'linma Turlari Controller - CUBA REST API Pattern
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_HUniversityDepartmentType</li>
 *   <li>Table: hemishe_h_university_department_type</li>
 *   <li>Primary key: code (VARCHAR, NOT UUID!)</li>
 *   <li>Base URL: /app/rest/v2/entities/hemishe_HUniversityDepartmentType</li>
 *   <li>100% backward compatible with OLD-HEMIS CUBA Platform REST API</li>
 * </ul>
 *
 * <p><strong>Endpoints (7 ta):</strong></p>
 * <ul>
 *   <li>GET /{entityId} - ID bo'yicha olish</li>
 *   <li>PUT /{entityId} - Yangilash</li>
 *   <li>DELETE /{entityId} - O'chirish (soft delete)</li>
 *   <li>GET /search - URL parametrlari bilan qidirish</li>
 *   <li>POST /search - JSON filter bilan qidirish</li>
 *   <li>GET / - Barcha ro'yxat (sahifalangan)</li>
 *   <li>POST / - Yangi yaratish</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "08.OTM bo'linma turlari", description = "OTM bo'linma turlarini boshqarish API (Fakultet, Kafedra, Bo'lim, Markaz va h.k.)")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HUniversityDepartmentType")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityDepartmentTypeController {

    private final ClassifierLegacyService classifierService;

    private static final String ENTITY_NAME = "hemishe_HUniversityDepartmentType";

    // =====================================================
    // 1. GET BY ID (entityId = code)
    // =====================================================

    @Operation(
        summary = "Bo'linma turini ID bo'yicha olish",
        description = """
            Berilgan identifikator (code) bo'yicha bitta bo'linma turini qaytaradi.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityDepartmentType/{entityId}
            **Auth:** Bearer token (majburiy)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Bo'linma turi qaytarildi."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi. Token noto'g'ri yoki muddati o'tgan."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q. Foydalanuvchida bu ma'lumotni o'qish huquqi yo'q."),
        @ApiResponse(responseCode = "404", description = "Topilmadi. Berilgan ID bilan bo'linma turi mavjud emas.")
    })
    @GetMapping("/{entityId}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Bo'linma turi identifikatori (code)", required = true, example = "11")
            @PathVariable String entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni JSON ga yozish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        log.info("=== GET bo'linma turi === entityId={}, returnNulls={}", entityId, returnNulls);

        Optional<HUniversityDepartmentType> entity = classifierService.findDepartmentTypeById(entityId);

        if (entity.isEmpty()) {
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(classifierService.toDepartmentTypeMap(entity.get(), returnNulls));
    }

    // =====================================================
    // 2. UPDATE
    // =====================================================

    @Operation(
        summary = "Bo'linma turini yangilash",
        description = """
            Mavjud bo'linma turini yangilaydi. Faqat jo'natilgan maydonlar yangilanadi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Yangilangan bo'linma turi qaytarildi."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(
            @Parameter(description = "Bo'linma turi identifikatori (code)", required = true)
            @PathVariable String entityId,
            @RequestBody Map<String, Object> entityData) {

        log.info("UPDATE bo'linma turi: {}", entityId);

        Optional<HUniversityDepartmentType> existingOpt = classifierService.findDepartmentTypeById(entityId);

        if (existingOpt.isEmpty()) {
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        HUniversityDepartmentType entity = existingOpt.get();

        // Maydonlarni yangilash
        if (entityData.containsKey("name")) {
            entity.setName((String) entityData.get("name"));
        }
        if (entityData.containsKey("nameRu")) {
            entity.setNameRu((String) entityData.get("nameRu"));
        }
        if (entityData.containsKey("nameEn")) {
            entity.setNameEn((String) entityData.get("nameEn"));
        }
        if (entityData.containsKey("active")) {
            entity.setActive((Boolean) entityData.get("active"));
        }

        entity.setUpdateTs(LocalDateTime.now());

        HUniversityDepartmentType saved = classifierService.saveDepartmentType(entity);

        return ResponseEntity.ok(classifierService.toDepartmentTypeMap(saved, false));
    }

    // =====================================================
    // 3. DELETE (Soft Delete)
    // =====================================================

    @Operation(
        summary = "Bo'linma turini o'chirish",
        description = """
            Bo'linma turini o'chiradi (soft delete - delete_ts qo'yiladi).

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Bo'linma turi o'chirildi."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @DeleteMapping("/{entityId}")
    public ResponseEntity<?> delete(
            @Parameter(description = "Bo'linma turi identifikatori (code)", required = true)
            @PathVariable String entityId) {

        log.info("DELETE bo'linma turi: {}", entityId);

        Optional<HUniversityDepartmentType> entity = classifierService.findDepartmentTypeById(entityId);

        if (entity.isEmpty()) {
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        // Soft delete
        entity.get().setActive(false);
        classifierService.softDeleteDepartmentType(entity.get());

        return ResponseEntity.ok().build();
    }

    // =====================================================
    // 4. SEARCH (GET with filter parameter)
    // =====================================================

    @Operation(
        summary = "Bo'linma turlarini qidirish (GET)",
        description = """
            Filter shartlari bo'yicha bo'linma turlarini qidiradi.
            Filter JSON obyekt sifatida URL parametrida beriladi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Filter shartlariga mos bo'linma turlari qaytarildi."),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov. Masalan, filter qiymati parse qilib bo'lmadi."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q.")
    })
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @Parameter(description = "Filter sharti (JSON)", required = true)
            @RequestParam String filter,
            @Parameter(description = "Jami sonni 'X-Total-Count' headerda qaytarish")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Chiqariladigan yozuvlar soni")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Saralash maydoni")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni JSON ga yozish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        log.info("SEARCH bo'linma turlari (GET) - filter: {}", filter);

        return search(filter, offset, limit, sort, returnCount, returnNulls);
    }

    // =====================================================
    // 5. SEARCH (POST with filter in body)
    // =====================================================

    @Operation(
        summary = "Bo'linma turlarini qidirish (POST)",
        description = """
            Filter shartlari bo'yicha bo'linma turlarini qidiradi.
            Filter JSON obyekt sifatida request body da beriladi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Filter shartlariga mos bo'linma turlari qaytarildi."),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q.")
    })
    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @Parameter(description = "Jami sonni 'X-Total-Count' headerda qaytarish")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Chiqariladigan yozuvlar soni")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Saralash maydoni")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni JSON ga yozish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        log.info("SEARCH bo'linma turlari (POST) - filter: {}", filterBody);

        // CUBA format: {"filter":{"conditions":[...]}}
        // filter kalitini olish va JSON stringga aylantirish
        String filterStr = null;
        if (filterBody != null) {
            Object filterObj = filterBody.get("filter");
            if (filterObj != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    filterStr = mapper.writeValueAsString(filterObj);
                } catch (Exception e) {
                    log.warn("Filter JSON ga aylantirishda xato: {}", e.getMessage());
                    filterStr = filterObj.toString();
                }
            }
        }
        return search(filterStr, offset, limit, sort, returnCount, returnNulls);
    }

    // =====================================================
    // 6. LIST ALL
    // =====================================================

    @Operation(
        summary = "Barcha bo'linma turlarini olish",
        description = """
            Barcha bo'linma turlarini sahifalangan ro'yxat sifatida qaytaradi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Bo'linma turlari ro'yxati qaytarildi."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q.")
    })
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listAll(
            @Parameter(description = "Jami sonni 'X-Total-Count' headerda qaytarish")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Chiqariladigan yozuvlar soni")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Saralash maydoni")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni JSON ga yozish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        log.info("LIST ALL bo'linma turlari");

        List<HUniversityDepartmentType> allEntities = classifierService.findAllDepartmentTypes();

        // Sahifalash
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<HUniversityDepartmentType> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> classifierService.toDepartmentTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    // =====================================================
    // 7. CREATE
    // =====================================================

    @Operation(
        summary = "Yangi bo'linma turi yaratish",
        description = """
            Yangi bo'linma turi yaratadi. Request body da JSON obyekt kutiladi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Yaratilgan bo'linma turi qaytarildi."),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov. Masalan, mavjud bo'lmagan reference."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q.")
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> entityData) {

        log.info("CREATE bo'linma turi: {}", entityData);

        String code = (String) entityData.get("code");
        String name = (String) entityData.get("name");
        String nameRu = (String) entityData.get("nameRu");
        String nameEn = (String) entityData.get("nameEn");

        try {
            // Service handles soft-deleted entity restoration via native query
            HUniversityDepartmentType saved = classifierService.createOrRestoreDepartmentType(code, name, nameRu, nameEn);

            return ResponseEntity.ok(classifierService.toDepartmentTypeMap(saved, false));

        } catch (Exception e) {
            log.error("CREATE xatosi: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Server error");
            errorResponse.put("details", "Entity yaratishda xatolik: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * GET va POST search endpointlari uchun umumiy logika.
     *
     * <p><strong>CUBA Filter Format:</strong></p>
     * <pre>
     * {"conditions":[{"property":"name","operator":"contains","value":"Fakultet"}]}
     * </pre>
     *
     * <p><strong>Supported operators:</strong> =, contains, startsWith, endsWith, notEmpty, isNull</p>
     */
    private ResponseEntity<List<Map<String, Object>>> search(
            String filter, Integer offset, Integer limit, String sort, Boolean returnCount, Boolean returnNulls) {

        List<HUniversityDepartmentType> allEntities = classifierService.findAllDepartmentTypes();

        // CUBA JSON filter yoki oddiy string filter
        if (filter != null && !filter.isEmpty()) {
            allEntities = applyFilter(allEntities, filter);
        }

        // Sahifalash
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<HUniversityDepartmentType> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> classifierService.toDepartmentTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    /**
     * CUBA JSON filter yoki oddiy string filter qo'llash
     */
    @SuppressWarnings("unchecked")
    private List<HUniversityDepartmentType> applyFilter(List<HUniversityDepartmentType> entities, String filter) {
        // CUBA JSON format ekanligini tekshirish
        if (filter.trim().startsWith("{")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> filterJson = mapper.readValue(filter, Map.class);

                Object conditionsObj = filterJson.get("conditions");
                if (conditionsObj instanceof List) {
                    List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;

                    for (Map<String, Object> condition : conditions) {
                        String property = (String) condition.get("property");
                        String operator = (String) condition.get("operator");
                        Object value = condition.get("value");

                        entities = filterByCondition(entities, property, operator, value);
                    }
                }
                return entities;
            } catch (Exception e) {
                log.warn("CUBA filter parse xatosi, oddiy filter sifatida ishlatiladi: {}", e.getMessage());
            }
        }

        // Oddiy string filter - name bo'yicha qidirish
        String searchTerm = filter.toLowerCase();
        return entities.stream()
            .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(searchTerm))
            .collect(Collectors.toList());
    }

    /**
     * CUBA condition bo'yicha filtrlash
     */
    private List<HUniversityDepartmentType> filterByCondition(
            List<HUniversityDepartmentType> entities, String property, String operator, Object value) {

        return entities.stream()
            .filter(e -> matchesCondition(e, property, operator, value))
            .collect(Collectors.toList());
    }

    /**
     * Entity CUBA conditionga mos kelishini tekshirish
     */
    private boolean matchesCondition(HUniversityDepartmentType entity, String property, String operator, Object value) {
        Object fieldValue = getFieldValue(entity, property);

        if (operator == null) operator = "=";

        switch (operator) {
            case "=":
                return fieldValue != null && fieldValue.toString().equals(value != null ? value.toString() : null);
            case "contains":
                return fieldValue != null && value != null &&
                       fieldValue.toString().toLowerCase().contains(value.toString().toLowerCase());
            case "startsWith":
                return fieldValue != null && value != null &&
                       fieldValue.toString().toLowerCase().startsWith(value.toString().toLowerCase());
            case "endsWith":
                return fieldValue != null && value != null &&
                       fieldValue.toString().toLowerCase().endsWith(value.toString().toLowerCase());
            case "notEmpty":
                return fieldValue != null && !fieldValue.toString().isEmpty();
            case "isNull":
                return fieldValue == null;
            default:
                return true;
        }
    }

    /**
     * Entity dan field qiymatini olish
     */
    private Object getFieldValue(HUniversityDepartmentType entity, String property) {
        if (property == null) return null;

        switch (property) {
            case "code": return entity.getCode();
            case "name": return entity.getName();
            case "nameRu": return entity.getNameRu();
            case "nameEn": return entity.getNameEn();
            case "active": return entity.getActive();
            case "version": return entity.getVersion();
            default: return null;
        }
    }
}
