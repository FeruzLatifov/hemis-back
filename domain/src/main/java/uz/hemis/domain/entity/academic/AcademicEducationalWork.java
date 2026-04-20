package uz.hemis.domain.entity.academic;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Academic Educational Work Entity
 *
 * Educational work information
 *
 * O'quv ishlari haqida ma'lumot
 */
@Entity
@Table(name = "hemishe_ri_academic_educational_work")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AcademicEducationalWork extends BaseEntity {

    // FK fields - stored as String (code reference)
    @Column(name = "_university")
    private String university;

    @Column(name = "_education_year", length = 32)
    private String educationYear;

    @Column(name = "speciality_code")
    private String specialityCode;

    @Column(name = "speciality_name", length = 1024)
    private String specialityName;

    @Column(name = "document")
    private String document;

    @Column(name = "subjects", length = 1024)
    private String subjects;

    @Column(name = "language_name")
    private String languageName;

    @Column(name = "_course", length = 32)
    private String course;

    @Column(name = "student_count")
    private Integer studentCount;
}
