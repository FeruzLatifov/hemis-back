package uz.hemis.admin.dto.user;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Admin user DTO for API responses
 *
 * Contains user profile information (no password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDTO {

    /**
     * User ID
     */
    private String id;

    /**
     * Username (login)
     */
    private String username;

    /**
     * Email address
     */
    private String email;

    /**
     * Full name
     */
    private String name;

    /**
     * Preferred locale (uz, ru, en)
     */
    private String locale;

    /**
     * Active status
     */
    private Boolean active;

    /**
     * Account creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
