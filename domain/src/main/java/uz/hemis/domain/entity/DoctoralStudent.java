package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Doctoral Student Entity
 *
 * Table: hemishe_e_doctoral_student
 * Purpose: PhD/Doctoral student management (education_type = '13')
 *
 * Relationships:
 * - Extends Student information
 * - Belongs to University
 * - References Scientific Advisor
 * - References Department/Cathedra
 *
 * Legacy: Maps to old-HEMIS DoctoralStudentService
 *
 * Business Rules:
 * - Only for doctoral students (education_type = '13')
 * - Requires scientific advisor assignment
 * - Dissertation defense tracking
 * - Research plan monitoring
 */
@Entity
@Table(name = "hemishe_e_doctoral_student")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctoralStudent extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "doctoral_code", length = 64, unique = true)
    private String doctoralCode;

    @Column(name = "_student")
    private UUID student;

    @Column(name = "_university", length = 64)
    private String university;

    @Column(name = "_department")
    private UUID department;

    @Column(name = "_scientific_advisor")
    private UUID scientificAdvisor;

    @Column(name = "_doctoral_student_type", length = 32)
    private String doctoralStudentType;

    @Column(name = "dissertation_topic", length = 1024)
    private String dissertationTopic;

    @Column(name = "dissertation_topic_uz", length = 1024)
    private String dissertationTopicUz;

    @Column(name = "dissertation_topic_ru", length = 1024)
    private String dissertationTopicRu;

    @Column(name = "dissertation_topic_en", length = 1024)
    private String dissertationTopicEn;

    @Column(name = "_speciality_code", length = 32)
    private String specialityCode;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "expected_defense_date")
    private LocalDate expectedDefenseDate;

    @Column(name = "actual_defense_date")
    private LocalDate actualDefenseDate;

    @Column(name = "_defense_status", length = 32)
    private String defenseStatus;

    @Column(name = "order_number", length = 128)
    private String orderNumber;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "research_direction", length = 512)
    private String researchDirection;

    @Column(name = "notes", length = 2048)
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive;

    public boolean isDefended() {
        return "DEFENDED".equals(defenseStatus) && actualDefenseDate != null;
    }
}
