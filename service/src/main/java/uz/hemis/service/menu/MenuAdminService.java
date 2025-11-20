package uz.hemis.service.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Menu;
import uz.hemis.domain.entity.MenuAuditLog;
import uz.hemis.domain.repository.MenuAuditLogRepository;
import uz.hemis.domain.repository.MenuRepository;
import uz.hemis.service.mapper.MenuMapper;
import uz.hemis.service.menu.dto.MenuAdminRequest;
import uz.hemis.service.menu.dto.MenuAdminResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu Administration Service
 *
 * <p>Provides CRUD operations for menu management with cache invalidation</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Complete CRUD operations (Create, Read, Update, Delete)</li>
 *   <li>Hierarchical menu structure support</li>
 *   <li>Soft delete pattern (preserves audit trail)</li>
 *   <li>Automatic cache invalidation on mutations</li>
 *   <li>Input validation and business rule enforcement</li>
 *   <li>Transaction management for data consistency</li>
 * </ul>
 *
 * <p><strong>Cache Strategy:</strong></p>
 * <ul>
 *   <li>All mutation operations (create/update/delete/toggle) evict menu cache</li>
 *   <li>Read operations do not cache (admin queries are infrequent)</li>
 *   <li>Cache cleared manually via clearCache() endpoint</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>Controller-level: @PreAuthorize("hasAuthority('system.menus.manage')")</li>
 *   <li>Service-level: Input validation via Jakarta Bean Validation</li>
 *   <li>Database-level: Unique constraints, foreign keys</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuAdminService {

    private final MenuRepository menuRepository;
    private final MenuAuditLogRepository auditLogRepository;
    private final MenuMapper menuMapper;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;
    private final MenuService menuService;

    /**
     * Maximum depth for recursive menu loading
     * Industry standard: 7 levels (root → L1 → L2 → L3 → L4 → L5 → L6)
     * Prevents StackOverflowError and performance degradation
     */
    private static final int MAX_MENU_DEPTH = 7;

    // =====================================================
    // READ OPERATIONS (no cache, real-time admin data)
    // =====================================================

    /**
     * Get all menus including inactive and deleted ones
     *
     * <p>Returns complete menu tree for administration purposes</p>
     * <p>Includes soft-deleted items (deletedAt != null)</p>
     *
     * @return list of all menus with hierarchical structure
     */
    @Transactional(readOnly = true)
    public List<MenuAdminResponse> getAllMenus() {
        log.debug("Getting all menus for admin (including inactive/deleted)");

        List<Menu> allMenus = menuRepository.findAllForAdmin();
        List<MenuAdminResponse> responses = menuMapper.toResponseList(allMenus);

        // Build hierarchical structure (start at depth 0)
        responses.forEach(response -> loadChildrenRecursively(response, 0));

        log.debug("Found {} total menus", responses.size());
        return responses;
    }

    /**
     * Get menu by ID
     *
     * @param id menu UUID
     * @return menu details with children
     * @throws ResourceNotFoundException if menu not found
     */
    @Transactional(readOnly = true)
    public MenuAdminResponse getMenuById(UUID id) {
        log.debug("Getting menu by ID: {}", id);

        Menu menu = menuRepository.findByIdForAdmin(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));

        MenuAdminResponse response = menuMapper.toResponse(menu);
        loadChildrenRecursively(response, 0);

        return response;
    }

    /**
     * Get menu by code
     *
     * @param code menu code
     * @return menu details
     * @throws ResourceNotFoundException if menu not found
     */
    @Transactional(readOnly = true)
    public MenuAdminResponse getMenuByCode(String code) {
        log.debug("Getting menu by code: {}", code);

        Menu menu = menuRepository.findByCodeForAdmin(code)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with code: " + code));

        MenuAdminResponse response = menuMapper.toResponse(menu);
        loadChildrenRecursively(response, 0);

        return response;
    }

    // =====================================================
    // CREATE OPERATION
    // =====================================================

    /**
     * Create new menu item
     *
     * <p>Validates:</p>
     * <ul>
     *   <li>Unique code constraint</li>
     *   <li>Parent menu exists (if parentId provided)</li>
     *   <li>No circular references</li>
     * </ul>
     *
     * <p>Automatically evicts menu cache after creation</p>
     *
     * @param request menu creation request
     * @return created menu with generated ID
     * @throws BadRequestException if validation fails
     */
    @Transactional
    @CacheEvict(value = "menu", allEntries = true)
    public MenuAdminResponse createMenu(MenuAdminRequest request) {
        log.info("Creating new menu: {}", request.getCode());

        // Validate unique code
        if (menuRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Menu with code '" + request.getCode() + "' already exists");
        }

        // Convert DTO to entity
        Menu menu = menuMapper.toEntity(request);

        // Set parent if provided
        if (request.getParentId() != null) {
            Menu parent = menuRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BadRequestException("Parent menu not found with ID: " + request.getParentId()));
            menu.setParent(parent);
        }

        // Save to database
        try {
            Menu saved = menuRepository.save(menu);
            log.info("✅ Menu created successfully: {} (ID: {})", saved.getCode(), saved.getId());

            // ✅ NEW: Log audit trail
            logAudit(saved.getId(), MenuAuditLog.Actions.CREATE, null, entityToMap(saved));

            // Clear cache
            clearMenuCache();

            return menuMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("❌ Failed to create menu due to constraint violation", e);
            throw new BadRequestException("Failed to create menu: " + e.getMessage());
        }
    }

    // =====================================================
    // UPDATE OPERATION
    // =====================================================

    /**
     * Update existing menu item
     *
     * <p>Validates:</p>
     * <ul>
     *   <li>Menu exists</li>
     *   <li>Code unique (if changed)</li>
     *   <li>Parent exists (if parentId changed)</li>
     *   <li>No circular references (cannot set self or descendant as parent)</li>
     * </ul>
     *
     * <p>Automatically evicts menu cache after update</p>
     *
     * @param id menu ID to update
     * @param request updated menu data
     * @return updated menu
     * @throws ResourceNotFoundException if menu not found
     * @throws BadRequestException if validation fails
     */
    @Transactional
    @CacheEvict(value = "menu", allEntries = true)
    public MenuAdminResponse updateMenu(UUID id, MenuAdminRequest request) {
        log.info("Updating menu ID: {}", id);

        // Find existing menu
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));

        // Validate code uniqueness (if changed)
        if (!menu.getCode().equals(request.getCode())) {
            if (menuRepository.existsByCode(request.getCode())) {
                throw new BadRequestException("Menu with code '" + request.getCode() + "' already exists");
            }
        }

        // Update parent if changed
        UUID currentParentId = menu.getParent() != null ? menu.getParent().getId() : null;
        if (!Objects.equals(currentParentId, request.getParentId())) {
            if (request.getParentId() != null) {
                // Validate parent exists
                Menu newParent = menuRepository.findById(request.getParentId())
                        .orElseThrow(() -> new BadRequestException("Parent menu not found with ID: " + request.getParentId()));

                // Prevent circular reference
                if (isCircularReference(id, request.getParentId())) {
                    throw new BadRequestException("Circular reference detected: cannot set descendant as parent");
                }

                menu.setParent(newParent);
            } else {
                menu.setParent(null); // Make it root menu
            }
        }

        // ✅ NEW: Snapshot old value before update
        Map<String, Object> oldValue = entityToMap(menu);

        // Update fields via mapper
        menuMapper.updateEntityFromRequest(request, menu);

        // Save to database
        try {
            Menu updated = menuRepository.save(menu);
            log.info("✅ Menu updated successfully: {} (ID: {})", updated.getCode(), updated.getId());

            // ✅ NEW: Log audit trail
            logAudit(updated.getId(), MenuAuditLog.Actions.UPDATE, oldValue, entityToMap(updated));

            // Clear cache
            clearMenuCache();

            return menuMapper.toResponse(updated);
        } catch (DataIntegrityViolationException e) {
            log.error("❌ Failed to update menu due to constraint violation", e);
            throw new BadRequestException("Failed to update menu: " + e.getMessage());
        }
    }

    // =====================================================
    // DELETE OPERATION (Soft Delete)
    // =====================================================

    /**
     * Soft delete menu (sets deletedAt timestamp)
     *
     * <p>Preserves audit trail and allows recovery</p>
     * <p>Also soft-deletes all child menus recursively</p>
     * <p>Automatically evicts menu cache after deletion</p>
     *
     * @param id menu ID to delete
     * @throws ResourceNotFoundException if menu not found
     */
    @Transactional
    @CacheEvict(value = "menu", allEntries = true)
    public void deleteMenu(UUID id) {
        log.info("Soft deleting menu ID: {}", id);

        // Find menu
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));

        // ✅ NEW: Snapshot old value
        Map<String, Object> oldValue = entityToMap(menu);

        // Soft delete (set deletedAt timestamp)
        menu.setDeletedAt(LocalDateTime.now());
        menuRepository.save(menu);

        // Also soft delete all children recursively
        deleteChildrenRecursively(menu);

        log.info("✅ Menu soft deleted: {} (ID: {})", menu.getCode(), id);

        // ✅ NEW: Log audit trail
        logAudit(id, MenuAuditLog.Actions.DELETE, oldValue, null);

        // Clear cache
        clearMenuCache();
    }

    // =====================================================
    // TOGGLE ACTIVE STATUS
    // =====================================================

    /**
     * Toggle menu active status
     *
     * <p>Changes active = true ↔ false</p>
     * <p>Automatically evicts menu cache after toggle</p>
     *
     * @param id menu ID
     * @return updated menu
     * @throws ResourceNotFoundException if menu not found
     */
    @Transactional
    @CacheEvict(value = "menu", allEntries = true)
    public MenuAdminResponse toggleActive(UUID id) {
        log.info("Toggling active status for menu ID: {}", id);

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));

        // ✅ NEW: Snapshot old value
        Map<String, Object> oldValue = entityToMap(menu);

        menu.setActive(!menu.getActive());
        Menu updated = menuRepository.save(menu);

        log.info("✅ Menu active status toggled: {} -> {}", menu.getCode(), updated.getActive());

        // ✅ NEW: Log audit trail
        String action = updated.getActive() ? MenuAuditLog.Actions.ACTIVATE : MenuAuditLog.Actions.DEACTIVATE;
        logAudit(id, action, oldValue, entityToMap(updated));

        // Clear cache
        clearMenuCache();

        return menuMapper.toResponse(updated);
    }

    // =====================================================
    // REORDER OPERATION
    // =====================================================

    /**
     * Change menu display order
     *
     * <p>Updates orderNumber field</p>
     * <p>Automatically evicts menu cache after reorder</p>
     *
     * @param id menu ID
     * @param newOrder new order number
     * @throws ResourceNotFoundException if menu not found
     */
    @Transactional
    @CacheEvict(value = "menu", allEntries = true)
    public void reorderMenu(UUID id, Integer newOrder) {
        log.info("Reordering menu ID: {} to order: {}", id, newOrder);

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));

        // ✅ NEW: Snapshot old value
        Map<String, Object> oldValue = entityToMap(menu);

        menu.setOrderNumber(newOrder);
        menuRepository.save(menu);

        log.info("✅ Menu reordered: {} -> order {}", menu.getCode(), newOrder);

        // ✅ NEW: Log audit trail
        logAudit(id, MenuAuditLog.Actions.REORDER, oldValue, entityToMap(menu));

        // Clear cache
        clearMenuCache();
    }

    // =====================================================
    // CACHE MANAGEMENT
    // =====================================================

    /**
     * Manually clear all menu cache
     *
     * <p>Clears both L1 (Caffeine) and L2 (Redis) cache</p>
     * <p>Use this when cache becomes stale or after direct database updates</p>
     */
    @CacheEvict(value = "menu", allEntries = true)
    public void clearCache() {
        log.info("Manually clearing menu cache");
        clearMenuCache();
        log.info("✅ Menu cache cleared successfully");
    }

    // =====================================================
    // PRIVATE HELPER METHODS
    // =====================================================

    /**
     * Load children recursively for hierarchical structure
     *
     * @param response menu response to load children for
     * @param currentDepth current recursion depth (0-based)
     */
    private void loadChildrenRecursively(MenuAdminResponse response, int currentDepth) {
        // ✅ FIX: Prevent StackOverflowError with depth limit
        if (currentDepth >= MAX_MENU_DEPTH) {
            log.warn("⚠️ Maximum menu depth ({}) reached for menu: {}", MAX_MENU_DEPTH, response.getId());
            return;
        }

        if (response.getId() == null) return;

        List<Menu> children = menuRepository.findByParentId(response.getId());
        if (!children.isEmpty()) {
            List<MenuAdminResponse> childResponses = menuMapper.toResponseList(children);
            // ✅ FIX: Pass incremented depth to prevent infinite recursion
            childResponses.forEach(child -> loadChildrenRecursively(child, currentDepth + 1));
            response.setChildren(childResponses);
        }
    }

    /**
     * Soft delete all children recursively
     *
     * <p>✅ FIX #11: Use findAllChildrenByParentId() to include inactive children</p>
     * <p>Previously used findByParentId() which only returned active=true children,
     * causing inactive children to never be deleted</p>
     */
    private void deleteChildrenRecursively(Menu parent) {
        // ✅ FIX: Use admin query that includes inactive children
        List<Menu> children = menuRepository.findAllChildrenByParentId(parent.getId());
        for (Menu child : children) {
            child.setDeletedAt(LocalDateTime.now());
            menuRepository.save(child);
            deleteChildrenRecursively(child); // Recursive
        }
    }

    /**
     * Check if setting newParentId as parent of menuId would create circular reference
     *
     * @param menuId current menu ID
     * @param newParentId proposed parent ID
     * @return true if circular reference detected
     */
    private boolean isCircularReference(UUID menuId, UUID newParentId) {
        if (menuId.equals(newParentId)) {
            return true; // Cannot set self as parent
        }

        // Check if newParentId is a descendant of menuId
        Menu parent = menuRepository.findById(newParentId).orElse(null);
        while (parent != null) {
            if (parent.getId().equals(menuId)) {
                return true; // Circular reference detected
            }
            parent = parent.getParent();
        }

        return false;
    }

    /**
     * Clear menu cache (L1 + L2) and invalidate across all pods
     *
     * <p><strong>Cache Invalidation Strategy:</strong></p>
     * <ul>
     *   <li>Step 1: Clear local cache (Caffeine/Redis)</li>
     *   <li>Step 2: Increment cache version + Publish Redis Pub/Sub event</li>
     *   <li>Step 3: All pods receive event → clear their L1 Caffeine cache</li>
     *   <li>Step 4: Next request: cache miss → reload from database</li>
     * </ul>
     */
    private void clearMenuCache() {
        // Step 1: Clear local cache (current pod)
        var menuCache = cacheManager.getCache("menu");
        if (menuCache != null) {
            menuCache.clear();
            log.debug("Menu cache cleared (local pod)");
        }

        // Step 2: Invalidate cache across all pods (Redis Pub/Sub)
        menuService.invalidateMenuCache();
    }

    // =====================================================
    // AUDIT TRAIL HELPERS (NEW v2.0)
    // =====================================================

    /**
     * Log audit trail entry
     *
     * <p>Records WHO changed WHAT and WHEN</p>
     * <p>Includes before/after snapshots for complete audit trail</p>
     *
     * @param menuId Menu ID (null for bulk operations)
     * @param action Action type (CREATE, UPDATE, DELETE, etc.)
     * @param oldValue Before snapshot (null for CREATE)
     * @param newValue After snapshot (null for DELETE)
     */
    private void logAudit(UUID menuId, String action, Map<String, Object> oldValue, Map<String, Object> newValue) {
        try {
            MenuAuditLog auditLog = MenuAuditLog.builder()
                .menuId(menuId)
                .action(action)
                .changedBy(getCurrentUsername())
                .changedAt(LocalDateTime.now())
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("✅ Audit logged: action={}, menuId={}", action, menuId);

        } catch (Exception e) {
            log.error("❌ Failed to log audit trail (non-critical)", e);
            // Don't fail transaction if audit logging fails
        }
    }

    /**
     * Convert Menu entity to Map (for audit snapshots)
     *
     * <p>Creates JSON-friendly representation of menu state</p>
     *
     * @param menu Menu entity
     * @return Map representation
     */
    private Map<String, Object> entityToMap(Menu menu) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", menu.getId());
        map.put("code", menu.getCode());
        map.put("i18nKey", menu.getI18nKey());
        map.put("url", menu.getUrl());
        map.put("icon", menu.getIcon());
        map.put("permission", menu.getPermission());
        map.put("parentId", menu.getParentId());
        map.put("orderNumber", menu.getOrderNumber());
        map.put("active", menu.getActive());
        map.put("createdBy", menu.getCreatedBy());
        map.put("updatedBy", menu.getUpdatedBy());
        return map;
    }

    /**
     * Get current authenticated username from SecurityContext
     *
     * @return Username or "system" if not authenticated
     */
    private String getCurrentUsername() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
            return "system";
        } catch (Exception e) {
            log.warn("Failed to get current username, using 'system'");
            return "system";
        }
    }

    // =====================================================
    // EXPORT/IMPORT (NEW v2.0)
    // =====================================================

    /**
     * Export entire menu structure as JSON
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Backup menu configuration</li>
     *   <li>Transfer menu between environments (dev → prod)</li>
     *   <li>Version control (commit JSON to git)</li>
     *   <li>Disaster recovery</li>
     * </ul>
     *
     * @return JSON string of all menus
     */
    @Transactional(readOnly = true)
    public String exportMenuStructure() {
        log.info("📤 Exporting menu structure");

        List<Menu> allMenus = menuRepository.findAllForAdmin();

        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(allMenus);
            log.info("✅ Exported {} menu items", allMenus.size());
            return json;
        } catch (Exception e) {
            log.error("❌ Export failed", e);
            throw new RuntimeException("Failed to export menu structure", e);
        }
    }

    /**
     * Import menu structure from JSON
     *
     * <p><strong>WARNING:</strong> This operation is destructive!</p>
     * <ul>
     *   <li>Does NOT delete existing menus</li>
     *   <li>Inserts new menus from JSON</li>
     *   <li>Updates existing menus (matched by code)</li>
     *   <li>Generates new UUIDs for all imports</li>
     * </ul>
     *
     * <p><strong>Recommended:</strong> Backup before import!</p>
     *
     * @param json JSON string (array of menu objects)
     * @return Number of imported items
     * @throws BadRequestException if JSON is invalid
     */
    @Transactional
    @CacheEvict(value = "menu", allEntries = true)
    public int importMenuStructure(String json) {
        log.warn("⚠️  Importing menu structure from JSON");

        try {
            // Parse JSON
            List<Menu> importedMenus = objectMapper.readValue(
                json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Menu.class)
            );

            int count = 0;
            for (Menu menu : importedMenus) {
                // Check if menu with same code exists
                Optional<Menu> existingOpt = menuRepository.findByCodeForAdmin(menu.getCode());

                if (existingOpt.isPresent()) {
                    // Update existing menu
                    Menu existing = existingOpt.get();
                    Map<String, Object> oldValue = entityToMap(existing);

                    existing.setI18nKey(menu.getI18nKey());
                    existing.setUrl(menu.getUrl());
                    existing.setIcon(menu.getIcon());
                    existing.setPermission(menu.getPermission());
                    existing.setOrderNumber(menu.getOrderNumber());
                    existing.setActive(menu.getActive());
                    existing.setUpdatedBy(getCurrentUsername());

                    menuRepository.save(existing);
                    logAudit(existing.getId(), "IMPORT_UPDATE", oldValue, entityToMap(existing));
                    count++;

                } else {
                    // Create new menu
                    menu.setId(null);  // Generate new UUID
                    menu.setCreatedBy(getCurrentUsername());
                    Menu saved = menuRepository.save(menu);
                    logAudit(saved.getId(), "IMPORT_CREATE", null, entityToMap(saved));
                    count++;
                }
            }

            log.info("✅ Imported {} menu items", count);

            // Invalidate cache
            clearMenuCache();

            return count;

        } catch (Exception e) {
            log.error("❌ Import failed", e);
            throw new BadRequestException("Failed to import menu structure: " + e.getMessage());
        }
    }

    /**
     * Get audit logs for specific menu
     *
     * @param menuId Menu ID
     * @return List of audit logs (newest first)
     */
    @Transactional(readOnly = true)
    public List<MenuAuditLog> getAuditLogs(UUID menuId) {
        return auditLogRepository.findByMenuId(menuId);
    }

    /**
     * Get recent audit logs (all menus)
     *
     * @param days Number of days to look back
     * @return List of recent audit logs
     */
    @Transactional(readOnly = true)
    public List<MenuAuditLog> getRecentAuditLogs(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return auditLogRepository.findRecentChanges(since);
    }
}
