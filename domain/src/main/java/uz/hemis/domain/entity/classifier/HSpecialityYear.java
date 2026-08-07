package uz.hemis.domain.entity.classifier;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Normalized year of a speciality ({@code h_speciality_year}, V018) — 1:N child of {@link HSpeciality}.
 *
 * <p>The xlsx year field is a range ({@code 2024.2026}) or list ({@code 2023,2024,2026});
 * the ETL expands it into one row per concrete year. Years exist only for APPROVED rows.</p>
 */
@Entity
@Table(name = "h_speciality_year")
@Getter
@Setter
public class HSpecialityYear implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "speciality_id", nullable = false)
    private UUID specialityId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HSpecialityYear that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
