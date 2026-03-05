package uz.hemis.api.legacy.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.dto.LegacyUserInfoResponse;
import uz.hemis.domain.entity.User;
import uz.hemis.service.legacy.UserLegacyService;

/**
 * Legacy User Controller - OLD-HEMIS Compatibility
 *
 * <p><strong>OLD-HEMIS URL:</strong> GET /app/rest/user/info</p>
 *
 * <p>Response format matches old-hemis EXACT structure (NO wrapper):
 * Direct UserInfo object return</p>
 */
@Tag(
    name = "01.Token, Foydalanuvchilar",
    description = "OAuth2 autentifikatsiya - token olish, yangilash. " +
                  "Old-hemis loyihasidagi foydalanuvchilar uchun uzluksiz xizmat."
)
@RestController
@RequestMapping("/app/rest")  // ✅ Base path (v2 will be in @GetMapping)
@RequiredArgsConstructor
@Slf4j
public class LegacyUserInfoController {

    private final UserLegacyService userService;

    /**
     * OLD-HEMIS Compatible User Info Endpoint
     *
     * <p><strong>URL:</strong> GET /app/rest/user/info (v2 yo'q!)</p>
     *
     * <p><strong>Response format (NO WRAPPER):</strong></p>
     * <pre>
     * {
     *   "id": "uuid",
     *   "login": "feruz",
     *   "name": "feruz ",
     *   "firstName": "feruz",
     *   "middleName": null,
     *   "lastName": null,
     *   "position": null,
     *   "email": null,
     *   "timeZone": null,
     *   "language": "ru",
     *   "_instanceName": "feruz [feruz]",
     *   "locale": "uz",
     *   "university": "TATU"
     * }
     * </pre>
     */
    @Operation(
        summary = "Joriy foydalanuvchi ma'lumotlari",
        description = """
            Hozirgi vaqtda tizimga kirgan foydalanuvchi ma'lumotlarini olish.

            **OLD-HEMIS compatible** - /app/rest/user/info endpoint.

            **Response format:**
            Direct UserInfo object (NO wrapper) - matches old-hemis exactly.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Foydalanuvchi ma'lumotlari muvaffaqiyatli olindi",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LegacyUserInfoResponse.UserData.class),
                examples = @ExampleObject(value = """
                    {
                      "id": "00000000-0000-0000-0000-000000000000",
                      "login": "username",
                      "name": "User Full Name",
                      "firstName": "User",
                      "middleName": "Middle",
                      "lastName": "Name",
                      "position": "Position",
                      "email": "user@example.com",
                      "timeZone": "Asia/Tashkent",
                      "language": "ru",
                      "_instanceName": "User [username]",
                      "locale": "ru"
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi - token noto'g'ri yoki muddati o'tgan")
    })
    /**
     * OLD-HEMIS URL: /app/rest/v2/userInfo
     *
     * Support both variations for backward compatibility:
     * 1. /app/rest/v2/userInfo (primary - old-hemis URL)
     * 2. /app/rest/user/info (alternative)
     */
    @GetMapping({"/v2/userInfo", "/user/info"})
    public ResponseEntity<LegacyUserInfoResponse.UserData> getUserInfo(Authentication authentication) {
        log.info("GET /app/rest/user/info - principal: {}", authentication.getName());

        try {
            // authentication.getName() returns userId (UUID from JWT 'sub' claim)
            java.util.UUID userId = java.util.UUID.fromString(authentication.getName());
            User user = userService.findByIdWithUniversity(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Get university name for 'name' and '_instanceName' fields
            String universityName = userService.getUniversityName(user);

            // Build user data (old-hemis format - NO wrapper!)
            String universityCode = user.getUniversity() != null ? user.getUniversity().getCode() : null;
            LegacyUserInfoResponse.UserData userData = LegacyUserInfoResponse.UserData.builder()
                    .id(user.getId().toString())
                    .login(user.getUsername())
                    .name(universityName)
                    .firstName(user.getFirstName())
                    .middleName(user.getMiddleName())
                    .lastName(user.getLastName())
                    .position(user.getPosition())
                    .email(user.getEmail())
                    .timeZone(user.getTimeZone())
                    .language(user.getLanguage() != null ? user.getLanguage() : "uz")
                    .instanceName(userService.buildInstanceName(user, universityName))
                    .locale(user.getLocale() != null ? user.getLocale() : "uz")
                    .university(universityCode)
                    .build();

            log.info("Returning user info for: {} (university: {})", user.getUsername(), universityName);

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            log.error("Error fetching user info: ", e);
            throw new RuntimeException("Error fetching user info: " + e.getMessage(), e);
        }
    }
}
