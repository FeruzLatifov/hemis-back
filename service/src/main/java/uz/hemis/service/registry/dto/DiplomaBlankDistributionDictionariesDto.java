package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Diploma Blank Distribution Dictionaries DTO — reference data for filters/form.
 *
 * <p>Cached ("diplomaBlankDistributionDictionaries").</p>
 */
@Schema(name = "DiplomaBlankDistributionDictionaries",
        description = "Reference data for diploma-blank distribution dropdowns (cached)")
public record DiplomaBlankDistributionDictionariesDto(

    @Schema(description = "University options {code,name}")
    List<DictionaryItem> universities,

    @Schema(description = "Education-year options {code,name}")
    List<DictionaryItem> educationYears,

    @Schema(description = "Education-type options {code,name}")
    List<DictionaryItem> educationTypes,

    @Schema(description = "Blank-category options {code,name}")
    List<DictionaryItem> blankCategories,

    @Schema(description = "Generate-status options {code,name}")
    List<DictionaryItem> generateStatuses
) {

    @Schema(name = "DiplomaBlankDistributionDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(

        @Schema(description = "Code/value", example = "00001", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "Display label", example = "TATU", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
    ) {}
}
