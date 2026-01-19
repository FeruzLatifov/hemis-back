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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.HUniversityDepartmentType;
import uz.hemis.domain.repository.HUniversityDepartmentTypeRepository;

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

    private final HUniversityDepartmentTypeRepository repository;

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
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Bo'linma turi identifikatori (code)", required = true, example = "11")
            @PathVariable String entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni JSON ga yozish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        log.info("=== GET bo'linma turi === entityId={}, returnNulls={}", entityId, returnNulls);

        Optional<HUniversityDepartmentType> entity = repository.findById(entityId);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
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
    @Transactional
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Bo'linma turi identifikatori (code)", required = true)
            @PathVariable String entityId,
            @RequestBody Map<String, Object> entityData) {

        log.info("UPDATE bo'linma turi: {}", entityId);

        Optional<HUniversityDepartmentType> existingOpt = repository.findById(entityId);

        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
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

        HUniversityDepartmentType saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, false));
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
    @Transactional
    public ResponseEntity<Void> delete(
            @Parameter(description = "Bo'linma turi identifikatori (code)", required = true)
            @PathVariable String entityId) {

        log.info("DELETE bo'linma turi: {}", entityId);

        Optional<HUniversityDepartmentType> entity = repository.findById(entityId);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Soft delete
        entity.get().setDeleteTs(LocalDateTime.now());
        entity.get().setActive(false);
        repository.save(entity.get());

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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

        String filterStr = filterBody != null ? filterBody.toString() : null;
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
    @Transactional(readOnly = true)
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

        List<HUniversityDepartmentType> allEntities = repository.findAll();

        // Sahifalash
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<HUniversityDepartmentType> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> toMap(e, returnNulls))
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
    @Transactional
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> entityData) {

        log.info("CREATE bo'linma turi: {}", entityData);

        HUniversityDepartmentType entity = new HUniversityDepartmentType();
        entity.setCode((String) entityData.get("code"));
        entity.setName((String) entityData.get("name"));
        entity.setNameRu((String) entityData.get("nameRu"));
        entity.setNameEn((String) entityData.get("nameEn"));
        entity.setActive(true);
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        HUniversityDepartmentType saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, false));
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Entity ni CUBA-style JSON map ga o'girish
     *
     * <p><strong>OLD-HEMIS FIELD ORDER (100% compatible):</strong></p>
     * <ul>
     *   <li>_entityName, _instanceName, id - har doim</li>
     *   <li>nameRu, deleteTs - returnNulls=true bo'lganda</li>
     *   <li>code, name, active - har doim (null bo'lmasa)</li>
     *   <li>nameEn - returnNulls=true bo'lganda</li>
     *   <li>version - har doim</li>
     *   <li>deletedBy - returnNulls=true bo'lganda</li>
     * </ul>
     *
     * <p><strong>NOT INCLUDED (old-hemis da yo'q):</strong></p>
     * <ul>
     *   <li>createdBy, createTs, updatedBy, updateTs - QAYTARILMAYDI!</li>
     * </ul>
     */
    private Map<String, Object> toMap(HUniversityDepartmentType entity, Boolean returnNulls) {
        log.info("toMap() called: returnNulls={}, nameRu={}, nameEn={}", returnNulls, entity.getNameRu(), entity.getNameEn());

        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS exact field order
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getName());
        map.put("id", entity.getCode()); // CUBA da id = code

        // returnNulls=true bo'lganda nameRu
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);

        // returnNulls=true bo'lganda deleteTs
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);

        // Asosiy maydonlar (har doim, null bo'lmasa)
        putIfNotNull(map, "code", entity.getCode(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);

        // returnNulls=true bo'lganda nameEn
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);

        // Version (har doim)
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        // returnNulls=true bo'lganda deletedBy
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        // MUHIM: createdBy, createTs, updatedBy, updateTs - OLD-HEMIS DA YO'Q!
        // Shuning uchun QAYTARILMAYDI!

        return map;
    }

    /**
     * Faqat null bo'lmasa map ga qo'shish (returnNulls=true bo'lmasa)
     */
    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    /**
     * GET va POST search endpointlari uchun umumiy logika
     */
    private ResponseEntity<List<Map<String, Object>>> search(
            String filter, Integer offset, Integer limit, String sort, Boolean returnCount, Boolean returnNulls) {

        List<HUniversityDepartmentType> allEntities = repository.findAll();

        // Oddiy filter (name bo'yicha qidirish)
        if (filter != null && !filter.isEmpty()) {
            String searchTerm = filter.toLowerCase();
            allEntities = allEntities.stream()
                .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        }

        // Sahifalash
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<HUniversityDepartmentType> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }
}
