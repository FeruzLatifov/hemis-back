package uz.hemis.api.legacy.controller.student;

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
import uz.hemis.common.dto.student.StudentIdRequest;
import uz.hemis.common.log.LogSafe;
import uz.hemis.service.legacy.UserLegacyService;
import uz.hemis.service.student.StudentGpaService;
import uz.hemis.service.student.StudentService;
import uz.hemis.service.student.VerificationService;
import uz.hemis.service.integration.HemisApiService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uz.hemis.api.legacy.adapter.LegacyResponseHelper;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

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
    private final UserLegacyService userLegacyService;
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
    @PreAuthorize("hasAuthority('students.view')")
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
        log.info("[CUBA Service] student/verify: pinfl={}", LogSafe.pinfl(pinfl));
        return ResponseEntity.ok(verificationService.verifyByPinfl(pinfl));
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
    @PreAuthorize("hasAuthority('students.view')")
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
        log.info("[CUBA Service] student/contractInfo: pinfl={}", LogSafe.pinfl(pinfl));
        return ResponseEntity.ok(hemisApiService.getContractInfo(pinfl));
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
    @PreAuthorize("hasAuthority('students.edit')")
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
        // PII safety: never log full request body (contains PINFL, passport, address).
        log.info("[CUBA Service] student/id: keys={}", request.keySet());

        // Extract "data" wrapper (old-hemis format)
        Object dataObj = request.get("data");
        if (dataObj == null) {
            log.warn("Missing 'data' parameter in request");
            return ResponseEntity.ok(LegacyResponseHelper.failureMap("Missing 'data' parameter"));
        }

        // Convert to StudentIdRequest
        // Note: PHP may send year as Integer, so use String.valueOf() for safe conversion
        StudentIdRequest studentIdRequest = new StudentIdRequest();
        if (dataObj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) dataObj;
            studentIdRequest.setCitizenship(data.get("citizenship") != null ? String.valueOf(data.get("citizenship")) : null);
            studentIdRequest.setPinfl(data.get("pinfl") != null ? String.valueOf(data.get("pinfl")) : null);
            studentIdRequest.setSerial(data.get("serial") != null ? String.valueOf(data.get("serial")) : null);
            studentIdRequest.setYear(data.get("year") != null ? String.valueOf(data.get("year")) : null);
            studentIdRequest.setEducationType(data.get("education_type") != null ? String.valueOf(data.get("education_type")) : null);
            studentIdRequest.setEducationForm(data.get("education_form") != null ? String.valueOf(data.get("education_form")) : null);
        }

        // Get current user's university code from users table
        String universityCode = getCurrentUserUniversityCode();

        // Validate university code
        if (universityCode == null || universityCode.isEmpty()) {
            log.error("University code not found for current user");
            return ResponseEntity.ok(LegacyResponseHelper.failureMap("User university not configured"));
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
                    return userLegacyService.findUniversityCodeById(userUuid)
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
     * Update student information (transfer or data update)
     *
     * <p><strong>URL:</strong> {@code POST /services/student/update}</p>
     *
     * <p><strong>OLD-HEMIS Compatible:</strong></p>
     * <ul>
     *   <li>Request format: {"student": {"id": "...", "university": {"code": "..."}, "studentStatus": {"code": "..."}, ...}}</li>
     *   <li>Transfer: status='12' + boshqa OTM kodi → talaba ko'chiriladi, null qaytariladi</li>
     *   <li>Data update: transfer sharti yo'q → maydonlar yangilanadi, {id, code, verified, points} qaytariladi</li>
     * </ul>
     *
     * @param request Student update request (OLD HEMIS nested format)
     * @return {id, code, verified, points} yoki transfer holatda bo'sh body
     */
    @PreAuthorize("hasAuthority('students.edit')")
    @PostMapping("/update")
    @Operation(summary = "Talabani o'zgartirish", description = "Talaba ma'lumotlarini yangilash (transfer yoki data update). Transfer bo'lmasa {id, code, verified, points} qaytaradi.")
    public ResponseEntity<?> update(@RequestBody Map<String, Object> request) {
        // PII safety: never log full request body (contains PINFL, passport, parent_phone).
        log.info("[CUBA Service] student/update: keys={}", request.keySet());
        Object result = studentService.updateStudent(request);

        // OLD-HEMIS returns 200 OK with empty body when no transfer conditions met
        // (NOT 204 No Content - that breaks compatibility!)
        if (result == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Validate student status
     *
     * <p><strong>URL:</strong> {@code GET /services/student/validate}</p>
     *
     * @param data Validation data (PINFL or Passport serial number)
     * @return Student validation status
     */
    @PreAuthorize("hasAuthority('students.view')")
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
        // PII safety: input may be PINFL or passport — mask via Pinfl helper (graceful for non-PINFL).
        log.info("[CUBA Service] student/validate: data={}",
                uz.hemis.common.vo.Pinfl.maskOrEmpty(data));
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
    @PreAuthorize("hasAuthority('students.edit')")
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
        // PII safety: never log full request body (contains PINFL).
        log.info("[CUBA Service] student/gpa: keys={}", request.keySet());

        // Get current username for createdBy field
        String username = getCurrentUsername();

        try {
            Map<String, Object> result = studentGpaService.upsert(request, username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("[CUBA Service] student/gpa: Invalid request - {}", e.getMessage());
            return ResponseEntity.badRequest().body(LegacyResponseHelper.errorMap("Bad Request", e.getMessage()));
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

    // =====================================================
    // OLD-HEMIS Service Endpoints (16 endpoints)
    // =====================================================

    /**
     * Get student by PINFL (flat service format)
     * OLD-HEMIS: StudentServiceBean.get(pinfl) — returns {success, data}
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/get")
    @Operation(summary = "Talaba ma'lumotlari (PINFL)", description = "PINFL bo'yicha talaba ma'lumotlarini olish (status: 11, 16)")
    public ResponseEntity<?> get(
            @Parameter(description = "PINFL") @RequestParam String pinfl) {
        log.info("[CUBA Service] student/get: pinfl={}", LogSafe.pinfl(pinfl));
        Map<String, Object> data = studentService.getByPinflFlat(pinfl);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        if (data == null) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
        } else {
            response.put("success", true);
            response.put("code", "ok");
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get active student by PINFL
     * OLD-HEMIS: StudentServiceBean.getActive(pinfl) — returns {success, code, data}
     * Note: adds decreeInfoNumber IS NOT NULL check
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/getActive")
    @Operation(summary = "Aktiv talaba (PINFL)", description = "PINFL bo'yicha aktiv talaba ma'lumotlarini olish")
    public ResponseEntity<?> getActive(
            @Parameter(description = "PINFL") @RequestParam String pinfl) {
        log.info("[CUBA Service] student/getActive: pinfl={}", LogSafe.pinfl(pinfl));
        Map<String, Object> data = studentService.getActiveFlat(pinfl);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        if (data == null) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
        } else {
            response.put("success", true);
            response.put("code", "ok");
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get student by code (ID)
     * OLD-HEMIS: StudentServiceBean.getById(id) — returns {success, code, data}
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/getById")
    @Operation(summary = "Talaba (kod bo'yicha)", description = "Talaba kodi bo'yicha ma'lumotlarini olish (status: 11)")
    public ResponseEntity<?> getByIdCode(
            @Parameter(description = "Talaba kodi") @RequestParam String id) {
        log.info("[CUBA Service] student/getById: id={}", id);
        Map<String, Object> data = studentService.getByCodeFlat(id);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        if (data == null) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
        } else {
            response.put("success", true);
            response.put("code", "ok");
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get doctoral student by PINFL
     * OLD-HEMIS: StudentServiceBean.getDoctoral(pinfl) — returns {success, code, data}
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/getDoctoral")
    @Operation(summary = "Doktorant talaba (PINFL)", description = "PINFL bo'yicha doktorant talaba ma'lumotlarini olish")
    public ResponseEntity<?> getDoctoral(
            @Parameter(description = "PINFL") @RequestParam String pinfl) {
        log.info("[CUBA Service] student/getDoctoral: pinfl={}", LogSafe.pinfl(pinfl));
        Map<String, Object> data = studentService.getDoctoralFlat(pinfl);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        if (data == null) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
        } else {
            response.put("success", true);
            response.put("code", "ok");
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get student with fallback status chain
     * OLD-HEMIS: StudentServiceBean.getWithStatus(pinfl) — returns {success, code, data}
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/getWithStatus")
    @Operation(summary = "Talaba (status bilan)", description = "PINFL bo'yicha talaba - status '11' → oxirgi yozuv → seriya raqam")
    public ResponseEntity<?> getWithStatus(
            @Parameter(description = "PINFL") @RequestParam String pinfl) {
        log.info("[CUBA Service] student/getWithStatus: pinfl={}", LogSafe.pinfl(pinfl));
        Map<String, Object> data = studentService.getWithStatusFlat(pinfl);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        if (data == null) {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
        } else {
            response.put("success", true);
            response.put("code", "ok");
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Test get - billing format
     * OLD-HEMIS: StudentServiceBean.testGet(pinfl)
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/testGet")
    @Operation(summary = "Test talaba (billing format)", description = "Billing API formati uchun talaba ma'lumotlari")
    public ResponseEntity<?> testGet(
            @Parameter(description = "PINFL") @RequestParam String pinfl) {
        log.info("[CUBA Service] student/testGet: pinfl={}", LogSafe.pinfl(pinfl));
        Map<String, Object> result = studentService.testGetFlat(pinfl);
        return ResponseEntity.ok(result);
    }

    /**
     * Get students by university (paginated)
     * OLD-HEMIS: StudentServiceBean.students(university, limit, offset)
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/students")
    @Operation(summary = "Talabalar ro'yxati (OTM bo'yicha)", description = "OTM kodi bo'yicha talabalar ro'yxati (max 1000)")
    public ResponseEntity<?> students(
            @Parameter(description = "OTM kodi") @RequestParam String university,
            @Parameter(description = "Limit (max 1000)") @RequestParam(defaultValue = "100") int limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") int offset) {
        log.info("[CUBA Service] student/students: university={}, limit={}, offset={}", university, limit, offset);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        if (limit > 1000) {
            response.put("success", false);
            response.put("code", "bad_request");
            response.put("data", "Max limit is 1000");
            return ResponseEntity.ok(response);
        }
        Map<String, Object> result = studentService.getStudentsByUniversityFlatWithCount(university, limit, offset);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        if (data != null && !data.isEmpty()) {
            response.put("success", true);
            response.put("code", "ok");
            response.put("count", result.get("count"));
            response.put("limit", limit);
            response.put("offset", offset);
            response.put("data", data);
        } else {
            response.put("success", false);
            response.put("code", "not_found");
            response.put("data", "Student not found!");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get Tashkent students (paginated)
     * OLD-HEMIS: StudentServiceBean.tashkentStudents(limit, offset)
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/tashkentStudents")
    @Operation(summary = "Toshkent talablari", description = "Toshkent shahridagi OTM talablari (max 1000)")
    public ResponseEntity<?> tashkentStudents(
            @Parameter(description = "Limit (max 1000)") @RequestParam(defaultValue = "100") int limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") int offset) {
        log.info("[CUBA Service] student/tashkentStudents: limit={}, offset={}", limit, offset);
        if (limit > 1000) {
            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("success", false);
            response.put("code", "bad_request");
            response.put("data", "Max limit is 1000");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(studentService.getTashkentStudentsResult(limit, offset));
    }

    /**
     * Check expelled students by PINFLs
     * OLD-HEMIS: StudentServiceBean.isExpel(pinfls)
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/isExpel")
    @Operation(summary = "Chetlatilgan talabalar tekshirish", description = "PINFL lar bo'yicha chetlatilgan talabalarni tekshirish")
    public ResponseEntity<?> isExpel(
            @Parameter(description = "PINFL lar (vergul bilan ajratilgan)") @RequestParam String pinfls) {
        if (pinfls == null || pinfls.isBlank()) {
            return ResponseEntity.badRequest().body(LegacyResponseHelper.failureMap("pinfls required"));
        }
        String[] pinflArray = java.util.Arrays.stream(pinfls.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        log.info("[CUBA Service] student/isExpel: {} PINFL qabul qilindi", pinflArray.length);
        if (pinflArray.length == 0) {
            return ResponseEntity.badRequest().body(LegacyResponseHelper.failureMap("No valid PINFLs provided"));
        }
        Map<String, Object> result = studentService.isExpel(pinflArray);
        return ResponseEntity.ok(result);
    }

    /**
     * Check students against MVD data
     * OLD-HEMIS: StudentServiceBean.check()
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/check")
    @Operation(summary = "Talabalar MVD tekshirish", description = "Barcha talabalarni MVD ma'lumotlari bilan solishtirish")
    public ResponseEntity<?> check() {
        log.info("[CUBA Service] student/check");
        Map<String, Object> result = studentService.checkStudents();
        return ResponseEntity.ok(result);
    }

    /**
     * Check scholarship eligibility
     * OLD-HEMIS: StudentServiceBean.checkScholarship(tin, pinfls)
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/checkScholarship")
    @Operation(summary = "Stipendiya tekshirish", description = "PostgreSQL stipend_check() funktsiyasi orqali tekshirish")
    public ResponseEntity<?> checkScholarship(
            @Parameter(description = "INN (TIN)") @RequestParam String tin,
            @Parameter(description = "PINFL lar (JSON array)") @RequestParam String pinfls) {
        // PINFL JSON array — log only count to avoid PII leak.
        log.info("[CUBA Service] student/checkScholarship: tin={}, pinfls.length={}",
                tin, pinfls != null ? pinfls.length() : 0);
        // OLD-HEMIS (CUBA): pinfls is a JSON array parameter like ["pinfl1","pinfl2"]
        String trimmed = pinfls.trim();
        if (!trimmed.startsWith("[")) {
            // OLD-HEMIS compatible: always return 200 with error in body
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", "Invalid parameter value");
            error.put("details", "Invalid parameter value for pinfls");
            return ResponseEntity.ok(error);
        }
        // Parse JSON array
        String[] pinflArray;
        try {
            pinflArray = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(trimmed, String[].class);
        } catch (Exception e) {
            // OLD-HEMIS compatible: always return 200 with error in body
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", "Invalid parameter value");
            error.put("details", "Invalid parameter value for pinfls");
            return ResponseEntity.ok(error);
        }
        Map<String, Object> result = studentService.checkScholarshipNative(tin, pinflArray);
        return ResponseEntity.ok(result);
    }

    /**
     * Tashkent statistics: by payment form
     * OLD-HEMIS: StudentServiceBean.byTashkentAndPaymentForm()
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/byTashkentAndPaymentForm")
    @Operation(summary = "Toshkent - to'lov shakli", description = "Toshkent shahri talablari statistikasi (to'lov shakli kesimida)")
    public ResponseEntity<?> byTashkentAndPaymentForm() {
        log.info("[CUBA Service] student/byTashkentAndPaymentForm");
        return ResponseEntity.ok(studentService.byTashkentAndPaymentForm());
    }

    /**
     * Tashkent statistics: by region/district
     * OLD-HEMIS: StudentServiceBean.byTashkentAndRegionDistrict()
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/byTashkentAndRegionDistrict")
    @Operation(summary = "Toshkent - viloyat/tuman", description = "Toshkent shahri talablari statistikasi (viloyat va tumanlar kesimida)")
    public ResponseEntity<?> byTashkentAndRegionDistrict() {
        log.info("[CUBA Service] student/byTashkentAndRegionDistrict");
        return ResponseEntity.ok(studentService.byTashkentAndRegionDistrict());
    }

    /**
     * Tashkent statistics: by region/district/education type
     * OLD-HEMIS: StudentServiceBean.byTashkentAndRegionDistrictAndEduType()
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/byTashkentAndRegionDistrictAndEduType")
    @Operation(summary = "Toshkent - viloyat/tuman/ta'lim turi", description = "Toshkent shahri talablari (viloyat, tuman va ta'lim turi kesimida)")
    public ResponseEntity<?> byTashkentAndRegionDistrictAndEduType() {
        log.info("[CUBA Service] student/byTashkentAndRegionDistrictAndEduType");
        return ResponseEntity.ok(studentService.byTashkentAndRegionDistrictAndEduType());
    }

    /**
     * Tashkent statistics: by faculty/course
     * OLD-HEMIS: StudentServiceBean.byTashkentAndFacultyAndCourse()
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/byTashkentAndFacultyAndCourse")
    @Operation(summary = "Toshkent - fakultet/kurs", description = "Toshkent shahri talablari (fakultet va kurslar kesimida)")
    public ResponseEntity<?> byTashkentAndFacultyAndCourse() {
        log.info("[CUBA Service] student/byTashkentAndFacultyAndCourse");
        return ResponseEntity.ok(studentService.byTashkentAndFacultyAndCourse());
    }

    /**
     * Tashkent statistics: by education form/type/gender
     * OLD-HEMIS: StudentServiceBean.byTashkentAndEduFormTypeAndGender()
     */
    @PreAuthorize("hasAuthority('students.view')")
    @GetMapping("/byTashkentAndEduFormTypeAndGender")
    @Operation(summary = "Toshkent - shakl/turi/jins", description = "Toshkent shahri talablari (ta'lim shakli, turi va jinslar kesimida)")
    public ResponseEntity<?> byTashkentAndEduFormTypeAndGender() {
        log.info("[CUBA Service] student/byTashkentAndEduFormTypeAndGender");
        return ResponseEntity.ok(studentService.byTashkentAndEduFormTypeAndGender());
    }

}
