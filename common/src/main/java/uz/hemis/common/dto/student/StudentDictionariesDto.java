package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Student Dictionaries DTO - Reference data for student filters
 *
 * Purpose: Provide dropdown options for student filter selects
 * Frontend: Used in StudentsFilters panel (CustomTagFilter components)
 * Cache: 1 hour TTL (classifiers don't change often)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "courses", "studentStatuses", "paymentForms",
    "educationTypes", "educationForms", "genders"
})
@Schema(
    name = "StudentDictionaries",
    description = "Reference data for student filter dropdowns (cached for performance)"
)
public class StudentDictionariesDto {

    @Schema(
        description = "Course options (1-kurs, 2-kurs, etc.)"
    )
    private List<DictionaryItem> courses;

    @Schema(
        description = "Student status options (Aktiv, Chetlashtirilgan, etc.)"
    )
    private List<DictionaryItem> studentStatuses;

    @Schema(
        description = "Payment form options (Grant, Kontrakt)"
    )
    private List<DictionaryItem> paymentForms;

    @Schema(
        description = "Education type options (Bakalavr, Magistr, etc.)"
    )
    private List<DictionaryItem> educationTypes;

    @Schema(
        description = "Education form options (Kunduzgi, Sirtqi, etc.)"
    )
    private List<DictionaryItem> educationForms;

    @Schema(
        description = "Gender options (Erkak, Ayol)"
    )
    private List<DictionaryItem> genders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonPropertyOrder({"code", "name"})
    @Schema(
        name = "StudentDictionaryItem",
        description = "Dictionary item for student filter dropdown"
    )
    public static class DictionaryItem {

        @Schema(
            description = "Unique code/value",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String code;

        @Schema(
            description = "Display name (localized)",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String name;
    }
}
