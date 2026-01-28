package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Permission Entity - Granular Permission Management
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Define granular permissions using resource.action format</li>
 *   <li>Clean, human-readable permission system</li>
 *   <li>Independent from CUBA Platform's sec_permission</li>
 * </ul>
 *
 * <p><strong>Table:</strong> hemishe_permission</p>
 *
 * <p><strong>Permission Format:</strong></p>
 * <ul>
 *   <li>Code: resource.action (e.g., "students.view", "reports.create")</li>
 *   <li>Resource: Entity/module name (students, teachers, reports, etc.)</li>
 *   <li>Action: Operation (view, create, edit, delete, manage, export)</li>
 * </ul>
 *
 * <p><strong>Examples:</strong></p>
 * <ul>
 *   <li>students.view - View student list and details</li>
 *   <li>students.create - Add new students</li>
 *   <li>reports.export - Export reports to Excel/PDF</li>
 *   <li>users.manage - Full user management</li>
 * </ul>
 *
 * <p><strong>Categories:</strong></p>
 * <ul>
 *   <li>CORE - Essential business entities (students, teachers)</li>
 *   <li>REPORTS - Reporting functionality</li>
 *   <li>ADMIN - Administrative features (users, roles)</li>
 *   <li>INTEGRATION - External API integrations</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * Primary Key (UUID)
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    // =====================================================
    // Permission Identification
    // =====================================================

    /**
     * Resource (entity/module name)
     * <p>Examples: students, teachers, reports, universities, users</p>
     */
    @Column(name = "resource", nullable = false, length = 100)
    private String resource;

    /**
     * Action (operation)
     * <p>Examples: view, create, edit, delete, manage, export</p>
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Full permission code (resource.action)
     * <p>Auto-generated from resource + action</p>
     * <p>Examples: students.view, reports.create, users.manage</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 200)
    private String code;

    /**
     * Permission name (human-readable)
     * <p>Examples: View Students, Create Reports, Manage Users</p>
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Full description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Category (for organization)
     * <p>Values: CORE, REPORTS, ADMIN, INTEGRATION</p>
     */
    @Column(name = "category", length = 50)
    private String category;

    // =====================================================
    // Audit Fields (CUBA Standard Pattern)
    // =====================================================

    /**
     * Creation timestamp
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Version (optimistic locking)
     */
    @Version
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    // =====================================================
    // Relationships
    // =====================================================

    /**
     * Roles that have this permission
     */
    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if permission is active (not deleted)
     *
     * @return true if permission is not soft-deleted
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Check if permission is deleted (soft delete)
     *
     * @return true if deleted_at is not null
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if this is a read-only permission (action = 'view')
     *
     * @return true if action is 'view'
     */
    public boolean isReadOnly() {
        return "view".equals(action);
    }

    /**
     * Check if this is a write permission (create, edit, delete, manage)
     *
     * @return true if action is create/edit/delete/manage
     */
    public boolean isWritePermission() {
        return "create".equals(action)
            || "edit".equals(action)
            || "delete".equals(action)
            || "manage".equals(action);
    }

    /**
     * Check if this is an admin permission (category = ADMIN)
     *
     * @return true if category is ADMIN
     */
    public boolean isAdminPermission() {
        return "ADMIN".equals(category);
    }

    /**
     * Generate permission code from resource and action
     * <p>Format: resource.action</p>
     *
     * @return Generated code
     */
    public String generateCode() {
        if (resource != null && action != null) {
            return resource + "." + action;
        }
        return null;
    }

    // =====================================================
    // PrePersist Hook
    // =====================================================

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (version == null) {
            version = 1;
        }
        // Auto-generate code if not set
        if (code == null) {
            code = generateCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Update code if resource or action changed
        if (code == null || !code.equals(generateCode())) {
            code = generateCode();
        }
    }

    // =====================================================
    // Equals & HashCode (based on ID)
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
