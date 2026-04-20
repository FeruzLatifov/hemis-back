package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.common.validation.ValidPhoneNumber;

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
    @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
    @Schema(description = "Password (will be BCrypt hashed)", example = "secret")
    private String password;

    @Size(max = 255, message = "Full name must be at most 255 characters")
    @Schema(description = "Full name", example = "John Doe", nullable = true)
    private String fullName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    @Schema(description = "Email address", example = "john@example.com", nullable = true)
    private String email;

    @ValidPhoneNumber
    @Size(max = 50, message = "Phone must be at most 50 characters")
    @Schema(description = "Phone number (UZ format +998XXXXXXXXX)", example = "+998901234567", nullable = true)
    private String phone;

    @Size(max = 255, message = "University code must be at most 255 characters")
    @Schema(description = "University code (null for system admins)", example = "TATU", nullable = true)
    private String universityCode;

    @NotEmpty(message = "At least one role is required")
    @Schema(description = "Role IDs to assign")
    private Set<UUID> roleIds;

    @Schema(description = "Whether the user is enabled", example = "true")
    @Builder.Default
    private Boolean enabled = true;
}
