package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StudentDiploma Entity - Talaba diplomlari
 *
 * <p>Table: hemishe_e_student_diploma</p>
 * <p>Primary key: id (UUID)</p>
 *
 * <p>CUBA entity: hemishe_EStudentDiploma</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_student_diploma")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class StudentDiploma implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "create_ts")
    private LocalDateTime createTs;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @Column(name = "_university", nullable = false, length = 255)
    private String university;

    @Column(name = "_student", nullable = false)
    private UUID student;

    @Column(name = "_speciality", nullable = false, length = 255)
    private String speciality;

    @Column(name = "diploma_number", nullable = false, length = 255)
    private String diplomaNumber;

    @Column(name = "register_number", length = 255)
    private String registerNumber;

    @Column(name = "register_date")
    private LocalDate registerDate;

    @Column(name = "translations", columnDefinition = "text")
    private String translations;

    @Column(name = "academic_record", columnDefinition = "text")
    private String academicRecord;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "_department", length = 255)
    private String department;

    @Column(name = "total_acload", length = 255)
    private String totalAcload;

    @Column(name = "avg_grade", length = 255)
    private String avgGrade;

    @Column(name = "speciality_name", columnDefinition = "text")
    private String specialityName;

    @Column(name = "_diplom_category", length = 32)
    private String diplomCategory;

    @Column(name = "_education_year", length = 32)
    private String educationYear;

    @Column(name = "_education_type", length = 32)
    private String educationType;

    @Column(name = "total_credit", length = 255)
    private String totalCredit;

    @Column(name = "speciality_code", length = 255)
    private String specialityCode;

    @Column(name = "tag", length = 255)
    private String tag;

    @Column(name = "verify", length = 255)
    private String verify;

    @Column(name = "hash", length = 255)
    private String hash;

    @Column(name = "blank_generate_status_code", length = 32)
    private String blankGenerateStatusCode;

    @Column(name = "study_duration")
    private Float studyDuration;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Column(name = "_admission_year", length = 32)
    private String admissionYear;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createTs = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTs = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deleteTs != null;
    }
}
