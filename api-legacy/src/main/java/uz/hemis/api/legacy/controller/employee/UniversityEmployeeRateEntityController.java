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
import uz.hemis.domain.entity.employee.EmployeeRate;
import uz.hemis.service.legacy.employee.EmployeeRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * University Employee Rate Entity Controller (CUBA Pattern)
 * Tag: 06.Xodim lavozimlari
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_HUniversityEmployeeRate</p>
 *
 * <p><strong>Xodim mehnat stavkalari:</strong></p>
 * <ul>
 *   <li>11 - 1,00 stavka</li>
 *   <li>12 - 0,75 stavka</li>
 *   <li>13 - 0,50 stavka</li>
 *   <li>14 - 0,25 stavka</li>
 *   <li>15 - 0,30 stavka</li>
 *   <li>16 - 0,20 stavka</li>
 *   <li>17 - 0,15 stavka</li>
 *   <li>18 - 0,10 stavka</li>
 *   <li>19 - 0,05 stavka</li>
 * </ul>
 *
 * <p><strong>Master/Replica Routing:</strong></p>
 * <ul>
 *   <li>All GET endpoints use @Transactional(readOnly = true) -> Replica DB</li>
 *   <li>Write operations would use @Transactional -> Master DB</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "06.Xodim lavozimlari", description = "Xodim mehnat stavkalari klassifikatori - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HUniversityEmployeeRate")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityEmployeeRateEntityController {

    private final EmployeeRefLegacyService employeeRefService;

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta stavkani olish",
        description = """
            Kod bo'yicha mehnat stavkasi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeRate/{entityId}
            **Auth:** Bearer token (required)
            **Database:** Replica (read-only)

            **Stavkalar:** 11=1,00, 12=0,75, 13=0,50, 14=0,25, 15=0,30, 16=0,20, 17=0,15, 18=0,10, 19=0,05
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Stavka kodi", example = "11")
            @PathVariable String entityId,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET EmployeeRate by code: {}", entityId);

        Optional<EmployeeRate> entity = employeeRefService.findEmployeeRateById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(employeeRefService.toEmployeeRateMap(entity.get(), returnNulls));
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping
    @Operation(
        summary = "Barcha xodim mehnat stavkalari",
        description = """
            Sahifalangan mehnat stavkalari ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeRate
            **Auth:** Bearer token (required)
            **Database:** Replica (read-only)
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

        log.debug("GET all EmployeeRate - offset: {}, limit: {}", offset, limit);

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
        Page<EmployeeRate> page = employeeRefService.findAllEmployeeRate(pageRequest);

        List<Map<String, Object>> result = page.getContent().stream()
            .map(e -> employeeRefService.toEmployeeRateMap(e, returnNulls))
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
        summary = "Stavkalarni qidirish (GET)",
        description = """
            URL parametrlari orqali mehnat stavkalarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            **Database:** Replica (read-only)
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search EmployeeRate with filter: {}", filter);

        List<EmployeeRate> entities = employeeRefService.findAllEmployeeRate(Sort.by(Sort.Direction.ASC, "code"));
        List<Map<String, Object>> result = entities.stream()
            .map(e -> employeeRefService.toEmployeeRateMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @PostMapping("/search")
    @Operation(
        summary = "Stavkalarni qidirish (POST)",
        description = """
            JSON filter orqali mehnat stavkalarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            **Database:** Replica (read-only)
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search EmployeeRate with filter: {}", filter);

        List<EmployeeRate> entities = employeeRefService.findAllEmployeeRate(Sort.by(Sort.Direction.ASC, "code"));
        List<Map<String, Object>> result = entities.stream()
            .map(e -> employeeRefService.toEmployeeRateMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
