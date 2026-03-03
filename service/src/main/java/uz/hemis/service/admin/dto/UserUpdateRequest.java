package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Schema(description = "User update request (username is immutable)")
public class UserUpdateRequest {

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

    @Schema(description = "Role IDs to assign (replaces existing roles)")
    private Set<UUID> roleIds;
}
