package uz.hemis.domain.entity.finance;

import uz.hemis.domain.entity.base.BaseEntity;
import uz.hemis.domain.entity.student.Student;
import uz.hemis.domain.entity.university.University;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Contract Entity
 *
 * Table: hemishe_e_contract
 * Purpose: Student education contract management (for contract-based students)
 *
 * Relationships:
 * - Belongs to Student
 * - Belongs to University
 * - References Education Year
 *
 * Legacy: Maps to old-HEMIS ContractService
 *
 * Business Rules:
 * - Only for contract students (payment_form = '12')
 * - Contract amount determined by government decree per education level
 * - Can be paid annually, semi-annually, or per semester
 */
@Entity
@Table(name = "hemishe_e_contract")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contract extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "contract_number", length = 128, unique = true)
    private String contractNumber;

    @Column(name = "_student")
    private UUID student;

    @Column(name = "_university", length = 64)
    private String university;

    @Column(name = "_education_year", length = 32)
    private String educationYear;

    @Column(name = "_contract_type", length = 32)
    private String contractType;

    /** Total contract amount — must be ≥ 0 (refund flow handled by separate negative-adjustment entry). */
    @Column(name = "contract_sum", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "contractSum must be non-negative")
    private BigDecimal contractSum;

    /** Paid amount — must be ≥ 0 and ≤ contractSum (enforced by {@link #validateInvariants}). */
    @Column(name = "paid_sum", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "paidSum must be non-negative")
    private BigDecimal paidSum;

    /** Remaining = contractSum - paidSum (auto-recomputed in {@link #validateInvariants}). */
    @Column(name = "remaining_sum", precision = 15, scale = 2)
    private BigDecimal remainingSum;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "_payment_form", length = 32)
    private String paymentForm;

    @Column(name = "_status", length = 32)
    private String status;

    @Column(name = "contractor_name", length = 256)
    private String contractorName;

    @Column(name = "contractor_passport", length = 64)
    private String contractorPassport;

    @Column(name = "contractor_address", length = 512)
    private String contractorAddress;

    @Column(name = "contractor_phone", length = 32)
    private String contractorPhone;

    @Column(name = "notes", length = 2048)
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive;

    public boolean isFullyPaid() {
        if (contractSum == null || paidSum == null) return false;
        return paidSum.compareTo(contractSum) >= 0;
    }

    /**
     * Money invariant guard — runs before INSERT/UPDATE.
     *
     * <p>Enforces: {@code paidSum >= 0}, {@code contractSum >= 0},
     * {@code paidSum <= contractSum}, {@code remainingSum = contractSum - paidSum}.
     * Auto-computes {@code remainingSum} if caller didn't set it (or set inconsistently).</p>
     *
     * <p>Throws {@link IllegalStateException} on violation — caught by
     * {@code @ControllerAdvice} and translated to HTTP 422.</p>
     */
    @PrePersist
    @PreUpdate
    private void validateInvariants() {
        if (contractSum == null) return;
        if (contractSum.signum() < 0) {
            throw new IllegalStateException("Contract.contractSum must be >= 0, was: " + contractSum);
        }
        if (paidSum != null) {
            if (paidSum.signum() < 0) {
                throw new IllegalStateException("Contract.paidSum must be >= 0, was: " + paidSum);
            }
            if (paidSum.compareTo(contractSum) > 0) {
                throw new IllegalStateException("Contract.paidSum (" + paidSum
                        + ") must not exceed contractSum (" + contractSum + ")");
            }
            // Auto-derive remainingSum — single source of truth.
            remainingSum = contractSum.subtract(paidSum);
        } else if (remainingSum == null) {
            remainingSum = contractSum;
        }
    }
}
