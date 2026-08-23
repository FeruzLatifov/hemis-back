package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Department Dictionaries DTO - Reference data for filters
 *
 * Purpose: Provide dropdown options for department filters
 * Frontend: Used in filter panel selects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "DepartmentDictionaries",
    description = "Reference data for department filter dropdowns (cached for performance)"
)
public class DepartmentDictionariesDto {

    @Schema(
        description = "Status options for filter dropdown"
    )
    private List<DictionaryItem> statuses;

    @Schema(
        description = "Department types from database (all types, not just departments)"
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
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String code;

        @Schema(
            description = "Display label (localized)",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String label;

        @Schema(
            description = "Additional description (optional)"
        )
        private String description;
    }
}
