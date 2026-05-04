package uz.hemis.domain.entity.system;

import uz.hemis.domain.entity.security.Permission;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Menu Entity - Database-Driven Menu Structure
 *
 * <p><strong>CRITICAL - Dynamic Menu Management:</strong></p>
 * <ul>
 *   <li>Table: menus (NEW table for database-driven menu)</li>
 *   <li>Purpose: Store hierarchical menu structure with i18n support</li>
 *   <li>Industry Best Practice: Google Cloud / AWS / Netflix approach</li>
 *   <li>Admin UI support: CRUD operations via REST API</li>
 * </ul>
 *
 * <p><strong>Hierarchical Structure:</strong></p>
 * <ul>
 *   <li>Self-referencing: parent_id -> menus.id</li>
 *   <li>Unlimited nesting levels supported</li>
 *   <li>Order within same parent: order_number</li>
 *   <li>Root menus: parent_id IS NULL</li>
 * </ul>
 *
 * <p><strong>i18n Support:</strong></p>
 * <ul>
 *   <li>i18n_key references h_system_message_translation</li>
 *   <li>Translations in 4 languages: uz-UZ, oz-UZ, ru-RU, en-US</li>
 *   <li>No hardcoded labels in Java code</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>Permission-based filtering (null = public access)</li>
 *   <li>Active/inactive status (active = true/false)</li>
 *   <li>Soft delete support (deleted_at IS NULL)</li>
 * </ul>
 *
 * <p><strong>Cache Strategy:</strong></p>
 * <ul>
 *   <li>L1 Cache: Caffeine (in-memory, fast)</li>
 *   <li>L2 Cache: Redis (distributed, persistent)</li>
 *   <li>Cache invalidation on menu CRUD operations</li>
 *   <li>TTL: 1 hour (configurable)</li>
 * </ul>
 *
 * @see AuditableEntity
 * @since 2.0.0
 */
@Entity
@Table(name = "menu")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Menu extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Identity Fields
    // =====================================================

    /**
     * Menu code (unique identifier)
     * Column: code VARCHAR(100) NOT NULL UNIQUE
     *
     * <p>Unique menu identifier for programmatic access</p>
     * <p>Examples: "dashboard", "registry-e-reestr", "rating-university"</p>
     * <p>Naming convention: lowercase, hyphen-separated</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    /**
     * i18n translation key
     * Column: i18n_key VARCHAR(200) NOT NULL
     *
     * <p>Translation key for menu label (gettext model: English text as key)</p>
     * <p>References: system_message_translations table</p>
     * <p>Examples: "Dashboard", "E-Registry"</p>
     */
    @Column(name = "i18n_key", nullable = false, length = 200)
    private String i18nKey;

    // =====================================================
    // Navigation Fields
    // =====================================================

    /**
     * Navigation URL
     * Column: url VARCHAR(500)
     *
     * <p>Frontend route URL (null for parent menus with children)</p>
     * <p>Examples: "/dashboard", "/registry/e-reestr", null</p>
     */
    @Column(name = "url", length = 500)
    private String url;

    /**
     * Icon name
     * Column: icon VARCHAR(100)
     *
     * <p>Icon identifier for UI rendering</p>
     * <p>Examples: "home", "database", "star", "users"</p>
     * <p>Icon library: Application-specific (e.g., Ant Design, Material Icons)</p>
     */
    @Column(name = "icon", length = 100)
    private String icon;

    // =====================================================
    // Security Fields
    // =====================================================

    /**
     * Required permission
     * Column: permission VARCHAR(200)
     *
     * <p>Permission code required to view this menu item</p>
     * <p>NULL = public access (visible to all authenticated users)</p>
     * <p>Examples: "dashboard.view", "registry.e-reestr.view"</p>
     */
    @Column(name = "permission", length = 200)
    private String permission;

    // =====================================================
    // Hierarchical Structure
    // =====================================================

    /**
     * Parent menu (self-reference)
     *
     * <p>NULL = root level menu</p>
     * <p>NOT NULL = child menu item</p>
     * <p>Supports unlimited nesting levels</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_menu_parent"))
    private Menu parent;

    /**
     * Child menus
     *
     * <p>Automatically populated by JPA</p>
     * <p><strong>Cascade scope (refactored 2026-05-04):</strong> faqat PERSIST + MERGE.
     * REMOVE va orphanRemoval olib tashlandi — soft-delete'da parent yo'q qilinsa,
     * children'lar ham `delete_ts` qilinardi va parent restore'da hierarchy'ni qaytarib bo'lmasdi
     * (audit trail buziladi). Endi children alohida service-layer'da soft-delete qilinadi.</p>
     */
    @OneToMany(mappedBy = "parent",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false)
    @OrderBy("orderNumber ASC")
    private List<Menu> children = new ArrayList<>();

    // =====================================================
    // Ordering Fields
    // =====================================================

    /**
     * Display order
     * Column: order_number INTEGER NOT NULL DEFAULT 0
     *
     * <p>Order within same parent (lower = first)</p>
     * <p>Examples: 0, 1, 2, 10, 20</p>
     * <p>Allows gaps for future insertions</p>
     */
    @Column(name = "order_number", nullable = false)
    private Integer orderNumber = 0;

    // =====================================================
    // Classification Fields
    // =====================================================

    /**
     * Menu type (MAIN / SYSTEM)
     * Column: menu_type VARCHAR(20) NOT NULL DEFAULT 'main'
     *
     * <p>{@link MenuType#MAIN} = regular menu (default, shown in main section)</p>
     * <p>{@link MenuType#SYSTEM} = system menu (shown below separator)</p>
     * <p>Allows frontend to separate menu sections without hardcoded logic</p>
     */
    @Convert(converter = MenuType.Converter.class)
    @Column(name = "menu_type", nullable = false, length = 20)
    private MenuType menuType = MenuType.MAIN;

    // =====================================================
    // Status Fields
    // =====================================================

    /**
     * Active status
     * Column: active BOOLEAN NOT NULL DEFAULT true
     *
     * <p>true = visible to users (default)</p>
     * <p>false = hidden from users (soft hide)</p>
     * <p>Use this instead of hard delete for temporary hide</p>
     */
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Check if this is a root menu (no parent)
     *
     * @return true if parent is null
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Check if this menu has children
     *
     * @return true if children list is not empty
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Add child menu
     *
     * @param child child menu to add
     */
    public void addChild(Menu child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.setParent(this);
    }

    /**
     * Remove child menu
     *
     * @param child child menu to remove
     */
    public void removeChild(Menu child) {
        if (children != null) {
            children.remove(child);
            child.setParent(null);
        }
    }

    /**
     * Get parent ID (null-safe)
     *
     * @return parent UUID or null
     */
    public UUID getParentId() {
        return parent != null ? parent.getId() : null;
    }

    // =====================================================
    // Validation
    // =====================================================

    /**
     * Validate menu before persist/update
     *
     * <p>Called by JPA lifecycle hooks</p>
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        // Code must not be empty
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Menu code cannot be empty");
        }

        // i18n key must not be empty
        if (i18nKey == null || i18nKey.trim().isEmpty()) {
            throw new IllegalStateException("Menu i18n key cannot be empty");
        }

        // Order number defaults to 0
        if (orderNumber == null) {
            orderNumber = 0;
        }

        // Active defaults to true
        if (active == null) {
            active = true;
        }
    }
}
