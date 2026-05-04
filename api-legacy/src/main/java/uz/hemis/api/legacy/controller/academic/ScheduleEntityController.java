package uz.hemis.api.legacy.controller.academic;

import uz.hemis.api.legacy.adapter.LegacyResponseHelper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.domain.entity.student.Schedule;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "12.Dars jadvali", description = "Dars jadvali entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_ESchedule")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ScheduleEntityController {

    private final AcademicEntityLegacyService academicService;
    private final CubaFilterHelper filterHelper;

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Schedule> entity = academicService.findScheduleById(entityId);
        if (entity.isEmpty()) return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity hemishe_ESchedule with id " + entityId + " not found"));
        return ResponseEntity.ok(academicService.toScheduleMap(entity.get(), returnNulls));
    }

    @PreAuthorize("hasAuthority('students.edit')")
    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Optional<Schedule> existingOpt = academicService.findScheduleById(entityId);
        if (existingOpt.isEmpty()) return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity hemishe_ESchedule with id " + entityId + " not found"));
        Schedule saved = academicService.saveSchedule(existingOpt.get());
        return ResponseEntity.ok(academicService.toScheduleMap(saved, returnNulls));
    }

    @PreAuthorize("hasAuthority('students.delete')")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        Optional<Schedule> entity = academicService.findScheduleById(entityId);
        if (entity.isEmpty()) return ResponseEntity.status(404).build();
        academicService.deleteSchedule(entity.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Schedule> allEntities = academicService.findAllSchedule();
        List<Schedule> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> academicService.toScheduleMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('students.view')")
    @PostMapping("/search")
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

        List<Schedule> allEntities = academicService.findAllSchedule();
        List<Schedule> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> academicService.toScheduleMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "50") Integer limit, @RequestParam(required = false) String sort, @RequestParam(required = false) Boolean returnNulls) {
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            sorting = Sort.by(parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
        }
        Page<Schedule> entityPage = academicService.findAllSchedule(PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1), sorting));
        return ResponseEntity.ok(entityPage.getContent().stream().map(e -> academicService.toScheduleMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('students.edit')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, @RequestParam(required = false) Boolean returnNulls) {
        Schedule entity = new Schedule();
        Schedule saved = academicService.saveSchedule(entity);
        return ResponseEntity.ok(academicService.toScheduleMap(saved, returnNulls));
    }
}
