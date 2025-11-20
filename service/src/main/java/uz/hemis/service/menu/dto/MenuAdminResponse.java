package uz.hemis.service.menu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Menu Admin Response DTO
 *
 * <p>Used for returning menu data via Admin API</p>
 * <p>Includes complete menu information with audit trail</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Menu admin response with full details")
public class MenuAdminResponse {

    @Schema(description = "Menu unique ID", example = "00000000-0000-0000-0000-000000000061")
    private UUID id;

    @Schema(description = "Unique menu code", example = "system-users")
    private String code;

    @Schema(description = "Translation key for i18n", example = "menu.system.users")
    private String i18nKey;

    @Schema(description = "Navigation URL", example = "/system/users", nullable = true)
    private String url;

    @Schema(description = "Icon identifier", example = "users", nullable = true)
    private String icon;

    @Schema(description = "Required permission", example = "system.users.view", nullable = true)
    private String permission;

    @Schema(description = "Parent menu ID", example = "00000000-0000-0000-0000-000000000006", nullable = true)
    private UUID parentId;

    @Schema(description = "Parent menu code", example = "system", nullable = true)
    private String parentCode;

    @Schema(description = "Display order", example = "1")
    private Integer orderNumber;

    @Schema(description = "Active status", example = "true")
    private Boolean active;

    @Schema(description = "Child menus (hierarchical)")
    @Builder.Default
    private List<MenuAdminResponse> children = new ArrayList<>();

    // Audit fields
    @Schema(description = "Creation timestamp", example = "2025-11-16T14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Creator username", example = "admin", nullable = true)
    private String createdBy;

    @Schema(description = "Last update timestamp", example = "2025-11-16T15:30:00", nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "Last updater username", example = "admin", nullable = true)
    private String updatedBy;

    @Schema(description = "Soft delete timestamp", example = "null", nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    // Helper computed fields
    @Schema(description = "Is this a root menu (no parent)", example = "false")
    public boolean isRoot() {
        return parentId == null;
    }

    @Schema(description = "Does this menu have children", example = "true")
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Schema(description = "Is this menu deleted", example = "false")
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
