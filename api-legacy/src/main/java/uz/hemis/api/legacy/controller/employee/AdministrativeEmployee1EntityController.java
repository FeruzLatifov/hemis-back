package uz.hemis.api.legacy.controller.employee;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.api.legacy.util.CubaSearchBodyParser;
import uz.hemis.domain.entity.employee.AdministrativeEmployee1;
import uz.hemis.service.legacy.employee.EmployeeRefLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Administrative Employee1 Entity Controller (CUBA Pattern)
 * Topish-1000 universitetlaridan PhD/DSc darajali o'qituvchilar hisoboti
 *
 * @since 2.0.0
 */
@Tag(name = "38.Inspeksiya administrative teacher", description = "Topish-1000 universitetlaridan PhD/DSc darajali o'qituvchilar hisoboti")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeEmployee1EntityController {

    private final EmployeeRefLegacyService employeeService;
    private final CubaFilterHelper filterHelper;

    private static final String ENTITY_NAME = "hemishe_RIAdministrativeEmployee1";

    @PreAuthorize("hasAuthority('teachers.edit')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        log.info("POST create hemishe_RIAdministrativeEmployee1: {}", body);
        AdministrativeEmployee1 entity = new AdministrativeEmployee1();
        employeeService.updateAdministrativeEmployee1FromMap(entity, body);
        AdministrativeEmployee1 saved = employeeService.saveAdministrativeEmployee1(entity);
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee1Map(saved, returnNulls));
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/{entityId}")
    public ResponseEntity<?> getById(@PathVariable UUID entityId, @RequestParam(required = false) Boolean returnNulls) {
        log.debug("GET hemishe_RIAdministrativeEmployee1 by id: {}", entityId);
        Optional<AdministrativeEmployee1> entity = employeeService.findAdministrativeEmployee1ById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee1Map(entity.get(), returnNulls));
    }

    @PreAuthorize("hasAuthority('teachers.edit')")
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        log.info("PUT hemishe_RIAdministrativeEmployee1 id: {}", entityId);
        Optional<AdministrativeEmployee1> existingOpt = employeeService.findAdministrativeEmployee1ById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AdministrativeEmployee1 entity = existingOpt.get();
        employeeService.updateAdministrativeEmployee1FromMap(entity, body);
        AdministrativeEmployee1 saved = employeeService.saveAdministrativeEmployee1(entity);
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee1Map(saved, returnNulls));
    }

    @PreAuthorize("hasAuthority('teachers.delete')")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.info("DELETE hemishe_RIAdministrativeEmployee1 id: {}", entityId);
        Optional<AdministrativeEmployee1> entity = employeeService.findAdministrativeEmployee1ById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        employeeService.softDeleteAdministrativeEmployee1(entity.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("GET all hemishe_RIAdministrativeEmployee1 - offset: {}, limit: {}", offset, limit);
        List<AdministrativeEmployee1> allEntities = employeeService.findAllAdministrativeEmployee1();
        int start = Math.min(offset, allEntities.size());
        int end = Math.min(start + limit, allEntities.size());
        List<AdministrativeEmployee1> paged = allEntities.subList(start, end);
        List<Map<String, Object>> result = paged.stream().map(e -> employeeService.toAdministrativeEmployee1Map(e, returnNulls)).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter, @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit, @RequestParam(required = false) Boolean returnNulls) {
        log.debug("GET search hemishe_RIAdministrativeEmployee1 with filter: {}", filter);
        return search(filter, offset, limit, returnNulls);
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {
        int effectiveLimit = limit != null ? limit : 50;
        int effectiveOffset = offset != null ? offset : 0;
        String filterJson = null;
        if (body != null) {
            effectiveOffset = CubaSearchBodyParser.extractOffset(body, effectiveOffset);
            effectiveLimit = CubaSearchBodyParser.extractLimit(body, effectiveLimit);
            filterJson = CubaSearchBodyParser.extractFilter(body);
        }
        log.debug("POST search hemishe_RIAdministrativeEmployee1 - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);
        return search(filterJson, effectiveOffset, effectiveLimit, returnNulls);
    }

    private ResponseEntity<List<Map<String, Object>>> search(String filter, Integer offset, Integer limit, Boolean returnNulls) {
        List<AdministrativeEmployee1> allEntities = employeeService.findAllAdministrativeEmployee1();
        if (filter != null && !filter.isBlank()) {
            allEntities = filterHelper.applyFilter(allEntities, filter, req -> getFieldValue(req.entity(), req.property()));
        }
        int start = Math.min(offset, allEntities.size());
        int end = Math.min(start + limit, allEntities.size());
        List<AdministrativeEmployee1> paged = allEntities.subList(start, end);
        List<Map<String, Object>> result = paged.stream().map(e -> employeeService.toAdministrativeEmployee1Map(e, returnNulls)).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private Object getFieldValue(AdministrativeEmployee1 entity, String property) {
        return switch (property) {
            case "id" -> entity.getId() != null ? entity.getId().toString() : null;
            case "diplomaSerialNumber" -> entity.getDiplomaSerialNumber();
            case "diplomaType" -> entity.getDiplomaType();
            case "diplomaDate" -> entity.getDiplomaDate() != null ? entity.getDiplomaDate().toString() : null;
            case "specialityCode" -> entity.getSpecialityCode();
            case "specialityName" -> entity.getSpecialityName();
            case "councilDate" -> entity.getCouncilDate() != null ? entity.getCouncilDate().toString() : null;
            case "councilNumber" -> entity.getCouncilNumber();
            case "foreignUniversity" -> entity.getForeignUniversity();
            case "version" -> entity.getVersion();
            case "_university", "university" -> entity.getUniversity();
            case "_educationYear", "educationYear" -> entity.getEducationYear();
            case "_employee", "employee" -> entity.getEmployee() != null ? entity.getEmployee().toString() : null;
            case "_country", "country" -> entity.getCountry();
            case "_degree", "degree" -> entity.getDegree();
            case "_rank", "rank" -> entity.getRank();
            default -> null;
        };
    }
}
