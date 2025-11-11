package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Administrative Employee 2 Entity
 *
 * Professors teaching at prestigious top-1000 universities in the world during the rating year
 *
 * Рейтинг аниқланаётган йилда жаҳоннинг нуфузли топ-1000 университетларида
 * малака ошириш курсларида таълим олган профессор-ўқитувчилар тўғрисида маълумот
 */
@Entity
@Table(name = "hemishe_ri_administrative_employee2")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeEmployee2 extends BaseEntity {

    @Column(name = "_university", nullable = false)
    private UUID university;

    @Column(name = "_education_year", nullable = false)
    private UUID educationYear;

    @Column(name = "_employee")
    private UUID employee;

    @Column(name = "_country")
    private UUID country;

    @Column(name = "foreign_university", length = 1024)
    private String foreignUniversity;

    @Column(name = "speciality_code")
    private String specialityCode;

    @Column(name = "speciality_name", length = 1024)
    private String specialityName;

    @Column(name = "training_type_name", length = 512)
    private String trainingTypeName;

    @Column(name = "training_contract", length = 512)
    private String trainingContract;

    @Column(name = "training_date_start")
    private LocalDate trainingDateStart;

    @Column(name = "training_date_end")
    private LocalDate trainingDateEnd;

    @Column(name = "year_")
    private String year;

    @Lob
    @Column(name = "subject")
    private String subject;

    @Column(name = "_internship_form")
    private UUID internshipForm;

    @Column(name = "_internship_type")
    private UUID internshipType;
}
