package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

/**
 * Faculty Entity - Mapped to hemishe_e_faculty table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_faculty</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_faculty")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Faculty extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Faculty code (unique within university)
     * Column: code VARCHAR(255)
     */
    @Column(name = "code", length = 255)
    private String code;

    /**
     * Faculty name
     * Column: name VARCHAR(1024)
     */
    @Column(name = "name", length = 1024)
    private String name;

    /**
     * Short name
     * Column: short_name VARCHAR(255)
     */
    @Column(name = "short_name", length = 255)
    private String shortName;

    // =====================================================
    // University Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * University code
     * Column: _university VARCHAR(255)
     * References: hemishe_e_university.code (VARCHAR PK)
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_university", length = 255)
    private String university;

    // =====================================================
    // Classifiers (LEGACY FIELDS WITH _)
    // =====================================================

    /**
     * Faculty type code
     * Column: _faculty_type VARCHAR(32)
     * References: hemishe_h_faculty_type.code
     */
    @Column(name = "_faculty_type", length = 32)
    private String facultyType;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Active flag
     * Column: active BOOLEAN
     */
    @Column(name = "active")
    private Boolean active;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if faculty is active
     *
     * @return true if active flag is true AND not deleted
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    @Override
    public String toString() {
        return "Faculty{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", university='" + university + '\'' +
                ", active=" + active +
                '}';
    }
}
