package uz.hemis.api.legacy.controller.academic;

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
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.api.legacy.util.CubaSearchBodyParser;
import uz.hemis.domain.entity.academic.AcademicGroup;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Akademik Guruhlar Controller - CUBA REST API Pattern
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_RAcademicGroup</li>
 *   <li>Table: hemishe_r_academic_group</li>
 *   <li>Primary key: id (UUID)</li>
 *   <li>Base URL: /app/rest/v2/entities/hemishe_RAcademicGroup</li>
 *   <li>100% backward compatible with OLD-HEMIS CUBA Platform REST API</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "32.Akademik hisobotlar akademik guruhlar", description = "Akademik guruhlar hisobotlarini boshqarish API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RAcademicGroup")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AcademicGroupEntityController {

    private final AcademicEntityLegacyService academicService;
    private final CubaFilterHelper filterHelper;

    private static final String ENTITY_NAME = "hemishe_RAcademicGroup";

    // =====================================================
    // 1. GET BY ID
    // =====================================================

    @Operation(
        summary = "Akademik guruh yozuvini ID bo'yicha olish",
        description = "Berilgan UUID bo'yicha bitta yozuvni qaytaradi."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/{entityId}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Entity ID (UUID)", required = true)
            @PathVariable String entityId,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("GET hemishe_RAcademicGroup: {}", entityId);

        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicGroup> entity = academicService.findAcademicGroupById(id);

            if (entity.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Entity not found",
                    "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"
                ));
            }

            return ResponseEntity.ok(academicService.toAcademicGroupMap(entity.get(), returnNulls));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Invalid UUID",
                "details", "Invalid UUID format: " + entityId
            ));
        }
    }

    // =====================================================
    // 2. UPDATE
    // =====================================================

    @Operation(
        summary = "Akademik guruh yozuvini yangilash",
        description = "Mavjud yozuvni yangilaydi."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @PreAuthorize("hasAuthority('students.edit')")
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(
            @PathVariable String entityId,
            @RequestBody Map<String, Object> entityData) {

        log.info("UPDATE hemishe_RAcademicGroup: {}", entityId);

        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicGroup> existingOpt = academicService.findAcademicGroupById(id);

            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Entity not found",
                    "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"
                ));
            }

            AcademicGroup entity = existingOpt.get();
            academicService.updateAcademicGroupFromMap(entity, entityData);
            entity.setUpdateTs(LocalDateTime.now());

            AcademicGroup saved = academicService.saveAcademicGroup(entity);
            return ResponseEntity.ok(academicService.toAcademicGroupMap(saved, false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Invalid UUID",
                "details", "Invalid UUID format: " + entityId
            ));
        }
    }

    // =====================================================
    // 3. DELETE (Soft Delete)
    // =====================================================

    @Operation(
        summary = "Akademik guruh yozuvini o'chirish",
        description = "Soft delete - delete_ts qo'yiladi."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @PreAuthorize("hasAuthority('students.delete')")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<?> delete(@PathVariable String entityId) {

        log.info("DELETE hemishe_RAcademicGroup: {}", entityId);

        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicGroup> entity = academicService.findAcademicGroupById(id);

            if (entity.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Entity not found",
                    "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"
                ));
            }

            academicService.deleteAcademicGroup(entity.get());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Invalid UUID",
                "details", "Invalid UUID format: " + entityId
            ));
        }
    }

    // =====================================================
    // 4. SEARCH (GET)
    // =====================================================

    @Operation(
        summary = "Akademik guruhlarni qidirish (GET)",
        description = "Filter shartlari bo'yicha qidiradi."
    )
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam String filter,
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_RAcademicGroup (GET) - filter: {}", filter);
        return search(filter, offset, limit, returnCount, returnNulls);
    }

    // =====================================================
    // 5. SEARCH (POST)
    // =====================================================

    @Operation(
        summary = "Akademik guruhlarni qidirish (POST)",
        description = "Filter shartlari bo'yicha qidiradi."
    )
    @PreAuthorize("hasAuthority('students.view')")
    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_RAcademicGroup (POST) - filter: {}", filterBody);

        String filterStr = null;
        Integer bodyOffset = offset;
        Integer bodyLimit = limit;

        if (filterBody != null) {
            filterStr = CubaSearchBodyParser.extractFilter(filterBody);
            bodyOffset = CubaSearchBodyParser.extractOffset(filterBody, bodyOffset);
            bodyLimit = CubaSearchBodyParser.extractLimit(filterBody, bodyLimit);
        }
        return search(filterStr, bodyOffset, bodyLimit, returnCount, returnNulls);
    }

    // =====================================================
    // 6. LIST ALL
    // =====================================================

    @Operation(
        summary = "Barcha akademik guruhlar yozuvlarini olish",
        description = "Sahifalangan ro'yxat."
    )
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping({"", "/"})
    public ResponseEntity<List<Map<String, Object>>> listAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("LIST ALL hemishe_RAcademicGroup");

        List<AcademicGroup> allEntities = academicService.findAllAcademicGroup();

        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<AcademicGroup> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> academicService.toAcademicGroupMap(e, returnNulls))
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
        summary = "Yangi akademik guruh yozuvi yaratish",
        description = "Yangi yozuv yaratadi."
    )
    @PreAuthorize("hasAuthority('students.edit')")
    @PostMapping({"", "/"})
    public ResponseEntity<?> create(@RequestBody Map<String, Object> entityData) {

        log.info("CREATE hemishe_RAcademicGroup: {}", entityData);

        try {
            AcademicGroup entity = new AcademicGroup();

            if (entityData.containsKey("id") && entityData.get("id") != null) {
                entity.setId(UUID.fromString(entityData.get("id").toString()));
            }

            academicService.updateAcademicGroupFromMap(entity, entityData);
            AcademicGroup saved = academicService.saveAcademicGroup(entity);
            return ResponseEntity.ok(academicService.toAcademicGroupMap(saved, false));

        } catch (Exception e) {
            log.error("CREATE xatosi: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Server error",
                "details", e.getClass().getSimpleName() + ": " + e.getMessage()
            ));
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private ResponseEntity<List<Map<String, Object>>> search(
            String filter, Integer offset, Integer limit, Boolean returnCount, Boolean returnNulls) {

        List<AcademicGroup> allEntities = academicService.findAllAcademicGroup();

        if (filter != null && !filter.isEmpty()) {
            allEntities = filterHelper.applyFilter(allEntities, filter,
                req -> getFieldValue(req.entity(), req.property()));
        }

        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<AcademicGroup> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> academicService.toAcademicGroupMap(e, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    private Object getFieldValue(AcademicGroup entity, String property) {
        if (property == null) return null;

        return switch (property) {
            case "universityCode" -> entity.getUniversityCode();
            case "universityName" -> entity.getUniversityName();
            case "educationTypeCode" -> entity.getEducationTypeCode();
            case "educationTypeName" -> entity.getEducationTypeName();
            case "educationFormCode" -> entity.getEducationFormCode();
            case "educationFormName" -> entity.getEducationFormName();
            case "educationYearCode" -> entity.getEducationYearCode();
            case "educationYearName" -> entity.getEducationYearName();
            case "groupCount" -> entity.getGroupCount();
            default -> null;
        };
    }
}
