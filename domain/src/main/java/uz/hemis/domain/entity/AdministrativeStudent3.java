package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * Administrative Student 3 Entity
 *
 * Bachelor's graduate employment information (within 6 months after graduation)
 *
 * Бакалавр битирувчиларининг (битирувдан кейин 6 ой ичида) иш билан
 * таъминланганлиги тўғрисида маълумот
 */
@Entity
@Table(name = "hemishe_ri_administrative_student3")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeStudent3 extends BaseEntity {

    @Column(name = "_university", nullable = false)
    private UUID university;

    @Column(name = "_education_year", nullable = false)
    private UUID educationYear;

    @Column(name = "_student")
    private UUID student;

    @Column(name = "company", length = 2048)
    private String company;

    @Column(name = "position_")
    private String position;

    @Column(name = "masters_university_name", length = 1048)
    private String mastersUniversityName;

    @Column(name = "_education_type")
    private UUID educationType;
}
