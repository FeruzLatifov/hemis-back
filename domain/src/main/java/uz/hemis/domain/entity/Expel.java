package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Chetlashgan Talabalar Entity (Akademik hisobotlar)
 *
 * <p>Table: hemishe_r_expel</p>
 * <p>Primary key: id (UUID)</p>
 *
 * <p>Chetlashgan talabalar hisoboti ma'lumotlari</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_r_expel")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class Expel implements Serializable, Persistable<UUID> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "university_code")
    private String universityCode;

    @Column(name = "university_name", columnDefinition = "TEXT")
    private String universityName;

    @Column(name = "faculty_code")
    private String facultyCode;

    @Column(name = "faculty_name", columnDefinition = "TEXT")
    private String facultyName;

    @Column(name = "education_type_code")
    private String educationTypeCode;

    @Column(name = "education_type_name")
    private String educationTypeName;

    @Column(name = "education_year_code")
    private String educationYearCode;

    @Column(name = "education_year_name")
    private String educationYearName;

    @Column(name = "semester_type_code")
    private String semesterTypeCode;

    @Column(name = "semester_type_name")
    private String semesterTypeName;

    @Column(name = "course_code")
    private String courseCode;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "update_date")
    private LocalDate updateDate;

    @Column(name = "expel_reason_code")
    private String expelReasonCode;

    @Column(name = "expel_reason_name")
    private String expelReasonName;

    @Column(name = "expel_count")
    private Integer expelCount;

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

    @Transient
    private boolean isNew = true;

    public boolean isDeleted() {
        return deleteTs != null;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    protected void markNotNew() {
        this.isNew = false;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (version == null) {
            version = 1;
        }
        if (createTs == null) {
            createTs = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTs = LocalDateTime.now();
    }
}
