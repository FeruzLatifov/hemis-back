package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MenuItem DTO for API JSON serialization
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>REST API request/response for menu management</li>
 *   <li>Static export to menu.json for frontend consumption</li>
 *   <li>Hierarchical tree structure support</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <ul>
 *   <li>Admin Panel: CRUD operations for menu items</li>
 *   <li>Static Export: Generate menu.json for Nginx serving</li>
 *   <li>Frontend: Load from /static/menu/menu.json (zero backend load)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Data
public class MenuItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    @JsonProperty("id")
    private UUID id;

    // =====================================================
    // Identification
    // =====================================================

    /**
     * Unique menu item code
     * Examples: 'students', 'teachers', 'reports'
     */
    @JsonProperty("code")
    @NotBlank(message = "Menu code is required")
    @Size(max = 100, message = "Code must not exceed 100 characters")
    private String code;

    // =====================================================
    // Multilingual Labels
    // =====================================================

    /**
     * Uzbek label (required)
     */
    @JsonProperty("labelUz")
    @NotBlank(message = "Uzbek label is required")
    @Size(max = 255, message = "Label must not exceed 255 characters")
    private String labelUz;

    /**
     * Russian label (optional)
     */
    @JsonProperty("labelRu")
    @Size(max = 255)
    private String labelRu;

    /**
     * English label (optional)
     */
    @JsonProperty("labelEn")
    @Size(max = 255)
    private String labelEn;

    // =====================================================
    // Navigation
    // =====================================================

    /**
     * Frontend route path
     * NULL for parent-only items
     */
    @JsonProperty("route")
    @Size(max = 255)
    private String route;

    /**
     * Icon component name
     * Examples: 'UserIcon', 'BookIcon', 'ChartIcon'
     */
    @JsonProperty("icon")
    @Size(max = 50)
    private String icon;

    // =====================================================
    // Hierarchy
    // =====================================================

    /**
     * Parent menu item ID
     * NULL for root-level items
     */
    @JsonProperty("parentId")
    private UUID parentId;

    /**
     * Child menu items
     * Used for tree structure in export
     */
    @JsonProperty("children")
    private List<MenuItemDto> children = new ArrayList<>();

    /**
     * Display order (ascending)
     */
    @JsonProperty("orderNum")
    @NotNull(message = "Order number is required")
    private Integer orderNum;

    // =====================================================
    // Permission-Based Visibility
    // =====================================================

    /**
     * Permission code required to view
     * NULL = visible to all authenticated users
     */
    @JsonProperty("permissionCode")
    @Size(max = 100)
    private String permissionCode;

    /**
     * Visibility flag
     */
    @JsonProperty("visible")
    @NotNull
    private Boolean visible = true;

    // =====================================================
    // Audit Fields (for admin panel)
    // =====================================================

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // =====================================================
    // Utility Methods
    // =====================================================

    /**
     * Get label by language code
     *
     * @param lang Language code ('uz', 'ru', 'en')
     * @return Label in specified language
     */
    public String getLabel(String lang) {
        return switch (lang) {
            case "ru" -> labelRu != null ? labelRu : labelUz;
            case "en" -> labelEn != null ? labelEn : labelUz;
            default -> labelUz;
        };
    }

    /**
     * Check if this is a root-level menu item
     *
     * @return true if parentId is null
     */
    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * Check if this menu item has children
     *
     * @return true if children list is not empty
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
