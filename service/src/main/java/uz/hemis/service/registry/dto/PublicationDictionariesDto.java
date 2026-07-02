package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Scientific Publication Registry Dictionaries DTO - reference data for filters (cached).
 */
@Schema(name = "PublicationDictionaries", description = "Reference data for scientific publication filter dropdowns (cached)")
public record PublicationDictionariesDto(
    List<DictionaryItem> universities,
    List<DictionaryItem> publicationTypes
) {

    @Schema(name = "PublicationDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
