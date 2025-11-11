package uz.hemis.admin.dto.auth;

import lombok.*;

import java.util.List;

/**
 * Permission DTO for access control
 *
 * Defines what actions user can perform on entities/screens
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDTO {

    /**
     * Entity name (e.g., EStudent, ETeacher)
     */
    private String entity;

    /**
     * Screen ID (e.g., student-browse, teacher-edit)
     * Optional - can be null for entity-level permissions
     */
    private String screen;

    /**
     * Allowed actions
     * Values: read, create, update, delete
     */
    private List<String> actions;

    /**
     * Create read-only permission
     */
    public static PermissionDTO readOnly(String entity) {
        return PermissionDTO.builder()
                .entity(entity)
                .actions(List.of("read"))
                .build();
    }

    /**
     * Create full CRUD permission
     */
    public static PermissionDTO fullAccess(String entity) {
        return PermissionDTO.builder()
                .entity(entity)
                .actions(List.of("read", "create", "update", "delete"))
                .build();
    }

    /**
     * Create screen-specific permission
     */
    public static PermissionDTO forScreen(String screen, List<String> actions) {
        return PermissionDTO.builder()
                .screen(screen)
                .actions(actions)
                .build();
    }
}
