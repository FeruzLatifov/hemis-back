package uz.hemis.domain.entity.academic;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

/**
 * Academic Methodologic Publications Entity
 *
 * Methodological publications information
 *
 * Uslubiy nashrlar haqida ma'lumot
 */
@Entity
@Table(name = "hemishe_ri_academic_methodologic_publications")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AcademicMethodologicPublications extends BaseEntity {

    // FK fields - stored as String (code reference)
    @Column(name = "_university")
    private String university;

    @Column(name = "_education_year", length = 32)
    private String educationYear;

    @Column(name = "author_fullname", nullable = false)
    private String authorFullname;

    @Column(name = "speciality_code")
    private String specialityCode;

    @Column(name = "speciality_name")
    private String specialityName;

    @Column(name = "book_type")
    private String bookType;

    @Column(name = "book_name", length = 1024)
    private String bookName;

    @Column(name = "certificate_date")
    private LocalDate certificateDate;

    @Column(name = "certificate_number")
    private String certificateNumber;
}
