package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User creation request")
public class UserCreateRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, digits, underscores, dots, and hyphens")
    @Schema(description = "Login username (unique)", example = "john_doe")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, and one special character")
    @Schema(description = "Password (will be BCrypt hashed)", example = "P@ssw0rd!")
    private String password;

    @Size(max = 255, message = "Full name must be at most 255 characters")
    @Schema(description = "Full name", example = "John Doe", nullable = true)
    private String fullName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    @Schema(description = "Email address", example = "john@example.com", nullable = true)
    private String email;

    @Size(max = 50, message = "Phone must be at most 50 characters")
    @Pattern(regexp = "^\\+998[0-9]{9}$", message = "Phone number must be in format +998XXXXXXXXX")
    @Schema(description = "Phone number", example = "+998901234567", nullable = true)
    private String phone;

    @Size(max = 255, message = "Entity code must be at most 255 characters")
    @Schema(description = "University entity code (null for system admins)", example = "TATU", nullable = true)
    private String entityCode;

    @NotEmpty(message = "At least one role is required")
    @Schema(description = "Role IDs to assign")
    private Set<UUID> roleIds;

    @Schema(description = "Whether the user is enabled", example = "true")
    @Builder.Default
    private Boolean enabled = true;
}
