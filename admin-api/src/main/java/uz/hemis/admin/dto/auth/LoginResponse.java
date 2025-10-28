package uz.hemis.admin.dto.auth;

import lombok.*;
import uz.hemis.admin.dto.university.UniversityDTO;
import uz.hemis.admin.dto.user.AdminUserDTO;

import java.util.List;

/**
 * Login response DTO
 *
 * Contains JWT tokens, user info, university, and permissions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * JWT access token (30 days expiration)
     * Format: Bearer token for Authorization header
     */
    private String token;

    /**
     * JWT refresh token (60 days expiration)
     * Used to obtain new access token
     */
    private String refreshToken;

    /**
     * Authenticated user information
     */
    private AdminUserDTO user;

    /**
     * User's assigned university
     */
    private UniversityDTO university;

    /**
     * User permissions (for frontend access control)
     */
    private List<PermissionDTO> permissions;
}
