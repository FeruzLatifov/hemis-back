package uz.hemis.domain.entity.academic;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Academic subjects report (fanlar hisoboti).
 *
 * <p>Table: {@code hemishe_r_academic_subjects} (CUBA legacy report).</p>
 * <p>Audit columns populated via Spring Data JPA Auditing.</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_r_academic_subjects")
@SQLRestriction("delete_ts IS NULL")
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createTs;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    @LastModifiedBy
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
        if (id == null) id = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AcademicSubjects that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
