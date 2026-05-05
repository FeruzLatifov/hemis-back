package uz.hemis.domain.entity.university;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;

/**
 * Organization — legal entity identity registry (TIN + name).
 *
 * <p>Populated from {@code /legalentity/legalentity-info/} response
 * {@code founders[].founderLegal} (api-mspd). The legalentity API only
 * returns {@code tin} and {@code name} populated for legal founders;
 * other attributes are NULL there. For richer data, callers re-query the
 * API by founder TIN on demand.</p>
 *
 * <p>Pattern: analogous to {@code employee} (PINFL UNIQUE) — minimal
 * identity registry referenced by other tables (university_founder,
 * oauth_client, ...).</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "organization")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Organization extends AuditableEntity {

    @Column(name = "tin", nullable = false, unique = true, length = 20)
    private String tin;

    @Column(name = "name", nullable = false, length = 500)
    private String name;
}
