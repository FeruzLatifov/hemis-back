package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * Speciality statistics DTO for directions page.
 * Each row represents one speciality with student count breakdown.
 *
 * @since 2.0.0
 */
@JsonPropertyOrder({
    "id", "code", "name", "educationType",
    "totalStudents", "activeStudents", "graduatedStudents", "expelledStudents"
})
public record SpecialityStatsDto(
    String id,
    String code,
    String name,
    String educationType,   // BACHELOR | MASTER | ORDINATURA
    long totalStudents,
    long activeStudents,    // status '11'
    long graduatedStudents, // status '14'
    long expelledStudents   // status '12'
) implements Serializable {}
