package uz.hemis.api.legacy.controller;

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
import uz.hemis.domain.repository.UserRepository;

/**
 * Legacy User Controller - OLD-HEMIS Compatibility
 *
 * <p><strong>OLD-HEMIS URL:</strong> GET /app/rest/user/info</p>
 *
 * <p>Response format matches old-hemis EXACT structure (NO wrapper):
 * Direct UserInfo object return</p>
 */
@Tag(
    name = "01.Token",
    description = "OAuth2 autentifikatsiya - token olish, yangilash. " +
                  "Old-hemis loyihasidagi foydalanuvchilar uchun uzluksiz xizmat."
)
@RestController
@RequestMapping("/app/rest")  // ✅ Base path (v2 will be in @GetMapping)
@RequiredArgsConstructor
@Slf4j
public class LegacyUserInfoController {

    private final UserRepository userRepository;

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
            // ✅ authentication.getName() returns userId (UUID from JWT 'sub' claim)
            // Convert to UUID and find user by ID with university (eager fetch)
            java.util.UUID userId = java.util.UUID.fromString(authentication.getName());
            User user = userRepository.findByIdWithUniversity(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // ✅ Get university name for 'name' and '_instanceName' fields
            String universityName = getUniversityName(user);

            // Build user data (old-hemis format - NO wrapper!)
            LegacyUserInfoResponse.UserData userData = LegacyUserInfoResponse.UserData.builder()
                    .id(user.getId().toString())
                    .login(user.getUsername())
                    .name(universityName)  // ✅ OLD-HEMIS: university name, not user fullName!
                    .firstName(user.getFirstName())
                    .middleName(user.getMiddleName())
                    .lastName(user.getLastName())
                    .position(user.getPosition())
                    .email(user.getEmail())
                    .timeZone(user.getTimeZone())
                    .language(user.getLanguage() != null ? user.getLanguage() : "uz")  // ✅ OLD-HEMIS: default "uz" not "ru"
                    .instanceName(buildInstanceName(user, universityName))  // ✅ "{universityName} [{login}]"
                    .locale(user.getLocale() != null ? user.getLocale() : "uz")  // ✅ OLD-HEMIS: default "uz" not "ru"
                    // NOTE: "university" field is NOT in old-hemis /app/rest/v2/userInfo response! Removed for 100% compatibility
                    .build();

            log.info("Returning user info for: {} (university: {})", user.getUsername(), universityName);

            // ✅ OLD-HEMIS compatibility: Return UserData directly (NO wrapper)
            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            log.error("Error fetching user info: ", e);
            throw new RuntimeException("Error fetching user info: " + e.getMessage(), e);
        }
    }

    /**
     * Get university name from user
     *
     * <p>OLD-HEMIS format: returns university name or empty string</p>
     * <p>Example: "Jizzax davlat pedagogika universiteti"</p>
     *
     * @param user User entity with university eagerly fetched
     * @return university name or empty string if no university
     */
    private String getUniversityName(User user) {
        if (user.getUniversity() != null && user.getUniversity().getName() != null) {
            return user.getUniversity().getName();
        }
        return "";
    }

    /**
     * Build instance name (old-hemis format)
     *
     * <p>Format: "{universityName} [{login}]"</p>
     * <p>Example: "Jizzax davlat pedagogika universiteti [otm351]"</p>
     *
     * @param user User entity
     * @param universityName University name (or empty string)
     * @return formatted instance name
     */
    private String buildInstanceName(User user, String universityName) {
        if (universityName != null && !universityName.isEmpty()) {
            return universityName + " [" + user.getUsername() + "]";
        }
        // Fallback: username [username] if no university
        return user.getUsername() + " [" + user.getUsername() + "]";
    }
}
