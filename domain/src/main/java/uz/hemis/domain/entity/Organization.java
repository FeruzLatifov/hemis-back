package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.time.LocalDateTime;

/**
 * Organization — legal entity registry (TIN UNIQUE).
 *
 * <p>Analogous to {@link Employee} (PINFL UNIQUE) for individuals.
 * One organization = one record, referenced by university_founder and other tables.</p>
 *
 * <p>World equivalents:
 * <ul>
 *   <li>SAP: Business Partner (type=ORG)</li>
 *   <li>PeopleSoft: VENDOR</li>
 * </ul></p>
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
public class Organization extends AuditableEntity {

    @Column(name = "tin", nullable = false, unique = true, length = 20)
    private String tin;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "short_name", length = 255)
    private String shortName;

    @Column(name = "opf")
    private Integer opf;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "api_raw_response", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String apiRawResponse;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
