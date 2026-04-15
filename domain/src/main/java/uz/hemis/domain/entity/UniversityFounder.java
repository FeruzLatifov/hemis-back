package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * University Founder — shareholders (individuals and legal entities)
 *
 * <p><strong>Table:</strong> university_founder (1:N with university)</p>
 *
 * <p>Does NOT extend ModernBaseEntity — no soft delete.
 * Founder records are tracked historically via is_current + effective_from/to.</p>
 *
 * <p><strong>Founder types:</strong></p>
 * <ul>
 *   <li>individual — jismoniy shaxs (employee_id FK, pinfl fallback)</li>
 *   <li>legal — yuridik shaxs (organization_id FK)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_founder")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityFounder implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // =====================================================
    // University Reference
    // =====================================================

    /**
     * University code — plain column, NOT a JPA relationship.
     */
    @Column(name = "university_code", nullable = false)
    private String universityCode;

    // =====================================================
    // Founder Type
    // =====================================================

    /**
     * Founder type: 'individual' or 'legal'.
     * CHECK constraint in DB: founder_type IN ('individual', 'legal')
     */
    @Column(name = "founder_type", nullable = false, length = 20)
    private String founderType;

    // =====================================================
    // Individual Founder (jismoniy shaxs)
    // =====================================================

    /** Individual founder → employee. PINFL, FIO — employee dan JOIN. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // =====================================================
    // Legal Founder (yuridik shaxs → organization)
    // =====================================================

    /** Legal founder → organization (name, TIN from here) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    // =====================================================
    // Share Info
    // =====================================================

    @Column(name = "share_percent", precision = 5, scale = 2)
    private BigDecimal sharePercent;

    @Column(name = "share_sum")
    private Long shareSum;

    // =====================================================
    // Historical Tracking
    // =====================================================

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = true;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    // =====================================================
    // Audit Fields
    // =====================================================

    @Version
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // =====================================================
    // JPA Lifecycle Hooks
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
        if (isCurrent == null) {
            isCurrent = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Equals & HashCode (based on ID)
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniversityFounder)) return false;
        UniversityFounder that = (UniversityFounder) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UniversityFounder{" +
                "id=" + id +
                ", universityCode='" + universityCode + '\'' +
                ", founderType='" + founderType + '\'' +
                ", employee=" + (employee != null ? employee.getPinfl() : "null") +
                ", employee=" + (employee != null ? employee.getPinfl() : "null") +
                ", isCurrent=" + isCurrent +
                '}';
    }
}
