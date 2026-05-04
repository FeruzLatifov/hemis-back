package uz.hemis.common.dto.security;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Legacy Loaded User DTO - CUBA-compatible user representation
 *
 * <p>This DTO replaces direct dependency on SecUser entity in security module.</p>
 * <p>Contains CUBA-specific fields mapped by adapter.</p>
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>Security module defines what it needs (this DTO)</li>
 *   <li>Domain module provides implementation (adapter)</li>
 *   <li>No JPA/Hibernate dependency in security</li>
 * </ul>
 *
 * <p><strong>CUBA Compatibility:</strong></p>
 * <ul>
 *   <li>Maps from sec_user table</li>
 *   <li>Supports group-based authorization</li>
 *   <li>University restriction support</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Internal cache DTO — Redis serialization order saqlash uchun
@JsonPropertyOrder({
    "id", "login", "password",
    "active", "deleted",
    "groupNames", "systemAdmin", "universityCode",
    "accountNonLocked", "credentialsNonExpired",
    "authorities"
})
public class LegacyLoadedUser {

    /**
     * User ID (UUID) - from sec_user.id
     */
    private UUID id;

    /**
     * Login username - from sec_user.login
     */
    private String login;

    /**
     * BCrypt hashed password - from sec_user.password
     */
    private String password;

    /**
     * Account active status - from sec_user.active
     */
    private boolean active;

    /**
     * Soft delete status - from sec_user.delete_ts IS NOT NULL
     */
    private boolean deleted;

    /**
     * Comma-separated CUBA group names
     */
    private String groupNames;

    /**
     * System admin flag (university = NULL)
     */
    private boolean systemAdmin;

    /**
     * University code restriction
     */
    private String universityCode;

    /**
     * Account non-locked status
     */
    private boolean accountNonLocked;

    /**
     * Credentials non-expired status
     */
    private boolean credentialsNonExpired;

    /**
     * Pre-computed authorities (mapped from CUBA groups)
     */
    private Set<String> authorities;
}
