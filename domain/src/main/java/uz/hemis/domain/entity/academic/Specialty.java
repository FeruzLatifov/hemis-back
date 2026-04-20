package uz.hemis.domain.entity.academic;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Specialty Entity - Mapped to hemishe_e_university_speciality table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_university_speciality</li>
 *   <li>Primary Key: id (UUID)</li>
 *   <li>No soft delete (delete_ts column does not exist)</li>
 * </ul>
 *
 * <p>This table does NOT have BaseEntity audit columns (version, create_ts, etc.),
 * so it implements its own ID management.</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_university_speciality")
@Getter
@Setter
public class Specialty implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "_university", length = 255)
    private String university;

    @Column(name = "_education_type", length = 32)
    private String educationType;

    @Column(name = "_education_year", length = 32)
    private String educationYear;

    @Column(name = "speciality_code", length = 255)
    private String code;

    @Column(name = "speciality_name", columnDefinition = "TEXT")
    private String name;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "_faculty", length = 255)
    private String faculty;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public boolean isDeleted() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Specialty that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Specialty{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", university='" + university + '\'' +
                ", faculty=" + faculty +
                ", active=" + active +
                '}';
    }
}
