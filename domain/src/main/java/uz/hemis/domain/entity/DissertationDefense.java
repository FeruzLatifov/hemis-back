package uz.hemis.domain.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_dissertation_defense")
@Where(clause = "delete_ts IS NULL")
public class DissertationDefense extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "_doctorate_student")
    private UUID doctorateStudent;

    @Column(name = "defense_date")
    private LocalDate defenseDate;

    @Lob
    @Column(name = "defense_place")
    private String defensePlace;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(name = "diploma_number")
    private String diplomaNumber;

    @Column(name = "diploma_given_date")
    private LocalDate diplomaGivenDate;

    @Lob
    @Column(name = "diploma_given_by_whom")
    private String diplomaGivenByWhom;

    @Column(name = "register_number")
    private String registerNumber;

    @Lob
    @Column(name = "filename")
    private String filename;

    @Column(name = "position")
    private Integer position;

    @Column(name = "active")
    private Boolean active;

    @Lob
    @Column(name = "translations")
    private String translations;

    @Column(name = "_speciality")
    private UUID speciality;
}
