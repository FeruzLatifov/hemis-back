package uz.hemis.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "LoginRequest",
    description = "Web login credentials"
)
public class LoginRequest {

    @Schema(
        description = "Username for authentication",
        minLength = 3,
        maxLength = 50
    )
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @Schema(
        description = "User password",
        minLength = 3,
        maxLength = 100,
        format = "password"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 3, max = 100)
    private String password;
}
