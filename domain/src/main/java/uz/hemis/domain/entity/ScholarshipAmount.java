package uz.hemis.domain.entity;

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

    @Column(name = "summa")
    private Double summa;

    @Column(name = "local_id", length = 255)
    private String localId;
}
