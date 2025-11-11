package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Scholarship Entity
 *
 * Table: hemishe_e_scholarship
 * Purpose: Student financial aid/stipend management
 *
 * Relationships:
 * - Belongs to Student
 * - Belongs to University
 * - References Education Year
 *
 * Legacy: Maps to old-HEMIS ScholarshipService
 *
 * Business Rules:
 * - Budget students (payment_form = '11') are eligible for state scholarships
 * - Contract students (payment_form = '12') may receive scholarships if they belong to
 *   special social categories (social_category IN ('11', '12', '13', '15'))
 * - Scholarship amounts are defined by government decree and vary by education level
 */
@Entity
@Table(name = "hemishe_e_scholarship")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Scholarship extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Business Key
    // =====================================================

    /**
     * Scholarship code (unique business identifier)
     * Column: scholarship_code VARCHAR(64)
     */
    @Column(name = "scholarship_code", length = 64, unique = true)
    private String scholarshipCode;

    // =====================================================
    // References
    // =====================================================

    /**
     * Legacy field: _student (Student UUID)
     * Student receiving the scholarship
     */
    @Column(name = "_student")
    private UUID student;

    /**
     * Legacy field: _university (University code)
     * University providing the scholarship
     */
    @Column(name = "_university", length = 64)
    private String university;

    /**
     * Legacy field: _education_year (Education Year code)
     * Academic year for which scholarship is awarded
     * Example: "2024"
     */
    @Column(name = "_education_year", length = 32)
    private String educationYear;

    /**
     * Semester (1 or 2)
     */
    @Column(name = "semester")
    private Integer semester;

    // =====================================================
    // Scholarship Details
    // =====================================================

    /**
     * Scholarship type
     * Values: STATE, PRESIDENTIAL, NAMED, PRIVATE, SOCIAL
     */
    @Column(name = "_scholarship_type", length = 32)
    private String scholarshipType;

    /**
     * Scholarship amount (monthly)
     * Column: amount DECIMAL(15,2)
     */
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Start date of scholarship period
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * End date of scholarship period
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Payment date
     */
    @Column(name = "payment_date")
    private LocalDate paymentDate;

    /**
     * Scholarship status
     * Values: APPROVED, ACTIVE, SUSPENDED, CANCELLED, EXPIRED
     */
    @Column(name = "_status", length = 32)
    private String status;

    // =====================================================
    // Approval Information
    // =====================================================

    /**
     * Approval order number (University order/decree)
     */
    @Column(name = "order_number", length = 128)
    private String orderNumber;

    /**
     * Approval date
     */
    @Column(name = "order_date")
    private LocalDate orderDate;

    /**
     * Approved by (Rector name)
     */
    @Column(name = "approved_by", length = 256)
    private String approvedBy;

    // =====================================================
    // Payment Information
    // =====================================================

    /**
     * Payment method
     * Values: CASH, CARD, BANK_TRANSFER
     */
    @Column(name = "_payment_method", length = 32)
    private String paymentMethod;

    /**
     * Bank account number (for bank transfers)
     */
    @Column(name = "bank_account", length = 64)
    private String bankAccount;

    /**
     * Bank code (MFO)
     */
    @Column(name = "bank_code", length = 16)
    private String bankCode;

    /**
     * Payment transaction reference
     */
    @Column(name = "transaction_ref", length = 128)
    private String transactionRef;

    // =====================================================
    // Additional Information
    // =====================================================

    /**
     * Reason for special scholarship (if applicable)
     * E.g., "Excellent academic performance", "Social category", "Presidential quota"
     */
    @Column(name = "reason", length = 512)
    private String reason;

    /**
     * Notes
     */
    @Column(name = "notes", length = 2048)
    private String notes;

    /**
     * Is active flag
     */
    @Column(name = "is_active")
    private Boolean isActive;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if scholarship is currently active
     *
     * @return true if status is ACTIVE and within date range
     */
    public boolean isCurrentlyActive() {
        if (!"ACTIVE".equals(status) || !Boolean.TRUE.equals(isActive)) {
            return false;
        }

        LocalDate now = LocalDate.now();
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * Check if scholarship is expired
     *
     * @return true if end date has passed
     */
    public boolean isExpired() {
        return endDate != null && LocalDate.now().isAfter(endDate);
    }
}
