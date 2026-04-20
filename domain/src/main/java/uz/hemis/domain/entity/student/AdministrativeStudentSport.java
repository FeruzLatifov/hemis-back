package uz.hemis.domain.entity.student;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import uz.hemis.domain.type.StringToUuidType;

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
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeStudentSport extends BaseEntity {

    // FK fields - Custom type handles String<->UUID conversion for PostgreSQL
    @Column(name = "_university", nullable = false)
    @Type(StringToUuidType.class)
    private UUID university;

    @Column(name = "_education_year", nullable = false)
    @Type(StringToUuidType.class)
    private UUID educationYear;

    @Column(name = "_student")
    @Type(StringToUuidType.class)
    private UUID student;

    @Column(name = "_sport_type")
    @Type(StringToUuidType.class)
    private UUID sportType;

    @Column(name = "sport_date")
    private LocalDate sportDate;

    @Column(name = "sport_type_rank")
    private String sportTypeRank;

    @Column(name = "sport_type_rank_document")
    private String sportTypeRankDocument;
}
