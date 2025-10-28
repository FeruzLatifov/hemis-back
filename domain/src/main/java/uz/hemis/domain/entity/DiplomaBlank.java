package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

/**
 * Diploma Blank Entity
 *
 * Table: hemishe_e_diploma_blank
 * Purpose: Blank diploma forms (printed forms with serial numbers)
 *
 * Workflow:
 * 1. Ministry distributes blank forms to universities
 * 2. Universities receive blanks with serial numbers
 * 3. When student graduates, blank is assigned to diploma
 * 4. Blank status changes: AVAILABLE → ASSIGNED → ISSUED
 *
 * Legacy: Maps to old-HEMIS DiplomBlank entity
 * External Access: /app/rest/diplom-blank/get, /app/rest/diplom-blank/setStatus
 */
@Entity
@Table(name = "hemishe_e_diploma_blank")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiplomaBlank extends BaseEntity {

    /**
     * Blank code (serial number on printed form)
     * Format: Series + Number, e.g., "AB 1234567"
     */
    @Column(name = "blank_code", length = 64, unique = true)
    private String blankCode;

    /**
     * Series (2-3 letters)
     * Example: "AB", "CD", "EF"
     */
    @Column(name = "series", length = 8)
    private String series;

    /**
     * Number (6-7 digits)
     * Example: "1234567"
     */
    @Column(name = "number", length = 16)
    private String number;

    /**
     * Legacy field: _university (University code)
     * University to which this blank is allocated
     */
    @Column(name = "_university", length = 64)
    private String university;

    /**
     * Blank type
     * Values: BACHELOR, MASTER, DOCTORATE, DIPLOMA, CERTIFICATE
     */
    @Column(name = "_blank_type", length = 32)
    private String blankType;

    /**
     * Blank status
     * Values: AVAILABLE, ASSIGNED, ISSUED, DAMAGED, LOST, ANNULLED
     */
    @Column(name = "_status", length = 32)
    private String status;

    /**
     * Date when blank was received by university
     */
    @Column(name = "received_date")
    private LocalDate receivedDate;

    /**
     * Date when blank was issued/assigned to diploma
     */
    @Column(name = "issued_date")
    private LocalDate issuedDate;

    /**
     * Academic year for which blank is allocated
     * Example: 2024
     */
    @Column(name = "academic_year")
    private Integer academicYear;

    /**
     * Supplier/manufacturer of blank
     */
    @Column(name = "supplier", length = 256)
    private String supplier;

    /**
     * Production batch number
     */
    @Column(name = "batch_number", length = 64)
    private String batchNumber;

    /**
     * Reason for status change (if DAMAGED, LOST, ANNULLED)
     */
    @Column(name = "status_reason", length = 512)
    private String statusReason;

    /**
     * Security features (watermark, hologram, etc.)
     */
    @Column(name = "security_features", length = 1024)
    private String securityFeatures;

    @Column(name = "notes", length = 2048)
    private String notes;
}
