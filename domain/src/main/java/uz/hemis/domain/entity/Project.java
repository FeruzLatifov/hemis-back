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
@Table(name = "hemishe_e_project")
@Where(clause = "delete_ts IS NULL")
public class Project extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Lob
    @Column(name = "name")
    private String name;

    @Column(name = "project_number")
    private String projectNumber;

    @Column(name = "_university")
    private UUID university;

    @Column(name = "_department")
    private UUID department;

    @Column(name = "_project_type")
    private UUID projectType;

    @Column(name = "_locality")
    private UUID locality;

    @Column(name = "_project_currency")
    private UUID projectCurrency;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "position")
    private Integer position;

    @Column(name = "active")
    private Boolean active;

    @Lob
    @Column(name = "translations")
    private String translations;
}
