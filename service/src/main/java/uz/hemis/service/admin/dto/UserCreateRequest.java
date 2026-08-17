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

    /**
     * Account type — drives the create flow:
     * <ul>
     *   <li>{@code PERSON} (default) — ministry/university human staff. Login = PINFL,
     *       person fields autofilled from the GUVD passport-data gateway.</li>
     *   <li>{@code UNIVERSITY_LOGIN} — OTM service/integration login for the old-hemis
     *       backward-compatible password grant. Manual username + password + university,
     *       NO PINFL / person data.</li>
     * </ul>
     */
    @Pattern(regexp = "PERSON|UNIVERSITY_LOGIN", message = "accountType must be PERSON or UNIVERSITY_LOGIN")
    @Schema(description = "Account type: PERSON (PINFL autofill) or UNIVERSITY_LOGIN (manual service login)",
            example = "PERSON", nullable = true)
    private String accountType;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, digits, underscores, dots, and hyphens")
    @Schema(description = "Login username. For PERSON accounts this is the PINFL (set on the client); "
            + "for UNIVERSITY_LOGIN it is the manual service login.", example = "31507976020031")
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

    // =====================================================
    // PERSON account fields (accountType = PERSON) — PINFL + GUVD passport-data autofill.
    // Ignored for UNIVERSITY_LOGIN accounts.
    // =====================================================

    @Pattern(regexp = "^\\d{14}$", message = "PINFL must be 14 digits")
    @Schema(description = "PINFL (14 digits) — required for PERSON accounts; login is set to this value",
            example = "31507976020031", nullable = true)
    private String pinfl;

    @Size(max = 255) @Schema(nullable = true) private String firstName;
    @Size(max = 255) @Schema(nullable = true) private String lastName;
    @Size(max = 255) @Schema(nullable = true) private String middleName;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "birthDate must be yyyy-MM-dd")
    @Schema(description = "Birth date yyyy-MM-dd", example = "1990-01-15", nullable = true)
    private String birthDate;

    @Size(max = 255) @Schema(nullable = true) private String birthPlace;

    @Size(max = 16) @Schema(description = "Passport series+number", example = "AB1234567", nullable = true)
    private String passport;

    @Size(max = 255) @Schema(nullable = true) private String passportGivePlace;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "passportIssuedDate must be yyyy-MM-dd")
    @Schema(nullable = true) private String passportIssuedDate;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "passportExpiryDate must be yyyy-MM-dd")
    @Schema(nullable = true) private String passportExpiryDate;

    @Size(max = 10) @Schema(nullable = true) private String gender;
    @Size(max = 64) @Schema(nullable = true) private String nationality;
    @Size(max = 512) @Schema(nullable = true) private String address;

    @Schema(description = "Person photo base64 (GUVD)", nullable = true)
    private String photo;
}
