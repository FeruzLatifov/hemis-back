package uz.hemis.api.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.menu.MenuService;
import uz.hemis.service.menu.PermissionService;
import uz.hemis.service.menu.dto.MenuResponse;

import java.util.Map;

/**
 * Menu Controller
 * Provides menu API endpoints
 */
@RestController
@RequestMapping("/api/v1/web/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final MenuService menuService;
    private final PermissionService permissionService;

    /**
     * GET /api/v1/web/menu?locale=uz-UZ
     * Get filtered menu for authenticated user
     */
    @GetMapping
    public ResponseEntity<MenuResponse> getMenu(
        @RequestParam(defaultValue = "uz-UZ") String locale,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getSubject();
        log.debug("GET /api/v1/web/menu - username: {}, locale: {}", username, locale);

        MenuResponse menu = menuService.getMenuForUsername(username, locale);
        return ResponseEntity.ok(menu);
    }

    /**
     * POST /api/v1/web/menu/check-access
     * Check if user can access specific path
     */
    @PostMapping("/check-access")
    public ResponseEntity<Map<String, Boolean>> checkAccess(
        @RequestBody Map<String, String> request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getSubject();
        String path = request.get("path");

        log.debug("POST /api/v1/web/menu/check-access - username: {}, path: {}", username, path);

        boolean accessible = permissionService.canAccessPath(username, path);

        return ResponseEntity.ok(Map.of("accessible", accessible));
    }

    /**
     * POST /api/v1/web/menu/clear-cache
     * Clear menu cache
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, Object>> clearCache() {
        log.info("POST /api/v1/web/menu/clear-cache");

        // TODO: Implement cache clearing when Redis caching is added
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Cache cleared successfully (placeholder - caching not yet implemented)"
        ));
    }

    /**
     * GET /api/v1/web/menu/structure
     * Get full menu structure (admin only)
     */
    @GetMapping("/structure")
    public ResponseEntity<MenuResponse> getStructure(
        @RequestParam(defaultValue = "uz-UZ") String locale,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getSubject();
        log.debug("GET /api/v1/web/menu/structure - username: {}, locale: {}", username, locale);

        // For now, return same as regular menu
        // TODO: Add admin-only check
        MenuResponse menu = menuService.getMenuForUsername(username, locale);
        return ResponseEntity.ok(menu);
    }
}
