package uz.hemis.university.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.StudentCubaService;

import java.util.Map;

/**
 * Student API Controller
 *
 * <p><strong>Feature:</strong> Student Management</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong> StudentServiceBean.java (37 separate ServiceBeans pattern)</p>
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Student verification and retrieval</li>
 *   <li>Student enrollment and status tracking</li>
 *   <li>Contract and scholarship management</li>
 *   <li>Academic performance (GPA)</li>
 *   <li>Student lists and statistics</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern (Backward Compatible):</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_StudentService/{methodName}
 * → StudentCubaService.{methodName}()
 * </pre>
 *
 * <p><strong>Endpoints:</strong> 18 student-specific endpoints</p>
 * <p><strong>Users:</strong> 200+ universities across Uzbekistan</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/hemishe_StudentService")
@RequiredArgsConstructor
@Slf4j
public class StudentApiController {

    private final StudentCubaService studentCubaService;

    /**
     * Verify student exists by PINFL
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_StudentService/verify?pinfl={pinfl}
     * </pre>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * curl 'https://ministry.hemis.uz/app/rest/v2/services/hemishe_StudentService/verify?pinfl=12345678901234' \
     *   -H 'Authorization: Bearer {token}'
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "exists": true,
     *   "student": {...}
     * }
     * </pre>
     *
     * @param pinfl PINFL (14 digits)
     * @return verification result with student data if exists
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestParam("pinfl") String pinfl) {
        log.info("📋 Student verify - PINFL: {}", pinfl);
        Map<String, Object> result = studentCubaService.verify(pinfl);
        log.info("✅ Student verify completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get student by PINFL
     *
     * <p>Returns full student information including enrollment, faculty, and speciality</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return student details
     */
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> get(@RequestParam("pinfl") String pinfl) {
        log.info("📋 Student get - PINFL: {}", pinfl);
        return ResponseEntity.ok(studentCubaService.get(pinfl));
    }

    /**
     * Get student with enrollment status
     *
     * <p>Includes detailed status information (active, expelled, graduated, etc.)</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return student data with status details
     */
    @GetMapping("/getWithStatus")
    public ResponseEntity<Map<String, Object>> getWithStatus(@RequestParam("pinfl") String pinfl) {
        log.info("📋 Student getWithStatus - PINFL: {}", pinfl);
        return ResponseEntity.ok(studentCubaService.getWithStatus(pinfl));
    }

    /**
     * Get student by internal ID
     *
     * @param id Student ID (UUID format)
     * @return student details
     */
    @GetMapping("/getById")
    public ResponseEntity<Map<String, Object>> getById(@RequestParam("id") String id) {
        log.info("📋 Student getById - ID: {}", id);
        return ResponseEntity.ok(studentCubaService.getById(id));
    }

    /**
     * Get doctoral student information
     *
     * <p>Returns data specific to doctoral/PhD students</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return doctoral student details
     */
    @GetMapping("/getDoctoral")
    public ResponseEntity<Map<String, Object>> getDoctoral(@RequestParam("pinfl") String pinfl) {
        log.info("📋 Student getDoctoral - PINFL: {}", pinfl);
        return ResponseEntity.ok(studentCubaService.getDoctoral(pinfl));
    }

    /**
     * Check if students are expelled (batch operation)
     *
     * <p>Accepts multiple PINFLs and returns expulsion status for each</p>
     *
     * @param pinfl Array of PINFLs to check
     * @return expulsion status for each student
     */
    @GetMapping("/isExpel")
    public ResponseEntity<Map<String, Object>> isExpel(@RequestParam("pinfl") String[] pinfl) {
        log.info("📋 Student isExpel - Count: {}", pinfl != null ? pinfl.length : 0);
        return ResponseEntity.ok(studentCubaService.isExpel(pinfl));
    }

    /**
     * Get contract information for student
     *
     * <p>Returns contract details including payment amount, contract number, and validity period</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return contract information
     */
    @GetMapping("/contractInfo")
    public ResponseEntity<Map<String, Object>> contractInfo(@RequestParam("pinfl") String pinfl) {
        log.info("📋 Student contractInfo - PINFL: {}", pinfl);
        return ResponseEntity.ok(studentCubaService.contractInfo(pinfl));
    }

    /**
     * Health check for Student service
     *
     * <p>Returns service status and availability</p>
     *
     * @return health check result
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check() {
        log.debug("🏥 Student service health check");
        return ResponseEntity.ok(studentCubaService.check());
    }

    /**
     * List students by university
     *
     * <p>Returns paginated list of students for a specific university</p>
     *
     * @param university University code
     * @param limit Maximum number of records (optional)
     * @param offset Offset for pagination (optional)
     * @return paginated student list
     */
    @GetMapping("/students")
    public ResponseEntity<Map<String, Object>> students(
            @RequestParam("university") String university,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset) {
        log.info("📋 Student students - University: {}, Limit: {}, Offset: {}", university, limit, offset);
        return ResponseEntity.ok(studentCubaService.students(university, limit, offset));
    }

    /**
     * Calculate student GPA
     *
     * <p>Returns current GPA based on all completed courses</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return GPA calculation result
     */
    @GetMapping("/gpa")
    public ResponseEntity<Map<String, Object>> gpa(@RequestParam("pinfl") String pinfl) {
        log.info("📊 Student gpa - PINFL: {}", pinfl);
        return ResponseEntity.ok(studentCubaService.gpa(pinfl));
    }

    /**
     * Update student data
     *
     * <p>Updates student information (contact details, address, etc.)</p>
     *
     * @param studentData Student data to update
     * @return update result
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, Object> studentData) {
        log.info("✏️ Student update - Data received");
        Map<String, Object> result = studentCubaService.update(studentData);
        log.info("✅ Student update completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    /**
     * Check scholarship eligibility
     *
     * <p>Checks if student qualifies for scholarship based on GPA and other criteria</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return scholarship eligibility status
     */
    @GetMapping("/checkScholarship")
    public ResponseEntity<Map<String, Object>> checkScholarship(@RequestParam("pinfl") String pinfl) {
        log.info("💰 Student checkScholarship - PINFL: {}", pinfl);
        return ResponseEntity.ok(studentCubaService.checkScholarship(pinfl));
    }

    /**
     * Check scholarship eligibility (v2 - with semester)
     *
     * <p>Checks scholarship eligibility for a specific semester</p>
     *
     * @param pinfl PINFL (14 digits)
     * @param semester Semester code
     * @return scholarship eligibility for specified semester
     */
    @GetMapping("/checkScholarship2")
    public ResponseEntity<Map<String, Object>> checkScholarship2(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("semester") String semester) {
        log.info("💰 Student checkScholarship2 - PINFL: {}, Semester: {}", pinfl, semester);
        return ResponseEntity.ok(studentCubaService.checkScholarship2(pinfl, semester));
    }

    /**
     * Get student ID by criteria
     *
     * <p>Searches for student ID using various criteria (name, passport, etc.)</p>
     *
     * @param data Search criteria (JSON string)
     * @return student ID if found
     */
    @GetMapping("/id")
    public ResponseEntity<Map<String, Object>> id(@RequestParam("data") String data) {
        log.info("🔍 Student id - Data: {}", data);
        return ResponseEntity.ok(studentCubaService.id(data));
    }

    /**
     * Validate student data
     *
     * <p>Validates student information before enrollment or update</p>
     *
     * @param studentData Student data to validate
     * @return validation result with errors if any
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, Object> studentData) {
        log.info("✔️ Student validate - Data received");
        Map<String, Object> result = studentCubaService.validate(studentData);
        log.info("✅ Student validate completed - Valid: {}", result.get("valid"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get Tashkent students for university
     *
     * <p>Returns students registered in Tashkent region</p>
     *
     * @param university University code
     * @return list of Tashkent students
     */
    @GetMapping("/tashkentStudents")
    public ResponseEntity<Map<String, Object>> tashkentStudents(@RequestParam("university") String university) {
        log.info("🏙️ Student tashkentStudents - University: {}", university);
        return ResponseEntity.ok(studentCubaService.tashkentStudents(university));
    }

    /**
     * Get students by Tashkent and payment form
     *
     * <p>Returns students in Tashkent filtered by payment form (grant/contract)</p>
     *
     * @param university University code
     * @param paymentForm Payment form code (grant/contract)
     * @return filtered student list
     */
    @GetMapping("/byTashkentAndPaymentForm")
    public ResponseEntity<Map<String, Object>> byTashkentAndPaymentForm(
            @RequestParam("university") String university,
            @RequestParam("paymentForm") String paymentForm) {
        log.info("🏙️💰 Student byTashkentAndPaymentForm - University: {}, PaymentForm: {}", university, paymentForm);
        return ResponseEntity.ok(studentCubaService.byTashkentAndPaymentForm(university, paymentForm));
    }

    /**
     * Get contract statistics for university
     *
     * <p>Returns statistics on contract students (count, payment amounts, etc.)</p>
     *
     * @param university University code
     * @return contract statistics
     */
    @GetMapping("/contractStatistics")
    public ResponseEntity<Map<String, Object>> contractStatistics(@RequestParam("university") String university) {
        log.info("📊 Student contractStatistics - University: {}", university);
        return ResponseEntity.ok(studentCubaService.contractStatistics(university));
    }

    // =====================================================
    // ✅ 18 STUDENT ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Separate controller per feature
    // Equivalent to: StudentServiceBean.java in CUBA Platform
    // =====================================================
}
