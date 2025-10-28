package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    @JsonProperty("employment_code")
    private String employmentCode;

    @JsonProperty("_student")
    private UUID student;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_diploma")
    private UUID diploma;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_tin")
    private String companyTin;

    @JsonProperty("company_address")
    private String companyAddress;

    @JsonProperty("company_phone")
    private String companyPhone;

    @JsonProperty("_employment_type")
    private String employmentType;

    @JsonProperty("position")
    private String position;

    @JsonProperty("employment_date")
    private LocalDate employmentDate;

    @JsonProperty("contract_number")
    private String contractNumber;

    @JsonProperty("contract_date")
    private LocalDate contractDate;

    @JsonProperty("salary")
    private BigDecimal salary;

    @JsonProperty("_employment_status")
    private String employmentStatus;

    @JsonProperty("termination_date")
    private LocalDate terminationDate;

    @JsonProperty("termination_reason")
    private String terminationReason;

    @JsonProperty("_soato")
    private String soato;

    @JsonProperty("_industry_code")
    private String industryCode;

    @JsonProperty("is_specialty_related")
    private Boolean isSpecialtyRelated;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("create_ts")
    private LocalDateTime createTs;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("update_ts")
    private LocalDateTime updateTs;

    @JsonProperty("updated_by")
    private String updatedBy;
}
