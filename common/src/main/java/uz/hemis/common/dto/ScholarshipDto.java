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

/**
 * Scholarship DTO
 *
 * Legacy JSON field names preserved for backward compatibility
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    @JsonProperty("scholarship_code")
    private String scholarshipCode;

    @JsonProperty("_student")
    private UUID student;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_education_year")
    private String educationYear;

    @JsonProperty("semester")
    private Integer semester;

    @JsonProperty("_scholarship_type")
    private String scholarshipType;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("payment_date")
    private LocalDate paymentDate;

    @JsonProperty("_status")
    private String status;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("order_date")
    private LocalDate orderDate;

    @JsonProperty("approved_by")
    private String approvedBy;

    @JsonProperty("_payment_method")
    private String paymentMethod;

    @JsonProperty("bank_account")
    private String bankAccount;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("transaction_ref")
    private String transactionRef;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("is_active")
    private Boolean isActive;

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
