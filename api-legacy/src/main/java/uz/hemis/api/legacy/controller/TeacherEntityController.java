package uz.hemis.api.legacy.controller;

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
import uz.hemis.domain.entity.Teacher;
import uz.hemis.domain.repository.TeacherRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher Entity Controller (CUBA Pattern)
 * Tag 05: O'qituvchilar (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_ETeacher
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_ETeacher
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_ETeacher/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_ETeacher/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_ETeacher/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_ETeacher/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_ETeacher/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_ETeacher           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_ETeacher           - Create new
 */
@Tag(name = "05.O'qituvchi")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_ETeacher")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class TeacherEntityController {

    private final TeacherRepository repository;
    private static final String ENTITY_NAME = "hemishe_ETeacher";

    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta o'qituvchi ma'lumotlarini olish",
        description = "ID bo'yicha bitta o'qituvchi ma'lumotlarini qaytaradi. view=_local - faqat asosiy fieldlar."
    )
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @Parameter(description = "Dinamik atributlarni qo'shish") @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("GET teacher by id: {}, view: {}", entityId, view);

        Optional<Teacher> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls, view));
    }

    @PutMapping("/{entityId}")
    @Operation(
        summary = "O'qituvchi ma'lumotlarini o'zgartirish",
        description = "Mavjud o'qituvchi ma'lumotlarini yangilaydi. Faqat yuborilgan fieldlar o'zgaradi (partial update)."
    )
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("PUT teacher id: {}, body keys: {}", entityId, body.keySet());

        Optional<Teacher> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Teacher entity = existingOpt.get();
        updateFromMap(entity, body);

        Teacher saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls, view));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete teacher", description = "Soft deletes a teacher")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE teacher id: {}", entityId);

        Optional<Teacher> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "O'qituvchilarni qidirish (GET)", description = "URL parametrlari orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("GET search teachers with filter: {}, view: {}", filter, view);

        List<Teacher> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "O'qituvchilarni qidirish (POST)", description = "JSON filter orqali qidirish")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("POST search teachers with filter: {}, view: {}", filter, view);

        List<Teacher> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Barcha o'qituvchilar ro'yxati", description = "Sahifalangan ro'yxat qaytaradi")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset (boshlanish nuqtasi)") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit (sahifa hajmi)") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Saralash (field-asc/desc)") @RequestParam(required = false) String sort,
            @Parameter(description = "Dinamik atributlar") @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Null qiymatlarni qaytarish") @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (_local, _minimal, default)") @RequestParam(required = false) String view) {

        log.debug("GET all teachers - offset: {}, limit: {}, view: {}", offset, limit, view);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<Teacher> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create teacher", description = "Creates a new teacher")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new teacher");

        Teacher entity = new Teacher();
        updateFromMap(entity, body);
        Teacher saved = repository.save(entity);
        
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    /**
     * Teacher entity ni CUBA Map formatiga o'tkazish
     * view=_local uchun: _entityName, _instanceName, id va barcha _local fieldlar
     * Field tartibi old-hemis bilan 100% mos bo'lishi kerak
     */
    private Map<String, Object> toMap(Teacher entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();

        // CUBA metadata
        map.put("_entityName", ENTITY_NAME);

        // Instance name: "LASTNAME FIRSTNAME FATHERNAME" formatida
        String instanceName = entity.getFullName() != null && !entity.getFullName().isEmpty() ?
            entity.getFullName() : "Teacher-" + entity.getId();
        map.put("_instanceName", instanceName);

        // Primary key
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // view=_local bo'lsa - faqat oddiy fieldlar (old-hemis tartibi bilan)
        if ("_local".equals(view)) {
            putIfNotNull(map, "pinfl", entity.getPinfl(), returnNulls);
            putIfNotNull(map, "birthday", entity.getBirthDate(), returnNulls);
            putIfNotNull(map, "firstname", entity.getFirstName(), returnNulls);  // getFirstName() - manual getter
            putIfNotNull(map, "code", entity.getCode(), returnNulls);
            putIfNotNull(map, "tag", entity.getTag(), returnNulls);
            putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
            putIfNotNull(map, "address", entity.getAddress(), returnNulls);
            putIfNotNull(map, "lastname", entity.getSecondName(), returnNulls);  // getSecondName() - manual getter
            putIfNotNull(map, "fathername", entity.getThirdName(), returnNulls);  // getThirdName() - manual getter
            putIfNotNull(map, "phone", entity.getPhone(), returnNulls);
            putIfNotNull(map, "employeeYear", entity.getEmployeeYear(), returnNulls);
        } else {
            // Default view - barcha fieldlar
            putIfNotNull(map, "pinfl", entity.getPinfl(), returnNulls);
            putIfNotNull(map, "birthday", entity.getBirthDate(), returnNulls);
            putIfNotNull(map, "firstname", entity.getFirstName(), returnNulls);
            putIfNotNull(map, "lastname", entity.getSecondName(), returnNulls);
            putIfNotNull(map, "fathername", entity.getThirdName(), returnNulls);
            putIfNotNull(map, "code", entity.getCode(), returnNulls);
            putIfNotNull(map, "tag", entity.getTag(), returnNulls);
            putIfNotNull(map, "serialNumber", entity.getSerialNumber(), returnNulls);
            putIfNotNull(map, "address", entity.getAddress(), returnNulls);
            putIfNotNull(map, "phone", entity.getPhone(), returnNulls);
            putIfNotNull(map, "employeeYear", entity.getEmployeeYear(), returnNulls);
            putIfNotNull(map, "citizenship", entity.getCitizenship(), returnNulls);
            putIfNotNull(map, "_gender", entity.getGender(), returnNulls);
            putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
            putIfNotNull(map, "_academic_degree", entity.getAcademicDegree(), returnNulls);
            putIfNotNull(map, "_academic_rank", entity.getAcademicRank(), returnNulls);
        }

        return map;
    }

    // Backward compatibility uchun overload
    private Map<String, Object> toMap(Teacher entity, Boolean returnNulls) {
        return toMap(entity, returnNulls, null);
    }

    /**
     * Map dan Teacher entity ga fieldlarni o'tkazish (partial update)
     * CUBA Platform bilan 100% mos format
     *
     * Qabul qilinadigan field nomlari:
     * - firstname, lastname, fathername - ismlar
     * - pinfl, serialNumber - identifikatorlar
     * - birthday - tug'ilgan sana (YYYY-MM-DD)
     * - phone, address - kontakt
     * - employeeYear, code, tag - qo'shimcha
     * - _citizenship, _gender, _university - reference kodlar
     * - _academic_degree, _academic_rank - ilmiy darajalar
     */
    private void updateFromMap(Teacher entity, Map<String, Object> map) {
        // Personal info
        if (map.containsKey("firstname")) {
            entity.setFirstname((String) map.get("firstname"));
        }
        if (map.containsKey("lastname")) {
            entity.setLastname((String) map.get("lastname"));
        }
        if (map.containsKey("fathername")) {
            entity.setFathername((String) map.get("fathername"));
        }

        // Identifiers
        if (map.containsKey("pinfl")) {
            entity.setPinfl((String) map.get("pinfl"));
        }
        if (map.containsKey("serialNumber")) {
            entity.setSerialNumber((String) map.get("serialNumber"));
        }

        // Birthday - YYYY-MM-DD formatda
        if (map.containsKey("birthday")) {
            Object birthdayObj = map.get("birthday");
            if (birthdayObj instanceof String) {
                entity.setBirthday(java.time.LocalDate.parse((String) birthdayObj));
            }
        }

        // Contact
        if (map.containsKey("phone")) {
            entity.setPhone((String) map.get("phone"));
        }
        if (map.containsKey("address")) {
            entity.setAddress((String) map.get("address"));
        }

        // Additional
        if (map.containsKey("employeeYear")) {
            entity.setEmployeeYear((String) map.get("employeeYear"));
        }
        if (map.containsKey("code")) {
            entity.setCode((String) map.get("code"));
        }
        if (map.containsKey("tag")) {
            entity.setTag((String) map.get("tag"));
        }

        // References (underscore prefix - CUBA convention)
        if (map.containsKey("_citizenship")) {
            entity.setCitizenship((String) map.get("_citizenship"));
        }
        if (map.containsKey("_gender")) {
            entity.setGender((String) map.get("_gender"));
        }
        if (map.containsKey("_university")) {
            entity.setUniversity((String) map.get("_university"));
        }
        if (map.containsKey("_academic_degree")) {
            entity.setAcademicDegree((String) map.get("_academic_degree"));
        }
        if (map.containsKey("_academic_rank")) {
            entity.setAcademicRank((String) map.get("_academic_rank"));
        }
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
