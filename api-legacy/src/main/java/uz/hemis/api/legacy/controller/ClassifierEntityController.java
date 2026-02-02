package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.EducationType;
import uz.hemis.domain.entity.EducationForm;
import uz.hemis.domain.entity.HCourse;
import uz.hemis.domain.entity.EducationYear;
import uz.hemis.domain.entity.TransferType;
import uz.hemis.domain.repository.EducationTypeRepository;
import uz.hemis.domain.repository.EducationFormRepository;
import uz.hemis.domain.repository.HCourseRepository;
import uz.hemis.domain.repository.EducationYearRepository;
import uz.hemis.domain.repository.TransferTypeRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Classifier Entity Controllers (CUBA Pattern)
 *
 * Provides read-only access to classifier/reference tables:
 * - hemishe_HEducationType → hemishe_h_education_type
 * - hemishe_HEducationForm → hemishe_h_education_form
 * - hemishe_HCourse → hemishe_h_course
 * - hemishe_HEducationYear → hemishe_h_education_year
 *
 * All classifiers have String PK (code), not UUID.
 */
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ClassifierEntityController {

    private static final DateTimeFormatter CUBA_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // =====================================================
    // hemishe_HEducationType
    // =====================================================
    @Tag(name = "90.Klassifikatorlar", description = "Klassifikator ma'lumotlari")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HEducationType")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class EducationTypeEntityController {

        private final EducationTypeRepository repository;
        private static final String ENTITY_NAME = "hemishe_HEducationType";

        @GetMapping
        @Operation(summary = "Ta'lim turlari ro'yxati")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            List<EducationType> all = repository.findAll();
            int from = Math.min(offset, all.size());
            int to = Math.min(from + limit, all.size());
            List<Map<String, Object>> result = new ArrayList<>();
            for (EducationType e : all.subList(from, to)) {
                result.add(toMap(e, returnNulls));
            }
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "Ta'lim turi bo'yicha olish")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return repository.findById(code)
                    .map(e -> ResponseEntity.ok(toMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }

        private Map<String, Object> toMap(EducationType e, Boolean returnNulls) {
            return classifierToMap(ENTITY_NAME, e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                    e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                    e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
        }
    }

    // =====================================================
    // hemishe_HEducationForm
    // =====================================================
    @Tag(name = "90.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HEducationForm")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class EducationFormEntityController {

        private final EducationFormRepository repository;
        private static final String ENTITY_NAME = "hemishe_HEducationForm";

        @GetMapping
        @Operation(summary = "Ta'lim shakllari ro'yxati")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            List<EducationForm> all = repository.findAll();
            int from = Math.min(offset, all.size());
            int to = Math.min(from + limit, all.size());
            List<Map<String, Object>> result = new ArrayList<>();
            for (EducationForm e : all.subList(from, to)) {
                result.add(toMap(e, returnNulls));
            }
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "Ta'lim shakli bo'yicha olish")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return repository.findById(code)
                    .map(e -> ResponseEntity.ok(toMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }

        private Map<String, Object> toMap(EducationForm e, Boolean returnNulls) {
            return classifierToMap(ENTITY_NAME, e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                    e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                    e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
        }
    }

    // =====================================================
    // hemishe_HCourse
    // =====================================================
    @Tag(name = "90.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HCourse")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class HCourseEntityController {

        private final HCourseRepository repository;
        private static final String ENTITY_NAME = "hemishe_HCourse";

        @GetMapping
        @Operation(summary = "Kurslar ro'yxati (1-kurs, 2-kurs, ...)")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            List<HCourse> all = repository.findAll();
            int from = Math.min(offset, all.size());
            int to = Math.min(from + limit, all.size());
            List<Map<String, Object>> result = new ArrayList<>();
            for (HCourse e : all.subList(from, to)) {
                result.add(toMap(e, returnNulls));
            }
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "Kurs bo'yicha olish")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return repository.findById(code)
                    .map(e -> ResponseEntity.ok(toMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }

        private Map<String, Object> toMap(HCourse e, Boolean returnNulls) {
            return classifierToMap(ENTITY_NAME, e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                    e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                    e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
        }
    }

    // =====================================================
    // hemishe_HEducationYear
    // =====================================================
    @Tag(name = "90.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HEducationYear")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class EducationYearEntityController {

        private final EducationYearRepository repository;
        private static final String ENTITY_NAME = "hemishe_HEducationYear";

        @GetMapping
        @Operation(summary = "O'quv yillari ro'yxati")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            List<EducationYear> all = repository.findAll();
            int from = Math.min(offset, all.size());
            int to = Math.min(from + limit, all.size());
            List<Map<String, Object>> result = new ArrayList<>();
            for (EducationYear e : all.subList(from, to)) {
                result.add(toMap(e, returnNulls));
            }
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "O'quv yili bo'yicha olish")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return repository.findById(code)
                    .map(e -> ResponseEntity.ok(toMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }

        private Map<String, Object> toMap(EducationYear e, Boolean returnNulls) {
            return classifierToMap(ENTITY_NAME, e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                    e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                    e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
        }
    }

    // =====================================================
    // hemishe_HTransferType
    // =====================================================
    @Tag(name = "90.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HTransferType")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class TransferTypeEntityController {

        private final TransferTypeRepository repository;
        private static final String ENTITY_NAME = "hemishe_HTransferType";

        @GetMapping
        @Operation(summary = "O'tkazish turlari ro'yxati")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            List<TransferType> all = repository.findAll();
            int from = Math.min(offset, all.size());
            int to = Math.min(from + limit, all.size());
            List<Map<String, Object>> result = new ArrayList<>();
            for (TransferType e : all.subList(from, to)) {
                result.add(toMap(e, returnNulls));
            }
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "O'tkazish turi bo'yicha olish")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return repository.findById(code)
                    .map(e -> ResponseEntity.ok(toMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }

        private Map<String, Object> toMap(TransferType e, Boolean returnNulls) {
            return classifierToMap(ENTITY_NAME, e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                    e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                    e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
        }
    }

    // =====================================================
    // Shared CUBA Map builder for all classifiers
    // =====================================================
    static Map<String, Object> classifierToMap(String entityName, String code, String name,
            String nameRu, String nameEn, Integer version, Boolean active,
            LocalDateTime createTs, String createdBy,
            LocalDateTime updateTs, String updatedBy,
            LocalDateTime deleteTs, String deletedBy,
            Boolean returnNulls) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", entityName);
        map.put("_instanceName", name);
        map.put("code", code);
        map.put("name", name);

        putIfNotNull(map, "nameRu", nameRu, returnNulls);
        putIfNotNull(map, "nameEn", nameEn, returnNulls);
        putIfNotNull(map, "version", version, returnNulls);
        putIfNotNull(map, "active", active, returnNulls);
        putIfNotNull(map, "createTs", createTs != null ? createTs.format(CUBA_DT) : null, returnNulls);
        putIfNotNull(map, "createdBy", createdBy, returnNulls);
        putIfNotNull(map, "updateTs", updateTs != null ? updateTs.format(CUBA_DT) : null, returnNulls);
        putIfNotNull(map, "updatedBy", updatedBy, returnNulls);
        putIfNotNull(map, "deleteTs", deleteTs != null ? deleteTs.format(CUBA_DT) : null, returnNulls);
        putIfNotNull(map, "deletedBy", deletedBy, returnNulls);

        return map;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
