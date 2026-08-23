package uz.hemis.api.legacy.controller.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.api.legacy.adapter.LegacyResponseHelper;
import uz.hemis.domain.entity.employee.UniversityEmployeeType;
import uz.hemis.service.legacy.employee.EmployeeRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * University Employee Type Entity Controller (CUBA Pattern)
 * Tag: 09.OTM xodimlari kategoriyasi
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_HUniversityEmployeeType</p>
 *
 * <p><strong>OTM xodimlari kategoriyalari:</strong></p>
 * <ul>
 *   <li>10 - Boshqa</li>
 *   <li>11 - Administrativ-boshqaruv xodim</li>
 *   <li>12 - Professor-o'qituvchi xodim</li>
 *   <li>13 - O'quv-yordamchi va texnik xodim</li>
 *   <li>14 - Xizmat ko'rsatuvchi xodim</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "09.OTM xodimlari kategoriyasi", description = "OTM xodimlari turlari klassifikatori - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HUniversityEmployeeType")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityEmployeeTypeEntityController {

    private final EmployeeRefLegacyService employeeRefService;
    private final LegacySecurityHelper securityHelper;
    private static final String ENTITY_NAME = "hemishe_HUniversityEmployeeType";

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta xodim turini olish",
        description = """
            Kod bo'yicha xodim turi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}
            **Auth:** Bearer token (required)

            **Turlar:** 10=Boshqa, 11=Administrativ, 12=Professor, 13=Texnik, 14=Xizmat
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Tur kodi")
            @PathVariable String entityId,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET UniversityEmployeeType by code: {}", entityId);

        Optional<UniversityEmployeeType> entity = employeeRefService.findUniversityEmployeeTypeById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(employeeRefService.toUniversityEmployeeTypeMap(entity.get(), returnNulls));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{entityId}")
    @Operation(
        summary = "Xodim turini yangilash",
        description = """
            Xodim turi ma'lumotlarini yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}
            **Auth:** Bearer token (required)

            Faqat yuborilgan maydonlar yangilanadi.

            **Mavjud fieldlar:**
            - name - O'zbekcha nomi
            - nameEn - Inglizcha nomi
            - nameRu - Ruscha nomi
            - active - Faol holati (true/false)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "Tur kodi")
            @PathVariable String entityId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Yangilanadigan maydonlar",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema()
                )
            )
            @RequestBody Map<String, Object> entityData) {

        log.info("PUT UniversityEmployeeType - entityId: {}, data: {}", entityId, entityData);

        Optional<UniversityEmployeeType> existingOpt = employeeRefService.findUniversityEmployeeTypeById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("UniversityEmployeeType not found: {}", entityId);
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        UniversityEmployeeType saved = employeeRefService.updateUniversityEmployeeType(
            existingOpt.get(), entityData, securityHelper.getCurrentUsername());
        log.info("UniversityEmployeeType updated successfully: {}", entityId);

        return ResponseEntity.ok(employeeRefService.toUniversityEmployeeTypeMinimalMap(saved));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Xodim turini o'chirish",
        description = """
            Xodim turini o'chirish (soft delete).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** DELETE /app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}
            **Auth:** Bearer token (required)

            ⚠️ **Diqqat:** Bu soft delete - ma'lumot bazadan o'chirilmaydi,
            faqat deleteTs va deletedBy fieldlari o'rnatiladi.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "Tur kodi")
            @PathVariable String entityId) {

        log.info("DELETE UniversityEmployeeType - entityId: {}", entityId);

        Optional<UniversityEmployeeType> existingOpt = employeeRefService.findUniversityEmployeeTypeById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("UniversityEmployeeType not found for delete: {}", entityId);
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        employeeRefService.softDeleteUniversityEmployeeType(existingOpt.get(), securityHelper.getCurrentUsername());
        log.info("UniversityEmployeeType soft deleted successfully: {}", entityId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
        summary = "Yangi xodim turini yaratish",
        description = """
            Yangi xodim turi yaratish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_HUniversityEmployeeType
            **Auth:** Bearer token (required)

            **Majburiy fieldlar:**
            - code - Tur kodi (unique)
            - name - O'zbekcha nomi

            **Ixtiyoriy fieldlar:**
            - nameEn - Inglizcha nomi
            - nameRu - Ruscha nomi
            - active - Faol holati (default: true)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov yoki code allaqachon mavjud"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Yangi xodim turi ma'lumotlari",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema()
                )
            )
            @RequestBody Map<String, Object> entityData) {

        log.info("POST UniversityEmployeeType - data: {}", entityData);

        Object codeObj = entityData.get("code");
        if (codeObj == null) {
            codeObj = entityData.get("id");
        }
        String code = codeObj != null ? String.valueOf(codeObj) : null;
        String name = (String) entityData.get("name");

        if (code == null || code.isBlank()) {
            log.warn("POST UniversityEmployeeType - code is required");
            return ResponseEntity.badRequest().body(LegacyResponseHelper.errorMap("code is required", "code maydoni majburiy"));
        }

        if (name == null || name.isBlank()) {
            log.warn("POST UniversityEmployeeType - name is required");
            return ResponseEntity.badRequest().body(LegacyResponseHelper.errorMap("name is required", "name maydoni majburiy"));
        }

        UniversityEmployeeType saved = employeeRefService.createOrUpsertUniversityEmployeeType(
            code, name, entityData, securityHelper.getCurrentUsername());
        log.info("UniversityEmployeeType created/upserted successfully: {}", code);

        return ResponseEntity.ok(employeeRefService.toUniversityEmployeeTypeMinimalMap(saved));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
        summary = "Barcha xodim turlari",
        description = """
            Sahifalangan xodim turlari ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeType
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all UniversityEmployeeType - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.by(Sort.Direction.ASC, "code");
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length >= 2) {
                Sort.Direction direction = sortParts[1].equalsIgnoreCase("DESC")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
                sorting = Sort.by(direction, sortParts[0]);
            }
        }

        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), limit, sorting);
        Page<UniversityEmployeeType> page = employeeRefService.findAllUniversityEmployeeType(pageRequest);

        List<Map<String, Object>> result = page.getContent().stream()
            .map(e -> employeeRefService.toUniversityEmployeeTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    @Operation(
        summary = "Xodim turlarini qidirish (GET)",
        description = """
            URL parametrlari orqali xodim turlarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **CUBA Filter Format (URL encoded JSON):**
            ```
            filter={"conditions":[{"property":"active","operator":"=","value":true}]}
            ```

            **Qo'llab-quvvatlanadigan operatorlar:** =, <>, like, in
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        log.debug("GET search UniversityEmployeeType with filter: {}", filter);

        List<UniversityEmployeeType> entities = employeeRefService.findAllUniversityEmployeeType(Sort.by(Sort.Direction.ASC, "code"));

        if (filter != null && !filter.isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> filterMap = new ObjectMapper().readValue(filter, Map.class);
                entities = employeeRefService.applyCubaFilter(entities, filterMap);
            } catch (Exception e) {
                log.warn("Failed to parse CUBA filter: {}", e.getMessage());
            }
        }

        int fromIndex = Math.min(offset, entities.size());
        int toIndex = Math.min(offset + limit, entities.size());
        List<UniversityEmployeeType> paginatedEntities = entities.subList(fromIndex, toIndex);

        List<Map<String, Object>> result = paginatedEntities.stream()
            .map(e -> employeeRefService.toUniversityEmployeeTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/search")
    @Operation(
        summary = "Xodim turlarini qidirish (POST)",
        description = """
            JSON filter orqali xodim turlarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **CUBA Filter Format:**
            ```json
            {
              "filter": {
                "conditions": [
                  {"property": "code", "operator": "=", "value": "12"}
                ]
              },
              "limit": 50,
              "offset": 0
            }
            ```

            **Qo'llab-quvvatlanadigan operatorlar:** =, <>, like, in
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search UniversityEmployeeType with body: {}", requestBody);

        List<UniversityEmployeeType> entities = employeeRefService.findAllUniversityEmployeeType(Sort.by(Sort.Direction.ASC, "code"));

        if (requestBody != null && requestBody.containsKey("filter")) {
            entities = employeeRefService.applyCubaFilter(entities, requestBody.get("filter"));
        }

        int limit = 50;
        int offset = 0;
        if (requestBody != null) {
            if (requestBody.containsKey("limit")) {
                Object limitObj = requestBody.get("limit");
                if (limitObj instanceof Number) {
                    limit = ((Number) limitObj).intValue();
                }
            }
            if (requestBody.containsKey("offset")) {
                Object offsetObj = requestBody.get("offset");
                if (offsetObj instanceof Number) {
                    offset = ((Number) offsetObj).intValue();
                }
            }
        }

        int fromIndex = Math.min(offset, entities.size());
        int toIndex = Math.min(offset + limit, entities.size());
        List<UniversityEmployeeType> paginatedEntities = entities.subList(fromIndex, toIndex);

        List<Map<String, Object>> result = paginatedEntities.stream()
            .map(e -> employeeRefService.toUniversityEmployeeTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
