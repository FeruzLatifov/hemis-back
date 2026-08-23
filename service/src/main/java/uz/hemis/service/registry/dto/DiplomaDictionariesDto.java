package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Diploma Registry Dictionaries DTO - reference data for the Diplomas registry filters.
 *
 * <p>Cached ("diplomasDictionaries").</p>
 */
@Schema(name = "DiplomaDictionaries", description = "Reference data for diploma filter dropdowns (cached)")
public record DiplomaDictionariesDto(

    @Schema(description = "University options {code,name}")
    List<DictionaryItem> universities,

    @Schema(description = "Education year options {code,name}")
    List<DictionaryItem> educationYears
) {

    @Schema(name = "DiplomaDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(

        @Schema(description = "Code/value", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "Display label", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
    ) {}
}
