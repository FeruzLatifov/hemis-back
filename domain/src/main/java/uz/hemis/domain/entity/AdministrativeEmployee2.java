package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

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
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeEmployee2 extends BaseEntity {

    // Foreign keys - bazada VARCHAR sifatida saqlangan (OLD-HEMIS compatibility)
    @Column(name = "_university")
    private String university;

    @Column(name = "_education_year")
    private String educationYear;

    @Column(name = "_employee")
    private String employee;

    @Column(name = "_country")
    private String country;

    @Column(name = "_internship_form")
    private String internshipForm;

    @Column(name = "_internship_type")
    private String internshipType;

    // Simple fields
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

    @Column(name = "subject", length = 1024)
    private String subject;
}
