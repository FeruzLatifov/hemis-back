package uz.hemis.api.legacy.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.User;
import uz.hemis.service.legacy.UserLegacyService;

import java.util.Map;

@Tag(name = "01.Token, Foydalanuvchilar")
@RestController
@RequestMapping("/app/rest/v2/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserLegacyService userService;

    @Operation(
            summary = "Get current user",
            description = "Authenticated user'ning ma'lumotlarini olish",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        log.info("GET /app/rest/v2/user/me - username: {}", authentication.getName());

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(userService.toUserMeResponse(user));
    }

    @Operation(
            summary = "Validate token",
            description = "Token'ning amal qilish muddatini tekshirish",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
        log.info("GET /app/rest/v2/user/validate - username: {}", authentication.getName());

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(userService.toValidateResponse(user));
    }
}

