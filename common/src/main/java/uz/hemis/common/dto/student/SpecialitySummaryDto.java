package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * Summary statistics for all specialities (directions page header cards).
 *
 * @since 2.0.0
 */
@JsonPropertyOrder({
    "totalSpecialities", "withStudents", "withoutStudents", "totalStudentsInSpecialities"
})
public record SpecialitySummaryDto(
    long totalSpecialities,
    long withStudents,
    long withoutStudents,
    long totalStudentsInSpecialities
) implements Serializable {}
