package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Methodical Publication Registry Dictionaries DTO - reference data for filters (cached).
 */
@Schema(name = "MethodicalDictionaries", description = "Reference data for methodical publication filter dropdowns (cached)")
public record MethodicalDictionariesDto(
    List<DictionaryItem> universities,
    List<DictionaryItem> methodicalTypes
) {

    @Schema(name = "MethodicalDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
