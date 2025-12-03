package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uz.hemis.service.StudentGpaService;
import uz.hemis.service.StudentService;
import uz.hemis.service.VerificationService;
import uz.hemis.service.integration.HemisApiService;

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
@Tag(name = "04.Talaba", description = "Talaba ma'lumotlari bilan ishlash xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class StudentServiceController {

    private final StudentService studentService;
    private final StudentGpaService studentGpaService;
    private final VerificationService verificationService;
    private final UserRepository userRepository;
    private final HemisApiService hemisApiService;

    /**
     * Talaba tasdiqlash ballarini olish (DTM verification)
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/verify}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - DTM verification ballarni qaytaradi</p>
     *
     * <p><strong>Response format:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "count": 2,
     *   "records": [
     *     {
     *       "_entityName": "hemishe_EVerification",
     *       "id": "...",
     *       "pinfl": "...",
     *       "points": "69.3",
     *       "paymentForm": {...},
     *       "educationType": {...},
     *       "university": {...},
     *       "educationYear": {...},
     *       "category": {...}
     *     }
     *   ]
     * }
     * </pre>
     *
     * @param pinfl Talaba PINFL raqami (14 xonali)
     * @return DTM verification ballari va ma'lumotlari
     */
    @GetMapping("/verify")
    @Operation(
            summary = "Talaba tasdiqlash (DTM verification)",
            description = """
                Talabaning DTM verification ballarini olish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** GET /app/rest/v2/services/student/verify
                **Auth:** Bearer token (required)

                **Parametr:**
                - pinfl: Talaba PINFL raqami (14 xonali)

                **Response:** DTM verification ballari ro'yxati
                - success: muvaffaqiyat holati
                - count: topilgan verification yozuvlari soni
                - records: verification ma'lumotlari ro'yxati
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Verification ballari qaytarildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> verify(
            @Parameter(description = "Talaba PINFL raqami", example = "52503015440023")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] student/verify: pinfl={}", pinfl);
        return ResponseEntity.ok(verificationService.verifyByPinfl(pinfl));
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
     * Talaba shartnoma ma'lumotlarini olish (api.hemis.uz proxy)
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/student/contractInfo}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * <p><strong>OLD-HEMIS Logic (StudentServiceBean.contractInfo):</strong></p>
     * <ol>
     *   <li>SSettings dan token olish (myTokenService.getApiHemisToken())</li>
     *   <li>api.hemis.uz ga proxy so'rov yuborish</li>
     *   <li>Response ni qaytarish</li>
     * </ol>
     *
     * <p><strong>NEW-HEMIS Implementation:</strong></p>
     * <ol>
     *   <li>Token HEMIS_API_TOKEN environment variable dan olinadi</li>
     *   <li>HemisApiService orqali api.hemis.uz ga so'rov yuboriladi</li>
     *   <li>Response aynan OLD-HEMIS formatida qaytariladi</li>
     * </ol>
     *
     * <p><strong>Response format (api.hemis.uz):</strong></p>
     * <pre>
     * {
     *   "statusCode": 200,
     *   "message": "Muvaffaqiyatli bajarildi",
     *   "timeStamp": "2025-11-29T06:18:22.817959598",
     *   "object": {
     *     "institutionType": "Oliy ta'lim",
     *     "pinfl": "61111065190052",
     *     "fullName": "RAXIMJONOV DILSHODBEK DILMUROD O'G'LI",
     *     "contractNumber": "300-12/12-22/Q-1207",
     *     "contractDate": "2022-09-14",
     *     "eduOrganizationId": 316,
     *     "eduOrganization": "Toshkent axborot texnologiyalari universiteti...",
     *     "eduSpeciality": "Dasturiy injiniring...",
     *     "eduContractSum": 8623000,
     *     "gpa": 3.86,
     *     "debit": 2300000,
     *     "credit": 6323000,
     *     ...
     *   }
     * }
     * </pre>
     *
     * <p><strong>Configuration (.env):</strong></p>
     * <pre>
     * HEMIS_API_BASE_URL=https://api.hemis.uz
     * HEMIS_API_TOKEN=your-secret-token
     * HEMIS_API_TIMEOUT=30
     * </pre>
     *
     * @param pinfl Talaba PINFL raqami (14 xonali)
     * @return Shartnoma ma'lumotlari (api.hemis.uz dan)
     */
    @GetMapping("/contractInfo")
    @Operation(
            summary = "Talaba shartnoma ma'lumotlari (Contract Info)",
            description = """
                Talabaning shartnoma ma'lumotlarini api.hemis.uz dan olish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** GET /app/rest/v2/services/student/contractInfo
                **Auth:** Bearer token (required)

                **Parametr:**
                - pinfl: Talaba PINFL raqami (14 xonali)

                **Ishlash logikasi:**
                1. HEMIS_API_TOKEN environment variable dan token olinadi
                2. api.hemis.uz ga proxy so'rov yuboriladi
                3. Response aynan qaytariladi

                **Configuration (.env):**
                - HEMIS_API_BASE_URL=https://api.hemis.uz (default)
                - HEMIS_API_TOKEN=your-token (required)
                - HEMIS_API_TIMEOUT=30 (seconds)
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Shartnoma ma'lumotlari qaytarildi",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Shartnoma ma'lumotlari",
                                    value = """
                                        {
                                          "statusCode": 200,
                                          "message": "Muvaffaqiyatli bajarildi",
                                          "timeStamp": "2025-11-29T06:18:22.817959598",
                                          "object": {
                                            "institutionType": "Oliy ta'lim",
                                            "pinfl": "61111065190052",
                                            "fullName": "RAXIMJONOV DILSHODBEK DILMUROD O'G'LI",
                                            "contractNumber": "300-12/12-22/Q-1207",
                                            "contractDate": "2022-09-14",
                                            "eduOrganizationId": 316,
                                            "eduOrganization": "Toshkent axborot texnologiyalari universiteti...",
                                            "eduSpeciality": "Dasturiy injiniring...",
                                            "eduContractSum": 8623000,
                                            "gpa": 3.86,
                                            "debit": 2300000,
                                            "credit": 6323000
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Token sozlanmagan",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Token xatosi",
                                    value = """
                                        {
                                          "success": false,
                                          "code": "token_not_configured",
                                          "message": "HEMIS API token not configured. Set HEMIS_API_TOKEN in environment"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> contractInfo(
            @Parameter(description = "Talaba PINFL raqami", example = "61111065190052")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] student/contractInfo: pinfl={}", pinfl);
        return ResponseEntity.ok(hemisApiService.getContractInfo(pinfl));
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
            summary = "Check - Talaba ID sini olish",
            description = """
                Talaba universal ID sini olish.

                Universal ID ni olish uchun quyidagi ma'lumotlar talab etiladi:
                - **PINFL** - Jismoniy shaxs identifikatsiya raqami
                - **Passport seria va nomer** - Fuqarolik hujjati
                - **O'qishga kirgan yili** - Ta'lim yili (masalan: "2024")
                - **O'qish turi** - 11=Bakalavr, 12=Magistr, 13=Doktorant
                - **O'qish shakli** - 11=Kunduzgi, 12=Sirtqi, 13=Kechki
                - **Jinsi** - 11=Erkak, 12=Ayol

                **API 2 xil usulda ishlaydi:**
                1. **Yangi talaba** - vazirlik bazasida topilmasa, yangi talaba yaratiladi va universal ID biriktiriladi
                2. **Mavjud talaba** - vazirlik bazasida mavjud bo'lsa, talaba obyekti qaytariladi
                3. **Xatolik** - biror ma'lumot noto'g'ri bo'lsa xatolik qaytariladi

                **ID formati:** {universityCode}{YY}{educationType}{sequence}
                **Misol:** 520241100001 = university 520, yil 24, turi 11, tartib 00001
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
     * @param data Validation data (PINFL or Passport serial number)
     * @return Student validation status
     */
    @GetMapping("/validate")
    @Operation(
            summary = "Talaba statusini tekshirish (Passport seriya)",
            description = """
                Talabaning joriy statusini tekshirish - PINFL yoki passport seriya orqali.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** GET /app/rest/v2/services/student/validate
                **Auth:** Bearer token (required)

                **Parametr:**
                - data: PINFL (14 xonali) yoki Passport seriya/raqami (masalan: A0939758)

                **Response kodlari:**
                - `not_active` - Talaba topilmadi, yangi yaratish mumkin
                - `active` - Talaba faol, o'qimoqda
                - `graduated` - Talaba bitirgan

                **Misol response (talaba topilmadi):**
                ```json
                {
                    "success": true,
                    "code": "not_active",
                    "message": "You can create new student!",
                    "data": "Student not found!"
                }
                ```
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Talaba statusi qaytarildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> validate(
            @Parameter(description = "PINFL yoki Passport seriya/raqami", example = "A0939758")
            @RequestParam String data) {
        log.info("[CUBA Service] student/validate: data={}", data);
        return ResponseEntity.ok(studentService.validateStudent(data));
    }

    /**
     * Talaba GPA yozuvini yaratish/yangilash (UPSERT)
     *
     * <p><strong>URL:</strong> {@code POST /app/rest/v2/services/student/gpa}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * <p><strong>Old-hemis logic:</strong></p>
     * <ol>
     *   <li>Mavjud GPA ni ID yoki (studentId + educationYear) bo'yicha qidiradi</li>
     *   <li>Agar topilsa, o'chiradi</li>
     *   <li>Yangi GPA yozuvini saqlaydi</li>
     * </ol>
     *
     * <p><strong>Request Body format (CUBA Entity):</strong></p>
     * <pre>
     * {
     *   "gpa": {
     *     "id": "UUID (optional)",
     *     "studentId": {"id": "UUID"},
     *     "educationYear": {"code": "2023"},
     *     "level": {"code": "12"},
     *     "gpa": "3.90",
     *     "method": "one_year",
     *     "creditSum": "50.0",
     *     "subjects": 12,
     *     "debtSubjects": 0
     *   }
     * }
     * </pre>
     *
     * <p><strong>Response format:</strong></p>
     * <pre>
     * {
     *   "_entityName": "hemishe_EStudentGpa",
     *   "id": "UUID",
     *   "debtSubjects": 0,
     *   "method": "one_year",
     *   "level": {"_entityName": "hemishe_HCourse", "id": "12", "code": "12"},
     *   "creditSum": "50.0",
     *   "subjects": 12,
     *   "educationYear": {"_entityName": "hemishe_HEducationYear", "id": "2023", "code": "2023"},
     *   "version": 1,
     *   "studentId": {"_entityName": "hemishe_EStudent", "id": "UUID", "fullname": ""},
     *   "createdBy": "otm401",
     *   "gpa": "3.90",
     *   "createTs": "2025-11-28 21:46:40.863",
     *   "updateTs": "2025-11-28 21:46:40.863"
     * }
     * </pre>
     *
     * @param request GPA ma'lumotlari (CUBA service format)
     * @return Saqlangan GPA yozuvi
     */
    @PostMapping("/gpa")
    @Operation(
            summary = "Talaba GPA servis (UPSERT)",
            description = """
                Talaba GPA yozuvini yaratish yoki yangilash (UPSERT pattern).

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** POST /app/rest/v2/services/student/gpa
                **Auth:** Bearer token (required)
                **Content-Type:** application/json

                **Ishlash logikasi:**
                1. Mavjud GPA ni ID yoki (studentId + educationYear) bo'yicha qidiradi
                2. Agar topilsa, eski yozuvni o'chiradi
                3. Yangi GPA yozuvini saqlaydi

                **Request Body:**
                - gpa.studentId.id: Talaba UUID (required)
                - gpa.educationYear.code: Ta'lim yili kodi (masalan: "2023")
                - gpa.level.code: Kurs darajasi kodi (masalan: "12" = 2-kurs)
                - gpa.gpa: GPA qiymati (masalan: "3.90")
                - gpa.method: Hisoblash usuli ("one_year" yoki "all_year")
                - gpa.creditSum: Jami kredit
                - gpa.subjects: Fan soni
                - gpa.debtSubjects: Qarzdor fan soni
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - GPA yozuvi saqlandi",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Saqlangan GPA",
                                    value = """
                                        {
                                          "_entityName": "hemishe_EStudentGpa",
                                          "id": "73a35e2a-a20b-69b6-9eb5-b11c493b59a7",
                                          "debtSubjects": 0,
                                          "method": "one_year",
                                          "level": {
                                            "_entityName": "hemishe_HCourse",
                                            "id": "12",
                                            "code": "12"
                                          },
                                          "creditSum": "50.0",
                                          "subjects": 12,
                                          "educationYear": {
                                            "_entityName": "hemishe_HEducationYear",
                                            "id": "2023",
                                            "code": "2023"
                                          },
                                          "version": 1,
                                          "studentId": {
                                            "_entityName": "hemishe_EStudent",
                                            "id": "001bf61e-cd66-499e-84f7-f42ae8646289",
                                            "fullname": ""
                                          },
                                          "createdBy": "otm401",
                                          "gpa": "3.90",
                                          "createTs": "2025-11-28 21:46:40.863",
                                          "updateTs": "2025-11-28 21:46:40.863"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri request format"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> gpa(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/gpa: request={}", request);

        // Get current username for createdBy field
        String username = getCurrentUsername();

        try {
            Map<String, Object> result = studentGpaService.upsert(request, username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("[CUBA Service] student/gpa: Invalid request - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Bad Request",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get current username from JWT token
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString("username");
        }
        return "anonymous";
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
