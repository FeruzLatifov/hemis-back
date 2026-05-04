package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.UUID;

/**
 * Student enrollment card for duplicate analysis detail view.
 * All classifier codes are resolved to human-readable names via JOIN.
 */
@JsonPropertyOrder({
    "id", "code", "fullName",
    "universityCode", "universityName",
    "studentStatusCode", "studentStatusName", "active",
    "educationTypeCode", "educationTypeName",
    "educationFormCode", "educationFormName",
    "paymentFormCode", "paymentFormName",
    "courseCode", "courseName",
    "groupName", "educationYear", "specialityName"
})
public record DuplicateStudentCard(
        UUID id,
        String code,
        String fullName,
        String universityCode,
        String universityName,
        String studentStatusCode,
        String studentStatusName,
        boolean active,
        String educationTypeCode,
        String educationTypeName,
        String educationFormCode,
        String educationFormName,
        String paymentFormCode,
        String paymentFormName,
        String courseCode,
        String courseName,
        String groupName,
        String educationYear,
        String specialityName
) implements Serializable {}
