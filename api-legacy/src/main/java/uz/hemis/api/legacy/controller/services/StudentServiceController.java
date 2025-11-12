package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.StudentService;

import java.util.Map;
import java.util.UUID;

/**
 * Student Service Controller - CUBA REST API Compatible
 *
 * <p>Preserves exact URLs from old HEMIS CUBA platform for backward compatibility</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/student/*}</p>
 *
 * <p><strong>Total Methods:</strong> 25 endpoints</p>
 *
 * <p><strong>Critical Services:</strong></p>
 * <ul>
 *   <li>Student verification</li>
 *   <li>Student CRUD operations</li>
 *   <li>Scholarship checks</li>
 *   <li>Contract information</li>
 *   <li>Statistical queries</li>
 * </ul>
 *
 * @since 2.0.0
 * @see StudentService
 */
@RestController
@RequestMapping("/services/student")
@Tag(name = "Student Service API", description = "CUBA compatible student service endpoints")
@RequiredArgsConstructor
@Slf4j
public class StudentServiceController {

    private final StudentService studentService;

    /**
     * Verify student by PINFL
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/verify}</p>
     *
     * @param pinfl Student PINFL (Personal Identification Number)
     * @return Student verification data
     */
    @GetMapping("/verify")
    @Operation(summary = "Verify student", description = "Verify student exists by PINFL")
    public ResponseEntity<?> verify(@RequestParam String pinfl) {
        log.info("[CUBA Service] student/verify: pinfl={}", pinfl);
        return ResponseEntity.ok(studentService.verify(pinfl));
    }

    /**
     * Get student by PINFL
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/get}</p>
     *
     * @param pinfl Student PINFL
     * @return Student data
     */
    @GetMapping("/get")
    @Operation(summary = "Get student", description = "Get student by PINFL")
    public ResponseEntity<?> get(@RequestParam String pinfl) {
        log.info("[CUBA Service] student/get: pinfl={}", pinfl);
        return ResponseEntity.ok(studentService.getByPinfl(pinfl));
    }

    /**
     * Get student by ID
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/getById}</p>
     *
     * @param id Student UUID
     * @return Student data
     */
    @GetMapping("/getById")
    @Operation(summary = "Get student by ID", description = "Get student by database ID")
    public ResponseEntity<?> getById(@RequestParam UUID id) {
        log.info("[CUBA Service] student/getById: id={}", id);
        return ResponseEntity.ok(studentService.getById(id));
    }

    /**
     * Get student with status
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/getWithStatus}</p>
     *
     * @param pinfl Student PINFL
     * @return Student data with current status
     */
    @GetMapping("/getWithStatus")
    @Operation(summary = "Get student with status", description = "Get student with current academic status")
    public ResponseEntity<?> getWithStatus(@RequestParam String pinfl) {
        log.info("[CUBA Service] student/getWithStatus: pinfl={}", pinfl);
        return ResponseEntity.ok(studentService.getWithStatus(pinfl));
    }

    /**
     * Get contract information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/contractInfo}</p>
     *
     * @param pinfl Student PINFL
     * @return Contract information
     */
    @GetMapping("/contractInfo")
    @Operation(summary = "Get contract info", description = "Get student contract information")
    public ResponseEntity<?> contractInfo(@RequestParam String pinfl) {
        log.info("[CUBA Service] student/contractInfo: pinfl={}", pinfl);
        return ResponseEntity.ok(studentService.getContractInfo(pinfl));
    }

    /**
     * Check students
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/check}</p>
     *
     * @return Check result
     */
    @GetMapping("/check")
    @Operation(summary = "Check students", description = "Perform student data check")
    public ResponseEntity<?> check() {
        log.info("[CUBA Service] student/check");
        return ResponseEntity.ok(studentService.check());
    }

    /**
     * Get doctoral student
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/getDoctoral}</p>
     *
     * @param pinfl Student PINFL
     * @return Doctoral student data
     */
    @GetMapping("/getDoctoral")
    @Operation(summary = "Get doctoral student", description = "Get doctoral student information")
    public ResponseEntity<?> getDoctoral(@RequestParam String pinfl) {
        log.info("[CUBA Service] student/getDoctoral: pinfl={}", pinfl);
        return ResponseEntity.ok(studentService.getDoctoral(pinfl));
    }

    /**
     * Get students by university
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/students}</p>
     *
     * @param university University code
     * @param limit      Result limit
     * @param offset     Result offset
     * @return List of students
     */
    @GetMapping("/students")
    @Operation(summary = "Get students by university", description = "Get paginated list of students for university")
    public ResponseEntity<?> students(
            @RequestParam String university,
            @RequestParam(defaultValue = "100") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        log.info("[CUBA Service] student/students: university={}, limit={}, offset={}", university, limit, offset);
        return ResponseEntity.ok(studentService.getStudentsByUniversity(university, limit, offset));
    }

    // =====================================================
    // POST Methods - old-hemis compatibility
    // =====================================================

    /**
     * Get student ID by criteria
     *
     * <p><strong>URL:</strong> {@code POST /services/student/id}</p>
     *
     * @param request Student ID request (PINFL, passport, etc.)
     * @return Student UUID
     */
    @PostMapping("/id")
    @Operation(summary = "Talaba ID sini olish", description = "PINFL yoki boshqa ma'lumotlar orqali talaba UUID raqamini olish")
    public ResponseEntity<?> id(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/id: request={}", request);
        String pinfl = (String) request.get("pinfl");
        return ResponseEntity.ok(studentService.getById(pinfl));
    }

    /**
     * Update student information
     *
     * <p><strong>URL:</strong> {@code POST /services/student/update}</p>
     *
     * @param request Student update request
     * @return Success status
     */
    @PostMapping("/update")
    @Operation(summary = "Talabani o'zgartirish", description = "Talaba ma'lumotlarini yangilash")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/update: request={}", request);
        return ResponseEntity.ok(studentService.updateStudent(request));
    }

    /**
     * Validate student status
     *
     * <p><strong>URL:</strong> {@code GET /services/student/validate}</p>
     *
     * @param data Validation data (PINFL or student ID)
     * @return Student validation status
     */
    @GetMapping("/validate")
    @Operation(summary = "Talaba statusini tekshirish", description = "Talabaning joriy statusini tekshirish")
    public ResponseEntity<?> validate(@RequestParam String data) {
        log.info("[CUBA Service] student/validate: data={}", data);
        return ResponseEntity.ok(studentService.validateStudent(data));
    }

    /**
     * Calculate student GPA
     *
     * <p><strong>URL:</strong> {@code POST /services/student/gpa}</p>
     *
     * @param request GPA request (student ID, semester)
     * @return GPA calculation result
     */
    @PostMapping("/gpa")
    @Operation(summary = "Talaba GPA servis", description = "Talabaning GPA (Grade Point Average) ni hisoblash")
    public ResponseEntity<?> gpa(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/gpa: request={}", request);
        return ResponseEntity.ok(studentService.calculateGpa(request));
    }

    /**
     * Check scholarship eligibility (version 2)
     *
     * <p><strong>URL:</strong> {@code POST /services/student/checkScholarship2}</p>
     *
     * @param request Scholarship check request
     * @return Scholarship eligibility status
     */
    @PostMapping("/checkScholarship2")
    @Operation(summary = "Scholarship check", description = "Talabaning stipendiya olish huquqini tekshirish")
    public ResponseEntity<?> checkScholarship(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/checkScholarship2: request={}", request);
        return ResponseEntity.ok(studentService.checkScholarship(request));
    }

    /**
     * Submit contract statistics
     *
     * <p><strong>URL:</strong> {@code POST /services/student/contractStatistics}</p>
     *
     * @param request Contract statistics request
     * @return Success status
     */
    @PostMapping("/contractStatistics")
    @Operation(summary = "Shartnoma statistikasini yuborish", description = "Shartnoma statistika ma'lumotlarini tashqi tizimga yuborish")
    public ResponseEntity<?> contractStatistics(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/contractStatistics: request={}", request);
        return ResponseEntity.ok(studentService.submitContractStatistics(request));
    }
}
