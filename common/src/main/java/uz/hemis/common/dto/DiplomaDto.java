package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Diploma DTO
 *
 * Legacy JSON field names preserved for backward compatibility
 * External API: /app/rest/diploma/info, /app/rest/diploma/byhash
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiplomaDto implements Serializable {

    private UUID id;

    @JsonProperty("diploma_number")
    private String diplomaNumber;

    @JsonProperty("_student")
    private UUID student;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_specialty")
    private UUID specialty;

    @JsonProperty("_diploma_blank")
    private UUID diplomaBlank;

    @JsonProperty("serial_number")
    private String serialNumber;

    @JsonProperty("_diploma_type")
    private String diplomaType;

    @JsonProperty("issue_date")
    private LocalDate issueDate;

    @JsonProperty("registration_date")
    private LocalDate registrationDate;

    @JsonProperty("graduation_year")
    private Integer graduationYear;

    @JsonProperty("qualification")
    private String qualification;

    @JsonProperty("average_grade")
    private Double averageGrade;

    @JsonProperty("_honors")
    private String honors;

    @JsonProperty("diploma_hash")
    private String diplomaHash;

    @JsonProperty("rector_name")
    private String rectorName;

    @JsonProperty("_status")
    private String status;

    @JsonProperty("qr_code")
    private String qrCode;

    @JsonProperty("verification_url")
    private String verificationUrl;

    @JsonProperty("notes")
    private String notes;

    // Audit fields
    @JsonProperty("create_ts")
    private LocalDateTime createTs;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("update_ts")
    private LocalDateTime updateTs;

    @JsonProperty("updated_by")
    private String updatedBy;
}
