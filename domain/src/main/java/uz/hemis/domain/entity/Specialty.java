package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * Specialty Entity - Mapped to hemishe_e_specialty table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_specialty</li>
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
@Table(name = "hemishe_e_specialty")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Specialty extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Specialty code (unique within university)
     * Column: code VARCHAR(255)
     */
    @Column(name = "code", length = 255)
    private String code;

    /**
     * Specialty name
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
    // Faculty Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Faculty ID
     * Column: _faculty UUID
     * References: hemishe_e_faculty.id
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_faculty")
    private UUID faculty;

    // =====================================================
    // Classifiers (LEGACY FIELDS WITH _)
    // =====================================================

    /**
     * Specialty type code
     * Column: _specialty_type VARCHAR(32)
     * References: hemishe_h_specialty_type.code
     */
    @Column(name = "_specialty_type", length = 32)
    private String specialtyType;

    /**
     * Education type code
     * Column: _education_type VARCHAR(32)
     * References: hemishe_h_education_type.code
     * Examples: '11' = Bachelor, '12' = Master, '13' = PhD
     */
    @Column(name = "_education_type", length = 32)
    private String educationType;

    /**
     * Education form code
     * Column: _education_form VARCHAR(32)
     * References: hemishe_h_education_form.code
     * Examples: '11' = Full-time, '12' = Part-time, '13' = Evening, '14' = Distance
     */
    @Column(name = "_education_form", length = 32)
    private String educationForm;

    /**
     * Study period code
     * Column: _study_period VARCHAR(32)
     * References: hemishe_h_study_period.code
     */
    @Column(name = "_study_period", length = 32)
    private String studyPeriod;

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
     * Check if specialty is active
     *
     * @return true if active flag is true AND not deleted
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    @Override
    public String toString() {
        return "Specialty{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", university='" + university + '\'' +
                ", faculty=" + faculty +
                ", active=" + active +
                '}';
    }
}
