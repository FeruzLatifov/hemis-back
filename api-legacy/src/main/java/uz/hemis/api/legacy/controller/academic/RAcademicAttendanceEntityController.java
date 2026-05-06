package uz.hemis.api.legacy.controller.academic;

import uz.hemis.api.legacy.adapter.LegacyResponseHelper;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.api.legacy.util.CubaSearchBodyParser;
import uz.hemis.domain.entity.research.RAcademicAttendance;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Akademik Davomat Hisobotlari Controller - CUBA REST API Pattern
 *
 * @since 2.0.0
 */
@Tag(name = "35.Akademik hisobotlar davomat", description = "Akademik davomat hisobotlarini boshqarish API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RAcademicAttendance")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class RAcademicAttendanceEntityController {

    private final AcademicEntityLegacyService academicService;
    private final CubaFilterHelper filterHelper;

    private static final String ENTITY_NAME = "hemishe_RAcademicAttendance";

    @Operation(summary = "ID bo'yicha topish (CUBA entity API)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{entityId}")
    public ResponseEntity<?> getById(@PathVariable String entityId, @RequestParam(required = false) Boolean returnNulls) {
        log.info("GET hemishe_RAcademicAttendance: {}", entityId);
        try {
            UUID id = UUID.fromString(entityId);
            Optional<RAcademicAttendance> entity = academicService.findRAcademicAttendanceById(id);
            if (entity.isEmpty()) {
                return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
            }
            return ResponseEntity.ok(academicService.toRAcademicAttendanceMap(entity.get(), returnNulls));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(LegacyResponseHelper.errorMap("Invalid UUID", "Invalid UUID format: " + entityId));
        }
    }

    @Operation(summary = "Yangilash (CUBA entity API)")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(@PathVariable String entityId, @RequestBody Map<String, Object> entityData) {
        log.info("UPDATE hemishe_RAcademicAttendance: {}", entityId);
        try {
            UUID id = UUID.fromString(entityId);
            Optional<RAcademicAttendance> existingOpt = academicService.findRAcademicAttendanceById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
            }
            RAcademicAttendance entity = existingOpt.get();
            academicService.updateRAcademicAttendanceFromMap(entity, entityData);
            entity.setUpdateTs(LocalDateTime.now());
            RAcademicAttendance saved = academicService.saveRAcademicAttendance(entity);
            return ResponseEntity.ok(academicService.toRAcademicAttendanceMap(saved, false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(LegacyResponseHelper.errorMap("Invalid UUID", "Invalid UUID format: " + entityId));
        }
    }

    @Operation(summary = "Soft delete (CUBA entity API)")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<?> delete(@PathVariable String entityId) {
        log.info("DELETE hemishe_RAcademicAttendance: {}", entityId);
        try {
            UUID id = UUID.fromString(entityId);
            Optional<RAcademicAttendance> entity = academicService.findRAcademicAttendanceById(id);
            if (entity.isEmpty()) {
                return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
            }
            academicService.deleteRAcademicAttendance(entity.get());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(LegacyResponseHelper.errorMap("Invalid UUID", "Invalid UUID format: " + entityId));
        }
    }

    @Operation(summary = "GET search — CUBA filter qidirish")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam String filter, @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {
        log.info("SEARCH hemishe_RAcademicAttendance (GET) - filter: {}", filter);
        return search(filter, offset, limit, returnCount, returnNulls);
    }

    @Operation(summary = "POST search — CUBA filter JSON body bilan qidirish")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @RequestParam(required = false) Boolean returnCount, @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Boolean returnNulls) {
        log.info("SEARCH hemishe_RAcademicAttendance (POST) - filter: {}", filterBody);
        String filterStr = null;
        Integer bodyOffset = offset, bodyLimit = limit;
        if (filterBody != null) {
            filterStr = CubaSearchBodyParser.extractFilter(filterBody);
            bodyOffset = CubaSearchBodyParser.extractOffset(filterBody, bodyOffset);
            bodyLimit = CubaSearchBodyParser.extractLimit(filterBody, bodyLimit);
        }
        return search(filterStr, bodyOffset, bodyLimit, returnCount, returnNulls);
    }

    @Operation(summary = "Endpoint")
    @PreAuthorize("isAuthenticated()")
    @GetMapping({"", "/"})
    public ResponseEntity<List<Map<String, Object>>> listAll(
            @RequestParam(required = false) Boolean returnCount, @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Boolean returnNulls) {
        log.info("LIST ALL hemishe_RAcademicAttendance");
        List<RAcademicAttendance> allEntities = academicService.findAllRAcademicAttendance();
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();
        List<RAcademicAttendance> paged = allEntities.subList(Math.min(start, allEntities.size()), Math.min(end, allEntities.size()));
        List<Map<String, Object>> result = paged.stream().map(e -> academicService.toRAcademicAttendanceMap(e, returnNulls)).collect(Collectors.toList());
        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @Operation(summary = "Yangi entity yaratish (CUBA entity API)")
    @PreAuthorize("isAuthenticated()")
    @PostMapping({"", "/"})
    public ResponseEntity<?> create(@RequestBody Map<String, Object> entityData) {
        log.info("CREATE hemishe_RAcademicAttendance: {}", entityData);
        try {
            RAcademicAttendance entity = new RAcademicAttendance();
            if (entityData.containsKey("id") && entityData.get("id") != null) {
                entity.setId(UUID.fromString(entityData.get("id").toString()));
            }
            academicService.updateRAcademicAttendanceFromMap(entity, entityData);
            RAcademicAttendance saved = academicService.saveRAcademicAttendance(entity);
            return ResponseEntity.ok(academicService.toRAcademicAttendanceMap(saved, false));
        } catch (Exception e) {
            log.error("CREATE xatosi: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(LegacyResponseHelper.errorMap("Server error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private ResponseEntity<List<Map<String, Object>>> search(String filter, Integer offset, Integer limit, Boolean returnCount, Boolean returnNulls) {
        List<RAcademicAttendance> allEntities = academicService.findAllRAcademicAttendance();
        if (filter != null && !filter.isEmpty()) {
            allEntities = filterHelper.applyFilter(allEntities, filter, req -> getFieldValue(req.entity(), req.property()));
        }
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();
        List<RAcademicAttendance> paged = allEntities.subList(Math.min(start, allEntities.size()), Math.min(end, allEntities.size()));
        List<Map<String, Object>> result = paged.stream().map(e -> academicService.toRAcademicAttendanceMap(e, returnNulls)).collect(Collectors.toList());
        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    private Object getFieldValue(RAcademicAttendance entity, String property) {
        if (property == null) return null;
        return switch (property) {
            case "universityCode" -> entity.getUniversityCode();
            case "universityName" -> entity.getUniversityName();
            case "facultyCode" -> entity.getFacultyCode();
            case "facultyName" -> entity.getFacultyName();
            case "educationTypeCode" -> entity.getEducationTypeCode();
            case "educationTypeName" -> entity.getEducationTypeName();
            case "educationYearCode" -> entity.getEducationYearCode();
            case "educationYearName" -> entity.getEducationYearName();
            case "semesterTypeCode" -> entity.getSemesterTypeCode();
            case "semesterTypeName" -> entity.getSemesterTypeName();
            case "courseCode" -> entity.getCourseCode();
            case "courseName" -> entity.getCourseName();
            case "attendancePercent" -> entity.getAttendancePercent();
            case "badAttendanceStudentCount" -> entity.getBadAttendanceStudentCount();
            default -> null;
        };
    }
}
