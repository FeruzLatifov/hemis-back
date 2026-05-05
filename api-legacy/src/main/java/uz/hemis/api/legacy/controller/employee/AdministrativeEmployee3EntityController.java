package uz.hemis.api.legacy.controller.employee;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.api.legacy.util.CubaSearchBodyParser;
import uz.hemis.domain.entity.employee.AdministrativeEmployee3;
import uz.hemis.service.legacy.employee.EmployeeRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Administrative Employee3 Entity Controller (CUBA Pattern)
 * Xorijiy DSc/professor unvoniga ega o'qituvchilar hisoboti
 *
 * @since 2.0.0
 */
@Tag(name = "40.Inspeksiya administrative teacher3", description = "Xorijiy DSc/professor unvoniga ega o'qituvchilar hisoboti")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeEmployee3EntityController {

    private final EmployeeRefLegacyService employeeService;
    private final CubaFilterHelper filterHelper;

    private static final String ENTITY_NAME = "hemishe_RIAdministrativeEmployee3";

    @Operation(summary = "Yangi entity yaratish (CUBA entity API)")
    @PreAuthorize("hasAuthority('teachers.edit')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        log.info("POST create hemishe_RIAdministrativeEmployee3: {}", body);
        AdministrativeEmployee3 entity = new AdministrativeEmployee3();
        employeeService.updateAdministrativeEmployee3FromMap(entity, body);
        AdministrativeEmployee3 saved = employeeService.saveAdministrativeEmployee3(entity);
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee3Map(saved, returnNulls));
    }

    @Operation(summary = "ID bo'yicha topish (CUBA entity API)")
    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/{entityId}")
    public ResponseEntity<?> getById(@PathVariable UUID entityId, @RequestParam(required = false) Boolean returnNulls) {
        log.debug("GET hemishe_RIAdministrativeEmployee3 by id: {}", entityId);
        Optional<AdministrativeEmployee3> entity = employeeService.findAdministrativeEmployee3ById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee3Map(entity.get(), returnNulls));
    }

    @Operation(summary = "Yangilash (CUBA entity API)")
    @PreAuthorize("hasAuthority('teachers.edit')")
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        log.info("PUT hemishe_RIAdministrativeEmployee3 id: {}", entityId);
        Optional<AdministrativeEmployee3> existingOpt = employeeService.findAdministrativeEmployee3ById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        AdministrativeEmployee3 entity = existingOpt.get();
        employeeService.updateAdministrativeEmployee3FromMap(entity, body);
        AdministrativeEmployee3 saved = employeeService.saveAdministrativeEmployee3(entity);
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee3Map(saved, returnNulls));
    }

    @Operation(summary = "Soft delete (CUBA entity API)")
    @PreAuthorize("hasAuthority('teachers.delete')")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.info("DELETE hemishe_RIAdministrativeEmployee3 id: {}", entityId);
        Optional<AdministrativeEmployee3> entity = employeeService.findAdministrativeEmployee3ById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        employeeService.softDeleteAdministrativeEmployee3(entity.get());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Barcha entity'lar (CUBA pagination — offset/limit)")
    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("GET all hemishe_RIAdministrativeEmployee3 - offset: {}, limit: {}", offset, limit);
        List<AdministrativeEmployee3> allEntities = employeeService.findAllAdministrativeEmployee3();
        int start = Math.min(offset, allEntities.size());
        int end = Math.min(start + limit, allEntities.size());
        List<AdministrativeEmployee3> paged = allEntities.subList(start, end);
        return ResponseEntity.ok(paged.stream().map(e -> employeeService.toAdministrativeEmployee3Map(e, returnNulls)).collect(Collectors.toList()));
    }

    @Operation(summary = "GET search — CUBA filter qidirish")
    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter, @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit, @RequestParam(required = false) Boolean returnNulls) {
        return search(filter, offset, limit, returnNulls);
    }

    @Operation(summary = "POST search — CUBA filter JSON body bilan qidirish")
    @PreAuthorize("hasAuthority('teachers.view')")
    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body, @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Boolean returnNulls) {
        int effectiveLimit = limit != null ? limit : 50;
        int effectiveOffset = offset != null ? offset : 0;
        String filterJson = null;
        if (body != null) {
            effectiveOffset = CubaSearchBodyParser.extractOffset(body, effectiveOffset);
            effectiveLimit = CubaSearchBodyParser.extractLimit(body, effectiveLimit);
            filterJson = CubaSearchBodyParser.extractFilter(body);
        }
        return search(filterJson, effectiveOffset, effectiveLimit, returnNulls);
    }

    private ResponseEntity<List<Map<String, Object>>> search(String filter, Integer offset, Integer limit, Boolean returnNulls) {
        List<AdministrativeEmployee3> allEntities = employeeService.findAllAdministrativeEmployee3();
        if (filter != null && !filter.isBlank()) {
            allEntities = filterHelper.applyFilter(allEntities, filter, req -> getFieldValue(req.entity(), req.property()));
        }
        int start = Math.min(offset, allEntities.size());
        int end = Math.min(start + limit, allEntities.size());
        List<AdministrativeEmployee3> paged = allEntities.subList(start, end);
        return ResponseEntity.ok(paged.stream().map(e -> employeeService.toAdministrativeEmployee3Map(e, returnNulls)).collect(Collectors.toList()));
    }

    private Object getFieldValue(AdministrativeEmployee3 entity, String property) {
        return switch (property) {
            case "id" -> entity.getId() != null ? entity.getId().toString() : null;
            case "fullname" -> entity.getFullname();
            case "workPlace" -> entity.getWorkPlace();
            case "specialityName" -> entity.getSpecialityName();
            case "subject" -> entity.getSubject();
            case "contractData" -> entity.getContractData();
            case "arrivalDate" -> entity.getArrivalDate() != null ? entity.getArrivalDate().toString() : null;
            case "departureDate" -> entity.getDepartureDate() != null ? entity.getDepartureDate().toString() : null;
            case "lessonTime" -> entity.getLessonTime();
            case "year" -> entity.getYear();
            case "_university", "university" -> entity.getUniversity();
            case "_educationYear", "educationYear" -> entity.getEducationYear();
            case "_employee", "employee" -> entity.getEmployee() != null ? entity.getEmployee().toString() : null;
            case "_country", "country" -> entity.getCountry();
            case "_employeeForm", "employeeForm" -> entity.getEmployeeForm();
            case "_condutionForm", "condutionForm" -> entity.getCondutionForm();
            default -> null;
        };
    }
}
