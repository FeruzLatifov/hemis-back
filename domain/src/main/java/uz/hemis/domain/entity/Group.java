package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Group Entity - Mapped to hemishe_e_university_group table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_university_group (7 columns)</li>
 *   <li>Primary Key: id (UUID)</li>
 *   <li>No audit columns (create_ts, etc.)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_university_group")
@Getter
@Setter
public class Group implements Serializable {

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

    @Column(name = "group_id", length = 255)
    private String groupId;

    @Column(name = "group_name", columnDefinition = "TEXT")
    private String groupName;

    @Column(name = "active")
    private Boolean active;
}
