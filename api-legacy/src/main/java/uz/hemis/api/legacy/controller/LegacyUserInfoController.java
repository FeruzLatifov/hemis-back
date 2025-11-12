package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Role;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Legacy User Controller - OLD-HEMIS Compatibility
 * 
 * Provides /app/rest/user/info endpoint (without v2) for backward compatibility
 */
@Tag(name = "01. Authentication", description = "Autentifikatsiya va foydalanuvchi ma'lumotlari")
@RestController
@RequestMapping("/app/rest/user")
@RequiredArgsConstructor
@Slf4j
public class LegacyUserInfoController {

    private final UserRepository userRepository;

    /**
     * OLD-HEMIS Compatible User Info Endpoint
     * 
     * URL: GET /app/rest/user/info (v2 yo'q!)
     * 
     * Response format matches old-hemis UserInfo structure
     */
    @Operation(
            summary = "Joriy foydalanuvchi ma'lumotlari",
            description = "Authenticated user'ning to'liq ma'lumotlarini olish.\n\n" +
                    "**OLD-HEMIS compatible** - /app/rest/user/info endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        log.info("GET /app/rest/user/info - username: {}", authentication.getName());

        try {
            User user = userRepository.findByUsernameWithRoles(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // OLD-HEMIS UserInfo format (CUBA compatible)
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId() != null ? user.getId().toString() : "");
            response.put("username", user.getUsername() != null ? user.getUsername() : "");
            response.put("name", user.getFullName() != null ? user.getFullName() : user.getUsername());
            response.put("email", user.getEmail() != null ? user.getEmail() : "");
            response.put("locale", "uz");  // CUBA format
            
            // Roles - eagerly fetched so no lazy loading issues
            if (user.getRoleSet() != null && !user.getRoleSet().isEmpty()) {
                response.put("roles", user.getRoleSet().stream()
                    .map(Role::getCode)
                    .collect(Collectors.toList()));
            }
            
            // University info (simple string in this version)
            if (user.getEntityCode() != null && !user.getEntityCode().isEmpty()) {
                response.put("university", user.getEntityCode());
            }

            log.info("Returning user info for: {}", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching user info: ", e);
            throw new RuntimeException("Error fetching user info: " + e.getMessage(), e);
        }
    }
}
