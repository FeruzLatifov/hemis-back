package uz.hemis.service.menu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.domain.entity.system.MenuType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * I18n translation key (e.g., "Dashboard", "E-Registry")
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
     * ✅ FIX #21: Dynamic labels for all supported languages
     * <p>Map of locale → translated label</p>
     * <p>Example: {"uz-UZ": "Bosh sahifa", "ru-RU": "Главная", "en-US": "Home"}</p>
     *
     * <p><strong>Benefits:</strong></p>
     * <ul>
     *   <li>Add new language without code changes</li>
     *   <li>Frontend can display all available translations</li>
     *   <li>Single source for multilingual support</li>
     * </ul>
     */
    @Builder.Default
    private Map<String, String> labels = new HashMap<>();

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

    /**
     * Menu type: MAIN (default) or SYSTEM.
     * <p>JSON wire format: lowercase ("main" / "system") — MenuType ga
     * @JsonValue qo'yilgan, frontend shu shaklni kutadi.</p>
     */
    @Builder.Default
    private MenuType menuType = MenuType.MAIN;

    @Builder.Default
    private List<MenuItem> items = new ArrayList<>();

    @Builder.Default
    private Boolean active = true;

    private Integer order;
}
