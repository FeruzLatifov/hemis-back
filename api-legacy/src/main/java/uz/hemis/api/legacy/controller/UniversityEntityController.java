package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.repository.UniversityRepository;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.net.URI;

import java.util.*;
import java.util.stream.Collectors;

/**
 * University Entity Controller (CUBA Pattern)
 * Tag 15: OTM (Entity API)
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/entities/hemishe_EUniversity}</p>
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Preserves exact CUBA entity API pattern</li>
 *   <li>Response format: CUBA Map structure with _entityName, _instanceName</li>
 *   <li>Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)</li>
 *   <li>View: eUniversity-view - asosiy maydonlar</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET    /app/rest/v2/entities/hemishe_EUniversity           - OTM ro'yxati</li>
 *   <li>GET    /app/rest/v2/entities/hemishe_EUniversity/{id}      - ID bo'yicha olish</li>
 *   <li>PUT    /app/rest/v2/entities/hemishe_EUniversity/{id}      - Yangilash</li>
 *   <li>DELETE /app/rest/v2/entities/hemishe_EUniversity/{id}      - O'chirish</li>
 *   <li>GET    /app/rest/v2/entities/hemishe_EUniversity/search    - Qidirish (GET)</li>
 *   <li>POST   /app/rest/v2/entities/hemishe_EUniversity/search    - Qidirish (POST)</li>
 *   <li>POST   /app/rest/v2/entities/hemishe_EUniversity           - Yaratish</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "15.OTM", description = "Oliy ta'lim muassasalari - universitetlar ro'yxati va boshqaruvi")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EUniversity")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityEntityController {

    private final UniversityRepository repository;
    private final CubaFilterHelper filterHelper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String ENTITY_NAME = "hemishe_EUniversity";

    /**
     * ID bo'yicha OTM olish
     */
    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(summary = "OTM olish (ID bo'yicha)", description = "Bitta OTM ni code bo'yicha qaytaradi")
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "OTM kodi") @PathVariable String entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET /entities/hemishe_EUniversity/{}", entityId);

        Optional<University> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EUniversity/{} - topilmadi", entityId);
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_EUniversity with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls, view));
    }

    /**
     * OTM yangilash
     *
     * <p><strong>Legacy Endpoint:</strong> PUT /app/rest/v2/entities/hemishe_EUniversity/{entityId}</p>
     *
     * @param entityId OTM kodi
     * @param body Yangilanadigan maydonlar
     * @param returnNulls null maydonlarni qaytarish
     * @return Yangilangan OTM
     */
    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "OTM yangilash",
        description = "Mavjud OTM ni yangilaydi. Faqat body da ko'rsatilgan maydonlar yangilanadi."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "OTM topilmadi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "OTM kodi") @PathVariable String entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "null maydonlarni qaytarish") @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT /entities/hemishe_EUniversity/{}", entityId);

        Optional<University> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("PUT /entities/hemishe_EUniversity/{} - topilmadi", entityId);
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_EUniversity with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        University entity = existingOpt.get();
        updateFromMap(entity, body);

        University saved = repository.save(entity);
        log.info("PUT /entities/hemishe_EUniversity/{} - muvaffaqiyatli yangilandi", entityId);
        return ResponseEntity.ok(toMap(saved, returnNulls, null));
    }

    /**
     * OTM o'chirish (soft delete)
     *
     * <p><strong>Legacy Endpoint:</strong> DELETE /app/rest/v2/entities/hemishe_EUniversity/{entityId}</p>
     *
     * @param entityId OTM kodi
     * @return 204 No Content yoki 404 Not Found
     */
    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "OTM o'chirish",
        description = "OTM ni o'chiradi (soft delete). deleteTs va deletedBy maydonlari o'rnatiladi."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "OTM topilmadi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "OTM kodi") @PathVariable String entityId) {

        log.info("DELETE /entities/hemishe_EUniversity/{}", entityId);

        Optional<University> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("DELETE /entities/hemishe_EUniversity/{} - topilmadi", entityId);
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_EUniversity with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        repository.delete(entity.get());
        log.info("DELETE /entities/hemishe_EUniversity/{} - muvaffaqiyatli o'chirildi", entityId);
        // OLD-HEMIS: 200 OK (not 204 No Content)
        return ResponseEntity.ok().build();
    }

    /**
     * OTM qidirish (GET)
     */
    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "OTM qidirish (GET)", description = "URL parametrlari orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<University> allEntities = repository.findAll();
        List<University> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    /**
     * OTM qidirish (POST)
     */
    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "OTM qidirish (POST)", description = "JSON filter orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Offset") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Limit") @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        int effectiveOffset = filterHelper.extractInt(body, "offset", offset, 0);
        int effectiveLimit = filterHelper.extractInt(body, "limit", limit, 50);
        String filterJson = filterHelper.extractFilterFromBody(body);

        log.debug("POST search - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<University> allEntities = repository.findAll();
        List<University> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    /**
     * OTM ro'yxatini olish
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/entities/hemishe_EUniversity</p>
     *
     * <p>OLD-HEMIS formatida OTM ro'yxatini qaytaradi (eUniversity-view):</p>
     * <pre>
     * [
     *   {
     *     "_entityName": "hemishe_EUniversity",
     *     "_instanceName": "301-Andijon davlat universiteti",
     *     "code": "301",
     *     "name": "Andijon davlat universiteti",
     *     "tin": "200123456",
     *     "address": "Andijon sh.",
     *     "ownership": { "_entityName": "...", "code": "11", "name": "Davlat" },
     *     "universityType": { "_entityName": "...", "code": "11", "name": "Universitet" },
     *     "soato": { "_entityName": "...", "code": "1703", "name": "Andijon viloyati" }
     *   }
     * ]
     * </pre>
     *
     * @param returnCount X-Total-Count headerni qaytarish
     * @param offset pagination boshlanish nuqtasi
     * @param limit sahifa hajmi
     * @param sort saralash (masalan: "name" yoki "-code")
     * @param view ko'rinish (eUniversity-view)
     * @return OTM ro'yxati OLD-HEMIS formatida
     */
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(
        summary = "OTM ro'yxati",
        description = "Barcha oliy ta'lim muassasalari ro'yxatini OLD-HEMIS formatida qaytaradi (eUniversity-view)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "[{\"_entityName\":\"hemishe_EUniversity\",\"_instanceName\":\"301-Andijon davlat universiteti\",\"code\":\"301\",\"name\":\"Andijon davlat universiteti\"}]")))
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "X-Total-Count headerni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Pagination boshlanish nuqtasi") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Sahifa hajmi (bo'lmasa barcha yozuvlar)") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Saralash (masalan: name, -code)") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "Ko'rinish nomi (eUniversity-view)") @RequestParam(required = false) String view) {

        log.info("GET /entities/hemishe_EUniversity - offset: {}, limit: {}, view: {}", offset, limit, view);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        List<University> entities;
        if (limit == null) {
            // OLD-HEMIS: limit bo'lmasa barcha yozuvlarni qaytarish
            entities = repository.findAll(sorting);
        } else {
            int effectiveOffset = offset != null ? offset : 0;
            int page = effectiveOffset / limit;
            PageRequest pageRequest = PageRequest.of(page, limit, sorting);
            entities = repository.findAll(pageRequest).getContent();
        }

        List<Map<String, Object>> result = entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList());

        log.info("Jami {} ta OTM topildi", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * Yangi OTM yaratish yoki mavjud OTM ni yangilash
     *
     * <p><strong>Legacy Endpoint:</strong> POST /app/rest/v2/entities/hemishe_EUniversity</p>
     *
     * <p>OLD-HEMIS formatida OTM yaratadi yoki mavjudini yangilaydi (CUBA Entity API pattern):</p>
     * <ul>
     *   <li>Agar body da "id" yoki "code" mavjud bo'lsa va bazada bor - yangilash</li>
     *   <li>Agar body da "id" yoki "code" yo'q yoki bazada mavjud emas - yaratish</li>
     * </ul>
     *
     * <p><strong>Request body formati:</strong></p>
     * <pre>
     * {
     *   "code": "999",
     *   "name": "Test universiteti",
     *   "tin": "123456789",
     *   "address": "Test manzil",
     *   "active": true,
     *   "studentUrl": "student.test.uz",
     *   "teacherUrl": "teacher.test.uz",
     *   "universityUrl": "www.test.uz",
     *   "addStudent": true,
     *   "accreditationEdit": false,
     *   "gpaEdit": false,
     *   "oneId": true,
     *   "allowGrouping": true,
     *   "allowTransferOutside": true,
     *   "gradingSystem": false
     * }
     * </pre>
     *
     * <p><strong>Response:</strong> Yaratilgan/yangilangan OTM OLD-HEMIS formatida + Location header</p>
     *
     * @param body OTM ma'lumotlari JSON formatida
     * @param returnNulls null maydonlarni responseda qaytarish
     * @param request HTTP request (Location header uchun)
     * @return Yaratilgan/yangilangan OTM OLD-HEMIS formatida
     */
    @PostMapping
    @Transactional
    @Operation(
        summary = "OTM yaratish yoki yangilash",
        description = "Yangi OTM yaratadi yoki mavjudini yangilaydi (CUBA Entity API pattern). " +
                      "Agar body da id/code mavjud va bazada bor bo'lsa - yangilash, aks holda yaratish."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi/yangilandi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EUniversity\",\"_instanceName\":\"999-Test universiteti\",\"id\":\"999\",\"code\":\"999\",\"name\":\"Test universiteti\",\"active\":true}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov - code majburiy"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "OTM ma'lumotlari",
                required = true,
                content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"code\":\"999\",\"name\":\"Test universiteti\",\"tin\":\"123456789\",\"address\":\"Test manzil\",\"active\":true}"))
            )
            @RequestBody Map<String, Object> body,
            @Parameter(description = "null maydonlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            HttpServletRequest request) {

        log.info("POST /entities/hemishe_EUniversity - OTM yaratish/yangilash. Body: {}", body);

        // code ni olish (id, code yoki _university dan)
        String code = null;
        if (body.containsKey("id")) {
            code = String.valueOf(body.get("id"));
            log.debug("Code from 'id': {}", code);
        } else if (body.containsKey("code")) {
            code = String.valueOf(body.get("code"));
            log.debug("Code from 'code': {}", code);
        } else if (body.containsKey("_university")) {
            // CUBA format: _university may be a Map with "code" key or a plain String
            Object uniObj = body.get("_university");
            if (uniObj instanceof Map) {
                Object uniCode = ((Map<?, ?>) uniObj).get("code");
                if (uniCode != null) {
                    code = uniCode.toString();
                }
            } else if (uniObj != null) {
                code = uniObj.toString();
            }
            log.debug("Code from '_university': {}", code);
        }
        log.info("Extracted code: {}", code);

        if (code == null || code.isEmpty() || "null".equals(code)) {
            // CUBA compatibility: code yo'q bo'lsa, auto-generate qilish
            // old-hemis xuddi shunday ishlaydi — yangi UUID-based code yaratadi
            code = String.valueOf(System.currentTimeMillis() % 100000);
            log.info("POST /entities/hemishe_EUniversity - code auto-generated: {}", code);
        }

        // Mavjud OTM ni tekshirish
        Optional<University> existingOpt = repository.findById(code);
        University entity;

        if (existingOpt.isPresent()) {
            // Yangilash
            log.info("POST /entities/hemishe_EUniversity - mavjud OTM yangilanmoqda: {}", code);
            entity = existingOpt.get();
        } else {
            // Yaratish
            log.info("POST /entities/hemishe_EUniversity - yangi OTM yaratilmoqda: {}", code);
            entity = new University();
            entity.setCode(code);
        }

        updateFromMap(entity, body);
        University saved = repository.save(entity);

        // Location header yaratish (CUBA pattern)
        URI location = ServletUriComponentsBuilder
                .fromRequestUri(request)
                .path("/{id}")
                .buildAndExpand(saved.getCode())
                .toUri();

        log.info("POST /entities/hemishe_EUniversity - muvaffaqiyatli: {}", saved.getCode());

        // CUBA POST pattern: minimal response (only _entityName, _instanceName, id)
        Map<String, Object> minimalResponse = new java.util.LinkedHashMap<>();
        minimalResponse.put("_entityName", ENTITY_NAME);
        minimalResponse.put("_instanceName", saved.getCode() + "-" + (saved.getName() != null ? saved.getName() : ""));
        minimalResponse.put("id", saved.getCode());

        return ResponseEntity.ok()
                .header("Location", location.toString())
                .body(minimalResponse);
    }

    /**
     * University entity ni OLD-HEMIS formatiga o'girish
     *
     * <p>OLD-HEMIS response formati (aynan shu tartibda):</p>
     * <pre>
     * {
     *   "_entityName": "hemishe_EUniversity",
     *   "_instanceName": "301-Andijon davlat universiteti",
     *   "id": "301",
     *   "studentUrl": "student.adu.uz",
     *   "code": "301",
     *   "tin": "202217655",
     *   "addStudent": true,
     *   "address": "Andijon shahar",
     *   "accreditationEdit": false,
     *   "active": true,
     *   "version": 20,
     *   "oneId": true,
     *   "allowGrouping": true,
     *   "teacherUrl": "hemis.adu.uz",
     *   "allowTransferOutside": true,
     *   "name": "Andijon davlat universiteti",
     *   "gpaEdit": false
     * }
     * </pre>
     *
     * @param entity University entity
     * @param returnNulls null maydonlarni qaytarish
     * @param view ko'rinish nomi (e'tiborsiz - OLD-HEMIS formati)
     * @return OLD-HEMIS formatidagi map
     */
    private Map<String, Object> toMap(University entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name: "code-name" format (CUBA pattern)
        String instanceName = entity.getCode() + "-" + (entity.getName() != null ? entity.getName() : "");
        map.put("_instanceName", instanceName);

        // id = code (OLD-HEMIS formatida id maydoni bor)
        map.put("id", entity.getCode());

        // URL maydonlari
        putIfNotNull(map, "studentUrl", entity.getStudentUrl(), returnNulls);
        putIfNotNull(map, "gradingSystem", entity.getGradingSystem(), returnNulls);

        // Asosiy maydonlar
        map.put("code", entity.getCode());
        putIfNotNull(map, "uzbmbUrl", entity.getUzbmbUrl(), returnNulls);
        putIfNotNull(map, "tin", entity.getTin(), returnNulls);
        putIfNotNull(map, "universityUrl", entity.getUniversityUrl(), returnNulls);
        putIfNotNull(map, "addStudent", entity.getAddStudent(), returnNulls);
        putIfNotNull(map, "address", entity.getAddress(), returnNulls);
        putIfNotNull(map, "accreditationEdit", entity.getAccreditationEdit(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "cadastre", entity.getCadastre(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "oneId", entity.getOneId(), returnNulls);
        putIfNotNull(map, "allowGrouping", entity.getAllowGrouping(), returnNulls);
        putIfNotNull(map, "teacherUrl", entity.getTeacherUrl(), returnNulls);
        putIfNotNull(map, "allowTransferOutside", entity.getAllowTransferOutside(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "gpaEdit", entity.getGpaEdit(), returnNulls);
        putIfNotNull(map, "addForeignStudent", entity.getAddForeignStudent(), returnNulls);
        putIfNotNull(map, "addTransferStudent", entity.getAddTransferStudent(), returnNulls);

        // Text fields
        putIfNotNull(map, "mailAddress", entity.getMailAddress(), returnNulls);
        putIfNotNull(map, "bankInfo", entity.getBankInfo(), returnNulls);
        putIfNotNull(map, "accreditationInfo", entity.getAccreditationInfo(), returnNulls);

        // Audit fields
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs() != null ? entity.getDeleteTs().toString() : null, returnNulls);

        return map;
    }

    /**
     * Map dan University entity ga ma'lumotlarni o'tkazish
     *
     * <p>OLD-HEMIS CUBA entity formatiga mos holda barcha maydonlarni qo'llab-quvvatlaydi.</p>
     *
     * @param entity University entity
     * @param map Request body map
     */
    private void updateFromMap(University entity, Map<String, Object> map) {
        // Asosiy maydonlar
        if (map.containsKey("name")) {
            entity.setName((String) map.get("name"));
        }
        if (map.containsKey("tin")) {
            entity.setTin((String) map.get("tin"));
        }
        if (map.containsKey("address")) {
            entity.setAddress((String) map.get("address"));
        }
        if (map.containsKey("cadastre")) {
            entity.setCadastre((String) map.get("cadastre"));
        }

        // URL maydonlari
        if (map.containsKey("universityUrl")) {
            entity.setUniversityUrl((String) map.get("universityUrl"));
        }
        if (map.containsKey("studentUrl")) {
            entity.setStudentUrl((String) map.get("studentUrl"));
        }
        if (map.containsKey("teacherUrl")) {
            entity.setTeacherUrl((String) map.get("teacherUrl"));
        }
        if (map.containsKey("uzbmbUrl")) {
            entity.setUzbmbUrl((String) map.get("uzbmbUrl"));
        }

        // Location classifiers
        if (map.containsKey("soato")) {
            entity.setSoato((String) map.get("soato"));
        }
        if (map.containsKey("soatoRegion")) {
            entity.setSoatoRegion((String) map.get("soatoRegion"));
        }

        // University classifiers
        if (map.containsKey("universityType")) {
            entity.setUniversityType((String) map.get("universityType"));
        }
        if (map.containsKey("ownership")) {
            entity.setOwnership((String) map.get("ownership"));
        }
        if (map.containsKey("universityVersion")) {
            entity.setUniversityVersion((String) map.get("universityVersion"));
        }
        if (map.containsKey("universityActivityStatus")) {
            entity.setUniversityActivityStatus((String) map.get("universityActivityStatus"));
        }
        if (map.containsKey("universityBelongsTo")) {
            entity.setUniversityBelongsTo((String) map.get("universityBelongsTo"));
        }
        if (map.containsKey("universityContractCategory")) {
            entity.setUniversityContractCategory((String) map.get("universityContractCategory"));
        }
        if (map.containsKey("parentUniversity")) {
            entity.setParentUniversity((String) map.get("parentUniversity"));
        }
        if (map.containsKey("terrain")) {
            entity.setTerrain((String) map.get("terrain"));
        }

        // Boolean flags
        if (map.containsKey("active")) {
            entity.setActive(toBoolean(map.get("active")));
        }
        if (map.containsKey("gpaEdit")) {
            entity.setGpaEdit(toBoolean(map.get("gpaEdit")));
        }
        if (map.containsKey("accreditationEdit")) {
            entity.setAccreditationEdit(toBoolean(map.get("accreditationEdit")));
        }
        if (map.containsKey("addStudent")) {
            entity.setAddStudent(toBoolean(map.get("addStudent")));
        }
        if (map.containsKey("allowGrouping")) {
            entity.setAllowGrouping(toBoolean(map.get("allowGrouping")));
        }
        if (map.containsKey("allowTransferOutside")) {
            entity.setAllowTransferOutside(toBoolean(map.get("allowTransferOutside")));
        }
        if (map.containsKey("oneId")) {
            entity.setOneId(toBoolean(map.get("oneId")));
        }
        if (map.containsKey("gradingSystem")) {
            entity.setGradingSystem(toBoolean(map.get("gradingSystem")));
        }
        if (map.containsKey("addForeignStudent")) {
            entity.setAddForeignStudent(toBoolean(map.get("addForeignStudent")));
        }
        if (map.containsKey("addTransferStudent")) {
            entity.setAddTransferStudent(toBoolean(map.get("addTransferStudent")));
        }

        // Additional text fields
        if (map.containsKey("mailAddress")) {
            entity.setMailAddress((String) map.get("mailAddress"));
        }
        if (map.containsKey("bankInfo")) {
            entity.setBankInfo((String) map.get("bankInfo"));
        }
        if (map.containsKey("accreditationInfo")) {
            entity.setAccreditationInfo((String) map.get("accreditationInfo"));
        }
    }

    /**
     * Object ni Boolean ga xavfsiz o'girish
     *
     * @param value Object qiymat (Boolean, String yoki boshqa)
     * @return Boolean qiymat yoki null
     */
    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String str = (String) value;
            if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
                return true;
            }
            if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
                return false;
            }
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return null;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, uz.hemis.api.legacy.adapter.JsonNull.INSTANCE);
        }
    }
}
