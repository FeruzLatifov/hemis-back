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
 * Fanlar Entity (Akademik hisobotlar)
 *
 * <p>Table: hemishe_r_academic_subjects</p>
 * <p>Primary key: id (UUID)</p>
 *
 * <p>Fanlar hisoboti ma'lumotlari</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_r_academic_subjects")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AcademicSubjects implements Serializable, Persistable<UUID> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "university_code")
    private String universityCode;

    @Column(name = "university_name", columnDefinition = "TEXT")
    private String universityName;

    @Column(name = "education_type_code")
    private String educationTypeCode;

    @Column(name = "education_type_name")
    private String educationTypeName;

    @Column(name = "education_year_code")
    private String educationYearCode;

    @Column(name = "education_year_name")
    private String educationYearName;

    @Column(name = "curriculum_code")
    private String curriculumCode;

    @Column(name = "curriculum_name", columnDefinition = "TEXT")
    private String curriculumName;

    @Column(name = "block_code")
    private String blockCode;

    @Column(name = "block_name")
    private String blockName;

    @Column(name = "subject_count")
    private Integer subjectCount;

    @Column(name = "update_date")
    private LocalDate updateDate;

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
