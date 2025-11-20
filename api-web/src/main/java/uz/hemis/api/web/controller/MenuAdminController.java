package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.MenuAuditLog;
import uz.hemis.service.menu.MenuAdminService;
import uz.hemis.service.menu.dto.MenuAdminRequest;
import uz.hemis.service.menu.dto.MenuAdminResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Menu Administration Controller
 *
 * <p>RESTful API for menu CRUD operations (Admin only)</p>
 *
 * <p><strong>Base Path:</strong> {@code /api/v1/web/admin/menus}</p>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All endpoints require authentication (JWT Bearer token)</li>
 *   <li>All endpoints require {@code system.menus.manage} permission</li>
 *   <li>Returns 401 Unauthorized if not authenticated</li>
 *   <li>Returns 403 Forbidden if missing permission</li>
 * </ul>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>List all menus (including inactive/deleted)</li>
 *   <li>Get menu by ID or code</li>
 *   <li>Create new menu</li>
 *   <li>Update existing menu</li>
 *   <li>Soft delete menu (preserves audit trail)</li>
 *   <li>Toggle active status</li>
 *   <li>Reorder menus</li>
 *   <li>Clear menu cache</li>
 * </ul>
 *
 * <p><strong>Validation:</strong></p>
 * <ul>
 *   <li>Automatic request validation via Jakarta Bean Validation</li>
 *   <li>Returns 400 Bad Request with error details on validation failure</li>
 * </ul>
 *
 * <p><strong>Cache Management:</strong></p>
 * <ul>
 *   <li>All mutations (create/update/delete/toggle) automatically clear menu cache</li>
 *   <li>Manual cache clear endpoint available</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web/admin/menus")
@Tag(
    name = "Menu Administration",
    description = """
        Menu CRUD operations for administrators.

        **Required Permission:** system.menus.manage

        **Features:**
        - Complete menu management (create, read, update, delete)
        - Hierarchical structure support (parent-child relationships)
        - Soft delete (preserves audit trail)
        - Automatic cache invalidation
        - Input validation and business rule enforcement

        **Use Case:**
        - Admin UI menu management panel
        - Dynamic menu configuration without code deployment
        - Permission-based navigation setup
        """
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('system.menus.manage')")
@RequiredArgsConstructor
@Slf4j
public class MenuAdminController {

    private final MenuAdminService menuAdminService;

    // =====================================================
    // READ OPERATIONS
    // =====================================================

    /**
     * GET /api/v1/web/admin/menus
     * List all menus (including inactive and deleted)
     */
    @GetMapping
    @Operation(
        summary = "List all menus",
        description = """
            Returns complete list of all menus including inactive and deleted ones.

            **Response includes:**
            - Root menus (parent_id = null)
            - Child menus (hierarchical structure)
            - Inactive menus (active = false)
            - Soft-deleted menus (deleted_at != null)
            - Complete audit trail (created_at, updated_at, created_by, updated_by)

            **Ordering:** Hierarchical by parent → order_number

            **Use Case:** Admin UI menu tree table
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved all menus",
        content = @Content(schema = @Schema(implementation = MenuAdminResponse.class))
    )
    public ResponseEntity<List<MenuAdminResponse>> getAllMenus(@AuthenticationPrincipal Jwt jwt) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.debug("GET /api/v1/web/admin/menus - requester: {}", requester);

        List<MenuAdminResponse> menus = menuAdminService.getAllMenus();
        return ResponseEntity.ok(menus);
    }

    /**
     * GET /api/v1/web/admin/menus/{id}
     * Get menu by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get menu by ID",
        description = """
            Returns single menu with complete details including children.

            **Response includes:**
            - All menu fields (code, i18nKey, url, icon, permission, etc.)
            - Parent information (parentId, parentCode)
            - Hierarchical children
            - Audit trail

            **Use Case:** Menu detail view, edit form
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Menu found",
            content = @Content(schema = @Schema(implementation = MenuAdminResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Menu not found with given ID"
        )
    })
    public ResponseEntity<MenuAdminResponse> getMenuById(
        @Parameter(description = "Menu UUID", example = "00000000-0000-0000-0000-000000000001")
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.debug("GET /api/v1/web/admin/menus/{} - requester: {}", id, requester);

        MenuAdminResponse menu = menuAdminService.getMenuById(id);
        return ResponseEntity.ok(menu);
    }

    /**
     * GET /api/v1/web/admin/menus/by-code/{code}
     * Get menu by code
     */
    @GetMapping("/by-code/{code}")
    @Operation(
        summary = "Get menu by code",
        description = """
            Returns menu by unique code (e.g., "dashboard", "registry-e-reestr").

            **Use Case:** Programmatic menu lookup, link validation
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Menu found"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Menu not found with given code"
        )
    })
    public ResponseEntity<MenuAdminResponse> getMenuByCode(
        @Parameter(description = "Menu code", example = "dashboard")
        @PathVariable String code,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.debug("GET /api/v1/web/admin/menus/by-code/{} - requester: {}", code, requester);

        MenuAdminResponse menu = menuAdminService.getMenuByCode(code);
        return ResponseEntity.ok(menu);
    }

    // =====================================================
    // CREATE OPERATION
    // =====================================================

    /**
     * POST /api/v1/web/admin/menus
     * Create new menu
     */
    @PostMapping
    @Operation(
        summary = "Create new menu",
        description = """
            Creates new menu item with validation.

            **Validations:**
            - code: Required, lowercase-hyphen format, 3-100 chars, must be unique
            - i18nKey: Required, 5-200 chars
            - url: Optional, max 500 chars
            - icon: Optional, max 100 chars
            - permission: Optional, max 200 chars
            - parentId: Optional UUID (must reference existing menu)
            - orderNumber: Required, non-negative integer
            - active: Required, boolean

            **Business Rules:**
            - Code must be unique across all menus
            - Parent menu must exist (if parentId provided)
            - No circular references allowed

            **Side Effects:**
            - Automatically clears menu cache (L1 + L2)
            - Triggers cache warmup on next menu request

            **Use Case:** Add new menu item via Admin UI
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Menu created successfully",
            content = @Content(schema = @Schema(implementation = MenuAdminResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input (validation failed or business rule violated)"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Missing system.menus.manage permission"
        )
    })
    public ResponseEntity<MenuAdminResponse> createMenu(
        @Valid @RequestBody MenuAdminRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.info("POST /api/v1/web/admin/menus - code: {} - requester: {}", request.getCode(), requester);

        MenuAdminResponse created = menuAdminService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =====================================================
    // UPDATE OPERATION
    // =====================================================

    /**
     * PUT /api/v1/web/admin/menus/{id}
     * Update existing menu
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update existing menu",
        description = """
            Updates menu with validation.

            **Validations:** Same as create endpoint

            **Business Rules:**
            - Menu must exist
            - Code must be unique (if changed)
            - Parent must exist (if parentId changed)
            - Cannot set self or descendant as parent (circular reference check)

            **Side Effects:**
            - Automatically clears menu cache (L1 + L2)

            **Use Case:** Edit menu via Admin UI
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Menu updated successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or business rule violated"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Menu not found"
        )
    })
    public ResponseEntity<MenuAdminResponse> updateMenu(
        @Parameter(description = "Menu UUID to update")
        @PathVariable UUID id,
        @Valid @RequestBody MenuAdminRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.info("PUT /api/v1/web/admin/menus/{} - code: {} - requester: {}", id, request.getCode(), requester);

        MenuAdminResponse updated = menuAdminService.updateMenu(id, request);
        return ResponseEntity.ok(updated);
    }

    // =====================================================
    // DELETE OPERATION (Soft Delete)
    // =====================================================

    /**
     * DELETE /api/v1/web/admin/menus/{id}
     * Soft delete menu
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete menu (soft delete)",
        description = """
            Soft deletes menu by setting deleted_at timestamp.

            **Behavior:**
            - Sets deleted_at = NOW()
            - Preserves all data (audit trail maintained)
            - Also soft-deletes all child menus recursively
            - Menu excluded from user-facing queries automatically
            - Admin queries still see deleted menus

            **Side Effects:**
            - Automatically clears menu cache (L1 + L2)

            **Recovery:** Can be restored by setting deleted_at = NULL via database

            **Use Case:** Remove menu from navigation
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Menu deleted successfully (no content)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Menu not found"
        )
    })
    public ResponseEntity<Void> deleteMenu(
        @Parameter(description = "Menu UUID to delete")
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.info("DELETE /api/v1/web/admin/menus/{} - requester: {}", id, requester);

        menuAdminService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // TOGGLE ACTIVE STATUS
    // =====================================================

    /**
     * PATCH /api/v1/web/admin/menus/{id}/toggle
     * Toggle menu active status
     */
    @PatchMapping("/{id}/toggle")
    @Operation(
        summary = "Toggle menu active status",
        description = """
            Toggles active status: true ↔ false

            **Behavior:**
            - If active = true, sets to false (hide menu)
            - If active = false, sets to true (show menu)

            **Side Effects:**
            - Automatically clears menu cache (L1 + L2)

            **Use Case:** Quickly enable/disable menu without full update
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Active status toggled successfully"
    )
    public ResponseEntity<MenuAdminResponse> toggleActive(
        @Parameter(description = "Menu UUID")
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.info("PATCH /api/v1/web/admin/menus/{}/toggle - requester: {}", id, requester);

        MenuAdminResponse updated = menuAdminService.toggleActive(id);
        return ResponseEntity.ok(updated);
    }

    // =====================================================
    // REORDER OPERATION
    // =====================================================

    /**
     * PATCH /api/v1/web/admin/menus/{id}/reorder
     * Change menu display order
     */
    @PatchMapping("/{id}/reorder")
    @Operation(
        summary = "Change menu display order",
        description = """
            Updates orderNumber field to change menu position.

            **Behavior:**
            - Sets orderNumber to new value
            - Lower orderNumber = displayed first
            - Menus sorted by orderNumber within same parent

            **Side Effects:**
            - Automatically clears menu cache (L1 + L2)

            **Use Case:** Drag-and-drop reordering in Admin UI
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Menu reordered successfully"
    )
    public ResponseEntity<Map<String, Object>> reorderMenu(
        @Parameter(description = "Menu UUID")
        @PathVariable UUID id,
        @Parameter(description = "New order number", example = "5")
        @RequestParam Integer newOrder,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.info("PATCH /api/v1/web/admin/menus/{}/reorder - newOrder: {} - requester: {}", id, newOrder, requester);

        menuAdminService.reorderMenu(id, newOrder);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Menu reordered successfully",
            "menuId", id,
            "newOrder", newOrder
        ));
    }

    // =====================================================
    // CACHE MANAGEMENT
    // =====================================================

    /**
     * POST /api/v1/web/admin/menus/cache/clear
     * Manually clear menu cache
     */
    @PostMapping("/cache/clear")
    @Operation(
        summary = "Clear menu cache",
        description = """
            Manually clears all menu cache (L1 Caffeine + L2 Redis).

            **Use Case:**
            - After direct database updates
            - When cache becomes stale
            - Testing cache behavior

            **Note:** CRUD operations automatically clear cache, so manual clearing is rarely needed.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Cache cleared successfully"
    )
    public ResponseEntity<Map<String, Object>> clearCache(@AuthenticationPrincipal Jwt jwt) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.info("POST /api/v1/web/admin/menus/cache/clear - requester: {}", requester);

        menuAdminService.clearCache();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Menu cache cleared successfully (L1 + L2)",
            "clearedBy", requester,
            "timestamp", System.currentTimeMillis()
        ));
    }

    // =====================================================
    // EXPORT/IMPORT (NEW v2.0)
    // =====================================================

    /**
     * GET /api/v1/web/admin/menus/export
     * Export menu structure as JSON
     */
    @GetMapping("/export")
    @Operation(
        summary = "Export menu structure",
        description = """
            Exports entire menu structure as JSON.

            **Use Cases:**
            - Backup menu configuration
            - Transfer menu between environments (dev → prod)
            - Version control (commit JSON to git)
            - Disaster recovery

            **Response Format:** JSON array of menu objects with all fields
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Menu structure exported successfully"
    )
    public ResponseEntity<String> exportMenuStructure(@AuthenticationPrincipal Jwt jwt) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.info("GET /api/v1/web/admin/menus/export - requester: {}", requester);

        String json = menuAdminService.exportMenuStructure();
        return ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .header("Content-Disposition", "attachment; filename=menu-structure-export.json")
            .body(json);
    }

    /**
     * POST /api/v1/web/admin/menus/import
     * Import menu structure from JSON
     */
    @PostMapping("/import")
    @Operation(
        summary = "Import menu structure",
        description = """
            Imports menu structure from JSON.

            **WARNING:** This operation updates existing menus!
            - Existing menus (matched by code) will be updated
            - New menus will be inserted
            - Does NOT delete existing menus

            **Recommended:** Backup before import!

            **Request Body:** JSON array of menu objects
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Menu structure imported successfully"
    )
    public ResponseEntity<Map<String, Object>> importMenuStructure(
        @RequestBody String json,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.warn("POST /api/v1/web/admin/menus/import - requester: {}", requester);

        int count = menuAdminService.importMenuStructure(json);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Menu structure imported successfully",
            "importedCount", count,
            "importedBy", requester,
            "timestamp", System.currentTimeMillis()
        ));
    }

    // =====================================================
    // AUDIT TRAIL (NEW v2.0)
    // =====================================================

    /**
     * GET /api/v1/web/admin/menus/{id}/audit-logs
     * Get audit logs for specific menu
     */
    @GetMapping("/{id}/audit-logs")
    @Operation(
        summary = "Get menu audit logs",
        description = """
            Returns complete audit trail for specific menu.

            **Response includes:**
            - All changes (CREATE, UPDATE, DELETE, REORDER, etc.)
            - WHO changed (username)
            - WHEN changed (timestamp)
            - WHAT changed (before/after snapshots)

            **Ordering:** Newest first (changed_at DESC)
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Audit logs retrieved successfully"
    )
    public ResponseEntity<List<MenuAuditLog>> getAuditLogs(
        @Parameter(description = "Menu UUID")
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.debug("GET /api/v1/web/admin/menus/{}/audit-logs - requester: {}", id, requester);

        List<MenuAuditLog> auditLogs = menuAdminService.getAuditLogs(id);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * GET /api/v1/web/admin/menus/audit-logs/recent
     * Get recent audit logs for all menus
     */
    @GetMapping("/audit-logs/recent")
    @Operation(
        summary = "Get recent audit logs",
        description = """
            Returns recent audit logs for all menus.

            **Query Parameters:**
            - days (default: 7) - Number of days to look back

            **Use Case:** Admin dashboard recent activity widget
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Recent audit logs retrieved successfully"
    )
    public ResponseEntity<List<MenuAuditLog>> getRecentAuditLogs(
        @Parameter(description = "Number of days to look back", example = "7")
        @RequestParam(defaultValue = "7") int days,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String requester = jwt != null ? jwt.getSubject() : "unknown";
        log.debug("GET /api/v1/web/admin/menus/audit-logs/recent?days={} - requester: {}", days, requester);

        List<MenuAuditLog> auditLogs = menuAdminService.getRecentAuditLogs(days);
        return ResponseEntity.ok(auditLogs);
    }
}
