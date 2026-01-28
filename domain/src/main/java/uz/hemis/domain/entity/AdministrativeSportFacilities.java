package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Administrative Sport Facilities Entity
 *
 * Sport facilities area information
 *
 * Sport inshootlari maydoni haqida ma'lumot
 */
@Entity
@Table(name = "hemishe_ri_administrative_sport_facilities")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AdministrativeSportFacilities extends BaseEntity {

    // FK fields - stored as String (code reference)
    @Column(name = "_university")
    private String university;

    @Column(name = "_education_year", length = 32)
    private String educationYear;

    @Column(name = "square")
    private Double square;
}
