package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Faculty Dictionaries DTO - Reference data for filters
 * 
 * Purpose: Provide dropdown options for faculty filters
 * Frontend: Used in filter panel selects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "FacultyDictionaries",
    description = "Reference data for faculty filter dropdowns (cached for performance)"
)
public class FacultyDictionariesDto {

    @Schema(
        description = "Status options for filter dropdown",
        example = "[{\"code\":\"true\",\"label\":\"Active\"},{\"code\":\"false\",\"label\":\"Inactive\"}]"
    )
    private List<DictionaryItem> statuses;

    @Schema(
        description = "Department types from database (all types, not just faculties)",
        example = "[{\"code\":\"11\",\"label\":\"Fakultet\"},{\"code\":\"12\",\"label\":\"Kafedra\"}]"
    )
    private List<DictionaryItem> departmentTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(
        name = "DictionaryItem",
        description = "Generic dictionary item for dropdown options"
    )
    public static class DictionaryItem {
        
        @Schema(
            description = "Unique code/value for this option",
            example = "true",
            required = true
        )
        private String code;

        @Schema(
            description = "Display label (localized)",
            example = "Active",
            required = true
        )
        private String label;

        @Schema(
            description = "Additional description (optional)",
            example = "Active faculties only"
        )
        private String description;
    }
}
