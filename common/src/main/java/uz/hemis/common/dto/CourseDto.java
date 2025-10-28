package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Course DTO - API Response/Request for Course
 *
 * <p><strong>CRITICAL - Legacy JSON Field Names:</strong></p>
 * <p>All field names with underscore prefixes MUST be preserved for backward compatibility
 * with 200+ universities using the old CUBA Platform API.</p>
 *
 * <p><strong>JSON Field Mapping (FROZEN - NO CHANGES ALLOWED):</strong></p>
 * <ul>
 *   <li>_university → _university (NOT university)</li>
 *   <li>_subject → _subject (NOT subject)</li>
 *   <li>_course_type → _course_type (NOT courseType)</li>
 *   <li>_assessment_type → _assessment_type (NOT assessmentType)</li>
 * </ul>
 *
 * <p><strong>Audit Fields:</strong> NOT included in DTO (internal only)</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * Course ID (UUID)
     * JSON: "id"
     */
    @JsonProperty("id")
    private UUID id;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Course code
     * JSON: "code"
     */
    @JsonProperty("code")
    private String code;

    /**
     * Course name
     * JSON: "name"
     */
    @JsonProperty("name")
    private String name;

    /**
     * Short name
     * JSON: "short_name"
     */
    @JsonProperty("short_name")
    private String shortName;

    // =====================================================
    // References (LEGACY FIELD NAMES WITH _)
    // =====================================================

    /**
     * University code
     * JSON: "_university" (NOT "university")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * References: hemishe_e_university.code (VARCHAR PK)
     */
    @JsonProperty("_university")
    private String university;

    /**
     * Subject ID
     * JSON: "_subject" (NOT "subject")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * References: hemishe_h_subject.id (UUID)
     */
    @JsonProperty("_subject")
    private UUID subject;

    // =====================================================
    // Academic Fields
    // =====================================================

    /**
     * Credit count
     * JSON: "credit_count"
     */
    @JsonProperty("credit_count")
    private Integer creditCount;

    /**
     * Total hours
     * JSON: "total_hours"
     */
    @JsonProperty("total_hours")
    private Integer totalHours;

    /**
     * Lecture hours
     * JSON: "lecture_hours"
     */
    @JsonProperty("lecture_hours")
    private Integer lectureHours;

    /**
     * Practice hours
     * JSON: "practice_hours"
     */
    @JsonProperty("practice_hours")
    private Integer practiceHours;

    /**
     * Lab hours
     * JSON: "lab_hours"
     */
    @JsonProperty("lab_hours")
    private Integer labHours;

    /**
     * Semester number
     * JSON: "semester"
     */
    @JsonProperty("semester")
    private Integer semester;

    // =====================================================
    // Classifiers (LEGACY FIELD NAMES WITH _)
    // =====================================================

    /**
     * Course type code
     * JSON: "_course_type" (NOT "courseType")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_course_type")
    private String courseType;

    /**
     * Assessment type code
     * JSON: "_assessment_type" (NOT "assessmentType")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * Examples: 'EXAM', 'CREDIT', 'DIFF_CREDIT'
     */
    @JsonProperty("_assessment_type")
    private String assessmentType;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Active flag
     * JSON: "active"
     */
    @JsonProperty("active")
    private Boolean active;

    /**
     * Elective course flag
     * JSON: "is_elective"
     */
    @JsonProperty("is_elective")
    private Boolean isElective;
}
