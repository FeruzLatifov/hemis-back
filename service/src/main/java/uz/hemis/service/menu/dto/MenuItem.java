package uz.hemis.service.menu.dto;

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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    private String id;

    /**
     * Label in current locale (dynamic based on request)
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

    private String url;
    private String icon;
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
