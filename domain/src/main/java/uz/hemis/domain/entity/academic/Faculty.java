package uz.hemis.domain.entity.academic;

import uz.hemis.domain.entity.base.BaseEntity;
import uz.hemis.domain.entity.university.University;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Faculty Entity - Mapped to hemishe_e_faculty table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_faculty</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @SQLRestriction("delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_faculty")
@SQLRestriction("delete_ts IS NULL")
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

    // =====================================================
    // University Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * University code — references {@code hemishe_e_university.code} (VARCHAR PK).
     * Underscore prefix preserved from legacy schema.
     */
    @Column(name = "_university", length = 255)
    private String university;

    /** Faculty active if not soft-deleted. */
    public boolean isActive() {
        return !isDeleted();
    }

    @Override
    public String toString() {
        return "Faculty{id=" + getId() + ", code='" + code + "', university='" + university + "'}";
    }
}
