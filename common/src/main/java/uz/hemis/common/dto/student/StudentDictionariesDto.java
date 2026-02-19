package uz.hemis.common.dto.student;

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
@Schema(
    name = "StudentDictionaries",
    description = "Reference data for student filter dropdowns (cached for performance)"
)
public class StudentDictionariesDto {

    @Schema(
        description = "Course options (1-kurs, 2-kurs, etc.)",
        example = "[{\"code\":\"1\",\"name\":\"1-kurs\"}]"
    )
    private List<DictionaryItem> courses;

    @Schema(
        description = "Student status options (Aktiv, Chetlashtirilgan, etc.)",
        example = "[{\"code\":\"11\",\"name\":\"Aktiv\"}]"
    )
    private List<DictionaryItem> studentStatuses;

    @Schema(
        description = "Payment form options (Grant, Kontrakt)",
        example = "[{\"code\":\"11\",\"name\":\"Davlat granti\"},{\"code\":\"12\",\"name\":\"To'lov-kontrakt\"}]"
    )
    private List<DictionaryItem> paymentForms;

    @Schema(
        description = "Education type options (Bakalavr, Magistr, etc.)",
        example = "[{\"code\":\"11\",\"name\":\"Bakalavr\"},{\"code\":\"12\",\"name\":\"Magistr\"}]"
    )
    private List<DictionaryItem> educationTypes;

    @Schema(
        description = "Education form options (Kunduzgi, Sirtqi, etc.)",
        example = "[{\"code\":\"11\",\"name\":\"Kunduzgi\"},{\"code\":\"12\",\"name\":\"Sirtqi\"}]"
    )
    private List<DictionaryItem> educationForms;

    @Schema(
        description = "Gender options (Erkak, Ayol)",
        example = "[{\"code\":\"1\",\"name\":\"Erkak\"},{\"code\":\"2\",\"name\":\"Ayol\"}]"
    )
    private List<DictionaryItem> genders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(
        name = "StudentDictionaryItem",
        description = "Dictionary item for student filter dropdown"
    )
    public static class DictionaryItem {

        @Schema(
            description = "Unique code/value",
            example = "11",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String code;

        @Schema(
            description = "Display name (localized)",
            example = "Bakalavr",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String name;
    }
}
