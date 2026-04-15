package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.finance.ContractStatisticsService;
import uz.hemis.service.student.StudentService;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Student Extra Service Controller
 *
 * <p>checkScholarship va contractStatistics endpointlari uchun alohida controller.
 * Bu methodlar "04.Talaba" tag'iga tegishli emas — o'z tag'larida ko'rinadi:</p>
 * <ul>
 *   <li>checkScholarship2 → 58.UzASBO</li>
 *   <li>contractStatistics → 36.Shartnoma statistikasi</li>
 * </ul>
 *
 * <p>Class darajasida @Tag("04.Talaba") yo'q — springdoc-openapi class tag'ini
 * method-level @Operation(tags) ga qo'shib qo'ymasligini ta'minlaydi.</p>
 */
@RestController
@RequestMapping("/app/rest/v2/services/student")
@RequiredArgsConstructor
@Slf4j
public class StudentExtraServiceController {

    private final StudentService studentService;
    private final ContractStatisticsService contractStatisticsService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkScholarship2")
    @Operation(
            summary = "Scholarship check",
            description = "Talabaning stipendiya olish huquqini tekshirish",
            tags = {"58.UzASBO"}
    )
    public ResponseEntity<?> checkScholarship(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/checkScholarship2: request={}", request);
        return ResponseEntity.ok(studentService.checkScholarship(request));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/contractStatistics")
    @Operation(
            summary = "Shartnoma statistikasini yuborish",
            tags = {"36.Shartnoma statistikasi"},
            description = """
                Shartnoma statistika ma'lumotlarini bazaga saqlash.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** POST /app/rest/v2/services/student/contractStatistics
                **Auth:** Bearer token (required)
                **Content-Type:** application/json

                **Request Body:**
                - contractStatistics.university.code: OTM kodi (required)
                - contractStatistics.educationYear.code: Ta'lim yili kodi (masalan: "2021")
                - contractStatistics.educationType.code: Ta'lim turi kodi
                - contractStatistics.educationForm.code: Ta'lim shakli kodi
                - contractStatistics.faculty.code: Fakultet kodi
                - contractStatistics.course.code: Kurs kodi
                - contractStatistics.semester.code: Semestr kodi
                - contractStatistics.date: Sana (YYYY-MM-DD)
                - contractStatistics.dailyCount: Kunlik soni
                - contractStatistics.total: Jami soni
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Statistika saqlandi",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Saqlangan statistika",
                                    value = """
                                        {
                                          "success": true,
                                          "message": "Successfully created!",
                                          "data": {
                                            "_entityName": "hemishe_RContractStatistics",
                                            "id": "35458c9b-1534-1977-79f0-0cfd9289e3e8",
                                            "date": "2021-09-08",
                                            "educationType": {
                                              "_entityName": "hemishe_HEducationType",
                                              "id": "12",
                                              "code": "12"
                                            },
                                            "university": {
                                              "_entityName": "hemishe_EUniversity",
                                              "id": "999",
                                              "code": "999"
                                            },
                                            "version": 1,
                                            "dailyCount": 5,
                                            "total": 5,
                                            "createdBy": "otm351"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri request format"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> contractStatistics(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] student/contractStatistics: request={}", request);
        String username = getCurrentUsername();
        try {
            return ResponseEntity.ok(contractStatisticsService.submitContractStatistics(request, username));
        } catch (Exception e) {
            // OLD-HEMIS compatible: return 200 with {success: false, message: ...}
            log.error("[CUBA Service] contractStatistics error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString("username");
        }
        return "anonymous";
    }
}
