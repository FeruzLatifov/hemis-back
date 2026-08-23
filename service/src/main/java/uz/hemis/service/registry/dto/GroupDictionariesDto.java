package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Group Dictionaries DTO - Reference data for Study Groups registry filters.
 *
 * <p>Purpose: Provide dropdown options (education types, education years, statuses).</p>
 * <p>Frontend: Used in filter panel selects. Cached ("groupDictionaries").</p>
 */
@Schema(
    name = "GroupDictionaries",
    description = "Reference data for study-group filter dropdowns (cached for performance)"
)
public record GroupDictionariesDto(

    @Schema(description = "Education type options for filter dropdown")
    List<DictionaryItem> educationTypes,

    @Schema(description = "Education year options for filter dropdown")
    List<DictionaryItem> educationYears,

    @Schema(description = "Status options (active/inactive) for filter dropdown")
    List<DictionaryItem> statuses
) {

    @Schema(name = "GroupDictionaryItem", description = "Generic dictionary item for dropdown options")
    public record DictionaryItem(

        @Schema(description = "Unique code/value for this option",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "Display label",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String name
    ) {}
}
