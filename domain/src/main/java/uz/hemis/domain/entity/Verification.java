package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

/**
 * Verification Entity - DTM verification ballari
 *
 * <p>Table: hemishe_e_verification</p>
 * <p>DTM imtihon ballari va tasdiqlash ma'lumotlari</p>
 *
 * <p>OLD-HEMIS response formatida:</p>
 * <pre>
 * {
 *   "_entityName": "hemishe_EVerification",
 *   "id": "...",
 *   "pinfl": "...",
 *   "points": "69.3",
 *   "paymentForm": {...},
 *   "educationType": {...},
 *   "university": {...},
 *   "educationYear": {...},
 *   "category": {...}
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_verification")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Verification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * PINFL - Personal Identification Number
     */
    @Column(name = "pinfl", length = 255)
    private String pinfl;

    /**
     * University code (FK to hemishe_e_university.code)
     */
    @Column(name = "_university", length = 255)
    private String university;

    /**
     * Education year code (FK to hemishe_h_education_year.code)
     */
    @Column(name = "_education_year", length = 32)
    private String educationYear;

    /**
     * Education type code (FK to hemishe_h_education_type.code)
     */
    @Column(name = "_education_type", length = 32)
    private String educationType;

    /**
     * Payment form code (FK to hemishe_h_payment_form.code)
     */
    @Column(name = "_payment_form", length = 32)
    private String paymentForm;

    /**
     * Verification category code (FK to hemishe_h_verification_type.code)
     */
    @Column(name = "_category", length = 32)
    private String category;

    /**
     * DTM imtihon ballari
     */
    @Column(name = "points", length = 255)
    private String points;

    // =====================================================
    // Relationships (lazy loaded for performance)
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "_university", referencedColumnName = "code", insertable = false, updatable = false)
    private University universityEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "_category", referencedColumnName = "code", insertable = false, updatable = false)
    private VerificationType categoryEntity;
}
