package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_student_certificate")
@Where(clause = "delete_ts IS NULL")
public class StudentCertificate extends BaseEntity {

    @Column(name = "_university")
    private UUID university;

    @Column(name = "_student")
    private UUID student;

    @Column(name = "_certificate_type")
    private UUID certificateType;

    @Column(name = "_certificate_name")
    private UUID certificateName;

    @Column(name = "_certificate_grade")
    private UUID certificateGrade;

    @Column(name = "_certificate_subject")
    private UUID certificateSubject;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "valid_date")
    private LocalDate validDate;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "active")
    private Boolean active;
}
