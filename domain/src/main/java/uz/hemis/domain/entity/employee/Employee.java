package uz.hemis.domain.entity.employee;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.common.vo.PhoneNumber;
import uz.hemis.common.vo.Pinfl;
import uz.hemis.common.vo.Tin;
import uz.hemis.domain.converter.PhoneNumberConverter;
import uz.hemis.domain.converter.PinflConverter;
import uz.hemis.domain.converter.TinConverter;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "employee")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends AuditableEntity {

    @Column(name = "pinfl", nullable = false, unique = true, length = 14)
    @Convert(converter = PinflConverter.class)
    private Pinfl pinfl;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 2)
    private String gender;

    @Column(name = "citizenship", length = 10)
    private String citizenship;

    @Column(name = "nationality", length = 10)
    private String nationality;

    @Column(name = "passport_series", length = 10)
    private String passportSeries;

    @Column(name = "passport_number", length = 20)
    private String passportNumber;

    @Column(name = "passport_date")
    private LocalDate passportDate;

    @Column(name = "phone", length = 50)
    @Convert(converter = PhoneNumberConverter.class)
    private PhoneNumber phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /** Hierarchical SOATO code → hemishe_h_soato.code FK. */
    @Column(name = "soato_code", length = 20)
    private String soatoCode;

    @Column(name = "academic_degree", length = 10)
    private String academicDegree;

    @Column(name = "academic_rank", length = 10)
    private String academicRank;

    @Column(name = "tin", length = 20)
    @Convert(converter = TinConverter.class)
    private Tin tin;
}
