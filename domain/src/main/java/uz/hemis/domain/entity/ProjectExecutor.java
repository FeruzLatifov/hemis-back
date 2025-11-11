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
@Table(name = "hemishe_e_project_executor")
@Where(clause = "delete_ts IS NULL")
public class ProjectExecutor extends BaseEntity {

    @Column(name = "_project")
    private UUID project;

    @Column(name = "_project_executor_type")
    private UUID projectExecutorType;

    @Column(name = "id_number")
    private Integer idNumber;

    @Column(name = "outsider")
    private String outsider;

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
