package uz.hemis.api.legacy.controller.document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
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
import uz.hemis.api.legacy.util.CubaSearchBodyParser;
import uz.hemis.domain.entity.student.StudentDiploma;
import uz.hemis.service.legacy.DiplomaLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Student Diploma Entity Controller
 * Tag 12: Diplomlar
 *
 * CUBA Platform REST API compatible controller for student diplomas
 * Entity: hemishe_EStudentDiploma
 *
 * Endpoints (7 ta):
 * 1. GET    /app/rest/v2/entities/hemishe_EStudentDiploma           - Barcha diplomlar
 * 2. GET    /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - Bitta diploma
 * 3. PUT    /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - Yangilash
 * 4. POST   /app/rest/v2/entities/hemishe_EStudentDiploma           - Yaratish
 * 5. DELETE /app/rest/v2/entities/hemishe_EStudentDiploma/{id}      - O'chirish
 * 6. GET    /app/rest/v2/entities/hemishe_EStudentDiploma/search    - Qidirish (GET)
 * 7. POST   /app/rest/v2/entities/hemishe_EStudentDiploma/search    - Qidirish (POST)
 */
@Tag(name = "12.Diplomlar", description = "Talaba diplomlari (hemishe_EStudentDiploma) - CUBA compatible CRUD")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentDiploma")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class StudentDiplomaEntityController {

    private final ObjectMapper objectMapper;
    private final DiplomaLegacyService diplomaService;

    // =============================================
    // 1. GET ALL - Barcha diplomlar
    // =============================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Barcha diplomlarni olish", description = "Diplomlar ro'yxatini pagination bilan qaytaradi. CUBA filter qo'llab-quvvatlanadi.")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sort (e.g. 'createTs-desc')") @RequestParam(required = false) String sort,
            @Parameter(description = "CUBA filter JSON") @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all diplomas - limit: {}, offset: {}, filter: {}", limit, offset, filter);

        // Filter mavjud bo'lsa - bazadan to'g'ridan-to'g'ri filter qilamiz
        if (filter != null && !filter.isEmpty()) {
            try {
                Map<String, Object> filterMap = objectMapper.readValue(filter, new TypeReference<>() {});

                if (filterMap.containsKey("conditions")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> conditions = (List<Map<String, Object>>) filterMap.get("conditions");

                    List<StudentDiploma> filtered = diplomaService.applyDatabaseFiltering(conditions, limit, offset);
                    if (filtered != null) {
                        List<Map<String, Object>> result = filtered.stream()
                                .map(e -> diplomaService.toDiplomaMap(e, returnNulls, view))
                                .collect(Collectors.toList());
                        return ResponseEntity.ok(result);
                    }
                }
            } catch (Exception e) {
                log.warn("Filter parse error, using pagination: {}", e.getMessage());
            }
        }

        // Filter yo'q yoki qo'llab-quvvatlanmaydigan filter - standart pagination
        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest;
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            pageRequest = PageRequest.of(page, limit, Sort.by(direction, field));
        } else {
            pageRequest = PageRequest.of(page, limit);
        }
        Page<StudentDiploma> resultPage = diplomaService.findAll(pageRequest);

        List<Map<String, Object>> result = resultPage.getContent().stream()
                .map(e -> diplomaService.toDiplomaMap(e, returnNulls, view))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =============================================
    // 2. GET BY ID - Bitta diploma
    // =============================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{entityId}")
    @Operation(summary = "Bitta diplomni olish", description = "UUID bo'yicha diplomni qaytaradi")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET diploma by id: {}", entityId);

        Optional<StudentDiploma> entity = diplomaService.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(diplomaService.toDiplomaMap(entity.get(), returnNulls, view));
    }

    // =============================================
    // 3. PUT - Yangilash
    // =============================================
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{entityId}")
    @Operation(summary = "Diplomni yangilash", description = "Mavjud diplomni yangilaydi")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT diploma id: {}", entityId);

        Optional<StudentDiploma> existingOpt = diplomaService.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StudentDiploma entity = existingOpt.get();
        diplomaService.updateDiplomaFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        StudentDiploma saved = diplomaService.save(entity);

        return ResponseEntity.ok(diplomaService.minimalDiplomaResponse(saved));
    }

    // =============================================
    // 4. POST - Yaratish
    // =============================================
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(summary = "Yangi diploma yaratish", description = "Yangi diploma yaratadi")
    public ResponseEntity<?> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create diploma");

        StudentDiploma entity = new StudentDiploma();

        // ID ni body dan olish yoki yangi yaratish
        if (body.containsKey("id")) {
            entity.setId(UUID.fromString((String) body.get("id")));
        } else {
            entity.setId(UUID.randomUUID());
        }

        diplomaService.updateDiplomaFromMap(entity, body);
        entity.setCreateTs(LocalDateTime.now());

        StudentDiploma saved = diplomaService.save(entity);

        return ResponseEntity.ok(diplomaService.minimalDiplomaResponse(saved));
    }

    // =============================================
    // 5. DELETE - O'chirish (soft delete)
    // =============================================
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "Diplomni o'chirish", description = "Diplomni soft delete qiladi")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE diploma id: {}", entityId);

        Optional<StudentDiploma> entity = diplomaService.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        diplomaService.softDelete(entity.get());

        // Old-hemis bilan bir xil: 200 OK (204 emas)
        return ResponseEntity.ok().build();
    }

    // =============================================
    // 6. GET /search - Qidirish (URL params)
    // Old Hemis bilan bir xil: CUBA JSON filter qabul qiladi
    // =============================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    @Operation(summary = "Diplomlarni qidirish (GET)", description = "CUBA filter formatida diplomlarni qidiradi")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        log.debug("GET search diplomas with filter: {}, limit: {}, offset: {}", filter, limit, offset);

        List<StudentDiploma> result;

        if (filter != null && !filter.isEmpty()) {
            try {
                Map<String, Object> filterMap = objectMapper.readValue(filter, new TypeReference<>() {});

                if (filterMap.containsKey("conditions")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> conditions = (List<Map<String, Object>>) filterMap.get("conditions");

                    // Database filtering
                    result = diplomaService.applyDatabaseFiltering(conditions, limit, offset);
                    if (result != null) {
                        return ResponseEntity.ok(
                                result.stream()
                                        .map(e -> diplomaService.toDiplomaMap(e, returnNulls, view))
                                        .collect(Collectors.toList())
                        );
                    }

                    // Fallback: Memory filtering
                    int page = offset / Math.max(limit, 1);
                    PageRequest pageRequest = PageRequest.of(page, limit * 10);
                    Page<StudentDiploma> resultPage = diplomaService.findAll(pageRequest);
                    result = diplomaService.applyConditions(resultPage.getContent(), conditions);
                    result = diplomaService.applyPagination(result, limit, offset);
                } else {
                    // conditions yo'q - oddiy text search
                    result = diplomaService.findByDiplomaNumberContaining(filter);
                    result = diplomaService.applyPagination(result, limit, offset);
                }
            } catch (Exception e) {
                // JSON parse xatosi - oddiy text search
                log.debug("Filter is not JSON, using text search: {}", filter);
                result = diplomaService.findByDiplomaNumberContaining(filter);
                result = diplomaService.applyPagination(result, limit, offset);
            }
        } else {
            int page = offset / Math.max(limit, 1);
            PageRequest pageRequest = PageRequest.of(page, limit);
            Page<StudentDiploma> resultPage = diplomaService.findAll(pageRequest);
            result = resultPage.getContent();
        }

        return ResponseEntity.ok(
                result.stream()
                        .map(e -> diplomaService.toDiplomaMap(e, returnNulls, view))
                        .collect(Collectors.toList())
        );
    }

    // =============================================
    // 7. POST /search - Qidirish (JSON filter)
    // =============================================
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/search")
    @Operation(summary = "Diplomlarni qidirish (POST)", description = "CUBA filter formatida diplomlarni qidiradi")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {

        int effectiveLimit = CubaSearchBodyParser.extractInt(requestBody, "limit", limit, 50);
        int effectiveOffset = CubaSearchBodyParser.extractInt(requestBody, "offset", offset, 0);

        log.info("POST search diplomas with effectiveLimit: {}, effectiveOffset: {}", effectiveLimit, effectiveOffset);

        List<StudentDiploma> result;

        // OLD-HEMIS CUBA format: {"filter": {"conditions": [...]}}
        if (requestBody != null && requestBody.containsKey("filter")) {
            Object filterObj = requestBody.get("filter");
            @SuppressWarnings("unchecked")
            Map<String, Object> filterMap = (Map<String, Object>) filterObj;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> conditions = (List<Map<String, Object>>) filterMap.get("conditions");

            // Database-level filtering
            result = diplomaService.applyDatabaseFiltering(conditions, effectiveLimit, effectiveOffset);
            if (result != null) {
                return ResponseEntity.ok(
                        result.stream()
                                .map(e -> diplomaService.toDiplomaMap(e, returnNulls, view))
                                .collect(Collectors.toList())
                );
            }

            // Fallback: Memory filtering
            int page = effectiveOffset / Math.max(effectiveLimit, 1);
            PageRequest pageRequest = PageRequest.of(page, effectiveLimit * 10);
            Page<StudentDiploma> resultPage = diplomaService.findAll(pageRequest);
            result = diplomaService.applyConditions(resultPage.getContent(), conditions);
            result = diplomaService.applyPagination(result, effectiveLimit, effectiveOffset);
        } else {
            int page = effectiveOffset / Math.max(effectiveLimit, 1);
            PageRequest pageRequest = PageRequest.of(page, effectiveLimit);
            Page<StudentDiploma> resultPage = diplomaService.findAll(pageRequest);
            result = resultPage.getContent();
        }

        return ResponseEntity.ok(
                result.stream()
                        .map(e -> diplomaService.toDiplomaMap(e, returnNulls, view))
                        .collect(Collectors.toList())
        );
    }
}
