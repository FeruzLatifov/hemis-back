package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.List;

/**
 * Full duplicate group analysis for detail drawer.
 * Includes resolved classifier names and analysis summary.
 *
 * @param pinfl              the shared PINFL
 * @param fullName           representative student name
 * @param count              total records
 * @param universityCount    distinct universities
 * @param activeCount        currently active records
 * @param reason             categorization reason
 * @param reasonDescription  human-readable explanation of the problem
 * @param recommendation     suggested action
 * @param students           enrollment cards with resolved names, active first
 */
@JsonPropertyOrder({
    "pinfl", "fullName",
    "count", "universityCount", "activeCount",
    "reason", "reasonDescription", "recommendation",
    "students"
})
public record DuplicateGroupDetailDto(
        String pinfl,
        String fullName,
        int count,
        int universityCount,
        int activeCount,
        String reason,
        String reasonDescription,
        String recommendation,
        List<DuplicateStudentCard> students
) implements Serializable {}
