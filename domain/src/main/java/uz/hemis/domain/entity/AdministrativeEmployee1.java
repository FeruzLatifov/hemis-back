package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Administrative Employee 1 Entity
 *
 * Teachers with PhD or DSc degrees from prestigious top-1000 universities in the world
 *
 * Жаҳоннинг нуфузли топ-1000 университетларида PhD ёки DSc илмий
 * даражасига эга бўлган ўқитувчилар тўғрисида маълумот
 */
@Entity
@Table(name = "hemishe_ri_administrative_employee1")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeEmployee1 extends BaseEntity {

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

    @Column(name = "_degree")
    private UUID degree;

    @Column(name = "_rank")
    private UUID rank;

    @Column(name = "diploma_type")
    private String diplomaType;

    @Column(name = "diploma_serial_number")
    private String diplomaSerialNumber;

    @Column(name = "diploma_date")
    private LocalDate diplomaDate;

    @Column(name = "speciality_code")
    private String specialityCode;

    @Column(name = "speciality_name", length = 1024)
    private String specialityName;

    @Column(name = "council_date")
    private LocalDate councilDate;

    @Column(name = "council_number")
    private String councilNumber;
}
