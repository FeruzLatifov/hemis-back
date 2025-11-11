package uz.hemis.app.controller;

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

@Tag(name = "User Management")
@RestController
@RequestMapping("/app/rest/v2/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;

    @Operation(
            summary = "Get current user",
            description = "Authenticated user'ning ma'lumotlarini olish",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        log.info("GET /app/rest/v2/user/me - username: {}", authentication.getName());

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId().toString());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("roles", user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().stream().map(Role::getCode).toArray(String[]::new)
                : new String[0]);
        response.put("university", user.getUniversity());
        response.put("enabled", user.getEnabled());
        response.put("accountNonLocked", user.getAccountNonLocked());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validate token",
            description = "Token'ning amal qilish muddatini tekshirish",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
        log.info("GET /app/rest/v2/user/validate - username: {}", authentication.getName());

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("userId", user.getId().toString());
        response.put("username", user.getUsername());

        return ResponseEntity.ok(response);
    }
}

