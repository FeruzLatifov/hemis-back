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
 * Scholarship Entity
 *
 * Table: hemishe_e_student_scholarship_full
 * Purpose: Student scholarship/stipend management
 *
 * Relationships:
 * - Belongs to Student
 * - Belongs to University
 * - References Education Year, Semester, etc.
 */
@Entity
@Table(name = "hemishe_e_student_scholarship_full")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Scholarship extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "_student")
    private UUID student;

    @Column(name = "_university", length = 255)
    private String university;

    @Column(name = "_education_type", length = 32)
    private String educationType;

    @Column(name = "_education_form", length = 32)
    private String educationForm;

    @Column(name = "_payment_form", length = 32)
    private String paymentForm;

    @Column(name = "_semester", length = 32)
    private String semester;

    @Column(name = "_education_year", length = 32)
    private String educationYear;

    @Column(name = "_stipend_category", length = 32)
    private String stipendCategory;

    @Column(name = "_stipend_type", length = 32)
    private String stipendType;

    @Column(name = "decree", length = 255)
    private String decree;

    @Column(name = "group_", length = 255)
    private String group;

    @Column(name = "curriculum", length = 255)
    private String curriculum;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "local_id", length = 255)
    private String localId;

    @Column(name = "semester_number", length = 32)
    private String semesterNumber;
}
