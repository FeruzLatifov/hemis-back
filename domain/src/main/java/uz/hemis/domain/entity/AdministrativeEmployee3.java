package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Administrative Employee 3 Entity
 *
 * Professors with DSc degree or professor title without scientific degree
 *
 * Хорижий олий таълим муассасаларидан DSc илмий даражаси ёки
 * илмий даражасиз профессор унвонига эга профессор-ўқитувчилар тўғрисида маълумот
 */
@Entity
@Table(name = "hemishe_ri_administrative_employee3")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeEmployee3 extends BaseEntity {

    // Foreign keys - bazada VARCHAR sifatida saqlangan (OLD-HEMIS compatibility)
    @Column(name = "_university")
    private String university;

    @Column(name = "_education_year")
    private String educationYear;

    @Column(name = "_country")
    private String country;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "work_place")
    private String workPlace;

    @Column(name = "speciality_name", length = 1024)
    private String specialityName;

    @Column(name = "subject", length = 512)
    private String subject;

    @Column(name = "contract_data", length = 512)
    private String contractData;

    @Column(name = "_employee")
    private UUID employee;

    @Column(name = "_employee_form")
    private String employeeForm;

    @Column(name = "_condution_form")
    private String condutionForm;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "lesson_time")
    private Integer lessonTime;

    @Column(name = "year_")
    private String year;
}
