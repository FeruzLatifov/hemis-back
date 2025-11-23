package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.StudentIdRequest;
import uz.hemis.domain.repository.UserRepository;
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
@RequestMapping("/app/rest/v2/services/student")
@Tag(name = "Student Service API", description = "CUBA compatible student service endpoints")
@RequiredArgsConstructor
@Slf4j
public class StudentServiceController {

    private final StudentService studentService;
    private final UserRepository userRepository;

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
     * Talaba unique ID sini olish yoki yaratish (OLD-HEMIS Compatible)
     *
     * <p><strong>URL:</strong> {@code POST /app/rest/v2/services/student/id}</p>
     *
     * <p><strong>Old-hemis format (JSON):</strong></p>
     * <pre>
     * {
     *   "data": {
     *     "citizenship": "11",
     *     "pinfl": "31507976020031",
     *     "serial": "AA6970877",
     *     "year": "2024",
     *     "education_type": "11",
     *     "education_form": "11"
     *   }
     * }
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "is_new": true/false,
     *   "unique_id": "0102241100001",
     *   "student": {...}
     * }
     * </pre>
     *
     * @param request {"data": StudentIdRequest}
     * @return Talaba ID va ma'lumotlari
     */
    @PostMapping("/id")
    @Operation(
            summary = "Talaba ID sini olish",
            description = """
                Talaba uchun unique ID olish yoki yangi ID yaratish.

                **Agar talaba mavjud bo'lsa** - mavjud ID qaytariladi.
                **Agar talaba mavjud bo'lmasa** - yangi ID generatsiya qilinadi va talaba yaratiladi.

                **Majburiy parametrlar:**
                - citizenship: Fuqarolik kodi (11 = O'zbekiston)
                - pinfl: JSHSHIR raqami (O'zbeklar uchun)
                - serial: Passport seria/raqami
                - year: Ta'lim yili (masalan: "2024")
                - education_type: Ta'lim turi (11=Bakalavr, 12=Magistr, 13=Doktorant)

                **ID formati:** {universityCode}{YY}{educationType}{sequence}
                **Misol:** 010224110001
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Talaba ID muvaffaqiyatli qaytarildi",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Yangi talaba",
                                    value = """
                                        {
                                          "success": true,
                                          "is_new": true,
                                          "unique_id": "0102241100001",
                                          "university": "0102",
                                          "student": {
                                            "id": "...",
                                            "code": "0102241100001",
                                            "pinfl": "31507976020031"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Talaba aktiv (xatolik)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Aktiv talaba xatosi",
                                    value = """
                                        {
                                          "success": false,
                                          "message": "Student is active!",
                                          "is_active": true,
                                          "student": {...}
                                        }
                                        """
                            )
                    )
            )
    })
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> id(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/id: request={}", request);

        // Extract "data" wrapper (old-hemis format)
        Object dataObj = request.get("data");
        if (dataObj == null) {
            log.warn("Missing 'data' parameter in request");
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Missing 'data' parameter"
            ));
        }

        // Convert to StudentIdRequest
        StudentIdRequest studentIdRequest = new StudentIdRequest();
        if (dataObj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) dataObj;
            studentIdRequest.setCitizenship((String) data.get("citizenship"));
            studentIdRequest.setPinfl((String) data.get("pinfl"));
            studentIdRequest.setSerial((String) data.get("serial"));
            studentIdRequest.setYear((String) data.get("year"));
            studentIdRequest.setEducationType((String) data.get("education_type"));
            studentIdRequest.setEducationForm((String) data.get("education_form"));
        }

        // Get current user's university code from users table
        String universityCode = getCurrentUserUniversityCode();

        // Validate university code
        if (universityCode == null || universityCode.isEmpty()) {
            log.error("University code not found for current user");
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User university not configured"
            ));
        }

        log.info("Using university code: {} for student/id request", universityCode);

        // Call service
        Map<String, Object> result = studentService.generateStudentId(studentIdRequest, universityCode);
        return ResponseEntity.ok(result);
    }

    /**
     * Get current user's university code from JWT token and users table
     *
     * <p><strong>Flow:</strong></p>
     * <ol>
     *   <li>Extract user ID from JWT 'sub' claim</li>
     *   <li>Query users table for university_id</li>
     *   <li>Return university code (e.g., "401")</li>
     * </ol>
     *
     * @return university code or null if not found
     */
    private String getCurrentUserUniversityCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("No authentication found in security context");
            return null;
        }

        // Extract user ID from JWT token
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String userId = jwt.getSubject();  // JWT 'sub' claim = user UUID
            String username = jwt.getClaimAsString("username");

            log.debug("Current user: {} (userId: {})", username, userId);

            if (userId != null) {
                try {
                    UUID userUuid = UUID.fromString(userId);
                    // Query users table for university_id
                    return userRepository.findUniversityCodeById(userUuid)
                            .orElseGet(() -> {
                                log.warn("University not found for user: {}", username);
                                return null;
                            });
                } catch (IllegalArgumentException e) {
                    log.error("Invalid UUID in JWT subject: {}", userId);
                }
            }
        }

        log.warn("Unable to extract university code from authentication: {}", auth.getClass().getSimpleName());
        return null;
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
