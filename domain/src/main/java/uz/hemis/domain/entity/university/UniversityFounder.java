package uz.hemis.domain.entity.university;

import uz.hemis.domain.entity.employee.Employee;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;
import uz.hemis.domain.entity.enums.FounderType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * University Founder — shareholders (individuals and legal entities)
 *
 * <p><strong>Table:</strong> university_founder (1:N with university)</p>
 *
 * <p>Extends AuditableEntity — soft delete via deleted_at/deleted_by.
 * Founder records are also tracked historically via is_current + effective_from/to.</p>
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
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityFounder extends AuditableEntity {

    private static final long serialVersionUID = 1L;

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
     * Founder type — stored as STRING to match DB CHECK constraint.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "founder_type", nullable = false, length = 20)
    private FounderType founderType;

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

    @Override
    public String toString() {
        return "UniversityFounder{" +
                "id=" + getId() +
                ", universityCode='" + universityCode + '\'' +
                ", founderType=" + founderType +
                ", isCurrent=" + isCurrent +
                '}';
    }
}
