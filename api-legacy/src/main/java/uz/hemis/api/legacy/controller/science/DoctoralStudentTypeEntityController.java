package uz.hemis.api.legacy.controller.science;

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
import uz.hemis.domain.entity.DoctoralStudentType;
import uz.hemis.service.legacy.science.ScienceDoctorateEntityLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Doktorantura Talabasi Turi Controller - CUBA REST API Pattern
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_HDoctoralStudentType</li>
 *   <li>Table: hemishe_h_doctoral_student_type</li>
 *   <li>Primary key: code (VARCHAR, NOT UUID!)</li>
 *   <li>Base URL: /app/rest/v2/entities/hemishe_HDoctoralStudentType</li>
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
@Tag(name = "29.Ilmiy doktorantura talabalari turlari", description = "Doktorantura talabasi turlarini boshqarish API (Asosiy doktorant, Tayanch doktorant, Mustaqil tadqiqotchi va h.k.)")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HDoctoralStudentType")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DoctoralStudentTypeEntityController {

    private final ScienceDoctorateEntityLegacyService scienceService;

    private static final String ENTITY_NAME = "hemishe_HDoctoralStudentType";

    // =====================================================
    // 1. GET BY ID (entityId = code)
    // =====================================================

    @Operation(
        summary = "Doktorantura talabasi turini ID bo'yicha olish",
        description = """
            Berilgan identifikator (code) bo'yicha bitta turni qaytaradi.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}
            **Auth:** Bearer token (majburiy)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Tur qaytarildi."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi. Token noto'g'ri yoki muddati o'tgan."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q. Foydalanuvchida bu ma'lumotni o'qish huquqi yo'q."),
        @ApiResponse(responseCode = "404", description = "Topilmadi. Berilgan ID bilan tur mavjud emas.")
    })
    @GetMapping("/{entityId}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Tur identifikatori (code)", required = true, example = "11")
            @PathVariable String entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni JSON ga yozish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        log.info("=== GET doktorantura talabasi turi === entityId={}, returnNulls={}", entityId, returnNulls);

        Optional<DoctoralStudentType> entity = scienceService.findDoctoralStudentTypeById(entityId);

        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(scienceService.toDoctoralStudentTypeMap(entity.get(), returnNulls));
    }

    // =====================================================
    // 2. UPDATE
    // =====================================================

    @Operation(
        summary = "Doktorantura talabasi turini yangilash",
        description = """
            Mavjud turni yangilaydi. Faqat jo'natilgan maydonlar yangilanadi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Yangilangan tur qaytarildi."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(
            @Parameter(description = "Tur identifikatori (code)", required = true)
            @PathVariable String entityId,
            @RequestBody Map<String, Object> entityData) {

        log.info("UPDATE doktorantura talabasi turi: {}", entityId);

        Optional<DoctoralStudentType> existingOpt = scienceService.findDoctoralStudentTypeById(entityId);

        if (existingOpt.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        DoctoralStudentType entity = existingOpt.get();

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

        DoctoralStudentType saved = scienceService.saveDoctoralStudentType(entity);

        return ResponseEntity.ok(scienceService.toDoctoralStudentTypeMap(saved, false));
    }

    // =====================================================
    // 3. DELETE (Soft Delete)
    // =====================================================

    @Operation(
        summary = "Doktorantura talabasi turini o'chirish",
        description = """
            Turni o'chiradi (soft delete - delete_ts qo'yiladi).

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Tur o'chirildi."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @DeleteMapping("/{entityId}")
    public ResponseEntity<?> delete(
            @Parameter(description = "Tur identifikatori (code)", required = true)
            @PathVariable String entityId) {

        log.info("DELETE doktorantura talabasi turi: {}", entityId);

        Optional<DoctoralStudentType> entity = scienceService.findDoctoralStudentTypeById(entityId);

        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        scienceService.deleteDoctoralStudentType(entity.get());

        return ResponseEntity.ok().build();
    }

    // =====================================================
    // 4. SEARCH (GET with filter parameter)
    // =====================================================

    @Operation(
        summary = "Doktorantura talabasi turlarini qidirish (GET)",
        description = """
            Filter shartlari bo'yicha turlarni qidiradi.
            Filter JSON obyekt sifatida URL parametrida beriladi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Filter shartlariga mos turlar qaytarildi."),
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

        log.info("SEARCH doktorantura talabasi turlari (GET) - filter: {}", filter);

        return search(filter, offset, limit, sort, returnCount, returnNulls);
    }

    // =====================================================
    // 5. SEARCH (POST with filter in body)
    // =====================================================

    @Operation(
        summary = "Doktorantura talabasi turlarini qidirish (POST)",
        description = """
            Filter shartlari bo'yicha turlarni qidiradi.
            Filter JSON obyekt sifatida request body da beriladi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Filter shartlariga mos turlar qaytarildi."),
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

        log.info("SEARCH doktorantura talabasi turlari (POST) - filter: {}", filterBody);

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
        summary = "Barcha doktorantura talabasi turlarini olish",
        description = """
            Barcha turlarni sahifalangan ro'yxat sifatida qaytaradi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Turlar ro'yxati qaytarildi."),
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

        log.info("LIST ALL doktorantura talabasi turlari");

        List<DoctoralStudentType> allEntities = scienceService.findAllDoctoralStudentType();

        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<DoctoralStudentType> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> scienceService.toDoctoralStudentTypeMap(e, returnNulls))
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
        summary = "Yangi doktorantura talabasi turi yaratish",
        description = """
            Yangi tur yaratadi. Request body da JSON obyekt kutiladi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli. Yaratilgan tur qaytarildi."),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov. Masalan, mavjud bo'lmagan reference."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q.")
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> entityData) {

        log.info("CREATE doktorantura talabasi turi: {}", entityData);

        String code = (String) entityData.get("code");
        String name = (String) entityData.get("name");
        String nameRu = (String) entityData.get("nameRu");
        String nameEn = (String) entityData.get("nameEn");

        // Validatsiya
        if (code == null || code.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Server error");
            errorResponse.put("details", "code is required");
            return ResponseEntity.status(500).body(errorResponse);
        }

        try {
            DoctoralStudentType entity = scienceService.upsertDoctoralStudentType(code, name, nameRu, nameEn);

            return ResponseEntity.status(201).body(scienceService.toDoctoralStudentTypeMinimalMap(entity));

        } catch (Exception e) {
            log.error("CREATE xatosi: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Server error");
            errorResponse.put("details", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private ResponseEntity<List<Map<String, Object>>> search(
            String filter, Integer offset, Integer limit, String sort, Boolean returnCount, Boolean returnNulls) {

        List<DoctoralStudentType> allEntities = scienceService.findAllDoctoralStudentType();

        if (filter != null && !filter.isEmpty()) {
            allEntities = applyFilter(allEntities, filter);
        }

        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<DoctoralStudentType> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> scienceService.toDoctoralStudentTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    @SuppressWarnings("unchecked")
    private List<DoctoralStudentType> applyFilter(List<DoctoralStudentType> entities, String filter) {
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

        String searchTerm = filter.toLowerCase();
        return entities.stream()
            .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(searchTerm))
            .collect(Collectors.toList());
    }

    private List<DoctoralStudentType> filterByCondition(
            List<DoctoralStudentType> entities, String property, String operator, Object value) {

        return entities.stream()
            .filter(e -> matchesCondition(e, property, operator, value))
            .collect(Collectors.toList());
    }

    private boolean matchesCondition(DoctoralStudentType entity, String property, String operator, Object value) {
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

    private Object getFieldValue(DoctoralStudentType entity, String property) {
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
