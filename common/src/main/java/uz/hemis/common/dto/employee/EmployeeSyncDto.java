package uz.hemis.common.dto.employee;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Univer (per-OTM Yii2 e_employee row) → markaz (employee + employee_job) sync payload.
 *
 * <p>Idempotent upsert key: {@code pinfl} (PINFL — 14 digit).</p>
 * <p>Job upsert key: {@code (universityCode, sourceUid)} pair (V015 unique partial index).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "sourceUid", "pinfl", "firstName", "lastName", "middleName", "birthDate",
        "genderCode", "citizenshipCode", "nationalityCode", "academicDegreeCode", "academicRankCode",
        "passport", "phone", "email", "address",
        "departmentCode", "positionCode", "positionTypeCode",
        "startDate", "endDate", "contractNumber", "isCurrent"
})
@Schema(name = "EmployeeSyncDto", description = "Univer xodimi → markaz sync payload")
public class EmployeeSyncDto implements Serializable {

    /** Univer'ning ichki ID — idempotent upsert key (employee_job.source_uid). */
    @Size(max = 100)
    @Schema(example = "univer-e_employee-12345")
    private String sourceUid;

    /** PINFL — natural unique key. Bo'sh bo'lsa row skip. */
    @NotBlank
    @Pattern(regexp = "^\\d{14}$", message = "PINFL must be 14 digits")
    @Schema(example = "12345678901234")
    private String pinfl;

    @NotBlank @Size(max = 255)
    private String firstName;

    @NotBlank @Size(max = 255)
    private String lastName;

    @Size(max = 255)
    private String middleName;

    private LocalDate birthDate;

    @Size(max = 20) private String genderCode;
    @Size(max = 20) private String citizenshipCode;
    @Size(max = 20) private String nationalityCode;
    @Size(max = 20) private String academicDegreeCode;
    @Size(max = 20) private String academicRankCode;
    @Size(max = 20) private String passport;
    @Size(max = 50) private String phone;
    @Size(max = 255) private String email;
    private String address;

    /** Univer department code → markaz hemishe_e_university_department.code. */
    @Size(max = 255)
    private String departmentCode;

    /** Markaz h_position.code (227 ta klassifikator). Univer'da `position` integer → markazda translate. */
    @Size(max = 10)
    private String positionCode;

    /** Markaz h_position_type.code. */
    @Size(max = 10)
    private String positionTypeCode;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 100)
    private String contractNumber;

    private Boolean isCurrent;
}
