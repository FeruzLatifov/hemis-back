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
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.domain.entity.employee.AdministrativeEmployee2;
import uz.hemis.service.legacy.employee.EmployeeRefLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Administrative Employee2 Entity Controller (CUBA Pattern)
 * Topish-1000 universitetlarida malaka oshirgan o'qituvchilar hisoboti
 *
 * @since 2.0.0
 */
@Tag(name = "39.Inspeksiya administrative teacher2", description = "Topish-1000 universitetlarida malaka oshirgan o'qituvchilar hisoboti")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeEmployee2EntityController {

    private final EmployeeRefLegacyService employeeService;
    private final CubaFilterHelper filterHelper;
    private final LegacySecurityHelper securityHelper;

    private static final String ENTITY_NAME = "hemishe_RIAdministrativeEmployee2";

    /** OWASP A01 BOLA defense — caller must own the entity's university. */
    private boolean isAccessAllowed(AdministrativeEmployee2 entity) {
        String callerCode = securityHelper.getUniversityCodeFromContext();
        if (callerCode == null || callerCode.isEmpty()) {
            return true; // admin/system scope
        }
        return callerCode.equals(entity.getUniversity());
    }

    private Map<String, Object> forbiddenBody() {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", "Forbidden");
        err.put("details", "Resource belongs to another university");
        return err;
    }

    @Operation(summary = "Yangi entity yaratish (CUBA entity API)")
    @PreAuthorize("hasAuthority('teachers.edit')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        log.info("POST create hemishe_RIAdministrativeEmployee2");
        // Mass-assignment defense — body cannot relocate to foreign OTM via _university.
        String callerCode = securityHelper.getUniversityCodeFromContext();
        if (callerCode != null && !callerCode.isEmpty()) {
            body.remove("_university");
            body.remove("university");
        }
        AdministrativeEmployee2 entity = new AdministrativeEmployee2();
        employeeService.updateAdministrativeEmployee2FromMap(entity, body);
        if (callerCode != null && !callerCode.isEmpty()) {
            entity.setUniversity(callerCode);
        }
        AdministrativeEmployee2 saved = employeeService.saveAdministrativeEmployee2(entity);
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee2Map(saved, returnNulls));
    }

    @Operation(summary = "ID bo'yicha topish (CUBA entity API)")
    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/{entityId}")
    public ResponseEntity<?> getById(@PathVariable UUID entityId,
                                      @RequestParam(required = false) Boolean returnNulls,
                                      @RequestParam(required = false) String view,
                                      @RequestParam(required = false) Boolean dynamicAttributes) {
        log.debug("GET hemishe_RIAdministrativeEmployee2 by id: {}, view: {}", entityId, view);
        Optional<AdministrativeEmployee2> entity = employeeService.findAdministrativeEmployee2ById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        if (!isAccessAllowed(entity.get())) {
            return ResponseEntity.status(403).body(forbiddenBody());
        }
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee2Map(entity.get(), returnNulls, view));
    }

    @Operation(summary = "Yangilash (CUBA entity API)")
    @PreAuthorize("hasAuthority('teachers.edit')")
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        log.info("PUT hemishe_RIAdministrativeEmployee2 id: {}", entityId);
        Optional<AdministrativeEmployee2> existingOpt = employeeService.findAdministrativeEmployee2ById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        AdministrativeEmployee2 entity = existingOpt.get();
        if (!isAccessAllowed(entity)) {
            return ResponseEntity.status(403).body(forbiddenBody());
        }
        // Mass-assignment defense — body cannot relocate entity to foreign OTM.
        body.remove("_university");
        body.remove("university");
        employeeService.updateAdministrativeEmployee2FromMap(entity, body);
        AdministrativeEmployee2 saved = employeeService.saveAdministrativeEmployee2(entity);
        return ResponseEntity.ok(employeeService.toAdministrativeEmployee2Map(saved, returnNulls));
    }

    @Operation(summary = "Soft delete (CUBA entity API)")
    @PreAuthorize("hasAuthority('teachers.delete')")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE hemishe_RIAdministrativeEmployee2 id: {}", entityId);
        Optional<AdministrativeEmployee2> entity = employeeService.findAdministrativeEmployee2ById(entityId);
        if (entity.isEmpty()) return ResponseEntity.notFound().build();
        if (!isAccessAllowed(entity.get())) {
            return ResponseEntity.status(403).body(forbiddenBody());
        }
        employeeService.softDeleteAdministrativeEmployee2(entity.get());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Barcha entity'lar (CUBA pagination — offset/limit)")
    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {
        log.debug("GET all hemishe_RIAdministrativeEmployee2 - offset: {}, limit: {}", offset, limit);
        List<AdministrativeEmployee2> allEntities = employeeService.findAllAdministrativeEmployee2();
        int start = Math.min(offset, allEntities.size());
        int end = Math.min(start + limit, allEntities.size());
        List<AdministrativeEmployee2> paged = allEntities.subList(start, end);
        return ResponseEntity.ok(paged.stream().map(e -> employeeService.toAdministrativeEmployee2Map(e, returnNulls)).collect(Collectors.toList()));
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
        List<AdministrativeEmployee2> allEntities = employeeService.findAllAdministrativeEmployee2();
        if (filter != null && !filter.isBlank()) {
            allEntities = filterHelper.applyFilter(allEntities, filter, req -> getFieldValue(req.entity(), req.property()));
        }
        int start = Math.min(offset, allEntities.size());
        int end = Math.min(start + limit, allEntities.size());
        List<AdministrativeEmployee2> paged = allEntities.subList(start, end);
        return ResponseEntity.ok(paged.stream().map(e -> employeeService.toAdministrativeEmployee2Map(e, returnNulls)).collect(Collectors.toList()));
    }

    private Object getFieldValue(AdministrativeEmployee2 entity, String property) {
        return switch (property) {
            case "id" -> entity.getId() != null ? entity.getId().toString() : null;
            case "foreignUniversity" -> entity.getForeignUniversity();
            case "specialityCode" -> entity.getSpecialityCode();
            case "specialityName" -> entity.getSpecialityName();
            case "trainingTypeName" -> entity.getTrainingTypeName();
            case "trainingContract" -> entity.getTrainingContract();
            case "trainingDateStart" -> entity.getTrainingDateStart() != null ? entity.getTrainingDateStart().toString() : null;
            case "trainingDateEnd" -> entity.getTrainingDateEnd() != null ? entity.getTrainingDateEnd().toString() : null;
            case "year" -> entity.getYear();
            case "subject" -> entity.getSubject();
            case "_university", "university" -> entity.getUniversity();
            case "_educationYear", "educationYear" -> entity.getEducationYear();
            case "_employee", "employee" -> entity.getEmployee();
            case "_country", "country" -> entity.getCountry();
            case "_internshipForm", "internshipForm" -> entity.getInternshipForm();
            case "_internshipType", "internshipType" -> entity.getInternshipType();
            default -> null;
        };
    }
}
