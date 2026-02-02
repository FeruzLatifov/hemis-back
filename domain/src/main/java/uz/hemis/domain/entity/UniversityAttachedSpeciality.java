package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * UniversityAttachedSpeciality Entity - hemishe_e_university_attached_speciality
 * PK: UUID (BaseEntity)
 */
@Entity
@Table(name = "hemishe_e_university_attached_speciality")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityAttachedSpeciality extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "_university", length = 255)
    private String university;

    @Column(name = "_education_form", length = 32)
    private String educationForm;

    @Column(name = "_speciality_bachelor")
    private UUID specialityBachelor;

    @Column(name = "_speciality_master")
    private UUID specialityMaster;

    @Column(name = "_speciality_ordinatura")
    private UUID specialityOrdinatura;

    @Column(name = "_speciality_doctoral")
    private UUID specialityDoctoral;

    @Column(name = "active")
    private Boolean active;
}
