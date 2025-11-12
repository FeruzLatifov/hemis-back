package uz.hemis.service.menu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.service.I18nService;
import uz.hemis.service.menu.dto.MenuItem;
import uz.hemis.service.menu.dto.MenuResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Menu Service
 * Filters and prepares menu based on user permissions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuConfig menuConfig;
    private final PermissionService permissionService;
    private final I18nService i18nService;
    private final UserRepository userRepository;

    /**
     * Get filtered menu for user by username
     * (Convenience method for controllers)
     */
    public MenuResponse getMenuForUsername(String username, String locale) {
        log.info("Getting menu for username: {}, locale: {}", username, locale);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return getMenuForUser(user.getId(), locale);
    }

    /**
     * Get filtered menu for user
     */
    public MenuResponse getMenuForUser(UUID userId, String locale) {
        log.info("Getting menu for user: {}, locale: {}", userId, locale);

        // Get user permissions
        List<String> userPermissions = permissionService.getUserPermissions(userId);
        log.info("User {} has {} permissions: {}", userId, userPermissions.size(),
            userPermissions.size() > 0 ? userPermissions.subList(0, Math.min(5, userPermissions.size())) : "[]");

        // Get menu structure
        List<MenuItem> menuStructure = menuConfig.menuStructure();
        log.info("Menu structure has {} root items", menuStructure.size());

        // Filter by permissions
        List<MenuItem> filteredMenu = filterMenuByPermissions(
            menuStructure,
            userPermissions,
            locale
        );
        log.info("Filtered menu has {} items", filteredMenu.size());

        // Sort by order
        sortMenuItems(filteredMenu);

        // Build response
        return MenuResponse.builder()
            .menu(filteredMenu)
            .permissions(userPermissions)
            .locale(locale)
            ._meta(MenuResponse.MetaData.builder()
                .cached(false)
                .cacheExpiresAt(System.currentTimeMillis() + 3600000) // 1 hour
                .generatedAt(LocalDateTime.now().toString())
                .build())
            .build();
    }

    /**
     * Filter menu items by permissions (recursive)
     */
    private List<MenuItem> filterMenuByPermissions(
        List<MenuItem> items,
        List<String> permissions,
        String locale
    ) {
        List<MenuItem> filtered = new ArrayList<>();

        for (MenuItem item : items) {
            if (hasPermission(item.getPermission(), permissions)) {
                // Get translation key from label (e.g., "menu.dashboard")
                String translationKey = item.getLabel();

                // Create filtered copy with translations in all 4 languages
                MenuItem filteredItem = MenuItem.builder()
                    .id(item.getId())
                    .label(i18nService.getMessage(translationKey, locale))         // Current locale
                    .labelUz(i18nService.getMessage(translationKey, "uz-UZ"))      // Uzbek Latin
                    .labelOz(i18nService.getMessage(translationKey, "oz-UZ"))      // Uzbek Cyrillic
                    .labelRu(i18nService.getMessage(translationKey, "ru-RU"))      // Russian
                    .labelEn(i18nService.getMessage(translationKey, "en-US"))      // English
                    .url(item.getUrl())
                    .icon(item.getIcon())
                    .permission(item.getPermission())
                    .active(item.getActive())
                    .order(item.getOrder())
                    .build();

                // Filter children recursively
                if (item.getItems() != null && !item.getItems().isEmpty()) {
                    List<MenuItem> filteredChildren = filterMenuByPermissions(
                        item.getItems(),
                        permissions,
                        locale
                    );
                    filteredItem.setItems(filteredChildren);
                }

                filtered.add(filteredItem);
            }
        }

        return filtered;
    }

    /**
     * Check if user has permission (with wildcard support)
     */
    private boolean hasPermission(String required, List<String> userPermissions) {
        if (required == null || required.isEmpty()) {
            return true;
        }

        // Super admin
        if (userPermissions.contains("*")) {
            return true;
        }

        // Exact match
        if (userPermissions.contains(required)) {
            return true;
        }

        // Wildcard pattern
        for (String permission : userPermissions) {
            if (permission.endsWith(".*")) {
                String prefix = permission.substring(0, permission.length() - 2);
                if (required.startsWith(prefix + ".")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Sort menu items by order field (recursive)
     */
    private void sortMenuItems(List<MenuItem> items) {
        items.sort(Comparator.comparing(
            MenuItem::getOrder,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));

        // Sort children
        for (MenuItem item : items) {
            if (item.getItems() != null && !item.getItems().isEmpty()) {
                sortMenuItems(item.getItems());
            }
        }
    }
}
