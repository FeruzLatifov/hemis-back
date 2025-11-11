package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * Administrative Student 2 Entity
 *
 * Foreign university academic exchange programs (by students)
 *
 * Рейтинг аниқланаётган йилда хорижий олий таълим муассасалари билан
 * академик алмашув дастурлари (талабалар томонидан) тўғрисида маълумот
 */
@Entity
@Table(name = "hemishe_ri_administrative_student2")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeStudent2 extends BaseEntity {

    @Column(name = "_university", nullable = false)
    private UUID university;

    @Column(name = "_education_year", nullable = false)
    private UUID educationYear;

    @Column(name = "exchange_document")
    private String exchangeDocument;

    @Column(name = "student_fullname")
    private String studentFullname;

    @Column(name = "_country")
    private UUID country;

    @Column(name = "exchange_university_name")
    private String exchangeUniversityName;

    @Column(name = "_education_type")
    private UUID educationType;

    @Column(name = "speciality_code")
    private String specialityCode;

    @Column(name = "speciality_name", length = 2048)
    private String specialityName;

    @Column(name = "exchange_type")
    private String exchangeType;
}
