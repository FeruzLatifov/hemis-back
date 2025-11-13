package uz.hemis.service.menu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu Item DTO
 * Represents a single menu item with potential children
 *
 * <p><strong>Cache Compatibility:</strong></p>
 * <ul>
 *   <li>@JsonIgnoreProperties(ignoreUnknown = true) - Ignore unknown fields during deserialization</li>
 *   <li>Fixes Redis cache deserialization error for virtual fields (orderNum, visible)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuItem {

    /**
     * Unique menu item code (e.g., "dashboard", "registry-e-reestr")
     * <p>Used for routing and identification</p>
     */
    private String id;

    /**
     * Route path (e.g., "/dashboard", "/registry/e-reestr")
     * <p>Frontend uses this for navigation</p>
     */
    private String url;

    /**
     * I18n translation key (e.g., "menu.dashboard", "menu.registry.e_reestr")
     * <p>Used to fetch translations from i18n service</p>
     * <p><strong>IMPORTANT:</strong> This is the source of truth for translations</p>
     */
    private String i18nKey;

    /**
     * Label in current locale (dynamic based on request)
     * <p>Computed from i18nKey + current locale</p>
     */
    private String label;

    /**
     * Label in Uzbek Latin (for multi-language support)
     */
    @JsonProperty("labelUz")
    private String labelUz;

    /**
     * Label in Uzbek Cyrillic (for multi-language support)
     */
    @JsonProperty("labelOz")
    private String labelOz;

    /**
     * Label in Russian (for multi-language support)
     */
    @JsonProperty("labelRu")
    private String labelRu;

    /**
     * Label in English (for multi-language support)
     */
    @JsonProperty("labelEn")
    private String labelEn;

    /**
     * Icon name (e.g., "home", "database", "users")
     * <p>Used by frontend icon library</p>
     */
    private String icon;

    /**
     * Required permission to view this menu item
     * <p>Format: "dashboard.view", "registry.e-reestr.view"</p>
     */
    private String permission;

    @Builder.Default
    private List<MenuItem> items = new ArrayList<>();

    @Builder.Default
    private Boolean active = true;

    // Alias for 'active' - frontend expects 'visible'
    @JsonProperty("visible")
    public Boolean getVisible() {
        return active;
    }

    public void setVisible(Boolean visible) {
        this.active = visible;
    }

    private Integer order;

    // Frontend compatibility: orderNum
    @JsonProperty("orderNum")
    public Integer getOrderNum() {
        return order != null ? order : 0;
    }
}
