package uz.hemis.domain.entity.finance;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;
import uz.hemis.domain.entity.university.University;

import java.time.LocalDate;

/**
 * Diploma Blank Distribution Entity — ministry-managed serial-range allocation.
 *
 * <p>Table: {@code diploma_blank_distribution} (NEW central table, V016).</p>
 *
 * <p>One row = one contiguous serial-range allocation
 * ({@code blankStartNumber}..{@code blankEndNumber}) to a single OTM
 * (universitet) for a given education year/type and blank category.</p>
 *
 * <p><strong>Central CRUD</strong> — the ministry manages this registry centrally;
 * OTMs read it via existing legacy endpoints (NO fanout / outbox / webhook).</p>
 *
 * <p>Modern schema — {@link AuditableEntity} (7 audit columns, soft-delete via
 * {@code deleted_at}). Classifier codes are stored raw; display names are resolved
 * at read time by the service layer (LEFT JOIN with raw-code fallback).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "diploma_blank_distribution")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiplomaBlankDistribution extends AuditableEntity {

    /** OTM (universitet) code — FK to {@code hemishe_e_university(code)}. */
    @Column(name = "university_code", nullable = false)
    private String universityCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_code", referencedColumnName = "code",
                insertable = false, updatable = false)
    private University university;

    /** Education-year classifier code ({@code hemishe_h_education_year}). */
    @Column(name = "education_year", length = 32)
    private String educationYear;

    /** Education-type classifier code ({@code hemishe_h_education_type}). */
    @Column(name = "education_type", length = 32)
    private String educationType;

    /** Blank-category classifier code ({@code hemishe_h_diplom_blank_category}). */
    @Column(name = "blank_category", length = 32)
    private String blankCategory;

    /** Blank series (e.g. AB). */
    @Column(name = "blank_seria", length = 32)
    private String blankSeria;

    /** Inclusive first serial number of the allocated range. */
    @Column(name = "blank_start_number")
    private Integer blankStartNumber;

    /** Inclusive last serial number of the allocated range (>= start). */
    @Column(name = "blank_end_number")
    private Integer blankEndNumber;

    /** Generation lifecycle status classifier code ({@code hemishe_h_diplom_blank_generate_status}). */
    @Column(name = "generate_status_code", length = 32)
    private String generateStatusCode;

    @Column(name = "distribution_date")
    private LocalDate distributionDate;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
