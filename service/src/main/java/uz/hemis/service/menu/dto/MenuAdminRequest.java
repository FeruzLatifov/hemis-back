package uz.hemis.service.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Menu Admin Request DTO
 *
 * <p>Used for creating and updating menu items via Admin API</p>
 *
 * <p><strong>Validation Rules:</strong></p>
 * <ul>
 *   <li>code: Required, lowercase-hyphen format, 3-100 chars</li>
 *   <li>i18nKey: Required, 5-200 chars</li>
 *   <li>orderNumber: Required, non-negative</li>
 *   <li>active: Required, boolean</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Menu creation/update request")
public class MenuAdminRequest {

    @NotBlank(message = "Menu code is required")
    @Size(min = 3, max = 100, message = "Menu code must be 3-100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Menu code must be lowercase letters, numbers, and hyphens only")
    @Schema(
        description = "Unique menu code (lowercase-hyphen format)",
        example = "system-users"
    )
    private String code;

    @NotBlank(message = "i18n key is required")
    @Size(min = 5, max = 200, message = "i18n key must be 5-200 characters")
    @Schema(
        description = "Translation key for menu label",
        example = "menu.system.users"
    )
    private String i18nKey;

    @Size(max = 500, message = "URL must not exceed 500 characters")
    @Schema(
        description = "Navigation URL (null for parent menus)",
        example = "/system/users",
        nullable = true
    )
    private String url;

    @Size(max = 100, message = "Icon name must not exceed 100 characters")
    @Schema(
        description = "Icon identifier for UI",
        example = "users",
        nullable = true
    )
    private String icon;

    @Size(max = 200, message = "Permission must not exceed 200 characters")
    @Schema(
        description = "Required permission code (null = public access)",
        example = "system.users.view",
        nullable = true
    )
    private String permission;

    @Schema(
        description = "Parent menu ID (null for root menu)",
        example = "00000000-0000-0000-0000-000000000006",
        nullable = true
    )
    private UUID parentId;

    @NotNull(message = "Order number is required")
    @Schema(
        description = "Display order within same parent (lower = first)",
        example = "1",
        minimum = "0"
    )
    private Integer orderNumber;

    @NotNull(message = "Active status is required")
    @Schema(
        description = "Menu active status (true = visible, false = hidden)",
        example = "true"
    )
    private Boolean active;
}
