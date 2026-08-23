package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * Scholarship Registry Detail DTO - detail-drawer payload for a single scholarship.
 *
 * <p>READ-ONLY. All {@link ScholarshipRowDto} fields plus classifier codes and the monthly
 * amounts child list from {@code hemishe_e_student_scholarship_amount}.</p>
 */
@Schema(name = "ScholarshipRegistryDetail", description = "Full scholarship detail for the read-only detail drawer")
public record ScholarshipDetailDto(

    @Schema(description = "Scholarship id (UUID primary key)", requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Student id (UUID)")
    String studentId,

    @Schema(description = "Student full name")
    String studentName,

    @Schema(description = "University code")
    String universityCode,

    @Schema(description = "University name")
    String universityName,

    @Schema(description = "Education year classifier code")
    String educationYear,

    @Schema(description = "Semester number")
    String semesterNumber,

    @Schema(description = "Stipend category classifier code")
    String stipendCategory,

    @Schema(description = "Stipend type classifier code")
    String stipendType,

    @Schema(description = "Payment form classifier code")
    String paymentForm,

    @Schema(description = "Decree")
    String decree,

    @Schema(description = "Start date")
    LocalDate startDate,

    @Schema(description = "End date")
    LocalDate endDate,

    @Schema(description = "Active status (derived from end_date)")
    Boolean active,

    @Schema(description = "Education type classifier code")
    String educationType,

    @Schema(description = "Education form classifier code")
    String educationForm,

    @Schema(description = "Semester classifier code")
    String semester,

    @Schema(description = "Monthly scholarship amounts")
    List<AmountItem> amounts
) {

    @Schema(name = "ScholarshipAmountItem", description = "Monthly scholarship amount row")
    public record AmountItem(

        @Schema(description = "Payment month")
        LocalDate month,

        @Schema(description = "Amount (UZS)")
        Double amount
    ) {}
}
