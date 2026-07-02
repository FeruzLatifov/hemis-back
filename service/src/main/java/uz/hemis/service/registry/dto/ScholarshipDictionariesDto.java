package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Scholarship Registry Dictionaries DTO - reference data for the Scholarships registry filters.
 *
 * <p>Cached ("scholarshipsDictionaries").</p>
 */
@Schema(name = "ScholarshipDictionaries", description = "Reference data for scholarship filter dropdowns (cached)")
public record ScholarshipDictionariesDto(

    @Schema(description = "University options {code,name}")
    List<DictionaryItem> universities,

    @Schema(description = "Education year options {code,name}")
    List<DictionaryItem> educationYears
) {

    @Schema(name = "ScholarshipDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(

        @Schema(description = "Code/value", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "Display label", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
    ) {}
}
