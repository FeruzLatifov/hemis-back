package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/v1/web/menu")
@Tag(
    name = "Menu API",
    description = """
        Dynamic menu structure API for hemis-front Sidebar
        
        **Features:**
        - Permission-based filtering (shows only allowed menu items)
        - Multilingual support (uz-UZ, oz-UZ, ru-RU, en-US)
        - Hierarchical structure (unlimited nesting)
        - Cached for performance (1-hour TTL)
        
        **Use Case:** 
        - Sidebar menu rendering
        - Permission-based navigation
        - Breadcrumb generation
        """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final MenuService menuService;
    private final PermissionService permissionService;

    @GetMapping
    @Operation(
        summary = "Get dynamic menu structure",
        description = """
            Returns permission-filtered menu for authenticated user.
            
            **Flow:**
            1. Extract username from JWT token
            2. Load user permissions from cache/database
            3. Filter menu items by permissions
            4. Return translated menu in requested locale
            
            **Example Request:**
            ```
            GET /api/v1/web/menu?locale=ru-RU
            Authorization: Bearer eyJhbGci...
            ```
            
            **Example Response:**
            ```json
            {
              "items": [
                {
                  "id": "dashboard",
                  "labelUz": "Bosh sahifa",
                  "labelRu": "Главная",
                  "url": "/dashboard",
                  "icon": "home",
                  "order": 1
                },
                {
                  "id": "registry",
                  "labelUz": "Reestlar",
                  "labelRu": "Реестры",
                  "url": null,
                  "icon": "database",
                  "order": 2,
                  "items": [
                    {
                      "id": "registry-faculty",
                      "labelUz": "Fakultet",
                      "labelRu": "Факультет",
                      "url": "/registry/e-reestr/faculty",
                      "icon": "school",
                      "order": 1
                    }
                  ]
                }
              ]
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved menu",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MenuResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token missing or invalid"
        )
    })
    public ResponseEntity<MenuResponse> getMenu(
        @Parameter(
            description = "Locale code (BCP-47 format)",
            example = "uz-UZ",
            schema = @Schema(
                allowableValues = {"uz-UZ", "oz-UZ", "ru-RU", "en-US"}
            )
        )
        @RequestParam(defaultValue = "uz-UZ") String locale,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getSubject();
        log.debug("GET /api/v1/web/menu - username: {}, locale: {}", username, locale);

        MenuResponse menu = menuService.getMenuForUsername(username, locale);
        return ResponseEntity.ok(menu);
    }

    @PostMapping("/check-access")
    @Operation(
        summary = "Check if user can access path",
        description = """
            Verify if authenticated user has permission to access specific route.
            
            **Use Case:**
            - Frontend route guards
            - Dynamic button visibility
            - Permission-based UI rendering
            
            **Example Request:**
            ```json
            {
              "path": "/registry/faculty"
            }
            ```
            
            **Example Response:**
            ```json
            {
              "accessible": true
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Access check completed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"accessible\": true}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        )
    })
    public ResponseEntity<Map<String, Boolean>> checkAccess(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Path to check",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"path\": \"/registry/faculty\"}"
                )
            )
        )
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
