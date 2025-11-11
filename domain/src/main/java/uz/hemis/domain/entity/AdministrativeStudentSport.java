package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Administrative Student Sport Entity
 *
 * Student sports achievements and rankings
 *
 * Талабаларнинг спорт юутуқлари ва спорт разрядлари тўғрисида маълумот
 */
@Entity
@Table(name = "hemishe_ri_administrative_student_sport")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeStudentSport extends BaseEntity {

    @Column(name = "_university", nullable = false)
    private UUID university;

    @Column(name = "_education_year", nullable = false)
    private UUID educationYear;

    @Column(name = "_student")
    private UUID student;

    @Column(name = "_sport_type")
    private UUID sportType;

    @Column(name = "sport_date")
    private LocalDate sportDate;

    @Column(name = "sport_type_rank")
    private String sportTypeRank;

    @Column(name = "sport_type_rank_document")
    private String sportTypeRankDocument;
}
