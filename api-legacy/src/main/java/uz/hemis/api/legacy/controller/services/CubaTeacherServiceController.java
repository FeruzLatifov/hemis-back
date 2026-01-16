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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.TeacherIdRequest;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.service.TeacherService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * CUBA Teacher Service Controller - OLD-HEMIS Compatible
 *
 * <p>Preserves exact URLs from old HEMIS CUBA platform for backward compatibility</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/teacher/*}</p>
 *
 * <p><strong>Methods:</strong></p>
 * <ul>
 *   <li>id - O'qituvchi Universal ID sini olish</li>
 *   <li>addJob - Xodimga lavozim qo'shish</li>
 *   <li>get - PINFL bo'yicha o'qituvchi ma'lumotlarini olish</li>
 *   <li>getById - UUID bo'yicha o'qituvchi ma'lumotlarini olish</li>
 * </ul>
 *
 * @since 2.0.0
 * @see TeacherService
 */
@RestController
@RequestMapping("/app/rest/v2/services/teacher")
@Tag(name = "05.O'qituvchi", description = "O'qituvchi ma'lumotlari bilan ishlash xizmatlari (CUBA API)")
@RequiredArgsConstructor
@Slf4j
public class CubaTeacherServiceController {

    private final TeacherService teacherService;
    private final UserRepository userRepository;

    /**
     * O'qituvchi Universal ID sini olish
     *
     * <p><strong>URL:</strong> {@code POST /app/rest/v2/services/teacher/id}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - Mavjud o'qituvchini qaytaradi yoki yangi yaratadi</p>
     *
     * <p><strong>Logic:</strong></p>
     * <ol>
     *   <li>O'zbekiston fuqarosi (citizenship=11): PINFL bo'yicha qidirish</li>
     *   <li>Chet el fuqarosi: serialNumber + citizenship bo'yicha qidirish</li>
     *   <li>Topilsa: mavjud o'qituvchi qaytariladi (is_new=false)</li>
     *   <li>Topilmasa: yangi o'qituvchi yaratiladi (is_new=true)</li>
     * </ol>
     *
     * <p><strong>Request format:</strong></p>
     * <pre>
     * {
     *   "data": {
     *     "citizenship": "11",
     *     "pinfl": "32305967340015",
     *     "serial": "XX1111117",
     *     "year": "2019",
     *     "gender": "11",
     *     "main_university": "380"
     *   }
     * }
     * </pre>
     *
     * <p><strong>Response format:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "is_new": false,
     *   "teacher": {
     *     "_entityName": "hemishe_ETeacher",
     *     "id": "uuid",
     *     "code": "3801911001",
     *     "pinfl": "32305967340015",
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param requestBody Request body with "data" wrapper
     * @return O'qituvchi ma'lumotlari va is_new flag
     */
    @PostMapping("/id")
    @Operation(
            summary = "O'qituvchi ID sini olish",
            description = """
                O'qituvchi Universal ID sini olish yoki yangi yaratish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** POST /app/rest/v2/services/teacher/id
                **Auth:** Bearer token (required)

                **Logic:**
                1. O'zbekiston fuqarosi (citizenship=11): PINFL bo'yicha qidiradi
                2. Chet el fuqarosi: passport + citizenship bo'yicha qidiradi
                3. Topilsa: mavjud o'qituvchi qaytariladi (is_new=false)
                4. Topilmasa: yangi o'qituvchi yaratiladi (is_new=true)

                **ID Format:** {universityCode}{YY}{gender}{sequence}
                Example: "3801911001" = university 380, year 2019, male (11), sequence 001
                """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "O'qituvchi qidiruv/yaratish ma'lumotlari",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TeacherIdRequestWrapper.class),
                    examples = @ExampleObject(
                            name = "O'qituvchi ID olish",
                            value = """
                                {
                                  "data": {
                                    "citizenship": "11",
                                    "pinfl": "32305967340015",
                                    "serial": "XX1111117",
                                    "year": "2019",
                                    "gender": "11"
                                  }
                                }
                                """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - O'qituvchi ma'lumotlari qaytarildi"),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov parametrlari"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> getTeacherId(
            @RequestBody Map<String, Object> requestBody
    ) {
        log.info("POST /app/rest/v2/services/teacher/id - request: {}", requestBody);

        // Extract "data" wrapper (CUBA format)
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) requestBody.get("data");
        if (data == null) {
            data = requestBody; // Fallback: direct request without wrapper
        }

        // Parse request
        TeacherIdRequest request = new TeacherIdRequest();
        request.setCitizenship((String) data.get("citizenship"));
        request.setPinfl((String) data.get("pinfl"));
        request.setSerial((String) data.get("serial"));
        request.setYear((String) data.get("year"));
        request.setGender((String) data.get("gender"));
        request.setMainUniversity((String) data.get("main_university"));

        // Get university code from authenticated user
        String universityCode = getUniversityCodeFromContext();
        if (universityCode == null) {
            log.error("University code not found in security context");
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", "University code not found");
            return ResponseEntity.badRequest().body(error);
        }

        log.info("Processing teacher ID request - University: {}, PINFL: {}, Serial: {}",
                universityCode, request.getPinfl(), request.getSerial());

        Map<String, Object> result = teacherService.generateTeacherId(request, universityCode);
        return ResponseEntity.ok(result);
    }

    /**
     * Extract university code from security context
     */
    private String getUniversityCodeFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        // JWT token dan username olish
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaimAsString("username");
            if (username == null) {
                username = jwt.getSubject();
            }

            // User dan university olish
            if (username != null) {
                try {
                    UUID userId = UUID.fromString(jwt.getSubject());
                    var userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent() && userOpt.get().getUniversity() != null) {
                        return userOpt.get().getUniversity().getCode();
                    }
                } catch (Exception e) {
                    log.debug("Could not get university from user: {}", e.getMessage());
                }
            }
        }

        // Fallback: default test university for development
        log.warn("Using default university code for development");
        return "520";
    }

    /**
     * Xodimga yangi lavozim qo'shish
     *
     * <p><strong>URL:</strong> {@code POST /app/rest/v2/services/teacher/addJob}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - Xodimga yangi ish joyini qo'shish</p>
     *
     * <p><strong>Logic:</strong></p>
     * <ol>
     *   <li>Employee (Teacher) ni ID bo'yicha topish</li>
     *   <li>Agar topilmasa, soft-deleted bo'lsa restore qilish</li>
     *   <li>Agar xodim asosiy shtat birligida ishlasa va yangi job ham asosiy shtat bo'lsa - xato</li>
     *   <li>Yangi job yaratish va saqlash</li>
     * </ol>
     *
     * <p><strong>Request format:</strong></p>
     * <pre>
     * {
     *   "job": {
     *     "employee": {"id": "uuid"},
     *     "university": {"code": "401"},
     *     "department": {"code": "401-102"},
     *     "employeeForm": {"code": "11"},
     *     "employeeStatus": {"code": "11"},
     *     "employeeType": {"code": "11"},
     *     "employeeRate": {"code": "13"},
     *     "employeePosition": {"code": "11"},
     *     "jobStartDate": "2020-01-06",
     *     "jobEndDate": "2023-08-01"
     *   }
     * }
     * </pre>
     *
     * <p><strong>Response format:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Xodim lavozimi saqlandi",
     *   "data": {...}
     * }
     * </pre>
     *
     * @param requestBody Request body with "job" wrapper
     * @return Success status and message
     */
    @PostMapping("/addJob")
    @Tag(name = "06.Xodim lavozimlari", description = "Xodim lavozimlari bilan ishlash")
    @Operation(
            summary = "Xodim ish joyini yaratish - YANGI",
            description = """
                Xodimga yangi lavozim yoki ish joyini qo'shish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** POST /app/rest/v2/services/teacher/addJob
                **Auth:** Bearer token (required)

                **Logic:**
                1. Employee (Teacher) ni ID bo'yicha topish
                2. Agar topilmasa, soft-deleted bo'lsa restore qilish
                3. Agar xodim asosiy shtat birligida (employeeForm=11, employeeStatus=11) ishlasa va yangi job ham asosiy shtat bo'lsa - xato qaytariladi
                4. Yangi job yaratish va saqlash

                **Employee Form kodlari:**
                - 11 = Asosiy shtat
                - 12 = O'rindoshlik (ichki)
                - 13 = O'rindoshlik (tashqi)
                - 14 = Soatbay

                **Employee Status kodlari:**
                - 11 = Ishlamoqda
                - 12 = Ta'tilda
                - 13 = Ishdan bo'shatilgan
                """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Lavozim ma'lumotlari (job wrapper bilan)",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AddJobRequestWrapper.class),
                    examples = @ExampleObject(
                            name = "Xodim lavozimi qo'shish",
                            value = """
                                {
                                  "job": {
                                    "employee": {"id": "00000000-0000-0000-0000-000000000000"},
                                    "university": {"code": "401"},
                                    "department": {"code": "401-102"},
                                    "employeeForm": {"code": "11"},
                                    "employeeStatus": {"code": "11"},
                                    "employeeType": {"code": "11"},
                                    "employeeRate": {"code": "13"},
                                    "employeePosition": {"code": "11"},
                                    "jobStartDate": "2020-01-06",
                                    "jobEndDate": "2023-08-01"
                                  }
                                }
                                """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - Lavozim qo'shildi yoki xato xabari qaytarildi"),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov parametrlari"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    @Transactional
    public ResponseEntity<Map<String, Object>> addJob(
            @RequestBody Map<String, Object> requestBody
    ) {
        // Security: Foydalanuvchining universitetini olish
        String userUniversityCode = getUniversityCodeFromContext();
        log.info("POST /app/rest/v2/services/teacher/addJob - university: {}", userUniversityCode);

        // So'rovdagi universitetni tekshirish
        @SuppressWarnings("unchecked")
        Map<String, Object> job = (Map<String, Object>) requestBody.get("job");
        if (job == null) {
            job = requestBody;
        }

        String requestedUniversity = extractUniversityCode(job);

        // Agar so'rovda universitet ko'rsatilmagan bo'lsa - avtomatik belgilash
        if (requestedUniversity == null || requestedUniversity.isEmpty()) {
            setUniversityCode(job, userUniversityCode);
            log.debug("Auto-set university to: {}", userUniversityCode);
        }
        // Agar boshqa universitet ko'rsatilgan bo'lsa - rad etish
        else if (!requestedUniversity.equals(userUniversityCode)) {
            log.warn("Attempted to add job for different university: requested={}, user={}",
                    requestedUniversity, userUniversityCode);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "Boshqa universitetga lavozim qo'shish mumkin emas!");
            return ResponseEntity.ok(error);
        }

        Map<String, Object> result = teacherService.addJob(requestBody);
        return ResponseEntity.ok(result);
    }

    /**
     * So'rovdan universitet kodini olish
     */
    @SuppressWarnings("unchecked")
    private String extractUniversityCode(Map<String, Object> job) {
        Object university = job.get("university");
        if (university == null) {
            return null;
        }
        if (university instanceof String s) {
            return s.isEmpty() ? null : s;
        }
        if (university instanceof Map) {
            Map<String, Object> uniMap = (Map<String, Object>) university;
            Object code = uniMap.get("code");
            if (code != null) {
                return code.toString();
            }
        }
        return null;
    }

    /**
     * So'rovga universitet kodini qo'yish
     */
    @SuppressWarnings("unchecked")
    private void setUniversityCode(Map<String, Object> job, String universityCode) {
        Map<String, Object> uniMap = new LinkedHashMap<>();
        uniMap.put("code", universityCode);
        job.put("university", uniMap);
    }

    /**
     * Swagger schema uchun wrapper class - Teacher ID
     */
    @Schema(description = "O'qituvchi ID so'rovi (CUBA format)")
    public static class TeacherIdRequestWrapper {
        @Schema(description = "So'rov ma'lumotlari", required = true)
        public TeacherIdRequest data;
    }

    /**
     * Swagger schema uchun wrapper class - Add Job
     */
    @Schema(description = "Xodim lavozimi so'rovi (CUBA format)")
    public static class AddJobRequestWrapper {
        @Schema(description = "Lavozim ma'lumotlari", required = true)
        public AddJobRequest job;
    }

    @Schema(description = "Lavozim ma'lumotlari")
    public static class AddJobRequest {
        @Schema(description = "Xodim ID si", example = "{\"id\": \"00000000-0000-0000-0000-000000000000\"}")
        public Map<String, String> employee;

        @Schema(description = "Universitet kodi", example = "{\"code\": \"401\"}")
        public Map<String, String> university;

        @Schema(description = "Bo'lim kodi", example = "{\"code\": \"401-102\"}")
        public Map<String, String> department;

        @Schema(description = "Shtat shakli kodi (11=asosiy, 12=ichki o'rindoshlik)", example = "{\"code\": \"11\"}")
        public Map<String, String> employeeForm;

        @Schema(description = "Ish holati kodi (11=ishlamoqda)", example = "{\"code\": \"11\"}")
        public Map<String, String> employeeStatus;

        @Schema(description = "Xodim turi kodi", example = "{\"code\": \"11\"}")
        public Map<String, String> employeeType;

        @Schema(description = "Stavka kodi", example = "{\"code\": \"13\"}")
        public Map<String, String> employeeRate;

        @Schema(description = "Lavozim kodi", example = "{\"code\": \"11\"}")
        public Map<String, String> employeePosition;

        @Schema(description = "Ish boshlash sanasi", example = "2020-01-06")
        public String jobStartDate;

        @Schema(description = "Ish tugash sanasi", example = "2023-08-01")
        public String jobEndDate;
    }
}
