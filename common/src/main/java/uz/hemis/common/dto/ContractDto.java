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
public class ContractDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    @JsonProperty("contract_number")
    private String contractNumber;

    @JsonProperty("_student")
    private UUID student;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_education_year")
    private String educationYear;

    @JsonProperty("_contract_type")
    private String contractType;

    @JsonProperty("contract_sum")
    private BigDecimal contractSum;

    @JsonProperty("paid_sum")
    private BigDecimal paidSum;

    @JsonProperty("remaining_sum")
    private BigDecimal remainingSum;

    @JsonProperty("contract_date")
    private LocalDate contractDate;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("_payment_form")
    private String paymentForm;

    @JsonProperty("_status")
    private String status;

    @JsonProperty("contractor_name")
    private String contractorName;

    @JsonProperty("contractor_passport")
    private String contractorPassport;

    @JsonProperty("contractor_address")
    private String contractorAddress;

    @JsonProperty("contractor_phone")
    private String contractorPhone;

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
