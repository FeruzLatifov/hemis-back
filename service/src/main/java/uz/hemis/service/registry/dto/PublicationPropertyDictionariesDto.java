package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Intellectual Property Registry Dictionaries DTO - reference data for filters (cached).
 *
 * <p>{@code patentTypes} is derived from the distinct raw codes present in the table
 * (no dedicated classifier reference table exists).</p>
 */
@Schema(name = "PublicationPropertyDictionaries", description = "Reference data for intellectual-property filter dropdowns (cached)")
public record PublicationPropertyDictionariesDto(
    List<DictionaryItem> universities,
    List<DictionaryItem> patentTypes
) {

    @Schema(name = "PublicationPropertyDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
