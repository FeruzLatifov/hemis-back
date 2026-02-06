package uz.hemis.api.legacy.controller.classifier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.legacy.ClassifierLegacyService;

import java.util.*;
import java.util.stream.Collectors;

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

    // =====================================================
    // hemishe_HEducationType
    // =====================================================
    @Tag(name = "13.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HEducationType")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class EducationTypeEntityController {

        private final ClassifierLegacyService classifierService;

        @GetMapping
        @Operation(summary = "Ta'lim turlari ro'yxati (old-hemis da yo'q)")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            var all = classifierService.findAllEducationTypes();
            var paged = classifierService.applyPagination(all, limit, offset);
            List<Map<String, Object>> result = paged.stream()
                    .map(e -> classifierService.toEducationTypeMap(e, returnNulls))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "Ta'lim turi bo'yicha olish (old-hemis da yo'q)")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return classifierService.findEducationTypeByCode(code)
                    .map(e -> ResponseEntity.ok(classifierService.toEducationTypeMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }
    }

    // =====================================================
    // hemishe_HEducationForm
    // =====================================================
    @Tag(name = "13.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HEducationForm")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class EducationFormEntityController {

        private final ClassifierLegacyService classifierService;

        @GetMapping
        @Operation(summary = "Ta'lim shakllari ro'yxati (old-hemis da yo'q)")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            var all = classifierService.findAllEducationForms();
            var paged = classifierService.applyPagination(all, limit, offset);
            List<Map<String, Object>> result = paged.stream()
                    .map(e -> classifierService.toEducationFormMap(e, returnNulls))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "Ta'lim shakli bo'yicha olish (old-hemis da yo'q)")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return classifierService.findEducationFormByCode(code)
                    .map(e -> ResponseEntity.ok(classifierService.toEducationFormMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }
    }

    // =====================================================
    // hemishe_HCourse
    // =====================================================
    @Tag(name = "13.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HCourse")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class HCourseEntityController {

        private final ClassifierLegacyService classifierService;

        @GetMapping
        @Operation(summary = "Kurslar ro'yxati (old-hemis da yo'q)")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            var all = classifierService.findAllCourses();
            var paged = classifierService.applyPagination(all, limit, offset);
            List<Map<String, Object>> result = paged.stream()
                    .map(e -> classifierService.toCourseMap(e, returnNulls))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "Kurs bo'yicha olish (old-hemis da yo'q)")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return classifierService.findCourseByCode(code)
                    .map(e -> ResponseEntity.ok(classifierService.toCourseMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }
    }

    // =====================================================
    // hemishe_HEducationYear
    // =====================================================
    @Tag(name = "13.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HEducationYear")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class EducationYearEntityController {

        private final ClassifierLegacyService classifierService;

        @GetMapping
        @Operation(summary = "O'quv yillari ro'yxati (old-hemis da yo'q)")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            var all = classifierService.findAllEducationYears();
            var paged = classifierService.applyPagination(all, limit, offset);
            List<Map<String, Object>> result = paged.stream()
                    .map(e -> classifierService.toEducationYearMap(e, returnNulls))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "O'quv yili bo'yicha olish (old-hemis da yo'q)")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return classifierService.findEducationYearByCode(code)
                    .map(e -> ResponseEntity.ok(classifierService.toEducationYearMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }
    }

    // =====================================================
    // hemishe_HTransferType
    // =====================================================
    @Tag(name = "13.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HTransferType")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class TransferTypeEntityController {

        private final ClassifierLegacyService classifierService;

        @GetMapping
        @Operation(summary = "O'tkazish turlari ro'yxati (old-hemis da yo'q)")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            var all = classifierService.findAllTransferTypes();
            var paged = classifierService.applyPagination(all, limit, offset);
            List<Map<String, Object>> result = paged.stream()
                    .map(e -> classifierService.toTransferTypeMap(e, returnNulls))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "O'tkazish turi bo'yicha olish (old-hemis da yo'q)")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return classifierService.findTransferTypeByCode(code)
                    .map(e -> ResponseEntity.ok(classifierService.toTransferTypeMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }
    }

    // =====================================================
    // hemishe_HAdmissionType
    // =====================================================
    @Tag(name = "13.Klassifikatorlar")
    @RestController
    @RequestMapping("/app/rest/v2/entities/hemishe_HAdmissionType")
    @RequiredArgsConstructor
    @SecurityRequirement(name = "bearerAuth")
    public static class AdmissionTypeEntityController {

        private final ClassifierLegacyService classifierService;

        @GetMapping
        @Operation(summary = "Qabul turlari ro'yxati")
        public ResponseEntity<List<Map<String, Object>>> getAll(
                @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
                @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
                @RequestParam(required = false) Boolean returnNulls) {
            var all = classifierService.findAllAdmissionTypes();
            var paged = classifierService.applyPagination(all, limit, offset);
            List<Map<String, Object>> result = paged.stream()
                    .map(e -> classifierService.toAdmissionTypeMap(e, returnNulls))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }

        @GetMapping("/{code}")
        @Operation(summary = "Qabul turi bo'yicha olish")
        public ResponseEntity<Map<String, Object>> getByCode(
                @PathVariable String code,
                @RequestParam(required = false) Boolean returnNulls) {
            return classifierService.findAdmissionTypeByCode(code)
                    .map(e -> ResponseEntity.ok(classifierService.toAdmissionTypeMap(e, returnNulls)))
                    .orElse(ResponseEntity.notFound().build());
        }
    }
}
