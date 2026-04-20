package uz.hemis.domain.entity.student;

import uz.hemis.domain.entity.base.BaseEntity;

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
@Table(name = "hemishe_e_student_certificate")
@SQLRestriction("delete_ts IS NULL")
public class StudentCertificate extends BaseEntity {

    @Column(name = "_university", length = 255)
    private String university;

    @Column(name = "_student")
    private UUID student;

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
