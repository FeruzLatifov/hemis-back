package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Dissertation Defense Registry Dictionaries DTO - reference data for filters (cached).
 */
@Schema(name = "DissertationDefenseDictionaries", description = "Reference data for dissertation-defense filter dropdowns (cached)")
public record DissertationDefenseDictionariesDto(
    List<DictionaryItem> universities
) {

    @Schema(name = "DissertationDefenseDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
