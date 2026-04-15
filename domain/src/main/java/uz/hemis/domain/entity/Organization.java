package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

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
    private String apiRawResponse;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
