package uz.hemis.common.dto.security;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Loaded User DTO - Framework-agnostic user representation
 *
 * <p>This DTO replaces direct dependency on User entity in security module.</p>
 * <p>Contains only security-relevant fields pre-computed by adapter.</p>
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>Security module defines what it needs (this DTO)</li>
 *   <li>Domain module provides implementation (adapter)</li>
 *   <li>No JPA/Hibernate dependency in security</li>
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
    "id", "username", "password",
    "enabled", "accountNonLocked", "deleted",
    "authorities", "permissions"
})
public class LoadedUser {

    /**
     * User ID (UUID)
     */
    private UUID id;

    /**
     * Username for authentication
     */
    private String username;

    /**
     * BCrypt hashed password
     */
    private String password;

    /**
     * Account enabled status
     */
    private boolean enabled;

    /**
     * Account non-locked status
     */
    private boolean accountNonLocked;

    /**
     * Soft delete status
     */
    private boolean deleted;

    /**
     * Pre-computed authorities (ROLE_* + permission codes)
     * <p>Adapter computes this from Role and Permission entities</p>
     */
    private Set<String> authorities;

    /**
     * Permission codes only (for caching)
     */
    private Set<String> permissions;
}
