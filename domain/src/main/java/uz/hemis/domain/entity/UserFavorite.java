package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserFavorite Entity - User Quick Access Favorites
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Table: user_favorites (NEW table for user favorite menu items)</li>
 *   <li>Stores user's pinned/favorite menu items for quick access</li>
 *   <li>Ordered by order_number for custom user arrangement</li>
 *   <li>Maximum 10 favorites per user</li>
 * </ul>
 *
 * <p><strong>Relationships:</strong></p>
 * <ul>
 *   <li>user_id → users.id (UUID, the authenticated user)</li>
 *   <li>menu_code → menus.code (VARCHAR, references menu item)</li>
 *   <li>Unique constraint: (user_id, menu_code) - one favorite per menu per user</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong></p>
 * <ul>
 *   <li>Each user can have at most 10 favorites</li>
 *   <li>Favorites are ordered by order_number (lower = first)</li>
 *   <li>Duplicate menu_code per user is not allowed</li>
 *   <li>No soft delete - favorites are physically deleted</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Entity
@Table(
    name = "user_favorites",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_user_favorites_user_menu",
            columnNames = {"user_id", "menu_code"}
        )
    },
    indexes = {
        @Index(name = "idx_user_favorites_user_id", columnList = "user_id"),
        @Index(name = "idx_user_favorites_user_order", columnList = "user_id, order_number")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class UserFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Identity Fields
    // =====================================================

    /**
     * Primary key - UUID
     * Column: id UUID NOT NULL
     *
     * <p>Auto-generated UUID primary key</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // =====================================================
    // Relationship Fields
    // =====================================================

    /**
     * User ID (owner of this favorite)
     * Column: user_id UUID NOT NULL
     *
     * <p>References the authenticated user's ID</p>
     * <p>Extracted from JWT subject claim</p>
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Menu code (the favorited menu item)
     * Column: menu_code VARCHAR(100) NOT NULL
     *
     * <p>References menus.code for the favorite menu item</p>
     * <p>Examples: "dashboard", "registry-e-reestr", "rating-university"</p>
     */
    @Column(name = "menu_code", nullable = false, length = 100)
    private String menuCode;

    // =====================================================
    // Ordering Fields
    // =====================================================

    /**
     * Display order
     * Column: order_number INTEGER NOT NULL DEFAULT 0
     *
     * <p>Order within user's favorites list (lower = first)</p>
     * <p>Users can reorder their favorites via drag-and-drop</p>
     */
    @Column(name = "order_number", nullable = false)
    private Integer orderNumber = 0;

    // =====================================================
    // Audit Fields
    // =====================================================

    /**
     * Creation timestamp
     * Column: created_at TIMESTAMP NOT NULL
     *
     * <p>Set automatically when favorite is added</p>
     * <p>Not updatable after creation</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =====================================================
    // JPA Lifecycle Hooks
    // =====================================================

    /**
     * Set creation timestamp before INSERT
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // =====================================================
    // Object Methods
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFavorite)) return false;
        UserFavorite that = (UserFavorite) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserFavorite{" +
            "id=" + id +
            ", userId=" + userId +
            ", menuCode='" + menuCode + '\'' +
            ", orderNumber=" + orderNumber +
            ", createdAt=" + createdAt +
            '}';
    }
}
