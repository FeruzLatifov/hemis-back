package uz.hemis.domain.entity.finance;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

/**
 * ScholarshipAmount Entity
 *
 * Table: hemishe_e_student_scholarship_amount
 * Purpose: Monthly scholarship payment amounts
 *
 * Relationships:
 * - Belongs to Scholarship (EStudentScholarshipFull)
 */
@Entity
@Table(name = "hemishe_e_student_scholarship_amount")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipAmount extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "_student_scholarship")
    private UUID studentScholarship;

    @Column(name = "month_")
    private LocalDate month;

    /**
     * Monthly scholarship payment amount (UZS).
     *
     * <p><strong>WARNING:</strong> CUBA legacy DB column is {@code double precision}
     * (FROZEN schema — alter taqiq). Java field type tied to DB. To preserve precision,
     * caller (FinanceEntityLegacyService) parses input via {@link BigDecimal} +
     * {@code setScale(2, HALF_UP)} before {@link #setSumma(Double)} converts.</p>
     *
     * <p><strong>Future migration:</strong> Liquibase {@code ALTER COLUMN summa TYPE
     * numeric(15,2)} once 224 OTM CUBA installations are decommissioned.</p>
     */
    @Column(name = "summa")
    private Double summa;

    @Column(name = "local_id", length = 255)
    private String localId;
}
