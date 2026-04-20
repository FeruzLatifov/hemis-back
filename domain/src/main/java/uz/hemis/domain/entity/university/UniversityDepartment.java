package uz.hemis.domain.entity.university;

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

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * University Department Entity — Bo'lim/Kafedra klassifikatori.
 *
 * <p>Table: {@code hemishe_e_university_department} (legacy CUBA schema).</p>
 * <p>Primary key: assigned VARCHAR code (not UUID) — implements {@link Persistable}
 * so Spring Data JPA picks {@code persist()} vs {@code merge()} correctly.</p>
 *
 * <p>Audit columns follow legacy CUBA naming ({@code create_ts}, {@code update_ts},
 * {@code delete_ts}) but are populated via Spring Data JPA Auditing.</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_university_department")
@SQLRestriction("delete_ts IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class UniversityDepartment implements Serializable, Persistable<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Transient
    private boolean isNew = true;

    @Id
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "name_uz")
    private String nameUz;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "university_code", length = 64)
    private String universityCode;

    @Column(name = "parent_code", length = 64)
    private String parentCode;

    @Column(name = "path")
    private String path;

    @Column(name = "_deparment_type", length = 32)
    private String departmentType;

    @Column(name = "status")
    private Boolean status;

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

    public String getName() {
        return nameUz != null ? nameUz : code;
    }

    public boolean isDeleted() {
        return deleteTs != null;
    }

    @Override
    public String getId() {
        return code;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniversityDepartment that)) return false;
        return code != null && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
