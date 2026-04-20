package uz.hemis.domain.entity.student;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import uz.hemis.domain.type.StringToUuidType;

import java.util.UUID;

/**
 * Administrative Student 4 Entity
 *
 * Students who won awards in international olympiads, prestigious competitions and sports competitions
 *
 * Халқаро олимпиадалар, нуфузли мусобақалар ва спорт мусобақаларида
 * мукофотга сазовор бўлган талабалар тўғрисида маълумот
 */
@Entity
@Table(name = "hemishe_ri_administrative_student4")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeStudent4 extends BaseEntity {

    // FK fields - Custom type handles String<->UUID conversion for PostgreSQL
    @Column(name = "_university", nullable = false)
    @Type(StringToUuidType.class)
    private UUID university;

    @Column(name = "_education_year", nullable = false)
    @Type(StringToUuidType.class)
    private UUID educationYear;

    @Column(name = "_country")
    @Type(StringToUuidType.class)
    private UUID country;

    @Column(name = "_student")
    @Type(StringToUuidType.class)
    private UUID student;

    @Column(name = "olimpiada_type")
    private String olimpiadaType;

    @Column(name = "olimpiada_place")
    private String olimpiadaPlace;

    @Column(name = "olimpiada_name", length = 1024)
    private String olimpiadaName;

    @Column(name = "olimpiada_section_name")
    private String olimpiadaSectionName;

    @Column(name = "olimpiada_place_date")
    private String olimpiadaPlaceDate;

    @Column(name = "olimpiada_subject")
    private String olimpiadaSubject;

    @Column(name = "taken_position")
    private String takenPosition;

    @Column(name = "diploma_serial")
    private String diplomaSerial;

    @Column(name = "diploma_number")
    private String diplomaNumber;
}
