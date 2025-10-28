package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Diploma Blank DTO
 *
 * Legacy JSON field names preserved for backward compatibility
 * External API: /app/rest/diplom-blank/get, /app/rest/diplom-blank/setStatus
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiplomaBlankDto implements Serializable {

    private String id;  // Using String for legacy compatibility (was VARCHAR in old HEMIS)

    @JsonProperty("blank_code")
    private String blankCode;

    @JsonProperty("series")
    private String series;

    @JsonProperty("number")
    private String number;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_blank_type")
    private String blankType;

    @JsonProperty("_status")
    private String status;

    @JsonProperty("received_date")
    private LocalDate receivedDate;

    @JsonProperty("issued_date")
    private LocalDate issuedDate;

    @JsonProperty("academic_year")
    private Integer academicYear;

    @JsonProperty("supplier")
    private String supplier;

    @JsonProperty("batch_number")
    private String batchNumber;

    @JsonProperty("status_reason")
    private String statusReason;

    @JsonProperty("security_features")
    private String securityFeatures;

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
