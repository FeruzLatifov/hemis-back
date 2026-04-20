package uz.hemis.domain.entity.research;

import uz.hemis.domain.entity.base.BaseEntity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_project_meta")
@SQLRestriction("delete_ts IS NULL")
public class ProjectMeta extends BaseEntity {

    @Column(name = "_project")
    private UUID project;

    @Column(name = "fiscal_year")
    private Integer fiscalYear;

    @Column(name = "budget")
    private Double budget;

    @Column(name = "quantity_members")
    private Integer quantityMembers;

    @Column(name = "position")
    private Integer position;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "_translations", columnDefinition = "TEXT")
    private String translations;
}
