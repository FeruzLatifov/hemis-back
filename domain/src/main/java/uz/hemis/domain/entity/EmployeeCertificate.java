package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_empoyee_certificate")
@SQLRestriction("delete_ts IS NULL")
public class EmployeeCertificate extends BaseEntity {

    @Column(name = "_university", nullable = false, length = 255)
    private String university;

    @Column(name = "_employee", nullable = false)
    private UUID employee;

    @Column(name = "_certificate_type", length = 32)
    private String certificateType;

    @Column(name = "_certificate_name", length = 32)
    private String certificateName;

    @Column(name = "_certificate_grade", length = 32)
    private String certificateGrade;

    @Column(name = "_certificate_subject", length = 32)
    private String certificateSubject;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "valid_date")
    private LocalDate validDate;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "active")
    private Boolean active;
}
