package uz.hemis.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User Info Response - /auth/me endpoint
 *
 * <p><strong>Slim DTO - Minimal fields only</strong></p>
 *
 * <p>Industry best practice:</p>
 * <ul>
 *   <li>Frontend caches this response</li>
 *   <li>Contains only essential fields</li>
 *   <li>No nested entities (performance)</li>
 *   <li>Permissions list for authorization</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /**
     * User basic info (minimal)
     */
    private UserBasicInfo user;

    /**
     * User permissions (for frontend authorization)
     */
    private List<String> permissions;

    /**
     * User roles (for UI display)
     */
    private List<String> roles;

    /**
     * University info (minimal)
     */
    private UniversityBasicInfo university;

    /**
     * Nested DTO: User Basic Info (slim)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBasicInfo {
        private String id;           // UUID as string
        private String username;
        private String fullName;
        private String email;
        private String locale;       // uz-UZ, oz-UZ, ru-RU, en-US
        private Boolean active;
    }

    /**
     * Nested DTO: University Basic Info (slim)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UniversityBasicInfo {
        private String code;         // OTM code
        private String name;
        private String shortName;
    }
}
