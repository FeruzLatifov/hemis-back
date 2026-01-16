package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_employee_jobs")
@Where(clause = "delete_ts IS NULL")
public class EmployeeJobs extends BaseEntity {

    @Column(name = "_employee")
    private UUID employee;

    @Column(name = "_university")
    private String university;

    @Column(name = "_department")
    private String department;

    @Column(name = "_employee_type")
    private String employeeType;

    @Column(name = "_employee_position")
    private String employeePosition;

    @Column(name = "_employee_rate")
    private String employeeRate;

    @Column(name = "_employee_form")
    private String employeeForm;

    @Column(name = "_employee_status")
    private String employeeStatus;

    @Column(name = "job_start_date")
    private LocalDate jobStartDate;

    @Column(name = "job_end_date")
    private LocalDate jobEndDate;

    @Column(name = "tag")
    private String tag;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "decree_date")
    private LocalDate decreeDate;

    @Column(name = "decree_number")
    private String decreeNumber;
}
