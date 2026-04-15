package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.AdministrativeStudent2;
import uz.hemis.service.legacy.student.StudentEntityLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * AdministrativeStudent2 Entity Controller (CUBA Pattern)
 * Entity: hemishe_RIAdministrativeStudent2
 */
@Tag(name = "41.Inspeksiya administrative student2 - Akademik almashinuv", description = "Xorij OTMlari bilan akademik almashinuv dasturlari")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeStudent2")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeStudent2EntityController {

    private final StudentEntityLegacyService studentService;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeStudent2";

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/{entityId}")
    @Operation(summary = "Yozuvni ID bo'yicha olish")
    public ResponseEntity<?> getById(
            @PathVariable("entityId") UUID entityId,
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("GET hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> entity = studentService.findAdministrativeStudent2ById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(studentService.toAdministrativeStudent2Map(entity.get(), returnNulls));
    }

    @PreAuthorize("hasAuthority('students.edit')")
    @PutMapping("/{entityId}")
    @Operation(summary = "Yozuvni yangilash")
    public ResponseEntity<?> update(
            @PathVariable("entityId") UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("UPDATE hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> existingOpt = studentService.findAdministrativeStudent2ById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        AdministrativeStudent2 entity = existingOpt.get();
        studentService.updateAdministrativeStudent2FromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        AdministrativeStudent2 saved = studentService.saveAdministrativeStudent2(entity);
        return ResponseEntity.ok(studentService.toAdministrativeStudent2Map(saved, returnNulls));
    }

    @PreAuthorize("hasAuthority('students.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "Yozuvni o'chirish")
    public ResponseEntity<?> delete(@PathVariable("entityId") UUID entityId) {

        log.info("DELETE hemishe_RIAdministrativeStudent2: {}", entityId);

        Optional<AdministrativeStudent2> existingOpt = studentService.findAdministrativeStudent2ById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        studentService.deleteAdministrativeStudent2(existingOpt.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/search")
    @Operation(summary = "Yozuvlarni qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "limit", defaultValue = "50") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_RIAdministrativeStudent2 (GET) - filter: {}", filter);

        Page<AdministrativeStudent2> page = studentService.findAllAdministrativeStudent2(
                PageRequest.of(offset / Math.max(limit, 1), limit, Sort.unsorted())
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (AdministrativeStudent2 entity : page.getContent()) {
            result.add(studentService.toAdministrativeStudent2Map(entity, returnNulls));
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('students.view')")
    @PostMapping("/search")
    @Operation(summary = "Yozuvlarni qidirish (POST)")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @RequestParam(value = "limit", required = false) Integer limitParam,
            @RequestParam(value = "offset", required = false) Integer offsetParam,
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_RIAdministrativeStudent2 (POST) - filter: {}", filterBody);

        int limit = 50;
        int offset = 0;

        if (filterBody != null) {
            if (filterBody.containsKey("limit")) {
                Object bodyLimit = filterBody.get("limit");
                if (bodyLimit instanceof Number) {
                    limit = ((Number) bodyLimit).intValue();
                }
            }
            if (filterBody.containsKey("offset")) {
                Object bodyOffset = filterBody.get("offset");
                if (bodyOffset instanceof Number) {
                    offset = ((Number) bodyOffset).intValue();
                }
            }
        }
        if (limitParam != null) limit = limitParam;
        if (offsetParam != null) offset = offsetParam;

        Page<AdministrativeStudent2> page = studentService.findAllAdministrativeStudent2(
                PageRequest.of(offset / Math.max(limit, 1), limit, Sort.unsorted())
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (AdministrativeStudent2 entity : page.getContent()) {
            result.add(studentService.toAdministrativeStudent2Map(entity, returnNulls));
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping
    @Operation(summary = "Barcha yozuvlarni olish")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(value = "returnCount", required = false) Boolean returnCount,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "50") int limit,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("LIST ALL hemishe_RIAdministrativeStudent2");

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        Page<AdministrativeStudent2> page = studentService.findAllAdministrativeStudent2(
                PageRequest.of(offset / Math.max(limit, 1), limit, sorting)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (AdministrativeStudent2 entity : page.getContent()) {
            result.add(studentService.toAdministrativeStudent2Map(entity, returnNulls));
        }

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    @PreAuthorize("hasAuthority('students.edit')")
    @PostMapping
    @Operation(summary = "Yangi yozuv yaratish")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> create(
            @RequestBody Object body,
            @RequestParam(value = "returnNulls", required = false) Boolean returnNulls) {

        log.info("CREATE hemishe_RIAdministrativeStudent2: {}", body);

        boolean isArrayRequest = body instanceof List;

        List<Map<String, Object>> items;
        if (body instanceof List) {
            items = (List<Map<String, Object>>) body;
        } else if (body instanceof Map) {
            items = List.of((Map<String, Object>) body);
        } else {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Invalid request body");
            error.put("details", "Body must be an array or object");
            return ResponseEntity.badRequest().body(error);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> item : items) {
            AdministrativeStudent2 entity = new AdministrativeStudent2();
            studentService.updateAdministrativeStudent2FromMap(entity, item);
            entity.setCreateTs(LocalDateTime.now());

            AdministrativeStudent2 saved = studentService.saveAdministrativeStudent2(entity);
            results.add(studentService.toAdministrativeStudent2Map(saved, returnNulls));
        }

        if (isArrayRequest) {
            return ResponseEntity.ok(results);
        }
        return ResponseEntity.ok(results.get(0));
    }
}
