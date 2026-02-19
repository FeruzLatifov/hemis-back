package uz.hemis.common.dto.student;

import java.io.Serializable;

/**
 * Summary statistics for all specialities (directions page header cards).
 *
 * @since 2.0.0
 */
public record SpecialitySummaryDto(
    long totalSpecialities,
    long withStudents,
    long withoutStudents,
    long totalStudentsInSpecialities
) implements Serializable {}
