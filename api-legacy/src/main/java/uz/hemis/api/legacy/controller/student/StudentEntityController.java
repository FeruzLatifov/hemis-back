package uz.hemis.api.legacy.controller.student;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.adapter.LegacyEntityAdapter;
import uz.hemis.common.dto.student.StudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.service.legacy.UserLegacyService;
import uz.hemis.service.student.StudentService;
import uz.hemis.service.student.mapper.StudentLegacyMapper;

import uz.hemis.api.legacy.adapter.JsonNull;

import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Student Entity Controller (CUBA Pattern) - REFACTORED
 * Tag 03: Talabalar (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EStudent
 *
 * ✅ CLEAN ARCHITECTURE IMPLEMENTATION:
 * - Uses Service layer for all business logic
 * - Uses LegacyEntityAdapter for CUBA compatibility
 * - Enforces soft-delete only (no physical DELETE)
 * - Validates all operations
 * - Manages cache automatically
 * - Logs all operations for audit
 *
 * ✅ 100% BACKWARD COMPATIBLE:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_EStudent
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 * - Same HTTP status codes
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EStudent/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EStudent/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EStudent/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EStudent           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EStudent           - Create new
 *
 * @since 2.0.0 (Clean Architecture)
 * @author Senior System Architect
 */
@Tag(name = "04.Talaba", description = "Talaba entity ma'lumotlari API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudent")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class StudentEntityController {

    private final StudentService studentService;
    private final LegacyEntityAdapter adapter;
    private final StudentLegacyMapper studentLegacyMapper;
    private final UserLegacyService userLegacyService;

    private static final String ENTITY_NAME = "hemishe_EStudent";
    private static final String VIEW_LOCAL = "_local";

    /**
     * Sort property whitelist — OWASP A03 (SQL injection / DoS via arbitrary ORDER BY).
     * Faqat indexed/safe columnlar (CUBA klient request qiladi). Whitelisted bo'lmagan
     * property'lar {@code createTs} ga fallback qilinadi va WARN log'ga yoziladi.
     */
    private static final java.util.Set<String> STUDENT_SORT_WHITELIST = java.util.Set.of(
            "id", "code", "firstname", "lastname", "fathername", "pinfl",
            "createTs", "updateTs", "studentStatus", "active",
            "university", "faculty", "speciality", "course",
            "educationType", "educationForm", "educationYear"
    );

    /**
     * Build _instanceName for CUBA compatibility
     * Format: "LASTNAME FIRSTNAME FATHERNAME"
     */
    private String buildInstanceName(StudentDto dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getLastname() != null) sb.append(dto.getLastname());
        if (dto.getFirstname() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(dto.getFirstname());
        }
        if (dto.getFathername() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(dto.getFathername());
        }
        return sb.length() > 0 ? sb.toString() : dto.getId().toString();
    }

    /**
     * Bitta talaba ma'lumotlarini olish
     *
     * ✅ REFACTORED: Uses service layer
     * ✅ BACKWARD COMPATIBLE: Same response format (CUBA Map)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta talaba ma'lumotlarini olish",
        description = """
            ID bo'yicha talaba ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EStudent/{entityId}
            **Auth:** Bearer token (required)

            **Parametrlar:**
            - dynamicAttributes: Dinamik atributlarni qaytarish (boolean)
            - returnNulls: Null qiymatlarni qaytarish (boolean)
            - view: CUBA view nomi (string)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Talaba ma'lumotlari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi - Token noto'g'ri yoki muddati o'tgan"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - Foydalanuvchida entity o'qish huquqi yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan talaba topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Talaba UUID identifikatori")
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qaytarish")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi (masalan: eStudent-view)")
            @RequestParam(required = false) String view) {

        log.debug("GET student by id: {} (via service layer), view: {}", entityId, view);

        try {
            // When view is specified and NOT _local, use legacy mapper with expanded reference objects
            if (view != null && !VIEW_LOCAL.equals(view)) {
                return getByIdWithExpandedRefs(entityId, returnNulls);
            }

            // Service layer - with cache, validation, etc.
            StudentDto dto = studentService.findById(entityId);

            // Convert to CUBA format for backward compatibility
            // Pass view parameter to filter fields (_local excludes underscore-prefixed fields)
            Map<String, Object> cubaMap = adapter.toMap(dto, ENTITY_NAME, returnNulls, view);

            // OLD-HEMIS compatibility: these fields ARE returned by OLD-HEMIS
            // admissionType, transferCountry, transferType — all preserved for backward compatibility

            // OLD-HEMIS compatibility: add fields that exist in CUBA entity but not in DTO
            if (Boolean.TRUE.equals(returnNulls)) {
                Object jsonNull = JsonNull.INSTANCE;
                // Decree info fields (OLD-HEMIS: decreeInfoName/Number/Date)
                cubaMap.putIfAbsent("decreeInfoName", jsonNull);
                cubaMap.putIfAbsent("decreeInfoNumber", jsonNull);
                cubaMap.putIfAbsent("decreeInfoDate", jsonNull);
                // Additional fields from OLD-HEMIS
                cubaMap.putIfAbsent("status", jsonNull);
                cubaMap.putIfAbsent("speciality", jsonNull);
                cubaMap.putIfAbsent("roommateCount", jsonNull);
                cubaMap.putIfAbsent("eduStartDate", jsonNull);
                cubaMap.putIfAbsent("graduationDate", jsonNull);
                cubaMap.putIfAbsent("studyDuration", jsonNull);
                cubaMap.putIfAbsent("deletedBy", jsonNull);
                cubaMap.putIfAbsent("deleteTs", jsonNull);
            }

            return ResponseEntity.ok(cubaMap);

        } catch (ResourceNotFoundException e) {
            log.debug("Student not found: {}", entityId);
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }
    }

    /**
     * Get student with expanded reference objects (for views like eStudent-view)
     * Uses StudentLegacyMapper which queries classifier tables to build CUBA-format nested objects
     */
    private ResponseEntity<Map<String, Object>> getByIdWithExpandedRefs(UUID entityId, Boolean returnNulls) {
        Optional<Student> studentOpt = studentService.findEntityById(entityId);
        if (studentOpt.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        Student student = studentOpt.get();
        Map<String, Object> cubaMap = studentLegacyMapper.toLegacyMap(student);
        cubaMap = postProcessStudentLegacyMap(cubaMap, returnNulls);

        return ResponseEntity.ok(cubaMap);
    }

    /**
     * Post-process a student legacy map for OLD-HEMIS compatibility.
     * Adds null fields for missing references, handles university nested fields,
     * removes NEW-hemis-only fields, and reorders to match OLD-HEMIS field order.
     *
     * Used by both getById (expanded view) and getAll (expanded view).
     */
    private Map<String, Object> postProcessStudentLegacyMap(Map<String, Object> cubaMap, Boolean returnNulls) {
        // OLD-HEMIS compatibility: ALWAYS add null fields for missing reference objects
        // (OLD-HEMIS returns null fields regardless of returnNulls parameter in view mode)
        Object jsonNull = JsonNull.INSTANCE;
        // Reference fields that OLD-HEMIS eStudent-view returns as null when absent.
        // Includes ALL fields confirmed from OLD-HEMIS PHP test comparison.
        String[] refFields = {
            "currentEducationYear", "country", "educationType", "educationYear", "educationForm",
            "language", "socialCategory", "studentStatus", "citizenship", "gender",
            "nationality", "paymentForm", "grantType", "studentType", "course",
            "accomodation", "statusEducationYear",
            "university", "faculty", "soato", "terrain", "currentSoato", "currentTerrain",
            "specialityBachelor", "specialityMaster", "specialityDoctoral", "specialityOrdinatura",
            "decreeInfoName", "decreeInfoNumber", "decreeInfoDate",
            "academicMobileType", "transferCountry", "povertyLevel",
            "graduationYear", "admissionType", "academicReason",
            "livingStatus", "roommateType", "status",
            "expelReason", "stipendRate", "doctoralStudentType",
            "roommateCount", "eduStartDate", "transferUniversity",
            "graduationDate", "transferType", "studyDuration", "speciality",
            "points", "isDuplicate"
        };
        for (String field : refFields) {
            cubaMap.putIfAbsent(field, jsonNull);
        }

        // OLD-HEMIS compatibility: scalar fields that should be returned as null when absent
        // Jackson NON_NULL strips null values from ObjectMapper serialization in toLegacyMap(),
        // so we must add them back when returnNulls=true (old-hemis returns all fields in view mode)
        if (Boolean.TRUE.equals(returnNulls)) {
            String[] scalarFields = {
                "pinfl", "serialNumber", "birthday", "phone", "email", "address",
                "currentAddress", "parentPhone", "responsiblePersonPhone", "geoAddress",
                "passportGivenDate", "tag", "fathername", "groupId", "groupName",
                "enrollOrderName", "enrollOrderDate", "enrollOrderNumber", "enrollOrderCategory",
                "statusOrderName", "statusOrderDate", "statusOrderNumber", "statusOrderCategory",
                "isGraduate", "verified"
            };
            for (String field : scalarFields) {
                cubaMap.putIfAbsent(field, jsonNull);
            }
        }

        // Remove .code from specific nested classifiers where OLD-HEMIS doesn't include it
        // (most classifiers in OLD DO have .code — only these 4 don't)
        String[] noCodeClassifiers = {"educationType", "educationYear", "faculty", "grantType"};
        for (String field : noCodeClassifiers) {
            Object obj = cubaMap.get(field);
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) obj;
                nested.remove("code");
            }
        }

        // Remove name_ru from soato nested object (OLD-HEMIS doesn't include it)
        Object soatoObj = cubaMap.get("soato");
        if (soatoObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> soatoMap = (Map<String, Object>) soatoObj;
            soatoMap.remove("name_ru");
        }

        // Remove name_ru from currentSoato (OLD-HEMIS doesn't include it)
        Object cSoatoObj = cubaMap.get("currentSoato");
        if (cSoatoObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cSoatoMap = (Map<String, Object>) cSoatoObj;
            cSoatoMap.remove("name_ru");
        }

        // Strip .code, .soato.parent_code from terrain (OLD-HEMIS doesn't include them)
        stripTerrainExtras(cubaMap, "terrain");
        // Strip .code, .soato.parent_code from currentTerrain
        stripTerrainExtras(cubaMap, "currentTerrain");

        // University nested fields cleanup — remove only code/version, add missing null fields
        Object uniObj = cubaMap.get("university");
        if (uniObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> uniMap = (Map<String, Object>) uniObj;
            uniMap.remove("code");
            uniMap.remove("version");
            // OLD-HEMIS returns these fields as null when absent (DTO @JsonInclude(NON_NULL) strips them)
            String[] uniNullFields = {
                "gradingSystem", "accreditationInfo", "uzbmbUrl", "universityUrl",
                "bankInfo", "mailAddress", "cadastre", "addForeignStudent", "addTransferStudent"
            };
            for (String f : uniNullFields) {
                uniMap.putIfAbsent(f, jsonNull);
            }
            // versionType and universityContractCategory — add nameRu/nameEn, strip code
            Object vtObj = uniMap.get("versionType");
            if (vtObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> vtMap = (Map<String, Object>) vtObj;
                vtMap.putIfAbsent("nameRu", jsonNull);
                vtMap.putIfAbsent("nameEn", jsonNull);
                vtMap.remove("code");
            }
            Object uccObj = uniMap.get("universityContractCategory");
            if (uccObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> uccMap = (Map<String, Object>) uccObj;
                uccMap.putIfAbsent("nameRu", jsonNull);
                uccMap.putIfAbsent("nameEn", jsonNull);
                uccMap.remove("code");
            }
        }

        // Remove .code from statusEducationYear (OLD doesn't include it)
        Object seObj = cubaMap.get("statusEducationYear");
        if (seObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> seMap = (Map<String, Object>) seObj;
            seMap.remove("code");
        }

        // OLD-HEMIS compatibility: Remove fields that NEW-hemis adds but OLD-hemis doesn't have
        String[] fieldsToRemove = {
            "commonSpecialityCode", "commonSpecialityName",
            "createdBy", "updatedBy",
            "fullname",
            "specialityCode", "specialityName",
            "version",
            "deleteTs", "deletedBy"
        };
        for (String field : fieldsToRemove) {
            cubaMap.remove(field);
        }

        // OLD-HEMIS compatibility: Strip version from ALL nested classifier/reference objects
        // (entity endpoints don't have version in classifiers, only service endpoints do)
        stripVersionFromNestedObjects(cubaMap);

        // OLD-HEMIS compatibility: Reorder fields to match OLD-HEMIS exact order
        cubaMap = reorderFieldsToMatchOldHemis(cubaMap);

        return cubaMap;
    }

    /**
     * Strip .code and .soato.parent_code from terrain/currentTerrain nested objects.
     * OLD-HEMIS entity view doesn't include these fields in terrain objects.
     */
    @SuppressWarnings("unchecked")
    private void stripTerrainExtras(Map<String, Object> cubaMap, String terrainField) {
        Object terrainObj = cubaMap.get(terrainField);
        if (terrainObj instanceof Map) {
            Map<String, Object> terrainMap = (Map<String, Object>) terrainObj;
            terrainMap.remove("code");
            // Strip soato.parent_code inside terrain; strip name_ru only from terrain (NOT currentTerrain)
            Object terrainSoatoObj = terrainMap.get("soato");
            if (terrainSoatoObj instanceof Map) {
                Map<String, Object> terrainSoatoMap = (Map<String, Object>) terrainSoatoObj;
                terrainSoatoMap.remove("parent_code");
                if ("terrain".equals(terrainField)) {
                    terrainSoatoMap.remove("name_ru");
                }
            }
        }
    }

    /**
     * Remove "version" from all nested Map objects (classifiers, soato, terrain, etc.)
     * Entity endpoints don't include version in nested objects (only service endpoints do).
     */
    @SuppressWarnings("unchecked")
    private void stripVersionFromNestedObjects(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, Object> nested = (Map<String, Object>) value;
                nested.remove("version");
                stripVersionFromNestedObjects(nested);
            }
        }
    }

    /**
     * Reorder fields to match OLD-HEMIS exact field order
     * OLD-HEMIS has a specific field order that must be preserved for 100% compatibility
     */
    private Map<String, Object> reorderFieldsToMatchOldHemis(Map<String, Object> original) {
        // OLD-HEMIS exact field order (from actual response analysis)
        String[] fieldOrder = {
            "_entityName", "_instanceName", "id", "currentEducationYear", "isGraduate", "country",
            "statusOrderDate", "decreeInfoName", "educationType", "groupId", "language", "socialCategory",
            "educationYear", "educationForm", "faculty", "points", "academicMobileType", "transferCountry",
            "povertyLevel", "studentSuccess", "specialityMaster", "currentTerrain", "statusEducationYear",
            "specialityDoctoral", "createTs", "graduationYear", "tag", "decreeInfoNumber", "responsiblePersonPhone",
            "terrain", "geoAddress", "admissionType", "serialNumber", "specialityOrdinatura", "currentSoato",
            "active", "statusOrderCategory", "decreeInfoDate", "academicReason", "lastname", "groupName",
            "statusOrderNumber", "nationality", "phone", "livingStatus", "roommateType", "grantType", "status",
            "enrollOrderName", "pinfl", "birthday", "studentType", "firstname", "code", "paymentForm", "gender",
            "university", "soato", "parentPhone", "speciality", "enrollOrderDate", "expelReason", "enrollOrderNumber",
            "stipendRate", "course", "studentStatus", "roommateCount", "isDuplicate", "email", "address",
            "citizenship", "eduStartDate", "passportGivenDate", "verified", "currentAddress", "fathername",
            "transferUniversity", "graduationDate", "statusOrderName", "accomodation", "enrollOrderCategory",
            "transferType", "studyDuration", "specialityBachelor", "updateTs", "doctoralStudentType"
        };

        Map<String, Object> ordered = new LinkedHashMap<>();

        // First, add fields in the correct order
        for (String field : fieldOrder) {
            if (original.containsKey(field)) {
                ordered.put(field, original.get(field));
            }
        }

        // Then, add any remaining fields not in the order list (shouldn't happen, but just in case)
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            if (!ordered.containsKey(entry.getKey())) {
                ordered.put(entry.getKey(), entry.getValue());
            }
        }

        return ordered;
    }

    /**
     * Talaba ma'lumotlarini o'zgartirish (Partial Update)
     *
     * ✅ REFACTORED: Uses service.partialUpdate() - only passed fields are updated
     * ✅ BACKWARD COMPATIBLE: Accepts CUBA Map format
     * ✅ CUBA PATTERN: PUT = partial update (only fields in JSON body are changed)
     */

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{entityId}")
    @Operation(
        summary = "Talaba ma'lumotlarini o'zgartirish",
        description = """
            Mavjud talaba ma'lumotlarini qisman yangilash.

            **Faqat JSON body da yuborilgan fieldlar yangilanadi!**
            Yuborilmagan fieldlar o'zgartirilmaydi.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_EStudent/{entityId}
            **Auth:** Bearer token (required)

            **Misol request body:**
            ```json
            {
                "phone": "+998901234567",
                "email": "student@example.com"
            }
            ```
            Faqat phone va email yangilanadi, boshqa fieldlar o'zgartirilmaydi.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Talaba yangilandi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov - Validatsiya xatosi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi - Token noto'g'ri yoki muddati o'tgan"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - Foydalanuvchida yangilash huquqi yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan talaba topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Talaba UUID identifikatori")
            @PathVariable UUID entityId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Yangilanadigan fieldlar (faqat yuborilgan fieldlar o'zgaradi)",
                required = true,
                content = @Content(mediaType = "application/json",
                    schema = @Schema()))
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "Response view — _local bo'lsa to'liq entity qaytariladi")
            @RequestParam(required = false) String responseView) {

        log.info("PUT student id: {} - partial update (only passed fields)", entityId);
        log.debug("Fields to update: {}", body.keySet());

        try {
            // Convert CUBA Map to DTO (null fields stay null)
            StudentDto dto = adapter.fromMap(body, StudentDto.class);

            // Service layer - PARTIAL UPDATE (null values ignored)
            // Only fields passed in JSON body will be updated
            StudentDto updated = studentService.partialUpdate(entityId, dto);

            // OLD-HEMIS COMPATIBLE:
            // responseView=_local → to'liq entity qaytarish (_local viewda)
            // responseView=null → minimal response (faqat _entityName, _instanceName, id)
            if (responseView != null) {
                Map<String, Object> fullResponse = adapter.toMap(updated, ENTITY_NAME, returnNulls, responseView);
                log.info("Student {} updated successfully (responseView={})", entityId, responseView);
                return ResponseEntity.ok(fullResponse);
            }

            Map<String, Object> minimalResponse = new LinkedHashMap<>();
            minimalResponse.put("_entityName", ENTITY_NAME);
            minimalResponse.put("_instanceName", buildInstanceName(updated));
            minimalResponse.put("id", updated.getId().toString());

            log.info("Student {} updated successfully", entityId);
            return ResponseEntity.ok(minimalResponse);

        } catch (ResourceNotFoundException e) {
            log.warn("Student not found for update: {}", entityId);
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }
    }

    /**
     * Talabani o'chirish (SOFT DELETE ONLY)
     *
     * ✅ REFACTORED: Uses service.softDelete() - NO PHYSICAL DELETE
     * ✅ BACKWARD COMPATIBLE: Same response as OLD-HEMIS (200 OK empty body)
     *
     * CRITICAL: This is a soft delete (sets delete_ts).
     * Physical DELETE is blocked at service and database level.
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Talabani o'chirish",
        description = """
            Talabani soft delete qilish (delete_ts ni belgilaydi).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** DELETE /app/rest/v2/entities/hemishe_EStudent/{entityId}
            **Auth:** Bearer token (required)

            **Muhim:**
            - Bu soft delete - ma'lumot bazadan o'chirilmaydi, faqat delete_ts belgilanadi.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Talaba o'chirildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan ID bilan talaba topilmadi")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "Talaba UUID identifikatori")
            @PathVariable UUID entityId) {
        log.debug("DELETE student id: {} (SOFT DELETE via service)", entityId);

        try {
            // University isolation: extract user's university from JWT
            String userUniversityCode = getUniversityCodeFromContext();
            studentService.softDelete(entityId, userUniversityCode);

            // OLD-HEMIS COMPATIBLE: Return 200 OK with empty body
            return ResponseEntity.ok().build();

        } catch (ResourceNotFoundException e) {
            log.debug("Student not found for delete: {}", entityId);
            // OLD-HEMIS COMPATIBLE: Return 404 with JSON error body
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

    /**
     * Barcha talabalar ro'yxati (paginated)
     *
     * ✅ REFACTORED: Uses service layer with proper pagination
     * ✅ BACKWARD COMPATIBLE: Same response format and parameters
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
        summary = "Barcha talabalar ro'yxati",
        description = """
            Sahifalangan talabalar ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EStudent
            **Auth:** Bearer token (required)

            **Pagination:**
            - offset: Boshlang'ich pozitsiya (default: 0)
            - limit: Sahifadagi yozuvlar soni (default: 50)
            - returnCount: X-Total-Count headerini qaytarish
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Talabalar ro'yxati qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish (X-Total-Count header)") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Tartiblash (masalan: firstname,ASC)") @RequestParam(required = false) String sort,
            @Parameter(description = "Dinamik atributlarni qaytarish") @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi") @RequestParam(required = false) String view) {

        log.debug("GET all students - offset: {}, limit: {} (via service)", offset, limit);

        // Parse sort parameter — whitelist guard (OWASP A03 SQL injection vector via ORDER BY).
        // PostgreSQL'da arbitrary property name = sequential scan DoS yoki @Formula column'ni
        // expose qilish riski. Faqat indexed/safe columnlar ruxsat etiladi.
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length >= 2) {
                String property = sortParts[0].trim();
                if (!STUDENT_SORT_WHITELIST.contains(property)) {
                    log.warn("Rejected non-whitelisted sort property: {}", property);
                    property = "createTs"; // safe fallback
                }
                Sort.Direction direction = sortParts[1].equalsIgnoreCase("DESC")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
                sorting = Sort.by(direction, property);
            }
        }

        // Service layer with pagination (guard against limit=0)
        int safeLimit = (limit != null && limit > 0) ? limit : 20;
        int safeOffset = (offset != null && offset >= 0) ? offset : 0;
        PageRequest pageRequest = PageRequest.of(safeOffset / safeLimit, safeLimit, sorting);
        List<Map<String, Object>> cubaMaps;
        long totalElements;

        if (view != null && !VIEW_LOCAL.equals(view)) {
            // Expanded view — use StudentLegacyMapper for nested classifier objects (like getById does)
            Page<Student> entityPage = studentService.findAllEntities(pageRequest);
            totalElements = entityPage.getTotalElements();
            cubaMaps = new ArrayList<>();
            for (Student student : entityPage.getContent()) {
                Map<String, Object> cubaMap = studentLegacyMapper.toLegacyMap(student);
                cubaMap = postProcessStudentLegacyMap(cubaMap, returnNulls);
                cubaMaps.add(cubaMap);
            }
        } else {
            // _local view or no view — flat DTO (existing logic)
            Page<StudentDto> page = studentService.findAll(pageRequest);
            totalElements = page.getTotalElements();
            List<StudentDto> dtos = page.getContent();
            cubaMaps = adapter.toMapList(dtos, ENTITY_NAME, returnNulls, view);

            // OLD-HEMIS compatibility: speciality qaytariladi (adapter _speciality sifatida o'tkazib yuboradi)
            for (int i = 0; i < cubaMaps.size(); i++) {
                Map<String, Object> map = cubaMaps.get(i);
                StudentDto dto = dtos.get(i);
                if (dto.getSpeciality() != null) {
                    map.put("speciality", dto.getSpeciality());
                }
            }
        }

        // Add count header if requested (CUBA compatibility)
        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(totalElements))
                .body(cubaMaps);
        }

        return ResponseEntity.ok(cubaMaps);
    }

    /**
     * Yangi talaba yaratish
     *
     * ✅ REFACTORED: Uses service layer with validation
     * ✅ BACKWARD COMPATIBLE: Accepts CUBA Map format
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
        summary = "Talaba yaratish",
        description = """
            Yangi talaba yozuvini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EStudent
            **Auth:** Bearer token (required)

            Request body CUBA entity formatida bo'lishi kerak. Boshqa entitylarga
            reference berishda nested object ichida faqat identifikator (id yoki code) yuboring.

            **Misol request body:**
            ```json
            {
                "code": "520241100001",
                "pinfl": "00000000000000",
                "serialNumber": "AA0000000",
                "firstname": "Ism",
                "lastname": "Familiya",
                "fathername": "Otasining ismi",
                "university": { "code": "520" },
                "educationType": { "code": "11" },
                "educationForm": { "code": "11" },
                "educationYear": "2024",
                "studentStatus": { "code": "11" }
            }
            ```

            **Qaytariladigan natija (OLD-HEMIS format):**
            ```json
            {
                "_entityName": "hemishe_EStudent",
                "_instanceName": "FAMILIYA ISM",
                "id": "uuid-here"
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Talaba yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov - Validatsiya xatosi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - Foydalanuvchida yaratish huquqi yo'q")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls) {

        // PII safety: never log full request body (contains PINFL, passport, address, parent_phone).
        log.info("POST create student - keys={}", body == null ? "null" : body.keySet());

        try {
            // CUBA UPSERT: if body contains 'id' and student exists, update instead of create
            Object idObj = body.get("id");
            if (idObj != null) {
                try {
                    UUID existingId = UUID.fromString(idObj.toString());
                    try {
                        StudentDto existing = studentService.findById(existingId);
                        // Student exists — do UPDATE (CUBA upsert pattern)
                        log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                        StudentDto updateDto = adapter.fromMap(body, StudentDto.class);
                        StudentDto updated = studentService.update(existingId, updateDto);

                        Map<String, Object> minimalResponse = new LinkedHashMap<>();
                        minimalResponse.put("_entityName", ENTITY_NAME);
                        minimalResponse.put("_instanceName", buildInstanceName(updated));
                        minimalResponse.put("id", updated.getId().toString());

                        log.info("Student upserted (updated) with id: {}", updated.getId());
                        return ResponseEntity.ok(minimalResponse);
                    } catch (ResourceNotFoundException e) {
                        // Student not found — proceed with create
                        log.info("POST with id={} — student not found, creating new", existingId);
                    }
                } catch (IllegalArgumentException e) {
                    // id is not a valid UUID — proceed with create
                    log.debug("POST id='{}' is not a valid UUID, proceeding with create", idObj);
                }
            }

            // Convert CUBA Map to DTO
            StudentDto dto = adapter.fromMap(body, StudentDto.class);
            log.info("Converted DTO: code={}, pinfl={}, university={}, educationType={}",
                     dto.getCode(), uz.hemis.common.vo.Pinfl.maskOrEmpty(dto.getPinfl()),
                     dto.getUniversity(), dto.getEducationType());

            // Service layer - with validation, cache, audit
            StudentDto created = studentService.create(dto);

            // OLD-HEMIS COMPATIBLE: Return minimal response (only _entityName, _instanceName, id)
            // Old-hemis POST response format: {"_entityName":"hemishe_EStudent","_instanceName":"...","id":"..."}
            Map<String, Object> minimalResponse = new LinkedHashMap<>();
            minimalResponse.put("_entityName", ENTITY_NAME);
            minimalResponse.put("_instanceName", buildInstanceName(created));
            minimalResponse.put("id", created.getId().toString());

            log.info("Student created successfully with id: {}", created.getId());
            return ResponseEntity.ok(minimalResponse);
        } catch (Exception e) {
            log.error("Error creating student: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Add null fields (nameRu, nameEn, active) to classifier nested objects
     */
    @SuppressWarnings("unchecked")
    private void addClassifierNullFields(Map<String, Object> parent, String fieldName, Object jsonNull) {
        Object obj = parent.get(fieldName);
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            map.putIfAbsent("nameRu", jsonNull);
            map.putIfAbsent("nameEn", jsonNull);
            map.putIfAbsent("active", map.get("active") != null ? map.get("active") : true);
        }
    }

    /**
     * Extract current user's university code from JWT token
     */
    private String getUniversityCodeFromContext() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            org.springframework.security.oauth2.jwt.Jwt jwt = jwtAuth.getToken();
            String userId = jwt.getSubject();
            if (userId != null) {
                try {
                    UUID userUuid = UUID.fromString(userId);
                    return userLegacyService.findUniversityCodeById(userUuid).orElse(null);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid UUID in JWT subject: {}", userId);
                }
            }
        }
        return null;
    }

}
