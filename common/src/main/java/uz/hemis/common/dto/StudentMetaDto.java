package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StudentMeta DTO for API JSON serialization
 *
 * <p><strong>CRITICAL - API Contract Preservation:</strong></p>
 * <ul>
 *   <li>Field names (@JsonProperty) match legacy JSON EXACTLY</li>
 *   <li>Underscore prefixes preserved (_university, _student_status, etc.)</li>
 *   <li>NO breaking changes to field names, types, or structure</li>
 *   <li>200+ universities depend on this JSON contract</li>
 * </ul>
 *
 * <p><strong>NO-RENAME • NO-DELETE • NO-BREAKING-CHANGES</strong></p>
 *
 * @since 1.0.0
 */
@Data
public class StudentMetaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * StudentMeta ID (UUID)
     * JSON field: "id"
     */
    @JsonProperty("id")
    private UUID id;

    // =====================================================
    // Business Keys
    // =====================================================

    /**
     * University-specific unique ID
     * JSON field: "uId"
     */
    @JsonProperty("uId")
    private Integer uId;

    /**
     * University code
     * JSON field: "university" (matches old-hemis)
     */
    @JsonProperty("university")
    private String university;

    /**
     * Student ID number (university internal)
     * JSON field: "studentIdNumber"
     * Note: old-hemis uses "_stdent_id_number" in DB but camelCase in JSON
     */
    @JsonProperty("studentIdNumber")
    private String studentIdNumber;

    // =====================================================
    // References (UUID)
    // =====================================================

    /**
     * Student UUID reference
     * JSON field: "student" (matches old-hemis)
     */
    @JsonProperty("student")
    private UUID student;

    /**
     * Department UUID reference
     * JSON field: "department" (matches old-hemis)
     */
    @JsonProperty("department")
    private UUID department;

    // =====================================================
    // Education Classifiers
    // =====================================================

    /**
     * Education type code
     * JSON field: "educationType" (matches old-hemis)
     */
    @JsonProperty("educationType")
    private String educationType;

    /**
     * Education form code
     * JSON field: "educationForm" (matches old-hemis)
     */
    @JsonProperty("educationForm")
    private String educationForm;

    /**
     * Semester code
     * JSON field: "semester" (matches old-hemis)
     */
    @JsonProperty("semester")
    private String semester;

    /**
     * Level code (course level)
     * JSON field: "level" (matches old-hemis)
     */
    @JsonProperty("level")
    private String level;

    /**
     * Education year code
     * JSON field: "educationYear" (matches old-hemis)
     */
    @JsonProperty("educationYear")
    private String educationYear;

    /**
     * Payment form code
     * JSON field: "paymentForm" (matches old-hemis)
     */
    @JsonProperty("paymentForm")
    private String paymentForm;

    /**
     * Student status code
     * JSON field: "studentStatus" (matches old-hemis)
     */
    @JsonProperty("studentStatus")
    private String studentStatus;

    // =====================================================
    // Group Information
    // =====================================================

    /**
     * Group ID (integer)
     * JSON field: "groupId"
     */
    @JsonProperty("groupId")
    private Integer groupId;

    /**
     * Group name
     * JSON field: "groupName"
     */
    @JsonProperty("groupName")
    private String groupName;

    /**
     * Subgroup ID
     * JSON field: "subgroupId"
     */
    @JsonProperty("subgroupId")
    private Integer subgroupId;

    /**
     * Subgroup name
     * JSON field: "subgroupName"
     */
    @JsonProperty("subgroupName")
    private String subgroupName;

    // =====================================================
    // Registration Information
    // =====================================================

    /**
     * Diploma registration status
     * JSON field: "diplomaRegistration"
     */
    @JsonProperty("diplomaRegistration")
    private Integer diplomaRegistration;

    /**
     * Employment registration status
     * JSON field: "employmentRegistration"
     */
    @JsonProperty("employmentRegistration")
    private Integer employmentRegistration;

    // =====================================================
    // Order Information
    // =====================================================

    /**
     * Order number
     * JSON field: "orderNumber"
     */
    @JsonProperty("orderNumber")
    private String orderNumber;

    /**
     * Order date
     * JSON field: "orderDate"
     */
    @JsonProperty("orderDate")
    private LocalDate orderDate;

    /**
     * Status change reason
     * JSON field: "statusChangeReason" (matches old-hemis)
     */
    @JsonProperty("statusChangeReason")
    private String statusChangeReason;

    // =====================================================
    // Speciality Information
    // =====================================================

    /**
     * Speciality code
     * JSON field: "speciality"
     */
    @JsonProperty("speciality")
    private String speciality;

    /**
     * Accreditation accepted flag
     * JSON field: "accreditationAccepted"
     */
    @JsonProperty("accreditationAccepted")
    private Boolean accreditationAccepted;

    // =====================================================
    // Decree Information
    // =====================================================

    /**
     * Decree number
     * JSON field: "decreeNumber"
     */
    @JsonProperty("decreeNumber")
    private String decreeNumber;

    /**
     * Decree name
     * JSON field: "decreeName"
     */
    @JsonProperty("decreeName")
    private String decreeName;

    /**
     * Decree date
     * JSON field: "decreeDate"
     */
    @JsonProperty("decreeDate")
    private LocalDate decreeDate;

    // =====================================================
    // Additional Classifiers
    // =====================================================

    /**
     * Academic mobile code
     * JSON field: "academicMobile" (matches old-hemis)
     */
    @JsonProperty("academicMobile")
    private String academicMobile;

    /**
     * Grant type code
     * JSON field: "grantType" (matches old-hemis)
     */
    @JsonProperty("grantType")
    private String grantType;

    /**
     * Student data contract ID
     * JSON field: "studentDataContract"
     */
    @JsonProperty("studentDataContract")
    private Integer studentDataContract;

    /**
     * Restore meta ID (for restoration tracking)
     * JSON field: "restoreMetaId"
     */
    @JsonProperty("restoreMetaId")
    private Integer restoreMetaId;

    // =====================================================
    // Status Fields
    // =====================================================

    /**
     * Active flag
     * JSON field: "active"
     */
    @JsonProperty("active")
    private Boolean active;

    // =====================================================
    // University Timestamps
    // =====================================================

    /**
     * University created at timestamp
     * JSON field: "universityCreatedAt"
     * Format: "yyyy-MM-dd HH:mm:ss.SSS" (matches old-hemis)
     */
    @JsonProperty("universityCreatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime universityCreatedAt;

    /**
     * University updated at timestamp
     * JSON field: "universityUpdatedAt"
     * Format: "yyyy-MM-dd HH:mm:ss.SSS" (matches old-hemis)
     */
    @JsonProperty("universityUpdatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime universityUpdatedAt;

    // =====================================================
    // Version (for optimistic locking)
    // =====================================================

    /**
     * Version
     * JSON field: "version"
     */
    @JsonProperty("version")
    private Integer version;
}
