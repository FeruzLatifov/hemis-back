package uz.hemis.api.legacy.controller.employee;

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
import uz.hemis.domain.entity.employee.UniversityEmployeeStatusType;
import uz.hemis.service.legacy.employee.EmployeeRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * University Employee Status Type Entity Controller (CUBA Pattern)
 * Tag: 06.Xodim lavozimlari
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_HUniversityEmployeeStatusType</p>
 *
 * <p><strong>Xodim holatlari:</strong></p>
 * <ul>
 *   <li>11 - Ishlamoqda</li>
 *   <li>12 - Ta'tilda</li>
 *   <li>13 - Xizmat safarida</li>
 *   <li>14 - Bo'shagan</li>
 * </ul>
 *
 * <p><strong>100% Backward Compatible:</strong></p>
 * <ul>
 *   <li>Preserves exact CUBA entity API pattern</li>
 *   <li>URL: /app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType</li>
 *   <li>Response format: CUBA Map structure with _entityName, _instanceName</li>
 *   <li>Parameters: returnNulls, view (CUBA-compatible)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "06.Xodim lavozimlari", description = "Xodim holatlari klassifikatori - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityEmployeeStatusTypeEntityController {

    private final EmployeeRefLegacyService employeeRefService;

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta xodim holatini olish",
        description = """
            Kod bo'yicha xodim holati ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/{entityId}
            **Auth:** Bearer token (required)

            **Holatlar:** 11=Ishlamoqda, 12=Ta'tilda, 13=Xizmat safarida, 14=Bo'shagan
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Holat ma'lumotlari qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi - Berilgan kod bilan holat topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Holat kodi", example = "11")
            @PathVariable String entityId,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET UniversityEmployeeStatusType by code: {}", entityId);

        Optional<UniversityEmployeeStatusType> entity = employeeRefService.findUniversityEmployeeStatusTypeById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(employeeRefService.toUniversityEmployeeStatusTypeMap(entity.get(), returnNulls));
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping
    @Operation(
        summary = "Barcha xodim holatlari",
        description = """
            Sahifalangan xodim holatlari ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType
            **Auth:** Bearer token (required)

            **Holatlar:** 11=Ishlamoqda, 12=Ta'tilda, 13=Xizmat safarida, 14=Bo'shagan
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Holatlar ro'yxati qaytarildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish (X-Total-Count header)")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni")
            @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Tartiblash (masalan: name,ASC)")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "CUBA view nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET all UniversityEmployeeStatusType - offset: {}, limit: {}", offset, limit);

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
        Page<UniversityEmployeeStatusType> page = employeeRefService.findAllUniversityEmployeeStatusType(pageRequest);

        List<Map<String, Object>> result = page.getContent().stream()
            .map(e -> employeeRefService.toUniversityEmployeeStatusTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/search")
    @Operation(
        summary = "Xodim holatlarini qidirish (GET)",
        description = """
            URL parametrlari orqali xodim holatlarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/search
            **Auth:** Bearer token (required)
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search UniversityEmployeeStatusType with filter: {}", filter);

        List<UniversityEmployeeStatusType> entities = employeeRefService.findAllUniversityEmployeeStatusType(Sort.by(Sort.Direction.ASC, "code"));
        List<Map<String, Object>> result = entities.stream()
            .map(e -> employeeRefService.toUniversityEmployeeStatusTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @PostMapping("/search")
    @Operation(
        summary = "Xodim holatlarini qidirish (POST)",
        description = """
            JSON filter orqali xodim holatlarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/search
            **Auth:** Bearer token (required)
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search UniversityEmployeeStatusType with filter: {}", filter);

        List<UniversityEmployeeStatusType> entities = employeeRefService.findAllUniversityEmployeeStatusType(Sort.by(Sort.Direction.ASC, "code"));
        List<Map<String, Object>> result = entities.stream()
            .map(e -> employeeRefService.toUniversityEmployeeStatusTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
