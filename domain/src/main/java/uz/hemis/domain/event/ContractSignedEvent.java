package uz.hemis.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Contract Signed Event
 *
 * <p>Published when a student contract is successfully signed</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Generate invoice for payment</li>
 *   <li>Send contract document to student</li>
 *   <li>Notify finance department</li>
 *   <li>Update student financial status</li>
 *   <li>Report to regulatory authorities</li>
 *   <li>Audit trail and compliance</li>
 * </ul>
 *
 * <p><strong>Event Flow:</strong></p>
 * <pre>
 * ContractService.signContract()
 *   → Validate contract terms
 *   → Save contract entity
 *   → Update student status
 *   → Publish ContractSignedEvent
 *   → Event listeners react:
 *      - InvoiceService.generateInvoice()
 *      - EmailService.sendContractCopy()
 *      - FinanceService.recordContract()
 *      - ReportingService.updateStatistics()
 * </pre>
 *
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractSignedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Contract ID
     */
    private UUID contractId;

    /**
     * Contract number (e.g., "CT-2024-001234")
     */
    private String contractNumber;

    /**
     * Student ID
     */
    private UUID studentId;

    /**
     * Student full name
     */
    private String studentFullName;

    /**
     * Student code
     */
    private String studentCode;

    /**
     * Contract type (full-paid, grant, quota, etc.)
     */
    private String contractType;

    /**
     * Total contract amount
     */
    private BigDecimal totalAmount;

    /**
     * Currency (UZS, USD, etc.)
     */
    private String currency;

    /**
     * Academic year (e.g., "2024-2025")
     */
    private String academicYear;

    /**
     * Contract start date
     */
    private LocalDate startDate;

    /**
     * Contract end date
     */
    private LocalDate endDate;

    /**
     * Sign date
     */
    private LocalDate signDate;

    /**
     * Payment schedule (monthly, semester, annual)
     */
    private String paymentSchedule;

    /**
     * Number of installments
     */
    private Integer installments;

    /**
     * Faculty ID
     */
    private UUID facultyId;

    /**
     * Faculty name
     */
    private String facultyName;

    /**
     * Specialty ID
     */
    private UUID specialtyId;

    /**
     * Specialty name
     */
    private String specialtyName;

    /**
     * Timestamp when event was created
     */
    private Instant timestamp;

    /**
     * User who signed the contract
     */
    private String signedBy;

    /**
     * Parent/Guardian name (if applicable)
     */
    private String guardianName;

    /**
     * Additional metadata (JSON)
     */
    private String metadata;
}
