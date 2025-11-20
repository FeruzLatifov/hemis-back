package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Menu Audit Log Entity - Complete Audit Trail
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Track ALL changes to menu structure</li>
 *   <li>WHO changed WHAT and WHEN</li>
 *   <li>Before/After snapshots (old_value vs new_value)</li>
 *   <li>Compliance and security requirements</li>
 * </ul>
 *
 * <p><strong>Tracked Actions:</strong></p>
 * <ul>
 *   <li>CREATE - New menu item created</li>
 *   <li>UPDATE - Menu item modified (label, URL, icon, permission, etc.)</li>
 *   <li>DELETE - Menu item soft-deleted (deletedAt set)</li>
 *   <li>REORDER - Menu order changed (drag & drop)</li>
 *   <li>ACTIVATE - Menu item activated (active = true)</li>
 *   <li>DEACTIVATE - Menu item deactivated (active = false)</li>
 *   <li>RESTORE - Menu item restored from soft delete</li>
 * </ul>
 *
 * <p><strong>Industry Best Practice:</strong></p>
 * <ul>
 *   <li>Google Workspace Audit Logs pattern</li>
 *   <li>AWS CloudTrail pattern</li>
 *   <li>Immutable log entries (no updates/deletes)</li>
 *   <li>JSON snapshots for full context</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Entity
@Table(
    name = "menu_audit_logs",
    indexes = {
        @Index(name = "idx_menu_audit_menu_id", columnList = "menu_id"),
        @Index(name = "idx_menu_audit_action", columnList = "action"),
        @Index(name = "idx_menu_audit_changed_by", columnList = "changed_by"),
        @Index(name = "idx_menu_audit_changed_at", columnList = "changed_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuAuditLog {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * Audit log entry ID (UUID)
     * <p>Auto-generated on insert</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // =====================================================
    // Audit Fields
    // =====================================================

    /**
     * Menu ID that was changed
     * <p>Foreign key to menus table (nullable for bulk operations)</p>
     */
    @Column(name = "menu_id")
    private UUID menuId;

    /**
     * Action performed
     * <p>Values: CREATE, UPDATE, DELETE, REORDER, ACTIVATE, DEACTIVATE, RESTORE</p>
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Username who made the change
     * <p>From JWT token or authentication context</p>
     */
    @Column(name = "changed_by", nullable = false, length = 255)
    private String changedBy;

    /**
     * Timestamp of change
     * <p>Auto-set on insert</p>
     */
    @Column(name = "changed_at", nullable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();

    // =====================================================
    // Change Details (JSON Snapshots)
    // =====================================================

    /**
     * Old value (before change) as JSON
     *
     * <p><strong>Format:</strong></p>
     * <pre>
     * {
     *   "code": "dashboard",
     *   "i18nKey": "menu.dashboard",
     *   "url": "/dashboard",
     *   "icon": "home",
     *   "permission": "dashboard.view",
     *   "active": true,
     *   "orderNumber": 1
     * }
     * </pre>
     *
     * <p>NULL for CREATE action</p>
     */
    @Column(name = "old_value", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> oldValue;

    /**
     * New value (after change) as JSON
     *
     * <p><strong>Format:</strong> Same as oldValue</p>
     * <p>NULL for DELETE action</p>
     */
    @Column(name = "new_value", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> newValue;

    // =====================================================
    // Additional Context
    // =====================================================

    /**
     * IP address of requester (optional)
     * <p>For security tracking</p>
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent string (optional)
     * <p>Browser/client identification</p>
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Additional notes or reason for change (optional)
     * <p>Free-text field for admin comments</p>
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Check if this is a CREATE action
     *
     * @return true if action is CREATE
     */
    public boolean isCreateAction() {
        return "CREATE".equals(action);
    }

    /**
     * Check if this is an UPDATE action
     *
     * @return true if action is UPDATE
     */
    public boolean isUpdateAction() {
        return "UPDATE".equals(action);
    }

    /**
     * Check if this is a DELETE action
     *
     * @return true if action is DELETE
     */
    public boolean isDeleteAction() {
        return "DELETE".equals(action);
    }

    /**
     * Check if this is a REORDER action
     *
     * @return true if action is REORDER
     */
    public boolean isReorderAction() {
        return "REORDER".equals(action);
    }

    // =====================================================
    // Lifecycle Hooks
    // =====================================================

    /**
     * Set timestamp before persist
     */
    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    // =====================================================
    // Object Methods
    // =====================================================

    @Override
    public String toString() {
        return "MenuAuditLog{" +
            "id=" + id +
            ", menuId=" + menuId +
            ", action='" + action + '\'' +
            ", changedBy='" + changedBy + '\'' +
            ", changedAt=" + changedAt +
            '}';
    }

    // =====================================================
    // Constants
    // =====================================================

    /**
     * Action type constants
     */
    public static class Actions {
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String REORDER = "REORDER";
        public static final String ACTIVATE = "ACTIVATE";
        public static final String DEACTIVATE = "DEACTIVATE";
        public static final String RESTORE = "RESTORE";
        public static final String BULK_UPDATE = "BULK_UPDATE";
    }
}
