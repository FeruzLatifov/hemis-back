package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bandlik Statistikasi Entity (Employment Statistics)
 *
 * <p>Table: hemishe_r_employment</p>
 * <p>Primary key: id (UUID)</p>
 *
 * <p>Bo'lim, ta'lim yili, ta'lim turi, ta'lim shakli bo'yicha
 * bitiruvchilar bandlik statistikasi</p>
 *
 * <p><strong>OLD-HEMIS Compatible:</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_REmployment</li>
 *   <li>Table: hemishe_r_employment</li>
 *   <li>100% backward compatible with CUBA Platform</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_r_employment")
@Getter
@Setter
public class REmployment implements Serializable, Persistable<UUID> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Universitet identifikatori (uId)
     */
    @Column(name = "u_id")
    private Long uId;

    /**
     * Miqdor (talabalar soni)
     */
    @Column(name = "qty")
    private Integer qty;

    /**
     * Universitet (FK)
     */
    @Column(name = "_university")
    private String universityCode;

    /**
     * Bo'lim/Kafedra (FK)
     */
    @Column(name = "_department")
    private String departmentCode;

    /**
     * Ta'lim yili (FK)
     */
    @Column(name = "_education_year")
    private String educationYearCode;

    /**
     * Ta'lim turi (FK)
     */
    @Column(name = "_education_type")
    private String educationTypeCode;

    /**
     * Ta'lim shakli (FK)
     */
    @Column(name = "_education_form")
    private String educationFormCode;

    /**
     * To'lov shakli (FK)
     */
    @Column(name = "_payment_form")
    private String paymentFormCode;

    /**
     * Jins (FK)
     */
    @Column(name = "_gender")
    private String genderCode;

    /**
     * Ish joyi mosligi (FK)
     */
    @Column(name = "_workplace_compatibility")
    private String workplaceCompatibilityCode;

    /**
     * Bitiruvchi soha turi (FK)
     */
    @Column(name = "_graduate_fields_type")
    private String graduateFieldsTypeCode;

    /**
     * Bitiruvchi nofaol turi (FK)
     */
    @Column(name = "_graduate_inactive_type")
    private String graduateInactiveTypeCode;

    // =====================================================
    // CUBA Audit Fields
    // =====================================================

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "create_ts")
    private LocalDateTime createTs;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    protected void markNotNew() {
        this.isNew = false;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (version == null) {
            version = 1;
        }
        if (createTs == null) {
            createTs = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTs = LocalDateTime.now();
    }
}
