package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change password request")
public class ChangePasswordRequest {

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
    @Schema(description = "New password (will be BCrypt hashed)", format = "password")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Confirm new password", format = "password")
    private String confirmPassword;
}
