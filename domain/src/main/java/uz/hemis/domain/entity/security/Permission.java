package uz.hemis.domain.entity.security;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.converter.PermissionActionConverter;
import uz.hemis.domain.entity.base.AuditableEntity;
import uz.hemis.domain.entity.enums.PermissionAction;
import uz.hemis.domain.entity.enums.PermissionCategory;

import java.util.HashSet;
import java.util.Set;

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
@Table(name = "permission")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends AuditableEntity {

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
     * Action (operation) — stored as lowercase in DB via {@link PermissionActionConverter}.
     */
    @Convert(converter = PermissionActionConverter.class)
    @Column(name = "action", nullable = false, length = 50)
    private PermissionAction action;

    /**
     * Full permission code (resource.action)
     * <p>Auto-generated from resource + action</p>
     * <p>Examples: students.view, reports.create, users.manage</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 255)
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
     * Category — matches DB CHECK constraint.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    @Builder.Default
    private PermissionCategory category = PermissionCategory.CUSTOM;

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
        return getDeletedAt() == null;
    }

    /** @return true if action is {@link PermissionAction#VIEW} */
    public boolean isReadOnly() {
        return PermissionAction.VIEW == action;
    }

    /** @return true if action is a write operation (CREATE, EDIT, DELETE, MANAGE, APPROVE) */
    public boolean isWritePermission() {
        return action == PermissionAction.CREATE
            || action == PermissionAction.EDIT
            || action == PermissionAction.DELETE
            || action == PermissionAction.MANAGE
            || action == PermissionAction.APPROVE;
    }

    /** @return true if category is {@link PermissionCategory#ADMIN} */
    public boolean isAdminPermission() {
        return PermissionCategory.ADMIN == category;
    }

    /**
     * Generate permission code from resource and action.
     * <p>Format: {@code resource.action} (action stored lowercase).</p>
     */
    public String generateCode() {
        if (resource != null && action != null) {
            return resource + "." + action.name().toLowerCase();
        }
        return null;
    }

    // =====================================================
    // PrePersist / PreUpdate Hooks
    // =====================================================

    @Override
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        // Auto-generate code if not set
        if (code == null) {
            code = generateCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Update code if resource or action changed
        if (code == null || !code.equals(generateCode())) {
            code = generateCode();
        }
    }

    @Override
    public String toString() {
        return "Permission{id=" + getId() + ", code='" + code + "', category=" + category + '}';
    }
}
