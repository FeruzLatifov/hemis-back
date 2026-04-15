package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "pinfl", nullable = false, unique = true, length = 14)
    private String pinfl;

    @Column(name = "employee_id_number", length = 50)
    private String employeeIdNumber;

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

    @Column(name = "country", length = 10)
    private String country;

    @Column(name = "passport_series", length = 10)
    private String passportSeries;

    @Column(name = "passport_number", length = 20)
    private String passportNumber;

    @Column(name = "passport_date")
    private LocalDate passportDate;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "province", length = 20)
    private String province;

    @Column(name = "district", length = 20)
    private String district;

    @Column(name = "academic_degree", length = 10)
    private String academicDegree;

    @Column(name = "academic_rank", length = 10)
    private String academicRank;

    @Column(name = "specialty", length = 500)
    private String specialty;

    @Column(name = "tin", length = 20)
    private String tin;

    @Column(name = "image", columnDefinition = "jsonb")
    private String image;

    @Column(name = "year_of_enter")
    private Integer yearOfEnter;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "source_university", length = 10)
    private String sourceUniversity;

    @Column(name = "hemis_uid")
    private String hemisUid;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
