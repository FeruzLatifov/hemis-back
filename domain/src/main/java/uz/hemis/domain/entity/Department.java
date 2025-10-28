package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * Department (Cathedra) Entity
 *
 * Table: hemishe_e_department
 * Purpose: Academic departments/chairs within faculties
 *
 * Relationships:
 * - Belongs to Faculty
 * - Belongs to University
 * - Has Teachers
 * - Offers Courses
 *
 * Legacy: Maps to old-HEMIS Cathedra entity
 */
@Entity
@Table(name = "hemishe_e_department")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {

    @Column(name = "department_code", length = 64, unique = true)
    private String departmentCode;

    @Column(name = "department_name", length = 512)
    private String departmentName;

    @Column(name = "department_name_uz", length = 512)
    private String departmentNameUz;

    @Column(name = "department_name_ru", length = 512)
    private String departmentNameRu;

    @Column(name = "department_name_en", length = 512)
    private String departmentNameEn;

    /**
     * Legacy field: _university (University code)
     * Example: "100001" for Tashkent State University
     */
    @Column(name = "_university", length = 64)
    private String university;

    /**
     * Legacy field: _faculty (Faculty UUID)
     */
    @Column(name = "_faculty")
    private UUID faculty;

    /**
     * Department head (Teacher UUID)
     */
    @Column(name = "_head")
    private UUID head;

    /**
     * Department type
     * Values: GENERAL, SPECIALIZED, RESEARCH
     */
    @Column(name = "_department_type", length = 32)
    private String departmentType;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "room_number", length = 64)
    private String roomNumber;

    @Column(name = "building", length = 128)
    private String building;

    /**
     * Active/Inactive status
     */
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "description", length = 2048)
    private String description;

    /**
     * Ordering for display
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
}
