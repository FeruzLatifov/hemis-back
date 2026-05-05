package uz.hemis.api.legacy.controller.academic;

import uz.hemis.api.legacy.adapter.LegacyResponseHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uz.hemis.domain.entity.academic.AcademicSubjects;
import uz.hemis.service.legacy.academic.AcademicEntityLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Fanlar Controller - CUBA REST API Pattern
 *
 * @since 2.0.0
 */
@Tag(name = "33.Akademik hisobotlar fanlar", description = "Fanlar hisobotlarini boshqarish API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RAcademicSubjects")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AcademicSubjectsEntityController {

    private final AcademicEntityLegacyService academicService;
    private final CubaFilterHelper filterHelper;

    private static final String ENTITY_NAME = "hemishe_RAcademicSubjects";

    @Operation(summary = "ID bo'yicha topish (CUBA entity API)")
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/{entityId}")
    public ResponseEntity<?> getById(@PathVariable String entityId, @RequestParam(required = false) Boolean returnNulls) {
        log.info("GET hemishe_RAcademicSubjects: {}", entityId);
        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicSubjects> entity = academicService.findAcademicSubjectsById(id);
            if (entity.isEmpty()) {
                return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
            }
            return ResponseEntity.ok(academicService.toAcademicSubjectsMap(entity.get(), returnNulls));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(LegacyResponseHelper.errorMap("Invalid UUID", "Invalid UUID format: " + entityId));
        }
    }

    @Operation(summary = "Yangilash (CUBA entity API)")
    @PreAuthorize("hasAuthority('students.edit')")
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(@PathVariable String entityId, @RequestBody Map<String, Object> entityData) {
        log.info("UPDATE hemishe_RAcademicSubjects: {}", entityId);
        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicSubjects> existingOpt = academicService.findAcademicSubjectsById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
            }
            AcademicSubjects entity = existingOpt.get();
            academicService.updateAcademicSubjectsFromMap(entity, entityData);
            entity.setUpdateTs(LocalDateTime.now());
            AcademicSubjects saved = academicService.saveAcademicSubjects(entity);
            return ResponseEntity.ok(academicService.toAcademicSubjectsMap(saved, false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(LegacyResponseHelper.errorMap("Invalid UUID", "Invalid UUID format: " + entityId));
        }
    }

    @Operation(summary = "Soft delete (CUBA entity API)")
    @PreAuthorize("hasAuthority('students.delete')")
    @DeleteMapping("/{entityId}")
    public ResponseEntity<?> delete(@PathVariable String entityId) {
        log.info("DELETE hemishe_RAcademicSubjects: {}", entityId);
        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicSubjects> entity = academicService.findAcademicSubjectsById(id);
            if (entity.isEmpty()) {
                return ResponseEntity.status(404).body(LegacyResponseHelper.errorMap("Entity not found", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
            }
            academicService.deleteAcademicSubjects(entity.get());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(LegacyResponseHelper.errorMap("Invalid UUID", "Invalid UUID format: " + entityId));
        }
    }

    @Operation(summary = "GET search — CUBA filter qidirish")
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam String filter, @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {
        log.info("SEARCH hemishe_RAcademicSubjects (GET) - filter: {}", filter);
        return search(filter, offset, limit, returnCount, returnNulls);
    }

    @Operation(summary = "POST search — CUBA filter JSON body bilan qidirish")
    @PreAuthorize("hasAuthority('students.view')")
    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @RequestParam(required = false) Boolean returnCount, @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Boolean returnNulls) {
        log.info("SEARCH hemishe_RAcademicSubjects (POST) - filter: {}", filterBody);
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
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping({"", "/"})
    public ResponseEntity<List<Map<String, Object>>> listAll(
            @RequestParam(required = false) Boolean returnCount, @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Boolean returnNulls) {
        log.info("LIST ALL hemishe_RAcademicSubjects");
        List<AcademicSubjects> allEntities = academicService.findAllAcademicSubjects();
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();
        List<AcademicSubjects> paged = allEntities.subList(Math.min(start, allEntities.size()), Math.min(end, allEntities.size()));
        List<Map<String, Object>> result = paged.stream().map(e -> academicService.toAcademicSubjectsMap(e, returnNulls)).collect(Collectors.toList());
        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @Operation(summary = "Yangi entity yaratish (CUBA entity API)")
    @PreAuthorize("hasAuthority('students.edit')")
    @PostMapping({"", "/"})
    public ResponseEntity<?> create(@RequestBody Map<String, Object> entityData) {
        log.info("CREATE hemishe_RAcademicSubjects: {}", entityData);
        try {
            AcademicSubjects entity = new AcademicSubjects();
            if (entityData.containsKey("id") && entityData.get("id") != null) {
                entity.setId(UUID.fromString(entityData.get("id").toString()));
            }
            academicService.updateAcademicSubjectsFromMap(entity, entityData);
            AcademicSubjects saved = academicService.saveAcademicSubjects(entity);
            return ResponseEntity.ok(academicService.toAcademicSubjectsMap(saved, false));
        } catch (Exception e) {
            log.error("CREATE xatosi: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(LegacyResponseHelper.errorMap("Server error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private ResponseEntity<List<Map<String, Object>>> search(String filter, Integer offset, Integer limit, Boolean returnCount, Boolean returnNulls) {
        List<AcademicSubjects> allEntities = academicService.findAllAcademicSubjects();
        if (filter != null && !filter.isEmpty()) {
            allEntities = filterHelper.applyFilter(allEntities, filter, req -> getFieldValue(req.entity(), req.property()));
        }
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();
        List<AcademicSubjects> paged = allEntities.subList(Math.min(start, allEntities.size()), Math.min(end, allEntities.size()));
        List<Map<String, Object>> result = paged.stream().map(e -> academicService.toAcademicSubjectsMap(e, returnNulls)).collect(Collectors.toList());
        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    private Object getFieldValue(AcademicSubjects entity, String property) {
        if (property == null) return null;
        return switch (property) {
            case "universityCode" -> entity.getUniversityCode();
            case "universityName" -> entity.getUniversityName();
            case "educationTypeCode" -> entity.getEducationTypeCode();
            case "educationTypeName" -> entity.getEducationTypeName();
            case "educationYearCode" -> entity.getEducationYearCode();
            case "educationYearName" -> entity.getEducationYearName();
            case "curriculumCode" -> entity.getCurriculumCode();
            case "curriculumName" -> entity.getCurriculumName();
            case "blockCode" -> entity.getBlockCode();
            case "blockName" -> entity.getBlockName();
            case "subjectCount" -> entity.getSubjectCount();
            default -> null;
        };
    }
}
